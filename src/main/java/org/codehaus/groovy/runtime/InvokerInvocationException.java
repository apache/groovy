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

import java.lang.reflect.InvocationTargetException;

/**
 * An exception thrown if a method is called and an exception occurred
 */
public class InvokerInvocationException extends GroovyRuntimeException {

    private static final long serialVersionUID = 1337849572129640775L;

    public InvokerInvocationException(InvocationTargetException e) {
        super(e.getTargetException());
    }

    public InvokerInvocationException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        Throwable cause = getCause();
        return (cause==null)?"java.lang.NullPointerException":cause.toString();
    }
}
