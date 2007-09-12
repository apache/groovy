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
import sun.misc.Unsafe;

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
/*
        Class type = field.getType();

        if (!Modifier.isStatic(field.getModifiers())) {
            if (type == double.class)
              return new DoubleAccessor(field);
            else
            if (type == float.class)
              return new FloatAccessor(field);
            else
            if (type == int.class)
              return new IntegerAccessor(field);
            else
            if (type == long.class)
              return new LongAccessor(field);
            else
            if (type == char.class)
              return new CharAccessor(field);
            else
            if (type == short.class)
              return new ShortAccessor(field);
            else
            if (type == byte.class)
              return new ByteAccessor(field);
            else
            if (type == boolean.class)
              return new BooleanAccessor(field);
            else
            if (type == String.class)
              return new StringAccessor(field);
            else
              return new ObjectAccessor(field);
        }
        else {
              return new MetaFieldProperty(field);
        }
*/
        return new MetaFieldProperty(field);
    }

    static abstract class UnsafeAccessor extends MetaFieldProperty{
        static Unsafe theUnsafe;
        static {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);

                theUnsafe = (Unsafe)f.get(null);
            } catch (Exception ex) {
            }
        }

        long offset;

        UnsafeAccessor (Field field) {
            super (field);

            if (Modifier.isStatic(field.getModifiers()) )
              offset = theUnsafe.staticFieldOffset(field);
            else
              offset = theUnsafe.objectFieldOffset(field);
        }
    }

    static class DoubleAccessor extends UnsafeAccessor{
        DoubleAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return new Double(theUnsafe.getDouble(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Number)
                theUnsafe.putDouble(theObject,offset,((Number)value).doubleValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class FloatAccessor extends UnsafeAccessor{
        FloatAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return new Float(theUnsafe.getFloat(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Number)
                theUnsafe.putFloat(theObject,offset,((Number)value).floatValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class IntegerAccessor extends UnsafeAccessor{
        IntegerAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return new Integer(theUnsafe.getInt(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Number)
              theUnsafe.putInt(theObject,offset,((Number)value).intValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class ShortAccessor extends UnsafeAccessor{
        ShortAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return new Short(theUnsafe.getShort(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Number)
                theUnsafe.putShort(theObject,offset,((Number)value).shortValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class LongAccessor extends UnsafeAccessor{
        LongAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return new Long(theUnsafe.getLong(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Number)
                theUnsafe.putLong(theObject,offset,((Number)value).longValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class ByteAccessor extends UnsafeAccessor{
        ByteAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return new Byte(theUnsafe.getByte(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Number)
                theUnsafe.putByte(theObject,offset,((Number)value).byteValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class CharAccessor extends UnsafeAccessor{
        CharAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return new Character(theUnsafe.getChar(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Character)
                theUnsafe.putChar(theObject,offset,((Character)value).charValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class BooleanAccessor extends UnsafeAccessor{
        BooleanAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return Boolean.valueOf(theUnsafe.getBoolean(theObject,offset));
        }
        public void setProperty (Object theObject, Object value) {
            if (value instanceof Boolean)
                theUnsafe.putBoolean(theObject,offset,((Boolean)value).booleanValue());
            else
              super.setProperty(theObject, value);
        }
    }

    static class ObjectAccessor extends UnsafeAccessor{
        ObjectAccessor(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return theUnsafe.getObject(theObject,offset);
        }
        public void setProperty (Object theObject, Object value) {
            theUnsafe.putObject(theObject,offset,value);
        }
    }

    static class ObjectAccessorStatic extends UnsafeAccessor{
        ObjectAccessorStatic(Field field) {
            super(field);
        }

        public Object getProperty (Object theObject) {
            return theUnsafe.getObject(null,offset);
        }
        public void setProperty (Object theObject, Object value) {
            theUnsafe.putObject(null,offset,value);
        }
    }

    static class StringAccessor extends ObjectAccessor{
        StringAccessor(Field field) {
            super(field);
        }
        public void setProperty (Object theObject, Object value) {
            theUnsafe.putObject(theObject,offset,String.valueOf(value));
        }
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
