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

import groovy.transform.Immutable
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class SemanticsTest {

    @Test
    void testVariableDefinition() {
        // tag::variable_definition_example1[]
        String x
        def y
        // end::variable_definition_example1[]
        // tag::variable_definition_example2[]
        var z
        // end::variable_definition_example2[]
    }

    @Test
    void testVariableAssignment() {
        assertScript '''
        // tag::variable_assignment_example[]
        x = 1
        println x

        x = new java.util.Date()
        println x

        x = -3.1499392
        println x

        x = false
        println x

        x = "Hi"
        println x
        // end::variable_assignment_example[]
        '''
    }

    @Test
    void testMultipleAssignment() {
        // tag::multiple_assignment_example[]
        def (a, b, c) = [10, 20, 'foo']
        assert a == 10 && b == 20 && c == 'foo'
        // end::multiple_assignment_example[]
    }

    @Test
    void testMultipleAssignmentWithTypes() {
        // tag::multiple_assignment_with_types[]
        def (int i, String j) = [10, 'foo']
        assert i == 10 && j == 'foo'
        // end::multiple_assignment_with_types[]
    }

    @Test
    void testMultipleAssignmentWithExistingVariables() {
        // tag::multiple_assignment_with_existing_variables[]
        def nums = [1, 3, 5]
        def a, b, c
        (a, b, c) = nums
        assert a == 1 && b == 3 && c == 5
        // end::multiple_assignment_with_existing_variables[]
    }

    @Test
    void testMultipleAssignmentWithArraysAndLists() {
        // tag::multiple_assignment_with_arrays_and_lists[]
        def (_, month, year) = "18th June 2009".split()
        assert "In $month of $year" == 'In June of 2009'
        // end::multiple_assignment_with_arrays_and_lists[]
    }

    @Test
    void testMultipleAssignmentOverflow() {
        // tag::multiple_assignment_overflow[]
        def (a, b, c) = [1, 2]
        assert a == 1 && b == 2 && c == null
        // end::multiple_assignment_overflow[]
    }

    @Test
    void testMultipleAssignmentUnderflow() {
        // tag::multiple_assignment_underflow[]
        def (a, b) = [1, 2, 3]
        assert a == 1 && b == 2
        // end::multiple_assignment_underflow[]
    }

    @Test
    void testIfElse() {
        // tag::if_else_example[]
        def x = false
        def y = false

        if ( !x ) {
            x = true
        }

        assert x == true

        if ( x ) {
            x = false
        } else {
            y = true
        }

        assert x == y
        // end::if_else_example[]
    }

    @Test
    void testSwitchCase() {
        // tag::switch_case_example[]
        def x = 1.23
        def result = ""

        switch (x) {
          case "foo":
            result = "found foo"
            // lets fall through

          case "bar":
            result += "bar"

          case [4, 5, 6, 'inList']:
            result = "list"
            break

          case 12..30:
            result = "range"
            break

          case Integer:
            result = "integer"
            break

          case Number:
            result = "number"
            break

          case ~/fo*/: // toString() representation of x matches the pattern?
            result = "foo regex"
            break

          case { it < 0 }: // or { x < 0 }
            result = "negative"
            break

          default:
            result = "default"
        }

        assert result == "number"
        // end::switch_case_example[]
    }

    @Test
    void testSwitchExpression() {
        def person = 'Romeo'
        // tag::switch_expression[]
        def partner = switch(person) {
            case 'Romeo'  -> 'Juliet'
            case 'Adam'   -> 'Eve'
            case 'Antony' -> 'Cleopatra'
            case 'Bonnie' -> 'Clyde'
        }
        // end::switch_expression[]
        assert partner == 'Juliet'
    }

    @Test
    void testClassicForLoop() {
        // tag::classic_for_loop_example[]
        String message = ''
        for (int i = 0; i < 5; i += 1) {
            message += 'Hi '
        }
        assert message == 'Hi Hi Hi Hi Hi '
        // end::classic_for_loop_example[]
    }

    @Test
    void testGroovyForLoop() {
        // tag::groovy_for_loop_example[]
        // iterate over a range
        def x = 0
        for ( i in 0..9 ) {
            x += i
        }
        assert x == 45

        // iterate over a list
        x = 0
        for ( i in [0, 1, 2, 3, 4] ) {
            x += i
        }
        assert x == 10

        // iterate over an array
        x = 0
        for ( i in new int[]{0, 1, 2, 3, 4} ) {
            x += i
        }
        assert x == 10

        // iterate over a map
        def map = [a:1, b:2, c:3]
        x = 0
        for ( e in map ) {
            x += e.value
        }
        assert x == 6

        // iterate over values in a map
        x = 0
        for ( v in map.values() ) {
            x += v
        }
        assert x == 6

        // iterate over the characters in a string
        def list = []
        for ( c in 'abc' ) {
            list.add(c)
        }
        assert list == ['a', 'b', 'c']

        // iterate with index
        for ( int i, k in map.keySet() ) {
            assert map.get(k) == i + 1
        }
        // end::groovy_for_loop_example[]
    }

    @Test
    void testWhileLoop() {
        // tag::while_loop_example[]
        def x = 0
        def y = 5

        while ( y-- > 0 ) {
            x++
        }

        assert x == 5
        // end::while_loop_example[]
    }

    @Test
    void testTryCatch() {
        // tag::try_catch_example[]
        try {
            'moo'.toLong()   // this will generate an exception
            assert false     // asserting that this point should never be reached
        } catch ( e ) {
            assert e in NumberFormatException
        }
        // end::try_catch_example[]
    }

    @Test
    void testTryCatchFinally() {
        // tag::try_catch_finally_example[]
        def z
        try {
            def i = 7, j = 0
            try {
                def k = i / j
                assert false        //never reached due to Exception in previous line
            } finally {
                z = 'reached here'  //always executed even if Exception thrown
            }
        } catch ( e ) {
            assert e in ArithmeticException
            assert z == 'reached here'
        }
        // end::try_catch_finally_example[]
    }

    @Test
    void testDestructuringMultipleAssignment() {
        // tag::destructuring[]
        def coordinates = new Coordinates(latitude: 43.23, longitude: 3.67) // <1>

        def (la, lo) = coordinates                                          // <2>

        assert la == 43.23                                                  // <3>
        assert lo == 3.67
        // end::destructuring[]
    }

    static
    // tag::coordinates-class[]
    @Immutable
    class Coordinates {
        double latitude
        double longitude

        double getAt(int idx) {
            if (idx == 0) latitude
            else if (idx == 1) longitude
            else throw new Exception('Wrong coordinate index, use 0 or 1')
        }
    }
    // end::coordinates-class[]
}
