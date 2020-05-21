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

import groovy.test.GroovyTestCase;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroovyDocToolTest extends GroovyTestCase {
    private static final String MOCK_DIR = "mock/doc";
    private static final String TEMPLATES_DIR = "main/resources/org/codehaus/groovy/tools/groovydoc/gstringTemplates";

    GroovyDocTool xmlTool;
    GroovyDocTool xmlToolForTests;
    GroovyDocTool plainTool;
    GroovyDocTool htmlTool;

    public void setUp() {
        plainTool = new GroovyDocTool(new String[]{"src/test/groovy"});

        xmlTool = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/main/java", "../../src/main/java", "src/test/groovy"}, // source file dirs
                new String[]{TEMPLATES_DIR + "/topLevel/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/packageLevel/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/classLevel/classDocStructuredData.xml"},
                new ArrayList<LinkArgument>(),
                new Properties()
        );

        xmlToolForTests = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/test/groovy", "src/test/resources", "../../src/test"}, // source file dirs
                new String[]{TEMPLATES_DIR + "/topLevel/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/packageLevel/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/classLevel/classDocStructuredData.xml"},
                new ArrayList<LinkArgument>(),
                new Properties()
        );

        ArrayList<LinkArgument> links = new ArrayList<LinkArgument>();
        LinkArgument link = new LinkArgument();
        link.setHref("https://docs.oracle.com/javase/8/docs/api/");
        link.setPackages("java.,org.xml.,javax.,org.xml.");
        links.add(link);

        htmlTool = makeHtmltool(links, new Properties());
    }

    private GroovyDocTool makeHtmltool(ArrayList<LinkArgument> links, Properties props) {
        return new GroovyDocTool(
                new FileSystemResourceManager("src/main/resources"), // template storage
                new String[] {"src/test/groovy", "../../src/test"}, // source file dirs
                GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                links,
                props
        );
    }

    public void testPlainGroovyDocTool() throws Exception {
        List<String> srcList = new ArrayList<String>();
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
                    break;
                }
            }
            assertTrue(seenThisMethod);
        }
    }

    public void testGroovyDocTheCategoryMethodClass() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/cli/picocli/CliBuilder.groovy");
        srcList.add("groovy/test/GroovyLogTestCase.groovy");
        srcList.add("groovy/mock/interceptor/StrictExpectation.groovy");
        srcList.add("org/codehaus/groovy/runtime/GroovyCategorySupport.java");
        srcList.add("org/codehaus/groovy/runtime/ConvertedMap.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String groovyCategorySupportDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/GroovyCategorySupport.html");
        assertTrue("Expect hasCategoryInAnyThread in:\n" + groovyCategorySupportDocument, groovyCategorySupportDocument != null &&
                groovyCategorySupportDocument.indexOf("<method modifiers=\"public static \" returns=\"boolean\" name=\"hasCategoryInAnyThread\">") > 0);

        String categoryMethodDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/GroovyCategorySupport.CategoryMethodList.html");
        assertNotNull("Expected to find GroovyCategorySupport.CategoryMethodList in: " + output, categoryMethodDocument);
        assertTrue("Expected add in:\n" + categoryMethodDocument, categoryMethodDocument != null &&
                categoryMethodDocument.indexOf("<method modifiers=\"public \" returns=\"boolean\" name=\"add\">") > 0);

        String packageDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/packageDocStructuredData.xml");
        assertTrue("Failed to find 'packageDocStructuredData.xml' in generated output", packageDocument != null);
        assertTrue(packageDocument.indexOf("<class name=\"GroovyCategorySupport\" />") > 0);
        assertTrue("Expected GroovyCategorySupport.CategoryMethod in:\n" + packageDocument, packageDocument.indexOf("<class name=\"GroovyCategorySupport.CategoryMethod\" />") > 0);

        String rootDocument = output.getText(MOCK_DIR + "/rootDocStructuredData.xml");
        assertTrue("Failed to find 'rootDocStructuredData.xml' in generated output", rootDocument != null);
        assertTrue(rootDocument.indexOf("<package name=\"org/codehaus/groovy/runtime\" />") > 0);
        assertTrue(rootDocument.indexOf("<class path=\"org/codehaus/groovy/runtime/GroovyCategorySupport\" name=\"GroovyCategorySupport\" />") > 0);
        assertTrue(rootDocument.indexOf("<class path=\"org/codehaus/groovy/runtime/GroovyCategorySupport.CategoryMethod\" name=\"GroovyCategorySupport.CategoryMethod\" />") > 0);
    }

    public void testConstructors() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/TestConstructors";
        srcList.add(base + ".groovy");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String constructorDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, constructorDoc);
        assertTrue(constructorDoc.indexOf("<constructor modifiers=\"public \" name=\"TestConstructors\">") > 0);
        assertTrue(constructorDoc.indexOf("<parameter type=\"java.lang.ClassLoader\" name=\"parent\" />") > 0);
    }

    public void testClassComment() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc,builderDoc.contains("A class comment"));
    }

    public void testMethodComment() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/ClassWithMethodComment.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String defTabColDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/ClassWithMethodComment.html");
        assertTrue(defTabColDoc.contains("This is a method comment"));
    }

    public void testPackageName() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc.contains("<containingPackage name=\"org/codehaus/groovy/tools/groovydoc/testfiles\">org.codehaus.groovy.tools.groovydoc.testfiles</containingPackage>"));
    }

    public void testExtendsClauseWithoutSuperClassInTree() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc.contains("<extends>BuilderSupport</extends>"));
    }

    public void testExtendsClauseWithSuperClassInTree() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        srcList.add("groovy/util/BuilderSupport.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc.contains("<extends>BuilderSupport</extends>"));
    }

    public void testInterfaceExtendsClauseWithMultipleInterfaces() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterfaceWithMultipleInterfaces.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterfaceWithMultipleInterfaces.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterface1.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);

        String groovyClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterfaceWithMultipleInterfaces.html");
        assertNotNull("GroovyInterfaceWithMultipleInterfaces not found in: " + output, groovyClassDoc);
        assertTrue(groovyClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterfaceWithMultipleInterfaces.html");
        assertNotNull("JavaInterfaceWithMultipleInterfaces not found in: " + output, javaClassDoc);
        assertTrue(javaClassDoc, javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc, javaClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(javaClassDoc, javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
    }

    public void testImplementsClauseWithMultipleInterfaces() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterface1.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);

        String groovyClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.html");
        assertNotNull("GroovyClassWithMultipleInterfaces not found in: " + output, groovyClassDoc);
        assertTrue(groovyClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.html");
        assertNotNull("JavaClassWithMultipleInterfaces not found in: " + output, javaClassDoc);
        assertTrue(javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
    }

    public void testFullyQualifiedNamesInImplementsClause() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterface1.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);

        String groovyClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.html");
        assertTrue(groovyClassDoc, groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc, groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.html");
        assertTrue(javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
    }

    public void testDefaultPackage() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("DefaultPackageClassSupport.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/DefaultPackage/DefaultPackageClassSupport.html");
        assertTrue(doc.indexOf("<extends>GroovyTestCase</extends>") > 0);
    }

    public void testJavaClassMultiCatch() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/MultiCatchExample";
        srcList.add(base + ".java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, doc);
        assertTrue(doc, doc.contains("foo has a multi-catch exception inside"));
    }

    public void testStaticModifier() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/StaticModifier";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String staticModifierDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, staticModifierDoc);
        assertTrue("static not found in: \"" + staticModifierDoc + "\"", staticModifierDoc.contains("static"));
    }

    public void testAnonymousInnerClassMethodsNotIncluded() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/ClassWithAnonymousInnerClass";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String classWithAnonymousInnerClassDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, classWithAnonymousInnerClassDoc);
        assertTrue("innerClassMethod found in: \"" + classWithAnonymousInnerClassDoc + "\"", !classWithAnonymousInnerClassDoc.contains("innerClassMethod"));
    }

    public void testJavaClassWithDiamondOperator() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, doc);
        assertTrue("stringList not found in: \"" + doc + "\"", doc.contains("stringList"));
    }

    public void testJavaStaticNestedClassWithDiamondOperator() throws Exception {
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/JavaStaticNestedClassWithDiamond";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for outer class " + base, doc);
        assertTrue("Outer class expectedObject not found in: \"" + doc + "\"", doc.contains("expectedObject"));
        String docNested = output.getText(MOCK_DIR + "/" + base + ".Nested.html");
        assertNotNull("No GroovyDoc found for nested class " + base, docNested);
        assertTrue("Nested class comment not found in: \"" + docNested + "\"", docNested.contains("static nested class comment"));
    }

    public void testVisibilityPublic() throws Exception {
        Properties props = new Properties();
        props.put("publicScope", "true");
        testVisibility(props, true, false, false, false);
    }

    public void testVisibilityProtected() throws Exception {
        Properties props = new Properties();
        props.put("protectedScope", "true");
        testVisibility(props, true, true, false, false);
    }

    public void testVisibilityPackage() throws Exception {
        Properties props = new Properties();
        props.put("packageScope", "true");
        testVisibility(props, true, true, true, false);
    }

    public void testVisibilityPrivate() throws Exception {
        Properties props = new Properties();
        props.put("privateScope", "true");
        testVisibility(props, true, true, true, true);
    }

    public void testSinglePropertiesFromGetterSetter() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "str properties should be there", "<a href=\"#str\">str</a>", true);
    }

    public void testReOrderPropertiesFromGetterSetter() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "str1 properties should be there", "<a href=\"#str1\">str1</a>", true);
    }

    public void testCheckOtherTypesPropertiesFromGetterSetter() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "int properties should be there", "<a href=\"#int\">int</a>", true);
    }

    public void testPropertiesShouldNotBePresentForGetterAlone() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "shouldNotBePresent properties shouldn't be there", "<a href=\"#shouldNotBePresent\">shouldNotBePresent</a>", false);
    }

    public void testPropertiesPublicGetPrivateSet() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "_public_get_private_set shouldn't be present"
                    , "<a href=\"#_public_get_private_set\">_public_get_private_set</a>", false);
    }

    public void testPropertiesPrivateGetPublicSet() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "_private_get_public_set shouldn't be present",
                "<a href=\"#_private_get_public_set\">_private_get_public_set</a>", false);
    }

    public void testPropertiesPrivateGetPrivateSet() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "_private_get_private_set shouldn't be present",
                "<a href=\"#_private_get_private_set\">_private_get_private_set</a>", false);
    }

    public void testPropertiesShouldBePresentForSetIsBooleanType() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "testBoolean properties should be there", "<a href=\"#testBoolean\">testBoolean</a>", true);
    }

    public void testPropertiesShouldBePresentForIsSetBooleanType() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "testBoolean2 properties should be there","<a href=\"#testBoolean2\">testBoolean2</a>", true);
    }

    private void testPropertiesFromGetterSetter(String fileName,String assertMessage,String expected,boolean isTrue) throws Exception {
        htmlTool = makeHtmltool(new ArrayList<LinkArgument>(), new Properties());
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/";
        srcList.add(base + fileName + ".groovy");
        htmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String exampleClass = output.getText(MOCK_DIR + "/" + base + fileName + ".html");
        if (isTrue)
            assertTrue(assertMessage, exampleClass.contains(expected));
        else
            assertFalse(assertMessage,exampleClass.contains(expected));
    }

    private void testVisibility(Properties props, boolean a, boolean b, boolean c, boolean d) throws Exception {
        htmlTool = makeHtmltool(new ArrayList<LinkArgument>(), props);
        List<String> srcList = new ArrayList<String>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/ExampleVisibility";
        srcList.add(base + "G.groovy");
        srcList.add(base + "J.java");
        htmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String javaExampleClass = output.getText(MOCK_DIR + "/" + base + "J.html");
        assertMethodVisibility(base + "J", output, javaExampleClass, a, b, c, d);
        String groovyExampleClass = output.getText(MOCK_DIR + "/" + base + "G.html");
        assertMethodVisibility(base + "G", output, groovyExampleClass, a, b, c, d);
    }

    private void assertMethodVisibility(String base, MockOutputTool output, String text, boolean a, boolean b, boolean c, boolean d) {
        assertNotNull("No GroovyDoc found for " + base + "\nFound: " + output, text);
        assertTrue("method a1" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("<a href=\"#a1()\">a1</a>"));
        assertTrue("method a2" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("<a href=\"#a2()\">a2</a>"));
        assertTrue("method b" + (b ? " not" : "") + " found in: \"" + text + "\"", b ^ !text.contains("<a href=\"#b()\">b</a>"));
        assertTrue("method c1" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("<a href=\"#c1()\">c1</a>"));
        assertTrue("method c2" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("<a href=\"#c2()\">c2</a>"));
        assertTrue("method d" + (d ? " not" : "") + " found in: \"" + text + "\"", d ^ !text.contains("<a href=\"#d()\">d</a>"));

        assertTrue("field _a" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("<a href=\"#_a\">_a</a>"));
        assertTrue("field _b" + (b ? " not" : "") + " found in: \"" + text + "\"", b ^ !text.contains("<a href=\"#_b\">_b</a>"));
        assertTrue("field _c" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("<a href=\"#_c\">_c</a>"));
        assertTrue("field _d" + (d ? " not" : "") + " found in: \"" + text + "\"", d ^ !text.contains("<a href=\"#_d\">_d</a>"));

        assertTrue("class A1" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("A1</a></code>"));
        assertTrue("class A2" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("A2</a></code>"));
        assertTrue("class B" + (b ? " not" : "") + " found in: \"" + text + "\"", b ^ !text.contains("B</a></code>"));
        assertTrue("class C" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("C</a></code>"));
        assertTrue("class D" + (d ? " not" : "") + " found in: \"" + text + "\"", d ^ !text.contains("D</a></code>"));
    }

    public void testMultipleConstructorErrorBug() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/MultipleConstructorErrorBug.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/MultipleConstructorErrorBug.html");
        // VARBINARY() and other methods were assumed to be Constructors, make sure they aren't anymore...
        assertTrue(text,text.indexOf("<method modifiers=\"public static \" returns=\"java.lang.String\" name=\"VARBINARY\">") > 0);
    }

    public void testReturnTypeResolution() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        srcList.add("org/codehaus/groovy/groovydoc/GroovyClassDoc.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue("GroovyClassDoc should appear in:\n" + text, text.indexOf("org.codehaus.groovy.groovydoc.GroovyClassDoc") > 0);
    }

    public void testParameterTypeResolution() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        srcList.add("org/codehaus/groovy/groovydoc/GroovyPackageDoc.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue("GroovyPackageDoc should appear in:\n" + text, text.indexOf("<parameter type=\"org.codehaus.groovy.groovydoc.GroovyPackageDoc\"") > 0);
    }

    public void testFileEncodingFallbackToCharset() throws Exception {
        String expectedCharset = "ISO-88591";

        Properties props = new Properties();
        props.setProperty("charset", expectedCharset);

        GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src"),
                new String[0],
                new String[0],
                new String[0],
                new String[0],
                new ArrayList<LinkArgument>(),
                props);

        assertEquals("'fileEncoding' falls back to 'charset' if not provided", expectedCharset, tool.properties.getProperty("fileEncoding"));
    }

    public void testCharsetFallbackToFileEncoding() throws Exception {
        String expectedCharset = "ISO-88591";

        Properties props = new Properties();
        props.setProperty("fileEncoding", expectedCharset);

        GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src"),
                new String[0],
                new String[0],
                new String[0],
                new String[0],
                new ArrayList<LinkArgument>(),
                props);

        assertEquals("'charset' falls back to 'fileEncoding' if not provided", expectedCharset, tool.properties.getProperty("charset"));

    }

    public void testFileEncodingCharsetFallbackToDefaultCharset() throws Exception {
        String expectedCharset = Charset.defaultCharset().name();

        GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src"),
                new String[0],
                new String[0],
                new String[0],
                new String[0],
                new ArrayList<LinkArgument>(),
                new Properties());

        assertEquals("'charset' falls back to the default charset", expectedCharset, tool.properties.getProperty("charset"));
        assertEquals("'fileEncoding' falls back to the default charset", expectedCharset, tool.properties.getProperty("fileEncoding"));
    }

    // GROOVY-5940
    public void testWrongPackageNameInClassHierarchyWithPlainTool() throws Exception {
        List<String> srcList = new ArrayList<String>();

        String fullPathBaseA = "org/codehaus/groovy/tools/groovydoc/testfiles/a/Base";
        srcList.add(fullPathBaseA + ".groovy");

        String fullPathBaseB = "org/codehaus/groovy/tools/groovydoc/testfiles/b/Base";
        srcList.add(fullPathBaseB + ".groovy");

        String fullPathBaseC = "org/codehaus/groovy/tools/groovydoc/testfiles/c/Base";

        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantA.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/b/DescendantB.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantC.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantD.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantE.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantF.groovy");

        plainTool.add(srcList);

        GroovyRootDoc root = plainTool.getRootDoc();

        // loop through classes in tree
        GroovyClassDoc classDocDescendantA = getGroovyClassDocByName(root, "DescendantA");
        assertEquals(fullPathBaseA, root.classNamed(classDocDescendantA, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantB = getGroovyClassDocByName(root, "DescendantB");
        assertEquals(fullPathBaseB, root.classNamed(classDocDescendantB, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantC = getGroovyClassDocByName(root, "DescendantC");
        assertEquals(fullPathBaseA, root.classNamed(classDocDescendantC, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantD = getGroovyClassDocByName(root, "DescendantD");
        assertEquals(fullPathBaseA, root.classNamed(classDocDescendantD, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantE = getGroovyClassDocByName(root, "DescendantE");
        assertNotNull("Expecting to find DescendantE", classDocDescendantE);
        GroovyClassDoc base = root.classNamed(classDocDescendantE, "Base");
        // TODO reinstate next two lines or justify why they should be removed
//        assertNotNull("Expecting to find Base in: " + Arrays.stream(root.classes()).map(GroovyClassDoc::getFullPathName).collect(Collectors.joining(", ")), base);
//        assertEquals(fullPathBaseC, base.getFullPathName());

        GroovyClassDoc classDocDescendantF = getGroovyClassDocByName(root, "DescendantF");
        assertNotNull("Expecting to find DescendantF", classDocDescendantF);
        // TODO reinstate next line or justify why it should be removed
//        assertEquals(fullPathBaseC, root.classNamed(classDocDescendantF, "Base").getFullPathName());
    }

    // GROOVY-5939
    public void testArrayPropertyLinkWithSelfReference() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String arrayPropertyLinkDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.html");

        Pattern p = Pattern.compile("<a(.+?)ArrayPropertyLink.html'>(.+?)</a>\\[\\]");
        Matcher m = p.matcher(arrayPropertyLinkDoc);

        assertTrue(m.find());
        assertEquals("There should be at least a single reference to the ArrayPropertyLink[] in:\n" + arrayPropertyLinkDoc, "ArrayPropertyLink", m.group(2));
    }

    public void testClassesAreNotInitialized() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/UsesClassesWithFailingStaticInit.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/UsesClassesWithFailingStaticInit.html");

        assertTrue("Expected JavaWithFailingStaticInit and GroovyWithFailingStaticInit in:\n" + doc, doc.contains("JavaWithFailingStaticInit") && doc.contains("GroovyWithFailingStaticInit"));
    }

    public void testArrayPropertyLinkWithExternalReference() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/PropertyLink.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String propertyLinkDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/PropertyLink.html");

        Pattern p = Pattern.compile("<a(.+?)ArrayPropertyLink.html'>(.+?)</a>\\[\\]");
        Matcher m = p.matcher(propertyLinkDoc);

        assertTrue(m.find());
        assertEquals("There has to be at least a single reference to the ArrayPropertyLink[]", "ArrayPropertyLink", m.group(2));
    }

    public void testInnerEnumReference() throws Exception {
        List<String> srcList = new ArrayList<String>();

        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/InnerEnum.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/InnerClassProperty.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String derivDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/InnerClassProperty.html");

        // TODO FIXME? - old behavior: Enum was not qualified by outer class InnerEnum
        Pattern p = Pattern.compile("<a(.+?)testfiles/InnerEnum.Enum.html'>(InnerEnum\\.)?(.+?)</a>");
        Matcher m = p.matcher(derivDoc);

        assertTrue("Expecting to find InnerEnum.Enum anchor in:\n" + derivDoc, m.find());
        assertEquals("There has to be a reference to class Enum", "Enum", m.group(3));
    }

    public void testClassAliasing() throws Exception {

        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/Alias.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String derivDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Alias.html");

        Pattern p = Pattern.compile("<a href='(.+?)java/util/ArrayList.html' title='ArrayList'>(.+?)</a>");
        Matcher m = p.matcher(derivDoc);

        assertTrue("expect ArrayList anchor in:\n" + derivDoc, m.find());
        assertEquals("Expect link text to contain ArrayList", "ArrayList", m.group(2));
    }

    public void testImplementedInterfaceWithAlias() throws Exception {
        // FooAdapter imports both api.Foo and lib.Foo, using "lib.Foo as FooImpl" to disambiguate.
        // lib.Foo is imported later than api.Foo, so groovydoc tries to resolve to lib.Foo first.
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/alias/api/Foo.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/alias/lib/Foo.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/alias/FooAdapter.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String fooAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/alias/FooAdapter.html");

        // "Interfaces and Traits" section should show "Foo" as one of the implemented interfaces,
        // and that should link to api/Foo.html, not to lib/Foo.html.
        final Matcher interfacesAndTraits = Pattern.compile(
                "<dt>All Implemented Interfaces and Traits:</dt>\\s*" +
                "<dd><a href='[./]*/org/codehaus/groovy/tools/groovydoc/testfiles/alias/(api|lib)/Foo\\.html'>(Foo|FooImpl)</a></dd>"
        ).matcher(fooAdapterDoc);

        // Constructor is actually "FooAdapter(FooImpl foo)",
        // but it should show "Foo" as the link text, not "FooImpl".
        // The Foo parameter type should link to lib/Foo.html, not api/Foo.html.
        final Matcher constructor = Pattern.compile(
                "FooAdapter(</[a-z]+>)*\\(<a href='[./]*/org/codehaus/groovy/tools/groovydoc/testfiles/alias/(api|lib)/Foo.html'>(Foo|FooImpl)</a> foo\\)"
        ).matcher(fooAdapterDoc);

        assertTrue("Interfaces and Traits pattern should match for this test to make sense in: " + fooAdapterDoc, interfacesAndTraits.find());
        assertTrue("Constructor pattern should match for this test to make sense", constructor.find());

        assertEquals("The implemented interface should link to api.Foo", "api", interfacesAndTraits.group(1));
        assertEquals("The implemented interface link text should be Foo", "Foo", interfacesAndTraits.group(2));
        assertEquals("The constructor parameter should link to lib.Foo", "lib", constructor.group(2));
        assertEquals("The constructor parameter link text should be Foo", "Foo", constructor.group(3));
    }

    public void testClassDeclarationHeader() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
                base + "/JavaInterfaceWithTypeParam.java",
                base + "/GroovyInterfaceWithTypeParam.groovy",
                base + "/JavaInterfaceWithMultipleInterfaces.java",
                base + "/GroovyInterfaceWithMultipleInterfaces.groovy",
                base + "/ClassWithMethodComment.java",
                base + "/DocumentedClass.groovy",
                base + "/JavaClassWithMultipleInterfaces.java",
                base + "/GroovyClassWithMultipleInterfaces.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javaBaseInterface = output.getText(MOCK_DIR + "/" + base + "/JavaInterfaceWithTypeParam.html");
        final String groovyBaseInterface = output.getText(MOCK_DIR + "/" + base + "/GroovyInterfaceWithTypeParam.html");
        final String javaDerivedInterface = output.getText(MOCK_DIR + "/" + base + "/JavaInterfaceWithMultipleInterfaces.html");
        final String groovyDerivedInterface = output.getText(MOCK_DIR + "/" + base + "/GroovyInterfaceWithMultipleInterfaces.html");
        final String javaBaseClass = output.getText(MOCK_DIR + "/" + base + "/ClassWithMethodComment.html");
        final String groovyBaseClass = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        final String javaDerivedClass = output.getText(MOCK_DIR + "/" + base + "/JavaClassWithMultipleInterfaces.html");
        final String groovyDerivedClass = output.getText(MOCK_DIR + "/" + base + "/GroovyClassWithMultipleInterfaces.html");

        final String object = Pattern.quote(
            "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html' title='Object'>Object</a>");
        final String interfaces = Pattern.quote(
            "org.codehaus.groovy.tools.groovydoc.testfiles.GroovyInterface1, " +
            "org.codehaus.groovy.tools.groovydoc.testfiles.JavaInterface1, " +
            "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html' title='Runnable'>Runnable</a>");

        final Pattern baseInterface = Pattern.compile(
            "<pre>" +
            "(public&nbsp;)?interface (Java|Groovy)InterfaceWithTypeParam&lt;T&gt;" +
            "</pre>");
        final Pattern derivedInterface = Pattern.compile(
            "<pre>" +
            "(public&nbsp;)?interface (Java|Groovy)InterfaceWithMultipleInterfaces\n" +
            "extends " + interfaces +
            "</pre>");
        final Pattern baseClass = Pattern.compile(
            "<pre>" +
            "(public&nbsp;)?class (ClassWithMethodComment|DocumentedClass)\n" +
            "extends " + object +
            "</pre>");
        final Pattern derivedClass = Pattern.compile(
            "<pre>" +
            "(public&nbsp;)?abstract&nbsp;class (Java|Groovy)ClassWithMultipleInterfaces\n" +
            "extends " + object + "\n" +
            "implements " + interfaces +
            "</pre>");

        assertTrue("The Java base interface declaration header should match", baseInterface.matcher(javaBaseInterface).find());
        assertTrue("The Groovy base interface declaration header should match", baseInterface.matcher(groovyBaseInterface).find());
        assertTrue("The Java derived interface declaration header should match", derivedInterface.matcher(javaDerivedInterface).find());
        assertTrue("The Groovy derived interface declaration header should match", derivedInterface.matcher(groovyDerivedInterface).find());
        assertTrue("The Java base class declaration header should match", baseClass.matcher(javaBaseClass).find());
        assertTrue("The Groovy base class declaration header should match", baseClass.matcher(groovyBaseClass).find());
        assertTrue("The Java derived class declaration header should match", derivedClass.matcher(javaDerivedClass).find());
        assertTrue("The Groovy derived class declaration header should match", derivedClass.matcher(groovyDerivedClass).find());
    }

    public void testJavaGenericsTitle() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Java.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");

        final Matcher title = Pattern.compile(Pattern.quote(
                "<h2 title=\"[Java] Class Java&lt;N extends Number & Comparable&lt;? extends Number&gt;&gt;\" class=\"title\">"+
                "[Java] Class Java&lt;N extends Number & Comparable&lt;? extends Number&gt;&gt;</h2>"
        )).matcher(javadoc);

        assertTrue("The title should have the generics information", title.find());
    }

    public void testGroovyGenericsTitle() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Groovy.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Matcher title = Pattern.compile(Pattern.quote(
                "<h2 title=\"[Groovy] Trait Groovy&lt;N extends Number & Comparable&lt;? extends Number&gt;&gt;\" class=\"title\">"+
                        "[Groovy] Trait Groovy&lt;N extends Number & Comparable&lt;? extends Number&gt;&gt;</h2>"
        )).matcher(groovydoc);

        assertTrue("The title should have the generics information", title.find());
    }

    public void testParamTagForTypeParams() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Java.java",
                base + "/Groovy.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Pattern classTypeParams = Pattern.compile(
                "<DL><DT><B>Type Parameters:</B></DT><DD><code>N</code> -  Doc.</DD></DL>"
        );
        final Pattern methodTypeParams = Pattern.compile(
                "<DL><DT><B>Type Parameters:</B></DT><DD><code>A</code> -  Doc.</DD><DD><code>B</code> -  Doc.</DD></DL>"
        );

        assertTrue("The Java class doc should have type parameters definitions", classTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy class doc should have type parameters definitions", classTypeParams.matcher(groovydoc).find());
        assertTrue("The Java method doc should have type parameters definitions", methodTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy method doc should have type parameters definitions", methodTypeParams.matcher(groovydoc).find());
    }

    public void testMethodTypeParams() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Java.java",
                base + "/Groovy.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Pattern methodSummaryTypeParams = Pattern.compile(
                "<td class=\"colFirst\"><code>&lt;A, B&gt;</code></td>"
        );
        final Pattern methodDetailsTypeParams = Pattern.compile(
                "<h4>&lt;A, B&gt; (public&nbsp;)?static&nbsp;int <strong>compare</strong>"
        );

        assertTrue("The Java method summary should have type parameters", methodSummaryTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy method summary should have type parameters", methodSummaryTypeParams.matcher(groovydoc).find());
        assertTrue("The Java method details should have type parameters", methodDetailsTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy method details should have type parameters", methodDetailsTypeParams.matcher(groovydoc).find());
    }

    public void testMethodParamTypeParams() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Java.java",
                base + "/Groovy.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Pattern methodSummary = Pattern.compile(Pattern.quote(
                "<code><strong><a href=\"#compare(Class, Class)\">compare</a></strong>"
                        + "("
                        + "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;A&gt; a, "
                        + "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;B&gt; b"
                        + ")"
                        + "</code>"
        ));
        final Pattern methodDetailAnchor = Pattern.compile(Pattern.quote(
                "<a name=\"compare(Class, Class)\"><!-- --></a>"
        ));
        final Pattern methodDetailTitle = Pattern.compile(Pattern.quote(
                "<strong>compare</strong>" +
                        "(" +
                        "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;A&gt; a, " +
                        "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;B&gt; b" +
                        ")"
        ));

        assertTrue("The Java method summary should include type parameters", methodSummary.matcher(javadoc).find());
        assertTrue("The Java method detail anchor should NOT include type parameters", methodDetailAnchor.matcher(javadoc).find());
        assertTrue("The Java method detail title should include type parameters", methodDetailTitle.matcher(javadoc).find());
        assertTrue("The Groovy method summary should include type parameters", methodSummary.matcher(groovydoc).find());
        assertTrue("The Groovy method detail anchor should NOT include type parameters", methodDetailAnchor.matcher(groovydoc).find());
        assertTrue("The Groovy method detail title should include type parameters", methodDetailTitle.matcher(groovydoc).find());
    }

    public void testAnnotations() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/anno";
        htmlTool.add(Arrays.asList(
                base + "/Groovy.groovy",
                base + "/Java.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");
        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");

        assertTrue("The Groovy class declaration header should have the annotation", Pattern.compile(Pattern.quote(
                "<pre>@groovy.transform.EqualsAndHashCode(cache: true)\n" +
                        "class Groovy"
        )).matcher(groovydoc).find());

        assertTrue("The Java class declaration header should have the annotation", Pattern.compile(Pattern.quote(
                "<pre>@<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>\n" +
                        "@<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/SuppressWarnings.html' title='SuppressWarnings'>SuppressWarnings</a>(\"foo\")\n" +
                        "public&nbsp;class Java"
        )).matcher(javadoc).find());

        assertTrue("The Groovy field details should have the annotation", Pattern.compile(Pattern.quote(
                "<h4>@groovy.transform.Internal\n" +
                        "public&nbsp;<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/String.html' title='String'>String</a> " +
                        "<strong>annotatedField</strong></h4>"
        )).matcher(groovydoc).find());

        assertTrue("The Java field details should have the annotation", Pattern.compile(Pattern.quote(
                "<h4>@<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>\n" +
                        "public&nbsp;<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/String.html' title='String'>String</a> " +
                        "<strong>annotatedField</strong></h4>"
        )).matcher(javadoc).find());

        // TODO: Annotations for properties are missing, for some reason.

        assertTrue("The Groovy ctor details should have the annotation", Pattern.compile(Pattern.quote(
                "<h4>@groovy.transform.NamedVariant\n" +
                        "<strong>Groovy</strong>(" +
                        "@groovy.transform.NamedParam " +
                        "<a href='https://docs.oracle.com/javase/8/docs/api/java/util/List.html' title='List'>List</a> ctorParam" +
                        ")</h4>"
        )).matcher(groovydoc).find());

        assertTrue("The Java ctor details should have the annotation", Pattern.compile(Pattern.quote(
                "<h4>@<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>\n" +
                        "public&nbsp;<strong>Java</strong>()</h4>"
        )).matcher(javadoc).find());

        // Note also the param annotation
        assertTrue("The Groovy method details should have the annotations", Pattern.compile(Pattern.quote(
                "<h4>@groovy.transform.NamedVariant\n" +
                        "void <strong>annotatedMethod</strong>(" +
                        "@groovy.transform.NamedParam(required: true) " +
                        "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/String.html' title='String'>String</a> methodParam" +
                        ")</h4>"
        )).matcher(groovydoc).find());

        assertTrue("The Java method details should have the annotations", Pattern.compile(Pattern.quote(
                "<h4>@<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>\n" +
                        "public&nbsp;void <strong>annotatedMethod</strong>(" +
                        "@CommandLine.Parameters(hidden = true) " +
                        "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/String.html' title='String'>String</a> annotatedParam" +
                        ")</h4>"
        )).matcher(javadoc).find());
    }

    public void testAbstractMethods() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
            base + "/GroovyClassWithMultipleInterfaces.groovy",
            base + "/JavaClassWithDiamond.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/GroovyClassWithMultipleInterfaces.html");
        final String javadoc = StringGroovyMethods.normalize(output.getText(MOCK_DIR + "/" + base + "/JavaClassWithDiamond.html"));

        final Pattern methodSummary = Pattern.compile("<code>(public&nbsp;)?abstract&nbsp;void</code>");
        final Pattern methodDetails = Pattern.compile("<h4>(public&nbsp;)?abstract&nbsp;void <strong>link</strong>");

        assertTrue("The Groovy method summary should contain 'abstract'", methodSummary.matcher(groovydoc).find());
        assertTrue("The Java method summary should contain 'abstract'", methodSummary.matcher(javadoc).find());
        assertTrue("The Groovy method details should contain 'abstract'", methodDetails.matcher(groovydoc).find());
        assertTrue("The Java method details should contain 'abstract'", methodDetails.matcher(javadoc).find());
    }

    public void testLinksToSamePackage() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
                base + "/GroovyInterface1.groovy",
                base + "/JavaClassWithDiamond.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/GroovyInterface1.html");
        final String javadoc = StringGroovyMethods.normalize(output.getText(MOCK_DIR + "/" + base + "/JavaClassWithDiamond.html"));

        final Matcher groovyClassComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html#link()' title='Java'>Java</a> " +
                        "<DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html' title='JavaClassWithDiamond'>JavaClassWithDiamond</a></DD>" +
                        "</DL></p>"
        )).matcher(groovydoc);
        final Matcher groovyMethodComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html#link()' title='Java link'>Java link</a> " +
                        "<DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html#link()' title='JavaClassWithDiamond.link'>JavaClassWithDiamond.link</a></DD>" +
                        "</DL></p>"
        )).matcher(groovydoc);
        final Matcher javaClassComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html#link()' title='Groovy link'>Groovy link</a>\n" +
                        "  <DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html' title='GroovyInterface1'>GroovyInterface1</a></DD>" +
                        "</DL></p>"
        )).matcher(javadoc);
        final Matcher javaMethodComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html#link()' title='Groovy link'>Groovy link</a>\n" +
                        "      <DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html#link()' title='GroovyInterface1.link'>GroovyInterface1.link</a></DD>" +
                        "</DL></p>"
        )).matcher(javadoc);

        assertTrue("The Groovy class comment should contain links", groovyClassComment.find());
        assertTrue("The Groovy method comment should contain links", groovyMethodComment.find());
        assertTrue("The Java class comment should contain links", javaClassComment.find());
        assertTrue("The Java method comment should contain links", javaMethodComment.find());
    }

    public void testPrivateDefaultConstructor() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
            base + "/GroovyClassWithMultipleInterfaces.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/GroovyClassWithMultipleInterfaces.html");

        final Matcher matcher = Pattern.compile(Pattern.quote("GroovyClassWithMultipleInterfaces()")).matcher(groovydoc);

        assertFalse("Private ctor should not be listed", matcher.find());
    }

    public void testScript() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/Script.groovy");
        xmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String scriptDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Script.html");
        assertNotNull("Expected to find Script.html in: " + output, scriptDoc);
        assertTrue("There should be a reference to method sayHello in: " + scriptDoc, containsTagWithName(scriptDoc, "method", "sayHello"));
        assertTrue("Expecting say Hello in:\n" + scriptDoc, scriptDoc.contains("Use this to say Hello"));

        assertTrue("There should be a reference to method sayGoodbye", containsTagWithName(scriptDoc, "method", "sayGoodbye"));
        assertTrue("Expecting bid farewell in:\n" + scriptDoc, scriptDoc.contains("Use this to bid farewell"));

        assertTrue("There should be a reference to property instanceProp in:\n" + scriptDoc, containsTagWithName(scriptDoc, "property", "instanceProp"));

        assertTrue("There should be a reference to field staticField", containsTagWithName(scriptDoc, "field", "staticField"));

        assertFalse("Script local variables should not appear in groovydoc output", scriptDoc.contains("localVar"));
    }

    private boolean containsTagWithName(String text, String tagname, String name) {
        return text.matches("(?s).*<"+ tagname + "[^>]* name=\""+ name + "\".*");
    }

    private GroovyClassDoc getGroovyClassDocByName(GroovyRootDoc root, String name) {
        GroovyClassDoc[] classes = root.classes();

        for (GroovyClassDoc clazz : classes) {
            if (clazz.getFullPathName().endsWith(name)) {
                return clazz;
            }
        }

        return null;
    }
}
