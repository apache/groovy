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
package org.apache.groovy.json.internal;

import groovy.json.JsonException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Exceptions class (JSON internal exception utilities).
 */
class ExceptionsTest {

    @Test
    void testDieNoMessage() {
        assertThrows(Exceptions.JsonInternalException.class, () -> {
            Exceptions.die();
        });
    }

    @Test
    void testDieWithMessage() {
        Exceptions.JsonInternalException ex = assertThrows(
            Exceptions.JsonInternalException.class,
            () -> Exceptions.die("custom message")
        );
        assertTrue(ex.getMessage().contains("custom message"));
    }

    @Test
    void testDieWithClassAndMessage() {
        assertThrows(Exceptions.JsonInternalException.class, () -> {
            Exceptions.die(String.class, "test error");
        });
    }

    @Test
    void testHandle() {
        Exception original = new RuntimeException("original");
        
        Exceptions.JsonInternalException ex = assertThrows(
            Exceptions.JsonInternalException.class,
            () -> Exceptions.handle(original)
        );
        
        assertSame(original, ex.getCause());
    }

    @Test
    void testHandleWithClass() {
        Exception original = new IllegalArgumentException("arg error");
        
        Exceptions.JsonInternalException ex = assertThrows(
            Exceptions.JsonInternalException.class,
            () -> Exceptions.handle(String.class, original)
        );
        
        assertSame(original, ex.getCause());
    }

    @Test
    void testHandleWithClassAlreadyJsonInternalException() {
        Exceptions.JsonInternalException original = new Exceptions.JsonInternalException("already wrapped");
        
        Exceptions.JsonInternalException ex = assertThrows(
            Exceptions.JsonInternalException.class,
            () -> Exceptions.handle(String.class, original)
        );
        
        assertSame(original, ex);
    }

    @Test
    void testHandleWithClassMessageAndThrowable() {
        Throwable original = new RuntimeException("cause");
        
        Exceptions.JsonInternalException ex = assertThrows(
            Exceptions.JsonInternalException.class,
            () -> Exceptions.handle(String.class, "wrapper message", original)
        );
        
        assertTrue(ex.getMessage().contains("wrapper message"));
        assertSame(original, ex.getCause());
    }

    @Test
    void testHandleWithMessageAndThrowable() {
        Throwable original = new RuntimeException("cause");
        
        Exceptions.JsonInternalException ex = assertThrows(
            Exceptions.JsonInternalException.class,
            () -> Exceptions.handle("error occurred", original)
        );
        
        assertTrue(ex.getMessage().contains("error occurred"));
        assertSame(original, ex.getCause());
    }

    @Test
    void testJsonInternalExceptionMessage() {
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException("test message");
        assertEquals("test message", ex.getMessage());
    }

    @Test
    void testJsonInternalExceptionWithCause() {
        RuntimeException cause = new RuntimeException("cause message");
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException("wrapper", cause);
        
        assertTrue(ex.getMessage().contains("wrapper"));
        assertTrue(ex.getMessage().contains("cause message"));
        assertSame(cause, ex.getCause());
    }

    @Test
    void testJsonInternalExceptionWrappingCause() {
        RuntimeException cause = new RuntimeException("original error");
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException(cause);
        
        assertEquals("Wrapped Exception", ex.getMessage().split("\n")[0].trim());
        assertSame(cause, ex.getCause());
    }

    @Test
    void testJsonInternalExceptionGetLocalizedMessage() {
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException("localized test");
        assertEquals(ex.getMessage(), ex.getLocalizedMessage());
    }

    @Test
    void testJsonInternalExceptionGetStackTraceWithCause() {
        RuntimeException cause = new RuntimeException("cause");
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException(cause);
        
        // Should return the cause's stack trace elements (equivalent content)
        StackTraceElement[] causeTrace = cause.getStackTrace();
        StackTraceElement[] exTrace = ex.getStackTrace();
        
        assertEquals(causeTrace.length, exTrace.length);
        for (int i = 0; i < causeTrace.length; i++) {
            assertEquals(causeTrace[i], exTrace[i]);
        }
    }

