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
package groovy.test;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.isAtLeast;

/**
 * <p>{@code GroovyAssert} contains a set of static assertion and test helper methods for JUnit 4+.
 * They augment the kind of helper methods found in JUnit 4's {@link org.junit.Assert} class.
 * JUnit 3 users typically don't use these methods but instead,
 * the equivalent methods in {@link groovy.test.GroovyTestCase}.
 * </p>
 *
 * <p>
 * {@code GroovyAssert} methods can either be used by fully qualifying the static method like:
 *
 * <pre>
 *     groovy.test.GroovyAssert.shouldFail { ... }
 * </pre>
 *
 * or by importing the static methods with one ore more static imports:
 *
 * <pre>
 *     import static groovy.test.GroovyAssert.shouldFail
 * </pre>
 * </p>
 * <em>Backwards compatibility note:</em>
 * Prior to Groovy 4, {@code GroovyAssert} extended JUnit 4's {@link org.junit.Assert} class.
 * This meant that you could statically import static methods from that class via {@code GroovyAssert}, e.g.:
 * <pre>
 *     import static groovy.test.GroovyAssert.assertNotNull
 * </pre>
 * This is generally regarded as a code smell since inheritance is primarily to do with instance methods.
 * From Groovy 4, you should import such methods directly, e.g.:
 * <pre>
 *     import static org.junit.Assert.assertNotNull
 * </pre>
 *
 * @see groovy.test.GroovyTestCase
 * @since 2.3
 */
public class GroovyAssert {

    private static final int MAX_NESTED_EXCEPTIONS = 10;

    private static final AtomicInteger counter = new AtomicInteger(0);

    public static final String TEST_SCRIPT_NAME_PREFIX = "TestScript";

    /**
     * @return a generic script name to be used by {@code GroovyShell#evaluate}.
     */
    protected static String genericScriptName() {
        return TEST_SCRIPT_NAME_PREFIX + (counter.getAndIncrement()) + ".groovy";
    }

    //--------------------------------------------------------------------------

    /**
     * Asserts that the script runs without any exceptions
     *
     * @param script the script that should pass without any exception
     */
    public static void assertScript(final String script) {
        assertScript(new GroovyShell(), script);
    }

    /**
     * Asserts that the script runs using the given shell without any exceptions
     *
     * @param shell the shell to use to evaluate the script
     * @param script the script that should pass without any exception
     */
    public static void assertScript(final GroovyShell shell, final String script) {
        shell.evaluate(script, genericScriptName()); // TODO: unwrap GroovyRuntimeException
    }

    //--------------------------------------------------------------------------

    public static void fail(final String message) {
        if (message == null) {
            throw new AssertionError();
        }
        throw new AssertionError(message);
    }

    //--------------------------------------------------------------------------

    /**
     * Asserts that the given script fails when evaluated.
     *
     * @param script the script expected to fail
     * @throws AssertionError if no failure
     * @return the caught exception
     */
    public static Throwable shouldFail(final String script) {
        return shouldFail(new GroovyShell(), script);
    }

    /**
     * Asserts that the given script fails when evaluated using the given shell.
     *
     * @param shell the shell to use to evaluate the script
     * @param script the script expected to fail
     * @throws AssertionError if no failure
     * @return the caught exception
     */
    public static Throwable shouldFail(final GroovyShell shell, final String script) {
        Throwable th = null;
        try {
            shell.evaluate(script, genericScriptName());
        } catch (GroovyRuntimeException gre) {
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable t) {
            th = t;
        }

        if (th == null) {
            fail("Script should have failed");
        }

        return th;
    }

    /**
     * Asserts that the given script fails when evaluated and that a particular
     * type of exception is thrown.
     *
     * @param clazz  the class of the expected exception
     * @param script the script expected to fail
     * @throws AssertionError if no failure
     * @return the caught exception
     */
    public static <T extends Throwable> T shouldFail(final Class<T> clazz, final String script) {
        return shouldFail(new GroovyShell(), clazz, script);
    }

