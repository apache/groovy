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
import groovy.lang.MetaProperty;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassHelper;

/**
 * This class represents a property with multiple setters. Unlike a MetaBeanProperty you cannot get the setter
 * in this case. Instead invocation is done through the meta class of the property recevier.
 */
public class MultipleSetterProperty extends MetaProperty {
    private MetaMethod getter;
    private CachedField field;
    private final String setterName;

    public MultipleSetterProperty(String name) {
        super(name, Object.class);
        this.setterName = MetaProperty.getSetterName(name);
    }

    @Override
    public Object getProperty(Object object) {
        MetaMethod getter = getGetter();
        if (getter == null) {
            if (field != null) return field.getProperty(object);
            throw new GroovyRuntimeException("Cannot read write-only property: " + name);
        }
        return getter.invoke(object, MetaClassHelper.EMPTY_ARRAY);
    }

    @Override
    public void setProperty(Object object, Object newValue) {
        InvokerHelper.getMetaClass(object).invokeMethod(object, setterName, new Object[]{newValue});
    }

    public void setField(CachedField f) {
        this.field = f;
    }

    public CachedField getField() {
        return field;
    }

    public void setGetter(MetaMethod getter) {
        this.getter = getter;
    }

    public MetaMethod getGetter() {
        return getter;
    }

    public MetaProperty createStaticVersion() {
        boolean mf = field==null || field.isStatic();
        boolean mg = getter==null || getter.isStatic();
        if (mf && mg) return this;
        if (mg) {
            MultipleSetterProperty newMsp = new MultipleSetterProperty(name);
            newMsp.setGetter(getter);
            return newMsp;
        } else if (mf) {
            MultipleSetterProperty newMsp = new MultipleSetterProperty(name);
            newMsp.setField(field);
            return newMsp;
        }
        return null;
    }
}
