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
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.reflect.Array;

/**
 * Manages parameter type information for methods and constructors, supporting lazy initialization.
 * <p>
 * Stores both cached class representations and native Java {@code Class} objects,
 * initializing them on-demand with thread-safe synchronization.
 */
public class ParameterTypes {

    private volatile CachedClass[] parameterTypes;
    private volatile Class[] nativeParamTypes;
    private boolean isVargsMethod;

    /**
     * Constructs a {@code ParameterTypes} with uninitialized parameter types.
     */
    public ParameterTypes() {
    }

    /**
     * Constructs a {@code ParameterTypes} with the specified native parameter types.
     *
     * @param pt the native Java {@code Class} array representing method parameters
     */
    public ParameterTypes(Class[] pt) {
        nativeParamTypes = pt;
    }

    /**
     * Constructs a {@code ParameterTypes} from class names.
     *
     * @param pt an array of fully qualified class names
     * @deprecated Use {@link #ParameterTypes(Class[])} instead
     * @throws NoClassDefFoundError if any class name cannot be resolved
     */
    @Deprecated
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

    /**
     * Constructs a {@code ParameterTypes} with the specified cached parameter types.
     *
     * @param pt an array of cached class objects representing method parameters
     */
    public ParameterTypes(CachedClass[] pt) {
        setParametersTypes(pt);
    }

    /**
     * Sets the cached parameter types and determines if this is a varargs method.
     *
     * @param pt the array of cached parameter types
     */
    protected final void setParametersTypes(CachedClass[] pt) {
        this.parameterTypes = pt;
        isVargsMethod = pt.length > 0 && pt[pt.length - 1].isArray;
    }

    /**
     * Returns the cached class representations of the parameter types.
     * Lazily initializes the types on first access.
     *
     * @return an array of cached parameter types
     */
    public CachedClass[] getParameterTypes() {
        if (parameterTypes == null) {
            getParametersTypes0();
        }
        return parameterTypes;
    }

