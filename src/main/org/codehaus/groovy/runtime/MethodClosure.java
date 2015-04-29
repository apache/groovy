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

import groovy.lang.Closure;
import groovy.lang.MetaMethod;

import java.util.List;


/**
 * Represents a method on an object using a closure which can be invoked
 * at any time
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class MethodClosure extends Closure {

    public static boolean ALLOW_RESOLVE = false;

    private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
    private String method;

    public MethodClosure(Object owner, String method) {
        super(owner);
        this.method = method;

        final Class clazz = owner.getClass()==Class.class?(Class) owner:owner.getClass();
        
        maximumNumberOfParameters = 0;
        parameterTypes = EMPTY_CLASS_ARRAY;

        List<MetaMethod> methods = InvokerHelper.getMetaClass(clazz).respondsTo(owner, method);
        
        for(MetaMethod m : methods) {
            if (m.getParameterTypes().length > maximumNumberOfParameters) {
                Class[] pt = m.getNativeParameterTypes();
                maximumNumberOfParameters = pt.length;
                parameterTypes = pt;
            }
        }
    }
    
    public String getMethod() {
        return method;
    }

    protected Object doCall(Object arguments) {
        return InvokerHelper.invokeMethod(getOwner(), method, arguments);
    }

    private Object readResolve() {
        if (ALLOW_RESOLVE) {
            return this;
        }
        throw new UnsupportedOperationException();
    }
    
    public Object getProperty(String property) {
        if ("method".equals(property)) {
            return getMethod();
        } else  return super.getProperty(property);        
    }
}
