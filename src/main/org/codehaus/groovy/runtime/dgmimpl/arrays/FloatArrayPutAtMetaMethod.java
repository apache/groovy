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

public class FloatArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
        private static final CachedClass OBJECT_CLASS = ReflectionCache.OBJECT_CLASS;
        private static final CachedClass ARR_CLASS = ReflectionCache.getCachedClass(float[].class);
        private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

        public FloatArrayPutAtMetaMethod() {
            parameterTypes = PARAM_CLASS_ARR;
        }

        public final CachedClass getDeclaringClass() {
            return ARR_CLASS;
        }

        public Object invoke(Object object, Object[] args) {
            final float[] objects = (float[]) object;
            final int index = normaliseIndex(((Integer) args[0]).intValue(), objects.length);
            Object newValue = args[1];
            if (!(newValue instanceof Float)) {
                Number n = (Number) newValue;
                objects[index] = ((Number)newValue).floatValue();
            }
            else
              objects[index] = ((Float)args[1]).floatValue();
            return null;
        }

        public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
            if (!(args [0] instanceof Integer) || !(args [1] instanceof Float))
              return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
            else
                return new MyPojoMetaMethodSite(site, metaClass, metaMethod, params);
        }

    private static class MyPojoMetaMethodSite extends PojoMetaMethodSite {
        public MyPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        public Object call(Object receiver, Object[] args) throws Throwable {
            if ((receiver instanceof float[] && args[0] instanceof Integer && args[1] instanceof Float )
                    && checkPojoMetaClass()) {
                final float[] objects = (float[]) receiver;
                objects[normaliseIndex(((Integer) args[0]).intValue(), objects.length)] = ((Float)args[1]).floatValue();
                return null;
            }
            else
              return super.call(receiver,args);
        }

        public Object call(Object receiver, Object arg1, Object arg2) throws Throwable {
            if (checkPojoMetaClass()) {
                try {
                    final float [] objects = (float[]) receiver;
                    objects[normaliseIndex(((Integer) arg1).intValue(), objects.length)] = ((Float)arg2).floatValue();
                    return null;
                }
                catch (ClassCastException e) {
                    if ((receiver instanceof float[]) && (arg1 instanceof Integer))
                      throw e;
                }
            }
            return super.call(receiver,arg1,arg2);
        }
    }
}
