package io.ballerina.stdlib.xmldata.json;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TupleType;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * Test class for json partial parsing.
 */
public class JsonToJsonTest {

    @Test
    public void fromJsonByteArrayWithType() throws JsonParser.JsonParserException {
        String str = "{\"name\":{\"fname\":\"John\", \"lname\":\"Taylor\"}," +
                " \"state\":\"CA\", \"age\":true, \"out\":\"Ushira\"}";
        HashMap<String, Field> fieldMap = new HashMap<>();
        HashMap<String, Field> fieldMap2 = new HashMap<>();
        HashMap<String, Field> fieldMap3 = new HashMap<>();

        fieldMap2.put("fname", TypeCreator.createField(PredefinedTypes.TYPE_STRING, "fname", SymbolFlags.REQUIRED));
        fieldMap2.put("lname", TypeCreator.createField(PredefinedTypes.TYPE_STRING, "lname", SymbolFlags.REQUIRED));
        RecordType r2 = TypeCreator.createRecordType("", new Module("usk", "tst"), 0,
                fieldMap2, null, false, 0);

        fieldMap.put("state", TypeCreator.createField(PredefinedTypes.TYPE_STRING, "state", SymbolFlags.REQUIRED));
        fieldMap.put("age", TypeCreator.createField(PredefinedTypes.TYPE_INT, "age", SymbolFlags.REQUIRED));
        fieldMap.put("name", TypeCreator.createField(PredefinedTypes.TYPE_JSON, "name", SymbolFlags.REQUIRED));

        RecordType r = TypeCreator.createRecordType("Rec", new Module("usk", "tst"), 0,
                fieldMap, null, false, 0);

        fieldMap3.put("name", TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED));


        ArrayType arr = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING, 3);
        TupleType tup = TypeCreator.createTupleType(Arrays.asList(PredefinedTypes.TYPE_INT, arr));
        fieldMap3.put("age", TypeCreator.createField(tup, "age", SymbolFlags.REQUIRED));

        RecordType r3 = TypeCreator.createRecordType("Rec2", new Module("usk", "tst"), 0,
                fieldMap3, PredefinedTypes.TYPE_STRING, false, 0);

        String str2 = "{\"name\":\"Ushira\", \"age\":[94, [\"home\", \"sweet\", \"home\"]]}";
        Object out = JsonToJson.fromJsonByteArrayWithType2(str.getBytes(), r);
        String f = "dd";
    }
}
