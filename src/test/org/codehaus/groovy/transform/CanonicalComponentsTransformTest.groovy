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

import groovy.test.GroovyShellTestCase
import groovy.transform.AutoClone
import groovy.transform.AutoExternalize
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor
import org.codehaus.groovy.control.MultipleCompilationErrorsException

import static groovy.transform.AutoCloneStyle.*
import groovy.transform.ToString
import groovy.transform.Canonical

class CanonicalComponentsTransformTest extends GroovyShellTestCase {

    void testTupleConstructorWithEnum() {
        assertScript """
            @groovy.transform.TupleConstructor
            enum Operator {
                PLUS('+'), MINUS('-')
                String symbol
            }
            assert Operator.PLUS.next() == Operator.MINUS
        """
    }

    void testHashCodeNullWrapperTypeCompileStatic_GROOVY7518() {
        assertScript """
            import groovy.transform.*

            @EqualsAndHashCode
            @CompileStatic
            class Person {
                Character someCharacter
                Integer someInteger
                Long someLong
                Float someFloat
                Double someDouble
            }

            assert new Person().hashCode()
        """
    }

    void testBooleanPropertyGROOVY6407() {
        assertScript """
            @groovy.transform.EqualsAndHashCode
            @groovy.transform.ToString
            class Demo {
                boolean myBooleanProperty

                boolean isMyBooleanProperty() {
                    false
                }

                static main(args) {
                    assert new Demo().hashCode() == 7590
                    assert new Demo(myBooleanProperty: true).toString() == 'Demo(false)'
                }
            }
        """
    }

