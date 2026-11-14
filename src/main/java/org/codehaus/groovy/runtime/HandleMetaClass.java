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

import groovy.lang.DelegatingMetaClass;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.GroovyObject;
import groovy.lang.MetaBeanProperty;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;

import java.lang.reflect.Method;

/**
 * A delegating meta-class that can replace the meta-class of one instance on demand.
 */
public class HandleMetaClass extends DelegatingMetaClass {
    private Object object;
    private static final Object NONE = new Object();

    /**
     * Delegates to {@code mc} with no instance attached.
     * {@link #replaceDelegate()} then installs an {@link ExpandoMetaClass} on the class.
     * Equivalent to {@link #HandleMetaClass(MetaClass, Object) HandleMetaClass(mc, null)}.
     *
     * @param mc the meta class to delegate to
     */
    public HandleMetaClass(MetaClass mc) {
        this(mc, null);
    }

    /**
     * Delegates to {@code mc} and records whether {@code obj} still needs a per-instance
     * meta class. A null {@code obj} leaves replacement to the class, as
     * {@link #HandleMetaClass(MetaClass)} does. A non-null {@code obj} is remembered
     * when the meta class of {@code obj}'s class is {@code mc}, or when {@code mc} is
     * not an {@link ExpandoMetaClass}. Otherwise the instance is treated as already
     * having its own meta class and {@link #replaceDelegate()} leaves it alone.
     *
     * @param mc the meta class to delegate to
     * @param obj the instance whose meta class may be replaced, or {@code null} for the class
     */
    public HandleMetaClass(MetaClass mc, Object obj) {
        super(mc);
        if (obj != null) {
            if (InvokerHelper.getMetaClass(obj.getClass()) == mc || !(mc instanceof ExpandoMetaClass))
              object = obj; // object has default metaclass, so we need to replace it on demand
            else
              object = NONE; // object already has per instance metaclass
        }
    }

    @Override
    public void initialize() {
        replaceDelegate();
        delegate.initialize();
    }

    /**
     * Ensures the delegate is an {@link ExpandoMetaClass} when a replacement is still
     * pending, then returns that delegate.
     * With no instance, a delegate that is not already an expando meta class is replaced
     * by {@code new ExpandoMetaClass(theClass, true, true)}, initialized, and registered
     * on the class; an existing expando delegate is registered again.
     * With a pending instance, a new {@code ExpandoMetaClass(theClass, false, true)} is
     * created, expando methods are copied when the previous delegate is an
     * {@link ExpandoMetaClass}, and the new meta class is set on that instance.
     * After an instance replacement, further calls do not replace again.
     * The delegate is cast to {@link GroovyObject}.
     *
     * @return the delegate after any replacement
     */
    public GroovyObject replaceDelegate() {
        if (object == null) {
            if (!(delegate instanceof ExpandoMetaClass)) {
              delegate = new ExpandoMetaClass(delegate.getTheClass(), true, true);
              delegate.initialize();
            }
            DefaultGroovyMethods.setMetaClass(delegate.getTheClass(), delegate);
        }
        else {
          if (object != NONE) {
              final MetaClass metaClass = delegate;
              delegate = new ExpandoMetaClass(delegate.getTheClass(), false, true);
              if (metaClass instanceof ExpandoMetaClass emc) {
                  for (MetaMethod method : emc.getExpandoMethods())
                    ((ExpandoMetaClass)delegate).registerInstanceMethod(method);
              }
              delegate.initialize();
              MetaClassHelper.doSetMetaClass(object, delegate);
              object = NONE;
          }
        }
        return (GroovyObject)delegate;
    }

    @Override
    public Object invokeMethod(String name, Object args) {
        return replaceDelegate().invokeMethod(name, args);
    }

    // this method mimics EMC behavior
    @Override
    public Object getProperty(String property) {
        if(ExpandoMetaClass.isValidExpandoProperty(property)) {
            if(property.equals(ExpandoMetaClass.STATIC_QUALIFIER) ||
               property.equals(ExpandoMetaClass.CONSTRUCTOR) ||
               Holder.META_CLASS.hasProperty(this, property) == null) {
                  return replaceDelegate().getProperty(property);
            }
        }
        return Holder.META_CLASS.getProperty(this, property);
    }

    @Override
    public void setProperty(String property, Object newValue) {
        replaceDelegate().setProperty(property, newValue);
    }

    @Override
    public void addNewInstanceMethod(Method method) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNewStaticMethod(Method method) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMetaMethod(MetaMethod metaMethod) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addMetaBeanProperty(MetaBeanProperty metaBeanProperty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || getAdaptee().equals(obj) || (obj instanceof HandleMetaClass && equals(((HandleMetaClass)obj).getAdaptee()));
    }

    // Lazily initialize the single instance of the HandleMetaClass metaClass
    private static class Holder {
        static final MetaClass META_CLASS = InvokerHelper.getMetaClass(HandleMetaClass.class);
    }
}
