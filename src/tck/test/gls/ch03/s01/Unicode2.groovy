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
 */
class Unicode2 extends GroovyTestCase {

    void testUTF16SupplementaryCharacters() {
        def s = "\uD840\uDC00"
        assert 2 == s.length() // number of Unicode code units
        assert 1 == s.codePointCount(0, s.length()) // number of Unicode code points
    }

    /*
    void testIdentifiers() {
        def foo\u0044 = 12
        assert 20 == foo\u0044 + 8
    }
    */
}

