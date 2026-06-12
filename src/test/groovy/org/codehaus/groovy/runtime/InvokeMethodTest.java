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
package org.codehaus.groovy.runtime;

import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.IntRange;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests method invocation.
 */
final class InvokeMethodTest {

    @Test
    void testInvokeMethodNoParams() {
        Object value = invoke(this, "mockCallWithNoParams", null);
        assertEquals("NoParams", value);

        value = invoke(this, "mockCallWithNoParams", new Object[0]);
        assertEquals("NoParams", value);
    }

    @Test
    void testInvokeMethodOneParam() {
        Object value = invoke(this, "mockCallWithOneParam", "abc");
        assertEquals("OneParam", value);
    }

    @Test
    void testInvokeMethodOneParamWhichIsNull() {
        Object value = invoke(this, "mockCallWithOneNullParam", new Object[]{null});
        assertEquals("OneParamWithNull", value);

        value = invoke(this, "mockCallWithOneNullParam", null);
        assertEquals("OneParamWithNull", value);
    }

    @Test
    void testInvokeOverloadedMethodWithOneParamWhichIsNull() {
        Object value = invoke(this, "mockOverloadedMethod", new Object[]{null});
        assertEquals("Object", value);
    }

    @Test
    void testInvokeMethodOneCollectionParameter() {
        Object[] foo = {"a", "b", "c"};

        Object value = invoke(this, "mockCallWithOneCollectionParam", new Object[]{foo});
        assertEquals(Integer.valueOf(3), value);

        var list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        value = invoke(this, "mockCallWithOneCollectionParam", list);
        assertEquals(Integer.valueOf(2), value);
    }

    @Test
    void testInvokePrintlnMethod() {
        Object value = invoke(System.out, "println", "testing System.out.println...");
        assertEquals(null, value);
    }

    @Test
    void testMethodChooserNull() {
        assertMethodChooser("Object", new Object[]{null});
    }

    @Test
    void testMethodChooserNoParams() {
        assertMethodChooser("void", null);
    }

    @Test
    void testMethodChooserObject() {
        assertMethodChooser("Object", new Object());
        assertMethodChooser("Object", new Date());
        assertMethodChooser("Object", new StringBuffer());
        assertMethodChooser("Object", Character.valueOf('a'));
    }

    @Test
    void testMethodChooserString() {
        assertMethodChooser("String", "foo");
    }

    @Test
    void testMethodChooserNumber() {
        assertMethodChooser("Number", Integer.valueOf(2));
        assertMethodChooser("Number", Double.valueOf(2));
    }

    @Test
    void testMethodChooserTwoParams() {
        var list = new ArrayList<String>();
        list.add("foo");
        list.add("bar");
        assertMethodChooser("Object,Object", list.toArray());

        Object[] blah = {"a", "b"};
        assertMethodChooser("Object,Object", blah);
    }

    @Test
    void testInstanceofWorksForArray() {
        Class<?> type = Object[].class;
        Object value = new Object[1];
        assertTrue(type.isInstance(value), "instanceof works for array");
    }

    @Test
    void testMethodChooserTwoParamsWithSecondAnObjectArray() {
        Object[] blah = {"a", new Object[]{"b"}};
        assertMethodChooser("Object,Object[]", blah);
    }

    @Test
    void testCollectionMethods() {
        Object list = InvokerHelper.createList(new Object[]{"a", "b"});

        Object value = invoke(list, "size", null);
        assertEquals(2, value, "size of collection");

        value = invoke(list, "contains", "a");
        assertEquals(true, value, "contains method");
    }

    @Test
    void testNewMethods() {
        Object value = invoke("hello", "size", null);
        assertEquals(5, value, "size of string");
    }

    @Test
    void testStaticMethod() {
        Object value = invoke(DummyBean.class, "dummyStaticMethod", "abc");
        assertEquals("ABC", value);
    }

    @Test
    void testBaseClassMethod() {
        Object object = new DummyBean();
        Object value = invoke(object, "toString", null);
        assertEquals(object.toString(), value);
    }

    // SPG modified to reflect DefaultGroovyMethod name change and expected result from Integer/Integer division.
    @Test
    void testDivideNumbers() {
        assertMethodCall(Double .valueOf(10), "div", Double .valueOf(2), Double.valueOf(5));
        assertMethodCall(Double .valueOf(10), "div", Integer.valueOf(2), Double.valueOf(5));
        assertMethodCall(Integer.valueOf(10), "div", Double .valueOf(2), Double.valueOf(5));
        assertMethodCall(Integer.valueOf(10), "div", Integer.valueOf(2), new BigDecimal("5"));
    }

