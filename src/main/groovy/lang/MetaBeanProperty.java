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


import java.math.BigDecimal;
import java.math.BigInteger;

import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a property on a bean which may have a getter and/or a setter
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Pilho Kim
 * @version $Revision$
 */
public class MetaBeanProperty extends MetaProperty {

    private MetaMethod getter;
    private MetaMethod setter;

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
     * @throws Exception if the property could not be evaluated
     */
    public Object getProperty(Object object) throws Exception {
        if (getter == null) {
            //@todo we probably need a WriteOnlyException class
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
    public void setProperty(Object object, Object newValue) {
        if (setter == null) {
            throw new GroovyRuntimeException("Cannot set read-only property: " + name);
        }

        try {
            // we'll convert a GString to String if needed
            if (newValue != null && getType() == String.class && !(newValue instanceof String)) {
                newValue = newValue.toString();
            }
            else {
                // Set property for primitive types
                newValue = coercePrimitiveValue(newValue, getType());
            }

            setter.invoke(object, new Object[] { newValue });
        }
        catch (IllegalArgumentException e) {    // exception for executing as scripts
            try {
                newValue = DefaultTypeTransformation.castToType(newValue, getType());
                setter.invoke(object, new Object[] { newValue });
            }
            catch (Exception ex) {
                throw new TypeMismatchException("The property '" + toName(object.getClass()) + "." + name
                        + "' can not refer to the value '"
                        + newValue + "' (type " + toName(newValue.getClass())
                        + "), because it is of the type " + toName(getType())
                        + ". The reason is from java.lang.IllegalArgumentException.");
            }
        }
        catch (ClassCastException e) {    // exception for executing as compiled classes
            try {
                newValue = DefaultTypeTransformation.castToType(newValue, getType());
                setter.invoke(object, new Object[]{newValue});
            }
            catch (Exception ex) {
                throw new TypeMismatchException("The property '" + toName(object.getClass()) + "." + name
                        + "' can not refer to the value '"
                        + newValue + "' (type " + toName(newValue.getClass())
                        + "), because it is of the type " + toName(getType())
                        + ". The reason is from java.lang.ClassCastException.");
            }
        }
        catch (Exception e) {
            throw new GroovyRuntimeException("Cannot set property: " + name +
                    " reason: " + e.getMessage(), e);
        }
    }

    /**
     * Coerce the object <code>src</code> to the target class.
     */
    protected static Object coercePrimitiveValue(Object src, Class target) {
        Object newValue = src;

        if (newValue instanceof BigDecimal) {
            if (target == java.math.BigInteger.class) {
                newValue = ((BigDecimal) newValue).unscaledValue();
            }
            else if (target == Double.class) {
                newValue = new Double(((BigDecimal) newValue).doubleValue());
            }
            else if (target == Float.class) {
                newValue = new Float(((BigDecimal) newValue).floatValue());
            }
            else if (target == Long.class) {
                newValue = new Long(((BigDecimal) newValue).longValue());
            }
            else if (target == Integer.class) {
                newValue = new Integer(((BigDecimal) newValue).intValue());
            }
            else if (target == Short.class) {
                newValue = new Short((short) ((BigDecimal) newValue).intValue());
            }
            else if (target == Byte.class) {
                newValue = new Byte((byte) ((BigDecimal) newValue).intValue());
            }
            else if (target == Character.class) {
                newValue = new Character((char) ((BigDecimal) newValue).intValue());
            }
        }
        else if (newValue instanceof BigInteger) {
            if (target == BigDecimal.class) {
                newValue = new BigDecimal((BigInteger) newValue);
            }
            else if (target == Double.class) {
                newValue = new Double(((java.math.BigInteger) newValue).doubleValue());
            }
            else if (target == Float.class) {
                newValue = new Float(((java.math.BigInteger) newValue).floatValue());
            }
            else if (target == Long.class) {
                newValue = new Long(((java.math.BigInteger) newValue).longValue());
            }
            else if (target == Integer.class) {
                newValue = new Integer(((java.math.BigInteger) newValue).intValue());
            }
            else if (target == Short.class) {
                newValue = new Short((short) ((java.math.BigInteger) newValue).intValue());
            }
            else if (target == Byte.class) {
                newValue = new Byte((byte) ((java.math.BigInteger) newValue).intValue());
            }
            else if (target == Character.class) {
                newValue = new Character((char) ((java.math.BigInteger) newValue).intValue());
            }
        }
        else if (newValue instanceof java.lang.Long) {
            if (target == Integer.class) {
                newValue = new Integer(((Long) newValue).intValue());
            }
            else if (target == Short.class) {
                newValue = new Short(((Long) newValue).shortValue());
            }
            else if (target == Byte.class) {
                newValue = new Byte(((Long) newValue).byteValue());
            }
            else if (target == Character.class) {
                newValue = new Character((char) ((Long) newValue).intValue());
            }
            else if (target == BigInteger.class) {
                newValue = new BigInteger("" + newValue);
            }
            else if (target == BigDecimal.class) {
                newValue = new BigDecimal("" + newValue);
            }
        }
        else if (newValue instanceof java.lang.Integer) {
            if (target == Double.class) {
                newValue = new Double(((Integer) newValue).intValue());
            }
            else if (target == Float.class) {
                newValue = new Float(((Integer) newValue).floatValue());
            }
            else if (target == Long.class) {
                newValue = new Long(((Integer) newValue).intValue());
            }
            else if (target == Short.class) {
                newValue = new Short(((Integer) newValue).shortValue());
            }
            else if (target == Byte.class) {
                newValue = new Byte(((Integer) newValue).byteValue());
            }
            else if (target == Character.class) {
                newValue = new Character((char) ((Integer) newValue).intValue());
            }
            else if (target == BigDecimal.class) {
                newValue = new BigDecimal("" + newValue);
            }
            else if (target == BigInteger.class) {
                newValue = new BigInteger("" + newValue);
            }
        }
        else if (newValue instanceof java.lang.Short) {
            if (target == Double.class) {
                newValue = new Double(((Short) newValue).shortValue());
            }
            else if (target == Float.class) {
                newValue = new Float(((Short) newValue).shortValue());
            }
            else if (target == Long.class) {
                newValue = new Long(((Short) newValue).shortValue());
            }
            else if (target == Integer.class) {
                newValue = new Integer(((Short) newValue).shortValue());
            }
            else if (target == Byte.class) {
                newValue = new Byte((byte) ((Short) newValue).shortValue());
            }
            else if (target == Character.class) {
                newValue = new Character((char) ((Short) newValue).shortValue());
            }
            else if (target == BigDecimal.class) {
                newValue = new BigDecimal("" + newValue);
            }
            else if (target == BigInteger.class) {
                newValue = new BigInteger("" + newValue);
            }
        }
        else if (newValue instanceof java.lang.Byte) {
            if (target == Double.class) {
                newValue = new Double(((Byte) newValue).byteValue());
            }
            else if (target == Float.class) {
                newValue = new Float(((Byte) newValue).byteValue());
            }
            else if (target == Long.class) {
                newValue = new Long(((Byte) newValue).byteValue());
            }
            else if (target == Integer.class) {
                newValue = new Integer(((Byte) newValue).byteValue());
            }
            else if (target == Short.class) {
                newValue = new Short(((Byte) newValue).byteValue());
            }
            else if (target == Character.class) {
                newValue = new Character((char) ((Byte) newValue).byteValue());
            }
            else if (target == BigDecimal.class) {
                newValue = new BigDecimal("" + newValue);
            }
            else if (target == BigInteger.class) {
                newValue = new BigInteger("" + newValue);
            }
        }
        else if (newValue instanceof java.lang.Character) {
            if (target == Double.class) {
                newValue = new Double(((int) ((Character) newValue).charValue() & 0xFFFF));
            }
            else if (target == Long.class) {
                newValue = new Long((long) ((Character) newValue).charValue());
            }
            else if (target == Integer.class) {
                newValue = new Integer((int) ((Character) newValue).charValue());
            }
            else if (target == Short.class) {
                newValue = new Short((short) ((Character) newValue).charValue());
            }
            else if (target == BigDecimal.class) {
                newValue = new BigDecimal("" + ((int) ((Character) newValue).charValue() & 0xFFFF));
            }
            else if (target == BigInteger.class) {
                newValue = new BigInteger("" + ((int) ((Character) newValue).charValue() & 0xFFFF));
            }
            else if (target == String.class) {
                newValue = new String("" + newValue);
            }
        }
        return newValue;
    }

    private String toName(Class c) {
        String s = c.toString();
        if (s.startsWith("class ") && s.length() > 6) {
            return s.substring(6);
        }
        else {
            return s;
        }
    }


    /**
     * Get the getter method.
     */
    public MetaMethod getGetter() {
        return getter;
    }

    /**
     * Get the setter method.
     */
    public MetaMethod getSetter() {
        return setter;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for get*() methods.
     */
    void setGetter(MetaMethod getter) {
        this.getter = getter;
    }

    /**
     * This is for MetaClass to patch up the object later when looking for set*() methods.
     */
    void setSetter(MetaMethod setter) {
        this.setter = setter;
    }
}
