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

import groovy.lang.GString;
import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import static org.codehaus.groovy.reflection.ReflectionCache.OBJECT_ARRAY_CLASS;

public class ObjectArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {

    @Override
    public final CachedClass getDeclaringClass() {
        return OBJECT_ARRAY_CLASS;
    }

    @Override
    public Object invoke(final Object object, final Object[] arguments) {
        var array = (Object[]) object;
        int index = normaliseIndex((Integer) arguments[0], array.length);
        array[index] = adjustNewValue(array, arguments[1]);
        return null;
    }

    private static Object adjustNewValue(final Object[] objects, final Object newValue) {
        Class<?> arrayComponentClass = objects.getClass().getComponentType();
        Object adjustedNewVal = newValue;
        if (newValue instanceof Number) {
            if (!arrayComponentClass.equals(newValue.getClass())) {
                adjustedNewVal = DefaultTypeTransformation.castToType(newValue, arrayComponentClass);
            }
        } else if (Character.class.isAssignableFrom(arrayComponentClass)) {
            adjustedNewVal = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
        } else if (String.class.equals(arrayComponentClass) && newValue instanceof GString) {
            adjustedNewVal = DefaultTypeTransformation.castToType(newValue, arrayComponentClass);
        } else if (Number.class.isAssignableFrom(arrayComponentClass)) {
            if (newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
                Character ch = DefaultTypeTransformation.getCharFromSizeOneString(newValue);
                adjustedNewVal = DefaultTypeTransformation.castToType(ch, arrayComponentClass);
            }
        } else if (arrayComponentClass.isArray()) {
            adjustedNewVal = DefaultTypeTransformation.castToType(newValue, arrayComponentClass);
        }
        return adjustedNewVal;
    }

    @Override
    public CallSite createPojoCallSite(final CallSite site, final MetaClassImpl metaClass, final MetaMethod metaMethod, final Class[] params, final Object receiver, final Object[] args) {
        if (!(args[0] instanceof Integer)) {
            return PojoMetaMethodSite.createNonAwareCallSite(site, metaClass, metaMethod, params, args);
        } else {
            return new PojoMetaMethodSite(site, metaClass, metaMethod, params) {
                @Override
                public Object call(final Object receiver, final Object index, final Object value) throws Throwable {
                    if (checkPojoMetaClass()) {
                        try {
                            Object[] array = (Object[]) receiver;
                            array[normaliseIndex((Integer) index, array.length)] = adjustNewValue(array, value);
                            return null;
                        }
                        catch (ClassCastException e) {
                            if ((receiver instanceof Object[]) && (index instanceof Integer)) {
                                throw e;
                            }
                        }
                    }
                    return super.call(receiver, index, value);
                }
            };
        }
    }
}
