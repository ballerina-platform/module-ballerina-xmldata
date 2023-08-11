/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.stdlib.xmldata.compiler.object;

import io.ballerina.compiler.syntax.tree.NodeLocation;
import io.ballerina.tools.diagnostics.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to store details of record.
 *
 * @since 2.7.0
 */
public class Record {

    private final String name;
    private final Location location;
    private Boolean nameAnnotation = false;
    private final List<NodeLocation> optionalFieldLocations = new ArrayList<>();
    private final List<String> childRecordNames = new ArrayList<>();
    private final List<NodeLocation> multipleNonPrimitiveTypeLocations = new ArrayList<>();

    public Record(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void addOptionalFieldLocations(NodeLocation optionalsField) {
        optionalFieldLocations.add(optionalsField);
    }

    public List<NodeLocation> getOptionalFieldLocations() {
        return optionalFieldLocations;
    }

    public void addMultipleNonPrimitiveTypeLocations(NodeLocation optionalsField) {
        multipleNonPrimitiveTypeLocations.add(optionalsField);
    }

    public List<NodeLocation> getMultipleNonPrimitiveTypeLocations() {
        return multipleNonPrimitiveTypeLocations;
    }

    public void addChildRecordNames(String name) {
        if (!childRecordNames.contains(name)) {
            childRecordNames.add(name);
        }
    }

    public List<String> getChildRecordNames() {
        return childRecordNames;
    }

    public void setNameAnnotation() {
        nameAnnotation = true;
    }

    public boolean hasNameAnnotation() {
        return nameAnnotation;
    }

    public Location getLocation() {
        return location;
    }
}
