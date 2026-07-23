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
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.codehaus.groovy.runtime.typehandling.ShortTypeHandling;

public class IntegerArrayPutAtMetaMethod extends ArrayPutAtMetaMethod {
    private static final CachedClass ARRAY_CLASS = ReflectionCache.getCachedClass(int[].class);

    @Override
    public final CachedClass getDeclaringClass() {
        return ARRAY_CLASS;
    }

    @Override
    public Object invoke(Object object, Object[] args) {
        final int[] objects = (int[]) object;
        final int index = normaliseIndex((Integer) args[0], objects.length);
        Object newValue = args[1];
        if (!(newValue instanceof Integer)) {
            if (newValue instanceof Character || newValue instanceof String || newValue instanceof GString) {
                Character ch = ShortTypeHandling.castToChar(newValue);
                objects[index] = (Integer) DefaultTypeTransformation.castToType(ch, Integer.class);
            } else {
                objects[index] = ((Number) newValue).intValue();
            }
        } else
            objects[index] = (Integer) args[1];
        return null;
    }
}
