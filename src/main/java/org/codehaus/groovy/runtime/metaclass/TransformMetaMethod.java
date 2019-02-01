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

import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;

/**
 * A MetaMethod implementation useful for implementing coercion based invocations
 */
public class TransformMetaMethod extends MetaMethod {
    
    private final MetaMethod metaMethod;

    public TransformMetaMethod(MetaMethod metaMethod) {
        this.metaMethod = metaMethod;
        setParametersTypes(metaMethod.getParameterTypes());
        nativeParamTypes = metaMethod.getNativeParameterTypes();
    }

    public int getModifiers() {
        return metaMethod.getModifiers();
    }

    public String getName() {
        return metaMethod.getName();
    }

    public Class getReturnType() {
        return metaMethod.getReturnType();
    }

    public CachedClass getDeclaringClass() {
        return metaMethod.getDeclaringClass();
    }

    public Object invoke(Object object, Object[] arguments) {
        return metaMethod.invoke(object, arguments);
    }
}
