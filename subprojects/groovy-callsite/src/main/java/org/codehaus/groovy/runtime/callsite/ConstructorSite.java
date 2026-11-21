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

import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClassImpl;
import org.codehaus.groovy.reflection.CachedConstructor;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.util.Map;

/**
 * Call site bound to one {@link CachedConstructor}.
 * {@code callConstructor} invokes that constructor when the receiver is the meta-class's
 * class, the meta-class version is unchanged, and the argument classes still match.
 * Otherwise it falls back to the default constructor call.
 */
public class ConstructorSite extends MetaClassSite {
    final CachedConstructor constructor;
    final Class[] params;
    private final int version;

    /**
     * Caches {@code constructor} and {@code params}, and records
     * {@code metaClass}'s version for {@link #checkCall(Object, Object[])}.
     */
    public ConstructorSite(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
        super(site, metaClass);
        this.constructor = constructor;
        this.params = params;
        this.version = metaClass.getVersion();
    }

    /**
     * When {@link #checkCall(Object, Object[])} passes, unwraps {@code args}
     * and calls {@link CachedConstructor#doConstructorInvoke(Object[])},
     * which coerces them. A {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Otherwise falls back to
     * {@link CallSiteArray#defaultCallConstructor(CallSite, Object, Object[])}.
     */
    @Override
    public Object callConstructor(Object receiver, Object[] args) throws Throwable {
        if (checkCall(receiver, args)) {
            MetaClassHelper.unwrap(args);
            try {
                return constructor.doConstructorInvoke(args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else
            return CallSiteArray.defaultCallConstructor(this, receiver, args);
    }

    /**
     * Returns {@code true} when {@code receiver} is the meta-class's class,
     * the meta-class version is still the version recorded at construction,
     * and {@link MetaClassHelper#sameClasses(Class[], Object[])} matches the
     * cached parameter classes to {@code args}.
     */
    protected final boolean checkCall(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass() // metaclass match receiver
                && ((MetaClassImpl) metaClass).getVersion() == version // metaClass still be valid
                && MetaClassHelper.sameClasses(params, args);
    }

    /**
     * Chooses a constructor site from {@code args}.
     * When {@link CachedConstructor#correctArguments(Object[])} returns the
     * same array and {@code noWrappers(args)} is true, returns a
     * {@link ConstructorSiteNoUnwrap} if {@code noCoerce} is true (its
     * {@code callConstructor} calls
     * {@link CachedConstructor#doConstructorInvoke(Object[])} and skips
     * {@link MetaClassHelper#unwrap(Object[])}) and a
     * {@link ConstructorSiteNoUnwrapNoCoerce} if {@code noCoerce} is false
     * (its {@code callConstructor} calls {@link CachedConstructor#invoke(Object[])}
     * with no unwrap and no coerce).
     * If those argument checks fail, returns a {@link ConstructorSite}.
     */
    public static ConstructorSite createConstructorSite(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params, Object[] args) {
        if (constructor.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(constructor, args))
                    return new ConstructorSiteNoUnwrap(site, metaClass, constructor, params);
                else
                    return new ConstructorSiteNoUnwrapNoCoerce(site, metaClass, constructor, params);
            }
        }
        return new ConstructorSite(site, metaClass, constructor, params);
    }


    /**
     * Call site where we know there is no need to unwrap arguments
     */
    public static class ConstructorSiteNoUnwrap extends ConstructorSite {

        /**
         * Delegates to {@link ConstructorSite}, recording the same
         * meta-class version.
         */
        public ConstructorSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

        /**
         * When {@link #checkCall(Object, Object[])} passes, calls
         * {@link CachedConstructor#doConstructorInvoke(Object[])}, which
         * coerces, and skips {@link MetaClassHelper#unwrap(Object[])}. A
         * {@link GroovyRuntimeException} is rethrown via
         * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
         * Otherwise falls back to
         * {@link CallSiteArray#defaultCallConstructor(CallSite, Object, Object[])}.
         */
        @Override
        public final Object callConstructor(Object receiver, Object[] args) throws Throwable {
            if (checkCall(receiver, args)) {
                try {
                    return constructor.doConstructorInvoke(args);
                } catch (GroovyRuntimeException gre) {
                    throw ScriptBytecodeAdapter.unwrap(gre);
                }
            } else
                return CallSiteArray.defaultCallConstructor(this, receiver, args);
        }
    }

    /**
     * Call site where we know there is no need neither unwrap nor coerce arguments
     */
    public static class ConstructorSiteNoUnwrapNoCoerce extends ConstructorSite {

        /**
         * Delegates to {@link ConstructorSite}, recording the same
         * meta-class version.
         */
        public ConstructorSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

        /**
         * When {@link #checkCall(Object, Object[])} passes, calls
         * {@link CachedConstructor#invoke(Object[])} with no unwrap and no
         * coerce. A {@link GroovyRuntimeException} is rethrown via
         * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
         * Otherwise falls back to
         * {@link CallSiteArray#defaultCallConstructor(CallSite, Object, Object[])}.
         */
        @Override
        public Object callConstructor(Object receiver, Object[] args) throws Throwable {
            if (checkCall(receiver, args)) {
                try {
                    return constructor.invoke(args);
                } catch (GroovyRuntimeException gre) {
                    throw ScriptBytecodeAdapter.unwrap(gre);
                }
            } else
                return CallSiteArray.defaultCallConstructor(this, receiver, args);
        }
    }

    /**
     * Constructor site that, after the inherited {@link #checkCall(Object, Object[])}
     * passes, invokes the cached constructor with an empty argument array
     * and then applies {@code args[0]} as a property map. The constructor
     * call itself neither unwraps nor coerces. Otherwise it falls back to
     * the default constructor call.
     */
    public static class NoParamSite extends ConstructorSiteNoUnwrapNoCoerce {
        private static final Object[] NO_ARGS = new Object[0];

        /**
         * Delegates to {@link ConstructorSiteNoUnwrapNoCoerce}, recording
         * the same meta-class version.
         */
        public NoParamSite(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

        /**
         * When {@link #checkCall(Object, Object[])} passes, calls
         * {@link CachedConstructor#invoke(Object[])} with an empty argument
         * array, then casts {@code args[0]} to {@link Map} and passes it to
         * {@link MetaClassImpl#setProperties(Object, Map)}. That constructor
         * call neither unwraps nor coerces. A {@link GroovyRuntimeException}
         * from setting properties is rethrown via
         * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
         * Otherwise falls back to
         * {@link CallSiteArray#defaultCallConstructor(CallSite, Object, Object[])}.
         */
        @Override
        public final Object callConstructor(Object receiver, Object[] args) throws Throwable {
            if (checkCall(receiver, args)) {
                final Object bean = constructor.invoke(NO_ARGS);
                try {
                    ((MetaClassImpl) metaClass).setProperties(bean, (Map) args[0]);
                } catch (GroovyRuntimeException gre) {
                    throw ScriptBytecodeAdapter.unwrap(gre);
                }
                return bean;
            } else
                return CallSiteArray.defaultCallConstructor(this, receiver, args);
        }
    }

    /**
     * Constructor site that, after the inherited {@link #checkCall(Object, Object[])}
     * passes, invokes the cached constructor with only {@code args[0]} and
     * then applies {@code args[1]} as a property map. Those constructor
     * arguments are neither unwrapped nor coerced. Otherwise it falls back
     * to the default constructor call.
     */
    public static class NoParamSiteInnerClass extends ConstructorSiteNoUnwrapNoCoerce {
        /**
         * Delegates to {@link ConstructorSiteNoUnwrapNoCoerce}, recording
         * the same meta-class version.
         */
        public NoParamSiteInnerClass(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

        /**
         * When {@link #checkCall(Object, Object[])} passes, calls
         * {@link CachedConstructor#invoke(Object[])} with a one-element
         * array holding {@code args[0]}, then casts {@code args[1]} to
         * {@link Map} and passes it to {@link MetaClassImpl#setProperties(Object, Map)}.
         * Those constructor arguments are neither unwrapped nor coerced. A
         * {@link GroovyRuntimeException} from setting properties is rethrown
         * via {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
         * Otherwise falls back to
         * {@link CallSiteArray#defaultCallConstructor(CallSite, Object, Object[])}.
         */
        @Override
        public final Object callConstructor(Object receiver, Object[] args) throws Throwable {
            if (checkCall(receiver, args)) {
                final Object[] newArgs = new Object[] {args[0]};
                final Object bean = constructor.invoke(newArgs);
                try {
                    ((MetaClassImpl) metaClass).setProperties(bean, (Map) args[1]);
                } catch (GroovyRuntimeException gre) {
                    throw ScriptBytecodeAdapter.unwrap(gre);
                }
                return bean;
            } else
                return CallSiteArray.defaultCallConstructor(this, receiver, args);
        }
    }
}
