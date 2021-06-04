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
package groovy.transform.stc

import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

/**
 * Unit tests for static type checking : generics.
 */
class GenericsSTCTest extends StaticTypeCheckingTestCase {

    void testDeclaration1() {
        assertScript '''
            List test = new LinkedList<String>()
        '''
    }

    void testDeclaration2() {
        assertScript '''
            Map<String,Integer> obj = new HashMap<String,Integer>()
        '''
    }

    void testDeclaration3() {
        shouldFailWithMessages '''
            Map<String,String> obj = new HashMap<String,Integer>()
        ''', 'Incompatible generic argument types. Cannot assign java.util.HashMap<java.lang.String, java.lang.Integer> to: java.util.Map<java.lang.String, java.lang.String>'
    }

    void testDeclaration4() {
        // no generics checked after first wildcard
        shouldFailWithMessages '''
            Map<? extends CharSequence,String> obj = new HashMap<String,Integer>()
        ''', 'Incompatible generic argument types. Cannot assign java.util.HashMap<java.lang.String, java.lang.Integer> to: java.util.Map<? extends java.lang.CharSequence, java.lang.String>'
    }

    void testAddOnList() {
        shouldFailWithMessages '''
            List<String> list = []
            list.add(1)
        ''', "[Static type checking] - Cannot find matching method java.util.ArrayList#add(int)"
    }

    void testAddOnList2() {
        assertScript '''
            List<String> list = []
            list.add 'Hello'
        '''

        assertScript '''
            List<Integer> list = []
            list.add 1
        '''
    }

    void testAddOnListWithDiamond() {
        assertScript '''
            List<String> list = new LinkedList<>()
            list.add 'Hello'
        '''
    }

    void testAddOnListUsingLeftShift() {
        shouldFailWithMessages '''
            List<String> list = []
            list << 1
        ''', 'Cannot call <T> java.util.ArrayList#leftShift(T) with arguments [int]'
    }

    void testAddOnList2UsingLeftShift() {
        assertScript '''
            List<String> list = []
            list << 'Hello'
        '''

        assertScript '''
            List<Integer> list = []
            list << 1
        '''
    }

    void testAddOnListWithParameterizedTypeLeftShift() {
        assertScript '''
            class Trie<T> {}
            List<Trie<String>> list = []
            list << new Trie<String>()
        '''
    }

    void testAddOnListWithDiamondUsingLeftShift() {
        assertScript '''
            List<String> list = new LinkedList<>()
            list << 'Hello'
        '''
    }

    void testListInferrenceWithNullElems() {
        assertScript '''
            List<String> strings = ['a', null]
            assert strings == ['a',null]
        '''
    }

    void testListInferrenceWithAllNullElems() {
        assertScript '''
            List<String> strings = [null, null]
            assert strings == [null,null]
        '''
    }

