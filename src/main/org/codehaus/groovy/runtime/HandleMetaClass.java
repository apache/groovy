/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import groovy.lang.*;

import java.lang.reflect.Method;

public class HandleMetaClass extends DelegatingMetaClass {
    private Object object;
    private static MetaClass myMetaClass;
    private static final Object NONE = new Object();

    public HandleMetaClass(MetaClass mc) {
        this(mc, null);
    }

    public HandleMetaClass(MetaClass mc, Object obj) {
        super(mc);
        if (obj != null) {
            if (InvokerHelper.getMetaClass(obj.getClass()) == mc || !(mc instanceof ExpandoMetaClass))
              object = obj; // object has default meta class, so we need to replace it on demand
            else
              object = NONE; // object already has per instance meta class
        }

        if (myMetaClass == null)
          myMetaClass = InvokerHelper.getMetaClass(getClass());
    }

    public void initialize() {
        replaceDelegate();
        delegate.initialize();
    }

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
              if (metaClass instanceof ExpandoMetaClass) {
                  ExpandoMetaClass emc = (ExpandoMetaClass) metaClass;
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

    public Object invokeMethod(String name, Object args) {
        return replaceDelegate().invokeMethod(name, args);
    }

    // this method mimics EMC behavior
    public Object getProperty(String property) {
        if(ExpandoMetaClass.isValidExpandoProperty(property)) {
            if(property.equals(ExpandoMetaClass.STATIC_QUALIFIER) ||
               property.equals(ExpandoMetaClass.CONSTRUCTOR) ||
               myMetaClass.hasProperty(this, property) == null) {
                  return replaceDelegate().getProperty(property);
            }
        }
        return myMetaClass.getProperty(this, property);
    }

    public void setProperty(String property, Object newValue) {
        replaceDelegate().setProperty(property, newValue);
    }

    public void addNewInstanceMethod(Method method) {
        throw new UnsupportedOperationException();
    }

    public void addNewStaticMethod(Method method) {
        throw new UnsupportedOperationException();
    }

    public void addMetaMethod(MetaMethod metaMethod) {
        throw new UnsupportedOperationException();
    }

    public void addMetaBeanProperty(MetaBeanProperty metaBeanProperty) {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        return super.equals(obj) || getAdaptee().equals(obj) || (obj instanceof HandleMetaClass && equals(((HandleMetaClass)obj).getAdaptee()));
    }
}
