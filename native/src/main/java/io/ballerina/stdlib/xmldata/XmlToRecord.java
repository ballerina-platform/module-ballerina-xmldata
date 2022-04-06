/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.xmldata;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.stdlib.xmldata.utils.XmlDataUtils;
import org.ballerinalang.langlib.value.CloneWithType;

import java.util.Map;

import static io.ballerina.stdlib.xmldata.XmlToJson.toJson;

/**
 * This class converts an XML to a Ballerina record type.
 *
 * @since 2.0.2
 */
public class XmlToRecord {

    public static Object toRecord(BXml xml, boolean preserveNamespaces, BTypedesc type) {
        try {
            Object jsonObject = toJson(xml, preserveNamespaces, type.getDescribingType());
            if (jsonObject instanceof BError) {
                return XmlDataUtils.getError("xml type mismatch with record type: " +
                        ((BError) jsonObject).getErrorMessage());
            }
            Object record = CloneWithType.convert(type.getDescribingType(), jsonObject);
            if (record instanceof BError) {
                return XmlDataUtils.getError("xml type mismatch with record type: " +
                        ((Map) ((BError) record).getDetails()).get(StringUtils.fromString("message")).toString());
            }
            return record;
        } catch (Exception e) {
            return XmlDataUtils.getError("failed to convert xml to record type: " + e.getMessage());
        }
    }
}
