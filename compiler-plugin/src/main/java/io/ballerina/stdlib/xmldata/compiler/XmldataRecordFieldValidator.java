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

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.ArrayList;
import java.util.List;

/**
 * Xmldata record field analyzer.
 */
public class XmldataRecordFieldValidator implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private final List<RecordFieldNode> recordNodes = new ArrayList<>();
    private static final String STRING = "string";
    private static final String DECIMAL = "decimal";
    private static final String FLOAT = "float";
    private static final String BOOLEAN = "boolean";
    private static final String INT = "int";
    private static final String QUESTION_MARK = "?";

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
        if (node instanceof FunctionBodyBlockNode) {
            FunctionBodyBlockNode functionBodyBlockNode = (FunctionBodyBlockNode) node;
            for (StatementNode statementNode: functionBodyBlockNode.statements()) {
                String statement = statementNode.toString();
                if (statementNode instanceof VariableDeclarationNode && statement.contains("xmldata:toRecord")) {
                    String[] words = statement.trim().split(" ");
                    if (words[0].startsWith("record")) {
                        return;
                    } else {
                        checkRecordField(words[0], ctx);
                    }
                }
            }
        }
    }

    private void checkRecordField(String recordName, SyntaxNodeAnalysisContext ctx) {
        for (RecordFieldNode recordNode: this.recordNodes) {
            String name = recordNode.parent().parent().children().get(1).toString().trim();
            String typeName = recordNode.typeName().toString().trim();
            if (name.equals(recordName.trim())) {
                if (typeName.contains(QUESTION_MARK)) {
                    DiagnosticInfo diagnosticInfo = new DiagnosticInfo(DiagnosticsCodes.XMLDATA_101.getCode(),
                            DiagnosticsCodes.XMLDATA_101.getMessage(), DiagnosticsCodes.XMLDATA_101.getSeverity());
                    ctx.reportDiagnostic(
                            DiagnosticFactory.createDiagnostic(diagnosticInfo, recordNode.location()));
                    if (typeName.contains("|")) {
                        String[] types = typeName.split("\\|");
                        for (String type: types) {
                            if (type.trim().contains(QUESTION_MARK)) {
                                if (isNonPrimitiveOptionalType(type)) {
                                    checkRecordField(type.substring(0, type.length() - 1), ctx);
                                }
                            } else {
                                if (isNonPrimitiveType(type)) {
                                    checkRecordField(type, ctx);
                                }
                            }
                        }
                    } else if (isNonPrimitiveOptionalType(typeName)) {
                        checkRecordField(typeName.substring(0, typeName.length() - 1), ctx);
                    }
                } else if (isNonPrimitiveType(typeName)) {
                    checkRecordField(typeName, ctx);
                }
            }
        }
    }

    private boolean isNonPrimitiveType(String typeName) {
        return !(typeName.equals(STRING) || typeName.equals(INT) || typeName.equals(DECIMAL) ||
                typeName.equals(FLOAT) || typeName.equals(BOOLEAN));
    }

    private boolean isNonPrimitiveOptionalType(String typeName) {
        return !(typeName.equals(STRING + QUESTION_MARK) || typeName.equals(INT + QUESTION_MARK) ||
                typeName.equals(DECIMAL + QUESTION_MARK) || typeName.equals(FLOAT + QUESTION_MARK) ||
                typeName.equals(BOOLEAN + QUESTION_MARK));
    }
}
