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
package org.codehaus.groovy.transform

class CanonicalTransformTest extends GroovyShellTestCase {

    void testCanonical() {
        def objects = evaluate("""
            import groovy.transform.Canonical
            @Canonical class Foo {
                String x, y
            }
            [new Foo(x:'x', y:'y'),
             new Foo('x', 'y')]
        """)

        assertEquals objects[0].hashCode(), objects[1].hashCode()
        assertEquals objects[0], objects[1]
    }

    void testCanonicalWithDeclaredConstructor() {
        def msg = shouldFail(GroovyRuntimeException) {
            assertScript """
                @groovy.transform.Canonical class Foo {
                    def foo, bar, baz
                
                    Foo() {}
                
                    Foo(foo, bar) {
                        this.foo = foo
                        this.bar = bar
                    }
                }
            
                def foo = new Foo('a', 'b')
                def foo1 = new Foo()
                foo1.foo = 'a'
                foo1.bar = 'b'
                assert foo == foo1
            
                // Fail here
                new Foo('a', 'b', 'c')
            """
        }
        assert msg.contains('Could not find matching constructor')
    }

    void testCanonicalNotCopyOrCloneProperty() {
        assertScript """
            def date = new Date()
            def array = [1, 2, 3] as Integer[]
            def map = [foo: 'bar']
            def collection = [4, 5, 6]
            
            @groovy.transform.Canonical class Foo {
                Date date
                Integer[] array
                Map map
                Collection collection
            }
            
            def foo = new Foo(date, array, map, collection)
            
            assert date.is(foo.date)
            assert array.is(foo.array)
            assert map.is(foo.map)
            assert collection.is(foo.collection)
        """
    }

    void testCanonicalWithSuper() {
        // related to Apache PR#9: "Add includeSuper property to @Canonical annotation"
        assertScript """
            import groovy.transform.*
            @Canonical
            class Foo {
              int a
            }
            @Canonical(callSuper=true, includeSuperProperties=true, includeNames=true)
            class Bar extends Foo {
              int b
            }
            @Canonical(callSuper=true, includeSuper=true)
            @TupleConstructor(includeSuperProperties=true)
            class Baz extends Foo {
              int b
            }
            def (b1, b2, b3) = [new Bar(a:5, b:20), new Bar(10, 20), new Baz(15, 20)]
            assert [b1, b2, b3].toString() == '[Bar(b:20, a:5), Bar(b:20, a:10), Baz(20, Foo(15))]'
            assert b1 != b2
            assert b1.hashCode() != b2.hashCode()
        """
    }

    void testCanonicalChange() {
        def objects = evaluate("""
              @groovy.transform.Canonical class Foo {
                  String x, y
              }
              [new Foo(x:'x', y:'y'),
               new Foo('x', 'y')]
        """)
        objects[0].x = 'z'
        assertFalse objects[0] == objects[1]
        assertFalse objects[0].hashCode() == objects[1].hashCode()
    }

    void testUntyped() {
        def object = evaluate("""
              @groovy.transform.Canonical class Foo {
                  def x
                  def y = 10
              }
              new Foo()
        """)

        assert null == object.x
        assert 10 == object.y

        object.x = 20
        assert 20 == object.x
        object.x = [1, 2, 3]
        assert [1, 2, 3] == object.x
    }

    void testCanonicalRectangleFieldsNamesTupleToString() {
        assert 'Rectangle(w:10, h:20, x:5, y:7)' == evaluate("""
            import groovy.transform.*
            @Canonical
            @ToString(includeNames=true, includeFields=true)
            @TupleConstructor(includeFields=true)
            class Rectangle {
                int w, h
                private int x, y
            }
            new Rectangle(10, 20, 5, 7).toString()
        """)
    }

    void testCanonicalRectangleFieldsToString() {
        assert 'Rectangle(10, 20, 0, 0)' == evaluate("""
            import groovy.transform.*
            @Canonical
            @ToString(includeFields=true)
            class Rectangle {
                int w, h
                private int x, y
            }
            new Rectangle(10, 20).toString()
        """)
    }

    void testCanonicalDefaultToString() {
        assert 'Rectangle(10, 20)' == evaluate("""
            import groovy.transform.*
            @Canonical
            class Rectangle {
                int w, h
                private int x, y
            }
            new Rectangle(10, 20).toString()
        """)
    }

