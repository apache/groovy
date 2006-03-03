/*
 * Copyright 2005 John G. Wilson
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
 *
 */

package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaMethod;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.groovy.GroovyBugError;

/**
 * @author John Wilson
 * @author Jochen Theodorou
 */
public class MetaClassHelper {

    public static final Object[] EMPTY_ARRAY = {};
    public static Class[] EMPTY_TYPE_ARRAY = {};
    protected static final Object[] ARRAY_WITH_NULL = { null };
    protected static final Logger log = Logger.getLogger(MetaClassHelper.class.getName());
    private static final int MAX_ARG_LEN = 12;
    
    public static boolean accessibleToConstructor(final Class at, final Constructor constructor) {
        boolean accessible = false;
        if (Modifier.isPublic(constructor.getModifiers())) {
            accessible = true;
        }
        else if (Modifier.isPrivate(constructor.getModifiers())) {
            accessible = at.getName().equals(constructor.getName());
        }
        else if ( Modifier.isProtected(constructor.getModifiers()) ) {
            if ( at.getPackage() == null && constructor.getDeclaringClass().getPackage() == null ) {
                accessible = true;
            }
            else if ( at.getPackage() == null && constructor.getDeclaringClass().getPackage() != null ) {
                accessible = false;
            }
            else if ( at.getPackage() != null && constructor.getDeclaringClass().getPackage() == null ) {
                accessible = false;
            }
            else if ( at.getPackage().equals(constructor.getDeclaringClass().getPackage()) ) {
                accessible = true;
            }
            else {
                boolean flag = false;
                Class clazz = at;
                while ( !flag && clazz != null ) {
                    if (clazz.equals(constructor.getDeclaringClass()) ) {
                        flag = true;
                        break;
                    }
                    if (clazz.equals(Object.class) ) {
                        break;
                    }
                    clazz = clazz.getSuperclass();
                }
                accessible = flag;
            }
        }
        else {
            if ( at.getPackage() == null && constructor.getDeclaringClass().getPackage() == null ) {
                accessible = true;
            }
            else if ( at.getPackage() == null && constructor.getDeclaringClass().getPackage() != null ) {
                accessible = false;
            }
            else if ( at.getPackage() != null && constructor.getDeclaringClass().getPackage() == null ) {
                accessible = false;
            }
            else if ( at.getPackage().equals(constructor.getDeclaringClass().getPackage()) ) {
                accessible = true;
            }
        }
        return accessible;
    }
    
