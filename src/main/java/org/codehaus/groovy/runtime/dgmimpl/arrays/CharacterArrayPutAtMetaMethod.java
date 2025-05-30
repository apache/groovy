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
package org.codehaus.groovy.runtime.dgmimpl.arrays;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;
import org.codehaus.groovy.runtime.typehandling.ShortTypeHandling;

public class CharacterArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
    private static final CachedClass ARRAY_CLASS = ReflectionCache.getCachedClass(char[].class);

    @Override
    public final CachedClass getDeclaringClass() {
        return ARRAY_CLASS;
    }

    @Override
    public Object invoke(Object object, Object[] args) {
        final char[] objects = (char[]) object;
        final int index = normaliseIndex((Integer) args[0], objects.length);
        objects[index] = ShortTypeHandling.castToChar(args[1]);
        return null;
    }

    @Override
    public CallSite createPojoCallSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Object receiver, Object[] args) {
        if (!(args[0] instanceof Integer) || !(args[1] instanceof Character))
            return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
        else
            return new MyPojoMetaMethodSite(site, metaClass, metaMethod, params);
    }

    private static class MyPojoMetaMethodSite extends PojoMetaMethodSite {
        public MyPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params) {
            super(site, metaClass, metaMethod, params);
        }

        @Override
        public Object call(Object receiver, Object[] args) throws Throwable {
            if ((receiver instanceof char[] && args[0] instanceof Integer && args[1] instanceof Character)
                    && checkPojoMetaClass()) {
                final char[] objects = (char[]) receiver;
                objects[normaliseIndex((Integer) args[0], objects.length)] = (Character) args[1];
                return null;
            } else
                return super.call(receiver, args);
        }

        @Override
        public Object call(Object receiver, Object arg1, Object arg2) throws Throwable {
            if (checkPojoMetaClass()) {
                try {
                    final char[] objects = (char[]) receiver;
                    objects[normaliseIndex((Integer) arg1, objects.length)] = (Character) arg2;
                    return null;
                }
                catch (ClassCastException e) {
                    if ((receiver instanceof char[]) && (arg1 instanceof Integer))
                        throw e;
                }
            }
            return super.call(receiver, arg1, arg2);
        }
    }
}
