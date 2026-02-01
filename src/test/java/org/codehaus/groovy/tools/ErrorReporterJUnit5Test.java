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
package org.codehaus.groovy.tools;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for ErrorReporter class.
 */
class ErrorReporterJUnit5Test {

    @Test
    void testConstructorWithException() {
        Exception e = new RuntimeException("test error");
        ErrorReporter reporter = new ErrorReporter(e);
        assertNotNull(reporter);
    }

    @Test
    void testConstructorWithExceptionAndDebug() {
        Exception e = new RuntimeException("test error");
        ErrorReporter reporter = new ErrorReporter(e, true);
        assertNotNull(reporter);
    }

    @Test
    void testWriteToPrintStream() {
        Exception e = new RuntimeException("test message");
        ErrorReporter reporter = new ErrorReporter(e);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertTrue(output.contains("test message"));
    }

    @Test
    void testWriteToPrintWriter() {
        Exception e = new RuntimeException("writer test");
        ErrorReporter reporter = new ErrorReporter(e);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        reporter.write(pw);
        
        String output = sw.toString();
        assertTrue(output.contains("writer test"));
    }

    @Test
    void testWriteWithDebugMode() {
        Exception e = new RuntimeException("debug error");
        ErrorReporter reporter = new ErrorReporter(e, true);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertTrue(output.contains("debug error"));
        assertTrue(output.contains("stacktrace"));
    }

    @Test
    void testReportCompilationFailedException() {
        Exception cause = new Exception("compile error");
        CompilationFailedException e = new CompilationFailedException(0, null, cause);
        ErrorReporter reporter = new ErrorReporter(e);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertNotNull(output);
    }

    @Test
    void testReportGroovyRuntimeException() {
        GroovyRuntimeException e = new GroovyRuntimeException("runtime error");
        ErrorReporter reporter = new ErrorReporter(e);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertTrue(output.contains("runtime error"));
    }

    @Test
    void testReportGenericException() {
        Exception e = new IllegalArgumentException("generic error");
        ErrorReporter reporter = new ErrorReporter(e);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertTrue(output.contains("generic error"));
    }

    @Test
    void testReportThrowable() {
        Throwable t = new Error("serious error");
        ErrorReporter reporter = new ErrorReporter(t);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertTrue(output.contains("serious error"));
        assertTrue(output.contains("a serious error occurred"));
    }

    @Test
    void testReportWithNullMessage() {
        Exception e = new RuntimeException((String) null);
        ErrorReporter reporter = new ErrorReporter(e);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        // Should not throw
        assertDoesNotThrow(() -> reporter.write(ps));
    }

    @Test
    void testDebugModeAlwaysShowsStackTrace() {
        Exception e = new RuntimeException("test");
        ErrorReporter reporter = new ErrorReporter(e, true);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        reporter.write(pw);
        
        String output = sw.toString();
        assertTrue(output.contains("stacktrace"));
    }

    @Test
    void testNonDebugModeForRegularException() {
        Exception e = new RuntimeException("simple error");
        ErrorReporter reporter = new ErrorReporter(e, false);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertTrue(output.contains("simple error"));
        // In non-debug mode, regular exceptions don't show stacktrace by default
    }

    @Test
    void testFlushIsCalled() {
        Exception e = new RuntimeException("flush test");
        ErrorReporter reporter = new ErrorReporter(e);
        
        final boolean[] flushed = {false};
        PrintStream ps = new PrintStream(new ByteArrayOutputStream()) {
            @Override
            public void flush() {
                flushed[0] = true;
                super.flush();
            }
        };
        
        reporter.write(ps);
        assertTrue(flushed[0]);
    }

    @Test
    void testExceptionChain() {
        Exception cause = new RuntimeException("root cause");
        Exception wrapper = new RuntimeException("wrapper", cause);
        ErrorReporter reporter = new ErrorReporter(wrapper);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        reporter.write(ps);
        
        String output = baos.toString();
        assertTrue(output.contains("wrapper"));
    }
}
