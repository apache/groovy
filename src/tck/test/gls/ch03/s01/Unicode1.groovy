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
package gls.ch03.s01

import groovy.test.GroovyTestCase

/**
 * Except for comments, identifiers and the contents of ... string 
 * literals, all input elements are formed from ASCII characters.
 *
 * TODO: Find a better way to test these things
 * Note that this is a little hard to test since the input file is ASCII.
 */
class Unicode1 extends GroovyTestCase {
    //TODO: find some way to assert that Unicode3.0 + is available

    /**
      * This doc comment checks that Unicode is allowed in javadoc.
      * e.g. \u05D0\u2136\u05d3\u05d7
      */
    void testComments() {
        // Unicode is allowed in comments
        // This is a comment \u0410\u0406\u0414\u0419
        /* Another comment \u05D0\u2136\u05d3\u05d7 */

        /**/ // Tiny comment
        /***/ // Also valid
    }

    void testStringLiterals() {
        assert 1 == "\u0040".length()
        assert "A" == "\u0041"
    }

    void testCharNotAvailableAsLiteral() {
        char a = 'x'
        char b = "x"
        def c = "x".charAt(0)
        assert a == b
        assert a == c 
    }

}

