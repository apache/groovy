/*
 * Copyright 2008 the original author or authors.
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
package org.codehaus.groovy.transform.vm5

/**
 * @author Paul King
 */
class ImmutableTransformTest extends GroovyShellTestCase {

    void testImmutable() {
        def objects = evaluate("""
              enum Coin { HEAD, TAIL }
              @Immutable final class Bar {
                  String x, y
                  Coin c
                  Collection nums
              }
              [new Bar(x:'x', y:'y', c:Coin.HEAD, nums:[1,2]),
               new Bar('x', 'y', Coin.HEAD, [1,2])]
        """)

        assertEquals objects[0].hashCode(), objects[1].hashCode()
        assertEquals objects[0], objects[1]
        assertTrue objects[0].nums.class.name.contains("Unmodifiable")
    }

    void testImmutableAsMapKey() {
        assertScript """
            @Immutable final class HasString {
                String s
            }
            def k1 = new HasString('xyz')
            def k2 = new HasString('xyz')
            def map = [(k1):42]
            assert map[k2] == 42
        """
    }

    void testImmutableWithOnlyMap() {
        assertScript """
            @Immutable final class HasMap {
                Map map
            }
            new HasMap([:])
        """
    }

    void testImmutableWithHashMap() {
        assertScript """
            @Immutable final class HasHashMap {
                HashMap map = [d:4]
            }
            assert new HasHashMap([a:1]).map == [a:1]
            assert new HasHashMap(c:3).map == [c:3]
            assert new HasHashMap(map:[b:2]).map == [b:2]
            assert new HasHashMap(null).map == [d:4]
            assert new HasHashMap().map == [d:4]
            assert new HasHashMap([:]).map == [:]
            assert new HasHashMap(map:5, c:3).map == [map:5, c:3]
            assert new HasHashMap(map:null).map == null
            assert new HasHashMap(map:[:]).map == [:]
        """
    }
}