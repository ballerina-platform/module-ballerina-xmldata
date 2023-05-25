/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.stdlib.xmldata.json;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static io.ballerina.runtime.api.utils.JsonUtils.NonStringValueProcessingMode.FROM_JSON_DECIMAL_STRING;
import static io.ballerina.runtime.api.utils.JsonUtils.NonStringValueProcessingMode.FROM_JSON_FLOAT_STRING;

/**
 * This class converts an JSON using projection.
 *
 * @since 3.0.0
 */
public class JsonParser {

    private static ThreadLocal<StateMachine> tlStateMachine = new ThreadLocal<StateMachine>() {
        @Override
        public StateMachine initialValue() {
            return new StateMachine();
        }
    };

    private static Object changeForBString(Object jsonObj) {
        if (jsonObj instanceof String) {
            return StringUtils.fromString((String) jsonObj);
        }
        return jsonObj;
    }

    /**
     * Parses the contents in the given {@link Reader} and returns a json.
     *
     * @param reader reader which contains the JSON content
     * @param mode   the mode to use when processing numeric values
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(Reader reader, JsonUtils.NonStringValueProcessingMode mode, Type type)
            throws BError, JsonParserException {
        StateMachine sm = tlStateMachine.get();
        try {
            sm.setMode(mode);

            Object out = sm.execute(reader, TypeUtils.getReferredType(type));
            return out;
        } finally {
            // Need to reset the state machine before leaving. Otherwise, references to the created
            // JSON values will be maintained and the java GC will not happen properly.
            sm.reset();
        }
    }

    /**
     * Parses the contents in the given {@link Reader} with projection and returns a json.
     *
     * @param reader reader which contains the JSON content
     * @param typed typedesc of the projection type
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(Reader reader, BTypedesc typed) throws JsonParserException {
        Object out = parse(reader, JsonUtils.NonStringValueProcessingMode.FROM_JSON_STRING, typed.getDescribingType());
        return out;
    }

    public static Object parse(Reader reader, Type type) throws JsonParserException {
        return parse(reader, JsonUtils.NonStringValueProcessingMode.FROM_JSON_STRING, type);
    }

    /**
     * Represents a JSON parser related exception.
     */
    public static class JsonParserException extends Exception {
        public JsonParserException(String msg) {
            super(msg);
        }

    }

    /**
     * Represents the state machine used for JSON parsing.
     */
     static class StateMachine {

        private static final char CR = 0x000D;
        private static final char NEWLINE = 0x000A;
        private static final char HZ_TAB = 0x0009;
        private static final char SPACE = 0x0020;
        private static final char BACKSPACE = 0x0008;
        private static final char FORMFEED = 0x000C;
        private static final char QUOTES = '"';
        private static final char REV_SOL = '\\';
        private static final char SOL = '/';
        private static final char EOF = (char) -1;
        private static final String NULL = "null";
        private static final String TRUE = "true";
        private static final String FALSE = "false";

