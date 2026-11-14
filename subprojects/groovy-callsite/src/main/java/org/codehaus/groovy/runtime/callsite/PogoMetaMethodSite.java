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

import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.reflect.Method;

/**
 * POGO call site
 *   metaclass - cached
 *   method - cached
*/
public class PogoMetaMethodSite extends PlainObjectMetaMethodSite {
    private static final VMPlugin VM_PLUGIN = VMPluginFactory.getPlugin();
    private final int version;
    private final boolean skipVersionCheck;

    /**
     * Caches {@code metaClass}, {@code metaMethod}, and {@code params},
     * records {@code metaClass}'s version, and skips later version checks
     * when {@code metaClass}'s class is exactly {@link MetaClassImpl}.
     */
    public PogoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
        super(site, metaClass, metaMethod, params);
        version = metaClass.getVersion();
        skipVersionCheck = metaClass.getClass()==MetaClassImpl.class;
    }

    /**
     * Unwraps {@code args} with {@link MetaClassHelper#unwrap(Object[])}
     * and calls {@link MetaMethod#doMethodInvoke(Object, Object[])}, which
     * coerces them. A {@link GroovyRuntimeException} is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Performs no receiver, meta-class version, or argument-class check.
     */
    public Object invoke(Object receiver, Object[] args) throws Throwable {
        MetaClassHelper.unwrap(args);
        try {
            return metaMethod.doMethodInvoke(receiver,  args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    /**
     * When {@link #checkCall(Object, Object[])} passes, {@link #invoke(Object, Object[])}
     * unwraps {@code args} and calls {@link MetaMethod#doMethodInvoke(Object, Object[])}.
     * A {@link GroovyRuntimeException} from that call is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Otherwise falls back to
     * {@link CallSiteArray#defaultCallCurrent(CallSite, GroovyObject, Object[])}.
     */
    @Override
    public Object callCurrent(GroovyObject receiver, Object[] args) throws Throwable {
        if(checkCall(receiver, args)) {
            try {
                return invoke(receiver,args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCallCurrent(this, receiver, args);
        }
    }

    /**
     * When {@link #checkCall(Object, Object[])} passes, {@link #invoke(Object, Object[])}
     * unwraps {@code args} and calls {@link MetaMethod#doMethodInvoke(Object, Object[])}.
     * A {@link GroovyRuntimeException} from that call is rethrown via
     * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
     * Otherwise falls back to {@link CallSiteArray#defaultCall(CallSite, Object, Object[])}.
     */
    @Override
    public Object call(Object receiver, Object[] args) throws Throwable {
        if(checkCall(receiver, args)) {
            try {
                return invoke(receiver,args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
            return CallSiteArray.defaultCall(this, receiver, args);
        }
    }

    private boolean nonParamCheck(Object receiver) {
        try {
            return !GroovyCategorySupport.hasCategoryInCurrentThread()
                    && ((GroovyObject)receiver).getMetaClass() == metaClass // metaClass still be valid
                    && (skipVersionCheck || ((MetaClassImpl) metaClass).getVersion() == version);
        } catch (NullPointerException e) {
            if (receiver == null) return false;
            throw e;
        } catch (ClassCastException e) {
            if (!(receiver instanceof GroovyObject)) return false;
            throw e;
        }
    }

    /**
     * Returns {@code true} when the current thread has no Groovy category,
     * {@code receiver} is a {@link GroovyObject} whose meta-class is this
     * site's meta-class, the recorded meta-class version still matches
     * (skipped when the meta-class's class is exactly {@link MetaClassImpl}),
     * and {@link MetaClassHelper#sameClasses(Class[], Object[])} matches
     * {@code args}. A {@code null} receiver or a receiver that is not a
     * {@link GroovyObject} returns {@code false}. Any other
     * {@link NullPointerException} or {@link ClassCastException} is rethrown.
     */
    protected boolean checkCall(Object receiver, Object[] args) {
        return nonParamCheck(receiver)
                && MetaClassHelper.sameClasses(params, args);
    }

    /**
     * Returns {@code true} when the current thread has no Groovy category,
     * {@code receiver} is a {@link GroovyObject} whose meta-class is this
     * site's meta-class, the recorded meta-class version still matches
     * (skipped when the meta-class's class is exactly {@link MetaClassImpl}),
     * and {@link MetaClassHelper#sameClasses(Class[])} reports that the
     * cached parameter list is empty. A {@code null} receiver or a receiver
     * that is not a {@link GroovyObject} returns {@code false}. Any other
     * {@link NullPointerException} or {@link ClassCastException} is rethrown.
     */
    protected boolean checkCall(Object receiver) {
        return nonParamCheck(receiver)
                && MetaClassHelper.sameClasses(params);
    }

    /**
     * Returns {@code true} when the current thread has no Groovy category,
     * {@code receiver} is a {@link GroovyObject} whose meta-class is this
     * site's meta-class, the recorded meta-class version still matches
     * (skipped when the meta-class's class is exactly {@link MetaClassImpl}),
     * and {@link MetaClassHelper#sameClasses(Class[], Object)} matches
     * {@code arg1}. A {@code null} receiver or a receiver that is not a
     * {@link GroovyObject} returns {@code false}. Any other
     * {@link NullPointerException} or {@link ClassCastException} is rethrown.
     */
    protected boolean checkCall(Object receiver, Object arg1) {
        return nonParamCheck(receiver)
                && MetaClassHelper.sameClasses(params, arg1);
    }

    /**
     * Returns {@code true} when the current thread has no Groovy category,
     * {@code receiver} is a {@link GroovyObject} whose meta-class is this
     * site's meta-class, the recorded meta-class version still matches
     * (skipped when the meta-class's class is exactly {@link MetaClassImpl}),
     * and {@link MetaClassHelper#sameClasses(Class[], Object, Object)}
     * matches {@code arg1} and {@code arg2}. A {@code null} receiver or a
     * receiver that is not a {@link GroovyObject} returns {@code false}.
     * Any other {@link NullPointerException} or {@link ClassCastException}
     * is rethrown.
     */
    protected boolean checkCall(Object receiver, Object arg1, Object arg2) {
        return nonParamCheck(receiver)
                && MetaClassHelper.sameClasses(params, arg1, arg2);
    }

    /**
     * Returns {@code true} when the current thread has no Groovy category,
     * {@code receiver} is a {@link GroovyObject} whose meta-class is this
     * site's meta-class, the recorded meta-class version still matches
     * (skipped when the meta-class's class is exactly {@link MetaClassImpl}),
     * and {@link MetaClassHelper#sameClasses(Class[], Object, Object, Object)}
     * matches {@code arg1}, {@code arg2}, and {@code arg3}. A {@code null}
     * receiver or a receiver that is not a {@link GroovyObject} returns
     * {@code false}. Any other {@link NullPointerException} or
     * {@link ClassCastException} is rethrown.
     */
    protected boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3) {
        return nonParamCheck(receiver)
                && MetaClassHelper.sameClasses(params, arg1, arg2, arg3);
    }

    /**
     * Returns {@code true} when the current thread has no Groovy category,
     * {@code receiver} is a {@link GroovyObject} whose meta-class is this
     * site's meta-class, the recorded meta-class version still matches
     * (skipped when the meta-class's class is exactly {@link MetaClassImpl}),
     * and {@link MetaClassHelper#sameClasses(Class[], Object, Object, Object, Object)}
     * matches {@code arg1} through {@code arg4}. A {@code null} receiver or
     * a receiver that is not a {@link GroovyObject} returns {@code false}.
     * Any other {@link NullPointerException} or {@link ClassCastException}
     * is rethrown.
     */
    protected boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) {
        return nonParamCheck(receiver)
                && MetaClassHelper.sameClasses(params, arg1, arg2, arg3, arg4);
    }

    /**
     * Returns a POGO site for {@code metaMethod}. When its class is exactly
     * {@link CachedMethod}, delegates to {@link #createCachedMethodSite}.
     * Otherwise, when {@link MetaMethod#correctArguments(Object[])} returns
     * the same array and {@code noWrappers(args)} is true, returns a
     * {@link PogoMetaMethodSiteNoUnwrap} if {@code noCoerce} is true (its
     * {@code invoke} calls {@link MetaMethod#doMethodInvoke(Object, Object[])}
     * and skips {@link MetaClassHelper#unwrap(Object[])}) and a
     * {@link PogoMetaMethodSiteNoUnwrapNoCoerce} if {@code noCoerce} is false
     * (its {@code invoke} calls {@link MetaMethod#invoke(Object, Object[])}
     * with no unwrap and no coerce).
     * If those argument checks fail, returns a {@link PogoMetaMethodSite}.
     */
    public static CallSite createPogoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.getClass() == CachedMethod.class)
          return createCachedMethodSite (site, metaClass, (CachedMethod) metaMethod, params, args);

        return createNonAwareCallSite(site, metaClass, metaMethod, params, args);
    }

    private static CallSite createNonAwareCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new PogoMetaMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else {
                    return new PogoMetaMethodSiteNoUnwrapNoCoerce(site, metaClass, metaMethod, params);
                }
            }
        }
        return new PogoMetaMethodSite(site, metaClass, metaMethod, params);
    }

    /**
     * Chooses a {@link CachedMethod} POGO site from {@code args}.
     * When {@link MetaMethod#correctArguments(Object[])} returns the same
     * array and {@code noWrappers(args)} is true, returns a
     * {@link PogoCachedMethodSiteNoUnwrap} if {@code noCoerce} is true,
     * and otherwise delegates to
     * {@link CachedMethodCallSites#createPogoMetaMethodSite(CachedMethod, CallSite, MetaClassImpl, Class[])}.
     * Otherwise returns a {@link PogoCachedMethodSite}, whose {@code invoke}
     * unwraps, coerces, and reflects.
     */
    public static CallSite createCachedMethodSite(CallSite site, MetaClassImpl metaClass, CachedMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new PogoCachedMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else {
                    return CachedMethodCallSites.createPogoMetaMethodSite(metaMethod, site, metaClass, params);
                }
            }
        }
        return new PogoCachedMethodSite(site, metaClass, metaMethod, params);
    }

    /**
     * POGO site that invokes a reflected {@link Method}.
     * {@code invoke} still unwraps the arguments and coerces them with
     * {@link MetaMethod#coerceArgumentsToClasses(Object[])} before
     * {@link PlainObjectMetaMethodSite#doInvoke}. Category, meta-class
     * identity, version, and argument-class checks stay those of
     * {@link PogoMetaMethodSite}.
     */
    public static class PogoCachedMethodSite extends PogoMetaMethodSite {
        final Method reflect;

        /**
         * Passes {@code metaMethod} through the VM plugin
         * {@code transformMetaMethod} and stores the {@link Method} from
         * {@link CachedMethod#setAccessible()}.
         */
        public PogoCachedMethodSite(CallSite site, MetaClassImpl metaClass, CachedMethod metaMethod, Class[] params) {
            super(site, metaClass, VM_PLUGIN.transformMetaMethod(metaClass, metaMethod), params);
            reflect = ((CachedMethod) super.metaMethod).setAccessible();
        }

        /**
         * Unwraps {@code args}, coerces them with
         * {@link MetaMethod#coerceArgumentsToClasses(Object[])}, and invokes
         * the reflected method through {@link PlainObjectMetaMethodSite#doInvoke}.
         * Performs no receiver, meta-class version, or argument-class check.
         */
        @Override
        public Object invoke(Object receiver, Object[] args) throws Throwable {
            MetaClassHelper.unwrap(args);
            args = metaMethod.coerceArgumentsToClasses(args);
            return doInvoke(receiver, args, reflect);
        }
    }

    /**
     * Reflected POGO site that skips {@link MetaClassHelper#unwrap(Object[])}.
     * {@code invoke} still coerces with
     * {@link MetaMethod#coerceArgumentsToClasses(Object[])} before
     * {@link PlainObjectMetaMethodSite#doInvoke}. Category, meta-class
     * identity, version, and argument-class checks stay those of
     * {@link PogoMetaMethodSite}.
     */
    public static class PogoCachedMethodSiteNoUnwrap extends PogoCachedMethodSite {

        /**
         * Delegates to {@link PogoCachedMethodSite}, recording the same
         * meta-class version and reflected method.
         */
        public PogoCachedMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, CachedMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        /**
         * Coerces {@code args} with
         * {@link MetaMethod#coerceArgumentsToClasses(Object[])} and invokes
         * the reflected method through {@link PlainObjectMetaMethodSite#doInvoke}.
         * Skips {@link MetaClassHelper#unwrap(Object[])} and performs no
         * receiver, meta-class version, or argument-class check.
         */
        @Override
        public final Object invoke(Object receiver, Object[] args) throws Throwable {
            args = metaMethod.coerceArgumentsToClasses(args);
            return doInvoke(receiver, args, reflect);
        }
    }

    /**
     * Reflected POGO site that skips both
     * {@link MetaClassHelper#unwrap(Object[])} and
     * {@link MetaMethod#coerceArgumentsToClasses(Object[])}.
     * {@code invoke} passes {@code args} to
     * {@link PlainObjectMetaMethodSite#doInvoke}. Category, meta-class
     * identity, version, and argument-class checks stay those of
     * {@link PogoMetaMethodSite}.
     */
    public static class PogoCachedMethodSiteNoUnwrapNoCoerce extends PogoCachedMethodSite {

        /**
         * Delegates to {@link PogoCachedMethodSite}, recording the same
         * meta-class version and reflected method.
         */
        public PogoCachedMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, CachedMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        /**
         * Invokes the reflected method through
         * {@link PlainObjectMetaMethodSite#doInvoke} with no unwrap and no
         * coerce, and with no receiver, meta-class version, or argument-class
         * check.
         */
        @Override
        public final Object invoke(Object receiver, Object[] args) throws Throwable {
            return doInvoke(receiver, args, reflect);
        }
    }

    /**
     * Call site where we know there is no need to unwrap arguments
     */
    public static class PogoMetaMethodSiteNoUnwrap extends PogoMetaMethodSite {

        /**
         * Delegates to {@link PogoMetaMethodSite}, recording the same
         * meta-class version and version-check flag.
         */
        public PogoMetaMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        /**
         * Calls {@link MetaMethod#doMethodInvoke(Object, Object[])}, which
         * coerces, and skips {@link MetaClassHelper#unwrap(Object[])}. A
         * {@link GroovyRuntimeException} is rethrown via
         * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
         * Performs no receiver, meta-class version, or argument-class check.
         */
        @Override
        public final Object invoke(Object receiver, Object[] args) throws Throwable {
            try {
                return metaMethod.doMethodInvoke(receiver,  args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        }
    }

    /**
     * Call site where we know there is no need neither unwrap nor coerce arguments
     */
    public static class PogoMetaMethodSiteNoUnwrapNoCoerce extends PogoMetaMethodSite {

        /**
         * Delegates to {@link PogoMetaMethodSite}, recording the same
         * meta-class version and version-check flag.
         */
        public PogoMetaMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        /**
         * Calls {@link MetaMethod#invoke(Object, Object[])} with no
         * {@link MetaClassHelper#unwrap(Object[])} and no
         * {@link MetaMethod#coerceArgumentsToClasses(Object[])}. A
         * {@link GroovyRuntimeException} is rethrown via
         * {@link ScriptBytecodeAdapter#unwrap(GroovyRuntimeException)}.
         * Performs no receiver, meta-class version, or argument-class check.
         */
        @Override
        public final Object invoke(Object receiver, Object[] args) throws Throwable {
            try {
                return metaMethod.invoke(receiver,  args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        }
    }
}
