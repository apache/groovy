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
package groovy

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for GEP-19 structural pattern matching in switch: type patterns
 * with binding and {@code when} guards (arrow-form labels).
 */
final class SwitchPatternMatchingTest {

    @Test
    void testTypePatternDispatch() {
        def describe = { obj ->
            switch (obj) {
                case String s  -> s.toUpperCase()
                case Integer i -> i * 2
                case Number n  -> n.doubleValue()
                default        -> 'other'
            }
        }
        assert describe('abc') == 'ABC'
        assert describe(21) == 42
        assert describe(1.5G) == 1.5d
        assert describe(null) == 'other'
    }

    @Test
    void testWhenGuardFallsThroughToNextCase() {
        def sign = { obj ->
            switch (obj) {
                case Integer i when i > 0 -> 'positive'
                case Integer i when i < 0 -> 'negative'
                case Integer i            -> 'zero'
                default                   -> 'not an int'
            }
        }
        assert sign(7) == 'positive'
        assert sign(-7) == 'negative'
        assert sign(0) == 'zero'
        assert sign('x') == 'not an int'
    }

    @Test
    void testGuardSeesPatternVariableAndOuterLocals() {
        int min = 3
        def result = switch ('abcd') {
            case String s when s.length() > min -> "long ${s.length()}"
            case String s                       -> 'short'
            default                             -> 'other'
        }
        assert result == 'long 4'
    }

    @Test
    void testPatternsCoexistWithLegacyLabels() {
        def classify = { obj ->
            switch (obj) {
                case null                 -> 'nil'
                case 'a'..'c'             -> 'a to c'
                case ~/[a-z]+/            -> 'letters'
                case Integer i when i > 9 -> 'big int'
                case Integer i            -> "int $i"
                case { it == [] }         -> 'empty list closure'
                default                   -> 'other'
            }
        }
        assert classify('abc') == 'letters'
        assert classify('b') == 'a to c'
        assert classify(10) == 'big int'
        assert classify(5) == 'int 5'
        assert classify([]) == 'empty list closure'
        assert classify(null) == 'nil'
        assert classify(3.5) == 'other'
    }

    @Test
    void testArrowSwitchStatementWithPatterns() {
        def out = []
        switch (42 as Object) {
            case String s  -> out << "string $s"
            case Integer i -> out << "int ${i + 1}"
            default        -> out << 'other'
        }
        assert out == ['int 43']
    }

    @Test
    void testPatternVariableNotVisibleAfterSwitch() {
        shouldFail MissingPropertyException, '''
            switch (42 as Object) {
                case Integer i -> i
                default        -> 0
            }
            i
        '''
    }

    @Test
    void testSubjectEvaluatedOnce() {
        assertScript '''
            int count = 0
            def next = { count++; 42 }
            def result = switch (next()) {
                case Integer i when i == 42 -> 'match'
                default                     -> 'no'
            }
            assert result == 'match'
            assert count == 1
        '''
    }

