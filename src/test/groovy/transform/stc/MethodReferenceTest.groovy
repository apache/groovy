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

import groovy.test.GroovyTestCase

class MethodReferenceTest extends GroovyTestCase {
    // class::instanceMethod
    void testFunctionCI() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
                assert result == ['1', '2', '3']
            }

            p()
        '''
    }

    // class::instanceMethod
    void testFunctionCI2() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(Integer::toString).collect(Collectors.toList())
                assert result == ['1', '2', '3']
            }

            p()
        '''
    }

    // class::instanceMethod -- GROOVY-10047
    void testFunctionCI3() {
        assertScript '''
            import java.util.function.Function
            import static java.util.stream.Collectors.toMap

            @groovy.transform.CompileStatic
            void p() {
                List<String> list = ['a','bc','def']
                Function<String,String> self = str -> str // help for toMap
                def map = list.stream().collect(toMap(self, String::length))
                assert map == [a: 1, bc: 2, 'def': 3]
            }

            p()
        '''

        assertScript '''
            import java.util.function.Function
            import static java.util.stream.Collectors.toMap

            @groovy.transform.CompileStatic
            void p() {
                List<String> list = ['a','bc','def']
                // TODO: inference for T in toMap(Function<? super T,...>, Function<? super T,...>)
                def map = list.stream().collect(toMap(Function.<String>identity(), String::length))
                assert map == [a: 1, bc: 2, 'def': 3]
            }

            p()
        '''
    }

    // class::instanceMethod
    void testFunctionCI4() {
        def err = shouldFail '''
            import static java.util.stream.Collectors.toList

            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(String::toString).collect(toList())
                assert result == ["1", "2", "3"]
            }

            p()
        '''

        assert err =~ /Invalid receiver type: java.lang.Integer is not compatible with java.lang.String/
    }

    // class::instanceMethod -- GROOVY-9814
    void testFunctionCI5() {
        assertScript '''
            import java.util.function.*
            import groovy.transform.*

            @CompileStatic
            class One { String id }

            @CompileStatic
            class Two extends One { }

            @CompileStatic @Immutable(knownImmutableClasses=[Function])
            class FunctionHolder<T> {
                Function<T, ?> extractor

                def apply(T t) {
                    extractor.apply(t)
                }
            }

            def fh = new FunctionHolder(One::getId)
            assert fh.apply(new One(id:'abc')) == 'abc'

            fh = new FunctionHolder(One::getId)
            assert fh.apply(new Two(id:'xyz')) == 'xyz' // sub-type argument
        '''
    }

    // class::instanceMethod -- GROOVY-9974
    void testPredicateCI() {
        assertScript '''
            @groovy.transform.CompileStatic
            void test(List<String> strings = ['']) {
                strings.removeIf(String::isEmpty)
                assert strings.isEmpty()
            }
            test()
        '''
    }

    // class::instanceMethod
    void testBinaryOperatorCI() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, BigDecimal::add)

                assert 6.0G == result
            }

            p()
        '''
    }

    // instance::instanceMethod
    void testBinaryOperatorII() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    // instance::instanceMethod
    void testBinaryOperatorII_COMPATIBLE() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                BigDecimal add(Number a, Number b) {
                    ((BigDecimal) a).add((BigDecimal) b)
                }
            }
        '''
    }

    // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder()::add)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                public BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION2() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder().getThis()::add)

                assert new BigDecimal(6) == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }

                Adder getThis() {
                    return this
                }
            }
        '''
    }

    // instance::staticMethod
    void testBinaryOperatorIS() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, adder::add)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder()::add)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION2() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, Adder.newInstance()::add)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                static BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }

                static Adder newInstance() {
                    new Adder()
                }
            }
        '''
    }

    // arrayClass::new
    void testFunctionCN() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().toArray(Integer[]::new)
                assert result == new Integer[] { 1, 2, 3 }
            }

            p()
        '''
    }

    // class::new
    void testFunctionCN2() {
        assertScript '''
            import static java.util.stream.Collectors.toList

            @groovy.transform.CompileStatic
            void p() {
                def result = ["1", "2", "3"].stream().map(Integer::new).collect(toList())
                assert result == [1, 2, 3]
            }

            p()
        '''
    }

    // class::new -- GROOVY-10033
    void testFunctionCN3() {
        assertScript '''
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class C {
                C(Function<String,Integer> f) {
                    def i = f.apply('42')
                    assert i == 42
                }
                static test() {
                    new C(Integer::new)
                }
            }
            C.test()
        '''
    }

    // class::new -- GROOVY-10033
    void testFunctionCN4() {
        assertScript '''
            import java.util.function.Function

            class A {
                A(Function<A,B> f) {
                    B b = f.apply(this)
                    assert b instanceof X.Y
                }
            }
            class B {
                B(A a) {
                    assert a != null
                }
            }
            @groovy.transform.CompileStatic
            class X extends A {
              public X() {
                super(Y::new)
              }
              private static class Y extends B {
                Y(A a) {
                  super(a)
                }
              }
            }

            new X()
        '''
    }

    // class::staticMethod
    void testFunctionCS() {
        assertScript '''
            import static java.util.stream.Collectors.toList

            @groovy.transform.CompileStatic
            void p() {
                def result = [1, -2, 3].stream().map(Math::abs).collect(toList())
                assert [1, 2, 3] == result
            }

            p()
        '''
    }

    // class::staticMethod
    void testFunctionCS2() {
        assertScript '''
            import java.util.function.Function
            import static java.util.stream.Collectors.toMap

            @groovy.transform.CompileStatic
            void p() {
                List<String> list = ['x','y','z']
                def map = list.stream().collect(toMap(Function.identity(), Collections::singletonList))
                assert map == [x: ['x'], y: ['y'], z: ['z']]
            }

            p()
        '''
    }

    // class::staticMethod -- GROOVY-9799
    void testFunctionCS3() {
        assertScript '''
            class C {
                String x
            }

            class D {
                String x
                static D from(C c) {
                    new D(x: c.x)
                }
            }

            @groovy.transform.CompileStatic
            def test(C c) {
                Optional.of(c).map(D::from).get()
            }

            def d = test(new C(x: 'x'))
            assert d.x == 'x'
        '''
    }

    // class::staticMethod
    void testFunctionCS_RHS() {
        assertScript '''
            import java.util.function.Function
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                Function<Integer, Integer> f = Math::abs
                def result = [1, -2, 3].stream().map(f).collect(Collectors.toList())

                assert [1, 2, 3] == result
            }

            p()
        '''
    }

    // class::staticMethod
    void testFunctionCS_RHS_NOTYPE() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def f = Math::abs // No explicit type defined, so it is actually a method closure. We can make it smarter in a later version.
                def result = [1, -2, 3].stream().map(f).collect(Collectors.toList())

                assert [1, 2, 3] == result
            }

            p()
        '''
    }

    // instance::instanceMethod
    void testBinaryOperatorII_RHS() {
        assertScript '''
            import java.util.function.BinaryOperator

            @groovy.transform.CompileStatic
            void p() {
                Adder adder = new Adder()
                BinaryOperator<BigDecimal> b = adder::add
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, b)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    // expression::instanceMethod
    void testBinaryOperatorII_RHS2() {
        assertScript '''
            import java.util.function.BinaryOperator

            @groovy.transform.CompileStatic
            void p() {
                BinaryOperator<BigDecimal> b = new Adder()::add
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, b)

                assert 6.0G == result
            }

            p()

            @groovy.transform.CompileStatic
            class Adder {
                BigDecimal add(BigDecimal a, BigDecimal b) {
                    a.add(b)
                }
            }
        '''
    }

    // class::new
    void testFunctionCN_RHS() {
        assertScript '''
            import java.util.function.Function
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                Function<String, Integer> f = Integer::new
                assert [1, 2, 3] == ["1", "2", "3"].stream().map(f).collect(Collectors.toList())
            }

            p()
        '''
    }

    // arrayClass::new
    void testIntFunctionCN_RHS() {
        assertScript '''
            import java.util.function.IntFunction
            import java.util.stream.Stream

            @groovy.transform.CompileStatic
            void p() {
                IntFunction<Integer[]> f = Integer[]::new
                assert new Integer[] { 1, 2, 3 } == [1, 2, 3].stream().toArray(f)
            }

            p()
        '''
    }

    void testMethodNotFound() {
        def errMsg = shouldFail '''
            @groovy.transform.CompileStatic
            void p() {
                [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, BigDecimal::addx)
            }

            p()
        '''

        assert errMsg.contains('Failed to find the expected method[addx(java.math.BigDecimal,java.math.BigDecimal)] in the type[java.math.BigDecimal]')
    }

    // class::instanceMethod
    void testFunctionCI_WRONGTYPE() {
        def errMsg = shouldFail '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(String::toString).collect(Collectors.toList())
                assert 3 == result.size()
                assert ['1', '2', '3'] == result
            }

            p()
        '''

        assert errMsg.contains('Invalid receiver type: java.lang.Integer is not compatible with java.lang.String')
    }

    // class::instanceMethod, actually class::staticMethod
    void testFunctionCI_DGM() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = ['a', 'ab', 'abc'].stream().map(String::size).collect(Collectors.toList())
                assert [1, 2, 3] == result
            }

            p()
        '''
    }

    // class::staticMethod
    void testFunctionCS_DGSM() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every(e -> e instanceof Thread)
            }

            p()
        '''
    }

    // class::instanceMethod
    void testFunctionCI_SHADOW_DGM() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [[a:1], [b:2], [c:3]].stream().map(Object::toString).collect(Collectors.toList())
                assert 3 == result.size()
                assert ['[a:1]', '[b:2]', '[c:3]'] == result
            }

            p()
        '''
    }

    // class::staticMethod
    void testFunctionCS_MULTI_DGSM() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every(e -> e instanceof Thread)

                result = [{}, {}, {}].stream().map(Thread::startDaemon).collect(Collectors.toList())
                assert result.every(e -> e instanceof Thread)
            }

            p()
        '''
    }
}
