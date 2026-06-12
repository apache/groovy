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

import java.io.Serial;

/**
 * Base exception class for Groovy compiler errors and warnings.
 * Implements {@link GroovyExceptionInterface} to track exception severity.
 */
public class GroovyException extends Exception implements GroovyExceptionInterface {
    @Serial private static final long serialVersionUID = -61298636122042408L;

    /**
     * Indicates whether this exception is fatal and should halt compilation.
     * Defaults to {@code true}.
     */
    private boolean fatal = true;

    /**
     * Constructs a {@code GroovyException} with no detail message or cause.
     */
    public GroovyException() {
    }

    /**
     * Constructs a {@code GroovyException} with the specified detail message.
     *
     * @param message the detail message describing this exception
     */
    public GroovyException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code GroovyException} with the specified detail message and cause.
     *
     * @param message the detail message describing this exception
     * @param cause the underlying cause of this exception
     */
    public GroovyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code GroovyException} with the specified fatality flag.
     *
     * @param fatal {@code true} if this exception should halt compilation; {@code false} otherwise
     */
    public GroovyException(boolean fatal) {
        super();
        this.fatal = fatal;
    }

    /**
     * Constructs a {@code GroovyException} with the specified detail message and fatality flag.
     *
     * @param message the detail message describing this exception
     * @param fatal {@code true} if this exception should halt compilation; {@code false} otherwise
     */
    public GroovyException(String message, boolean fatal) {
        super(message);
        this.fatal = fatal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFatal() {
        return fatal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFatal(boolean fatal) {
        this.fatal = fatal;
    }
}
