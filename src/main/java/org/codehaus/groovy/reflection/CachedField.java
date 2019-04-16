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
package org.codehaus.groovy.reflection;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaProperty;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.codehaus.groovy.reflection.ReflectionUtils.makeAccessibleInPrivilegedAction;

public class CachedField extends MetaProperty {
    private final Field cachedField;

    public CachedField(Field field) {
        super (field.getName(), field.getType());
        this.cachedField = field;
    }

    public boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public int getModifiers() {
        return cachedField.getModifiers();
    }

    /**
     * @return the property of the given object
     * @throws RuntimeException if the property could not be evaluated
     */
    public Object getProperty(final Object object) {
        makeAccessibleIfNecessary();
        AccessPermissionChecker.checkAccessPermission(cachedField);
        try {
            return cachedField.get(object);
        } catch (IllegalAccessException e) {
            throw new GroovyRuntimeException("Cannot get the property '" + name + "'.", e);
        }
    }

    /**
     * Sets the property on the given object to the new value
     *
     * @param object on which to set the property
     * @param newValue the new value of the property
     * @throws RuntimeException if the property could not be set
     */
    public void setProperty(final Object object, Object newValue) {
        makeAccessibleIfNecessary();
        AccessPermissionChecker.checkAccessPermission(cachedField);
        final Object goalValue = DefaultTypeTransformation.castToType(newValue, cachedField.getType());

        if (isFinal()) {
            throw new GroovyRuntimeException("Cannot set the property '" + name + "' because the backing field is final.");
        }
        try {
            cachedField.set(object, goalValue);
        } catch (IllegalAccessException ex) {
            throw new GroovyRuntimeException("Cannot set the property '" + name + "'.", ex);
        }
    }

    public Class getDeclaringClass() {
        return cachedField.getDeclaringClass();
    }

    public Field getCachedField() {
        makeAccessibleIfNecessary();
        AccessPermissionChecker.checkAccessPermission(cachedField);
        return cachedField;
    }

    private boolean makeAccessibleDone = false;
    private void makeAccessibleIfNecessary() {
        if (!makeAccessibleDone) {
            makeAccessibleInPrivilegedAction(cachedField);
            makeAccessibleDone = true;
        }
    }
}
