/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy

/** 
 * Tests creating Maps in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class MapConstructionTest extends GroovyTestCase {

    void testMap() {
        def m = [ 1 : 'abc', 2 : 'def', 3 : 'xyz' ]

        println(m)

        def mtoo = [ 1 : [ "innerKey" : "innerValue" ], 2 : m ]

        println(mtoo)

        assertMap(m)
    }

    def testMapAsParameter() {
        assertMap([ 1 : 'abc', 2 : 'def', 3 : 'xyz' ])
    }

    def testMapViaHashMap() {
        def m = new HashMap()
        m.put(1, 'abc')
        m.put(2, 'def')
        m.put(3, 'xyz')
        assertMap(m)
    }

    void assertMap(m) {
        assert m instanceof Map
        // do not test the final type, i.e. assuming m is a HashMap

        def result = 0
        def text = ""
        for ( e in m ) {
            result = result + e.key
            text = text + e.value
        }
        assert result == 6
        assert text == "abcdefxyz"

        assert m.size() == 3

        assert m[2] == 'def'
    }
}
