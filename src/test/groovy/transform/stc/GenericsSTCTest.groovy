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

import groovy.test.NotYetImplemented
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
        ''',
        'Incompatible generic argument types. Cannot assign java.util.HashMap<java.lang.String, java.lang.Integer> to: java.util.Map<java.lang.String, java.lang.String>'
    }

    void testDeclaration4() {
        // no generics checked after first wildcard
        shouldFailWithMessages '''
            Map<? extends CharSequence,String> obj = new HashMap<String,Integer>()
        ''',
        'Incompatible generic argument types. Cannot assign java.util.HashMap<java.lang.String, java.lang.Integer> to: java.util.Map<? extends java.lang.CharSequence, java.lang.String>'
    }

    void testAddOnList() {
        shouldFailWithMessages '''
            List<String> list = []
            list.add(1)
        ''',
        'Cannot call java.util.ArrayList#add(java.lang.String) with arguments [int]'
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
        ''',
        'Cannot call <T> org.codehaus.groovy.runtime.DefaultGroovyMethods#leftShift(java.util.List<T>, T) with arguments [java.util.ArrayList<java.lang.String>, int]'
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

    void testListInferenceWithNullElems() {
        assertScript '''
            List<String> strings = ['a', null]
            assert strings == ['a',null]
        '''
    }

    void testListInferenceWithAllNullElems() {
        assertScript '''
            List<String> strings = [null, null]
            assert strings == [null,null]
        '''
    }

    void testAddOnListWithDiamondAndWrongType() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list.add 'Hello'
        ''',
        'Cannot call java.util.LinkedList#add(java.lang.Integer) with arguments [java.lang.String]'
    }

    void testAddOnListWithDiamondAndWrongTypeUsingLeftShift() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list << 'Hello'
        ''',
        'Cannot call <T> org.codehaus.groovy.runtime.DefaultGroovyMethods#leftShift(java.util.List<T>, T) with arguments [java.util.LinkedList<java.lang.Integer>, java.lang.String]'
    }

    void testAddOnListWithDiamondAndNullUsingLeftShift() {
        assertScript '''
            List<Integer> list = new LinkedList<>()
            list << null
        '''
    }

    void testCompareToOnPrimitive() {
        assertScript '''
            int a = 1
            assert (a < 2)
            assert !(a < 1)
            assert (a <= 1)
        '''
        // GROOVY-11383
        shouldFailWithMessages '''
            int a = 42
            def b = (a < new Object())
        ''',
        'Cannot call java.lang.Integer#compareTo(java.lang.Integer) with arguments [java.lang.Object]'
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
        ''',
        'Cannot call java.util.Optional#orElse(java.lang.Integer) with arguments [double]'
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
        ''',
        'Cannot call java.util.Optional#orElse(java.lang.Integer) with arguments [double]'
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
        ''',
        'Incompatible generic argument types.' // Cannot assign java.util.ArrayList<java.lang.Object> to: java.util.List<java.lang.String>

        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.genericsTypes[0].toString() == 'java.lang.String'
                assert type.genericsTypes[1].toString() == 'java.util.ArrayList<java.lang.Object>' // not <E>
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
            def <T extends Number> List<T> g(Class<T> t) {
                f(t).stream().filter(n -> n.intValue() > 0).toList()
            }

            def result = g(Integer)
            assert result == [ 42 ]
        '''

        for (cast in ['t::cast', 'n -> t.cast(n)', 'n -> (N) n', '{ n -> (N) n }']) {
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
            interface Handle {
                Result getResult()
            }
            class HandleContainer<H extends Handle> {
                H handle
            }
            abstract class State<H extends Handle> {
                // Why not return HandleContainer<H>? I can't really say.
                def <T extends Handle> HandleContainer<T> getHandleContainer(key) {
                }
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

    // GROOVY-10978
    void testReturnTypeInferenceWithMethodGenerics1x() {
        assertScript '''
            interface Handle {
                int getCount()
            }
            class HandleContainer<H extends Handle> {
                H handle
            }
            interface Input {
                HandleContainer<? extends Handle> getResult(key)
            }
            interface State {
                def <X extends Handle> HandleContainer<X> getResult(key)
            }

            void test(Input input, State state) {
                def container = state.getResult('k') ?: input.getResult('k')
                Handle handle = container.handle
                Integer count = handle.count
                assert count == 1
            }

            Handle h = {->1}
            def c = new HandleContainer(handle: h)
            test({k->c} as Input, {k->c} as State)
        '''

        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
        try {
            def a = new File(parentDir, 'Handle.java')
            a.write '''
                interface Handle {
                    int getCount();
                }
            '''
            def b = new File(parentDir, 'HandleContainer.java')
            b.write '''
                class HandleContainer<H extends Handle> {
                    H handle;
                }
            '''
            def c = new File(parentDir, 'Input.java')
            c.write '''
                interface Input {
                    HandleContainer<? extends Handle> getResult(Object key);
                }
            '''
            def d = new File(parentDir, 'State.java')
            d.write '''
                interface State {
                    <X extends Handle> HandleContainer<X> getResult(Object key);
                }
            '''
            def e = new File(parentDir, 'Tester.groovy')
            e.write '''
                void test(Input input, State state) {
                    def container = state.getResult('k') ?: input.getResult('k')
                    Handle handle = container.handle
                    Integer count = handle.count
                    assert count == 1
                }

                Handle h = {->1}
                def c = new HandleContainer(handle: h)
                test({k->c} as Input, {k->c} as State)
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d, e)
            cu.compile()

            loader.loadClass('Tester').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
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
        for (t in ['R', 'S', 'T', 'U']) { // BiFunction uses R, T and U
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

    // GROOVY-7316, GROOVY-10256
    void testReturnTypeInferenceWithMethodGenerics16() {
        shouldFailWithMessages '''
            def <T extends CharSequence> T chars() {
            }
            List test() {
                chars()
            }
        ''',
        'Cannot return value of type java.lang.CharSequence for method returning java.util.List'
    }

    // GROOVY-10098
    void testReturnTypeInferenceWithMethodGenerics17() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T extends Number> {
              T p
              T m() {
                Closure<T> x = { -> p }
                x() // Cannot return value of type Object for method returning T
              }
            }
            assert new C<>(42).m() == 42
        '''
    }

    void testReturnTypeInferenceWithMethodGenerics18() {
        assertScript '''
            @Grab('com.google.guava:guava:31.1-jre')
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

    // GROOVY-8638
    void testReturnTypeInferenceWithMethodGenerics18a() {
        shouldFailWithMessages '''
            @Grab('com.google.guava:guava:31.1-jre')
            import com.google.common.collect.*

            def mmap = (ListMultimap<String, Integer>) ArrayListMultimap.create()
            Map<String, Set<Integer>> map = mmap.asMap()
        ''',
        'Cannot assign java.util.Map<java.lang.String, java.util.Collection<java.lang.Integer>> to: java.util.Map<java.lang.String, java.util.Set<java.lang.Integer>>'
    }

    // GROOVY-10120
    @NotYetImplemented
    void testReturnTypeInferenceWithMethodGenerics18b() {
        shouldFailWithMessages '''
            @Grab('com.google.guava:guava:31.1-jre')
            import com.google.common.collect.*

            ListMultimap<String, Integer> mmap = ArrayListMultimap.create()
            Map<String, Set<Integer>> map = mmap.asMap() // ArrayListMultimap#asMap() is bridge and lacks generics
        ''',
        'Cannot assign java.util.Map<java.lang.String, java.util.Collection<java.lang.Integer>> to: java.util.Map<java.lang.String, java.util.Set<java.lang.Integer>>'
    }

    // GROOVY-10222
    void testReturnTypeInferenceWithMethodGenerics19() {
        assertScript '''
            class Task {
                def <T> T exec(args) {
                    args
                }
            }
            class Test {
                Task task
                def <T> T exec(args) {
                    task.exec(args) // Cannot return value of type #T for method returning T
                }
            }
            String result = new Test(task: new Task()).exec('works')
            assert result == 'works'
        '''
    }

    // GROOVY-9500
    void testReturnTypeInferenceWithMethodGenerics20() {
        assertScript '''
            trait Entity<D> {
            }

            abstract class Path<F extends Entity, T extends Entity> implements Iterable<Path.Segment<F,T>> {
                interface Segment<F, T> {
                    F start()
                    T end()
                }

                abstract F start()
                T end() {
                  end // Cannot return value of type Path$Segment<F,T> for method returning T
                }
                T end

                @Override
                void forEach(java.util.function.Consumer<? super Segment<F, T>> action) {
                }
                @Override
                Spliterator<Segment<F, T>> spliterator() {
                }
                @Override
                Iterator<Segment<F, T>> iterator() {
                }
            }

            null
        '''
    }

    // GROOVY-10339
    void testReturnTypeInferenceWithMethodGenerics21() {
        for (type in ['Character', 'Integer']) {
            shouldFailWithMessages """
                Character foo() {
                }
                def <T> T bar(T x, T y) {
                }
                $type z = bar(foo(), 1)
            """,
            'Cannot assign value of type (java.io.Serializable & java.lang.Comparable',') to variable of type java.lang.' + type
        }
    }

    // GROOVY-10339
    void testReturnTypeInferenceWithMethodGenerics22() {
        for (type in ['Comparable', 'Serializable']) {
            assertScript """
                String foo() {
                }
                def <T> T bar(T x, T y) {
                }
                $type z = bar(foo(), 1)
            """
        }
    }

    // GROOVY-10347
    void testReturnTypeInferenceWithMethodGenerics23() {
        assertScript '''
            String[] one = ['foo','bar'], two = ['baz']
            Map<String,Integer> map = one.collectEntries{[it,1]} + two.collectEntries{[it,2]}
            assert map == [foo:1, bar:1, baz:2]
        '''
    }

    // GROOVY-10347
    void testReturnTypeInferenceWithMethodGenerics24() {
        assertScript '''
            class Pogo {
                String s
            }
            class Sorter implements Comparator<Pogo>, Serializable {
                int compare(Pogo p1, Pogo p2) { p1.s <=> p2.s }
            }

            Pogo[] pogos = [new Pogo(s:'foo'), new Pogo(s:'bar')]
            List<String> strings = pogos.sort(true, new Sorter())*.s // sort(T[],boolean,Comparator<? super T>)
            assert strings == ['bar', 'foo']
        '''
    }

    // GROOVY-10589
    void testReturnTypeInferenceWithMethodGenerics25() {
        String pogo = '''
            @groovy.transform.TupleConstructor
            class Pogo {
                final java.time.Instant timestamp
            }
            List<Pogo> pogos = []
        '''
        assertScript pogo + '''
            Comparator<Pogo> cmp = { Pogo a, Pogo b -> a.timestamp <=> b.timestamp }
            pogos = pogos.sort(false, cmp)
        '''
        assertScript pogo + '''
            pogos = pogos.sort(false) { Pogo a, Pogo b ->
                a.timestamp <=> b.timestamp
            }
        '''
        assertScript pogo + '''
            pogos = pogos.sort(false) { it.timestamp }
        '''
    }

    // GROOVY-7992
    void testReturnTypeInferenceWithMethodGenerics26() {
        assertScript '''
            List<String> strings = ['foo','bar','baz']
            Comparator<Object> cmp = { o1, o2 -> o1.toString() <=> o2.toString() }

            @groovy.transform.ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.List<java.lang.String>'
            })
            def result = strings.sort(false, cmp) // List<T> sort(Iterable<T>,boolean,Comparator<? super T>)
            assert result == ['bar', 'baz', 'foo']
        '''
    }

    // GROOVY-10622
    void testReturnTypeInferenceWithMethodGenerics27() {
        String types = '''
            @groovy.transform.TupleConstructor(defaults=false)
            class A<X> {
                X x
            }
            @groovy.transform.TupleConstructor(defaults=false)
            class B<Y> {
                Y y
            }
            class C {
                C(byte b) { }
            }
        '''
        assertScript types + '''
            def <T extends A<Byte>> void test(B<T> b_of_t) {
                def t = b_of_t.getY()
                def x = t.getX()
                new C(x)
            }
            test(new B<>(new A<>((byte)1)))
        '''
        assertScript types + '''
            def <T extends A<Byte>> void test(B<T> b_of_t) {
                def t = b_of_t.y
                def x = t.x
                new C(x)
            }
            test(new B<>(new A<>((byte)1)))
        '''
    }

    // GROOVY-10637
    void testReturnTypeInferenceWithMethodGenerics28() {
        assertScript '''
            class Outer extends groovy.transform.stc.MyBean<Inner> {
                static class Inner {
                    String string
                }
                def bar() {
                    { -> value.string }.call()
                }
            }

            def foo = new Outer(value: new Outer.Inner(string:'hello world'))
            assert foo.bar() == 'hello world'
        '''
    }

    // GROOVY-10646
    void testReturnTypeInferenceWithMethodGenerics29() {
        String types = '''
            class Model {
            }
            interface Output<T> {
                T getT()
            }
            abstract class WhereDSL<Type> {
                abstract Type where()
            }
            abstract class Input<T> extends WhereDSL<ReferencesOuterClassTP> {
                class ReferencesOuterClassTP implements Output<T> {
                    @Override T getT() { return null }
                }
            }
        '''
        assertScript types + '''
            void m(Input<Model> input) {
                def output = input.where()
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE).toString(false) == 'Model'
                })
                def result = output.getT()
            }
        '''
        assertScript types + '''
            @FunctionalInterface
            interface Xform extends java.util.function.Function<Input<Model>, Output<Model>> {
            }

            void select(Xform xform) {
            }

            select { input ->
                def result = input.where()
                return result // Cannot return value of type Input$ReferencesOuterClassTP for closure expecting Output<Model>
            }
        '''
    }

    // GROOVY-10749
    void testReturnTypeInferenceWithMethodGenerics30() {
        String named = 'class Named { String name }'

        for (expr in ['Named.&getName', 'Named::getName', '{Named named -> named.getName()}', '(Named named) -> named.getName()']) {
            assertScript named + """
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.getNodeMetaData(INFERRED_TYPE)
                    assert type.toString(false) == 'java.util.stream.Collector<Named, ?, java.util.Map<java.lang.String, java.util.List<Named>>>'
                })
                def collector = java.util.stream.Collectors.groupingBy($expr)
            """
        }

        // explicit type args
        assertScript named + '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.stream.Collector<Named, ?, java.util.Map<java.lang.String, java.util.List<Named>>>'
            })
            def c1 = java.util.stream.Collectors.<Named,String>groupingBy(named -> named.getName())
            //                                   ^^^^^^^^^^^^^^

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.stream.Collector<Named, ?, java.util.Map<java.lang.String, Named>>'
            })
            def c2 = java.util.stream.Collectors.<Named,String,Named>toMap(named -> named.getName(), named -> named)
            //                                   ^^^^^^^^^^^^^^^^^^^^
        '''
    }

    // GROOVY-11257
    void testReturnTypeInferenceWithMethodGenerics31() {
        assertScript '''
            interface A<T> {
                default Optional<T> getThing() {
                    Optional.empty()
                }
            }
            interface B<U> extends A<U> {
            }
            class C {
                def <V> B<V> forge(V v) {
                    return new B(){}
                }
                void test() {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def type = node.getNodeMetaData(INFERRED_TYPE)
                        assert type.toString(false) == 'java.util.Optional<java.math.BigInteger>'
                    })
                    def opt = forge(1G).thing
                    def num = opt.orElse(42G).intValue()
                }
            }

            new C().test()
        '''
    }

    void testDiamondInferenceFromConstructor1() {
        assertScript '''
            class Foo<U> {
                U method() { }
            }
            Foo<Integer> foo = new Foo<>()
            Integer result = foo.method()
        '''
    }

    void testDiamondInferenceFromConstructor2() {
        assertScript '''
            Set<Long> set = new HashSet<>()
        '''
    }

    void testDiamondInferenceFromConstructor3() {
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
    void testDiamondInferenceFromConstructor4() {
        assertScript '''
            Map<Thread.State, Object> map = new EnumMap<>(Thread.State)
            assert map.size() == 0
            assert map.isEmpty()
        '''
    }

    // GROOVY-6232
    void testDiamondInferenceFromConstructor5() {
        for (args in ['"a",new Object()', 'new Object(),"b"', '"a","b"']) {
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
    void testDiamondInferenceFromConstructor6() {
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
    void testDiamondInferenceFromConstructor7() {
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
    void testDiamondInferenceFromConstructor7a() {
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
    void testDiamondInferenceFromConstructor8() {
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
    void testDiamondInferenceFromConstructor8a() {
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
    void testDiamondInferenceFromConstructor8b() {
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

    void testDiamondInferenceFromConstructor9() {
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
        ''',
        'Incompatible generic argument types. Cannot assign C<D> to: A<java.lang.String>'

        shouldFailWithMessages '''
            Set<List<String>> strings = new HashSet<>([new ArrayList<Number>()])
        ''',
        'Incompatible generic argument types. Cannot assign java.util.HashSet<java.util.ArrayList<java.lang.Number>> to: java.util.Set<java.util.List<java.lang.String>>'
    }

    // GROOVY-9972
    void testDiamondInferenceFromConstructor9a() {
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

    void testDiamondInferenceFromConstructor9b() {
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
        ''',
        'Incompatible generic argument types. Cannot assign C<? extends java.lang.Object> to: C<D>'
    }

    // GROOVY-9963
    void testDiamondInferenceFromConstructor10() {
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
    void testDiamondInferenceFromConstructor11() {
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
    void testDiamondInferenceFromConstructor12() {
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
    void testDiamondInferenceFromConstructor13() {
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
    void testDiamondInferenceFromConstructor14() {
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
            C.m(flag ? new A<>(new B()) : new A<>((B)null))
            C.m(flag ? new A<>(new B()) : new A<>((B)new E())) // Cannot call m(A<B>) with arguments [A<? extends B>]
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

    // GROOVY-10114
    void testDiamondInferenceFromConstructor14a() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class A<T> {
              T p
            }
            class B {
                Character m() {
                    (Character) '!'
                }
            }
            (false ? new A<B>(new B()) : new A<>(new B())).p.m()
            (false ? new A< >(new B()) : new A<>(new B())).p.m()
            def a = (true ? new A<>(new B()) : new A<>(new B()))
            assert a.p.m() == (char)'!'
        '''
    }

    // GROOVY-9995
    void testDiamondInferenceFromConstructor15() {
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

    // GROOVY-10283
    void testDiamondInferenceFromConstructor16() {
        assertScript '''
            class A<T1, T2> {
            }
            class B<T1 extends Number, T2 extends A<C, ? extends T1>> {
                T2 t
                B(T2 t) {
                    this.t  = t // Cannot assign value of type A<C,? extends T1> to variable of type T2
                }
            }
            class C {
            }

            new B<Integer,A<C,Integer>>(new A<>())
        '''
    }

    // GROOVY-10291
    void testDiamondInferenceFromConstructor17() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class A<X> {
                X x
            }
            class B<Y> {
                def m(Y y) { null }
                def test() {
                    def c = { Y yy -> null }

                    Y y = null
                    m(new A<>(y).x) // works
                    c(new A<>(y).x) // fails
                }
            }
            new B().test()
        '''
    }

    // GROOVY-10228
    void testDiamondInferenceFromConstructor18() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<T> {
                T p
            }
            def m(Number n) {
                'works'
            }
            def x = 12345
            x = m(new C<>(x).getP()) // Cannot find matching method
            assert x == 'works'
        '''
    }

    // GROOVY-10323
    void testDiamondInferenceFromConstructor19() {
        assertScript '''
            class C<T> {
            }
            def <T,T> T m(C<T> c) {
            }
            Number n = m(new C<>())
        '''
    }

    // GROOVY-10324
    void testDiamondInferenceFromConstructor20() {
        assertScript '''
            class C<T> {
            }
            def <X> X m(C<X> c) {
            }
            List<String> x = m(new C<>())
        '''
    }

    // GROOVY-10310
    void testDiamondInferenceFromConstructor21() {
        assertScript '''
            @groovy.transform.TupleConstructor
            class A<T> {
                T f
            }
            class B<T> {
            }
            def <T> A<T> m(T t, B<? extends T> b_of_t) {
                new A<>(t)
            }
            def x = 'x'
            m(x, new B<>()) // Cannot call m(T,B<? extends T>) with arguments...
        '''
    }

    // GROOVY-10344
    void testDiamondInferenceFromConstructor22() {
        assertScript '''
            class C<X,Y> {
            }
            def <T extends C<? extends Number, ? extends Number>> T m(T t) {
                return t
            }
            def x = m(new C<>())
            assert x instanceof C
        '''
    }

    // GROOVY-10230
    void testDiamondInferenceFromConstructor23() {
        assertScript '''
            class A {
                def <T extends C<Number,Number>> T m(T t) {
                    return t
                }
            }
            class B extends A {
                @Override
                def <T extends C<Number,Number>> T m(T t) {
                    T x = null; super.m(true ? t : x)
                }
            }
            class C<X,Y> {
            }
            def x = new B().m(new C<>())
            assert x instanceof C
        '''
    }

    // GROOVY-10351
    void testDiamondInferenceFromConstructor24() {
        assertScript '''
            class C<T> {
                C(T one, D<T,? extends T> two) {
                }
            }
            class D<U,V> {
            }
            D<Integer,? extends Integer> d_of_i_and_i = null
            C<Integer> c_of_i = new C<>(1,d_of_i_and_i) // 3 witnesses for T
        '''
    }

    // GROOVY-10368
    void testDiamondInferenceFromConstructor25() {
        for (t in ['T', 'T extends Number', 'T extends Object']) {
            assertScript """
                class C<$t> {
                    C(p) {
                    }
                }
                void m(C<Integer> c) {
                }
                m(new C<>(null)) // Cannot call m(C<Integer>) with arguments [C<# extends Number>]
            """
        }
    }

    // GROOVY-10367
    void testDiamondInferenceFromConstructor26() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class C<X, Y extends X> { // works without Y
              X x
            }
            def <Z extends Number> void test(Z z) {
              z = new C<>(z).x // Cannot assign value of type Object to variable of type Z
            }
            test(null)
        '''
    }

    // GROOVY-10343
    void testDiamondInferenceFromConstructor27() {
        assertScript '''
            class C<T1, T2 extends T1> {
                T1 p
                C(T1 p) { this.p = p }
                T2 m() { return null }
            }
            void test(Integer x) {
                def c = new C<>(x) // type witness for T1 can also help bound T2
                def y = c.m()
                Integer z = y // Cannot assign value of type Object to variable of type Integer
            }
            test(1234)
        '''
    }

    // GROOVY-10316
    void testDiamondInferenceFromConstructor28() {
        assertScript '''
            class A<T> {
                A(T t) {
                }
            }
            @groovy.transform.TupleConstructor(defaults=false)
            class B<T> {
                A<T> p
            }
            def b = new B<>(new A<>(1L))
            A<Long> x = b.p // Cannot assign A<Object> to A<Long>
        '''
    }

    // GROOVY-10624
    void testDiamondInferenceFromConstructor29() {
        assertScript '''
            class A<T> {
            }
            class B<T> {
                B(A<T> a) { }
            }
            B<Float> x = new B<>(new A<>()) // Cannot assign B<Object> to B<Float>
        '''
    }

    // GROOVY-10266
    void testDiamondInferenceFromConstructor30() {
        assertScript '''
            @groovy.transform.TupleConstructor(defaults=false)
            class A<T> {
                T t
            }
            class B<U> {
                def m() {
                    U v = null
                    U w = new A<>(v).t

                    String x = ""
                    String y = new A<>(x).t
                }
            }
            new B<String>().m()
        '''
    }

    // GROOVY-10662
    void testDiamondInferenceFromConstructor31() {
        assertScript '''
            class A<X, T> {
                A(T t, X x) {}
                void m(X x) {}
            }
            class B<T extends Number> {
                void test() {
                    T t = (T) null
                    Character c = 'c'
                    def a = new A<>(c, t)
                    a.m((T) null) // Cannot find matching method A#m(T)
                }
            }
            new B<Integer>().test()
        '''
    }

    // GROOVY-10633
    void testDiamondInferenceFromConstructor32() {
        assertScript '''
            class A<T, Y> {
                public B<Y> f
                A(B<Y> b_of_y, T t) {
                    this.f = b_of_y
                }
            }
            class B<T> {
                void m(T t) {
                }
            }
            <T extends Number> void test() {
                def x = new B<T>()
                new A<>(x, '').f.m((T) null)
            }
            test()
        '''
    }

    // GROOVY-10699
    void testDiamondInferenceFromConstructor33() {
        assertScript '''
            class A<T> {
                A(B<T> b_of_t) {
                }
            }
            class B<T> {
                B(T t) {
                }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'A<java.lang.String>'
            })
            def x = new A<>(new B<>('str'))
        '''

        assertScript '''import java.util.function.Consumer
            class C<T> {
                C(Consumer<T> consumer_of_t) {
                    consumer_of_t.accept(null)
                }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'C<java.lang.String>'
            })
            def y = new C<>((String s) -> { print(s) }) // error: Expected type Object for lambda parameter: s
        '''

        assertScript '''import java.util.function.Supplier
            class D<T> {
                D(Supplier<T> supplier_of_t) {
                    T t = supplier_of_t.get()
                }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'D<java.lang.String>'
            })
            def z = new D<>(() -> 'str')
        '''
    }

    // GROOVY-10698
    void testDiamondInferenceFromConstructor34() {
        assertScript '''
            class A<T> {
                A(T t, B<T> b_of_t) {
                }
            }
            class B<U> {
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'A<java.lang.String>'
            })
            def x = new A<>('witness', new B<>()) // Cannot call A#<init>(Object,B<Object>) with arguments [String, B<T>]
        '''
    }

    // GROOVY-10847
    void testDiamondInferenceFromConstructor35() {
        assertScript '''
            class A<T, U> {
            }
            class B {
              def <X extends A<Character, Boolean>, Y extends X> Object m(X x, Y y) {
              }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.arguments[1].getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'A<java.lang.Character, java.lang.Boolean>'
            })
            def c = new B().m(null, new A<>())
        '''
    }

    // GROOVY-10674
    void testDiamondInferenceFromConstructor36() {
        assertScript '''
            class Foo<BB extends Bar<Byte>, X extends BB> {
                X x
                Foo(X x) {
                    this.x = x
                }
            }
            class Bar<T extends Number> {
            }

            class Baz {
                static Foo<Bar<Byte>, ? super Bar<Byte>> foo = new Foo<>(new Bar<>())
            }
            new Baz()
        '''

        assertScript '''
            class Foo<BBQ extends Bar<Byte, ? extends Byte>, X extends BBQ> {
                X x
                Foo(X x) {
                    this.x = x
                }
            }
            class Bar<T extends Number, S extends T> {
            }

            class Baz {
                Foo<Bar<Byte,Byte>, ? super Bar<Byte,Byte>> foo = new Foo<>(new Bar<>())
            }
            new Baz()
        '''
    }

    // GROOVY-10890
    void testDiamondInferenceFromConstructor37() {
        assertScript '''
            enum A { X }
            enum B { Y }
            void m(Map<A,B> map) {}
            void test(Map<A,B> map) {
                m(new EnumMap<>(map))
            }
            test([(A.X): B.Y])
        '''
    }

    // GROOVY-11192
    void testDiamondInferenceFromConstructor38() {
        assertScript '''
            interface I<X,Y> {}
            class C<Z> implements I<Number,Z> {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.type
                assert type.toString(false) == 'C<java.lang.String>'
            })
            I<Number,String> i = new C<>()
        '''

        assertScript '''
            interface I<X,Y> {}
            class C<Z> implements I<Z,String> {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.type
                assert type.toString(false) == 'C<java.lang.Number>'
            })
            I<Number,String> i = new C<>()
        '''

        assertScript '''
            interface I<X,Y> {}
            class C<Z> implements I<Z,Z> {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.type
                assert type.toString(false) == 'C<java.lang.String>'
            })
            I<String,String> i = new C<>()
        '''

        assertScript '''
            interface I<X,Y> {}
            class C<V,K> implements I<K,V> {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.type
                assert type.toString(false) == 'C<java.lang.String, java.lang.Number>'
            })
            I<Number,String> i = new C<>()
        '''

        assertScript '''
            interface I<X,Y,Z> {}
            abstract class A<K,V>
                implements I<K,V,Short> {}
            class C<T,U> extends A<U,T> {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.type
                assert type.toString(false) == 'C<java.lang.Long, java.lang.Integer>'
            })
            I<Integer,Long,Short> i = new C<>()
        '''
    }

    void testDiamondInferenceFromConstructor39() {
        // no relation between C's and I's type vars

        assertScript '''
            interface I<X,Y> {}
            class C<Z> implements I<Number,String> {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.type
                assert type.toString(false) == 'C<Z>' // TODO: <> or <?>
            })
            I<Number,String> i = new C<>() // TODO: Should this be an error?
        '''

        assertScript '''
            interface I<X,Y,Z> {}
            abstract class A<K,V>
                implements I<K,V,Short> {}
            class C<T> extends A<Integer,Long> {}

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.rightExpression.type
                assert type.toString(false) == 'C<T>' // TODO: <> or <?>
            })
            I<Integer,Long,Short> i = new C<>() // TODO: Should this be an error?
        '''
    }

    // GROOVY-10280
    void testTypeArgumentPropagation() {
        assertScript '''
            class Test<T> {
                T test() {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def type = node.getNodeMetaData(INFERRED_TYPE)
                        assert type.toString(false) == 'T'
                    })
                    def t = new Foo<T>().x.y.z
                }
            }
            class Foo<X> {
                Bar<X> x = new Bar<>()
            }
            class Bar<T> {
                Baz<T> y = new Baz<>()
            }
            class Baz<Z> {
                Z z
            }

            new Test<String>().test()
        '''
    }

    void testLinkedListWithListArgument() {
        assertScript '''
            List<String> list = new LinkedList<String>(['1','2','3'])
        '''
    }

    void testLinkedListWithListArgumentAndWrongElementTypes() {
        shouldFailWithMessages '''
            List<String> list = new LinkedList<String>([1,2,3])
        ''',
        'Cannot call java.util.LinkedList#<init>(java.util.Collection<? extends java.lang.String>) with arguments [java.util.ArrayList<java.lang.Integer>]'
    }

    void testGenericAssignmentWithSubClass() {
        assertScript '''
            List<String> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        '''
    }

    void testGenericAssignmentWithSubClassAndWrongGenericType() {
        shouldFailWithMessages '''
            List<Integer> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        ''',
        'Incompatible generic argument types'
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
        ''',
        'Cannot assign java.util.ArrayList<java.lang.String> to: java.util.List<? super java.lang.Number>'
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

    // GROOVY-10765
    void testAssignmentShouldWorkForParameterizedMap2() {
        assertScript '''import java.util.function.BiFunction

            def <X, Y, Z> Map<X, Z> transform(Map<X, Y> map, BiFunction<? super X, ? super Y, Z> transformer) {
                map.collectEntries { k, v -> [k, transformer.apply(k, v)] }
            }

            Map<String, ? extends File> one = [:]
            Map<String, Integer> two = transform(one) { k, v -> v.hashCode() }
        '''
    }

    // GROOVY-10222
    void testAssignmentShouldWorkForParameterizedType() {
        assertScript '''
            class C<T> {
                def <X> X m() {
                }
                void test() {
                    T x = m() // Cannot assign value of type #X to variable of type T
                }
            }
            new C().test()
        '''
    }

    // GROOVY-10342
    void testAssignmentShouldWorkForParameterizedType2() {
        assertScript '''
            class C<T> {
                T t
            }
            def <X> X m() {
                123
            }
            def <N extends Number> void test() {
                int x = m()
                Integer y = m()
                C<Integer> z = new C<>(); z.t = m()

                C<N> c_of_n = new C<N>(); c_of_n.t = m() // Cannot assign value of type #X to variable of type N
            }
            test()
        '''

        shouldFailWithMessages '''
            def <X extends CharSequence> X m() {
            }
            def <N extends Number> void test() {
                N n = m()
            }
        ''',
        'Cannot assign value of type java.lang.CharSequence to variable of type N'
    }

    // GROOVY-10754
    void testAssignmentShouldWorkForParameterizedType3() {
        assertScript '''
            class A<T> { }
            class B extends A<Number> { }
            class C<T extends A<Number>, U extends T> {
                C(T t, U u) { }
            }

            void test(B x, B y) {
                C<? extends B, ? extends B> z = new C<>(x, y) // Cannot assign C<A<Number>,B> to C<? extends B, ? extends B>
            }
            test(null,null)
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

        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
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
        }
    }

    // GROOVY-10911
    void testMethodCallShouldWorkForProperUpperBound() {
        assertScript '''
            abstract class A {
                String name
            }
            class B extends A {
                int count
            }
            class C extends A {
                double avg
            }
            void proc(List<? extends Object> list) {
            }
            void test(A a) {
                def list
                if (a instanceof B) {
                    list = [a.name, a.count, ""]
                } else {
                    def c = a as C
                    list = [c.name, "  ", c.avg]
                }
                proc(list) // STC OOB exception!
            }
            test(new B())
            test(new C())
        '''
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
        ''',
        'Cannot find matching method FooWithGenerics#say(java.lang.Object)'
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
        ''',
        'Cannot call A#<init>(java.lang.Class<java.lang.String>, java.lang.Class<java.lang.Integer>) with arguments [java.lang.Class<java.lang.Integer>, java.lang.Class<java.lang.String>]'
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

    void testPutWithPrimitiveValue() {
        assertScript '''
            def map = new HashMap<String, Integer>()
            map.put('hello', 1)
        '''
    }

    void testPutAtWithPrimitiveValue() {
        assertScript '''
            def map = new HashMap<String, Integer>()
            map['hello'] = 1
        '''
    }

    void testPutWithWrongValueType() {
        shouldFailWithMessages '''
            def map = new HashMap<String, Integer>()
            map.put('hello', new Object())
        ''',
        'Cannot call java.util.HashMap#put(java.lang.String, java.lang.Integer) with arguments [java.lang.String, java.lang.Object]'
    }

    void testPutAtWithWrongValueType() {
        shouldFailWithMessages '''
            def map = new HashMap<Integer, Integer>()
            map[123] = new Object()
        ''',
        'Cannot call <K,V> org.codehaus.groovy.runtime.DefaultGroovyMethods#putAt(java.util.Map<K, V>, K, V) with arguments [java.util.HashMap<java.lang.Integer, java.lang.Integer>, int, java.lang.Object]'
    }

    // GROOVY-8788
    void testPutAtWithWrongValueType2() {
        shouldFailWithMessages '''
            def map = new HashMap<String, Integer>()
            map['x'] = new Object()
        ''',
        'Cannot assign value of type java.lang.Object to variable of type java.lang.Integer'
    }

    // GROOVY-9069
    void testPutAtWithWrongValueType3() {
        shouldFailWithMessages '''
            class ConfigAttribute { }
            void test(Map<String, Map<String, List<String>>> maps) {
                maps.each { String key, Map<String, List<String>> map ->
                    Map<String, List<ConfigAttribute>> value = [:]
                    maps.put(key, value)
                    maps[key] = value
                }
            }
        ''',
        'Cannot call java.util.Map#put(java.lang.String, java.util.Map<java.lang.String, java.util.List<java.lang.String>>) with arguments [java.lang.String, java.util.LinkedHashMap<java.lang.String, java.util.List<ConfigAttribute>>]',
        'Cannot assign java.util.LinkedHashMap<java.lang.String, java.util.List<ConfigAttribute>> to: java.util.Map<java.lang.String, java.util.List<java.lang.String>>'

        assertScript '''
            void test(Map<String, Map<String, List<String>>> maps) {
                maps.each { String key, Map<String, List<String>> map ->
                    Map<String, List<String>> value = [:]
                    maps.put(key, value)
                    maps[key] = value
                }
            }
        '''
    }

    // GROOVY-10576
    void testPutAllWithMapParameterUnbounded() {
        assertScript '''
            class C {
                Map<String,Object> map
                void test(Map<String,?> m) {
                    map.putAll(m) // Cannot call Map#putAll(Map<? extends String, ? extends Object>) with arguments [Map<String, ?>]
                }
            }
            def obj = new C(map:[:])
            obj.test(foo:'bar')
            def map = obj.map

            assert map == [foo:'bar']
        '''
    }

    void testMethodCallWithMapParameterUnbounded() {
        assertScript """
            import static ${this.class.name}.isEmpty
            class C {
                Map<String,?> map = new HashMap()
            }
            assert isEmpty(new C().map)
        """
    }

    // GROOVY-9460
    void testMethodCallWithClassParameterUnbounded() {
        assertScript '''
            class Bar {
                static void baz(Class<?> target) {
                }
            }
            class Foo<X> { // cannot be "T" because that matches type parameter in Class
                void test(Class<X> c) {
                    Bar.baz(c) // Cannot call Bar#baz(Class<?>) with arguments [Class<X>]
                }
            }
            new Foo<String>().test(String.class)
        '''
    }

    // GROOVY-10525
    void testMethodCallWithClassParameterUnbounded2() {
        assertScript '''
            @Grab('javax.validation:validation-api:1.1.0.Final')
            import javax.validation.Validator

            void test(Object bean, List<Class<?>> types, Validator validator) {
                validator.validate(bean, types as Class<?>[])
            }
        '''
    }

    void testShouldComplainAboutToInteger() {
        shouldFailWithMessages '''
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
        ''',
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
        ''',
        'Cannot call <X> groovy.transform.stc.GenericsSTCTest$ClassA#bar(java.lang.Class<java.lang.Long>) with arguments [java.lang.Class<? extends java.lang.Object>]'
    }

    // GROOVY-8961, GROOVY-9915
    void testShouldUseMethodGenericType3() {
        for (mods in ['', 'static']) {
            assertScript """
                $mods void setX(List<String> strings) {
                }
                x = Collections.emptyList()
            """
            assertScript """
                $mods void setX(Collection<String> strings) {
                }
                x = Collections.emptyList()
            """
            assertScript """
                $mods void setX(Iterable<String> strings) {
                }
                x = Collections.emptyList()
            """

            shouldFailWithMessages """
                $mods void setX(List<String> strings) {
                }
                x = Collections.<Integer>emptyList()
            """,
            'Incompatible generic argument types. Cannot assign java.util.List<java.lang.Integer> to: java.util.List<java.lang.String>'
        }
    }

    // GROOVY-9734, GROOVY-9915, GROOVY-11057
    void testShouldUseMethodGenericType4() {
        for (mods in ['', 'static']) {
            assertScript """
                $mods void m(List<String> strings) {
                }
                m(Collections.emptyList())
                m([])
            """
            assertScript """
                $mods void m(Collection<String> strings) {
                }
                m(Collections.emptyList())
                m([])
            """
            assertScript """
                $mods void m(Iterable<String> strings) {
                }
                m(Collections.emptyList())
                m([])
            """
            assertScript """
                $mods void m(Map<String,Long> mapping) {
                }
                m(Collections.emptyMap())
                m([:])
            """
        }
    }

    // GROOVY-11276
    void testShouldUseMethodGenericType4a() {
        assertScript '''
            def map = new HashMap<String,List<String>>()
            map.put('foo', Collections.emptyList())
            map.put('bar', [])
        '''
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
        for (toSet in ['D.&wrap', 'Collections.&singleton', '{x -> [x].toSet()}', '{Collections.singleton(it)}']) {
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
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
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

    // GROOVY-10225
    void testShouldUseMethodGenericType13() {
        assertScript '''
            def <T> T m(T t) {
                assert t == null
            }
            def <N extends Number, X extends N> void test() {
                X x = (X) null
                m(false ? x : (X) null) // was NPE
            }
            test()
        '''
    }

    // GROOVY-10315
    void testShouldUseMethodGenericType14() {
        for (args in ['m2(), c.m()', 'c.m(), m2()']) {
            assertScript """
                class C<T> {
                    def T m() {
                    }
                }
                def <X> X m2() {
                }
                def <Y> void m3(Y y1, Y y2) {
                }
                def <Z> void test(C<Z> c) {
                    m3($args)
                }
                test(new C<String>())
            """
        }
    }

    // GROOVY-10364
    void testShouldUseMethodGenericType15() {
        assertScript '''
            class A<T> {
            }
            class B<T> {
                void m(A<T> a_of_t, T t) {
                }
            }
            class C<X, Y extends X> {
                void test() {
                    B<Y> b_of_y = new B<Y>()
                    b_of_y.m(new A<Y>(), (Y) null) // Cannot call B#m(A<Y extends X>, Y) with arguments [A<Y>, Y]
                }
            }
            new C<Number,Integer>().test()
        '''
    }

    // GROOVY-10648
    void testShouldUseMethodGenericType16() {
        assertScript '''
            def <T extends Number> Set<T> test(Iterable<T> iterable) {
                final Set<T> result = new HashSet<>()
                iterable.forEach { result.add(it) }
                return result
            }
            def set = test([1,2.3])
            assert set.size() == 2
            assert 1 in set
        '''
    }

    // GROOVY-10725
    void testShouldUseMethodGenericType17() {
        assertScript '''
            List<String> list = ['foo','bar']
            Set<Map<String,String>> set_of_maps = []
            set_of_maps.addAll(list.collectEntries { [it, it.toUpperCase()] })
            assert set_of_maps.first() == [foo: 'FOO', bar: 'BAR']
        '''
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
        ''',
        'Cannot call java.util.ArrayList#addAll(java.util.Collection<? extends java.lang.String>) with arguments [java.util.Collection<java.lang.Integer>]'
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
                assert node.getNodeMetaData(INFERRED_TYPE).implementsInterface(LIST_TYPE)
                assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type instanceof LUB
            })
            def list = ["foo", "$bar"]
        '''

        shouldFailWithMessages base + '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE).implementsInterface(LIST_TYPE)
                assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type instanceof LUB
            })
            List<String> list = ["foo", "$bar"]
        ''',
        'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE).implementsInterface(LIST_TYPE)
                assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type == GSTRING_TYPE // single element means no LUB
            })
            List<String> list = ["$bar"]
        ''',
        'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            void m(List<String> list) {}
            m(["foo", "$bar"])
        ''',
        'You are trying to use a GString in place of a String'
    }

    // GROOVY-5559, GROOVY-10010
    void testGStringInMapShouldNotBeConsideredAsAString() {
        String base = 'def bar = 123'

        shouldFailWithMessages base + '''
            Map<String,String> map = [x:"foo", y:"$bar"]
        ''',
        'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            void m(Map<?,String> map) {}
            m([x:"foo", y:"$bar"])
        ''',
        'You are trying to use a GString in place of a String'

        shouldFailWithMessages base + '''
            void m(Map<?,String> map) {}
            m(x:"foo", y:"$bar")
        ''',
        'You are trying to use a GString in place of a String'
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

    // GROOVY-10373
    void testAssignNullTypeParameterWithUpperBounds2() {
        assertScript '''
            class A<I, E extends I>  {
                void bar(E y) { }
            }
            class B<T> {
            }
            class C<S extends B<Character>> {
                A<S, ? extends S> foo() {
                    (A<S, ? extends S>) null
                }
            }
            class D<I extends B<Character>> {
                void test() {
                    C<I> x = (C<I>) null
                    x?.foo()?.bar(null)
                }
            }
            new D<B<Character>>().test()
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
        ''',
        'Cannot use diamond <> with anonymous inner classes'
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
            map.put(123, new Date())
        ''',
        'Cannot call java.util.HashMap#put(java.util.Date, java.util.Date) with arguments [int, java.util.Date]'
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
            map[123] = new Date()
        ''',
        'Cannot call <K,V> org.codehaus.groovy.runtime.DefaultGroovyMethods#putAt(java.util.Map<K, V>, K, V) with arguments [java.util.HashMap<java.util.Date, java.util.Date>, int, java.util.Date]'
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
        ''',
        'Cannot call java.util.HashMap#put(java.util.Date, java.util.Date) with arguments [java.util.Date, java.lang.String]'
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
        ''',
        'Cannot call <K,V> org.codehaus.groovy.runtime.DefaultGroovyMethods#putAt(java.util.Map<K, V>, K, V) with arguments [java.util.HashMap<java.util.Date, java.util.Date>, java.util.Date, java.lang.String]'
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
                assert ift == LIST_TYPE
                def gts = ift.genericsTypes
                assert gts != null
                assert gts.length == 1
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

    // GROOVY-10220
    void testCompatibleArgumentsForPlaceholders3() {
        assertScript '''
            class C<S, T extends Number> {
            }
            class D<T> {
                C<? extends T, Integer> f
                D(C<? extends T, Integer> p) {
                    f = p
                }
            }

            assert new D<String>(null).f == null
        '''
    }

    // GROOVY-10235
    void testCompatibleArgumentsForPlaceholders4() {
        assertScript '''
            import static java.util.concurrent.ConcurrentHashMap.newKeySet

            void accept(Collection<Integer> integers) {
                assert integers
            }

            Set<Integer> integers = newKeySet()
            integers.add(42)
            accept(integers)
        '''
    }

    void testCompatibleArgumentsForPlaceholders5() {
        assertScript '''
            def <X> X foo(X x) {
            }
            def <Y> Y bar() {
            }
            def <Z> void baz() {
                this.<Z>foo(bar())
            }
            this.<String>baz()
        '''
    }

    // GROOVY-10482
    void testCompatibleArgumentsForPlaceholders6() {
        for (it in ['', 'def', 'public', 'static', '@Deprecated']) {
            assertScript """
                class Foo<X> {
                    Foo(X x) {
                    }
                }
                $it <Y> Y bar() {
                }
                $it <Z> void baz() {
                    new Foo<Z>(bar()) // Cannot call Foo#<init>(Z) with arguments [#Y]
                }
                this.<String>baz()
            """
        }
    }

    // GROOVY-10499
    void testCompatibleArgumentsForPlaceholders7() {
        for (it in ['?', 'Y', '? extends Y']) {
            assertScript """
                class Foo<X> {
                    Foo(X x) {
                    }
                }
                class Bar<Y> {
                    Bar(Foo<${it}> foo, Y y) {
                    }
                    Y baz(Y y) {
                    }
                }
                def <Z> void test(Z z = null) {
                    new Bar<>(new Foo<Z>(z), z).baz(z) // Cannot find matching method Bar#baz(Z)
                }
                test()
            """
        }
    }

    // GROOVY-10100
    void testCompatibleArgumentsForPlaceholders8() {
        assertScript '''
            import java.util.function.Function

            class C<T> {
                T m(Object... args) {
                }
            }
            def <T extends Number> void test() {
                C<T> c = new C<>()
                Function<String[], T> f = c::m
                T t = f.apply(new String[]{"string"}) // Cannot assign value of type java.lang.String[] to variable of type T
            }
            test()
        '''
    }

    // GROOVY-10115
    void testCompatibleArgumentsForPlaceholders9() {
        assertScript '''
            class C<X, T extends X> {
                T t
                C(T t) {
                    this.t = t
                }
            }
            new C(null)
        '''
    }

    // GROOVY-10116
    void testCompatibleArgumentsForPlaceholders10() {
        assertScript '''
            class Foo<X,T> {
                Foo(Bar<X,T> bar) {
                }
            }
            class Bar<X,T> {
            }
            class Baz<T> {
                void test() {
                    Foo<T,Long> x = new Foo<T,Long>(new Bar<T,Long>()) // Cannot call Foo#<init>(Bar<T,Long>) with arguments [Bar<T,Long>]
                }
            }
            new Baz().test()
        '''
    }

    // GROOVY-10153
    void testCompatibleArgumentsForPlaceholders11() {
        for (t in ['A', 'B', 'C']) {
            assertScript """
                class A {}
                class B extends A {}
                class C extends B {}
                class Foo<T extends A> {}

                Foo<? super C> foo = new Foo<$t>()
                //  ^ lower bound is C (explicit); upper bound is A (implicit)
            """
        }
    }

    // GROOVY-10270
    void testCompatibleArgumentsForPlaceholders12() {
        assertScript '''import java.util.function.*
            void a(Function<Number,Byte> fn) { }
            class B {
                B(Function<Number,Byte> fn) { }
            }
            class C<T> {
                C(T t) { }
            }

            a({ Number x -> x.byteValue() })
            new B({ Number x -> x.byteValue() })
            new C<Function<Number,Byte>>({ Number x -> x.byteValue() } as Function<Number,Byte>)
            new C<Function<Number,Byte>>({ Number x -> x.byteValue() }                         )
        '''
    }

    // GROOVY-11003
    void testCompatibleArgumentsForPlaceholders13() {
        assertScript '''
            def <T> void m(Integer x, java.util.List<T>... lists) {
            }
            void test(java.util.List<Integer> p) {
                m(1, p)
                m(2)
            }

            test([1,2,3])
        '''
    }

    void testIncompatibleArgumentsForPlaceholders1() {
        shouldFailWithMessages '''
            def <T extends Number> T test(T one, T two) { }
            test(1,"II")
        ''',
        '#test(int, java.lang.String). Please check if the declared type is correct and if the method exists.'
    }

    // GROOVY-7720
    @NotYetImplemented
    void testIncompatibleArgumentsForPlaceholders2() {
        shouldFailWithMessages '''
            class Consumer<T> {
                <U> void accept(U param) {
                    T local = param
                }
            }
        ''',
        'Cannot assign value of type U to variable of type T'

        shouldFailWithMessages '''
            class Converter<T, U> {
                U convert(T param) {
                    return param
                }
            }
        ''',
        'Cannot return value of type T for method returning U'

        shouldFailWithMessages '''
            class C<X, Y> {
            }
            def <X extends C<Number, String>> X[] m() {
                new X[]{new C<Object, String>()}
            }
        ''',
        'Cannot convert from C<java.lang.Object, java.lang.String> to X'
    }

    // GROOVY-9902: incomplete generics should not stop type checking
    void testIncompatibleArgumentsForPlaceholders3() {
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
        'Cannot call TypedProperty#eq(java.lang.String) with arguments [int]',
        'Cannot call TypedProperty#eq(java.lang.Number) with arguments [java.lang.String]'
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
            jointCompilationOptions = [memStub: true]
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

    // GROOVY-10365
    void testCorrectlyBoundedTypeParameterTypeArgument() {
        assertScript '''
            interface I {
            }
            class A<T extends Number, Y> implements I {
                double m(Integer i) {
                    i.doubleValue()
                }
            }
            class B<T extends I> {
                public int f
                double test(A<Float, ? extends T> a) { // "T" is re-purposed
                    a.m(f) // Cannot call A#m(java.lang.Integer) with arguments [int]
                }
            }
            double result = new B<I>(f:2).test(new A<>())
            assert result == 2.0d
        '''
    }

    void testOutOfBoundsByExtendsGenericParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            Foo.bar([new Object()])
        ''',
        'Cannot call <T extends java.util.List<? extends java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.ArrayList<java.lang.Object>]'
    }

    void testOutOfBoundsBySuperGenericParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        ''',
        'Cannot call <T extends java.util.List<? super java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.ArrayList<java.lang.String>]'
    }

    void testOutOfBoundsByExtendsPlaceholderParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T list) {}
            }
            def <U extends List<Object>> void baz(U list) {
                Foo.bar(list)
            }
        ''',
        'Cannot call <T extends java.util.List<? extends java.lang.CharSequence>> Foo#bar(T) with arguments [U]'
    }

    void testOutOfBoundsBySuperPlaceholderParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T list) {}
            }
            def <U extends List<String>> void baz(U list) {
                Foo.bar(list)
            }
        ''',
        'Cannot call <T extends java.util.List<? super java.lang.CharSequence>> Foo#bar(T) with arguments [U]'
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

    // GROOVY-10166
    void testShouldFindMethodEvenWithRepeatNames1() {
        assertScript '''
            abstract class A<T extends C> {
                T getC() {
                }
                Map toMap() {
                    c.getMap(this)
                }
            }

            class C<T extends A> {
                Map getMap(T a) {
                }
            }

            new C()
        '''
    }

    // GROOVY-10049, GROOVY-10166
    void testShouldFindMethodEvenWithRepeatNames2() {
        assertScript '''
            abstract class A<T extends C> {
                T getC() {
                }
            }

            class C<T extends A> {
                T get() {
                    A a = null
                    a.c.get(1)
                }
                T get(int i) {
                }
            }

            new C()
        '''
    }

    // GROOVY-10196
    void testShouldFindMethodEvenWithRepeatNames3() {
        assertScript '''
            interface M<K,V> {
            }
            interface MM<K,V> extends M<K,List<V>> {
                void add(K key, V val)
            }
            class MMA<K,V> implements MM<K,V> {
                @Override
                void add(K key, V val) {
                }
            }
            class LMM<K,V> extends MMA<K,V> {
            }

            MM<String,String> map = new LMM<>()
            map.add('foo','bar')
        '''
    }

    // GROOVY-10322
    void testShouldFindMethodEvenWithRepeatNames4() {
        assertScript '''
            class C<T> {
                def <T> T m(T t) { // this T hides T from C<T>
                    return t
                }
            }
            int x = new C<String>().m(1) // no error, param and return types are `int` not `String`
            assert x == 1
        '''
    }

    // GROOVY-10337
    void testShouldFindMethodEvenWithRepeatNames5() {
        assertScript '''
            class C<X,Y> {
                C(C<Y,? extends Y> that) {
                }
            }
            def <T> void test() {
                new C<Number,T>((C<T,T>)null) // cannot call ctor with argument C<T,T>
            }
        '''
    }

    // GROOVY-5893
    void testPlusInClosure1() {
        for (type in ['def', 'var', 'Object', 'Number', 'Integer', 'Comparable']) {
            assertScript """
                List<Integer> list = [1, 2, 3]

                int sum = 0
                list.each { int i -> sum = sum + i }
                assert sum == 6

                sum = 0
                list.each { int i -> sum += i }
                assert sum == 6

                $type  sumViaInject = list.inject(0, { int x, int y -> x + y })
                //     ^^^^^^^^^^^^ T ^^^^ E[]    ^ U      ^ T    ^ E  ^^^^^ V
                assert sumViaInject == 6
            """
        }
    }

    // GROOVY-5893, GROOVY-7934
    void testPlusInClosure2() {
        for (type in ['def', 'var', 'Object', 'String', 'Serializable']) {
            assertScript """
                String[] array = ['1', '2', '3']
                $type  joinViaInject = array.inject(0, { x,y -> (x + ',' + y) })
                //                     integer or string ^ ^ string
                assert joinViaInject == '0,1,2,3'
            """
        }
    }

    // GROOVY-10320
    void testPlusOverride() {
        assertScript '''
            interface I<T extends I> {
              T plus(T t_aka_i)
            }
            @groovy.transform.TupleConstructor(defaults=false)
            class C implements I<C> {
                final BigDecimal n
                @Override
                C plus(C c) {
                    if (!c) return this
                    new C((n ?: 0.0) + (c.n ?: 0.0))
                }
            }

            def <X extends I> X test(List<X> items) {
                X total = null
                for (it in items) {
                    if (!total)
                        total = it
                    else
                        total += it // Cannot call X#plus(T) with arguments [X]
                }
                total
            }

            def total = test([new C(1.1), new C(2.2)])
            assert total.n == 3.3
        '''
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
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
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
                static test(String[] args) {
                    // Cannot assign value of type Object to variable of type CTypeTest
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
                    def s1 = cache {
                        new CTypeTest()
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

    // GROOVY-10557
    void testReturnTypeInferenceWithClosure2() {
        assertScript '''
            import java.util.function.Function
            class C {
                def <T> T m(Function<Reader,T> function)  {
                    new StringReader("").withCloseable { reader ->
                        function.apply(reader)
                    }
                }
            }
            Object result = new C().m { it.text.empty }
            //                          ^^ StringReader
            assert result == Boolean.TRUE
        '''
    }

    // GROOVY-10436
    void testReturnTypeInferenceWithClosure3() {
        String method = '''import java.util.function.BiConsumer

            def <T> BiConsumer<String, List<T>> m(BiConsumer<String, ? super T> consumer) {
                return (String text, List<T> list) -> {
                    for (T item : list) consumer.accept(text, item)
                }
            }
        '''
        assertScript method + '''
            this.<Number>m { string, number -> number.toBigDecimal() }
        '''
        assertScript method + '''
            // the only type witness for T is the closure parameter
            m { string, Number number -> number.toBigDecimal() }
        '''
    }

    // GROOVY-10699
    void testReturnTypeInferenceWithClosure4() {
        for (type in ['Function<T,T>', 'UnaryOperator<T>']) {
            assertScript """import java.util.function.*

                <T> T m($type x) {
                    x.apply(null)
                }

                void test() {
                    // the only type witness for T is the lambda parameter
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
                    })
                    def x = m( (Double d) -> d ) // Expected type Object for lambda parameter: d
                }
                test()
            """
        }
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
            class C<T> {
                C(T a, T b) {
                }
            }
            def c_of_map = new C<Map>("a", 1)
        ''',
        'Cannot call C#<init>(java.util.Map, java.util.Map) with arguments [java.lang.String, int]'
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

    // GROOVY-5891
    void testMethodLevelGenericsForMethodCall() {
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
    }

    // GROOVY-5885
    void testMethodLevelGenericsForMethodCall2() {
        assertScript '''
            class Test {
                public <T extends Test> T castToMe(Class<T> type, Object o) {
                    return type.cast(o);
                }
            }
            def t = new Test()
            assert t == t.castToMe(Test, t)
        '''
    }

    // GROOVY-6919
    void testMethodLevelGenericsForMethodCall3() {
        assertScript '''
            interface I1 {
                String getFoo()
            }
            interface I2 {
                String getBar()
            }
            def <T extends I1 & I2> void test(T obj) {
                obj?.getFoo()
                obj?.getBar()
            }
            test(null)
        '''
    }

    // GROOVY-10846
    void testClassLevelGenericsForPropertyRead() {
        for (vis in ['','public','protected','@groovy.transform.PackageScope']) {
            assertScript """
                class A {
                    $vis String b
                }
                class C<X extends A, Y extends List<X>> {
                    void test(X x) {
                        x.b = 'blah'
                    }
                }
                A a = []
                new C<A, List<A>>().test(a)
                assert a.b == 'blah'
            """
        }
    }

    // GROOVY-6919
    void testMethodLevelGenericsForPropertyRead() {
        assertScript '''
            interface I1 {
                String getFoo()
            }
            interface I2 {
                String getBar()
            }
            def <T extends I1 & I2> void test(T obj) {
                obj?.foo
                obj?.bar
            }
            test(null)
        '''
    }

    @NotYetImplemented
    void testMethodLevelGenericsForPropertyRead2() {
        assertScript '''
            interface I1 {
                static String getFoo() { 'foo' }
            }
            interface I2 {
                String bar = 'bar'
            }
            def <T extends I1 & I2> void test(Class<T> cls) {
                cls?.foo
                cls?.bar
            }
            test(null)
        '''
    }

    void testMethodLevelGenericsForPropertyRead3() {
        assertScript '''
            interface I1 {
                String getFoo()
            }
            interface I2 {
                String getBar()
            }
            class C<T extends I1 & I2> {
                def <U extends T> void test(U obj) {
                    obj?.foo
                    obj?.bar
                }
            }
            new C().test(null)
        '''
    }

    void testMethodLevelGenericsForPropertyRead4() {
        assertScript '''
            interface I1 {
                String getFoo()
            }
            interface I2 {
                String getBar()
            }
            @groovy.transform.SelfType(I2) trait T2 {
                abstract String getBaz()
            }
            def <T extends I1 & T2> void test(T obj) {
                obj?.foo
                obj?.bar
                obj?.baz
            }
            test(null)
        '''
    }

    // GROOVY-5839
    void testMethodShadowGenerics() {
        assertScript '''
            class C<T> {
                Collection<C<T>> attached = []
                def <T> void attach(C<T> toAttach) {
                    attached.add(toAttach) // TODO: GROOVY-7719
                }
            }
            def c1 = new C<Short>()
            def c2 = new C<Long>()
            c1.attach(c2)
        '''
    }

    void testSuperClassGenerics() {
        // GROOVY-6237
        assertScript '''
            class MyList extends LinkedList<Object> {
            }
            List<Object> o = new MyList()
        '''

        shouldFailWithMessages '''
            class MyList extends LinkedList<Object> {
            }
            List<String> o = new MyList()
        ''',
        'Incompatible generic argument types. Cannot assign MyList to: java.util.List<java.lang.String>'

        // GROOVY-5873
        assertScript '''
            abstract class A<T> {
                public T value
            }
            class C extends A<Integer> {
            }
            C c = new C()
            Integer i = c.value
        '''

        // GROOVY-5920
        assertScript '''
            class Data<T> {
                T value
            }
            class StringDataIterator implements Iterator<Data<String>> {
                boolean hasNext() { true }
                Data<String> next() { new Data<String>(value: 'tim') }
            }
            Data<String> elem = new StringDataIterator().next()
            assert elem.value.length() == 3
        '''
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
    void testInjectMethodWithInitialValueChoosesTheIterableVersion() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def method = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert method.name == 'inject'
                method = method.extensionMethodNode
                assert method.parameters[0].type == ITERABLE_TYPE
            })
            def result = ['a','bb','ccc'].inject(0) { int acc, String str -> acc += str.length(); acc }
            assert result == 6
        '''
    }

    // GROOVY-6504
    void testInjectMethodWithInitialValueChoosesTheCollectionVersion() {
        assertScript '''import org.codehaus.groovy.runtime.DefaultGroovyMethods
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def method = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert method.name == 'inject'
                assert method.parameters[0].type == COLLECTION_TYPE
            })
            def result = DefaultGroovyMethods.inject(['a','bb','ccc'], 0, { int acc, String str -> acc += str.length(); acc })
            assert result == 6
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

    // GROOVY-6723, GROOVY-6415
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
        for (modifier in ['public', 'static']) {
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
            jointCompilationOptions = [memStub: true]
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
            List<String> test() {
                return [1,2,3]
            }
        ''',
        'Cannot assign java.util.ArrayList<java.lang.Integer> to: java.util.List<java.lang.String>'

        shouldFailWithMessages '''
            List<CharSequence> test() {
                return [1,2,3]
            }
        ''',
        'Cannot assign java.util.ArrayList<java.lang.Integer> to: java.util.List<java.lang.CharSequence>'
    }

    // GROOVY-10295
    void testReturnTypeChecking2() {
        assertScript '''
            List<CharSequence> test() {
                return ['foo','bar','baz']
            }
        '''
        assertScript '''
            Map<String,Object> test() {
                return [date: new Date(), string: new String()]
            }
        '''
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
        for (op in ['.', '?.', '*.']) {
            File parentDir = File.createTempDir()
            config.with {
                targetDirectory = File.createTempDir()
                jointCompilationOptions = [memStub: true]
            }
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
                    return c() // Cannot return value of type Object for method returning T
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

        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
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

    // GROOVY-10234
    void testSelfReferentialTypeParameter() {
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
        try {
            def a = new File(parentDir, 'Main.groovy')
            a.write '''
                def <T> T getBean(Class<T> beanType) {
                    { obj, Class target -> Optional.of(obj.toString()) } as Service
                }

                def result = getBean(Service).convert(new ArrayList(), String)
                assert result.get() == '[]'
            '''
            def b = new File(parentDir, 'Service.java')
            b.write '''
                import java.util.Optional;
                import java.util.function.Function;

                public interface Service<Impl extends Service> {
                    <T> Optional<T> convert(Object object, Class<T> targetType);
                    <S, T> Impl addConverter(Class<S> sourceType, Class<T> targetType, Function<S, T> typeConverter);
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('Main').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    // GROOVY-10055
    void testSelfReferentialTypeParameter2() {
        assertScript '''
            abstract class A<Self extends A<Self>> {
                Self foo(inputs) {
                    // ...
                    this
                }
            }
            abstract class B<Self extends B<Self>> extends A<Self> {
                Self bar(inputs) {
                    // ...
                    this
                }
            }
            class C<Self extends C<Self>> extends B<Self> { // see org.testcontainers.containers.PostgreSQLContainer
                Self baz(inputs) {
                    // ...
                    this
                }
            }

            new C<>()
            .foo('x')
            .bar('y') // error
            .baz('z') // error
        '''
    }

    // GROOVY-10556
    void testSelfReferentialTypeParameter3() {
        for (self in ['(B) this', 'this as B']) {
            assertScript """
                abstract class A<B extends A<B,X>,X> {
                    B m() {
                       $self
                    }
                }
                (new A(){}).m()
            """
        }
    }

    // GROOVY-9968
    void testSelfReferentialTypeParameter4() {
        assertScript '''
            abstract class A<B extends A<?>> implements Iterable<B> {
            }
            void m(A iterable) {
                iterable?.each {
                    m(it)
                }
            }
            m(null)
        '''
    }

    // GROOVY-10619
    void testSelfReferentialTypeParameter5() {
        assertScript '''
            @Grab('org.springframework.boot:spring-boot-starter-webflux:2.6.7')
            import static org.springframework.http.MediaType.APPLICATION_JSON
            import org.springframework.core.ParameterizedTypeReference

            List<BigDecimal> test(org.springframework.web.reactive.function.client.WebClient web) {
                def response = web.get()
                    .uri('/something?id={id}', 'id').accept(APPLICATION_JSON).retrieve()
                    .toEntity(new ParameterizedTypeReference<List<BigDecimal>>() {})
                    .block(java.time.Duration.ofSeconds(2))
                return response.getBody() ?: []
            }
        '''
    }

    // GROOVY-10887
    void testSelfReferentialTypeParameter6() {
        assertScript '''
            @Grab('org.springframework.integration:spring-integration-groovy:5.5.15')
            import org.springframework.integration.channel.AbstractMessageChannel
            import org.springframework.integration.dsl.MessageChannelSpec
            import org.springframework.integration.dsl.MessageChannels
            @Grab('org.springframework:spring-messaging:5.3.24')
            import org.springframework.messaging.MessageChannel

            def <AMC extends AbstractMessageChannel, MCS extends MessageChannelSpec<MCS,AMC>> MCS mark(MCS self) {
                self.interceptor(null) // returns self
            }
            MessageChannel make() {
                mark(MessageChannels.publishSubscribe(true).minSubscribers(2)).get()
            }
        '''
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

    // GROOVY-10528
    void testRawTypeGuard() {
        assertScript '''
            void test(... args) {
                args.each { object ->
                    if (object instanceof Iterable) {
                        object.each { test(it) }
                    }
                }
            }
            test(1,[2,3])
        '''
    }

    // GROOVY-10671
    void testAssertJ() {
        assertScript '''
            @Grab('org.assertj:assertj-core:3.23.1')
            import static org.assertj.core.api.Assertions.assertThat

            def strings = (Collection<String>) ['a','b']
            assertThat(strings).as('assertion description')
                .containsExactlyInAnyOrderElementsOf(['a','b'])
        '''
    }

    // GROOVY-10673, GROOVY-11057
    void testMockito() {
        assertScript '''
            @Grab('org.mockito:mockito-core:4.11.0')
            import static org.mockito.Mockito.*

            class C {
                String string
            }
            interface I {
                C f(String s)
            }

            def i = mock(I)
            when(i.f(anyString())).thenAnswer { /*InvocationOnMock*/ iom ->
                new C(string: iom.arguments[0])
                new C().tap {
                    string = iom.arguments[0]
                }
            }
            assert i.f('x') instanceof C
        '''

        for (map in ['Collections.emptyMap()', '[:]']) {
            assertScript """
                @Grab('org.mockito:mockito-core:4.11.0')
                import static org.mockito.Mockito.*

                interface Configuration {
                    Map<String,Object> getSettings()
                }

                def configuration = mock(Configuration).tap {
                    when(it.getSettings()).thenReturn($map)
                }
                assert configuration.settings.isEmpty()
            """
        }
    }

    //--------------------------------------------------------------------------

    static class MyList
        extends LinkedList<String> {
    }

    static class ClassA<T> {
        def <X> Class<X> foo(Class<X> classType) {
            return classType;
        }
        def <X> Class<X> bar(Class<T> classType) {
            return null;
        }
    }

    static class JavaClassSupport {
        static class Container<T> {
        }
        static class StringContainer extends Container<String> {
        }
        static <T> List<T> unwrap(Collection<? extends Container<T>> list) {
        }
    }

    static boolean isEmpty(Map<?,?> map) {
        map == null || map.isEmpty()
    }
}
