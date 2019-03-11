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
package org.codehaus.groovy.runtime.typehandling;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.IteratorClosureAdapter;
import org.codehaus.groovy.runtime.MethodClosure;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Class providing various type conversions, coercions and boxing/unboxing operations.
 */
public class DefaultTypeTransformation {

    protected static final Object[] EMPTY_ARGUMENTS = {};
    protected static final BigInteger ONE_NEG = new BigInteger("-1");
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    //  --------------------------------------------------------
    //                  unboxing methods
    //  --------------------------------------------------------

    public static byte byteUnbox(Object value) {
        Number n = castToNumber(value, byte.class);
        return n.byteValue();
    }

    public static char charUnbox(Object value) {
        return ShortTypeHandling.castToChar(value);
    }

    public static short shortUnbox(Object value) {
        Number n = castToNumber(value, short.class);
        return n.shortValue();
    }

    public static int intUnbox(Object value) {
        Number n = castToNumber(value, int.class);
        return n.intValue();
    }

    public static boolean booleanUnbox(Object value) {
        return castToBoolean(value);
    }

    public static long longUnbox(Object value) {
        Number n = castToNumber(value, long.class);
        return n.longValue();
    }

    public static float floatUnbox(Object value) {
        Number n = castToNumber(value, float.class);
        return n.floatValue();
    }

    public static double doubleUnbox(Object value) {
        Number n = castToNumber(value, double.class);
        return n.doubleValue();
    }

    //  --------------------------------------------------------
    //                  boxing methods
    //  --------------------------------------------------------

    @Deprecated
    public static Object box(boolean value) {
        return value ? Boolean.TRUE : Boolean.FALSE;
    }

    @Deprecated
    public static Object box(byte value) {
        return value;
    }

    @Deprecated
    public static Object box(char value) {
        return value;
    }

    @Deprecated
    public static Object box(short value) {
        return value;
    }

    @Deprecated
    public static Object box(int value) {
        return value;
    }

    @Deprecated
    public static Object box(long value) {
        return value;
    }

    @Deprecated
    public static Object box(float value) {
        return value;
    }

    @Deprecated
    public static Object box(double value) {
        return value;
    }

    public static Number castToNumber(Object object) {
        // default to Number class in exception details, else use the specified Number subtype.
        return castToNumber(object, Number.class);
    }

    public static Number castToNumber(Object object, Class type) {
        if (object instanceof Number)
            return (Number) object;
        if (object instanceof Character) {
            return (int) (Character) object;
        }
        if (object instanceof GString) {
            String c = ((GString) object).toString();
            if (c.length() == 1) {
                return (int) c.charAt(0);
            } else {
                throw new GroovyCastException(c, type);
            }
        }
        if (object instanceof String) {
            String c = (String) object;
            if (c.length() == 1) {
                return (int) c.charAt(0);
            } else {
                throw new GroovyCastException(c, type);
            }
        }
        throw new GroovyCastException(object, type);
    }

    /**
     * Method used for coercing an object to a boolean value,
     * thanks to an <code>asBoolean()</code> method added on types.
     *
     * @param object to coerce to a boolean value
     * @return a boolean value
     */
    public static boolean castToBoolean(Object object) {
        // null is always false
        if (object == null) {
            return false;
        }

        // equality check is enough and faster than instanceof check, no need to check superclasses since Boolean is final
        if (object.getClass() == Boolean.class) {
            return (Boolean) object;
        }

        // if the object is not null and no Boolean, try to call an asBoolean() method on the object
        return (Boolean) InvokerHelper.invokeMethod(object, "asBoolean", InvokerHelper.EMPTY_ARGS);
    }

    @Deprecated
    public static char castToChar(Object object) {
        if (object instanceof Character) {
            return (Character) object;
        } else if (object instanceof Number) {
            Number value = (Number) object;
            return (char) value.intValue();
        } else {
            String text = object.toString();
            if (text.length() == 1) {
                return text.charAt(0);
            } else {
                throw new GroovyCastException(text, char.class);
            }
        }
    }

    public static Object castToType(Object object, Class type) {
        if (object == null) return null;
        if (type == Object.class) return object;

        final Class aClass = object.getClass();
        if (type == aClass) return object;
        if (type.isAssignableFrom(aClass)) return object;

        if (ReflectionCache.isArray(type)) return asArray(object, type);

        if (type.isEnum()) {
            return ShortTypeHandling.castToEnum(object, type);
        } else if (Collection.class.isAssignableFrom(type)) {
            return continueCastOnCollection(object, type);
        } else if (type == String.class) {
            return InvokerHelper.toString(object);
        } else if (type == Character.class) {
            return ShortTypeHandling.castToChar(object);
        } else if (type == Boolean.class) {
            return castToBoolean(object);
        } else if (type == Class.class) {
            return ShortTypeHandling.castToClass(object);
        } else if (type.isPrimitive()) {
            return castToPrimitive(object, type);
        }

        return continueCastOnNumber(object, type);
    }

