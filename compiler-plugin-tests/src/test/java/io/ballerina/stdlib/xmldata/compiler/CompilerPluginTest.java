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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Package;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests the custom xmldata compiler plugin.
 */
public class CompilerPluginTest {

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Path distributionPath = Paths.get("../", "target", "ballerina-runtime").toAbsolutePath();
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(distributionPath).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    private Package loadPackage(String path) {
        Path projectDirPath = Paths.get("src", "test", "resources", "diagnostics").
                toAbsolutePath().resolve(path);
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage();
    }

    @Test
    public void testInvalidType1() {
        DiagnosticResult diagnosticResult = loadPackage("sample1").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.get(0).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
        Assert.assertEquals(errorDiagnosticsList.get(1).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
    }

    @Test
    public void testInvalidType2() {
        DiagnosticResult diagnosticResult = loadPackage("sample2").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 4);
        Assert.assertEquals(errorDiagnosticsList.get(0).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
        Assert.assertEquals(errorDiagnosticsList.get(1).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
        Assert.assertEquals(errorDiagnosticsList.get(2).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
        Assert.assertEquals(errorDiagnosticsList.get(3).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
    }

    @Test
    public void testInvalidType3() {
        DiagnosticResult diagnosticResult = loadPackage("sample3").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 4);
        Assert.assertEquals(errorDiagnosticsList.get(0).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
    }

    @Test
    public void testInvalidType4() {
        DiagnosticResult diagnosticResult = loadPackage("sample4").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 4);
    }

    @Test
    public void testInvalidType5() {
        DiagnosticResult diagnosticResult = loadPackage("sample5").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 6);
    }

    @Test
    public void testInvalidType6() {
        DiagnosticResult diagnosticResult = loadPackage("sample6").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 4);
    }

    @Test
    public void testInvalidType7() {
        DiagnosticResult diagnosticResult = loadPackage("sample7").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 3);
    }

    @Test
    public void testInvalidType8() {
        DiagnosticResult diagnosticResult = loadPackage("sample8").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 5);
    }

    @Test
    public void testInvalidType9() {
        DiagnosticResult diagnosticResult = loadPackage("sample9").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 5);
    }

    @Test
    public void testInvalidUnionType1() {
        DiagnosticResult diagnosticResult = loadPackage("sample10").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 2);
        Assert.assertEquals(errorDiagnosticsList.get(1).diagnosticInfo().messageFormat(),
                "invalid field type: the record field does not support the optional value type");
        Assert.assertEquals(errorDiagnosticsList.get(0).diagnosticInfo().messageFormat(),
                "invalid union type: union type does not support multiple non-primitive record types");
    }
    @Test
    public void testInvalidChildAnnotation() {
        DiagnosticResult diagnosticResult = loadPackage("sample11").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.WARNING))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 1);
        Assert.assertEquals(errorDiagnosticsList.get(0).diagnosticInfo().messageFormat(),
                "invalid annotation attachment: child record does not allow name annotation");
    }

    @Test
    public void testInvalidChildAnnotation1() {
        DiagnosticResult diagnosticResult = loadPackage("sample12").getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream()
                .filter(r -> r.diagnosticInfo().severity().equals(DiagnosticSeverity.WARNING))
                .collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), 1);
        Assert.assertEquals(errorDiagnosticsList.get(0).diagnosticInfo().messageFormat(),
                "invalid annotation attachment: child record does not allow name annotation");
    }
}
