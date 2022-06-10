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

import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Xmldata record field analyzer.
 */
public class XmldataRecordFieldValidator implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private final List<RecordFieldNode> recordNodes = new ArrayList<>();
    private final List<RecordFieldNode> validatedRecordNodes = new ArrayList<>();
    private static final String STRING = "string";
    private static final String DECIMAL = "decimal";
    private static final String FLOAT = "float";
    private static final String BOOLEAN = "boolean";
    private static final String INT = "int";
    private static final String QUESTION_MARK = "?";
    private static final String TO_RECORD = "xmldata:toRecord";
    private static final String FROM_XML = "xmldata:fromXml";
    private static final String VERTICAL_BAR = "|";
    private static final String SQUARE_BRACKET = "[]";
    private static final String BRACKET = "[";

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {
        List<Diagnostic> diagnostics = ctx.semanticModel().diagnostics();
        for (Diagnostic diagnostic : diagnostics) {
            if (diagnostic.diagnosticInfo().severity() == DiagnosticSeverity.ERROR) {
                return;
            }
        }
        Node node = ctx.node();
        if (node instanceof RecordFieldNode) {
            this.recordNodes.add((RecordFieldNode) node);
        }
        if (node instanceof VariableDeclarationNode) {
            VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) node;
            Optional<ExpressionNode> initializer = variableDeclarationNode.initializer();
            if (!initializer.isEmpty()) {
                ExpressionNode expressionNode = initializer.get();
                if (expressionNode.toString().contains(TO_RECORD) || expressionNode.toString().contains(FROM_XML)) {
                    TypedBindingPatternNode typedBindingPatternNode = variableDeclarationNode.typedBindingPattern();
                    checkRecordField(typedBindingPatternNode.typeDescriptor().toString(), ctx);
                }
            }
        }
        if (node instanceof ModuleVariableDeclarationNode) {
            ModuleVariableDeclarationNode moduleVariableDeclarationNode = (ModuleVariableDeclarationNode) node;
            Optional<ExpressionNode> initializer = moduleVariableDeclarationNode.initializer();
            if (!initializer.isEmpty()) {
                ExpressionNode expressionNode = initializer.get();
                if (expressionNode.toString().contains(TO_RECORD) || expressionNode.toString().contains(FROM_XML)) {
                    TypedBindingPatternNode typedBindingPatternNode =
                            moduleVariableDeclarationNode.typedBindingPattern();
                    checkRecordField(typedBindingPatternNode.typeDescriptor().toString(), ctx);
                }
            }
        }
    }

    private void checkRecordField(String recordName, SyntaxNodeAnalysisContext ctx) {
        for (RecordFieldNode recordFieldNode: this.recordNodes) {
            if (!this.validatedRecordNodes.contains(recordFieldNode)) {
                String recordNameOfField = recordFieldNode.parent().parent().children().get(1).toString().trim();
                String filedType = recordFieldNode.typeName().toString().trim();
                if (recordNameOfField.equals(recordName.trim())) {
                    this.validatedRecordNodes.add(recordFieldNode);
                    if (filedType.contains(QUESTION_MARK)) {
                        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(DiagnosticsCodes.XMLDATA_101.getCode(),
                                DiagnosticsCodes.XMLDATA_101.getMessage(), DiagnosticsCodes.XMLDATA_101.getSeverity());
                        ctx.reportDiagnostic(
                                DiagnosticFactory.createDiagnostic(diagnosticInfo, recordFieldNode.location()));
                        if (filedType.contains(VERTICAL_BAR)) {
                            String[] types = filedType.split("\\" + VERTICAL_BAR);
                            for (String type : types) {
                                type = type.trim();
                                if (type.contains(QUESTION_MARK)) {
                                    if (isNonPrimitiveType(type)) {
                                        checkRecordFiled(type, type.length() - 1, ctx);
                                    }
                                } else {
                                    if (isNonPrimitiveType(type)) {
                                        checkRecordFiled(type, type.length(), ctx);
                                    }
                                }
                            }
                        } else if (isNonPrimitiveType(filedType)) {
                            checkRecordFiled(filedType, filedType.length() - 1, ctx);
                        }
                    } else {
                        if (filedType.contains(VERTICAL_BAR)) {
                            String[] types = filedType.split("\\" + VERTICAL_BAR);
                            for (String type : types) {
                                type = type.trim();
                                if (isNonPrimitiveType(type)) {
                                    checkRecordFiled(type, type.length(), ctx);
                                }
                            }
                        } else if (isNonPrimitiveType(filedType)) {
                            checkRecordFiled(filedType, filedType.length(), ctx);
                        }
                    }
                }
            }
        }
    }

    private void checkRecordFiled(String filedType, int endIndex, SyntaxNodeAnalysisContext ctx) {
        if (filedType.contains(SQUARE_BRACKET)) {
            checkRecordField(filedType.substring(0, filedType.indexOf(BRACKET)), ctx);
        } else {
            checkRecordField(filedType.substring(0, endIndex), ctx);
        }
    }

    private boolean isNonPrimitiveType(String typeName) {
        return !(typeName.contains(STRING) || typeName.contains(INT) || typeName.contains(DECIMAL) ||
                typeName.contains(FLOAT) || typeName.contains(BOOLEAN));
    }
}
