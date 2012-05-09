/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.transform.stc

/**
 * Unit tests for static type checking : fields and properties.
 *
 * @author Cedric Champeau
 */
class FieldsAndPropertiesSTCTest extends StaticTypeCheckingTestCase {

    void testAssignFieldValue() {
        assertScript """
            class A {
                int x
            }

            A a = new A()
            a.x = 1
        """
    }

    void testAssignFieldValueWithWrongType() {
        shouldFailWithMessages '''
            class A {
                int x
            }

            A a = new A()
            a.x = '1'
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testMapDotPropertySyntax() {
        assertScript '''
            HashMap map = [:]
            map['a'] = 1
            map.b = 2
            assert map.get('a') == 1
            assert map.get('b') == 2
        '''
    }

    void testInferenceFromFieldType() {
        assertScript '''
            class A {
                String name = 'Cedric'
            }
            A a = new A()
            def b = a.name
            b.toUpperCase() // type of b should be inferred from field type
        '''
    }

    void testAssignFieldValueWithAttributeNotation() {
        assertScript """
            class A {
                int x
            }

            A a = new A()
            a.@x = 1
        """
    }

    void testAssignFieldValueWithWrongTypeAndAttributeNotation() {
         shouldFailWithMessages '''
             class A {
                 int x
             }

             A a = new A()
             a.@x = '1'
         ''', 'Cannot assign value of type java.lang.String to variable of type int'
     }

    void testInferenceFromAttributeType() {
        assertScript '''
            class A {
                String name = 'Cedric'
            }
            A a = new A()
            def b = a.@name
            b.toUpperCase() // type of b should be inferred from field type
        '''
    }

    void testShouldComplainAboutMissingField() {
        shouldFailWithMessages '''
            Object o = new Object()
            o.x = 0
        ''', 'No such property: x for class: java.lang.Object'
    }

    void testShouldComplainAboutMissingField2() {
        shouldFailWithMessages '''
            class A {
            }
            A a = new A()
            a.x = 0
        ''', 'No such property: x for class: A'
    }

    void testFieldWithInheritance() {
        assertScript '''
            class A {
                int x
            }
            class B extends A {
            }
            B b = new B()
            b.x = 2
        '''
    }

    void testAttributeWithInheritance() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            class B extends A {
            }
            B b = new B()
            b.@x = 2
        ''', 'No such property: x for class: B'
    }

    void testFieldTypeWithInheritance() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            class B extends A {
            }
            B b = new B()
            b.x = '2'
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testFieldWithInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class B extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            B b = new B()
            b.x = 2
        '''
    }

    void testFieldWithInheritanceFromAnotherSourceUnit2() {
        shouldFailWithMessages '''
            class B extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            B b = new B()
            b.x = '2'
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testFieldWithSuperInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class B extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass2 {
            }
            B b = new B()
            b.x = 2
        '''
    }

    void testMethodUsageForProperty() {
        assertScript '''
            class Foo {
                String name
            }
            def name = new Foo().getName()
            name?.toUpperCase()
        '''
    }

    void testDateProperties() {
        assertScript '''
            Date d = new Date()
            def time = d.time
            d.time = 0
        '''
    }

    // GROOVY-5232
    void testSetterForProperty() {
        assertScript '''
            class Person {
                String name

                static Person create() {
                    def p = new Person()
                    p.setName("Guillaume")
                    // but p.name = "Guillaume" works
                    return p
                }
            }

            Person.create()
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldPass() {
        assertScript '''
            class Foo {
                int x = 1
            }
            1
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypes() {
        shouldFailWithMessages '''
            class Foo {
                int x = new Date()
            }
            1
        ''', 'Cannot assign value of type java.util.Date to variable of type int'
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypesWithClosure() {
        shouldFailWithMessages '''
            class Foo {
                Closure<List> cls = { Date aDate ->  aDate.getTime() }
            }
            1
        ''', 'Incompatible generic argument types. Cannot assign groovy.lang.Closure <java.lang.Long> to: groovy.lang.Closure <List>'
    }

    public static class BaseClass {
        int x
    }

    public static class BaseClass2 extends BaseClass {
    }
}

