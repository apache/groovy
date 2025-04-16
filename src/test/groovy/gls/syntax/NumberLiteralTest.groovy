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

class NumberLiteralTest extends CompilableTestSupport {

    void testLargeNumbersOverflowing() {
        shouldCompile '''
            int a = 0x80000000
            a = 0x80000000i

            long longNumber = 0x8000000000000000L
            longNumber = 0x8000_0000_0000_0000L
            longNumber = 010_0000_0000_0000_0000_0000L
            longNumber = 0b1000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000_0000L
        '''
    }

    void testValidIntegerLiterals() {
        shouldCompile '''
             def a = 2147483647I
             def b = -2147483648I
             def c = -2147483647I
         '''
    }

    void testValidLongLiterals() {
        shouldCompile '''
             def d = 9223372036854775807L
             def e = -9223372036854775808L
             def f = -9223372036854775807L
        '''
    }

    void testInvalidIntegerLiteral() {
        shouldNotCompile '''
            def n = 2147483648I
        '''
    }

    void testInvalidLongLiteral() {
        shouldNotCompile '''
            def n = 9223372036854775808L
        '''
    }

}