/*
 *
 * Copyright 2007-2009 the original author or authors.
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
import java.util.List;
import java.util.Properties;

/**
 * @author Jeremy Rayner
 */
public class GroovyDocToolTest extends GroovyTestCase {
    private static final String MOCK_DIR = "mock/doc";
    private static final String TEMPLATES_DIR = "main/org/codehaus/groovy/tools/groovydoc/gstring-templates";

    GroovyDocTool xmlTool;
    GroovyDocTool xmlToolForTests;
    GroovyDocTool plainTool;

    public void setUp() {
        plainTool = new GroovyDocTool(new String[]{"src/test"});

        xmlTool = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/main"}, // source file dirs
                new String[]{TEMPLATES_DIR + "/top-level/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/package-level/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/class-level/classDocStructuredData.xml"},
                new ArrayList(),
                new Properties()
        );

        xmlToolForTests = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/test"}, // source file dirs, // source file dirs
                new String[]{TEMPLATES_DIR + "/top-level/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/package-level/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/class-level/classDocStructuredData.xml"},
                new ArrayList(),
                new Properties()
        );
    }

    public void testPlainGroovyDocTool() throws Exception {
        List srcList = new ArrayList();
        srcList.add("org/codehaus/groovy/tools/groovydoc/GroovyDocToolTest.java");
        plainTool.add(srcList);
        GroovyRootDoc root = plainTool.getRootDoc();

        // loop through classes in tree
        GroovyClassDoc[] classDocs = root.classes();
        for (int i = 0; i < classDocs.length; i++) {
            GroovyClassDoc clazz = root.classes()[i];
            assertEquals("GroovyDocToolTest", clazz.name());

            // loop through methods in class
            boolean seenThisMethod = false;
            GroovyMethodDoc[] methodDocs = clazz.methods();
            for (int j = 0; j < methodDocs.length; j++) {
                GroovyMethodDoc method = clazz.methods()[j];
                if ("testPlainGroovyDocTool".equals(method.name())) {
                    seenThisMethod = true;
                }
            }
            assertTrue(seenThisMethod);
        }
    }

    public void testGroovyDocTheCategoryMethodClass() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/util/CliBuilder.groovy");
        srcList.add("groovy/lang/GroovyLogTestCase.groovy");
        srcList.add("groovy/mock/interceptor/StrictExpectation.groovy");
        srcList.add("groovy/ui/Console.groovy");
        srcList.add("org/codehaus/groovy/runtime/GroovyCategorySupport.java");
        srcList.add("org/codehaus/groovy/runtime/ConvertedMap.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String groovyCategorySupportDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/GroovyCategorySupport.html");
        assertTrue(groovyCategorySupportDocument != null &&
                groovyCategorySupportDocument.indexOf("<method modifiers=\"public static \" returns=\"boolean\" name=\"hasCategoryInAnyThread\">") > 0);

        String categoryMethodDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/GroovyCategorySupport.CategoryMethod.html");
        assertTrue(categoryMethodDocument != null &&
                categoryMethodDocument.indexOf("<method modifiers=\"\" returns=\"boolean\" name=\"isChildOfParent\">") > 0);

        String packageDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/packageDocStructuredData.xml");
        assertTrue("Failed to find 'packageDocStructuredData.xml' in generated output", packageDocument != null);
        assertTrue(packageDocument.indexOf("<class name=\"GroovyCategorySupport\" />") > 0);
        assertTrue(packageDocument.indexOf("<class name=\"GroovyCategorySupport.CategoryMethod\" />") > 0);

        String rootDocument = output.getText(MOCK_DIR + "/rootDocStructuredData.xml");
        assertTrue("Failed to find 'rootDocStructuredData.xml' in generated output", rootDocument != null);
        assertTrue(rootDocument.indexOf("<package name=\"org/codehaus/groovy/runtime\" />") > 0);
        assertTrue(rootDocument.indexOf("<class path=\"org/codehaus/groovy/runtime/GroovyCategorySupport\" name=\"GroovyCategorySupport\" />") > 0);
        assertTrue(rootDocument.indexOf("<class path=\"org/codehaus/groovy/runtime/GroovyCategorySupport.CategoryMethod\" name=\"GroovyCategorySupport.CategoryMethod\" />") > 0);
    }

    public void testConstructors() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/ui/Console.groovy");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String consoleDoc = output.getText(MOCK_DIR + "/groovy/ui/Console.html");
        assertTrue(consoleDoc.indexOf("<constructor modifiers=\"public \" name=\"Console\">") > 0);
        assertTrue(consoleDoc.indexOf("<parameter type=\"ClassLoader\" name=\"parent\" />") > 0);
    }

    public void testClassComment() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/xml/DOMBuilder.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("A helper class for creating a W3C DOM tree") > 0);
    }

    public void testMethodComment() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/model/DefaultTableColumn.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String defTabColDoc = output.getText(MOCK_DIR + "/groovy/model/DefaultTableColumn.html");
        assertTrue(defTabColDoc.indexOf("Evaluates the value of a cell") > 0);
    }

    public void testPackageName() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/xml/DOMBuilder.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<containingPackage name=\"groovy/xml\">groovy.xml</containingPackage>") > 0);
    }

    public void testExtendsClauseWithoutSuperClassInTree() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/xml/DOMBuilder.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        // TODO reinstate
