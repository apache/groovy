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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import org.apache.groovy.util.BeanUtils;
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.util.FastArray;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.codehaus.groovy.reflection.stdclasses.CachedSAMClass.getSAMMethod;

/**
 * Helpers for choosing a meta-method and for the argument-distance used by that choice.
 */
public class MetaClassHelper {

    /**
     * Shared zero-length array for a call that passes no arguments.
     */
    public static final Object[] EMPTY_ARRAY = {};
    /**
     * Shared zero-length class array for a signature with no parameters.
     */
    public static final Class [] EMPTY_TYPE_ARRAY = {};
    /**
     * Shared zero-length class array, the same instance as {@link #EMPTY_TYPE_ARRAY}.
     */
    public static final Class [] EMPTY_CLASS_ARRAY = EMPTY_TYPE_ARRAY;
    /**
     * Shared one-element array whose only slot is {@code null}.
     * The array is mutable: a write to the slot is visible to every user of this field.
     */
    public static final Object[] ARRAY_WITH_NULL = {null}; // mutable!

    private static final int MAX_ARG_LEN = 12;
    private static final int
            OBJECT_SHIFT = 23, INTERFACE_SHIFT = 0,
            PRIMITIVE_SHIFT = 21, VARGS_SHIFT = 44;
    /* dist binary layout:
    * 0-20: interface
    * 21-22: primitive dist
    * 23-43: object dist
    * 44-48: vargs penalty
    */

    /**
     * Tests whether {@code at} can access {@code constructor} under the visibility
     * rules applied here.
     * A public constructor is accessible.
     * A private constructor is accessible when the binary name of {@code at} equals
     * {@link Constructor#getName()}, which is the binary name of the declaring class.
     * A protected constructor is accessible when {@code at} and the declaring
     * class are in the same package, the unnamed package included. When both
     * classes have named packages and those packages differ, it is accessible
     * when the declaring class is {@code at} or a superclass of {@code at}.
     * A package-private constructor is accessible in that same-package case,
     * with no superclass exception.
     * A class in the unnamed package and a class in a named package are
     * treated as different packages for both of those modifiers.
     *
     * @param at the class attempting the access
     * @param constructor the constructor being accessed
     * @return {@code true} when the visibility rule for the constructor's modifiers accepts {@code at}
     */
    public static boolean accessibleToConstructor(final Class at, final Constructor constructor) {
        boolean accessible = false;
        final int modifiers = constructor.getModifiers();
        if (Modifier.isPublic(modifiers)) {
            accessible = true;
        } else if (Modifier.isPrivate(modifiers)) {
            accessible = at.getName().equals(constructor.getName());
        } else if (Modifier.isProtected(modifiers)) {
            Boolean isAccessible = checkCompatiblePackages(at, constructor);
            if (isAccessible != null) {
                accessible = isAccessible;
            } else {
                boolean flag = false;
                Class clazz = at;
                while (!flag && clazz != null) {
                    if (clazz.equals(constructor.getDeclaringClass())) {
                        flag = true;
                        break;
                    }
                    if (clazz.equals(Object.class)) {
                        break;
                    }
                    clazz = clazz.getSuperclass();
                }
                accessible = flag;
            }
        } else {
            Boolean isAccessible = checkCompatiblePackages(at, constructor);
            if (isAccessible != null) {
                accessible = isAccessible;
            }
        }
        return accessible;
    }

