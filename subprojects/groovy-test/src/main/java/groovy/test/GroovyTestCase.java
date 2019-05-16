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
package groovy.util;

import groovy.lang.Closure;
import groovy.test.GroovyAssert;
import junit.framework.TestCase;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * A JUnit 3 {@link junit.framework.TestCase} base class in Groovy.
 *
 * In case JUnit 4 is used, see {@link groovy.test.GroovyAssert}.
 *
 * @see groovy.test.GroovyAssert
 */
public class GroovyTestCase extends TestCase {

    protected static Logger log = Logger.getLogger(GroovyTestCase.class.getName());

    private static final AtomicInteger scriptFileNameCounter = new AtomicInteger(0);

    public static final String TEST_SCRIPT_NAME_PREFIX = "TestScript";

    private boolean useAgileDoxNaming = false;

    /**
     * Overload the getName() method to make the test cases look more like AgileDox
     * (thanks to Joe Walnes for this tip!)
     */
    public String getName() {
        if (useAgileDoxNaming) {
            return super.getName().substring(4).replaceAll("([A-Z])", " $1").toLowerCase();
        } else {
            return super.getName();
        }
    }

    public String getMethodName() {
        return super.getName();
    }

    /**
     * Asserts that the arrays are equivalent and contain the same values
     *
     * @param expected
     * @param value
     */
    protected void assertArrayEquals(Object[] expected, Object[] value) {
        String message =
                "expected array: " + InvokerHelper.toString(expected) + " value array: " + InvokerHelper.toString(value);
        assertNotNull(message + ": expected should not be null", expected);
        assertNotNull(message + ": value should not be null", value);
        assertEquals(message, expected.length, value.length);
        for (int i = 0, size = expected.length; i < size; i++) {
            assertEquals("value[" + i + "] when " + message, expected[i], value[i]);
        }
    }

    /**
     * Asserts that the array of characters has a given length
     *
     * @param length expected length
     * @param array  the array
     */
    protected void assertLength(int length, char[] array) {
        assertEquals(length, array.length);
    }

    /**
     * Asserts that the array of ints has a given length
     *
     * @param length expected length
     * @param array  the array
     */
    protected void assertLength(int length, int[] array) {
        assertEquals(length, array.length);
    }

    /**
     * Asserts that the array of objects has a given length
     *
     * @param length expected length
     * @param array  the array
     */
    protected void assertLength(int length, Object[] array) {
        assertEquals(length, array.length);
    }

    /**
     * Asserts that the array of characters contains a given char
     *
     * @param expected expected character to be found
     * @param array    the array
     */
    protected void assertContains(char expected, char[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == expected) {
                return;
            }
        }

        StringBuilder message = new StringBuilder();

        message.append(expected).append(" not in {");

        for (int i = 0; i < array.length; ++i) {
            message.append("'").append(array[i]).append("'");

            if (i < (array.length - 1)) {
                message.append(", ");
            }
        }

        message.append(" }");

        fail(message.toString());
    }

    /**
     * Asserts that the array of ints contains a given int
     *
     * @param expected expected int
     * @param array    the array
     */
    protected void assertContains(int expected, int[] array) {
        for (int anInt : array) {
            if (anInt == expected) {
                return;
            }
        }

        StringBuilder message = new StringBuilder();

        message.append(expected).append(" not in {");

        for (int i = 0; i < array.length; ++i) {
            message.append("'").append(array[i]).append("'");

            if (i < (array.length - 1)) {
                message.append(", ");
            }
        }

        message.append(" }");

        fail(message.toString());
    }

    /**
     * Asserts that the value of toString() on the given object matches the
     * given text string
     *
     * @param value    the object to be output to the console
     * @param expected the expected String representation
     */
    protected void assertToString(Object value, String expected) {
        Object console = InvokerHelper.invokeMethod(value, "toString", null);
        assertEquals("toString() on value: " + value, expected, console);
    }

    /**
     * Asserts that the value of inspect() on the given object matches the
     * given text string
     *
     * @param value    the object to be output to the console
     * @param expected the expected String representation
     */
    protected void assertInspect(Object value, String expected) {
        Object console = InvokerHelper.invokeMethod(value, "inspect", null);
        assertEquals("inspect() on value: " + value, expected, console);
    }

    /**
     * see {@link groovy.test.GroovyAssert#assertScript(String)}
     */
    protected void assertScript(final String script) throws Exception {
        GroovyAssert.assertScript(script);
    }

    protected String getTestClassName() {
        return TEST_SCRIPT_NAME_PREFIX + getMethodName() + (scriptFileNameCounter.getAndIncrement()) + ".groovy";
    }

    /**
     * see {@link groovy.test.GroovyAssert#shouldFail(groovy.lang.Closure)}
     */
    protected String shouldFail(Closure code) {
        return GroovyAssert.shouldFail(code).getMessage();
    }

    /**
     * see {@link groovy.test.GroovyAssert#shouldFail(Class, groovy.lang.Closure)}
     */
    protected String shouldFail(Class clazz, Closure code) {
        return GroovyAssert.shouldFail(clazz, code).getMessage();
    }

    /**
     * see {@link groovy.test.GroovyAssert#shouldFailWithCause(Class, groovy.lang.Closure)}
     */
    protected String shouldFailWithCause(Class clazz, Closure code) {
        return GroovyAssert.shouldFailWithCause(clazz, code).getMessage();
    }

    /**
     * see {@link groovy.test.GroovyAssert#shouldFail(Class, String)}
     */
    protected String shouldFail(Class clazz, String script) {
        return GroovyAssert.shouldFail(clazz, script).getMessage();
    }

    /**
     * see {@link groovy.test.GroovyAssert#shouldFail(String)}
     */
    protected String shouldFail(String script) {
        return GroovyAssert.shouldFail(script).getMessage();
    }

    /**
     * Returns a copy of a string in which all EOLs are \n.
     */
    protected String fixEOLs(String value) {
        return value.replaceAll("(\\r\\n?)|\n", "\n");
    }

    /**
     * see {@link groovy.test.GroovyAssert#notYetImplemented(java.lang.Object)}
     */
    public static boolean notYetImplemented(Object caller) {
        return GroovyAssert.notYetImplemented(caller);
    }

    /**
     * Convenience method for subclasses of GroovyTestCase, identical to
     * <pre> GroovyTestCase.notYetImplemented(this); </pre>.
     *
     * @return <code>false</code> when not itself already in the call stack
     * @see #notYetImplemented(java.lang.Object)
     */
    public boolean notYetImplemented() {
        return notYetImplemented(this);
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        if (expected == null && actual == null)
            return;
        if (expected != null && DefaultTypeTransformation.compareEqual(expected, actual))
            return;
        TestCase.assertEquals(message, expected, actual);
    }

    public static void assertEquals(Object expected, Object actual) {
        assertEquals(null, expected, actual);
    }

    public static void assertEquals(String expected, String actual) {
        assertEquals(null, expected, actual);
    }
}
