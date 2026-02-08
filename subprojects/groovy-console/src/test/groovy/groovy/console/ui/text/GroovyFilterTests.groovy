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
package groovy.console.ui.text

import groovy.test.GroovyTestCase

class GroovyFilterTests extends GroovyTestCase {

    void testDecimalIntegerLiteral() {

        assert '0' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL
        assert !('01' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL)
        assert '1' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL
        assert '1234l' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL
        assert '1234L' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL
        assert '1_000_000' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL
        assert '1__000__000' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL
        assert !('_123' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL)
        assert !('123_' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL)
        assert !('123_L' ==~ GroovyFilter.DECIMAL_INTEGER_LITERAL)
    }

    void testHexIntegerLiteral() {

        assert '0xA' ==~ GroovyFilter.HEX_INTEGER_LITERAL
        assert '0XA' ==~ GroovyFilter.HEX_INTEGER_LITERAL
        assert !('0x' ==~ GroovyFilter.HEX_INTEGER_LITERAL)
        assert !('AB' ==~ GroovyFilter.HEX_INTEGER_LITERAL)
        assert !('0x_123' ==~ GroovyFilter.HEX_INTEGER_LITERAL)
        assert !('0x123_' ==~ GroovyFilter.HEX_INTEGER_LITERAL)
        assert '0xAA01' ==~ GroovyFilter.HEX_INTEGER_LITERAL
        assert '0xAA_BB_CC' ==~ GroovyFilter.HEX_INTEGER_LITERAL
        assert '0x1_AA_BB_CC' ==~ GroovyFilter.HEX_INTEGER_LITERAL
        assert '0xAAAA_FFFF' ==~ GroovyFilter.HEX_INTEGER_LITERAL
        assert !('123' ==~ GroovyFilter.HEX_INTEGER_LITERAL)
    }

    void testOctalIntegerLiteral() {

        assert '011223344' ==~ GroovyFilter.OCTAL_INTEGER_LITERAL
        assert '01_22_33_77' ==~ GroovyFilter.OCTAL_INTEGER_LITERAL
        assert !('0_111' ==~ GroovyFilter.OCTAL_INTEGER_LITERAL)
        assert !('01119' ==~ GroovyFilter.OCTAL_INTEGER_LITERAL)
        assert !('01_00_11_' ==~ GroovyFilter.OCTAL_INTEGER_LITERAL)
        assert !('123' ==~ GroovyFilter.OCTAL_INTEGER_LITERAL)
    }

    void testBinaryIntegerLiteral() {

        assert '0b0011' ==~ GroovyFilter.BINARY_INTEGER_LITERAL
        assert '0B0011' ==~ GroovyFilter.BINARY_INTEGER_LITERAL
        assert '0b0000_1111_1111' ==~ GroovyFilter.BINARY_INTEGER_LITERAL
        assert !('0b_111' ==~ GroovyFilter.BINARY_INTEGER_LITERAL)
        assert !('0b111_' ==~ GroovyFilter.BINARY_INTEGER_LITERAL)
        assert !('123' ==~ GroovyFilter.BINARY_INTEGER_LITERAL)
    }

    void testDecimalFloatingPointLiteral() {

        assert '0.0' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '0.0f' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '0.0F' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '.0D' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '.0d' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '10.0e123' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '10.0e-123' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '10.0e+123' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '10.0e+1_2_3' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert '10.0E+1_2_3' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL
        assert !('01.0d' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL)
        assert !('_1.0d' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL)
        assert !('1_.0d' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL)
        assert !('1.0_d' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL)
        assert !('10.0e+1_2_3_' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL)
        assert !('10.0e+' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL)
        assert !('10.0e+_' ==~ GroovyFilter.DECIMAL_FLOATING_POINT_LITERAL)
    }

    void testHexadecimalFloatingPointLiteral() {

        assert '0x0' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0XAA' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0x.0' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0x.0' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0xAB.' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0xAB.p+123' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0xAB.p-123' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0xAB.P-123' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert '0xAB.p123' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL
        assert !('0x_AB.' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL)
        assert !('0xAB._' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL)
        assert !('0x._' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL)
        assert !('123' ==~ GroovyFilter.HEXADECIMAL_FLOATING_POINT_LITERAL)
    }

    void testQuotes() {

        assert '"test"' ==~ GroovyFilter.QUOTES
        assert '""' ==~ GroovyFilter.QUOTES

        assert '"""test""' ==~ GroovyFilter.QUOTES
        assert '""""""' ==~ GroovyFilter.QUOTES
    }

    void testSingleQuotes() {

        assert "''" ==~ GroovyFilter.SINGLE_QUOTES
        assert "'a'" ==~ GroovyFilter.SINGLE_QUOTES

        assert "'''a'''" ==~ GroovyFilter.SINGLE_QUOTES
        String multilineTripleSingleQuotes =  """'''
        a
        '''"""
        assert multilineTripleSingleQuotes ==~ GroovyFilter.SINGLE_QUOTES
    }

    void testSlashyQuotes() {
        assert '/foo/' ==~ GroovyFilter.SLASHY_QUOTES
        assert '/foo\\//' ==~ GroovyFilter.SLASHY_QUOTES
        assert '/foo\\/bar\\//' ==~ GroovyFilter.SLASHY_QUOTES
        assert '/foo\\/bar\\\\/\\//' ==~ GroovyFilter.SLASHY_QUOTES

        assert '$/foo$/' ==~ GroovyFilter.SLASHY_QUOTES
        assert '$/foo\nbar$/' ==~ GroovyFilter.SLASHY_QUOTES
        assert '$/foo\n/bar//$/' ==~ GroovyFilter.SLASHY_QUOTES
        assert '$/foo\n/bar//\n$/' ==~ GroovyFilter.SLASHY_QUOTES
    }
}
