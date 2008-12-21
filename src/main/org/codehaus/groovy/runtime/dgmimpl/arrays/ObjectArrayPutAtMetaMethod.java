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

import groovy.lang.GString;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

public class ObjectArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
    private static final CachedClass OBJECT_CLASS = ReflectionCache.getCachedClass(Object.class);
    private static final CachedClass OBJECT_ARR_CLASS = ReflectionCache.OBJECT_ARRAY_CLASS;
    private static final CachedClass [] PARAM_CLASS_ARR = new CachedClass[] {INTEGER_CLASS, OBJECT_CLASS};

    public ObjectArrayPutAtMetaMethod() {
        parameterTypes = PARAM_CLASS_ARR;
    }

    public final CachedClass getDeclaringClass() {
        return OBJECT_ARR_CLASS;
    }

    public Object invoke(Object object, Object[] arguments) {
        final Object[] objects = (Object[]) object;
        Object newValue = arguments[1];
    	Class arrayComponentClass = objects.getClass().getComponentType();
    	final int index = normaliseIndex(((Integer) arguments[0]).intValue(), objects.length);
        if (newValue instanceof Number) {
        	if (!arrayComponentClass.equals(newValue.getClass())) {
        		Object newVal = DefaultTypeTransformation.castToType(newValue, arrayComponentClass);
        		objects[index] = newVal;
        		return null;
        	}
        } else if (Character.class.isAssignableFrom(arrayComponentClass)) {
        	objects[index] = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
            return null;
        } else if (Number.class.isAssignableFrom(arrayComponentClass)) {
        	if(newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
        		Character ch = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        		objects[index] = DefaultTypeTransformation.castToType(ch, arrayComponentClass);
        		return null;
        	}
        }
        objects[index] = arguments[1];
        return null;
    }

    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        if (!(args [0] instanceof Integer))
          return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
        else
            return new MyPojoMetaMethodSite(site, metaClass, metaMethod, params);
    }

    private static class MyPojoMetaMethodSite extends PojoMetaMethodSite {
        public MyPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        public Object call(Object receiver, Object arg1, Object arg2) throws Throwable {
            if (checkPojoMetaClass()) {
                try {
                    final Object[] objects = (Object[]) receiver;
                    objects[normaliseIndex(((Integer) arg1).intValue(), objects.length)] = arg2;
                    return null;
                }
                catch (ClassCastException e) {
                    if ((receiver instanceof Object[]) && (arg1 instanceof Integer))
                      throw e;
                }
            }
            return super.call(receiver,arg1,arg2);
        }
    }
}