//        assertTrue(domBuilderDoc.indexOf("<extends>BuilderSupport</extends>") > 0);
        assertTrue(domBuilderDoc.indexOf("<extends>null</extends>") > 0);
    }

    public void testExtendsClauseWithSuperClassInTree() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/xml/DOMBuilder.java");
        srcList.add("groovy/util/BuilderSupport.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<extends>BuilderSupport</extends>") > 0);
    }

    public void testDefaultPackage() throws Exception {
        List srcList = new ArrayList();
        srcList.add("UberTestCaseBugs.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);
        String domBuilderDoc = output.getText(MOCK_DIR + "/DefaultPackage/UberTestCaseBugs.html");
        // TODO reinstate
//        assertTrue(domBuilderDoc.indexOf("<extends>TestCase</extends>") > 0);
        assertTrue(domBuilderDoc.indexOf("<extends>null</extends>") > 0);
    }

    public void testStaticModifier() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/swing/binding/AbstractButtonProperties.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String abstractButtonPropertiesDoc = output.getText(MOCK_DIR + "/groovy/swing/binding/AbstractButtonProperties.html");
        assertTrue(abstractButtonPropertiesDoc.indexOf("static") > 0);
    }

    public void testAnonymousInnerClassMethodsNotIncluded() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/swing/binding/AbstractButtonProperties.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String abstractButtonPropertiesDoc = output.getText(MOCK_DIR + "/groovy/swing/binding/AbstractButtonProperties.html");
        assertTrue(abstractButtonPropertiesDoc.indexOf("createBinding") < 0);
    }

    public void testMultipleConstructorError() throws Exception {
        List srcList = new ArrayList();
        srcList.add("groovy/sql/Sql.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String sqlDoc = output.getText(MOCK_DIR + "/groovy/sql/Sql.html");
        assertTrue(sqlDoc.indexOf("<method modifiers=\"public static \" returns=\"InParameter\" name=\"VARBINARY\">") > 0); // VARBINARY() and other methods in Sql.java were assumed to be Constructors, make sure they aren't anymore...
    }

    public void testReturnTypeResolution() throws Exception {
        List srcList = new ArrayList();
        srcList.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        srcList.add("org/codehaus/groovy/groovydoc/GroovyClassDoc.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue(text.indexOf("org.codehaus.groovy.groovydoc.GroovyClassDoc") > 0);
    }

    public void testParameterTypeResolution() throws Exception {
        List srcList = new ArrayList();
        srcList.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        srcList.add("org/codehaus/groovy/groovydoc/GroovyPackageDoc.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue(text.indexOf("<parameter type=\"org.codehaus.groovy.groovydoc.GroovyPackageDoc\"") > 0);
    }
    
    public void testMultipleSourcePaths() throws Exception {
        GroovyDocTool multipleXmlTool = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/main", "src/test"}, // source file dirs, // source file dirs
                new String[]{TEMPLATES_DIR + "/top-level/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/package-level/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/class-level/classDocStructuredData.xml"},
                new ArrayList(),
                new Properties()
        );
        
        List srcList = new ArrayList();
        srcList.add("groovy/model/DefaultTableColumn.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/GroovyDocToolTestSampleGroovy.groovy");
        multipleXmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        multipleXmlTool.renderToOutput(output, MOCK_DIR);
        assertTrue(output.getText(MOCK_DIR + "/groovy/model/DefaultTableColumn.html") != null);
        assertTrue(output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/GroovyDocToolTestSampleGroovy.html") != null);
    }
}
