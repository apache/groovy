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
package groovy.inspect;

import groovy.lang.GroovyShell;
import groovy.lang.MetaProperty;
import groovy.lang.PropertyValue;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InspectorTest extends MockObjectTestCase implements Serializable {
    public String someField = "only for testing";
    public static final String SOME_CONST = "only for testing";

    public InspectorTest(String name) {
        super(name);
    }

    // additional constructor not used directly but exercises inspection code
    public InspectorTest(String name, Object other) throws RuntimeException, Throwable {
        super(name);
    }

    public void testCtor() {
        Object object = new Object();
        Inspector inspector = new Inspector(object);
        assertEquals(object, inspector.getObject());
        try {
            new Inspector(null);
            fail("should have thown IllegalArgumentException");
        } catch (Exception expected) {
        }
    }

    public void testClassPropsJava() {
        Inspector insp = new Inspector(this);
        String[] classProps = insp.getClassProps();
        assertEquals("package groovy.inspect", classProps[Inspector.CLASS_PACKAGE_IDX]);
        assertEquals("public class InspectorTest", classProps[Inspector.CLASS_CLASS_IDX]);
        assertEquals("implements Serializable ", classProps[Inspector.CLASS_INTERFACE_IDX]);
        assertEquals("extends MockObjectTestCase", classProps[Inspector.CLASS_SUPERCLASS_IDX]);
        assertEquals("is Primitive: false, is Array: false, is Groovy: false", classProps[Inspector.CLASS_OTHER_IDX]);
    }

    public void testClassPropsGroovy() {
        Object testObject = new GroovyShell().evaluate("class Test {def meth1(a,b){}}\nreturn new Test()");
        Inspector insp = new Inspector(testObject);
        String[] classProps = insp.getClassProps();
        assertEquals("package n/a", classProps[Inspector.CLASS_PACKAGE_IDX]);
        assertEquals("public class Test", classProps[Inspector.CLASS_CLASS_IDX]);
        assertEquals("implements GroovyObject ", classProps[Inspector.CLASS_INTERFACE_IDX]);
        assertEquals("extends Object", classProps[Inspector.CLASS_SUPERCLASS_IDX]);
        assertEquals("is Primitive: false, is Array: false, is Groovy: true", classProps[Inspector.CLASS_OTHER_IDX]);
    }

    public void testMethods() {
        Inspector insp = new Inspector(new Object());
        Object[] methods = insp.getMethods();
        assertEquals(10, methods.length);
        String[] names = {"hashCode", "getClass", "wait", "wait", "wait", "equals", "notify", "notifyAll", "toString", "java.lang.Object"};
        assertNameEquals(names, methods);
        String[] details = {"JAVA", "public final", "Object", "void", "wait", "long, int", "InterruptedException"};
        assertContains(methods, details);
        // ctors are not considered static !
        String[] ctorDetails = {"JAVA", "public", "Object", "Object", "java.lang.Object", "", ""};
        assertContains(methods, ctorDetails);
    }

    public void testStaticMethods() {
        Inspector insp = new Inspector(this);
        Object[] methods = insp.getMethods();
        for (int i = 0; i < methods.length; i++) {
            String[] strings = (String[]) methods[i];
            if (strings[1].indexOf("static") > -1) return; // ok, found one static method
        }
        fail("there should have been at least one static method in this TestCase, e.g. 'fail'.");
    }

    public void testMetaMethods() {
        Inspector insp = new Inspector(new Object());
        Object[] metaMethods = insp.getMetaMethods();
        String[] names = {"sleep", "sleep", "println", "println", "println", "find", "find", "findResult", "findResult",
                "print", "print", "each", "invokeMethod", "asType", "inspect", "is", "isCase", "identity", "getAt",
                "putAt", "dump", "getMetaPropertyValues", "getProperties", "use", "use", "use", "printf", "printf",
                "eachWithIndex", "every", "every", "any", "any", "grep", "grep", "collect", "collect", "collect", "findAll","findAll",
                "split", "findIndexOf", "findIndexOf", "findLastIndexOf", "findLastIndexOf", "findIndexValues", "findIndexValues",
                "iterator", "addShutdownHook", "sprintf", "sprintf", "with", "inject", "inject", "getMetaClass", "setMetaClass",
                "metaClass", "respondsTo", "respondsTo", "hasProperty", "toString", "asBoolean"
        };
        assertEquals("Incorrect number of methods found examining: " + getNamesFor(metaMethods), names.length, metaMethods.length);
        assertNameEquals(names, metaMethods);
        String[] details = {"GROOVY", "public", "Object", "void", "println", "Object", "n/a"};
        assertContains(metaMethods, details);
    }

    static class ClassWithPrivate {
        private String hidden = "you can't see me";
    }

    // TODO: if our code can never access inspect in this way, it would be better
    // to move this to a boundary class and then we wouldn't need this test
    public void testInspectPrivateField() throws NoSuchFieldException {
        ClassWithPrivate underInspection = new ClassWithPrivate();
        Field field = underInspection.getClass().getDeclaredField("hidden");
        Inspector inspector = getTestableInspector(underInspection);
        String[] result = inspector.fieldInfo(field);
        assertEquals(Inspector.NOT_APPLICABLE, result[Inspector.MEMBER_VALUE_IDX]);
    }

    // TODO: if our code can never access inspect in this way, it would be better
    // to move this to a boundary class and then we wouldn't need this test
    public void testInspectUninspectableProperty() {
        Object dummyInstance = new Object();
        Inspector inspector = getTestableInspector(dummyInstance);
        Class[] paramTypes = {Object.class, MetaProperty.class};
        Object[] params = {null, null};
        Mock mock = mock(PropertyValue.class, paramTypes, params);
        mock.expects(once()).method("getType");
        mock.expects(once()).method("getName");
        mock.expects(once()).method("getValue").will(throwException(new RuntimeException()));
        PropertyValue propertyValue = (PropertyValue) mock.proxy();
        String[] result = inspector.fieldInfo(propertyValue);
        assertEquals(Inspector.NOT_APPLICABLE, result[Inspector.MEMBER_VALUE_IDX]);
    }

    private Inspector getTestableInspector(Object objectUnderInspection) {
        return new Inspector(objectUnderInspection) {
            public String[] fieldInfo(Field field) {
                return super.fieldInfo(field);
            }

            public String[] fieldInfo(PropertyValue pv) {
                return super.fieldInfo(pv);
            }
        };
    }

    public void testSortWithDifferentOrigin() {
        String[] details2 = {"JAVA", "public", "Object", "void", "println", "Object", "n/a"};
        String[] details1 = {"GROOVY", "public", "Object", "void", "println", "Object", "n/a"};
        String[] first = sortWithMemberComparator(details1, details2);
        assertEquals("GROOVY", first[0]);
    }

    public void testSortWithDifferentModifier() {
        String[] details2 = {null, "public", "Object", "void", "println", "Object", "n/a"};
        String[] details1 = {null, "private", "Object", "void", "println", "Object", "n/a"};
        String[] first = sortWithMemberComparator(details1, details2);
        assertEquals("private", first[1]);
    }

    private String[] sortWithMemberComparator(String[] details1, String[] details2) {
        List details = new ArrayList();
        details.add(details1);
        details.add(details2);
        Inspector.sort(details);
        return (String[]) details.get(0);
    }

    public void testStaticMetaMethods() {
        Matcher matcher = Pattern.compile("").matcher("");
        Inspector insp = new Inspector(matcher);
        Object[] metaMethods = insp.getMetaMethods();
        assertUnique(Inspector.sort(Arrays.asList(metaMethods)));
        String[] details = {"GROOVY", "public static", "Matcher", "Matcher", "getLastMatcher", "", "n/a"};
        assertContains(metaMethods, details);
    }

    public void testFields() {
        Inspector insp = new Inspector(this);
        Object[] fields = insp.getPublicFields();
        assertEquals(5, fields.length); // 3 from JMock
        String[] names = {"someField", "SOME_CONST", "ANYTHING", "NULL", "NOT_NULL"};
        assertNameEquals(names, fields);
        String[] details = {"JAVA", "public", "InspectorTest", "String", "someField", "'only for testing'"};
        assertContains(fields, details);
    }

    public void testProperties() {
        Inspector insp = new Inspector(this);
        Object[] properties = insp.getPropertyInfo();
        assertEquals(2, properties.length);
        String[] names = {"class", "name"};
        assertNameEquals(names, properties);
        String[] details = {"GROOVY", "public", "n/a", "Class", "class", "class groovy.inspect.InspectorTest"};
        assertContains(properties, details);
    }

    public void testPrint() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(bytes);
        String ls = System.lineSeparator();
        String[] first = {"a", "b"};
        String[] second = {"x", "y"};
        Object[] memberInfo = {first, second};
        Inspector.print(printStream, memberInfo);
        assertEquals("0:\ta b " + ls + "1:\tx y " + ls, bytes.toString());
        // just for coverage, print to System.out (yuck)
        Inspector.print(memberInfo);
    }

    private List getNamesFor(Object[] metaMethods) {
        List result = new ArrayList();
        for (int i = 0; i < metaMethods.length; i++) {
            String[] strings = (String[]) metaMethods[i];
            result.add(strings[Inspector.MEMBER_NAME_IDX]);
        }
        return result;
    }

    private void assertNameEquals(String[] names, Object[] metaMethods) {
        Set metaSet = new HashSet();
        for (int i = 0; i < metaMethods.length; i++) {
            String[] strings = (String[]) metaMethods[i];
            metaSet.add(strings[Inspector.MEMBER_NAME_IDX]);
        }
        Set nameSet = new HashSet(Arrays.asList(names));
        assertEquals(nameSet, metaSet);
    }

    private void assertContains(Object[] candidates, String[] sample) {
        String sampleBuffer = concat(sample);
        for (int i = 0; i < candidates.length; i++) {
            String[] entry = (String[]) candidates[i];
            if (sampleBuffer.equals(concat(entry))) return;
        }
        fail("should have found sample: " + sampleBuffer);
    }

    private void assertUnique(Collection sortedMembers) {
        if (sortedMembers.size() < 2) return;
        Comparator comp = new Inspector.MemberComparator();
        Iterator iter = sortedMembers.iterator();
        Object last = iter.next();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (0 == comp.compare(last, element)) {
                fail("found duplication for element " + element);
            }
            last = element;
        }
    }

    private String concat(String[] details) {
        StringBuilder detailBuffer = new StringBuilder();
        for (int i = 0; i < details.length; i++) {
            detailBuffer.append(details[i]);
            detailBuffer.append(" ");
        }
        return detailBuffer.toString();
    }

}
