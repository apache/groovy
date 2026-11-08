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
package semantics

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Documentation examples for structural pattern matching in switch (GEP-19).
 */
final class SwitchPatternsTest {

    @Test
    void testTypePatterns() {
        // tag::type_patterns[]
        def describe = { obj ->
            switch (obj) {
                case Integer i when i > 0 -> "positive $i"    // guard: evaluated once the pattern matches
                case Integer i            -> "integer $i"
                case String s             -> s.toUpperCase()  // s is a String within the arm
                default                   -> 'other'
            }
        }
        assert describe(7) == 'positive 7'
        assert describe(-3) == 'integer -3'
        assert describe('abc') == 'ABC'
        assert describe(1.5) == 'other'
        // end::type_patterns[]
    }

    @Test
    void testRecordPatterns() {
        assertScript '''
            // tag::record_patterns[]
            record Point(int x, int y) {}
            record Line(Point start, Point end) {}

            def describe = { obj ->
                switch (obj) {
                    case Point(int x, int y) when x == y -> "diagonal $x"
                    case Point(int x, _)                 -> "x=$x"                       // `_` discards a component
                    case Line(Point(_, var y1), Point p) -> "from y=$y1 to ${p.x()},${p.y()}"
                    default                              -> 'other'
                }
            }
            assert describe(new Point(3, 3)) == 'diagonal 3'
            assert describe(new Point(1, 2)) == 'x=1'
            assert describe(new Line(new Point(0, 1), new Point(4, 5))) == 'from y=1 to 4,5'
            assert describe('not a record') == 'other'
            // end::record_patterns[]
        '''
    }

    @Test
    void testRecordPatternInInstanceof() {
        assertScript '''
            // tag::instanceof_record_pattern[]
            record Point(int x, int y) {}

            def p = new Point(3, 4)
            if (p instanceof Point(int x, int y)) {
                assert x + y == 7
            }
            assert p instanceof Point(var a, _) && a == 3
            // end::instanceof_record_pattern[]
        '''
    }

    @Test
    void testListPatterns() {
        // tag::list_patterns[]
        def describe = { xs ->
            switch (xs) {
                case []                                   -> 'empty'
                case [var only]                           -> "single: $only"
                case [1, var x, ...]                      -> "starts with 1, then $x"   // `...` absorbs the rest
                case [var first, var... middle, var last] -> "$first .. $last, $middle between"
                default                                   -> 'not a list'
            }
        }
        assert describe([]) == 'empty'
        assert describe(['a']) == 'single: a'
        assert describe([1, 2, 3]) == 'starts with 1, then 2'
        assert describe([5, 6, 7, 8]) == '5 .. 8, [6, 7] between'
        assert describe('no') == 'not a list'
        // end::list_patterns[]
    }

    @Test
    void testMapPatterns() {
        // tag::map_patterns[]
        def describe = { m ->
            switch (m) {
                case [:]                             -> 'empty map'
                case [type: 'circle', radius: var r] -> "circle r=$r"                  // literal value matched by ==
                case [name: String n, var... rest]   -> "named $n, other keys ${rest.keySet()}"
                default                              -> 'other'
            }
        }
        assert describe([:]) == 'empty map'
        assert describe([type: 'circle', radius: 2]) == 'circle r=2'
        assert describe([name: 'Groovy', age: 23]) == 'named Groovy, other keys [age]'
        assert describe([type: 'square']) == 'other'
        // end::map_patterns[]
    }

    @Test
    void testLegacyLabelsMix() {
        // tag::legacy_labels[]
        def describe = { obj ->
            switch (obj) {
                case [1, 2, 3]      -> 'in the list'      // legacy label: containment
                case [var a, var b] -> "pair $a, $b"      // pattern: a binding form is present
                case ~/[a-z]+/      -> 'lowercase word'   // legacy label: regex
                default             -> 'other'
            }
        }
        assert describe(2) == 'in the list'
        assert describe([7, 8]) == 'pair 7, 8'
        assert describe('abc') == 'lowercase word'
        // end::legacy_labels[]
    }

    @Test
    void testNullSubject() {
        // tag::null_subject[]
        def describe = { obj ->
            switch (obj) {
                case null     -> 'null'        // a legacy label still matches null
                case String s -> "string $s"   // a pattern never does
                default       -> 'other'
            }
        }
        assert describe(null) == 'null'
        assert describe('x') == 'string x'
        // end::null_subject[]
    }

    @Test
    void testSealedExhaustiveness() {
        assertScript '''
            // tag::sealed_exhaustiveness[]
            import groovy.transform.CompileStatic

            sealed interface Shape permits Circle, Square {}
            record Circle(double radius) implements Shape {}
            record Square(double side) implements Shape {}

            @CompileStatic
            double area(Shape shape) {
                return switch (shape) {   // no default needed: Circle and Square cover Shape
                    case Circle c -> Math.PI * c.radius() * c.radius()
                    case Square s -> s.side() * s.side()
                }
            }
            assert area(new Square(3)) == 9
            // end::sealed_exhaustiveness[]
        '''
    }

    @Test
    void testPatternStatementMustBeExhaustive() {
        // tag::statement_exhaustiveness[]
        def err = shouldFail '''
            import groovy.transform.TypeChecked

            @TypeChecked
            void show(Object o) {
                switch (o) {                 // a pattern label makes the statement subject to the check
                    case String s -> println s
                }
            }
        '''
        assert err.message.contains('the switch statement does not cover all possible input values')
        // end::statement_exhaustiveness[]
    }
}
