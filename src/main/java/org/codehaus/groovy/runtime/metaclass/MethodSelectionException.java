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

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedConstructor;
import org.codehaus.groovy.util.FastArray;

import java.lang.reflect.Modifier;

/**
 * This exception is thrown if the runtime is unable to select
 * a method. This class builds the exception text when calling 
 * getMessage.
 * <p>
 * <b>Note:</b> This exception as for internal use only!
 * 
 * @since Groovy 1.1
 */
public class MethodSelectionException extends GroovyRuntimeException {

    private static final long serialVersionUID = 8126246630023758333L;
    private final String methodName;
    private final FastArray methods;
    private final Class[] arguments;
    
    /**
     * Creates a new MethodSelectionException.
     * @param methodName name of the method
     * @param methods    a FastArray of methods
     * @param arguments  the method call argument classes
     */
    public MethodSelectionException(String methodName, FastArray methods, Class[] arguments) {
        super(methodName);
        this.methodName = methodName;
        this.arguments = arguments;
        this.methods = methods;
    }

    public String getMessage() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Could not find which method ").append(methodName);
        appendClassNames(buffer,arguments);
        buffer.append(" to invoke from this list:");
        appendMethods(buffer);
        return buffer.toString();
    }
    
    
    private static void appendClassNames(StringBuilder argBuf, Class[] classes) {
        argBuf.append("(");
        for (int i = 0; i < classes.length; i++) {
            if (i > 0) {
                argBuf.append(", ");
            }
            Class clazz = classes[i];
            String name = clazz==null? "null": clazz.getName();
            argBuf.append(name);
        }
        argBuf.append(")");
    }
    
    private void appendMethods(StringBuilder buffer) {
        for (int i = 0; i < methods.size; i++) {
            buffer.append("\n  ");
            Object methodOrConstructor = methods.get(i);
            if (methodOrConstructor instanceof MetaMethod) {
                MetaMethod method = (MetaMethod) methodOrConstructor;
                buffer.append(Modifier.toString(method.getModifiers()));
                buffer.append(" ").append(method.getReturnType().getName());
                buffer.append(" ").append(method.getDeclaringClass().getName());
                buffer.append("#");
                buffer.append(method.getName());
                appendClassNames(buffer,method.getNativeParameterTypes());
            }
            else {
                CachedConstructor constructor = (CachedConstructor) methodOrConstructor;
                buffer.append(Modifier.toString(constructor.getModifiers()));
                buffer.append(" ").append(constructor.getDeclaringClass().getName());
                buffer.append("#<init>");
                appendClassNames(buffer,constructor.getNativeParameterTypes());
            }
        }
    }
}