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
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.wrappers.Wrapper;
import org.codehaus.groovy.util.FastArray;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetaClassHelper {

    public static final Object[] EMPTY_ARRAY = {};
    public static final Class[] EMPTY_TYPE_ARRAY = {};
    public static final Object[] ARRAY_WITH_NULL = {null};
    protected static final Logger LOG = Logger.getLogger(MetaClassHelper.class.getName());
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

    public static final Class[] EMPTY_CLASS_ARRAY = new Class[0];

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
            /*BigInteger[12]*/  {18, 19,  9, 10,  7,  8, 16, 17,  5,  6,  3,  4,  0, 14, 15, 12, 13, 11,  1,  2,},
            /*float[13]*/       {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  0,  1,  2,  3,  4,  5,  6,},
            /*Float[14]*/       {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  1,  0,  2,  3,  4,  5,  6,},
            /*double[15]*/      {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5,  6,  0,  1,  2,  3,  4,},
            /*Double[16]*/      {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5,  6,  1,  0,  2,  3,  4,},
            /*BigDecimal[17]*/  {18, 19, 14, 15, 12, 13, 16, 17, 10, 11,  8,  9,  7,  5,  6,  3,  4,  0,  1,  2,},
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
            if (sub != -1) sub++;
            // we are interested in the longest path only
            max = Math.max(max, sub);
        }
        // we do not add one for super classes, only for interfaces
        int superClassMax = getMaximumInterfaceDistance(c.getSuperclass(), interfaceClass);
        if (superClassMax != -1) superClassMax++;
        return Math.max(max, superClassMax);
    }

    private static long calculateParameterDistance(Class argument, CachedClass parameter) {
        /**
         * note: when shifting with 32 bit, you should only shift on a long. If you do
         *       that with an int, then i==(i<<32), which means you loose the shift
         *       information
         */

        if (parameter.getTheClass() == argument) return 0;

        if (parameter.isInterface()) {
            int dist = getMaximumInterfaceDistance(argument, parameter.getTheClass()) << INTERFACE_SHIFT;
            if (dist>-1 || !(argument!=null && Closure.class.isAssignableFrom(argument))) {
                return dist;
            } // else go to object case
        }

        long objectDistance = 0;
        if (argument != null) {
            long pd = getPrimitiveDistance(parameter.getTheClass(), argument);
            if (pd != -1) return pd << PRIMITIVE_SHIFT;

            // add one to dist to be sure interfaces are preferred
            objectDistance += PRIMITIVES.length + 1;

            // GROOVY-5114 : if we have to choose between two methods
            // foo(Object[]) and foo(Object) and that the argument is an array type
            // then the array version should be preferred
            if (argument.isArray() && !parameter.isArray) {
                objectDistance+=4;
            }
            Class clazz = ReflectionCache.autoboxType(argument);
            while (clazz != null) {
                if (clazz == parameter.getTheClass()) break;
                if (clazz == GString.class && parameter.getTheClass() == String.class) {
                    objectDistance += 2;
                    break;
                }
                clazz = clazz.getSuperclass();
                objectDistance += 3;
            }
        } else {
            // choose the distance to Object if a parameter is null
            // this will mean that Object is preferred over a more
            // specific type
            Class clazz = parameter.getTheClass();
            if (clazz.isPrimitive()) {
                objectDistance += 2;
            } else {
                while (clazz != Object.class && clazz != null) {
                    clazz = clazz.getSuperclass();
                    objectDistance += 2;
                }
            }
        }
        return objectDistance << OBJECT_SHIFT;
    }

    public static long calculateParameterDistance(Class[] arguments, ParameterTypes pt) {
        CachedClass[] parameters = pt.getParameterTypes();
        if (parameters.length == 0) return 0;

        long ret = 0;
        int noVargsLength = parameters.length - 1;

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

        /**
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

        if (arguments.length == parameters.length) {
            // case C&D, we use baseType to calculate and set it
            // to the value we need according to case C and D
            CachedClass baseType = parameters[noVargsLength]; // case C
            if (!parameters[noVargsLength].isAssignableFrom(arguments[noVargsLength])) {
                baseType = ReflectionCache.getCachedClass(baseType.getTheClass().getComponentType()); // case D
                ret += 2L << VARGS_SHIFT; // penalty for vargs
            }
            ret += calculateParameterDistance(arguments[noVargsLength], baseType);
        } else if (arguments.length > parameters.length) {
            // case B
            // we give our a vargs penalty for each exceeding argument and iterate
            // by using parameters[noVargsLength].getComponentType()
            ret += (2L + arguments.length - parameters.length) << VARGS_SHIFT; // penalty for vargs
            CachedClass vargsType = ReflectionCache.getCachedClass(parameters[noVargsLength].getTheClass().getComponentType());
            for (int i = noVargsLength; i < arguments.length; i++) {
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
     * This is the complement to the java.beans.Introspector.decapitalize(String) method.
     * We handle names that begin with an initial lowerCase followed by upperCase specially
     * (which is to make no change).
     * See GROOVY-3211.
     *
     * @param property the property name to capitalize
     * @return the name capitalized, except when we don't
     */
    public static String capitalize(final String property) {
        final String rest = property.substring(1);

        // Funky rule so that names like 'pNAME' will still work.
        if (Character.isLowerCase(property.charAt(0)) && (rest.length() > 0) && Character.isUpperCase(rest.charAt(0))) {
            return property;
        }

        return property.substring(0, 1).toUpperCase() + rest;
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
        for (int i = 0; i != len; ++i) {
            final Object[] data = methods.getArray();
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
     * param instance array to the type array
     *
     * @param args the arguments
     * @return the types of the arguments
     */
    public static Class[] convertToTypeArray(Object[] args) {
        if (args == null)
            return null;
        int s = args.length;
        Class[] ans = new Class[s];
        for (int i = 0; i < s; i++) {
            Object o = args[i];
            ans[i] = getClassWithNullAndWrapper(o);
        }
        return ans;
    }

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

    public static GroovyRuntimeException createExceptionText(String init, MetaMethod method, Object object, Object[] args, Throwable reason, boolean setReason) {
        return new GroovyRuntimeException(
                init
                        + method
                        + " on: "
                        + object
                        + " with arguments: "
                        + InvokerHelper.toString(args)
                        + " reason: "
                        + reason,
                setReason ? reason : null);
    }

    protected static String getClassName(Object object) {
        if (object == null) return null;
        return (object instanceof Class) ? ((Class) object).getName() : object.getClass().getName();
    }

    /**
     * Returns a callable object for the given method name on the object.
     * The object acts like a Closure in that it can be called, like a closure
     * and passed around - though really its a method pointer, not a closure per se.
     *
     * @param object     the object containing the method
     * @param methodName the method of interest
     * @return the resulting closure-like method pointer
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        return new MethodClosure(object, methodName);
    }

    public static boolean isAssignableFrom(Class classToTransformTo, Class classToTransformFrom) {
        if (classToTransformTo == classToTransformFrom
                || classToTransformFrom == null
                || classToTransformTo == Object.class) {
            return true;
        }

        classToTransformTo = ReflectionCache.autoboxType(classToTransformTo);
        classToTransformFrom = ReflectionCache.autoboxType(classToTransformFrom);
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
            if (classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == Long.class) {
            if (classToTransformFrom == Integer.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == Float.class) {
            if (classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == Short.class) {
            if (classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == String.class) {
            if (GString.class.isAssignableFrom(classToTransformFrom)) {
                return true;
            }
        }

        return ReflectionCache.isAssignableFrom(classToTransformTo, classToTransformFrom);
    }

    public static boolean isGenericSetMethod(MetaMethod method) {
        return (method.getName().equals("set"))
                && method.getParameterTypes().length == 2;
    }

    protected static boolean isSuperclass(Class clazz, Class superclass) {
        while (clazz != null) {
            if (clazz == superclass) return true;
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public static boolean parametersAreCompatible(Class[] arguments, Class[] parameters) {
        if (arguments.length != parameters.length) return false;
        for (int i = 0; i < arguments.length; i++) {
            if (!isAssignableFrom(parameters[i], arguments[i])) return false;
        }
        return true;
    }

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

    protected static String normalizedValue(Object argument) {
        String value;
        try {
            value = argument.toString();
            if (value.length() > MAX_ARG_LEN) {
                value = value.substring(0, MAX_ARG_LEN - 2) + "..";
            }
            if (argument instanceof String) {
                value = "\'" + value + "\'";
            }
        } catch (Exception e) {
            value = shortName(argument);
        }
        return value;
    }

    protected static String shortName(Object object) {
        if (object == null || object.getClass() == null) return "unknownClass";
        String name = getClassName(object);
        if (name == null) return "unknownClassName"; // *very* defensive...
        int lastDotPos = name.lastIndexOf('.');
        if (lastDotPos < 0 || lastDotPos >= name.length() - 1) return name;
        return name.substring(lastDotPos + 1);
    }

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
        if (arg instanceof Wrapper) {
            Wrapper w = (Wrapper) arg;
            return w.getType();
        }
        return arg.getClass();
    }

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

    public static boolean sameClasses(Class[] params) {
        return params.length == 0;
    }

    public static boolean sameClasses(Class[] params, Object arg1) {
        if (params.length != 1)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;

        return true;
    }

    public static boolean sameClasses(Class[] params, Object arg1, Object arg2) {
        if (params.length != 2)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;
        return params[1] == getClassWithNullAndWrapper(arg2);
    }

    public static boolean sameClasses(Class[] params, Object arg1, Object arg2, Object arg3) {
        if (params.length != 3)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;
        if (params[1] != getClassWithNullAndWrapper(arg2)) return false;
        return params[2] == getClassWithNullAndWrapper(arg3);
    }

    public static boolean sameClasses(Class[] params, Object arg1, Object arg2, Object arg3, Object arg4) {
        if (params.length != 4)
            return false;

        if (params[0] != getClassWithNullAndWrapper(arg1)) return false;
        if (params[1] != getClassWithNullAndWrapper(arg2)) return false;
        if (params[2] != getClassWithNullAndWrapper(arg3)) return false;
        return params[3] == getClassWithNullAndWrapper(arg4);
    }

    public static boolean sameClass(Class[] params, Object arg) {
        return params[0] == getClassWithNullAndWrapper(arg);

    }

    public static Class[] castArgumentsToClassArray(Object[] argTypes) {
        if (argTypes == null) return EMPTY_CLASS_ARRAY;
        Class[] classes = new Class[argTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Object argType = argTypes[i];
            if (argType instanceof Class) {
                classes[i] = (Class) argType;
            } else if (argType == null) {
                classes[i] = null;
            } else {
//                throw new IllegalArgumentException("Arguments to method [respondsTo] must be of type java.lang.Class!");
                classes[i] = argType.getClass();
            }
        }
        return classes;
    }

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
     * Sets the meta class for an object, by delegating to the appropriate
     * {@link DefaultGroovyMethods} helper method. This method was introduced as
     * a breaking change in 2.0 to solve rare cases of stack overflow. See GROOVY-5285.
     *
     * The method is named doSetMetaClass in order to prevent misusages. Do not use
     * this method directly unless you know what you do.
     *
     * @param self the object for which to set the meta class
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
        return java.beans.Introspector.decapitalize(prop);
    }
}
