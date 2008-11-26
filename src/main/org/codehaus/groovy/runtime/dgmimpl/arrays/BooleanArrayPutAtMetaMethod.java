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
package org.codehaus.groovy.runtime.dgmimpl.arrays;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;

public class BooleanArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
        private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
        private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(boolean[].class);
        private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

        public BooleanArrayPutAtMetaMethod() {
            parameterTypes = PARAM_CLASS_ARR;
        }

        public final CachedClass getDeclaringClass() {
            return ARR_CLASS;
        }

        public Object invoke(Object object, Object[] args) {
            final boolean[] objects = (boolean[]) object;
            final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
            objects[index] = ((Boolean)args[1]).booleanValue();
            return null;
        }

        public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            if (!(args [0] instanceof Integer) || !(args [1] instanceof Boolean))
              return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
            else
                return new MyPojoMetaMethodSite(site, metaClass, metaMethod, params);
        }

    private static class MyPojoMetaMethodSite extends PojoMetaMethodSite {
        public MyPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        public Object call(Object receiver, Object[] args) throws Throwable {
            if ((receiver instanceof boolean[] && args[0] instanceof Integer && args[1] instanceof Boolean )
                    && checkPojoMetaClass()) {
                final boolean[] objects = (boolean[]) receiver;
                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Boolean)args[1]).booleanValue();
                return null;
            }
            else
              return super.call(receiver,args);
        }

        public Object call(Object receiver, Object arg1, Object arg2) throws Throwable {
            if (checkPojoMetaClass()) {
                try {
                    final boolean[] objects = (boolean[]) receiver;
                    objects[normaliseIndex(((Integer) arg1).intValue(), objects.length)] = ((Boolean)arg2).booleanValue();
                    return null;
                }
                catch (ClassCastException e) {
                    if ((receiver instanceof boolean[]) && (arg1 instanceof Integer))
                      throw e;
                }
            }
            return super.call(receiver,arg1,arg2);
        }
    }
}
