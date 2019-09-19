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
package gls.ch03.s03

import groovy.test.GroovyTestCase

/**
 * GLS 3.3:
 * Implementations first recognize Unicode escapes in their input, translating 
 * the ASCII characters backslash and 'u' followed by four hexadecimal digits
 * to the Unicode character with the indicated hexadecimal value, and passing
 * all other characters unchanged.  
 */
class UnicodeEscapes2 extends GroovyTestCase {

    // GLS: If an even number of backslashes precede the 'u', it is not 
    // an escape
    /*
    void testCountBackslash() {
        def a = 1
        assert \u0061 == 1 // char 61 is 'a'
        
        // Not intepreted as an escape
        // \\u0061 == 1 // @fail:parse

        assert "\u0061".length() == 1
        // Double backslash interpreted as a single backslash in string
        assert "\\u0061".length() == 6
        assert "\\\u0061".length() == 2
        
    }
    */

    // GLS: If an eligible \ is followed by u, or more than one u, and the last u
    // is not followed by four hexadecimal digits, then a compile-time error
    // occurs.
    /*
    void testFourHexDigits() {
        // If five digits, only the first four count
        def \u00610 = 2
        assert a0 == 2

        // Subsequent lines won't work. The backslash has been replaced by a forward slash
        // so that the file parses. (Comments don't comment out unicode escapes.)
        // assert "/u7" == "\07" //@fail:parse 
        // def /u61 = 2 //@fail:parse 
        // def /u061 = 2 //@fail:parse 
    }
    */

    void testInvalidHexDigits() {
        // Subsequent lines won't work. The backslash has been replaced by a forward slash
        // so that the file parses. (Comments don't comment out unicode escapes.)
        // assert "/ufffg" == "a" // @fail:parse
        // assert "/uu006g" == "a" // @fail:parse
        // assert "/uab cd" == "acd" // @fail:parse
    }
}
