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

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;

/**
 * Base class for NewInstanceMetaMethod and NewStaticMetaMethod
 */
public class NewMetaMethod extends ReflectionMetaMethod {
    /**
     * Empty array of cached classes used as default
     */
    protected static final CachedClass[] EMPTY_TYPE_ARRAY = {};
    /**
     * The parameter types from the bytecode (including the first parameter which is the class)
     */
    protected CachedClass[] bytecodeParameterTypes ;

    /**
     * Constructs a new NewMetaMethod.
     *
     * @param method the cached method
     */
    public NewMetaMethod(CachedMethod method) {
        super(method);
        bytecodeParameterTypes = method.getParameterTypes();

        int size = bytecodeParameterTypes.length;
        CachedClass[] logicalParameterTypes;
        if (size <= 1) {
            logicalParameterTypes = EMPTY_TYPE_ARRAY;
        } else {
            logicalParameterTypes = new CachedClass[--size];
            System.arraycopy(bytecodeParameterTypes, 1, logicalParameterTypes, 0, size);
        }
        setParametersTypes(logicalParameterTypes);
    }

    @Override
    public CachedClass getDeclaringClass() {
        return getBytecodeParameterTypes()[0];
    }

    /**
     * Gets the bytecode parameter types (including the first parameter which is the class).
     *
     * @return the bytecode parameter types
     */
    public CachedClass[] getBytecodeParameterTypes() {
        return bytecodeParameterTypes;
    }

    /**
     * Gets the owner class (the first bytecode parameter type).
     *
     * @return the owner class
     */
    public CachedClass getOwnerClass() {
        return getBytecodeParameterTypes()[0];
    }
}
