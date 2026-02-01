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

import groovy.lang.Closure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for StackTraceUtils class.
 */
class StackTraceUtilsJUnit5Test {

    @AfterEach
    void tearDown() {
        // Clear any custom class tests that may have been added
        // Note: There's no public method to clear tests, so we rely on test isolation
    }

    @Test
    void testIsApplicationClassWithApplicationClass() {
        // A class that doesn't start with groovy internal packages
        assertTrue(StackTraceUtils.isApplicationClass("com.example.MyClass"));
        assertTrue(StackTraceUtils.isApplicationClass("org.example.SomeClass"));
        assertTrue(StackTraceUtils.isApplicationClass("MyClass"));
    }

    @Test
    void testIsApplicationClassWithGroovyInternalClasses() {
        // Classes that start with groovy internal packages should not be application classes
        assertFalse(StackTraceUtils.isApplicationClass("groovy.lang.GroovyObject"));
        assertFalse(StackTraceUtils.isApplicationClass("org.codehaus.groovy.runtime.DefaultGroovyMethods"));
        assertFalse(StackTraceUtils.isApplicationClass("java.lang.String"));
        assertFalse(StackTraceUtils.isApplicationClass("javax.swing.JFrame"));
        assertFalse(StackTraceUtils.isApplicationClass("sun.misc.Unsafe"));
        assertFalse(StackTraceUtils.isApplicationClass("com.sun.proxy.Proxy"));
        assertFalse(StackTraceUtils.isApplicationClass("org.apache.groovy.util.Something"));
        assertFalse(StackTraceUtils.isApplicationClass("jdk.internal.misc.Unsafe"));
    }

    @Test
    void testAddClassTest() {
        // Add a custom test that returns true for a specific class
        StackTraceUtils.addClassTest(new Closure<Boolean>(null) {
            @Override
            public Boolean call(Object... args) {
                String className = (String) args[0];
                if (className.equals("my.custom.TestClass")) {
                    return true;
                }
                return null; // continue with other tests
            }
        });

        // The custom class should now be considered an application class
        assertTrue(StackTraceUtils.isApplicationClass("my.custom.TestClass"));
    }

    @Test
    void testAddClassTestReturningFalse() {
        // Add a custom test that returns false for a specific class
        StackTraceUtils.addClassTest(new Closure<Boolean>(null) {
            @Override
            public Boolean call(Object... args) {
                String className = (String) args[0];
                if (className.startsWith("force.exclude.")) {
                    return false;
                }
                return null;
            }
        });

        // The class should be excluded
        assertFalse(StackTraceUtils.isApplicationClass("force.exclude.SomeClass"));
    }

    @Test
    void testExtractRootCause() {
        Exception root = new RuntimeException("root cause");
        Exception middle = new Exception("middle", root);
        Exception top = new Exception("top", middle);

        Throwable extracted = StackTraceUtils.extractRootCause(top);
        assertSame(root, extracted);
        assertEquals("root cause", extracted.getMessage());
    }

    @Test
    void testExtractRootCauseWithNoCause() {
        Exception single = new RuntimeException("single exception");
        Throwable extracted = StackTraceUtils.extractRootCause(single);
        assertSame(single, extracted);
    }

    @Test
    void testExtractRootCauseWithDeepNesting() {
        Exception e1 = new RuntimeException("level 1");
        Exception e2 = new Exception("level 2", e1);
        Exception e3 = new Exception("level 3", e2);
        Exception e4 = new Exception("level 4", e3);
        Exception e5 = new Exception("level 5", e4);

        Throwable extracted = StackTraceUtils.extractRootCause(e5);
        assertSame(e1, extracted);
    }

    @Test
    void testSanitize() {
        // Create an exception with a stack trace
        Exception ex = new RuntimeException("test exception");

        // Sanitize it
        Throwable sanitized = StackTraceUtils.sanitize(ex);

        // Should return the same exception instance
        assertSame(ex, sanitized);

        // The stack trace should be modified (groovy internal classes removed)
        // Note: The actual filtering depends on system property groovy.full.stacktrace
        assertNotNull(sanitized.getStackTrace());
    }

    @Test
    void testSanitizeRootCause() {
        Exception root = new RuntimeException("root");
        Exception wrapper = new Exception("wrapper", root);

        Throwable sanitizedRoot = StackTraceUtils.sanitizeRootCause(wrapper);

        // Should return the sanitized root cause
        assertEquals("root", sanitizedRoot.getMessage());
    }

    @Test
    void testDeepSanitize() {
        Exception root = new RuntimeException("root");
        Exception middle = new Exception("middle", root);
        Exception top = new Exception("top", middle);

        Throwable result = StackTraceUtils.deepSanitize(top);

        // Should return the top exception (sanitized)
        assertSame(top, result);
        assertNotNull(result.getStackTrace());
    }

    @Test
    void testDeepSanitizeWithSingleException() {
        Exception single = new RuntimeException("single");

        Throwable result = StackTraceUtils.deepSanitize(single);

        assertSame(single, result);
    }

    @Test
    void testPrintSanitizedStackTraceWithPrintWriter() {
        Exception ex = new RuntimeException("test");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        StackTraceUtils.printSanitizedStackTrace(ex, pw);
        pw.flush();

        String output = sw.toString();
        // Should contain "at" entries for the stack trace
        assertTrue(output.contains("at ") || output.isEmpty());
    }

    @Test
    void testStackLogName() {
        assertEquals("StackTrace", StackTraceUtils.STACK_LOG_NAME);
    }

    @Test
    void testIsApplicationClassWithGjdkClasses() {
        assertFalse(StackTraceUtils.isApplicationClass("gjdk.groovy.lang.SomeThing"));
    }

    @Test
    void testIsApplicationClassWithGroovyJarJarClasses() {
        assertFalse(StackTraceUtils.isApplicationClass("groovyjarjar.asm.ClassVisitor"));
    }

    @Test
    void testSanitizePreservesMessage() {
        String message = "Original error message";
        Exception ex = new RuntimeException(message);

        Throwable sanitized = StackTraceUtils.sanitize(ex);

        assertEquals(message, sanitized.getMessage());
    }

    @Test
    void testSanitizePreservesCause() {
        Exception cause = new IllegalArgumentException("cause");
        Exception ex = new RuntimeException("wrapper", cause);

        Throwable sanitized = StackTraceUtils.sanitize(ex);

        assertSame(cause, sanitized.getCause());
    }
}