    private synchronized void getParametersTypes0() {
        if (parameterTypes == null) {
            var npt = nativeParamTypes == null ? getPT() : nativeParamTypes;
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

    /**
     * Returns the native Java {@code Class} objects for the parameter types.
     * Lazily initializes the types on first access.
     *
     * @return an array of native parameter classes
     */
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

    /**
     * Protected method subclasses override to provide parameter types.
     * Default implementation throws {@code UnsupportedOperationException}.
     *
     * @return the native parameter class array
     * @throws UnsupportedOperationException if not overridden by subclass
     */
    protected Class[] getPT() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    /**
     * Returns whether this represents a varargs (variable arguments) method.
     *
     * @return {@code true} if the last parameter is an array type, {@code false} otherwise
     */
    public boolean isVargsMethod() {
        if (parameterTypes == null) {
            getParametersTypes0(); // GROOVY-11293
        }
        return isVargsMethod;
    }

    /**
     * Checks if this varargs method should treat arguments as a varargs invocation.
     *
     * @param arguments the actual arguments passed to the method
     * @return {@code true} if varargs conversion is needed, {@code false} otherwise
     */
    public boolean isVargsMethod(Object[] arguments) {
        if (isVargsMethod()) {
            int aCount = arguments.length;
            int pCount = parameterTypes.length;
            if (aCount > pCount || aCount == pCount-1) { // too many or too few?
                return true;
            }
            if (aCount == pCount) {
                Object last = arguments[aCount-1]; // is null or different type?
                return last == null || !getArgClass(last).equals(parameterTypes[pCount-1].getTheClass());
            }
        }
        return false;
    }

    /**
     * Coerces arguments to match the expected parameter types, handling type conversions and varargs expansion.
     * First corrects argument count for varargs methods, then coerces each argument to its target type.
     *
     * @param arguments the arguments to coerce
     * @return the coerced argument array
     * @throws IllegalArgumentException if null is passed where a primitive type is expected
     */
    public final Object[] coerceArgumentsToClasses(Object[] arguments) {
        arguments = correctArguments(arguments);
        // TODO: if isVargsMethod, coerce array items
        for (int i = 0; i != arguments.length; ++i) {
            var argument = arguments[i];
            if (argument instanceof Wrapper)
                argument = ((Wrapper) argument).unwrap();
            if (argument != null) {
                arguments[i] = parameterTypes[i].coerceArgument(argument);
            } else if (parameterTypes[i].isPrimitive()) { // GROOVY-11203, et al.
                throw new IllegalArgumentException("Cannot call method with null" +
                    " for parameter " + i + ", which expects " + parameterTypes[i]);
            } else {
                arguments[i] = null; // GROOVY-11930
            }
        }
        return arguments;
    }

    /**
     * Corrects argument count to match method signature, handling varargs expansion and null filling.
     * Transforms arguments to match the expected parameter count by expanding varargs arrays or filling nulls.
     *
     * @param arguments the arguments to correct
     * @return the corrected argument array
     */
    public Object[] correctArguments(Object[] arguments) {
        if (arguments == null) {
            arguments = MetaClassHelper.EMPTY_ARRAY;
        }

        var pt = getParameterTypes();
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
     * @param arguments the arguments used to call the method
     * @param paramTypes        the types of the parameters the method takes
     */
    private static Object[] fitToVargs(final Object[] arguments, final CachedClass[] paramTypes) {
        int aCount = arguments.length, pCount = paramTypes.length;
        var vaType = paramTypes[pCount-1].getTheClass();
        Object[] unwrappedArguments = arguments.clone();
        MetaClassHelper.unwrap(unwrappedArguments);

        // get type of each vargs element -- arguments are not primitive
        vaType = TypeUtil.autoboxType(vaType.getComponentType());

        if (aCount == pCount - 1) {
            // one argument is missing, so fill it with an empty array
            Object[] args = new Object[pCount];
            System.arraycopy(unwrappedArguments, 0, args, 0, aCount);
            args[aCount] = Array.newInstance(vaType, 0);
            return args;
        } else if (aCount == pCount) {
            // the number of arguments is correct, but if the last argument
            // is no array we have to wrap it in an array; if last argument
            // is null, then we don't have to do anything
            var lastArgument = getArgClass(arguments[aCount - 1]);
            if (lastArgument != null && !lastArgument.isArray()) {
                Object[] args = new Object[pCount];
                System.arraycopy(unwrappedArguments, 0, args, 0, pCount - 1);
                args[pCount-1] = makeCommonArray(unwrappedArguments, pCount - 1, vaType);
                return args;
            } else {
                // we may have to box the argument!
                return unwrappedArguments;
            }
        } else if (aCount > pCount) {
            // wrap tail arguments in an array
            Object[] args = new Object[pCount];
            System.arraycopy(unwrappedArguments, 0, args, 0, pCount - 1);
            args[pCount-1] = makeCommonArray(unwrappedArguments, pCount - 1, vaType);
            return args;
        } else {
            throw new GroovyBugError("trying to call a vargs method without enough arguments");
        }
    }

    private static Object makeCommonArray(Object[] arguments, int offset, Class<?> baseType) {
        Object[] result = (Object[]) Array.newInstance(baseType, arguments.length - offset);
        for (int i = offset; i != arguments.length; ++i) {
            Object v = arguments[i];
            v = DefaultTypeTransformation.castToType(v, baseType);
            result[i - offset] = v;
        }
        return result;
    }

    /**
     * Checks if the given argument types are valid for this method, considering varargs conversion.
     *
     * @param argumentTypes the argument types to validate
     * @return {@code true} if the types match the method parameters; {@code false} otherwise
     */
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
        for (int i = 0; i != pt.length; ++i) {
            if (!pt[i].isAssignableFrom(arguments[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given actual arguments exactly match this method's parameter types.
     * All arguments must be assignable to their corresponding parameters.
     *
     * @param args the actual arguments to validate
     * @return {@code true} if the arguments are valid for exact invocation; {@code false} otherwise
     */
    public boolean isValidExactMethod(Object[] args) {
        // let's check the parameter types match
        getParametersTypes0();
        int size = args.length;
        if (size != parameterTypes.length)
            return false;

        for (int i = 0; i != size; ++i) {
            final Object arg = args[i];
            if (arg != null && !parameterTypes[i].isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given argument types exactly match this method's parameter types.
     * All types must be assignable to their corresponding parameters.
     *
     * @param args the argument types to validate
     * @return {@code true} if the types are valid for exact invocation; {@code false} otherwise
     */
    public boolean isValidExactMethod(Class[] args) {
        // let's check the parameter types match
        getParametersTypes0();
        int size = args.length;
        if (size != parameterTypes.length)
            return false;

        for (int i = 0; i != size; ++i) {
            var arg = args[i];
            if (arg != null && !parameterTypes[i].isAssignableFrom(arg)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidVargsMethod(Class[] argumentTypes, CachedClass[] parameterTypes, int nthParameter) {
        for (int i = 0; i < nthParameter; i += 1) {
            if (!parameterTypes[i].isAssignableFrom(argumentTypes[i])) {
                return false;
            }
        }

        CachedClass arrayType = parameterTypes[nthParameter];
        CachedClass componentType = ReflectionCache.getCachedClass(arrayType.getTheClass().getComponentType());

        // check direct match
        if (argumentTypes.length == parameterTypes.length) {
            var argumentType = argumentTypes[nthParameter];
            if (arrayType.isAssignableFrom(argumentType) || (argumentType.isArray()
                    && componentType.isAssignableFrom(argumentType.getComponentType()))) {
                return true;
            }
        }

        // check vararg match
        for (int i = nthParameter; i < argumentTypes.length; i += 1) {
            if (!componentType.isAssignableFrom(argumentTypes[i])) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks if the given actual arguments are valid for this method, considering varargs conversion.
     * @param arguments the actual arguments to validate
     * @return {@code true} if the arguments are valid for this method; {@code false} otherwise
     */
    public boolean isValidMethod(Object[] arguments) {
        if (arguments == null) return true;
        final CachedClass[] parameterTypes = getParameterTypes();
        final int nArguments = arguments.length, nParameters = parameterTypes.length, nthParameter = nParameters - 1;

        if (nParameters > 0 && parameterTypes[nthParameter].isArray && nArguments >= nthParameter) {
            for (int i = 0; i < nthParameter; i += 1) {
                if (!parameterTypes[i].isAssignableFrom(getArgClass(arguments[i]))) {
                    return false;
                }
            }
            CachedClass arrayType = parameterTypes[nthParameter];
            CachedClass componentType = ReflectionCache.getCachedClass(arrayType.getTheClass().getComponentType());
            // check direct match
            if (nArguments == parameterTypes.length) {
                var argumentType = getArgClass(arguments[nthParameter]);
                if (arrayType.isAssignableFrom(argumentType) || (argumentType.isArray()
                        && componentType.isAssignableFrom(argumentType.getComponentType()))) {
                    return true;
                }
            }
            // check vararg match
            for (int i = nthParameter; i < nArguments; i += 1) {
                if (!componentType.isAssignableFrom(getArgClass(arguments[i]))) {
                    return false;
                }
            }
            return true;
        } else if (nArguments == nParameters) {
            for (int i = 0; i < nArguments; i += 1) {
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