        private static final State DOC_START_STATE = new DocumentStartState();
        private static final State DOC_END_STATE = new DocumentEndState();
        static final State FIRST_FIELD_READY_STATE = new FirstFieldReadyState();
        private static final State NON_FIRST_FIELD_READY_STATE = new NonFirstFieldReadyState();
        private static final State FIELD_NAME_STATE = new FieldNameState();
        private static final State END_FIELD_NAME_STATE = new EndFieldNameState();
        private static final State FIELD_VALUE_READY_STATE = new FieldValueReadyState();
        private static final State STRING_FIELD_VALUE_STATE = new StringFieldValueState();
        private static final State NON_STRING_FIELD_VALUE_STATE = new NonStringFieldValueState();
        private static final State NON_STRING_VALUE_STATE = new NonStringValueState();
        private static final State STRING_VALUE_STATE = new StringValueState();
        private static final State FIELD_END_STATE = new FieldEndState();
        private static final State STRING_AE_ESC_CHAR_PROCESSING_STATE = new StringAEEscapedCharacterProcessingState();
        private static final State STRING_AE_PROCESSING_STATE = new StringAEProcessingState();
        private static final State FIELD_NAME_UNICODE_HEX_PROCESSING_STATE = new FieldNameUnicodeHexProcessingState();
        static final State FIRST_ARRAY_ELEMENT_READY_STATE = new FirstArrayElementReadyState();
        private static final State NON_FIRST_ARRAY_ELEMENT_READY_STATE = new NonFirstArrayElementReadyState();
        private static final State STRING_ARRAY_ELEMENT_STATE = new StringArrayElementState();
        private static final State NON_STRING_ARRAY_ELEMENT_STATE = new NonStringArrayElementState();
        private static final State ARRAY_ELEMENT_END_STATE = new ArrayElementEndState();
        private static final State STRING_FIELD_ESC_CHAR_PROCESSING_STATE =
                new StringFieldEscapedCharacterProcessingState();
        private static final State STRING_VAL_ESC_CHAR_PROCESSING_STATE =
                new StringValueEscapedCharacterProcessingState();
        private static final State FIELD_NAME_ESC_CHAR_PROCESSING_STATE =
                new FieldNameEscapedCharacterProcessingState();
        private static final State STRING_FIELD_UNICODE_HEX_PROCESSING_STATE =
                new StringFieldUnicodeHexProcessingState();
        private static final State STRING_VALUE_UNICODE_HEX_PROCESSING_STATE =
                new StringValueUnicodeHexProcessingState();
        private JsonUtils.NonStringValueProcessingMode mode = JsonUtils.NonStringValueProcessingMode.FROM_JSON_STRING;
        Type definedJsonType = PredefinedTypes.TYPE_JSON;
        ArrayType definedJsonArrayType = TypeCreator.createArrayType(definedJsonType);


        Object currentJsonNode;
        Deque<Object> nodesStack;
        Deque<String> fieldNames;

        private StringBuilder hexBuilder = new StringBuilder(4);
        private char[] charBuff = new char[1024];
        private int charBuffIndex;

        private int index;
        private int line;
        private int column;
        private char currentQuoteChar;
        Field currentField;
        Stack<Map<String, Field>> fieldHierarchy = new Stack<>();
        Stack<Type> restType = new Stack<>();
        RecordType rootRecord;
        Type rootArray;

        int jsonFieldDepth = 0;

        StateMachine() {
            reset();
        }

        public void reset() {
            this.index = 0;
            this.currentJsonNode = null;
            this.line = 1;
            this.column = 0;
            this.nodesStack = new ArrayDeque<>();
            this.fieldNames = new ArrayDeque<>();
            this.setMode(JsonUtils.NonStringValueProcessingMode.FROM_JSON_STRING);
            this.fieldHierarchy.clear();
            this.currentField = null;
            this.restType.clear();
            this.jsonFieldDepth = 0;
            this.rootRecord = null;
            this.rootArray = null;
        }

        private void setMode(JsonUtils.NonStringValueProcessingMode mode) {
            this.mode = mode;
            if (this.mode == FROM_JSON_DECIMAL_STRING) {
                definedJsonType = PredefinedTypes.TYPE_JSON_DECIMAL;
            } else if (this.mode == FROM_JSON_FLOAT_STRING) {
                definedJsonType = PredefinedTypes.TYPE_JSON_FLOAT;
            } else {
                definedJsonType = PredefinedTypes.TYPE_JSON;
            }
        }

        private static boolean isWhitespace(char ch) {
            return ch == SPACE || ch == HZ_TAB || ch == NEWLINE || ch == CR;
        }

        private static void throwExpected(String... chars) throws JsonParserException {
            throw new JsonParserException("expected '" + String.join("' or '", chars) + "'");
        }

        private void processLocation(char ch) {
            if (ch == '\n') {
                this.line++;
                this.column = 0;
            } else {
                this.column++;
            }
        }

