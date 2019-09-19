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
import groovy.test.GroovyTestCase;
import junit.framework.AssertionFailedError;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Tests method invocation
 */
public class InvokeMethodTest extends GroovyTestCase {

    // Method invocation tests
    //-------------------------------------------------------------------------

    public void testInvokeMethodNoParams() throws Throwable {
        Object value = invoke(this, "mockCallWithNoParams", null);
        assertEquals("return value", "NoParams", value);

        value = invoke(this, "mockCallWithNoParams", new Object[0]);
        assertEquals("return value", "NoParams", value);
    }

    public void testInvokeMethodOneParam() throws Throwable {
        Object value = invoke(this, "mockCallWithOneParam", "abc");
        assertEquals("return value", "OneParam", value);
    }

    public void testInvokeMethodOneParamWhichIsNull() throws Throwable {
        Object value = invoke(this, "mockCallWithOneNullParam", new Object[]{null});
        assertEquals("return value", "OneParamWithNull", value);

        value = invoke(this, "mockCallWithOneNullParam", null);
        assertEquals("return value", "OneParamWithNull", value);
    }

    public void testInvokeOverloadedMethodWithOneParamWhichIsNull() throws Throwable {
        Object value = invoke(this, "mockOverloadedMethod", new Object[]{null});
        assertEquals("return value", "Object", value);
    }

    public void testInvokeMethodOneCollectionParameter() throws Throwable {
        Object[] foo = {"a", "b", "c"};

        Object value = invoke(this, "mockCallWithOneCollectionParam", new Object[]{foo});
        assertEquals("return value", new Integer(3), value);

        List list = new ArrayList();
        list.add("a");
        list.add("b");
        value = invoke(this, "mockCallWithOneCollectionParam", list);
        assertEquals("return value", new Integer(2), value);
    }

    public void testInvokePrintlnMethod() throws Throwable {
        Object value = invoke(System.out, "println", "testing System.out.println...");
        assertEquals("return value", null, value);
    }

    public void testMethodChooserNull() throws Throwable {
        assertMethodChooser("Object", new Object[]{null});
    }

    public void testMethodChooserNoParams() throws Throwable {
        assertMethodChooser("void", null);
    }

    public void testMethodChooserObject() throws Throwable {
        assertMethodChooser("Object", new Object());
        assertMethodChooser("Object", new Date());
    }

    public void testMethodChooserString_FAILS() throws Throwable {
        if (notYetImplemented()) return;
        assertMethodChooser("String", "foo");
        assertMethodChooser("String", new StringBuffer());
        assertMethodChooser("String", new Character('a'));
    }

    public void testMethodChooserNumber() throws Throwable {
        assertMethodChooser("Number", new Integer(2));
        assertMethodChooser("Number", new Double(2));
    }

    public void testMethodChooserTwoParams() throws Throwable {
        List list = new ArrayList();
        list.add("foo");
        list.add("bar");
        assertMethodChooser("Object,Object", list.toArray());

        Object[] blah = {"a", "b"};
        assertMethodChooser("Object,Object", blah);
    }

    public void testInstanceofWorksForArray() {
        Class type = Object[].class;
        Object value = new Object[1];
        assertTrue("instanceof works for array", type.isInstance(value));
    }

    public void testMethodChooserTwoParamsWithSecondAnObjectArray() throws Throwable {
        Object[] blah = {"a", new Object[]{"b"}
        };
        assertMethodChooser("Object,Object[]", blah);
    }

    public void testCollectionMethods() throws Throwable {
        Object list = InvokerHelper.createList(new Object[]{"a", "b"});

        Object value = invoke(list, "size", null);
        assertEquals("size of collection", new Integer(2), value);

        value = invoke(list, "contains", "a");
        assertEquals("contains method", Boolean.TRUE, value);
    }

    public void testNewMethods() throws Throwable {
        Object value = invoke("hello", "size", null);
        assertEquals("size of string", new Integer(5), value);
    }

