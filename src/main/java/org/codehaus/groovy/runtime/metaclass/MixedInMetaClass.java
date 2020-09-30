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

public class MixedInMetaClass extends OwnedMetaClass {
    private final WeakReference owner;

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