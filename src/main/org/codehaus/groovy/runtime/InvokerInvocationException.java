/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.GroovyRuntimeException;

import java.lang.reflect.InvocationTargetException;

/**
 * An exception thrown if a method is called and an exception occurred
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class InvokerInvocationException extends GroovyRuntimeException {

    public InvokerInvocationException(InvocationTargetException e) {
        super(e.getTargetException());
    }

    public InvokerInvocationException(Throwable cause) {
        super(cause);
    }

    public String getMessage() {
        Throwable cause = getCause();
        return (cause==null)?"java.lang.NullPointerException":cause.toString();
    }
}
