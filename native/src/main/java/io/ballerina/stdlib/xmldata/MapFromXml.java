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

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;

/**
 * This class converts an XML to a Ballerina record type.
 *
 * @since 2.3.0
 */
public class MapFromXml {
    private static final MapType XML_MAP_TYPE = TypeCreator.createMapType(PredefinedTypes.TYPE_XML);

    public static Object fromXml(BXml xml, BTypedesc type) {
        if (type.getDescribingType().getTag() == TypeTags.RECORD_TYPE_TAG) {
            return XmlToRecord.toRecord(xml, true, "", type);
        } else {
            return  XmlToJson.toJson(xml, true, "", type.getDescribingType());
        }
    }
}
