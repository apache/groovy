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

import java.lang.reflect.Method;
import groovy.lang.Closure;

/**
 * This class is a general adapter to adapt a closure to any Java interface.
 * <p>
 * @author Ben Yu
 * @author <a href="mailto:blackdrag@gmx.org">Jochen Theodorou</a>
 * Jul 27, 2006 3:50:51 PM
 */
public class ConvertedClosure extends ConversionHandler {
    private String methodName;
    
    /**
     * to create a ConvertedClosure object.
     * @param closure the closure object.
     */
    public ConvertedClosure(Closure closure, String method) {
        super(closure);
        this.methodName = method;
    }
    
    public ConvertedClosure(Closure closure) {
        this(closure,null);
    }
    
    public Object invokeCustom(Object proxy, Method method, Object[] args)
    throws Throwable {
        if (methodName!=null && !methodName.equals(method.getName())) return null;
        return ((Closure) getDelegate()).call(args);
    }
}