        public Object execute(Reader reader, Type type) throws BError, JsonParserException {
            if (type.getTag() == TypeTags.RECORD_TYPE_TAG) {
                rootRecord = (RecordType) type;
                this.fieldHierarchy.push(new HashMap<>(rootRecord.getFields()));
                this.restType.push(rootRecord.getRestFieldType());
            } else if (type.getTag() == TypeTags.ARRAY_TAG || type.getTag() == TypeTags.TUPLE_TAG) {
                rootArray = type;
            } else {
                throw ErrorCreator.createError(StringUtils.fromString("incompatible type for json: " + type));
            }

            State currentState = DOC_START_STATE;
            try {
                char[] buff = new char[1024];
                int count;
                while ((count = reader.read(buff)) > 0) {
                    this.index = 0;
                    while (this.index < count) {
                        currentState = currentState.transition(this, buff, this.index, count);
                    }
                }
                return this.currentJsonNode;
            } catch (IOException e) {
                throw ErrorCreator.createError(StringUtils.fromString("Error reading JSON: " + e.getMessage()));
            } catch (JsonParserException e) {
                throw ErrorCreator.createError(StringUtils.fromString(e.getMessage() + " at line: " + this.line +
                                                       " column: " + this.column));
            }
        }

        private void append(char ch) {
            try {
                this.charBuff[this.charBuffIndex] = ch;
                this.charBuffIndex++;
            } catch (ArrayIndexOutOfBoundsException e) {
                /* this approach is faster than checking for the size by ourself */
                this.growCharBuff();
                this.charBuff[this.charBuffIndex++] = ch;
            }
        }

        private void growCharBuff() {
            char[] newBuff = new char[charBuff.length * 2];
            System.arraycopy(this.charBuff, 0, newBuff, 0, this.charBuff.length);
            this.charBuff = newBuff;
        }

        private State finalizeNonArrayObject() throws JsonParserException {
            if (jsonFieldDepth > 0) {
                this.jsonFieldDepth--;
            }
            Map<String, Field> remainingFields = this.fieldHierarchy.pop();
            this.restType.pop();
            for (Field field : remainingFields.values()) {
                if (SymbolFlags.isFlagOn(field.getFlags(), SymbolFlags.REQUIRED)) {
                    throw new JsonParserException("required field '" + field.getFieldName() + "' not present in JSON");
                }
            }
            return finalizeObject();
        }

        private State finalizeObject() throws JsonParserException {
            if (this.nodesStack.isEmpty()) {
                if (currentJsonNode instanceof BArray) {
                    currentJsonNode =
                            JsonCreator.finalizeArray(this, rootArray, (BArray) currentJsonNode);
                }
                return DOC_END_STATE;
            }

            Object parentNode = this.nodesStack.pop();

            if (TypeUtils.getReferredType(TypeUtils.getType(parentNode)).getTag() == TypeTags.RECORD_TYPE_TAG ||
                TypeUtils.getReferredType(TypeUtils.getType(parentNode)).getTag() == TypeTags.MAP_TAG) {
                if (currentJsonNode instanceof BArray) {
                    currentJsonNode =
                            JsonCreator.finalizeArray(this, currentField.getFieldType(), (BArray) currentJsonNode);
                }
                ((BMap<BString, Object>) parentNode).put(StringUtils.fromString(fieldNames.pop()),
                        currentJsonNode);
                currentJsonNode = parentNode;
                return FIELD_END_STATE;

            }
            ((BArray) parentNode).append(changeForBString(currentJsonNode));
            currentJsonNode = parentNode;
            return ARRAY_ELEMENT_END_STATE;
        }



        /**
         * A specific state in the JSON parsing state machine.
         */
        interface State {

            /**
             * Input given to the current state for a transition.
             *
             * @param sm the state machine
             * @param buff the input characters for the current state
             * @param i the location from the character should be read from
             * @param count the number of characters to read from the buffer
             * @return the new resulting state
             */
            State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException;

        }

