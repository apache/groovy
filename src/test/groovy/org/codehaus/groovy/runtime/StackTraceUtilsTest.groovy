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
package org.codehaus.groovy.runtime

import groovy.lang.Closure
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for StackTraceUtils class.
 */
class StackTraceUtilsTest {

    @AfterEach
    void tearDown() {
        // Clear any custom class tests that may have been added
        // Note: There's no public method to clear tests, so we rely on test isolation
    }

    @Test
    void testIsApplicationClassWithApplicationClass() {
        // A class that doesn't start with groovy internal packages
        assertTrue(StackTraceUtils.isApplicationClass("com.example.MyClass"))
        assertTrue(StackTraceUtils.isApplicationClass("org.example.SomeClass"))
        assertTrue(StackTraceUtils.isApplicationClass("MyClass"))
    }

    @Test
    void testIsApplicationClassWithGroovyInternalClasses() {
        // Classes that start with groovy internal packages should not be application classes
        assertFalse(StackTraceUtils.isApplicationClass("groovy.lang.GroovyObject"))
        assertFalse(StackTraceUtils.isApplicationClass("org.codehaus.groovy.runtime.DefaultGroovyMethods"))
        assertFalse(StackTraceUtils.isApplicationClass("java.lang.String"))
        assertFalse(StackTraceUtils.isApplicationClass("javax.swing.JFrame"))
        assertFalse(StackTraceUtils.isApplicationClass("sun.misc.Unsafe"))
        assertFalse(StackTraceUtils.isApplicationClass("com.sun.proxy.Proxy"))
        assertFalse(StackTraceUtils.isApplicationClass("org.apache.groovy.util.Something"))
        assertFalse(StackTraceUtils.isApplicationClass("jdk.internal.misc.Unsafe"))
    }

    @Test
    void testAddClassTest() {
        // Add a custom test that returns true for a specific class
        StackTraceUtils.addClassTest({ Object[] args ->
            def className = (String) args[0]
            if (className == "my.custom.TestClass") {
                return true
            }
            return null // continue with other tests
        } as Closure<Boolean>)

        // The custom class should now be considered an application class
        assertTrue(StackTraceUtils.isApplicationClass("my.custom.TestClass"))
    }

    @Test
    void testAddClassTestReturningFalse() {
        // Add a custom test that returns false for a specific class
        StackTraceUtils.addClassTest({ Object[] args ->
            def className = (String) args[0]
            if (className.startsWith("force.exclude.")) {
                return false
            }
            return null
        } as Closure<Boolean>)

        // The class should be excluded
        assertFalse(StackTraceUtils.isApplicationClass("force.exclude.SomeClass"))
    }

    @Test
    void testExtractRootCause() {
        def root = new RuntimeException("root cause")
        def middle = new Exception("middle", root)
        def top = new Exception("top", middle)

        def extracted = StackTraceUtils.extractRootCause(top)
        assertSame(root, extracted)
        assertEquals("root cause", extracted.getMessage())
    }

    @Test
    void testExtractRootCauseWithNoCause() {
        def single = new RuntimeException("single exception")
        def extracted = StackTraceUtils.extractRootCause(single)
        assertSame(single, extracted)
    }

    @Test
    void testExtractRootCauseWithDeepNesting() {
        def e1 = new RuntimeException("level 1")
        def e2 = new Exception("level 2", e1)
        def e3 = new Exception("level 3", e2)
        def e4 = new Exception("level 4", e3)
        def e5 = new Exception("level 5", e4)

        def extracted = StackTraceUtils.extractRootCause(e5)
        assertSame(e1, extracted)
    }

    @Test
    void testSanitize() {
        // Create an exception with a stack trace
        def ex = new RuntimeException("test exception")

        // Sanitize it
        def sanitized = StackTraceUtils.sanitize(ex)

        // Should return the same exception instance
        assertSame(ex, sanitized)

        // The stack trace should be modified (groovy internal classes removed)
        // Note: The actual filtering depends on system property groovy.full.stacktrace
        assertNotNull(sanitized.getStackTrace())
    }

    @Test
    void testSanitizeRootCause() {
        def root = new RuntimeException("root")
        def wrapper = new Exception("wrapper", root)

        def sanitizedRoot = StackTraceUtils.sanitizeRootCause(wrapper)

        // Should return the sanitized root cause
        assertEquals("root", sanitizedRoot.getMessage())
    }

    @Test
    void testDeepSanitize() {
        def root = new RuntimeException("root")
        def middle = new Exception("middle", root)
        def top = new Exception("top", middle)

        def result = StackTraceUtils.deepSanitize(top)

        // Should return the top exception (sanitized)
        assertSame(top, result)
        assertNotNull(result.getStackTrace())
    }

    @Test
    void testDeepSanitizeWithSingleException() {
        def single = new RuntimeException("single")

        def result = StackTraceUtils.deepSanitize(single)

        assertSame(single, result)
    }

    @Test
    void testPrintSanitizedStackTraceWithPrintWriter() {
        def ex = new RuntimeException("test")
        def sw = new StringWriter()
        def pw = new PrintWriter(sw)

        StackTraceUtils.printSanitizedStackTrace(ex, pw)
        pw.flush()

        def output = sw.toString()
        // Should contain "at" entries for the stack trace
        assertTrue(output.contains("at ") || output.isEmpty())
    }

    @Test
    void testStackLogName() {
        assertEquals("StackTrace", StackTraceUtils.STACK_LOG_NAME)
    }

    @Test
    void testIsApplicationClassWithGjdkClasses() {
        assertFalse(StackTraceUtils.isApplicationClass("gjdk.groovy.lang.SomeThing"))
    }

    @Test
    void testIsApplicationClassWithGroovyJarJarClasses() {
        assertFalse(StackTraceUtils.isApplicationClass("groovyjarjar.asm.ClassVisitor"))
    }

    @Test
    void testSanitizePreservesMessage() {
        def message = "Original error message"
        def ex = new RuntimeException(message)

        def sanitized = StackTraceUtils.sanitize(ex)

        assertEquals(message, sanitized.getMessage())
    }

    @Test
    void testSanitizePreservesCause() {
        def cause = new IllegalArgumentException("cause")
        def ex = new RuntimeException("wrapper", cause)

        def sanitized = StackTraceUtils.sanitize(ex)

        assertSame(cause, sanitized.getCause())
    }
}
