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
package org.codehaus.groovy.runtime.metaclass;

import groovy.lang.MissingMethodException;

import java.io.Serial;

/**
 * A {@link MissingMethodException} that does not populate the stack trace.
 * This exception is optimized for performance when the stack trace is not needed,
 * as stack trace generation can be expensive.
 * <p>
 * This exception is for internal use only.
 */
public class MissingMethodExceptionNoStack extends MissingMethodException {

    @Serial private static final long serialVersionUID = -4567395518573062216L;

    /**
     * Constructs a new MissingMethodExceptionNoStack.
     *
     * @param method the name of the missing method
     * @param type the class where the method was not found
     * @param arguments the arguments that were passed to the missing method call
     */
    public MissingMethodExceptionNoStack(String method, Class type, Object[] arguments) {
        this(method,type,arguments,false);
    }

    /**
     * Constructs a new MissingMethodExceptionNoStack.
     *
     * @param method the name of the missing method
     * @param type the class where the method was not found
     * @param arguments the arguments that were passed to the missing method call
     * @param isStatic true if the missing method was a static method, false otherwise
     */
    public MissingMethodExceptionNoStack(String method, Class type, Object[] arguments, boolean isStatic) {
        super (method, type, arguments, isStatic);
    }

    /**
     * Overrides the default stack trace filling to optimize performance.
     * Returns this exception without populating the stack trace.
     *
     * @return this exception unchanged
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
