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
package org.apache.groovy.runtime

import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.junit.jupiter.api.Assertions.assertArrayEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotSame
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests for the {@link ObjectUtil} binary-compatibility facade (GROOVY-12257).
 * <p>
 * Class files compiled by Groovy 4.0.5+ with {@code @Immutable} contain
 * {@code INVOKESTATIC org/apache/groovy/runtime/ObjectUtil.cloneObject
 * (Ljava/lang/Object;)Ljava/lang/Object;} in generated constructors and
 * getters, so both the exact linkage shape and the 4.x runtime semantics
 * are locked in here.
 */
final class ObjectUtilTest {

    /** The exact symbol Groovy 4 bytecode links against must keep resolving. */
    @Test
    void cloneObject_keepsTheErasedSignatureGroovy4BytecodeReferences() {
        def m = ObjectUtil.getMethod('cloneObject', Object)
        assertTrue(Modifier.isPublic(m.modifiers))
        assertTrue(Modifier.isStatic(m.modifiers))
        assertEquals(Object, m.returnType)
        assertEquals('org.apache.groovy.runtime.ObjectUtil', m.declaringClass.name)
    }

    @Test
    void cloneObject_null_returnsNull() {
        assertNull(ObjectUtil.cloneObject(null))
    }

    @Test
    void cloneObject_nonCloneable_throwsCloneNotSupported() {
        def e = assertThrows(CloneNotSupportedException) { ObjectUtil.cloneObject('a string') }
        assertEquals('java.lang.String', e.message)
    }

    @Test
    void cloneObject_primitiveArrays_cloneToEqualDistinctArrays() {
        byte[] bytes = [1, 2]
        short[] shorts = [3, 4]
        int[] ints = [5, 6]
        char[] chars = ['a' as char, 'b' as char]
        long[] longs = [7L, 8L]
        float[] floats = [1.5f, 2.5f]
        double[] doubles = [3.5d, 4.5d]
        boolean[] booleans = [true, false]
        [bytes, shorts, ints, chars, longs, floats, doubles, booleans].each { array ->
            def copy = ObjectUtil.cloneObject(array)
            assertNotSame(array, copy)
            assertEquals(array.class, copy.class)
            assertTrue(Arrays.equals(array, copy))
        }
    }

    @Test
    void cloneObject_objectArray_clonesShallowly() {
        String[] source = ['x', 'y']
        String[] copy = ObjectUtil.cloneObject(source)
        assertNotSame(source, copy)
        assertArrayEquals(source, copy)
    }

    /** The reproducer shape: a Cloneable collection defensively copied by @Immutable. */
    @Test
    void cloneObject_arrayList_clonesViaPublicClone() {
        def source = new ArrayList<>(['value'])
        def copy = ObjectUtil.cloneObject(source)
        assertNotSame(source, copy)
        assertEquals(source, copy)
        copy << 'mutated'
        assertEquals(['value'], source)
    }

    @Test
    void cloneObject_cloneableWithPublicClone_usesIt() {
        def source = new PublicClone(value: 42)
        def copy = ObjectUtil.cloneObject(source)
        assertNotSame(source, copy)
        assertEquals(42, copy.value)
    }

    /** The 4.x implementation used getMethod, so a non-public clone() never worked; keep that contract. */
    @Test
    void cloneObject_cloneableWithoutPublicClone_throwsNoSuchMethod() {
        assertThrows(NoSuchMethodException) { ObjectUtil.cloneObject(new ProtectedClone()) }
    }

    static final class PublicClone implements Cloneable {
        int value

        @Override
        Object clone() {
            new PublicClone(value: value)
        }
    }

    static final class ProtectedClone implements Cloneable {
        @Override
        protected Object clone() {
            super.clone()
        }
    }
}
