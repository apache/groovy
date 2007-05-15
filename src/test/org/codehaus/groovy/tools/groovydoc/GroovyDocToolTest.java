/*
 *
 * Copyright 2007 Jeremy Rayner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.groovy.tools.groovydoc;

import groovy.util.GroovyTestCase;

public class GroovyDocToolTest extends GroovyTestCase {
    GroovyDocTool xmlTool;
    private static final String FS = "/";
    private static final String MOCK_DIR = "mock" + FS + "doc";
    private static final String TEMPLATES_DIR = "main" + FS + "org" + FS + "codehaus" + FS + "groovy" + FS + "tools" + FS + "groovydoc" + FS + "gstring-templates";

    public void setUp() {
        xmlTool = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                "src" + FS + "main", // source file dirs
                new String[]{TEMPLATES_DIR + FS + "top-level" + FS + "rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + FS + "package-level" + FS + "packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + FS + "class-level" + FS + "classDocStructuredData.xml"}
        );
    }

    public void testGroovyDocTheCategoryMethodClass() throws Exception {
        xmlTool.add("groovy" + FS + "util" + FS + "CliBuilder.groovy");
        xmlTool.add("groovy" + FS + "lang" + FS + "GroovyLogTestCase.groovy");
        xmlTool.add("groovy" + FS + "mock" + FS + "interceptor" + FS + "StrictExpectation.groovy");
        xmlTool.add("groovy" + FS + "ui" + FS + "Console.groovy");
        xmlTool.add("org" + FS + "codehaus" + FS + "groovy" + FS + "runtime" + FS + "GroovyCategorySupport.java");
        xmlTool.add("org" + FS + "codehaus" + FS + "groovy" + FS + "runtime" + FS + "ConvertedMap.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        System.out.println("output = " + output);

        String categoryMethodDocument = output.getText(MOCK_DIR + FS + "org" + FS + "codehaus" + FS + "groovy" + FS + "runtime" + FS + "CategoryMethod.html"); // todo - figure out how to get xml extension for templates

        assertTrue(categoryMethodDocument.indexOf("<method returns=\"boolean\" name=\"hasCategoryInAnyThread\">") > 0);

        String packageDocument = output.getText(MOCK_DIR + FS + "org" + FS + "codehaus" + FS + "groovy" + FS + "runtime" + FS + "packageDocStructuredData.xml");
        // TODO: fix code then reinstate assertion
        //assertTrue(packageDocument.indexOf("<class name=\"CategoryMethod\" />") > 0);

        String rootDocument = output.getText(MOCK_DIR + FS + "rootDocStructuredData.xml");
        // TODO: fix code then reinstate assertion(s)
        // assertTrue(rootDocument.indexOf("<package name=\"org" + FS + "codehaus" + FS + "groovy" + FS + "runtime\" />") > 0);
        // assertTrue(rootDocument.indexOf("<class path=\"org" + FS + "codehaus" + FS + "groovy" + FS + "runtime" + FS + "CategoryMethod\" name=\"CategoryMethod\" />") > 0);
    }

    public void testConstructors() throws Exception {
        xmlTool.add("groovy" + FS + "ui" + FS + "Console.groovy");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String consoleDoc = output.getText(MOCK_DIR + FS + "groovy" + FS + "ui" + FS + "Console.html");
        assertTrue(consoleDoc.indexOf("<constructor name=\"Console\">") > 0);
        assertTrue(consoleDoc.indexOf("<parameter type=\"ClassLoader\" name=\"parent\" />") > 0);
    }

    public void testClassComment() throws Exception {
        xmlTool.add("groovy" + FS + "xml" + FS + "DOMBuilder.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + FS + "groovy" + FS + "xml" + FS + "DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("A helper class for creating a W3C DOM tree") > 0);
    }

    public void testPackageName() throws Exception {
        xmlTool.add("groovy" + FS + "xml" + FS + "DOMBuilder.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + FS + "groovy" + FS + "xml" + FS + "DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<containingPackage name=\"groovy" + FS + "xml\">groovy.xml</containingPackage>") > 0);
    }
}