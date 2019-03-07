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
package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

/**
 * Unit tests for static compilation: basic math operations.
 */
class StaticCompileMathTest extends AbstractBytecodeTestCase {
    void testIntSum() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m() {
                int a = 10
                int b = 20
                int c = a+b
            }
            assert m()==30
        '''
    }

    void testLongSum() {
        assertScript '''
            @groovy.transform.CompileStatic
            long m() {
                long a = 10
                long b = 20
                long c = a+b
            }
            assert m()==30
        '''
    }

    void testShortSum() {
        assertScript '''
            @groovy.transform.CompileStatic
            int m() {
                short a = 10
                short b = 20
                int c = a+b
            }
            assert m()==30
        '''
    }

    void testFloatSum() {
        assertScript '''
            @groovy.transform.CompileStatic
            float m() {
                float a = 10f
                float b = 20f
                float c = a+b
            }
            assert m()==30f
        '''
    }

    void testDoubleSum() {
        assertScript '''
            @groovy.transform.CompileStatic
            double m() {
                double a = 10d
                double b = 20d
                double c = a+b
            }
            assert m()==30d
        '''
    }

    void testDoublePlusInt() {
        assertScript '''
            @groovy.transform.CompileStatic
            double m() {
                double a = 10d
                int b = 1
                double c = a+b
            }
            assert m()==11d
        '''
    }
    
    void testIntMinusDouble() {
        assertScript '''
            @groovy.transform.CompileStatic
            double m() {
                int x = 10
                double y = 0.5
                x-y
            }
            assert m()==9.5d
        '''
    }

    void testIntMinusBigDec() {
        extractionOptions = [method: 'm', print:true]
        assertScript '''
            @groovy.transform.CompileStatic
            double m() {
                return 1i - 1.0 // 1.0 is BigDecimal
            }
            assert m()==0
        '''
    }

    void testIntPlusBigDec() {
        extractionOptions = [method: 'm', print:true]
        assertScript '''
            @groovy.transform.CompileStatic
            double m() {
                return 1i + 1.0 // 1.0 is BigDecimal
            }
            assert m()==2
        '''
    }

    void testIntMultiplyBigDec() {
        extractionOptions = [method: 'm', print:true]
        assertScript '''
            @groovy.transform.CompileStatic
            double m() {
                return 1i * 1.0 // 1.0 is BigDecimal
            }
            assert m()==1
        '''
    }

    void testIntDivBigDec() {
        extractionOptions = [method: 'm', print:true]
        assertScript '''
            @groovy.transform.CompileStatic
            double m() {
                return 1i / 1.0 // 1.0 is BigDecimal
            }
            assert m()==1
        '''
    }

    void testStaticCompilePiComputationWithPrimitives() {
//        extractionOptions = [method: 'doIt', print:true]
        assertScript '''@groovy.transform.CompileStatic
            def doIt() {
                final int n = 10000000i // 10 times fewer due to speed issues.
                final double delta = 1.0d / n
                final startTime = System.nanoTime()
                double sum = 0.0d
                for (int i = 0; i < n; i++) { sum += 1.0d / (1.0d + ((i - 0.5d) * delta) ** 2i) }
                final double pi = 4.0d * sum * delta
                final elapseTime = (System.nanoTime() - startTime) / 1e9
                println("==== Groovy Sequential Primitives pi = " + pi)
                println("==== Groovy Sequential Primitives iteration count = " + n)
                println("==== Groovy Sequential Primitives elapse = " + elapseTime)
            }

            doIt()
        '''
    }

    void testStaticCompilePiComputationWithPrimitivesAndRangeLoop() {
//        extractionOptions = [method: 'doIt', print:true]
        assertScript '''@groovy.transform.CompileStatic
            def doIt() {
                final int n = 10000000i // 10 times fewer due to speed issues.
                final double delta = 1.0d / n
                final startTime = System.nanoTime()
                double sum = 0.0d
                for (int i in 1..n) { sum += 1.0d / (1.0d + ((i - 0.5d) * delta) ** 2i) }
                final double pi = 4.0d * sum * delta
                final elapseTime = (System.nanoTime() - startTime) / 1e9
                println("==== Groovy Sequential Primitives pi = " + pi)
                println("==== Groovy Sequential Primitives iteration count = " + n)
                println("==== Groovy Sequential Primitives elapse = " + elapseTime)
            }

            doIt()
        '''
    }

    void testStaticCompileLeftShiftEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            int foo() {
                int i = 1
                i <<= 2
                i
            }
            assert foo()==4
        '''
    }

    void testStaticCompileRightShiftEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            int foo() {
                int i = 4
                i >>= 2
                i
            }
            assert foo()==1
        '''
    }

    void testStaticCompilePlusEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            int foo() {
                int i = 4
                i += 2
                i
            }
            assert foo()==6
        '''
    }

    void testStaticCompileMinusEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            int foo() {
                int i = 4
                i -= 2
                i
            }
            assert foo()==2
        '''
    }

    void testStaticCompileDivideEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            int foo() {
                int i = 4
                i /= 2
                i
            }
            assert foo()==2
        '''
    }

    void testStaticCompileMultiplyEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            int foo() {
                int i = 4
                i *= 2
                i
            }
            assert foo()==8
        '''
    }

    void testStaticCompilePowerEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            def foo() {
                def i = 4
                i **= 2
                i
            }
            assert foo()==16
        '''
    }

    void testStaticCompileModEquals() {
        assertScript '''
            @groovy.transform.CompileStatic
            int foo() {
                int i = 3
                i %= 2
                i
            }
            assert foo()==1
        '''
    }

    void testPrimitiveIntCompareNotEqualShouldUseFastPath() {
        def source = '''
        @groovy.transform.CompileStatic
        boolean cmp(int i, int j) {
            boolean b = i==j
            return i!=j
        }
        assert cmp(1,1) == false
        assert cmp(1,2) == true
        '''
        assertScript(source)
        def bytecode = compile(method:'cmp', source)
        assert bytecode.hasStrictSequence([
                'ILOAD 1',
                'ILOAD 2',
                'IF_ICMPNE'
        ])
    }

    void testPrimitiveLongCompareNotEqualShouldUseFastPath() {
        def source = '''
        @groovy.transform.CompileStatic
        boolean cmp(long i, long j) {
            boolean b = i==j
            return i!=j
        }
        assert cmp(1,1) == false
        assert cmp(1,2) == true
        '''
        assertScript(source)
        def bytecode = compile(method:'cmp', source)
        assert bytecode.hasStrictSequence([
                'LLOAD 1',
                'LLOAD 3',
                'LCMP',
                'IFNE'
        ])
    }
}