    @Test
    void testJsonInternalExceptionGetStackTraceWithoutCause() {
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException("no cause");
        
        // Should return its own stack trace
        assertNotNull(ex.getStackTrace());
        assertTrue(ex.getStackTrace().length > 0);
    }

    @Test
    void testJsonInternalExceptionGetCause() {
        RuntimeException cause = new RuntimeException("the cause");
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException(cause);
        
        assertSame(cause, ex.getCause());
    }

    @Test
    void testJsonInternalExceptionPrintStackTracePrintStream() {
        RuntimeException cause = new RuntimeException("cause error");
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException(cause);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        ex.printStackTrace(ps);
        ps.flush();
        
        String output = baos.toString();
        assertTrue(output.contains("Wrapped Exception"));
        assertTrue(output.contains("original exception"));
    }

    @Test
    void testJsonInternalExceptionPrintStackTracePrintStreamNoCause() {
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException("direct error");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        
        ex.printStackTrace(ps);
        ps.flush();
        
        String output = baos.toString();
        assertTrue(output.contains("direct error"));
    }

    @Test
    void testJsonInternalExceptionPrintStackTracePrintWriter() {
        RuntimeException cause = new RuntimeException("writer cause");
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException(cause);
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        ex.printStackTrace(pw);
        pw.flush();
        
        String output = sw.toString();
        assertTrue(output.contains("Wrapped Exception"));
    }

    @Test
    void testJsonInternalExceptionPrintStackTracePrintWriterNoCause() {
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException("writer direct");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        ex.printStackTrace(pw);
        pw.flush();
        
        String output = sw.toString();
        assertTrue(output.contains("writer direct"));
    }

    @Test
    void testJsonInternalExceptionIsJsonException() {
        Exceptions.JsonInternalException ex = new Exceptions.JsonInternalException("test");
        assertTrue(ex instanceof JsonException);
    }

    @Test
    void testToString() {
        Exception ex = new RuntimeException("test error for toString");
        String result = Exceptions.toString(ex);
        
        assertNotNull(result);
        assertTrue(result.contains("test error for toString"));
    }

    @Test
    void testSputsWithBuffer() {
        CharBuf buf = CharBuf.create(100);
        String result = Exceptions.sputs(buf, "hello", "world", 42);
        
        assertTrue(result.contains("hello"));
        assertTrue(result.contains("world"));
        assertTrue(result.contains("42"));
    }

    @Test
    void testSputsWithoutBuffer() {
        String result = Exceptions.sputs("one", "two", "three");
        
        assertTrue(result.contains("one"));
        assertTrue(result.contains("two"));
        assertTrue(result.contains("three"));
    }

    @Test
    void testSputsWithNullValue() {
        String result = Exceptions.sputs("value", null, "other");
        
        assertTrue(result.contains("value"));
        assertTrue(result.contains("<NULL>"));
        assertTrue(result.contains("other"));
    }

    @Test
    void testSputsWithArray() {
        Object[] arr = {"a", "b"};
        String result = Exceptions.sputs("prefix", arr);
        
        assertTrue(result.contains("prefix"));
        // Arrays are printed using Collections.singletonList().toString()
        assertNotNull(result);
    }

    @Test
    void testSputsEmpty() {
        String result = Exceptions.sputs();
        assertNotNull(result);
        assertTrue(result.endsWith("\n"));
    }

    @Test
    void testSputsSingleValue() {
        String result = Exceptions.sputs("single");
        assertTrue(result.contains("single"));
    }

    @Test
    void testToStringWithNestedCause() {
        RuntimeException cause = new RuntimeException("root cause");
        Exception wrapper = new Exception("wrapper", cause);
        
        String result = Exceptions.toString(wrapper);
        assertNotNull(result);
        assertTrue(result.contains("wrapper"));
    }
}
