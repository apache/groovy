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

class StaticCompilationTest extends AbstractBytecodeTestCase {
    void testEmptyMethod() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            void m() {}
        ''')
        assert bytecode.hasStrictSequence(
                ['public m()V',
                        'L0',
                        'LINENUMBER 3 L0',
                        'RETURN']
        )
    }

    void testPrimitiveReturn1() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() { 1 }
        ''')
        assert bytecode.hasStrictSequence(
                ['ICONST_1', 'IRETURN']
        )
    }

    void testPrimitiveReturn2() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            long m() { 1L }
        ''')
        assert bytecode.hasStrictSequence(
                ['LCONST_1', 'LRETURN']
        )
    }

    void testPrimitiveReturn3() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            short m() { 1 }
        ''')
        assert bytecode.hasStrictSequence(
                ['ICONST_1', 'I2S', 'IRETURN']
        )
    }

    void testPrimitiveReturn4() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            byte m() { 1 }
        ''')
        assert bytecode.hasStrictSequence(
                ['ICONST_1', 'I2B', 'IRETURN']
        )
    }

    void testIdentityReturns() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m(int i) { i }
        ''')
        assert bytecode.hasStrictSequence(
                ['ILOAD', 'IRETURN']
        )

        bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            long m(long l) { l }
        ''')
        assert bytecode.hasStrictSequence(
                ['LLOAD', 'LRETURN']
        )

        bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            short m(short l) { l }
        ''')
        assert bytecode.hasStrictSequence(
                ['ILOAD', 'IRETURN']
        )

        bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            float m(float l) { l }
        ''')
        assert bytecode.hasStrictSequence(
                ['FLOAD', 'FRETURN']
        )

        bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            double m(double l) { l }
        ''')
        assert bytecode.hasStrictSequence(
                ['DLOAD', 'DRETURN']
        )

        bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            Object m(Object l) { l }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD', 'ARETURN']
        )
    }

    void testSingleAssignment() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            int a = 1
        }''').hasSequence([
                "ICONST_1",
                "ISTORE",
                "RETURN"
        ])
    }

    void testReturnSingleAssignment() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        int m() {
            int a = 1
        }''').hasSequence([
                "ICONST_1",
                "ISTORE",
                "ILOAD",
                "IRETURN"
        ])
    }

    void testIntLeftShift() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            int a = 1
            int b = a << 32
        }''').hasStrictSequence([
                "ILOAD",
                "BIPUSH 32",
                "ISHL"
        ])
    }

    void testLongLeftShift() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            long a = 1L
            long b = a << 32
        }''').hasStrictSequence([
                "LLOAD",
                "BIPUSH 32",
                "LSHL"
        ])
    }

    void testArrayGet() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m(int[] arr) {
            arr[0]
        }''').hasStrictSequence([
                "ALOAD 1",
                "ICONST_0",
                "INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArrayGet ([II)I"
        ])
    }

    void testArraySet() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m(int[] arr) {
            arr[0] = 0
        }''').hasStrictSequence([
                "ICONST_0",
                "ISTORE 2",
                "ALOAD 1",
                "ICONST_0",
                "ILOAD 2",
                "INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet ([III)V"
        ])
    }

