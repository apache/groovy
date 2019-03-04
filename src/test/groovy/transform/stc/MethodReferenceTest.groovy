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
}
