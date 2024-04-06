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
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.*;

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
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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

    public static Object castToType(final Object object, final Class type) {
        if (object == null) return type == boolean.class ? Boolean.FALSE : null;
        if (type == Object.class) return object;

        final Class aClass = object.getClass();
        if (type == aClass || type.isAssignableFrom(aClass)) {
            return object;
        }

        if (type.isArray()) {
            return asArray(object, type);
        } else if (type.isEnum()) {
            return ShortTypeHandling.castToEnum(object, type);
        } else if (Collection.class.isAssignableFrom(type)) {
            return continueCastOnCollection(object, type);
        } else if (type == String.class) {
            return FormatHelper.toString(object);
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

    private static Object continueCastOnCollection(final Object object, final Class type) {
        if (object instanceof Collection && type.isAssignableFrom(LinkedHashSet.class)) {
            return new LinkedHashSet((Collection) object);
        }

        Supplier<Collection> newCollection = () -> {
            if (type.isAssignableFrom(ArrayList.class) && Modifier.isAbstract(type.getModifiers())) {
                return new ArrayList();
            } else if (type.isAssignableFrom(LinkedHashSet.class) && Modifier.isAbstract(type.getModifiers())) {
                return new LinkedHashSet();
            } else {
                try {
                    return (Collection) type.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new GroovyCastException("Could not instantiate instance of: " + type.getName() + ". Reason: " + e);
                }
            }
        };

        if (object.getClass().isArray()) {
            Collection answer = newCollection.get();
            // we cannot just wrap in a List as we support primitive type arrays
            int length = Array.getLength(object);
            for (int i = 0; i < length; i += 1) {
                answer.add(Array.get(object, i));
            }
            return answer;
        }

        if (object instanceof BaseStream || object instanceof Optional) {
            Collection answer = newCollection.get();
            answer.addAll(asCollection(object));
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
                double answer = n.doubleValue();
                //throw a runtime exception if conversion would be out-of-range for the type.
                if (!(n instanceof Double) && (answer == Double.NEGATIVE_INFINITY
                        || answer == Double.POSITIVE_INFINITY)) {
                    throw new GroovyRuntimeException("Automatic coercion of " + n.getClass().getName()
                            + " value " + n + " to double failed. Value is out of range.");
                }
                return answer;
            }
            if (type == BigDecimal.class) {
                return NumberMath.toBigDecimal(n);
            }
            if (type == BigInteger.class) {
                return NumberMath.toBigInteger(n);
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
            double answer = doubleUnbox(object);
            //throw a runtime exception if conversion would be out-of-range for the type.
            if (!(object instanceof Double) && (answer == Double.NEGATIVE_INFINITY
                    || answer == Double.POSITIVE_INFINITY)) {
                throw new GroovyRuntimeException("Automatic coercion of " + object.getClass().getName()
                        + " value " + object + " to double failed. Value is out of range.");
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
            // let's try to invoke the constructor with the list as arguments
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
        Exception suppressed = null;
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
                        // keep the original exception as suppressed exception to allow easier failure analysis
                        suppressed = ex;
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
        if (suppressed != null) {
            gce.addSuppressed(suppressed);
        }
        throw gce;
    }

    public static Object asArray(final Object object, final Class type) {
        if (type.isAssignableFrom(object.getClass())) {
            return object;
        }

        if (object instanceof IntStream) {
            if (type.equals(int[].class)) {
                return ((IntStream) object).toArray();
            } else if (type.equals(long[].class)) {
                return ((IntStream) object).asLongStream().toArray();
            } else if (type.equals(double[].class)) {
                return ((IntStream) object).asDoubleStream().toArray();
            } else if (type.equals(Integer[].class)) {
                return ((IntStream) object).boxed().toArray(Integer[]::new);
            }
        } else if (object instanceof LongStream) {
            if (type.equals(long[].class)) {
                return ((LongStream) object).toArray();
            } else if (type.equals(double[].class)) {
                return ((LongStream) object).asDoubleStream().toArray();
            } else if (type.equals(Long[].class)) {
                return ((LongStream) object).boxed().toArray(Long[]::new);
            }
        } else if (object instanceof DoubleStream) {
            if (type.equals(double[].class)) {
                return ((DoubleStream) object).toArray();
            } else if (type.equals(Double[].class)) {
                return ((DoubleStream) object).boxed().toArray(Double[]::new);
            }
        }

        Class<?> elementType = type.getComponentType();
        Collection<?> collection = asCollection(object);
        Object array = Array.newInstance(elementType, collection.size());

        int i = 0;
        for (Object element : collection) {
            Array.set(array, i++, castToType(element, elementType));
        }

        return array;
    }

    public static <T> Collection<T> asCollection(final T[] value) {
        return arrayAsCollection(value);
    }

    public static Collection asCollection(final Object value) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        } else if (value instanceof Collection) {
            return (Collection) value;
        } else if (value instanceof Map) {
            return ((Map) value).entrySet();
        } else if (value.getClass().isArray()) {
            return arrayAsCollection(value);
        } else if (value instanceof BaseStream) {
            return StreamGroovyMethods.toList((BaseStream) value);
        } else if (value instanceof String || value instanceof GString) {
            return StringGroovyMethods.toList((CharSequence) value);
        } else if (value instanceof Optional) { // GROOVY-10223
            return ((Optional<?>) value).map(Collections::singleton).orElseGet(Collections::emptySet);
        } else if (value instanceof Class && ((Class) value).isEnum()) {
            Object[] values = (Object[]) InvokerHelper.invokeMethod(value, "values", EMPTY_OBJECT_ARRAY);
            return Arrays.asList(values);
        } else if (value instanceof File) {
            try {
                return ResourceGroovyMethods.readLines((File) value);
            } catch (IOException e) {
                throw new GroovyRuntimeException("Error reading file: " + value, e);
            }
        } else if (value instanceof MethodClosure) {
            MethodClosure method = (MethodClosure) value;
            IteratorClosureAdapter<?> adapter = new IteratorClosureAdapter<>(method.getDelegate());
            method.call(adapter);
            return adapter.asList();
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
        return Arrays.asList(value);
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
        Objects.requireNonNull(array);
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

    /**
     * Allows conversion of arrays into an immutable List view
     *
     * @param array an array
     * @return a List view of the array
     */
    public static List primitiveArrayToUnmodifiableList(Object array) {
        return new ArrayToUnmodifiableListAdapter(array);
    }

    static class ArrayToUnmodifiableListAdapter implements List {
        private Object delegate;

        public ArrayToUnmodifiableListAdapter(Object delegate) {
            Objects.requireNonNull(delegate);
            this.delegate = delegate;
        }

        @Override
        public int size() {
            return Array.getLength(delegate);
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            for (Object next : this) {
                if (next.equals(o)) return true;
            }
            return false;
        }

        private class Itr implements Iterator {
            private int idx = 0;

            @Override
            public boolean hasNext() {
                return idx < size();
            }

            @Override
            public Object next() {
                return get(idx++);
            }
        }

        @Override
        public Iterator iterator() {
            return new Itr();
        }

        @Override
        public Object get(int index) {
            Object item = Array.get(delegate, index);
            if (item != null && item.getClass().isArray() && item.getClass().getComponentType().isPrimitive()) {
                item = primitiveArrayToUnmodifiableList(item);
            }
            return item;
        }

        @Override
        public int indexOf(Object o) {
            int idx = 0;
            boolean found = false;
            while (!found && idx < size()) {
                found = get(idx).equals(o);
                if (!found) idx++;
            }
            return found ? idx : -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            int idx = size() - 1;
            boolean found = false;
            while (!found && idx >= 0) {
                found = get(idx).equals(o);
                if (!found) idx--;
            }
            return found ? idx : -1;
        }

        @Override
        public boolean containsAll(Collection coll) {
            for (Object next : coll) {
                if (!contains(next)) return false;
            }
            return true;
        }

        @Override
        public ListIterator listIterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator listIterator(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List subList(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] toArray() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object[] toArray(Object[] a) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object set(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeIf(Predicate filter) {
            throw new UnsupportedOperationException();
        }
    }

    public static Object[] primitiveArrayBox(Object array) {
        int size = Array.getLength(array);
        Object[] ret = (Object[]) Array.newInstance(TypeUtil.autoboxType(array.getClass().getComponentType()), size);
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
        if (left == right) {
            return 0;
        } else if (left == null) {
            return -1;
        } else if (right == null) {
            return 1;
        }
        Exception cause = null;
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
            } else if (left instanceof  String && (right instanceof String || right instanceof GString || right instanceof Character)) {
                return ((String) left).compareTo(right.toString());
            } else if (left instanceof GString && (right instanceof String || right instanceof GString || right instanceof Character)) {
                return left.toString().compareTo(right.toString());
            }
            if (!equalityCheckOnly || left.getClass().isAssignableFrom(right.getClass())
                    || (right.getClass() != Object.class && right.getClass().isAssignableFrom(left.getClass()) // GROOVY-4046
                        || right instanceof Comparable) // GROOVY-7954
            ) {
                // GROOVY-7876: when comparing for equality we try to only call compareTo when an assignable
                // relationship holds but with a container/holder class and because of erasure, we might still end
                // up with the prospect of a ClassCastException which we want to ignore but only if testing equality
                try {
                    // GROOVY-9711: don't rely on Java method selection
                    return (int) InvokerHelper.invokeMethod(left, "compareTo", right);
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
            left = primitiveArrayToUnmodifiableList(left);
        }
        if (rightClass.isArray() && rightClass.getComponentType().isPrimitive()) {
            right = primitiveArrayToUnmodifiableList(right);
        }
        if (left instanceof Object[] && right instanceof List) {
            return ArrayGroovyMethods.equals((Object[]) left, (List) right);
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
            if (Objects.equals(k1, k2)) {
                Object v1 = ((Map.Entry) left).getValue();
                Object v2 = ((Map.Entry) right).getValue();
                return v1 == v2 || (v1 != null && DefaultTypeTransformation.compareEqual(v1, v2));
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
