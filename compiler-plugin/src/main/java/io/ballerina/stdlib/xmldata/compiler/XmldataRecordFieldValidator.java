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

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.NilTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
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

    private final List<SyntaxNodeAnalysisContext> nodes = new ArrayList<>();
    private final List<SyntaxNodeAnalysisContext> validatedNodes = new ArrayList<>();
    private static final String TO_RECORD = "xmldata:toRecord";
    private static final String FROM_XML = "xmldata:fromXml";

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
            this.nodes.add(ctx);
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
        for (SyntaxNodeAnalysisContext syntaxNodeAnalysisContext: this.nodes) {
            if (!this.validatedNodes.contains(syntaxNodeAnalysisContext)) {
                RecordFieldNode recordFieldNode = ((RecordFieldNode) syntaxNodeAnalysisContext.node());
                String recordNameOfField = recordFieldNode.parent().parent().children().get(1).toString().trim();
                if (recordNameOfField.equals(recordName.trim())) {
                    this.validatedNodes.add(syntaxNodeAnalysisContext);
                    Optional<Symbol> varSymOptional = syntaxNodeAnalysisContext.semanticModel().
                            symbol(syntaxNodeAnalysisContext.node());
                    if (varSymOptional.isPresent()) {
                        TypeSymbol typeSymbol = ((RecordFieldSymbol) varSymOptional.get()).typeDescriptor();
                        if (typeSymbol instanceof UnionTypeSymbol) {
                            List<TypeSymbol> typeSymbols = ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors();
                            for (TypeSymbol symbol : typeSymbols) {
                                validateType(ctx, recordFieldNode, symbol);
                            }
                        } else {
                            validateType(ctx, recordFieldNode, typeSymbol);
                        }
                    }
                }
            }
        }
    }

    private void validateType(SyntaxNodeAnalysisContext ctx, RecordFieldNode recordFieldNode, TypeSymbol typeSymbol) {
        if (typeSymbol instanceof NilTypeSymbol) {
            reportDiagnosticInfo(ctx, recordFieldNode);
        } else if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            checkRecordField(typeSymbol.getName().get(), ctx);
        } else if (typeSymbol instanceof ArrayTypeSymbol) {
            TypeSymbol arrayTypeSymbol = ((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor();
            if (arrayTypeSymbol instanceof TypeReferenceTypeSymbol) {
                checkRecordField(arrayTypeSymbol.getName().get(), ctx);
            }
        }
    }
    private void reportDiagnosticInfo(SyntaxNodeAnalysisContext ctx, RecordFieldNode recordFieldNode) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(DiagnosticsCodes.XMLDATA_101.getCode(),
                DiagnosticsCodes.XMLDATA_101.getMessage(), DiagnosticsCodes.XMLDATA_101.getSeverity());
        ctx.reportDiagnostic(
                DiagnosticFactory.createDiagnostic(diagnosticInfo, recordFieldNode.location()));
    }
}
