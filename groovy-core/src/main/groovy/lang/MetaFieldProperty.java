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
import java.security.PrivilegedAction;

/**
 * Represents a property on a bean which may have a getter and/or a setter
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class MetaFieldProperty extends MetaProperty {

    boolean alreadySetAccessible;
    private Field field;

    public static MetaFieldProperty create(Field field) {
        return new MetaFieldProperty(field);
    }

    private MetaFieldProperty(Field field) {
        super(field.getName(), field.getType());
        this.field = field;
    }

    /**
     * @return the property of the given object
     * @throws Exception if the property could not be evaluated
     */
    public Object getProperty(final Object object) {
        if ( !alreadySetAccessible ) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            alreadySetAccessible = true;
        }

        try {
            return field.get(object);
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
        final Object goalValue = DefaultTypeTransformation.castToType(newValue, field.getType());

        if ( !alreadySetAccessible ) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            alreadySetAccessible = true;
        }

        try {
            field.set(object, goalValue);
        } catch (IllegalAccessException ex) {
            throw new GroovyRuntimeException("Cannot set the property '" + name + "'.", ex);
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