    public static Object[] asWrapperArray(Object parameters, Class componentType) {
        Object[] ret=null;
        if (componentType == boolean.class) {
            boolean[] array = (boolean[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Boolean(array[i]);
            }
        } else if (componentType == char.class) {
            char[] array = (char[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Character(array[i]);
            }
        } else if (componentType == byte.class) {
            byte[] array = (byte[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Byte(array[i]);
            }
        } else if (componentType == int.class) {
            int[] array = (int[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Integer(array[i]);
            }
        } else if (componentType == short.class) {
            short[] array = (short[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Short(array[i]);
            }
        } else if (componentType == long.class) {
            long[] array = (long[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Long(array[i]);
            }
        } else if (componentType == double.class) {
            double[] array = (double[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Double(array[i]);
            }
        } else if (componentType == float.class) {
            float[] array = (float[]) parameters;
            ret = new Object[array.length];
            for (int i=0; i<array.length; i++) {
                ret[i] = new Float(array[i]);
            }
        }
        
        return ret;
    }
    
    
    /**
     * @param list
     * @param parameterType
     * @return
     */
    public static Object asPrimitiveArray(List list, Class parameterType) {
        Class arrayType = parameterType.getComponentType();
        Object objArray = Array.newInstance(arrayType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (arrayType.isPrimitive()) {
                if (obj instanceof Integer) {
                    Array.setInt(objArray, i, ((Integer) obj).intValue());
                }
                else if (obj instanceof Double) {
                    Array.setDouble(objArray, i, ((Double) obj).doubleValue());
                }
                else if (obj instanceof Boolean) {
                    Array.setBoolean(objArray, i, ((Boolean) obj).booleanValue());
                }
                else if (obj instanceof Long) {
                    Array.setLong(objArray, i, ((Long) obj).longValue());
                }
                else if (obj instanceof Float) {
                    Array.setFloat(objArray, i, ((Float) obj).floatValue());
                }
                else if (obj instanceof Character) {
                    Array.setChar(objArray, i, ((Character) obj).charValue());
                }
                else if (obj instanceof Byte) {
                    Array.setByte(objArray, i, ((Byte) obj).byteValue());
                }
                else if (obj instanceof Short) {
                    Array.setShort(objArray, i, ((Short) obj).shortValue());
                }
            }
            else {
                Array.set(objArray, i, obj);
            }
        }
        return objArray;
    }
    
    protected static Class autoboxType(Class type) {
        if (type.isPrimitive()) {
            if (type == int.class) {
                return Integer.class;
            }
            else if (type == double.class) {
                return Double.class;
            }
            else if (type == long.class) {
                return Long.class;
            }
            else if (type == boolean.class) {
                return Boolean.class;
            }
            else if (type == float.class) {
                return Float.class;
            }
            else if (type == char.class) {
                return Character.class;
            }
            else if (type == byte.class) {
                return Byte.class;
            }
            else if (type == short.class) {
                return Short.class;
            }
        }
        return type;
    }
    
    public static int calculateParameterDistance(Class[] arguments, Class[] parameters) {
        int dist=0;
        for (int i=0; i<arguments.length; i++) {
            if (parameters[i]==arguments[i]) continue;
            
            if (parameters[i].isInterface()) {
                dist+=3;
                continue;
            }
            
            if (arguments[i]!=null) {
                if (autoboxType(parameters[i]) == autoboxType(arguments[i])){
                    // type is not equal, but boxed types are. Increase distance 
                    // by 1 to reflect the change in type
                    dist +=1;
                    continue;
                }
                if (arguments[i].isPrimitive() || parameters[i].isPrimitive()) {
                    // type is not equal, increase distance by 2 to reflect
                    // the change in type
                    dist+=2;
                    continue;
                }
                
                // add one to dist to be sure interfaces are prefered
                dist++;
                Class clazz = arguments[i];
                while (clazz!=null) {
                    if (clazz==parameters[i]) break;
                    if (clazz==GString.class && parameters[i]==String.class) {
                        dist+=2;
                        break;
                    }
                    clazz = clazz.getSuperclass();
                    dist+=3;
                }
            } else {
                // choose the distance to Object if a parameter is null
                // this will mean that Object is prefered over a more
                // specific type
                // remove one to dist to be sure Object is prefered
                dist--;
                Class clazz = parameters[i];
                while (clazz!=Object.class) {
                    clazz = clazz.getSuperclass();
                    dist+=2;
                }
            }
        }
        return dist;
    }
    
    public static String capitalize(String property) {
        return property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
    }
    
    /**
     * @return the method with 1 parameter which takes the most general type of
     *         object (e.g. Object)
     */
    public static Object chooseEmptyMethodParams(List methods) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            Class[] paramTypes = getParameterTypes(method);
            int paramLength = paramTypes.length;
            if (paramLength == 0) {
                return method;
            }
        }
        return null;
    }
    
    /**
     * @return the method with 1 parameter which takes the most general type of
     *         object (e.g. Object) ignoring primitve types
     */
    public static Object chooseMostGeneralMethodWith1NullParam(List methods) {
        // lets look for methods with 1 argument which matches the type of the
        // arguments
        Class closestClass = null;
        Object answer = null;
        
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Object method = iter.next();
            Class[] paramTypes = getParameterTypes(method);
            int paramLength = paramTypes.length;
            if (paramLength == 1) {
                Class theType = paramTypes[0];
                if (theType.isPrimitive()) continue;
                if (closestClass == null || isAssignableFrom(theType, closestClass)) {
                    closestClass = theType;
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
    protected static Object coerceGString(Object argument, Class clazz) {
        if (clazz!=String.class) return argument;
        if (!(argument instanceof GString)) return argument;
        return argument.toString();
    }
    
    protected static Object coerceNumber(Object argument, Class param) {
        if ((Number.class.isAssignableFrom(param) || param.isPrimitive()) && argument instanceof Number) { // Number types
            Object oldArgument = argument;
            boolean wasDouble = false;
            boolean wasFloat = false;
            if (param == Byte.class || param == Byte.TYPE ) {
                argument = new Byte(((Number)argument).byteValue());
            } else if (param == Double.class || param == Double.TYPE) {
                wasDouble = true;
                argument = new Double(((Number)argument).doubleValue());
            } else if (param == Float.class || param == Float.TYPE) {
                wasFloat = true;
                argument = new Float(((Number)argument).floatValue());
            } else if (param == Integer.class || param == Integer.TYPE) {
                argument = new Integer(((Number)argument).intValue());
            } else if (param == Long.class || param == Long.TYPE) {
                argument = new Long(((Number)argument).longValue());
            } else if (param == Short.class || param == Short.TYPE) {
                argument = new Short(((Number)argument).shortValue());
            } else if (param == BigDecimal.class ) {
                argument = new BigDecimal(String.valueOf((Number)argument));
            } else if (param == BigInteger.class) {
                argument = new BigInteger(String.valueOf((Number)argument));
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
                    BigDecimal newbd = new BigDecimal(String.valueOf((Number)argument));
                    throwException = !oldArgument.equals(newbd);
                }
                
                if (throwException) throw new IllegalArgumentException(param+" out of range while converting from BigDecimal");
            }

        }
        return argument;
    }
        
     protected static Object coerceArray(Object argument, Class param) {
         if (!param.isArray()) return argument;
         Class argumentClass = argument.getClass();
         if (!argumentClass.isArray()) return argument;
            
         Class paramComponent = param.getComponentType();
         if (paramComponent.isPrimitive()) {
             if (paramComponent == boolean.class && argumentClass==Boolean[].class) {
                 argument = InvokerHelper.convertToBooleanArray(argument);
             } else if (paramComponent == byte.class && argumentClass==Byte[].class) {
                 argument = InvokerHelper.convertToByteArray(argument);
             } else if (paramComponent == char.class && argumentClass==Character[].class) {
                 argument = InvokerHelper.convertToCharArray(argument);
             } else if (paramComponent == short.class && argumentClass==Short[].class) {
                 argument = InvokerHelper.convertToShortArray(argument);
             } else if (paramComponent == int.class && argumentClass==Integer[].class) {
                 argument = InvokerHelper.convertToIntArray(argument);
             } else if (paramComponent == long.class &&
                        (argumentClass == Long[].class || argumentClass  == Integer[].class))
             {
                 argument = InvokerHelper.convertToLongArray(argument);
             } else if (paramComponent == float.class &&
                        (argumentClass == Float[].class || argumentClass == Integer[].class))
             {
                 argument = InvokerHelper.convertToFloatArray(argument);
             } else if (paramComponent == double.class &&
                        (argumentClass == Double[].class || argumentClass==Float[].class  
                         || BigDecimal.class.isAssignableFrom(argumentClass)))
             {
                 argument = InvokerHelper.convertToDoubleArray(argument);
             }
         } else if (paramComponent==String.class && argument instanceof GString[]) {
             GString[] strings = (GString[]) argument;
             String[] ret = new String[strings.length];
             for (int i=0; i<strings.length; i++) {
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
            Class[] params1 = aMethod.getParameterTypes();
            Class[] params2 = method.getParameterTypes();
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
     * @param args
     * @return
     */
    public static Class[] convertToTypeArray(Object[] args) {
        if (args == null)
            return null;
        int s = args.length;
        Class[] ans = new Class[s];
        for (int i = 0; i < s; i++) {
            Object o = args[i];
            if (o != null) {
                ans[i] = o.getClass();
            } else {
                ans[i] = null;
            }
        }
        return ans;
    }
    
    /**
     * @param listenerType
     *            the interface of the listener to proxy
     * @param listenerMethodName
     *            the name of the method in the listener API to call the
     *            closure on
     * @param closure
     *            the closure to invoke on the listenerMethodName method
     *            invocation
     * @return a dynamic proxy which calls the given closure on the given
     *         method name
     */
    public static Object createListenerProxy(Class listenerType, final String listenerMethodName, final Closure closure) {
        InvocationHandler handler = new ClosureListener(listenerMethodName, closure);
        return Proxy.newProxyInstance(listenerType.getClassLoader(), new Class[] { listenerType }, handler);
    }
    
    public static Object doConstructorInvoke(Constructor constructor, Object[] argumentArray) {
        if (log.isLoggable(Level.FINER)){
            logMethodCall(constructor.getDeclaringClass(), constructor.getName(), argumentArray);
        }
        argumentArray = coerceArgumentsToClasses(argumentArray,constructor.getParameterTypes());
        try {
            return constructor.newInstance(argumentArray);
        } catch (InvocationTargetException e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalArgumentException e) {
            throw new GroovyRuntimeException(
                    "failed to invoke constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e);
        } catch (IllegalAccessException e) {
            throw new GroovyRuntimeException(
                    "could not access constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e);
        } catch (Exception e) {
            throw new GroovyRuntimeException(
                    "failed to invoke constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                    e);
        }
    }
    
    public static Object[] coerceArgumentsToClasses(Object[] argumentArray, Class[] paramTypes) {
        // correct argumentArray's length
        if (argumentArray == null) {
            argumentArray = EMPTY_ARRAY;
        } else if (paramTypes.length == 1 && argumentArray.length == 0) {
            if (isVargsMethod(paramTypes,argumentArray))
                argumentArray = new Object[]{Array.newInstance(paramTypes[0].getComponentType(),0)};
            else
                argumentArray = ARRAY_WITH_NULL;
        } else if (isVargsMethod(paramTypes,argumentArray)) {
            argumentArray = fitToVargs(argumentArray, paramTypes);
        }
        
        //correct Type
        for (int i=0; i<argumentArray.length; i++) {
            Object argument = argumentArray[i];
            if (argument==null) continue;
            Class parameterType = paramTypes[i];
            if (parameterType.isInstance(argument)) continue;
            
            argument = coerceGString(argument,parameterType);
            argument = coerceNumber(argument,parameterType);
            argument = coerceArray(argument,parameterType);
            argumentArray[i] = argument;
        }
        return argumentArray;
    }

    private static Object makeCommonArray(Object[] arguments, int offset, Class fallback) {
    	// arguments.leght>0 && !=null
    	Class baseClass = null;
    	for (int i = offset; i < arguments.length; i++) {
			if (arguments[i]==null) continue;
			Class argClass = arguments[i].getClass();
			if (baseClass==null) {
				baseClass = argClass;
			} else {
				for (;baseClass!=Object.class; baseClass=baseClass.getSuperclass()){
					if (baseClass.isAssignableFrom(argClass)) break;
				}
			}
		}
        if (baseClass==null) {
            // all arguments were null
            baseClass = fallback;
        }
    	Object result = makeArray(null,baseClass,arguments.length-offset);
    	System.arraycopy(arguments,offset,result,0,arguments.length-offset);
    	return result;
    }
    
    private static Object makeArray(Object obj, Class secondary, int length) {
    	Class baseClass = secondary;
    	if (obj!=null) {
    		baseClass = obj.getClass();
    	}
    	/*if (GString.class.isAssignableFrom(baseClass)) {
    		baseClass = GString.class;
    	}*/
    	return Array.newInstance(baseClass,length);
    }
    
    /**
     * this method is called when the number of arguments to a method is greater than 1
     * and if the method is a vargs method. This method will then transform the given
     * arguments to make the method callable
     * 
     * @param argumentArray the arguments used to call the method
     * @param paramTypes the types of the paramters the method takes
     */
    private static Object[] fitToVargs(Object[] argumentArray, Class[] paramTypes) {
    	Class vargsClass = autoboxType(paramTypes[paramTypes.length-1].getComponentType());
    	
        if (argumentArray.length == paramTypes.length-1) {
            // the vargs argument is missing, so fill it with an empty array
            Object[] newArgs = new Object[paramTypes.length];
            System.arraycopy(argumentArray,0,newArgs,0,argumentArray.length);
            Object vargs = makeArray(null,vargsClass,0);
            newArgs[newArgs.length-1] = vargs;
            return newArgs;
        } else if (argumentArray.length==paramTypes.length) {
            // the number of arguments is correct, but if the last argument 
            // is no array we have to wrap it in a array. if the last argument
            // is null, then we don't have to do anything
            Object lastArgument = argumentArray[argumentArray.length-1];
            if (lastArgument!=null && !lastArgument.getClass().isArray()) {
                // no array so wrap it
                Object vargs = makeArray(lastArgument,vargsClass,1);
                System.arraycopy(argumentArray,argumentArray.length-1,vargs,0,1);
                argumentArray[argumentArray.length-1]=vargs;
                return argumentArray;
            } else {
                // we may have to box the arguemnt!
                return argumentArray;
            } 
        } else if (argumentArray.length>paramTypes.length) {
            // the number of arguments is too big, wrap all exceeding elements
            // in an array, but keep the old elements that are no vargs
            Object[] newArgs = new Object[paramTypes.length];
            // copy arguments that are not a varg
            System.arraycopy(argumentArray,0,newArgs,0,paramTypes.length-1);
            // create a new array for the vargs and copy them
            int numberOfVargs = argumentArray.length-paramTypes.length;
            Object vargs = makeCommonArray(argumentArray,paramTypes.length-1,vargsClass);
            newArgs[newArgs.length-1] = vargs;
            return newArgs;
        } else {
            throw new GroovyBugError("trying to call a vargs method without enough arguments");
        }
    }
    
    public static Object doMethodInvoke(Object object, MetaMethod method, Object[] argumentArray) {
        Class[] paramTypes = method.getParameterTypes();
        argumentArray = coerceArgumentsToClasses(argumentArray,paramTypes);
        try {
            return method.invoke(object, argumentArray);
        } catch (InvocationTargetException e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalAccessException e) {
            throw new GroovyRuntimeException(
                    "could not access method: "
                    + method
                    + " on: "
                    + object
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                    e);
        } catch (IllegalArgumentException e) {
            throw new GroovyRuntimeException(
                    "failed to invoke method: "
                    + method
                    + " on: "
                    + object
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + "reason: "
                    + e
            );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new GroovyRuntimeException(
                    "failed to invoke method: "
                    + method
                    + " on: "
                    + object
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e,
                    e);
        }
    }
    
    protected static String getClassName(Object object) {
        return (object instanceof Class) ? ((Class)object).getName() : object.getClass().getName();
    }
    
    /**
     * Returns a callable object for the given method name on the object.
     * The object acts like a Closure in that it can be called, like a closure
     * and passed around - though really its a method pointer, not a closure per se.
     */
    public static Closure getMethodPointer(Object object, String methodName) {
        return new MethodClosure(object, methodName);
    }
    
    public static Class[] getParameterTypes(Object methodOrConstructor) {
        if (methodOrConstructor instanceof MetaMethod) {
            MetaMethod method = (MetaMethod) methodOrConstructor;
            return method.getParameterTypes();
        }
        if (methodOrConstructor instanceof Method) {
            Method method = (Method) methodOrConstructor;
            return method.getParameterTypes();
        }
        if (methodOrConstructor instanceof Constructor) {
            Constructor constructor = (Constructor) methodOrConstructor;
            return constructor.getParameterTypes();
        }
        throw new IllegalArgumentException("Must be a Method or Constructor");
    }
   
    protected static boolean isAssignableFrom(Class classToTransformTo, Class classToTransformFrom) {
        if (classToTransformFrom==null) return true;
        classToTransformTo = autoboxType(classToTransformTo);
        classToTransformFrom = autoboxType(classToTransformFrom);
        
        if (classToTransformTo == classToTransformFrom) {
        	return true;
        }
        // note: there is not coercion for boolean and char. Range matters, precision doesn't
        else if (classToTransformTo == Integer.class) {
        	if (	classToTransformFrom == Integer.class
        			|| classToTransformFrom == Short.class
        			|| classToTransformFrom == Byte.class)
        	return true;
        }
        else if (classToTransformTo == Double.class) {
        	if (	classToTransformFrom == Double.class
        			|| classToTransformFrom == Integer.class
        			|| classToTransformFrom == Long.class
        			|| classToTransformFrom == Short.class
        			|| classToTransformFrom == Byte.class
        			|| classToTransformFrom == Float.class
                    || classToTransformFrom == BigDecimal.class
                    || classToTransformFrom == BigInteger.class)
        	return true;
        }
        else if (classToTransformTo == BigDecimal.class) {
            if (    classToTransformFrom == Double.class
                    || classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == Float.class
                    || classToTransformFrom == BigDecimal.class
                    || classToTransformFrom == BigInteger.class)
            return true;
        }
        else if (classToTransformTo == BigInteger.class) {
            if (    classToTransformFrom == Integer.class
                    || classToTransformFrom == Long.class
                    || classToTransformFrom == Short.class
                    || classToTransformFrom == Byte.class
                    || classToTransformFrom == BigInteger.class)
            return true;
        }
        else if (classToTransformTo == Long.class) {
        	if (	classToTransformFrom == Long.class
        			|| classToTransformFrom == Integer.class
        			|| classToTransformFrom == Short.class
        			|| classToTransformFrom == Byte.class)
        	return true;
        }
        else if (classToTransformTo == Float.class) {
        	if (	classToTransformFrom == Float.class
        			|| classToTransformFrom == Integer.class
        			|| classToTransformFrom == Long.class
        			|| classToTransformFrom == Short.class
        			|| classToTransformFrom == Byte.class)
        	return true;
        }
        else if (classToTransformTo == Short.class) {
        	if (	classToTransformFrom == Short.class
        			|| classToTransformFrom == Byte.class)
        	return true;
        }
        else if (classToTransformTo==String.class) {
            if (	classToTransformFrom == String.class ||
            		GString.class.isAssignableFrom(classToTransformFrom))
            return true;
        }
        
        boolean answer = classToTransformTo.isAssignableFrom(classToTransformFrom);
        return answer;
    }
    
    public static boolean isGenericSetMethod(MetaMethod method) {
        return (method.getName().equals("set"))
        && method.getParameterTypes().length == 2;
    }
    
    protected static boolean isSuperclass(Class claszz, Class superclass) {
        while (claszz!=null) {
            if (claszz==superclass) return true;
            claszz = claszz.getSuperclass();
        }
        return false;
    }
    
    public static boolean isValidMethod(Class[] paramTypes, Class[] arguments, boolean includeCoerce) {
        if (arguments == null) {
            return true;
        }
        int size = arguments.length;
        
        if (   (size>=paramTypes.length || size==paramTypes.length-1)
                && paramTypes.length>0
                && paramTypes[paramTypes.length-1].isArray())
        {
            // first check normal number of parameters
            for (int i = 0; i < paramTypes.length-1; i++) {
                if (isAssignableFrom(paramTypes[i], arguments[i])) continue;
                return false;
            }
            // check varged
            Class clazz = paramTypes[paramTypes.length-1].getComponentType();
            for (int i=paramTypes.length; i<size; i++) {
                if (isAssignableFrom(clazz, arguments[i])) continue;
                return false;
            }
            return true;
        } else if (paramTypes.length == size) {
            // lets check the parameter types match
            for (int i = 0; i < size; i++) {
                if (isAssignableFrom(paramTypes[i], arguments[i])) continue;
                return false;
            }
            return true;
        } else if (paramTypes.length == 1 && size == 0) {
            return true;
        }
        return false;
        
    }
    
    public static boolean isValidMethod(Object method, Class[] arguments, boolean includeCoerce) {
        Class[] paramTypes = getParameterTypes(method);
        return isValidMethod(paramTypes, arguments, includeCoerce);
    }
    
    public static boolean isVargsMethod(Class[] paramTypes, Object[] arguments) {
        if (paramTypes.length==0) return false;
        if (!paramTypes[paramTypes.length-1].isArray()) return false;
        // -1 because the varg part is optional
        if (paramTypes.length-1==arguments.length) return true;
        if (paramTypes.length-1>arguments.length) return false;
        if (arguments.length>paramTypes.length) return true;
        
        // only case left is arguments.length==paramTypes.length
        Object last = arguments[arguments.length-1];
        if (last==null) return true;
        Class clazz = last.getClass();
        if (clazz.equals(paramTypes[paramTypes.length-1])) return false;
        
        return true;
    }
    
    public static void logMethodCall(Object object, String methodName, Object[] arguments) {
        String className = getClassName(object);
        String logname = "methodCalls." + className + "." + methodName;
        Logger objLog = Logger.getLogger(logname);
        if (! objLog.isLoggable(Level.FINER)) return;
        StringBuffer msg = new StringBuffer(methodName);
        msg.append("(");
        if (arguments != null){
            for (int i = 0; i < arguments.length;) {
                msg.append(normalizedValue(arguments[i]));
                if (++i < arguments.length) { msg.append(","); }
            }
        }
        msg.append(")");
        objLog.logp(Level.FINER, className, msg.toString(), "called from MetaClass.invokeMethod");
    }
    
    protected static String normalizedValue(Object argument) {
        String value;
        try {
            value = argument.toString();
            if (value.length() > MAX_ARG_LEN){
                value = value.substring(0,MAX_ARG_LEN-2) + "..";
            }
            if (argument instanceof String){
                value = "\'"+value+"\'";
            }
        } catch (Exception e) {
            value = shortName(argument);
        }
        return value;
    }
    
    public static boolean parametersAreCompatible(Class[] arguments, Class[] parameters) {
        if (arguments.length!=parameters.length) return false;
        for (int i=0; i<arguments.length; i++) {
            if (!isAssignableFrom(parameters[i],arguments[i])) return false;
        }
        return true;
    }
    
    protected static String shortName(Object object) {
        if (object == null || object.getClass()==null) return "unknownClass";
        String name = getClassName(object);
        if (name == null) return "unknownClassName"; // *very* defensive...
        int lastDotPos = name.lastIndexOf('.');
        if (lastDotPos < 0 || lastDotPos >= name.length()-1) return name;
        return name.substring(lastDotPos+1);
    }
    
    public static Class[] wrap(Class[] classes) {
        Class[] wrappedArguments = new Class[classes.length];
        for (int i = 0; i < wrappedArguments.length; i++) {
            Class c = classes[i];
            if (c==null) continue;
            if (c.isPrimitive()) {
                if (c==Integer.TYPE) {
                    c=Integer.class;
                } else if (c==Byte.TYPE) {
                    c=Byte.class;
                } else if (c==Long.TYPE) {
                    c=Long.class;
                } else if (c==Double.TYPE) {
                    c=Double.class;
                } else if (c==Float.TYPE) {
                    c=Float.class;
                }
            } else if (isSuperclass(c,GString.class)) {
                c = String.class;
            }
            wrappedArguments[i]=c;
        }
        return wrappedArguments;
    }
}