        /**
         * Represents the JSON document start state.
         */
        private static class DocumentStartState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == '{') {
                        state = JsonCreator.initRootObject(sm);
                    } else if (ch == '[') {
                        state = JsonCreator.initRootArray(sm);
                    } else if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        sm.currentQuoteChar = ch;
                        state = STRING_VALUE_STATE;
                    } else if (ch == EOF) {
                        throw new JsonParserException("empty JSON document");
                    } else {
                        state = NON_STRING_VALUE_STATE;
                    }
                    break;
                }
                if (state == NON_STRING_VALUE_STATE) {
                    sm.index = i;
                } else {
                    sm.index = i + 1;
                }
                return state;
            }

        }

        /**
         * Represents the JSON document end state.
         */
        private static class DocumentEndState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch) || ch == EOF) {
                        state = this;
                        continue;
                    }
                    throw new JsonParserException("JSON document has already ended");
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state just before the first object field is defined.
         */
        private static class FirstFieldReadyState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == QUOTES) {
                        state = FIELD_NAME_STATE;
                        sm.currentQuoteChar = ch;
                    } else if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == '}') {
                        state = sm.finalizeNonArrayObject();
                    } else {
                        StateMachine.throwExpected("\"", "}");
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state just before the first array element is defined.
         */
        private static class FirstArrayElementReadyState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = STRING_ARRAY_ELEMENT_STATE;
                        sm.currentQuoteChar = ch;
                    } else if (ch == '{') {
                        state = JsonCreator.initNewObject(sm);
                    } else if (ch == '[') {
                        state = JsonCreator.initNewArray(sm);
                    } else if (ch == ']') {
                        state = sm.finalizeObject();
                    } else {
                        state = NON_STRING_ARRAY_ELEMENT_STATE;
                    }
                    break;
                }
                if (state == NON_STRING_ARRAY_ELEMENT_STATE) {
                    sm.index = i;
                } else {
                    sm.index = i + 1;
                }
                return state;
            }

        }

        /**
         * Represents the state just before a non-first object field is defined.
         */
        private static class NonFirstFieldReadyState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == QUOTES) {
                        sm.currentQuoteChar = ch;
                        state = FIELD_NAME_STATE;
                    } else if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else {
                        StateMachine.throwExpected("\"");
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state just before a non-first array element is defined.
         */
        private static class NonFirstArrayElementReadyState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = STRING_ARRAY_ELEMENT_STATE;
                        sm.currentQuoteChar = ch;
                    } else if (ch == '{') {
                        state = JsonCreator.initNewObject(sm);
                    } else if (ch == '[') {
                        state = JsonCreator.initNewArray(sm);
                    } else {
                        state = NON_STRING_ARRAY_ELEMENT_STATE;
                    }
                    break;
                }
                if (state == NON_STRING_ARRAY_ELEMENT_STATE) {
                    sm.index = i;
                } else {
                    sm.index = i + 1;
                }
                return state;
            }

        }

        private String value() {
            String result = new String(this.charBuff, 0, this.charBuffIndex);
            this.charBuffIndex = 0;
            return result;
        }

        private String processFieldName() {
            String value = this.value();
            this.fieldNames.push(value);
            return value;
        }

        /**
         * Represents the state during a field name.
         */
        private static class FieldNameState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                char ch;
                State state = null;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        String jsonFieldName = sm.processFieldName();
                        if (sm.jsonFieldDepth == 0) {
                            sm.currentField = sm.fieldHierarchy.peek().remove(jsonFieldName);
                        }
                        state = END_FIELD_NAME_STATE;
                    } else if (ch == REV_SOL) {
                        state = FIELD_NAME_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        sm.append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state where a field name definition has ended.
         */
        private static class EndFieldNameState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == ':') {
                        state = FIELD_VALUE_READY_STATE;
                    } else {
                        StateMachine.throwExpected(":");
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state where a field value is about to be defined.
         */
        private static class FieldValueReadyState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == QUOTES) {
                        state = STRING_FIELD_VALUE_STATE;
                        sm.currentQuoteChar = ch;
                    } else if (ch == '{') {
                        state = JsonCreator.initNewObject(sm);
                    } else if (ch == '[') {
                        state = JsonCreator.initNewArray(sm);
                    } else {
                        state = NON_STRING_FIELD_VALUE_STATE;
                    }
                    break;
                }
                if (state == NON_STRING_FIELD_VALUE_STATE) {
                    sm.index = i;
                } else {
                    sm.index = i + 1;
                }
                return state;
            }

        }

        /**
         * Represents the state during a string field value is defined.
         */
        private static class StringFieldValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        String s = sm.value();
                        if (sm.jsonFieldDepth > 0) {
                            ((BMap<BString, Object>) sm.currentJsonNode).put(
                                    StringUtils.fromString(sm.fieldNames.pop()), StringUtils.fromString(s));
                        } else if (sm.currentField != null) {
                            ((BMap<BString, Object>) sm.currentJsonNode).put(
                                    StringUtils.fromString(sm.fieldNames.pop()),
                                    StringUtils.fromString((String) JsonCreator.convertJSON(sm, s,
                                            sm.currentField.getFieldType())));
                        } else if (sm.restType.peek() != null && sm.restType.peek().getTag() != TypeTags.ANYDATA_TAG) {
                            try {
                                ((BMap<BString, Object>) sm.currentJsonNode).put(
                                        StringUtils.fromString(sm.fieldNames.pop()),
                                        StringUtils.fromString((String) JsonCreator.convertJSON(sm, s,
                                                sm.restType.peek())));
                            // this element will be ignored in projection
                            } catch (JsonParserException ignored) { }
                        }
                        state = FIELD_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_FIELD_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        sm.append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state during a string array element is defined.
         */
        private static class StringArrayElementState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        ((BArray) sm.currentJsonNode).append(changeForBString(sm.value()));
                        state = ARRAY_ELEMENT_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_AE_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        sm.append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state during a non-string field value is defined.
         */
        private static class NonStringFieldValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == '{') {
                        state = JsonCreator.initNewObject(sm);
                    } else if (ch == '[') {
                        state = JsonCreator.initNewArray(sm);
                    } else if (ch == '}') {
                        sm.processNonStringValue(ValueType.FIELD);
                        state = sm.finalizeNonArrayObject();
                    } else if (ch == ']') {
                        sm.processNonStringValue(ValueType.FIELD);
                        state = sm.finalizeObject();
                    } else if (ch == ',') {
                        sm.processNonStringValue(ValueType.FIELD);
                        state = NON_FIRST_FIELD_READY_STATE;
                    } else if (StateMachine.isWhitespace(ch)) {
                        sm.processNonStringValue(ValueType.FIELD);
                        state = FIELD_END_STATE;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        sm.append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state during a non-string array element is defined.
         */
        private static class NonStringArrayElementState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == '{') {
                        state = JsonCreator.initNewObject(sm);
                    } else if (ch == '[') {
                        state = JsonCreator.initNewArray(sm);
                    } else if (ch == ']') {
                        sm.processNonStringValue(ValueType.ARRAY_ELEMENT);
                        state = sm.finalizeObject();
                    } else if (ch == ',') {
                        sm.processNonStringValue(ValueType.ARRAY_ELEMENT);
                        state = NON_FIRST_ARRAY_ELEMENT_READY_STATE;
                    } else if (StateMachine.isWhitespace(ch)) {
                        sm.processNonStringValue(ValueType.ARRAY_ELEMENT);
                        state = ARRAY_ELEMENT_END_STATE;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        sm.append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state during a string value is defined.
         */
        private static class StringValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (ch == sm.currentQuoteChar) {
                        sm.currentJsonNode = changeForBString(sm.value());
                        state = DOC_END_STATE;
                    } else if (ch == REV_SOL) {
                        state = STRING_VAL_ESC_CHAR_PROCESSING_STATE;
                    } else if (ch == EOF) {
                        throw new JsonParserException("unexpected end of JSON document");
                    } else {
                        sm.append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        enum ValueType {
            FIELD, VALUE, ARRAY_ELEMENT
        }

        private void processNonStringValue(ValueType type) throws JsonParserException {
            String str = value();
            Type currentType = null;
            // no need to convert since arrays are handled as json[]
            if (!type.equals(ValueType.ARRAY_ELEMENT)) {
                if (currentField != null) {
                    currentType = this.currentField.getFieldType();
                // do not allow adding fields to open records without explicit rest type
                } else if (this.restType.peek() != null && this.restType.peek().getTag() != TypeTags.ANYDATA_TAG) {
                    currentType = this.restType.peek();
                } else {
                    return;
                }
            }

            if (str.indexOf('.') >= 0) {
                try {
                    double d = Double.parseDouble(str);
                    switch (mode) {
                        case FROM_JSON_FLOAT_STRING:
                            JsonCreator.setValueToJsonType(this, type, d, currentType);
                            break;
                        case FROM_JSON_DECIMAL_STRING:
                            JsonCreator.setValueToJsonType(this, type, BDecimal.valueOf(d), currentType);
                            break;
                        default:
                            if (JsonCreator.isNegativeZero(str)) {
                                JsonCreator.setValueToJsonType(this, type, d, currentType);
                            } else {
                                JsonCreator.setValueToJsonType(this, type, BDecimal.valueOf(d), currentType);
                            }
                            break;
                    }
                } catch (NumberFormatException ignore) {
                    throw new JsonParserException("unrecognized token '" + str + "'");
                }
            } else {
                char ch = str.charAt(0);
                if (ch == 't' && TRUE.equals(str)) {
                    Object convertedVal = type.equals(ValueType.ARRAY_ELEMENT) ?
                            Boolean.TRUE
                            : JsonCreator.convertJSON(this, Boolean.TRUE, currentType);
                    switch (type) {
                        case ARRAY_ELEMENT:
                            ((BArray) this.currentJsonNode).append(convertedVal);
                            break;
                        case FIELD:
                            ((BMap<BString, Object>) this.currentJsonNode).put(
                                    StringUtils.fromString(this.fieldNames.pop()), convertedVal);
                            break;
                        case VALUE:
                            currentJsonNode = convertedVal;
                            break;
                        default:
                            break;
                    }
                } else if (ch == 'f' && FALSE.equals(str)) {
                    Object convertedVal = type.equals(ValueType.ARRAY_ELEMENT) ?
                            Boolean.FALSE
                            : JsonCreator.convertJSON(this, Boolean.FALSE, currentType);
                    switch (type) {
                        case ARRAY_ELEMENT:
                            ((BArray) this.currentJsonNode).append(convertedVal);
                            break;
                        case FIELD:
                            ((BMap<BString, Object>) this.currentJsonNode).put(
                                    StringUtils.fromString(this.fieldNames.pop()), convertedVal);
                            break;
                        case VALUE:
                            currentJsonNode = convertedVal;
                            break;
                        default:
                            break;
                    }
                } else if (ch == 'n' && NULL.equals(str)) {
                    Object convertedVal = type.equals(ValueType.ARRAY_ELEMENT) ?
                            null : JsonCreator.convertJSON(this, null, currentType);
                    switch (type) {
                        case ARRAY_ELEMENT:
                            ((BArray) this.currentJsonNode).append(convertedVal);
                            break;
                        case FIELD:
                            ((BMap<BString, Object>) this.currentJsonNode).put(
                                    StringUtils.fromString(this.fieldNames.pop()), convertedVal);
                            break;
                        case VALUE:
                            currentJsonNode = convertedVal;
                            break;
                        default:
                            break;
                    }
                } else {
                    try {
                        switch (mode) {
                            case FROM_JSON_FLOAT_STRING:
                                JsonCreator.setValueToJsonType(this, type, Double.parseDouble(str), currentType);
                                break;
                            case FROM_JSON_DECIMAL_STRING:
                                JsonCreator.setValueToJsonType(this, type,
                                        BDecimal.valueOf(Double.parseDouble(str)), currentType);
                                break;
                            default:
                                if (JsonCreator.isNegativeZero(str)) {
                                    JsonCreator.setValueToJsonType(this, type, Double.parseDouble(str),
                                            currentType);
                                } else {
                                    JsonCreator.setValueToJsonType(this, type, Long.parseLong(str), currentType);
                                }
                                break;
                        }
                    } catch (NumberFormatException ignore) {
                        throw new JsonParserException("unrecognized token '" + str + "'");
                    }
                }
            }
        }

        /**
         * Represents the state during a non-string value is defined.
         */
        private static class NonStringValueState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch) || ch == EOF) {
                        sm.currentJsonNode = null;
                        sm.processNonStringValue(ValueType.VALUE);
                        state = DOC_END_STATE;
                    } else {
                        sm.append(ch);
                        state = this;
                        continue;
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state where an object field has ended.
         */
        private static class FieldEndState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == ',') {
                        state = NON_FIRST_FIELD_READY_STATE;
                    } else if (ch == '}') {
                        state = sm.finalizeNonArrayObject();
                    } else {
                        StateMachine.throwExpected(",", "}");
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state where an array element has ended.
         */
        private static class ArrayElementEndState implements State {

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if (StateMachine.isWhitespace(ch)) {
                        state = this;
                        continue;
                    } else if (ch == ',') {
                        state = NON_FIRST_ARRAY_ELEMENT_READY_STATE;
                    } else if (ch == ']') {
                        state = sm.finalizeObject();
                    } else {
                        StateMachine.throwExpected(",", "]");
                    }
                    break;
                }
                sm.index = i + 1;
                return state;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a object string field.
         */
        private static class StringFieldUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_FIELD_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from an array string field.
         */
        private static class StringAEProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_ARRAY_ELEMENT_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a string value.
         */
        private static class StringValueUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed
         * from a field name.
         */
        private static class FieldNameUnicodeHexProcessingState extends UnicodeHexProcessingState {

            @Override
            protected State getSourceState() {
                return FIELD_NAME_STATE;
            }

        }

        /**
         * Represents the state where an escaped unicode character in hex format is processed.
         */
        private abstract static class UnicodeHexProcessingState implements State {

            protected abstract State getSourceState();

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                for (; i < count; i++) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    if ((ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f')) {
                        sm.hexBuilder.append(ch);
                        if (sm.hexBuilder.length() >= 4) {
                            sm.append(this.extractUnicodeChar(sm));
                            this.reset(sm);
                            state = this.getSourceState();
                            break;
                        }
                        state = this;
                        continue;
                    }
                    this.reset(sm);
                    StateMachine.throwExpected("hexadecimal value of an unicode character");
                    break;
                }
                sm.index = i + 1;
                return state;
            }

            private void reset(StateMachine sm) {
                sm.hexBuilder.setLength(0);
            }

            private char extractUnicodeChar(StateMachine sm) {
                return StringEscapeUtils.unescapeJava("\\u" + sm.hexBuilder.toString()).charAt(0);
            }

        }

        /**
         * Represents the state where an escaped character is processed in a object string field.
         */
        private static class StringFieldEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_FIELD_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed in an array string field.
         */
        private static class StringAEEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_ARRAY_ELEMENT_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed in a string value.
         */
        private static class StringValueEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return STRING_VALUE_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed in a field name.
         */
        private static class FieldNameEscapedCharacterProcessingState extends EscapedCharacterProcessingState {

            @Override
            protected State getSourceState() {
                return FIELD_NAME_STATE;
            }

        }

        /**
         * Represents the state where an escaped character is processed.
         */
        private abstract static class EscapedCharacterProcessingState implements State {

            protected abstract State getSourceState();

            @Override
            public State transition(StateMachine sm, char[] buff, int i, int count) throws JsonParserException {
                State state = null;
                char ch;
                if (i < count) {
                    ch = buff[i];
                    sm.processLocation(ch);
                    switch (ch) {
                        case '"':
                            sm.append(QUOTES);
                            state = this.getSourceState();
                            break;
                        case '\\':
                            sm.append(REV_SOL);
                            state = this.getSourceState();
                            break;
                        case '/':
                            sm.append(SOL);
                            state = this.getSourceState();
                            break;
                        case 'b':
                            sm.append(BACKSPACE);
                            state = this.getSourceState();
                            break;
                        case 'f':
                            sm.append(FORMFEED);
                            state = this.getSourceState();
                            break;
                        case 'n':
                            sm.append(NEWLINE);
                            state = this.getSourceState();
                            break;
                        case 'r':
                            sm.append(CR);
                            state = this.getSourceState();
                            break;
                        case 't':
                            sm.append(HZ_TAB);
                            state = this.getSourceState();
                            break;
                        case 'u':
                            if (this.getSourceState() == STRING_FIELD_VALUE_STATE) {
                                state = STRING_FIELD_UNICODE_HEX_PROCESSING_STATE;
                            } else if (this.getSourceState() == STRING_VALUE_STATE) {
                                state = STRING_VALUE_UNICODE_HEX_PROCESSING_STATE;
                            } else if (this.getSourceState() == FIELD_NAME_STATE) {
                                state = FIELD_NAME_UNICODE_HEX_PROCESSING_STATE;
                            } else if (this.getSourceState() == STRING_ARRAY_ELEMENT_STATE) {
                                state = STRING_AE_PROCESSING_STATE;
                            } else {
                                throw new JsonParserException("unknown source '" + this.getSourceState() +
                                        "' in escape char processing state");
                            }
                            break;
                        default:
                            StateMachine.throwExpected("escaped characters");
                    }
                }
                sm.index = i + 1;
                return state;
            }

        }
    }
}
