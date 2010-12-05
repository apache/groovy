/*
 * Copyright 2008-2010 the original author or authors.
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
package org.codehaus.groovy.transform

/**
 * @author Paul King
 */
class ImmutableTransformTest extends GroovyShellTestCase {

    void testImmutable() {
        def objects = evaluate("""
            import groovy.transform.Immutable
            enum Coin { HEAD, TAIL }
            @Immutable class Bar {
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

    void testImmutableLegacy() {
        def objects = evaluate("""
            // note: uses legacy groovy.lang.Immutable
            enum Coin { HEAD, TAIL }
            @Immutable class Bar {
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

    void testImmutableField() {
        def person = evaluate("""
            import groovy.transform.Immutable
            @Immutable class Person {
                boolean married
            }
            new Person(married:false)
        """)
        shouldFail(ReadOnlyPropertyException) {
            person.married = true
        }
    }

    void testImmutableFieldLegacy() {
        def person = evaluate("""
            // note: uses legacy groovy.lang.Immutable
            @Immutable class Person {
                boolean married
            }
            new Person(married:false)
        """)
        shouldFail(ReadOnlyPropertyException) {
            person.married = true
        }
    }

    void testImmutableCantAlsoBeMutable() {
        def msg = shouldFail(RuntimeException) {
            assertScript """
                import groovy.transform.*
                @Immutable
                @Canonical
                class Foo {
                    String bar
                }
            """
        }
        assert msg.contains("@Canonical class 'Foo' can't also be @Immutable")
    }

    void testImmutableListProp() {
        def objects = evaluate("""
            import groovy.transform.Immutable
            @Immutable class HasList {
                String[] letters
                List nums
            }
            def letters = 'A,B,C'.split(',')
            def nums = [1, 2]
            [new HasList(letters:letters, nums:nums),
             new HasList(letters, nums)]
        """)

        assertEquals objects[0].hashCode(), objects[1].hashCode()
        assertEquals objects[0], objects[1]
        assert objects[0].letters.size() == 3
        assert objects[0].nums.size() == 2
    }

    void testImmutableAsMapKey() {
        assertScript """
            import groovy.transform.Immutable
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
            import groovy.transform.Immutable
            @Immutable final class HasMap {
                Map map
            }
            new HasMap([:])
        """
    }

    void testImmutableWithPrivateStaticFinalField() {
        assertScript """
          @groovy.transform.Immutable class Foo {
              private static final String BAR = 'baz'
          }
          assert new Foo().BAR == 'baz'
      """
    }

    void testImmutableWithInvalidPropertyName() {
        def msg = shouldFail(MissingPropertyException) {
            assertScript """
                import groovy.transform.Immutable
                @Immutable class Simple { }
                new Simple(missing:'Name')
            """
        }
        assert msg.contains('No such property: missing for class: Simple')
    }

    void testImmutableWithHashMap() {
        assertScript """
            import groovy.transform.Immutable
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

    void testImmutableEquals() {
        assertScript """
            import groovy.transform.Immutable
            @Immutable class This { String value }
            @Immutable class That { String value }
            class Other { }

            assert new This('foo') == new This("foo")
            assert new This('f${"o"}o') == new This("foo")

            assert new This('foo') != new This("bar")
            assert new This('foo') != new That("foo")
            assert new This('foo') != new Other()
            assert new Other() != new This("foo")
        """
    }

    void testExistingToString() {
        assertScript """
            import groovy.transform.Immutable
            @Immutable class Foo {
                String value
            }
            @Immutable class Bar {
                String value
                String toString() { 'zzz' + _toString() }
            }
            @Immutable class Baz {
                String value
                String toString() { 'zzz' + _toString() }
                def _toString() { 'xxx' }
            }
            def foo = new Foo('abc')
            def bar = new Bar('abc')
            def baz = new Baz('abc')
            assert bar.toString() == 'zzz' + foo.toString().replaceAll('Foo', 'Bar')
            assert baz.toString() == 'zzzxxx'
        """
    }

    void testExistingEquals() {
        assertScript """
            import groovy.transform.Immutable
            @Immutable class Foo {
                String value
            }
            @Immutable class Bar {
                String value
                // doesn't follow normal conventions - for testing only
                boolean equals(other) { value == 'abc' || _equals(other) }
            }
            @Immutable class Baz {
                String value
                // doesn't follow normal conventions - for testing only
                boolean equals(Baz other) { value == 'abc' || _equals(other) }
                def _equals(other) { false }
            }
            def foo1 = new Foo('abc')
            def foo2 = new Foo('abc')
            def foo3 = new Foo('def')
            assert foo1 == foo2
            assert foo1 != foo3

            def bar1 = new Bar('abc')
            def bar2 = new Bar('abc')
            def bar3 = new Bar('def')
            def bar4 = new Bar('def')
            assert bar1 == bar2
            assert bar1 == bar3
            assert bar3 != bar1

            def baz1 = new Baz('abc')
            def baz2 = new Baz('abc')
            def baz3 = new Baz('def')
            def baz4 = new Baz('def')
            assert baz1 == baz2
            assert baz1 == baz3
            assert baz3 != baz1
            assert baz3 != baz4
        """
    }

    void testExistingHashCode() {
        assertScript """
            import groovy.transform.Immutable
            @Immutable class Foo {
                String value
            }
            @Immutable class Bar {
                String value
                // doesn't follow normal conventions - for testing only
                int hashCode() { value == 'abc' ? -1 : _hashCode() }
            }
            @Immutable class Baz {
                String value
                // doesn't follow normal conventions - for testing only
                int hashCode() { value == 'abc' ? -1 : _hashCode() }
                def _hashCode() { -100 }
            }
            def foo1 = new Foo('abc')
            def foo2 = new Foo('abc')
            assert foo1.hashCode() == foo2.hashCode()

            def bar1 = new Bar('abc')
            def bar2 = new Bar('def')
            def bar3 = new Bar('def')
            assert bar1.hashCode() == -1
            assert bar2.hashCode() == bar3.hashCode()

            def baz1 = new Baz('abc')
            def baz2 = new Baz('def')
            assert baz1.hashCode() == -1
            assert baz2.hashCode() == -100
        """
    }

    void testPrivateFieldAssignedViaConstructor() {
        assertScript '''
            @Immutable class Numbers {
                private int a1 = 1
                private int b1 = -1
                private int c1
                private final int a2 = 2
                private final int b2 = -2
                private final int c2
                private static int a3 = 3
                private static int b3 = -3
                private static int c3
                private static final int a4 = 4
                private static final int b4 = -4
                private static final int c4 = 4
            }
            def n1 = new Numbers(b1:1, b3:3, c1:1, c2:2, c3:3)
            assert [1..4, 'a'..'c'].combinations().collect{ num, let -> n1."$let$num" } ==
                       [1, 2, 3, 4, 1, -2, 3, -4, 1, 2, 3, 4]
        '''
    }

    void testPrivateFinalFieldAssignedViaConstructorShouldCauseError() {
        shouldFail(ReadOnlyPropertyException) {
            evaluate '''
                @Immutable class Numbers {
                    private final int b2 = -2
                }
                def n1 = new Numbers(b2:2)
            '''
        }
    }

    void testImmutableWithImmutableFields() {
        assertScript '''
            import groovy.transform.Immutable
            @Immutable class Bar { Integer i }
            @Immutable class Foo { Bar b }
            def fb = new Foo(new Bar(3))
            assert fb.toString() == 'Foo(Bar(3))'
        '''
    }

    void testImmutableWithConstant() {
        assertScript '''
            import groovy.transform.Immutable
            @Immutable class MinIntegerHolder {
                Integer i
                public static final MIN = 3
                Integer getMinI() { [i, MIN].max() }
            }
            def mih2 = new MinIntegerHolder(2)
            def mih4 = new MinIntegerHolder(4)
            assert mih2.minI == 3
            assert mih4.minI == 4
        '''
    }

    void testStaticsAllowed_ThoughUsuallyBadDesign() {
        // design here is questionable as getDescription() method is not idempotent
        assertScript '''
            import groovy.transform.Immutable
            @Immutable class Person {
               String first, last
               static species = 'Human'
               String getFullname() {
                 "$first $last"
               }
               String getDescription() {
                 "$fullname is a $species"
               }
            }

            def spock = new Person('Leonard', 'Nimoy')
            assert spock.species == 'Human'
            assert spock.fullname == 'Leonard Nimoy'
            assert spock.description == 'Leonard Nimoy is a Human'

            spock.species = 'Romulan'
            assert spock.species == 'Romulan'

            Person.species = 'Vulcan'
            assert spock.species == 'Vulcan'
            assert spock.fullname == 'Leonard Nimoy'
            assert spock.description == 'Leonard Nimoy is a Vulcan'
        '''
    }
}
