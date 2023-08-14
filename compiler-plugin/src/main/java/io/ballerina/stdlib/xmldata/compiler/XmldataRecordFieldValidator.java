/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.stdlib.xmldata.compiler;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NilTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.stdlib.xmldata.compiler.object.Record;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Xmldata record field analyzer.
 */
public class XmldataRecordFieldValidator implements AnalysisTask<SyntaxNodeAnalysisContext> {
    private final Map<String, Record> records = new HashMap<>();
    private final Map<String, Location> recordNamesUsedInFunction = new HashMap<>();
    private final List<String> validatedRecords = new ArrayList<>();
    private static final String TO_RECORD = "xmldata:toRecord";
    private static final String FROM_XML = "xmldata:fromXml";
    private static final String NAME_ANNOTATION = "xmldata:Name";

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {
        List<Diagnostic> diagnostics = ctx.semanticModel().diagnostics();
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                return;
            }
        }

        ModulePartNode rootNode = (ModulePartNode) ctx.node();
        for (ModuleMemberDeclarationNode member : rootNode.members()) {
            if (member instanceof FunctionDefinitionNode) {
                processFunctionDefinitionNode((FunctionDefinitionNode) member);
            } else if (member instanceof ModuleVariableDeclarationNode) {
                processModuleVariableDeclarationNode((ModuleVariableDeclarationNode) member);
            } else if (member instanceof TypeDefinitionNode) {
                processTypeDefinitionNode((TypeDefinitionNode) member);
            }
        }
        for (Map.Entry<String, Location> entry : this.recordNamesUsedInFunction.entrySet()) {
            if (this.records.containsKey(entry.getKey())) {
                validateRecord(ctx, this.records.get(entry.getKey()), entry.getValue());
            }
        }
    }

    private void processFunctionDefinitionNode(FunctionDefinitionNode functionDefinitionNode) {
        ChildNodeList childNodeList = functionDefinitionNode.functionBody().children();
        for (Node node : childNodeList) {
            if (node instanceof VariableDeclarationNode) {
                VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) node;
                Optional<ExpressionNode> initializer = variableDeclarationNode.initializer();
                if (initializer.isPresent()) {
                    if (isValidFunctionName(initializer.get())) {
                        addRecordName(variableDeclarationNode.typedBindingPattern().typeDescriptor());
                    }
                }
            }
        }
    }

    private void processModuleVariableDeclarationNode(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        Optional<ExpressionNode> initializer = moduleVariableDeclarationNode.initializer();
        if (initializer.isPresent()) {
            if (isValidFunctionName(initializer.get())) {
                addRecordName(moduleVariableDeclarationNode.typedBindingPattern().typeDescriptor());
            }
        }
    }

    private boolean isValidFunctionName(ExpressionNode expressionNode) {
        if (expressionNode instanceof CheckExpressionNode) {
            expressionNode = ((CheckExpressionNode) expressionNode).expression();
        }
        if (expressionNode instanceof FunctionCallExpressionNode) {
            FunctionCallExpressionNode functionCallExpressionNode =
                    (FunctionCallExpressionNode) expressionNode;
            String functionName = functionCallExpressionNode.functionName().toSourceCode().trim();
            return functionName.equals(TO_RECORD) || functionName.equals(FROM_XML);
        }
        return false;
    }

    private void addRecordName(TypeDescriptorNode typeDescriptor) {
        if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            String returnTypeName = ((SimpleNameReferenceNode) typeDescriptor).name().text().trim();
            if (!this.recordNamesUsedInFunction.containsKey(returnTypeName)) {
                this.recordNamesUsedInFunction.put(returnTypeName, typeDescriptor.location());
            }
        }
    }

    private void processTypeDefinitionNode(TypeDefinitionNode typeDefinitionNode) {
        Node typeDescriptor = typeDefinitionNode.typeDescriptor();
        if (typeDescriptor instanceof RecordTypeDescriptorNode) {
            RecordTypeDescriptorNode recordTypeDescriptorNode = (RecordTypeDescriptorNode) typeDescriptor;
            Record record = new Record(typeDefinitionNode.typeName().text(), typeDefinitionNode.location());
            typeDefinitionNode.metadata().ifPresent(metadataNode -> {
                NodeList<AnnotationNode> annotations = metadataNode.annotations();
                for (AnnotationNode annotationNode : annotations) {
                    if (annotationNode.annotReference().toSourceCode().trim().equals(NAME_ANNOTATION)) {
                        record.setNameAnnotation();
                    }
                }
            });
            for (Node field : recordTypeDescriptorNode.fields()) {
                Node type;
                if (field instanceof RecordFieldNode) {
                    RecordFieldNode recordFieldNode = (RecordFieldNode) field;
                    type = recordFieldNode.typeName();
                    processFieldType(type, record);
                } else if (field instanceof RecordFieldWithDefaultValueNode) {
                    RecordFieldWithDefaultValueNode recordFieldNode = (RecordFieldWithDefaultValueNode) field;
                    type = recordFieldNode.typeName();
                    processFieldType(type, record);
                }
            }
            this.records.put(record.getName().trim(), record);
        }
    }

    private void processFieldType(Node type, Record record) {
        if (type instanceof OptionalTypeDescriptorNode) {
            record.addOptionalFieldLocations(type.location());
            type = ((OptionalTypeDescriptorNode) type).typeDescriptor();
        }
        if (type instanceof NilTypeDescriptorNode) {
            record.addOptionalFieldLocations(type.location());
        }
        if (type instanceof UnionTypeDescriptorNode) {
            processUnionType((UnionTypeDescriptorNode) type, 0, record, type);
        }
        if (type instanceof ArrayTypeDescriptorNode) {
            type = ((ArrayTypeDescriptorNode) type).memberTypeDesc();
        }
        if (type instanceof SimpleNameReferenceNode) {
            SimpleNameReferenceNode simpleNameReferenceNode = (SimpleNameReferenceNode) type;
            record.addChildRecordNames(simpleNameReferenceNode.name().text().trim());
        }
    }

    private void processUnionType(UnionTypeDescriptorNode unionTypeDescriptorNode, int noOfSimpleNamesType,
                                  Record record, Node type) {
        for (Node unionType : unionTypeDescriptorNode.children()) {
            if (unionType instanceof UnionTypeDescriptorNode) {
                processUnionType((UnionTypeDescriptorNode) unionType, noOfSimpleNamesType, record, type);
            }
            if (unionType instanceof OptionalTypeDescriptorNode) {
                record.addOptionalFieldLocations(unionType.location());
                unionType = ((OptionalTypeDescriptorNode) unionType).typeDescriptor();
            }
            if (unionType instanceof NilTypeDescriptorNode) {
                record.addOptionalFieldLocations(unionType.location());
            }
            if (unionType instanceof ArrayTypeDescriptorNode) {
                unionType = ((ArrayTypeDescriptorNode) unionType).memberTypeDesc();
            }
            if (unionType instanceof SimpleNameReferenceNode) {
                noOfSimpleNamesType++;
                SimpleNameReferenceNode simpleNameReferenceNode = (SimpleNameReferenceNode) unionType;
                record.addChildRecordNames(simpleNameReferenceNode.name().text().trim());
            }
        }
        if (noOfSimpleNamesType > 1) {
            record.addMultipleNonPrimitiveTypeLocations(type.location());
        }
    }

    private void validateRecord(SyntaxNodeAnalysisContext ctx, Record record, Location declarationLocation) {
        this.validatedRecords.add(record.getName());
        for (Location location : record.getMultipleNonPrimitiveTypeLocations()) {
            reportDiagnosticInfo(ctx, location, DiagnosticsCodes.XMLDATA_102);
        }
        for (Location location : record.getOptionalFieldLocations()) {
            reportDiagnosticInfo(ctx, location, DiagnosticsCodes.XMLDATA_101);
        }
        for (String childRecordName : record.getChildRecordNames()) {
            if (!this.validatedRecords.contains(childRecordName)) {
                Record childRecord =  this.records.get(childRecordName);
                validateRecord(ctx, childRecord, declarationLocation);
                if (childRecord.hasNameAnnotation() && !recordNamesUsedInFunction.containsKey(childRecordName.trim())) {
                    reportDiagnosticInfo(ctx, declarationLocation, DiagnosticsCodes.XMLDATA_103);
                }
            }
        }
    }

    private void reportDiagnosticInfo(SyntaxNodeAnalysisContext ctx, Location location,
                                      DiagnosticsCodes diagnosticsCodes) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(diagnosticsCodes.getCode(),
                diagnosticsCodes.getMessage(), diagnosticsCodes.getSeverity());
        ctx.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, location));
    }
}
