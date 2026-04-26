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

import static java.lang.System.Logger.Level.ERROR;

/**
 * Internal exception helpers used by the JSON parser implementation.
 */
public class Exceptions {

    private static final System.Logger LOGGER = System.getLogger(Exceptions.class.getName());

    /**
     * Throws a generic internal JSON exception.
     *
     * @return never returns normally
     */
    public static boolean die() {
        throw new JsonInternalException("died");
    }

    /**
     * Throws an internal JSON exception with the supplied message.
     *
     * @param message the failure message
     * @return never returns normally
     */
    public static boolean die(String message) {
        throw new JsonInternalException(message);
    }

    /**
     * Throws an internal JSON exception with the supplied message.
     *
     * @param clazz the ignored target type used for generic flow
     * @param message the failure message
     * @param <T> the generic return type
     * @return never returns normally
     */
    public static <T> T die(Class<T> clazz, String message) {
        throw new JsonInternalException(message);
    }

    /**
     * Rethrows the supplied exception as an internal JSON exception.
     *
     * @param e the exception to wrap
     */
    public static void handle(java.lang.Exception e) {
        throw new JsonInternalException(e);
    }

    /**
     * Rethrows the supplied exception as an internal JSON exception.
     *
     * @param clazz the ignored target type used for generic flow
     * @param e the exception to wrap
     * @param <T> the generic return type
     * @return never returns normally
     */
    public static <T> T handle(Class<T> clazz, java.lang.Exception e) {
        if (e instanceof JsonInternalException) {
            throw (JsonInternalException) e;
        }
        throw new JsonInternalException(e);
    }

    /**
     * Rethrows the supplied throwable as an internal JSON exception with a message.
     *
     * @param clazz the ignored target type used for generic flow
     * @param message the failure message
     * @param e the throwable to wrap
     * @param <T> the generic return type
     * @return never returns normally
     */
    public static <T> T handle(Class<T> clazz, String message, Throwable e) {
        throw new JsonInternalException(message, e);
    }

    /**
     * Rethrows the supplied throwable as an internal JSON exception with a message.
     *
     * @param message the failure message
     * @param e the throwable to wrap
     */
    public static void handle(String message, Throwable e) {
        throw new JsonInternalException(message, e);
    }

    /**
     * Runtime exception used inside the JSON internals to simplify propagation.
     */
    public static class JsonInternalException extends JsonException {

        /**
         * Creates an exception with the supplied message.
         *
         * @param message the exception message
         */
        public JsonInternalException(String message) {
            super(message);
        }

        /**
         * Creates an exception with the supplied message and cause.
         *
         * @param message the exception message
         * @param cause the wrapped cause
         */
        public JsonInternalException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Creates an exception that wraps another throwable.
         *
         * @param cause the wrapped cause
         */
        public JsonInternalException(Throwable cause) {
            super("Wrapped Exception", cause);
        }

        /**
         * Prints the wrapped stack trace to the supplied stream.
         *
         * @param s the target stream
         */
        @Override
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

        /**
         * Returns the message, including the wrapped cause summary when present.
         *
         * @return the formatted exception message
         */
        @Override
        public String getMessage() {
            return super.getMessage() + (getCause() == null ? "" :
                    getCauseMessage());
        }

        private String getCauseMessage() {
            return "\n CAUSE " + getCause().getClass().getName() + " :: " +
                    getCause().getMessage();
        }

        /**
         * Returns the localized message.
         *
         * @return the localized message
         */
        @Override
        public String getLocalizedMessage() {
            return this.getMessage();
        }

        /**
         * Returns the wrapped stack trace when a cause is present.
         *
         * @return the effective stack trace
         */
        @Override
        public StackTraceElement[] getStackTrace() {
            if (getCause() != null) {
                return getCause().getStackTrace();
            } else {
                return super.getStackTrace();
            }
        }

        /**
         * Returns the wrapped cause.
         *
         * @return the wrapped cause, or {@code null}
         */
        @Override
        public Throwable getCause() {
            return super.getCause();
        }

        /**
         * Prints the wrapped stack trace to the supplied writer.
         *
         * @param s the target writer
         */
        @Override
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

        /**
         * Logs the wrapped stack trace.
         */
        @Override
        public void printStackTrace() {
            LOGGER.log(ERROR, this.getMessage());

            if (getCause() != null) {
                LOGGER.log(ERROR, "This Exception was wrapped, the original exception stack trace is:", getCause());
            } else {
                super.printStackTrace();
            }
        }
    }

    /**
     * Formats an exception and its stack trace for debug output.
     *
     * @param ex the exception to format
     * @return the formatted exception text
     */
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

    /**
     * Appends messages to the supplied buffer and returns its current contents.
     *
     * @param buf the destination buffer
     * @param messages the messages to append
     * @return the buffer content after appending the messages
     */
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

    /**
     * Creates a temporary buffer, appends the supplied messages, and returns the result.
     *
     * @param messages the messages to append
     * @return the formatted message text
     */
    public static String sputs(Object... messages) {
        CharBuf buf = CharBuf.create(100);
        return sputs(buf, messages);
    }
}
