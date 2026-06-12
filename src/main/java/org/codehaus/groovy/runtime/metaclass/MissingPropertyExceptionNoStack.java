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

import groovy.lang.MissingPropertyException;

import java.io.Serial;

/**
 * A {@link MissingPropertyException} that does not populate the stack trace.
 * This exception is optimized for performance when the stack trace is not needed,
 * as stack trace generation can be expensive.
 * <p>
 * This exception is for internal use only.
 */
public class MissingPropertyExceptionNoStack extends MissingPropertyException {

    @Serial private static final long serialVersionUID = 8993570436675442348L;

    /**
     * Constructs a new MissingPropertyExceptionNoStack.
     *
     * @param propertyName the name of the missing property
     * @param theClass the class where the property was not found
     */
    public MissingPropertyExceptionNoStack(String propertyName, Class theClass) {
        super(propertyName, theClass);
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
