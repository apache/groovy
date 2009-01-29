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

import java.util.concurrent.LinkedBlockingQueue

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

    void testThreadNaming() {
        def t = Thread.start("MyNamedThread"){
            sleep 200 // give ourselves time to find the thread
        }
        assert Thread.allStackTraces.keySet().any{ thread -> thread.name == 'MyNamedThread' }
        t.join()
    }

    void testDefiningQueue() {
        def result = [1, 2, 3, 4, 5] as Queue
        assert result instanceof Queue
        assert result.size() == 5
        assert result.sum() == 15
    }

    void testLinkedBlockingQueue() {
        def q = [1, 2, 3, 4, 5, 6, 7, 8, 9] as LinkedBlockingQueue
        assert q.size() == 9
        assert q.class == LinkedBlockingQueue
        q += 10
        assert q.size() == 10
        assert q.class == LinkedBlockingQueue
        def r = q.findAll{ it % 2 == 0 }
        assert r.size() == 5
        assert r.class == LinkedBlockingQueue
        def s = ((r as LinkedList) - [4, 6]) as LinkedBlockingQueue
        assert s.size() == 3
        assert s.class == LinkedBlockingQueue
        [2, 8, 10].each{ assert it in s }
    }
}

enum Suit { HEARTS, CLUBS, SPADES, DIAMONDS }
