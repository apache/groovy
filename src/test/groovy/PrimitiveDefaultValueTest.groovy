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
package groovy

import groovy.test.GroovyTestCase

/**
 * Test for non-initialized fields or variables of the primitive types.
 */
class PrimitiveDefaultValueTest extends GroovyTestCase {

    private int x
    private long y
    private double z
    private byte b
    private short s
    private float f
    private boolean flag
    private char c

    private static final Character ZERO = '\0'

    void testThisPrimitiveDefaultValues() {
        assertEquals(this.x, 0)
        assertEquals(this.y, 0L)
        assertEquals(this.z, 0.0d)
        assertEquals(this.b, (byte) 0)
        assertEquals(this.s, (short) 0)
        assertEquals(this.f, 0.0f)
        assertFalse(this.flag)
        assertEquals(this.c, ZERO)
    }

    void testPrimitiveDefaultValues() {
        def a = new ClassForPrimitiveDefaultValue()
        assertEquals(a.x, 0)
        assertEquals(a.y, 0L)
        assertEquals(a.z, 0.0d)
        assertEquals(a.b, (byte) 0)
        assertEquals(a.s, (short) 0)
        assertEquals(a.f, 0.0f)
        assertFalse(a.flag)
        assertEquals(a.c, ZERO)
    }

    void testDefaultPrimitiveValuesForAttributes() {
        def a = new ClassForPrimitiveDefaultValue()
        assertEquals(a.@x, 0)
        assertEquals(a.@y, 0L)
        assertEquals(a.@z, 0.0d)
        assertEquals(a.@b, (byte) 0)
        assertEquals(a.@s, (short) 0)
        assertEquals(a.@f, 0.0f)
        assertFalse(a.@flag)
        assertEquals(a.@c, ZERO)
    }

    void testDefaultPrimitiveValuesForProperties() {
        def a = new ClassForPrimitiveDefaultValue()
        assertEquals(a.x1, 0)
        assertEquals(a.y1, 0L)
        assertEquals(a.z1, 0.0d)
        assertEquals(a.b1, (byte) 0)
        assertEquals(a.s1, (short) 0)
        assertEquals(a.f1, 0.0f)
        assertFalse(a.flag1)
        assertEquals(a.c1, ZERO)
    }
}

class ClassForPrimitiveDefaultValue {
    public int x
    public long y
    public double z
    public byte b
    public short s
    public float f
    public boolean flag
    public char c

    int x1
    long y1
    double z1
    byte b1
    short s1
    float f1
    boolean flag1
    char c1
}


