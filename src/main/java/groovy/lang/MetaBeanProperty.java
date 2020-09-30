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

    /**
     * Sole constructor setting name, type (class), getter and setter.
     */
    public MetaBeanProperty(String name, Class type, MetaMethod getter, MetaMethod setter) {
        super(name, type);
        this.getter = getter;
        this.setter = setter;
    }

    /**
     * Get the property of the given object.
     *
     * @param object which to be got
     * @return the property of the given object
     * @throws RuntimeException if the property could not be evaluated
     */
    @Override
    public Object getProperty(Object object) {
        MetaMethod getter = getGetter();
        if (getter == null) {
            if (field != null) return field.getProperty(object);
            //TODO: create a WriteOnlyException class?
            throw new GroovyRuntimeException("Cannot read write-only property: " + name);
        }
        return getter.invoke(object, MetaClassHelper.EMPTY_ARRAY);
    }

    /**
     * Set the property on the given object to the new value.
     *
     * @param object   on which to set the property
     * @param newValue the new value of the property
     * @throws RuntimeException if the property could not be set
     */
    @Override
    public void setProperty(Object object, Object newValue) {
        MetaMethod setter = getSetter();
        if (setter == null) {
            if (field != null && !Modifier.isFinal(field.getModifiers())) {
                field.setProperty(object, newValue);
                return;
            }
            throw new GroovyRuntimeException("Cannot set read-only property: " + name);
        }
        newValue = DefaultTypeTransformation.castToType(newValue, getType());
        setter.invoke(object, new Object[]{newValue});
    }

    /**
     * Get the getter method.
     *
     * @return the getter method for this property.
     */
    public MetaMethod getGetter() {
        return getter;
    }

    /**
     * Get the setter method.
     *
     * @return the setter method for this property.
     */
    public MetaMethod getSetter() {
        return setter;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for get*() methods.
     *
     * @param getter The getter for this property
     */
    void setGetter(MetaMethod getter) {
        this.getter = getter;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for set*() methods.
     *
     * @param setter The setter for this property 
     */
    void setSetter(MetaMethod setter) {
        this.setter = setter;
    }

    /**
     * Gets the visibility modifiers for the property as defined by the getter and setter methods.
     *
     * @return the visibility modifier of the getter, the setter, or both depending on which exist
     */
    @Override
    public int getModifiers() {
        MetaMethod getter = getGetter();
        MetaMethod setter = getSetter();
        if (setter != null && getter == null) return setter.getModifiers();
        if (getter != null && setter == null) return getter.getModifiers();
        int modifiers = getter.getModifiers() | setter.getModifiers();
        int visibility = 0;
        if (Modifier.isPublic(modifiers)) visibility = Modifier.PUBLIC;
        if (Modifier.isProtected(modifiers)) visibility = Modifier.PROTECTED;
        if (Modifier.isPrivate(modifiers)) visibility = Modifier.PRIVATE;
        int states = getter.getModifiers() & setter.getModifiers();
        states &= ~(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);
        states |= visibility;
        return states;
    }

    /**
     * Sets the field of this property
     *
     * @param field
     */
    public void setField(CachedField field) {
        this.field = field;
    }

    /**
     * Gets the field of this property
     *
     * @return The field of this property
     */
    public CachedField getField() {
        return field;
    }
}
