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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code @Immutable} transform.
 */
final class ImmutableTransformTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports { star 'groovy.transform' }
    }

    @Test
    void testImmutable() {
        def objects = shell.evaluate '''
            enum Coin { HEAD, TAIL }
            @Immutable class Bar {
                String x, y
                Coin c
                Collection nums
            }
            [new Bar(x:'x', y:'y', c:Coin.HEAD, nums:[1,2]), new Bar('x', 'y', Coin.HEAD, [1,2])]
        '''
        assert objects[0].hashCode() == objects[1].hashCode()
        assert objects[0] == objects[1]
        assert objects[0].nums.class.name.contains('Unmodifiable')
    }

    @Test
    void testImmutableClonesListAndCollectionFields() {
        def objects = shell.evaluate '''
            def myNums = [1, 2]
            @Immutable class Bar {
                List nums
                Collection otherNums
            }
            def myBar = new Bar(nums:myNums, otherNums:myNums)
            myNums << 3
            [myNums, myBar]
        '''
        assert objects[0] !== objects[1].nums
        assert objects[0] !== objects[1].otherNums
        assert objects[1].nums !== objects[1].otherNums
        assert objects[0].size() == 3
        assert objects[1].nums.size() == 2
        assert objects[1].otherNums.size() == 2
        assert objects[1].nums.class.name.contains('Unmodifiable')
        assert objects[1].otherNums.class.name.contains('Unmodifiable')
    }

    @Test
    void testImmutableField() {
        def person = shell.evaluate '''
            @Immutable class Person {
                boolean married
            }
            new Person(married:false)
        '''
        shouldFail(ReadOnlyPropertyException) {
            person.married = true
        }
    }

    @Test
    void testCloneableField() {
        def (originalDolly, lab) = shell.evaluate '''
            @AutoClone
            class Dolly implements Cloneable {
                String name
            }

            @Immutable class Lab {
                String name
                Cloneable clone
            }

            def dolly = new Dolly(name: "The Sheep")
            [dolly, new Lab(name: "Area 51", clone: dolly)]
        '''
        def clonedDolly = lab.clone
        def clonedDolly2 = lab.clone
        assert lab.name == 'Area 51'
        assert !originalDolly.is(clonedDolly)
        assert originalDolly.name == clonedDolly.name
        assert !clonedDolly2.is(clonedDolly)
        assert clonedDolly2.name == clonedDolly.name
    }

    @Test
    void testCloneableFieldNotCloneableObject() {
        shouldFail shell, CloneNotSupportedException, '''
            class Dolly {
                String name
            }

            @Immutable class Lab {
                String name
                Cloneable clone
            }

            def dolly = new Dolly(name: "The Sheep")
            [dolly, new Lab(name: "Area 51", clone: dolly)]
        '''
    }

    @Test
    void testImmutableArrayProp() {
        def objects = shell.evaluate '''
            @Immutable class HasArray {
                String[] letters
            }
            def letters = 'A,B,C'.split(',')
            [new HasArray(letters:letters), new HasArray(letters)]
        '''
        assert objects[0].hashCode() == objects[1].hashCode()
        assert objects[0].equals( objects[1] )
        assert objects[0].letters.size() == 3
    }

    @Test
    void testImmutableListProp() {
        def objects = shell.evaluate '''
            @Immutable class HasList {
                List numbers
            }
            def numbers = [1,2,3]
            [new HasList(numbers:numbers), new HasList(numbers)]
        '''
        assert objects[0].hashCode() == objects[1].hashCode()
        assert objects[0].equals( objects[1] )
        assert objects[0].numbers.size() == 3
    }

    @Test
    void testImmutableAsMapKey() {
        assertScript shell, '''
            @Immutable final class HasString {
                String s
            }
            def k1 = new HasString('xyz')
            def k2 = new HasString('xyz')
            def map = [(k1):42]
            assert map[k2] == 42
        '''
    }

    @Test
    void testImmutableWithOnlyMap() {
        assertScript shell, '''
            @Immutable final class HasMap {
                Map map
            }
            new HasMap([:])
        '''
    }

    @Test
    void testImmutableWithPrivateStaticFinalField() {
        assertScript shell, '''
            @Immutable class Foo {
                private static final String BAR = 'baz'
            }
            assert new Foo().BAR == 'baz'
        '''
    }

    @Test
    void testImmutableWithInvalidPropertyName() {
        def err = shouldFail shell, MissingPropertyException, '''
            @Immutable class Simple {}
            new Simple(missing:'Name')
        '''
        assert err =~ 'No such property: missing for class: Simple'
    }

    @Test
    void testImmutableWithHashMap() {
        assertScript shell, '''
            @Immutable(propertyHandler=groovy.transform.options.LegacyHashMapPropertyHandler, noArg=false)
            class HasHashMap {
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
        '''
    }

    @Test
    void testImmutableEquals() {
        assertScript shell, """
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

    @Test
    void testExistingToString() {
        assertScript shell, '''
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
        '''
    }

    @Test
    void testExistingEquals() {
        assertScript shell, '''
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
        '''
    }

    @Test
    void testExistingHashCode() {
        assertScript shell, '''
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
        '''
    }

    @Test
    void testBuiltinImmutables() {
        assertScript shell, '''
            import java.awt.Color

            @Immutable class Person {
                UUID id
                String name
                Date dob
                Color favColor
                Class helperType
            }

            def p = new Person(id: UUID.randomUUID(), name: 'Fred', dob: new Date(), favColor: Color.GREEN, helperType: StringBuffer)
            def propClasses = [p.id, p.name, p.dob, p.favColor, p.helperType]*.class.name
            assert propClasses == ['java.util.UUID', 'java.lang.String', 'java.util.Date', 'java.awt.Color', 'java.lang.Class']
        '''
    }

    // GROOVY-4363
    @Test
    void testPrivateFieldAssignedViaConstructor() {
        assertScript shell, '''
            @Immutable(includeStatic = true)
            class Numbers {
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
            assert [1..4, 'a'..'c'].combinations().collect{ num, let -> n1."$let$num" } == [1, 2, 3, 4, 1, -2, 3, -4, 1, 2, 3, 4]
        '''
    }

    // GROOVY-4363
    @Test
    void testPrivateFinalFieldAssignedViaConstructorShouldCauseError() {
        shouldFail shell, ReadOnlyPropertyException, '''
            @Immutable class Numbers {
                private final int b2 = -2
            }
            def n1 = new Numbers(b2:2)
        '''
    }

    @Test
    void testImmutableWithImmutableFields() {
        assertScript shell, '''
            @Immutable class Bar { Integer i }
            @Immutable class Foo { Bar b }
            def fb = new Foo(new Bar(3))
            assert fb.toString() == 'Foo(Bar(3))'
        '''
    }

    @Test
    void testImmutableWithConstant() {
        assertScript shell, '''
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

    @Test
    void testStaticsAllowed_ThoughUsuallyBadDesign() {
        // design here is questionable as getDescription() method is not idempotent
        assertScript shell, '''
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

    @Test
    void testImmutableToStringVariants() {
        assertScript shell, '''
            @Immutable
            class Person1 { String first, last }

            @Immutable
            @ToString(includeNames=true)
            class Person2 { String first, last }

            @Immutable
            @ToString(excludes="last")
            class Person3 { String first, last }

            assert new Person1("Hamlet", "D'Arcy").toString() == "Person1(Hamlet, D'Arcy)"
            assert new Person2(first: "Hamlet", last: "D'Arcy").toString() == "Person2(first:Hamlet, last:D'Arcy)"
            assert new Person3("Hamlet", "D'Arcy").toString() == "Person3(Hamlet)"
        '''
    }

    @Test // GROOVY-4997
    void testImmutableUsageOnInnerClasses() {
        assertScript shell, '''
            class A4997 {
                @Immutable
                static class B4997 { String name }
                @Immutable
                class C4997 { String name }
                def test() {
                    assert new C4997(name: 'foo').toString() == 'A4997$C4997(foo)'
                }
            }
            assert new A4997.B4997(name: 'bar').toString() == 'A4997$B4997(bar)'
            new A4997().test()
        '''
    }

    @Test
    void testKnownImmutableClassesWithNamedParameters() {
        assertScript shell, '''
            @Immutable(knownImmutableClasses = [Address])
            class Person {
                String first, last
                Address address
            }

            @TupleConstructor @ToString class Address { final String street }

            assert new Person(first: 'John', last: 'Doe', address: new Address('Some Street')).toString() == 'Person(John, Doe, Address(Some Street))'
        '''
    }

    @Test
    void testKnownImmutableClassesWithExplicitConstructor() {
        assertScript shell, '''
            @Immutable(knownImmutableClasses = [Address])
            class Person {
                String first, last
                Address address
            }

            // ok, not really immutable but deem it such for the purpose of this test
            @groovy.transform.ToString class Address { String street }

            assert new Person('John', 'Doe', new Address(street: 'Street')).toString() == 'Person(John, Doe, Address(Street))'
        '''
    }

    @Test
    void testKnownImmutableClassesWithCoercedConstruction() {
        assertScript shell, '''
            @Immutable(knownImmutableClasses = [Address])
            class Person {
                String first, last
                Address address
            }

            // ok, not really immutable but deem it such for the purpose of this test
            @groovy.transform.Canonical class Address { String street }

            assert new Person(first: 'John', last: 'Doe', address: ['Street']).toString() == 'Person(John, Doe, Address(Street))'
        '''
    }

    @Test
    void testKnownImmutableClassesMissing() {
        def err = shouldFail shell, RuntimeException, '''
            @ToString class Address { String street }

            @Immutable
            class Person {
                String first, last
                Address address
            }

            new Person(first: 'John', last: 'Doe', address: new Address(street: 'Street'))
        '''
        assert err.message.contains("Unsupported type (Address) found for field 'address' while constructing immutable class Person")
        assert err.message.contains("Immutable classes only support properties with effectively immutable types")
    }

    // GROOVY-5828
    @Test
    void testKnownImmutableCollectionClass() {
        assertScript shell, '''
            @Immutable
            class ItemsControl { List list }
            def itemsControl = new ItemsControl(['Fee', 'Fi', 'Fo', 'Fum'])
            assert itemsControl.list.class.name.contains('Unmodifiable')

            // ok, Items not really immutable but pretend so for the purpose of this test
            @Immutable(knownImmutableClasses = [List])
            class Items { List list }
            def items = new Items(['Fee', 'Fi', 'Fo', 'Fum'])
            assert !items.list.class.name.contains('Unmodifiable')
        '''
    }

    // GROOVY-5828
    @Test
    void testKnownImmutables() {
        assertScript shell, '''
            // ok, Items not really immutable but pretend so for the purpose of this test
            @Immutable(knownImmutables = ['list1'])
            class Items {
                List list1
                List list2
            }
            def items = new Items(['Fee', 'Fi'], ['Fo', 'Fum'])
            assert !items.list1.class.name.contains('Unmodifiable')
            assert items.list2.class.name.contains('Unmodifiable')
        '''
    }

    // GROOVY-5449
    @Test
    void testShouldNotThrowNPE() {
        def err = shouldFail shell, RuntimeException, '''
            @Immutable
            class Person {
                def name
            }
        '''
        assert err.message.contains("Unsupported type (java.lang.Object or def) found for field 'name' while ")
    }

    // GROOVY-6192
    @Test
    void testWithEqualsAndHashCodeASTOverride() {
        assertScript shell, '''
            @Immutable
            @EqualsAndHashCode(includes = ['id'])
            class B {
                String id
                String name
            }

            assert new B('1', 'foo').equals(new B('1', 'foo2'))
        '''
    }

    // GROOVY-6354
    @Test
    void testCopyWith() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith = true)
            |class Person {
            |    String first, last
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( first:'tim', last:'yates' )
        assert tim.first == 'tim'

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( first:'tim' )
        assert tim.is( tim2 )

        // This should also be the same instance and no changes
        def tim3 = tim.copyWith( first:'tim', whatever:true )
        assert tim.is( tim3 )

        // As should this
        def tim4 = tim.copyWith( whatever:true )
        assert tim.is( tim4 )

        // And this
        def tim5 = tim.copyWith()
        assert tim.is( tim5 )

        // This should be a new instance with a new firstname
        def alice = tim.copyWith( first:'alice' )
        assert tim != alice
        assert alice.first == 'alice'
        assert !alice.is( tim )
    }

    @Test
    void testGenericsCopyWith() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith = true)
            |class Person {
            |    List<String> names
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( [ 'Tim', 'Yates' ] )
        assert tim.names == [ 'Tim', 'Yates' ]

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( names:[ 'Tim', 'Yates' ] )
        assert tim.is( tim2 )

        // This should be a new instance
        def alice = tim.copyWith( names:[ 'Alice', 'Yates' ] )
        assert tim != alice
        assert alice.names == [ 'Alice', 'Yates' ]
        assert !alice.is( tim )
    }

    @Test
    void testWithPrivatesCopyWith() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith=true)
            |class Foo {
            |  String first
            |  String last
            |  private String cache
            |  List<String> nicknames
            |  def full() {
            |    if (!cache) cache = "$first $last (${nicknames.join(', ')})"
            |    cache
            |  }
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( 'Tim', 'Yates', [ 'tim', 'nick1' ] )
        assert tim.full() == 'Tim Yates (tim, nick1)'

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( nicknames:[ 'tim', 'nick1' ] )
        assert tim.is( tim2 )

        // This should be a new instance
        def alice = tim.copyWith( first:'Alice', nicknames:[ 'ali' ] )
        assert tim != alice
        assert !alice.is( tim )
        assert alice.full() == 'Alice Yates (ali)'
    }

    @Test
    void testStaticWithPrivatesCopyWith() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith=true)
            |@groovy.transform.CompileStatic
            |class Foo {
            |  String first
            |  String last
            |  private String cache
            |  List<String> nicknames
            |  def full() {
            |    if (!cache) cache = "$first $last (${nicknames.join(', ')})"
            |    cache
            |  }
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( 'Tim', 'Yates', [ 'tim', 'nick1' ] )
        assert tim.full() == 'Tim Yates (tim, nick1)'

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( nicknames:[ 'tim', 'nick1' ] )
        assert tim.is( tim2 )

        // This should be a new instance
        def alice = tim.copyWith( first:'Alice', nicknames:[ 'ali' ] )
        assert tim != alice
        assert !alice.is( tim )
        assert alice.full() == 'Alice Yates (ali)'
    }

    @Test
    void testTypedWithPrivatesCopyWith() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith=true)
            |@groovy.transform.TypeChecked
            |class Foo {
            |  String first
            |  String last
            |  private String cache
            |  List<String> nicknames
            |  def full() {
            |    if (!cache) cache = "$first $last (${nicknames.join(', ')})"
            |    cache
            |  }
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( 'Tim', 'Yates', [ 'tim', 'nick1' ] )
        assert tim.full() == 'Tim Yates (tim, nick1)'

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( nicknames:[ 'tim', 'nick1' ] )
        assert tim.is( tim2 )

        // This should be a new instance
        def alice = tim.copyWith( first:'Alice', nicknames:[ 'ali' ] )
        assert tim != alice
        assert !alice.is( tim )
        assert alice.full() == 'Alice Yates (ali)'
    }

    @Test
    void testStaticCopyWith() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.CompileStatic
            |class Person {
            |    String first, last
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( first:'tim', last:'yates' )
        assert tim.first == 'tim'

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( first:'tim' )
        assert tim.is( tim2 )

        // This should also be the same instance and no changes
        def tim3 = tim.copyWith( first:'tim', whatever:true )
        assert tim.is( tim3 )

        // As should this
        def tim4 = tim.copyWith( whatever:true )
        assert tim.is( tim4 )

        // And this
        def tim5 = tim.copyWith()
        assert tim.is( tim5 )

        // This should be a new instance with a new firstname
        def alice = tim.copyWith( first:'alice' )
        assert tim != alice
        assert alice.first == 'alice'
        assert !alice.is( tim )
    }

    @Test
    void testTypedCopyWith() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.TypeChecked
            |class Person {
            |    String first, last
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( first:'tim', last:'yates' )
        assert tim.first == 'tim'

        // This should be the same instance and no changes
        def tim2 = tim.copyWith( first:'tim' )
        assert tim.is( tim2 )

        // This should also be the same instance and no changes
        def tim3 = tim.copyWith( first:'tim', whatever:true )
        assert tim.is( tim3 )

        // As should this
        def tim4 = tim.copyWith( whatever:true )
        assert tim.is( tim4 )

        // And this
        def tim5 = tim.copyWith()
        assert tim.is( tim5 )

        // This should be a new instance with a new firstname
        def alice = tim.copyWith( first:'alice' )
        assert tim != alice
        assert alice.first == 'alice'
        assert !alice.is( tim )
    }

    @Test
    void testCopyWithSkipping() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith = true)
            |class Person {
            |    String firstname, lastname
            |    List<Person> copyWith( i ) {
            |        (1..i).collect { this }
            |    }
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( firstname:'tim', lastname:'yates' )
        assert tim.firstname == 'tim'

        // Check original copyWith remains
        def result = tim.copyWith( 2 )
        assert result.size() == 2
        assert result.firstname == [ 'tim', 'tim' ]
    }

    @Test
    void testStaticCopyWithSkipping() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.CompileStatic
            |class Person {
            |    String firstname, lastname
            |    List<Person> copyWith( i ) {
            |        (1..i).collect { this }
            |    }
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( firstname:'tim', lastname:'yates' )
        assert tim.firstname == 'tim'

        // Check original copyWith remains
        def result = tim.copyWith( 2 )
        assert result.size() == 2
        assert result.firstname == [ 'tim', 'tim' ]
    }

    @Test
    void testTypedCopyWithSkipping() {
        def tester = new GroovyClassLoader().parseClass('''\
            |@groovy.transform.Immutable(copyWith = true)
            |@groovy.transform.TypeChecked
            |class Person {
            |    String firstname, lastname
            |    List<Person> copyWith( i ) {
            |        (1..i).collect { this }
            |    }
            |}
            |'''.stripMargin() )

        // One instance
        def tim = tester.newInstance( firstname:'tim', lastname:'yates' )
        assert tim.firstname == 'tim'

        // Check original copyWith remains
        def result = tim.copyWith( 2 )
        assert result.size() == 2
        assert result.firstname == [ 'tim', 'tim' ]
    }

    // GROOVY-6293
    @Test
    void testDefaultValuesAreImmutable() {
        assertScript shell, '''
            @Immutable class Y { Collection c = []; int foo = 1 }
            def y = new Y(foo: 3)
            assert y.c.class.name.contains('Unmodifiable')
            assert y.c == []
            assert y.foo == 3
        '''
    }

    // GROOVY-6473
    @Test
    void testNoArgConstructor() {
        assertScript shell, '''
            @Immutable class Y { Collection c = []; int foo = 1 }
            def y = new Y()
            assert y.c.class.name.contains('Unmodifiable')
            assert y.c == []
            assert y.foo == 1
        '''
    }

    // GROOVY-7227
    @Test
    void testKnownImmutablesWithInvalidPropertyNameResultsInError() {
        def err = shouldFail shell, '''
            @Immutable(knownImmutables=['sirName'])
            class Person {
                String surName
            }
            new Person(surName: "Doe")
        '''
        assert err.message.contains("Error during immutable class processing: 'knownImmutables' property 'sirName' does not exist.")
    }

    // GROOVY-7162
    @Test
    void testImmutableWithSuperClass() {
        assertScript shell, '''
            @EqualsAndHashCode
            class Person {
                String name
            }

            @Immutable
            @TupleConstructor(includeSuperProperties=true)
            @EqualsAndHashCode(callSuper=true)
            @ToString(includeNames=true, includeSuperProperties=true)
            class Athlete extends Person {
                String sport
            }

            def d1 = new Athlete('Michael Jordan', 'BasketBall')
            def d2 = new Athlete(name: 'Roger Federer', sport: 'Tennis')
            assert d1 != d2
            assert d1.toString() == 'Athlete(sport:BasketBall, name:Michael Jordan)'
            assert d2.toString() == 'Athlete(sport:Tennis, name:Roger Federer)'
        '''
    }

    // GROOVY-7599
    @Test
    void testImmutableWithJSR310() {
        assertScript shell, '''
            import java.time.*

            @Immutable
            class Person {
              String first, last
              LocalDate born
            }

            def mmm = new Person('Fred', 'Brooks', LocalDate.of(1931, Month.APRIL, 19))
            assert mmm.toString() == 'Person(Fred, Brooks, 1931-04-19)'
        '''
    }

    // GROOVY-7600
    @Test
    void testImmutableWithOptional() {
        assertScript shell, '''
            @Immutable class Person {
                String name
                Optional<String> address
            }
            def p = new Person('Joe', Optional.of('Home'))
            assert p.toString() == 'Person(Joe, Optional[Home])'
            assert p.address.get() == 'Home'
        '''
        shouldFail shell, MultipleCompilationErrorsException, '''
            @Immutable class Person {
                String name
                Optional<Date> address
            }
        '''
    }

    // GROOVY-8416
    @Test
    void testMapFriendlyNamedArgs() {
        assertScript shell, '''
            @Immutable
            class Point {
                int x, y
            }
            def coordinates = [x: 1, y: 2]
            assert coordinates instanceof LinkedHashMap
            def p1 = new Point(coordinates)
            assert p1.x == 1
            def p2 = new Point(new HashMap(coordinates))
            assert p2.x == 1
            def p3 = new Point(new TreeMap(coordinates))
            assert p3.x == 1
        '''
    }

    // GROOVY-8967
    @Test
    void testPropertiesWithDefaultValues() {
        assertScript shell, '''
            @Immutable
            class Thing {
                int i = 42
                Date d = new Date()
                Collection c = [42]
                String value = "default"
            }

            def thing = new Thing()
            assert thing.with{ [i, c, d.class, value] } == [42, [42], Date, 'default']
            thing = new Thing(c: null, d: null, value: null, i: -1)
            assert thing.with{ [i, c, d, value] } == [-1, null, null, null]
        '''
    }

    // GROOVY-10790
    @Test
    void testDefaultsTrueExtraConstructors() {
        assertScript shell, '''
            @Immutable(defaults=true, noArg=false)
            class Foo {
                String bar, baz = 'z'
            }
            assert new Foo('x','y').toString() == 'Foo(x, y)'
            assert new Foo('x').toString() == 'Foo(x, z)'
            assert new Foo().toString() == 'Foo(null, z)'
        '''
        assertScript shell, '''
            @Immutable(defaults=true) // MapConstructor also creates no-arg ctor
            class Foo {
                String bar, baz = 'z'
            }
            assert new Foo('x','y').toString() == 'Foo(x, y)'
            assert new Foo('x').toString() == 'Foo(x, z)'
            assert new Foo().toString() == 'Foo(null, z)'
        '''
    }

    // GROOVY-10108
    @Test
    void testSpecialNamedArgHandlingForMap() {
        assertScript shell, '''
            @Immutable(specialNamedArgHandling=false)
            class Type {
                Map<Long, String> data
            }

            Map<Long, String> data = [(1L):'foo', (2L):'bar']
            def obj= new Type(data)

            assert obj['data'][2L] == 'bar'
        '''
    }

    // GROOVY-11329
    @Test
    void testAllPropertiesAndTrait() {
        assertScript shell, '''
            import groovy.transform.Immutable

            trait T {
                String foo
            }

            @Immutable(allProperties=true)
            class C implements T {
                String bar
            }

            assert new C('bar', 'foo').toString() == 'C(bar, foo)'
        '''
    }
}