    @Test
    void testBaseFailMethod() {
        assertThrows(MissingMethodException.class, () -> invoke(this, "fail", "hello"));
    }

    @Test
    void testToArrayOnList() {
        var list = new ArrayList<String>();
        list.add("Hello");

        Object[] value = (Object[]) invoke(list, "toArray", null);
        assertArrayEquals(list.toArray(), value);
        assertEquals(1, value.length);
        assertEquals("Hello", value[0]);

        value = (Object[]) invoke(list, "toArray", new Object[0]);
        assertArrayEquals(list.toArray(), value);
    }

    @Test
    void testInvalidOverloading() {
        assertThrows(GroovyRuntimeException.class,
                () -> invoke(this, "badOverload", new Object[]{"a", "b"}),
                "Should fail as an ambiguous method is invoked");
    }

    @Test
    void testPlusWithNull() {
        String param = "called with: ";
        Object value = invoke(param, "plus", new Object[]{null});
        assertEquals(param + null, value);
    }

    @Test
    void testCallIntMethodWithInteger() {
        Object value = invoke(this, "overloadedRemove", new Object[]{Integer.valueOf(5)});
        assertEquals("int5", value);
    }

    @Test
    void testCallListRemove() {
        var list = new ArrayList<String>();
        list.add("foo");
        list.add("bar");

        invoke(list, "remove", new Object[]{Integer.valueOf(0)});

        assertEquals(1, list.size(), () -> "Should have just 1 item left: " + list);
    }

    @Test
    void testCoerceGStringToString() {
        GString param = new GString(new Object[]{"James"}) {
            public String[] getStrings() {
                return new String[]{"Hello "};
            }
        };
        Object value = invoke(this, "methodTakesString", new Object[]{param});
        assertEquals(param.toString(), value);
    }

    @Test
    void testCoerceGStringToStringOnGetBytes() throws Exception {
        GString param = new GString(new Object[]{CompilerConfiguration.DEFAULT_SOURCE_ENCODING}) {
            public String[] getStrings() {
                return new String[]{""};
            }
        };
        Object value = invoke("test", "getBytes", new Object[]{param});
        assertEquals("test".getBytes(CompilerConfiguration.DEFAULT_SOURCE_ENCODING).getClass(), value.getClass());
    }