    void testCanonicalRectangleTupleNamesToString() {
        assert 'Rectangle(w:10, h:20)' == evaluate("""
            import groovy.transform.*
            @Canonical
            @ToString(includeNames=true)
            @TupleConstructor(includeFields=true)
            class Rectangle {
                int w, h
                private int x, y
            }
            new Rectangle(10, 20, 5, 7).toString()
        """)
    }

    void testCanonicalRectangleFieldsTupleToString() {
        assert 'Rectangle(10, 20, 5, 7)' == evaluate("""
            import groovy.transform.*
            @Canonical
            @ToString(includeFields=true)
            @TupleConstructor(includeFields=true)
            class Rectangle {
                int w, h
                private int x, y
            }
            new Rectangle(10, 20, 5, 7).toString()
        """)
    }

    void testCanonicalRectangleFieldsNamesToString() {
        assert 'Rectangle(w:10, h:20, x:0, y:0)' == evaluate("""
            import groovy.transform.*
            @Canonical
            @ToString(includeNames=true, includeFields=true)
            class Rectangle {
                int w, h
                private int x, y
            }
            new Rectangle(10, 20).toString()
        """)
    }

    void testMapConstructorOptional() {
        assertScript """
            @groovy.transform.Canonical class Foo {
                String bar
                String baz = 'a'
            }
            
            def foo = new Foo(bar: 'c')
            def foo1 = new Foo(baz: 'd')
            assert 'a' == foo.baz
            assert 'c' == foo.bar
            assert 'd' == foo1.baz
            assert null == foo1.bar
        """
    }

    void testMapConstructorOptionalPrimitive() {
        assertScript """
            @groovy.transform.Canonical class Foo {
                String a
                int b
                char c
                short d
                long e
                byte f
                double h
                float i
                boolean j
            }
            
            new Foo("foo")
            new Foo("foo", 10)
            new Foo("foo", 10, (char) 20)
            new Foo("foo", 10, (char) 20, (short) 30)
            new Foo("foo", 10, (char) 20, (short) 30, 40L)
            new Foo("foo", 10, (char) 20, (short) 30, 40L, (byte) 50)
            new Foo("foo", 10, (char) 20, (short) 30, 40L, (byte) 50, 0.0)
            new Foo("foo", 10, (char) 20, (short) 30, 40L, (byte) 50, 0.0, 0.0F)
        """
    }

    void testOrderedConstructorOptional() {
        assertScript """
            @groovy.transform.Canonical class Foo {
                String bar
                String baz = 'a'
            }
            
            def foo = new Foo()
            def foo0 = new Foo('c')
            def foo1 = new Foo('c', 'd')
            assert null == foo.bar
            assert 'a' == foo.baz
            assert 'c' == foo0.bar
            assert 'a' == foo0.baz
            assert 'c' == foo1.bar
            assert 'd' == foo1.baz
        """
    }

