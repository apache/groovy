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

/**
 * @todo GROOVY-1037
 *
 *    $Revision 1.0
 *    Test for non-initialized fields or variables of the primitive types.
 *
 * @author Pilho Kim
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

    void testThisPrimitiveDefaultValues() {
        this.x == 0
        this.y == 0L
        this.z == 0.0
        this.b == (byte) 0
        this.s == (short) 0
        this.f == 0.0F
        this.flag == false
        this.c == (char) 0
    }

    void testPrimitiveDefaultValues() {
        def a = new ClassForPrimitiveDefaultValue()
        a.x == 0
        a.y == 0L
        a.z == 0.0
        a.b == (byte) 0
        a.s == (short) 0
        a.f == 0.0F
        a.flag == false
        a.c == (char) 0
    }

    void testDefaultPrimitiveValuesForAttributes() {
        def a = new ClassForPrimitiveDefaultValue()
        a.@x == 0
        a.@y == 0L
        a.@z == 0.0
        a.@b == (byte) 0
        a.@s == (short) 0
        a.@f == 0.0F
        a.@flag == false
        a.@c == (char) 0
    }

    void testDefaultPrimitiveValuesForProperties() {
        def a = new ClassForPrimitiveDefaultValue()
        a.x1 == 0
        a.y1 == 0L
        a.z1 == 0.0
        a.b1 == (byte) 0
        a.s1 == (short) 0
        a.f1 == 0.0F
        a.flag1 == false
        a.c1 == (char) 0
    }
}

class ClassForPrimitiveDefaultValue {
    int x
    long y
    double z
    byte b
    short s
    float f
    boolean flag
    char c

    int x1
    long y1
    double z1
    byte b1
    short s1
    float f1
    boolean flag1
    char c1
}


