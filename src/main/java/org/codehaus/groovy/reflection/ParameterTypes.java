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
package org.codehaus.groovy.reflection;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.reflect.Array;

public class ParameterTypes {
    private static final Class[] NO_PARAMETERS = new Class[0];

    protected volatile Class[] nativeParamTypes;
    protected volatile CachedClass[] parameterTypes;

    protected boolean isVargsMethod;

    public ParameterTypes() {
    }

    public ParameterTypes(Class pt[]) {
        nativeParamTypes = pt;
    }

    public ParameterTypes(String pt[]) {
        nativeParamTypes = new Class[pt.length];
        for (int i = 0; i != pt.length; ++i) {
            try {
                nativeParamTypes[i] = Class.forName(pt[i]);
            } catch (ClassNotFoundException e) {
                NoClassDefFoundError err = new NoClassDefFoundError();
                err.initCause(e);
                throw err;
            }
        }
    }

    public ParameterTypes(CachedClass[] parameterTypes) {
        setParametersTypes(parameterTypes);
    }

    protected final void setParametersTypes(CachedClass[] pt) {
        this.parameterTypes = pt;
        isVargsMethod = pt.length > 0 && pt[pt.length - 1].isArray;
    }

    public CachedClass[] getParameterTypes() {
        if (parameterTypes == null) {
            getParametersTypes0();
        }

        return parameterTypes;
    }

    private synchronized void getParametersTypes0() {
        if (parameterTypes != null)
            return;

        Class[] npt = nativeParamTypes == null ? getPT() : nativeParamTypes;
        if (npt.length == 0) {
            nativeParamTypes = NO_PARAMETERS;
            setParametersTypes(CachedClass.EMPTY_ARRAY);
        } else {

            CachedClass[] pt = new CachedClass[npt.length];
            for (int i = 0; i != npt.length; ++i)
                pt[i] = ReflectionCache.getCachedClass(npt[i]);

            nativeParamTypes = npt;
            setParametersTypes(pt);
        }
    }

    public Class[] getNativeParameterTypes() {
        if (nativeParamTypes == null) {
            getNativeParameterTypes0();
        }
        return nativeParamTypes;
    }

    private synchronized void getNativeParameterTypes0() {
        if (nativeParamTypes != null)
            return;

        Class[] npt;
        if (parameterTypes != null) {
            npt = new Class[parameterTypes.length];
            for (int i = 0; i != parameterTypes.length; ++i) {
                npt[i] = parameterTypes[i].getTheClass();
            }
        } else
            npt = getPT();
        nativeParamTypes = npt;
    }

    protected Class[] getPT() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    public boolean isVargsMethod() {
        return isVargsMethod;
    }

    public boolean isVargsMethod(Object[] arguments) {
        // Uncomment if at some point this method can be called before parameterTypes initialized
        // getParameterTypes();
        if (!isVargsMethod)
            return false;

        final int lenMinus1 = parameterTypes.length - 1;
        // -1 because the varg part is optional
        if (lenMinus1 == arguments.length) return true;
        if (lenMinus1 > arguments.length) return false;
        if (arguments.length > parameterTypes.length) return true;

        // only case left is arguments.length == parameterTypes.length
        Object last = arguments[arguments.length - 1];
        if (last == null) return true;
        Class clazz = last.getClass();
        return !clazz.equals(parameterTypes[lenMinus1].getTheClass());

    }

    public final Object[] coerceArgumentsToClasses(Object[] argumentArray) {
        // Uncomment if at some point this method can be called before parameterTypes initialized
        // getParameterTypes();
        argumentArray = correctArguments(argumentArray);

        final CachedClass[] pt = parameterTypes;
        final int len = argumentArray.length;
        for (int i = 0; i < len; i++) {
            final Object argument = argumentArray[i];
            if (argument != null) {
                argumentArray[i] = pt[i].coerceArgument(argument);
            }
        }
        return argumentArray;
    }

