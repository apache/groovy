/*
 * Copyright 2003-2007 the original author or authors.
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

package groovy.lang;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Represents a property on a bean which may have a getter and/or a setter
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MetaFieldProperty extends MetaProperty {

    private Field field;

    public MetaFieldProperty(Field field) {
        super(field.getName(), field.getType());
        this.field = field;
    }

    /**
     * @return the property of the given object
     * @throws Exception if the property could not be evaluated
     */
    public Object getProperty(final Object object) {
        try {
            Object value = (Object) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IllegalAccessException {   
                    field.setAccessible(true);
                    return field.get(object);
                }
            });
            return value;
        } catch (PrivilegedActionException pe) {
            throw new GroovyRuntimeException("Cannot get the property '" + name + "'.", pe.getException());
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
        final Object goalValue = DefaultTypeTransformation.castToType(newValue, field.getType());
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IllegalAccessException, GroovyRuntimeException {
                    field.setAccessible(true);
                    field.set(object, goalValue);
                    return null;
                }
            });
        } catch (PrivilegedActionException ex) {
            throw new GroovyRuntimeException("Cannot set the property '" + name + "'.", ex.getException());
        }
    }

    private String toName(Class c) {
        String s = c.toString();
        if (s.startsWith("class ") && s.length() > 6)
            return s.substring(6);
        else
            return s;
    }
    
    public int getModifiers() {
        return field.getModifiers();
    }
    
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }
}
