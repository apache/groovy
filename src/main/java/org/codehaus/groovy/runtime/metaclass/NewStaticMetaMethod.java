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

import org.codehaus.groovy.reflection.CachedMethod;

import java.lang.reflect.Modifier;

/**
 * A MetaMethod implementation where the underlying method is really a static
 * helper method on some class.
 *
 * This implementation is used to add new static methods to the JDK writing them as normal
 * static methods with the first parameter being the class on which the method is added.
 */
public class NewStaticMetaMethod extends NewMetaMethod {

    /**
     * Constructs a new NewStaticMetaMethod.
     *
     * @param method the cached static method
     */
    public NewStaticMetaMethod(CachedMethod method) {
        super(method);
    }

    /**
     * Indicates this method is static.
     *
     * @return true, as this wraps a static method
     */
    @Override
    public boolean isStatic() {
        return true;
    }

    /**
     * Returns the modifiers for this method (PUBLIC | STATIC).
     *
     * @return PUBLIC and STATIC modifiers
     */
    @Override
    public int getModifiers() {
        return Modifier.PUBLIC | Modifier.STATIC;
    }

    /**
     * Invokes the underlying static method with null as the first argument.
     * This adapts static method calls to the underlying static method signature.
     *
     * @param object the object (ignored for static methods)
     * @param arguments the method arguments
     * @return the method return value
     */
    @Override
    public Object invoke(Object object, Object[] arguments) {
        int size = arguments.length;
        Object[] newArguments = new Object[size + 1];
        System.arraycopy(arguments, 0, newArguments, 1, size);
        newArguments[0] = null;
        return super.invoke(null, newArguments);
    }
}
