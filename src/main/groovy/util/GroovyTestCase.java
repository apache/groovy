/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import junit.framework.TestCase;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

/**
 * A default JUnit TestCase in Groovy. This provides a number of helper methods
 * plus avoids the JUnit restriction of requiring all test* methods to be void
 * return type.
 *
 * @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Dierk Koenig (the notYetImplemented feature, changes to shouldFail)
 * @version $Revision$
 */
public class GroovyTestCase extends TestCase {

    protected static Logger log = Logger.getLogger(GroovyTestCase.class.getName());
    private static int counter;
    public static final String TEST_SCRIPT_NAME_PREFIX = "TestScript";

    private boolean useAgileDoxNaming = false;

    public GroovyTestCase() {
    }

    /**
     * Overload the getName() method to make the test cases look more like AgileDox
     * (thanks to Joe Walnes for this tip!)
     */
    public String getName() {
        if (useAgileDoxNaming) {
            return super.getName().substring(4).replaceAll("([A-Z])", " $1").toLowerCase();
        }
        else {
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
     * @param array the array
     */
    protected void assertLength(int length, char[] array) {
        assertEquals(length, array.length);
    }

    /**
     * Asserts that the array of ints has a given length
     *
     * @param length expected length
     * @param array the array
     */
    protected void assertLength(int length, int[] array) {
        assertEquals(length, array.length);
    }

    /**
     * Asserts that the array of objects has a given length
     *
     * @param length expected length
     * @param array the array
     */
    protected void assertLength(int length, Object[] array) {
        assertEquals(length, array.length);
    }

    /**
     * Asserts that the array of characters contains a given char
     *
     * @param expected expected character to be found
     * @param array the array
     */
    protected void assertContains(char expected, char[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == expected) {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

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
     * @param array the array
     */
    protected void assertContains(int expected, int[] array) {
        for (int i = 0; i < array.length; ++i) {
            if (array[i] == expected) {
                return;
            }
        }

        StringBuffer message = new StringBuffer();

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
     * @param value the object to be output to the console
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
     * @param value the object to be output to the console
     * @param expected the expected String representation
     */
    protected void assertInspect(Object value, String expected) {
        Object console = InvokerHelper.invokeMethod(value, "inspect", null);
        assertEquals("inspect() on value: " + value, expected, console);
    }

    /**
     * Asserts that the script runs without any exceptions
     *
     * @param script the script that should pass without any exception thrown
     */
    protected void assertScript(final String script) throws Exception {
        GroovyShell shell = new GroovyShell();
        shell.evaluate(script, getTestClassName());
    }

    protected String getTestClassName() {
        return TEST_SCRIPT_NAME_PREFIX + getMethodName() + (counter++) + ".groovy";
    }

    /**
     * Asserts that the given code closure fails when it is evaluated
     *
     * @param code
     * @return the message of the thrown Throwable
     */
    protected String shouldFail(Closure code) {
        boolean failed = false;
        String result = null;
        try {
            code.call();
        }
        catch (Throwable e) {
                failed = true;
                result = e.getMessage();
        }
        assertTrue("Closure " + code + " should have failed", failed);
        return result;
    }

    /**
     * Asserts that the given code closure fails when it is evaluated
     * and that a particular exception is thrown.
     *
     * @param clazz the class of the expected exception
     * @param code the closure that should fail
     * @return the message of the expected Throwable
     */
    protected String shouldFail(Class clazz, Closure code) {
        Throwable th = null;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            th = unwrap(gre);
        } catch (Throwable e) {
            th = e;
        }

        if (th==null) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName());
        } else if (! clazz.isInstance(th)) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName() + ", instead got Exception " + th);
        }
        return th.getMessage();
    }

    protected String shouldFailWithCause(Class clazz, Closure code) {
        Throwable th = null;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            th = gre;
            while (th.getCause()!=null && th.getCause()!=gre){ // if wrapped, find the root cause
                th=th.getCause();
                if (th!=gre && (th instanceof GroovyRuntimeException)) {
                    gre = (GroovyRuntimeException) th;
                }
            }
        } catch (Throwable e) {
            th = e;
        }

