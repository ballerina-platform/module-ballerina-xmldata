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

package io.ballerina.stdlib.xmldata.utils;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Constants used in Ballerina XmlData library.
 *
 * @since 1.1.0
 */
public class Constants {

    private Constants() {}

    public static final String OPTIONS_ATTRIBUTE_PREFIX = "attributePrefix";
    public static final String OPTIONS_PRESERVE_NS = "preserveNamespaces";
    public static final String UNDERSCORE = "_";
    public static final String COLON = ":";
    public static final MapType JSON_MAP_TYPE = TypeCreator.createMapType(PredefinedTypes.TYPE_JSON);
    public static final ArrayType JSON_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON);
    public static final String FIELD = "$field$.";
    public static final String NAME_SPACE = "Namespace";
    public static final String URI = "uri";
    public static final String PREFIX = "prefix";
    public static final String ATTRIBUTE = "Attribute";
    public static final String SKIP_ATTRIBUTE = "skip";
    public static final String ADD_IF_HAS_ANNOTATION = "add";
    public static final int DEFAULT_TYPE_FLAG = 2049;
    public static final String NAME = "Name";
    public static final BString VALUE = StringUtils.fromString("value");
}