    public void testStaticMethod() throws Throwable {
        Object value = invoke(DummyBean.class, "dummyStaticMethod", "abc");
        assertEquals("size of string", "ABC", value);
    }

    public void testBaseClassMethod() throws Throwable {
        Object object = new DummyBean();
        Object value = invoke(object, "toString", null);
        assertEquals("toString", object.toString(), value);
    }

    //SPG modified to reflect DefaultGroovyMethod name change and expected result from
    //Integer/Integer division.
    public void testDivideNumbers() throws Throwable {
        assertMethodCall(new Double(10), "div", new Double(2), new Double(5));
        assertMethodCall(new Double(10), "div", new Integer(2), new Double(5));
        assertMethodCall(new Integer(10), "div", new Double(2), new Double(5));
        assertMethodCall(new Integer(10), "div", new Integer(2), new java.math.BigDecimal("5"));
    }

    public void testBaseFailMethod() throws Throwable {
        try {
            invoke(this, "fail", "hello");
        } catch (AssertionFailedError e) {
            // worked
        }
    }

    public void testToArrayOnList() throws Throwable {
        List object = new ArrayList();
        object.add("Hello");

        Object[] value = (Object[]) invoke(object, "toArray", null);
        assertArrayEquals(object.toArray(), value);
        assertEquals(1, value.length);
        assertEquals("Hello", value[0]);

        value = (Object[]) invoke(object, "toArray", new Object[0]);
        assertArrayEquals(object.toArray(), value);
    }

    public void testInvalidOverloading() throws Throwable {
        try {
            invoke(this, "badOverload", new Object[]{"a", "b"});
            fail("Should fail as an unambiguous method is invoked");
        }
        catch (GroovyRuntimeException e) {
            System.out.println("Caught: " + e);
        }
    }

    public void testPlusWithNull() throws Throwable {
        String param = "called with: ";
        Object value = invoke(param, "plus", new Object[]{null});
        assertEquals("called with null", param + null, value);
    }

    public void testCallIntMethodWithInteger() throws Throwable {
        Object value = invoke(this, "overloadedRemove", new Object[]{new Integer(5)});
        assertEquals("called with integer", "int5", value);
    }

    public void testCallListRemove() throws Throwable {
        List list = new ArrayList();
        list.add("foo");
        list.add("bar");

        invoke(list, "remove", new Object[]{new Integer(0)});

        assertEquals("Should have just 1 item left: " + list, 1, list.size());
    }

    public void testCoerceGStringToString() throws Throwable {
        GString param = new GString(new Object[]{"James"}) {
            public String[] getStrings() {
                return new String[]{"Hello "};
            }
        };
        Object value = invoke(this, "methodTakesString", new Object[]{param});
        assertEquals("converted GString to string", param.toString(), value);
    }

    public void testCoerceGStringToStringOnGetBytes() throws Throwable {
        GString param = new GString(new Object[]{CompilerConfiguration.DEFAULT_SOURCE_ENCODING}) {
            public String[] getStrings() {
                return new String[]{""};
            }
        };
        Object value = invoke("test", "getBytes", new Object[]{param});
        assertEquals("converted GString to string", "test".getBytes(CompilerConfiguration.DEFAULT_SOURCE_ENCODING).getClass(), value.getClass());
    }

    public void testBadBDToDoubleCoerce() throws Throwable {
        try {
            invoke(Math.class, "floor", new BigDecimal("1.7E309"));
        } catch (IllegalArgumentException e) {
            assertTrue("Math.floor(1.7E309) should fail because it is out of range for a Double. "
                    + e, e.getMessage().indexOf("out of range") > 0);
            return;
        }
        fail("Math.floor(1.7E309) should fail because it is out of range for a Double.");
    }

    public void testClassMethod() throws Throwable {
        Class c = String.class;
        Object value = invoke(c, "getName", null);
        assertEquals("Class.getName()", c.getName(), value);
        c = getClass();
        value = invoke(c, "getName", null);
        assertEquals("Class.getName()", c.getName(), value);
    }

