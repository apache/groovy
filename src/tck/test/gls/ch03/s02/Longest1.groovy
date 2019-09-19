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
package gls.ch03.s02

import groovy.test.GroovyTestCase

/**
 * GLS 3.2: The longest possible translation is used at each step, even if the 
 * result does not ultimately make a correct program while another lexical 
 * translation would.
 * 
 * This is fundamental to the way the lexer works. If there is a problem with
 * it, other tests (e.g. to test functionality of operators or identifier
 * names) would expose it quickly. Nevertheless, we test some combinations
 * here for consistency.
 */
class Longest1 extends GroovyTestCase {

    // Increment and decrement operators
    void testPrefixIncDec() {
        def a = 20
        def b = 10
        def c = a - b
        //c = a -- b // @fail:parse 
        //c = a ++ b // @fail:parse
        //c = a +- b // @pass
        //c = a -+ b // @pass
    }
}

