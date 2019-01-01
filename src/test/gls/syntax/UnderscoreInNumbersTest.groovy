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
package gls.syntax

import gls.CompilableTestSupport

/**
 * Most of the below examples were taken from the Project Coin proposal here:
 * http://mail.openjdk.java.net/pipermail/coin-dev/2009-April/001628.html
 */
class UnderscoreInNumbersTest extends CompilableTestSupport {

    void testUnderscore() {
        assertScript '''
            assert 1_2_3_4_5 == 12345
        '''
    }

    /**
     * http://mail.openjdk.java.net/pipermail/coin-dev/2009-April/001628.html
     */
    void testExamplesShouldCompileFromProposal() {
        shouldCompile '''
            long creditCardNumber = 1234_5678_9012_3456L
            long socialSecurityNumbers = 999_99_9999L
            float monetaryAmount = 12_345_132.12
            long hexBytes = 0xFF_EC_DE_5E
            long hexWords = 0xFFEC_DE5E
            long maxLong = 0x7fff_ffff_ffff_ffffL
            long alsoMaxLong = 9_223_372_036_854_775_807L

            // this one is mentioned in the proposal but I think is wrong
            // since we shouldn't allow _., ie. an underscore (or a series of underscores)
            // should alwasy be surrounded with at least one number
            // double whyWouldYouEverDoThis = 0x1_.ffff_ffff_ffff_fp10_23
        '''
    }

    /**
     * http://mail.openjdk.java.net/pipermail/coin-dev/2009-April/001628.html
     */
    void testExampleFromProposalWithBinaryLiterals() {
        shouldCompile '''
            byte nybbles = 0b0010_0101
            long bytes = 0b11010010_01101001_10010100_10010010
            int weirdBitfields = 0b000_10_101
        '''
    }

    /**
     * Underscore must be placed between digits
     * http://mail.openjdk.java.net/pipermail/coin-dev/2009-April/001628.html
     */
    void testPositionOfUnderscoresAndWhatsValidOrInvalid() {
        shouldCompile '''
            int x1 = _52;  // This is an identifier, not a numeric literal.
            int x2 = 5_2;  // OK. (Decimal literal)
            int x3 = 5_______2; // OK. (Decimal literal.)

            int x6 = 0x5_2;  // OK. (Hexadecimal literal)

            int x7 = 0_52;   // OK. (Octal literal)
            int x8 = 05_2;   // OK. (Octal literal)
        '''

        shouldNotCompile '''
            int x2 = 52_;  // Illegal. (Underscores must always be between digits)

            int x4 = 0_x52;  // Illegal. Can't put underscores in the "0x" radix prefix.
            int x5 = 0x_52;  // Illegal. (Underscores must always be between digits)

            int x6 = 0x52_;  // Illegal. (Underscores must always be between digits)
            int x6 = 0x_;    // Illegal. (Not valid with the underscore removed)

            int x9 = 052_;   // Illegal. (Underscores must always be between digits)
        '''
    }

    void testInvalidPlacementOfUnderscore() {
        shouldNotCompile ''' def i = 10101_ '''
        shouldNotCompile ''' def d = 10101_.0 '''
        shouldNotCompile ''' def d = 10101.0_ '''
    }
}
