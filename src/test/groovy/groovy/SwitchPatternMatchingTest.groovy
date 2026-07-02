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

    @Test
    void testListPatternDispatch() {
        assertScript '''
            def describe(x) {
                switch (x) {
                    case []               -> 'empty'
                    case [var only]       -> "one: $only"
                    case [var a, var b]   -> "two: $a, $b"
                    default               -> 'other'
                }
            }
            assert describe([]) == 'empty'
            assert describe([42]) == 'one: 42'
            assert describe([1, 2]) == 'two: 1, 2'
            assert describe([1, 2, 3]) == 'other'
            assert describe('s') == 'other'   // not a List, array or Iterable
            assert describe([a: 1]) == 'other'
            assert describe(null) == 'other'  // patterns do not match null
        '''
    }

    @Test
    void testListPatternRestForms() {
        assertScript '''
            def r = switch ([1, 2, 3]) {
                case [var h, var... t] -> "h=$h, t=$t"
                default                -> 'no'
            }
            assert r == 'h=1, t=[2, 3]'
            def r2 = switch ([1]) {
                case [var h, var... t] -> "h=$h, t=$t"
                default                -> 'no'
            }
            assert r2 == 'h=1, t=[]'
            def r3 = switch (['a', 'b']) {
                case [... t] -> t        // `... t` is a shortcut for `var... t`
                default      -> 'no'
            }
            assert r3 == ['a', 'b']
            def r4 = switch (1..4) {
                case [var first, var... middle, var last] -> "$first, $middle, $last"
                default                                   -> 'no'
            }
            assert r4 == '1, [2, 3], 4'
        '''
    }

    @Test
    void testEmptyAndBareRestListPatterns() {
        assertScript '''
            def shape(x) {
                switch (x) {
                    case []    -> 'empty'
                    case [...] -> 'non-empty'
                    default    -> 'not a list'
                }
            }
            assert shape([]) == 'empty'
            assert shape([1, 2]) == 'non-empty'
            assert shape(42) == 'not a list'
            // without a preceding empty case, `[...]` matches any list, including empty
            def r = switch ([]) {
                case [...] -> 'any list'
                default    -> 'no'
            }
            assert r == 'any list'
        '''
    }

    @Test
    void testListPatternTypedElements() {
        assertScript '''
            def r = switch ([1, 'x']) {
                case [Integer i, String s] -> "$i-$s"
                default                    -> 'no'
            }
            assert r == '1-x'
            def r2 = switch (['x', 1]) {
                case [Integer i, String s] -> "$i-$s"
                default                    -> 'no'
            }
            assert r2 == 'no'
            def r3 = switch ([1, null]) {
                case [Integer i, String s] -> "$i-$s" // typed element does not match null
                case [Integer i, var v]    -> "$i:$v" // var element does
                default                    -> 'no'
            }
            assert r3 == '1:null'
        '''
    }

    @Test
    void testListPatternTypedRest() {
        assertScript '''
            def sum(x) {
                switch (x) {
                    case [Integer... nums] -> nums.sum() ?: 0
                    default                -> 'not all ints'
                }
            }
            assert sum([1, 2, 3]) == 6
            assert sum([]) == 0
            assert sum([1, 'x']) == 'not all ints'
        '''
    }

    @Test
    void testListPatternLiteralElements() {
        assertScript '''
            def r = switch ([1, 9, 8]) {
                case [1, var x, ...] -> "starts with 1, then $x"
                default              -> 'no'
            }
            assert r == 'starts with 1, then 9'
            def r2 = switch ([2, 9]) {
                case [1, var x, ...] -> 'yes'
                default              -> 'no'
            }
            assert r2 == 'no'
        '''
    }

    @Test
    void testListPatternGuard() {
        assertScript '''
            def r = switch ([1, 2]) {
                case [var a, var b] when a < b -> 'ascending'
                case [var a, var b]            -> 'other pair'
                default                        -> 'no'
            }
            assert r == 'ascending'
            def r2 = switch ([2, 1]) {
                case [var a, var b] when a < b -> 'ascending'
                case [var a, var b]            -> 'other pair'
                default                        -> 'no'
            }
            assert r2 == 'other pair'
        '''
    }

    @Test
    void testNestedPatternsInListPattern() {
        assertScript '''
            record Point(int x, int y) {}
            def r = switch ([new Point(3, 4), 'z']) {
                case [Point(var x, _), var tag] -> "$x-$tag"
                default                         -> 'no'
            }
            assert r == '3-z'
            def r2 = switch ([[1], 2]) {
                case [[var a], var b] -> "$a, $b"
                default               -> 'no'
            }
            assert r2 == '1, 2'
            def r3 = switch ([1, 2]) {
                case [[var a], var b] -> "$a, $b" // 1 is not destructurable
                default               -> 'no'
            }
            assert r3 == 'no'
        '''
    }

    @Test
    void testListPatternDestructuresArraysAndIterables() {
        assertScript '''
            def describe(x) {
                switch (x) {
                    case [var a, var... rest] -> "$a+${rest.size()}"
                    default                   -> 'no'
                }
            }
            assert describe(new int[] {7, 8, 9}) == '7+2'
            assert describe(new String[] {'a'}) == 'a+0'
            assert describe([10, 20] as LinkedHashSet) == '10+1'
        '''
    }

    @Test
    void testListPatternWildcardElements() {
        assertScript '''
            def r = switch ([1, 2, 3]) {
                case [var _, var x, var _] -> x  // `var _` is bind-and-discard
                default                    -> 'no'
            }
            assert r == 2
            def r2 = switch ([1, 2, 3, 4]) {
                case [var h, ... _] -> h
                default             -> 'no'
            }
            assert r2 == 1
        '''
    }

    @Test
    void testListPatternCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            def m(Object o) {
                switch (o) {
                    case [Integer a, var... t] -> a + t.size()
                    case []                    -> 0
                    default                    -> -1
                }
            }
            assert m([5, 'x', 'y']) == 7
            assert m([]) == 0
            assert m('s') == -1
        '''
    }

    @Test
    void testLegacyListLiteralLabelsKeepIsCaseSemantics() {
        // a list literal without a binding form keeps its legacy containment semantics
        assertScript '''
            def r = switch (2) {
                case [1, 2, 3] -> 'contained'
                default        -> 'no'
            }
            assert r == 'contained'
            def r2 = switch (5) {
                case [1, 2, 3] -> 'contained'
                default        -> 'no'
            }
            assert r2 == 'no'
            def r3 = switch (2) {
                case [1, 2, 3]: yield 'contained' // colon form stays legacy as well
                default: yield 'no'
            }
            assert r3 == 'contained'
            def r4 = switch ([1, 2]) {
                case [[1, 2], [3, 4]] -> 'contained' // nested literals stay legacy too
                default               -> 'no'
            }
            assert r4 == 'contained'
            def r5 = switch (3) {
                case [1, 2, 3] -> 'contained' // legacy containment amid pattern labels
                case Integer i -> "int $i"
                default        -> 'no'
            }
            assert r5 == 'contained'
            def r6 = switch (7) {
                case [1, 2, 3] -> 'contained'
                case Integer i -> "int $i"
                default        -> 'no'
            }
            assert r6 == 'int 7'
        '''
    }

    @Test
    void testGuardOnLegacyListLabelRejected() {
        def err = shouldFail '''
            def r = switch (2) {
                case [1, 2, 3] when true -> 'x'
                default                  -> 'y'
            }
        '''
        assert err.message.contains('`when` guards are only supported on pattern labels')
    }

    @Test
    void testListPatternSupportsAtMostOneRestBinding() {
        def err = shouldFail '''
            def r = switch ([1, 2]) {
                case [var... a, var... b] -> 'x'
                default                   -> 'y'
            }
        '''
        assert err.message.contains('at most one rest binding')
    }

    @Test
    void testListPatternIncompatibleSubjectRejectedWhenTypeChecked() {
        def err = shouldFail '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Integer i) {
                switch (i) {
                    case [var a] -> a
                    default      -> 'other'
                }
            }
        '''
        assert err.message.contains('list pattern is incompatible with the switch subject type')
    }

    @Test
    void testListPatternExhaustivenessUnassessed() {
        // a list pattern is always conditional, so no exhaustiveness warning is issued
        assert patternSwitchWarnings('''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(List l) {
                switch (l) {
                    case [var a] -> a
                    case []      -> 0
                }
            }
        ''').isEmpty()
    }

    @Test
    void testMapPatternDispatch() {
        assertScript '''
            def describe(x) {
                switch (x) {
                    case [:]                        -> 'empty map'
                    case [name: var n, age: var a]  -> "person $n, $a"
                    case [name: var n]              -> "named $n"
                    default                         -> 'other'
                }
            }
            assert describe([:]) == 'empty map'
            assert describe([name: 'sam', age: 42]) == 'person sam, 42'
            assert describe([name: 'sam', age: 42, id: 1]) == 'person sam, 42' // open: extra keys ignored
            assert describe([name: 'dee']) == 'named dee'
            assert describe([age: 42]) == 'other'  // required key missing
            assert describe([1, 2]) == 'other'     // not a map
            assert describe(null) == 'other'       // patterns do not match null
        '''
    }

    @Test
    void testMapPatternLiteralAndTypedValues() {
        assertScript '''
            def r = switch ([type: 'circle', radius: 5]) {
                case [type: 'circle', radius: var r] -> "circle r=$r"
                case [type: 'square', side: var s]   -> "square s=$s"
                default                              -> 'other'
            }
            assert r == 'circle r=5'
            def r2 = switch ([name: 42]) {
                case [name: String n] -> "string $n" // typed value does not match
                case [name: var v]    -> "any $v"
                default               -> 'other'
            }
            assert r2 == 'any 42'
            def r3 = switch ([name: null]) {
                case [name: String n] -> "string $n" // typed value does not match null
                case [name: var v]    -> "present $v" // var does, if the key is present
                default               -> 'other'
            }
            assert r3 == 'present null'
        '''
    }

    @Test
    void testMapPatternRest() {
        assertScript '''
            def r = switch ([name: 'sam', age: 42, id: 1]) {
                case [name: String n, ... rest] -> "named $n; others=$rest"
                default                         -> 'other'
            }
            assert r == "named sam; others=[age:42, id:1]"
            def r2 = switch ([name: 'sam']) {
                case [name: String n, ... rest] -> rest
                default                         -> 'other'
            }
            assert r2 == [:]
            def r3 = switch ([name: 'dee', x: 1]) {
                case [name: def n, ...] -> "any named map ($n)" // others discarded
                default                 -> 'other'
            }
            assert r3 == 'any named map (dee)'
        '''
    }

    @Test
    void testMapPatternGuard() {
        assertScript '''
            def r = switch ([age: 42]) {
                case [age: Integer a] when a >= 18 -> 'adult'
                case [age: Integer a]              -> 'minor'
                default                            -> 'other'
            }
            assert r == 'adult'
            def r2 = switch ([age: 7]) {
                case [age: Integer a] when a >= 18 -> 'adult'
                case [age: Integer a]              -> 'minor'
                default                            -> 'other'
            }
            assert r2 == 'minor'
        '''
    }

    @Test
    void testNestedPatternsInMapPattern() {
        assertScript '''
            record Point(int x, int y) {}
            def r = switch ([origin: new Point(3, 4), tags: ['a', 'b']]) {
                case [origin: Point(var x, _), tags: [var first, ...]] -> "$x-$first"
                default                                                -> 'other'
            }
            assert r == '3-a'
            def r2 = switch ([outer: [inner: 7]]) {
                case [outer: [inner: var v]] -> v
                default                      -> 'other'
            }
            assert r2 == 7
            def r3 = switch ([[a: 1], 'x']) {
                case [[a: var v], var tag] -> "$v-$tag" // map pattern nested in a list pattern
                default                    -> 'other'
            }
            assert r3 == '1-x'
        '''
    }

    @Test
    void testMapPatternCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            def m(Object o) {
                switch (o) {
                    case [name: String n, ... rest] -> n + rest.size()
                    case [:]                        -> 'empty'
                    default                         -> 'other'
                }
            }
            assert m([name: 'sam', x: 1, y: 2]) == 'sam2'
            assert m([:]) == 'empty'
            assert m('s') == 'other'
        '''
    }

    @Test
    void testLegacyMapLiteralLabelsKeepIsCaseSemantics() {
        // a map literal without a binding form keeps its legacy lookup semantics
        assertScript '''
            def r = switch ('a') {
                case [a: true, b: false] -> 'truthy value'
                default                  -> 'no'
            }
            assert r == 'truthy value'
            def r2 = switch ('b') {
                case [a: true, b: false] -> 'truthy value'
                default                  -> 'no'
            }
            assert r2 == 'no'
            def r3 = switch ('a') {
                case [a: [1, 2]]: yield 'truthy value' // colon form stays legacy as well
                default: yield 'no'
            }
            assert r3 == 'truthy value'
            def r4 = switch ('a') {
                case [a: true]  -> 'legacy amid pattern labels'
                case String s   -> "string $s"
                default         -> 'no'
            }
            assert r4 == 'legacy amid pattern labels'
        '''
    }

    @Test
    void testMapPatternKeysMustBeConstants() {
        def err = shouldFail '''
            def k = 'name'
            def r = switch ([name: 'sam']) {
                case [("$k".toString()): var n] -> n
                default                         -> 'other'
            }
        '''
        assert err.message.contains('map pattern keys must be constants')
    }

    @Test
    void testMapPatternSupportsAtMostOneRestBinding() {
        def err = shouldFail '''
            def r = switch ([a: 1]) {
                case [a: var v, ... r1, ... r2] -> 'x'
                default                         -> 'y'
            }
        '''
        assert err.message.contains('at most one rest binding')
    }

    @Test
    void testMapPatternIncompatibleSubjectRejectedWhenTypeChecked() {
        def err = shouldFail '''
            import groovy.transform.TypeChecked
            @TypeChecked
            def m(Integer i) {
                switch (i) {
                    case [name: var n] -> n
                    default            -> 'other'
                }
            }
        '''
        assert err.message.contains('map pattern is incompatible with the switch subject type')
    }

    @Test
    void testPatternVariableNamesMayRepeatAcrossArms() {
        // each arm of a pattern switch has its own scope
        assertScript '''
            record Circle(int r) {}
            record Square(int side) {}
            def area(shape) {
                switch (shape) {
                    case Circle(var d) -> 3 * d * d
                    case Square(var d) -> d * d
                    case Integer d     -> d
                    default            -> 0
                }
            }
            assert area(new Circle(2)) == 12
            assert area(new Square(3)) == 9
            assert area(5) == 5
        '''
    }

    @Test
    void testRecordDeconstructedOncePerMatch() {
        // the closure-free lowering binds pattern variables while matching,
        // so a matched record is deconstructed exactly once
        assertScript '''
            class Counter {
                static int reads = 0
                final int value
                Counter(int value) { this.value = value }
                List toList() { reads++; [value] }
            }
            def r = switch (new Counter(7)) {
                case Counter(var v) -> v
                default             -> -1
            }
            assert r == 7
            assert Counter.reads == 1
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
