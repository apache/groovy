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
        if (true) return

        assertScript '''
            import java.util.stream.Stream

            @groovy.transform.CompileStatic
            void p() {
                def result = [new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)].stream().reduce(new BigDecimal(0), BigDecimal::add)

                assert new BigDecimal(6) == result
            }
            
            p()
        '''
    }

    // class::staticMethod
    void testFunctionCS() {
        assertScript '''
            import java.util.stream.Stream
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
            import java.util.stream.Stream
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)].stream().reduce(new BigDecimal(0), adder::add)

                assert new BigDecimal(6) == result
            }
            
            p()
            
            @groovy.transform.CompileStatic
            class Adder {
                public BigDecimal add(BigDecimal a, BigDecimal b) {
                    return a.add(b)
                }
            }
        '''
    }

    // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION() {
        assertScript '''
            import java.util.stream.Stream
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)].stream().reduce(new BigDecimal(0), new Adder()::add)

                assert new BigDecimal(6) == result
            }
            
            p()
            
            @groovy.transform.CompileStatic
            class Adder {
                public BigDecimal add(BigDecimal a, BigDecimal b) {
                    return a.add(b)
                }
            }
        '''
    }

    // expression::instanceMethod
    void testBinaryOperatorII_EXPRESSION2() {
        assertScript '''
            import java.util.stream.Stream
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)].stream().reduce(new BigDecimal(0), new Adder().getThis()::add)

                assert new BigDecimal(6) == result
            }
            
            p()
            
            @groovy.transform.CompileStatic
            class Adder {
                public BigDecimal add(BigDecimal a, BigDecimal b) {
                    return a.add(b)
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
            import java.util.stream.Stream
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                Adder adder = new Adder()
                def result = [new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)].stream().reduce(new BigDecimal(0), adder::add)

                assert new BigDecimal(6) == result
            }
            
            p()
            
            @groovy.transform.CompileStatic
            class Adder {
                public static BigDecimal add(BigDecimal a, BigDecimal b) {
                    return a.add(b)
                }
            }
        '''
    }

    // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION() {
        assertScript '''
            import java.util.stream.Stream
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)].stream().reduce(new BigDecimal(0), new Adder()::add)

                assert new BigDecimal(6) == result
            }
            
            p()
            
            @groovy.transform.CompileStatic
            class Adder {
                public static BigDecimal add(BigDecimal a, BigDecimal b) {
                    return a.add(b)
                }
            }
        '''
    }

    // expression::staticMethod
    void testBinaryOperatorIS_EXPRESSION2() {
        assertScript '''
            import java.util.stream.Stream
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                def result = [new BigDecimal(1), new BigDecimal(2), new BigDecimal(3)].stream().reduce(new BigDecimal(0), Adder.newInstance()::add)

                assert new BigDecimal(6) == result
            }
            
            p()
            
            @groovy.transform.CompileStatic
            class Adder {
                public static BigDecimal add(BigDecimal a, BigDecimal b) {
                    return a.add(b)
                }
                
                static Adder newInstance() {
                    return new Adder()
                }
            }
        '''
    }

    // arrayClass::new
    void testIntFunctionCN() {
        assertScript '''
            import java.util.stream.Stream

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
            import java.util.stream.Stream
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
            import java.util.stream.Stream
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

    // class::new
    void testFunctionCN_RHS() {
        assertScript '''
            import java.util.function.Function
            import java.util.stream.Stream
            import java.util.stream.Collectors

            @groovy.transform.CompileStatic
            void p() {
                Function<String, Integer> f = Integer::new
                assert [1, 2, 3] == ["1", "2", "3"].stream().map(f).collect(Collectors.toList())
            }
            
            p()

        '''
    }
}
