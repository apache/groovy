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

class MapConstructorTransformTest extends GroovyShellTestCase {
    void testMapConstructorWithFinalFields() {
        assertScript '''
            import groovy.transform.*

            @ToString
            @MapConstructor
            class Person {
                final String first, last
            }

            assert new Person(first: 'Dierk', last: 'Koenig').toString() == 'Person(Dierk, Koenig)'
        '''
    }

    void testMapConstructorWithSetters() {
        assertScript '''
            import groovy.transform.*

            @ToString
            @MapConstructor(useSetters=true)
            class Person {
                String first, last
                void setFirst(String first) {
                    this.first = first?.toUpperCase()
                }
            }

            assert new Person(first: 'Dierk', last: 'Koenig').toString() == 'Person(DIERK, Koenig)'
        '''
    }

    void testMapConstructorWithIncludesAndExcludes() {
        assertScript '''
            import groovy.transform.*

            @ToString(includes='first')
            @MapConstructor(includes='first')
            class Person {
                String first, last
            }

            assert new Person(first: 'Dierk').toString() == 'Person(Dierk)'
        '''
        assertScript '''
            import groovy.transform.*

            @ToString @MapConstructor(includes='first')
            class Person {
                String first, last
            }

            assert new Person(first: 'Dierk', last: 'Koenig').toString() == 'Person(Dierk, null)'
        '''
        assertScript '''
            import groovy.transform.*

            @ToString @MapConstructor(excludes='last')
            class Person {
                String first, last
            }

            assert new Person(first: 'Dierk', last: 'Koenig').toString() == 'Person(Dierk, null)'
        '''
    }

    void testMapConstructorWithPost() {
        def msg = shouldFail(MissingPropertyException, '''
            import groovy.transform.*
            import org.codehaus.groovy.transform.ImmutableASTTransformation

            @ToString
            @MapConstructor(post={ ImmutableASTTransformation.checkPropNames(this, args) })
            class Person {
                String first, last
            }

            new Person(last: 'Koenig', nickname: 'mittie')
        ''')
        assert msg.contains('No such property: nickname for class: Person')
    }

    void testMapConstructorWithPostAndFields() {
        assertScript '''
            import groovy.transform.*

            @ToString(includeFields=true, includeNames=true)
            @MapConstructor(includeFields=true, post={ full = "$first $last" })
            class Person {
                final String first, last
                private final String full
            }

            assert new Person(first: 'Dierk', last: 'Koenig').toString() ==
                'Person(first:Dierk, last:Koenig, full:Dierk Koenig)'
        '''
    }

    void testMapConstructorWithPreAndPost() {
        assertScript '''
            import groovy.transform.*

            @TupleConstructor
            class Person {
                String first, last
            }

            @CompileStatic // optional
            @ToString(includeSuperProperties=true)
            @MapConstructor(pre={ super(args?.first, args?.last); args = args ?: [:] }, post = { first = first?.toUpperCase() })
            class Author extends Person {
                String bookName
            }

            assert new Author(first: 'Dierk', last: 'Koenig', bookName: 'ReGinA').toString() == 'Author(ReGinA, DIERK, Koenig)'
            assert new Author().toString() == 'Author(null, null, null)'
        '''
    }

    void testIncludesAndExcludesTogetherResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.MapConstructor

                @MapConstructor(includes='surName', excludes='surName')
                class Person {
                    String surName
                }

                new Person()
            """
        }
        assert message.contains("Error during @MapConstructor processing: Only one of 'includes' and 'excludes' should be supplied not both.")
    }

    void testIncludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.MapConstructor

                @MapConstructor(includes='sirName')
                class Person {
                    String firstName
                    String surName
                }

                new Person(surname: "Doe")
            """
        }
        assert message.contains("Error during @MapConstructor processing: 'includes' property 'sirName' does not exist.")
    }

    void testExcludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.MapConstructor

                @MapConstructor(excludes='sirName')
                class Person {
                    String firstName
                    String surName
                }

                new Person(surname: "Doe")
            """
        }
        assert message.contains("Error during @MapConstructor processing: 'excludes' property 'sirName' does not exist.")
    }

    void testInternalFieldsAreIncludedIfRequested() {
        assertScript '''
            import groovy.transform.*

            @MapConstructor(allNames = true)
            class HasInternalProperty {
                final String $
            }

            assert new HasInternalProperty($: "foo").$ == "foo"
        '''
    }

    // GROOVY-8012
    void testMapConstructorWithNoArgs() {
        assertScript '''
            @groovy.transform.MapConstructor
            class Foo {
                String bar
            }

            assert new Foo() instanceof Foo
        '''
    }

    // GROOVY-8776
    void testNestedMapConstructorCS() {
        assertScript '''
            import groovy.transform.*
            class GroovyMapConstructorCheck {
                @CompileStatic
                @MapConstructor
                @ToString
                static class Goo {
                    int x0
                }
            }
            assert new GroovyMapConstructorCheck.Goo(x0:123).toString() == 'GroovyMapConstructorCheck$Goo(123)'
        '''
    }

    // GROOVY-8777
    void testMapConstructorUsedInInnerCS() {
        assertScript '''
            import groovy.transform.*

            @CompileStatic
            class GroovyMapConstructorCheck {
                @MapConstructor(noArg = true)
                class Goo {
                    final int x0

                    @Override
                    public String toString() {
                        return "Goo(|$x0|)"
                    }
                }

                def go() {
                    new Goo(x0:123).toString().toUpperCase()
                }
            }

            final check = new GroovyMapConstructorCheck()
            assert check.go() == 'GOO(|123|)'
        '''
    }

}
