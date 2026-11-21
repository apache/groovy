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
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.GroovyCategorySupport;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.NullObject;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.vmplugin.VMPlugin;
import org.codehaus.groovy.vmplugin.VMPluginFactory;

import java.lang.reflect.Method;

/**
 * POJO call site
 *   metaclass - cached
 *   method - cached
*/
public class PojoMetaMethodSite extends PlainObjectMetaMethodSite {
    private static final VMPlugin VM_PLUGIN = VMPluginFactory.getPlugin();

    /**
     * Meta-class version recorded by the constructor.
     * {@link #checkPojoMetaClass()} accepts the site only while
     * {@link MetaClassImpl#getVersion()} is still this value.
     */
    protected final int version;

    /**
     * Caches {@code metaClass}, {@code metaMethod}, and {@code params}, and
     * records {@code metaClass}'s version in {@link #version}.
     */
    public PojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
        super(site, metaClass, metaMethod, params);
        version = metaClass.getVersion();
    }

    /**
     * Unwraps {@code args} with {@link MetaClassHelper#unwrap(Object[])}
     * and calls {@link MetaMethod#doMethodInvoke(Object, Object[])}, which
     * coerces them. Performs no receiver, meta-class version, or
     * argument-class check.
     */
    public Object invoke(Object receiver, Object[] args) throws Throwable {
        MetaClassHelper.unwrap(args);
        return metaMethod.doMethodInvoke(receiver,  args);
    }

    /**
     * When {@link #checkCall(Object, Object[])} passes, {@link #invoke(Object, Object[])}
     * unwraps {@code args} and calls {@link MetaMethod#doMethodInvoke(Object, Object[])}.
     * Otherwise falls back to {@link CallSiteArray#defaultCall(CallSite, Object, Object[])}.
     */
    @Override
    public Object call(Object receiver, Object[] args) throws Throwable {
        if(checkCall(receiver, args))
          return invoke(receiver,args);
        else
          return CallSiteArray.defaultCall(this, receiver, args);
    }

    /**
     * Returns whether the current thread has no Groovy category and
     * {@link MetaClassImpl#getVersion()} is still {@link #version}.
     */
    protected final boolean checkPojoMetaClass() {
        return !GroovyCategorySupport.hasCategoryInCurrentThread()
            && ((MetaClassImpl)metaClass).getVersion() == version;
    }

    /**
     * Returns {@code true} when {@code receiver}'s class is the meta-class's
     * class, {@link #checkPojoMetaClass()} passes, and
     * {@link MetaClassHelper#sameClasses(Class[], Object[])} matches the
     * cached parameter classes to {@code args}. A {@code null} receiver is
     * checked again as {@link NullObject}. Any other
     * {@link NullPointerException} is rethrown.
     */
    protected final boolean checkCall(Object receiver, Object[] args) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // metaclass match receiver
               && checkPojoMetaClass()
               && MetaClassHelper.sameClasses(params, args);
        }
        catch (NullPointerException e) {
            if (receiver == null)
              return checkCall(NullObject.getNullObject(), args);

            throw e;
        }
    }

    /**
     * Returns {@code true} when {@code receiver}'s class is the meta-class's
     * class, {@link #checkPojoMetaClass()} passes, and
     * {@link MetaClassHelper#sameClasses(Class[])} reports that the cached
     * parameter list is empty. A {@code null} receiver is checked again as
     * {@link NullObject}. Any other {@link NullPointerException} is rethrown.
     */
    protected final boolean checkCall(Object receiver) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // metaclass match receiver
               && checkPojoMetaClass()
               && MetaClassHelper.sameClasses(params);
        } catch (NullPointerException e) {
            if (receiver == null)
              return checkCall(NullObject.getNullObject());

            throw e;
        }
    }

    /**
     * Returns {@code true} when {@code receiver}'s class is the meta-class's
     * class, {@link #checkPojoMetaClass()} passes, and
     * {@link MetaClassHelper#sameClasses(Class[], Object)} matches the cached
     * parameter class to {@code arg1}. A {@code null} receiver is checked
     * again as {@link NullObject}. Any other {@link NullPointerException}
     * is rethrown.
     */
    protected final boolean checkCall(Object receiver, Object arg1) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // metaclass match receiver
               && checkPojoMetaClass()
               && MetaClassHelper.sameClasses(params, arg1);
        } catch (NullPointerException e) {
            if (receiver == null)
              return checkCall(NullObject.getNullObject(), arg1);

            throw e;
        }
    }

    /**
     * Returns {@code true} when {@code receiver}'s class is the meta-class's
     * class, {@link #checkPojoMetaClass()} passes, and
     * {@link MetaClassHelper#sameClasses(Class[], Object, Object)} matches
     * the cached parameter classes to {@code arg1} and {@code arg2}. A
     * {@code null} receiver is checked again as {@link NullObject}. Any
     * other {@link NullPointerException} is rethrown.
     */
    protected final boolean checkCall(Object receiver, Object arg1, Object arg2) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // metaclass match receiver
               && checkPojoMetaClass()
               && MetaClassHelper.sameClasses(params, arg1, arg2);
        } catch (NullPointerException e) {
            if (receiver == null)
              return checkCall(NullObject.getNullObject(), arg1, arg2);

            throw e;
        }
    }

    /**
     * Returns {@code true} when {@code receiver}'s class is the meta-class's
     * class, {@link #checkPojoMetaClass()} passes, and
     * {@link MetaClassHelper#sameClasses(Class[], Object, Object, Object)}
     * matches the cached parameter classes to {@code arg1}, {@code arg2},
     * and {@code arg3}. A {@code null} receiver is checked again as
     * {@link NullObject}. Any other {@link NullPointerException} is rethrown.
     */
    protected final boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // metaclass match receiver
               && checkPojoMetaClass()
               && MetaClassHelper.sameClasses(params, arg1, arg2, arg3);
        } catch (NullPointerException e) {
            if (receiver == null)
              return checkCall(NullObject.getNullObject(), arg1, arg2, arg3);

            throw e;
        }
    }

    /**
     * Returns {@code true} when {@code receiver}'s class is the meta-class's
     * class, {@link #checkPojoMetaClass()} passes, and
     * {@link MetaClassHelper#sameClasses(Class[], Object, Object, Object, Object)}
     * matches the cached parameter classes to {@code arg1} through
     * {@code arg4}. A {@code null} receiver is checked again as
     * {@link NullObject}. Any other {@link NullPointerException} is rethrown.
     */
    protected final boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) {
        try {
            return receiver.getClass() == metaClass.getTheClass() // metaclass match receiver
               && checkPojoMetaClass()
               && MetaClassHelper.sameClasses(params, arg1, arg2, arg3, arg4);
        } catch (NullPointerException e) {
            if (receiver == null)
              return checkCall(NullObject.getNullObject(), arg1, arg2, arg3, arg4);

            throw e;
        }
    }

    /**
     * Returns a site for {@code metaMethod}. When its class is exactly
     * {@link CachedMethod}, delegates to {@link #createCachedMethodSite}.
     * Otherwise delegates to {@link #createNonAwareCallSite}.
     * {@code receiver} is unused.
     */
    public static CallSite createPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        if (metaMethod.getClass() == CachedMethod.class)
          return createCachedMethodSite(site, metaClass, (CachedMethod) metaMethod, params, args);

        return createNonAwareCallSite(site, metaClass, metaMethod, params, args);
    }

    /**
     * Chooses a {@link CachedMethod} site from {@code args}.
     * When {@link MetaMethod#correctArguments(Object[])} returns the same
     * array and {@code noWrappers(args)} is true, returns a
     * {@link PojoCachedMethodSiteNoUnwrap} if {@code noCoerce} is true,
     * and otherwise delegates to
     * {@link CachedMethodCallSites#createPojoMetaMethodSite(CachedMethod, CallSite, MetaClassImpl, Class[])}.
     * Otherwise returns a {@link PojoCachedMethodSite}, whose {@code invoke}
     * unwraps, coerces, and reflects.
     */
    public static CallSite createCachedMethodSite(CallSite site, MetaClassImpl metaClass, CachedMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new PojoCachedMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else
                    return CachedMethodCallSites.createPojoMetaMethodSite(metaMethod, site, metaClass, params);
            }
        }
        return new PojoCachedMethodSite(site, metaClass, metaMethod, params);
    }

    /**
     * Chooses a {@link PojoMetaMethodSite} from {@code args}.
     * When {@link MetaMethod#correctArguments(Object[])} returns the same
     * array and {@code noWrappers(args)} is true, returns a
     * {@link PojoMetaMethodSiteNoUnwrap} if {@code noCoerce} is true (its
     * {@code invoke} calls {@link MetaMethod#doMethodInvoke(Object, Object[])}
     * and skips {@link MetaClassHelper#unwrap(Object[])}) and a
     * {@link PojoMetaMethodSiteNoUnwrapNoCoerce} if {@code noCoerce} is false
     * (its {@code invoke} calls {@link MetaMethod#invoke(Object, Object[])}
     * with no unwrap and no coerce).
     * Otherwise returns a {@link PojoMetaMethodSite}.
     */
    public static CallSite createNonAwareCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new PojoMetaMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else
                    return new PojoMetaMethodSiteNoUnwrapNoCoerce(site, metaClass, metaMethod, params);
            }
        }
        return new PojoMetaMethodSite(site, metaClass, metaMethod, params);
    }

    /**
     * POJO site that invokes a reflected {@link Method}.
     * {@code invoke} still unwraps the arguments and coerces them with
     * {@link MetaMethod#coerceArgumentsToClasses(Object[])} before
     * {@link PlainObjectMetaMethodSite#doInvoke}. Receiver, category,
     * version, and argument-class checks stay those of {@link PojoMetaMethodSite}.
     */
    public static class PojoCachedMethodSite extends PojoMetaMethodSite {
        final Method reflect;

        /**
         * Passes {@code metaMethod} through the VM plugin
         * {@code transformMetaMethod} and stores the {@link Method} from
         * {@link CachedMethod#setAccessible()}.
         */
        public PojoCachedMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
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
     * Reflected POJO site that skips {@link MetaClassHelper#unwrap(Object[])}.
     * {@code invoke} still coerces with
     * {@link MetaMethod#coerceArgumentsToClasses(Object[])} before
     * {@link PlainObjectMetaMethodSite#doInvoke}. Receiver, category, version,
     * and argument-class checks stay those of {@link PojoMetaMethodSite}.
     */
    public static class PojoCachedMethodSiteNoUnwrap extends PojoCachedMethodSite {

        /**
         * Delegates to {@link PojoCachedMethodSite}, recording the same
         * meta-class version and reflected method.
         */
        public PojoCachedMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
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
     * Reflected POJO site that skips both
     * {@link MetaClassHelper#unwrap(Object[])} and
     * {@link MetaMethod#coerceArgumentsToClasses(Object[])}.
     * {@code invoke} passes {@code args} to
     * {@link PlainObjectMetaMethodSite#doInvoke}. Receiver, category, version,
     * and argument-class checks stay those of {@link PojoMetaMethodSite}.
     */
    public static class PojoCachedMethodSiteNoUnwrapNoCoerce extends PojoCachedMethodSite {

        /**
         * Delegates to {@link PojoCachedMethodSite}, recording the same
         * meta-class version and reflected method.
         */
        public PojoCachedMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
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
    public static class PojoMetaMethodSiteNoUnwrap extends PojoMetaMethodSite {

        /**
         * Delegates to {@link PojoMetaMethodSite}, recording the same
         * meta-class version.
         */
        public PojoMetaMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
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
    public static class PojoMetaMethodSiteNoUnwrapNoCoerce extends PojoMetaMethodSite {

        /**
         * Delegates to {@link PojoMetaMethodSite}, recording the same
         * meta-class version.
         */
        public PojoMetaMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
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
