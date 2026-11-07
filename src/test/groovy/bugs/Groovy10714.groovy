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

import org.codehaus.groovy.runtime.MethodClosure
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import java.util.function.BinaryOperator
import java.util.function.Function

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A functional argument implements only a functional interface whose abstract
 * method it can be called with, so overloads taking functional interfaces of
 * different arity are told apart by the arity the argument accepts, at run time
 * as well as under static type checking.
 */
final class Groovy10714 {

    static class Ext {
        static <T> String red(Collection<T> c, BinaryOperator<T> op) { 'combiner:' + c.stream().reduce(op).get() }
        static <T, R> String red(Collection<T> c, Function<T, R> f) { 'transform:' + c.collect { f.apply(it) } }
    }

    static class Pair {
        Integer f(Integer a, Integer b) { a + b }
    }

    static class Single {
        Integer f(Integer a) { -a }
    }

    private static final String EXT = '''
        import java.util.function.*
        class Ext {
            static <T> String red(Collection<T> c, BinaryOperator<T> op) { 'combiner:' + c.stream().reduce(op).get() }
            static <T, R> String red(Collection<T> c, Function<T, R> f) { 'transform:' + c.collect { f.apply(it) } }
        }
        class Calc {
            Integer add(Integer a, Integer b) { a + b }
            Integer twice(Integer a) { a * 2 }
        }
    '''

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.TypeChecked'])
    void testMethodReference(String mode) {
        assertScript EXT + """
            $mode
            void test() {
                List<Integer> numbers = [1, 2, 3]
                List<String> strings = ['a ', ' b']
                def calc = new Calc()
                3.times { // past the call site's first link
                    assert Ext.red(numbers, Integer::sum) == 'combiner:6'
                    assert Ext.red(numbers, Math::negateExact) == 'transform:[-1, -2, -3]'
                    assert Ext.red(strings, String::concat) == 'combiner:a  b'
                    assert Ext.red(strings, String::trim) == 'transform:[a, b]'
                    assert Ext.red(numbers, calc::add) == 'combiner:6'
                    assert Ext.red(numbers, calc::twice) == 'transform:[2, 4, 6]'
                    assert Ext.red(numbers, calc.&add) == 'combiner:6'
                    assert Ext.red(['1', '2'], Integer::new) == 'transform:[1, 2]'
                }
            }

            test()
        """
    }

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.TypeChecked'])
    void testRunnableOrConsumer(String mode) {
        assertScript """
            import java.util.function.*
            interface CheckedConsumer<T> { void accept(T t) throws Throwable }
            interface CheckedRunnable { void run() throws Throwable }
            class Try<T> {
                final T value
                Try(T value) { this.value = value }
                String andThenTry(Consumer<? super T> consumer) { consumer.accept(value); 'Consumer' }
                String andThenTry(Runnable runnable) { runnable.run(); 'Runnable' }
                String andThenTryChecked(CheckedConsumer<? super T> consumer) { consumer.accept(value); 'CheckedConsumer' }
                String andThenTryChecked(CheckedRunnable runnable) { runnable.run(); 'CheckedRunnable' }
            }
            class Table {
                String last
                void putItem(List<String> request) { last = 'List' }
                void putItem(Consumer<Object> consumer) { last = 'Consumer' }
                void putItem(String item) { last = 'String' }
            }
            class Steps {
                static void noop() {}
            }

            $mode
            void test() {
                def table = new Table()
                def attempt = new Try<List<String>>(['x'])
                assert attempt.andThenTry(table::putItem) == 'Consumer'
                assert table.last == 'List'
                assert attempt.andThenTry(Steps::noop) == 'Runnable'
                assert attempt.andThenTryChecked(table::putItem) == 'CheckedConsumer'
                assert attempt.andThenTryChecked(Steps::noop) == 'CheckedRunnable'
            }

            test()
        """
    }

