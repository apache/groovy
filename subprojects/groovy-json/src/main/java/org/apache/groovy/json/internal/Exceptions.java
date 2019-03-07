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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;

public class Exceptions {

    public static boolean die() {
        throw new JsonInternalException("died");
    }

    public static boolean die(String message) {
        throw new JsonInternalException(message);
    }

    public static <T> T die(Class<T> clazz, String message) {
        throw new JsonInternalException(message);
    }

    public static void handle(java.lang.Exception e) {
        throw new JsonInternalException(e);
    }

    public static <T> T handle(Class<T> clazz, java.lang.Exception e) {
        if (e instanceof JsonInternalException) {
            throw (JsonInternalException) e;
        }
        throw new JsonInternalException(e);
    }

    public static <T> T handle(Class<T> clazz, String message, Throwable e) {
        throw new JsonInternalException(message, e);
    }

    public static void handle(String message, Throwable e) {
        throw new JsonInternalException(message, e);
    }

    public static class JsonInternalException extends JsonException {

        public JsonInternalException(String message) {
            super(message);
        }

        public JsonInternalException(String message, Throwable cause) {
            super(message, cause);
        }

        public JsonInternalException(Throwable cause) {
            super("Wrapped Exception", cause);
        }

        public void printStackTrace(PrintStream s) {
            s.println(this.getMessage());
            if (getCause() != null) {
                s.println("This Exception was wrapped, the original exception\n" +
                        "stack trace is:\n");
                getCause().printStackTrace(s);
            } else {
                super.printStackTrace(s);
            }
        }

        public String getMessage() {
            return super.getMessage() + (getCause() == null ? "" :
                    getCauseMessage());
        }

        private String getCauseMessage() {
            return "\n CAUSE " + getCause().getClass().getName() + " :: " +
                    getCause().getMessage();
        }

        public String getLocalizedMessage() {
            return this.getMessage();
        }

        public StackTraceElement[] getStackTrace() {
            if (getCause() != null) {
                return getCause().getStackTrace();
            } else {
                return super.getStackTrace();
            }
        }

        public Throwable getCause() {
            return super.getCause();
        }

        public void printStackTrace(PrintWriter s) {
            s.println(this.getMessage());

            if (getCause() != null) {
                s.println("This Exception was wrapped, the original exception\n" +
                        "stack trace is:\n");
                getCause().printStackTrace(s);
            } else {
                super.printStackTrace(s);
            }
        }

        public void printStackTrace() {
            System.err.println(this.getMessage());

            if (getCause() != null) {
                System.err.println("This Exception was wrapped, the original exception\n" +
                        "stack trace is:\n");
                getCause().printStackTrace();
            } else {
                super.printStackTrace();
            }
        }
    }

    public static String toString(Exception ex) {
        CharBuf buffer = CharBuf.create(255);
        buffer.addLine(ex.getLocalizedMessage());

        final StackTraceElement[] stackTrace = ex.getStackTrace();
        for (StackTraceElement element : stackTrace) {
            buffer.add(element.getClassName());
            sputs(buffer, "class", element.getClassName(),
                    "method", element.getMethodName(), "line", element.getLineNumber());
        }

        return buffer.toString();
    }

    public static String sputs(CharBuf buf, Object... messages) {
        int index = 0;
        for (Object message : messages) {
            if (index != 0) {
                buf.add(' ');
            }
            index++;

            if (message == null) {
                buf.add("<NULL>");
            } else if (message.getClass().isArray()) {
                buf.add(Collections.singletonList(message).toString());
            } else {
                buf.add(message.toString());
            }
        }
        buf.add('\n');

        return buf.toString();
    }

    public static String sputs(Object... messages) {
        CharBuf buf = CharBuf.create(100);
        return sputs(buf, messages);
    }
}
