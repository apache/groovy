/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.runtime.typehandling;

import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;

public class DefaultTypeTransformation {
    
    protected static final Object[] EMPTY_ARGUMENTS = {};
    protected static final BigInteger ONE_NEG = new BigInteger("-1");
    
    //  --------------------------------------------------------
    //                  unboxing methods
    //  --------------------------------------------------------       
    
    public static byte byteUnbox(Object value) {
        Number n = castToNumber(value);
        return n.byteValue();
    }

    public static char charUnbox(Object value) {
        return castToChar(value);
    }

    public static short shortUnbox(Object value) {
        Number n = castToNumber(value);
        return n.shortValue();
    }

    public static int intUnbox(Object value) {
        Number n = castToNumber(value);
        return n.intValue();
    }

    public static boolean booleanUnbox(Object value) {
        return castToBoolean(value);
    }

    public static long longUnbox(Object value) {
        Number n = castToNumber(value);
        return n.longValue();
    }

    public static float floatUnbox(Object value) {
        Number n = castToNumber(value);
        return n.floatValue();
    }

    public static double doubleUnbox(Object value) {
        Number n = castToNumber(value);
        return n.doubleValue();
    } 

    //  --------------------------------------------------------
    //                  boxing methods
    //  --------------------------------------------------------       
    
    public static Object box(boolean value) {
        return value ? Boolean.TRUE : Boolean.FALSE;
    }

    public static Object box(byte value) {
        return new Byte(value);
    }

    public static Object box(char value) {
        return new Character(value);
    }

    public static Object box(short value) {
        return new Short(value);
    }

    public static Object box(int value) {
        return Integer.valueOf(value);
    }

    public static Object box(long value) {
        return new Long(value);
    }

    public static Object box(float value) {
        return new Float(value);
    }

    public static Object box(double value) {
        return new Double(value);
    }
    
    public static Number castToNumber(Object object) {
        if (object instanceof Number)
            return (Number) object;
        if (object instanceof Character) {
            return Integer.valueOf(((Character) object).charValue());
        }
        if (object instanceof String) {
            String c = (String) object;
            if (c.length() == 1) {
                return Integer.valueOf(c.charAt(0));
            }
            else {
                throw new GroovyCastException(c,Integer.class);
            }
        }
        throw new GroovyCastException(object,Number.class);
    }
    
    public static boolean castToBoolean(Object object) {
    	if (object == null) {
    		return false;
    	}
    	if (object instanceof Boolean) {
            Boolean booleanValue = (Boolean) object;
            return booleanValue.booleanValue();
        }
        if (object instanceof Matcher) {
            Matcher matcher = (Matcher) object;
            RegexSupport.setLastMatcher(matcher);
            return matcher.find();
        }
        if (object instanceof Collection) {
            Collection collection = (Collection) object;
            return !collection.isEmpty();
        }
        if (object instanceof Map) {
            Map map = (Map) object;
            return !map.isEmpty();
        }
        if (object instanceof Iterator) {
            Iterator iterator = (Iterator) object;
            return iterator.hasNext();
        }
        if (object instanceof Enumeration) {
            Enumeration e = (Enumeration) object;
            return e.hasMoreElements();
        }
        if (object instanceof CharSequence) {
        	CharSequence string =  (CharSequence) object;
            return string.length() > 0;
        } 
        if (object instanceof Object[]) {
        	Object[] array =  (Object[]) object;
            return array.length > 0;
        } 
        if (object instanceof Character) {
            Character c = (Character) object;
            return c.charValue() != 0;
        }
        if (object instanceof Number) {
            Number n = (Number) object;
            return n.doubleValue() != 0;
        }
        return true;
    }
    
    public static char castToChar(Object object) {
        if (object instanceof Character) {
            return ((Character) object).charValue();            
        } else if (object instanceof Number) {
            Number value = (Number) object;
            return (char) value.intValue();
        } else {
            String text = object.toString();
            if (text.length() == 1) {
                return text.charAt(0);
            }
            else {
                throw new GroovyCastException(text,char.class);
            }
        }
    }
    
