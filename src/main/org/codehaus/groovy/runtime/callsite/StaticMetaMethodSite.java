/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.runtime.MetaClassHelper;

/**
 * POJO call site
 *   meta class - cached
 *   method - cached
 *
 * @author Alex Tkachman
*/
public class StaticMetaMethodSite extends MetaMethodSite {
    private final int version;

    public StaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
        super(site, metaClass, metaMethod, params);
        version = metaClass.getVersion ();
    }

    public Object invoke(Object receiver, Object[] args) {
        MetaClassHelper.unwrap(args);
        return metaMethod.doMethodInvoke(receiver,  args);
    }

    protected boolean checkCall(Object receiver, Object[] args) {
        return receiver == metaClass.getTheClass() // meta class match receiver
               && ((MetaClassImpl)metaClass).getVersion() == version // metaClass still be valid
           && MetaClassHelper.sameClasses(params, args);
    }

    public final Object call(Object receiver, Object[] args) {
        if(checkCall(receiver, args))
          return invoke(receiver, args);
        else
          return CallSiteArray.defaultCall(this, receiver, args);
    }

    public final Object callStatic(Object receiver, Object[] args) {
        if(checkCall(receiver, args))
          return invoke(receiver, args);
        else
          return CallSiteArray.defaultCallStatic(this, receiver, args);
    }

    public static StaticMetaMethodSite createStaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object[] args) {
        if (metaMethod.correctArguments(args) == args) {
            if (noWrappers(args)) {
                if (noCoerce(metaMethod,args))
                    return new StaticMetaMethodSiteNoUnwrap(site, metaClass, metaMethod, params);
                else {
//                    if (metaMethod.getClass() == CachedMethod.class) {
//                        CachedMethod m = (CachedMethod)metaMethod;
//                        if (m.isPublic()) {
//                            final StaticMetaMethodSite res = m.createStaticMetaMethodSite(site, metaClass, metaMethod, params, site.array.owner);
//                            if (res != null)
//                              return res;
//                        }
//                    }

                    return new StaticMetaMethodSiteNoUnwrapNoCoerce(site, metaClass, metaMethod, params);
                }
            }
        }
        return new StaticMetaMethodSite(site, metaClass, metaMethod, params);
    }

    /**
     * Call site where we know there is no need to unwrap arguments
     */
    public static class StaticMetaMethodSiteNoUnwrap extends StaticMetaMethodSite {

        public StaticMetaMethodSiteNoUnwrap(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(site, metaClass, metaMethod, params);
        }

        public final Object invoke(Object receiver, Object[] args) {
            return metaMethod.doMethodInvoke(receiver,  args);
        }
    }

    /**
     * Call site where we know there is no need neither unwrap nor coerce arguments
     */
    public static class StaticMetaMethodSiteNoUnwrapNoCoerce extends StaticMetaMethodSite {

        public StaticMetaMethodSiteNoUnwrapNoCoerce(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
            super(site, metaClass, metaMethod, params);
        }

        public final Object invoke(Object receiver, Object[] args) {
            return metaMethod.invoke(receiver,  args);
        }
    }
}
