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
package org.codehaus.groovy.classgen.asm

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

class MethodPatternsTest extends AbstractBytecodeTestCase {

    void testUnoptimizedIfWithNestedOptimizedLoop(){
        if (config.indyEnabled) return;
        // in this example the if block contains statements that will not be optimized
        // but we still want to optimize the for loops, which can.
        // The test will check there is a optimized bytecode sequence for the loops.
        assert compile('''

            long sum = 0;
            double m = 1;

            if( true ) {

                System.err.println( "START");
                long t0 = System.currentTimeMillis();

                for( int j=0; j<1000; j++ ) {
                    for( int i=0; i<100_000; i++ ) {
                       sum = sum + i;
                       m = m*i;
                    }
                }

                long t1 = System.currentTimeMillis();
                System.err.println( "END - " + (t1-t0)+"ms");
            }

            System.err.println( "Done: "+sum+" "+m );
        ''').hasSequence([
                // for (int j=0; j<1000; j++) start and condition
                'ICONST_0',
                'SIPUSH 1000',
                'IF_ICMPGE',
                'ICONST_1',
                // for (int i=0; i<100_000; i++) start and condition
                'ICONST_0',
                'LDC 100000',
                'IF_ICMPGE',
                'ICONST_1',
                'GOTO',
                'ICONST_0',
                'IFEQ',
                // sum = sum + i
                'LLOAD',
                'ILOAD',
                'I2L',
                'LADD',
                // m = m * i
                'DLOAD',
                'ILOAD',
                'I2D',
                'DMUL',
                // for (int i=0; i<100_000; i++) increment
                'ILOAD',
                'ICONST_1',
                'IADD',
                'ISTORE',
                // for (int j=0; j<1000; j++) increment
                'ILOAD',
                'ICONST_1',
                'IADD',
                'ISTORE'
        ])
    }

    // make a test for native compilation of the ackerman function
    // and ensure the nested call is optimized
    void testAckerman() {
        if (config.indyEnabled) return;
        assert compile(method: 'A', '''
            int A(int x, int y) {
                if (x == 0) return y+1
                if (y == 0) return A(x-1, 1)
                return A(x-1, A(x, y-1))
            }
        ''').hasSequence([
            // if (x==0) return y+1
            'ILOAD 1',
            'ICONST_0',
            'IF_ICMPNE',
            'ICONST_1',
            'ICONST_0',
            'IFEQ',     // x==0 and branching till here
            'ILOAD 2',
            'ICONST_1',
            'IADD',     // y+1
            'IRETURN',  // return
            // if (y==0) return A(x-1,1)
            'ILOAD 2',
            'ICONST_0',
            'IF_ICMPNE',
            'ICONST_1',
            'ICONST_0',
            'IFEQ',     // y==0 and branching till here
            'ALOAD 0',
            'ILOAD 1',
            'ICONST_1',
            'ISUB',     // x-1 argument
            'ICONST_1',
            'INVOKEVIRTUAL script.A (II)I', // A(x-1,1)
            'IRETURN',  //return
            // return A(x-1,A(x,y-1))
            'ALOAD 0',
            'ILOAD 1',
            'ICONST_1',
            'ISUB',     // outer A x-1 argument
            'ALOAD 0',
            'ILOAD 1',  // inner A x argument
            'ILOAD 2',
            'ICONST_1',
            'ISUB',     //inner A y-1 argument
            'INVOKEVIRTUAL script.A (II)I', // inner A
            'INVOKEVIRTUAL script.A (II)I', // outer A
            'IRETURN' //return
        ])
    }