    public static Object castToType(Object object, Class type) {
        if (object == null) {
            return null;
        }

        if (type == Object.class)
          return object;

        final Class aClass = object.getClass();
        if (type == aClass) return object;
        // TODO we should move these methods to groovy method, like g$asType() so that
        // we can use operator overloading to customize on a per-type basis
        if (ReflectionCache.isArray(type)) {
            return asArray(object, type);

        }
        if (ReflectionCache.isAssignableFrom(type, aClass)) {
            return object;
        }
        if (Collection.class.isAssignableFrom(type)) {
            int modifiers = type.getModifiers();
            Collection answer;
            if (object instanceof Collection && type.isAssignableFrom(HashSet.class) &&
                    (type == HashSet.class || Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))) {
                return new HashSet((Collection)object);
            }
            if (aClass.isArray()) {
                if (type.isAssignableFrom(ArrayList.class) && (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))) {
                    answer = new ArrayList();
                } else {
                    // let's call the collections constructor
                    // passing in the list wrapper
                    try {
                        answer = (Collection) type.newInstance();
                    }
                    catch (Exception e) {
                        throw new GroovyCastException("Could not instantiate instance of: " + type.getName() + ". Reason: " + e);
                    }
                }

                // we cannot just wrap in a List as we support primitive type arrays
                int length = Array.getLength(object);
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(object, i);
                    answer.add(element);
                }
                return answer;
            }
        }
        if (type == String.class) {
            return object.toString();
        } else if (type == Character.class) {
            return box(castToChar(object));
        } else if (type == Boolean.class) {
            return box(castToBoolean(object));
        } else if (type == Class.class) {
            return castToClass(object);
        } else if (Number.class.isAssignableFrom(type)) {
            Number n = castToNumber(object);
            if (type == Byte.class) {
                return new Byte(n.byteValue());
            } else if (type == Character.class) {
                return new Character((char) n.intValue());
            } else if (type == Short.class) {
                return new Short(n.shortValue());
            } else if (type == Integer.class) {
                return Integer.valueOf(n.intValue());
            } else if (type == Long.class) {
                return new Long(n.longValue());
            } else if (type == Float.class) {
                return new Float(n.floatValue());
            } else if (type == Double.class) {
                Double answer = new Double(n.doubleValue());
                //throw a runtime exception if conversion would be out-of-range for the type.
                if (!(n instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                        || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                    throw new GroovyRuntimeException("Automatic coercion of " + n.getClass().getName()
                            + " value " + n + " to double failed.  Value is out of range.");
                }
                return answer;
            } else if (type == BigDecimal.class) {
                return new BigDecimal(n.toString());
            } else if (type == BigInteger.class) {
                if (object instanceof Float || object instanceof Double) {
                    BigDecimal bd = new BigDecimal(n.doubleValue());
                    return bd.toBigInteger();
                } else if (object instanceof BigDecimal) {
                    return ((BigDecimal) object).toBigInteger();
                } else {
                    return new BigInteger(n.toString());
                }
            }
        } else if (type.isPrimitive()) {
            if (type == boolean.class) {
               return box(booleanUnbox(object)); 
            } else if (type == byte.class) {
                return box(byteUnbox(object));
            } else if (type == char.class) {
                return box(charUnbox(object));
            } else if (type == short.class) {
                return box(shortUnbox(object));
            } else if (type == int.class) {
                return box(intUnbox(object));
            } else if (type == long.class) {
                return box(longUnbox(object));
            } else if (type == float.class) {
                return box(floatUnbox(object));
            } else if (type == double.class) {
                Double answer = new Double(doubleUnbox(object));
                //throw a runtime exception if conversion would be out-of-range for the type.
                if (!(object instanceof Double) && (answer.doubleValue() == Double.NEGATIVE_INFINITY
                        || answer.doubleValue() == Double.POSITIVE_INFINITY)) {
                    throw new GroovyRuntimeException("Automatic coercion of " + aClass.getName()
                            + " value " + object + " to double failed.  Value is out of range.");
                }
                return answer;
            }
        }
        Object[] args = null;
        if (object instanceof Collection) {
            // let's try invoke the constructor with the list as arguments
            // such as for creating a Dimension, Point, Color etc.
            Collection collection = (Collection) object;
            args = collection.toArray();
        } else if (object instanceof Object[]) {
            args = (Object[]) object;
        } else if (object instanceof Map) {
            // emulate named params constructor
            args = new Object[1];
            args[0] = object;
        }
        if (args != null) {
            try {
                return InvokerHelper.invokeConstructorOf(type, args);
            } catch (InvokerInvocationException iie){
                throw iie;
            } catch (Exception e) {
                // let's ignore exception and return the original object
                // as the caller has more context to be able to throw a more
                // meaningful exception
            }
        }
        throw new GroovyCastException(object,type);
    }

    private static Class castToClass(Object object) {
        try {
            return Class.forName (object.toString());
        } catch (Exception e) {
            throw new GroovyCastException(object,Class.class);
        }
    }

    public static Object asArray(Object object, Class type) {
        if (type.isAssignableFrom(object.getClass())) {
            return object;
        }
        Collection list = asCollection(object);
        int size = list.size();
        Class elementType = type.getComponentType();
        Object array = Array.newInstance(elementType, size);
        int idx = 0;

        if (boolean.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setBoolean(array, idx, booleanUnbox(element));
            }
        }
        else if (byte.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setByte(array, idx, byteUnbox(element));
            }
        }
        else if (char.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setChar(array, idx, charUnbox(element));
            }
        }
        else if (double.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setDouble(array, idx, doubleUnbox(element));
            }
        }
        else if (float.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setFloat(array, idx, floatUnbox(element));
            }
        }
        else if (int.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setInt(array, idx, intUnbox(element));
            }
        }
        else if (long.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setLong(array, idx, longUnbox(element));
            }
        }
        else if (short.class.equals(elementType)) {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Array.setShort(array, idx, shortUnbox(element));
            }
        }
        else {
            for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
                Object element = iter.next();
                Object coercedElement = castToType(element, elementType);
                Array.set(array, idx, coercedElement);
            }
        }
        return array;
    }
    
    public static Collection asCollection(Object value) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }
        else if (value instanceof Collection) {
            return (Collection) value;
        }
        else if (value instanceof Map) {
            Map map = (Map) value;
            return map.entrySet();
        }
        else if (value.getClass().isArray()) {
            return arrayAsCollection(value);
        }
        else if (value instanceof MethodClosure) {
            MethodClosure method = (MethodClosure) value;
            IteratorClosureAdapter adapter = new IteratorClosureAdapter(method.getDelegate());
            method.call(adapter);
            return adapter.asList();
        }
        else if (value instanceof String) {
            return DefaultGroovyMethods.toList((String) value);
        }
        else if (value instanceof GString) {
            return DefaultGroovyMethods.toList(value.toString());
        }
        else if (value instanceof File) {
            try {
                return DefaultGroovyMethods.readLines((File) value);
            }
            catch (IOException e) {
                throw new GroovyRuntimeException("Error reading file: " + value, e);
            }
        }
        else if (isEnumSubclass(value)) {
            Object[] values = (Object[])InvokerHelper.invokeMethod(value, "values", new Object[0]);
            return Arrays.asList(values);
        }
        else {
            // let's assume it's a collection of 1
            return Collections.singletonList(value);
        }
    }

    public static Collection arrayAsCollection(Object value) {
        if (value.getClass().getComponentType().isPrimitive()) {
            return primitiveArrayToList(value);
        }
        return Arrays.asList((Object[]) value);
    }

    /**
     * Determines whether the value object is a Class object representing a
     * subclass of java.lang.Enum. Uses class name check to avoid breaking on
     * pre-Java 5 JREs.
     *
     * @param value an object
     * @return true if the object is an Enum
     */
    public static boolean isEnumSubclass(Object value) {
        if (value instanceof Class) {
            Class superclass = ((Class)value).getSuperclass();
            while (superclass != null) {
                if (superclass.getName().equals("java.lang.Enum")) {
                    return true;
                }
                superclass = superclass.getSuperclass();
            }
        }

        return false;
    }
    
    /**
     * Allows conversion of arrays into a mutable List
     *
     * @param array an array
     * @return the array as a List
     */
    public static List primitiveArrayToList(Object array) {
        int size = Array.getLength(array);
        List list = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            Object item = Array.get(array, i);
            if (item != null && item.getClass().isArray() && item.getClass().getComponentType().isPrimitive()) {
                item = primitiveArrayToList(item);
            }
            list.add(item);
        }
        return list;
    }
    
    public static Object[] primitiveArrayBox(Object array) {
        int size = Array.getLength(array);
        Object[] ret = (Object[]) Array.newInstance(ReflectionCache.autoboxType(array.getClass().getComponentType()), size);
        for (int i = 0; i < size; i++) {
            ret[i]=Array.get(array, i);
        }
        return ret;
    }
    
    /**
     * Compares the two objects handling nulls gracefully and performing numeric type coercion if required
     */
    public static int compareTo(Object left, Object right) {
        return compareToWithEqualityCheck(left, right, false);
    }

    private static int compareToWithEqualityCheck(Object left, Object right, boolean equalityCheckOnly) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        else if (right == null) {
            return 1;
        }
        if (left instanceof Comparable) {
            if (left instanceof Number) {
                if (isValidCharacterString(right)) {
                    return DefaultGroovyMethods.compareTo((Number) left, (Character) box(castToChar(right)));
                }
                if (right instanceof Character || right instanceof Number) {
                    return DefaultGroovyMethods.compareTo((Number) left, castToNumber(right));
                }
            }
            else if (left instanceof Character) {
                if (isValidCharacterString(right)) {
                    return DefaultGroovyMethods.compareTo((Character)left,(Character)box(castToChar(right)));
                }
                if (right instanceof Number) {
                    return DefaultGroovyMethods.compareTo((Character)left,(Number)right);
                }
            }
            else if (right instanceof Number) {
                if (isValidCharacterString(left)) {
                    return DefaultGroovyMethods.compareTo((Character)box(castToChar(left)),(Number) right);
                }
            }
            else if (left instanceof String && right instanceof Character) {
                return ((String) left).compareTo(right.toString());
            }
            else if (left instanceof String && right instanceof GString) {
                return ((String) left).compareTo(right.toString());
            }
            if (!equalityCheckOnly || left.getClass().isAssignableFrom(right.getClass())
                    || right.getClass().isAssignableFrom(left.getClass())
                    || (left instanceof GString && right instanceof String)) {
                Comparable comparable = (Comparable) left;
                return comparable.compareTo(right);
            }
        }

        if (equalityCheckOnly) {
            return -1; // anything other than 0
        }
        throw new GroovyRuntimeException("Cannot compare " + left.getClass().getName() + " with value '" +
                left + "' and " + right.getClass().getName() + " with value '" + right + "'");
    }

    public static boolean compareEqual(Object left, Object right) {
        if (left == right) return true;
        if (left == null || right == null) return false;
        if (left instanceof Comparable) {
            return compareToWithEqualityCheck(left, right, true) == 0;
        }
        // handle arrays on both sides as special case for efficiency
        Class leftClass = left.getClass();
        Class rightClass = right.getClass();
        if (leftClass.isArray() && rightClass.isArray()) {
            return compareArrayEqual(left, right);
        }
        if (leftClass.isArray() && leftClass.getComponentType().isPrimitive()) {
            left = primitiveArrayToList(left);
        }
        if (rightClass.isArray() && rightClass.getComponentType().isPrimitive()) {
            right = primitiveArrayToList(right);
        }
        if (left instanceof Object[] && right instanceof List) {
            return DefaultGroovyMethods.equals((Object[]) left, (List) right);
        }
        if (left instanceof List && right instanceof Object[]) {
            return DefaultGroovyMethods.equals((List) left, (Object[]) right);
        }
        if (left instanceof List && right instanceof List) {
            return DefaultGroovyMethods.equals((List) left, (List) right);
        }
        return ((Boolean) InvokerHelper.invokeMethod(left, "equals", right)).booleanValue();
    }

    public static boolean compareArrayEqual(Object left, Object right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (Array.getLength(left) != Array.getLength(right)) {
            return false;
        }
        for (int i = 0; i < Array.getLength(left); i++) {
            Object l = Array.get(left, i);
            Object r = Array.get(right, i);
            if (!compareEqual(l, r)) return false;
        }
        return true;
    }

    /**
     * @return true if the given value is a valid character string (i.e. has length of 1)
     */
    private static boolean isValidCharacterString(Object value) {
        if (value instanceof String) {
            String s = (String) value;
            if (s.length() == 1) {
                return true;
            }
        }
        return false;
    }

    public static int[] convertToIntArray(Object a) {
        int[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[I")) {
            ans = (int[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new int[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = ((Number) ia[i]).intValue();
            }
        }
        return ans;
    }

    public static boolean[] convertToBooleanArray(Object a) {
        boolean[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[Z")) {
            ans = (boolean[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new boolean[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = ((Boolean) ia[i]).booleanValue();
            }
        }
        return ans;
    }

    public static byte[] convertToByteArray(Object a) {
        byte[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[B")) {
            ans = (byte[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new byte[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] != null) {
                    ans[i] = ((Number) ia[i]).byteValue();
                }
            }
        }
        return ans;
    }

    public static short[] convertToShortArray(Object a) {
        short[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[S")) {
            ans = (short[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new short[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Number) ia[i]).shortValue();
            }
        }
        return ans;
    }

    public static char[] convertToCharArray(Object a) {
        char[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[C")) {
            ans = (char[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new char[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = ((Character) ia[i]).charValue();
            }
        }
        return ans;
    }

    public static long[] convertToLongArray(Object a) {
        long[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[J")) {
            ans = (long[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new long[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = ((Number) ia[i]).longValue();
            }
        }
        return ans;
    }

    public static float[] convertToFloatArray(Object a) {
        float[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[F")) {
            ans = (float[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new float[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = ((Number) ia[i]).floatValue();
            }
        }
        return ans;
    }

    public static double[] convertToDoubleArray(Object a) {
        double[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[D")) {
            ans = (double[]) a;
        }
        else {
            Object[] ia = (Object[]) a;
            ans = new double[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = ((Number) ia[i]).doubleValue();
            }
        }
        return ans;
    }

    public static Object convertToPrimitiveArray(Object a, Class type) {
        if (type == Byte.TYPE) {
            return convertToByteArray(a);
        }
        if (type == Boolean.TYPE) {
            return convertToBooleanArray(a);
        }
        if (type == Short.TYPE) {
            return convertToShortArray(a);
        }
        if (type == Character.TYPE) {
            return convertToCharArray(a);
        }
        if (type == Integer.TYPE) {
            return convertToIntArray(a);
        }
        if (type == Long.TYPE) {
            return convertToLongArray(a);
        }
        if (type == Float.TYPE) {
            return convertToFloatArray(a);
        }
        if (type == Double.TYPE) {
            return convertToDoubleArray(a);
        }
        else {
            return a;
        }
    }

}
