package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

/**
 * Tests for static compilation: checks that closures are called properly.
 */
class StaticCompileClosureCallTest extends AbstractBytecodeTestCase {
    void testShouldCallClosure() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { 666 }
                return closure()
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

    void testShouldCallClosureWithOneArg() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { int x -> x }
                return closure(666)
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

    void testShouldCallClosureWithTwoArgs() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { int x, int y -> x+y }
                return closure(333,333)
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

    void testShouldCallClosureWithThreeArgs() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { int x, int y, int z -> z*(x+y) }
                return closure(333,333,1)
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

    void testStaticCompilationOfClosures() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
              final static int VAL = 333
              void m() {
                 def cl = { println 'Hello' }
                 cl()
              }
              String mString() {
                 def cl = { 'Hello' }
                 cl()
              }
              int mInt() {
                 def cl = { 666 }
                 cl()
              }
              int mEmbedInt() {
                  def cl = {
                      def cl2 = { 666 }
                      cl2()
                  }
                  cl()
              }
              int mConst() {
                  def cl = { VAL }
                  cl()
              }
            }

            def a = new A()
            a.m()
            assert a.mString() == 'Hello'
            assert a.mInt()==666
            assert a.mEmbedInt() == 666
            assert a.mConst() == 333
        '''
    }

    void testWriteSharedVariableInClosure() {
        def bytecode = compile([method:'m'],'''
        @groovy.transform.CompileStatic
        void m() {
            String test = 'test'
            def cl = { test = 'TEST' }
            cl()
            assert test == 'TEST'
        }
        ''')
        clazz.newInstance().main()
    }

    void testCallPrivateMethodFromClosure() {
        assertScript '''
        @groovy.transform.CompileStatic
        class Foo {
            void m() {
                String test = 'test'
                def cl = { test = bar() }
                cl()
                assert test == 'TEST'
            }
            private String bar() { 'TEST' }
        }
        new Foo().m()
        '''
    }

    void testCallStaticPrivateMethodFromClosure() {
        assertScript '''
        @groovy.transform.CompileStatic
        class Foo {
            void m() {
                String test = 'test'
                def cl = { test = bar() }
                cl()
                assert test == 'TEST'
            }
            private static String bar() { 'TEST' }
        }
        new Foo().m()
        '''
    }

    void testCallMethodWithinClosure() {
        assertScript '''
        @groovy.transform.CompileStatic
        class Foo {
            static void m(StackTraceElement[] trace) {
                trace.each { StackTraceElement stackTraceElement -> !stackTraceElement.className.startsWith('foo') }
            }
        }
        1
        '''
    }
    
    // GROOVY-6199
    void testCallClassMethodFromNestedClosure() {
        assertScript '''
            class MyClass {
                void run() {
                    1.times {
                        1.times {
                            myMethod()
                        }
                    }
                }
                void myMethod() {
                    bool = true
                }
                def bool = false
            }
            def mc = new MyClass()
            mc.run()
            assert mc.bool
        '''
    }
}
