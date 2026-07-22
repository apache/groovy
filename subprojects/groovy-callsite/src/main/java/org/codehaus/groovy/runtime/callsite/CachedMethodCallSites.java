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

import groovy.lang.MetaClassImpl;
import org.codehaus.groovy.reflection.CachedMethod;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Creates optimized call sites for {@link CachedMethod} instances.
 * Logic formerly lived on {@link CachedMethod} in groovy-core.
 * <p>
 * Per-method compile state is held in a side map (not on {@code CachedMethod})
 * so groovy-core reflection metadata stays free of classic call-site concerns.
 * State mutations for a given method are synchronized on that method's state
 * object.
 *
 * @since 6.0.0
 * @deprecated Classic call-site caching is deprecated; prefer invokedynamic.
 */
@Deprecated
public final class CachedMethodCallSites {

    private static final ConcurrentHashMap<CachedMethod, State> STATES = new ConcurrentHashMap<>();

    private CachedMethodCallSites() {
    }

    private static State state(final CachedMethod method) {
        return STATES.computeIfAbsent(method, m -> new State());
    }

    /**
     * Creates an optimized POGO call site, compiling a specialized site class when possible.
     */
    public static CallSite createPogoMetaMethodSite(final CachedMethod method, final CallSite site,
                                                    final MetaClassImpl metaClass, final Class[] params) {
        return create(method, site, metaClass, params,
                CallSiteGenerator::compilePogoMethod,
                PogoMetaMethodSite.PogoCachedMethodSiteNoUnwrapNoCoerce::new,
                Kind.POGO);
    }

    /**
     * Creates an optimized POJO call site, compiling a specialized site class when possible.
     */
    public static CallSite createPojoMetaMethodSite(final CachedMethod method, final CallSite site,
                                                    final MetaClassImpl metaClass, final Class[] params) {
        return create(method, site, metaClass, params,
                CallSiteGenerator::compilePojoMethod,
                PojoMetaMethodSite.PojoCachedMethodSiteNoUnwrapNoCoerce::new,
                Kind.POJO);
    }

    /**
     * Creates an optimized static call site, compiling a specialized site class when possible.
     */
    public static CallSite createStaticMetaMethodSite(final CachedMethod method, final CallSite site,
                                                      final MetaClassImpl metaClass, final Class[] params) {
        return create(method, site, metaClass, params,
                CallSiteGenerator::compileStaticMethod,
                StaticMetaMethodSite.StaticMetaMethodSiteNoUnwrapNoCoerce::new,
                Kind.STATIC);
    }

    private static CallSite create(final CachedMethod method, final CallSite site,
                                   final MetaClassImpl metaClass, final Class[] params,
                                   final Function<CachedMethod, Constructor> compiler,
                                   final FallbackSiteFactory fallback, final Kind kind) {
        State st = state(method);
        synchronized (st) {
            if (!st.skipCompiled) {
                Constructor<CallSite> ctor = st.get(kind);
                if (ctor == null) {
                    if (CallSiteGenerator.isCompilable(method)) {
                        @SuppressWarnings("unchecked")
                        Constructor<CallSite> compiled = compiler.apply(method);
                        ctor = compiled;
                    }
                    if (ctor != null) {
                        st.set(kind, ctor);
                    } else {
                        st.skipCompiled = true;
                    }
                }
                if (ctor != null) {
                    try {
                        return ctor.newInstance(site, metaClass, method, params, ctor);
                    } catch (Error e) {
                        st.skipCompiled = true;
                        throw e;
                    } catch (Throwable e) {
                        st.skipCompiled = true;
                    }
                }
            }
        }
        return fallback.create(site, metaClass, method, params);
    }

    private enum Kind { POGO, POJO, STATIC }

    @FunctionalInterface
    private interface FallbackSiteFactory {
        CallSite create(CallSite site, MetaClassImpl metaClass, CachedMethod method, Class[] params);
    }

    private static final class State {
        private SoftReference<Constructor<CallSite>> pogo;
        private SoftReference<Constructor<CallSite>> pojo;
        private SoftReference<Constructor<CallSite>> staticCtor;
        boolean skipCompiled;

        Constructor<CallSite> get(final Kind kind) {
            SoftReference<Constructor<CallSite>> ref = switch (kind) {
                case POGO -> pogo;
                case POJO -> pojo;
                case STATIC -> staticCtor;
            };
            return ref != null ? ref.get() : null;
        }

        void set(final Kind kind, final Constructor<CallSite> ctor) {
            SoftReference<Constructor<CallSite>> ref = new SoftReference<>(ctor);
            switch (kind) {
                case POGO -> pogo = ref;
                case POJO -> pojo = ref;
                case STATIC -> staticCtor = ref;
            }
        }
    }
}
