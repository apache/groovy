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

class ToStringTransformTest extends GroovyShellTestCase {

    void testSimpleToString() {
        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString
            class Person {
                String firstName
                String surName
            }

            new Person(firstName: 'John', surName: 'Doe').toString()
        """)

        assertEquals("Person(John, Doe)", toString)
    }

    void testIgnoreNulls() {
        def toString = evaluate("""
                import groovy.transform.ToString

                @ToString(ignoreNulls = true)
                class Person {
                    String firstName
                    String surName
                }

                new Person(firstName: null, surName: 'Doe').toString()
            """)

        assertEquals("Person(Doe)", toString)

        toString = evaluate("""
                import groovy.transform.ToString

                @ToString(ignoreNulls = true)
                class Person {
                    String firstName
                    String surName
                }

                new Person(firstName: null, surName: null).toString()
            """)

        assertEquals("Person()", toString)
    }

    void testIncludeFieldNamesAndIgnoreNulls() {
        def toString = evaluate("""
                    import groovy.transform.ToString

                    @ToString(includeNames = true, ignoreNulls = true)
                    class Person {
                        String firstName
                        String surName
                    }

                    new Person(firstName: null, surName: 'Doe').toString()
                """)

        assertEquals("Person(surName:Doe)", toString)
    }

    void testIncludeFieldNamesAndDoNotIgnoreNulls() {
        def toString = evaluate("""
                        import groovy.transform.ToString

                        @ToString(includeNames = true, ignoreNulls = false)
                        class Person {
                            String firstName
                            String surName
                        }

                        new Person(firstName:null, surName: 'Doe').toString()
                    """)

        assertEquals("Person(firstName:null, surName:Doe)", toString)
    }

    void testIncludeFieldsAndIgnoreNulls() {
        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString(includeFields = true, includeNames = true, ignoreNulls = true)
            class Person {
                String firstName
                String surName

                private age = 50
            }

            new Person(firstName:null, surName: 'Doe').toString()
        """)

