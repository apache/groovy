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
package org.apache.groovy.contracts.util;

import java.lang.reflect.Field;

/**
 * This utility is meant to be used to replace direct calls to private
 * field variables in class invariants.
 */
public class FieldValues {

    @SuppressWarnings("unchecked")
    public static <T> T fieldValue(Object obj, String fieldName, Class<T> type) throws IllegalAccessException {
        Validate.notNull(obj);
        Validate.notNull(fieldName);

        Field f = findField(obj.getClass(), "thisObject");
        if (f == null) throw new IllegalArgumentException("Field thisObject could not be found!");
        f.setAccessible(true);

        Object target = f.get(obj);

        f = findField(target.getClass(), fieldName);
        if (f == null) throw new IllegalArgumentException("Field " + fieldName + " could not be found!");
        f.setAccessible(true);

        return (T) f.get(target);
    }

    private static Field findField(Class<?> clazz, String name) {
        Class<?> next = clazz;
        while (!Object.class.equals(next) && next != null) {
            Field[] fields = next.getDeclaredFields();
            for (Field field : fields) {
                if ((name.equals(field.getName()))) {
                    return field;
                }
            }
            next = next.getSuperclass();
        }
        return null;
    }
}