    void testAddOnListWithDiamondAndWrongType() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list.add 'Hello'
        ''', 'Cannot find matching method java.util.LinkedList#add(java.lang.String). Please check if the declared type is correct and if the method exists.'
    }

    void testAddOnListWithDiamondAndWrongTypeUsingLeftShift() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list << 'Hello'
        ''', 'Cannot call <T> java.util.LinkedList#leftShift(T) with arguments [java.lang.String]'
    }

    void testAddOnListWithDiamondAndNullUsingLeftShift() {
        assertScript '''
            List<Integer> list = new LinkedList<>()
            list << null
        '''
    }

    void testReturnTypeInference1() {
        assertScript '''
            class Foo<U> {
                U method() { }
            }
            Foo<Integer> foo = new Foo<Integer>()
            Integer result = foo.method()
        '''
    }

    void testReturnTypeInference2() {
        assertScript '''
        Object m() {
          def s = '1234'
          println 'Hello'
        }
        '''
    }

    void testReturnTypeInferenceWithMethodGenerics1() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.List<java.lang.Long>'
            })
            def list = Arrays.asList(new Long[]{1L,0L})
            assert list.size() == 2
            assert list.get(0) == 1
            assert list.get(1) == 0
        '''

        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.List<java.lang.Long>'
            })
            def list = Arrays.asList(1L,0L)
            assert list.size() == 2
            assert list.get(0) == 1
            assert list.get(1) == 0
        '''

        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.List<java.lang.Long>'
            })
            def list = Arrays.asList(0L)
            assert list.size() == 1
            assert list.get(0) == 0
        '''

        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.List<java.lang.Object>'
            })
            def list = Arrays.asList()
            assert list.size() == 0
        '''
    }

    // GROOVY-10062
    void testReturnTypeInferenceWithMethodGenerics2() {
        assertScript '''
            def <T> T m(T t, ... zeroOrMore) {
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == Integer_TYPE
            })
            def obj = m(42)
        '''
    }

    void testReturnTypeInferenceWithMethodGenerics3() {
        assertScript '''
            Number number = Optional.of(42).get()
            assert number.intValue() == 42
        '''
    }

    // GROOVY-9796
    void testReturnTypeInferenceWithMethodGenerics4() {
        shouldFailWithMessages '''
            Number number = Optional.of(42).orElse(Double.NaN)
            assert number.intValue() == 42
        ''', '[Static type checking] - Cannot find matching method java.util.Optional#orElse(double).'
    }

    void testReturnTypeInferenceWithMethodGenerics5() {
        assertScript '''
            Number number = Optional.of((Number) 42).orElse(Double.NaN)
            assert number.intValue() == 42
        '''
    }

    // GROOVY-9796
    void testReturnTypeInferenceWithMethodGenerics6() {
        shouldFailWithMessages '''
            Number number = Optional.ofNullable((Integer) null).orElse(42d)
            assert number.intValue() == 42
        ''', '[Static type checking] - Cannot find matching method java.util.Optional#orElse(double).'
    }

    void testReturnTypeInferenceWithMethodGenerics7() {
        assertScript '''
            Number number = Optional.<Number>ofNullable((Integer) null).orElse(42d)
            assert number.intValue() == 42
        '''
    }

    // GROOVY-9033
    void testReturnTypeInferenceWithMethodGenerics8() {
        shouldFailWithMessages '''
            List<String> test() {
              def x = [].each { }
              x.add(new Object())
              return x // List<E>
            }
        ''', 'Incompatible generic argument types.' // Cannot assign java.util.List<java.lang.Object> to: java.util.List<java.lang.String>

        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.genericsTypes[0].toString() == 'java.lang.String'
                assert type.genericsTypes[1].toString() == 'java.util.List<java.lang.Object>' // not List<E>
            })
            def map = [ key: [] ]
        '''
    }

    // GROOVY-10049, GROOVY-10053
    void testReturnTypeInferenceWithMethodGenerics9() {
        assertScript '''
            def <X> Set<X> f(Class<X> x) {
                Collections.singleton(x.newInstance(42))
            }
            def <Y extends Number> List<Y> g(Class<Y> y) {
                f(y).stream().filter(n -> n.intValue() > 0).toList()
            }

            def result = g(Integer)
            assert result == [ 42 ]
        '''

        ['t::cast', 'n -> t.cast(n)', 'n -> (N) n', '{ n -> (N) n }'].each { cast ->
            assertScript """
                Set<Number> f() {
                    Collections.<Number>singleton(42)
                }
                def <N extends Number> Set<N> g(Class<N> t) {
                    Set<N> result = new HashSet<>()
                    f().stream().filter(t::isInstance)
                        .map($cast).forEach(n -> result.add(n))
                    return result
                }

                def result = g(Integer)
                assert result == [42] as Set
            """
        }

        assertScript '''
            def <T> String test(Iterable<T> iterable) {
                Iterator<T> it = iterable.iterator()
                if (it.hasNext()) {
                    List<String[]> table = []
                    it.forEachRemaining(r -> {
                        if (r instanceof List) {
                            String[] cells = ((List) r).stream().map(c -> c?.toString())
                            table.add(cells)
                        }
                    })
                    return table
                }
            }

            String result = test([ ['x'], ['y'], [null] ])
            assert result == '[[x], [y], [null]]'
        '''
    }

    // GROOVY-10051
    void testReturnTypeInferenceWithMethodGenerics10() {
        assertScript '''
            abstract class State<H extends Handle> {
                // Why not return HandleContainer<H>? I can't really say.
                def <T extends Handle> HandleContainer<T> getHandleContainer(key) {
                }
            }
            class HandleContainer<H extends Handle> {
                H handle
            }
            interface Handle {
                Result getResult()
            }
            class Result {
                int itemCount
                String[] items
            }

            List<String> getStrings(State state, List keys) {
                keys.collectMany { key ->
                    List<String> strings = Collections.emptyList()
                    def container = state.getHandleContainer(key) // returns HandleContainer<Object> not HandleContainer<SearchHandle>
                    if (container != null) {
                        def result = container.handle.result
                        if (result != null && result.itemCount > 0) {
                            strings = Arrays.asList(result.items)
                        }
                    }
                    strings
                }
            }

            assert getStrings(null,[]).isEmpty()
        '''
    }

    // GROOVY-10067
    void testReturnTypeInferenceWithMethodGenerics11() {
        assertScript '''
            def <N extends Number> N getNumber() {
                return (N) 42
            }
            def f(Integer i) {
            }
            def g(int i) {
            }

            Integer i = this.<Integer>getNumber()
            f(this.<Integer>getNumber())
            g(this.<Integer>getNumber())

            i = (Integer) getNumber()
            f((Integer) getNumber())
            g((Integer) getNumber())

            i = getNumber()
            f(getNumber())
            g(getNumber())

            i = number
            f(number)
            g(number)
        '''
    }

    // GROOVY-8974
    void testReturnTypeInferenceWithMethodGenerics12() {
        assertScript '''
            def <T> T identity(T t) { t }
            List<String> list = identity(new ArrayList<>())
            list.add('foo')
            def foo = list[0]
            assert foo.toUpperCase() == 'FOO'
        '''
    }

    // GROOVY-9064
    void testReturnTypeInferenceWithMethodGenerics13() {
        assertScript '''
            List getSomeRows() { [new String[]{'x'}] }
            List<String[]> rows = getSomeRows()
            rows.each { row ->
                def col = row[0].toUpperCase()
                assert col == 'X'
            }
        '''
    }

    // GROOVY-8409
    void testReturnTypeInferenceWithMethodGenerics14() {
        ['R', 'S', 'T', 'U'].each { t -> // BiFunction uses R, T and U
            assertScript """
                def <$t> $t applyFunction(java.util.function.BiFunction<Date, URL, $t> action) {
                    $t result = action.apply(new Date(), new URL('https://www.example.com'))
                    return result
                }

                // GroovyCastException: Cannot cast object 'foo' with class 'java.lang.String' to class 'java.util.Date'
                java.util.function.BiFunction<Date, URL, String> func = { Date d, URL u -> 'foo' }
                def result = applyFunction(func)
                assert result == 'foo'
            """
        }
    }

    // GROOVY-8409, GROOVY-10067
    void testReturnTypeInferenceWithMethodGenerics15() {
        shouldFailWithMessages '''
            List<CharSequence> list = ['x'].collect() // GROOVY-10074
        ''',
        'Incompatible generic argument types. Cannot assign java.util.List<java.lang.String> to: java.util.List<java.lang.CharSequence>'

        shouldFailWithMessages '''
            List<CharSequence> list = ['x'].stream().toList() // TODO: fix type param bound of StreamGroovyMethods#toList(Stream<T>)
        ''',
        'Incompatible generic argument types. Cannot assign java.util.List<java.lang.String> to: java.util.List<java.lang.CharSequence>'

        assertScript '''
            import static java.util.stream.Collectors.toList
            List<CharSequence> list = ['x'].stream().collect(toList())
        '''
    }

    // GROOVY-10098
    void testReturnTypeInferenceWithMethodGenerics16() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T extends Number> {
              T p
              T m() {
                Closure<T> x = { -> p }
                x() // Cannot return value of type Object on method returning type T
              }
            }
            assert new C<>(42).m() == 42
        '''
    }

    // GROOVY-8638
    void testReturnTypeInferenceWithMethodGenerics17() {
        assertScript '''
            @Grab('com.google.guava:guava:30.1.1-jre')
            import com.google.common.collect.*

            ListMultimap<String, Integer> mmap = ArrayListMultimap.create()

            Map<String, Collection<Integer>> map = mmap.asMap()
            Set<Map.Entry<String, Collection<Integer>>> set = map.entrySet()
            Iterator<Map.Entry<String, Collection<Integer>>> it = set.iterator()
            while (it.hasNext()) { Map.Entry<String, Collection<Integer>> entry = it.next()
                Collection<Integer> values = entry.value
            }
        '''
    }

    void testDiamondInferrenceFromConstructor1() {
        assertScript '''
            class Foo<U> {
                U method() { }
            }
            Foo<Integer> foo = new Foo<>()
            Integer result = foo.method()
        '''
    }

    void testDiamondInferrenceFromConstructor2() {
        assertScript '''
            Set<Long> set = new HashSet<>()
        '''
    }

    void testDiamondInferrenceFromConstructor3() {
        assertScript '''
            new HashSet<>(Arrays.asList(0L))
        '''

        assertScript '''
            Set<Number> set = new HashSet<>(Arrays.asList(0L))
        '''

        // not diamond inference, but tests compatible assignment
        assertScript '''
            Set<Number> set = new HashSet<Number>(Arrays.asList(0L))
        '''

        assertScript '''
            Set<? super Number> set = new HashSet<>(Arrays.asList(0L))
        '''

        assertScript '''
            Set<? extends Number> set = new HashSet<>(Arrays.asList(0L))
        '''
    }

    // GROOVY-7419
    void testDiamondInferrenceFromConstructor4() {
        assertScript '''
            Map<Thread.State, Object> map = new EnumMap<>(Thread.State)
            assert map.size() == 0
            assert map.isEmpty()
        '''
    }

    // GROOVY-6232
    void testDiamondInferrenceFromConstructor5() {
        ['"a",new Object()', 'new Object(),"b"', '"a","b"'].each { args ->
            assertScript """
                class C<T> {
                    C(T x, T y) {
                    }
                }
                C<Object> c = new C<>($args)
            """
        }
    }

    // GROOVY-9948
    void testDiamondInferrenceFromConstructor6() {
        assertScript '''
            class C<T> {
                T p
                C(T p) {
                    this.p = p
                }
            }

            C<Integer> c = new C<>(1)
            assert c.p < 10
        '''
    }

    // GROOVY-9948
    void testDiamondInferrenceFromConstructor7() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }

            C<Integer> c = new C<>(1)
            assert c.p < 10
        '''
    }

    // GROOVY-9984
    void testDiamondInferrenceFromConstructor7a() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }

            C<Integer> c = new C<>(null)
            assert c.p === null
        '''
    }

    // GROOVY-9956
    void testDiamondInferrenceFromConstructor8() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            interface I { }
            class D implements I { }

            C<I> ci = new C<I>(new D())
            ci = new C<>(new D()) // infers C<D> on RHS
        '''
    }

    // GROOVY-9996
    void testDiamondInferrenceFromConstructor8a() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            interface I { }
            class D implements I { }
            void test(C<I> c) { assert c.p instanceof D }

            I i = new D() // infers D for "i"
            def ci = new C<>(i) // infers C<D> for "ci"
            test(ci)
        '''
    }

    // GROOVY-10011
    void testDiamondInferrenceFromConstructor8b() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            interface I { }
            class D implements I { }

            void test(I i) {
                if (i instanceof D) {
                    C<D> cd = new C<>(i)
                }
            }
            test(new D())
        '''
    }

    void testDiamondInferrenceFromConstructor9() {
        assertScript '''
            abstract class A<X> { }
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> extends A<T> {
                T p
            }
            interface I { }
            class D implements I { }

            A<I> ai = new C<>(new D())
        '''

        shouldFailWithMessages '''
            abstract class A<X> { }
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> extends A<T> {
                T p
            }
            interface I { }
            class D implements I { }

            A<String> ax = new C<>(new D())
        ''', 'Incompatible generic argument types. Cannot assign C<D> to: A<java.lang.String>'

        shouldFailWithMessages '''
            Set<List<String>> strings = new HashSet<>([new ArrayList<Number>()])
        ''', 'Incompatible generic argument types. Cannot assign java.util.HashSet<java.util.ArrayList<java.lang.Number>> to: java.util.Set<java.util.List<java.lang.String>>'
    }

    // GROOVY-9972
    void testDiamondInferrenceFromConstructor9a() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            class D {
                public String f = 'D#f'
            }
            C<D> cd = true ? new C<>(new D()) : new C<>(new D())
            assert cd.p.f.toLowerCase() == 'd#f'
        '''

        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            class D {
                public String f = 'D#f'
            }
            boolean b = false
            C<D> cd = b ? new C<>(new D()) : (b ? new C<>((D) null) : new C<>(new D()))
            assert cd.p.f.toLowerCase() == 'd#f'
        '''

        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            class D {
                public String f = 'D#f'
            }
            def cd
            if (true) {
                cd = new C<>(new D())
            } else {
                cd = new C<>((D) null)
            }
            assert cd.p.f.toLowerCase() == 'd#f'
        '''

        assertScript '''
            @groovy.transform.TupleConstructor
            class C {
                List<D> list
            }
            class D {
            }
            List test(C... array) {
                // old code used "List<D> many" as target for guts of closure
                List<D> many = array.collectMany { it.list ?: [] }
            }
            def result = test(new C(), new C(list:[new D()]))
            assert result.size() == 1
        '''
    }

    void testDiamondInferrenceFromConstructor9b() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            class D {
                public String f = 'D#f'
            }
            def foo = { flag, C<D> cd = (flag ? new C<>(new D()) : new C<>(new D())) ->
                cd.p.f.toLowerCase()
            }
            assert foo.call(true) == 'd#f'
        '''

        shouldFailWithMessages '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            class D {
                public String f = 'D#f'
            }
            def foo = { flag, C<D> cd = (flag ? new C<>(new D()) : new C<>(new Object())) ->
                cd.p.f.toLowerCase()
            }
        ''', 'Incompatible generic argument types. Cannot assign C<? extends java.lang.Object> to: C<D>'
    }

    // GROOVY-9963
    void testDiamondInferrenceFromConstructor10() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            static void m(String s) {
                assert s.isEmpty()
            }
            m(new C<>("").p)
        '''
    }

    // GROOVY-10080
    void testDiamondInferrenceFromConstructor11() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            class D {
                int m(Object[] objects) {
                    42
                }
            }
            def closure = { ->
                new C<>(new D())
            }
            def result = closure().p.m(new BigDecimal[0]) // Cannot find matching method Object#m(...)
            assert result == 42
        '''
    }

    // GROOVY-10086
    void testDiamondInferrenceFromConstructor12() {
        shouldFailWithMessages '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            class D {
            }
            void m(int i, C<D>... zeroOrMore) {
                D d = zeroOrMore[0].p
            }
            m(0, new C<>(1))
        ''',
        'Cannot call', 'm(int, C<D>[]) with arguments [int, C<java.lang.Integer>]'
    }

    // GROOVY-9970
    void testDiamondInferrenceFromConstructor13() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class A<T extends B> {
                T p
            }
            class B {
            }
            class C<T extends Number> {
                void test(T n) {
                    A<B> x = new A<>(new B())
                    def closure = { ->
                        A<B> y = new A<>(new B())
                    }
                    closure.call()
                }
            }
            new C<Long>().test(42L)
        '''
    }

    // GROOVY-9983
    void testDiamondInferrenceFromConstructor14() {
        String types = '''
            @groovy.transform.TupleConstructor(defaults=false)
            class A<T> {
                T p
            }
            class B {
            }
            class C {
                static m(A<B> a_of_b) {
                }
            }
        '''

        assertScript types + '''
            class E extends B {
            }
            boolean flag = true

            A<B> v = new A<>(null)
            A<B> w = new A<>(new B())
            A<B> x = new A<>(new E())
            A<B> y = flag ? new A<>(new B()) : new A<>(new B())
            A<B> z = flag ? new A<>(new B()) : new A<>(new E())

            C.m(new A<>(null))
            C.m(new A<>(new B()))
            C.m(new A<>(new E()))
            C.m(flag ? new A<>(new B()) : new A<>(new B()))
            C.m(flag ? new A<>(new B()) : new A<>(new E())) // Cannot call m(A<B>) with arguments [A<? extends B>]
        '''

        shouldFailWithMessages types + '''
            A<B> x = new A<>(new Object())
            A<B> y = true ? new A<>(new B()) : new A<>(new Object())

            C.m(new A<>(new Object()))
            C.m(true ? new A<>(new B()) : new A<>(new Object()))
        ''',
        'Cannot assign A<java.lang.Object> to: A<B>',
        'Cannot assign A<? extends java.lang.Object> to: A<B>',
        'Cannot call C#m(A<B>) with arguments [A<java.lang.Object>]',
        'Cannot call C#m(A<B>) with arguments [A<? extends java.lang.Object>]'
    }

    // GROOVY-9995
    void testDiamondInferrenceFromConstructor15() {
        [
            ['Closure<A<Long>>', 'java.util.concurrent.Callable<A<Long>>'],
            ['new A<>(42L)', 'return new A<>(42L)']
        ].combinations { functionalType, returnStmt ->
            assertScript """
                @groovy.transform.TupleConstructor(defaults=false)
                class A<T extends Number> {
                    T p
                }
                $functionalType callable = { ->
                    $returnStmt
                }
                Long n = callable.call().p
                assert n == 42L
            """
        }
    }

    void testLinkedListWithListArgument() {
        assertScript '''
            List<String> list = new LinkedList<String>(['1','2','3'])
        '''
    }

    void testLinkedListWithListArgumentAndWrongElementTypes() {
        shouldFailWithMessages '''
            List<String> list = new LinkedList<String>([1,2,3])
        ''', 'Cannot call java.util.LinkedList#<init>(java.util.Collection<? extends java.lang.String>) with arguments [java.util.List<java.lang.Integer>]'
    }

    void testGenericAssignmentWithSubClass() {
        assertScript '''
            List<String> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        '''
    }

    void testGenericAssignmentWithSubClassAndWrongGenericType() {
        shouldFailWithMessages '''
            List<Integer> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        ''', 'Incompatible generic argument types'
    }

    void testAddShouldBeAllowedOnUncheckedGenerics() {
        assertScript '''
            List list = []
            list.add 'Hello'
            list.add 2
            list.add 'the'
            list.add 'world'
        '''
    }

    void testAssignmentShouldFailBecauseOfLowerBound() {
        shouldFailWithMessages '''
            List<? super Number> list = ['string']
        ''', 'Number'
    }

    // GROOVY-9914, GROOVY-10036
    void testAssignmentShouldWorkForParameterizedMap() {
        assertScript '''
            Map test(Map<String,String> one) {
                Map<String,Integer> two = one.collectEntries { k,v ->
                    [(k): v.hashCode()]
                }
            }
            assert test(foo:'bar').containsKey('foo')
        '''

        assertScript '''
            def list = ['foo','bar','baz']
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.Map<java.lang.String, java.lang.Object>'
            })
            def map = list.<String,Object,String>collectEntries {
                [(it): it.hashCode()]
            }
        '''
    }

    // GROOVY-9555
    void testAssignmentShouldWorkForProperUpperBound() {
        assertScript '''
            interface I<T> {
            }
            class C implements I<Object> {
            }
            interface Factory {
                def <T extends I> T getInstance(Class<T> clazz)
            }

            void test(Factory f) {
                C c = f.getInstance(C)
                assert c instanceof C
                assert c instanceof I
            }
            test { Class clazz -> clazz.newInstance() }
        '''

        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [stubDir: File.createTempDir()]
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'Face.java')
            a.write '''
                public interface Face<T> {
                }
            '''
            def b = new File(parentDir, 'Impl.java')
            b.write '''
                public class Impl implements Face<Object> {
                }
            '''
            def c = new File(parentDir, 'Supplier.java')
            c.write '''
                public interface Supplier {
                    public <T extends Face> T getInstance(Class<T> clazz);
                }
            '''
            def d = new File(parentDir, 'Tester.groovy')
            d.write '''
                void test(Supplier s) {
                    def x = s.getInstance(Impl)
                    assert x instanceof Impl
                    assert x instanceof Face
                }
                test { Class clazz -> clazz.newInstance() }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d)
            cu.compile()

            loader.loadClass('Tester').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    void testGroovy5154() {
        assertScript '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    FooBound fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            new Foo()
        '''
    }

    void testGroovy5154WithSubclass() {
        assertScript '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    FooBound2 fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            class FooBound2 extends FooBound {}
            new Foo()
        '''
    }

    void testGroovy5154WithIncorrectType() {
        shouldFailWithMessages '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    Object fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            new Foo()
        ''', 'Cannot find matching method FooWithGenerics#say(java.lang.Object)'
    }

    // GROOVY-5237
    void testGenericTypeArgumentAsField() {
        assertScript '''
            class Container<T> {
                private T initialValue
                Container(T initialValue) { this.initialValue = initialValue }
                T get() { initialValue }
            }
            Container<Date> c = new Container<Date>(new Date())
            long time = c.get().time
        '''
    }

    // GROOVY-9860
    void testGenericTypeArgumentInCtorCall() {
        assertScript '''
            def <T> void test() {
                def bind = { T a, T b ->
                    new Tuple2<T, T>(a, b)
                }
                assert bind('foo', 'bar').toString() == '[foo, bar]'
            }
            test()
        '''
    }

    void testReturnAnntationClass() {
        assertScript '''
            import java.lang.annotation.Documented
            Documented annotation = Deprecated.getAnnotation(Documented)
        '''
    }

    void testReturnListOfParameterizedType() {
        assertScript '''
            class A {}
            class B extends A { void bar() {} }
            public <T extends A> List<T> foo() { [] }

            List<B> list = foo()
            list.add(new B())
        '''
    }

    // GROOVY-6126
    void testChoosesCorrectMethodOfParameterizedReturnType() {
        assertScript '''
            class Loader {
                public <T> T load(Class<T> entityClass, Serializable id) {entityClass.newInstance()}
                public void load(final Object entity, final Serializable id) {}
            }

            class MyClass<D> {
                Class<D> persistentClass
                Loader hibernateTemplate = new Loader()
                MyClass(Class<D> c) {
                    this.persistentClass = c
                }

                D load(Serializable id) {
                    id = convertIdentifier(id)
                    if (id != null) {
                        return hibernateTemplate.load(persistentClass, id)
                    }
                }

                Serializable convertIdentifier(Serializable s) {"1"}
            }
            class Foo{}

            MyClass<Foo> mc = new MyClass(Foo)
            Foo foo = mc.load("2")'''
    }

    void testMethodCallWithClassParameterUsingClassLiteralArg() {
        assertScript '''
            class A {}
            class B extends A {}
            class Foo {
                void m(Class<? extends A> clazz) {}
            }
            new Foo().m(B)
        '''
    }

    void testMethodCallWithClassParameterUsingClassLiteralArgWithoutWrappingClass() {
        assertScript '''
            class A {}
            class B extends A {}
            void m(Class<? extends A> clazz) {}
            m(B)
        '''
    }

    // GROOVY-7945
    void testSpecialCtorCallWithClassLiteral() {
        shouldFailWithMessages '''
            abstract class A<X, Y> {
                private final Class<X> x
                private final Class<Y> y

                A(Class<X> x, Class<Y> y) {
                    this.x = x
                    this.y = y
                }

                void test() {
                    println("{$x} and {$y}")
                }
            }

            class B extends A<String, Integer> {
                B() {
                    super(Integer, String)
                }
            }

            A<String, Integer> a = new B()
            a.test()
        ''', 'Cannot call A#<init>(java.lang.Class<java.lang.String>, java.lang.Class<java.lang.Integer>) with arguments [java.lang.Class<java.lang.Integer>, java.lang.Class<java.lang.String>]'
    }

    // GROOVY-9460
    void testMethodCallWithClassParameterUnbounded() {
        assertScript '''
            class Bar {
                static void baz(Class<?> target) {
                }
            }
            class Foo<D> { // cannot be "T" because that matches type parameter in Class
                void test(Class<D> param) {
                    Bar.baz(param) // Cannot call Bar#baz(java.lang.Class<?>) with arguments [java.lang.Class<D>]
                }
            }
            new Foo<String>().test(String.class)
        '''
    }

    void testConstructorCallWithClassParameterUsingClassLiteralArg() {
        assertScript '''
            class A {}
            class B extends A {}
            class C extends B {}
            class Foo {
                Foo(Class<? extends A> clazz) {}
            }
            new Foo(B)
            new Foo(C)
        '''
    }

    void testConstructorCallWithClassParameterUsingClassLiteralArgAndInterface() {
        assertScript '''
            interface A {}
            class B implements A {}
            class C extends B {}
            class Foo {
                Foo(Class<? extends A> clazz) {}
            }
            new Foo(B)
            new Foo(C)
        '''
    }

    void testPutMethodWithPrimitiveValue() {
        assertScript '''
            def map = new HashMap<String, Integer>()
            map.put('hello', 1)
        '''
    }

    void testPutAtMethodWithPrimitiveValue() {
        assertScript '''
            def map = new HashMap<String, Integer>()
            map['hello'] = 1
        '''
    }

    void testPutMethodWithWrongValueType() {
        shouldFailWithMessages '''
            def map = new HashMap<String, Integer>()
            map.put('hello', new Object())
        ''',
        'Cannot find matching method java.util.HashMap#put(java.lang.String, java.lang.Object). Please check if the declared type is correct and if the method exists.'
    }

    void testPutAtMethodWithWrongValueType() {
        shouldFailWithMessages '''
            def map = new HashMap<String, Integer>()
            map['hello'] = new Object()
        ''',
        'Cannot call <K,V> java.util.HashMap#putAt(java.lang.String, java.lang.Integer) with arguments [java.lang.String, java.lang.Object]'
    }

    // GROOVY-9069
    void testPutAtMethodWithWrongValueType2() {
        shouldFailWithMessages '''
            class ConfigAttribute {
            }
            void test(Map<String, Map<String, List<String>>> maps) {
                maps.each { String key, Map<String, List<String>> map ->
                    Map<String, List<ConfigAttribute>> value = [:] // Why "List<ConfigAttribute>"?
                    // populate value
                    maps[key] = value
                    maps.put(key, value)
                }
            }
        ''',
        'Cannot call <K,V> java.util.Map#putAt(java.lang.String, java.util.Map<java.lang.String, java.util.List<java.lang.String>>) with arguments [java.lang.String, java.util.LinkedHashMap<java.lang.String, java.util.List<ConfigAttribute>>]',
        'Cannot find matching method java.util.Map#put(java.lang.String, java.util.LinkedHashMap<java.lang.String, java.util.List<ConfigAttribute>>). Please check if the declared type is correct and if the method exists.'
    }

    void testPutAtMethodWithWrongValueType3() {
        assertScript '''
            void test(Map<String, Map<String, List<String>>> maps) {
                maps.each { String key, Map<String, List<String>> map ->
                    Map<String, List<String>> value = [:]
                    // populate value
                    maps[key] = value
                    maps.put(key, value)
                }
            }
        '''
    }

    void testShouldComplainAboutToInteger() {
        String code = '''
            class Test {
                static test2() {
                    if (new Random().nextBoolean()) {
                        def a = new ArrayList<String>()
                        a << "a" << "b" << "c"
                        return a
                    } else {
                        def b = new LinkedList<Number>()
                        b << 1 << 2 << 3
                        return b
                    }
                }

                static test() {
                    def result = test2()
                    result[0].toInteger()
                    //result[0].toString()
                }
            }
            new Test()
        '''

        shouldFailWithMessages code,
                'Cannot find matching method java.lang.Object#getAt(int)',
                'Cannot find matching method java.lang.Object#toInteger()'
    }

    void testAssignmentOfNewInstance() {
        assertScript '''
            class Foo {
                static Class clazz = Date
                public static void main(String... args) {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        assert node.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                    })
                    def obj = clazz.newInstance()
                }
            }
        '''
    }

    // GROOVY-5415
    void testShouldUseMethodGenericType1() {
        assertScript '''import groovy.transform.stc.GenericsSTCTest.ClassA
        class ClassB {
            void bar() {
                def ClassA<Long> a = new ClassA<Long>();
                a.foo(this.getClass());
            }
        }
        new ClassB()
        '''
    }

    // GROOVY-5415
    void testShouldUseMethodGenericType2() {
        shouldFailWithMessages '''import groovy.transform.stc.GenericsSTCTest.ClassA
        class ClassB {
            void bar() {
                def ClassA<Long> a = new ClassA<Long>();
                a.bar(this.getClass());
            }
        }
        new ClassB()
        ''', 'Cannot call <X> groovy.transform.stc.GenericsSTCTest$ClassA#bar(java.lang.Class<java.lang.Long>) with arguments [java.lang.Class<? extends java.lang.Object>]'
    }

    // GROOVY-8961, GROOVY-9915
    void testShouldUseMethodGenericType3() {
        ['', 'static'].each { mods ->
            assertScript """
                $mods void setX(List<String> strings) {
                }
                void test() {
                    x = Collections.emptyList()
                }
                test()
            """
            assertScript """
                $mods void setX(Collection<String> strings) {
                }
                void test() {
                    x = Collections.emptyList()
                }
                test()
            """
            assertScript """
                $mods void setX(Iterable<String> strings) {
                }
                void test() {
                    x = Collections.emptyList()
                }
                test()
            """

            shouldFailWithMessages """
                $mods void setX(List<String> strings) {
                }
                void test() {
                    x = Collections.<Integer>emptyList()
                }
            """, 'Incompatible generic argument types. Cannot assign java.util.List<java.lang.Integer> to: java.util.List<java.lang.String>'
        }
    }

    // GROOVY-9734, GROOVY-9915
    void testShouldUseMethodGenericType4() {
        ['', 'static'].each { mods ->
            assertScript """
                $mods void m(List<String> strings) {
                }
                void test() {
                    m(Collections.emptyList())
                }
                test()
            """
            assertScript """
                $mods void m(Collection<String> strings) {
                }
                void test() {
                    m(Collections.emptyList())
                }
                test()
            """
            assertScript """
                $mods void m(Iterable<String> strings) {
                }
                void test() {
                    m(Collections.emptyList())
                }
                test()
            """
        }
    }

    // GROOVY-9751
    void testShouldUseMethodGenericType5() {
        assertScript '''
            interface Service {
                Number transform(String s)
            }
            void test(Service service) {
                Set<Number> numbers = []
                List<String> strings = ['1','2','3']
                numbers.addAll(strings.collect(service.&transform))
            }
            test({ String s -> Integer.valueOf(s) } as Service)
        '''
    }

    void testShouldUseMethodGenericType6() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type.equals(CLOSURE_TYPE)
                assert type.getGenericsTypes()[0].getType().equals(STRING_TYPE)
            })
            def ptr = "".&toString
        '''
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type.equals(CLOSURE_TYPE)
                assert type.getGenericsTypes()[0].getType().equals(Double_TYPE)
            })
            def ptr = Math.&abs
        '''
    }

    // GROOVY-9762
    void testShouldUseMethodGenericType7() {
        for (toList in ['{ list(it) }', 'o -> list(o)', 'this.&list', 'this::list']) {
            assertScript """
                def <T> List<T> list(T item) {
                    return [item]
                }
                def test() {
                    Optional<Integer> opt = Optional.ofNullable(1)
                    List<Integer> ret = opt.map($toList).get()
                    return ret
                }
                assert test() == [1]
            """
        }
    }

    // GROOVY-9803
    void testShouldUseMethodGenericType8() {
        assertScript '''
            def opt = Optional.of(42)
                .map(Collections::singleton)
                .map(x -> x.first().intValue()) // Cannot find matching method java.lang.Object#intValue()
            assert opt.get() == 42
        '''
        // same as above but with separate type parameter name for each location
        ['D.&wrap', 'Collections.&singleton', '{x -> [x].toSet()}', '{Collections.singleton(it)}'].each { toSet ->
            assertScript """
                abstract class A<I,O> {
                    abstract O apply(I input)
                }
                class C<T> {
                    static <U> C<U> of(U item) {
                        new C<U>()
                    }
                    def <V> C<V> map(A<? super T, ? super V> func) {
                        new C<V>()
                    }
                }
                class D {
                    static <W> Set<W> wrap(W o) {
                    }
                }

                void test() {
                    def c = C.of(42)
                    def d = c.map($toSet)
                    def e = d.map(x -> x.first().intValue())
                }

                test()
            """
        }
    }

    // GROOVY-9945
    void testShouldUseMethodGenericType9() {
        assertScript '''
            interface I<T> {
            }
            class A<T> implements I<String> {
                def m(T t) { 'works' }
            }
            class B<T> extends A<T> {
            }

            def bee = new B<Float>()
            def obj = bee.m(3.14f)
            assert obj == 'works'
        '''
    }

    // GROOVY-8409, GROOVY-9915
    void testShouldUseMethodGenericType10() {
        assertScript '''
            interface OngoingStubbing<T> /*extends IOngoingStubbing*/ {
                // type parameter from enclosing type in signature:
                OngoingStubbing<T> thenReturn(T value)
            }
            static <T> OngoingStubbing<T> when(T methodCall) {
                [thenReturn: { T value -> null }] as OngoingStubbing<T>
            }
            Optional<String> foo() {
            }

            when(foo()).thenReturn(Optional.empty())
        '''
    }

    // GROOVY-8409, GROOVY-9902
    void testShouldUseMethodGenericType11() {
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [stubDir: File.createTempDir()]
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'Main.groovy')
            a.write '''
                def obj = new Pojo()
                Foo raw = obj.getFoo('')
                raw.bar = raw.baz // Cannot assign value of type Object to variable of type R
            '''
            def b = new File(parentDir, 'Pojo.java')
            b.write '''
                public class Pojo {
                    public <R extends I> Foo<R> getFoo(String key) {
                        return new Foo<>();
                    }
                }
            '''
            def c = new File(parentDir, 'Types.groovy')
            c.write '''
                interface I {
                }
                class Foo<T extends I> {
                    T bar
                    T baz
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    // GROOVY-10088
    void testShouldUseMethodGenericType12() {
        shouldFailWithMessages '''
            class C<T> {
                void setP(T t) { }
            }
            class D<X> extends C<X> {
            }

            new D<Number>().p = 'x'
        ''',
        'Cannot assign value of type java.lang.String to variable of type java.lang.Number'
    }

    // GROOVY-5516
    void testAddAllWithCollectionShouldBeAllowed() {
        assertScript '''import org.codehaus.groovy.transform.stc.ExtensionMethodNode
            List<String> list = ['a','b','c']
            Collection<String> strings = list.findAll { it }
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def dmt = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert dmt.declaringClass.implementsInterface(LIST_TYPE)
                assert dmt instanceof ExtensionMethodNode == false
                assert dmt.name == 'addAll'
            })
            boolean r = list.addAll(strings)
        '''
    }

    void testAddAllWithCollectionShouldNotBeAllowed() {
        shouldFailWithMessages '''
            List<String> list = ['a','b','c']
            Collection<Integer> numbers = (Collection<Integer>) [1,2,3]
            boolean r = list.addAll(numbers)
        ''', 'Cannot call java.util.ArrayList#addAll(java.util.Collection<? extends java.lang.String>) with arguments [java.util.Collection<java.lang.Integer>]'
    }

    // GROOVY-5528
    void testAssignmentToInterfaceFromUserClassWithGenerics() {
        assertScript '''
            class UserList<T> extends LinkedList<T> {}
            List<String> list = new UserList<String>()
        '''
    }

    // GROOVY-5559, GROOVY-10010
    void testGStringInListShouldNotBeConsideredAsAString() {
        String base = '''import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode as LUB
            def bar = 1
        '''

        assertScript base + '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == LIST_TYPE
                assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type instanceof LUB
            })
            def list = ["foo", "$bar"]
        '''

        shouldFailWithMessages base + '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == LIST_TYPE
                assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type instanceof LUB
            })
            List<String> list = ["foo", "$bar"]
        ''', 'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == LIST_TYPE
                assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type == GSTRING_TYPE // single element means no LUB
            })
            List<String> list = ["$bar"]
        ''', 'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            void m(List<String> list) {}
            m(["foo", "$bar"])
        ''', 'You are trying to use a GString in place of a String'
    }

    // GROOVY-5559, GROOVY-10010
    void testGStringInMapShouldNotBeConsideredAsAString() {
        String base = 'def bar = 123'

        shouldFailWithMessages base + '''
            Map<String,String> map = [x:"foo", y:"$bar"]
        ''', 'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            void m(Map<?,String> map) {}
            m([x:"foo", y:"$bar"])
        ''', 'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            void m(Map<?,String> map) {}
            m(x:"foo", y:"$bar")
        ''', 'You are trying to use a GString in place of a String'
    }

    // GROOVY-5559: related behaviour
    void testGStringVsString() {
        assertScript '''
            int i = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == GSTRING_TYPE
            })
            def s = "i=$i"
            assert s == 'i=1'
        '''
    }

    // GROOVY-5594
    void testMapEntryUsingPropertyNotation() {
        assertScript '''
        Map.Entry<Date, Integer> entry = null

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == make(Date)
        })
        def k = entry?.key

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
        })
        def v = entry?.value
        '''
    }

    void testInferenceFromMap() {
        assertScript '''
        Map<Date, Integer> map = [:]

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            def infType = node.getNodeMetaData(INFERRED_TYPE)
            assert infType == make(Set)
            def entryInfType = infType.genericsTypes[0].type
            assert entryInfType == make(Map.Entry)
            assert entryInfType.genericsTypes[0].type == make(Date)
            assert entryInfType.genericsTypes[1].type == Integer_TYPE
        })
        def entries = map?.entrySet()
        '''
    }

    void testInferenceFromListOfMaps() {
        assertScript '''
        List<Map<Date, Integer>> maps = []

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            def listType = node.getNodeMetaData(INFERRED_TYPE)
            assert listType == Iterator_TYPE
            def infType = listType.genericsTypes[0].type
            assert infType == make(Map)
            assert infType.genericsTypes[0].type == make(Date)
            assert infType.genericsTypes[1].type == Integer_TYPE
        })
        def iter = maps?.iterator()
        '''
    }

    void testAssignNullMapWithGenerics() {
        assertScript '''
            Map<String, Integer> foo = null
            Integer result = foo?.get('a')
            assert result == null
        '''
    }

    void testAssignNullListWithGenerics() {
        assertScript '''
            List<Integer> foo = null
            Integer result = foo?.get(0)
            assert result == null
        '''
    }

    void testAssignNullListWithGenericsWithSequence() {
        assertScript '''
            List<Integer> foo = [1]
            foo = null
            Integer result = foo?.get(0)
            assert result == null
        '''
    }

    // GROOVY-10107
    void testAssignNullTypeParameterWithUpperBounds() {
        assertScript '''
            class C<T extends Number> {
                void m() {
                    T n = null
                }
            }
            new C<Long>().m()
        '''
    }

    void testMethodCallWithArgumentUsingNestedGenerics() {
        assertScript '''
           ThreadLocal<Map<Integer, String>> cachedConfigs = new ThreadLocal<Map<Integer, String>>()
           def configs = new HashMap<Integer, String>()
           cachedConfigs.set configs
        '''
    }

    void testInferDiamondUsingAIC() {
        shouldFailWithMessages '''
            Map<String,Date> map = new HashMap<>() {}
        ''', 'Cannot use diamond <> with anonymous inner classes'
    }

    // GROOVY-5614
    void testInferDiamondForFields() {
        assertScript '''
            class Rules {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.initialExpression.getNodeMetaData(INFERRED_TYPE)
                    assert type == make(HashMap)
                    assert type.genericsTypes.length == 2
                    assert type.genericsTypes[0].type == Integer_TYPE
                    assert type.genericsTypes[1].type == make(Date)
                })

                final Map<Integer, Date> bindings1  = new HashMap<>();
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.initialExpression.getNodeMetaData(INFERRED_TYPE)
                    assert type == make(HashMap)
                    assert type.genericsTypes.length == 2
                    assert type.genericsTypes[0].type == STRING_TYPE
                    assert type.genericsTypes[1].type == STRING_TYPE
                })
                final Map<String, String> bindings2 = new HashMap<>();
            }
            def r = new Rules()

            r.bindings1[3] = new Date()
            assert r.bindings1.containsKey(3)

            r.bindings2['a'] = 'A'
            r.bindings2.put('b', 'B')

        '''
    }
    void testInferDiamondForAssignment() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == STRING_TYPE
                assert type.genericsTypes[1].type == STRING_TYPE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == STRING_TYPE
                assert type.genericsTypes[1].type == STRING_TYPE
            })
            Map<String, String> map = new HashMap<>()
        '''
    }
    void testInferDiamondForAssignmentWithDates() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
        '''
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalKeyUsingPut() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map.put('foo', new Date())
        ''', '[Static type checking] - Cannot find matching method java.util.HashMap#put(java.lang.String, java.util.Date). Please check if the declared type is correct and if the method exists.'
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalKeyUsingSquareBracket() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map['foo'] = new Date()
        ''', 'Cannot call <K,V> java.util.HashMap#putAt(java.util.Date, java.util.Date) with arguments [java.lang.String, java.util.Date]'
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalValueUsingPut() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map.put(new Date(), 'foo')
        ''', '[Static type checking] - Cannot find matching method java.util.HashMap#put(java.util.Date, java.lang.String). Please check if the declared type is correct and if the method exists.'
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalValueUsingSquareBracket() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map[new Date()] = 'foo'
        ''', 'Cannot call <K,V> java.util.HashMap#putAt(java.util.Date, java.util.Date) with arguments [java.util.Date, java.lang.String]'
    }

    void testCallMethodWithParameterizedArrayList() {
        assertScript '''
        class MyUtility {
            def methodOne() {
                def someFiles = new ArrayList<File>()
                def someString = ''
                methodTwo someString, someFiles
            }

            def methodTwo(String s, List<File> files) {}
        }
        new MyUtility()
        '''
    }

    void testGenericTypeArrayOfDGMMethod() {
        assertScript '''
            int[] arr = [0,1,2,3]
            assert arr.findAll() == [1,2,3]
        '''
    }

    // GROOVY-5617
    void testIntermediateListAssignmentOfGStrings() {
        assertScript '''
        def test() {
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.implementsInterface(LIST_TYPE)
                assert type.genericsTypes.length == 1
                assert type.genericsTypes[0].type == GSTRING_TYPE
            })
            List<GString> dates = ["${new Date()-1}", "${new Date()}", "${new Date()+1}"]
            dates*.toUpperCase()
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.implementsInterface(LIST_TYPE)
                assert type.genericsTypes.length == 1
                assert type.genericsTypes[0].type == GSTRING_TYPE
            })
            List<GString> copied = []
            copied.addAll(dates)
            List<String> upper = copied*.toUpperCase()
        }
        test()
        '''
    }

    // GROOVY-5650
    void testRegressionInGenericsTypeInference() {
        assertScript '''import groovy.transform.stc.GenericsSTCTest.JavaClassSupport as JavaClass
        List<JavaClass.StringContainer> containers = new ArrayList<>();
        containers.add(new JavaClass.StringContainer());
        List<String> strings = JavaClass.unwrap(containers);
        '''
    }

    // GROOVY-7549, GROOVY-9847
    void testRegressionInGenericsTypeInference2() {
        assertScript '''
            class Test {
                @groovy.transform.ToString(excludes='children')
                private static class TreeAttr {
                    String name
                    Integer val = 0
                    List<TreeAttr> children = []
                }

                static main(args) {
                    new Test().test(1)
                }

                void test(Integer count) {
                    TreeAttr root = new TreeAttr(name:'foo')
                    List<TreeAttr> collector = root.children

                    for (name in ['bar','baz']) { // tokens in a path
                        def item = collector.find { it.name == name }
                        if (!item) {
                            item = new TreeAttr(name: name)
                            collector.add(item)
                        }
                        collector = item.children
                        if (count) item.val += count
                    }
                }
            }
        '''
    }

    // In Groovy, we do not throw warnings (in general) and in that situation, not for unchecked
    // assignments like in Java
    // In the following test, the LHS of the assignment uses generics, while the RHS does not.
    // As we have the concept of flow typing too, we are facing a problem: what inferred type is the RHS?
    void testUncheckedAssignment() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def ift = node.getNodeMetaData(INFERRED_TYPE)
                assert ift == make(List)
                assert ift.isUsingGenerics()
                def gts = ift.genericsTypes
                assert gts.length==1
                assert gts[0].type == STRING_TYPE
            })
            List<String> list = (List) null
        '''
    }

    void testUncheckedAssignmentWithSuperInterface() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def ift = node.getNodeMetaData(INFERRED_TYPE)
                assert ift == make(List)
                assert ift.isUsingGenerics()
                def gts = ift.genericsTypes
                assert gts.length==1
                assert gts[0].type == STRING_TYPE
            })
            Iterable<String> list = (List) null
        '''
    }

    // GROOVY-5692, GROOVY-10006
    void testCompatibleArgumentsForPlaceholders1() {
        assertScript '''
            def <T> T test(T one, T two) { }
            def result = test(1,"II")
        '''
        assertScript '''
            def <T> T test(T one, T two, T three) { }
            def result = test(1,"II",Class)
        '''
        assertScript '''
            def <T extends Number> T test(T one, T two) { }
            def result = test(1L,2G)
        '''
    }

    // GROOVY-5692
    void testCompatibleArgumentsForPlaceholders2() {
        assertScript '''
            def <T> boolean test(T one, List<T> many) { }
            test(1,["II","III"])
        '''
    }

    void testIncompatibleArgumentsForPlaceholders1() {
        shouldFailWithMessages '''
            def <T extends Number> T test(T one, T two) { }
            test(1,"II")
        ''',
        '#test(int, java.lang.String). Please check if the declared type is correct and if the method exists.'
    }

    // GROOVY-9902: incomplete generics should not stop type checking
    void testIncompatibleArgumentsForPlaceholders2() {
        shouldFailWithMessages '''
            class Holder<Unknown> {
                TypedProperty<Number, Unknown> numberProperty = prop(Number)
                TypedProperty<String, Unknown> stringProperty = prop(String)

                def <T> TypedProperty<T, Unknown> prop(Class<T> clazz) {
                    return new TypedProperty<T, Unknown>(clazz: clazz)
                }

                // Note: type argument of Holder cannot be supplied to value attribute of @DelegatesTo
                def <T> T of(@DelegatesTo(value=Holder, strategy=Closure.DELEGATE_FIRST) Closure<T> c) {
                    this.with(c)
                }
            }

            class TypedProperty<X, Y> {
                Class<X> clazz

                void eq(X x) {
                    assert x.class == clazz : "x.class is ${x.class} not ${clazz}"
                }
            }

            void test(Holder<Object> h) {
                h.stringProperty.eq("${0}") // STC error
                h.of { // <-- 2nd type parameter discarded
                    stringProperty.eq(1234) // expect STC error
                    numberProperty.eq("xx") // expect STC error
                }
            }

            test(new Holder<Object>())
        ''',
        'Cannot call TypedProperty#eq(java.lang.String) with arguments [groovy.lang.GString]',
        'Cannot find matching method TypedProperty#eq(int)', // chooseBestMethod removes "eq"
        'Cannot find matching method TypedProperty#eq(java.lang.String)'
    }

    // GROOVY-5748
    void testPlaceholdersAndWildcards() {
        assertScript '''
            interface IStack<T> {
                INonEmptyStack<T, ? extends IStack<T>> push(T x)
            }

            interface IEmptyStack<T> extends IStack<T> {
                INonEmptyStack<T, IEmptyStack<T>> push(T x)
            }

            interface INonEmptyStack<T, TStackBeneath extends IStack<T>> extends IStack<T> {
                T getTop()

                TStackBeneath pop()

                INonEmptyStack<T, INonEmptyStack<T, TStackBeneath>> push(T x)
            }

            class EmptyStack<T> implements IEmptyStack<T> {
                INonEmptyStack<T, IEmptyStack<T>> push(T x) {
                    new NonEmptyStack<T, IEmptyStack<T>>(x, this)
                }
            }

            class NonEmptyStack<T, TStackBeneath extends IStack<T>>
                    implements INonEmptyStack<T, TStackBeneath> {
                private final TStackBeneath stackBeneathTop;
                private final T top

                NonEmptyStack(T top, TStackBeneath stackBeneathTop) {
                    this.top = top
                    this.stackBeneathTop = stackBeneathTop
                }

                T getTop() {
                    top
                }

                TStackBeneath pop() {
                    stackBeneathTop
                }

                INonEmptyStack<T, INonEmptyStack<T, TStackBeneath>> push(T x) {
                    new NonEmptyStack<T, INonEmptyStack<T, TStackBeneath>>(x, this)
                }
            }

            final IStack<Integer> stack = new EmptyStack<Integer>()

            def oneInteger = stack.push(1)
            assert oneInteger.getTop() == 1

            def twoIntegers = stack.push(1).push(2)
            assert twoIntegers.getTop() == 2

            def oneIntegerAgain = stack.push(1).push(2).pop()
            assert oneIntegerAgain.getTop() == 1 // Cannot find matching method IStack#getTop()
        '''
    }

    // GROOVY-5758
    void testShouldNotForbidAssignmentToString() {
        assertScript '''
            class A {
                public String foo
            }
            new A().foo = new ArrayList()
        '''
    }

    // GROOVY-5735
    void testCorrespondingParameterType() {
        assertScript '''
        public <T> void someMethod (java.lang.Class<T> clazz, T object) {}

        void method() {
            List<String> list = null
            someMethod(java.util.List.class, list)
        }

        method()
        '''
    }

    void testCorrectlyBoundedByWildcardGenericParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<?>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        '''
    }

    void testCorrectlyBoundedByExtendsGenericParameterType1() {
        assertScript '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        '''
    }

    // GROOVY-8960
    void testCorrectlyBoundedByExtendsGenericParameterType2() {
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [stubDir: File.createTempDir()]
        }
        try {
            def a = new File(parentDir, 'aJavaClass.java')
            a.write '''
                import java.io.Serializable;
                public class aJavaClass<A extends Serializable>  {
                    public static <A extends Serializable> aJavaClass<A> create(A a) {
                        return new aJavaClass<>(a);
                    }
                    private aJavaClass(A a) {
                    }
                    public enum anEnum {
                        entry1;
                    }
                }
            '''
            def b = new File(parentDir, 'aGroovyClass.groovy')
            b.write '''
                import aJavaClass
                class aGroovyClass {
                    static main(args) {
                        def out = aJavaClass.create(aJavaClass.anEnum.entry1)
                        assert out != null
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('aGroovyClass').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    void testCorrectlyBoundedBySuperGenericParameterType1() {
        assertScript '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            Foo.bar([new Object()])
        '''
    }

    // GROOVY-8034
    void testCorrectlyBoundedBySuperGenericParameterType2() {
        assertScript '''
            class A<I, O> {
                def <IO extends A<? super O, ?>> IO andThen(IO next) { next }
            }

            def a1 = new A<String , Integer>()
            def a2 = new A<Integer, Double >()
            def a3 = new A<Double , String >()
            def a4 = new A<String , Double >()
            def a5 = new A<Number , Object >()

            a1.andThen(a2)
            a2.andThen(a3)
            a3.andThen(a4)
            a4.andThen(a5)

            a1.andThen(a2)
                .andThen(a3)
                .andThen(a4)
                .andThen(a5)
        '''
    }

    void testCorrectlyBoundedByExtendsPlaceholderParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<? extends String>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux(['abc'])
        '''
    }

    void testCorrectlyBoundedBySuperPlaceholderParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<? super Object>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux([new Object()])
        '''
    }

    void testCorrectlyBoundedSubtypeGenericParameterType() {
        assertScript '''
            class Foo {
                static <T extends Collection<? extends CharSequence>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        '''
    }

    void testOutOfBoundsByExtendsGenericParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            Foo.bar([new Object()])
        ''', 'Cannot call <T extends java.util.List<? extends java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.ArrayList<java.lang.Object>]'
    }

    void testOutOfBoundsBySuperGenericParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        ''', 'Cannot call <T extends java.util.List<? super java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.ArrayList<java.lang.String>]'
    }

    void testOutOfBoundsByExtendsPlaceholderParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<Object>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux([new Object()])
        ''', 'Cannot call <T extends java.util.List<? extends java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.List<java.lang.Object>]'
    }

    void testOutOfBoundsBySuperPlaceholderParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<String>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux(['abc'])
        ''', 'Cannot call <T extends java.util.List<? super java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.List<java.lang.String>]'
    }

    // GROOVY-5721
    void testExtractComponentTypeFromSubclass() {
        assertScript '''
        class MyList extends ArrayList<Double> {}

        List<Double> list1 = new ArrayList<Double>()
        list1 << 0.0d

        // OK
        Double d1 = list1.get(0)

        //---------------------------

        List<Double> list2 = new MyList()
        list2 << 0.0d

        //Groovyc: [Static type checking] - Cannot assign value of type java.lang.Object to variable of type java.lang.Double
        Double d2 = list2.get(0)

        //---------------------------

        MyList list3 = new MyList()
        list3 << 0.0d

        //Groovyc: [Static type checking] - Cannot assign value of type java.lang.Object to variable of type java.lang.Double
        Double d3 = list3.get(0)
        '''

    }

    // GROOVY-5724
    void testJUnitHamcrest() {
        assertScript '''
            public class Matcher<T> {}
            public <T> void assertThat(T obj, Matcher<T> matcher) {}
            public <T> Matcher<T> notNullValue() {}
            String result = '12345'.substring(2)
            // assert
            assertThat(result, notNullValue())
        '''
    }

    // GROOVY-8103
    void testJUnitFestAssert() {
        assertScript '''
            import static Fluent.*
            import Util.Ours

            class Fluent {
                static FluentAPI  fluent(String s) { return new FluentAPI() }
                static <T extends FluentExtension> T fluent(T t) { return t }
            }

            class FluentAPI {
                FluentAPI isEqualTo(String s) { return this }
            }

            interface FluentExtension {
            }

            class Util {
                static class Ours implements FluentExtension {
                    Ours isSimilarTo(String json) { return this }
                }
                static Ours factory(String json) { new Ours() }
            }

            void test() {
                fluent('string').isEqualTo('x') // fine
                fluent(new Ours()).isSimilarTo('') // fine
                fluent(Util.factory('{}')).isSimilarTo('{"key":"val"}') // STC error
            }

            test()
        '''
    }

    // GROOVY-5836
    void testShouldFindMethodEvenIfUsingGenerics() {
        assertScript '''
            class Test<T> {
                void transform(boolean passThroughNulls, Closure<T> mapper) {}
                void transformAll(boolean passThroughNulls, Closure<T>... mappers) {
                    for (m in mappers) {
                        transform passThroughNulls, m
                    }
                }
            }
            new Test()
        '''
    }

    // GROOVY-5893
    void testPlusInClosure() {
        ['def', 'var', 'Object', 'Number', 'Integer', 'Comparable'].each { type ->
            assertScript """
                List<Integer> list = [1, 2, 3]

                int sum = 0
                list.each { int i -> sum = sum + i }
                assert sum == 6

                sum = 0
                list.each { int i -> sum += i }
                assert sum == 6

                $type sumWithInject = list.inject(0, { int x, int y -> x + y })
                //    ^^^^^^^^^^^^^ T ^^^^ E[]    ^ U      ^ U    ^ E  ^^^^^ V
                assert sumWithInject == 6
            """
        }
    }

    void testShouldNotCreateStackOverflow() {
        assertScript '''
            class Element {
              Iterator<List<Element>> multi() {
                [ [ ] ].iterator()
              }
            }
            new Element()
        '''

        // GROOVY-9822
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'Types.java')
            a.write '''
                import java.io.*;
                import java.util.*;

                // from org.apache.tinkerpop:gremlin-core:3.4.8

                interface TraversalStrategy<S extends TraversalStrategy> extends Serializable, Comparable<Class<? extends TraversalStrategy>> {
                    interface VerificationStrategy extends TraversalStrategy<VerificationStrategy> {
                    }
                }
                abstract class AbstractTraversalStrategy<S extends TraversalStrategy> implements TraversalStrategy<S> {
                }
                abstract // don't want to implement Comparable
                class ReadOnlyStrategy extends AbstractTraversalStrategy<TraversalStrategy.VerificationStrategy>
                        implements TraversalStrategy.VerificationStrategy {
                    static ReadOnlyStrategy instance() { return null; }
                }

                interface TraversalSource extends Cloneable, AutoCloseable {
                    default TraversalSource withStrategies(TraversalStrategy... strategies) {
                        return null;
                    }
                }
                abstract // don't want to implement AutoCloseable
                class GraphTraversalSource implements TraversalSource {
                    @Override
                    public GraphTraversalSource withStrategies(TraversalStrategy... strategies) {
                        return (GraphTraversalSource) TraversalSource.super.withStrategies(strategies);
                    }
                }
                class Graph {
                    public <C extends TraversalSource> C traversal(Class<C> c) {
                        return null;
                    }
                    public GraphTraversalSource traversal() {
                        return null;
                    }
                }
            '''
            def b = new File(parentDir, 'Script.groovy')
            b.write '''
                GraphTraversalSource test(Graph graph) {
                    def strategy = ReadOnlyStrategy.instance()
                    graph.traversal().withStrategies(strategy)
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    void testRegressionInConstructorCheck() {
        assertScript '''
            new ArrayList(['a','b','c'].collect { String it -> it.toUpperCase()})
        '''
    }

    void testReturnTypeInferenceWithMethodUsingWildcard() {
        assertScript '''
            public Object createInstance(Class<?> projectComponentClass, String foo) { projectComponentClass.newInstance() }
            createInstance(LinkedList, 'a')
        '''
    }

    // GROOVY-6051
    void testGenericsReturnTypeInferenceShouldNotThrowNPE() {
        assertScript '''
        class Bar {
          public static List<Date> bar(List<Date> dummy) {}
        }
        class Foo extends Bar {
            static public Date genericItem() {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def inft = node.getNodeMetaData(INFERRED_TYPE)
                    assert inft == make(List)
                    assert inft.genericsTypes[0].type == make(Date)
                })
                def res = bar(null)

                res[0]
            }
        }
        new Foo()
        '''
    }

    // GROOVY-6035
    void testReturnTypeInferenceWithClosure() {
        assertScript '''import org.codehaus.groovy.ast.expr.ClosureExpression
        class CTypeTest {

          public static void test1(String[] args) {

            // Cannot assign value of type java.lang.Object to variable of type CTypeTest
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def cl = node.rightExpression.arguments[0]
                assert cl instanceof ClosureExpression
                def type = cl.getNodeMetaData(INFERRED_TYPE)
                assert type == make(Closure)
                assert type.isUsingGenerics()
                assert type.genericsTypes
                assert type.genericsTypes[0].type.name == 'CTypeTest'

                type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.name == 'CTypeTest'
            })
            def s1 = cache  {
              return new CTypeTest();
            }

            CTypeTest s2 = cache {
                new CTypeTest()
            }

          }


          static <T> T cache(Closure<T> closure) {
            return closure.call();
          }

        }
        1
        '''
    }

    // GROOVY-6129
    void testShouldNotThrowNPE() {
        assertScript '''
            def map = new HashMap<>()
            map.put(1, 'foo')
            map.put('bar', new Date())
        '''
    }

    // GROOVY-6233
    void testConstructorArgumentsAgainstGenerics() {
        shouldFailWithMessages '''
            class Foo<T>{  Foo(T a, T b){} }
            def bar() {
                Foo<Map> f = new Foo<Map>("a",1)
            }
            bar()
        ''', '[Static type checking] - Cannot find matching method Foo#<init>(java.lang.String, int)'
    }

    // GROOVY-5742
    void testNestedGenerics() {
        assertScript '''
            import static Next.*

            abstract class Base<A extends Base<A>> {}
            class Done extends Base<Done> { }
            class Next<H, T extends Base<T>> extends Base<Next<H, T>> {
                H head; T tail
                static <H, T extends Base<T>> Next<H, T> next(H h, T t) { new Next<H, T>(head:h, tail:t) }
                String toString() { "Next($head, ${tail.toString()})" }
            }

            Next<Integer, Next<String, Done>> x = next(3, next("foo", new Done()))
        '''
    }

    // GROOVY-7848
    void testNestedGenerics2() {
        assertScript '''
            List<Integer> test() {
              def listOfLists = [[1,2], [3,4]]
              listOfLists.collect { pair -> pair[0] + pair[1] }
            }
            def result = test()
            assert result == [3,7]
        '''
    }

    void testMethodLevelGenericsFromInterface() {
        assertScript '''
            interface A {
                public <T> T getBean(Class<T> c)
            }
            interface B extends A {}
            interface C extends B {}

            void foo(C c) {
                String s = c?.getBean("".class)
            }
            foo(null)
            true
        '''
    }

    // GROOVY-5610
    void testMethodWithDefaultArgument() {
        assertScript '''
            class A{}
            class B extends A{}
            def foo(List<? extends A> arg, String value='default'){1}

            List<B> b = new ArrayList<>()
            assert foo(b) == 1
            List<A> a = new ArrayList<>()
            assert foo(a) == 1
        '''

        shouldFailWithMessages '''
            class A{}
            class B extends A{}
            def foo(List<? extends A> arg, String value='default'){1}

            List<Object> l = new ArrayList<>()
            assert foo(l) == 1
        ''',
        '#foo(java.util.List<? extends A>) with arguments [java.util.ArrayList<java.lang.Object>]'
    }

    void testMethodLevelGenericsForMethodCall() {
        // Groovy-5891
        assertScript '''
            public <T extends List<Integer>> T foo(Class<T> type, def x) {
                return type.cast(x)
            }
            def l = [1,2,3]
            assert foo(l.class, l) == l
        '''
        assertScript '''
            public <T extends Runnable> T foo(Class<T> type, def x) {
                return type.cast(x)
            }
            def cl = {1}
            assert foo(cl.class, cl) == cl
         '''
         assertScript '''
            public <T extends Runnable> T foo(Class<T> type, def x) {
                return type.cast(x) as T
            }
            def cl = {1}
            assert foo(cl.class, cl) == cl
         '''
         // GROOVY-5885
         assertScript '''
            class Test {
                public <X extends Test> X castToMe(Class<X> type, Object o) {
                    return type.cast(o);
                }
            }
            def t = new Test()
            assert t.castToMe(Test, t)  == t
         '''
    }

    // GROOVY-5839
    void testMethodShadowGenerics() {
        shouldFailWithMessages '''
            public class GoodCodeRed<T> {
                Collection<GoodCodeRed<T>> attached = []
                public <T> void attach(GoodCodeRed<T> toAttach) {
                    attached.add(toAttach)
                }
                static void foo() {
                    def g1 = new GoodCodeRed<Long>()
                    def g2 = new GoodCodeRed<Integer>()
                    g1.attach(g2);
                }
            }
            GoodCodeRed.foo()
        ''',
        'Cannot call <T> GoodCodeRed#attach(GoodCodeRed<java.lang.Long>) with arguments [GoodCodeRed<java.lang.Integer>]'
    }

    void testHiddenGenerics() {
        // Groovy-6237
        assertScript '''
            class MyList extends LinkedList<Object> {}
            List<Object> o = new MyList()
        '''

        shouldFailWithMessages '''
            class Blah {}
            class MyList extends LinkedList<Object> {}
            List<Blah> o = new MyList()
        ''','Incompatible generic argument types. Cannot assign MyList to: java.util.List<Blah>'

        // Groovy-5873
        assertScript """
            abstract class Parent<T> {
                public T value
            }
            class Impl extends Parent<Integer> {}
            Impl impl = new Impl()
            Integer i = impl.value
        """

        // GROOVY-5920
        assertScript """
            class Data<T> {
              T value
            }

            class StringDataIterator implements Iterator<Data<String>> {
              boolean hasNext() { true }
              void    remove()  {}
              Data<String> next() {
                new Data<String>( value: 'tim' )
              }
            }

            class Runner {
              static main( args ) {
                Data<String> elem = new StringDataIterator().next()
                assert elem.value.length() == 3
              }
            }
            Runner.main(null);
        """
    }

    void testReturnTypeInferenceRemovalWithGenerics() {
        assertScript '''
            class SynchronousPromise<T> {
                Closure<T> callable
                Object value

                SynchronousPromise(Closure<T> callable) {
                    this.callable = callable
                }

                T get() throws Throwable {
                    @ASTTest(phase=INSTRUCTION_SELECTION,value={
                        assert node.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                    })
                    value=callable.call()
                    return value
                }
            }

            def promise = new SynchronousPromise({ "Hello" })
            promise.get()
        '''
    }

    // GROOVY-6455
    void testDelegateWithGenerics() {
        assertScript '''
            class IntList {
                @Delegate List<Integer> delegate = new ArrayList<Integer>()
            }
            def l = new IntList()
            assert l == []
        '''
    }

    // GROOVY-6504
    void testInjectMethodWithInitialValueChoosesTheCollectionVersion() {
        assertScript '''import org.codehaus.groovy.transform.stc.ExtensionMethodNode
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def method = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert method.name == 'inject'
                assert method instanceof ExtensionMethodNode
                method = method.extensionMethodNode
                assert method.parameters[0].type == make(Collection)
            })
            def result = ['a','bb','ccc'].inject(0) { int acc, String str -> acc += str.length(); acc }
            assert  result == 6
        '''
    }

    // GROOVY-6504
    void testInjectMethodWithInitialValueChoosesTheCollectionVersionUsingDGM() {
        assertScript '''import org.codehaus.groovy.runtime.DefaultGroovyMethods
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def method = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert method.name == 'inject'
                assert method.parameters[0].type == make(Collection)
            })
            def result = DefaultGroovyMethods.inject(['a','bb','ccc'],0, { int acc, String str -> acc += str.length(); acc })
            assert  result == 6
        '''
    }

    // GROOVY-6760
    void testGenericsAtMethodLevelWithGenericsInTypeOfGenericType() {
        assertScript '''
            @Grab('com.netflix.rxjava:rxjava-core:0.18.1')
            import rx.Observable
            import java.util.concurrent.Callable

            static <T> Observable<T> observe(Callable<Iterable<T>> callable) {
                Observable.from(callable.call())
            }
            observe({ ["foo"] }) map {
                it.toUpperCase() // <- compiler doesn't know 'it' is a string
            } subscribe {
                assert it == "FOO"
            }
        '''
    }

    // GROOVY-6135
    void testGenericField() {
        assertScript '''
            class MyClass {
                static void main(args) {
                    Holder<Integer> holder = new Holder<Integer>()
                    holder.value = 5
                    assert holder.value > 4
                }
                private static class Holder<T> {
                    T value
                }
            }
        '''
    }

    //GROOVY-6723, GROOVY-6415
    void testIndirectMethodLevelGenerics() {
        assertScript '''
            class C1<A> {
                def void m1(A a) {C1.m2(a)}
                static <B> void m2(B b) {}
            }
            new C1().m1(null) // the call does not really matter
        '''
        assertScript '''
            class Test1 {
                static <A, B> void pair1(A a, B b) {}
                static <A, B> void pair2(A a, B b) {pair1(a, a)}
                static <A> List<A> list1(A a) {[a]}
                static <B> List<B> list2(B b) {list1(b)}
                static <A> List<A> list3(A a) {list1(a)}
            }
            Test1.pair2(1,2) // the call does not really matter
        '''

        assertScript '''
            class Foo {
                String method() {
                    return callT('abc')
                }

                private <T> T callT(T t) {
                    return callV(t)
                }

                private <V> V callV(V v) {
                    return v
                }
            }

            println new Foo().method()
        '''
    }

    // GROOVY-6358
    void testGenericsReturnedFromStaticMethodWithInnerClosureAndAsType() {
        assertScript '''
            import java.lang.reflect.Method

            interface Ifc {
               void method()
            }
            class Generator {
                static <T> T create (Class<T> clazz ){
                    return clazz.methods.collectEntries { Method method ->
                            [ (method.name) : { println "${method.name} called"} ]
                        }.asType(clazz)
                }
            }
            class User {
                static void main() {
                    Ifc ifc = Generator.create(Ifc)
                    ifc.method()
                }
            }

            User.main()
        '''
    }

    void testConcreteTypeInsteadOfGenerifiedInterface() {
        assertScript '''
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*
            import static org.codehaus.groovy.ast.ClassHelper.*

            interface Converter<F, T> {
            T convertC(F from)
            }
            class Holder<T> {
               T thing
               Holder(T thing) {
                this.thing = thing
               }
               def <R> Holder<R> convertH(Converter<? super T, ? extends R> func1) {
                  new Holder(func1.convertC(thing))
               }
            }
            class IntToFloatConverter implements Converter<Integer,Float> {
                public Float convertC(Integer from) { from.floatValue() }
            }
            void foo() {
                @ASTTest(phase=INSTRUCTION_SELECTION,value={
                    def holderType = node.getNodeMetaData(INFERRED_TYPE)
                    assert holderType.genericsTypes[0].type == Float_TYPE
                })
                def h1 = new Holder<Integer>(2).convertH(new IntToFloatConverter())
            }
            foo()
        '''
    }

    // GROOVY-6748
    void testCleanGenerics() {
        assertScript '''
            class Class1 {
                static <A, B> void method1(A a, B b) {
                    method2(a, b)
                }
                static <A, B> void method2(A a, B b) {}
                static <A, B> void method3(List<A> list1, List<B> list2) {
                    method1(list1.get(0), list2.get(0))
                }
            }
            new Class1().method3(["a"],["b"])
        '''
    }

    // GROOVY-6761
    void testInVariantAndContraVariantGenerics() {
        assertScript '''
            class Thing {
              public <O> void contravariant(Class<? super O> type, O object) {}
              public <O> void invariant(Class<O> type, O object) {}
              void m() {
                invariant(String, "foo")
                contravariant(String, "foo") // fails, can't find method
              }
            }
            new Thing().m()
        '''
    }

    // GROOVY-6731
    void testContravariantMethodResolution() {
        assertScript '''
            interface Function<T, R> {
                R apply(T t)
            }

            public <I, O> void transform(Function<? super I, ? extends O> function) {
                function.apply('')
            }

            String result = null
            transform(new Function<String, String>() {
                String apply(String input) {
                    result = "ok"
                }
            })

            assert result == 'ok'
        '''
    }

    // GROOVY-9998
    void testContravariantMethodResolution2() {
        assertScript '''
            import groovy.transform.*
            @TupleConstructor(defaults=false)
            class A {
              final int order
            }
            @InheritConstructors @ToString(includeSuperProperties=true)
            class B extends A {
            }

            Comparator<A> comparator = { a1, a2 -> Integer.compare(a1.order, a2.order) }

            def input = [new B(2), new B(3), new B(1), new B(0)]
            // sorted(Comparator<? super B>) using Comparator<A>
            def result = input.stream().sorted(comparator).toList()
            assert result.toString().equals("[B(0), B(1), B(2), B(3)]")
        '''
    }

    void testContravariantMethodResolutionWithImplicitCoercion1() {
        assertScript '''
            // static <T,E> List<T> collectMany(Iterable<E> it, Closure<? extends Collection<? extends T>> fn)
            List<String> strings = [1,2,3].collectMany {
                Collections.emptyList()
            }
            assert strings.isEmpty()
        '''
    }

    void testContravariantMethodResolutionWithImplicitCoercion2() {
        ['public', 'static'].each { modifier ->
            assertScript """
                $modifier <I, O> void transform(java.util.function.Function<? super I, ? extends O> function) {
                    function.apply('')
                }

                boolean called = false
                transform {
                    called = true
                }
                assert called
            """
        }
    }

    void testContravariantMethodResolutionWithImplicitCoercion3() {
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [stubDir: File.createTempDir()]
        }
        try {
            def a = new File(parentDir, 'Bean.java')
            a.write '''
                class Bean {
                    static Class<? extends CharSequence> getType() { return String.class; }
                }
            '''
            def b = new File(parentDir, 'Main.groovy')
            b.write '''
                def <T> T create(Class<T> c) { null }
                CharSequence cs = create(Bean.type)
                assert cs == null
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }

    // GROOVY-5981
    void testCovariantAssignment() {
        assertScript '''
            import javax.swing.*
            import java.awt.*

            class ComponentFixture<T extends Component> {}
            class JButtonFixture extends ComponentFixture<JButton> {}
            class ContainerFixture<T extends Container> extends ComponentFixture<T> {}
            abstract class ComponentAdapter<Fixture extends ComponentFixture> { Fixture getFixture() {} }
            abstract class ContainerAdapter<Fixture extends ContainerFixture> extends ComponentAdapter<Fixture> {}

            class ButtonComponent extends ComponentAdapter<JButtonFixture> {
                void setFixtureResolver(ContainerAdapter<? extends ContainerFixture> containerAdapter) {
                    ContainerFixture containerFixture = containerAdapter.getFixture() // Cannot assign value of type ComponentFixture to variable of type ContainerFixture
                }
            }

            new ButtonComponent()
        '''
    }

    // GROOVY-6856
    void testReturnTypeFitsInferredTypeWithBound() {
        assertScript '''
            class Wrapper {}

            class Foo<W extends Wrapper> {
                W doIt (List<W> l) {
                    l.iterator().next()
                }
            }
            Wrapper w = new Wrapper()
            assert new Foo<Wrapper>().doIt([w]) == w
        '''
    }

    // GROOVY-7691
    void testCovariantReturnTypeInferredFromField() {
        assertScript '''
            import groovy.transform.*

            @TupleConstructor(includeFields=true)
            abstract class A<N extends Number> {
                protected final N number
            }

            class C<L extends Long> extends A<L> { // further restriction of type parameter
                C(L longNumber) {
                    super(longNumber)
                }

                L getValue() {
                    return number
                }
            }

            assert new C<Long>(42L).value == 42L
        '''
    }

    // GROOVY-7691
    void testCovariantReturnTypeInferredFromProperty() {
        assertScript '''
            import groovy.transform.*

            @TupleConstructor
            abstract class A<N extends Number> {
                final N number
            }

            class C<L extends Long> extends A<L> {
                C(L longNumber) {
                    super(longNumber)
                }

                L getValue() {
                    return number
                }
            }

            assert new C<Long>(42L).value == 42L
        '''
    }

    void testCovariantReturnTypeInferredFromMethod1() {
        assertScript '''
            import groovy.transform.*

            @TupleConstructor(includeFields=true)
            abstract class A<N extends Number> {
                protected final N number

                protected N getNumber() {
                    return number
                }
            }

            class C<L extends Long> extends A<L> {
                C(L longNumber) {
                    super(longNumber)
                }

                L getValue() {
                    return getNumber()
                }
            }

            assert new C<Long>(42L).value == 42L
        '''
    }

    // GROOVY-9580
    void testCovariantReturnTypeInferredFromMethod2() {
        assertScript '''
            import groovy.transform.*

            @TupleConstructor(includeFields=true)
            abstract class A<N extends Number> {
                final N number
            }

            class C<L extends Long> extends A<L> {
                C(L longNumber) {
                    super(longNumber)
                }

                L getValue() {
                    return getNumber() // property method stubbed by StaticTypeCheckingVisitor
                }
            }

            assert new C<Long>(42L).value == 42L
        '''
    }

    // GROOVY-9635
    void testCovariantReturnTypeInferredFromMethod3() {
        assertScript '''
            import java.util.function.Function

            class C<R extends Number> {
                def <V> V m(Function<C, V> f) { // R from C is confused with R->V from Function
                    V result = f.apply(this)
                    return result
                }
            }

            def ret = new C().m(new Function<C, String>() {
                @Override
                String apply(C that) {
                    return 'foo'
                }
            })

            assert ret == 'foo'
        '''
    }

    void testReturnTypeChecking() {
        shouldFailWithMessages '''
            class Foo {
                List<String> run() {
                    [11, 12]
                }
            }
        ''', 'Incompatible generic argument types. Cannot assign java.util.List<java.lang.Integer> to: java.util.List<java.lang.String>'
    }

    void testBoundedReturnTypeChecking() {
        assertScript '''
            class Foo {
                List<? extends Serializable> run() {
                    [1, 'a']
                }
            }
            null
        '''

        // GROOVY-9821
        ['.', '?.', '*.'].each { op ->
            config.with {
                targetDirectory = File.createTempDir()
                jointCompilationOptions = [memStub: true]
            }
            File parentDir = File.createTempDir()
            try {
                def a = new File(parentDir, 'Types.java')
                a.write '''
                    interface A {
                        java.util.List<? extends B> getBees();
                    }
                    interface B {
                        Object getC();
                    }
                '''
                def b = new File(parentDir, 'Script.groovy')
                b.write """
                    def test(A a) {
                        a.bees${op}c
                    }
                """

                def loader = new GroovyClassLoader(this.class.classLoader)
                def cu = new JavaAwareCompilationUnit(config, loader)
                cu.addSources(a, b)
                cu.compile()
            } finally {
                parentDir.deleteDir()
                config.targetDirectory.deleteDir()
            }
        }
    }

    // GROOVY-9934
    void testBoundedReturnTypeChecking2() {
        assertScript '''
            class Bar {
            }
            class Foo<T extends Bar> {
                T method(T t) {
                    def c = { -> t }
                    return c() // Cannot return value of type Object on method returning type T
                }
            }
            def bar = new Bar()
            assert bar.is(new Foo<Bar>().method(bar))
        '''
    }

    // GROOVY-9891
    void testBoundedReturnTypeChecking3() {
        assertScript '''
            class Pogo {
                Map<String, ? extends Number> map
            }
            def test(Pogo p) {
                Collection<? extends Number> c = p.map.values()
                Iterable<? extends Number> i = p.map.values()
                i.iterator().next()
            }
            def n = test(new Pogo(map:[x:1,y:2.3]))
            assert n == 1
        '''

        //

        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'Pojo.java')
            a.write '''
                import java.util.Map;
                class Pojo {
                    Map<String, ? extends Number> getMap() {
                        return map;
                    }
                    void setMap(Map<String, ? extends Number> map) {
                        this.map = map;
                    }
                    private Map<String, ? extends Number> map = null;
                }
            '''
            def b = new File(parentDir, 'Script.groovy')
            b.write '''
                void test(Pojo p) {
                    Collection<? extends Number> c = p.map.values()
                    Iterable<? extends Number> i = p.map.values()
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    // GROOVY-7804
    void testParameterlessClosureToGenericSAMTypeArgumentCoercion() {
        assertScript '''
            interface Supplier<T> { public T get() }

            def <T> T doGet(Supplier<T> supplier) { supplier.get() }

            assert doGet { -> 'foo' } == 'foo'
        '''
    }

    // GROOVY-7713
    void testClosureReturnNull() {
        assertScript '''
            Closure<String> cl = {
                if (hashCode() > 0) {
                    return null
                }
                'foo'
            }
        '''
    }

    static class MyList extends LinkedList<String> {}

    public static class ClassA<T> {
        public <X> Class<X> foo(Class<X> classType) {
            return classType;
        }

        public <X> Class<X> bar(Class<T> classType) {
            return null;
        }
    }

    public static class JavaClassSupport {
        public static class Container<T> {
        }

        public static class StringContainer extends Container<String> {
        }

        public static <T> List<T> unwrap(Collection<? extends Container<T>> list) {
        }
    }
}