    void testCanonicalListProp() {
        def objects = evaluate("""
              @groovy.transform.Canonical class HasList {
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

        objects[0].nums = [1, 2, 3]
        objects[1].letters = 'D,E'.split(',')

        assert objects[0].nums.size() == 3
        assert objects[1].letters.size() == 2
        assertFalse objects[0] == objects[1]
        assert !(objects[0].hashCode() == objects[1].hashCode())
    }

    void testCanonicalChangeArray() {
        assertScript """
              @groovy.transform.Canonical class HasListAndMap {
                  Object[] foo
              }
              def object = new HasListAndMap()
              def object2 = new HasListAndMap(['bar'] as Object[])
              
              assert object != object2
              
              object.foo = new Object[1]
              object.foo[0] = 'bar'
              
              assert object == object2
        """
    }

    void testCanonicalChangeCollection() {
        def object = evaluate("""
              @groovy.transform.Canonical class HasListAndMap {
                  List nums
                  Map map
              }
              new HasListAndMap(nums:[], map:[:])
        """)

        object.nums << 1
        object.nums.add 2
        object.map.foo = "bat"
        assertEquals 2, object.nums.size()
        assertEquals 1, object.map.size()
        object.nums.remove 0
        assertEquals 1, object.nums.size()
    }

    void testCanonicalAsMapKey() {
        assertScript """
            @groovy.transform.Canonical final class HasString {
                String s
            }
            def k1 = new HasString('xyz')
            def k2 = new HasString('xyz')
            def map = [(k1):42]
            assert map[k2] == 42
        """
    }

    void testCanonicalWithOnlyMap() {
        assertScript """
            @groovy.transform.Canonical final class HasMap {
                Map map
            }
            def m = new HasMap([:])
            new HashMap()
        """
    }

    void testCanonicalWithInvalidPropertyName() {
        def msg = shouldFail(MissingPropertyException) {
            assertScript """
                @groovy.transform.Canonical class Simple { }
                new Simple(missing:'Name')
            """
        }
        assert msg.contains('No such property: missing for class: Simple')
    }

    void testCanonicalWithHashMap() {
        assertScript """
            @groovy.transform.Canonical final class HasHashMap {
                HashMap map = [d:4]
            }
            assert new HasHashMap([a:1]).map == [a:1]
            assert new HasHashMap(c:3).map == [c:3]
            assert new HasHashMap(null).map == null
            assert new HasHashMap().map == [d:4]
            assert new HasHashMap([:]).map == [:]
            assert new HasHashMap(map:5, c:3).map == [map:5, c:3]
            assert new HasHashMap(map:[:]).map == [map:[:]]
        """
    }

    void testCanonicalEquals() {
        assertScript """
            @groovy.transform.Canonical class This { String value }
            @groovy.transform.Canonical class That { String value }
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
            import groovy.transform.Canonical
            @Canonical class Foo {
                String value
            }
            @Canonical class Bar {
                String value
                String toString() { 'zzz' + _toString() }
            }
            @Canonical class Baz {
                String value
                String toString() { 'zzz' + _toString() }
                def _toString() { 'xxx' }
            }
            def foo = new Foo('abc')
            def foo0 = new Foo('abc')
            def foo1 = new Foo(value:'abc')
            def bar = new Bar('abc')
            def baz = new Baz('abc')
            assert bar.toString() == 'zzz' + foo.toString().replaceAll('Foo', 'Bar')
            assert baz.toString() == 'zzzxxx'

            assert 'Foo(abc)' == foo0.toString()
            foo0.value = 'cde'
            assert 'Foo(cde)' == foo0.toString()
            assert 'Foo(abc)' == foo1.toString()
            foo1.value = 'cde'
            assert 'Foo(cde)' == foo1.toString()
        """
    }

    void testExistingEquals() {
        assertScript """
            import groovy.transform.Canonical
            @Canonical class Foo {
                String value
            }
            @Canonical class Bar {
                String value
                // doesn't follow normal conventions - for testing only
                boolean equals(other) { value == 'abc' || _equals(other) }
            }
            @Canonical class Baz {
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
            import groovy.transform.Canonical
            @Canonical class Foo {
                String value
            }
            @Canonical class Bar {
                String value
                // doesn't follow normal conventions - for testing only
                int hashCode() { value == 'abc' ? -1 : _hashCode() }
            }
            @Canonical class Baz {
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

    void testEqualsCopesWithSelfReference() {
        assertScript """
            @groovy.transform.Canonical class Tree {
              Tree left, right
              Object item
            }

            def t = new Tree()
            t.left = t
            t.item = 4
            def s = new Tree()
            s.left = s
            s.item = 4
            assert s.equals(t)
            // not smart enough to handle mutual-recursion yet
            // don't use this annotation in such a scenario
            //
            // t.right = s
            // s.right = t
            // assert s.equals(t) // <= StackOverflowError
        """
    }

    void testHashCodeCopesWithSelfReference() {
        assertScript """
            @groovy.transform.Canonical class Tree {
                Object item
                Tree left, right
            }

            def t = new Tree(4)
            t.left = t
            t.right = t
            assert t.hashCode() == 7497
            // not smart enough to handle mutual-recursion yet
            // don't use this annotation in such a scenario
            //
            // def s = new Tree(5, t)
            // t.left = s
            // println t.hashCode() // <= StackOverflowError
        """
    }

    void testMapStyleConstructorSupportWithObjectOrMapFirstProperty() {
        // GROOVY-5243: special support for Map, Object, AbstractMap, HashMap but currently not LinkedHashMap
        assertScript """
            import groovy.transform.*

            def obj1 = new Canonical1(foo: [:], bar: 'BAR')
            def obj2 = new Canonical2(foo: [:], bar: 'BAR')
            def obj3 = new Canonical3(foo: [:], bar: 'BAR')

            check(obj1)
            check(obj2)
            check(obj3)

            def check(obj) {
              assert obj.foo == [:]
              assert obj.bar == 'BAR'
            }

            @Canonical
            class Canonical1 {
                def foo
                String bar
            }
            @Canonical
            class Canonical2 {
                Map foo
                String bar
            }
            @Canonical
            class Canonical3 {
                HashMap foo
                String bar
            }
        """
    }
}
