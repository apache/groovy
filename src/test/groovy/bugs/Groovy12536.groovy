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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A {@code List<String>} filled by dynamic code may hold {@code GString} elements.
 * Static compilation already coerces those elements where a {@code String} is required
 * for assignments, arguments, and closures. Native lambdas and method references must
 * do the same, instead of throwing {@code ClassCastException} from the generated bridge.
 */
final class Groovy12536 {

    @Test
    void testLambdaUsesGStringElementAsString() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class L {
                static Object upper(List<String> l) {
                    l.stream().map(s -> s.toUpperCase()).findFirst().get()
                }
                static Object length(List<String> l) {
                    l.stream().map(s -> s.length()).findFirst().get()
                }
                static Object text(List<String> l) {
                    l.stream().map(s -> s.toString()).findFirst().get()
                }
                static Object identity(List<String> l) {
                    l.stream().map(s -> s).findFirst().get()
                }
                static Object explicit(List<String> l) {
                    l.stream().map((String s) -> s.toUpperCase()).findFirst().get()
                }
                static Object closure(List<String> l) {
                    l.stream().map { it.toUpperCase() }.findFirst().get()
                }
            }

            def n = 'ab'
            List<String> gs = ["$n"]
            assert L.upper(gs) == 'AB'
            assert L.length(gs) == 2
            assert L.text(gs) == 'ab'
            assert L.identity(gs) == 'ab'
            assert L.identity(gs) instanceof String
            assert L.explicit(gs) == 'AB'
            assert L.closure(gs) == 'AB'
            assert L.upper(['ab']) == 'AB'
            assert L.identity(['ab']) == 'ab'
        '''
    }

    @Test
    void testMethodReferenceUsesGStringElementAsString() {
        assertScript '''
            import java.util.function.Function
            import groovy.transform.CompileStatic

            @CompileStatic
            class L {
                static Object upper(List<String> l) {
                    l.stream().map(String::toUpperCase).findFirst().get()
                }
                static Object length(List<String> l) {
                    l.stream().map(String::length).findFirst().get()
                }
                static Object text(List<String> l) {
                    l.stream().map(String::toString).findFirst().get()
                }
                static Object objectText(List<String> l) {
                    l.stream().map(Object::toString).findFirst().get()
                }
                static Object copy(List<String> l) {
                    l.stream().map(String::new).findFirst().get()
                }
                static Object parsed(List<String> l) {
                    Function<String, Integer> f = Integer::valueOf
                    l.stream().map(f).toList()
                }
                static Object charSequenceLength(List<String> l) {
                    l.stream().map(CharSequence::length).findFirst().get()
                }
            }

            def n = 'ab'
            List<String> gs = ["$n"]
            assert L.upper(gs) == 'AB'
            assert L.length(gs) == 2
            assert L.text(gs) == 'ab'
            assert L.text(gs) instanceof String
            assert L.objectText(gs) == 'ab'
            assert L.copy(gs) == 'ab'
            assert L.copy(gs) instanceof String
            assert L.parsed(['12', "${'3'}"]) == [12, 3]
            assert L.charSequenceLength(gs) == 2
            assert L.upper(['ab']) == 'AB'
        '''
    }

    @Test
    void testSeveralStringParametersAndBoundReferences() {
        assertScript '''
            import java.util.function.BiFunction
            import java.util.function.BinaryOperator
            import java.util.function.Consumer
            import groovy.transform.CompileStatic

            @CompileStatic
            class L {
                static Object joined(List<String> l) {
                    l.stream().reduce('', (a, b) -> a.concat(b))
                }
                static Object compared(List<String> l) {
                    l.stream().sorted(String::compareTo).toList()
                }
                static Object concatRef(List<String> l) {
                    BinaryOperator<String> op = String::concat
                    l.stream().reduce('', op)
                }
                static Object boundConcat(List<String> l) {
                    l.stream().map('-'::concat).findFirst().get()
                }
                static Object boundExtension(List<String> l) {
                    BiFunction<Integer, String, String> pad = 'ab'::center
                    def first = l.stream().findFirst().get()
                    pad.apply(5, first)
                }
                static Object consumed(List<String> l) {
                    def box = new Box()
                    Consumer<String> c = box::set
                    l.stream().forEach(c)
                    box.value
                }
                static Object nested(List<String> l) {
                    l.stream().map(s -> { def inner = { s.toUpperCase() }; inner() }).findFirst().get()
                }
            }

            class Box {
                String value
                void set(String s) { value = s }
            }

            def n = 'ab'
            def m = 'cd'
            List<String> gs = ["$n", "$m"]
            assert L.joined(gs) == 'abcd'
            assert L.compared(["$m", "$n"]) == ['ab', 'cd']
            assert L.concatRef(gs) == 'abcd'
            assert L.boundConcat(gs) == '-ab'
            assert L.boundExtension(["${'x'}"]) == 'xabxx'
            assert L.consumed(gs) == 'cd'
            assert L.nested(gs) == 'AB'
            assert L.joined(['ab']) == 'ab'
        '''
    }

    @Test
    void testNullAndRealStringAndUnrelatedGenerics() {
        assertScript '''
            import java.lang.constant.Constable
            import groovy.transform.CompileStatic

            interface Up { String apply(String s) }
            interface Id<T extends Constable> { String id(T t) }

            @CompileStatic
            class L {
                static Object identity(List<String> l) {
                    l.stream().map(s -> s).toList()
                }
                static Object plusOne(List<Integer> l) {
                    l.stream().map(i -> i + 1).toList()
                }
                static String plainSam(String s) {
                    Up up = (String x) -> x.toUpperCase()
                    up.apply(s)
                }
                static String constableSam(String s) {
                    Id<String> id = (t -> t.toString())
                    id.id(s)
                }
            }

            assert L.identity([null]) == [null]
            assert L.plusOne([1, 2]) == [2, 3]
            assert L.plainSam('ab') == 'AB'
            assert L.constableSam('ab') == 'ab'
        '''
    }

    @Test
    void testNonStringElementStillCasts() {
        def err = shouldFail ClassCastException, '''
            import groovy.transform.CompileStatic

            @CompileStatic
            def upper(List<String> l) {
                l.stream().map(s -> s.toUpperCase()).findFirst().get()
            }

            upper([new StringBuilder('ab')])
        '''
        assert err.message.contains('StringBuilder')
    }
}
