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
package org.codehaus.groovy.vmplugin.v8;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import groovy.lang.MetaObjectProtocol;
import groovy.lang.MetaProperty;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.metaclass.MissingMethodExecutionFailed;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.codehaus.groovy.runtime.wrappers.Wrapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import static org.codehaus.groovy.vmplugin.v8.IndyInterface.LOOKUP;

/**
 * This class contains guards, runtime filters and
 * MethodType signatures used by indy.
 */
public class IndyGuardsFiltersAndSignatures {

    private static final MethodType
            ZERO_GUARD = MethodType.methodType(boolean.class),
            OBJECT_GUARD = MethodType.methodType(boolean.class, Object.class),
            CLASS1_GUARD = MethodType.methodType(boolean.class, Class.class, Object.class),
            METACLASS1_GUARD = MethodType.methodType(boolean.class, MetaClass.class, Object.class),

    GRE_GUARD = MethodType.methodType(Object.class, GroovyRuntimeException.class),

    OBJECT_FILTER = MethodType.methodType(Object.class, Object.class),

    BOUND_INVOKER = MethodType.methodType(Object.class, Object[].class),
            ANO_INVOKER = MethodType.methodType(Object.class, Object.class, Object[].class),
            INVOKER = MethodType.methodType(Object.class, Object.class, String.class, Object[].class),
            GET_INVOKER = MethodType.methodType(Object.class, String.class);

    protected static final MethodHandle
            SAME_CLASS, UNWRAP_METHOD,
            SAME_MC, IS_NULL,
            UNWRAP_EXCEPTION, META_METHOD_INVOKER,
            GROOVY_OBJECT_INVOKER, GROOVY_OBJECT_GET_PROPERTY,
            HAS_CATEGORY_IN_CURRENT_THREAD_GUARD,
            BEAN_CONSTRUCTOR_PROPERTY_SETTER,
            META_PROPERTY_GETTER,
            SLOW_META_CLASS_FIND, META_CLASS_INVOKE_STATIC_METHOD,
            MOP_GET, MOP_INVOKE_CONSTRUCTOR, MOP_INVOKE_METHOD,
            INTERCEPTABLE_INVOKER,
            CLASS_FOR_NAME, BOOLEAN_IDENTITY,
            DTT_CAST_TO_TYPE, SAM_CONVERSION,
            HASHSET_CONSTRUCTOR, ARRAYLIST_CONSTRUCTOR, GROOVY_CAST_EXCEPTION,
            EQUALS;

    static {
        try {
            SAME_CLASS = LOOKUP.findStatic(IndyGuardsFiltersAndSignatures.class, "sameClass", CLASS1_GUARD);
            UNWRAP_METHOD = LOOKUP.findStatic(IndyGuardsFiltersAndSignatures.class, "unwrap", OBJECT_FILTER);
            SAME_MC = LOOKUP.findStatic(IndyGuardsFiltersAndSignatures.class, "isSameMetaClass", METACLASS1_GUARD);
            IS_NULL = LOOKUP.findStatic(IndyGuardsFiltersAndSignatures.class, "isNull", OBJECT_GUARD);
            UNWRAP_EXCEPTION = LOOKUP.findStatic(IndyGuardsFiltersAndSignatures.class, "unwrap", GRE_GUARD);
            GROOVY_OBJECT_INVOKER = LOOKUP.findStatic(IndyGuardsFiltersAndSignatures.class, "invokeGroovyObjectInvoker", INVOKER.insertParameterTypes(0, MissingMethodException.class));

            META_METHOD_INVOKER = LOOKUP.findVirtual(MetaMethod.class, "doMethodInvoke", ANO_INVOKER);
            HAS_CATEGORY_IN_CURRENT_THREAD_GUARD = LOOKUP.findStatic(GroovyCategorySupport.class, "hasCategoryInCurrentThread", ZERO_GUARD);
            GROOVY_OBJECT_GET_PROPERTY = LOOKUP.findVirtual(GroovyObject.class, "getProperty", GET_INVOKER);
            META_CLASS_INVOKE_STATIC_METHOD = LOOKUP.findVirtual(MetaObjectProtocol.class, "invokeStaticMethod", INVOKER);

            BEAN_CONSTRUCTOR_PROPERTY_SETTER = LOOKUP.findStatic(IndyGuardsFiltersAndSignatures.class, "setBeanProperties", MethodType.methodType(Object.class, MetaClass.class, Object.class, Map.class));
            META_PROPERTY_GETTER = LOOKUP.findVirtual(MetaProperty.class, "getProperty", OBJECT_FILTER);
            MOP_GET = LOOKUP.findVirtual(MetaObjectProtocol.class, "getProperty", MethodType.methodType(Object.class, Object.class, String.class));
            MOP_INVOKE_CONSTRUCTOR = LOOKUP.findVirtual(MetaObjectProtocol.class, "invokeConstructor", BOUND_INVOKER);
            MOP_INVOKE_METHOD = LOOKUP.findVirtual(MetaObjectProtocol.class, "invokeMethod", INVOKER);
            SLOW_META_CLASS_FIND = LOOKUP.findStatic(InvokerHelper.class, "getMetaClass", MethodType.methodType(MetaClass.class, Object.class));
            INTERCEPTABLE_INVOKER = LOOKUP.findVirtual(GroovyObject.class, "invokeMethod", MethodType.methodType(Object.class, String.class, Object.class));

            CLASS_FOR_NAME = LOOKUP.findStatic(Class.class, "forName", MethodType.methodType(Class.class, String.class, boolean.class, ClassLoader.class));

            BOOLEAN_IDENTITY = MethodHandles.identity(Boolean.class);
            DTT_CAST_TO_TYPE = LOOKUP.findStatic(DefaultTypeTransformation.class, "castToType", MethodType.methodType(Object.class, Object.class, Class.class));
            SAM_CONVERSION = LOOKUP.findStatic(CachedSAMClass.class, "coerceToSAM", MethodType.methodType(Object.class, Closure.class, Method.class, Class.class));
            HASHSET_CONSTRUCTOR = LOOKUP.findConstructor(HashSet.class, MethodType.methodType(void.class, Collection.class));
            ARRAYLIST_CONSTRUCTOR = LOOKUP.findConstructor(ArrayList.class, MethodType.methodType(void.class, Collection.class));
            GROOVY_CAST_EXCEPTION = LOOKUP.findConstructor(GroovyCastException.class, MethodType.methodType(void.class, Object.class, Class.class));

            EQUALS = LOOKUP.findVirtual(Object.class, "equals", OBJECT_GUARD);
        } catch (Exception e) {
            throw new GroovyBugError(e);
        }
    }

