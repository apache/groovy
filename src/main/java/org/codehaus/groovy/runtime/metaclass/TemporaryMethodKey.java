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

import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.MethodKey;


/**
 * A temporary implementation of MethodKey used to perform a fast lookup
 * for a method using a set of arguments to a method
 */
public class TemporaryMethodKey extends MethodKey {

    /**
     * The parameter values used to look up the method
     */
    private final Object[] parameterValues;

    /**
     * Constructs a new TemporaryMethodKey.
     *
     * @param sender the class making the method call
     * @param name the method name
     * @param parameterValues the actual parameter values (used to determine types)
     * @param isCallToSuper true if this is a call to a super method
     */
    public TemporaryMethodKey(Class sender, String name, Object[] parameterValues, boolean isCallToSuper) {
        super(sender, name, isCallToSuper);
        if (parameterValues == null) {
            parameterValues = MetaClassHelper.EMPTY_ARRAY;
        }
        this.parameterValues = parameterValues;
    }

    /**
     * Returns the number of parameters for this method key.
     *
     * @return the number of parameter values
     */
    @Override
    public int getParameterCount() {
        return parameterValues.length;
    }

    /**
     * Gets the class type of the parameter at the specified index.
     * If the parameter value is a Class object, returns it directly.
     * Otherwise, returns the class of the parameter value.
     * If the parameter is null, returns Object.class.
     *
     * @param index the parameter index
     * @return the class type of the parameter
     */
    @Override
    public Class getParameterType(int index) {
        Object value = parameterValues[index];

        if (value != null) {
            Class type = (Class)((value.getClass() == java.lang.Class.class) ?
                    value :
                    value.getClass());
            return type;
        }

        return Object.class;
    }
}
