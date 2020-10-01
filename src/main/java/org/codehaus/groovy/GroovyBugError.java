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
package org.codehaus.groovy;

/**
 * This class represents an error that is thrown when a bug is
 * recognized inside the runtime. Basically it is thrown when
 * a constraint is not fulfilled that should be fulfilled.
 */
public class GroovyBugError extends AssertionError {

    private static final long serialVersionUID = -9165076784700059275L;
    // message string
    private String message;
    // optional exception
    private final Exception exception;

    /**
     * constructs a bug error using the given text
     *
     * @param message the error message text
     */
    public GroovyBugError(String message) {
        this(message, null);
    }

    /**
     * Constructs a bug error using the given exception
     *
     * @param exception cause of this error
     */
    public GroovyBugError(Exception exception) {
        this(null, exception);
    }

    /**
     * Constructs a bug error using the given exception and
     * a text with additional information about the cause
     *
     * @param msg       additional information about this error
     * @param exception cause of this error
     */
    public GroovyBugError(String msg, Exception exception) {
        this.exception = exception;
        this.message = msg;
    }

    /**
     * Returns a String representation of this class by calling <code>getMessage()</code>.
     *
     * @see #getMessage()
     */
    @Override
    public String toString() {
        return getMessage();
    }

    /**
     * Returns the detail message string of this error. The message
     * will consist of the bug text prefixed by "BUG! " if there this
     * instance was created using a message. If this error was
     * constructed without using a bug text the message of the cause
     * is used prefixed by "BUG! UNCAUGHT EXCEPTION: "
     *
     * @return the detail message string of this error.
     */
    @Override
    public String getMessage() {
        if (message != null) {
            return "BUG! " + message;
        } else {
            return "BUG! UNCAUGHT EXCEPTION: " + exception.getMessage();
        }
    }

    @Override
    public Throwable getCause() {
        return this.exception;
    }

    /**
     * Returns the bug text to describe this error
     */
    public String getBugText() {
        if (message != null) {
            return message;
        } else {
            return exception.getMessage();
        }
    }

    /**
     * Sets the bug text to describe this error
     */
    public void setBugText(String msg) {
        this.message = msg;
    }
}