    /**
     * Asserts that the given script fails when evaluated using the given shell
     * and that a particular type of exception is thrown.
     *
     * @param shell  the shell to use to evaluate the script
     * @param clazz  the class of the expected exception
     * @param script the script expected to fail
     * @throws AssertionError if no failure
     * @return the caught exception
     */
    public static <T extends Throwable> T shouldFail(final GroovyShell shell, final Class<T> clazz, final String script) {
        Throwable th = null;
        try {
            shell.evaluate(script, genericScriptName());
        } catch (GroovyRuntimeException gre) {
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable t) {
            th = t;
        }

        if (th == null) {
            fail("Script should have failed with an exception of type " + clazz.getName());
        } else if (!clazz.isInstance(th)) {
            fail("Script should have failed with an exception of type " + clazz.getName() + ", instead got Exception " + th);
        }

        @SuppressWarnings("unchecked")
        T t = (T) th;
        return t;
    }

    //

    /**
     * Asserts that the given closure fails when executed.
     *
     * @param code the block expected to fail
     * @throws AssertionError if no failure
     * @return the caught exception
     */
    public static Throwable shouldFail(final Closure<?> code) {
        Throwable th = null;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable t) {
            th = t;
        }

        if (th == null) {
            fail("Closure " + code + " should have failed");
        }

