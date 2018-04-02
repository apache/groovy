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
package org.apache.groovy.jsondirect;

import org.apache.groovy.json.FastStringService;
import org.apache.groovy.json.FastStringServiceFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class DirectFastStringServiceFactory implements FastStringServiceFactory {
    static final Unsafe UNSAFE;
    static final long STRING_VALUE_FIELD_OFFSET;
    private static final boolean ENABLED;

    static final boolean WRITE_TO_FINAL_FIELDS = Boolean.parseBoolean(System.getProperty("groovy.json.faststringutils.write.to.final.fields", "false"));
    private static final boolean DISABLE = Boolean.parseBoolean(System.getProperty("groovy.json.faststringutils.disable", "false"));

    private static Unsafe loadUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);

        } catch (Exception e) {
            return null;
        }
    }

    static {
        UNSAFE = DISABLE ? null : loadUnsafe();
        ENABLED = UNSAFE != null;
    }

    private static long getFieldOffset(String fieldName) {
        if (ENABLED) {
            try {
                return UNSAFE.objectFieldOffset(String.class.getDeclaredField(fieldName));
            } catch (NoSuchFieldException e) {
                // field undefined
            }
        }
        return -1L;
    }

    static {
        STRING_VALUE_FIELD_OFFSET = getFieldOffset("value");
    }

    @Override
    public FastStringService getService() {
        if (STRING_VALUE_FIELD_OFFSET != -1L && valueFieldIsCharArray()) {
            return new DirectFastStringService();
        }
        // safe to return null here because then we'll get the default provider
        return null;
    }

    /**
     * JDK9 Compat Strings enhancement changed the internal representation of the value field from a char[]
     * to a byte[] (see http://openjdk.java.net/jeps/254).
     *
     * @return true if internal String value field is a char[], otherwise false
     */
    private static boolean valueFieldIsCharArray() {
        Object o = UNSAFE.getObject("", STRING_VALUE_FIELD_OFFSET);
        return (o instanceof char[]);
    }

}
