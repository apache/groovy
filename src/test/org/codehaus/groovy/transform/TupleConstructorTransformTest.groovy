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

import groovy.test.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code TupleConstructor} transform.
 */
final class TupleConstructorTransformTest {

    @Test
    void testBasics() {
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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

    // GROOVY-7522
    @Test
    void testExistingConstructorTakesPrecedence() {
        assertScript '''import groovy.transform.*
            @TupleConstructor
            class Cat {
                String name
                int age
                Cat(String name) {}
            }

            assert new Cat("Mr. Bigglesworth").name == null
            assert Cat.declaredConstructors.length == 1
        '''
    }

    // GROOVY-10790
    @Test @NotYetImplemented
    void testExistingConstructorTakesPrecedence2() {
        assertScript '''import groovy.transform.*
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
        def err = shouldFail '''import groovy.transform.*
            @TupleConstructor(includes='surName', excludes='surName')
            class Person {
                String surName
            }
        '''
        assert err.message.contains("Error during @TupleConstructor processing: Only one of 'includes' and 'excludes' should be supplied not both.")
    }

    @Test
    void testIncludesWithInvalidPropertyNameResultsInError() {
        def err = shouldFail '''import groovy.transform.*
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
        def err = shouldFail '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
    @Test @NotYetImplemented
    void testMultipleUsage2() {
        for (entry in [DUPLICATE:3, PREFER_COLLECTOR:1, PREFER_EXPLICIT:3, PREFER_EXPLICIT_MERGED:3]) {
            assertScript """import groovy.transform.*
                @TupleConstructor(defaults=false)
                @AnnotationCollector(mode=AnnotationCollectorMode.$entry.key)
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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

    // GROOVY-8455, GROOVY-8453
    @Test
    void testPropPsuedoPropAndFieldOrderIncludingInheritedMembers() {
        assertScript '''import groovy.transform.*
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

    // GROOVY-10790
    @Test @NotYetImplemented
    void testWithMapConstructor() {
        assertScript '''import groovy.transform.*
            @MapConstructor @TupleConstructor
            @ToString
            class Foo {
                String bar, baz = 'z'
            }

            assert new Foo('x','y').toString() == 'Foo(x, y)'
            assert new Foo('x').toString() == 'Foo(x, z)'
            assert new Foo().toString() == 'Foo(null, z)'
        '''
        // multiple sources of no-arg constructor
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
        assertScript '''import groovy.transform.*
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
