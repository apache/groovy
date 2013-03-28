/*
 * Copyright 2003-2013 the original author or authors.
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
package groovy.test;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GroovyAssert {
    private static final int MAX_NESTED_EXCEPTIONS = 10;

    /**
     * Asserts that the given code closure fails when it is evaluated
     *
     * @param code the code expected to fail
     * @return the caught exception
     */
    public static Throwable shouldFail(Closure code) {
        boolean failed = false;
        Throwable th = null;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            failed = true;
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable e) {
            failed = true;
            th = e;
        }
        assertTrue("Closure " + code + " should have failed", failed);
        return th;
    }

    /**
     * Asserts that the given code closure fails when it is evaluated
     * and that a particular type of exception is thrown.
     *
     * @param clazz the class of the expected exception
     * @param code  the closure that should fail
     * @return the caught exception
     */
    public static Throwable shouldFail(Class clazz, Closure code) {
        Throwable th = null;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable e) {
            th = e;
        }

        if (th == null) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName());
        } else if (!clazz.isInstance(th)) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName() + ", instead got Exception " + th);
        }
        return th;
    }

    /**
     * Asserts that the given code closure fails when it is evaluated
     * and that a particular Exception type can be attributed to the cause.
     * The expected exception class is compared recursively with any nested
     * exceptions using getCause() until either a match is found or no more
     * nested exceptions exist.
     * <p>
     * If a match is found, the matching exception is returned
     * otherwise the method will fail.
     *
     * @param expectedCause the class of the expected exception
     * @param code          the closure that should fail
     * @return the cause
     */
    public static Throwable shouldFailWithCause(Class expectedCause, Closure code) {
        if (expectedCause == null) {
            fail("The expectedCause class cannot be null");
        }
        Throwable cause = null;
        Throwable orig = null;
        int level = 0;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            orig = ScriptBytecodeAdapter.unwrap(gre);
            cause = orig.getCause();
        } catch (Throwable e) {
            orig = e;
            cause = orig.getCause();
        }

        if (orig != null && cause == null) {
            fail("Closure " + code + " was expected to fail due to a nested cause of type " + expectedCause.getName() +
            " but instead got a direct exception of type " + orig.getClass().getName() + " with no nested cause(s). Code under test has a bug or perhaps you meant shouldFail?");
        }

        while (cause != null && !expectedCause.isInstance(cause) && cause != cause.getCause() && level < MAX_NESTED_EXCEPTIONS) {
            cause = cause.getCause();
            level++;
        }

        if (orig == null) {
            fail("Closure " + code + " should have failed with an exception having a nested cause of type " + expectedCause.getName());
        } else if (cause == null || !expectedCause.isInstance(cause)) {
            fail("Closure " + code + " should have failed with an exception having a nested cause of type " + expectedCause.getName() + ", instead found these Exceptions:\n" + buildExceptionList(orig));
        }
        return cause;
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

}
