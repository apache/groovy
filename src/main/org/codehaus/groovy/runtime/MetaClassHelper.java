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

package org.codehaus.groovy.runtime;

import groovy.lang.*;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.reflection.CachedConstructor;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author John Wilson
 * @author Jochen Theodorou
 */
public class MetaClassHelper {

    public static final Object[] EMPTY_ARRAY = {};
    public static final Class[] EMPTY_TYPE_ARRAY = {};
    protected static final Object[] ARRAY_WITH_NULL = {null};
    protected static final Logger LOG = Logger.getLogger(MetaClassHelper.class.getName());
    private static final int MAX_ARG_LEN = 12;

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
                accessible = isAccessible.booleanValue();
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
                accessible = isAccessible.booleanValue();
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
                ret[i] = new Boolean(array[i]);
            }
        } else if (componentType == char.class) {
            char[] array = (char[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = new Character(array[i]);
            }
        } else if (componentType == byte.class) {
            byte[] array = (byte[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = new Byte(array[i]);
            }
        } else if (componentType == int.class) {
            int[] array = (int[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = new Integer(array[i]);
            }
        } else if (componentType == short.class) {
            short[] array = (short[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = new Short(array[i]);
            }
        } else if (componentType == long.class) {
            long[] array = (long[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = new Long(array[i]);
            }
        } else if (componentType == double.class) {
            double[] array = (double[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = new Double(array[i]);
            }
        } else if (componentType == float.class) {
            float[] array = (float[]) parameters;
            ret = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                ret[i] = new Float(array[i]);
            }
        }

        return ret;
    }


    /**
     * @param list the original list
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
                    Array.setInt(objArray, i, ((Integer) obj).intValue());
                } else if (obj instanceof Double) {
                    Array.setDouble(objArray, i, ((Double) obj).doubleValue());
                } else if (obj instanceof Boolean) {
                    Array.setBoolean(objArray, i, ((Boolean) obj).booleanValue());
                } else if (obj instanceof Long) {
                    Array.setLong(objArray, i, ((Long) obj).longValue());
                } else if (obj instanceof Float) {
                    Array.setFloat(objArray, i, ((Float) obj).floatValue());
                } else if (obj instanceof Character) {
                    Array.setChar(objArray, i, ((Character) obj).charValue());
                } else if (obj instanceof Byte) {
                    Array.setByte(objArray, i, ((Byte) obj).byteValue());
                } else if (obj instanceof Short) {
                    Array.setShort(objArray, i, ((Short) obj).shortValue());
                }
            } else {
                Array.set(objArray, i, obj);
            }
        }
        return objArray;
    }

    private static final Class[] PRIMITIVES = {
            byte.class, Byte.class, short.class, Short.class,
            int.class, Integer.class, long.class, Long.class,
            BigInteger.class, float.class, Float.class,
            double.class, Double.class, BigDecimal.class,
            Number.class, Object.class
    };
    private static final int[][] PRIMITIVE_DISTANCE_TABLE = {
            //              byte    Byte    short   Short   int     Integer     long    Long    BigInteger  float   Float   double  Double  BigDecimal, Number, Object
            /* byte*/{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,},
            /*Byte*/{1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,},
            /*short*/{14, 15, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,},
            /*Short*/{14, 15, 1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,},
            /*int*/{14, 15, 12, 13, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,},
            /*Integer*/{14, 15, 12, 13, 1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,},
            /*long*/{14, 15, 12, 13, 10, 11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,},
            /*Long*/{14, 15, 12, 13, 10, 11, 1, 0, 2, 3, 4, 5, 6, 7, 8, 9,},
            /*BigInteger*/{14, 15, 12, 13, 10, 11, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7,},
            /*float*/{14, 15, 12, 13, 10, 11, 8, 9, 7, 0, 1, 2, 3, 4, 5, 6,},
            /*Float*/{14, 15, 12, 13, 10, 11, 8, 9, 7, 1, 0, 2, 3, 4, 5, 6,},
            /*double*/{14, 15, 12, 13, 10, 11, 8, 9, 7, 5, 6, 0, 1, 2, 3, 4,},
            /*Double*/{14, 15, 12, 13, 10, 11, 8, 9, 7, 5, 6, 1, 0, 2, 3, 4,},
            /*BigDecimal*/{14, 15, 12, 13, 10, 11, 8, 9, 7, 5, 6, 3, 4, 0, 1, 2,},
            /*Numer*/{14, 15, 12, 13, 10, 11, 8, 9, 7, 5, 6, 3, 4, 2, 0, 1,},
            /*Object*/{14, 15, 12, 13, 10, 11, 8, 9, 7, 5, 6, 3, 4, 2, 1, 0,},
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
        if (c == null || c == interfaceClass) return 0;
        Class[] interfaces = c.getInterfaces();
        int max = 0;
        for (int i = 0; i < interfaces.length; i++) {
            int sub = 1 + getMaximumInterfaceDistance(interfaces[i], interfaceClass);
            max = Math.max(max, sub);
        }
        return Math.max(max, getMaximumInterfaceDistance(c.getSuperclass(), interfaceClass));
    }

    public static long calculateParameterDistance(Class[] arguments, Class[] parameters) {
        int objectDistance = 0, interfaceDistance = 0;
        for (int i = 0; i < arguments.length; i++) {
            if (parameters[i] == arguments[i]) continue;

            if (parameters[i].isInterface()) {
                objectDistance += PRIMITIVES.length;
                interfaceDistance += getMaximumInterfaceDistance(arguments[i], parameters[i]);
                continue;
            }

            if (arguments[i] != null) {
                int pd = getPrimitiveDistance(parameters[i], arguments[i]);
                if (pd != -1) {
                    objectDistance += pd;
                    continue;
                }

                // add one to dist to be sure interfaces are prefered
                objectDistance += PRIMITIVES.length + 1;
                Class clazz = ReflectionCache.autoboxType(arguments[i]);
                while (clazz != null) {
                    if (clazz == parameters[i]) break;
                    if (clazz == GString.class && parameters[i] == String.class) {
                        objectDistance += 2;
                        break;
                    }
                    clazz = clazz.getSuperclass();
                    objectDistance += 3;
                }
            } else {
                // choose the distance to Object if a parameter is null
                // this will mean that Object is prefered over a more
                // specific type
                // remove one to dist to be sure Object is prefered
                objectDistance--;
                Class clazz = parameters[i];
                if (clazz.isPrimitive()) {
                    objectDistance += 2;
                } else {
                    while (clazz != Object.class) {
                        clazz = clazz.getSuperclass();
                        objectDistance += 2;
                    }
                }
            }
        }
        long ret = objectDistance;
        ret <<= 32;
        ret |= interfaceDistance;
        return ret;
    }

    public static String capitalize(String property) {
        return property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
    }

    /**
     * @return the method with 1 parameter which takes the most general type of
     *         object (e.g. Object)
     */
    public static Object chooseEmptyMethodParams(List methods) {
        Object vargsMethod = null;
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            final ParameterTypes pt = getParameterTypes(method);
            CachedClass[] paramTypes = pt.getParameterTypes();
            int paramLength = paramTypes.length;
            if (paramLength == 0) {
                return method;
            } else if (paramLength == 1 && isVargsMethod(pt, EMPTY_ARRAY)) {
                vargsMethod = method;
            }
        }
        return vargsMethod;
    }

    /**
     * @return the method with 1 parameter which takes the most general type of
     *         object (e.g. Object) ignoring primitve types
     */
    public static Object chooseMostGeneralMethodWith1NullParam(List methods) {
        // lets look for methods with 1 argument which matches the type of the
        // arguments
        Class closestClass = null;
        Class closestVargsClass = null;
        Object answer = null;
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            final ParameterTypes pt = getParameterTypes(method);
            CachedClass[] paramTypes = pt.getParameterTypes();
            int paramLength = paramTypes.length;
            if (paramLength == 0 || paramLength > 2) continue;

            CachedClass theType = paramTypes[0];
            if (theType.isPrimitive) continue;

            if (paramLength == 2) {
                if (!isVargsMethod(pt, ARRAY_WITH_NULL)) continue;
                if (closestClass == theType.getCachedClass()) {
                    if (closestVargsClass == null) continue;
                    Class newVargsClass = paramTypes[1].getCachedClass();
                    if (closestVargsClass == null || isAssignableFrom(newVargsClass, closestVargsClass)) {
                        closestVargsClass = newVargsClass;
                        answer = method;
                    }
                } else if (closestClass == null || isAssignableFrom(theType.getCachedClass(), closestClass)) {
                    closestVargsClass = paramTypes[1].getCachedClass();
                    closestClass = theType.getCachedClass();
                    answer = method;
                }
            } else {
                if (closestClass == null || isAssignableFrom(theType.getCachedClass(), closestClass)) {
                    closestVargsClass = null;
                    closestClass = theType.getCachedClass();
                    answer = method;
                }
            }
        }
        return answer;
    }

    /**
     * Coerces a GString instance into String if needed
     *
     * @return the coerced argument
     */
    protected static Object coerceGString(Object argument, CachedClass clazz) {
        if (clazz.getCachedClass() != String.class) return argument;
        if (!(argument instanceof GString)) return argument;
        return argument.toString();
    }

    // PRECONDITION:
    //   !ReflectionCache.isAssignableFrom(parameterType, argument.getClass())
    protected static Object coerceNumber(Object argument, CachedClass cachedParam) {
        if (argument instanceof Number && (cachedParam.isNumber || cachedParam.isPrimitive)) { // Number types
            Object oldArgument = argument;
            boolean wasDouble = false;
            boolean wasFloat = false;
            Class param = cachedParam.getCachedClass();
            if (param == Byte.class || param == Byte.TYPE) {
                argument = new Byte(((Number) argument).byteValue());
            } else if (param == Double.class || param == Double.TYPE) {
                wasDouble = true;
                argument = new Double(((Number) argument).doubleValue());
            } else if (param == Float.class || param == Float.TYPE) {
                wasFloat = true;
                argument = new Float(((Number) argument).floatValue());
            } else if (param == Integer.class || param == Integer.TYPE) {
                argument = new Integer(((Number) argument).intValue());
            } else if (param == Long.class || param == Long.TYPE) {
                argument = new Long(((Number) argument).longValue());
            } else if (param == Short.class || param == Short.TYPE) {
                argument = new Short(((Number) argument).shortValue());
            } else if (param == BigDecimal.class) {
                argument = new BigDecimal(String.valueOf((Number) argument));
            } else if (param == BigInteger.class) {
                argument = new BigInteger(String.valueOf((Number) argument));
            }

            if (oldArgument instanceof BigDecimal) {
                BigDecimal oldbd = (BigDecimal) oldArgument;
                boolean throwException = false;
                if (wasDouble) {
                    Double d = (Double) argument;
                    if (d.isInfinite()) throwException = true;
                } else if (wasFloat) {
                    Float f = (Float) argument;
                    if (f.isInfinite()) throwException = true;
                } else {
                    BigDecimal newbd = new BigDecimal(String.valueOf((Number) argument));
                    throwException = !oldArgument.equals(newbd);
                }

                if (throwException)
                    throw new IllegalArgumentException(param + " out of range while converting from BigDecimal");
            }

        }
        return argument;
    }

    protected static Object coerceArray(Object argument, CachedClass param) {
        if (!param.isArray) return argument;
        Class argumentClass = argument.getClass();
        if (!argumentClass.isArray()) return argument;

        Class paramComponent = param.getCachedClass().getComponentType();
        if (paramComponent.isPrimitive()) {
            if (paramComponent == boolean.class && argumentClass == Boolean[].class) {
                argument = DefaultTypeTransformation.convertToBooleanArray(argument);
            } else if (paramComponent == byte.class && argumentClass == Byte[].class) {
                argument = DefaultTypeTransformation.convertToByteArray(argument);
            } else if (paramComponent == char.class && argumentClass == Character[].class) {
                argument = DefaultTypeTransformation.convertToCharArray(argument);
            } else if (paramComponent == short.class && argumentClass == Short[].class) {
                argument = DefaultTypeTransformation.convertToShortArray(argument);
            } else if (paramComponent == int.class && argumentClass == Integer[].class) {
                argument = DefaultTypeTransformation.convertToIntArray(argument);
            } else if (paramComponent == long.class &&
                    (argumentClass == Long[].class || argumentClass == Integer[].class)) {
                argument = DefaultTypeTransformation.convertToLongArray(argument);
            } else if (paramComponent == float.class &&
                    (argumentClass == Float[].class || argumentClass == Integer[].class)) {
                argument = DefaultTypeTransformation.convertToFloatArray(argument);
            } else if (paramComponent == double.class &&
                    (argumentClass == Double[].class || argumentClass == Float[].class
                            || BigDecimal[].class.isAssignableFrom(argumentClass))) {
                argument = DefaultTypeTransformation.convertToDoubleArray(argument);
            }
        } else if (paramComponent == String.class && argument instanceof GString[]) {
            GString[] strings = (GString[]) argument;
            String[] ret = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                ret[i] = strings[i].toString();
            }
            argument = ret;
        }
        return argument;
    }

    /**
     * @return true if a method of the same matching prototype was found in the
     *         list
     */
    public static boolean containsMatchingMethod(List list, MetaMethod method) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            MetaMethod aMethod = (MetaMethod) iter.next();
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
     * @param args
     */
    public static Class[] convertToTypeArray(Object[] args) {
        if (args == null)
            return null;
        int s = args.length;
        Class[] ans = new Class[s];
        for (int i = 0; i < s; i++) {
            Object o = args[i];
            if (o == null) {
                ans[i] = null;
            } else if (o instanceof Wrapper) {
                ans[i] = ((Wrapper) o).getType();
            } else {
                ans[i] = o.getClass();
            }
        }
        return ans;
    }

    public static Object doConstructorInvoke(CachedConstructor constructor, Object[] argumentArray) {
        final Constructor constr = constructor.cachedConstructor;
        if (LOG.isLoggable(Level.FINER)) {
            logMethodCall(constr.getDeclaringClass(), constr.getName(), argumentArray);
        }
        argumentArray = coerceArgumentsToClasses(argumentArray, constructor);
        try {
            return constr.newInstance(argumentArray);
        } catch (InvocationTargetException e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalArgumentException e) {
            throw createExceptionText("failed to invoke constructor: ", constr, argumentArray, e, false);
        } catch (IllegalAccessException e) {
            throw createExceptionText("could not access constructor: ", constr, argumentArray, e, false);
        } catch (Exception e) {
            throw createExceptionText("failed to invoke constructor: ", constr, argumentArray, e, true);
        }
    }

    private static GroovyRuntimeException createExceptionText(String init, Constructor constructor, Object[] argumentArray, Throwable e, boolean setReason) {
        throw new GroovyRuntimeException(
                init
                        + constructor
                        + " with arguments: "
                        + InvokerHelper.toString(argumentArray)
                        + " reason: "
                        + e,
                setReason ? e : null);
    }

    public static Object[] coerceArgumentsToClasses(Object[] argumentArray, ParameterTypes pt) {
        CachedClass paramTypes[] = pt.getParameterTypes();
        // correct argumentArray's length
        if (argumentArray == null) {
            argumentArray = EMPTY_ARRAY;
        } else if (paramTypes.length == 1 && argumentArray.length == 0) {
            if (isVargsMethod(pt, argumentArray))
                argumentArray = new Object[]{Array.newInstance(paramTypes[0].getCachedClass().getComponentType(), 0)};
            else
                argumentArray = ARRAY_WITH_NULL;
        } else if (isVargsMethod(pt, argumentArray)) {
            argumentArray = fitToVargs(argumentArray, paramTypes);
        }

        //correct Type
        for (int i = 0; i < argumentArray.length; i++) {
            Object argument = argumentArray[i];
            if (argument == null) continue;
            CachedClass parameterType = paramTypes[i];
            if (ReflectionCache.isAssignableFrom(parameterType.getCachedClass(), argument.getClass())) continue;

            argument = coerceGString(argument, parameterType);
            argument = coerceNumber(argument, parameterType);
            argument = coerceArray(argument, parameterType);
            argumentArray[i] = argument;
        }
        return argumentArray;
    }

    private static Object makeCommonArray(Object[] arguments, int offset, Class fallback) {
        // arguments.leght>0 && !=null
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
        Object result = makeArray(null, baseClass, arguments.length - offset);
        System.arraycopy(arguments, offset, result, 0, arguments.length - offset);
        return result;
    }

    private static Object makeArray(Object obj, Class secondary, int length) {
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
     * this method is called when the number of arguments to a method is greater than 1
     * and if the method is a vargs method. This method will then transform the given
     * arguments to make the method callable
     *
     * @param argumentArray the arguments used to call the method
     * @param paramTypes    the types of the paramters the method takes
     */
    private static Object[] fitToVargs(Object[] argumentArray, CachedClass[] paramTypes) {
        Class vargsClass = ReflectionCache.autoboxType(paramTypes[paramTypes.length - 1].getCachedClass().getComponentType());

        if (argumentArray.length == paramTypes.length - 1) {
            // the vargs argument is missing, so fill it with an empty array
            Object[] newArgs = new Object[paramTypes.length];
            System.arraycopy(argumentArray, 0, newArgs, 0, argumentArray.length);
            Object vargs = makeArray(null, vargsClass, 0);
            newArgs[newArgs.length - 1] = vargs;
            return newArgs;
        } else if (argumentArray.length == paramTypes.length) {
            // the number of arguments is correct, but if the last argument
            // is no array we have to wrap it in a array. if the last argument
            // is null, then we don't have to do anything
            Object lastArgument = argumentArray[argumentArray.length - 1];
            if (lastArgument != null && !lastArgument.getClass().isArray()) {
                // no array so wrap it
                Object vargs = makeArray(lastArgument, vargsClass, 1);
                System.arraycopy(argumentArray, argumentArray.length - 1, vargs, 0, 1);
                argumentArray[argumentArray.length - 1] = vargs;
                return argumentArray;
            } else {
                // we may have to box the arguemnt!
                return argumentArray;
            }
        } else if (argumentArray.length > paramTypes.length) {
            // the number of arguments is too big, wrap all exceeding elements
            // in an array, but keep the old elements that are no vargs
            Object[] newArgs = new Object[paramTypes.length];
            // copy arguments that are not a varg
            System.arraycopy(argumentArray, 0, newArgs, 0, paramTypes.length - 1);
            // create a new array for the vargs and copy them
            int numberOfVargs = argumentArray.length - paramTypes.length;
            Object vargs = makeCommonArray(argumentArray, paramTypes.length - 1, vargsClass);
            newArgs[newArgs.length - 1] = vargs;
            return newArgs;
        } else {
            throw new GroovyBugError("trying to call a vargs method without enough arguments");
        }
    }

    private static GroovyRuntimeException createExceptionText(String init, MetaMethod method, Object object, Object[] args, Throwable reason, boolean setReason) {
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

    public static Object doMethodInvoke(Object object, MetaMethod method, Object[] argumentArray) {
        CachedClass[] paramTypes = method.getParameterTypes();
        argumentArray = coerceArgumentsToClasses(argumentArray, method.getParamTypes());
        try {
            return method.invoke(object, argumentArray);
        } catch (IllegalArgumentException e) {
            //TODO: test if this is ok with new MOP, should be changed!
            // we don't want the exception being unwrapped if it is a IllegalArgumentException
            // but in the case it is for example a IllegalThreadStateException, we want the unwrapping
            // from the runtime
            //Note: the reason we want unwrapping sometimes and sometimes not is that the method
            // invokation tries to invoke the method with and then reacts with type transformation
            // if the invokation failed here. This is ok for IllegalArgumentException, but it is
            // possible that a Reflector will be used to execute the call and then an Exception from inside
            // the method is not wrapped in a InvocationTargetException and we will end here.
            boolean setReason = e.getClass() != IllegalArgumentException.class;
            throw createExceptionText("failed to invoke method: ", method, object, argumentArray, e, setReason);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw createExceptionText("failed to invoke method: ", method, object, argumentArray, e, true);
        }
    }

    protected static String getClassName(Object object) {
        if (object == null) return null;
        return (object instanceof Class) ? ((Class) object).getName() : object.getClass().getName();
    }

    /**
     * Returns a callable object for the given method name on the object.
     * The object acts like a Closure in that it can be called, like a closure
     * and passed around - though really its a method pointer, not a closure per se.
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        return new MethodClosure(object, methodName);
    }

    public static ParameterTypes getParameterTypes(Object methodOrConstructor) {
        if (methodOrConstructor instanceof ParameterTypes) {
            ParameterTypes pt = (CachedConstructor) methodOrConstructor;
            return pt;
        }
        if (methodOrConstructor instanceof MetaMethod) {
            return ((MetaMethod) methodOrConstructor).getParamTypes();
        }
        if (methodOrConstructor instanceof Method) {
            Method method = (Method) methodOrConstructor;
            return ReflectionCache.getCachedMethod(method);
        }
        if (methodOrConstructor instanceof Constructor) {
            Constructor constructor = (Constructor) methodOrConstructor;
            return ReflectionCache.getCachedConstructor(constructor);
        }
        throw new IllegalArgumentException("Must be a Method or Constructor");
    }

    public static boolean isAssignableFrom(Class classToTransformTo, Class classToTransformFrom) {
        if (classToTransformFrom == null) return true;
        classToTransformTo = ReflectionCache.autoboxType(classToTransformTo);
        classToTransformFrom = ReflectionCache.autoboxType(classToTransformFrom);

        if (classToTransformTo == classToTransformFrom) {
            return true;
        }
        // note: there is not coercion for boolean and char. Range matters, precision doesn't
        else if (classToTransformTo == Integer.class) {
            if (classToTransformFrom == Integer.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == BigInteger.class)
                return true;
        } else if (classToTransformTo == Double.class) {
            if (classToTransformFrom == Double.class
                    || classToTransformFrom == Integer.class
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
                    || classToTransformFrom == BigDecimal.class
                    || classToTransformFrom == BigInteger.class)
                return true;
        } else if (classToTransformTo == BigInteger.class) {
            if (classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == BigInteger.class)
                return true;
        } else if (classToTransformTo == Long.class) {
            if (classToTransformFrom == Long.class
                    || classToTransformFrom == Integer.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == Float.class) {
            if (classToTransformFrom == Float.class
                    || classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == Short.class) {
            if (classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class)
                return true;
        } else if (classToTransformTo == String.class) {
            if (classToTransformFrom == String.class ||
                    GString.class.isAssignableFrom(classToTransformFrom)) {
                return true;
            }
        }

        return ReflectionCache.isAssignableFrom(classToTransformTo, classToTransformFrom);
    }

    public static boolean isGenericSetMethod(MetaMethod method) {
        return (method.getName().equals("set"))
                && method.getParameterTypes().length == 2;
    }

    protected static boolean isSuperclass(Class claszz, Class superclass) {
        while (claszz != null) {
            if (claszz == superclass) return true;
            claszz = claszz.getSuperclass();
        }
        return false;
    }

    public static boolean isValidMethod(ParameterTypes pt, Class[] arguments, boolean includeCoerce) {
        if (arguments == null) {
            return true;
        }
        int size = arguments.length;

        CachedClass[] paramTypes = pt.getParameterTypes();
        if ((size >= paramTypes.length || size == paramTypes.length - 1)
                && paramTypes.length > 0
                && pt.isArray(paramTypes.length - 1)) {
            // first check normal number of parameters
            for (int i = 0; i < paramTypes.length - 1; i++) {
                if (isAssignableFrom(paramTypes[i].getCachedClass(), arguments[i])) continue;
                return false;
            }
            // check varged
            Class clazz = paramTypes[paramTypes.length - 1].getCachedClass().getComponentType();
            for (int i = paramTypes.length; i < size; i++) {
                if (isAssignableFrom(clazz, arguments[i])) continue;
                return false;
            }
            return true;
        } else if (paramTypes.length == size) {
            // lets check the parameter types match
            for (int i = 0; i < size; i++) {
                if (isAssignableFrom(paramTypes[i].getCachedClass(), arguments[i])) continue;
                return false;
            }
            return true;
        } else if (paramTypes.length == 1 && size == 0) {
            return true;
        }
        return false;

    }

    public static boolean isValidMethod(Object method, Class[] arguments, boolean includeCoerce) {
        return isValidMethod(getParameterTypes(method), arguments, includeCoerce);
    }

    public static boolean isVargsMethod(ParameterTypes pt, Object[] arguments) {
        CachedClass paramTypes[] = pt.getParameterTypes();
        if (paramTypes.length == 0) return false;
        if (!pt.isArray(paramTypes.length - 1)) return false;
        // -1 because the varg part is optional
        if (paramTypes.length - 1 == arguments.length) return true;
        if (paramTypes.length - 1 > arguments.length) return false;
        if (arguments.length > paramTypes.length) return true;

        // only case left is arguments.length == parameterTypes.length
        Object last = arguments[arguments.length - 1];
        if (last == null) return true;
        Class clazz = last.getClass();
        return !clazz.equals(paramTypes[paramTypes.length - 1]);

    }

    public static void logMethodCall(Object object, String methodName, Object[] arguments) {
        String className = getClassName(object);
        String logname = "methodCalls." + className + "." + methodName;
        Logger objLog = Logger.getLogger(logname);
        if (!objLog.isLoggable(Level.FINER)) return;
        StringBuffer msg = new StringBuffer(methodName);
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

    public static boolean parametersAreCompatible(Class[] arguments, Class[] parameters) {
        if (arguments.length != parameters.length) return false;
        for (int i = 0; i < arguments.length; i++) {
            if (!isAssignableFrom(parameters[i], arguments[i])) return false;
        }
        return true;
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
}
