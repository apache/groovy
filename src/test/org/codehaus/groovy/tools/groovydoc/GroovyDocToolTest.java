/*
 * Copyright 2007-2013 the original author or authors.
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

import groovy.util.GroovySwingTestCase;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jeremy Rayner
 * @author Andre Steingress
 */
public class GroovyDocToolTest extends GroovySwingTestCase {
    private static final String MOCK_DIR = "mock/doc";
    private static final String TEMPLATES_DIR = "main/org/codehaus/groovy/tools/groovydoc/gstringTemplates";

    GroovyDocTool xmlTool;
    GroovyDocTool xmlToolForTests;
    GroovyDocTool plainTool;
    GroovyDocTool htmlTool;

    public void setUp() {
        plainTool = new GroovyDocTool(new String[]{"src/test"});

        xmlTool = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/main"}, // source file dirs
                new String[]{TEMPLATES_DIR + "/topLevel/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/packageLevel/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/classLevel/classDocStructuredData.xml"},
                new ArrayList<LinkArgument>(),
                new Properties()
        );

        xmlToolForTests = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/test"}, // source file dirs, // source file dirs
                new String[]{TEMPLATES_DIR + "/topLevel/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/packageLevel/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/classLevel/classDocStructuredData.xml"},
                new ArrayList<LinkArgument>(),
                new Properties()
        );

        ArrayList<LinkArgument> links = new ArrayList<LinkArgument>();
        LinkArgument link = new LinkArgument();
        link.setHref("http://download.oracle.com/javase/7/docs/api");
        link.setPackages("java.,org.xml.,javax.,org.xml.");
        links.add(link);

        htmlTool = new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                new String[] {"src/test"}, // source file dirs
                new String[]{
                        TEMPLATES_DIR + "/topLevel/index.html",
                        TEMPLATES_DIR + "/topLevel/overview-frame.html",
                        TEMPLATES_DIR + "/topLevel/allclasses-frame.html",
                        TEMPLATES_DIR + "/topLevel/overview-summary.html",
                        TEMPLATES_DIR + "/topLevel/help-doc.html",
                        TEMPLATES_DIR + "/topLevel/index-all.html",
                        TEMPLATES_DIR + "/topLevel/deprecated-list.html",
                        TEMPLATES_DIR + "/topLevel/stylesheet.css",
                        TEMPLATES_DIR + "/topLevel/inherit.gif"
                },
                new String[]{
                        TEMPLATES_DIR + "/packageLevel/package-frame.html",
                        TEMPLATES_DIR + "/packageLevel/package-summary.html"
                },
                new String[]{
                        TEMPLATES_DIR + "/classLevel/classDocName.html"
                },
                links,
                new Properties()
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
                }
            }
            assertTrue(seenThisMethod);
        }
    }

    public void testGroovyDocTheCategoryMethodClass() throws Exception {
        if (isHeadless()) {
            return;
        }
        List<String> srcList = new ArrayList<String>();
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

        String categoryMethodDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/GroovyCategorySupport.CategoryMethodList.html");
        assertTrue(categoryMethodDocument != null &&
                categoryMethodDocument.indexOf("<method modifiers=\"public \" returns=\"boolean\" name=\"add\">") > 0);

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
        if (isHeadless()) {
            return;
        }
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/ui/Console.groovy");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String consoleDoc = output.getText(MOCK_DIR + "/groovy/ui/Console.html");
        assertTrue(consoleDoc.indexOf("<constructor modifiers=\"public \" name=\"Console\">") > 0);
        assertTrue(consoleDoc.indexOf("<parameter type=\"java.lang.ClassLoader\" name=\"parent\" />") > 0);
    }

    public void testClassComment() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/xml/DOMBuilder.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("A helper class for creating a W3C DOM tree") > 0);
    }

    public void testMethodComment() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/model/DefaultTableColumn.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String defTabColDoc = output.getText(MOCK_DIR + "/groovy/model/DefaultTableColumn.html");
        assertTrue(defTabColDoc.indexOf("Evaluates the value of a cell") > 0);
    }

    public void testPackageName() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/xml/DOMBuilder.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<containingPackage name=\"groovy/xml\">groovy.xml</containingPackage>") > 0);
    }

    public void testExtendsClauseWithoutSuperClassInTree() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/xml/DOMBuilder.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<extends>BuilderSupport</extends>") > 0);
    }

    public void testExtendsClauseWithSuperClassInTree() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/xml/DOMBuilder.java");
        srcList.add("groovy/util/BuilderSupport.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String domBuilderDoc = output.getText(MOCK_DIR + "/groovy/xml/DOMBuilder.html");
        assertTrue(domBuilderDoc.indexOf("<extends>BuilderSupport</extends>") > 0);
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
        assertTrue(groovyClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterfaceWithMultipleInterfaces.html");
        assertTrue(javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
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
        assertTrue(groovyClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.html");
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
        assertTrue(groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.html");
        assertTrue(javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
    }

    public void testDefaultPackage() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("UberTestCaseBugs.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);
        String domBuilderDoc = output.getText(MOCK_DIR + "/DefaultPackage/UberTestCaseBugs.html");
        assertTrue(domBuilderDoc.indexOf("<extends>TestCase</extends>") > 0);
    }

    public void testStaticModifier() throws Exception {
        if (isHeadless()) {
            return;
        }
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/swing/binding/AbstractButtonProperties.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String abstractButtonPropertiesDoc = output.getText(MOCK_DIR + "/groovy/swing/binding/AbstractButtonProperties.html");
        assertTrue(abstractButtonPropertiesDoc.indexOf("static") > 0);
    }

    public void testAnonymousInnerClassMethodsNotIncluded() throws Exception {
        if (isHeadless()) {
            return;
        }
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/swing/binding/AbstractButtonProperties.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String abstractButtonPropertiesDoc = output.getText(MOCK_DIR + "/groovy/swing/binding/AbstractButtonProperties.html");
        assertTrue(abstractButtonPropertiesDoc.indexOf("createBinding") < 0);
    }

    public void testMultipleConstructorError() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/sql/Sql.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String sqlDoc = output.getText(MOCK_DIR + "/groovy/sql/Sql.html");
        assertTrue(sqlDoc.indexOf("<method modifiers=\"public static \" returns=\"groovy.sql.InParameter\" name=\"VARBINARY\">") > 0); // VARBINARY() and other methods in Sql.java were assumed to be Constructors, make sure they aren't anymore...
    }

    public void testReturnTypeResolution() throws Exception {
        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        srcList.add("org/codehaus/groovy/groovydoc/GroovyClassDoc.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue(text.indexOf("org.codehaus.groovy.groovydoc.GroovyClassDoc") > 0);
    }

    public void testParameterTypeResolution() throws Exception {
        List<String> srcList = new ArrayList<String>();
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
                new String[]{TEMPLATES_DIR + "/topLevel/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/packageLevel/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/classLevel/classDocStructuredData.xml"},
                new ArrayList<LinkArgument>(),
                new Properties()
        );

        List<String> srcList = new ArrayList<String>();
        srcList.add("groovy/model/DefaultTableColumn.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/GroovyDocToolTestSampleGroovy.groovy");
        multipleXmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        multipleXmlTool.renderToOutput(output, MOCK_DIR);
        assertTrue(output.getText(MOCK_DIR + "/groovy/model/DefaultTableColumn.html") != null);
        assertTrue(output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/GroovyDocToolTestSampleGroovy.html") != null);
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
        assertTrue(fullPathBaseA.equals(root.classNamed(classDocDescendantA, "Base").getFullPathName()));

        GroovyClassDoc classDocDescendantB = getGroovyClassDocByName(root, "DescendantB");
        assertTrue(fullPathBaseB.equals(root.classNamed(classDocDescendantB, "Base").getFullPathName()));

        GroovyClassDoc classDocDescendantC = getGroovyClassDocByName(root, "DescendantC");
        assertTrue(fullPathBaseA.equals(root.classNamed(classDocDescendantC, "Base").getFullPathName()));

        GroovyClassDoc classDocDescendantD = getGroovyClassDocByName(root, "DescendantD");
        assertTrue(fullPathBaseA.equals(root.classNamed(classDocDescendantD, "Base").getFullPathName()));

        GroovyClassDoc classDocDescendantE = getGroovyClassDocByName(root, "DescendantE");
        assertTrue(fullPathBaseC.equals(root.classNamed(classDocDescendantE, "Base").getFullPathName()));

        GroovyClassDoc classDocDescendantF = getGroovyClassDocByName(root, "DescendantF");
        assertTrue(fullPathBaseC.equals(root.classNamed(classDocDescendantF, "Base").getFullPathName()));
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
        assertEquals("There has to be at least a single reference to the ArrayPropertyLink[]", "ArrayPropertyLink", m.group(2));
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

        Pattern p = Pattern.compile("<a(.+?)testfiles/InnerEnum.Enum.html'>(.+?)</a>");
        Matcher m = p.matcher(derivDoc);

        assertTrue(m.find());
        assertEquals("There has to be a reference to class Enum", "Enum", m.group(2));
    }

    public void testClassAliasing() throws Exception {

        List<String> srcList = new ArrayList<String>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/Alias.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String derivDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Alias.html");

        Pattern p = Pattern.compile("<a(.+?)java/util/ArrayList.html' title='ArrayList'>(.+?)</a>");
        Matcher m = p.matcher(derivDoc);

        assertTrue(m.find());
        assertEquals("There has to be a reference to class ArrayList", "ArrayList", m.group(2));
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