/*    void testPlusPlus() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            int i = 0
            i++
        }''').hasStrictSequence([
                "IINC",
        ])

    }

    void testMinusMinus() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            int i = 0
            i--
        }''').hasStrictSequence([
                "IINC",
        ])

    }

    void testPlusEquals() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        int m() {
            int i = 0
            i += 13
            return i
        }''').hasStrictSequence([
                "ILOAD",
                "ILOAD",
                "IADD",
                "ISTORE"
        ])
    }

    void testPlusEqualsFromArgs() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m(int i, int j) {
            i += j
        }''').hasStrictSequence([
                "ILOAD",
                "ILOAD",
                "IADD",
                "ISTORE"
        ])
    }*/

    void testFlow() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        String m(String str) {
            def obj = 1
            obj = str
            obj.toUpperCase()
        }
        m 'Cedric'
        ''').hasStrictSequence([
                "ICONST",
                "INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;",
                "ASTORE",
                "L1",
                "ALOAD 2",
                "POP",
                "L2",
                "LINENUMBER",
                "ALOAD 1",
                "ASTORE 3",
                "ALOAD 3",
                "ASTORE 2",
                "ALOAD 3",
                "POP",
                "L3",
                "LINENUMBER",
                "ALOAD 2",
                "CHECKCAST java/lang/String",
                "INVOKEVIRTUAL java/lang/String.toUpperCase ()Ljava/lang/String;",
                "ARETURN",
                "L4"
        ])
    }

    void testInstanceOf() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m(Object str) {
            if (str instanceof String) {
                str.toUpperCase()
            }
        }
        m 'Cedric'
        ''').hasStrictSequence([
                "ALOAD",
                "CHECKCAST java/lang/String",
                "INVOKEVIRTUAL java/lang/String.toUpperCase ()Ljava/lang/String;"
        ])
    }

    void testShouldGenerateDirectConstructorCall() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
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

    void testShouldGenerateDirectArrayConstruct() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
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

    void testShouldGenerateDirectBooleanArrayConstruct() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
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

    void testShouldTriggerDirectCallToOuterClassGetter() {
        assert compile([method: 'fromInner',classNamePattern:'.*Inner.*'], '''
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

    void testShouldOptimizeBytecodeByAvoidingCreationOfMopMethods() {
        def shell = new GroovyShell()
        def clazz = shell.evaluate '''
            import groovy.transform.TypeCheckingMode
            import groovy.transform.CompileStatic

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
        assert clazz instanceof Class
        assert clazz.name == 'B'
        def mopMethods = clazz.declaredMethods.findAll { it.name =~ /(super|this)\$/ }
        assert mopMethods.empty
    }

    void testShouldNotOptimizeBytecodeForMopMethodsBecauseOfSkip() {
        def shell = new GroovyShell()
        def clazz = shell.evaluate '''
            import groovy.transform.TypeCheckingMode
            import groovy.transform.CompileStatic

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
        assert clazz instanceof Class
        assert clazz.name == 'B'
        def mopMethods = clazz.declaredMethods.findAll { it.name =~ /(super|this)\$/ }
        assert !mopMethods.empty
    }

    // GROOVY-7124
    void testUseInvokeVirtualPreferredOverInvokeInterface() {
        assert compile([method: 'foo',classNamePattern:'B'], '''
        interface A { void m() }
        class B implements A {
            void m() {}
            @groovy.transform.CompileStatic
            void foo() {
                m()
            }
        }

        ''').hasStrictSequence(['INVOKEVIRTUAL B.m'])
    }

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

    void testShouldOptimizeCharInit() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            char c = 'x'
        }
        ''').hasStrictSequence([
                'LINENUMBER 4',
                'BIPUSH 120',
                'ISTORE'
        ])
    }

    void testShouldOptimizeCharComparison() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            char c1 = 'x'
            char c2 = 'x'
            boolean b = c1==c2
        }
        ''').hasSequence([
                'LINENUMBER 4',
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
            void m() {
                char c1 = 'x'
                char c2 = 'x'
                boolean b = c1==c2
                assert b
                char c3 = 'z'
                b = c1==c3
                assert !b
            }
            m()
        '''
    }

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
        assert compile([method:'m'], intExample).hasSequence([*loop_init,'IALOAD','ISTORE'])

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
        assert compile([method:'m'], shortExample).hasSequence([*loop_init,'SALOAD','ISTORE'])

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
        assert compile([method:'m'], byteExample).hasSequence([*loop_init,'BALOAD','ISTORE'])

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
        assert compile([method:'m'], longExample).hasSequence([*loop_init,'LALOAD','LSTORE'])

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
        assert compile([method:'m'], charExample).hasSequence([*loop_init,'CALOAD','ISTORE'])

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
        assert compile([method:'m'], boolExample).hasSequence([*loop_init,'BALOAD','ISTORE'])

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
        assert compile([method:'m'], floatExample).hasSequence([*loop_init,'FALOAD','FSTORE'])

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
        assert compile([method:'m'], doubleExample).hasSequence([*loop_init,'DALOAD','DSTORE'])

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
        assert compile([method:'m'], anyExample).hasSequence([*loop_init,'AALOAD','ASTORE'])

        // now check that everything runs fine
        [byteExample,shortExample, intExample, charExample, boolExample,
         longExample, floatExample, doubleExample, anyExample].each { script ->
            assertScript(script)
        }
    }

    void testCompareWithCharOptimization() {
        String code = '''
        @groovy.transform.CompileStatic
        boolean m(char[] arr) {
            char c = arr[0]
            ' '==c
        }
        assert m(' abc '.toCharArray()) == true
        '''
        assert compile([method:'m'],code).hasSequence(['BIPUSH','ILOAD','IF_ICMPNE'])
        assertScript(code)

        code = '''
        @groovy.transform.CompileStatic
        boolean m(char[] arr) {
            char c = arr[0]
            c==' '
        }
        assert m(' abc '.toCharArray()) == true
        '''
        assert compile([method:'m'],code).hasSequence(['ILOAD','BIPUSH','IF_ICMPNE'])
        assertScript(code)
    }

    void testShouldRemoveUnnecessaryCast() {
        assert compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            char c = (char) 'x'
        }
        ''').hasStrictSequence([
                'LINENUMBER 4',
                'BIPUSH 120',
                // No checkcast, but the idea is to check that further optimization was done
                // because the RHS is no longer a CastExpression but a ConstantExpression
                'ISTORE'
        ])
    }
}
