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
package org.codehaus.groovy.vmplugin.v7;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.util.ProxyGenerator;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.ConvertedClosure;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.transform.trait.Traits;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class contains several transformers for used during method invocation.
 */
public class TypeTransformers {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle
            TO_STRING, TO_BYTE, TO_INT, TO_LONG, TO_SHORT,
            TO_FLOAT, TO_DOUBLE, TO_BIG_INT, AS_ARRAY,
            TO_REFLECTIVE_PROXY, TO_GENERATED_PROXY, TO_SAMTRAIT_PROXY,
            DOUBLE_TO_BIG_DEC, DOUBLE_TO_BIG_DEC_WITH_CONVERSION, LONG_TO_BIG_DEC, BIG_INT_TO_BIG_DEC;

    static {
        try {
            TO_STRING = LOOKUP.findVirtual(Object.class, "toString", MethodType.methodType(String.class));
            TO_BYTE = LOOKUP.findVirtual(Number.class, "byteValue", MethodType.methodType(Byte.TYPE));
            TO_SHORT = LOOKUP.findVirtual(Number.class, "shortValue", MethodType.methodType(Short.TYPE));
            TO_INT = LOOKUP.findVirtual(Number.class, "intValue", MethodType.methodType(Integer.TYPE));
            TO_LONG = LOOKUP.findVirtual(Number.class, "longValue", MethodType.methodType(Long.TYPE));
            TO_FLOAT = LOOKUP.findVirtual(Number.class, "floatValue", MethodType.methodType(Float.TYPE));
            TO_DOUBLE = LOOKUP.findVirtual(Number.class, "doubleValue", MethodType.methodType(Double.TYPE));

            // BigDecimal conversion is done by using the double value
            DOUBLE_TO_BIG_DEC = LOOKUP.findConstructor(BigDecimal.class, MethodType.methodType(Void.TYPE, Double.TYPE));
            DOUBLE_TO_BIG_DEC_WITH_CONVERSION = MethodHandles.filterReturnValue(TO_DOUBLE, DOUBLE_TO_BIG_DEC);

            // BigDecimal conversion is done by using the double value
            LONG_TO_BIG_DEC = LOOKUP.findConstructor(BigDecimal.class, MethodType.methodType(Void.TYPE, Long.TYPE));

            // BigDecimal conversion is done by using the double value
            BIG_INT_TO_BIG_DEC = LOOKUP.findConstructor(BigDecimal.class, MethodType.methodType(Void.TYPE, BigInteger.class));


            // BigInteger conversion is done by using the string representation
            MethodHandle tmp = LOOKUP.findConstructor(BigInteger.class, MethodType.methodType(Void.TYPE, String.class));
            TO_BIG_INT = MethodHandles.filterReturnValue(TO_STRING, tmp);

            // generic array to array conversion
            AS_ARRAY = LOOKUP.findStatic(DefaultTypeTransformation.class, "asArray", MethodType.methodType(Object.class, Object.class, Class.class));

            // reflective proxy generation, since we need a ConvertedClosure but have only a normal Closure, we need to create that wrapper object as well
            MethodHandle newProxyInstance = LOOKUP.findStatic(Proxy.class, "newProxyInstance",
                    MethodType.methodType(Object.class, ClassLoader.class, Class[].class, InvocationHandler.class));
            MethodHandle newConvertedClosure = LOOKUP.findConstructor(ConvertedClosure.class, MethodType.methodType(Void.TYPE, Closure.class, String.class));
            // prepare target newProxyInstance for fold to drop additional arguments needed by newConvertedClosure
            MethodType newOrder = newProxyInstance.type().dropParameterTypes(2, 3);
            newOrder = newOrder.insertParameterTypes(0, InvocationHandler.class, Closure.class, String.class);
            tmp = MethodHandles.permuteArguments(newProxyInstance, newOrder, 3, 4, 0);
            // execute fold:
            TO_REFLECTIVE_PROXY = MethodHandles.foldArguments(tmp, newConvertedClosure.asType(newConvertedClosure.type().changeReturnType(InvocationHandler.class)));

            {
                // generated proxy using a map to store the closure
                MethodHandle map = LOOKUP.findStatic(Collections.class, "singletonMap",
                        MethodType.methodType(Map.class, Object.class, Object.class));
                newProxyInstance = LOOKUP.findVirtual(ProxyGenerator.class, "instantiateAggregateFromBaseClass",
                        MethodType.methodType(GroovyObject.class, Map.class, Class.class));
                newOrder = newProxyInstance.type().dropParameterTypes(1, 2);
                newOrder = newOrder.insertParameterTypes(0, Map.class, Object.class, Object.class);
                tmp = MethodHandles.permuteArguments(newProxyInstance, newOrder, 3, 0, 4);
                tmp = MethodHandles.foldArguments(tmp, map);
                TO_GENERATED_PROXY = tmp;
            }
            {
                // Trait SAM coercion generated proxy using a map to store the closure
                MethodHandle map = LOOKUP.findStatic(Collections.class, "singletonMap",
                        MethodType.methodType(Map.class, Object.class, Object.class));
                newProxyInstance = LOOKUP.findVirtual(ProxyGenerator.class, "instantiateAggregate",
                        MethodType.methodType(GroovyObject.class, Map.class, List.class));
                newOrder = newProxyInstance.type().dropParameterTypes(1, 2);
                newOrder = newOrder.insertParameterTypes(0, Map.class, Object.class, Object.class);
                tmp = MethodHandles.permuteArguments(newProxyInstance, newOrder, 3, 0, 4);
                tmp = MethodHandles.foldArguments(tmp, map);
                TO_SAMTRAIT_PROXY = tmp;
            }
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }

    /**
     * Adds a type transformer applied at runtime.
     * This method handles transformations to String from GString,
     * array transformations and number based transformations
     */
    protected static MethodHandle addTransformer(MethodHandle handle, int pos, Object arg, Class<?> parameter) {
        MethodHandle transformer = null;
        if (arg instanceof GString) {
            transformer = TO_STRING;
        } else if (arg instanceof Closure) {
            transformer = createSAMTransform(arg, parameter);
        } else if (Number.class.isAssignableFrom(parameter)) {
            transformer = selectNumberTransformer(parameter, arg);
        } else if (parameter.isArray()) {
            transformer = MethodHandles.insertArguments(AS_ARRAY, 1, parameter);
        }
        if (transformer == null)
            throw new GroovyBugError("Unknown transformation for argument " + arg + " at position " + pos + " with " + arg.getClass() + " for parameter of type " + parameter);
        return applyUnsharpFilter(handle, pos, transformer);
    }

    /**
     * creates a method handle able to transform the given Closure into a SAM type
     * if the given parameter is a SAM type
     */
    private static MethodHandle createSAMTransform(Object arg, Class<?> parameter) {
        Method method = CachedSAMClass.getSAMMethod(parameter);
        if (method == null) return null;
        // TODO: have to think about how to optimize this!
        if (parameter.isInterface()) {
            if (Traits.isTrait(parameter)) {
                // the following code will basically do this:
                // Map<String,Closure> impl = Collections.singletonMap(method.getName(),arg);
                // return ProxyGenerator.INSTANCE.instantiateAggregate(impl,Collections.singletonList(clazz));
                // TO_SAMTRAIT_PROXY is a handle (Object,Object,ProxyGenerator,Class)GroovyObject
                // where the second object is the input closure, everything else
                // needs to be provide and is in remaining order: method name,
                // ProxyGenerator.INSTANCE and singletonList(parameter)
                MethodHandle ret = TO_SAMTRAIT_PROXY;
                ret = MethodHandles.insertArguments(ret, 2, ProxyGenerator.INSTANCE, Collections.singletonList(parameter));
                ret = MethodHandles.insertArguments(ret, 0, method.getName());
                return ret;
            }
            // the following code will basically do this:
            // return Proxy.newProxyInstance(
            //        arg.getClass().getClassLoader(),
            //        new Class[]{parameter},
            //        new ConvertedClosure((Closure) arg));
            // TO_REFLECTIVE_PROXY will do that for us, though
            // input is the closure, the method name, the class loader and the 
            // class[]. All of that but the closure must be provided here  
            MethodHandle ret = TO_REFLECTIVE_PROXY;
            ret = MethodHandles.insertArguments(ret, 1,
                    method.getName(),
                    arg.getClass().getClassLoader(),
                    new Class[]{parameter});
            return ret;
        } else {
            // the following code will basically do this:
            //Map<String, Object> m = Collections.singletonMap(method.getName(), arg);
            //return ProxyGenerator.INSTANCE.
            //            instantiateAggregateFromBaseClass(m, parameter);
            // TO_GENERATED_PROXY is a handle (Object,Object,ProxyGenerator,Class)GroovyObject
            // where the second object is the input closure, everything else
            // needs to be provide and is in remaining order: method name, 
            // ProxyGenerator.INSTANCE and parameter
            MethodHandle ret = TO_GENERATED_PROXY;
            ret = MethodHandles.insertArguments(ret, 2, ProxyGenerator.INSTANCE, parameter);
            ret = MethodHandles.insertArguments(ret, 0, method.getName());
            return ret;
        }
    }

    /**
     * Apply a transformer as filter.
     * The filter may not match exactly in the types. In this case needed
     * additional type transformations are done by {@link MethodHandle#asType(MethodType)}
     */
    public static MethodHandle applyUnsharpFilter(MethodHandle handle, int pos, MethodHandle transformer) {
        MethodType type = transformer.type();
        Class<?> given = handle.type().parameterType(pos);
        if (type.returnType() != given || type.parameterType(0) != given) {
            transformer = transformer.asType(MethodType.methodType(given, type.parameterType(0)));
        }
        return MethodHandles.filterArguments(handle, pos, transformer);
    }

    /**
     * returns a transformer later applied as filter to transform one
     * number into another
     */
    private static MethodHandle selectNumberTransformer(Class<?> param, Object arg) {
        param = TypeHelper.getWrapperClass(param);

        if (param == Byte.class) {
            return TO_BYTE;
        } else if (param == Character.class || param == Integer.class) {
            return TO_INT;
        } else if (param == Long.class) {
            return TO_LONG;
        } else if (param == Float.class) {
            return TO_FLOAT;
        } else if (param == Double.class) {
            return TO_DOUBLE;
        } else if (param == BigInteger.class) {
            return TO_BIG_INT;
        } else if (param == BigDecimal.class) {
            if (arg instanceof Double) {
                return DOUBLE_TO_BIG_DEC;
            } else if (arg instanceof Long) {
                return LONG_TO_BIG_DEC;
            } else if (arg instanceof BigInteger) {
                return BIG_INT_TO_BIG_DEC;
            } else {
                return DOUBLE_TO_BIG_DEC_WITH_CONVERSION;
            }
        } else if (param == Short.class) {
            return TO_SHORT;
        } else {
            return null;
        }
    }
}