        return th;
    }

    /**
     * Asserts that the given closure fails when executed and that a particular
     * type of exception is thrown.
     *
     * @param type the class of the expected exception
     * @param code the block expected to fail
     * @throws AssertionError if no failure
     * @return the caught exception
     */
    public static <T extends Throwable> T shouldFail(final Class<T> type, final Closure<?> code) {
        Throwable th = null;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable t) {
            th = t;
        }

        if (th == null) {
            fail("Closure " + code + " should have failed with an exception of type " + type.getName());
        } else if (!type.isInstance(th)) {
            fail("Closure " + code + " should have failed with an exception of type " + type.getName() + ", but got: " + th);
        }

        @SuppressWarnings("unchecked")
        T t = (T) th;
        return t;
    }

    /**
     * Asserts that the given closure fails when executed and that a particular
     * exception type can be attributed to the cause. The expected exception is
     * compared recursively with any nested exceptions using getCause() until
     * either a match is found or no more nested exceptions exist.
     * <p>
     * If a match is found, the matching exception is returned otherwise the
     * method will fail.
     *
     * @param type the class of the expected nested exception
     * @param code the block expected to fail
     * @throws AssertionError if no failure
     * @return the cause
     */
    public static <T extends Throwable> T shouldFailWithCause(final Class<T> type, final Closure<?> code) {
        Throwable th = null;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable t) {
            th = t;
        }

        if (th == null) {
            fail("Closure " + code + " should have failed with an exception having a nested cause of type " + type.getName());
        } else if (th.getCause() == null) {
            fail("Closure " + code + " should have failed with an exception having a nested cause of type " + type.getName() +
                " but instead got a direct exception of type " + th.getClass().getName() + " with no cause. Code under test has a bug or perhaps you meant shouldFail?");
        }

        int level = 0;
        while (th != th.getCause() && level < MAX_NESTED_EXCEPTIONS) {
            th = th.getCause(); if (th == null || type.isInstance(th)) break;
            level += 1;
        }

        if (th == null || !type.isInstance(th)) {
            fail("Closure " + code + " should have failed with an exception having a nested cause of type " + type.getName() + ", instead found these exceptions:\n" + buildExceptionList(th));
        }

        @SuppressWarnings("unchecked")
        T t = (T) th;
        return t;
    }

    private static String buildExceptionList(Throwable th) {
        StringBuilder sb = new StringBuilder();
        int level = 0;
        while (th != null) {
            if (level > 1) {
                for (int i = 0; i < level - 1; i++) sb.append("   ");
            }
            if (level > 0) sb.append("-> ");
            if (level > MAX_NESTED_EXCEPTIONS) {
                sb.append("...");
                break;
            }
            sb.append(th.getClass().getName()).append(": ").append(th.getMessage()).append("\n");
            if (th == th.getCause()) {
                break;
            }
            th = th.getCause();
            level++;
        }
        return sb.toString();
    }

    //--------------------------------------------------------------------------

    /**
     * From JUnit. Finds from the call stack the active running JUnit test case.
     *
     * @return the test case method
     * @throws RuntimeException if no method could be found.
     */
    private static Method findRunningJUnitTestMethod(final Class<?> caller) {
        final Class<?>[] args = new Class<?>[]{};

        // search the initial junit test
        final StackTraceElement[] stackTrace = new Exception().getStackTrace();
        for (int i = stackTrace.length - 1; i >= 0; --i) {
            final StackTraceElement element = stackTrace[i];
            if (element.getClassName().equals(caller.getName())) {
                try {
                    final Method m = caller.getMethod(element.getMethodName(), args);
                    if (isPublicTestMethod(m)) {
                        return m;
                    }
                } catch (Exception ignore) {
                    // can't access, ignore it
                }
            }
        }
        throw new RuntimeException("No JUnit test case method found in call stack");
    }

    /**
     * From Junit. Test if the method is a JUnit 3 or 4 test.
     *
     * @param method the method
     * @return <code>true</code> if this is a junit test.
     */
    private static boolean isPublicTestMethod(final Method method) {
        final String name = method.getName();
        final Class<?>[] parameters = method.getParameterTypes();
        final Class<?> returnType = method.getReturnType();

        return parameters.length == 0
                && (name.startsWith("test") || hasTestAnnotation(method))
                && returnType.equals(Void.TYPE)
                && Modifier.isPublic(method.getModifiers());
    }

    private static boolean hasTestAnnotation(final Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            if ("org.junit.Test".equals(annotation.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Runs the calling JUnit test again and fails only if it unexpectedly runs.<br>
     * This is helpful for tests that don't currently work but should work one day,
     * when the tested functionality has been implemented.
     * <p>
     * The right way to use it for JUnit 3 is:
     * <pre>
     * public void testXXX() {
     *   if (GroovyAssert.notYetImplemented(this)) return;
     *   ... the real (now failing) unit test
     * }
     * </pre>
     * or for JUnit 4:
     * <pre>
     * &#64;Test
     * public void XXX() {
     *   if (GroovyAssert.notYetImplemented(this)) return;
     *   ... the real (now failing) unit test
     * }
     * </pre>
     *
     * Idea copied from HtmlUnit (many thanks to Marc Guillemot).
     * Future versions maybe available in the JUnit distribution.
     *
     * @return {@code false} when not itself already in the call stack
     * @throws AssertionError if no exception
     * @see NotYetImplemented
     */
    public static boolean notYetImplemented(final Object caller) {
        if (notYetImplementedFlag.get() != null) {
            return false;
        }
        notYetImplementedFlag.set(Boolean.TRUE);

        Logger log = Logger.getLogger(GroovyAssert.class.getName());
        Method testMethod = findRunningJUnitTestMethod(caller.getClass());
        try {
            log.info("Running " + testMethod.getName() + " as not yet implemented");
            testMethod.invoke(caller, (Object[]) new Class[]{});
            fail(testMethod.getName() + " is marked as not yet implemented but passes unexpectedly");
        } catch (Exception e) {
            log.info(testMethod.getName() + " fails which is expected as it is not yet implemented");
            // method execution failed, it is really "not yet implemented"
        } finally {
            notYetImplementedFlag.remove();
        }
        return true;
    }

    private static final ThreadLocal<Boolean> notYetImplementedFlag = new ThreadLocal<>();

    //--------------------------------------------------------------------------

    /**
     * @return true if the JDK version is at least the version given by specVersion (e.g. "1.8", "9.0")
     * @since 2.5.7
     */
    public static boolean isAtLeastJdk(final String specVersion) {
        boolean result = false;
        try {
            result = isAtLeast(new BigDecimal(System.getProperty("java.specification.version")), specVersion);
        } catch (Exception ignore) {
        }
        return result;
    }
}
