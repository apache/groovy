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
package org.codehaus.groovy.syntax;

import org.codehaus.groovy.GroovyException;

import java.io.IOException;
import java.io.Serial;

/**
 * Exception that wraps I/O exceptions ({@link IOException}) in a Groovy-specific context.
 * Provides convenient access to the underlying I/O cause while maintaining the
 * {@link GroovyException} hierarchy.
 */
public class ReadException extends GroovyException {
    @Serial private static final long serialVersionUID = 848585058428047961L;
    /** The underlying I/O exception. */
    private final IOException cause;

    /**
     * Constructs a ReadException from an I/O exception.
     * No specific message is provided.
     *
     * @param cause the {@link IOException} that triggered this exception
     */
    public ReadException(IOException cause) {
        super();
        this.cause = cause;
    }

    /**
     * Constructs a ReadException from a message and an I/O exception.
     *
     * @param message the error message
     * @param cause the underlying {@link IOException} that triggered this exception
     */
    public ReadException(String message, IOException cause) {
        super(message);
        this.cause = cause;
    }

    /**
     * Returns the underlying I/O cause of this exception.
     *
     * @return the {@link IOException} that caused this exception
     */
    public IOException getIOCause() {
        return this.cause;
    }

    /**
     * Returns a string representation of this exception.
     * If no custom message was provided, returns the cause's message.
     *
     * @return the exception message or I/O cause message
     */
    @Override
    public String toString() {
        String message = super.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = cause.getMessage();
        }

        return message;
    }

    /**
     * Returns the exception message.
     * Equivalent to {@link #toString()} for this exception type.
     *
     * @return the exception message or I/O cause message
     */
    @Override
    public String getMessage() {
        return toString();
    }
}