    private static Boolean checkCompatiblePackages(Class at, Constructor constructor) {
        if (at.getPackage() == null && constructor.getDeclaringClass().getPackage() == null) {
            return Boolean.TRUE;
        }
        if (at.getPackage() == null && constructor.getDeclaringClass().getPackage() != null) {
            return Boolean.FALSE;
        }
        if (at.getPackage() != null && constructor.getDeclaringClass().getPackage() == null) {
            return Boolean.FALSE;
        }
        if (at.getPackage().equals(constructor.getDeclaringClass().getPackage())) {
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Boxes a primitive array into a new {@link Object} array.
     * {@code parameters} is cast to the primitive array named by
     * {@code componentType}, and each element is boxed. The component types
     * handled are {@code boolean}, {@code char}, {@code byte}, {@code short},
     * {@code int}, {@code long}, {@code float}, and {@code double}. Any other
     * component type, {@code null} included, produces {@code null} and leaves
     * {@code parameters} unread.
     *
     * @param parameters a primitive array of component type {@code componentType}
     * @param componentType the primitive component type of {@code parameters}
     * @return a new array of boxed elements, or {@code null} when {@code componentType} is not one of those eight primitives
     */
    public static Object[] asWrapperArray(Object parameters, Class componentType) {
        Object[] ret = null;
        if (componentType == boolean.class) {
            boolean[] array = (boolean[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        } else if (componentType == char.class) {
            char[] array = (char[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        } else if (componentType == byte.class) {
            byte[] array = (byte[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        } else if (componentType == int.class) {
            int[] array = (int[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        } else if (componentType == short.class) {
            short[] array = (short[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        } else if (componentType == long.class) {
            long[] array = (long[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        } else if (componentType == double.class) {
            double[] array = (double[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        } else if (componentType == float.class) {
            float[] array = (float[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = array[i];
            }
        }

        return ret;
    }

    /**
     * @param list          the original list
     * @param parameterType the resulting array type
     * @return the constructed array
     */
    public static Object asPrimitiveArray(List list, Class parameterType) {
        Class arrayType = parameterType.getComponentType();
        Object objArray = Array.newInstance(arrayType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (arrayType.isPrimitive()) {
                if (obj instanceof Integer) {
                    Array.setInt(objArray, i, (Integer) obj);
                } else if (obj instanceof Double) {
                    Array.setDouble(objArray, i, (Double) obj);
                } else if (obj instanceof Boolean) {
                    Array.setBoolean(objArray, i, (Boolean) obj);
                } else if (obj instanceof Long) {
                    Array.setLong(objArray, i, (Long) obj);
                } else if (obj instanceof Float) {
                    Array.setFloat(objArray, i, (Float) obj);
                } else if (obj instanceof Character) {
                    Array.setChar(objArray, i, (Character) obj);
                } else if (obj instanceof Byte) {
                    Array.setByte(objArray, i, (Byte) obj);
                } else if (obj instanceof Short) {
                    Array.setShort(objArray, i, (Short) obj);
                }
            } else {
                Array.set(objArray, i, obj);
            }
        }
        return objArray;
    }

    private static final Class[] PRIMITIVES = {
            boolean.class,
            Boolean.class,
            byte.class,
            Byte.class,
            short.class,
            Short.class,
            char.class,
            Character.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            BigInteger.class,
            float.class,
            Float.class,
            double.class,
            Double.class,
            BigDecimal.class,
            Number.class,
            Object.class
    };

    // GROOVY-12439: penalty for the lossy BigInteger/BigDecimal to float/Float conversions,
    // so that they never tie with or beat a wider alternative; each other argument can lean
    // at most 3 towards a float candidate, and 254 * 3 < LOSSY for the JVM's 255 argument
    // maximum, while 255 * (LOSSY + 19) << PRIMITIVE_SHIFT stays well below VARGS_SHIFT,
    // so, as in Java, a lossy non-varargs method is still preferred over a varargs one
    private static final int LOSSY = 1 << 10;

    private static final int[][] PRIMITIVE_DISTANCE_TABLE = {
            //                    0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17  18  19
            /*boolean[0]*/      { 0,  1,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,  2,},
            /*Boolean[1]*/      { 1,  0,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,  2,},
            /*byte[2]*/         {18, 19,  0,  1,  2,  3, 16, 17,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,},
            /*Byte[3]*/         {18, 19,  1,  0,  2,  3, 16, 17,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15,},
            /*short[4]*/        {18, 19, 14, 15,  0,  1, 16, 17,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13,},
            /*Short[5]*/        {18, 19, 14, 15,  1,  0, 16, 17,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13,},
            /*char[6]*/         {18, 19, 16, 17, 14, 15,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13,},
            /*Character[7]*/    {18, 19, 16, 17, 14, 15,  1,  0,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13,},
            /*int[8]*/          {18, 19, 14, 15, 12, 13, 16, 17,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11,},
            /*Integer[9]*/      {18, 19, 14, 15, 12, 13, 16, 17,  1,  0,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11,},
            /*long[10]*/        {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9,},
            /*Long[11]*/        {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  1,  0,  2,  3,  4,  5,  6,  7,  8,  9,},
            /*BigInteger[12]*/  {18, 19,  9, 10,  7,  8, 16, 17,  5,  6,  3,  4,  0, 14 + LOSSY, 15 + LOSSY, 12, 13, 11,  1,  2,},
            /*float[13]*/       {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  0,  1,  2,  3,  4,  5,  6,},
            /*Float[14]*/       {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  1,  0,  2,  3,  4,  5,  6,},
            /*double[15]*/      {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5,  6,  0,  1,  2,  3,  4,},
            /*Double[16]*/      {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5,  6,  1,  0,  2,  3,  4,},
            /*BigDecimal[17]*/  {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5 + LOSSY,  6 + LOSSY,  3,  4,  0,  1,  2,},
            /*Number[18]*/      {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5,  6,  3,  4,  2,  0,  1,},
            /*Object[19]*/      {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5,  6,  3,  4,  2,  1,  0,},
    };

    private static int getPrimitiveIndex(Class c) {
        for (byte i = 0; i < PRIMITIVES.length; i++) {
            if (PRIMITIVES[i] == c) return i;
        }
        return -1;
    }

    private static int getPrimitiveDistance(Class from, Class to) {
        // we know here that from!=to, so a distance of 0 is never valid
        // get primitive type indexes
        int fromIndex = getPrimitiveIndex(from);
        int toIndex = getPrimitiveIndex(to);
        if (fromIndex == -1 || toIndex == -1) return -1;
        return PRIMITIVE_DISTANCE_TABLE[toIndex][fromIndex];
    }

    private static int getMaximumInterfaceDistance(Class c, Class interfaceClass) {
        // -1 means a mismatch
        if (c == null) return -1;
        // 0 means a direct match
        if (c == interfaceClass) return 0;
        Class[] interfaces = c.getInterfaces();
        int max = -1;
        for (Class anInterface : interfaces) {
            int sub = getMaximumInterfaceDistance(anInterface, interfaceClass);
            // we need to keep the -1 to track the mismatch, a +1
            // by any means could let it look like a direct match
            // we want to add one, because there is an interface between
            // the interface we search for and the interface we are in.
            if (sub != -1) sub += 1;
            // we are interested in the longest path only
            max = Math.max(max, sub);
        }
        // we do not add one for super classes, only for interfaces
        int superClassMax = getMaximumInterfaceDistance(c.getSuperclass(), interfaceClass);
        if (superClassMax != -1) superClassMax += 1;
        return Math.max(max, superClassMax);
    }

    private static int getParameterCount(final Class<?> closureOrLambdaClass) {
        int parameterCount = -2;
        if (isClosureLiteral(closureOrLambdaClass)) {
            // determine parameter count from generated "doCall" methods
            for (Method m : closureOrLambdaClass.getDeclaredMethods()) {
                if ("doCall".equals(m.getName())) {
                    if (parameterCount != -2) {
                        parameterCount = -1; // 0 or 1
                    } else {
                        parameterCount = m.getParameterCount();
                    }
                }
            }
        }
        return parameterCount;
    }

    /**
     * Tells whether choosing among {@code methodOrList} for these arguments depends on the
     * method closures among them, beyond their class: whether a method closure is passed
     * where the valid candidates take functional interfaces of different arity, as a method
     * reference can implement only an interface whose abstract method it can be called with.
     * Such a choice must not be cached by argument classes.
     *
     * @param methodOrList the overload set, as a {@link FastArray} of {@link ParameterTypes};
     *                     any other value makes this method return {@code false}
     * @param types argument classes passed to {@link ParameterTypes#isValidMethod(Class[])};
     *              {@code null} makes this method return {@code false}
     * @param arguments the call arguments. A {@link MethodClosure} is compared with the
     *                  SAM arity of the functional-interface parameter at the same index.
     *                  {@code null} makes this method return {@code false}
     * @return {@code true} when two candidates that are still valid for {@code types}
     *         accept a method closure at the same position through functional interfaces
     *         whose abstract methods take different numbers of parameters
     * @since 6.0.1
     */
    public static boolean dependsOnMethodClosureArity(final Object methodOrList, final Class[] types, final Object[] arguments) {
        if (!(methodOrList instanceof FastArray methods) || arguments == null || types == null) return false;
        for (int i = 0; i < arguments.length; i += 1) {
            if (!(arguments[i] instanceof MethodClosure)) continue;
            int arity = -1;
            for (int j = 0, n = methods.size(); j < n; j += 1) {
                var candidate = (ParameterTypes) methods.get(j);
                if (!candidate.isValidMethod(types)) continue;
                Class<?>[] parameters = candidate.getNativeParameterTypes();
                Method sam = i < parameters.length && parameters[i].isInterface() ? getSAMMethod(parameters[i]) : null;
                if (sam == null) continue;
                if (arity == -1) arity = sam.getParameterCount();
                else if (arity != sam.getParameterCount()) return true;
            }
        }
        return false;
    }

    /**
     * Drops the candidates with a functional-interface parameter whose abstract method takes
     * a number of arguments the method closure passed there cannot be called with; keeps
     * every candidate if none fits.
     *
     * @param <M> the candidate type. Each element is read as {@link ParameterTypes}
     * @param candidates the overload candidates to filter
     * @param arguments the call arguments. A candidate is kept only when the
     *                  functional-interface parameter at the index of each
     *                  {@link MethodClosure} has an abstract method the closure can be called with
     * @return the candidates that accept those method closures, or {@code candidates}
     *         itself when none of them fit
     * @since 6.0.1
     */
    public static <M> List<M> filterByMethodClosureArity(final List<M> candidates, final Object[] arguments) {
        List<M> fitting = new ArrayList<>(candidates.size());
        for (M candidate : candidates) {
            Class<?>[] parameters = ((ParameterTypes) candidate).getNativeParameterTypes();
            boolean fits = true;
            for (int i = 0, n = Math.min(arguments.length, parameters.length); i < n && fits; i += 1) {
                if (arguments[i] instanceof MethodClosure mc && parameters[i].isInterface()) {
                    Method sam = getSAMMethod(parameters[i]);
                    fits = (sam == null || mc.acceptsArgumentCount(sam.getParameterCount()));
                }
            }
            if (fits) fitting.add(candidate);
        }
        return fitting.isEmpty() ? candidates : fitting;
    }

    /**
     * The numbers of arguments a closure literal's class accepts, read from its
     * {@code doCall} methods; a trailing-array parameter accepts any count from one fewer.
     * Packed closures present the same view through their fixed-arity classes.
     *
     */
    private record ClosureArgumentCounts(BitSet counts, int varargsFrom) {
        boolean accepts(final int n) {
            return n >= varargsFrom || counts.get(n);
        }
    }

    private static final ClassValue<ClosureArgumentCounts> CLOSURE_ARGUMENT_COUNTS = new ClassValue<>() {
        @Override
        protected ClosureArgumentCounts computeValue(final Class<?> type) {
            BitSet counts = new BitSet();
            int varargsFrom = Integer.MAX_VALUE;
            for (Method m : type.getDeclaredMethods()) {
                if (!"doCall".equals(m.getName())) continue;
                int count = m.getParameterCount();
                counts.set(count);
                if (count > 0 && m.getParameterTypes()[count - 1].isArray()) {
                    varargsFrom = Math.min(varargsFrom, count - 1);
                }
            }
            if (counts.isEmpty()) varargsFrom = 0; // no doCall: accept any count
            return new ClosureArgumentCounts(counts, varargsFrom);
        }
    };

    /**
     * Drops the candidates with a functional-interface parameter whose abstract method takes
     * a number of arguments the closure literal passed there cannot be called with, as known
     * from its class; keeps every candidate if none fits. The choice is still determined by
     * the argument classes.
     *
     * @param <M> the candidate type. Each element is read as {@link ParameterTypes}
     * @param candidates the overload candidates to filter
     * @param arguments the argument classes. A closure-literal class constrains the
     *                  SAM arity of the functional-interface parameter at the same index.
     *                  When none of the classes is a closure literal, {@code candidates} is returned unchanged
     * @return the candidates that accept those closure classes, or {@code candidates}
     *         itself when none of them fit
     * @since 7.0.0
     */
    public static <M> List<M> filterByClosureArity(final List<M> candidates, final Class[] arguments) {
        boolean anyClosure = false;
        for (Class<?> argument : arguments) {
            if (isClosureLiteral(argument)) { anyClosure = true; break; }
        }
        if (!anyClosure) return candidates;
        List<M> fitting = new ArrayList<>(candidates.size());
        for (M candidate : candidates) {
            Class<?>[] parameters = ((ParameterTypes) candidate).getNativeParameterTypes();
            boolean fits = true;
            for (int i = 0, n = Math.min(arguments.length, parameters.length); i < n && fits; i += 1) {
                if (isClosureLiteral(arguments[i]) && parameters[i].isInterface()) {
                    Method sam = getSAMMethod(parameters[i]);
                    fits = (sam == null || CLOSURE_ARGUMENT_COUNTS.get(arguments[i]).accepts(sam.getParameterCount()));
                }
            }
            if (fits) fitting.add(candidate);
        }
        return fitting.isEmpty() ? candidates : fitting;
    }

    private static boolean isClosureLiteral(final Class<?> candidate) {
        return candidate != null && GeneratedClosure.class.isAssignableFrom(candidate);
    }

    private static long calculateParameterDistance(final Class<?> argument, final CachedClass parameter) {
        /*
         * note: when shifting with 32 bit, you should only shift on a long. If you do
         *       that with an int, then i==(i<<32), which means you loose the shift
         *       information
         */

        Class<?> parameterClass = parameter.getTheClass();
        if (parameterClass == argument || (parameterClass == Closure.class && isClosureLiteral(argument))) { // GROOVY-10714, GROOVY-11681
            return 0; // exact match
        }

        if (parameter.isInterface()) {
            long dist = argument == null ? 2 : getMaximumInterfaceDistance(argument, parameterClass);
            if (dist >= 0 || !Closure.class.isAssignableFrom(argument)) {
                return dist << INTERFACE_SHIFT;
            }
        }

        long objectDistance = 0;
        if (argument != null) {
            long dist = getPrimitiveDistance(parameterClass, argument);
            if (dist >= 0) {
                return dist << PRIMITIVE_SHIFT;
            }

            // add one to dist to be sure interfaces are preferred
            objectDistance += (PRIMITIVES.length + 1);

            // GROOVY-5114: if choosing between foo(Object[]) and foo(Object)
            // and the argument is an array, then array version is preferable
            if (argument.isArray() && !parameter.isArray) {
                objectDistance += 4;
            }

            Method sam;
            for (Class<?> c = TypeUtil.autoboxType(argument); c != null && c != parameterClass; c = c.getSuperclass()) {
                if (c == Closure.class && parameterClass.isInterface() && (sam= getSAMMethod(parameterClass)) != null) {
                    // in case of multiple overloads, give preference to same parameter count
                    // with fuzzy matching of count for implicit-parameter closures / lambdas
                    int argParamCount = getParameterCount(argument);
                    int samParamCount = sam.getParameterCount();
                    if ((argParamCount == samParamCount) ||                                  // GROOVY-9881
                        (argParamCount == -1 && (samParamCount == 0 || samParamCount == 1))) // GROOVY-10905
                        objectDistance -= 1;
                    else if (argParamCount >= 0)                                             // GROOVY-11121
                        objectDistance += 2;

                    objectDistance += 5; // ahead of Object but behind GroovyObjectSupport
                    break;
                }
                if (c == GString.class && parameterClass == String.class) {
                    objectDistance += 2;
                    break;
                }
                objectDistance += 3;
            }
        } else if (parameterClass == NullObject.class) {
            return 0; // exact match -- GROOVY-9367
        } else if (parameterClass == Object.class) {
            return 1; // tight match
        } else {
            // compute the distance to Object
            for (Class<?> c = parameterClass; c != null && c != Object.class; c = c.getSuperclass()) {
                objectDistance += 2;
            }
        }
        return objectDistance << OBJECT_SHIFT;
    }

    /**
     * Scores how far {@code arguments} are from the parameters of {@code pt}.
     * A smaller non-negative score is a closer match. A negative score means
     * an interface parameter the argument does not implement. An empty
     * parameter list scores {@code 0}.
     * <p>
     * Every parameter except the last contributes one term for the argument
     * in that position. An exact class match scores {@code 0}, as does a
     * class assignable to {@link GeneratedClosure} passed to {@link Closure}.
     * An interface parameter scores the longest path through superclasses and
     * superinterfaces, and scores a negative value when no such path exists.
     * A {@link Closure} that does not implement the interface is scored as an
     * object instead, with a lower score when its {@code doCall} count matches
     * the single abstract method. A class that declares more than one
     * {@code doCall} matches an abstract method of zero or one parameter.
     * Two types that are both in this class's primitive-distance table (the
     * primitives, their wrappers, {@link BigInteger}, {@link BigDecimal},
     * {@link Number}, and {@link Object}) contribute that table's conversion
     * distance. Any other pair contributes a superclass walk from the argument
     * toward the parameter, plus a further penalty when an array is passed to
     * a non-array parameter. The walk stops early when {@link GString} is
     * passed to {@link String}. A null argument scores {@code 0} for
     * {@link NullObject} and {@code 1} for {@link Object}; otherwise the score
     * is the distance from the parameter up to {@link Object}. A null argument
     * passed to an interface uses a small fixed interface distance instead.
     * <p>
     * The last parameter is then treated as a possible varargs parameter.
     * The varargs penalty occupies higher bits than those per-argument terms,
     * so a non-varargs fit outranks every varargs fit, and a varargs fit that
     * consumes fewer arguments outranks one that consumes more.
     * Equal lengths with the last argument Java-assignable to the last
     * parameter, null counting as assignable, add no penalty, and that
     * argument is scored against the last parameter.
     * Fewer arguments than parameters add one penalty unit and leave the
     * missing tail unscored.
     * Equal lengths where the last argument is not assignable to the last
     * parameter add two penalty units, and that argument is scored against
     * the parameter's component type.
     * More arguments than parameters add two penalty units plus one unit per
     * extra argument, and every argument from the last parameter onward is
     * scored against that parameter's component type.
     *
     * @param arguments classes of the call arguments, in order; a null entry is a null argument
     * @param pt the candidate parameter list
     * @return the packed distance; a smaller non-negative value is a closer fit
     */
    public static long calculateParameterDistance(Class[] arguments, ParameterTypes pt) {
        CachedClass[] parameters = pt.getParameterTypes();
        final int parametersLength = parameters.length;
        if (parametersLength == 0) return 0;

        long ret = 0;
        int noVargsLength = parametersLength - 1;

        // if the number of parameters does not match we have
        // a vargs usage
        //
        // case A: arguments.length<parameters.length
        //
        //         In this case arguments.length is always equal to
        //         noVargsLength because only the last parameter
        //         might be a optional vargs parameter
        //
        //         VArgs penalty: 1l
        //
        // case B: arguments.length>parameters.length
        //
        //         In this case all arguments with a index bigger than
        //         paramMinus1 are part of the vargs, so a
        //         distance calculation needs to be done against
        //         parameters[noVargsLength].getComponentType()
        //
        //         VArgs penalty: 2l+arguments.length-parameters.length
        //
        // case C: arguments.length==parameters.length &&
        //         isAssignableFrom( parameters[noVargsLength],
        //                           arguments[noVargsLength] )
        //
        //         In this case we have no vargs, so calculate directly
        //
        //         VArgs penalty: 0l
        //
        // case D: arguments.length==parameters.length &&
        //         !isAssignableFrom( parameters[noVargsLength],
        //                            arguments[noVargsLength] )
        //
        //         In this case we have a vargs case again, we need
        //         to calculate arguments[noVargsLength] against
        //         parameters[noVargsLength].getComponentType
        //
        //         VArgs penalty: 2l
        //
        //         This gives: VArgs_penalty(C)<VArgs_penalty(A)
        //                     VArgs_penalty(A)<VArgs_penalty(D)
        //                     VArgs_penalty(D)<VArgs_penalty(B)

        /*
         * In general we want to match the signature that allows us to use
         * as less arguments for the vargs part as possible. That means the
         * longer signature usually wins if both signatures are vargs, while
         * vargs looses always against a signature without vargs.
         *
         *  A vs B :
         *      def foo(Object[] a) {1}     -> case B
         *      def foo(a,b,Object[] c) {2} -> case A
         *      assert foo(new Object(),new Object()) == 2
         *  --> A preferred over B
         *
         *  A vs C :
         *      def foo(Object[] a) {1}     -> case B
         *      def foo(a,b)        {2}     -> case C
         *      assert foo(new Object(),new Object()) == 2
         *  --> C preferred over A
         *
         *  A vs D :
         *      def foo(Object[] a) {1}     -> case D
         *      def foo(a,Object[] b) {2}   -> case A
         *      assert foo(new Object()) == 2
         *  --> A preferred over D
         *
         *  This gives C<A<B,D
         *
         *  B vs C :
         *      def foo(Object[] a) {1}     -> case B
         *      def foo(a,b) {2}            -> case C
         *      assert foo(new Object(),new Object()) == 2
         *  --> C preferred over B, matches C<A<B,D
         *
         *  B vs D :
         *      def foo(Object[] a)   {1}   -> case B
         *      def foo(a,Object[] b) {2}   -> case D
         *      assert foo(new Object(),new Object()) == 2
         *  --> D preferred over B
         *
         *  This gives C<A<D<B
         */

        // first we calculate all arguments, that are for sure not part
        // of vargs.  Since the minimum for arguments is noVargsLength
        // we can safely iterate to this point
        for (int i = 0; i < noVargsLength; i++) {
            ret += calculateParameterDistance(arguments[i], parameters[i]);
        }

        final int argumentsLength = arguments.length;
        if (argumentsLength == parametersLength) {
            // case C&D, we use baseType to calculate and set it
            // to the value we need according to case C and D
            CachedClass baseType = parameters[noVargsLength]; // case C
            if (!parameters[noVargsLength].isAssignableFrom(arguments[noVargsLength])) {
                baseType = ReflectionCache.getCachedClass(baseType.getTheClass().getComponentType()); // case D
                ret += 2L << VARGS_SHIFT; // penalty for vargs
            }
            ret += calculateParameterDistance(arguments[noVargsLength], baseType);
        } else if (argumentsLength > parametersLength) {
            // case B
            // we give our a vargs penalty for each exceeding argument and iterate
            // by using parameters[noVargsLength].getComponentType()
            ret += (2L + argumentsLength - parametersLength) << VARGS_SHIFT; // penalty for vargs
            CachedClass vargsType = ReflectionCache.getCachedClass(parameters[noVargsLength].getTheClass().getComponentType());
            for (int i = noVargsLength; i < argumentsLength; i++) {
                ret += calculateParameterDistance(arguments[i], vargsType);
            }
        } else {
            // case A
            // we give a penalty for vargs, since we have no direct
            // match for the last argument
            ret += 1L << VARGS_SHIFT;
        }

        return ret;
    }

    /**
     * @deprecated Use {@link BeanUtils#capitalize} instead
     */
    @Deprecated(forRemoval = true, since = "3.0.0")
    public static String capitalize(final String property) {
        return BeanUtils.capitalize(property);
    }

    /**
     * @param methods the methods to choose from
     * @return the method with 1 parameter which takes the most general type of
     *         object (e.g. Object)
     */
    public static Object chooseEmptyMethodParams(FastArray methods) {
        Object vargsMethod = null;
        final int len = methods.size();
        final Object[] data = methods.getArray();
        for (int i = 0; i != len; ++i) {
            Object method = data[i];
            final ParameterTypes pt = (ParameterTypes) method;
            CachedClass[] paramTypes = pt.getParameterTypes();
            int paramLength = paramTypes.length;
            if (paramLength == 0) {
                return method;
            } else if (paramLength == 1 && pt.isVargsMethod(EMPTY_ARRAY)) {
                vargsMethod = method;
            }
        }
        return vargsMethod;
    }

    /**
     * Warning: this method does not choose properly if multiple methods with
     * the same distance are encountered
     * @param methods the methods to choose from
     * @return the method with 1 parameter which takes the most general type of
     *         object (e.g. Object) ignoring primitive types
     * @deprecated
     */
    @Deprecated
    public static Object chooseMostGeneralMethodWith1NullParam(FastArray methods) {
        // let's look for methods with 1 argument which matches the type of the
        // arguments
        CachedClass closestClass = null;
        CachedClass closestVargsClass = null;
        Object answer = null;
        int closestDist = -1;
        final int len = methods.size();
        final Object[] data = methods.getArray();
        for (int i = 0; i != len; ++i) {
            Object method = data[i];
            final ParameterTypes pt = (ParameterTypes) method;
            CachedClass[] paramTypes = pt.getParameterTypes();
            int paramLength = paramTypes.length;
            if (paramLength == 0 || paramLength > 2) continue;

            CachedClass theType = paramTypes[0];
            if (theType.isPrimitive) continue;

            if (paramLength == 2) {
                if (!pt.isVargsMethod(ARRAY_WITH_NULL)) continue;
                if (closestClass == null) {
                    closestVargsClass = paramTypes[1];
                    closestClass = theType;
                    answer = method;
                } else if (closestClass.getTheClass() == theType.getTheClass()) {
                    if (closestVargsClass == null) continue;
                    CachedClass newVargsClass = paramTypes[1];
                    if (isAssignableFrom(newVargsClass.getTheClass(), closestVargsClass.getTheClass())) {
                        closestVargsClass = newVargsClass;
                        answer = method;
                    }
                } else if (isAssignableFrom(theType.getTheClass(), closestClass.getTheClass())) {
                    closestVargsClass = paramTypes[1];
                    closestClass = theType;
                    answer = method;
                }
            } else {
                if (closestClass == null || isAssignableFrom(theType.getTheClass(), closestClass.getTheClass())) {
                    closestVargsClass = null;
                    closestClass = theType;
                    answer = method;
                    closestDist = -1;
                } else {
                    // closestClass and theType are not in a subtype relation, we need
                    // to check the distance to Object
                    if (closestDist == -1) closestDist = closestClass.getSuperClassDistance();
                    int newDist = theType.getSuperClassDistance();
                    if (newDist < closestDist) {
                        closestDist = newDist;
                        closestVargsClass = null;
                        closestClass = theType;
                        answer = method;
                    }
                }
            }
        }
        return answer;
    }

    //

    /**
     * @param list   a list of MetaMethods
     * @param method the MetaMethod of interest
     * @return true if a method of the same matching prototype was found in the
     *         list
     */
    public static boolean containsMatchingMethod(List list, MetaMethod method) {
        for (Object aList : list) {
            MetaMethod aMethod = (MetaMethod) aList;
            CachedClass[] params1 = aMethod.getParameterTypes();
            CachedClass[] params2 = method.getParameterTypes();
            if (params1.length == params2.length) {
                boolean matches = true;
                for (int i = 0; i < params1.length; i++) {
                    if (params1[i] != params2[i]) {
                        matches = false;
                        break;
                    }
                }
                if (matches) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Converts array of values (incl. {@code null}) to their respective types.
     *
     * @param arguments the arguments
     * @return the types of the arguments
     */
    public static Class[] convertToTypeArray(final Object[] arguments) {
        if (arguments == null) return null;
        int n = arguments.length;
        Class[] types = new Class[n];
        for (int i = 0; i < n; i += 1) {
            types[i] = getClassWithNullAndWrapper(arguments[i]);
        }
        return types;
    }

    /**
     * Copies {@code arguments} from {@code offset} to the end into a new array
     * whose component type is shared by that range.
     * Null elements are skipped while the type is chosen. The type starts as the
     * class of the first non-null element and walks toward {@link Object} until
     * {@link Class#isAssignableFrom(Class)} accepts every later non-null element.
     * A range of only nulls uses {@code fallback}. When the chosen class is
     * {@link Object} and {@code fallback} is an interface, the interface is used
     * instead when every slot in the range is non-null and, for each element,
     * {@code fallback} is a direct interface of that element's class or of a
     * superclass below {@link Object}.
     *
     * @param arguments the source values; elements before {@code offset} are ignored
     * @param offset the first index copied into the result
     * @param fallback component type of an all-null range, and the interface tried when the common class is {@link Object}
     * @return the array of the chosen component type holding the copied elements
     */
    public static Object makeCommonArray(Object[] arguments, int offset, Class fallback) {
        // arguments.length>0 && !=null
        Class baseClass = null;
        for (int i = offset; i < arguments.length; i++) {
            if (arguments[i] == null) continue;
            Class argClass = arguments[i].getClass();
            if (baseClass == null) {
                baseClass = argClass;
            } else {
                for (; baseClass != Object.class; baseClass = baseClass.getSuperclass()) {
                    if (baseClass.isAssignableFrom(argClass)) break;
                }
            }
        }
        if (baseClass == null) {
            // all arguments were null
            baseClass = fallback;
        }
        /*
         * If no specific super class has been found and type fallback is an interface, check if all arg classes
         * implement it. If yes, then that interface is the common type across arguments.
         */
        if (baseClass == Object.class && fallback.isInterface()) {
            int tmpCount = 0;
            for (int i = offset; i < arguments.length; i++) {
                if (arguments[i] != null) {
                    Class tmpClass;
                    Set<Class> intfs = new HashSet<Class>();
                    tmpClass = arguments[i].getClass();
                    for (; tmpClass != Object.class; tmpClass = tmpClass.getSuperclass()) {
                        intfs.addAll(Arrays.asList(tmpClass.getInterfaces()));
                    }
                    if (intfs.contains(fallback)) {
                        tmpCount++;
                    }
                }
            }
            // all arg classes implement interface fallback, so use that as the array component type
            if (tmpCount == arguments.length - offset) {
                baseClass = fallback;
            }
        }
        Object result = makeArray(null, baseClass, arguments.length - offset);
        System.arraycopy(arguments, offset, result, 0, arguments.length - offset);
        return result;
    }

    /**
     * Allocates an array of the given {@code length}. The component type is the
     * runtime class of {@code obj} when {@code obj} is non-null, and
     * {@code secondary} when it is null. The new array is empty; {@code obj} is
     * not stored in it.
     *
     * @param obj instance whose class supplies the component type, or {@code null}
     * @param secondary component type used when {@code obj} is null
     * @param length the requested array length
     * @return a new array of that component type and length
     */
    public static Object makeArray(Object obj, Class secondary, int length) {
        Class baseClass = secondary;
        if (obj != null) {
            baseClass = obj.getClass();
        }
        /*if (GString.class.isAssignableFrom(baseClass)) {
              baseClass = GString.class;
          }*/
        return Array.newInstance(baseClass, length);
    }

    /**
     * Builds a {@link GroovyRuntimeException} describing a failed meta-method call.
     * The message is {@code init}, the {@code method}, {@code " on: "}, the
     * receiver {@code object}, {@code " with arguments: "},
     * {@link FormatHelper#toString(Object) FormatHelper.toString}{@code (args)},
     * {@code " reason: "}, and {@code reason}, concatenated in that order.
     * When {@code setReason} is true, {@code reason} is also the exception cause;
     * when it is false, the cause is left null. This method returns the exception
     * and does not throw it.
     *
     * @param init text that opens the message
     * @param method the meta-method that failed
     * @param object the receiver the method was invoked on
     * @param args the arguments passed to the method
     * @param reason the failure to report
     * @param setReason whether {@code reason} is attached as the cause
     * @return the populated exception
     */
    public static GroovyRuntimeException createExceptionText(String init, MetaMethod method, Object object, Object[] args, Throwable reason, boolean setReason) {
        return new GroovyRuntimeException(
                init
                        + method
                        + " on: "
                        + object
                        + " with arguments: "
                        + FormatHelper.toString(args)
                        + " reason: "
                        + reason,
                setReason ? reason : null);
    }

    /**
     * Returns the binary name associated with {@code object}.
     * A null argument returns null. A {@link Class} contributes its own name,
     * rather than the name {@code java.lang.Class}. Every other value contributes
     * the name of its runtime class.
     *
     * @param object a class, an instance, or null
     * @return the binary name, or null when {@code object} is null
     */
    protected static String getClassName(Object object) {
        if (object == null) return null;
        return (object instanceof Class) ? ((Class) object).getName() : object.getClass().getName();
    }

    /**
     * Returns a callable object for the given method name on the object.
     * The object acts like a Closure in that it can be called, like a closure
     * and passed around - though really it's a method pointer, not a closure per se.
     *
     * @param object     the object containing the method
     * @param methodName the method of interest
     * @return the resulting closure-like method pointer
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        return new MethodClosure(object, methodName);
    }

    /**
     * Tests whether a value of {@code classToTransformFrom} may be passed where
     * {@code classToTransformTo} is expected, including Groovy coercions beyond
     * {@link Class#isAssignableFrom(Class)}.
     * A null source is accepted for every target, and a target of {@link Object}
     * accepts every source. Both classes are boxed before the remaining checks,
     * so a primitive and its wrapper match. {@code boolean} and {@code char}
     * take part through boxing and ordinary assignability. The further coercions are:
     * <ul>
     * <li>{@link Integer} also accepts {@link Short}, {@link Byte}, and {@link BigInteger}.</li>
     * <li>{@link Long} also accepts {@link Integer}, {@link Short}, and {@link Byte}.</li>
     * <li>{@link Float} also accepts {@link Integer}, {@link Long}, {@link Short}, and {@link Byte}.</li>
     * <li>{@link Double} also accepts {@link Integer}, {@link Long}, {@link Short}, {@link Byte},
     *     {@link Float}, {@link BigDecimal}, and {@link BigInteger}.</li>
     * <li>{@link BigInteger} also accepts {@link Integer}, {@link Long}, {@link Short}, and {@link Byte}.</li>
     * <li>{@link BigDecimal} also accepts {@link Double}, {@link Float}, {@link BigInteger},
     *     {@link Integer}, {@link Long}, {@link Short}, and {@link Byte}.</li>
     * <li>{@link Short} also accepts {@link Byte}.</li>
     * <li>{@link String} also accepts {@link GString} and its subclasses.</li>
     * </ul>
     * Every other pair uses {@link Class#isAssignableFrom(Class)} on the boxed classes.
     *
     * @param classToTransformTo the destination type
     * @param classToTransformFrom the source type, or null
     * @return {@code true} when the source may be used as the destination
     */
    public static boolean isAssignableFrom(Class classToTransformTo, Class classToTransformFrom) {
        if (classToTransformTo == classToTransformFrom
                || classToTransformFrom == null
                || classToTransformTo == Object.class) {
            return true;
        }

        classToTransformTo = TypeUtil.autoboxType(classToTransformTo);
        classToTransformFrom = TypeUtil.autoboxType(classToTransformFrom);
        if (classToTransformTo == classToTransformFrom) return true;

        // note: there is no coercion for boolean and char. Range matters, precision doesn't
        if (classToTransformTo == Integer.class) {
            if (classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == BigInteger.class)
                return true;
        } else if (classToTransformTo == Double.class) {
            if (classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == Float.class
                    || classToTransformFrom == BigDecimal.class
                    || classToTransformFrom == BigInteger.class)
                return true;
        } else if (classToTransformTo == BigDecimal.class) {
            if (classToTransformFrom == Double.class
                    || classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == Float.class
                    || classToTransformFrom == BigInteger.class)
                return true;
        } else if (classToTransformTo == BigInteger.class) {
            if (isIntegerLongShortByte(classToTransformFrom))
                return true;
        } else if (classToTransformTo == Long.class) {
            if (classToTransformFrom == Integer.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == Float.class) {
            if (isIntegerLongShortByte(classToTransformFrom))
                return true;
        } else if (classToTransformTo == Short.class) {
            if (classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == String.class) {
            if (GString.class.isAssignableFrom(classToTransformFrom)) {
                return true;
            }
        }

        return classToTransformTo.isAssignableFrom(classToTransformFrom);
    }

    private static boolean isIntegerLongShortByte(Class classToTransformFrom) {
        return classToTransformFrom == Integer.class
                || classToTransformFrom == Long.class
                || classToTransformFrom == Short.class
                || classToTransformFrom == Byte.class;
    }

    /**
     * Walks {@code clazz} and then each superclass, looking for {@code superclass}.
     * The walk includes {@code clazz} itself, so a class counts as a superclass of
     * itself here. A null {@code clazz} ends the walk immediately.
     *
     * @param clazz the class whose superclass chain is examined, or null
     * @param superclass the class to find on that chain
     * @return {@code true} when {@code superclass} is {@code clazz} or a superclass of it
     */
    protected static boolean isSuperclass(Class clazz, Class superclass) {
        while (clazz != null) {
            if (clazz == superclass) return true;
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    /**
     * Tests whether each argument class is acceptable for the parameter class in
     * the same position, using {@link #isAssignableFrom(Class, Class)}.
     * The arrays must have the same length; a shorter or longer argument list is
     * incompatible, varargs included.
     *
     * @param arguments the runtime argument classes
     * @param parameters the formal parameter classes
     * @return {@code true} when the lengths match and each argument is assignable to its parameter
     */
    public static boolean parametersAreCompatible(Class[] arguments, Class[] parameters) {
        final int argumentsLength = arguments.length;
        if (argumentsLength != parameters.length) return false;
        for (int i = 0; i < argumentsLength; i++) {
            if (!isAssignableFrom(parameters[i], arguments[i])) return false;
        }
        return true;
    }

    /**
     * Logs an invocation at {@link Level#FINER} when that level is enabled for
     * the call's logger. The logger name is {@code methodCalls.} followed by
     * {@link #getClassName(Object)} of {@code object} and {@code methodName};
     * a null receiver is concatenated as the text {@code null}. The log record's
     * source method is {@code methodName} plus the arguments rendered by
     * {@link #normalizedValue(Object)}, separated by commas. A null
     * {@code arguments} array produces an empty pair of parentheses. The log
     * message is {@code called from MetaClass.invokeMethod}. A logger that is
     * not at {@link Level#FINER} returns before the message is built.
     *
     * @param object the receiver, used in the logger name
     * @param methodName the method that was invoked
     * @param arguments the arguments, or null when there are none to render
     */
    public static void logMethodCall(Object object, String methodName, Object[] arguments) {
        String className = getClassName(object);
        String logname = "methodCalls." + className + "." + methodName;
        Logger objLog = Logger.getLogger(logname);
        if (!objLog.isLoggable(Level.FINER)) return;
        StringBuilder msg = new StringBuilder(methodName);
        msg.append("(");
        if (arguments != null) {
            for (int i = 0; i < arguments.length;) {
                msg.append(normalizedValue(arguments[i]));
                if (++i < arguments.length) {
                    msg.append(",");
                }
            }
        }
        msg.append(")");
        objLog.logp(Level.FINER, className, msg.toString(), "called from MetaClass.invokeMethod");
    }

    /**
     * Renders one argument for {@link #logMethodCall(Object, String, Object[])}.
     * The text is {@link Object#toString()}. When that text is longer than twelve
     * characters, the first ten are kept and {@code ..} is appended. A
     * {@link String} is then wrapped in single quotes. When {@code toString}
     * throws, the result is {@link #shortName(Object)} instead, which is also the
     * result for a null argument.
     *
     * @param argument the value to render, or null
     * @return the truncated text, quoted when the value is a {@link String}, or the short-name fallback
     */
    protected static String normalizedValue(Object argument) {
        String value;
        try {
            value = argument.toString();
            if (value.length() > MAX_ARG_LEN) {
                value = value.substring(0, MAX_ARG_LEN - 2) + "..";
            }
            if (argument instanceof String) {
                value = "'" + value + "'";
            }
        } catch (Exception e) {
            value = shortName(argument);
        }
        return value;
    }

    /**
     * Returns a simple name of {@code object} for a log line.
     * A null argument, or an argument whose class is null, yields
     * {@code unknownClass}. Otherwise the binary name from
     * {@link #getClassName(Object)} is used: a {@link Class} contributes its own
     * name, and any other value contributes its runtime class name. When that
     * name contains a {@code '.'} with at least one character after it, the
     * substring after the last dot is returned. A missing binary name yields
     * {@code unknownClassName}.
     *
     * @param object the value whose class name is shortened, or null
     * @return the simple name, {@code unknownClass}, or {@code unknownClassName}
     */
    protected static String shortName(Object object) {
        if (object == null || object.getClass() == null) return "unknownClass";
        String name = getClassName(object);
        if (name == null) return "unknownClassName"; // *very* defensive...
        int lastDotPos = name.lastIndexOf('.');
        if (lastDotPos < 0 || lastDotPos >= name.length() - 1) return name;
        return name.substring(lastDotPos + 1);
    }

    /**
     * Copies {@code classes}, boxing selected primitives and rewriting
     * {@link GString} types to {@link String}. A null entry stays null.
     * {@code int}, {@code byte}, {@code long}, {@code double}, and {@code float}
     * become the matching wrapper class. {@code boolean}, {@code char},
     * {@code short}, and {@code void} stay primitive. A type that is
     * {@link GString} or a subclass of it becomes {@link String}. Every other
     * class is copied unchanged.
     *
     * @param classes the classes to adapt; entries may be null
     * @return a new array of the same length with those substitutions
     */
    public static Class[] wrap(Class[] classes) {
        Class[] wrappedArguments = new Class[classes.length];
        for (int i = 0; i < wrappedArguments.length; i++) {
            Class c = classes[i];
            if (c == null) continue;
            if (c.isPrimitive()) {
                if (c == Integer.TYPE) {
                    c = Integer.class;
                } else if (c == Byte.TYPE) {
                    c = Byte.class;
                } else if (c == Long.TYPE) {
                    c = Long.class;
                } else if (c == Double.TYPE) {
                    c = Double.class;
                } else if (c == Float.TYPE) {
                    c = Float.class;
                }
            } else if (isSuperclass(c, GString.class)) {
                c = String.class;
            }
            wrappedArguments[i] = c;
        }
        return wrappedArguments;
    }

    /**
     * Tests whether each parameter class is identical to the class derived from
     * the argument in the same position. The arrays must have the same length.
     * A null argument matches a null parameter class. A {@link Wrapper} is
     * represented by {@link Wrapper#getType()}, and every other argument by
     * {@link Object#getClass()}. {@code weakNullCheck} is not consulted; null
     * and non-null arguments share this comparison.
     *
     * @param params the parameter classes, null slots included
     * @param arguments the call arguments
     * @param weakNullCheck unused
     * @return {@code true} when the lengths match and every slot matches by identity
     */
    public static boolean sameClasses(Class[] params, Object[] arguments, boolean weakNullCheck) {
        if (params.length != arguments.length)
            return false;

        for (int i = params.length - 1; i >= 0; i--) {
            Object arg = arguments[i];
            Class compareClass = getClassWithNullAndWrapper(arg);
            if (params[i] != compareClass) return false;
        }

        return true;
    }

    private static Class getClassWithNullAndWrapper(Object arg) {
        if (arg == null) return null;
        if (arg instanceof Wrapper w) {
            return w.getType();
        }
        return arg.getClass();
    }

    /**
     * Tests whether each parameter class is identical to the class derived from
     * the argument in the same position. The arrays must have the same length.
     * A null argument matches a null parameter class. A {@link Wrapper} is
     * represented by {@link Wrapper#getType()}, and every other argument by
     * {@link Object#getClass()}.
     *
     * @param params the parameter classes, null slots included
     * @param arguments the call arguments
     * @return {@code true} when the lengths match and every slot matches by identity
     */
    public static boolean sameClasses(Class[] params, Object[] arguments) {
        if (params.length != arguments.length)
            return false;

        for (int i = params.length - 1; i >= 0; i--) {
            Object arg = arguments[i];
            if (arg == null) {
                if (params[i] != null)
                    return false;
            } else {
                if (params[i] != getClassWithNullAndWrapper(arg))
                    return false;
            }
        }

        return true;
    }

    /**
     * Tests a call that passed no arguments against {@code params}.
     * No class is compared; the result is whether the array is empty.
     *
     * @param params the parameter classes of the candidate signature
     * @return {@code true} when {@code params} has length zero
     */
    public static boolean sameClasses(Class[] params) {
        return params.length == 0;
    }

    /**
     * Tests a one-argument call. {@code params} must have length one, and its
     * only class must be identical to the class of {@code arg1}. A null argument
     * matches a null parameter class, and a {@link Wrapper} is represented by
     * {@link Wrapper#getType()}.
     *
     * @param params the parameter classes; the length must be one
     * @param arg1 the sole argument, or null
     * @return {@code true} when the length is one and the class matches by identity
     */
    public static boolean sameClasses(Class[] params, Object arg1) {
        if (params.length != 1)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;

        return true;
    }

    /**
     * Tests a two-argument call. {@code params} must have length two, and each
     * class must be identical to the class of the argument in that position.
     * A null argument matches a null parameter class, and a {@link Wrapper} is
     * represented by {@link Wrapper#getType()}.
     *
     * @param params the parameter classes; the length must be two
     * @param arg1 the first argument, or null
     * @param arg2 the second argument, or null
     * @return {@code true} when the length is two and both classes match by identity
     */
    public static boolean sameClasses(Class[] params, Object arg1, Object arg2) {
        if (params.length != 2)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;
        return params[1] == getClassWithNullAndWrapper(arg2);
    }

    /**
     * Tests a three-argument call. {@code params} must have length three, and
     * each class must be identical to the class of the argument in that position.
     * A null argument matches a null parameter class, and a {@link Wrapper} is
     * represented by {@link Wrapper#getType()}.
     *
     * @param params the parameter classes; the length must be three
     * @param arg1 the first argument, or null
     * @param arg2 the second argument, or null
     * @param arg3 the third argument, or null
     * @return {@code true} when the length is three and every class matches by identity
     */
    public static boolean sameClasses(Class[] params, Object arg1, Object arg2, Object arg3) {
        if (params.length != 3)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;
        if (params[1] != getClassWithNullAndWrapper(arg2)) return false;
        return params[2] == getClassWithNullAndWrapper(arg3);
    }

    /**
     * Tests a four-argument call. {@code params} must have length four, and
     * each class must be identical to the class of the argument in that position.
     * A null argument matches a null parameter class, and a {@link Wrapper} is
     * represented by {@link Wrapper#getType()}.
     *
     * @param params the parameter classes; the length must be four
     * @param arg1 the first argument, or null
     * @param arg2 the second argument, or null
     * @param arg3 the third argument, or null
     * @param arg4 the fourth argument, or null
     * @return {@code true} when the length is four and every class matches by identity
     */
    public static boolean sameClasses(Class[] params, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (params.length != 4)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;
        if (params[1] != getClassWithNullAndWrapper(arg2)) return false;
        if (params[2] != getClassWithNullAndWrapper(arg3)) return false;
        return params[3] == getClassWithNullAndWrapper(arg4);
    }

    /**
     * Compares {@code params[0]} with the class of {@code arg}.
     * The length of {@code params} is ignored; index zero is the slot that is read.
     * A null argument matches a null slot, and a {@link Wrapper} is represented
     * by {@link Wrapper#getType()}.
     *
     * @param params the parameter classes; index zero is read
     * @param arg the argument to compare, or null
     * @return {@code true} when the first parameter class is identical to the class of {@code arg}
     */
    public static boolean sameClass(Class[] params, Object arg) {
        return params[0] == getClassWithNullAndWrapper(arg);
    }

    /**
     * @param arguments an array of classes, values, or nulls
     */
    public static Class[] castArgumentsToClassArray(final Object[] arguments) {
        if (arguments == null || arguments.length == 0) return EMPTY_CLASS_ARRAY;
        Class[] types = new Class[arguments.length];
        for (int i = 0; i < arguments.length; ++i) {
            Object argument = arguments[i];
            if (argument instanceof Class) {
                types[i] = (Class<?>) argument;
            } else if (argument != null) {
                types[i] = argument.getClass();
            } else {
                types[i] = null;
            }
        }
        return types;
    }

    /**
     * Replaces each {@link Wrapper} in {@code arguments} with
     * {@link Wrapper#unwrap()}. Slots that are not wrappers are left as they are.
     * The array is updated in place.
     *
     * @param arguments the argument array to edit
     */
    public static void unwrap(Object[] arguments) {
        //
        // Temp code to ignore wrapped parameters
        // The New MOP will deal with these properly
        //
        for (int i = 0; i != arguments.length; i++) {
            if (arguments[i] instanceof Wrapper) {
                arguments[i] = ((Wrapper) arguments[i]).unwrap();
            }
        }
    }

    /**
     * Re-normalizes a reflectively boxed primitive return value through the
     * {@code valueOf} caches. {@link Method#invoke} boxes
     * primitive returns with fresh instances, unlike MethodHandle-based and
     * generated-bytecode invocation, which box through the caches; without
     * normalization, reference identity ({@code ===}) of primitive returns
     * would differ between dispatch paths (GROOVY-12140).
     *
     * @param value the reflectively boxed return value (may be {@code null})
     * @param returnType the declared return type of the invoked method
     * @return the cache-normalized value
     * @since 6.0.0
     */
    public static Object normalizeBoxedReturn(final Object value, final Class<?> returnType) {
        if (value == null || !returnType.isPrimitive()) return value; // null includes void
        if (returnType == int.class) return internInt((Integer) value);
        if (returnType == boolean.class) return internBoolean((Boolean) value);
        if (returnType == long.class) return internLong((Long) value);
        if (returnType == char.class) return internChar((Character) value);
        if (returnType == byte.class) return internByte((Byte) value);
        if (returnType == short.class) return internShort((Short) value);
        return value; // float/double: valueOf does not cache on any path
    }

    private static Integer internInt(final int value) {
        return Integer.valueOf(value);
    }

    private static Boolean internBoolean(final boolean value) {
        return Boolean.valueOf(value);
    }

    private static Long internLong(final long value) {
        return Long.valueOf(value);
    }

    private static Character internChar(final char value) {
        return Character.valueOf(value);
    }

    private static Byte internByte(final byte value) {
        return Byte.valueOf(value);
    }

    private static Short internShort(final short value) {
        return Short.valueOf(value);
    }

    /**
     * Sets the metaclass for an object, by delegating to the appropriate
     * {@link DefaultGroovyMethods} helper method. This method was introduced as
     * a breaking change in 2.0 to solve rare cases of stack overflow. See GROOVY-5285.
     * <p>
     * The method is named doSetMetaClass in order to prevent misuses. Do not use
     * this method directly unless you know what you do.
     *
     * @param self the object for which to set the metaclass
     * @param mc the metaclass
     */
    public static void doSetMetaClass(Object self, MetaClass mc) {
        if (self instanceof GroovyObject) {
            DefaultGroovyMethods.setMetaClass((GroovyObject)self, mc);
        } else {
            DefaultGroovyMethods.setMetaClass(self, mc);
        }
    }

    /**
     * Converts a String into a standard property name.
     *
     * @param prop the original name
     * @return the converted name
     */
    public static String convertPropertyName(String prop) {
        if (Character.isDigit(prop.charAt(0))) {
            return prop;
        }
        return BeanUtils.decapitalize(prop);
    }
}
