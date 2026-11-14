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
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

/**
 * POJO call site
 *   metaclass - cached
 *   method - cached
*/
public class StaticMetaMethodSite extends MetaMethodSite {
    private final int version;

    /**
     * Caches {@code metaClass}, {@code metaMethod}, and {@code params}, and
     * records {@code metaClass}'s version for {@link #checkCall(Object, Object[])}.
     */
    public StaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
        super(site, metaClass, metaMethod, params);
        version = metaClass.getVersion ();
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
     * Returns {@code true} when {@code receiver} is the meta-class's class,
     * the meta-class version is still the version recorded at construction,
     * and {@link MetaClassHelper#sameClasses(Class[], Object[])} matches the
     * cached parameter classes to {@code args}.
     */
    protected final boolean checkCall(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass() // metaclass match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, args);
    }

    /**
     * Returns {@code true} when {@code receiver} is the meta-class's class,
     * the meta-class version is still the version recorded at construction,
     * and {@link MetaClassHelper#sameClasses(Class[])} reports that the
     * cached parameter list is empty.
     */
    protected final boolean checkCall(Object receiver) {
        return receiver == metaClass.getTheClass() // metaclass match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params);
    }

    /**
     * Returns {@code true} when {@code receiver} is the meta-class's class,
     * the meta-class version is still the version recorded at construction,
     * and {@link MetaClassHelper#sameClasses(Class[], Object)} matches the
     * cached parameter class to {@code arg1}.
     */
    protected final boolean checkCall(Object receiver, Object arg1) {
        return receiver == metaClass.getTheClass() // metaclass match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1);
    }

    /**
     * Returns {@code true} when {@code receiver} is the meta-class's class,
     * the meta-class version is still the version recorded at construction,
     * and {@link MetaClassHelper#sameClasses(Class[], Object, Object)}
     * matches the cached parameter classes to {@code arg1} and {@code arg2}.
     */
    protected final boolean checkCall(Object receiver, Object arg1, Object arg2) {
        return receiver == metaClass.getTheClass() // metaclass match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1, arg2);
    }

    /**
     * Returns {@code true} when {@code receiver} is the meta-class's class,
     * the meta-class version is still the version recorded at construction,
     * and {@link MetaClassHelper#sameClasses(Class[], Object, Object, Object)}
     * matches the cached parameter classes to {@code arg1}, {@code arg2},
     * and {@code arg3}.
     */
    protected final boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3) {
        return receiver == metaClass.getTheClass() // metaclass match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1, arg2, arg3);
    }

    /**
     * Returns {@code true} when {@code receiver} is the meta-class's class,
     * the meta-class version is still the version recorded at construction,
     * and {@link MetaClassHelper#sameClasses(Class[], Object, Object, Object, Object)}
     * matches the cached parameter classes to {@code arg1} through
     * {@code arg4}.
     */
    protected final boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) {
        return receiver == metaClass.getTheClass() // metaclass match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1, arg2, arg3, arg4);
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
                return invoke(receiver, args);
            } catch (GroovyRuntimeException gre) {
                throw ScriptBytecodeAdapter.unwrap(gre);
            }
        } else {
          return CallSiteArray.defaultCall(this, receiver, args);
        }
    }

    /**
     * When {@link #checkCall(Object, Object[])} passes, {@link #invoke(Object, Object[])}
     * unwraps {@code args} and calls {@link MetaMethod#doMethodInvoke(Object, Object[])}.
     * Otherwise falls back to
     * {@link CallSiteArray#defaultCallStatic(CallSite, Class, Object[])}.
     */
    @Override
    public Object callStatic(Class receiver, Object[] args) throws Throwable {
        if(checkCall(receiver, args))
          return invoke(receiver, args);
        else
          return CallSiteArray.defaultCallStatic(this, receiver, args);
    }

    /**
     * Chooses a static site from {@code args}.
     * When {@link MetaMethod#correctArguments(Object[])} returns the same
     * array and {@code noWrappers(args)} is true: if {@code noCoerce} is
     * true, returns a {@link StaticMetaMethodSiteNoUnwrap} (its {@code invoke}
     * calls {@link MetaMethod#doMethodInvoke(Object, Object[])} and skips
     * {@link MetaClassHelper#unwrap(Object[])}); if {@code noCoerce} is false
     * and {@code metaMethod}'s class is exactly {@link CachedMethod},
     * delegates to
     * {@link CachedMethodCallSites#createStaticMetaMethodSite(CachedMethod, CallSite, MetaClassImpl, Class[])};
     * otherwise returns a {@link StaticMetaMethodSiteNoUnwrapNoCoerce} (its
     * {@code invoke} calls {@link MetaMethod#invoke(Object, Object[])} with
     * no unwrap and no coerce).
     * If those argument checks fail, returns a {@link StaticMetaMethodSite}.
     */
    public static CallSite createStaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new StaticMetaMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else
                    if (metaMethod.getClass() == CachedMethod.class)
                      return CachedMethodCallSites.createStaticMetaMethodSite((CachedMethod) metaMethod, site, metaClass, params);
                    else
                      return new StaticMetaMethodSiteNoUnwrapNoCoerce (site, metaClass, metaMethod, params);
            }
        }
        return new StaticMetaMethodSite(site, metaClass, metaMethod, params);
    }

    /**
     * Call site where we know there is no need to unwrap arguments
     */
    public static class StaticMetaMethodSiteNoUnwrap extends StaticMetaMethodSite {

        /**
         * Delegates to {@link StaticMetaMethodSite}, recording the same
         * meta-class version.
         */
        public StaticMetaMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
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
    public static class StaticMetaMethodSiteNoUnwrapNoCoerce extends StaticMetaMethodSite {

        /**
         * Delegates to {@link StaticMetaMethodSite}, recording the same
         * meta-class version.
         */
        public StaticMetaMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
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
