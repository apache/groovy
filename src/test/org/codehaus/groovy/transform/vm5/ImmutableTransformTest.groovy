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
              @Immutable final class Foo {
                  String x, y
                  Coin c
                  Collection nums
              }
              [new Foo(x:'x', y:'y', c:Coin.HEAD, nums:[1,2]),
               new Foo('x', 'y', Coin.HEAD, [1,2])]
        """)

        assertEquals objects[0].hashCode(), objects[1].hashCode()
        assertEquals objects[0], objects[1]
        assertTrue objects[0].nums.class.name.contains("Unmodifiable")
    }

    void testImmutableWithOnlyMap() {
        assertScript """
            @Immutable final class Foo {
              Map bar
            }
            new Foo([:])
        """
    }

    void testImmutableWithHashMap() {
        assertScript """
            @Immutable final class Foo {
                HashMap bar = [d:4]
            }
            assert new Foo([a:1]).bar == [a:1]
            assert new Foo(c:3).bar == [c:3]
            assert new Foo(bar:[b:2]).bar == [b:2]
            assert new Foo(null).bar == [d:4]
            assert new Foo().bar == [d:4]
            assert new Foo([:]).bar == [:]
            assert new Foo(bar:5, c:3).bar == [bar:5, c:3]
            assert new Foo(bar:null).bar == null
            assert new Foo(bar:[:]).bar == [:]
        """
    }
}