    private static Object continueCastOnCollection(Object object, Class type) {
        int modifiers = type.getModifiers();
        Collection answer;
        if (object instanceof Collection && type.isAssignableFrom(LinkedHashSet.class) &&
                (type == LinkedHashSet.class || Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))) {
            return new LinkedHashSet((Collection) object);
        }
        if (object.getClass().isArray()) {
            if (type.isAssignableFrom(ArrayList.class) && (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))) {
                answer = new ArrayList();
            } else if (type.isAssignableFrom(LinkedHashSet.class) && (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers))) {
                answer = new LinkedHashSet();
            } else {
                // let's call the collections constructor
                // passing in the list wrapper
                try {
                    answer = (Collection) type.newInstance();
                } catch (Exception e) {
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

        return continueCastOnNumber(object, type);
    }

    private static Object continueCastOnNumber(Object object, Class type) {
        if (Number.class.isAssignableFrom(type)) {
            Number n = castToNumber(object, type);
            if (type == Byte.class) {
                return n.byteValue();
            }
            if (type == Character.class) {
                return (char) n.intValue();
            }
            if (type == Short.class) {
                return n.shortValue();
            }
            if (type == Integer.class) {
                return n.intValue();
            }
            if (type == Long.class) {
                return n.longValue();
            }
            if (type == Float.class) {
                return n.floatValue();
            }
            if (type == Double.class) {
                Double answer = n.doubleValue();
                //throw a runtime exception if conversion would be out-of-range for the type.
                if (!(n instanceof Double) && (answer == Double.NEGATIVE_INFINITY
                        || answer == Double.POSITIVE_INFINITY)) {
                    throw new GroovyRuntimeException("Automatic coercion of " + n.getClass().getName()
                            + " value " + n + " to double failed.  Value is out of range.");
                }
                return answer;
            }
            if (type == BigDecimal.class) {
                if (n instanceof Float || n instanceof Double) {
                    return new BigDecimal(n.doubleValue());
                }
                return new BigDecimal(n.toString());
            }
            if (type == BigInteger.class) {
                if (object instanceof Float || object instanceof Double) {
                    BigDecimal bd = new BigDecimal(n.doubleValue());
                    return bd.toBigInteger();
                }
                if (object instanceof BigDecimal) {
                    return ((BigDecimal) object).toBigInteger();
                }
                return new BigInteger(n.toString());
            }
        }

        return continueCastOnSAM(object, type);
    }

    private static Object castToPrimitive(Object object, Class type) {
        if (type == boolean.class) {
            return booleanUnbox(object);
        } else if (type == byte.class) {
            return byteUnbox(object);
        } else if (type == char.class) {
            return charUnbox(object);
        } else if (type == short.class) {
            return shortUnbox(object);
        } else if (type == int.class) {
            return intUnbox(object);
        } else if (type == long.class) {
            return longUnbox(object);
        } else if (type == float.class) {
            return floatUnbox(object);
        } else if (type == double.class) {
            Double answer = doubleUnbox(object);
            //throw a runtime exception if conversion would be out-of-range for the type.
            if (!(object instanceof Double) && (answer == Double.NEGATIVE_INFINITY
                    || answer == Double.POSITIVE_INFINITY)) {
                throw new GroovyRuntimeException("Automatic coercion of " + object.getClass().getName()
                        + " value " + object + " to double failed.  Value is out of range.");
            }
            return answer;
        } //nothing else possible
        throw new GroovyCastException(object, type);
    }

    private static Object continueCastOnSAM(Object object, Class type) {
        if (object instanceof Closure) {
            Method m = CachedSAMClass.getSAMMethod(type);
            if (m != null) {
                return CachedSAMClass.coerceToSAM((Closure) object, m, type);
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

        Exception nested = null;
        if (args != null) {
            try {
                return InvokerHelper.invokeConstructorOf(type, args);
            } catch (InvokerInvocationException iie) {
                throw iie;
            } catch (GroovyRuntimeException e) {
                if (e.getMessage().contains("Could not find matching constructor for")) {
                    try {
                        return InvokerHelper.invokeConstructorOf(type, object);
                    } catch (InvokerInvocationException iie) {
                        throw iie;
                    } catch (Exception ex) {
                        // let's ignore exception and return the original object
                        // as the caller has more context to be able to throw a more
                        // meaningful exception (but stash to get message later)
                        nested = e;
                    }
                } else {
                    nested = e;
                }
            } catch (Exception e) {
                // let's ignore exception and return the original object
                // as the caller has more context to be able to throw a more
                // meaningful exception (but stash to get message later)
                nested = e;
            }
        }

        GroovyCastException gce;
        if (nested != null) {
            gce = new GroovyCastException(object, type, nested);
        } else {
            gce = new GroovyCastException(object, type);
        }
        throw gce;
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
        for (Iterator iter = list.iterator(); iter.hasNext(); idx++) {
            Object element = iter.next();
            Array.set(array, idx, castToType(element, elementType));
        }

        return array;
    }

    public static <T> Collection<T> asCollection(T[] value) {
        return arrayAsCollection(value);
    }

    public static Collection asCollection(Object value) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        } else if (value instanceof Collection) {
            return (Collection) value;
        } else if (value instanceof Map) {
            Map map = (Map) value;
            return map.entrySet();
        } else if (value.getClass().isArray()) {
            return arrayAsCollection(value);
        } else if (value instanceof MethodClosure) {
            MethodClosure method = (MethodClosure) value;
            IteratorClosureAdapter adapter = new IteratorClosureAdapter(method.getDelegate());
            method.call(adapter);
            return adapter.asList();
        } else if (value instanceof String || value instanceof GString) {
            return StringGroovyMethods.toList((CharSequence) value);
        } else if (value instanceof File) {
            try {
                return ResourceGroovyMethods.readLines((File) value);
            } catch (IOException e) {
                throw new GroovyRuntimeException("Error reading file: " + value, e);
            }
        } else if (value instanceof Class && ((Class) value).isEnum()) {
            Object[] values = (Object[]) InvokerHelper.invokeMethod(value, "values", EMPTY_OBJECT_ARRAY);
            return Arrays.asList(values);
        } else {
            // let's assume it's a collection of 1
            return Collections.singletonList(value);
        }
    }

    public static Collection arrayAsCollection(Object value) {
        if (value.getClass().getComponentType().isPrimitive()) {
            return primitiveArrayToList(value);
        }
        return arrayAsCollection((Object[]) value);
    }

    public static <T> Collection<T> arrayAsCollection(T[] value) {
        return Arrays.asList((T[]) value);
    }

    /**
     * Determines whether the value object is a Class object representing a
     * subclass of java.lang.Enum. Uses class name check to avoid breaking on
     * pre-Java 5 JREs.
     *
     * @param value an object
     * @return true if the object is an Enum
     */
    @Deprecated
    public static boolean isEnumSubclass(Object value) {
        if (value instanceof Class) {
            Class superclass = ((Class) value).getSuperclass();
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
            ret[i] = Array.get(array, i);
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
        Exception cause = null;
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        }
        if (left instanceof Comparable || left instanceof Number) {
            if (left instanceof Number) {
                if (right instanceof Character || right instanceof Number) {
                    return DefaultGroovyMethods.compareTo((Number) left, castToNumber(right));
                }
                if (isValidCharacterString(right)) {
                    return DefaultGroovyMethods.compareTo((Number) left, ShortTypeHandling.castToChar(right));
                }
            } else if (left instanceof Character) {
                if (isValidCharacterString(right)) {
                    return DefaultGroovyMethods.compareTo((Character) left, ShortTypeHandling.castToChar(right));
                }
                if (right instanceof Number) {
                    return DefaultGroovyMethods.compareTo((Character) left, (Number) right);
                }
                if (right instanceof String) {
                    return (left.toString()).compareTo((String) right);
                }
                if (right instanceof GString) {
                    return (left.toString()).compareTo(right.toString());
                }
            } else if (right instanceof Number) {
                if (isValidCharacterString(left)) {
                    return DefaultGroovyMethods.compareTo(ShortTypeHandling.castToChar(left), (Number) right);
                }
            } else if (left instanceof String && right instanceof Character) {
                return ((String) left).compareTo(right.toString());
            } else if (left instanceof String && right instanceof GString) {
                return ((String) left).compareTo(right.toString());
            } else if (left instanceof GString && right instanceof String) {
                return ((GString) left).compareTo(right);
            } else if (left instanceof GString && right instanceof Character) {
                return ((GString) left).compareTo(right);
            }
            if (!equalityCheckOnly || left.getClass().isAssignableFrom(right.getClass())
                    || (right.getClass() != Object.class && right.getClass().isAssignableFrom(left.getClass()) //GROOVY-4046
                    || right instanceof Comparable) // GROOVY-7954
            ) {
                Comparable comparable = (Comparable) left;
                // GROOVY-7876: when comparing for equality we try to only call compareTo when an assignable
                // relationship holds but with a container/holder class and because of erasure, we might still end
                // up with the prospect of a ClassCastException which we want to ignore but only if testing equality
                try {
                    return comparable.compareTo(right);
                } catch (ClassCastException cce) {
                    if (!equalityCheckOnly) cause = cce;
                }
            }
        }

        if (equalityCheckOnly) {
            return -1; // anything other than 0
        }
        String message = MessageFormat.format("Cannot compare {0} with value ''{1}'' and {2} with value ''{3}''",
                left.getClass().getName(), left, right.getClass().getName(), right);
        if (cause != null) {
            throw new IllegalArgumentException(message, cause);
        } else {
            throw new IllegalArgumentException(message);
        }
    }

    public static boolean compareEqual(Object left, Object right) {
        if (left == right) return true;
        if (left == null) return right instanceof NullObject;
        if (right == null) return left instanceof NullObject;
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
        if (left instanceof Map.Entry && right instanceof Map.Entry) {
            Object k1 = ((Map.Entry) left).getKey();
            Object k2 = ((Map.Entry) right).getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = ((Map.Entry) left).getValue();
                Object v2 = ((Map.Entry) right).getValue();
                if (v1 == v2 || (v1 != null && DefaultTypeTransformation.compareEqual(v1, v2)))
                    return true;
            }
            return false;
        }
        return (Boolean) InvokerHelper.invokeMethod(left, "equals", right);
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
        return (value instanceof String || value instanceof GString) && value.toString().length() == 1;
    }

    @Deprecated
    public static int[] convertToIntArray(Object a) {
        int[] ans = null;

        // conservative coding
        if (a.getClass().getName().equals("[I")) {
            ans = (int[]) a;
        } else {
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

    @Deprecated
    public static boolean[] convertToBooleanArray(Object a) {
        boolean[] ans = null;

        // conservative coding
        if (a instanceof boolean[]) {
            ans = (boolean[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new boolean[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) continue;
                ans[i] = (Boolean) ia[i];
            }
        }
        return ans;
    }

    @Deprecated
    public static byte[] convertToByteArray(Object a) {
        byte[] ans = null;

        // conservative coding
        if (a instanceof byte[]) {
            ans = (byte[]) a;
        } else {
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

    @Deprecated
    public static short[] convertToShortArray(Object a) {
        short[] ans = null;

        // conservative coding
        if (a instanceof short[]) {
            ans = (short[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new short[ia.length];
            for (int i = 0; i < ia.length; i++) {
                ans[i] = ((Number) ia[i]).shortValue();
            }
        }
        return ans;
    }

    @Deprecated
    public static char[] convertToCharArray(Object a) {
        char[] ans = null;

        // conservative coding
        if (a instanceof char[]) {
            ans = (char[]) a;
        } else {
            Object[] ia = (Object[]) a;
            ans = new char[ia.length];
            for (int i = 0; i < ia.length; i++) {
                if (ia[i] == null) {
                    continue;
                }
                ans[i] = (Character) ia[i];
            }
        }
        return ans;
    }

    @Deprecated
    public static long[] convertToLongArray(Object a) {
        long[] ans = null;

        // conservative coding
        if (a instanceof long[]) {
            ans = (long[]) a;
        } else {
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

    @Deprecated
    public static float[] convertToFloatArray(Object a) {
        float[] ans = null;

        // conservative coding
        if (a instanceof float[]) {
            ans = (float[]) a;
        } else {
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

    @Deprecated
    public static double[] convertToDoubleArray(Object a) {
        double[] ans = null;

        // conservative coding
        if (a instanceof double[]) {
            ans = (double[]) a;
        } else {
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

    @Deprecated
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
        } else {
            return a;
        }
    }

    @Deprecated
    public static Character getCharFromSizeOneString(Object value) {
        if (value instanceof GString) value = value.toString();
        if (value instanceof String) {
            String s = (String) value;
            if (s.length() != 1) throw new IllegalArgumentException("String of length 1 expected but got a bigger one");
            return s.charAt(0);
        } else {
            return ((Character) value);
        }
    }

    public static Object castToVargsArray(Object[] origin, int firstVargsPos, Class<?> arrayType) {
        Class<?> componentType = arrayType.getComponentType();
        if (firstVargsPos >= origin.length) return Array.newInstance(componentType, 0);
        int length = origin.length - firstVargsPos;
        if (length == 1 && arrayType.isInstance(origin[firstVargsPos])) return origin[firstVargsPos];
        Object newArray = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Object convertedValue = castToType(origin[firstVargsPos + i], componentType);
            Array.set(newArray, i, convertedValue);
        }
        return newArray;
    }

}
