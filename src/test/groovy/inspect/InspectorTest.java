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
import groovy.lang.PropertyValue;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("serial")
public class InspectorTest implements Serializable {

    public String someField = "only for testing";

    public static final String SOME_CONST = "only for testing";

    public static void main(String[] args) {
    }

    private static void assertUnique(Collection<?> sortedMembers) {
        if (sortedMembers.size() > 1) {
            Comparator<Object> comp = new Inspector.MemberComparator();
            Iterator<?> iter = sortedMembers.iterator();
            Object last = iter.next();
            while (iter.hasNext()) {
                Object element = iter.next();
                if (comp.compare(last, element) == 0) {
                    fail("found duplication for element " + element);
                }
                last = element;
            }
        }
    }

    private static void assertContains(Object[] candidates, String[] sample) {
        String sampleBuffer = String.join(" ", sample);
        for (int i = 0; i < candidates.length; i++) {
            String[] entry = (String[]) candidates[i];
            if (sampleBuffer.equals(String.join(" ", entry))) return;
        }
        fail("should have found sample: " + sampleBuffer);
    }

    private static void assertNameEquals(String[] names, Object[] metaMethods) {
        Set<String> metaSet = new TreeSet<>();
        for (Object metaMethod : metaMethods) {
            String[] strings = (String[]) metaMethod;
            metaSet.add(strings[Inspector.MEMBER_NAME_IDX]);
        }
        Set<String> nameSet = new TreeSet<>(Arrays.asList(names));
        assertEquals(nameSet, metaSet);
    }

    private static Inspector getTestableInspector(Object objectUnderInspection) {
        return new Inspector(objectUnderInspection) {
            public String[] fieldInfo(Field field) {
                return super.fieldInfo(field);
            }

            public String[] fieldInfo(PropertyValue pv) {
                return super.fieldInfo(pv);
            }
        };
    }

