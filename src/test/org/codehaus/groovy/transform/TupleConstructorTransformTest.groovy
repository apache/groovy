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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code TupleConstructor} transform.
 */
final class TupleConstructorTransformTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports { star 'groovy.transform' }
    }

    @Test
    void testBasics() {
        assertScript shell, '''
            @TupleConstructor(defaults=false)
            class Person {
                String firstName, lastName
            }

            def p = new Person('John', 'Doe')
            assert p.firstName == 'John'
            assert p.lastName  == 'Doe'
        '''
    }

    @Test
    void testCopyConstructor() {
        assertScript shell, '''
            @TupleConstructor(force=true)
            class Person {
                String firstName, lastName
                Person(Person that) {
                    this.firstName = that.firstName
                }
            }

            def p = new Person('John', 'Doe')
            assert p.firstName == 'John'
            assert p.lastName  == 'Doe'

            p = new Person(p)
            assert p.firstName == 'John'
            assert p.lastName  == null
        '''
    }

    @Test
    void testFieldsAndInitializers() {
        assertScript shell, '''
            @TupleConstructor(includeFields=true)
            class Person {
                String firstName = 'John'
                private String lastName = 'Doe'
                String getLastName() { lastName }
            }

            def p = new Person()
            assert p.firstName == 'John'
            assert p.lastName  == 'Doe'

            p = new Person('Jane')
            assert p.firstName == 'Jane'
            assert p.lastName  == 'Doe'

            p = new Person('Jane', 'Eyre')
            assert p.firstName == 'Jane'
            assert p.lastName  == 'Eyre'
        '''
    }

    @Test
    void testFieldsAndNamesAndPost() {
        assertScript shell, '''
            @ToString(includeFields=true, includeNames=true)
            @TupleConstructor(post={ full = "$first $last" })
            class Person {
                final String first, last
                private final String full
            }

            assert new Person('Dierk', 'Koenig').toString() == 'Person(first:Dierk, last:Koenig, full:Dierk Koenig)'
        '''
    }

    @Test
    void testSuperPropsAndPreAndPost() {
        assertScript shell, '''
            @TupleConstructor
            class Person {
                String first, last
            }

            @CompileStatic // optional
            @ToString(includeSuperProperties=true)
            @TupleConstructor(includeSuperProperties=true, pre={ super(first, last?.toLowerCase()) }, post={ this.first = this.first?.toUpperCase() })
            class Author extends Person {
                String bookName
            }

            assert new Author('Dierk', 'Koenig', 'ReGinA').toString() == 'Author(ReGinA, DIERK, koenig)'
            assert new Author().toString() == 'Author(null, null, null)'
        '''
    }

    // GROOVY-7950
    @Test
    void testTraitPropsAndAllProperties() {
        assertScript shell, '''
            trait T {
                Number n
            }

            @TupleConstructor(allProperties=true, includes='s,n')
            class C implements T {
                String s
            }

            def obj = new C('answer',42)
            assert obj.s == 'answer'
            assert obj.n == 42
        '''
    }

    // GROOVY-8219
    @Test
    void testTraitPropsAndIncludeFields() {
        assertScript shell, '''
            trait T {
                Number n = 42
            }

            @TupleConstructor(includeFields=true)
            class C implements T {
                String s = ''
                public x
            }

            def obj = new C()
            assert obj.n == 42
            assert obj.s == ''
            assert obj.x == null
        '''
    }

    // GROOVY-7522
    @Test
    void testExistingConstructorTakesPrecedence() {
        assertScript shell, '''
            @TupleConstructor
            class Cat {
                String name
                int age
                Cat(String name) {}
            }

            assert new Cat("Mr. Bigglesworth").name == null
            assert Cat.declaredConstructors.length == 1
        '''
        assertScript shell, '''
            @TupleConstructor(force=true)
            class Cat {
                String name
                int age
                Cat(String name) {}
            }

            assert new Cat().name == null
            assert new Cat("Mr. Bigglesworth").name == null
            assert new Cat("Mr. Bigglesworth", 42).name == "Mr. Bigglesworth"
            assert Cat.declaredConstructors.length == 3 // (), (String) and (String,int)
        '''
    }

    @Test
    void testIncludesAndExcludesTogetherResultsInError() {
        def err = shouldFail shell, '''
            @TupleConstructor(includes='surName', excludes='surName')
            class Person {
                String surName
            }
        '''
        assert err.message.contains("Error during @TupleConstructor processing: Only one of 'includes' and 'excludes' should be supplied not both.")
    }

    @Test
    void testIncludesWithInvalidPropertyNameResultsInError() {
        def err = shouldFail shell, '''
            @TupleConstructor(includes='sirName')
            class Person {
                String firstName
                String surName
            }
        '''
        assert err.message.contains("Error during @TupleConstructor processing: 'includes' property 'sirName' does not exist.")
    }

    @Test
    void testExcludesWithInvalidPropertyNameResultsInError() {
        def err = shouldFail shell, '''
            @TupleConstructor(excludes='sirName')
            class Person {
                String firstName
                String surName
            }
        '''
        assert err.message.contains("Error during @TupleConstructor processing: 'excludes' property 'sirName' does not exist.")
    }

    // GROOVY-7523
    @Test
    void testIncludesWithEmptyList() {
        assertScript shell, '''
            @TupleConstructor(includes=[])
            class Cat {
                String name
                int age
            }

            assert Cat.declaredConstructors.size() == 1
        '''
    }

    // GROOVY-7524
    @Test
    void testWithInheritConstructors() {
        assertScript shell, '''
            @TupleConstructor
            class NameId {
                String name
                Integer id
            }

            @ToString(includeSuperProperties=true, ignoreNulls=true, includeNames=true)
            @TupleConstructor(force=true, defaults=false)
            @TupleConstructor(force=true, defaults=false, includeSuperProperties=true)
            @InheritConstructors
            class Cat extends NameId {
                Double age
            }

            assert new Cat("Felix").toString() == 'Cat(name:Felix)'
            assert new Cat("Felix", 42).toString() == 'Cat(name:Felix, id:42)'
            assert new Cat("Felix", 42, 3.5d).toString() == 'Cat(age:3.5, name:Felix, id:42)'
            assert new Cat(3.5d).toString() == 'Cat(age:3.5)'
            assert new Cat().toString() == 'Cat()'
        '''
    }

    // GROOVY-7672
    @Test
    void testMultipleUsages() {
        assertScript shell, '''
            import java.awt.Color

            class Named {
                String name
            }

            @ToString(includeSuperProperties=true, ignoreNulls=true, includeNames=true, includeFields=true)
            @TupleConstructor(force=true, defaults=false)
            @TupleConstructor(force=true, defaults=false, includeFields=true)
            @TupleConstructor(force=true, defaults=false, includeSuperProperties=true)
            class Cat extends Named {
                int age
                private Color color
                Cat() {}
            }

            assert new Cat("Felix", 3).toString() == 'Cat(age:3, name:Felix)'
            assert new Cat(3, Color.BLACK).toString() == 'Cat(age:3, color:java.awt.Color[r=0,g=0,b=0])'
            assert new Cat(3).toString() == 'Cat(age:3)'
            assert new Cat().toString() == 'Cat(age:0)'
            assert Cat.constructors.size() == 4
        '''
    }

    // GROOVY-10789
    @Test
    void testMultipleUsage2() {
        for (entry in [DUPLICATE:3, PREFER_COLLECTOR:1, PREFER_EXPLICIT:3, PREFER_EXPLICIT_MERGED:3]) {
            assertScript shell, """import static groovy.transform.AnnotationCollectorMode.*
                @TupleConstructor(defaults=false)
                @AnnotationCollector(mode=$entry.key)
                @interface Collector {
                }

                @Collector @TupleConstructor(defaults=true)
                class Foo {
                    String bar, baz
                }

                assert Foo.declaredConstructors.length == $entry.value
            """
        }
    }

    // GROOVY-6454
    @Test
    void testInternalFieldsAreIncludedIfRequested() {
        assertScript shell, '''
            @TupleConstructor(allNames=true)
            class HasInternalName {
                String $internal
            }

            assert new HasInternalName("foo").$internal == "foo"
        '''
    }

    // GROOVY-7981
    @Test
    void testVisibilityOptions() {
        assertScript shell, '''
            import static groovy.transform.options.Visibility.*
            import static java.lang.reflect.Modifier.isPrivate

            @VisibilityOptions(PRIVATE)
            @Immutable
            @ASTTest(phase=CANONICALIZATION, value={
                node.constructors.every { isPrivate(it.modifiers) }
            })
            class Person {
                String first, last
                int age
                static makePerson(Map args) {
                    new Person(args)
                }
            }

            @CompileStatic
            void test() {
                def p = Person.makePerson(first: 'John', last: 'Smith', age: 20)
                assert p.toString() == 'Person(John, Smith, 20)'
            }
            test()
        '''
    }

    // GROOVY-7981
    @Test
    void testMultipleVisibilityOptions() {
        assertScript shell, '''
            import static groovy.transform.options.Visibility.*
            import static java.lang.reflect.Modifier.*

            @VisibilityOptions(value=PROTECTED, id='first_only')
            @VisibilityOptions(constructor=PRIVATE, id='age_only')
            @TupleConstructor(visibilityId='first_only', includes='first', defaults=false, force=true)
            @TupleConstructor(visibilityId='age_only', includes='age', defaults=false, force=true)
            @ASTTest(phase=CANONICALIZATION, value={
                assert node.constructors.size() == 2
                node.constructors.each {
                    assert (it.typeDescriptor == 'void <init>(java.lang.String)' && isProtected(it.modifiers)) ||
                            (it.typeDescriptor == 'void <init>(int)' && isPrivate(it.modifiers))
                }
            })
            class Person {
                String first, last
                int age
                static test() {
                    assert new Person('John').first == 'John'
                    assert new Person(42).age == 42
                }
            }

            Person.test()
        '''
    }

    // GROOVY-8453, GROOVY-8455
    @Test
    void testPropPsuedoPropAndFieldOrderIncludingInheritedMembers() {
        assertScript shell, '''
            class Basepubf{}
            class Basep{}
            class Basepp{}
            class Base {
                Basep basep
                public Basepubf basepubf
                protected Byte baseProtField
                void setBasePseudoProp(Basepp bpp) {}
            }

            class Foopubf{}
            class Foop{}
            class Foopp{}
            class Foo extends Base {
                Foop foop
                public Foopubf foopubf
                protected Short fooProtField
                Foopp getFooPseudoProp() { null }
            }

            class Barpubf{}
            class Barp{}
            class Barpp{}
            class Bar extends Foo {
                Barp barp
                public Barpubf barpubf
                protected Integer barProtField
                void setBarPseudoProp(Barpp bpp) { }
            }

            class Bazpubf{}
            class Bazp{}
            class Bazpp{}
            @TupleConstructor(includeSuperProperties=true, includeFields=true, includeSuperFields=true, allProperties=true)
            class Baz extends Bar {
                Bazp bazp
                public Bazpubf bazpubf
                protected Long bazProtField
                void setBazPseudoProp(Bazpp bpp) { }
            }

            assert Baz.constructors.max{ it.parameterTypes.size() }.toString() ==
                'public Baz(Basep,Basepp,Basepubf,java.lang.Byte,Foop,Foopubf,java.lang.Short,Barp,Barpp,Barpubf,java.lang.Integer,Bazp,Bazpp,Bazpubf,java.lang.Long)'
        '''
    }

    // GROOVY-8207
    @Test
    void testDefaults() {
        assertScript shell, '''
            @TupleConstructor(defaults=false, includeFields=true)
            class C {
                protected int f
            }

            def obj = new C(42)
            assert obj.@f == 42
            assert C.declaredConstructors.length == 1
        '''
    }

    // GROOVY-10361
    @Test
    void testDefaultsMode() {
        assertScript shell, '''
            @TupleConstructor(defaultsMode=DefaultsMode.OFF, includeFields=true)
            class A {
                String won
                private int too
            }

            assert A.declaredConstructors.toString() == '[public A(java.lang.String,int)]'
        '''
        shouldFail shell, '''
            @TupleConstructor(defaultsMode=DefaultsMode.OFF, includeFields=true)
            class B {
                String won = 'one'
                private int too = 2
            }
        '''
        assertScript shell, '''
            @TupleConstructor(defaultsMode=DefaultsMode.AUTO, includeFields=true)
            class C {
                String one = 'won'
                int too = 2
                private int three
                private String four = 'for'
            }

            assert C.declaredConstructors*.toString().toSet() == [
                'public C(java.lang.String,int,int,java.lang.String)',
                'public C(java.lang.String,int,int)',
                'public C(java.lang.String,int)',
                'public C(int)'
            ].toSet()
        '''
        assertScript shell, '''
            @TupleConstructor(defaultsMode=DefaultsMode.ON, includeFields=true)
            class D {
                String one = 'won'
                int too = 2
                private int three
                private String four = 'for'
            }

            assert D.declaredConstructors*.toString().toSet() == [
                'public D(java.lang.String,int,int,java.lang.String)',
                'public D(java.lang.String,int,int)',
                'public D(java.lang.String,int)',
                'public D(java.lang.String)',
                'public D()'
            ].toSet()
        '''
        assertScript shell, '''
            @Canonical(defaultsMode=DefaultsMode.AUTO)
            class Bar {
                String a = 'a'
                long b
                Integer c = 24
                short d
                String e = 'e'
            }

            short one = 1
            assert new Bar(3L, one).toString() == 'Bar(a, 3, 24, 1, e)'
            assert new Bar('A', 3L, one).toString() == 'Bar(A, 3, 24, 1, e)'
            assert new Bar('A', 3L, 42, one).toString() == 'Bar(A, 3, 42, 1, e)'
            assert new Bar('A', 3L, 42, one, 'E').toString() == 'Bar(A, 3, 42, 1, E)'
        '''
    }

    // GROOVY-10919
    @Test
    void testNamedVariant() {
        assertScript shell, '''
            @TupleConstructor(includeFields=true, namedVariant=true)
            @ToString(includeFields=true, includeNames=true)
            class Foo {
                private final int x
                private       int y = 1
                private final int z
            }

            String string = new Foo(x:2, z:3)
            assert string == 'Foo(x:2, y:1, z:3)'
        '''
    }

    // GROOVY-10925
    @Test
    void testNamedVariant2() {
        def err = shouldFail shell, '''
            @TupleConstructor(includeFields=true, namedVariant=true)
            @ToString(includeNames=true, includeFields=true)
            class Foo {
                private final int x = 1
                private       int y = 2
                private final int z
            }

            String string = new Foo(x:3, y:3, z:3)
            assert string == 'Foo(x:3, y:3, z:3)'
        '''
        assert err =~ /Unrecognized namedArgKey: x/
    }

    // GROOVY-10790
    @Test
    void testWithMapConstructor() {
        assertScript shell, '''
            @MapConstructor @TupleConstructor
            @ToString
            class Foo {
                String bar, baz = 'z'
            }

            assert new Foo('x','y').toString() == 'Foo(x, y)'
            assert new Foo('x').toString() == 'Foo(x, z)'
            assert new Foo().toString() == 'Foo(null, z)'
        '''
        assertScript shell, ''' // multiple sources of no-arg constructor
            @MapConstructor(noArg=true) @TupleConstructor
            @ToString
            class Foo {
                String bar, baz = 'z'
            }

            assert new Foo('x','y').toString() == 'Foo(x, y)'
            assert new Foo('x').toString() == 'Foo(x, z)'
            assert new Foo().toString() == 'Foo(null, z)'
        '''
    }

    // GROOVY-10919
    @Test
    void testWithMapConstructor2() {
        assertScript shell, '''
            @MapConstructor(includeFields=true)
            @TupleConstructor(includeFields=true)
            @ToString(includeFields=true, includeNames=true)
            class Foo {
                private final int x
                private       int y = 1
                private final int z
            }

            String string = new Foo(x:2, z:3)
            assert string == 'Foo(x:2, y:1, z:3)'
        '''
    }

    // GROOVY-10925
    @Test
    void testWithMapConstructor3() {
        assertScript shell, '''
            @MapConstructor(includeFields=true)
            @TupleConstructor(includeFields=true)
            @ToString(includeFields=true, includeNames=true)
            class Foo {
                private final int x = 1
                private       int y = 2
                private final int z
            }

            String string = new Foo(x:3, y:3, z:3)
            assert string == 'Foo(x:3, y:3, z:3)'
        '''
    }
}
