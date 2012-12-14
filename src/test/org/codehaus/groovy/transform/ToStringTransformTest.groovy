/*
 * Copyright 2008-2012 the original author or authors.
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
 * @author Andre Steingress
 */
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

    void testSuper()  {

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

    void testIgnoreStaticProperties()  {

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

    void testWithCollection()  {

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

    void testExcludesAndIgnoreNulls()  {

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

    void testIncludesAndIgnoreNulls()  {

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

    void testSkipInternalProperties()  {

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

    void testSelfReference()  {

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
}
