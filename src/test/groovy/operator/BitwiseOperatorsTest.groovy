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
package groovy.operator

/**
 * Test Bitwise Operations
 */
class BitwiseOperatorsTest extends GroovyTestCase {

    void testBitwiseShift() {
        def a = 4
        def b = -4
        assert a << 1 == 8
        assert a << 2 == 16
        assert a >> 1 == 2
        assert a >> 2 == 1
        assert a >>> 1 == 2
        assert a >>> 2 == 1
        assert b << 1 == -8
        assert b << 2 == -16
        assert b >> 1 == -2
        assert b >> 2 == -1
        assert b >>> 1 == 0x7FFFFFFE
        assert b >>> 2 == 0x3FFFFFFF
    }

    void testBitwiseShiftEQUAL() {
        def a = 4
        a <<= 1
        assert a == 8
        a <<= 2
        assert a == 32
        a >>= 1
        assert a == 16
        a >>= 2
        assert a == 4

        def b = -4
        b <<= 1
        assert b == -8
        b <<= 2
        assert b == -32
        b >>= 1
        assert b == -16
        b >>= 2
        assert b == -4

        b = -4
        b >>>= 1
        assert b == 0x7FFFFFFE
        b = -8
        b >>>= 2
        assert b == 0x3FFFFFFE
    }

    void testBitwiseAnd() {

        def a = 13
        assert (a & 3) == 1 // 0x0000000D & 0x00000003
        assert (a & 7) == 5 // 0x0000000D & 0x00000007
        def b = -13
        assert (b & 3) == 3 // 0xFFFFFFF3 & 0x00000003
        assert (b & 7) == 3 // 0xFFFFFFF3 & 0x00000007
    }

    void testBitwiseAndOperatorPrecedence() {
        def a = 13
        assert (a & 3) == 1 // 0x0000000D & 0x00000003
        assert (a & 7) == 5 // 0x0000000D & 0x00000007
        def b = -13
        assert (b & 3) == 3 // 0xFFFFFFF3 & 0x00000003
        assert (b & 7) == 3 // 0xFFFFFFF3 & 0x00000007
    }

    void testBitwiseAndEqual() {
        def a = 13
        a &= 3
        assert a == 1 // 0x0000000D & 0x00000003
        a &= 4
        assert a == 0 // 0x00000001 & 0x00000004
        def b = -13
        b &= 3
        assert b == 3 // 0xFFFFFFF3 & 0x00000003
        b &= 7
        assert b == 3 // 0x00000003 & 0x00000007
    }

    void testBitwiseOr() {
        def a = 13
        assert (a | 8) == 13 // 0x0000000D | 0x00000008
        assert (a | 16) == 29 // 0x0000000D | 0x00000010
        def b = -13
        assert (b | 8) == -5 // 0xFFFFFFF3 | 0x00000008
        assert (b | 16) == -13 // 0xFFFFFFF3 | 0x00000010
    }

    void testBitwiseOrOperatorPrecedence() {
        def a = 13
        assert (a | 8) == 13 // 0x0000000D | 0x00000008
        assert (a | 16) == 29 // 0x0000000D | 0x00000010
        def b = -13
        assert (b | 8) == -5 // 0xFFFFFFF3 | 0x00000008
        assert (b | 16) == -13 // 0xFFFFFFF3 | 0x00000010
    }

    void testBitwiseOrEqual() {
        def a = 13
        a |= 2
        assert a == 15 // 0x0000000D | 0x00000002
        a |= 16
        assert a == 31 // 0x0000000F | 0x0000001F
        def b = -13
        b |= 8
        assert b == -5 // 0xFFFFFFF3 | 0x00000008
        b |= 1
        assert b == -5 // 0xFFFFFFFB | 0x00000001
    }

    void testBitwiseXor() {
        def a = 13
        assert (a ^ 10) == 7 // 0x0000000D ^ 0x0000000A = 0x000000007
        assert (a ^ 15) == 2 // 0x0000000D ^ 0x0000000F = 0x000000002
        def b = -13
        assert (b ^ 10) == -7 // 0xFFFFFFF3 ^ 0x0000000A = 0xFFFFFFF9
        assert (b ^ 15) == -4 // 0xFFFFFFF3 ^ 0x0000000F = 0xFFFFFFFC
    }

    void testBitwiseXorOperatorPrecedence() {
        def a = 13
        assert (a ^ 10) == 7 // 0x0000000D ^ 0x0000000A = 0x000000007
        assert (a ^ 15) == 2 // 0x0000000D ^ 0x0000000F = 0x000000002
        def b = -13
        assert (b ^ 10) == -7 // 0xFFFFFFF3 ^ 0x0000000A = 0xFFFFFFF9
        assert (b ^ 15) == -4 // 0xFFFFFFF3 ^ 0x0000000F = 0xFFFFFFFC
    }

    void testBitwiseXorEqual() {
        def a = 13
        a ^= 8
        assert a == 5 // 0x0000000D ^ 0x00000008 = 0x000000005
        a ^= 16
        assert a == 21 // 0x00000005 ^ 0x00000010 = 0x000000015
        def b = -13
        b ^= 8
        assert b == -5 // 0xFFFFFFF3 ^ 0x00000008 = 0xFFFFFFFB
        b ^= 16
        assert b == -21 // 0xFFFFFFFB ^ 0x00000010 = 0xFFFFFFEB
    }

