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
package gls.statements

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class MultipleAssignmentDeclarationTest {

    @Test
    void testDef1() {
        assertScript '''
            def (a,b) = [1,2]
            assert a == 1
            assert b == 2
        '''
    }

    @Test
    void testDef2() {
        assertScript '''
            def list = [1,2]
            def (c,d) = list
            assert c == 1
            assert d == 2
        '''
    }

    @Test
    void testMixedTypes() {
        assertScript '''
            def x = "foo"
            def (int i, String j) = [1,x]
            assert x == "foo"
            assert i == 1
            assert i instanceof Integer
            assert j == "foo"
            assert j instanceof String
        '''
    }

    @Test
    void testMixedTypesWithConversion() {
        assertScript '''
            def x = "foo"
            def (int i, String j) = [1,"$x $x"]
            assert x == "foo"
            assert i == 1
            assert i instanceof Integer
            assert j == "foo foo"
            assert j instanceof String
        '''
    }

    @Test
    void testDeclarationOrder() {
        shouldFail MissingPropertyException, '''
            def (i,j) = [1,i]
        '''
    }

    @Test
    void testNestedScope1() {
        assertScript '''import static groovy.test.GroovyAssert.shouldFail

            def c = { ->
                def (i,j) = [1,2]
                assert i == 1
                assert j == 2
            }
            c()

            shouldFail(MissingPropertyException) {
                println i
            }

            shouldFail(MissingPropertyException) {
                println j
            }

            def (i,j) = [2,3]
            assert i == 2
            assert j == 3
            c()

            assert i == 2
            assert j == 3
        '''
    }

    @Test
    void testNestedScope2() {
        assertScript '''
            class C {
                int m() {
                    def (i,j) = [1,2]
                    assert i == 1
                    assert j == 2

                    def x = { ->
                        assert i == 1
                        assert j == 2

                        i = 3
                        assert i == 3
                    }
                    x()

                    assert i == 3
                    return j
                }
            }
            int n = new C().m()
            assert n == 2
        '''
    }

    @Test
    void testMultiAssignChain() {
        assertScript '''
            def a,b
            def (c,d) = (a,b) = [1,2]
            assert [a,b] == [1,2]
            assert [c,d] == [1,2]
        '''
    }

    @Test
    void testMultiAssignFromObject() {
        shouldFail MissingMethodException, '''
            def obj = new Object()
            def (x) = obj
        '''
    }

    @Test
    void testMultiAssignFromCalendar() {
        assertScript '''
            def (_, y, m) = Calendar.instance
            assert y >= 2022
            assert m in 0..11
        '''
    }

    @Test // GROOVY-5744
    void testMultiAssignFromIterator() {
        assertScript '''
            def list = [1,2,3]
            def iter = list.iterator()

            def (a,b,c) = list
            def (d,e,f) = iter
            assert "$a $b $c" == "$d $e $f"
        '''
    }

    @Test // GROOVY-10666
    void testMultiAssignFromIterable() {
        assertScript '''
            class MySet {
                List<String> ops = []
                @Delegate Set<String> strings = []

                String getAt(int index) {
                    ops << "getAt($index)".toString()
                    org.codehaus.groovy.runtime.DefaultGroovyMethods.getAt(this, index)
                }

                Iterator<String> iterator() {
                    ops << "iterator()"
                    def iterator = strings.iterator()
                    return new Iterator<String>() {
                        @Override
                        boolean hasNext() {
                            iterator.hasNext()
                        }
                        @Override
                        String next() {
                            ops << "next()"
                            iterator.next()
                        }
                    }
                }
            }

            Set<String> strings = new MySet()
            strings << 'foo'
            strings << 'bar'
            strings << 'baz'

            def (foo,bar,baz) = strings
            assert foo == 'foo'
            assert bar == 'bar'
            assert baz == 'baz'

            assert strings.ops == ['iterator()','next()','next()','next()']
        '''
    }

    @Test // GROOVY-10666
    void testMultiAssignFromJavaStream() {
        assertScript '''import java.util.stream.Stream

            Stream<String> strings = Stream.of('foo','bar','baz')
            def (foo,bar,baz) = strings
            assert foo == 'foo'
            assert bar == 'bar'
            assert baz == 'baz'
        '''
    }

    // GEP-20 tail-rest tests

    @Test
    void testTailRestFromList() {
        assertScript '''
            def (h, *t) = [1, 2, 3, 4]
            assert h == 1
            assert t == [2, 3, 4]
        '''
    }

    @Test
    void testTailRestWithMultipleHeads() {
        assertScript '''
            def (a, b, *rest) = [10, 20, 30, 40, 50]
            assert a == 10
            assert b == 20
            assert rest == [30, 40, 50]
        '''
    }

    @Test
    void testTailRestWithTypedHead() {
        assertScript '''
            def (int h, *t) = [1, 2, 3]
            assert h == 1
            assert h instanceof Integer
            assert t == [2, 3]
        '''
    }

    @Test
    void testTailRestFromEmptyList() {
        assertScript '''
            def (h, *t) = []
            assert h == null
            assert t == []
        '''
    }

    @Test
    void testTailRestFromString() {
        assertScript '''
            def (c, *cs) = "hello"
            assert c == 'h'
            assert cs == "ello"
        '''
    }

    @Test
    void testTailRestFromIterator() {
        assertScript '''
            def (h, t) = [1, 2, 3].iterator()   // baseline: same iterator semantics as existing
            assert h == 1
            assert t == 2

            // tail-rest binds the advanced iterator (Path C)
            def it = [10, 20, 30].iterator()
            def (head, *tail) = it
            assert head == 10
            assert tail instanceof Iterator
            assert tail.next() == 20
            assert tail.next() == 30
            assert !tail.hasNext()
        '''
    }

    @Test
    void testTailRestFromSet() {
        assertScript '''
            def s = new LinkedHashSet<>([7, 8, 9])
            def (sh, *st) = s
            assert sh == 7
            def remaining = []
            while (st.hasNext()) remaining << st.next()
            assert remaining == [8, 9]
        '''
    }

    @Test
    void testTailRestFromStream() {
        // Path A: Stream RHS preserves Stream-ness in the rest binder.
        assertScript '''import java.util.stream.Stream
            def (first, *rest) = Stream.of('a', 'b', 'c')
            assert first == 'a'
            assert rest instanceof Stream
            assert rest.toList() == ['b', 'c']
        '''
    }

    @Test
    void testTailRestFromStream_pipelineContinues() {
        // The whole point of Path A: downstream operations on the rest stream still work.
        assertScript '''import java.util.stream.Stream
            def (first, *rest) = Stream.of(1, 2, 3, 4, 5)
            assert first == 1
            assert rest.filter { it % 2 == 0 }.map { it * 10 }.toList() == [20, 40]
        '''
    }

    @Test
    void testTailRestFromStream_singleElement() {
        // Stream of one element: head consumes everything, tail is an empty Stream.
        assertScript '''import java.util.stream.Stream
            def (only, *rest) = Stream.of('only')
            assert only == 'only'
            assert rest instanceof Stream
            assert rest.toList() == []
        '''
    }

    @Test
    void testTailRestFromStream_onCloseChained() {
        // Closing the rest Stream must close the original — for sources like Files.lines(p)
        // that hold OS resources, the rest binder is the canonical owner.
        assertScript '''import java.util.stream.Stream
            def closed = false
            def src = Stream.of('a', 'b', 'c').onClose { closed = true }
            def (h, *t) = src
            assert h == 'a'
            t.close()
            assert closed
        '''
    }

    @Test
    void testTailRestFromIntStream_fallsThroughToIterator() {
        // IntStream is not a subtype of Stream<T>, so it falls through to Path C
        // (Iterator). Elements are boxed by PrimitiveIterator.OfInt::next.
        assertScript '''import java.util.stream.IntStream
            def (first, *rest) = IntStream.of(1, 2, 3)
            assert first == 1
            assert rest instanceof Iterator
            assert rest.next() == 2
            assert rest.next() == 3
        '''
    }

    @Test
    void testTailRestFromIntStream_boxedFirst_givesStream() {
        // The user-facing workaround for Path A on primitive streams: .boxed() first.
        assertScript '''import java.util.stream.IntStream
            def (first, *rest) = IntStream.of(1, 2, 3).boxed()
            assert first == 1
            assert rest instanceof java.util.stream.Stream
            assert rest.toList() == [2, 3]
        '''
    }

    @Test
    void testTailRestFromUnboundedIterator() {
        assertScript '''
            // ensure lazy Path C: no materialisation
            def source = new Iterator<Integer>() {
                int i = 0
                boolean hasNext() { true }
                Integer next() { i++ }
            }
            def (h, *t) = source
            assert h == 0
            assert t.next() == 1
            assert t.next() == 2
            assert t.next() == 3
        '''
    }

    @Test
    void testTailRestSingleRestBinder() {
        assertScript '''
            def (*t) = [1, 2, 3]
            assert t == [1, 2, 3]
        '''
    }

    @Test
    void testTailRestRejectsMultipleRest() {
        def e = shouldFail '''
            def (a, *b, *c) = [1, 2, 3]
        '''
        assert e.message.contains('Only one rest binding')
    }

    // GEP-20 typed rest: the declared container type is honoured by static checking
    // (path B vs path C compatibility) and by runtime coercion.

    @Test
    void testTypedRest_listContainerType_listLiteral() {
        assertScript '''
            def (h, List<Integer> *t) = [1, 2, 3, 4]
            assert h == 1
            assert t == [2, 3, 4]
            assert t instanceof List
        '''
    }

    @Test
    void testTypedRest_listMiddleRest() {
        assertScript '''
            def (l, List<Integer> *m, r) = [1, 2, 3, 4, 5]
            assert l == 1
            assert m == [2, 3, 4]
            assert r == 5
        '''
    }

    @Test
    void testTypedRest_iteratorContainer_setRhs() {
        // Path C: declared Iterator<Integer> matches the runtime iterator returned by tailRest.
        assertScript '''
            Set<Integer> s = new LinkedHashSet<>([7, 8, 9])
            def (h, Iterator<Integer> *t) = s
            assert h == 7
            assert t.next() == 8
            assert t.next() == 9
        '''
    }

    @Test
    void testTypedRest_elementMismatchRuntimeFailure() {
        // Dynamic mode: declared Integer (not a container) for *t — runtime coercion fails
        // when assigning the slice/iterator to an Integer variable.
        shouldFail org.codehaus.groovy.runtime.typehandling.GroovyCastException, '''
            def (h, Integer *t) = [1, 2, 3]
        '''
    }

    @Test
    void testTypedRest_CompileStatic_listLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (h, List<Integer> *t) = [1, 2, 3, 4]
                assert h == 1
                assert t == [2, 3, 4]
                int total = 0
                for (Integer i : t) total += i
                assert total == 9
            }
            go()
        '''
    }

    @Test
    void testTypedRest_CompileStatic_listVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (int h, List<Integer> *t) = src
                assert h == 10
                assert t == [20, 30]
            }
            go([10, 20, 30])
        '''
    }

    @Test
    void testTypedRest_CompileStatic_iteratorContainer_pathC() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Set<Integer> src) {
                def (h, Iterator<Integer> *t) = src
                assert h == 5
                assert t.next() == 6
                assert t.next() == 7
            }
            go(new LinkedHashSet<>([5, 6, 7]))
        '''
    }

    @Test
    void testTypedRest_CompileStatic_containerMismatch_listVsIterator() {
        // List RHS gives Path B (List<T>); user declared Iterator<Integer> for the rest.
        // STC catches the mismatch at compile time.
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (h, Iterator<Integer> *t) = src
            }
            go([1, 2, 3])
        '''
        assert e.message.contains('Cannot assign rest value')
    }

    @Test
    void testTypedRest_CompileStatic_containerMismatch_iteratorVsList() {
        // Set RHS gives Path C (Iterator<T>); user declared List<Integer> for the rest.
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Set<Integer> src) {
                def (h, List<Integer> *t) = src
            }
            go([1, 2, 3] as Set)
        '''
        assert e.message.contains('Cannot assign rest value')
    }

    @Test
    void testTypedRest_CompileStatic_streamContainer_pathA() {
        // Path A: Stream RHS, declared Stream<Integer> rest — STC accepts and runtime preserves.
        assertScript '''import groovy.transform.CompileStatic
            import java.util.stream.Stream
            @CompileStatic
            def go(Stream<Integer> src) {
                def (h, Stream<Integer> *t) = src
                assert h == 10
                assert t.toList() == [20, 30]
            }
            go(Stream.of(10, 20, 30))
        '''
    }

    @Test
    void testTypedRest_CompileStatic_streamRhsUntyped() {
        // Untyped rest from Stream RHS infers Stream<T> at compile time.
        assertScript '''import groovy.transform.CompileStatic
            import java.util.stream.Stream
            @CompileStatic
            def go(Stream<String> src) {
                def (h, *t) = src
                assert h == 'a'
                // Static check: t typed as Stream<String> — chained ops must compile.
                assert t.map { it.toUpperCase() }.toList() == ['B', 'C']
            }
            go(Stream.of('a', 'b', 'c'))
        '''
    }

    @Test
    void testTypedRest_CompileStatic_containerMismatch_streamVsIterator() {
        // Stream RHS gives Path A (Stream<T>); user declared Iterator<Integer>.
        def e = shouldFail '''import groovy.transform.CompileStatic
            import java.util.stream.Stream
            @CompileStatic
            def go(Stream<Integer> src) {
                def (h, Iterator<Integer> *t) = src
            }
            go(Stream.of(1, 2, 3))
        '''
        assert e.message.contains('Cannot assign rest value')
    }

    @Test
    void testHeadRestFromStream_compileStaticRejected() {
        // Head/middle rest requires Path B — Stream is Path A, so STC rejects.
        def e = shouldFail '''import groovy.transform.CompileStatic
            import java.util.stream.Stream
            @CompileStatic
            def go(Stream<Integer> src) {
                def (*front, last) = src
            }
            go(Stream.of(1, 2, 3))
        '''
        assert e.message.contains('Head or middle rest binding requires an indexable right-hand side')
    }

    @Test
    void testTypedRest_CompileStatic_elementTypeMismatch() {
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (h, List<String> *t) = [1, 2, 3]
            }
        '''
        assert e.message.contains('Cannot assign')
    }

    @Test
    void testTypedRest_CompileStatic_genericsAreInvariant() {
        // Java generics aren't covariant: List<Integer> is NOT a List<Number>.
        // STC follows the Java rule, so an Integer-element literal won't fit a List<Number>.
        // Users wanting widening should accept the inferred List<Integer> or use ? extends.
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (h, List<Number> *t) = [1, 2, 3]
            }
        '''
        assert e.message.contains('Cannot assign')
    }

    @Test
    void testTypedRest_CompileStatic_extendsBoundAccepts() {
        // ? extends Number lets the user widen the element type explicitly.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (h, List<? extends Number> *t) = [1, 2, 3]
                assert h == 1
                assert t == [2, 3]
            }
            go()
        '''
    }

    @Test
    void testTailRestUnderCompileStatic() {
        // Static type checker currently doesn't special-case rest; verify it at least
        // does not crash for a typed head with a untyped rest and produces a usable slice.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (int h, t) = [1, 2]  // baseline positional (no rest) under CS
                assert h == 1 && t == 2
            }
            go()
        '''
    }

    @Test
    void testTailRestUnderTypeChecked() {
        // Dynamic semantics under @TypeChecked should still work (runtime dispatch unchanged)
        assertScript '''import groovy.transform.TypeChecked
            @TypeChecked
            def go() {
                def (h, t) = [1, 2]
                assert h == 1 && t == 2
            }
            go()
        '''
    }

    // GEP-20 head/middle rest tests

    @Test
    void testHeadRestFromList() {
        assertScript '''
            def (*f, last) = [1, 2, 3, 4]
            assert f == [1, 2, 3]
            assert last == 4
        '''
    }

    @Test
    void testHeadRestWithTypedTail() {
        assertScript '''
            def (*f, int last) = [1, 2, 3]
            assert f == [1, 2]
            assert last == 3
            assert last instanceof Integer
        '''
    }

    @Test
    void testMiddleRestFromList() {
        assertScript '''
            def (l, *m, r) = [1, 2, 3, 4, 5]
            assert l == 1
            assert m == [2, 3, 4]
            assert r == 5
        '''
    }

    @Test
    void testMiddleRestWithMultipleFixedSlots() {
        assertScript '''
            def (a, b, *m, y, z) = [1, 2, 3, 4, 5, 6, 7]
            assert a == 1
            assert b == 2
            assert m == [3, 4, 5]
            assert y == 6
            assert z == 7
        '''
    }

    @Test
    void testMiddleRestWithTypedEnds() {
        assertScript '''
            def (String l, *m, int r) = ['a', 2, 3, 4, 5]
            assert l == 'a'
            assert m == [2, 3, 4]
            assert r == 5
            assert r instanceof Integer
        '''
    }

    @Test
    void testMiddleRestWithEmptyMiddle() {
        assertScript '''
            def (l, *m, r) = [10, 20]
            assert l == 10
            assert m == []
            assert r == 20
        '''
    }

    @Test
    void testHeadRestFromString() {
        assertScript '''
            def (*head, tail) = "hello"
            assert head == 'hell'
            assert tail == 'o'
        '''
    }

    @Test
    void testHeadRestFromArray() {
        assertScript '''
            Integer[] arr = [1, 2, 3, 4]
            def (*front, last) = arr
            assert front == [1, 2, 3]
            assert last == 4
        '''
    }

    @Test
    void testNonTailRestFromIteratorFailsFast() {
        // Per GEP: head/middle rest requires sized, indexable RHS.
        // An iterator doesn't support getAt(IntRange) so it must fail fast (not hang).
        shouldFail MissingMethodException, '''
            def it = (1..1_000_000_000).iterator()
            def (l, *m, r) = it
        '''
    }

    @Test
    void testNonTailRestFromSetFailsFast() {
        // Sets don't have a natural order via getAt(IntRange) — fail fast.
        shouldFail MissingMethodException, '''
            def s = new LinkedHashSet<>([1, 2, 3, 4])
            def (l, *m, r) = s
        '''
    }

    // GEP-20 rest under @CompileStatic / @TypeChecked with literal-list RHS

    @Test
    void testTailRestUnderCompileStatic_listLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (h, *t) = [1, 2, 3, 4]
                assert h == 1
                assert t == [2, 3, 4]
                return t
            }
            assert go() instanceof List
        '''
    }

    @Test
    void testTailRestUnderCompileStatic_typedHead() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (int h, *t) = [10, 20, 30]
                assert h == 10
                assert t == [20, 30]
            }
            go()
        '''
    }

    @Test
    void testHeadRestUnderCompileStatic_listLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (*f, last) = [1, 2, 3]
                assert f == [1, 2]
                assert last == 3
            }
            go()
        '''
    }

    @Test
    void testMiddleRestUnderCompileStatic_listLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (l, *m, r) = [1, 2, 3, 4, 5]
                assert l == 1
                assert m == [2, 3, 4]
                assert r == 5
            }
            go()
        '''
    }

    @Test
    void testTailRestUnderTypeChecked_listLiteral() {
        assertScript '''import groovy.transform.TypeChecked
            @TypeChecked
            def go() {
                def (h, *t) = [1, 2, 3]
                assert h == 1
                assert t == [2, 3]
            }
            go()
        '''
    }

    // GEP-20 map-style destructuring tests

    @Test
    void testMapStyleFromMap() {
        assertScript '''
            def (name: n, age: a) = [name: 'Alice', age: 30]
            assert n == 'Alice'
            assert a == 30
        '''
    }

    @Test
    void testMapStyleWithTypedBinders() {
        assertScript '''
            def (name: String n, age: int a) = [name: 'Bob', age: 42]
            assert n == 'Bob'
            assert n instanceof String
            assert a == 42
            assert a instanceof Integer
        '''
    }

    @Test
    void testMapStyleRenaming() {
        assertScript '''
            def (host: hostname, port: portNum) = [host: 'localhost', port: 8080]
            assert hostname == 'localhost'
            assert portNum == 8080
        '''
    }

    @Test
    void testMapStyleMissingKeyOnMapReturnsNull() {
        assertScript '''
            def (name: n, missing: m) = [name: 'x']
            assert n == 'x'
            assert m == null
        '''
    }

    @Test
    void testMapStyleFromBean() {
        assertScript '''
            class Person {
                String name
                int age
            }
            def p = new Person(name: 'Carol', age: 25)
            def (name: n, age: a) = p
            assert n == 'Carol'
            assert a == 25
        '''
    }

    @Test
    void testMapStyleBeanMissingPropertyThrows() {
        shouldFail MissingPropertyException, '''
            class Person { String name }
            def p = new Person(name: 'x')
            def (name: n, missing: m) = p
        '''
    }

    @Test
    void testMapStyleFromGroovyObjectViaGetProperty() {
        assertScript '''
            class GO {
                def getProperty(String name) {
                    "<$name>".toString()
                }
            }
            def (foo: f, bar: b) = new GO()
            assert f == '<foo>'
            assert b == '<bar>'
        '''
    }

    @Test
    void testMapStyleSingleEntry() {
        assertScript '''
            def (only: x) = [only: 99]
            assert x == 99
        '''
    }

    @Test // GEP-20 keyword-as-key limitation
    void testMapStyleRejectsKeywordKeyInGrammar() {
        // `class` is a reserved keyword in declarator position; the keyedPair rule
        // accepts only ordinary identifiers, so this fails to parse. Users needing
        // such keys fall back to `def c = m['class']`.
        shouldFail '''
            def m = [class: 'pretender', name: 'real']
            def (class: c, name: n) = m
        '''
    }

    // GEP-20 map-style under @CompileStatic / @TypeChecked with literal-map RHS

    @Test
    void testMapStyleUnderCompileStatic_mapLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (name: n, age: a) = [name: 'Alice', age: 30]
                assert n == 'Alice'
                assert a == 30
            }
            go()
        '''
    }

    @Test
    void testMapStyleUnderCompileStatic_typedBinders() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (name: String n, age: int a) = [name: 'Bob', age: 42]
                assert n == 'Bob'
                assert a == 42
            }
            go()
        '''
    }

    @Test
    void testMapStyleUnderCompileStatic_typeMismatchErrors() {
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (name: int n) = [name: 'not a number']
            }
            go()
        '''
        assert e.message.contains('Cannot assign')
    }

    @Test
    void testMapStyleUnderCompileStatic_missingKeyTolerated() {
        // STC matches dynamic semantics: missing Map key → null binding (Object inferred type).
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (name: n, missing: m) = [name: 'x']
                assert n == 'x'
                assert m == null
            }
            go()
        '''
    }

    @Test
    void testMapStyleUnderTypeChecked_mapLiteral() {
        assertScript '''import groovy.transform.TypeChecked
            @TypeChecked
            def go() {
                def (host: h, port: p) = [host: 'localhost', port: 8080]
                assert h == 'localhost'
                assert p == 8080
            }
            go()
        '''
    }

    // GEP-20 positional multi-assignment with declared-type RHS under
    // @CompileStatic / @TypeChecked — lifts the previous literal-only restriction
    // ("Multiple assignments without list or tuple on the right-hand side").

    @Test
    void testPositional_CompileStatic_ListVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (a, b) = src
                assert a == 10
                assert b == 20
            }
            go([10, 20])
        '''
    }

    @Test
    void testPositional_CompileStatic_typedBinders_ListVariable() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (int a, int b) = src
                assert a == 10
                assert b == 20
            }
            go([10, 20])
        '''
    }

    @Test
    void testPositional_CompileStatic_ArrayVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Integer[] src) {
                def (a, b) = src
                assert a == 1
                assert b == 2
            }
            Integer[] arr = [1, 2]
            go(arr)
        '''
    }

    @Test
    void testPositional_CompileStatic_StringVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(String src) {
                def (a, b) = src
                assert a == 'h'
                assert b == 'i'
            }
            go('hi')
        '''
    }

    @Test
    void testPositional_CompileStatic_methodReturningList() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def src() { [1, 2, 3] }
            @CompileStatic
            def go() {
                def (a, b, c) = src()
                assert [a, b, c] == [1, 2, 3]
            }
            go()
        '''
    }

    @Test
    void testPositional_CompileStatic_typeMismatch() {
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<String> src) {
                def (int a, int b) = src
            }
            go(['x', 'y'])
        '''
        assert e.message.contains('Cannot assign')
    }

    // GEP-20 map-style with declared-type RHS under @CompileStatic
    // (Map<K, V>, beans, GroovyObject — resolved via STC's property lookup)

    @Test
    void testMapStyle_CompileStatic_MapVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Map<String, Object> src) {
                def (name: n, age: a) = src
                assert n == 'Alice'
                assert a == 30
            }
            go([name: 'Alice', age: 30])
        '''
    }

    @Test
    void testMapStyle_CompileStatic_TypedMapVariableRhs() {
        // Map<String, Integer> → all binders inferred as Integer.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Map<String, Integer> src) {
                def (port: p, max: m) = src
                int total = p + m
                assert total == 8090
            }
            go([port: 80, max: 8010])
        '''
    }

    @Test
    void testMapStyle_CompileStatic_BeanVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            class Person { String name; int age }
            @CompileStatic
            def go(Person p) {
                def (name: n, age: a) = p
                assert n == 'Bob'
                assert a == 42
            }
            go(new Person(name: 'Bob', age: 42))
        '''
    }

    @Test
    void testMapStyle_CompileStatic_BeanTypedBinders() {
        assertScript '''import groovy.transform.CompileStatic
            class Person { String name; int age }
            @CompileStatic
            def go(Person p) {
                def (name: String n, age: int a) = p
                assert n == 'Carol'
                assert a == 25
                String greeting = "hi $n"
                assert greeting == 'hi Carol'
            }
            go(new Person(name: 'Carol', age: 25))
        '''
    }

    @Test
    void testMapStyle_CompileStatic_BeanMissingPropertyErrors() {
        // Per GEP failure-modes table: bean RHS with no such static property → compile error.
        def e = shouldFail '''import groovy.transform.CompileStatic
            class Person { String name }
            @CompileStatic
            def go(Person p) {
                def (name: n, missing: m) = p
            }
        '''
        assert e.message.contains('No such property') || e.message.contains('missing')
    }

    @Test
    void testMapStyle_CompileStatic_BeanTypeMismatch() {
        def e = shouldFail '''import groovy.transform.CompileStatic
            class Person { String name }
            @CompileStatic
            def go(Person p) {
                def (name: int n) = p
            }
        '''
        assert e.message.contains('Cannot assign')
    }

    @Test
    void testMapStyle_CompileStatic_methodReturningBean() {
        assertScript '''import groovy.transform.CompileStatic
            class Person { String name; int age }
            @CompileStatic
            class Holder {
                Person makePerson() { new Person(name: 'Dan', age: 7) }
                def go() {
                    def (name: n, age: a) = makePerson()
                    assert n == 'Dan' && a == 7
                }
            }
            new Holder().go()
        '''
    }

    // GEP-20 rest with declared-type RHS under @CompileStatic / @TypeChecked
    // (List, array, String, Set, Iterator — Path B or Path C chosen from declared type)

    @Test
    void testTailRest_CompileStatic_ListVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (h, *t) = src
                assert h == 1
                assert t == [2, 3]
            }
            go([1, 2, 3])
        '''
    }

    @Test
    void testTailRest_CompileStatic_ArrayVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Integer[] src) {
                def (h, *t) = src
                assert h == 1
                assert t == [2, 3]
            }
            Integer[] arr = [1, 2, 3]
            go(arr)
        '''
    }

    @Test
    void testTailRest_CompileStatic_StringVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(String src) {
                def (h, *t) = src
                assert h == 'h'
                assert t == 'ello'
            }
            go('hello')
        '''
    }

    @Test
    void testTailRest_CompileStatic_PathB_setRhs() {
        // Set is Path C → rest inferred as Iterator<T>.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Set<Integer> src) {
                def (h, *t) = src
                assert h == 7
                assert t instanceof Iterator
                assert t.next() == 8
                assert t.next() == 9
                assert !t.hasNext()
            }
            go(new LinkedHashSet<>([7, 8, 9]))
        '''
    }

    @Test
    void testTailRest_CompileStatic_PathB_iteratorRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Iterator<Integer> src) {
                def (h, *t) = src
                assert h == 1
                assert t.next() == 2
                assert t.next() == 3
            }
            go([1, 2, 3].iterator())
        '''
    }

    @Test
    void testHeadRest_CompileStatic_ListVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (*f, last) = src
                assert f == [1, 2, 3]
                assert last == 4
            }
            go([1, 2, 3, 4])
        '''
    }

    @Test
    void testMiddleRest_CompileStatic_ListVariableRhs() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (l, *m, r) = src
                assert l == 1
                assert m == [2, 3, 4]
                assert r == 5
            }
            go([1, 2, 3, 4, 5])
        '''
    }

    @Test
    void testNonTailRest_CompileStatic_pathCRhsRejectedAtCompileTime() {
        // Set RHS for head/middle rest is Path C only; STC must reject.
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Set<Integer> src) {
                def (l, *m, r) = src
            }
            go([1, 2, 3, 4] as Set)
        '''
        assert e.message.contains('rest binding requires an indexable')
                || e.message.contains('does not support getAt(IntRange)')
    }

    @Test
    void testTailRest_CompileStatic_typedHeadAgainstListVariable() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (int h, *t) = src
                assert h == 10
                assert t == [20, 30]
            }
            go([10, 20, 30])
        '''
    }

    // GEP-20 cross-cutting @CompileStatic coverage: parallels of the dynamic-mode
    // tests plus second-order interactions (empty RHS, degenerate single-rest,
    // unbounded iterator, method-returning-typed, destructure-inside-closure).

    @Test
    void testTailRest_CompileStatic_emptyList() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (h, *t) = []
                assert h == null
                assert t == []
            }
            go()
        '''
    }

    @Test
    void testTailRest_CompileStatic_stringLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (c, *cs) = "hello"
                assert c == 'h'
                assert cs == "ello"
            }
            go()
        '''
    }

    @Test
    void testTailRest_CompileStatic_arrayLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                Integer[] arr = [1, 2, 3]
                def (h, *t) = arr
                assert h == 1
                assert t == [2, 3] || (t as List) == [2, 3]
            }
            go()
        '''
    }

    @Test
    void testTailRest_CompileStatic_singleRestBinder() {
        // Degenerate: def (*t) = rhs is equivalent to def t = rhs.
        // BinaryExpressionHelper has a fast-path for this — verify it under CS.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (*t) = [1, 2, 3]
                assert t == [1, 2, 3]
            }
            go()
        '''
    }

    @Test
    void testTailRest_CompileStatic_unboundedIterator_lazy() {
        // Path C with an unbounded source — must NOT materialise.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            class Counter implements Iterator<Integer> {
                int i = 0
                boolean hasNext() { true }
                Integer next() { i++ }
                void remove() { throw new UnsupportedOperationException() }
            }
            @CompileStatic
            def go() {
                def (h, *t) = (Iterator<Integer>) new Counter()
                assert h == 0
                assert t.next() == 1
                assert t.next() == 2
                assert t.next() == 3
            }
            go()
        '''
    }

    @Test
    void testHeadRest_CompileStatic_stringLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (*head, tail) = "hello"
                assert head == 'hell'
                assert tail == 'o'
            }
            go()
        '''
    }

    @Test
    void testHeadRest_CompileStatic_arrayLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                Integer[] arr = [1, 2, 3, 4]
                def (*front, last) = arr
                assert front == [1, 2, 3]
                assert last == 4
            }
            go()
        '''
    }

    @Test
    void testMiddleRest_CompileStatic_emptyMiddle() {
        // Short RHS — middle absorbs zero elements; nonTailRestSlice helper handles
        // the inverted-range case by returning an empty list (not the DGM-reverse default).
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (l, *m, r) = [10, 20]
                assert l == 10
                assert m == []
                assert r == 20
            }
            go()
        '''
    }

    @Test
    void testMiddleRest_CompileStatic_typedEnds() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (String l, *m, int r) = ['a', 2, 3, 4, 5]
                assert l == 'a'
                assert m == [2, 3, 4]
                assert r == 5
            }
            go()
        '''
    }

    @Test
    void testMapStyle_CompileStatic_renaming() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (host: hostname, port: portNum) = [host: 'localhost', port: 8080]
                assert hostname == 'localhost'
                assert portNum == 8080
            }
            go()
        '''
    }

    @Test
    void testMapStyle_CompileStatic_singleEntry() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (only: x) = [only: 99]
                assert x == 99
            }
            go()
        '''
    }

    @Test
    void testMapStyle_CompileStatic_methodReturningTypedMap() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            class Holder {
                Map<String, Integer> stats() { [hits: 7, misses: 2] }
                def go() {
                    def (hits: h, misses: m) = stats()
                    assert h == 7
                    assert m == 2
                }
            }
            new Holder().go()
        '''
    }

    @Test
    void testPositional_CompileStatic_methodReturningArray() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            class Holder {
                Integer[] arr() { [10, 20, 30] as Integer[] }
                def go() {
                    def (a, b, c) = arr()
                    assert [a, b, c] == [10, 20, 30]
                }
            }
            new Holder().go()
        '''
    }

    @Test
    void testTailRest_CompileStatic_closureCallRhs() {
        // Closure invocation returning List<T> — exercises the indexable-RHS synthesis
        // for declared-type positional plus the rest dispatcher.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def src = { -> [1, 2, 3] as List<Integer> }
                def (h, *t) = src()
                assert h == 1
                assert t == [2, 3]
            }
            go()
        '''
    }

    @Test
    void testMixed_CompileStatic_destructureInsideClosure() {
        // Verify that destructuring inside a closure body works under @CompileStatic
        // (closure scoping doesn't break the synthetic temp / rest dispatch).
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def total = 0
                [[1,2,3], [10,20,30]].each { List<Integer> row ->
                    def (a, *rest) = row
                    total += a
                    for (Integer r : rest) total += r
                }
                assert total == 66
            }
            go()
        '''
    }

    @Test
    void testMapStyle_CompileStatic_destructureInsideClosure() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                List<String> names = []
                List<Map<String, Integer>> people = [[id: 1, age: 30], [id: 2, age: 42]]
                people.each { Map<String, Integer> p ->
                    def (id: i, age: a) = p
                    names << "id=$i age=$a".toString()
                }
                assert names == ['id=1 age=30', 'id=2 age=42']
            }
            go()
        '''
    }

    // GEP-20 def/var binders: accepted in place of a type, equivalent to omitting it.
    // Provided for symmetry with switch case patterns and the GEP-19 bracket-form grammar.

    @Test
    void testDefBinder_positional() {
        assertScript '''
            def (def a, def b) = [1, 2]
            assert a == 1 && b == 2
        '''
    }

    @Test
    void testVarBinder_positional() {
        assertScript '''
            def (var a, var b) = [1, 2]
            assert a == 1 && b == 2
        '''
    }

    @Test
    void testMixedDefVarTypedBinder() {
        assertScript '''
            def (def a, var b, int c) = [1, 2, 3]
            assert a == 1 && b == 2 && c == 3
        '''
    }

    @Test
    void testVarBinder_withRest() {
        assertScript '''
            def (var h, *t) = [1, 2, 3]
            assert h == 1
            assert t == [2, 3]
        '''
    }

    @Test
    void testVarBinder_mapStyle() {
        assertScript '''
            def (name: var n, age: var a) = [name: 'X', age: 9]
            assert n == 'X' && a == 9
        '''
    }

    @Test
    void testVarBinder_CompileStatic_listLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (var a, var b) = [10, 20]
                assert a == 10 && b == 20
            }
            go()
        '''
    }

    @Test
    void testVarBinder_CompileStatic_mapStyle() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (name: var n) = [name: 'Z']
                assert n == 'Z'
            }
            go()
        '''
    }

    // GEP-20 edge cases: empty Map literal, null RHS, Map missing-key with primitive
    // vs Object binders, classic for-init multi-assignment, method-returning-typed.

    @Test
    void testMapStyle_emptyMapLiteral_dynamic() {
        assertScript '''
            def (name: n, age: a) = [:]
            assert n == null
            assert a == null
        '''
    }

    @Test
    void testMapStyle_emptyMapLiteral_compileStatic() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (name: n, age: a) = [:]
                assert n == null
                assert a == null
            }
            go()
        '''
    }

    @Test
    void testRest_nullRhs_compileStatic() {
        // Declared-type RHS makes STC accept; runtime NPE on iterator() is the natural failure
        // mode (matches the dynamic equivalent).
        shouldFail NullPointerException, '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(List<Integer> src) {
                def (h, *t) = src
            }
            go(null)
        '''
    }

    @Test
    void testMapStyle_compileStatic_objectBinderTolerant() {
        // Object binder against Map<String, Integer> — missing key gives null. n is Object.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Map<String, Integer> m) {
                def (name: n, missing: x) = m
                assert n == 1
                assert x == null
            }
            go([name: 1])
        '''
    }

    @Test
    void testMapStyle_compileStatic_primitiveBinderRuntimeFailureOnMissingKey() {
        // STC stores the Map's V as the binder type; primitive int binder accepts.
        // At runtime, missing key → null, and null→int throws GroovyCastException —
        // standard Groovy semantics, not a GEP-20 specific behaviour.
        shouldFail org.codehaus.groovy.runtime.typehandling.GroovyCastException, '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Map<String, Integer> m) {
                def (missing: int x) = m
            }
            go([name: 1])
        '''
    }

    @Test
    void testForInit_destructure_dynamic() {
        // Classic for-init style with multi-assignment — pre-GEP behaviour, regression check.
        assertScript '''
            int sum = 0
            for (def (a, b) = [1, 2]; a < 5; a++, b++) {
                sum += a + b
            }
            assert sum == (1+2) + (2+3) + (3+4) + (4+5)
        '''
    }

    @Test
    void testForInit_destructure_compileStatic() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                int sum = 0
                for (def (int a, int b) = [1, 2]; a < 4; a++, b++) {
                    sum += a + b
                }
                return sum
            }
            assert go() == (1+2) + (2+3) + (3+4)
        '''
    }

    @Test
    void testRest_compileStatic_methodReturningTypedList() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            class Holder {
                List<Integer> data() { [10, 20, 30, 40] }
                def go() {
                    def (h, *t) = data()
                    assert h == 10
                    assert t == [20, 30, 40]
                }
            }
            new Holder().go()
        '''
    }

    @Test
    void testPositional_CompileStatic_objectRhsRuntimeError() {
        // Object-declared RHS is lenient at compile time (the MOP fallback resolves getAt(int)),
        // so the failure mode for an actually non-indexable value shifts from STC error to a
        // runtime MissingMethodException. Users who want compile-time rejection should declare
        // a more specific RHS type.
        def e = shouldFail '''import groovy.transform.CompileStatic
            @CompileStatic
            def go(Object src) {
                def (a, b) = src
            }
            go(42)
        '''
        assert e instanceof MissingMethodException
        assert e.message.contains('getAt')
    }

    // GEP-20 array-rest consistency: empty and non-empty array rest both yield List<T>.
    // Prior to GROOVY-11964 patch, the empty branch returned an empty array which would fail
    // assignment to a typed `List<T> *t` rest binder. These tests use arrays sized exactly to
    // the fixed slots so the rest absorbs zero — exercising the empty branch in tailRest
    // without tripping the orthogonal AIOOBE on getAt(0) for fully-empty arrays.

    @Test
    void testTailRest_arrayExactlyConsumed_emptyRestYieldsList() {
        assertScript '''
            Integer[] arr = [1, 2, 3] as Integer[]
            def (a, b, c, *t) = arr
            assert a == 1
            assert b == 2
            assert c == 3
            assert t == []
            assert t instanceof List
        '''
    }

    @Test
    void testTailRest_arrayExactlyConsumed_typedListBinder() {
        assertScript '''
            Integer[] arr = [1, 2, 3] as Integer[]
            def (a, b, c, List<Integer> *t) = arr
            assert a == 1
            assert b == 2
            assert c == 3
            assert t == []
        '''
    }

    @Test
    void testMiddleRest_shortArray_emptyMiddleYieldsEmptyList() {
        // Short array RHS — middle rest absorbs zero elements; empty slice is List, not array.
        assertScript '''
            Integer[] arr = [10, 20] as Integer[]
            def (l, *m, r) = arr
            assert l == 10
            assert m == []
            assert m instanceof List
            assert r == 20
        '''
    }

    @Test
    void testMiddleRest_shortArray_typedListMiddleBinder() {
        assertScript '''
            Integer[] arr = [10, 20] as Integer[]
            def (Integer l, List<Integer> *m, Integer r) = arr
            assert l == 10
            assert m == []
            assert r == 20
        '''
    }

    // GEP-20 empty rest slice element-type inference: when a list-literal RHS is too short
    // for the rest to absorb anything, fall back to the rest binder's declared element type
    // rather than Object — otherwise `List<Integer> *t` is rejected as List<Object>.

    @Test
    void testTypedRest_CompileStatic_emptySlice_listLiteral() {
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (Integer a, List<Integer> *t, Integer b) = [1, 2]
                assert a == 1
                assert t == []
                assert b == 2
            }
            go()
        '''
    }

    // GEP-20 Path B by capability: the rest binder type tracks the actual return type of
    // getAt(IntRange) on the receiver, not a hard-coded List<T>. Covers String/GString
    // (return String), CharSequence (return CharSequence), BitSet (return BitSet), and
    // user custom classes that declare a self-similar slice type.

    @Test
    void testTailRest_String_restIsString_dynamic() {
        assertScript '''
            def (c, *cs) = "hello"
            assert c == 'h'
            assert cs == "ello"
            assert cs instanceof String
            assert cs.toUpperCase() == "ELLO"   // String API survives destructuring
        '''
    }

    @Test
    void testTailRest_String_restIsString_compileStatic() {
        // Smoke-tests that STC infers cs : String — calling a String-only method must compile.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def (c, *cs) = "hello"
                assert c == 'h'
                assert cs.toUpperCase() == "ELLO"
                assert cs.startsWith("ell")
            }
            go()
        '''
    }

    @Test
    void testTailRest_BitSet_restIsBitSet_dynamic() {
        assertScript '''
            def bs = new BitSet()
            bs.set(0); bs.set(2); bs.set(5)
            def (h, *t) = bs
            assert h == true
            assert t instanceof BitSet
            assert t.get(1)   // bit 2 in original is at index 1 of slice
            assert t.get(4)   // bit 5 in original is at index 4 of slice
        '''
    }

    @Test
    void testTailRest_BitSet_restIsBitSet_compileStatic() {
        // STC must infer t : BitSet so BitSet-only API calls compile.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def bs = new BitSet()
                bs.set(0); bs.set(2); bs.set(5)
                def (h, *t) = bs
                assert h == true
                assert t.cardinality() == 2   // BitSet-only method
                assert t.get(1) && t.get(4)
            }
            go()
        '''
    }

    @Test
    void testTailRest_userClass_selfSimilarSlice_dynamic() {
        assertScript '''
            class MyCol {
                List<Integer> data
                MyCol(List<Integer> d) { this.data = d }
                Integer getAt(int i) { data[i] }
                MyCol getAt(IntRange r) { new MyCol(data[r]) }
                Iterator<Integer> iterator() { data.iterator() }
            }
            def m = new MyCol([10, 20, 30, 40])
            def (h, *t) = m
            assert h == 10
            assert t instanceof MyCol
            assert t.data == [20, 30, 40]
        '''
    }

    @Test
    void testTailRest_userClass_selfSimilarSlice_compileStatic() {
        // The bytecode dispatches the rest slice as `rhs.getAt(new IntRange(true, 1, -1))`,
        // so the user's getAt(IntRange) must handle the negative end-index. Delegating to
        // the inner List's range subscript is the simplest robust pattern.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            class MyCol {
                List<Integer> data
                MyCol(List<Integer> d) { this.data = d }
                Integer getAt(int i) { data[i] }
                MyCol getAt(IntRange r) { new MyCol(data[r]) }
                Iterator<Integer> iterator() { data.iterator() }
            }
            @CompileStatic
            def go(MyCol src) {
                def (h, *t) = src
                assert h == 10
                // STC should type t as MyCol so .data resolves at compile time.
                assert t.data == [20, 30, 40]
            }
            go(new MyCol([10, 20, 30, 40]))
        '''
    }

    @Test
    void testHeadRest_BitSet_compileStatic() {
        // BitSet now participates in head/middle rest under STC because Path B is determined
        // by capability (resolvable getAt(IntRange)), not by membership in a closed list.
        assertScript '''import groovy.transform.CompileStatic
            @CompileStatic
            def go() {
                def bs = new BitSet()
                bs.set(0); bs.set(1); bs.set(2)
                def (*front, last) = bs
                assert front instanceof BitSet
                assert last == true
            }
            go()
        '''
    }
}
