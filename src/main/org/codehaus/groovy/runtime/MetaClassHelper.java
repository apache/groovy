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

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.*;
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
    public static final Object[] ARRAY_WITH_NULL = {null};
    protected static final Logger LOG = Logger.getLogger(MetaClassHelper.class.getName());
    private static final int MAX_ARG_LEN = 12;
    private static final int VARGS_SHIFT = 28;

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
        // -1 means a mismatch
        if (c == null) return -1;
        // 0 means a direct match
        if (c == interfaceClass) return 0;
        Class[] interfaces = c.getInterfaces();
        int max = -1;
        for (int i = 0; i < interfaces.length; i++) {
            int sub = getMaximumInterfaceDistance(interfaces[i], interfaceClass);
            // we need to keep the -1 to track the mismatch, a +1
            // by any means could let it look like a direct match
            // we want to add one, because there is an interface between
            // the interface we search for and the interface we are in.
            if (sub!=-1) sub++;
            // we are interested in the longest path only
            max = Math.max(max, sub);
        }
        // we do not add one for super classes, only for interfaces
        int superClassMax = getMaximumInterfaceDistance(c.getSuperclass(), interfaceClass);
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
            return getMaximumInterfaceDistance(argument, parameter.getTheClass())<<1;
        }

        long objectDistance = 0;
        if (argument != null) {
            long pd = getPrimitiveDistance(parameter.getTheClass(), argument);
            if (pd != -1) return pd << 32;

            // add one to dist to be sure interfaces are prefered
            objectDistance += PRIMITIVES.length + 1;
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
            // this will mean that Object is prefered over a more
            // specific type
            // remove one to dist to be sure Object is prefered
            objectDistance--;
            Class clazz = parameter.getTheClass();
            if (clazz.isPrimitive()) {
                objectDistance += 2;
            } else {
                while (clazz != Object.class) {
                    clazz = clazz.getSuperclass();
                    objectDistance += 2;
                }
            }
        }
        return objectDistance << 32;
    }

    public static long calculateParameterDistance(Class[] arguments, ParameterTypes pt) {
        CachedClass [] parameters = pt.getParameterTypes();
        if (parameters.length == 0) return 0;

        long ret = 0;        
        int noVargsLength = parameters.length-1;
        
        // if the number of parameters does not match we have 
        // a vargs usage
        //
        // case A: arguments.length<parameters.length
        //
        //         In this case arguments.length is always equal to
        //         noVargsLength because only the last parameter
        //         might be a optional vargs parameter
        //
        // case B: arguments.lenth>parameters.length
        //
        //         In this case all arguments with a index bigger than
        //         paramMinus1 are part of the vargs, so a 
        //         distance calculaion needs to be done against 
        //         parameters[noVargsLength].getComponentType()
        //
        // case C: arguments.length==parameters.length && 
        //         isAssignableFrom( parameters[noVargsLength],
        //                           arguments[noVargsLength] )
        //
        //         In this case we have no vargs, so calculate directly
        //
        // case D: arguments.length==parameters.length && 
        //         !isAssignableFrom( parameters[noVargsLength],
        //                            arguments[noVargsLength] )
        //
        //         In this case we have a vargs case again, we need 
        //         to calculate arguments[noVargsLength] against
        //         parameters[noVargsLength].getComponentType
        
        
        
        // first we calculate all arguments, that are for sure not part
        // of vargs.  Since the minimum for arguments is noVargsLength
        // we can safely iterate to this point
        for (int i = 0; i < noVargsLength; i++) {
            ret += calculateParameterDistance(arguments[i], parameters[i]);
        }
        
        if (arguments.length==parameters.length) {
            // case C&D, we use baseType to calculate and set it
            // to the value we need according to case C and D
            CachedClass baseType = parameters[noVargsLength]; // case C
            if (!parameters[noVargsLength].isAssignableFrom(arguments[noVargsLength])) {
                baseType= ReflectionCache.getCachedClass(baseType.getTheClass().getComponentType()); // case D
                ret+=2l<<VARGS_SHIFT; // penalty for vargs
            }
            ret += calculateParameterDistance(arguments[noVargsLength], baseType);
        } else if (arguments.length>parameters.length) {
            // case B
            // we give our a vargs penalty for each exceeding argument and iterate
            // by using parameters[noVargsLength].getComponentType()
            ret += (2+arguments.length-parameters.length)<<VARGS_SHIFT;
            CachedClass vargsType = ReflectionCache.getCachedClass(parameters[noVargsLength].getTheClass().getComponentType());
            for (int i = noVargsLength; i < arguments.length; i++) {
                ret += calculateParameterDistance(arguments[i], vargsType);
            }
        } else {
            // case A
            // we give a penalty for vargs, since we have no direct
            // match for the last argument
            ret+=1l<<VARGS_SHIFT;            
        }  

        return ret;
    }

    public static String capitalize(String property) {
        return property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
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
            final ParameterTypes pt = getParameterTypes(method);
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
     * @param methods the methods to choose from
     * @return the method with 1 parameter which takes the most general type of
     *         object (e.g. Object) ignoring primitve types
     */
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
            final ParameterTypes pt = getParameterTypes(method);
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
                    if (closestVargsClass == null || isAssignableFrom(newVargsClass.getTheClass(), closestVargsClass.getTheClass())) {
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
    private static int calculateSimplifiedClassDistanceToObject(Class clazz) {
        int objectDistance = 0;
        while (clazz != null) {
            clazz = clazz.getSuperclass();
            objectDistance++;
        }
        return objectDistance;
    }


    /**
     * @param list   a list of MetaMethods
     * @param method the MetaMethod of interest
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
        argumentArray = constructor.coerceArgumentsToClasses(argumentArray);
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

    public static Object doMethodInvoke(Object object, MetaMethod method, Object[] argumentArray) {
        return method.doMethodInvoke(object, argumentArray);
    }

    public static Object makeCommonArray(Object[] arguments, int offset, Class fallback) {
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
     * @param object the object containing the method
     * @param methodName the method of interest
     * @return the resulting closure-like method pointer
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        return new MethodClosure(object, methodName);
    }

    public static ParameterTypes getParameterTypes(Object methodOrConstructor) {
        if (methodOrConstructor instanceof ParameterTypes) {
            return (ParameterTypes) methodOrConstructor;
        }
        if (methodOrConstructor instanceof Method) {
            Method method = (Method) methodOrConstructor;
            return CachedMethod.find(method);
        }
        if (methodOrConstructor instanceof Constructor) {
            Constructor constructor = (Constructor) methodOrConstructor;
            return CachedConstructor.find(constructor);
        }
        throw new IllegalArgumentException("Must be a Method or Constructor");
    }

    public static boolean isAssignableFrom(Class classToTransformTo, Class classToTransformFrom) {
        if (classToTransformTo == classToTransformFrom) return true;
        if (classToTransformFrom == null) return true;
        if (classToTransformTo == Object.class) return true;

        classToTransformTo = ReflectionCache.autoboxType(classToTransformTo);
        classToTransformFrom = ReflectionCache.autoboxType(classToTransformFrom);
        if (classToTransformTo == classToTransformFrom) return true;

        // note: there is no coercion for boolean and char. Range matters, precision doesn't
        if (classToTransformTo == Integer.class) {
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
    
    public static boolean parametersAreCompatible(Class[] arguments, Class[] parameters) {
        if (arguments.length != parameters.length) return false;
        for (int i = 0; i < arguments.length; i++) {
            if (!isAssignableFrom(parameters[i], arguments[i])) return false;
        }
        return true;
    }

    public static boolean isValidMethod(Object method, Class[] arguments) {
        return getParameterTypes(method).isValidMethod(arguments);
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