    void testBitwiseOrInClosure() {
        def c1 = { x, y -> return x | y }
        assert c1(14, 5) == 15 // 0x0000000E | 0x00000005 = 0x0000000F
        assert c1(0x0D, 0xFE) == 255 // 0x0000000D | 0x000000FE = 0x000000FF

        def c2 = { x, y -> return x | y }
        assert c2(14, 5) == 15 // 0x0000000E | 0x00000005 = 0x0000000F
        assert c2(0x0D, 0xFE) == 255 // 0x0000000D | 0x000000FE = 0x000000FF
    }

    void testAmbiguityOfBitwiseOr() {
        def c1 = { x, y -> return x | y }
        assert c1(14, 5) == 15 // 0x0000000E | 0x00000005 = 0x0000000F
        assert c1(0x0D, 0xFE) == 255 // 0x0000000D | 0x000000FE = 0x000000FF

        def c2 = { x, y -> return x | y }
        assert c2(14, 5) == 15 // 0x0000000E | 0x00000005 = 0x0000000F
        assert c2(0x0D, 0xFE) == 255 // 0x0000000D | 0x000000FE = 0x000000FF

        def x = 3
        def y = 5
        c1 = { xx -> return y } // -> is a closure delimiter
        c2 = { return x & y } // & is a bitAnd
        def c3 = { return x ^ y } // & is a bitXor
        def c11 = {
            xx -> return y // -> is a closure delimiter
        }
        def c12 = {
            return (x | y) // | is a bitOr
        }
        def c13 = { xx -> return y // -> is a closure delimiter
        }
        def c14 = { -> return x | y // last | is a bitOr
        }

        assert c1(null) == 5
        assert c2() == 1
        assert c3() == 6
        assert c11(null) == 5
        assert c12() == 7
        assert c13(null) == 5
        assert c14() == 7

        x = 0x03

        def d1 = { xx -> return xx } // -> is a closure delimiter
        def d2 = { return x & x } // & is a bitAnd
        def d3 = { return x ^ x } // & is a bitXor
        def d11 = {
            xx -> return xx // -> is a closure delimiter
        }
        def d12 = {
            return (x | x) // | is a bitOr
        }
        def d13 = { xx -> return xx // -> is a closure delimiter
        }
        def d14 = { -> return x | x // last | is a bitOr
        }
        assert d1(0xF0) == 0xF0
        assert d2(0xF0) == 0x03
        assert d3(0xF0) == 0
        assert d11(0xF0) == 0xF0
        assert d12(0xF0) == 0x03
        assert d13(0xF0) == 0xF0
        assert d14() == 0x03
    }

    void testBitwiseNegation() {
        assert ~1 == -2 // ~0x00000001 = 0xFFFFFFFE
        assert ~-1 == 0 // ~0xFFFFFFFF = 0x00000000
        assert ~~5 == 5 // ~~0x00000005 = ~0xFFFFFFFA = 0xFFFFFFF5
        def a = 13
        assert ~a == -14 // ~0x0000000D = 0xFFFFFFF2
        assert ~~a == 13 // ~~0x0000000D = ~0xFFFFFFF2 = 0x0000000D
        assert -~a == 14 // -~0x0000000D = -0xFFFFFFF2 = 0x0000000E
    }

    void testBitwiseNegationType() {
        def x = ~7
        assert x.class == java.lang.Integer

        def y = ~"foo"
        assert y.class == java.util.regex.Pattern

        def z = ~"${x}"
        assert z.class == java.util.regex.Pattern
    }

    void testBitwiseNegationTypeCallFunction() {
        // integer test
        assert neg(2).class == java.lang.Integer
        assert neg(2) instanceof java.lang.Integer
        assert neg(2) == ~2

        // long test
        assert neg(2L).class == java.lang.Long
        assert neg(2L) instanceof java.lang.Long
        assert neg(2L) == ~2

        // BigInteger test
        assert neg(new java.math.BigInteger("2")).class == java.math.BigInteger
        assert neg(new java.math.BigInteger("2")) instanceof java.math.BigInteger
        assert neg(new java.math.BigInteger("2")) == ~2

        // BigInteger test
        assert neg(2G).class == java.math.BigInteger
        assert neg(2G) instanceof java.math.BigInteger
        assert neg(2G) == ~2

        assert neg("foo").class == java.util.regex.Pattern
        assert neg("foo") instanceof java.util.regex.Pattern
    }

    void testCorrectAutoboxing() {
        // test that the first parameter is boxed correctly, if not then this test
        // will possibly produce a verify error
        assert (!true | false) == false
        assert (true | false) == true
        assert (!true & false) == false
        assert (true & false) == false
        assert (true & !false) == true

    }

    Object neg(n) {
        if (n instanceof java.lang.Integer) {
            return ~n
        }
        if (n instanceof java.lang.Long) {
            return ~n
        }
        if (n instanceof java.math.BigInteger) {
            return ~n
        }
        return ~n.toString()
    }
}