    public Object[] correctArguments(Object[] argumentArray) {
        // correct argumentArray's length
        if (argumentArray == null) {
            return MetaClassHelper.EMPTY_ARRAY;
        }

        final CachedClass[] pt = getParameterTypes();
        if (pt.length == 1 && argumentArray.length == 0) {
            if (isVargsMethod)
                return new Object[]{Array.newInstance(pt[0].getTheClass().getComponentType(), 0)};
            else
                return MetaClassHelper.ARRAY_WITH_NULL;
        }

        if (isVargsMethod && isVargsMethod(argumentArray)) {
            return fitToVargs(argumentArray, pt);
        }

        return argumentArray;
    }

    /**
     * this method is called when the number of arguments to a method is greater than 1
     * and if the method is a vargs method. This method will then transform the given
     * arguments to make the method callable
     *
     * @param argumentArrayOrig the arguments used to call the method
     * @param paramTypes        the types of the parameters the method takes
     */
    private static Object[] fitToVargs(Object[] argumentArrayOrig, CachedClass[] paramTypes) {
        Class vargsClassOrig = paramTypes[paramTypes.length - 1].getTheClass().getComponentType();
        Class vargsClass = ReflectionCache.autoboxType(vargsClassOrig);
        Object[] argumentArray = argumentArrayOrig.clone();
        MetaClassHelper.unwrap(argumentArray);

        if (argumentArray.length == paramTypes.length - 1) {
            // the vargs argument is missing, so fill it with an empty array
            Object[] newArgs = new Object[paramTypes.length];
            System.arraycopy(argumentArray, 0, newArgs, 0, argumentArray.length);
            Object vargs = Array.newInstance(vargsClass, 0);
            newArgs[newArgs.length - 1] = vargs;
            return newArgs;
        } else if (argumentArray.length == paramTypes.length) {
            // the number of arguments is correct, but if the last argument
            // is no array we have to wrap it in a array. If the last argument
            // is null, then we don't have to do anything
            Object lastArgument = argumentArray[argumentArray.length - 1];
            if (lastArgument != null && !lastArgument.getClass().isArray()) {
                // no array so wrap it
                Object wrapped = makeCommonArray(argumentArray, paramTypes.length - 1, vargsClass);
                Object[] newArgs = new Object[paramTypes.length];
                System.arraycopy(argumentArray, 0, newArgs, 0, paramTypes.length - 1);
                newArgs[newArgs.length - 1] = wrapped;
                return newArgs;
            } else {
                // we may have to box the argument!
                return argumentArray;
            }
        } else if (argumentArray.length > paramTypes.length) {
            // the number of arguments is too big, wrap all exceeding elements
            // in an array, but keep the old elements that are no vargs
            Object[] newArgs = new Object[paramTypes.length];
            // copy arguments that are not a varg
            System.arraycopy(argumentArray, 0, newArgs, 0, paramTypes.length - 1);
            // create a new array for the vargs and copy them
            Object vargs = makeCommonArray(argumentArray, paramTypes.length - 1, vargsClass);
            newArgs[newArgs.length - 1] = vargs;
            return newArgs;
        } else {
            throw new GroovyBugError("trying to call a vargs method without enough arguments");
        }
    }

    private static Object makeCommonArray(Object[] arguments, int offset, Class baseClass) {
        Object[] result = (Object[]) Array.newInstance(baseClass, arguments.length - offset);
        for (int i = offset; i < arguments.length; i++) {
            Object v = arguments[i];
            v = DefaultTypeTransformation.castToType(v, baseClass);
            result[i - offset] = v;
        }
        return result;
    }

    public boolean isValidMethod(Class[] arguments) {
        if (arguments == null) return true;

        final int size = arguments.length;
        CachedClass[] pt = getParameterTypes();
        final int paramMinus1 = pt.length - 1;

        if (isVargsMethod && size >= paramMinus1)
            return isValidVarargsMethod(arguments, size, pt, paramMinus1);
        else if (pt.length == size)
            return isValidExactMethod(arguments, pt);
        else if (pt.length == 1 && size == 0 && !pt[0].isPrimitive)
            return true;
        return false;
    }

