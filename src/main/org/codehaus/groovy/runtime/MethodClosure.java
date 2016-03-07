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
import java.util.Arrays;


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
    private boolean isStaticMethod;

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
                isStaticMethod = m.isStatic();
            }
        }

        if (owner instanceof Class && !isStaticMethod) {
            maximumNumberOfParameters++;
            Class[] newParameterTypes = new Class[parameterTypes.length+1];
            System.arraycopy(parameterTypes, 0, newParameterTypes, 1, parameterTypes.length);
            newParameterTypes[0] = (Class)owner;
            parameterTypes = newParameterTypes;
        }
    }
    
    public String getMethod() {
        return method;
    }

    protected Object doCall(Object arguments) {
        if (getOwner() instanceof Class && !isStaticMethod) {
            if (arguments instanceof Object[]) {
                Object[] args = (Object[])arguments;
                Object insertedReceiver = args[0];
                Object[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                return InvokerHelper.invokeMethod(insertedReceiver, method, newArgs);
            }
            return InvokerHelper.invokeMethod(arguments, method, new Object[]{});
        }
        return InvokerHelper.invokeMethod(getOwner(), method, arguments);
    }

    @SuppressWarnings("unchecked")
    public Object call(Object... args) {
        try {
            return doCall(args);
        } catch (InvokerInvocationException e) {
            ExceptionUtils.sneakyThrow(e.getCause());
            return null; // unreachable statement
        }  catch (Exception e) {
            return throwRuntimeException(e);
        }
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
        }
        else if ("staticMethod".equals(property)) {
            return isStaticMethod;
        }
        else  return super.getProperty(property);
    }
}
