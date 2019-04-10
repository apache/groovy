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

class MethodReferenceTest extends GroovyTestCase {
    // class::instanceMethod
    void testFunctionCI() {
        // TODO can this be removed on JDK12
        if (true) return

        assertScript '''
            import java.util.stream.Collectors
            
            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(Object::toString).collect(Collectors.toList())
                assert 3 == result.size()
                assert ['1', '2', '3'] == result
            }
            
            p()
        '''
    }

    // class::instanceMethod
    void testFunctionCI_MULTI_MATCHED_METHODS() {
        assertScript '''
            import java.util.stream.Collectors
            
            @groovy.transform.CompileStatic
            void p() {
                def result = [1, 2, 3].stream().map(Integer::toString).collect(Collectors.toList())
                assert 3 == result.size()
                assert ['1', '2', '3'] == result
            }
            
            p()
        '''
    }

    // class::instanceMethod
    void testBinaryOperatorCI() {
        // TODO can this be removed on JDK12
        if (true) return

        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                def result = [1.0G, 2.0G, 3.0G].stream().reduce(0.0G, BigDecimal::add)

                assert 6.0G == result
            }
            
            p()
        '''
    }

    // class::staticMethod
    void testFunctionCS() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [1, -2, 3].stream().map(Math::abs).collect(Collectors.toList())

                assert [1, 2, 3] == result
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
                def result = [2.0G, 2.0G, 3.0G].stream().reduce(0.0G, new Adder()::add)

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
    void testIntFunctionCN() {
        assertScript '''
            @groovy.transform.CompileStatic
            void p() {
                assert new Integer[] { 1, 2, 3 } == [1, 2, 3].stream().toArray(Integer[]::new)
            }
            
            p()

        '''
    }

    // class::new
    void testFunctionCN() {
        assertScript '''
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                assert [1, 2, 3] == ["1", "2", "3"].stream().map(Integer::new).collect(Collectors.toList())
            }
            
            p()

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
}