    private static boolean isValidExactMethod(Class[] arguments, CachedClass[] pt) {
        // lets check the parameter types match
        int size = pt.length;
        for (int i = 0; i < size; i++) {
            if (!pt[i].isAssignableFrom(arguments[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidExactMethod(Object[] args) {
        // lets check the parameter types match
        getParametersTypes0();
        int size = args.length;
        if (size != parameterTypes.length)
            return false;

        for (int i = 0; i < size; i++) {
            if (args[i] != null && !parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidExactMethod(Class[] args) {
        // lets check the parameter types match
        getParametersTypes0();
        int size = args.length;
        if (size != parameterTypes.length)
            return false;

        for (int i = 0; i < size; i++) {
            if (args[i] != null && !parameterTypes[i].isAssignableFrom(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean testComponentAssignable(Class toTestAgainst, Class toTest) {
        Class component = toTest.getComponentType();
        if (component == null) return false;
        return MetaClassHelper.isAssignableFrom(toTestAgainst, component);
    }

    private static boolean isValidVarargsMethod(Class[] arguments, int size, CachedClass[] pt, int paramMinus1) {
        // first check normal number of parameters
        for (int i = 0; i < paramMinus1; i++) {
            if (pt[i].isAssignableFrom(arguments[i])) continue;
            return false;
        }

        // check direct match
        CachedClass varg = pt[paramMinus1];
        Class clazz = varg.getTheClass().getComponentType();
        if (size == pt.length &&
                (varg.isAssignableFrom(arguments[paramMinus1]) ||
                        testComponentAssignable(clazz, arguments[paramMinus1]))) {
            return true;
        }

        // check varged
        for (int i = paramMinus1; i < size; i++) {
            if (MetaClassHelper.isAssignableFrom(clazz, arguments[i])) continue;
            return false;
        }
        return true;
    }

    public boolean isValidMethod(Object[] arguments) {
        if (arguments == null) return true;

        final int size = arguments.length;
        CachedClass[] paramTypes = getParameterTypes();
        final int paramMinus1 = paramTypes.length - 1;

        if (size >= paramMinus1 && paramTypes.length > 0 &&
                paramTypes[(paramMinus1)].isArray) {
            // first check normal number of parameters
            for (int i = 0; i < paramMinus1; i++) {
                if (paramTypes[i].isAssignableFrom(getArgClass(arguments[i]))) continue;
                return false;
            }


            // check direct match
            CachedClass varg = paramTypes[paramMinus1];
            Class clazz = varg.getTheClass().getComponentType();
            if (size == paramTypes.length &&
                    (varg.isAssignableFrom(getArgClass(arguments[paramMinus1])) ||
                            testComponentAssignable(clazz, getArgClass(arguments[paramMinus1])))) {
                return true;
            }


            // check varged
            for (int i = paramMinus1; i < size; i++) {
                if (MetaClassHelper.isAssignableFrom(clazz, getArgClass(arguments[i]))) continue;
                return false;
            }
            return true;
        } else if (paramTypes.length == size) {
            // lets check the parameter types match
            for (int i = 0; i < size; i++) {
                if (paramTypes[i].isAssignableFrom(getArgClass(arguments[i]))) continue;
                return false;
            }
            return true;
        } else if (paramTypes.length == 1 && size == 0 && !paramTypes[0].isPrimitive) {
            return true;
        }
        return false;
    }

    private static Class getArgClass(Object arg) {
        Class cls;
        if (arg == null) {
            cls = null;
        } else {
            if (arg instanceof Wrapper) {
                cls = ((Wrapper) arg).getType();
            } else
                cls = arg.getClass();
        }
        return cls;
    }
}