    public void testProtectedMethod() throws Throwable {
        String param = "hello";
        Object value = invoke(this, "aProtectedMethod", param);
        assertEquals("protected method call", aProtectedMethod(param), value);
    }

    public void testPrivateMethod() throws Throwable {
        String param = "hello";
        Object value = invoke(this, "aPrivateMethod", param);
        assertEquals("private method call", aPrivateMethod(param), value);
    }

    public void testStringSubstringMethod() throws Throwable {
        String object = "hello";
        Object value = invoke(object, "substring", new Integer(2));
        assertEquals("substring(2)", object.substring(2), value);

        value = invoke(object, "substring", new Object[]{new Integer(1), new Integer(3)});
        assertEquals("substring(1,3)", object.substring(1, 3), value);
    }

    public void testListGetWithRange() throws Throwable {
        List list = Arrays.asList(new Object[]{"a", "b", "c"});
        Object range = new IntRange(true, 0, 2);
        Object value = invoke(list, "getAt", range);
        assertTrue("Returned List: " + value, value instanceof List);
        List retList = (List) value;
        assertEquals("List size", 3, retList.size());
    }

    public void testSetLenientOnDateFormat() throws Throwable {
        SimpleDateFormat a = new SimpleDateFormat("MM/dd/yyyy");

        Object value = invoke(a, "setLenient", new Object[]{Boolean.FALSE});
        assertEquals("void method", null, value);
    }

    public void testInvokeUnknownMethod() throws Throwable {
        try {
            Object value = invoke(this, "unknownMethod", "abc");
            fail("Should have thrown an exception");
        }
        catch (GroovyRuntimeException e) {
            // worked
        }
    }

    public void testInvokeMethodWithWrongNumberOfParameters() throws Throwable {
        try {
            Object[] args = {"a", "b"};
            invoke(this, "unknownMethod", args);
            fail("Should have thrown an exception");
        }
        catch (GroovyRuntimeException e) {
            // worked
        }
    }

    public void testInvokeMethodOnNullObject() throws Throwable {
        try {
            invoke(null, "mockCallWithNoParams", null);
            fail("Should have thrown an exception");
        }
        catch (NullPointerException e) {
            // worked
        }
    }

    // Mock methods used for testing
    //-------------------------------------------------------------------------

    public Object mockCallWithNoParams() {
        return "NoParams";
    }

    public Object mockCallWithOneParam(Object value) {
        assertEquals("Method not passed in the correct value", "abc", value);
        return "OneParam";
    }

    public Object mockCallWithOneNullParam(Object value) {
        assertEquals("Method not passed in the correct value", null, value);
        return "OneParamWithNull";
    }

    public Integer mockCallWithOneCollectionParam(Object collection) {
        Collection coll = DefaultTypeTransformation.asCollection(collection);
        return new Integer(coll.size());
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

    // Implementation methods
    //-------------------------------------------------------------------------

    protected Object aProtectedMethod(String param) {
        return param + " there!";
    }

    private Object aPrivateMethod(String param) {
        return param + " James!";
    }

    protected void assertMethodCall(Object object, String method, Object param, Object expected) {
        Object value = InvokerHelper.invokeMethod(object, method, new Object[]{param});
        assertEquals("result of method: " + method, expected, value);
    }

    /**
     * Asserts that invoking the method chooser finds the right overloaded
     * method implementation
     *
     * @param expected  is the expected value of the method
     * @param arguments the argument(s) to the method invocation
     */
    protected void assertMethodChooser(Object expected, Object arguments) throws Throwable {
        Object value = invoke(this, "mockOverloadedMethod", arguments);

        assertEquals("Invoking overloaded method for arguments: " + InvokerHelper.toString(arguments), expected, value);
    }

    protected Object invoke(Object object, String method, Object args) throws Throwable {
        try {
            return InvokerHelper.invokeMethod(object, method, args);
        }
        catch (InvokerInvocationException e) {
            throw e.getCause();
        }
    }
}
