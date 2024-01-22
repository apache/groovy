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

    protected volatile CachedClass[] parameterTypes;
    protected volatile Class[] nativeParamTypes;
    protected boolean isVargsMethod;

    public ParameterTypes() {
    }

    public ParameterTypes(Class[] pt) {
        nativeParamTypes = pt;
    }

    public ParameterTypes(String[] pt) {
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

    public ParameterTypes(CachedClass[] pt) {
        setParametersTypes(pt);
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
        if (parameterTypes == null) {
            Class[] npt = nativeParamTypes == null ? getPT() : nativeParamTypes;
            if (npt.length == 0) {
                nativeParamTypes = MetaClassHelper.EMPTY_TYPE_ARRAY;
                setParametersTypes(CachedClass.EMPTY_ARRAY);
            } else {
                CachedClass[] pt = new CachedClass[npt.length];
                for (int i = 0; i != npt.length; ++i) {
                    pt[i] = ReflectionCache.getCachedClass(npt[i]);
                }
                nativeParamTypes = npt;
                setParametersTypes(pt);
            }
        }
    }

    public Class[] getNativeParameterTypes() {
        if (nativeParamTypes == null) {
            getNativeParameterTypes0();
        }
        return nativeParamTypes;
    }

    private synchronized void getNativeParameterTypes0() {
        if (nativeParamTypes == null) {
            Class[] npt;
            if (parameterTypes == null) {
                npt = getPT();
            } else {
                npt = new Class[parameterTypes.length];
                for (int i = 0; i != parameterTypes.length; ++i) {
                    npt[i] = parameterTypes[i].getTheClass();
                }
            }
            nativeParamTypes = npt;
        }
    }

    protected Class[] getPT() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    public boolean isVargsMethod() {
        if (parameterTypes == null) {
            getParametersTypes0(); // GROOVY-11293
        }
        return isVargsMethod;
    }

    public boolean isVargsMethod(Object[] arguments) {
        if (isVargsMethod()) {
            int aCount = arguments.length;
            int pCount = parameterTypes.length;
            if (aCount > pCount || aCount == pCount-1) { // too many or too few?
                return true;
            }
            if (aCount == pCount) {
                Object last = arguments[aCount-1]; // is null or different type?
                return last == null || !last.getClass().equals(parameterTypes[pCount-1].getTheClass());
            }
        }
        return false;
    }

    public final Object[] coerceArgumentsToClasses(Object[] arguments) {
        arguments = correctArguments(arguments);
        for (int i = 0; i < arguments.length; ++i) {
            final Object argument = arguments[i];
            if (argument != null) {
                arguments[i] = parameterTypes[i].coerceArgument(argument);
            }
        }
        return arguments;
    }

    public Object[] correctArguments(Object[] arguments) {
        if (arguments == null) {
            arguments = MetaClassHelper.EMPTY_ARRAY;
        }

        CachedClass[] pt = getParameterTypes();
        if (pt.length == 1 && arguments.length == 0) {
            if (!isVargsMethod()) return MetaClassHelper.ARRAY_WITH_NULL;
            return new Object[]{Array.newInstance(pt[0].getTheClass().getComponentType(), 0)};
        }

        if (isVargsMethod(arguments)) {
            return fitToVargs(arguments, pt);
        }

        return arguments;
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
            // is no array we have to wrap it in an array. If the last argument
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

    private static Object makeCommonArray(Object[] arguments, int offset, Class<?> baseType) {
        Object[] result = (Object[]) Array.newInstance(baseType, arguments.length - offset);
        for (int i = offset; i < arguments.length; ++i) {
            Object v = arguments[i];
            v = DefaultTypeTransformation.castToType(v, baseType);
            result[i - offset] = v;
        }
        return result;
    }

    public boolean isValidMethod(Class[] argumentTypes) {
        if (argumentTypes == null) return true;
        CachedClass[] pt = getParameterTypes();
        final int nArguments = argumentTypes.length, nParameters = pt.length, nthParameter = nParameters - 1;

        if (isVargsMethod() && nArguments >= nthParameter)
            return isValidVargsMethod(argumentTypes, pt, nthParameter);
        else if (nArguments == nParameters)
            return isValidExactMethod(argumentTypes, pt);
        else if (nArguments == 0 && nParameters == 1 && !pt[0].isPrimitive)
            return true; // implicit null argument
        return false;
    }

    private static boolean isValidExactMethod(Class[] arguments, CachedClass[] pt) {
        // let's check the parameter types match
        int size = pt.length;
        for (int i = 0; i < size; ++i) {
            if (!pt[i].isAssignableFrom(arguments[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidExactMethod(Object[] args) {
        // let's check the parameter types match
        getParametersTypes0();
        int size = args.length;
        if (size != parameterTypes.length)
            return false;

        for (int i = 0; i < size; ++i) {
            if (args[i] != null && !parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    public boolean isValidExactMethod(Class[] args) {
        // let's check the parameter types match
        getParametersTypes0();
        int size = args.length;
        if (size != parameterTypes.length)
            return false;

        for (int i = 0; i < size; ++i) {
            if (args[i] != null && !parameterTypes[i].isAssignableFrom(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean testComponentAssignable(Class toTestAgainst, Class toTest) {
        Class component = toTest.getComponentType();
        return component != null && MetaClassHelper.isAssignableFrom(toTestAgainst, component);
    }

    private static boolean isValidVargsMethod(Class[] argumentTypes, CachedClass[] parameterTypes, int nthParameter) {
        for (int i = 0; i < nthParameter; ++i) {
            if (!parameterTypes[i].isAssignableFrom(argumentTypes[i])) {
                return false;
            }
        }

        CachedClass arrayType = parameterTypes[nthParameter];
        Class componentType = arrayType.getTheClass().getComponentType();

        // check direct match
        if (argumentTypes.length == parameterTypes.length) {
            Class<?> argumentType = argumentTypes[nthParameter];
            if (arrayType.isAssignableFrom(argumentType)
                    || testComponentAssignable(componentType, argumentType)) {
                return true;
            }
        }

        // check vararg match
        for (int i = nthParameter; i < argumentTypes.length; ++i) {
            if (!MetaClassHelper.isAssignableFrom(componentType, argumentTypes[i])) {
                return false;
            }
        }

        return true;
    }

    public boolean isValidMethod(Object[] arguments) {
        if (arguments == null) return true;
        final CachedClass[] parameterTypes = getParameterTypes();
        final int nArguments = arguments.length, nParameters = parameterTypes.length, nthParameter = nParameters - 1;

        if (nParameters > 0 && parameterTypes[nthParameter].isArray && nArguments >= nthParameter) {
            for (int i = 0; i < nthParameter; ++i) {
                if (!parameterTypes[i].isAssignableFrom(getArgClass(arguments[i]))) {
                    return false;
                }
            }
            CachedClass arrayType = parameterTypes[nthParameter];
            Class componentType = arrayType.getTheClass().getComponentType();
            // check direct match
            if (nArguments == parameterTypes.length) {
                Class<?> argumentType = getArgClass(arguments[nthParameter]);
                if (arrayType.isAssignableFrom(argumentType)
                        || testComponentAssignable(componentType, argumentType)) {
                    return true;
                }
            }
            // check vararg match
            for (int i = nthParameter; i < nArguments; ++i) {
                if (!MetaClassHelper.isAssignableFrom(componentType, getArgClass(arguments[i]))) {
                    return false;
                }
            }
            return true;
        } else if (nArguments == nParameters) {
            for (int i = 0; i < nArguments; ++i) {
                if (!parameterTypes[i].isAssignableFrom(getArgClass(arguments[i]))) {
                    return false;
                }
            }
            return true;
        } else if (nArguments == 0 && nParameters == 1 && !parameterTypes[0].isPrimitive) {
            return true; // implicit null argument
        }
        return false;
    }

    private static Class<?> getArgClass(final Object arg) {
        return arg == null ? null : (arg instanceof Wrapper ? ((Wrapper) arg).getType() : arg.getClass());
    }
}
