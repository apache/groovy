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
    public StaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class params[]) {
        super(site, metaClass, metaMethod, params);
    }

    public Object invoke(Object receiver, Object[] args) {
        MetaClassHelper.unwrap(args);
        return metaMethod.doMethodInvoke(receiver,  args);
    }

    public final CallSite acceptStatic(Object receiver, Object[] args) {
        if(receiver == metaClass.getTheClass() // meta class match receiver
               && ((MetaClassImpl)metaClass).getTheCachedClass().getMetaClassForClass() == metaClass // metaClass still be valid
           && MetaClassHelper.sameClasses(params, args)) // right arguments
          return this;
        else
          return createCallStaticSite((Class) receiver, args);
    }

    public final CallSite acceptCall(Object receiver, Object[] args) {
          return acceptStatic(receiver, args);
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
