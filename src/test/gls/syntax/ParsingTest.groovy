/*
 * Copyright 2003-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gls.syntax

public class ParsingTest extends gls.CompilableTestSupport {
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
