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

/**
 * Unit tests for static type checking : ternary operator.
 */
class TernaryOperatorSTCTest extends StaticTypeCheckingTestCase {

    void testByteByte() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == byte_TYPE
            })
            def n = true?(byte)1:(byte)0
        '''
    }

    void testShortShort() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == short_TYPE
            })
            def n = true?(short)1:(short)0
        '''
    }

    void testIntInt() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def n = true?1:0
        '''
    }

    void testLongLong() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == long_TYPE
            })
            def n = true?1L:0L
        '''
    }

    void testFloatFloat() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == float_TYPE
            })
            def n = true?1f:0f
        '''
    }

    void testDoubleDouble() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def n = true?1d:0d
        '''
    }

    void testBoolBool() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == boolean_TYPE
            })
            def n = true?true:false
        '''
    }

    void testDoubleFloat() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def n = true?1d:1f
        '''
    }

    // GROOVY-11014
    void testBoxedDoubleInt() {
        assertScript '''
            void test(Double d) {
                double n = d?.doubleValue() ?: 0
            }
            test(null)
        '''
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def n = new Double(0) ?: 0
        '''
    }

    void testDoubleFloatOneIsBoxed() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def n = true?1d:Float.valueOf(1f)
        '''
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def n = true?Double.valueOf(1d):1f
        '''
    }

    // GROOVY-8965
    void testDoubleFloatBothAreBoxed() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE).name == 'java.lang.Number'
            })
            def n = true?Double.valueOf(1d):Float.valueOf(1f)
        '''
    }

    void testDoubleDoubleBothAreBoxed() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def n = true?Double.valueOf(1d):Double.valueOf(1f)
        '''
    }

    // GROOVY-8029
    void testListLiteralAndListCoerce() {
        assertScript '''
            List<String> getStrings(List<Object> list) {
                list.collectMany{ [it.toString()] } ?: ([] as List<String>)
            }
            assert getStrings([]).isEmpty()
            assert getStrings([new Object(), new String()]).size() == 2
        '''
    }

    // GROOVY-10330
    void testTypeParameterTypeParameter1() {
        assertScript '''
            class C<T> {
                T y
                void m(T x, java.util.function.Function<T, T> f) {
                    assert f.apply(x) == 'foo'
                }
                void test(T x, java.util.function.Function<T, T> f) {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def type = node.getNodeMetaData(INFERRED_TYPE)
                        assert type.isGenericsPlaceHolder()
                        assert type.unresolvedName == 'T'
                    })
                    def z = true ? x : y
                    m(z, f)
                }
            }
            new C<String>().test('FOO', { it.toLowerCase() })
        '''
    }

    // GROOVY-10363
    void testTypeParameterTypeParameter2() {
        assertScript '''
            def <X extends java.util.function.Supplier<Number>> X m(X x, X y) {
                X z = true ? x : y // z infers as Supplier<Object>
                return z
            }
            assert m(null,null) == null
        '''
    }

    // GROOVY-10688
    void testTypeParameterTypeParameter3() {
        assertScript '''
            class A<T,U> {
            }
            <T> void test(
                A<Double, ? extends T> x) {
                A<Double, ? extends T> y = x
                A<Double, ? extends T> z = true ? y : x
            }
            test(null)
        '''
    }

    // GROOVY-10271
    void testFunctionalInterfaceTarget1() {
        ['true', 'false'].each { flag ->
            assertScript """import java.util.function.Supplier

                Supplier<Integer> x = { -> 1 }
                Supplier<Integer> y = $flag ? x : { -> 2 }

                assert y.get() == ($flag ? 1 : 2)
            """
        }
    }

    // GROOVY-10272
    void testFunctionalInterfaceTarget2() {
        assertScript '''
            import java.util.function.Function

            Function<Integer, Long> x
            if (true) {
                x = { a -> a.longValue() }
            } else {
                x = { Integer b -> (Long)b }
            }
            assert x.apply(42) == 42L

            Function<Integer, Long> y = (true ? { a -> a.longValue() } : { Integer b -> (Long)b })
            assert y.apply(42) == 42L
        '''
    }

    // GROOVY-10701
    void testFunctionalInterfaceTarget3() {
        for (type in ['Function<T,T>', 'UnaryOperator<T>']) {
            assertScript """import java.util.function.*

                def <T> T m1($type x) {
                    x.apply(null)
                }
                double m2(double d) {
                    Math.PI
                }

                def result = m1(true ? (Double d) -> 42.0d : this::m2)
                assert result == 42.0d
            """
        }
    }

    // GROOVY-10357
    void testAbstractMethodDefault() {
        assertScript '''
            import java.util.function.Function

            abstract class A {
                abstract long m(Function<Boolean,Integer> f = { Boolean b -> b ? +1 : -1 })
            }

            def a = new A() {
                @Override
                long m(Function<Boolean,Integer> f) {
                    f(true).longValue()
                }
            }
            assert a.m() == 1L
        '''
    }

    // GROOVY-10358
    void testCommonInterface1() {
        assertScript '''
            interface I {
                int m(int i)
            }
            abstract class A implements I {
            }
            class B<T> extends A {
                int m(int i) {
                    i + 1
                }
            }
            class C<T> extends A {
                int m(int i) {
                    i - 1
                }
            }

            C<String> c = null; int i = 1
            int x = (false ? c : new B<String>()).m(i) // Cannot find matching method A#m(int)
            assert x == 2
        '''
    }

    // GROOVY-10603
    void testCommonInterface2() {
        assertScript '''
            interface I {}
            interface J extends I {}
            class Foo implements I {}
            class Bar implements J {}

            I test(Foo x, Bar y) {
                true ? x : y // Cannot return value of type GroovyObject for method returning I
            }
            test(null, null)
        '''
    }

    void testCommonInterface3() {
        assertScript '''import static java.util.concurrent.ConcurrentHashMap.*
            Set<Integer> integers = false ? new HashSet<>() : newKeySet()
        '''
    }

    // GROOVY-10130
    void testInstanceofGuard() {
        assertScript '''
            class A {
            }
            class B extends A {
            }
            def test(A x) {
                (true && x instanceof B) ? new B[]{x} : null // Cannot convert A to B
            }
            assert test(null) == null
        '''
    }

    // GROOVY-5523
    void testNull1() {
        assertScript '''
            def findFile() {
                String str = ""
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
                })
                File f = str ? new File(str) : null
            }
        '''
        assertScript '''
            def findFile() {
                String str = ""
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
                })
                File f = str ? null : new File(str)
            }
        '''
        assertScript '''
            def findFile() {
                String str = ""
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
                })
                File f = str ? null : null
            }
        '''
    }

    void testNull2() {
        assertScript '''
            def test(String str) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
                })
                String s = str ?: null
            }

            assert test('x') == 'x'
            assert test('') == null
        '''
    }

    // GROOVY-5734
    void testNull3() {
        assertScript '''
            Integer test() { false ? null : 42 }

            assert test() == 42
        '''
    }

    @NotYetImplemented // GROOVY-10095
    void testNull4() {
        assertScript '''
            float x = false ? 1.0 : null
        '''
    }

    // GROOVY-10226
    void testNull5() {
        assertScript '''
            class A<T> {
            }
            def <T extends A<String>> T test() {
                final T x = null
                true ? (T) null : x
            }
            assert test() == null
        '''
    }

    // GROOVY-10158
    void testNull6() {
        assertScript '''
            class A<T> {
            }
            class B<T extends A<String>> {
                T m() {
                    final T x = null
                    final T y = null
                    ( true ? x : y )
                }
            }
            assert new B<A<String>>().m() == null
        '''
    }
}
