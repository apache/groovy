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
package groovy.util;

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
     * @param code the code expected to throw the exception
     * @return the message of the thrown Throwable
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
     * and that a particular exception is thrown.
     *
     * @param clazz the class of the expected exception
     * @param code  the closure that should fail
     * @return the message of the expected Throwable
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
     * and that a particular exception can be attributed to the cause.
     * The expected exception class is compared recursively with any nested
     * exceptions using getCause() until either a match is found or no more
     * nested exceptions exist.
     * <p>
     * If a match is found the error message associated with the matching
     * exception is returned. If no match was found the method will fail.
     *
     * @param clazz the class of the expected exception
     * @param code  the closure that should fail
     * @return the message of the expected Throwable
     */
    public static Throwable shouldFailWithCause(Class clazz, Closure code) {
        Throwable th = null;
        Throwable orig = null;
        int level = 0;
        try {
            code.call();
        } catch (GroovyRuntimeException gre) {
            orig = ScriptBytecodeAdapter.unwrap(gre);
            th = orig.getCause();
        } catch (Throwable e) {
            orig = e;
            th = orig.getCause();
        }

        while (th != null && !clazz.isInstance(th) && th != th.getCause() && level < MAX_NESTED_EXCEPTIONS) {
            th = th.getCause();
            level++;
        }

        if (orig == null) {
            fail("Closure " + code + " should have failed with an exception caused by type " + clazz.getName());
        } else if (th == null || !clazz.isInstance(th)) {
            fail("Closure " + code + " should have failed with an exception caused by type " + clazz.getName() + ", instead found these Exceptions:\n" + buildExceptionList(orig));
        }
        return th;
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
