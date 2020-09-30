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

public class ConstructorSite extends MetaClassSite {
    final CachedConstructor constructor;
    final Class[] params;
    private final int version;

    public ConstructorSite(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
        super(site, metaClass);
        this.constructor = constructor;
        this.params = params;
        this.version = metaClass.getVersion();
    }

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

    protected final boolean checkCall(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass() // meta class match receiver
                && ((MetaClassImpl) metaClass).getVersion() == version // metaClass still be valid
                && MetaClassHelper.sameClasses(params, args);
    }

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

        public ConstructorSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

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

        public ConstructorSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

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

    public static class NoParamSite extends ConstructorSiteNoUnwrapNoCoerce {
        private static final Object[] NO_ARGS = new Object[0];

        public NoParamSite(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

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
    
    public static class NoParamSiteInnerClass extends ConstructorSiteNoUnwrapNoCoerce {
        public NoParamSiteInnerClass(CallSite site, MetaClassImpl metaClass, CachedConstructor constructor, Class[] params) {
            super(site, metaClass, constructor, params);
        }

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