    @Test
    void testBadBDToDoubleCoerce() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> invoke(Math.class, "floor", new BigDecimal("1.7E309")),
                "Math.floor(1.7E309) should fail because it is out of range for a Double.");
        assertTrue(e.getMessage().indexOf("out of range") > 0, "Math.floor(1.7E309) should fail because it is out of range for a Double. " + e);
    }

    // GROOVY-11203
    @Test
    void testBadNullToBoolCoerce() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> invoke(Boolean.class, "toString", ScriptBytecodeAdapter.createPojoWrapper(null, boolean.class)),
                "Boolean.toString(null) should fail because null cannot be cast to boolean.");
        assertEquals("Cannot call method with null for parameter 0, which expects boolean", e.getMessage());
    }

    // GROOVY-11203
    @Test
    void testBadNullToIntCoerce() {
        var e = assertThrows(IllegalArgumentException.class,
                () -> invoke(Integer.class, "toString", ScriptBytecodeAdapter.createPojoWrapper(null, int.class)),
                "Integer.toString(null) should fail because null cannot be cast to int.");
        assertEquals("Cannot call method with null for parameter 0, which expects int", e.getMessage());
    }

    // GROOVY-11930
    @Test
    void testGoodNullToStringCoerce() {
        assertThrows(NumberFormatException.class,
                () -> invoke(Integer.class, "parseInt", ScriptBytecodeAdapter.createPojoWrapper(null, String.class)));
    }

    @Test
    void testClassMethod() {
        Class<?> c = String.class;
        Object value = invoke(c, "getName", null);
        assertEquals(c.getName(), value);
        c = getClass();
        value = invoke(c, "getName", null);
        assertEquals(c.getName(), value);
    }

    @Test
    void testProtectedMethod() {
        String param = "hello";
        Object value = invoke(this, "aProtectedMethod", param);
        assertEquals(aProtectedMethod(param), value);
    }

    @Test
    void testPrivateMethod() {
        String param = "hello";
        Object value = invoke(this, "aPrivateMethod", param);
        assertEquals(aPrivateMethod(param), value);
    }

    @Test
    void testStringSubstringMethod() {
        String object = "hello";
        Object value = invoke(object, "substring", Integer.valueOf(2));
        assertEquals(object.substring(2), value);

        value = invoke(object, "substring", new Object[]{Integer.valueOf(1), Integer.valueOf(3)});
        assertEquals(object.substring(1, 3), value);
    }

    @Test
    void testListGetWithRange() {
        var list = Arrays.<Object>asList("a", "b", "c");
        Object range = new IntRange(true, 0, 2);
        Object value = invoke(list, "getAt", range);
        assertTrue(value instanceof List);
        assertEquals(3, ((List<?>) value).size());
    }

    @Test
    void testSetLenientOnDateFormat() {
        SimpleDateFormat a = new SimpleDateFormat("MM/dd/yyyy");

        Object value = invoke(a, "setLenient", new Object[]{Boolean.FALSE});
        assertEquals(null, value);
    }

    @Test
    void testInvokeUnknownMethod() {
        assertThrows(GroovyRuntimeException.class,
            () -> invoke(this, "unknownMethod", "abc"));
    }

    @Test
    void testInvokeMethodWithWrongNumberOfParameters() {
        Object[] args = {"a", "b"};
        assertThrows(GroovyRuntimeException.class,
                () -> invoke(this, "unknownMethod", args));
    }

    @Test
    void testInvokeMethodOnNullObject() {
        assertThrows(NullPointerException.class,
                () -> invoke(null, "mockCallWithNoParams", null));
    }

    // Mock methods used for testing
    //--------------------------------------------------------------------------

    public Object mockCallWithNoParams() {
        return "NoParams";
    }

    public Object mockCallWithOneParam(Object value) {
        assertEquals("abc", value, "Method not passed in the correct value");
        return "OneParam";
    }

    public Object mockCallWithOneNullParam(Object value) {
        assertEquals(null, value, "Method not passed in the correct value");
        return "OneParamWithNull";
    }

    public Integer mockCallWithOneCollectionParam(Object collection) {
        var col = DefaultTypeTransformation.asCollection(collection);
        return Integer.valueOf(col.size());
    }

    public Object mockOverloadedMethod() {
        return "void";
    }

    public Object mockOverloadedMethod(Object object) {
        return "Object";
    }

    public Object mockOverloadedMethod(Number object) {
        return "Number";
    }

    public Object mockOverloadedMethod(String object) {
        return "String";
    }

    public Object mockOverloadedMethod(Object object, Object bar) {
        return "Object,Object";
    }

    public Object mockOverloadedMethod(Object object, Object[] array) {
        return "Object,Object[]";
    }

    public Object badOverload(String a, Object b) {
        return "String, Object";
    }

    public Object badOverload(Object a, String b) {
        return "Object, String";
    }

    public Object methodTakesString(String x) {
        return x;
    }

    public Object overloadedRemove(int idx) {
        return "int" + idx;
    }

    public Object overloadedRemove(Object value) {
        return "Object" + value;
    }

    protected Object aProtectedMethod(String param) {
        return param + " there!";
    }

    private Object aPrivateMethod(String param) {
        return param + " James!";
    }

    // Implementation methods
    //--------------------------------------------------------------------------

    private void assertMethodCall(Object object, String method, Object param, Object expected) {
        Object value = InvokerHelper.invokeMethod(object, method, new Object[]{param});
        assertEquals(expected, value, () -> "result of method: " + method);
    }

    /**
     * Asserts that invoking the method chooser finds the right overloaded
     * method implementation
     *
     * @param expected  the expected value of the method
     * @param arguments the argument(s) to the method invocation
     */
    private void assertMethodChooser(Object expected, Object arguments) {
        Object value = invoke(this, "mockOverloadedMethod", arguments);

        assertEquals(expected, value, () -> "Invoking overloaded method for arguments: " + FormatHelper.toString(arguments));
    }

    private Object invoke(Object object, String method, Object args) {
        try {
            return InvokerHelper.invokeMethod(object, method, args);
        } catch (InvokerInvocationException e) {
            throw (RuntimeException) e.getCause();
        }
    }
}