    void testForLoopSettingArray() {
        if (config.indyEnabled) return;
        assert compile('''
            int n = 10
            int[] x = new int[n]
            for (int i = 0; i < n; i++) x[i] = i + 1
        ''').hasSequence([
            'ILOAD',
            'ILOAD',
            'IF_ICMPGE',
            'ICONST_1',
            'GOTO',
            'ICONST_0',
            'IFEQ',
            'ILOAD',
            'ICONST_1',
            'IADD',
            'ISTORE',
            'ALOAD',
            'ILOAD',
            'ILOAD',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet ([III)V',
            'ILOAD',
            'POP',
            'ILOAD',
            'DUP',
            'ISTORE',
            'ICONST_1',
            'IADD',
            'DUP',
            'ISTORE',
            'POP',
            'ILOAD',
            'POP',
            'GOTO',
        ])
    }

    void testArrayIncrement() {
        if (config.indyEnabled) return;
        assert compile('''
            int n = 10
            int[] x = new int[n]
            for (int i = 0; i < n; i++) x[i]++
        ''').hasSequence([
            'ICONST_0',
            'ISTORE',
            'ILOAD',
            'POP',
            'ILOAD',
            'ILOAD',
            'IF_ICMPGE',
            'ICONST_1',
            'GOTO',
            'ICONST_0',
            'IFEQ',
            'ILOAD',
            'ISTORE',
            'ALOAD',
            'ILOAD',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArrayGet ([II)I',
            'DUP',
            'ISTORE',
            'ICONST_1',
            'IADD',
            'ISTORE',
            'ALOAD',
            'ILOAD',
            'ILOAD',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet ([III)V',
            'ILOAD',
            'POP',
            'ILOAD',
            'POP',
            'ILOAD',
            'DUP',
            'ISTORE',
            'ICONST_1',
            'IADD',
            'DUP',
            'ISTORE',
            'POP',
            'ILOAD',
            'POP',
            'GOTO',
        ])
    }

    void testForLoopSettingArrayWithOperatorUsedInAssignmentAndArrayRHS() {
        if (config.indyEnabled) return;
        assert compile('''
            int n = 10
            int[] x = new int[n]
            int[] y = new int[n]
            for (int i = 0; i < n; i++) x[i] += y[i]
        ''').hasSequence ([
            'ILOAD',
            'ILOAD',
            'IF_ICMPGE',
            'ICONST_1',
            'GOTO',
            'ICONST_0',
            'IFEQ',
            'ILOAD',
            'ISTORE',
            'ALOAD',
            'DUP',
            'ILOAD',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArrayGet ([II)I',
            'ALOAD',
            'ILOAD',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArrayGet ([II)I',
            'IADD',
            'DUP',
            'ISTORE',
            'ILOAD',
            'SWAP',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet ([III)V',
            'ILOAD',
            'DUP',
            'ISTORE',
            'ICONST_1',
            'IADD',
            'ISTORE',
            'GOTO'
        ])
    }

    void testRightShiftUnsignedWithLongArgument() {
        if (config.indyEnabled) return;
        assert compile(method: "hashCode", '''
            class X{
                long _tagReservationDate
                String userId, partnerItemId, trackingTag
                public int hashCode() {
                    final int prime = 31;
                    int result = 1;
                    result = prime * result + (partnerItemId?.hashCode() ?: 0)
                    result = prime * result + (int) (_tagReservationDate ^ (_tagReservationDate >>> 32))
                    result = prime * result + (trackingTag?.hashCode() ?: 0)
                    result = prime * result + (userId?.hashCode() ?: 0)
                    return result;
                }
            }
        ''').hasSequence ([
            'IMUL',
            'ALOAD 0',
            'GETFIELD X._tagReservationDate : J',
            'ALOAD 0',
            'GETFIELD X._tagReservationDate : J',
            'BIPUSH 32',
            'LUSHR',
            'LXOR',
            'L2I',
            'IADD',
        ])
    }

    void testObjectArraySet() {
        if (config.indyEnabled) return;
        assert compile(method: "foo", '''
            class X {
                void foo() {
                    X[] xa = new X[1]
                    xa[0] = new X()
                }
            }
        ''').hasSequence ([
            'ICONST_0',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.objectArraySet ([Ljava/lang/Object;ILjava/lang/Object;)V',
        ])
    }

    void testBooleanArraySet() {
        if (config.indyEnabled) return;
        assert compile(method: "foo", '''
            class X{
                void foo() {
                    boolean[] xa = new boolean[1]
                    xa[0] = false
                }
            }
        ''').hasSequence ([
            'ICONST_0',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.zArraySet ([ZIZ)V',
        ])
    }

    void testArray() {
        if (config.indyEnabled) return;
        def methods = [
            "short"     :   [1, "sArraySet ([SIS)V", "sArrayGet ([SI)S"],
            "int"       :   [1, "intArraySet ([III)V", "intArrayGet ([II)I"],
            "boolean"   :   [false, "zArraySet ([ZIZ)V", "zArrayGet ([ZI)Z"],
            "long"      :   [1l, "lArraySet ([JIJ)V","lArrayGet ([JI)J"],
            "float"     :   [1f, "fArraySet ([FIF)V", "fArrayGet ([FI)F"],
            "byte"      :   [1, "bArraySet ([BIB)V", "bArrayGet ([BI)B"],
            "char"      :   [1, "cArraySet ([CIC)V", "cArrayGet ([CI)C"],
            "double"    :   [1d, "dArraySet ([DID)V", "dArrayGet ([DI)D"]
        ]
        methods.each {
            assert compile(method: "foo", """
                class X{
                  void foo() {
                    ${it.key}[] xa = new ${it.key}[1]
                    xa[0] = ${it.value[0]}
                  }
                }
            """).hasSequence ([
                'ICONST_0',
                "INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8."+it.value[1],
            ])

            assert compile(method: "foo", """
                class X{
                  ${it.key} foo() {
                    ${it.key}[] xa = new ${it.key}[1]
                    xa[0]
                  }
                }
            """).hasSequence ([
                'ICONST_0',
                "INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.${it.value[2]}",
            ])
        }

        assertScript """
            a=[3:5]
            class B {
                int v;
            }
            B b = new B();
            b.v = 3
            
            clos = {
                if (it!=null) {
                    a[it.v] += 3
                }
            }
            clos.call(b)
            assert b.v == 3
            assert a[3] == 8        
        """
    }

    void testFib() {
        if (config.indyEnabled) return;
        assert compile(method: "fib", """
            int fib(int i) {
                i < 2 ? 1 : fib(i - 2) + fib(i - 1)
            }
        """).hasSequence ([
            'ILOAD 1',
            'ICONST_2',
            'IF_ICMPGE',
            'ICONST_1',
            'GOTO',
            'ICONST_0',
            'IFEQ',
            'ICONST_1',
            'GOTO',
            'ALOAD 0',
            'ILOAD 1',
            'ICONST_2',
            'ISUB',
            'INVOKEVIRTUAL script.fib (I)I',
            'ALOAD 0',
            'ILOAD 1',
            'ICONST_1',
            'ISUB',
            'INVOKEVIRTUAL script.fib (I)I',
            'IADD',
            'IRETURN'
        ])

        // check that there is no fastpath for this method, since n is Object
        def seq = compile(method: "fib", """
            def fib(n) {
                n<=2L?n:fib(n-1L)+fib(n-2L)
            }
        """).toSequence()
        // isOrigXY is used for the fastpath guards
        assert !seq.contains("isOrig")
    }

    void testNoBoxUnbox() {
        if (config.indyEnabled) return;
        assert compile(method: "someCode", """
            public boolean someCall() {
                return true;
            }
            
            public boolean someCode() {
                boolean val = someCall()
            }        
        """).hasSequence([
            'ALOAD',
            'INVOKEVIRTUAL script.someCall ()Z',
            'ISTORE',
            'ILOAD',
            'IRETURN',
        ])
    }

    void testDiv() {
        if (config.indyEnabled) return;
        def types = [
            "byte", "short", "int", "long", "double", "float"]
        types.each {type ->
            assert compile(method: "someCode","""
                def someCode() {
                    $type v = 5/4
                }
            """).hasSequence(["IDIV"])
        }
        types.each {type ->
            assert compile(method: "someCode","""
                def someCode() {
                    $type v = 0
                    v = 5/4
                }
            """).hasSequence(["IDIV"])
        }

        assert compile(method: "someCode", """
            def someCode() {
                long l = 5l/4l
            }
        """).hasSequence(["LDIV"])
        assert compile(method: "someCode", """
            def someCode() {
                long l
                l = 5l/4l
            }
        """).hasSequence(["LDIV"])
    }
}
