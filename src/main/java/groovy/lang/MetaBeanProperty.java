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
package groovy.lang;

import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.lang.reflect.Modifier;

/**
 * Represents a property on a bean which may have a getter and/or a setter
 */
public class MetaBeanProperty extends MetaProperty {

    private MetaMethod getter;
    private MetaMethod setter;
    private CachedField field;

    public MetaBeanProperty(final String name, final Class type, final MetaMethod getter, final MetaMethod setter) {
        super(name, type);
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Gets the visibility modifiers of the property as defined by the getter, setter and field.
     */
    @Override
    public int getModifiers() {
        int modifiers;
        MetaMethod getter = getGetter();
        MetaMethod setter = getSetter();
        final int staticAndVisibility = 0xF;
        if (getter == null) {
            modifiers = setter.getModifiers() & staticAndVisibility;
        } else if (setter == null) {
            modifiers = getter.getModifiers() & staticAndVisibility;
            CachedField field = getField(); // GROOVY-11562: final modifier
            if (field == null || field.isFinal()) modifiers |= Modifier.FINAL;
        } else {
            modifiers = (getter.getModifiers() & staticAndVisibility) | (setter.getModifiers() & staticAndVisibility);
            if (Modifier.isPublic   (modifiers)) modifiers &= ~(Modifier.PROTECTED | Modifier.PRIVATE);
            if (Modifier.isProtected(modifiers)) modifiers &= ~Modifier.PRIVATE;
        }
        return modifiers;
    }

    /**
     * Gets the property of the given object.
     *
     * @param object which to be got
     * @return the property of the given object
     * @throws RuntimeException if the property could not be evaluated
     */
    @Override
    public Object getProperty(final Object object) {
        MetaMethod getter = getGetter();
        if (getter == null) {
            if (getField() != null) {
                return getField().getProperty(object);
            }
            //TODO: create a WriteOnlyException class?
            throw new GroovyRuntimeException("Cannot read write-only property: " + name);
        }
        return getter.invoke(object, MetaClassHelper.EMPTY_ARRAY);
    }

    /**
     * Sets the property on the given object to the new value.
     *
     * @param object   on which to set the property
     * @param newValue the new value of the property
     * @throws RuntimeException if the property could not be set
     */
    @Override
    public void setProperty(final Object object, Object newValue) {
        MetaMethod setter = getSetter();
        if (setter == null) {
            CachedField field = getField();
            if (field != null && !field.isFinal()) {
                field.setProperty(object, newValue);
                return;
            }
            throw new GroovyRuntimeException("Cannot set read-only property: " + name);
        }
        newValue = DefaultTypeTransformation.castToType(newValue, getType());
        setter.invoke(object, new Object[]{newValue});
    }

    //--------------------------------------------------------------------------

    /**
     * Gets the field of this property.
     */
    public CachedField getField() {
        return field;
    }

    /**
     * Gets the getter method of this property.
     */
    public MetaMethod getGetter() {
        return getter;
    }

    /**
     * Gets the setter method of this property.
     */
    public MetaMethod getSetter() {
        return setter;
    }

    /**
     * Sets the field of this property.
     */
    public void setField(final CachedField field) {
        this.field = field;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for get*() methods.
     */
    void setGetter(final MetaMethod getter) {
        this.getter = getter;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for set*() methods.
     */
    void setSetter(final MetaMethod setter) {
        this.setter = setter;
    }
}
