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

class ParsingTest extends CompilableTestSupport {
    void testExpressionParsingWithCastingInFrontOfAClosure() {
        int[] numbers = new int[3]

        shouldCompile """
            (String) {-> print ""}.call()
        """
    
        shouldCompile """
            (String[]) {-> print ""}.call()
        """

        shouldCompile """
            (short) {-> print numbers[0]}.call()
        """
        
        shouldCompile """
            (short[]) {-> print numbers}.call()
        """
        def testObj = new Groovy2605()

        def val1 = (Groovy2605) {-> return testObj}.call()
        assert val1 instanceof Groovy2605
        
        def val2 = (String){-> return testObj}.call()
        assert val2 instanceof String
        assert val2 == "[A Groovy2605 object]"

        def val3 = (short) {-> return numbers[0]}.call()
        assert val3 instanceof Short

        def val4 = (short[]) {-> return numbers}.call()
        assert val4.class.componentType == short
    }

    void testCastPrecedence_Groovy4421_Groovy5185() {
        def i = (int)1/(int)2
        assert i.class==BigDecimal

        def result = (long)10.7 % 3L
        assert result == 1 && result instanceof Long

        assert '42' == (String) { -> 40 + 2 }.call()

        def percentage = 5.3
        assert '5%' == (int)Math.floor(percentage) + "%"

        def someInt = Integer.MAX_VALUE
        assert 4294967294L == (long)someInt + someInt
    }

    void testExpressionParsingWithCastInFrontOfAMap() {
        shouldCompile """
            def m = (Map)[a:{ "foo"; println 'bar' }]
        """
    }
}

class Groovy2605 {
    String toString(){
        return "[A Groovy2605 object]"
    }
}
