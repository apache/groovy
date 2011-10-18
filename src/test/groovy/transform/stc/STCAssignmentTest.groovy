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
 * Unit tests for static type checking : assignments.
 *
 * @author Cedric Champeau
 */
class STCAssignmentTest extends StaticTypeCheckingTestCase {

    void testAssignmentFailure() {
        shouldFailWithMessages """
            int x = new Object()
        """, "Cannot assign value of type java.lang.Object to variable of type java.lang.Integer"
    }

    void testAssignmentFailure2() {
        shouldFailWithMessages """
            Set set = new Object()
        """, "Cannot assign value of type java.lang.Object to variable of type java.util.Set"
    }

    void testAssignmentFailure3() {
        shouldFailWithMessages """
            Set set = new Integer(2)
        """, "Cannot assign value of type java.lang.Integer to variable of type java.util.Set"
    }

    void testIndirectAssignment() {
        shouldFailWithMessages """
            def o = new Object()
            int x = o
        """, "Cannot assign value of type java.lang.Object to variable of type java.lang.Integer"
    }

    void testIndirectAssignment2() {
        shouldFailWithMessages """
            def o = new Object()
            Set set = o
        """, "Cannot assign value of type java.lang.Object to variable of type java.util.Set"
    }

    void testIndirectAssignment3() {
        shouldFailWithMessages """
            int x = 2
            Set set = x
        """, "Cannot assign value of type int to variable of type java.util.Set"
    }

    void testAssignmentToEnum() {
        assertScript """
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 'a' // string to enum is implicit
            e = "${'a'}" // gstring to enum is implicit too
        """
    }

    void testAssignmentToEnumFailure() {
        shouldFailWithMessages """
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 1
        """, "Cannot assign value of type int to variable of type MyEnum"
    }

    void testAssignmentToString() {
        assertScript """
            String str = new Object()
        """
    }

    void testAssignmentToBoolean() {
        assertScript """
            boolean test = new Object()
        """
    }

    void testAssignmentToBooleanClass() {
        assertScript """
            Boolean test = new Object()
        """
    }

    void testAssignmentToClass() {
        assertScript """
            Class test = 'java.lang.String'
        """
    }

    void testPlusEqualsOnInt() {
        assertScript """
            int i = 0
            i += 1
        """
    }

    void testMinusEqualsOnInt() {
        assertScript """
            int i = 0
            i -= 1
        """
    }

    void testIntPlusEqualsString() {
        shouldFailWithMessages """
            int i = 0
            i += '1'
        """, "Cannot find matching method int#plus(java.lang.String)",
             "tbd..." // todo : remove when transformation will be complete
    }

    void testIntMinusEqualsString() {
        shouldFailWithMessages """
            int i = 0
            i -= '1'
        """, "Cannot find matching method int#minus(java.lang.String)",
             "tbd..." // todo : remove when transformation will be complete
    }

    void testStringPlusEqualsString() {
        assertScript """
            String str = 'test'
            str+='test2'
        """
    }

}