    void testCloningWithFinalFields() {
        def p1 = new Person1('John', 'Smith')
        def p2 = p1.clone()
        def c1 = new Customer1('John', 'Smith', ['ipod', 'shiraz'], new Date())
        def c2 = c1.clone()

        assert [p1, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(p1.first instanceof Cloneable)
        assert p1 == p2
        assert !p1.is(p2)
        assert !c1.is(c2)
        assert c1 == c2
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        assert p1.first.is(p2.first)
        assert c1.first.is(c2.first)
    }

    void testCloningWithFinalFieldsCompileStatic() {
        def p1 = new Person1CS('John', 'Smith')
        def p2 = p1.clone()
        def c1 = new Customer1CS('John', 'Smith', ['ipod', 'shiraz'], new Date())
        def c2 = c1.clone()

        assert [p1, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(p1.first instanceof Cloneable)
        assert p1 == p2
        assert !p1.is(p2)
        assert !c1.is(c2)
        assert c1 == c2
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        assert p1.first.is(p2.first)
        assert c1.first.is(c2.first)
    }

    void testCloningWithNonFinalFields() {
        def p1 = new Person2(first:'John', last:'Smith')
        def p2 = p1.clone()
        def c1 = new Customer2(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date())
        def c2 = c1.clone()

        assert [p1, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(p1.first instanceof Cloneable)
        assert !p1.is(p2)
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        assert p1.first.is(p2.first)
        assert c1.first.is(c2.first)
    }

    void testCloningWithNonFinalFieldsCompileStatic() {
        def p1 = new Person2CS(first:'John', last:'Smith')
        def p2 = p1.clone()
        def c1 = new Customer2CS(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date())
        def c2 = c1.clone()

        assert [p1, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(p1.first instanceof Cloneable)
        assert !p1.is(p2)
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        assert p1.first.is(p2.first)
        assert c1.first.is(c2.first)
    }

    void testCloningWithSerializable() {
        def c1 = new Customer3(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date(), bonus:10)
        def c2 = c1.clone()

        assert [c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(c1.first instanceof Cloneable)
        assert c1 == c2
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        // serialization gives us a new string even here
        assert !c1.first.is(c2.first)
        assert c1.bonus == c2.bonus
    }

    void testCloningWithSerializableCompileStatic() {
        def c1 = new Customer3CS(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date(), bonus:10)
        def c2 = c1.clone()

        assert [c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(c1.first instanceof Cloneable)
        assert c1 == c2
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        // serialization gives us a new string even here
        assert !c1.first.is(c2.first)
        assert c1.bonus == c2.bonus
    }

    void testCloningWithExternalizable() {
        def c1 = new Customer4(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date(), bonus:10)
        def c2 = c1.clone()

        assert c1 instanceof Externalizable
        assert [c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(c1.first instanceof Cloneable)
        assert c1 == c2
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        // serialization gives us a new string even here
        assert !c1.first.is(c2.first)
        assert c1.bonus == c2.bonus
    }

    void testCloningWithExternalizableCompileStatic() {
        def c1 = new Customer4CS(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date(), bonus:10)
        def c2 = c1.clone()

        assert c1 instanceof Externalizable
        assert [c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
        assert !(c1.first instanceof Cloneable)
        assert c1 == c2
        assert !c1.is(c2)
        assert !c1.favItems.is(c2.favItems)
        assert !c1.since.is(c2.since)
        // serialization gives us a new string even here
        assert !c1.first.is(c2.first)
        assert c1.bonus == c2.bonus
    }

    void testCloningWithDefaultObjectClone() {
        def c1 = new Customer5(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], age:25)
        def c2 = c1.clone()
        assert c1.first == c2.first
        assert c1.last == c2.last
        assert c1.favItems == c2.favItems
        // no need to use includeFields=true here because of Object clone!
        assert c1.agePeek() == c2.agePeek()
    }

    void testCloningWithDefaultObjectCloneCompileStatic() {
        def c1 = new Customer5CS(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], age:25)
        def c2 = c1.clone()
        assert c1.first == c2.first
        assert c1.last == c2.last
        assert c1.favItems == c2.favItems
        // no need to use includeFields=true here because of Object clone!
        assert c1.agePeek() == c2.agePeek()
    }

    // GROOVY-4786
    void testExcludesWithEqualsAndHashCode() {
        def p1 = new PointIgnoreY(x:1, y:10)
        def p2 = new PointIgnoreY(x:1, y:100)
        assert p1 == p2
        assert p1.hashCode() == p2.hashCode()
    }

    // GROOVY-4894
    void testIncludesWithToString() {
        def p1 = new PointIgnoreY(x:1, y:5)
        def p2 = new PointIgnoreY(x:10, y:50)
        assert p1.toString() == "org.codehaus.groovy.transform.PointIgnoreY(1)"
        assert p2.toString() == "org.codehaus.groovy.transform.PointIgnoreY(10)"
    }

    // GROOVY-4894
    void testNestedExcludes() {
        def (s1, s2) = new GroovyShell().evaluate("""
            import groovy.transform.*
            @Canonical(excludes='foo, baz')
            @ToString(includeNames=true)
            class Hello {
              String foo = 'A', bar = 'B', baz = 'C'
            }
            @Canonical(excludes='foo, baz')
            @ToString(excludes='foo', includeNames=true)
            class Goodbye {
              String foo = 'A', bar = 'B', baz = 'C'
            }
            [new Hello().toString(), new Goodbye().toString()]
        """)
        assert s1 == 'Hello(bar:B)'
        assert s2 == 'Goodbye(bar:B, baz:C)'
    }

    // GROOVY-4844
    void testToStringCustomGetter() {
        def p1 = new Point(1, 2)
        def p2 = new Point(1, 1) { int getY() { 2 } }
        assert p1.toString() == 'org.codehaus.groovy.transform.Point(1, 2)'
        assert p2.toString() == 'org.codehaus.groovy.transform.Point(1, 2)'
    }

    // GROOVY-4849
    void testEqualsOnEquivalentClasses() {
        def (p1, p2, p3) = new GroovyShell().evaluate("""
        import groovy.transform.*
        @Canonical class IntPair {
            int x, y
        }

        @InheritConstructors
        class IntPairWithSum extends IntPair {
            def sum() { x + y }
        }

        [new IntPair(1, 2), new IntPair(1, 1) { int getY() { 2 } }, new IntPairWithSum(x:1, y:2)]
        """)

        assert p1 == p2 && p2 == p1
        assert p1 == p3 && p3 == p1
        assert p3 == p2 && p2 == p3
    }

    // GROOVY-4849
    void testEqualsOnDifferentClasses() {
        def (p1, p2, p3, t1) = new GroovyShell().evaluate("""
        import groovy.transform.*
        @Canonical class IntPair {
            int x, y
            boolean hasEqualXY(other) { other.x == getX() && other.y == getY() }
        }

        @InheritConstructors
        class IntPairWithSum extends IntPair {
            def sum() { x + y }
        }

        @EqualsAndHashCode
        @TupleConstructor(includeSuperProperties=true)
        class IntTriple extends IntPair { int z }

        [new IntPair(1, 2), new IntPair(1, 1) { int getY() { 2 } }, new IntPairWithSum(x:1, y:2), new IntTriple(1, 2, 3)]
        """)

        assert p1 != t1 && p2 != t1 && t1 != p3
        assert p1.hasEqualXY(t1) && t1.hasEqualXY(p1)
        assert p2.hasEqualXY(t1) && t1.hasEqualXY(p2)
        assert p3.hasEqualXY(t1) && t1.hasEqualXY(p3)
    }

    // GROOVY-4714
    void testCachingOfHashCode() {
        def (h1, h2, h3, h4) = new GroovyShell().evaluate("""
        import groovy.transform.*
        // DO NOT DO THIS AT HOME - cache should only be true for Immutable
        // objects but we cheat here for testing purposes
        @EqualsAndHashCode(cache = true) class ShouldBeImmutableIntPair {
            /* final */ int x, y
        }
        // the control (hashCode should change when x or y changes)
        @EqualsAndHashCode class MutableIntPair {
            int x, y
        }
        def sbmip = new ShouldBeImmutableIntPair(x: 3, y: 4)
        def h1 = sbmip.hashCode()
        sbmip.x = 5
        def h2 = sbmip.hashCode()
        def mip = new MutableIntPair(x: 3, y: 4)
        def h3 = mip.hashCode()
        mip.x = 5
        def h4 = mip.hashCode()
        [h1, h2, h3, h4]
        """)

        assert h1 == h2 // since it is cached
        assert h3 != h4 // no caching
    }

    // GROOVY-5928
    void testCachingOfToString() {
        def (ts1, ts2, ts3, ts4) = new GroovyShell().evaluate("""
        import groovy.transform.*
        // DO NOT DO THIS AT HOME - cache should only be true for Immutable
        // objects but we cheat here for testing purposes
        @ToString(cache = true) class ShouldBeImmutableIntPair {
            /* final */ int x, y
        }
        // the control (toString should change when x or y changes)
        @ToString class MutableIntPair {
            int x, y
        }
        def sbmip = new ShouldBeImmutableIntPair(x: 3, y: 4)
        def ts1 = sbmip.toString()
        sbmip.x = 5
        def ts2 = sbmip.toString()
        def mip = new MutableIntPair(x: 3, y: 4)
        def ts3 = mip.toString()
        mip.x = 5
        def ts4 = mip.toString()
        [ts1, ts2, ts3, ts4]
        """)

        assert ts1 == ts2 // since it is cached
        assert ts3 != ts4 // no caching
    }

    // GROOVY-6337
    void testCanonicalWithLazy() {
        def result = new GroovyShell().evaluate('''
            @groovy.transform.Canonical
            class Person {
              @Lazy first = missing()
              @Lazy last = 'Smith'
              int age
            }
            def p = new Person(21)
            // $first setter is an implementation detail
            p.$first = 'Mary'
            p.toString()
        ''')
        assert result == 'Person(21, Mary, Smith)'
    }

    // GROOVY-5901
    void testSimpleCloning() {
      def p1 = new Person6(first:'John', last:'Smith', since:new Date())
      def p2 = p1.clone()
      def c1 = new Customer6(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date())
      def c2 = c1.clone()

      assert [p1, p1.since, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
      assert !(p1.first instanceof Cloneable)
      assert !p1.is(p2)
      assert !c1.is(c2)
      assert !c1.favItems.is(c2.favItems)
      assert !p1.since.is(p2.since)
      assert !c1.since.is(c2.since)
      assert p1.first.is(p2.first)
      assert c1.first.is(c2.first)
    }

    void testSimpleCloningCompileStatic() {
      def p1 = new Person6CS(first:'John', last:'Smith', since:new Date())
      def p2 = p1.clone()
      def c1 = new Customer6CS(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date())
      def c2 = c1.clone()

      assert [p1, p1.since, c1, c1.favItems, c1.since].every{ it instanceof Cloneable }
      assert !(p1.first instanceof Cloneable)
      assert !p1.is(p2)
      assert !c1.is(c2)
      assert !c1.favItems.is(c2.favItems)
      assert !p1.since.is(p2.since)
      assert !c1.since.is(c2.since)
      assert p1.first.is(p2.first)
      assert c1.first.is(c2.first)
    }

    // GROOVY-4849
    void testCanEqualDefined() {
        def p1 = new IntPair(1, 2)
        def p2 = new IntPairNoCanEqual(x:1, y:2)
        assert p1 != p2
        assert p1.hashCode() == p2.hashCode()
        assert 'canEqual' in p1.class.methods*.name
        assert !('canEqual' in p2.class.methods*.name)
    }

    // GROOVY-5864
    void testExternalizeMethodsWithImmutable() {
        try {
            new GroovyShell().parse '''
                @groovy.transform.ExternalizeMethods
                @groovy.transform.Immutable
                class Person {
                    String first
                }
            '''
            fail('The compilation should have failed as the final field first (created via @Immutable) is being assigned to (via @ExternalizeMethods).')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("cannot modify final field 'first' outside of constructor")
        }
    }

    // GROOVY-5864
    void testExternalizeVerifierWithNonExternalizableClass() {
        try {
            new GroovyShell().parse '''
                @groovy.transform.ExternalizeVerifier
                class Person { }
            '''
            fail("The compilation should have failed as the class doesn't implement Externalizable")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("An Externalizable class must implement the Externalizable interface")
        }
    }

    // GROOVY-5864
    void testExternalizeVerifierWithFinalField() {
        try {
            new GroovyShell().parse '''
                @groovy.transform.ExternalizeVerifier
                class Person implements Externalizable {
                    final String first
                    void writeExternal(ObjectOutput out)throws IOException{ }
                    void readExternal(ObjectInput objectInput)throws IOException,ClassNotFoundException{ }
                }
            '''
            fail("The compilation should have failed as the final field first (can't be set inside readExternal).")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("The Externalizable property (or field) 'first' cannot be final")
        }
    }

    // GROOVY-5864
    void testAutoExternalizeWithoutNoArg() {
        try {
            new GroovyShell().parse '''
                @groovy.transform.AutoExternalize
                class Person {
                    Person(String first) {}
                    String first
                }
            '''
            fail("The compilation should have failed as there is no no-arg constructor.")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("An Externalizable class requires a no-arg constructor but none found")
        }
    }

    // GROOVY-5864
    void testExternalizeVerifierWithNonExternalizableField() {
        try {
            new GroovyShell().parse '''
                class Name {}

                @groovy.transform.ExternalizeVerifier(checkPropertyTypes=true)
                class Person implements Externalizable {
                    Name name
                    int age
                    void writeExternal(ObjectOutput out)throws IOException{ }
                    void readExternal(ObjectInput objectInput)throws IOException,ClassNotFoundException{ }
                }
            '''
            fail("The compilation should have failed as the type of Name isn't Externalizable or Serializable.")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("strict type checking is enabled and the non-primitive property (or field) 'name' in an Externalizable class has the type 'Name' which isn't Externalizable or Serializable")
        }
    }

    // GROOVY-5864
    void testAutoExternalizeHappyPath() {
        new GroovyShell().evaluate """
            import org.codehaus.groovy.transform.*
            def orig = new Person7(name: new Name7('John', 'Smith'), address: new Address7(street: 'somewhere lane', town: 'my town'), age: 21, verified: true)
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream{ os -> os.writeObject(orig) }
            def bais = new ByteArrayInputStream(baos.toByteArray())
            bais.withObjectInputStream { is -> assert is.readObject().toString() == 'Person7(Name7(John, Smith), Address7(somewhere lane, my town), 21, true)' }
        """
    }

    void testAutoExternalizeHappyPathCompileStatic() {
        new GroovyShell().evaluate """
            import org.codehaus.groovy.transform.*
            def orig = new Person7CS(name: new Name7CS('John', 'Smith'), address: new Address7CS(street: 'somewhere lane', town: 'my town'), age: 21, verified: true)
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream{ os -> os.writeObject(orig) }
            def bais = new ByteArrayInputStream(baos.toByteArray())
            bais.withObjectInputStream { is -> assert is.readObject().toString() == 'Person7CS(Name7CS(John, Smith), Address7CS(somewhere lane, my town), 21, true)' }
        """
    }

    void testAutoExternalizeNestedClassCompileStatic_GROOVY7644() {
        new GroovyShell().evaluate '''
            import org.codehaus.groovy.transform.*

            def orig = new Person7NestedAddressCS.Address7CS(street: 'somewhere lane', town: 'my town')
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream{ os -> os.writeObject(orig) }
            def bais = new ByteArrayInputStream(baos.toByteArray())
            bais.withObjectInputStream { is -> assert is.readObject().toString() == 'Person7NestedAddressCS$Address7CS(somewhere lane, my town)' }
        '''
    }

    // GROOVY-4570
    void testToStringForEnums() {
        assert Color.PURPLE.toString() == 'org.codehaus.groovy.transform.Color(r:255, g:0, b:255)'
    }

    void testCustomCopyConstructor_GROOVY7016() {
        new GroovyShell().evaluate """
            import org.codehaus.groovy.transform.Shopper
            def p1 = new Shopper('John', [['bread', 'milk'], ['bacon', 'eggs']])
            def p2 = p1.clone()
            p2.shoppingHistory[0][1] = 'jam'
            assert p1.shoppingHistory[0] == ['bread', 'milk']
            assert p2.shoppingHistory[0] == ['bread', 'jam']
        """
    }

    void testTupleConstructorNoDefaultParameterValues_GROOVY7427() {
        new GroovyShell().evaluate """
            // checks special Map behavior isn't added if defaults=false
            import groovy.transform.*

            @ToString
            @TupleConstructor(defaults=false)
            class Person {
              def name
            }

            assert new Person('John Smith').toString() == 'Person(John Smith)'
            assert Person.constructors.size() == 1
        """
    }

    void testNullCloneableField_GROOVY7091() {
        new GroovyShell().evaluate """
            import groovy.transform.AutoClone
            @AutoClone
            class B {
              String name='B'
            }

            @AutoClone
            class A {
              B b
              C c
              ArrayList x
              List y
              String name='A'
            }

            @AutoClone
            class C {
              String name='C'
            }

            def b = new B().clone()
            assert b
            assert new A(b:b).clone()
            assert new A().clone()
        """
    }

    void testTupleConstructorUsesSetters_GROOVY7087() {
        new GroovyShell().evaluate """
            import groovy.transform.*

            @ToString @TupleConstructor(useSetters=true)
            class Foo1 {
                String bar, baz
                void setBar(String bar) {
                    this.bar = bar?.toUpperCase()
                }
            }

            assert new Foo1('cat', 'dog').toString() == 'Foo1(CAT, dog)'
            // check the default map-style constructor too
            assert new Foo1(bar: 'cat', baz: 'dog').toString() == 'Foo1(CAT, dog)'
        """
    }

    void testincludeSuperFieldsAndroperties_GROOVY8013() {
        new GroovyShell().evaluate """
            import groovy.transform.*

            @ToString
            class Foo {
                String baz = 'baz'
                protected String baz2
            }

            @TupleConstructor(includes='a,b,baz2', includeSuperFields=true)
            @ToString(includes='a,c,super,baz,d', includeFields=true, includeSuperProperties=true, includeSuper=true)
            class Bar extends Foo {
                int a = 1
                int b = 2
                private int c = 3
                public int d = 4
            }

            assert new Bar().toString() == 'Bar(1, 3, Foo(baz), baz, 4)'
        """
    }

    void testOrderTupleParamsUsingIncludes_GROOVY8016() {
        new GroovyShell().evaluate """
            import groovy.transform.*

            @ToString
            class Foo {
                String a
                String c
            }
            @TupleConstructor(includes='d,c,b,a', includeSuperProperties=true, includeFields=true)
            @ToString(includes='super,b,d', includeFields=true, includeSuperProperties=true, includeSuper=true)
            class Bar extends Foo {
                String d
                private String b
            }

            assert new Bar('1', '2', '3', '4').toString() == 'Bar(Foo(4, 2), 3, 1)'
        """
    }

    void testTupleConstructorWithForceDirectBypassesSetters_GROOVY7087() {
        new GroovyShell().evaluate """
            import groovy.transform.*

            @ToString @TupleConstructor
            class Foo2 {
                String bar, baz
                void setBar(String bar) {
                    this.bar = bar.toUpperCase()
                }
            }

            assert new Foo2(bar: 'cat', baz: 'dog').toString() == 'Foo2(CAT, dog)'
            assert new Foo2('cat', 'dog').toString() == 'Foo2(cat, dog)'
        """
    }

    void testEqualsHashCodeToStringConsistencyWithExplicitBooleanGetters_GROOVY7417() {
        new GroovyShell().evaluate """
            import groovy.transform.*

            @ToString
            @EqualsAndHashCode
            class A {
                boolean x
            }

            def a1 = new A(x: true)
            def a2 = new A(x: true)
            def a3 = new A(x: false)
            assert a1.toString() == a2.toString()
            assert a1.hashCode() == a2.hashCode()
            assert a1 == a2
            assert a1.toString() != a3.toString()
            assert a1.hashCode() != a3.hashCode()
            assert a1 != a3

            @ToString
            @EqualsAndHashCode
            class B {
                boolean x
                boolean isX() { false }
            }

            def b1 = new B(x: true)
            def b2 = new B(x: false)
            assert b1.toString() == b2.toString()
            assert b1.hashCode() == b2.hashCode()
            assert b1 == b2

            @ToString
            @EqualsAndHashCode
            class C {
                boolean x
                boolean getX() { false }
            }

            def c1 = new C(x: true)
            def c2 = new C(x: false)
            assert c1.toString() == c2.toString()
            assert c1.hashCode() == c2.hashCode()
            assert c1 == c2

            @ToString
            @EqualsAndHashCode
            class D {
                boolean x
                boolean isX() { false }
                boolean getX() { false }
            }

            def d1 = new D(x: true)
            def d2 = new D(x: false)
            assert d1.toString() == d2.toString()
            assert d1.hashCode() == d2.hashCode()
            assert d1 == d2
        """
    }

    void testHashCodeForInstanceWithNullPropertyAndField() {
        new GroovyShell().evaluate """
            import groovy.transform.*
            @EqualsAndHashCode(includeFields = true)
            class FieldAndPropertyIncludedInHashCode {            
                private String field
                String property
            }
            assert new FieldAndPropertyIncludedInHashCode().hashCode() == 442087
        """
    }

    void testHashCodeForInstanceWithNullPropertyAndJavaBeanProperty() {
        new GroovyShell().evaluate '''
            import groovy.transform.*
            @EqualsAndHashCode(allProperties = true)
            class FieldAndPropertyIncludedInHashCode {            
                String property
                String getField() { null }
            }
            assert new FieldAndPropertyIncludedInHashCode().hashCode() == 442087
        '''
    }

    // GROOVY-9009
    void testAutoCloneToStringCompileStatic() {
        new GroovyShell().evaluate '''
            import groovy.transform.*

            @ToString
            @CompileStatic
            @AutoClone
            class SomeClass {
                String someId
            }

            assert new SomeClass(someId: 'myid').clone().toString() == 'SomeClass(myid)'
        '''
    }
}

@TupleConstructor
@AutoClone(style=COPY_CONSTRUCTOR)
@EqualsAndHashCode
class Person1 { final String first, last }

@TupleConstructor(includeSuperProperties=true, callSuper=true)
@AutoClone(style=COPY_CONSTRUCTOR)
@EqualsAndHashCode
class Customer1 extends Person1 { final List favItems; final Date since }

@TupleConstructor
@AutoClone(style=COPY_CONSTRUCTOR)
@EqualsAndHashCode
@CompileStatic
class Person1CS { final String first, last }

@CompileStatic
@TupleConstructor(includeSuperProperties=true, callSuper=true)
@AutoClone(style=COPY_CONSTRUCTOR)
@EqualsAndHashCode
class Customer1CS extends Person1CS { final List favItems; final Date since }

@AutoClone(style=COPY_CONSTRUCTOR)
class Person2 { String first, last }

@AutoClone(style=COPY_CONSTRUCTOR)
class Customer2 extends Person2 { List favItems = []; Date since }

@AutoClone(style=COPY_CONSTRUCTOR)
@CompileStatic
class Person2CS { String first, last }

@CompileStatic
@AutoClone(style=COPY_CONSTRUCTOR)
class Customer2CS extends Person2CS { List favItems = []; Date since }

@AutoClone(style=SERIALIZATION)
@EqualsAndHashCode
class Customer3 implements Serializable {
    String first, last
    List favItems
    Date since
    int bonus
}

@AutoClone(style=SERIALIZATION)
@CompileStatic
@EqualsAndHashCode
class Customer3CS implements Serializable {
    String first, last
    List favItems
    Date since
    int bonus
}

@AutoExternalize
@AutoClone(style=SERIALIZATION)
@EqualsAndHashCode
class Customer4 {
    String first, last
    List favItems
    Date since
    int bonus
}

@CompileStatic
@AutoExternalize
@AutoClone(style=SERIALIZATION)
@EqualsAndHashCode
class Customer4CS {
    String first, last
    List favItems
    Date since
    int bonus
}

@AutoClone
class Customer5 {
    String first, last
    List favItems
    private int age
    int agePeek() { age }
}

@CompileStatic
@AutoClone
class Customer5CS {
    String first, last
    List favItems
    private int age
    int agePeek() { age }
}

@TupleConstructor
@AutoClone(style=SIMPLE)
@EqualsAndHashCode
class Person6 { String first, last; Date since }

@TupleConstructor(includeSuperProperties=true, callSuper=true)
@AutoClone(style=SIMPLE)
@EqualsAndHashCode
class Customer6 extends Person6 { List<String> favItems }

@TupleConstructor
@AutoClone(style=SIMPLE)
@EqualsAndHashCode
@CompileStatic
class Person6CS { String first, last; Date since }

@CompileStatic
@TupleConstructor(includeSuperProperties=true, callSuper=true)
@AutoClone(style=SIMPLE)
@EqualsAndHashCode
class Customer6CS extends Person6CS { List<String> favItems }

// GROOVY-5864
@Canonical
@ToString(includePackage=false)
class Name7 implements Serializable { String first, last }

// GROOVY-5864
@AutoExternalize
@ToString(includePackage=false)
class Address7 { String street, town }

// GROOVY-5864
@ToString(includePackage=false)
@AutoExternalize(checkPropertyTypes=true)
class Person7 {
    Name7 name
    Address7 address
    int age
    Boolean verified
}

@ToString(includePackage=false)
@AutoExternalize(checkPropertyTypes=true)
@CompileStatic
class Person7CS {
    Name7CS name
    Address7CS address
    int age
    Boolean verified
}

@Canonical
@CompileStatic
@ToString(includePackage=false)
class Name7CS implements Serializable { String first, last }

@AutoExternalize
@ToString(includePackage=false)
@CompileStatic
class Address7CS { String street, town }

// GROOVY-7644
class Person7NestedAddressCS {
    @ToString(includePackage=false)
    @AutoExternalize(checkPropertyTypes=true)
    @CompileStatic
    static class Address7CS {
        String street, town
    }
}

// GROOVY-4786
@EqualsAndHashCode(excludes="y")
@ToString(includes="x")
class PointIgnoreY {
    int x
    int y // y coordinate excluded from Equals and hashCode
}

// GROOVY-4844
@TupleConstructor @ToString
class Point { int x, y }

// GROOVY-4849
@Canonical class IntPair {
    int x, y
}

// GROOVY-4849
@EqualsAndHashCode(useCanEqual=false)
class IntPairNoCanEqual {
    int x, y
}

// GROOVY-4570
@ToString(includeNames=true)
enum Color {
    BLACK(0,0,0), WHITE(255,255,255), PURPLE(255,0,255)
    int r, g, b
    Color(int r, g, b) { this.r = r; this.g = g; this.b = b }
}

@TupleConstructor(force=true) @AutoClone(style=COPY_CONSTRUCTOR)
class Shopper {
    final String name
    final List<List<String>> shoppingHistory
    Shopper(Shopper other) {
        name = other.name
        // requires deep clone
        shoppingHistory = other.shoppingHistory*.clone()
    }
}
