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
package operator

import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic

/**
 * Test Logical Implication Operation
 */
class ImplicationOperatorTest extends GroovyTestCase {

    void testImplicationOperation() {
        assert false ==> false
        assert false ==> true
        assert !(true ==> false)
        assert true ==> true
    }

    void testImplicationIsRightAssociative() {
        assert false ==> false ==> false
        assert false ==> (false ==> false)
        assert !((false ==> false) ==> false)
    }

    @CompileStatic
    void testImplicationOperationCS() {
        boolean result = (false ==> false) && (false ==> true) && !(true ==> false) && (true ==> true)
        assert result
    }

    void testPrecedence1() {
        def result = false ==> false ? true ==> false : true ==> true
        assert !result
    }

    void testPrecedence2() {
        def result = true ==> true || false ==> false
        assert !result
    }

    void testPrecedence3() {
        def result = true ==> true | false ==> false
        assert !result
    }

    void testPrecedence4() {
        def result = true ==> false && true ==> false
        assert result
    }

    void testPrecedence5() {
        def result = true ==> false & true ==> false
        assert result
    }

    void testPrecedence6() {
        def result = 'abc' ==~ /[a-z]+/ ==> 'abc' ==~ /[a]+/
        assert !result
    }

    void testPrecedence7() {
        def result = 'abc' ==~ /[a]+/ ==> 'abc' ==~ /[a]+/ ==> 'abc' ==~ /[a]+/
        assert result
    }

    void testShortCircuiting() {
        String str = null
        assert str != null ==> 0 <= str.length()

        str = "abc"
        assert str != null ==> 0 <= str.length()
    }
}
