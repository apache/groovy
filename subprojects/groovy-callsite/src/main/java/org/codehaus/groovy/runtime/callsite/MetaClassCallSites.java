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
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.AdaptingMetaClass;
import groovy.lang.ExpandoMetaClass;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedConstructor;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass;

import java.util.Map;

/**
 * Builds classic (non-indy) call sites for a {@link MetaClassImpl}.
 * <p>
 * Logic formerly lived as {@code create*CallSite} methods on
 * {@link MetaClassImpl} / {@link ExpandoMetaClass} / {@link ClosureMetaClass}.
 * It uses only public MetaClass APIs so this class can live with the rest of
 * the classic call-site runtime without a split-package on {@code groovy.lang}.
 *
 * @since 6.0.0
 * @deprecated Prefer invokedynamic call sites; this class will be removed in a future major version.
 */
@Deprecated
public final class MetaClassCallSites {

    private static final String CALL_METHOD = "call";
    private static final String DO_CALL_METHOD = "doCall";
    private static final String CONSTRUCTOR_NAME = "<init>";

    private MetaClassCallSites() {
    }

    /**
     * Creates a POJO call site for the given metaclass.
     */
    public static CallSite createPojoCallSite(final MetaClassImpl metaClass, final CallSite site,
                                              final Object receiver, final Object[] args) {
        if (usesCustomInvokeMethod(metaClass)) {
            return new PojoMetaClassSite(site, metaClass);
        }
        if (metaClass instanceof ClosureMetaClass) {
            throw new UnsupportedOperationException();
        }
        if (isAdapting(metaClass)) {
            return new PojoMetaClassSite(site, metaClass);
        }
        Class[] params = MetaClassHelper.convertToTypeArray(args);
        MetaMethod metaMethod = metaClass.getMethodWithCaching(metaClass.getTheClass(), site.getName(), params);
        if (metaMethod != null) {
            return PojoMetaMethodSite.createPojoMetaMethodSite(site, metaClass, metaMethod, params, receiver, args);
        }
        return new PojoMetaClassSite(site, metaClass);
    }

    /**
     * Creates a static call site for the given metaclass.
     */
    public static CallSite createStaticSite(final MetaClassImpl metaClass, final CallSite site, final Object[] args) {
        if (metaClass instanceof ExpandoMetaClass emc && emc.hasCustomStaticInvokeMethod()) {
            return new StaticMetaClassSite(site, metaClass);
        }
        if (isAdapting(metaClass)) {
            return new StaticMetaClassSite(site, metaClass);
        }
        Class[] params = MetaClassHelper.convertToTypeArray(args);
        MetaMethod metaMethod = metaClass.retrieveStaticMethod(site.getName(), args);
        if (metaMethod != null) {
            return StaticMetaMethodSite.createStaticMetaMethodSite(site, metaClass, metaMethod, params, args);
        }
        return new StaticMetaClassSite(site, metaClass);
    }

    /**
     * Creates a POGO call site for the given metaclass.
     */
    public static CallSite createPogoCallSite(final MetaClassImpl metaClass, final CallSite site, final Object[] args) {
        if (usesCustomInvokeMethod(metaClass) || metaClass instanceof ClosureMetaClass) {
            return new PogoMetaClassSite(site, metaClass);
        }
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || isAdapting(metaClass)) {
            return new PogoMetaClassSite(site, metaClass);
        }
        Class[] params = MetaClassHelper.convertToTypeArray(args);
        String methodName = site.getName();
        // GROOVY-5806: select doCall but keep the original call-site name
        if (CALL_METHOD.equals(methodName) && isGroovyFunctor(metaClass)) {
            methodName = DO_CALL_METHOD;
        }
        MetaMethod metaMethod = metaClass.getMethodWithCaching(metaClass.getTheClass(), methodName, params);
        if (metaMethod != null) {
            return PogoMetaMethodSite.createPogoMetaMethodSite(site, metaClass, metaMethod, params, args);
        }
        return new PogoMetaClassSite(site, metaClass);
    }

    /**
     * Creates a current-scope POGO call site for the given metaclass.
     */
    public static CallSite createPogoCallCurrentSite(final MetaClassImpl metaClass, final CallSite site,
                                                     final Class sender, final Object[] args) {
        if (usesCustomInvokeMethod(metaClass) || metaClass instanceof ClosureMetaClass) {
            return new PogoMetaClassSite(site, metaClass);
        }
        if (GroovyCategorySupport.hasCategoryInCurrentThread() || isAdapting(metaClass)) {
            return new PogoMetaClassSite(site, metaClass);
        }
        Class[] params = MetaClassHelper.convertToTypeArray(args);
        MetaMethod metaMethod = metaClass.getMethodWithCaching(sender, site.getName(), params);
        if (metaMethod != null) {
            return PogoMetaMethodSite.createPogoMetaMethodSite(site, metaClass, metaMethod, params, args);
        }
        return new PogoMetaClassSite(site, metaClass);
    }

    /**
     * Creates a constructor call site for the given metaclass.
     */
    public static CallSite createConstructorSite(final MetaClassImpl metaClass, final CallSite site, final Object[] args) {
        if (metaClass instanceof ExpandoMetaClass emc) {
            Class<?>[] params = MetaClassHelper.convertToTypeArray(args);
            MetaMethod method = emc.pickMethod(CONSTRUCTOR_NAME, params);
            if (method != null && method.getParameterTypes().length == args.length
                    && method.getDeclaringClass().getTheClass().equals(emc.getTheClass())) {
                return new ConstructorMetaMethodSite(site, emc, method, params);
            }
        }
        if (isAdapting(metaClass)) {
            return new MetaClassConstructorSite(site, metaClass);
        }
        Class[] argTypes = MetaClassHelper.convertToTypeArray(args);
        CachedConstructor constructor = metaClass.chooseConstructor(argTypes);
        if (constructor != null) {
            return ConstructorSite.createConstructorSite(site, metaClass, constructor, argTypes, args);
        }
        if ((args.length == 1 && args[0] instanceof Map)
                || (args.length == 2 && args[1] instanceof Map
                && metaClass.getTheClass().getEnclosingClass() != null
                && metaClass.getTheClass().getEnclosingClass().isAssignableFrom(argTypes[0]))) {
            Object selected = metaClass.chooseNamedArgCompatibleConstructor(argTypes, args);
            if (selected instanceof CachedConstructor namedArgCtor) {
                return args.length == 1
                        ? new ConstructorSite.NoParamSite(site, metaClass, namedArgCtor, argTypes)
                        : new ConstructorSite.NoParamSiteInnerClass(site, metaClass, namedArgCtor, argTypes);
            }
        }
        return new MetaClassConstructorSite(site, metaClass);
    }

    private static boolean usesCustomInvokeMethod(final MetaClassImpl metaClass) {
        return metaClass instanceof ExpandoMetaClass emc && emc.hasCustomInvokeMethod();
    }

    private static boolean isAdapting(final MetaClassImpl metaClass) {
        return metaClass instanceof AdaptingMetaClass;
    }

    private static boolean isGroovyFunctor(final MetaClassImpl metaClass) {
        return GeneratedClosure.class.isAssignableFrom(metaClass.getTheClass());
    }
}
