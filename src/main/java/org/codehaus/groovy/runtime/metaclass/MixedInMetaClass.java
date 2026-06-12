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

import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.lang.ref.WeakReference;

/**
 * A delegating MetaClass that allows mixing in a mixin object.
 * This class delegates method invocation to an owner object's MetaClass
 * while maintaining a weak reference to the owner to prevent memory leaks.
 * <p>
 * This implementation is for internal use by the Groovy runtime's mixin feature.
 */
public class MixedInMetaClass extends OwnedMetaClass {
    /**
     * A weak reference to the owner object
     */
    private final WeakReference owner;

    /**
     * Constructs a new MixedInMetaClass.
     *
     * @param instance the instance that will have the mixin applied
     * @param owner the owner object that provides the mixin functionality
     */
    public MixedInMetaClass(Object instance, Object owner) {
        super(GroovySystem.getMetaClassRegistry().getMetaClass(instance.getClass()));
        this.owner = new WeakReference(owner);
        MetaClassHelper.doSetMetaClass(instance, this);
    }

    @Override
    protected Object getOwner() {
        return this.owner.get();
    }

    @Override
    protected MetaClass getOwnerMetaClass(Object owner) {
        return InvokerHelper.getMetaClass(owner);
    }

    @Override
    public Object invokeMethod(Class sender, Object receiver, String methodName, Object[] arguments, boolean isCallToSuper, boolean fromInsideClass) {
        if (isCallToSuper) {
            return delegate.invokeMethod(sender, receiver, methodName, arguments, true, fromInsideClass);
        }
        return super.invokeMethod(sender, receiver, methodName, arguments, false, fromInsideClass);
    }
}