    protected static final MethodHandle NULL_REF = MethodHandles.constant(Object.class, null);

    /**
     * This method is called by he handle to realize the bean constructor
     * with property map.
     */
    public static Object setBeanProperties(MetaClass mc, Object bean, Map properties) {
        for (Object o : properties.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = entry.getKey().toString();

            Object value = entry.getValue();
            mc.setProperty(bean, key, value);
        }
        return bean;
    }

    /**
     * {@link GroovyObject#invokeMethod(String, Object)} path as fallback.
     * This method is called by the handle as exception handler in case the
     * selected method causes a MissingMethodExecutionFailed, where
     * we will just give through the exception, and a normal
     * MissingMethodException where we call {@link GroovyObject#invokeMethod(String, Object)}
     * if receiver class, the type transported by the exception and the name
     * for the method stored in the exception and our current method name
     * are equal.
     * Should those conditions not apply we just rethrow the exception.
     */
    public static Object invokeGroovyObjectInvoker(MissingMethodException e, Object receiver, String name, Object[] args) {
        if (e instanceof MissingMethodExecutionFailed) {
            throw (MissingMethodException) e.getCause();
        } else if (receiver.getClass() == e.getType() && e.getMethod().equals(name)) {
            //TODO: we should consider calling this one directly for MetaClassImpl,
            //      then we save the new method selection

            // in case there's nothing else, invoke the object's own invokeMethod()
            return ((GroovyObject) receiver).invokeMethod(name, args);
        } else {
            throw e;
        }
    }

    /**
     * Unwraps a {@link GroovyRuntimeException}.
     * This method is called by the handle to unwrap internal exceptions
     * of the runtime.
     */
    public static Object unwrap(GroovyRuntimeException gre) throws Throwable {
        throw ScriptBytecodeAdapter.unwrap(gre);
    }

    /**
     * called by handle
     */
    public static boolean isSameMetaClass(MetaClass mc, Object receiver) {
        //TODO: remove this method if possible by switchpoint usage
        return receiver instanceof GroovyObject && mc == ((GroovyObject) receiver).getMetaClass();
    }

    /**
     * Unwraps a {@link Wrapper}.
     * This method is called by the handle to unwrap a Wrapper, which
     * we use to force method selection.
     */
    public static Object unwrap(Object o) {
        Wrapper w = (Wrapper) o;
        return w.unwrap();
    }

    /**
     * Guard to check if the argument is null.
     * This method is called by the handle to check
     * if the provided argument is null.
     */
    public static boolean isNull(Object o) {
        return o == null;
    }

    /**
     * Guard to check if the provided Object has the same
     * class as the provided Class. This method will
     * return false if the Object is null.
     */
    public static boolean sameClass(Class<?> c, Object o) {
        if (o == null) return false;
        return o.getClass() == c;
    }
}