    @Test
    void testTypePatternCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            def describe(Object obj) {
                switch (obj) {
                    case Integer i when i > 0 -> 'positive ' + (i * 2)
                    case String s             -> s.toUpperCase() // proves narrowing: only String has toUpperCase()
                    case Number n             -> 'number ' + n.doubleValue()
                    default                   -> 'other'
                }
            }
            assert describe(21) == 'positive 42'
            assert describe('abc') == 'ABC'
            assert describe(-1.5G) == 'number -1.5'
            assert describe(new Object()) == 'other'
            assert describe(null) == 'other'
        '''
    }

    @Test
    void testGenericTypePattern() {
        def result = switch (['a', 'bb'] as Object) {
            case List<String> strings -> strings*.size().sum()
            default                   -> -1
        }
        assert result == 3
    }

    @Test
    void testPatternRequiresArrowForm() {
        def err = shouldFail '''
            def result = switch (42) {
                case Integer i: yield i
                default: yield 0
            }
        '''
        assert err.message.contains('arrow form')
    }

    @Test
    void testStatementFormSwitchDoesNotSupportPatterns() {
        shouldFail '''
            switch (42) {
                case Integer i:
                    println i
                    break
            }
        '''
    }

    // GEP-19 phase 2: static type checking of pattern switches

    @Test
    void testImpossiblePatternRejectedWhenTypeChecked() {
        def err = shouldFail '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Integer i) {
                switch (i) {
                    case String s -> s
                    default       -> 'other'
                }
            }
        '''
        assert err.message.contains('incompatible with the switch subject type')
    }

    @Test
    void testImpossiblePatternIgnoredInDynamicCode() {
        // dynamic Groovy stays permissive: the arm simply never matches
        assertScript '''
            def m(Integer i) {
                switch (i) {
                    case String s -> s
                    default       -> 'other'
                }
            }
            assert m(42) == 'other'
        '''
    }

    @Test
    void testDominatedPatternWarningWhenTypeChecked() {
        def warnings = patternSwitchWarnings '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Object o) {
                switch (o) {
                    case Number n  -> 'number'
                    case Integer i -> 'integer'
                    default        -> 'other'
                }
            }
        '''
        assert warnings.any { it.contains('dominated by a preceding case label') }
    }

    @Test
    void testNonExhaustivePatternSwitchWarningWhenTypeChecked() {
        def warnings = patternSwitchWarnings '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Object o) {
                switch (o) {
                    case String s  -> s
                    case Integer i -> i
                }
            }
        '''
        assert warnings.any { it.contains('not exhaustive') }
    }

    @Test
    void testDefaultBranchMakesPatternSwitchExhaustive() {
        assert patternSwitchWarnings('''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Object o) {
                switch (o) {
                    case String s -> s
                    default       -> 'other'
                }
            }
        ''').isEmpty()
    }

    @Test
    void testUnconditionalPatternMakesPatternSwitchExhaustive() {
        assert patternSwitchWarnings('''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Integer i) {
                switch (i) {
                    case Number n -> n.intValue()
                }
            }
        ''').isEmpty()
    }

    @Test
    void testSealedHierarchyCoverageMakesPatternSwitchExhaustive() {
        assert patternSwitchWarnings('''
            import groovy.transform.TypeChecked
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape { double radius = 1 }
            final class Square implements Shape { double side = 1 }
            @TypeChecked
            def area(Shape shape) {
                switch (shape) {
                    case Circle c -> 3.14 * c.radius * c.radius
                    case Square s -> s.side * s.side
                }
            }
        ''').isEmpty()
    }

    @Test
    void testIncompleteSealedHierarchyCoverageWarns() {
        def warnings = patternSwitchWarnings '''
            import groovy.transform.TypeChecked
            sealed interface Shape permits Circle, Square {}
            final class Circle implements Shape { double radius = 1 }
            final class Square implements Shape { double side = 1 }
            @TypeChecked
            def area(Shape shape) {
                switch (shape) {
                    case Circle c -> 3.14 * c.radius * c.radius
                }
            }
        '''
        assert warnings.any { it.contains('not exhaustive') }
    }

    @Test
    void testGuardedPatternDoesNotCountTowardsExhaustiveness() {
        def warnings = patternSwitchWarnings '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Number n) {
                switch (n) {
                    case Number x when x.intValue() > 0 -> 'positive'
                }
            }
        '''
        assert warnings.any { it.contains('not exhaustive') }
    }

    @Test
    void testEnumShorthandMixedWithPatternsWhenCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic
            enum Color { RED, GREEN, BLUE }
            @CompileStatic
            def describe(Color c) {
                switch (c) {
                    case RED -> 'red'
                    case Color k when k.name().startsWith('G') -> 'greenish'
                    default -> 'other'
                }
            }
            assert describe(Color.RED) == 'red'
            assert describe(Color.GREEN) == 'greenish'
            assert describe(Color.BLUE) == 'other'
        '''
    }

    // GEP-19 phase 3: record patterns

    @Test
    void testRecordPatternBasics() {
        assertScript '''
            record Point(int x, int y) {}
            def r = switch (new Point(1, 2)) {
                case Point(int x, int y) -> x + y
                default                  -> -1
            }
            assert r == 3
        '''
    }

    @Test
    void testRecordPatternGuardAndWildcard() {
        assertScript '''
            record Point(int x, int y) {}
            def m = { obj ->
                switch (obj) {
                    case Point(int x, int y) when x == y -> "diagonal $x"
                    case Point(int x, _)                 -> "x=$x"
                    default                              -> 'other'
                }
            }
            assert m(new Point(3, 3)) == 'diagonal 3'
            assert m(new Point(1, 2)) == 'x=1'
            assert m('s') == 'other'
        '''
    }

    @Test
    void testNestedRecordPattern() {
        assertScript '''
            record Point(int x, int y) {}
            record Line(Point start, Point end) {}
            def r = switch (new Line(new Point(0, 0), new Point(4, 5))) {
                case Line(Point(var x1, _), Point p2) -> "$x1 to ${p2.x()},${p2.y()}"
                default                               -> 'other'
            }
            assert r == '0 to 4,5'
        '''
    }

    @Test
    void testRecordPatternComponentTypeNarrowing() {
        assertScript '''
            record Box(Object value) {}
            def m = { obj ->
                switch (obj) {
                    case Box(Integer i) -> "int $i"
                    case Box(String s)  -> "string $s"
                    default             -> 'other'
                }
            }
            assert m(new Box(42)) == 'int 42'
            assert m(new Box('t')) == 'string t'
            assert m(new Box(1.5)) == 'other'
        '''
    }

    @Test
    void testRecordPatternArityMismatchNeverMatches() {
        assertScript '''
            record Point(int x, int y) {}
            def r = switch (new Point(1, 2)) {
                case Point(var a) -> "one $a"
                default           -> 'no match'
            }
            assert r == 'no match'
        '''
    }

    @Test
    void testRecordPatternCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic
            record Point(int x, int y) {}
            @CompileStatic
            def m(Object o) {
                switch (o) {
                    case Point(int x, int y) when x == y -> 'diagonal ' + (x * 2)
                    case Point(int x, _)                 -> 'x ' + x
                    default                              -> 'other'
                }
            }
            assert m(new Point(2, 2)) == 'diagonal 4'
            assert m(new Point(1, 5)) == 'x 1'
            assert m('s') == 'other'
        '''
    }

    @Test
    void testRecordPatternArityCheckedWhenTypeChecked() {
        def err = shouldFail '''
            import groovy.transform.TypeChecked
            record Point(int x, int y) {}
            @TypeChecked
            def m(Object o) {
                switch (o) {
                    case Point(int x, _) when x > 0 -> x
                    case Point(var a)               -> a
                    default                         -> 'other'
                }
            }
        '''
        assert err.message.contains('specifies 1 component(s) but')
    }

    @Test
    void testRecordPatternDominatedByEarlierTypePattern() {
        def warnings = patternSwitchWarnings '''
            import groovy.transform.TypeChecked
            record Point(int x, int y) {}
            @TypeChecked
            def m(Object o) {
                switch (o) {
                    case Point p             -> 'point'
                    case Point(int x, int y) -> 'components'
                    default                  -> 'other'
                }
            }
        '''
        assert warnings.any { it.contains('dominated by a preceding case label') }
    }

    @Test
    void testLegacyMethodCallLabelsKeepIsCaseSemantics() {
        assertScript '''
            def lower(s) { s.toLowerCase() }
            def noArg() { 42 }
            def r = switch ('abc') {
                case lower('ABC') -> 'call label'
                default           -> 'no'
            }
            assert r == 'call label'
            def r2 = switch (42) {
                case noArg() -> 'no-arg call label'
                default      -> 'no'
            }
            assert r2 == 'no-arg call label'
        '''
    }

    @Test
    void testDeconstructableViaToList() {
        // not a record, but provides toList(): deconstructs like emulated Groovy records
        assertScript '''
            class Pair {
                def a, b
                Pair(a, b) { this.a = a; this.b = b }
                List toList() { [a, b] }
            }
            def r = switch (new Pair(1, 'x')) {
                case Pair(var a, var b) -> "$a-$b"
                default                 -> 'no'
            }
            assert r == '1-x'
        '''
    }

    private static List<String> patternSwitchWarnings(String source) {
        def cu = new CompilationUnit()
        cu.addSource('PatternSwitchTestScript.groovy', source)
        cu.compile(Phases.CLASS_GENERATION)
        (cu.errorCollector.warnings ?: [])*.message.findAll {
            it.contains('pattern switch') || it.contains('case pattern')
        }
    }
}
