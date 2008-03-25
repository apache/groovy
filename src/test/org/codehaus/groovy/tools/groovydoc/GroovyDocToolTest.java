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
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import java.util.ArrayList;

public class GroovyDocToolTest extends GroovyTestCase {
    GroovyDocTool xmlTool;
    GroovyDocTool xmlToolForTests;
    GroovyDocTool plainTool;
    private static final String MOCK_DIR = "mock/doc";
    private static final String TEMPLATES_DIR = "main/org/codehaus/groovy/tools/groovydoc/gstring-templates";

    public void setUp() {
        plainTool = new GroovyDocTool("src/test");

        xmlTool = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                "src/main", // source file dirs
                new String[]{TEMPLATES_DIR + "/top-level/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/package-level/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/class-level/classDocStructuredData.xml"},
                new ArrayList()
        );

        xmlToolForTests = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                "src/test", // source file dirs
                new String[]{TEMPLATES_DIR + "/top-level/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/package-level/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/class-level/classDocStructuredData.xml"},
                new ArrayList()
        );
    }

    public void testPlainGroovyDocTool() throws Exception {
        plainTool.add("org/codehaus/groovy/tools/groovydoc/GroovyDocToolTest.java");
        GroovyRootDoc root = plainTool.getRootDoc();

        // loop through classes in tree
        GroovyClassDoc[] classDocs = root.classes();
        for (int i=0; i< classDocs.length; i++) {
            GroovyClassDoc clazz = root.classes()[i];

            assertEquals("GroovyDocToolTest", clazz.name());

            // loop through methods in class
            boolean seenThisMethod = false;
            GroovyMethodDoc[] methodDocs = clazz.methods();
            for (int j=0; j< methodDocs.length; j++) {
                GroovyMethodDoc method = clazz.methods()[j];

                if ("testPlainGroovyDocTool".equals(method.name())) {
                    seenThisMethod = true;
                }

            }
            assertTrue(seenThisMethod);
        }
    }

    public void testGroovyDocTheCategoryMethodClass() throws Exception {
        xmlTool.add("groovy/util/CliBuilder.groovy");
        xmlTool.add("groovy/lang/GroovyLogTestCase.groovy");
        xmlTool.add("groovy/mock/interceptor/StrictExpectation.groovy");
        xmlTool.add("groovy/ui/Console.groovy");
        xmlTool.add("org/codehaus/groovy/runtime/GroovyCategorySupport.java");
        xmlTool.add("org/codehaus/groovy/runtime/ConvertedMap.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String categoryMethodDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/CategoryMethod.html"); // todo - figure out how to get xml extension for templates

        assertTrue(categoryMethodDocument.indexOf("<method modifiers=\"public static \" returns=\"boolean\" name=\"hasCategoryInAnyThread\">") > 0);

        String packageDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/packageDocStructuredData.xml");
        assertTrue(packageDocument.indexOf("<class name=\"CategoryMethod\" />") > 0);

        String rootDocument = output.getText(MOCK_DIR + "/rootDocStructuredData.xml");
        assertTrue(rootDocument.indexOf("<package name=\"org/codehaus/groovy/runtime\" />") > 0);
        assertTrue(rootDocument.indexOf("<class path=\"org/codehaus/groovy/runtime/CategoryMethod\" name=\"CategoryMethod\" />") > 0);
    }

    public void testConstructors() throws Exception {
        xmlTool.add("groovy/ui/Console.groovy");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String consoleDoc = output.getText(MOCK_DIR + "/groovy/ui/Console.html");
        assertTrue(consoleDoc.indexOf("<constructor modifiers=\"public \" name=\"Console\">") > 0);
        assertTrue(consoleDoc.indexOf("<parameter type=\"ClassLoader\" name=\"parent\" />") > 0);
    }

    public void testClassComment() throws Exception {
        xmlTool.add("groovy/xml/DOMBuilder.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("A helper class for creating a W3C DOM tree") > 0);
    }

    public void testMethodComment() throws Exception {
        xmlTool.add("groovy/model/DefaultTableColumn.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String defTabColDoc = output.getText(MOCK_DIR + "/groovy/model/DefaultTableColumn.html");

        assertTrue(defTabColDoc.indexOf("Evaluates the value of a cell") > 0);
    }
    public void testPackageName() throws Exception {
        xmlTool.add("groovy/xml/DOMBuilder.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<containingPackage name=\"groovy/xml\">groovy.xml</containingPackage>") > 0);
    }

    public void testExtendsClauseWithoutSuperClassInTree() throws Exception {
        xmlTool.add("groovy/xml/DOMBuilder.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<extends>BuilderSupport</extends>") > 0);
    }

    public void testExtendsClauseWithSuperClassInTree() throws Exception {
        xmlTool.add("groovy/xml/DOMBuilder.java");
        xmlTool.add("groovy/util/BuilderSupport.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<extends>BuilderSupport</extends>") > 0);
    }
    
    public void testDefaultPackage() throws Exception {
    	xmlToolForTests.add("UberTestCaseBugs.java");
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);
        String domBuilderDoc = output.getText(MOCK_DIR + "/DefaultPackage/UberTestCaseBugs.html");
        assertTrue(domBuilderDoc.indexOf("<extends>TestCase</extends>") > 0);    	
    }

    public void testStaticModifier() throws Exception {
        xmlTool.add("groovy/swing/binding/AbstractButtonProperties.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String abstractButtonPropertiesDoc = output.getText(MOCK_DIR + "/groovy/swing/binding/AbstractButtonProperties.html");
        assertTrue(abstractButtonPropertiesDoc.indexOf("static") > 0);
    }
    public void testAnonymousInnerClassMethodsNotIncluded() throws Exception {
        xmlTool.add("groovy/swing/binding/AbstractButtonProperties.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String abstractButtonPropertiesDoc = output.getText(MOCK_DIR + "/groovy/swing/binding/AbstractButtonProperties.html");
        assertTrue(abstractButtonPropertiesDoc.indexOf("createBinding") < 0);
    }
    public void testMultipleConstructorError() throws Exception {
        xmlTool.add("groovy/sql/Sql.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String sqlDoc = output.getText(MOCK_DIR + "/groovy/sql/Sql.html");
        assertTrue(sqlDoc.indexOf("<method modifiers=\"public static \" returns=\"InParameter\" name=\"VARBINARY\">") > 0); // VARBINARY() and other methods in Sql.java were assumed to be Constructors, make sure they aren't anymore...
    }

    public void testReturnTypeResolution() throws Exception {
        xmlTool.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        xmlTool.add("org/codehaus/groovy/groovydoc/GroovyClassDoc.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue(text.indexOf("org.codehaus.groovy.groovydoc.GroovyClassDoc") > 0);
    }
    public void testParameterTypeResolution() throws Exception {
        xmlTool.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        xmlTool.add("org/codehaus/groovy/groovydoc/GroovyPackageDoc.java");
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue(text.indexOf("<parameter type=\"org.codehaus.groovy.groovydoc.GroovyPackageDoc\"") > 0);
    }
}