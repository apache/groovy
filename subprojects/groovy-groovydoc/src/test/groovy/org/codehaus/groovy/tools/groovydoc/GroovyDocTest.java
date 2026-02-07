/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
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
package org.codehaus.groovy.tools.groovydoc;

import groovy.util.CharsetToolkit;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileRule;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroovyDocTest {

    private final BuildFileRule rule = new BuildFileRule();

    private File tmpDir;
    private static final String SRC_TESTFILES;

    static {
        String groovyDocResourcesPathInSubproject = "src/test/resources/groovydoc/";
        String groovyDocResourcesPathFromMainProject = "subprojects/groovy-groovydoc/" + groovyDocResourcesPathInSubproject;
        if (new File(groovyDocResourcesPathInSubproject).exists()) {
            SRC_TESTFILES = groovyDocResourcesPathInSubproject;
        } else if (new File(groovyDocResourcesPathFromMainProject).exists()) {
            SRC_TESTFILES = groovyDocResourcesPathFromMainProject;
        } else {
            SRC_TESTFILES = null;
        }
    }

    @BeforeEach
    public void setUp() {
        if (SRC_TESTFILES == null) {
            throw new RuntimeException("Could not identify path to resources dir.");
        }
        rule.configureProject(SRC_TESTFILES + "groovyDocTests.xml");
        tmpDir = new File(rule.getProject().getProperty("tmpdir"));
    }

    @AfterEach
    public void tearDown() {
        ResourceGroovyMethods.deleteDir(tmpDir);
    }

    @Test
    public void testCustomClassTemplate() throws Exception {
        rule.executeTarget("testCustomClassTemplate");

        File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles");
        String[] list = testfilesPackageDir.list((file, name) -> name.equals("DocumentedClass.html"));

        assertNotNull(list, "Dir not found: " + testfilesPackageDir.getAbsolutePath());
        assertEquals(1, list.length);
        File documentedClassHtmlDoc = new File(testfilesPackageDir, list[0]);

        List<String> lines = ResourceGroovyMethods.readLines(documentedClassHtmlDoc);
        assertTrue(lines.contains("<title>DocumentedClass</title>"), "\"<title>DocumentedClass</title>\" not in: " + lines);
        assertTrue(lines.contains("This is a custom class template."), "\"This is a custom class template.\" not in: " + lines);
    }

    @Test
    public void testSupportedJavadocVersion() throws Exception {
        rule.executeTarget("supportedGroovyDocJava");

        File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles/generics");
        String[] list = testfilesPackageDir.list((file, name) -> name.equals("Java.html"));

        assertNotNull(list, "Dir not found: " + testfilesPackageDir.getAbsolutePath());
        assertEquals(1, list.length);
        File documentedClass = new File(testfilesPackageDir, list[0]);
        assertTrue(documentedClass.exists(), "Java.html not found: " + documentedClass.getAbsolutePath());

        List<String> lines = ResourceGroovyMethods.readLines(documentedClass);
        assertTrue(lines.contains("<title>Java</title>"), "\"<title>Java</title>\" not in: " + lines);
    }

    @Test
    public void testUnsupportedJavadocVersion() {
        rule.executeTarget("unsupportedGroovyDocJava");

        File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles/generics");
        String[] list = testfilesPackageDir.list((file, name) -> name.equals("Java.html"));

        assertNotNull(list, "Dir not found: " + testfilesPackageDir.getAbsolutePath());
        assertEquals(0, list.length, "Files unexpectedly found when not expecting to parse");
    }

    @Test
    public void testInvalidJavaVersion() {
        try {
            rule.executeTarget("invalidJavaVersion");
        }
        catch(BuildException e) {
            assertEquals("java.lang.IllegalArgumentException: Unsupported Java Version: DNE", e.getMessage());
        }
    }

    @Test
    public void testFileEncoding() throws Exception {
        rule.executeTarget("testFileEncoding");

        File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles");
        String[] list = testfilesPackageDir.list((file, name) -> name.equals("DocumentedClass.html"));

        File documentedClassHtmlDoc = new File(testfilesPackageDir, list[0]);
        CharsetToolkit charsetToolkit = new CharsetToolkit(documentedClassHtmlDoc);

        assertEquals(StandardCharsets.UTF_16LE, charsetToolkit.getCharset(), "The generated groovydoc must be in 'UTF-16LE' file encoding.'");
    }

    @Test
    public void testJavadocForRecords() throws Exception {
        rule.executeTarget("testJavadocForRecords");

        File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles/records");
        String[] list = testfilesPackageDir.list((file, name) -> name.equals("Record.html"));

        assertNotNull(list, "Dir not found: " + testfilesPackageDir.getAbsolutePath());
        assertEquals(1, list.length);
        File documentedClassHtmlDoc = new File(testfilesPackageDir, list[0]);

        List<String> lines = ResourceGroovyMethods.readLines(documentedClassHtmlDoc);
        assertTrue(lines.contains("<title>Record</title>"), "\"<title>Record</title>\" not in: " + lines);
    }
}
