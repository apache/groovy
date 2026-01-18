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
import org.codehaus.groovy.runtime.MethodClosure
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

final class StaticCompilationTest extends AbstractBytecodeTestCase {

    @Test
    void testEmptyMethod() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() { }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 2',
                'RETURN'
            ])
    }

    @Test
    void testPrimitiveReturn1() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            int m() { 1 }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 2 L0',
                'ICONST_1',
                'IRETURN'
            ])
    }

    @Test
    void testPrimitiveReturn2() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            long m() { 1L }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 2',
                'LCONST_1',
                'LRETURN'
            ])
    }

    @Test
    void testPrimitiveReturn3() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            short m() { 1 }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 2',
                'ICONST_1',
                'I2S',
                'IRETURN'
            ])
    }

    @Test
    void testPrimitiveReturn4() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            byte m() { 1 }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 2',
                'ICONST_1',
                'I2B',
                'IRETURN'
            ])
    }

    @Test
    void testParameterReturns() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            int m(int i) { i }
        ''').hasStrictSequence(['ILOAD','IRETURN'])

        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            long m(long l) { l }
        ''').hasStrictSequence(['LLOAD','LRETURN'])

        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            short m(short s) { s }
        ''').hasStrictSequence(['ILOAD','IRETURN'])

        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            float m(float f) { f }
        ''').hasStrictSequence(['FLOAD','FRETURN'])

        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            double m(double d) { d }
        ''').hasStrictSequence(['DLOAD','DRETURN'])

        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            Object m(Object o) { o }
        ''').hasStrictSequence(['ALOAD','ARETURN'])
    }

    // GROOVY-11288
    @Test
    void testEmptyDeclaration0() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                int i
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_0',
                'ISTORE 1',
                'L1',
                'LINENUMBER 4',
                'RETURN'
            ])
    }

    @Test
    void testEmptyDeclaration1() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            int m() {
                int i
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_0',
                'ISTORE 1',
                'L1',
                'ILOAD 1',
                'IRETURN'
            ])
    }

    // GROOVY-11288
    @Test
    void testSingleAssignment0() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                int i = 1
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_1',
                'ISTORE 1',
                'L1',
                'LINENUMBER 4',
                'RETURN'
            ])
    }

    @Test
    void testSingleAssignment1() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            int m() {
                int i = 1
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_1',
                'ISTORE 1',
                'L1',
                'ILOAD 1',
                'IRETURN'
            ])
    }

    // GROOVY-11288
    @Test
    void testSingleAssignment2() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            class C {
                int i
                void m() {
                    i = 1
                }
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 5',
                'ICONST_1',
                'ALOAD 0',
                'SWAP',
                'PUTFIELD C.i',
                'L1',
                'LINENUMBER 6',
                'RETURN'
            ])
    }

    // GROOVY-11288
    @Test
    void testSingleAssignment3() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            class C {
                int i
                void m() {
                    i ?= 1
                }
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 5',
                'ALOAD 0',
                'GETFIELD C.i',
                'DUP',
                'IFEQ L1',
                'ICONST_1',
                'GOTO L2',
                'L1',
                'FRAME SAME1 I',
                'ICONST_0',
                'L2',
                'FRAME FULL [C] [I I]',
                'IFEQ L3',
                'GOTO L4',
                'L3',
                'FRAME SAME1 I',
                'POP',
                'ICONST_1',
                'L4',
                'FRAME SAME1 I',
                // store and load temp var 1 gone
                'ALOAD 0',
                'SWAP',
                'PUTFIELD C.i',
                // load temp var 1 and pop gone
                'L5',
                'LINENUMBER 6',
                'RETURN'
            ])
    }

    // GROOVY-11288
    @Test
    void testSubscriptAssignment1() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m(int[] i) {
                i[0] = 1
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_1',
                'ISTORE 2',
                'ALOAD 1',
                'ICONST_0',
                'ILOAD 2',
                'INVOKEDYNAMIC set([III)V [',
                '// handle kind 0x6 : INVOKESTATIC',
                'org/codehaus/groovy/vmplugin/v8/IndyInterface.staticArrayAccess',
                '// arguments: none',
                ']',
                'L1',
                'LINENUMBER 4',
                'RETURN'
            ])
    }

    // GROOVY-11288
    @Test
    void testSubscriptAssignment2() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m(int[] i) {
                i?[0] = 1
            }
        ''').hasStrictSequence([
                // ... for putAt
                'L2',
                'FRAME SAME1 java/lang/Object',
                'POP',
                // load temp var 2 and pop gone
                'L3',
                'LINENUMBER 4',
                'RETURN'
            ])
    }

    // GROOVY-11286
    @Test
    void testVoidMethod1() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                print ""
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ALOAD 0',
                'LDC ""',
                'INVOKEVIRTUAL script.print (Ljava/lang/Object;)V',
                // drop: ACONST_NULL, POP
                'L1',
                'LINENUMBER 4',
                'RETURN'
            ])
    }

    // GROOVY-11286
    @Test
    void testVoidMethod2() {
        assert compile(method: 'm', '''import static java.lang.System.gc
            @groovy.transform.CompileStatic
            void m() {
                gc()
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 4',
                'INVOKESTATIC java/lang/System.gc ()V',
                // drop: ACONST_NULL, POP
                'L1',
                'LINENUMBER 5',
                'RETURN'
            ])
    }

    // GROOVY-11286
    @Test
    void testVoidMethod3() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                System.out?.print("")
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'GETSTATIC java/lang/System.out : Ljava/io/PrintStream;',
                'DUP',
                'ASTORE 1',
                'IFNULL L1',
                'ALOAD 1',
                'LDC ""',
                'INVOKEVIRTUAL java/io/PrintStream.print (Ljava/lang/String;)V',
                /* replaced all this with 'L1'
                'ACONST_NULL',
                'GOTO L2',
                'L1',
                'FRAME APPEND [java/io/PrintStream]',
                'ACONST_NULL',
                'L2',
                'FRAME SAME',
                'POP',
                */
                'L1',
                'LINENUMBER 4',
                'FRAME APPEND [java/io/PrintStream]',
                'RETURN'
            ])
    }

    // GROOVY-11286, GROOVY-11453
    @Test
    void testVoidMethod4() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m(List<C> list) {
                list*.proc()
            }
            class C {
                void proc() {
                }
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ALOAD 1',
                'IFNONNULL L1',
              //'ACONST_NULL',
                'GOTO L2',
                'L1',
                'FRAME SAME',
              //'NEW java/util/ArrayList',
              //'DUP',
              //'INVOKESPECIAL java/util/ArrayList.<init> ()V',
              //'DUP',
              //'ASTORE 2',
                'ALOAD 1',
                'DUP',
                'ASTORE 2',
                'IFNULL L3',
                'ALOAD 2',
                'INVOKEINTERFACE java/util/List.iterator ()Ljava/util/Iterator; (itf)',
                'GOTO L4',
                'L3',
                'FRAME',
                'ACONST_NULL',
                'L4',
                'FRAME',
                'ACONST_NULL',
                'ASTORE 3',
                'L5',
                'ASTORE 4',
                'ALOAD 4',
                'IFNULL L2',
                'L6',
                'FRAME',
                'ALOAD 4',
                'INVOKEINTERFACE java/util/Iterator.hasNext ()Z',
                'IFEQ L2',
                'ALOAD 4',
                'INVOKEINTERFACE java/util/Iterator.next ()Ljava/lang/Object;',
                'INVOKEDYNAMIC cast(Ljava/lang/Object;)LC; [',
                '// handle kind 0x6 : INVOKESTATIC',
                'org/codehaus/groovy/vmplugin/v8/IndyInterface.bootstrap(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;I)Ljava/lang/invoke/CallSite;',
                '// arguments:',
                '"()",',
                '0',
                ']',
                'ASTORE 3',
              //'ALOAD 2',
                'ALOAD 3',
                'DUP',
                'ASTORE 5',
                'IFNULL L7',
                'ALOAD 5',
                'INVOKEVIRTUAL C.proc ()V',
              //'ACONST_NULL',
              //'GOTO L8',
                'L7',
                'FRAME',
              //'ACONST_NULL',
              //'L8',
              //'FRAME',
              //'INVOKEVIRTUAL java/util/ArrayList.add (Ljava/lang/Object;)Z',
              //'POP',
                'GOTO L6',
                'L2',
                'LINENUMBER 4',
                'FRAME FULL [script java/util/List] []',
              //'POP',
                'RETURN'
            ])
    }

    // GROOVY-11630
    @Test
    void testVoidMethod5() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                def list = [1,2,3]
                list.shuffle()
            }
        ''').hasStrictSequence([
                'L1',
                'LINENUMBER 4 L1',
                'ALOAD 1',
                'INVOKESTATIC org/codehaus/groovy/runtime/DefaultGroovyMethods.shuffle (Ljava/util/List;)V',
                // drop: ACONST_NULL, POP
                'L2',
                'LINENUMBER 5 L2',
                'RETURN'
            ])
    }

    @Test
    void testIntLeftShift() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                int a = 1
                int b = a << 32
            }
        ''').hasStrictSequence([
                'ILOAD',
                'BIPUSH 32',
                'ISHL'
            ])
    }

    @Test
    void testLongLeftShift() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                long a = 1L
                long b = a << 32
            }
        ''').hasStrictSequence([
                'LLOAD',
                'BIPUSH 32',
                'LSHL'
            ])
    }

    @Disabled @Test
    void testPlusPlus() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m(int i) {
                i++
            }
        ''').hasStrictSequence([
                'IINC'
            ])
    }

    @Disabled @Test
    void testMinusMinus() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m(int i) {
                i--
            }
        ''').hasStrictSequence([
                'IINC'
            ])
    }

    @Test
    void testPlusEquals() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            int m() {
                int i = 0
                i += 13
                return i
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_0',
                'ISTORE 1',
                'L1',
                'LINENUMBER 4',
                'ILOAD 1',
                'BIPUSH 13',
                'IADD',
/*TODO*/        'DUP',
                'ISTORE 1',
/*TODO*/        'POP',
                'L2',
                'LINENUMBER 5',
                'ILOAD 1',
                'IRETURN'
            ])
    }

    @Test
    void testPlusEqualsFromArgs() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m(int i, int j) {
                i += j
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ILOAD 1',
                'ILOAD 2',
                'IADD',
/*TODO*/        'DUP',
                'ISTORE 1',
/*TODO*/        'POP',
                'L1',
                'LINENUMBER 4',
                'RETURN'
            ])
    }

    @Test
    void testFlow() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            String m(String str) {
                def obj = 1
                obj = str
                obj.toUpperCase()
            }
            m 'Cedric'
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_1',
                'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
                'ASTORE 2',
                'L1',
                'LINENUMBER 4',
                'ALOAD 1',
                'ASTORE 2',
                'L2',
                'LINENUMBER 5',
                'ALOAD 2',
                'CHECKCAST java/lang/String',
                'INVOKEVIRTUAL java/lang/String.toUpperCase ()Ljava/lang/String;',
                'ARETURN'
            ])
    }

    @Test
    void testInstanceOf() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m(Object str) {
                if (str instanceof String) {
                    str.toUpperCase()
                }
            }
            m 'Cedric'
        ''').hasStrictSequence([
                'ALOAD',
                'CHECKCAST java/lang/String',
                'INVOKEVIRTUAL java/lang/String.toUpperCase ()Ljava/lang/String;'
            ])
    }

    @Test
    void testShouldGenerateDirectConstructorCall() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            class Foo {
                String msg
                Foo(int x, String y) { msg = y*x }
                static Foo foo() {
                    Foo result = [2,'Bar']
                }
            }
        ''').hasStrictSequence([
                'ICONST_2',
                'LDC "Bar"',
                'INVOKESPECIAL Foo.<init> (ILjava/lang/String;)V'
            ])
    }

    @Test
    void testShouldGenerateDirectArrayConstruct() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                int[] arr = [123,456]
            }
        ''').hasStrictSequence([
                'ICONST_2',
                'NEWARRAY T_INT',
                'DUP',
                'ICONST_0',
                'BIPUSH 123',
                'IASTORE'
            ])
    }

    @Test
    void testShouldGenerateDirectBooleanArrayConstruct() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                boolean[] arr = [123,false]
            }
        ''').hasStrictSequence([
                'ICONST_2',
                'NEWARRAY T_BOOLEAN',
                'DUP',
                'ICONST_0',
                'BIPUSH 123',
                'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
                'INVOKESTATIC org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation.booleanUnbox (Ljava/lang/Object;)Z',
                'BASTORE'
            ])
    }

    @Test
    void testShouldTriggerDirectCallToOuterClassGetter() {
        assert compile(method: 'fromInner', classNamePattern: '.*Inner.*', '''
            class Holder {
                String value
            }

            @groovy.transform.CompileStatic
            class Outer {
                String outerProperty = 'outer'
                private class Inner {
                    String fromInner() {
                        Holder holder = new Holder()
                        holder.value = outerProperty
                        holder.value
                    }
                }

                String blah() {
                    new Inner().fromInner()
                }
            }

            def o = new Outer()
            assert o.blah() == 'outer'
        ''').hasStrictSequence([
                'GETFIELD Outer$Inner.this$0',
                'INVOKEVIRTUAL Outer.getOuterProperty',
                'DUP',
                'ASTORE',
                'ALOAD',
                'ALOAD',
                'INVOKEVIRTUAL Holder.setValue'
            ])
    }

    @Test
    void testShouldOptimizeBytecodeByAvoidingCreationOfMopMethods() {
        def clazz = new GroovyShell().evaluate '''import groovy.transform.*
            @CompileStatic
            class A {
                def doSomething() { 'A' }
            }
            @CompileStatic
            class B extends A {
                def doSomething() { 'B' + super.doSomething() }
            }

            B
        '''
        assert clazz.name == 'B'
        def mopMethods = clazz.declaredMethods.findAll { it.name =~ /(super|this)\$/ }
        assert mopMethods.isEmpty()
    }

    @Test
    void testShouldNotOptimizeBytecodeForMopMethodsBecauseOfSkip() {
        def clazz = new GroovyShell().evaluate '''import groovy.transform.*
            @CompileStatic
            class A {
                def doSomething() { 'A' }
            }
            @CompileStatic
            class B extends A {
                @CompileStatic(TypeCheckingMode.SKIP)
                def doSomething() { 'B' + super.doSomething() }
            }

            B
        '''
        assert clazz.name == 'B'
        def mopMethods = clazz.declaredMethods.findAll { it.name =~ /(super|this)\$/ }
        assert !mopMethods.isEmpty()
    }

    // GROOVY-7124
    @Test
    void testUseInvokeVirtualPreferredOverInvokeInterface() {
        assert compile(method: 'foo', classNamePattern: 'B', '''
            interface A {
                void m()
            }
            class B implements A {
                void m() {}
                @groovy.transform.CompileStatic
                void foo() {
                    m()
                }
            }
        ''').hasStrictSequence(['INVOKEVIRTUAL B.m'])
    }

    @Test
    void testShouldNotTryToCastToSupposedDelegateType() {
        assertScript '''
            @groovy.transform.CompileStatic
            class ClassCastOhNoes {
               def foo(def o) {
                   def cl = {
                       delegate.getClass()
                   }
                   cl.delegate = o
                   cl()
               }
            }
            new ClassCastOhNoes().foo(100)
        '''
    }

    @Test
    void testShouldOptimizeCharInit() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                char c = 'x'
            }
        ''').hasStrictSequence([
                'LINENUMBER 3',
                'BIPUSH 120',
                'ISTORE'
            ])
    }

    @Test
    void testShouldOptimizeCharComparison() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                char c1 = 'x'
                char c2 = 'x'
                boolean b = c1==c2
            }
        ''').hasSequence([
                'LINENUMBER 3',
                'BIPUSH 120',
                'ISTORE',
                'BIPUSH 120',
                'ISTORE',
                'ILOAD',
                'ILOAD',
                'IF_ICMPNE',
            ])

        // make sure the code passes
        assertScript '''
            @groovy.transform.CompileStatic
            void test() {
                char c1 = 'x'
                char c2 = 'x'
                boolean b = c1==c2
                assert b
                char c3 = 'z'
                b = c1==c3
                assert !b
            }
            test()
        '''
    }

    @Test
    void testForEachLoopOptimization() {
        def loop_init = [
                'ALOAD', // load array (might be more complex than just ALOAD in general case)
                'DUP',
                'ASTORE', // store array to local var
                'ARRAYLENGTH', // get array len
                'ISTORE', // store it to local var
                'ICONST_0',
                'ISTORE', // initialize loop index
                'ILOAD', // load loop index
                'ILOAD', // load array length
                'IF_ICMPGE' // if greater or equal, end of loop
        ]

        // int[]
        def intExample = '''
            @groovy.transform.CompileStatic
            void m(int[] arr) {
                for (int i: arr) {
                   println(i)
                }
            }
            m([1,2] as int[])
        '''
        assert compile(method:'m', intExample).hasSequence([*loop_init,'IALOAD','ISTORE'])

        // short[]
        def shortExample = '''
            @groovy.transform.CompileStatic
            void m(short[] arr) {
                for (short i: arr) {
                   println(i)
                }
            }
            m([1,2] as short[])
        '''
        assert compile(method:'m', shortExample).hasSequence([*loop_init,'SALOAD','ISTORE'])

        // byte[]
        def byteExample = '''
            @groovy.transform.CompileStatic
            void m(byte[] arr) {
                for (byte i: arr) {
                   println(i)
                }
            }
            m([1,2] as byte[])
        '''
        assert compile(method:'m', byteExample).hasSequence([*loop_init,'BALOAD','ISTORE'])

        // long[]
        def longExample = '''
            @groovy.transform.CompileStatic
            void m(long[] arr) {
                for (long i: arr) {
                   println(i)
                }
            }
            m([1,2] as long[])
        '''
        assert compile(method:'m', longExample).hasSequence([*loop_init,'LALOAD','LSTORE'])

        // char[]
        def charExample = '''
            @groovy.transform.CompileStatic
            void m(char[] arr) {
                for (char i: arr) {
                   println(i)
                }
            }
            m('foo'.toCharArray())
        '''
        assert compile(method:'m', charExample).hasSequence([*loop_init,'CALOAD','ISTORE'])

        // boolean[]
        def boolExample = '''
            @groovy.transform.CompileStatic
            void m(boolean[] arr) {
                for (boolean i: arr) {
                   println(i)
                }
            }
            m([true,false] as boolean[])
        '''
        assert compile(method:'m', boolExample).hasSequence([*loop_init,'BALOAD','ISTORE'])

        // float[]
        def floatExample = '''
            @groovy.transform.CompileStatic
            void m(float[] arr) {
                for (float i: arr) {
                   println(i)
                }
            }
            m([1.5f,2.0f] as float[])
        '''
        assert compile(method:'m', floatExample).hasSequence([*loop_init,'FALOAD','FSTORE'])

        // double[]
        def doubleExample = '''
            @groovy.transform.CompileStatic
            void m(double[] arr) {
                for (double i: arr) {
                   println(i)
                }
            }
            m([1.1,2.2] as double[])
        '''
        assert compile(method:'m', doubleExample).hasSequence([*loop_init,'DALOAD','DSTORE'])

        // Any[]
        def anyExample = '''
            @groovy.transform.CompileStatic
            void m(String[] arr) {
                for (String i: arr) {
                   println(i)
                }
            }
            m(['a','b'] as String[])
        '''
        assert compile(method:'m', anyExample).hasSequence([*loop_init,'AALOAD','ASTORE'])

        // now check that everything runs fine
        [byteExample, shortExample, intExample, charExample, boolExample, longExample, floatExample, doubleExample, anyExample].each { script ->
            assertScript(script)
        }
    }

    @Test
    void testCompareWithCharOptimization() {
        String code = '''
            @groovy.transform.CompileStatic
            boolean m(char[] arr) {
                char c = arr[0]
                ' '==c
            }
            assert m(' abc '.toCharArray())
        '''
        assert compile(method:'m',code).hasSequence(['BIPUSH','ILOAD','IF_ICMPNE'])
        assertScript(code)

        code = '''
            @groovy.transform.CompileStatic
            boolean m(char[] arr) {
                char c = arr[0]
                c==' '
            }
            assert m(' abc '.toCharArray())
        '''
        assert compile(method:'m',code).hasSequence(['ILOAD','BIPUSH','IF_ICMPNE'])
        assertScript(code)
    }

    @Test
    void testShouldRemoveUnnecessaryCast() {
        assert compile(method: 'm', '''@groovy.transform.CompileStatic
            void m() {
                char c = (char) 'x'
            }
        ''').hasStrictSequence([
                'LINENUMBER 3',
                'BIPUSH 120',
                // No checkcast, but the idea is to check that further optimization was done
                // because the RHS is no longer a CastExpression but a ConstantExpression
                'ISTORE'
            ])
    }

    @Test
    void testInstanceMethodReference() {
        // dynamic case should be a method closure
        assert String::toUpperCase instanceof MethodClosure

        // static case should be compiled into functional interface
        String code = '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors

            @CompileStatic
            void m() {
                assert ['foo'].stream().map(String::toUpperCase).collect(Collectors.toList()) == ['FOO']
            }
            m()
        '''
        assert compile(method:'m', code).hasSequence([
            'INVOKEDYNAMIC apply()Ljava/util/function/Function;',
            // handle kind 0x6 : INVOKESTATIC
            'java/lang/invoke/LambdaMetafactory.metafactory',
            // handle kind 0x5 : INVOKEVIRTUAL
            'java/lang/String.toUpperCase()Ljava/lang/String;'
        ])
        assertScript(code)
    }

    // GROOVY-6925
    @Test
    void testOuterSCInnerSTC() {
        assert compile(classNamePattern: 'C', method: 'test', '''
            import groovy.transform.*
            import static org.codehaus.groovy.control.CompilePhase.*
            import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.*

            @CompileStatic
            class C {
                Object myObject() {
                    Integer.valueOf(1)
                }
                // TODO: package this into an annotation
                @ASTTest(phase=CANONICALIZATION, value={
                    node.putNodeMetaData(STATIC_COMPILE_NODE, Boolean.FALSE)
                })
                void test() {
                    String myString = myObject()
                }
            }
        ''').hasStrictSequence([
                'ALOAD 0',
                'INVOKEDYNAMIC invoke(LC;)Ljava/lang/Object;' // not INVOKEVIRTUAL
            ])

        def err = shouldFail '''
            import groovy.transform.*
            import static org.codehaus.groovy.control.CompilePhase.*
            import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.*

            @CompileStatic
            class C {
                Object myObject() {
                    Integer.valueOf(1)
                }
                // TODO: package this into an annotation
                @ASTTest(phase=CANONICALIZATION, value={
                    node.putNodeMetaData(STATIC_COMPILE_NODE, Boolean.FALSE)
                })
                void test() {
                    Number myNumber = myObject()
                }
            }
        '''
        assert err =~ /Cannot assign value of type java.lang.Object to variable of type java.lang.Number/
    }
}
