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
import org.codehaus.groovy.reflection.MixinInMetaClass;

/**
 * MetaMethod for mixed in classes.
 * This method delegates to the underlying method on the mixin instance.
 */
public class MixinInstanceMetaMethod extends MetaMethod{
    /**
     * The underlying metamethod from the mixin
     */
    private final MetaMethod method;
    /**
     * The metadata about the mixin in the metaclass
     */
    private final MixinInMetaClass mixinInMetaClass;

    /**
     * Constructs a new MixinInstanceMetaMethod.
     *
     * @param method the underlying metamethod from the mixin
     * @param mixinInMetaClass the mixin metadata
     */
    public MixinInstanceMetaMethod(MetaMethod method, MixinInMetaClass mixinInMetaClass) {
        this.method = method;
        this.mixinInMetaClass = mixinInMetaClass;
    }

    /**
     * Returns the modifiers of the underlying metamethod.
     *
     * @return the method modifiers
     */
    @Override
    public int getModifiers() {
        return method.getModifiers();
    }

    /**
     * Returns the name of the underlying metamethod.
     *
     * @return the method name
     */
    @Override
    public String getName() {
        return method.getName();
    }

    /**
     * Returns the return type of the underlying metamethod.
     *
     * @return the return type
     */
    @Override
    public Class getReturnType() {
        return method.getReturnType();
    }

    /**
     * Returns the class that declares this mixin method.
     *
     * @return the declaring class (from the mixin)
     */
    @Override
    public CachedClass getDeclaringClass() {
        return mixinInMetaClass.getInstanceClass();
    }

    /**
     * Invokes the underlying method on the mixin instance associated with the given object.
     *
     * @param object the object to invoke the method on
     * @param arguments the method arguments
     * @return the method return value
     */
    @Override
    public Object invoke(Object object, Object[] arguments) {
        // make sure parameterTypes gets set
        method.getParameterTypes();
        return method.invoke(mixinInMetaClass.getMixinInstance(object), method.correctArguments(arguments));
    }

    @Override
    protected Class[] getPT() {
        return method.getNativeParameterTypes();
    }
}
