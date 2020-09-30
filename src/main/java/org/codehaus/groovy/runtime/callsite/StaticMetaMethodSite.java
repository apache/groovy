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
 *   meta class - cached
 *   method - cached
*/
public class StaticMetaMethodSite extends MetaMethodSite {
    private final int version;

    public StaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
        super(site, metaClass, metaMethod, params);
        version = metaClass.getVersion ();
    }

    public Object invoke(Object receiver, Object[] args) throws Throwable {
        MetaClassHelper.unwrap(args);
        try {
            return metaMethod.doMethodInvoke(receiver,  args);
        } catch (GroovyRuntimeException gre) {
            throw ScriptBytecodeAdapter.unwrap(gre);
        }
    }

    protected final boolean checkCall(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, args);
    }

    protected final boolean checkCall(Object receiver) {
        return receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params);
    }

    protected final boolean checkCall(Object receiver, Object arg1) {
        return receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1);
    }

    protected final boolean checkCall(Object receiver, Object arg1, Object arg2) {
        return receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1, arg2);
    }

    protected final boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3) {
        return receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1, arg2, arg3);
    }

    protected final boolean checkCall(Object receiver, Object arg1, Object arg2, Object arg3, Object arg4) {
        return receiver == metaClass.getTheClass() // meta class match receiver
           && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, arg1, arg2, arg3, arg4);
    }

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

    @Override
    public Object callStatic(Class receiver, Object[] args) throws Throwable {
        if(checkCall(receiver, args))
          return invoke(receiver, args);
        else
          return CallSiteArray.defaultCallStatic(this, receiver, args);
    }

    public static CallSite createStaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new StaticMetaMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else
                    if (metaMethod.getClass() == CachedMethod.class)
                      return ((CachedMethod)metaMethod).createStaticMetaMethodSite(site, metaClass, params);
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

        public StaticMetaMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

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

        public StaticMetaMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

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