    private static String[] sortWithMemberComparator(String[] details1, String[] details2) {
        List<Object> details = new ArrayList<>();
        details.add(details1);
        details.add(details2);
        Inspector.sort(details);
        return (String[]) details.get(0);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testCtor() {
        Object object = new Object();
        Inspector inspector = new Inspector(object);
        assertEquals(object, inspector.getObject());

        try {
            new Inspector(null);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testClassPropsJava() {
        Inspector inspector = new Inspector(this);
        String[] classProps = inspector.getClassProps();
        assertEquals("package groovy.inspect",     classProps[Inspector.CLASS_PACKAGE_IDX]);
        assertEquals("public class InspectorTest", classProps[Inspector.CLASS_CLASS_IDX]);
        assertEquals("implements Serializable ",   classProps[Inspector.CLASS_INTERFACE_IDX]);
        assertEquals("extends Object",             classProps[Inspector.CLASS_SUPERCLASS_IDX]);
        assertEquals("is Primitive: false, is Array: false, is Groovy: false", classProps[Inspector.CLASS_OTHER_IDX]);
    }

    @Test
    public void testClassPropsGroovy() {
        Object testObject = new GroovyShell().evaluate("class Test {def meth1(a,b){}}\nreturn new Test()");
        Inspector inspector = new Inspector(testObject);
        String[] classProps = inspector.getClassProps();
        // TODO investigate "n/a" for JDK8, "" for JDK9+
        //assertEquals("package ", classProps[Inspector.CLASS_PACKAGE_IDX]);
        assertEquals("public class Test", classProps[Inspector.CLASS_CLASS_IDX]);
        assertEquals("implements GroovyObject ", classProps[Inspector.CLASS_INTERFACE_IDX]);
        assertEquals("extends Object", classProps[Inspector.CLASS_SUPERCLASS_IDX]);
        assertEquals("is Primitive: false, is Array: false, is Groovy: true", classProps[Inspector.CLASS_OTHER_IDX]);
    }

    @Test
    public void testMethods() {
        Inspector inspector = new Inspector(new Object());
        Object[] methods = inspector.getMethods();
        assertEquals(10, methods.length);
        String[] names = {"hashCode", "getClass", "wait", "wait", "wait", "equals", "notify", "notifyAll", "toString", "java.lang.Object"};
        assertNameEquals(names, methods);
        String[] details = {"JAVA", "public final", "Object", "void", "wait", "long, int", "InterruptedException"};
        assertContains(methods, details);
        // ctors are not considered static !
        String[] ctorDetails = {"JAVA", "public", "Object", "Object", "java.lang.Object", "", ""};
        assertContains(methods, ctorDetails);
    }

    @Test
    public void testStaticMethods() {
        Inspector inspector = new Inspector(this);
        Object[] methods = inspector.getMethods();
        for (Object method : methods) {
            String[] strings = (String[]) method;
            if (strings[1].indexOf("static") > -1) return; // ok, found one static method
        }
        fail("there should have been at least one public static method in this class");
    }

    @Test
    public void testMetaMethods() {
        Inspector inspector = new Inspector(new Object());
        Object[] metaMethods = inspector.getMetaMethods();
        String[] names = {
            "addShutdownHook", "any", "any", "asBoolean", "asType", "collect", "collect", "collect",
            "dump", "each", "eachWithIndex", "every", "every", "find", "find", "findAll", "findAll",
            "findIndexOf", "findIndexOf", "findIndexValues", "findIndexValues", "findLastIndexOf",
            "findLastIndexOf", "findResult", "findResult", "findResult", "findResult", "getAt", "getMetaClass",
            "getMetaPropertyValues", "getProperties", "grep", "grep", "hasProperty", "identity", "inject", "inject",
            "inspect", "invokeMethod", "is", "isCase", "isNotCase", "iterator", "metaClass", "print", "print",
            "printf", "printf", "println", "println", "println", "putAt", "respondsTo", "respondsTo",
            "setMetaClass", "split", "sprintf", "sprintf", "tap", "toString", "use", "use", "use", "with",
            "with", "withMethodClosure", "withTraits", "stream", "sleep", "sleep", "macro", "macro", "macro", "macro"
        };
        assertEquals("Incorrect number of methods found examining: " + Arrays.stream(metaMethods)
                .map(mm -> ((String[])mm)[Inspector.MEMBER_NAME_IDX]).collect(Collectors.toList()), names.length, metaMethods.length);
        assertNameEquals(names, metaMethods);
        String[] details = {"GROOVY", "public", "Object", "void", "println", "Object", "n/a"};
        assertContains(metaMethods, details);
    }

    // TODO: if our code can never access inspect in this way, it would be better
    // to move this to a boundary class and then we wouldn't need this test
    @Test
    public void testInspectPrivateField() throws Exception {
        ClassWithPrivate underInspection = new ClassWithPrivate();
        Field field = underInspection.getClass().getDeclaredField("hidden");
        Inspector inspector = getTestableInspector(underInspection);
        String[] result = inspector.fieldInfo(field);
        assertEquals(Inspector.NOT_APPLICABLE, result[Inspector.MEMBER_VALUE_IDX]);
    }

    // TODO: if our code can never access inspect in this way, it would be better
    // to move this to a boundary class and then we wouldn't need this test
    @Test
    public void testInspectUninspectableProperty() {
        Object object = new Object();
        Inspector inspector = getTestableInspector(object);
        final int[] name = new int[1], type = new int[1], value = new int[1];
        String[] result = inspector.fieldInfo(new PropertyValue(object, null) {
            @Override
            public String getName() {
                name[0] += 1; return "thing";
            }
            @Override
            public Class<?> getType() {
                type[0] += 1; return Object.class;
            }
            @Override
            public Object getValue() {
                value[0] += 1; throw new RuntimeException();
            }
        });
        assertEquals(1, name[0]);
        assertEquals(1, type[0]);
        assertEquals(1, value[0]);
        assertEquals(Inspector.NOT_APPLICABLE, result[Inspector.MEMBER_VALUE_IDX]);
    }

    @Test
    public void testSortWithDifferentOrigin() {
        String[] details2 = {"JAVA", "public", "Object", "void", "println", "Object", "n/a"};
        String[] details1 = {"GROOVY", "public", "Object", "void", "println", "Object", "n/a"};
        String[] first = sortWithMemberComparator(details1, details2);
        assertEquals("GROOVY", first[0]);
    }

    @Test
    public void testSortWithDifferentModifier() {
        String[] details2 = {null, "public", "Object", "void", "println", "Object", "n/a"};
        String[] details1 = {null, "private", "Object", "void", "println", "Object", "n/a"};
        String[] first = sortWithMemberComparator(details1, details2);
        assertEquals("private", first[1]);
    }

    @Test
    public void testStaticMetaMethods() {
        Matcher matcher = Pattern.compile("").matcher("");
        Inspector inspector = new Inspector(matcher);
        Object[] metaMethods = inspector.getMetaMethods();
        assertUnique(Inspector.sort(Arrays.asList(metaMethods)));
        String[] details = {"GROOVY", "public static", "Matcher", "Matcher", "getLastMatcher", "", "n/a"};
        assertContains(metaMethods, details);
    }

    @Test
    public void testFields() {
        Inspector inspector = new Inspector(this);
        Object[] fields = inspector.getPublicFields();
        assertEquals(2, fields.length);
        String[] names = {"someField", "SOME_CONST"};
        assertNameEquals(names, fields);
        String[] details = {"JAVA", "public", "InspectorTest", "String", "someField", "'only for testing'"};
        assertContains(fields, details);
    }

    @Test
    public void testProperties() {
        Inspector inspector = new Inspector(this);
        Object[] properties = inspector.getPropertyInfo();
        String[] names = {"class", "someField", "SOME_CONST"};
        assertEquals(3, properties.length);
        assertNameEquals(names, properties);
        String[] details = {"GROOVY", "public", "n/a", "Class", "class", "class groovy.inspect.InspectorTest"};
        assertContains(properties, details);
    }

    @Test
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

    //--------------------------------------------------------------------------

    static class ClassWithPrivate {
        @SuppressWarnings("unused")
        private String hidden = "you can't see me";
    }
}
