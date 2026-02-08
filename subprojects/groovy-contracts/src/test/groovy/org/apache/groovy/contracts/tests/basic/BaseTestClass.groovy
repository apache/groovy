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
package org.apache.groovy.contracts.tests.basic

import groovy.text.GStringTemplateEngine
import groovy.text.TemplateEngine
import org.junit.Before
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter

import static org.junit.Assert.assertTrue
import static org.junit.Assert.fail

class BaseTestClass {

    private static final int MAX_NESTED_EXCEPTIONS = 10;

    private TemplateEngine templateEngine
    private GroovyClassLoader loader;

    @Before
    void setUp() {
        templateEngine = new GStringTemplateEngine()
        loader = new GroovyClassLoader(getClass().getClassLoader())
    }

    String createSourceCodeForTemplate(final String template, final Map binding) {
        templateEngine.createTemplate(template).make(binding).toString()
    }

    def create_instance_of(final String sourceCode) {
        return create_instance_of(sourceCode, new Object[0])
    }

    def create_instance_of(final String sourceCode, def constructor_args) {

        def clazz = add_class_to_classpath(sourceCode)

        return clazz.newInstance(constructor_args as Object[])
    }

    def add_class_to_classpath(final String sourceCode) {
        loader.parseClass(sourceCode)
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
        catch (GroovyRuntimeException gre) {
            failed = true;
            result = ScriptBytecodeAdapter.unwrap(gre).getMessage();
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
            th = ScriptBytecodeAdapter.unwrap(gre);
        } catch (Throwable e) {
            th = e;
        }

        if (th == null) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName());
        } else if (!clazz.isInstance(th)) {
            fail("Closure " + code + " should have failed with an exception of type " + clazz.getName() + ", instead got Exception " + th);
        }
        return th.getMessage();
    }

    /**
     * Asserts that the given code closure fails when it is evaluated
     * and that a particular exception can be attributed to the cause.
     * The expected exception class is compared recursively with any nested
     * exceptions using getCause() until either a match is found or no more
     * nested exceptions exist.
     * <p/>
     * If a match is found the error message associated with the matching
     * exception is returned. If no match was found the method will fail.
     *
     * @param clazz the class of the expected exception
     * @param code the closure that should fail
     * @return the message of the expected Throwable
     */
    protected String shouldFailWithCause(Class clazz, Closure code) {
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
        return th.getMessage();
    }

    private String buildExceptionList(Throwable th) {
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
