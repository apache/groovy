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
package org.codehaus.groovy.runtime;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public final class MetaClassHelperTest {

    @Test // GROOVY-1262
    public void testGetClassName() {
        assertNull(MetaClassHelper.getClassName(null));
    }

    @Test
    public void normalizeBoxedReturn_nullAndNonPrimitivePassThrough() {
        Object sentinel = new Object();
        assertNull(MetaClassHelper.normalizeBoxedReturn(null, int.class));
        assertNull(MetaClassHelper.normalizeBoxedReturn(null, void.class));
        assertSame(sentinel, MetaClassHelper.normalizeBoxedReturn(sentinel, String.class));
        assertSame(sentinel, MetaClassHelper.normalizeBoxedReturn(sentinel, Object.class));
    }

    @Test
    public void normalizeBoxedReturn_intUsesValueOfCache() {
        Integer cached = Integer.valueOf(42);
        assertSame(cached, MetaClassHelper.normalizeBoxedReturn(cached, int.class));
        Integer outside = Integer.valueOf(10_000);
        assertEquals(10_000, ((Integer) MetaClassHelper.normalizeBoxedReturn(outside, int.class)).intValue());
    }

    @Test
    public void normalizeBoxedReturn_booleanLongCharByteShortUseValueOfCache() {
        assertSame(Boolean.TRUE, MetaClassHelper.normalizeBoxedReturn(Boolean.TRUE, boolean.class));
        assertSame(Boolean.FALSE, MetaClassHelper.normalizeBoxedReturn(Boolean.FALSE, boolean.class));
        assertSame(Long.valueOf(42L), MetaClassHelper.normalizeBoxedReturn(Long.valueOf(42L), long.class));
        assertSame(Character.valueOf('a'), MetaClassHelper.normalizeBoxedReturn(Character.valueOf('a'), char.class));
        assertSame(Byte.valueOf((byte) 42), MetaClassHelper.normalizeBoxedReturn(Byte.valueOf((byte) 42), byte.class));
        assertSame(Short.valueOf((short) 42), MetaClassHelper.normalizeBoxedReturn(Short.valueOf((short) 42), short.class));
    }

    @Test
    public void normalizeBoxedReturn_floatAndDoubleUnchanged() {
        Float f = Float.valueOf(1.5f);
        Double d = Double.valueOf(1.5d);
        assertSame(f, MetaClassHelper.normalizeBoxedReturn(f, float.class));
        assertSame(d, MetaClassHelper.normalizeBoxedReturn(d, double.class));
    }

    @Test
    public void normalizeBoxedReturn_freshIntegerIsInternedThroughValueOf() throws Exception {
        Integer fresh = construct(Integer.class, int.class, 42);
        assertNotSame(Integer.valueOf(42), fresh);
        assertSame(Integer.valueOf(42), MetaClassHelper.normalizeBoxedReturn(fresh, int.class));
    }

    @Test
    public void normalizeBoxedReturn_freshBooleanIsInternedThroughValueOf() throws Exception {
        Boolean freshTrue = construct(Boolean.class, boolean.class, true);
        Boolean freshFalse = construct(Boolean.class, boolean.class, false);
        assertSame(Boolean.TRUE, MetaClassHelper.normalizeBoxedReturn(freshTrue, boolean.class));
        assertSame(Boolean.FALSE, MetaClassHelper.normalizeBoxedReturn(freshFalse, boolean.class));
    }

    @Test
    public void normalizeBoxedReturn_freshLongCharByteShortAreInternedThroughValueOf() throws Exception {
        assertSame(Long.valueOf(42L), MetaClassHelper.normalizeBoxedReturn(construct(Long.class, long.class, 42L), long.class));
        assertSame(Character.valueOf('a'), MetaClassHelper.normalizeBoxedReturn(construct(Character.class, char.class, 'a'), char.class));
        assertSame(Byte.valueOf((byte) 42), MetaClassHelper.normalizeBoxedReturn(construct(Byte.class, byte.class, (byte) 42), byte.class));
        assertSame(Short.valueOf((short) 42), MetaClassHelper.normalizeBoxedReturn(construct(Short.class, short.class, (short) 42), short.class));
    }

    @SuppressWarnings("unchecked")
    private static <T> T construct(Class<T> type, Class<?> param, Object arg) throws Exception {
        Constructor<T> ctor = type.getDeclaredConstructor(param);
        ctor.setAccessible(true);
        return ctor.newInstance(arg);
    }
}
