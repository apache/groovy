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
            Map map = [:]
            map['a'] = 1
            map.b = 2
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
}

