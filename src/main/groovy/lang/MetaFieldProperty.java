/*
 * $Id$
 *
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *  1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *  2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  3. The name "groovy" must not be used to endorse or promote products
 * derived from this Software without prior written permission of The Codehaus.
 * For written permission, please contact info@codehaus.org.
 *  4. Products derived from this Software may not be called "groovy" nor may
 * "groovy" appear in their names without prior written permission of The
 * Codehaus. "groovy" is a registered trademark of The Codehaus.
 *  5. Due credit should be given to The Codehaus - http://groovy.codehaus.org/
 *
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *
 */
 
package groovy.lang;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.lang.reflect.Field;
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
    public Object getProperty(Object object) throws Exception {
        final Field field1 = field;
        final Object object1 = object;
        Object value = (Object) AccessController.doPrivileged(new PrivilegedExceptionAction() {
             public Object run() throws IllegalAccessException {   
                 field1.setAccessible(true);
                 return field1.get(object1);
             }
        });
        return value;
    }

    /**
     * Sets the property on the given object to the new value
     * 
     * @param object on which to set the property
     * @param newValue the new value of the property
     * @throws RuntimeException if the property could not be set
     */
    public void setProperty(Object object, Object newValue) {
        final Field field1 = field;
        final Object object1 = object;
        final Object newValue1 = newValue;
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws IllegalAccessException, TypeMismatchException, GroovyRuntimeException {   
                    try {
                        field1.set(object1, newValue1);
                        return newValue1;
                    }
                    catch (IllegalArgumentException e) {
                        Object newValue2 = DefaultTypeTransformation.castToType(newValue1, field1.getType());
                        field1.set(object1, newValue2);
                        return newValue2;
                    }
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
}
