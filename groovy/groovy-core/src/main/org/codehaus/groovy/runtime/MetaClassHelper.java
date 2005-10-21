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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author John Wilson
 *
 */

public class MetaClassHelper {

    public static final Object[] EMPTY_ARRAY = {};
    public static Class[] EMPTY_TYPE_ARRAY = {};
    protected static final Object[] ARRAY_WITH_EMPTY_ARRAY = { EMPTY_ARRAY };
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
                dist+=2;
                continue;
            }
            
            if (arguments[i]!=null) {
                if (arguments[i].isPrimitive() || parameters[i].isPrimitive()) {
                    // type is not equal, increase distance by one to reflect
                    // the change in type
                    dist++;
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
                    dist+=2;
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
     * Checks that one of the parameter types is a superset of the other and
     * that the two lists of types don't conflict. e.g. foo(String, Object) and
     * foo(Object, String) would conflict if called with foo("a", "b").
     *
     * Note that this method is only called with 2 possible signatures. i.e.
     * possible invalid combinations will already have been filtered out. So if
     * there were methods foo(String, Object) and foo(Object, String) then one
     * of these would be already filtered out if foo was called as foo(12, "a")
     */
    protected static void checkForInvalidOverloading(String name, Class[] baseTypes, Class[] derivedTypes) {
        for (int i = 0, size = baseTypes.length; i < size; i++) {
            Class baseType = baseTypes[i];
            Class derivedType = derivedTypes[i];
            if (!isAssignableFrom(derivedType, baseType)) {
                throw new GroovyRuntimeException(
                        "Ambiguous method overloading for method: "
                        + name
                        + ". Cannot resolve which method to invoke due to overlapping prototypes between: "
                        + InvokerHelper.toString(baseTypes)
                        + " and: "
                        + InvokerHelper.toString(derivedTypes));
            }
        }
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
                if (closestClass == null || isAssignableFrom(closestClass, theType)) {
                    closestClass = theType;
                    answer = method;
                }
            }
        }
        return answer;
    }
    
    /**
     * Coerces any GString instances into Strings
     *
     * @return true if some coercion was done.
     */
    public static boolean coerceGStrings(Object[] arguments) {
        boolean coerced = false;
        for (int i = 0, size = arguments.length; i < size; i++) {
            Object argument = arguments[i];
            if (argument instanceof GString) {
                arguments[i] = argument.toString();
                coerced = true;
            }
        }
        return coerced;
    }
    
    protected static Object[] coerceNumbers(MetaMethod method, Object[] arguments) {
        Object[] ans = null;
        boolean coerced = false; // to indicate that at least one param is coerced
        
        Class[] params = method.getParameterTypes();
        
        if (params.length != arguments.length) {
            return null;
        }
        
        ans = new Object[arguments.length];
        
        for (int i = 0, size = arguments.length; i < size; i++) {
            Object argument = arguments[i];
            Class param = params[i];
            if ((Number.class.isAssignableFrom(param) || param.isPrimitive()) && argument instanceof Number) { // Number types
                if (param == Byte.class || param == Byte.TYPE ) {
                    ans[i] = new Byte(((Number)argument).byteValue());
                    coerced = true; continue;
                }
                if (param == Double.class || param == Double.TYPE) {
                    ans[i] = new Double(((Number)argument).doubleValue());
                    coerced = true; continue;
                }
                if (param == Float.class || param == Float.TYPE) {
                    ans[i] = new Float(((Number)argument).floatValue());
                    coerced = true; continue;
                }
                if (param == Integer.class || param == Integer.TYPE) {
                    ans[i] = new Integer(((Number)argument).intValue());
                    coerced = true; continue;
                }
                if (param == Long.class || param == Long.TYPE) {
                    ans[i] = new Long(((Number)argument).longValue());
                    coerced = true; continue;
                }
                if (param == Short.class || param == Short.TYPE) {
                    ans[i] = new Short(((Number)argument).shortValue());
                    coerced = true; continue;
                }
                if (param == BigDecimal.class ) {
                    ans[i] = new BigDecimal(((Number)argument).doubleValue());
                    coerced = true; continue;
                }
                if (param == BigInteger.class) {
                    ans[i] = new BigInteger(String.valueOf(((Number)argument).longValue()));
                    coerced = true; continue;
                }
            }
            else if (param.isArray() && argument.getClass().isArray()) {
                Class paramElem = param.getComponentType();
                if (paramElem.isPrimitive()) {
                    if (paramElem == boolean.class && argument.getClass().getName().equals("[Ljava.lang.Boolean;")) {
                        ans[i] = InvokerHelper.convertToBooleanArray(argument);
                        coerced = true;
                        continue;
                    }
                    if (paramElem == byte.class && argument.getClass().getName().equals("[Ljava.lang.Byte;")) {
                        ans[i] = InvokerHelper.convertToByteArray(argument);
                        coerced = true;
                        continue;
                    }
                    if (paramElem == char.class && argument.getClass().getName().equals("[Ljava.lang.Character;")) {
                        ans[i] = InvokerHelper.convertToCharArray(argument);
                        coerced = true;
                        continue;
                    }
                    if (paramElem == short.class && argument.getClass().getName().equals("[Ljava.lang.Short;")) {
                        ans[i] = InvokerHelper.convertToShortArray(argument);
                        coerced = true;
                        continue;
                    }
                    if (paramElem == int.class && argument.getClass().getName().equals("[Ljava.lang.Integer;")) {
                        ans[i] = InvokerHelper.convertToIntArray(argument);
                        coerced = true;
                        continue;
                    }
                    if (paramElem == long.class
                            && argument.getClass().getName().equals("[Ljava.lang.Long;")
                            && argument.getClass().getName().equals("[Ljava.lang.Integer;")
                    ) {
                        ans[i] = InvokerHelper.convertToLongArray(argument);
                        coerced = true;
                        continue;
                    }
                    if (paramElem == float.class
                            && argument.getClass().getName().equals("[Ljava.lang.Float;")
                            && argument.getClass().getName().equals("[Ljava.lang.Integer;")
                    ) {
                        ans[i] = InvokerHelper.convertToFloatArray(argument);
                        coerced = true;
                        continue;
                    }
                    if (paramElem == double.class &&
                            argument.getClass().getName().equals("[Ljava.lang.Double;") &&
                            argument.getClass().getName().equals("[Ljava.lang.BigDecimal;") &&
                            argument.getClass().getName().equals("[Ljava.lang.Float;")) {
                        ans[i] = InvokerHelper.convertToDoubleArray(argument);
                        coerced = true;
                        continue;
                    }
                }
            }
        }
        return coerced ? ans : null;
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
        
        try {
            // the following patch was provided by Mori Kouhei to fix JIRA 435
            /* but it opens the ctor up to everyone, so it is no longer private!
             final Constructor ctor = constructor;
             AccessController.doPrivileged(new PrivilegedAction() {
             public Object run() {
             ctor.setAccessible(ctor.getDeclaringClass().equals(theClass));
             return null;
             }
             });
             */
            // end of patch
            
            return constructor.newInstance(argumentArray);
        }
        catch (InvocationTargetException e) {
            /*Throwable t = e.getTargetException();
             if (t instanceof Error) {
             Error error = (Error) t;
             throw error;
             }
             if (t instanceof RuntimeException) {
             RuntimeException runtimeEx = (RuntimeException) t;
             throw runtimeEx;
             }*/
            throw new InvokerInvocationException(e);
        }
        catch (IllegalArgumentException e) {
            if (coerceGStrings(argumentArray)) {
                try {
                    return constructor.newInstance(argumentArray);
                }
                catch (Exception e2) {
                    // allow fall through
                }
            }
            throw new GroovyRuntimeException(
                    "failed to invoke constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e);
        }
        catch (IllegalAccessException e) {
            throw new GroovyRuntimeException(
                    "could not access constructor: "
                    + constructor
                    + " with arguments: "
                    + InvokerHelper.toString(argumentArray)
                    + " reason: "
                    + e);
        }
        catch (Exception e) {
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
    
    public static Object doMethodInvoke(Object object, MetaMethod method, Object[] argumentArray) {
        //System.out.println("Evaluating method: " + method);
        //System.out.println("on object: " + object + " with arguments: " +
        // InvokerHelper.toString(argumentArray));
        //System.out.println(this.theClass);
        
        Class[] paramTypes = method.getParameterTypes();
        try {
            if (argumentArray == null) {
                argumentArray = EMPTY_ARRAY;
            } else if (paramTypes.length == 1 && argumentArray.length == 0) {
                if (isVargsMethod(paramTypes,argumentArray))
                    argumentArray = ARRAY_WITH_EMPTY_ARRAY;
                else
                    argumentArray = ARRAY_WITH_NULL;
            } else if (isVargsMethod(paramTypes,argumentArray)) {
                // vargs
                Object[] newArg = new Object[paramTypes.length];
                System.arraycopy(argumentArray,0,newArg,0,newArg.length-1);
                Object[] vargs = new Object[argumentArray.length-newArg.length+1];
                System.arraycopy(argumentArray,newArg.length-1,vargs,0,vargs.length);
                if (vargs.length == 1 && vargs[0] == null)
                    newArg[newArg.length-1] = null;
                else {
                    newArg[newArg.length-1] = vargs;
                }
                argumentArray = newArg;
            }
            return method.invoke(object, argumentArray);
        }
        catch (ClassCastException e) {
            if (coerceGStrings(argumentArray)) {
                try {
                    return doMethodInvoke(object, method, argumentArray);
                }
                catch (Exception e2) {
                    // allow fall through
                }
            }
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
        catch (InvocationTargetException e) {
            /*Throwable t = e.getTargetException();
             if (t instanceof Error) {
             Error error = (Error) t;
             throw error;
             }
             if (t instanceof RuntimeException) {
             RuntimeException runtimeEx = (RuntimeException) t;
             throw runtimeEx;
             }*/
            throw new InvokerInvocationException(e);
        }
        catch (IllegalAccessException e) {
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
        }
        catch (IllegalArgumentException e) {
            if (coerceGStrings(argumentArray)) {
                try {
                    return doMethodInvoke(object, method, argumentArray);
                }
                catch (Exception e2) {
                    // allow fall through
                }
            }
            Object[] args = coerceNumbers(method, argumentArray);
            if (args != null && !Arrays.equals(argumentArray,args)) {
                try {
                    return doMethodInvoke(object, method, args);
                }
                catch (Exception e3) {
                    // allow fall through
                }
            }
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
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
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
    
    private static boolean implementsInterface(Class clazz, Class iface) {
        if (!iface.isInterface()) return false;
        return iface.isAssignableFrom(clazz);
    }
    
    protected static boolean isAssignableFrom(Class mostSpecificType, Class type) {
        if (mostSpecificType==null) return true;
        // let's handle primitives
        if (mostSpecificType.isPrimitive() && type.isPrimitive()) {
            if (mostSpecificType == type) {
                return true;
            }
            else {  // note: there is not coercion for boolean and char. Range matters, precision doesn't
                if (type == int.class) {
                    return
                    mostSpecificType == int.class
                    || mostSpecificType == short.class
                    || mostSpecificType == byte.class;
                }
                else if (type == double.class) {
                    return
                    mostSpecificType == double.class
                    || mostSpecificType == int.class
                    || mostSpecificType == long.class
                    || mostSpecificType == short.class
                    || mostSpecificType == byte.class
                    || mostSpecificType == float.class;
                }
                else if (type == long.class) {
                    return
                    mostSpecificType == long.class
                    || mostSpecificType == int.class
                    || mostSpecificType == short.class
                    || mostSpecificType == byte.class;
                }
                else if (type == float.class) {
                    return
                    mostSpecificType == float.class
                    || mostSpecificType == int.class
                    || mostSpecificType == long.class
                    || mostSpecificType == short.class
                    || mostSpecificType == byte.class;
                }
                else if (type == short.class) {
                    return
                    mostSpecificType == short.class
                    || mostSpecificType == byte.class;
                }
                else {
                    return false;
                }
            }
        }
        if (type==String.class) {
            return  mostSpecificType == String.class ||
            GString.class.isAssignableFrom(mostSpecificType);
        }
        
        boolean answer = type.isAssignableFrom(mostSpecificType);
        if (!answer) {
            answer = autoboxType(type).isAssignableFrom(autoboxType(mostSpecificType));
        }
        return answer;
    }
    
    protected static boolean isCompatibleClass(Class type, Class value, boolean includeCoerce) {
        boolean answer = value == null || type.isAssignableFrom(value); // this might have taken care of primitive types, rendering part of the following code unnecessary
        if (!answer) {
            if (type.isPrimitive()) {
                if (type == int.class) {
                    return value == Integer.class;// || value == BigDecimal.class; //br added BigDecimal
                }
                else if (type == double.class) {
                    return value == Double.class || value == Float.class || value == Integer.class || value == BigDecimal.class;
                }
                else if (type == boolean.class) {
                    return value == Boolean.class;
                }
                else if (type == long.class) {
                    return value == Long.class || value == Integer.class; // || value == BigDecimal.class;//br added BigDecimal
                }
                else if (type == float.class) {
                    return value == Float.class || value == Integer.class; // || value == BigDecimal.class;//br added BigDecimal
                }
                else if (type == char.class) {
                    return value == Character.class;
                }
                else if (type == byte.class) {
                    return value == Byte.class;
                }
                else if (type == short.class) {
                    return value == Short.class;
                }
            } else if (type.isArray() && value.isArray()) {
                return isCompatibleClass(type.getComponentType(), value.getComponentType(), false);
            }
            else if (includeCoerce) {
                //if (type == String.class && value == GString.class) {
                if (type == String.class && GString.class.isAssignableFrom(value)) {
                    return true;
                }
                else if (value == Number.class) {
                    // lets allow numbers to be coerced downwards?
                    return Number.class.isAssignableFrom(type);
                }
            }
        }
        return answer;
    }
    
    protected static boolean isCompatibleInstance(Class type, Object value, boolean includeCoerce) {
        boolean answer = value == null || type.isInstance(value);
        if (!answer) {
            if (type.isPrimitive()) {
                if (type == int.class) {
                    return value instanceof Integer;
                }
                else if (type == double.class) {
                    return value instanceof Double || value instanceof Float || value instanceof Integer || value instanceof BigDecimal;
                }
                else if (type == boolean.class) {
                    return value instanceof Boolean;
                }
                else if (type == long.class) {
                    return value instanceof Long || value instanceof Integer;
                }
                else if (type == float.class) {
                    return value instanceof Float || value instanceof Integer;
                }
                else if (type == char.class) {
                    return value instanceof Character;
                }
                else if (type == byte.class) {
                    return value instanceof Byte;
                }
                else if (type == short.class) {
                    return value instanceof Short;
                }
            }
            else if(type.isArray() && value.getClass().isArray()) {
                return isCompatibleClass(type.getComponentType(), value.getClass().getComponentType(), false);
            }
            else if (includeCoerce) {
                if (type == String.class && value instanceof GString) {
                    return true;
                }
                else if (value instanceof Number) {
                    // lets allow numbers to be coerced downwards?
                    return Number.class.isAssignableFrom(type);
                }
            }
        }
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
                if (isCompatibleClass(paramTypes[i], arguments[i], includeCoerce)) continue;
                return false;
            }
            // check varged
            Class clazz = paramTypes[paramTypes.length-1].getComponentType();
            for (int i=paramTypes.length; i<size; i++) {
                if (isCompatibleClass(clazz, arguments[i], includeCoerce)) continue;
                return false;
            }
            return true;
        } else if (paramTypes.length == size) {
            // lets check the parameter types match
            for (int i = 0; i < size; i++) {
                if (isCompatibleClass(paramTypes[i], arguments[i], includeCoerce)) continue;
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
            if (!isAssignableFrom(arguments[i],parameters[i])) return false;
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
