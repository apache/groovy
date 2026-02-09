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
package org.codehaus.groovy.tools

import groovy.lang.GroovyRuntimeException
import org.codehaus.groovy.control.CompilationFailedException
import org.junit.jupiter.api.Test

import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter
import java.io.ByteArrayOutputStream

import org.junit.jupiter.api.function.Executable

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for ErrorReporter class.
 */
class ErrorReporterTest {

    @Test
    void testConstructorWithException() {
        def e = new RuntimeException("test error")
        def reporter = new ErrorReporter(e)
        assertNotNull(reporter)
    }

    @Test
    void testConstructorWithExceptionAndDebug() {
        def e = new RuntimeException("test error")
        def reporter = new ErrorReporter(e, true)
        assertNotNull(reporter)
    }

    @Test
    void testWriteToPrintStream() {
        def e = new RuntimeException("test message")
        def reporter = new ErrorReporter(e)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertTrue(output.contains("test message"))
    }

    @Test
    void testWriteToPrintWriter() {
        def e = new RuntimeException("writer test")
        def reporter = new ErrorReporter(e)

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)

        reporter.write(pw)

        def output = sw.toString()
        assertTrue(output.contains("writer test"))
    }

    @Test
    void testWriteWithDebugMode() {
        def e = new RuntimeException("debug error")
        def reporter = new ErrorReporter(e, true)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertTrue(output.contains("debug error"))
        assertTrue(output.contains("stacktrace"))
    }

    @Test
    void testReportCompilationFailedException() {
        def cause = new Exception("compile error")
        def e = new CompilationFailedException(0, null, cause)
        def reporter = new ErrorReporter(e)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertNotNull(output)
    }

    @Test
    void testReportGroovyRuntimeException() {
        def e = new GroovyRuntimeException("runtime error")
        def reporter = new ErrorReporter(e)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertTrue(output.contains("runtime error"))
    }

    @Test
    void testReportGenericException() {
        def e = new IllegalArgumentException("generic error")
        def reporter = new ErrorReporter(e)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertTrue(output.contains("generic error"))
    }

    @Test
    void testReportThrowable() {
        def t = new Error("serious error")
        def reporter = new ErrorReporter(t)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertTrue(output.contains("serious error"))
        assertTrue(output.contains("a serious error occurred"))
    }

    @Test
    void testReportWithNullMessage() {
        def e = new RuntimeException((String) null)
        def reporter = new ErrorReporter(e)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        // Should not throw
        assertDoesNotThrow({ -> reporter.write(ps) } as Executable)
    }

    @Test
    void testDebugModeAlwaysShowsStackTrace() {
        def e = new RuntimeException("test")
        def reporter = new ErrorReporter(e, true)

        def sw = new StringWriter()
        def pw = new PrintWriter(sw)

        reporter.write(pw)

        def output = sw.toString()
        assertTrue(output.contains("stacktrace"))
    }

    @Test
    void testNonDebugModeForRegularException() {
        def e = new RuntimeException("simple error")
        def reporter = new ErrorReporter(e, false)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertTrue(output.contains("simple error"))
        // In non-debug mode, regular exceptions don't show stacktrace by default
    }

    @Test
    void testFlushIsCalled() {
        def e = new RuntimeException("flush test")
        def reporter = new ErrorReporter(e)

        def flushed = [false]
        def ps = new PrintStream(new ByteArrayOutputStream()) {
            void flush() {
                flushed[0] = true
                super.flush()
            }
        }

        reporter.write(ps)
        assertTrue(flushed[0])
    }

    @Test
    void testExceptionChain() {
        def cause = new RuntimeException("root cause")
        def wrapper = new RuntimeException("wrapper", cause)
        def reporter = new ErrorReporter(wrapper)

        def baos = new ByteArrayOutputStream()
        def ps = new PrintStream(baos)

        reporter.write(ps)

        def output = baos.toString()
        assertTrue(output.contains("wrapper"))
    }
}
