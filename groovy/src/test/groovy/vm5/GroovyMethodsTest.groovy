/*
 * Copyright 2007-2008 the original author or authors.
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
package groovy.vm5

/** 
 * Tests the Java 5 variants of various new Groovy methods.
 *
 * @author Mike Dillon
 * @author Paul King
 */
class GroovyMethodsTest extends GroovyTestCase {
    void testEachOnEnumClassIteratesThroughTheValuesOfTheEnum() {
        def expected = Suit.values().toList()
        def answer = []
        Suit.each { answer << it }
        assert answer == expected
    }

    void testForLoopWithEnumClassIteratesThroughTheValuesOfTheEnum() {
        def expected = Suit.values().toList()
        def answer = []
        for (s in Suit) {
            answer << s
        }
        assert answer == expected
    }

    void testAsEnumType() {
        assert Suit.HEARTS == ("HEARTS" as Suit)

        shouldFail(IllegalArgumentException) {
            "FOO" as Suit
        } 
    }

    void testJavaEnumType() {
        def x = Language.English
        x++
        assert x == Language.French
        x = Language.English
        x--
        assert x == Language.Spanish
        assert Language.French in Language.English..Language.Spanish
    }

    void testThreadNaming() {
        def t = Thread.start("MyNamedThread"){
            sleep 100 // give ourselves time to find the thread
        }
        assert Thread.allStackTraces.keySet().any{ thread -> thread.name == 'MyNamedThread' }
        t.join()
    }

    void testStringBuilderPlusPutAtSizeLeftShift() {
        def sb = new StringBuilder('foo')
        assert sb + 'bar' == 'foobar'
        sb << 'baz'
        assert sb.size() == 6
        def result = sb.toString()
        assert result == 'foobaz'
        sb[3..4] = 'abc'
        result = sb.toString()
        assert result == 'fooabcz'
        sb[6..<6] = 'xy'
        result = sb.toString()
        assert result == 'fooabcxyz'
    }
}

enum Suit { HEARTS, CLUBS, SPADES, DIAMONDS }
