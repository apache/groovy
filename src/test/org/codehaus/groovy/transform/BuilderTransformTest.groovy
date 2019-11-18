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

import gls.CompilableTestSupport

/**
 * Tests for {@code @Builder} transform.
 */
class BuilderTransformTest extends CompilableTestSupport {

    void testSimpleBuilder() {
        assertScript """
            import groovy.transform.builder.*

            @Builder(builderStrategy=SimpleStrategy)
            class Person {
                String firstName
                String lastName
                int age
            }
            def person = new Person().setFirstName("Robert").setLastName("Lewandowski").setAge(21)
            assert person.firstName == "Robert"
            assert person.lastName == "Lewandowski"
            assert person.age == 21

            def methods = Person.methods.findAll{ it.name.startsWith('set') && it.name.endsWith('e') }
            assert methods*.name.toSet() == ['setLastName', 'setAge', 'setFirstName'] as Set
            assert methods.every{ it.getAnnotation(groovy.transform.Generated) }
         """
    }

    void testSimpleBuilderInvalidUseOfForClass() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*
            @Builder(builderStrategy=SimpleStrategy, forClass=String)
            class Person { }
        """
        assert message.contains("Annotation attribute 'forClass' not supported")
    }

    void testSimpleBuilderInvalidUseOfBuilderClassName() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*
            @Builder(builderStrategy=SimpleStrategy, builderClassName='Creator')
            class Person { }
        """
        assert message.contains("Annotation attribute 'builderClassName' not supported")
    }

    void testSimpleBuilderInvalidUseOfBuilderMethodName() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*
            @Builder(builderStrategy=SimpleStrategy, builderMethodName='creator')
            class Person { }
        """
        assert message.contains("Annotation attribute 'builderMethodName' not supported")
    }

    void testSimpleBuilderInvalidUseOfBuildMethodName() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*
            @Builder(builderStrategy=SimpleStrategy, buildMethodName='create')
            class Person { }
        """
        assert message.contains("Annotation attribute 'buildMethodName' not supported")
    }

    void testSimpleBuilderInvalidUseOfIncludeSuperProperties() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*
            @Builder(builderStrategy=SimpleStrategy, includeSuperProperties=true)
            class Person { }
        """
        assert message.contains("Annotation attribute 'includeSuperProperties' not supported")
    }

    void testSimpleBuilderCustomPrefix() {
        assertScript """
            import groovy.transform.builder.*

            @Builder(builderStrategy=SimpleStrategy, prefix="")
            class Person {
                String firstName
                String lastName
                int age
            }
            def person = new Person()
            person.firstName("Robert").lastName("Lewandowski")
            person.setAge(21) // normal setters remain but can't be chained
            assert person.firstName == "Robert"
            assert person.lastName == "Lewandowski"
            assert person.age == 21
        """
    }

    void testSimpleBuilderSetters() {
        assertScript """
            import groovy.transform.builder.*
            import groovy.transform.*

            @TupleConstructor(useSetters=true)
            @Builder(builderStrategy=SimpleStrategy, prefix="", useSetters=true)
            class Person {
                String name
                void setName(String name) { this.name = name?.toLowerCase() }
                Integer age
                void setAge(Integer age) { this.age = (age ?: 0) * 2 }
            }

            def person = new Person()
            person.name("John").age(21)
            assert person.name == "john"
            assert person.age == 42

            def p2 = new Person(name: 'Mary', age: 5)
            assert p2.name == "mary"
            assert p2.age == 10

            def p3 = new Person(name: 'TOM')
            p3.age = 15
            assert p3.name == "tom"
            assert p3.age == 30
        """
    }

    void testSimpleBuilderWithCanonicalAndExcludes() {
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.Canonical

            @Canonical(excludes='age')
            @Builder(builderStrategy=SimpleStrategy)
            class Person {
                String firstName
                String lastName
                int age
            }
            def p = new Person().setFirstName("Robert").setLastName("Lewandowski")
            p.age = 21 // non-chained version should still be there
            assert "$p.firstName $p.lastName $p.age" == 'Robert Lewandowski 21'
            // chained method
            assert Person.getMethod("setFirstName", String).returnType.name == 'Person'
            // normal Groovy non-chained version
            assert Person.getMethod("setAge", Integer.TYPE).returnType.name == 'void'
        '''
    }

    void testDefaultBuilder() {
        def shell = new GroovyShell()
        shell.parse """
            import groovy.transform.builder.Builder

            @Builder
            class Person {
                String firstName
                String lastName
                int age
            }
        """
        shell.evaluate """
            def builder = new Person.PersonBuilder()
            def person = builder.firstName("Robert").lastName("Lewandowski").age(21).build()
            assert person.firstName == "Robert"
            assert person.lastName == "Lewandowski"
            assert person.age == 21
        """
    }

    void testDefaultBuilderUsingBuilderMethod() {
        assertScript """
            import groovy.transform.builder.Builder

            @Builder
            class Person {
                String firstName
                String lastName
                int age
            }

            def builder = Person.builder()
            def person = builder.firstName("Robert").lastName("Lewandowski").age(21).build()
            assert person.firstName == "Robert"
            assert person.lastName == "Lewandowski"
            assert person.age == 21

            def methods = Person.builder().getClass().methods.findAll{ it.name in ['firstName', 'lastName', 'age'] }
            assert methods.every{ it.getAnnotation(groovy.transform.Generated) }
        """
    }

    void testDefaultBuilderGenerics() {
        assertScript """
            import groovy.transform.builder.Builder

            @Builder
            class CookBook {
                List<String> recipes
            }

            def c = CookBook.builder().recipes(['Eggs Benedict', 'Poached Salmon']).build()
            assert c.recipes == ['Eggs Benedict', 'Poached Salmon']
        """
        def message = shouldNotCompile '''
            import groovy.transform.builder.Builder
            import groovy.transform.CompileStatic

            @Builder
            class CookBook {
                List<String> recipes
            }


            @CompileStatic
            def methodBadParams() {
                CookBook.builder().recipes([35, 42]).build()
            }
        '''
        assert message =~ /.*Cannot call.*recipes.*java.util.List\s?<java.lang.String>.*with arguments.*java.util.List\s?<java.lang.Integer>.*/
    }

    void testInitializerGenerics() {
        assertScript """
            import groovy.transform.builder.*

            @Builder(builderStrategy=InitializerStrategy)
            class CookBook {
                List<String> recipes
            }

            def c = new CookBook(CookBook.createInitializer().recipes(['Eggs Benedict', 'Poached Salmon']))
            assert c.recipes == ['Eggs Benedict', 'Poached Salmon']
        """
        def message = shouldNotCompile '''
            import groovy.transform.builder.*

            @Builder(builderStrategy=InitializerStrategy)
            class CookBook {
                List<String> recipes
            }


            @groovy.transform.CompileStatic
            def methodBadParams() {
                new CookBook(CookBook.createInitializer().recipes([35, 42]))
            }
        '''
        assert message =~ /.*Cannot call.*recipes.*java.util.List\s?<java.lang.String>.*with arguments.*java.util.List\s?<java.lang.Integer>.*/
    }

    void testDefaultBuilderCustomNames() {
        def shell = new GroovyShell()
        shell.parse """
            import groovy.transform.builder.Builder

            @Builder(builderClassName="Foo", buildMethodName="create")
            class Person {
                String firstName
                String lastName
                int age
            }
        """
        shell.evaluate """
            def builder = new Person.Foo()
            def person = builder.firstName("Robert").lastName("Lewandowski").age(21).create()
            assert person.firstName == "Robert"
            assert person.lastName == "Lewandowski"
            assert person.age == 21
        """
    }

    void testDefaultBuilderUsingCanonical() {
        assertScript '''
            import groovy.transform.builder.Builder
            import groovy.transform.Canonical

            // explicit excludes overrides excludes from @Canonical
            @Builder(buildMethodName='make', builderMethodName='maker', prefix='with', excludes='age')
            @Canonical(includes='firstName,age')
            class Person {
                String firstName
                String lastName
                int age
            }

            def p = Person.maker().withFirstName("Robert").withLastName("Lewandowski").make()
            assert "$p.firstName $p.lastName" == "Robert Lewandowski"
        '''
    }

    void testDefaultBuilderInvalidIncludeWithMethodAnnotation() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*

            class NameUtil {
                @Builder(includes='first')
                static String join(String first, String last) {
                    first + ' ' + last
                }
            }
        """

        assert message.contains("includes/excludes only allowed on classes")
    }

    void testDefaultBuilderIncludeSuperProperties() {
        assertScript """
            import groovy.transform.builder.*
            import groovy.transform.*

            @Builder
            class Mamal {
                int age
            }
            @Builder(includeSuperProperties=true)
            class Person extends Mamal {
                String firstName
                String lastName
            }

            @CompileStatic
            def parentBuilder() {
                def builder = Person.builder()
                Person person = builder.age(21).firstName("Robert").lastName("Lewandowski").build()
                assert person.firstName == "Robert"
                assert person.lastName == "Lewandowski"
                assert person.age == 21
            }
            parentBuilder()
         """
    }

    void testExternalBuilder() {
        assertScript """
            import groovy.transform.builder.*

            class Person {
                String firstName
                String lastName
            }

            @Builder(builderStrategy=ExternalStrategy, forClass = Person)
            class PersonBuilder { }

            def person = new PersonBuilder().firstName("Robert").lastName("Lewandowski").build()
            assert person.firstName == "Robert"
            assert person.lastName == "Lewandowski"

            def methods = PersonBuilder.methods.findAll{ it.name in ['firstName', 'lastName', 'age'] }
            assert methods.every{ it.getAnnotation(groovy.transform.Generated) }
        """
    }

    void testExternalBuilderInvalidUseOfBuilderClassName() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*
            @Builder(builderStrategy=ExternalStrategy, forClass=String, builderClassName='Creator')
            class DummyStringBuilder { }
        """
        assert message.contains("Annotation attribute 'builderClassName' not supported")
    }

    void testExternalBuilderInvalidUseOfBuilderMethodName() {
        def message = shouldNotCompile """
            import groovy.transform.builder.*
            @Builder(builderStrategy=ExternalStrategy, forClass=String, builderMethodName='creator')
            class DummyStringBuilder { }
        """
        assert message.contains("Annotation attribute 'builderMethodName' not supported")
    }

    void testExternalBuilderCustomPrefix() {
        assertScript """
            import groovy.transform.builder.*

            class Person {
                String firstName
                String lastName
            }

            @Builder(builderStrategy=ExternalStrategy, forClass = Person, prefix = 'set')
            class PersonBuilder1 { }
            @Builder(builderStrategy=ExternalStrategy, forClass = Person, prefix = 'with')
            class PersonBuilder2 { }

            def p1 = new PersonBuilder1().setFirstName("Robert").setLastName("Lewandowski").build()
            p1.with { assert firstName == "Robert" && lastName == "Lewandowski" }
            def p2 = new PersonBuilder2().withFirstName("Robert").withLastName("Lewandowski").build()
            p2.with { assert firstName == "Robert" && lastName == "Lewandowski" }
        """
    }

    void testExternalBuilderWithIncludeAndCustomMethodName() {
        assertScript """
            import groovy.transform.builder.*
            import groovy.transform.Canonical

            @Canonical
            class Person {
                String firstName
                String lastName
            }

            @Builder(builderStrategy=ExternalStrategy, forClass = Person, includes = ['firstName'], buildMethodName="create")
            class PersonBuilder { }

            def personBuilder = new PersonBuilder()
            def person = personBuilder.firstName("Robert").create()
            assert person.firstName == "Robert"
            assert personBuilder.metaClass.methods.find { it.name == "lastName" } == null
            assert personBuilder.metaClass.methods.find { it.name == "firstName" } != null
        """
    }

    void testExternalBuilderWithExclude() {
        assertScript """
            import groovy.transform.builder.*

            class Person {
                String firstName
                String lastName
            }

            @Builder(builderStrategy=ExternalStrategy, forClass = Person, excludes = ['lastName'])
            class PersonBuilder { }

            def personBuilder = new PersonBuilder()
            def person = personBuilder.firstName("Robert").build()
            assert person.firstName == "Robert"
            assert personBuilder.metaClass.methods.find { it.name == "lastName" } == null
            assert personBuilder.metaClass.methods.find { it.name == "firstName" } != null
        """
    }

    void testExternalBuilderWithCanonicalAndExcludes() {
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.Canonical
            import static groovy.test.GroovyAssert.shouldFail

            @Canonical(excludes='born')
            class Person {
                String first
                String last
                int born
            }

            @Builder(builderStrategy=ExternalStrategy, forClass=Person, buildMethodName='create', prefix='with')
            class PersonBuilder { }

            def p = new PersonBuilder().withFirst('Johnny').withLast('Depp').create()
            assert "$p.first $p.last" == 'Johnny Depp'
            assert PersonBuilder.getMethod("withFirst", String).returnType.name == 'PersonBuilder'
            shouldFail(NoSuchMethodException) {
                PersonBuilder.getMethod("withBorn", Integer.TYPE)
            }
        '''
    }

    void testExternalBuilderIncludeSuperProperties() {
        assertScript """
            import groovy.transform.builder.*
            import groovy.transform.*

            class Mamal {
                int age
            }
            class Person extends Mamal {
                String firstName
                String lastName
            }

            @Builder(builderStrategy=ExternalStrategy, forClass=Person, includeSuperProperties=true)
            class PersonBuilder { }

            @CompileStatic
            def parentBuilder() {
                Person person = new PersonBuilder().age(21).firstName("Robert").lastName("Lewandowski").build()
                assert person.firstName == "Robert"
                assert person.lastName == "Lewandowski"
                assert person.age == 21
            }
            parentBuilder()
        """
    }

    void testInitializerStrategy() {
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.*

            @ToString
            @Builder(builderStrategy=InitializerStrategy)
            class Person {
                String firstName
                String lastName
                int age
            }

            @CompileStatic
            def firstLastAge() {
                assert new Person(Person.createInitializer().firstName("John").lastName("Smith").age(21)).toString() == 'Person(John, Smith, 21)'
            }
            // static case
            firstLastAge()
            // dynamic case
            assert new Person(Person.createInitializer().firstName("John").lastName("Smith").age(21)).toString() == 'Person(John, Smith, 21)'

            def methods = Person.createInitializer().getClass().methods.findAll{ it.name in ['firstName', 'lastName', 'age'] }
            assert methods.every{ it.getAnnotation(groovy.transform.Generated) }
        '''
        def message = shouldNotCompile '''
            import groovy.transform.builder.*
            import groovy.transform.CompileStatic

            @Builder(builderStrategy=InitializerStrategy)
            class Person {
                String firstName
                String lastName
                int age
            }

            @CompileStatic
            def firstLastButNoAge() {
                new Person(Person.createInitializer().firstName("John").lastName("Smith"))
            }
        '''
        assert message.contains('[Static type checking] - Cannot call Person#<init>')
        assert message =~ /.*SET.*SET.*UNSET.*/
    }

    void testInitializerStrategyCanonical() {
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.*

            @Canonical
            @Builder(builderStrategy=InitializerStrategy)
            class Person {
                String firstName
                String lastName
                int age
            }

            @CompileStatic
            def firstLastAge() {
                assert new Person(Person.createInitializer().firstName("John").lastName("Smith").age(21)).toString() == 'Person(John, Smith, 21)'
            }
            firstLastAge()
        '''
    }

    void testInitializerStrategyOnConstructorAndMethods() {
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.*

            @ToString
            @Builder(builderStrategy=InitializerStrategy)
            class Person {
                String firstName
                String lastName
                int age

                @Builder(builderStrategy=InitializerStrategy, builderClassName='FullNameInitializer', builderMethodName='fullNameInitializer')
                Person(String fullName, int age) {
                    String[] splitFullName = fullName.split(' ')
                    firstName = splitFullName?.first()
                    lastName = splitFullName?.last()
                    this.age = age
                }

                @Builder(builderStrategy=InitializerStrategy, builderClassName='NameListInitializer', builderMethodName='listInitializer')
                Person(List<String> nameParts, Integer age) {
                    firstName = nameParts[0]
                    lastName = nameParts[-1]
                    this.age = age
                }

                @Builder(builderStrategy=InitializerStrategy, builderClassName='StringInitializer', builderMethodName='stringInitializer')
                static Person personStringFactory(String allBits) {
                    String[] bits = allBits.split(',')
                    new Person(bits[0], bits[1], bits[2].toInteger())
                }
            }

            @CompileStatic
            def test() {
                assert new Person(Person.createInitializer().firstName('John').lastName('Smith').age(10)).toString() == 'Person(John, Smith, 10)'
                assert new Person(Person.fullNameInitializer().fullName('John Smith').age(10)).toString() == 'Person(John, Smith, 10)'
                assert new Person(Person.listInitializer().nameParts(['John', 'Smith']).age(10)).toString() == 'Person(John, Smith, 10)'
                assert Person.personStringFactory(Person.stringInitializer().allBits("John,Smith,10")).toString() == 'Person(John, Smith, 10)'
            }
            test()
        '''
    }

    void testInitializerStrategySetters() {
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.*

            @Canonical(useSetters=true)
            @Builder(builderStrategy=InitializerStrategy)
            class Person {
                String name
                void setName(String name) { this.name = name?.toUpperCase() }
            }

            @CompileStatic
            def make() {
                assert new Person(Person.createInitializer().name("John")).toString() == 'Person(JOHN)'
            }
            make()
        '''
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.*

            @Canonical
            @TupleConstructor(includes='')
            @Builder(builderStrategy=InitializerStrategy, useSetters=true, force=true)
            class Person {
                String name
                void setName(String name) { this.name = name?.toUpperCase() }
            }

            @CompileStatic
            def make() {
                assert new Person(Person.createInitializer().name("John")).toString() == 'Person(JOHN)'
            }
            make()
        '''
    }

    void testInitializerStrategyIncludeSuperProperties() {
        assertScript '''
            import groovy.transform.builder.*
            import groovy.transform.*

            class Mamal {
                int age
            }

            @ToString(includeSuperProperties=true)
            @Builder(builderStrategy=InitializerStrategy, includeSuperProperties=true)
            class Person extends Mamal {
                String firstName
                String lastName
            }

            @CompileStatic
            def parentBuilder() {
                assert new Person(Person.createInitializer().firstName("John").lastName("Smith").age(21)).toString() == 'Person(John, Smith, 21)'
            }
            // static case
            parentBuilder()
            // dynamic case
            assert new Person(Person.createInitializer().firstName("John").lastName("Smith").age(21)).toString() == 'Person(John, Smith, 21)'
        '''
        def message = shouldNotCompile '''
            import groovy.transform.builder.*
            import groovy.transform.*

            class Mamal {
                int age
            }

            @ToString
            @Builder(builderStrategy=InitializerStrategy, includeSuperProperties=true)
            class Person extends Mamal {
                String firstName
                String lastName
            }

            @CompileStatic
            def firstLastButNoAge() {
                new Person(Person.createInitializer().firstName("John").lastName("Smith"))
            }
        '''
        assert message.contains('[Static type checking] - Cannot call Person#<init>')
        assert message =~ /.*SET.*SET.*UNSET.*/
    }

    void testBuilderWithPackageName_GROOVY7501() {
        assertScript '''
            package alfa.beta

            import groovy.transform.builder.*

            @Builder class PersonDef { }
            assert PersonDef.builder().class.name == 'alfa.beta.PersonDef$PersonDefBuilder'

            @Builder(builderStrategy=InitializerStrategy) class PersonInit { String foo }
            assert PersonInit.createInitializer().class.name == 'alfa.beta.PersonInit$PersonInitInitializer'
        '''
    }

    void testInitializerStrategyEmptyCases_GROOVY7503() {
        def message = shouldNotCompile '''
            import groovy.transform.builder.*
            @Builder(builderStrategy=InitializerStrategy) class Foo { }
        '''
        assert message.contains('at least one property is required for this strategy')
        message = shouldNotCompile '''
            import groovy.transform.builder.*
            @Builder(builderStrategy=InitializerStrategy, excludes='bar') class Foo { String bar }
        '''
        assert message.contains('at least one property is required for this strategy')
        message = shouldNotCompile '''
            import groovy.transform.builder.*
            class Foo {
              @Builder(builderStrategy=InitializerStrategy)
              Foo() {
              }
            }
        '''
        assert message.contains('at least one parameter is required for this strategy')
    }

    void testInternalFieldsAreIncludedIfRequestedForSimpleStrategy_GROOVY6454() {
        assertScript '''
            import groovy.transform.builder.*

            @Builder(builderStrategy = SimpleStrategy, allNames = true)
            class HasInternalPropertyWithSimpleStrategy {
                String $internal
            }
            assert new HasInternalPropertyWithSimpleStrategy().set$internal("foo").$internal == "foo"
         '''
    }

    void testInternalFieldsAreIncludedIfRequestedForExternalStrategy_GROOVY6454() {
        assertScript '''
            import groovy.transform.builder.*

            class HasInternalProperty {
                String $internal
            }

            @Builder(builderStrategy = ExternalStrategy, forClass = HasInternalProperty, allNames = true)
            class HasInternalPropertyBuilder { }

            assert new HasInternalPropertyBuilder().$internal("foo").build().$internal == "foo"
         '''
    }

    void testInternalFieldsAreIncludedIfRequestedForDefaultStrategy_GROOVY6454() {
        assertScript '''
            import groovy.transform.builder.*

            @Builder(allNames = true)
            class HasInternalProperty {
                String $internal
            }

            assert HasInternalProperty.builder().$internal("foo").$internal == "foo"
         '''
    }

    void testInternalFieldsAreIncludedIfRequestedForInitializerStrategy_GROOVY6454() {
        assertScript '''
            import groovy.transform.builder.*

            @Builder(builderStrategy = InitializerStrategy, allNames = true)
            class HasInternalProperty {
                String $internal
            }

            def initializer = HasInternalProperty.createInitializer()
            assert new HasInternalProperty(initializer.$internal("foo")).$internal == "foo"
         '''
    }

    // GROOVY-8186
    void testJavaBeanPropertiesAreProperlyProcessed() {
        assertScript '''
            import groovy.transform.builder.*

            class Foo {
              String getName() {
                'John'
              }
              void setName(String ignore) {}
            }

            @Builder(builderStrategy=ExternalStrategy, forClass=Foo)
            class FooBuilder { }

            assert new FooBuilder().name('Mary').build().name == 'John'
         '''
    }
}