        assertEquals("Person(surName:Doe, age:50)", toString)
    }

    void testIncludeSuperProperties() {
        def toString = evaluate("""
            import groovy.transform.ToString

            class Person {
                String name
                private boolean flag = true
                static final int MAX_NAME_LENGTH = 30
            }

            @ToString(includeSuperProperties = true, includeNames = true)
            class BandMember extends Person {
                String bandName
            }

            new BandMember(name:'Bono', bandName: 'U2').toString()
        """)

        assertEquals("BandMember(bandName:U2, name:Bono)", toString)
    }

    void testSuper() {

        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString(ignoreNulls = true, includeNames = true)
            class HumanBeing {
                Boolean female = null
            }

            @ToString(includeSuper=true)
            class Person extends HumanBeing {
                String firstName
                String surName

                private age = 50
            }

            new Person(firstName:null, surName: 'Doe').toString()
        """)

        assertEquals("Person(null, Doe, HumanBeing())", toString)
    }

    void testIgnoreStaticProperties() {

        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString
            class Person {
                static int humanBeings = 0
            }

            new Person().toString()
        """)

        assertEquals("Person()", toString)
    }

    void testWithCollection() {

        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString(includeNames = true)
            class Person {
                def relatives = []
                def mates = [:]
            }

            new Person(relatives: ['a', 'b', 'c'], mates: [friends: ['c', 'd', 'e']]).toString()
        """)

        assertEquals("Person(relatives:[a, b, c], mates:[friends:[c, d, e]])", toString)
    }

    void testExcludesAndIgnoreNulls() {

        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString(excludes = 'surName', ignoreNulls = true)
            class Person {
                String surName
            }

            new Person(surName: 'Doe').toString()
        """)

        assertEquals("Person()", toString)
    }

    void testIncludesAndIgnoreNulls() {

        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString(includes = 'surName', ignoreNulls = true)
            class Person {
                String surName
            }

            new Person(surName: null).toString()
        """)

        assertEquals("Person()", toString)
    }

    void testSkipInternalProperties() {

        def toString = evaluate("""
            import groovy.transform.ToString

            @ToString(includeFields = true)
            class Person {
                private String \$surName = 'Doe'
            }

            new Person().toString()
        """)

        assertEquals("Person()", toString)
    }

    void testPseudoProperties() {
        def toString = evaluate('''
            import groovy.transform.*

            class Person {
                boolean isAdult() { false }
                boolean golfer = true
                boolean isSenior() { false }
                private getAge() { 40 }
                protected getBorn() { 1975 }
            }

            @ToString(excludes='last', includeNames=true, includeSuperProperties=true)
            class SportsPerson extends Person {
                private String _first
                String last, title
                boolean golfer = false
                boolean adult = true
                Boolean cyclist = true
                Boolean isMale() { true }
                void setFirst(String first) { this._first = first }
                String getFull() { "$_first $last" }
            }
            new SportsPerson(first: 'John', last: 'Smith', title: 'Mr').toString()
        ''')
        assert toString == "SportsPerson(title:Mr, cyclist:true, full:John Smith, golfer:false, senior:false, born:1975, adult:true)"
        // same again but with allProperties=false and with @CompileStatic for test coverage purposes
        toString = evaluate('''
            import groovy.transform.*

            class Person {
                boolean isAdult() { false }
                boolean golfer = true
                boolean isSenior() { false }
                private getAge() { 40 }
                protected getBorn() { 1975 }
            }

            @CompileStatic
            @ToString(excludes='last', includeNames=true, includeSuperProperties=true, allProperties=false)
            class SportsPerson extends Person {
                private String _first
                String last, title
                boolean golfer = false
                boolean adult = true
                Boolean cyclist = true
                Boolean isMale() { true }
                void setFirst(String first) { this._first = first }
                String getFull() { "$_first $last" }
            }
            new SportsPerson(first: 'John', last: 'Smith', title: 'Mr').toString()
        ''')
        assert toString == "SportsPerson(title:Mr, adult:true, cyclist:true, golfer:false)"
    }

    void testSelfReference() {

        def toString = evaluate("""
            import groovy.transform.*

            @ToString(includeFields=true, includeNames=true) class Tree {
                String val
                Tree left
                private Tree right
            }

            def self = new Tree(val:'foo', left:null, right:null)
            self.left = self
            self.right = self
            self.toString()
        """)

        assert toString == 'Tree(val:foo, left:(this), right:(this))'
    }

    void testIncludePackage() {
        def toString = evaluate("""
                package my.company

                import groovy.transform.ToString

                @ToString
                class Person {}

                new Person().toString()
            """)

        assertEquals("my.company.Person()", toString)

        toString = evaluate("""
                package my.company

                import groovy.transform.ToString

                @ToString(includePackage = true)
                class Person {}

                new Person().toString()
            """)

        assertEquals("my.company.Person()", toString)

        toString = evaluate("""
                package my.company
                
                import groovy.transform.ToString
                
                @ToString(includePackage = false)
                class Person {}
                
                new Person().toString()
            """)

        assertEquals("Person()", toString)
    }

    void testIncludeSuperWithoutSuperClassResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.ToString

                @ToString(includeSuper=true)
                class Person {
                    String surName
                }

                new Person(surName: "Doe").toString()
            """
        }
        assert message.contains("Error during @ToString processing: includeSuper=true but 'Person' has no super class.")
    }

    void testIncludesAndExcludesTogetherResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.ToString

                @ToString(includes='surName', excludes='surName')
                class Person {
                    String surName
                }

                new Person(surName: "Doe").toString()
            """
        }
        assert message.contains("Error during @ToString processing: Only one of 'includes' and 'excludes' should be supplied not both.")
    }

    void testIncludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.ToString

                @ToString(includes='sirName')
                class Person {
                    String surName
                }

                new Person(surName: "Doe").toString()
            """
        }
        assert message.contains("Error during @ToString processing: 'includes' property 'sirName' does not exist.")
    }

    void testExcludesWithInvalidPropertyNameResultsInError() {
        def message = shouldFail {
            evaluate """
                import groovy.transform.ToString

                @ToString(excludes='sirName')
                class Person {
                    String firstName
                    String surName
                }

                new Person(firstName: "John", surName: "Doe").toString()
            """
        }
        assert message.contains("Error during @ToString processing: 'excludes' property 'sirName' does not exist.")
    }

    void testExcludesWithPseudoPropertyName() {
        evaluate '''
            import groovy.transform.*

            @ToString(excludes='full')
            class Person {
                String first, last
                String getFull() { "$first $last" }
            }
            assert new Person(first: 'John', last: 'Smith').toString() == 'Person(John, Smith)'
        '''
    }

    void testInternalFieldsAreIncludedIfRequested() {
        evaluate '''
            import groovy.transform.*

            @ToString(allNames = true)
            class HasInternalProperty {
                String $
            }
            assert new HasInternalProperty($: "foo").toString() == 'HasInternalProperty(foo)'
        '''
    }

    void testIncludesWithSuper_Groovy8011() {
        def toString = evaluate("""
            import groovy.transform.*

            @ToString
            class Foo {
                String baz = 'baz'
            }

            @ToString(includes='super,num,blah', includeNames=true)
            class Bar extends Foo {
                String blah = 'blah'
                int num = 42
            }

            new Bar().toString()
        """)

        assert toString.contains('super:Foo(baz)')
    }

    void testIncludesOrdering_Groovy8014() {
        assertScript """
            import groovy.transform.*

            @ToString
            class Foo {
                String baz = 'baz'
            }

            @ToString(includes='a,c,super,b,d', includeFields=true, includeSuper=true)
            class Bar extends Foo {
                int a = 1
                int b = 2
                private int c = 3
                public int d = 4
            }

            assert new Bar().toString() == 'Bar(1, 3, Foo(baz), 2, 4)'
        """
    }

}
