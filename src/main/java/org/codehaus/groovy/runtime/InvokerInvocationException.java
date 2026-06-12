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

import groovy.lang.GroovyRuntimeException;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

/**
 * Exception thrown when a method invocation targets and throws an exception.
 * Wraps the underlying exception for proper exception chaining.
 */
public class InvokerInvocationException extends GroovyRuntimeException {

    @Serial private static final long serialVersionUID = 1337849572129640775L;

    /**
     * Constructs an InvokerInvocationException from an {@link InvocationTargetException}.
     * Extracts and wraps the underlying target exception.
     *
     * @param e the {@link InvocationTargetException} whose target exception will be wrapped
     */
    public InvokerInvocationException(InvocationTargetException e) {
        super(e.getTargetException());
    }

    /**
     * Constructs an InvokerInvocationException from any {@link Throwable}.
     *
     * @param cause the underlying cause of the invocation failure
     */
    public InvokerInvocationException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns a string representation of this exception message.
     * If a cause exists, returns its string representation; otherwise returns the NullPointerException message.
     *
     * @return the exception message
     */
    @Override
    public String getMessage() {
        Throwable cause = getCause();
        return (cause==null)?"java.lang.NullPointerException":cause.toString();
    }
}