    // the choice depends on the method reference, not only on its class
    @Test
    void testOneCallSiteGivenMethodReferencesOfDifferentArity() {
        assertScript EXT + '''
            def site(List numbers, functor) { Ext.red(numbers, functor) }

            def results = (1..20).collect { i ->
                site([1, 2, 3], i % 2 ? Integer::sum : Math::negateExact)
            }
            assert results.unique() == ['combiner:6', 'transform:[-1, -2, -3]']
        '''
    }

    @Test
    void testMultipleArityMethodReferenceStaysAmbiguous() {
        def err = shouldFail EXT + '''
            Ext.red(['1', '2'], Integer::valueOf) // valueOf(String) and valueOf(String, int)
        '''
        assert err.message.contains('Ambiguous method overloading')
    }

    @Test
    void testRehydratedMethodClosureFollowsItsOwner() {
        def pair = new MethodClosure(new Pair(), 'f')
        assert Ext.red([1, 2, 3], pair) == 'combiner:6'
        def single = pair.rehydrate(null, new Single(), null)
        assert Ext.red([1, 2, 3], single) == 'transform:[-1, -2, -3]'
    }

    @Test
    void testDehydratedMethodClosureChosenAsBefore() {
        def dehydrated = new MethodClosure(Integer, 'sum').dehydrate()
        def err = shouldFail { Ext.red([1, 2, 3], dehydrated) }
        assert err.message.contains('Ambiguous method overloading')
    }

    private static final String TRY = '''
        import java.util.function.Consumer
        class Try<T> {
            final T value
            Try(T value) { this.value = value }
            String andThenTry(Consumer<? super T> consumer) { consumer.accept(value); 'Consumer' }
            String andThenTry(Runnable runnable) { runnable.run(); 'Runnable' }
        }
    '''

    @ParameterizedTest
    @ValueSource(strings = ['', '@groovy.transform.PackedClosures'])
    void testClosureWithDeclaredParameters(String mode) {
        assertScript TRY + """
            $mode
            class Usage {
                static String kind(Object o) { o.getClass().simpleName }

                List results() {
                    def attempt = new Try<String>('x')
                    [attempt.andThenTry { String s -> assert s == 'x' },
                     attempt.andThenTry { String s, int n = 1 -> assert s == 'x' && n == 1 },
                     attempt.andThenTry((String s) -> { assert s == 'x' }),
                     attempt.andThenTry { -> },
                     attempt.andThenTry { it },
                     kind { String s -> s }]
                }
            }

            def (consumer, withDefault, lambda, none, implicit, kind) = new Usage().results()
            assert [consumer, withDefault, lambda] == ['Consumer'] * 3
            assert [none, implicit] == ['Runnable'] * 2
            assert ('$mode' as boolean) == kind.startsWith('Fixed') // packed, through a fixed-arity class
        """
    }

    @Test
    void testRehydratedClosure() {
        assertScript TRY + '''
            def attempt = new Try<String>('x')
            def closure = { String s -> assert s == 'x' }
            assert attempt.andThenTry(closure.rehydrate(null, null, null)) == 'Consumer'
            assert attempt.andThenTry(closure.dehydrate()) == 'Consumer'
        '''
    }

    @Test
    void testOneCallSiteGivenClosuresOfDifferentArity() {
        assertScript TRY + '''
            def site(Try attempt, functor) { attempt.andThenTry(functor) }

            def attempt = new Try<String>('x')
            def results = (1..20).collect { i ->
                site(attempt, i % 2 ? { String s -> assert s == 'x' } : { -> })
            }
            assert results.unique() == ['Consumer', 'Runnable']
        '''
    }

    // what a method closure accepts is worked out when needed, so its serial form stays as it was
    @Test
    void testSerialFormOfMethodClosureUnchanged() {
        def fields = ObjectStreamClass.lookup(MethodClosure).fields*.name
        assert fields.sort() == ['anyInstanceMethodExists', 'method']
    }
}