        if (th==null) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName());
        } else if (! clazz.isInstance(th)) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName() + ", instead got Exception " + th);
        }
        return th.getMessage();
    }

    /**
     *  Returns a copy of a string in which all EOLs are \n.
     */
    protected String fixEOLs( String value )
    {
        return value.replaceAll( "(\\r\\n?)|\n", "\n" );
    }

    /**
     * Runs the calling JUnit test again and fails only if it unexpectedly runs.<br/>
     * This is helpful for tests that don't currently work but should work one day,
     * when the tested functionality has been implemented.<br/>
     * The right way to use it is:
     * <pre>
     * public void testXXX() {
     *   if (GroovyTestCase.notYetImplemented(this)) return;
     *   ... the real (now failing) unit test
     * }
     * </pre>
     * Idea copied from HtmlUnit (many thanks to Marc Guillemot).
     * Future versions maybe available in the JUnit distro.
     * The purpose of providing a 'static' version is such that you can use the
     * feature even if not subclassing GroovyTestCase.
     * @return <false> when not itself already in the call stack
     */
    public static boolean notYetImplemented(TestCase caller) {
        if (notYetImplementedFlag.get() != null) {
            return false;
        }
        notYetImplementedFlag.set(Boolean.TRUE);

        final Method testMethod = findRunningJUnitTestMethod(caller.getClass());
        try {
            log.info("Running " + testMethod.getName() + " as not yet implemented");
            testMethod.invoke(caller, (Object[]) new Class[] {});
            fail(testMethod.getName() + " is marked as not yet implemented but passes unexpectedly");
        }
        catch (final Exception e) {
            log.info(testMethod.getName() + " fails which is expected as it is not yet implemented");
            // method execution failed, it is really "not yet implemented"
        }
        finally {
            notYetImplementedFlag.set(null);
        }
        return true;
    }

    /**
     * Convenience method for subclasses of GroovyTestCase, identical to
     * <pre> GroovyTestCase.notYetImplemented(this); </pre>.
     * @see #notYetImplemented(junit.framework.TestCase)
     * @return  <false> when not itself already in the call stack
     */
    public boolean notYetImplemented() {
        return notYetImplemented(this);
    }

    /**
     * From JUnit. Finds from the call stack the active running JUnit test case
     * @return the test case method
     * @throws RuntimeException if no method could be found.
     */
    private static Method findRunningJUnitTestMethod(Class caller) {
        final Class[] args = new Class[] {};

        // search the inial junit test
        final Throwable t = new Exception();
        for (int i=t.getStackTrace().length-1; i>=0; --i) {
            final StackTraceElement element = t.getStackTrace()[i];
            if (element.getClassName().equals(caller.getName())) {
                try {
                    final Method m = caller.getMethod(element.getMethodName(), args);
                    if (isPublicTestMethod(m)) {
                        return m;
                    }
                }
                catch (final Exception e) {
                    // can't access, ignore it
                }
            }
        }
        throw new RuntimeException("No JUnit test case method found in call stack");
    }


    /**
     * From Junit. Test if the method is a junit test.
     * @param method the method
     * @return <code>true</code> if this is a junit test.
     */
    private static boolean isPublicTestMethod(final Method method) {
        final String name = method.getName();
        final Class[] parameters = method.getParameterTypes();
        final Class returnType = method.getReturnType();

        return parameters.length == 0 && name.startsWith("test")
            && returnType.equals(Void.TYPE)
            && Modifier.isPublic(method.getModifiers());
    }

    public static void assertEquals(String message, Object expected, Object actual) {
        if (expected == null && actual == null)
			return;
		if (expected != null && DefaultTypeTransformation.compareEqual(expected, actual))
			return;
		failNotEquals(message, expected, actual);
    }

    public static void assertEquals(Object expected, Object actual) {
	    assertEquals(null, expected, actual);
	}

	public static void assertEquals(String expected, String actual) {
	    assertEquals(null, expected, actual);
	}

    private static final ThreadLocal notYetImplementedFlag = new ThreadLocal();

    private Throwable unwrap (GroovyRuntimeException gre) {
        Throwable th = gre;
        if (th.getCause() != null && th.getCause() != gre) th = th.getCause();
        if (th != gre && (th instanceof GroovyRuntimeException)) return unwrap((GroovyRuntimeException) th);
        return th;
    }
}
