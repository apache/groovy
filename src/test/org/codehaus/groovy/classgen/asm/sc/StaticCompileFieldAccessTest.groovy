package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

/**
 * Unit tests for static compilation: access field nodes.
 *
 * @author Cedric Champeau
 */
class StaticCompileFieldAccessTest extends AbstractBytecodeTestCase {
    void testAccessProperty() {
        compile(method:'m', '''
            class A {
                int x
            }

            A a = new A()
            @groovy.transform.CompileStatic
            void m(A a) {
                a.x = 10
            }
            m(a)
            assert a.x == 10
        ''')

        clazz.newInstance().run()
    }

    void testAccessField() {
        compile(method:'m', '''
            class A {
                int x
            }

            A a = new A()
            @groovy.transform.CompileStatic
            void m(A a) {
                a.@x = 10
            }
            m(a)
            assert a.@x == 10
        ''')

        clazz.newInstance().run()
    }

    void testReturnProperty() {
        compile(method:'m', '''
            class A {
                int x = 10
            }

            A a = new A()
            @groovy.transform.CompileStatic
            int m(A a) {
                return a.x
            }
            assert m(a) == 10
        ''')
        assert sequence.hasStrictSequence([
                'ALOAD',
                'INVOKEVIRTUAL A.getX ()I',
                'IRETURN'
        ])

        clazz.newInstance().run()
    }

    void testReturnPublicField() {
        compile(method:'m', '''
            class A {
                public int x = 10
            }

            A a = new A()
            @groovy.transform.CompileStatic
            int m(A a) {
                return a.x
            }
            assert m(a) == 10
        ''')
        assert sequence.hasStrictSequence([
                'ALOAD',
                'GETFIELD A.x : I',
                'IRETURN'
        ])

        clazz.newInstance().run()
    }

    void testReturnProtectedField() {
        compile(method:'m', '''
            class A {
                protected int x = 10
            }

            A a = new A()
            @groovy.transform.CompileStatic
            int m(A a) {
                return a.x
            }
            assert m(a) == 10
        ''')
        assert sequence.hasStrictSequence([
                'ALOAD',
                'GETFIELD A.x : I',
                'IRETURN'
        ])

        clazz.newInstance().run()
    }

    void testReturnProtectedFieldInDifferentPackage() {
        compile(method:'m', '''
            import org.codehaus.groovy.classgen.asm.sc.StaticCompileFieldAccessTest.StaticCompileFieldAccessSupport1 as A

            A a = new A()
            @groovy.transform.CompileStatic
            int m(A a) {
                return a.x
            }
            assert m(a) == 10
        ''')
        assert sequence.hasStrictSequence([
                'ALOAD',
                'LDC "x"',
                'INVOKEINTERFACE groovy/lang/GroovyObject.getProperty (Ljava/lang/String;)Ljava/lang/Object;'
        ])

        clazz.newInstance().run()
    }

    void testReturnPublicFieldFromNonGroovyObject() {
        compile(method:'m', '''
            java.awt.Point a = [100,200]

            @groovy.transform.CompileStatic
            int m(java.awt.Point a) {
                return a.@x
            }
            assert m(a) == 100
        ''')
       assert sequence.hasStrictSequence([
                'ALOAD',
                'GETFIELD java/awt/Point.x : I',
                'IRETURN'
        ])

        clazz.newInstance().run()
    }

    void testReturnFieldFromNonGroovyObjectUsingGetter() {
        compile(method:'m', '''
            java.awt.Point a = [100,200]

            @groovy.transform.CompileStatic
            double usingGetter(java.awt.Point a) {
                return a.x
            }
            assert usingGetter(a) == 100
        ''')
       assert sequence.hasStrictSequence([
                'ALOAD',
                'INVOKEVIRTUAL java/awt/Point.getX ()D',
                'DRETURN'
        ])

        clazz.newInstance().run()
    }

    void testReturnPropertyFromNonGroovyObject() {
        compile(method:'m', '''
            Object a = 'hello'

            @groovy.transform.CompileStatic
            Class m(Object a) {
                return a.class // getClass()
            }
            assert m(a) == String
        ''')
       assert sequence.hasStrictSequence([
                'ALOAD',
                'INVOKEVIRTUAL java/lang/Object.getClass ()Ljava/lang/Class;',
                'ARETURN'
        ])

        clazz.newInstance().run()
    }

    void testReturnPrivateFieldFromNonGroovyObjectAndNoGetter() {
        shouldFail {
            compile(method:'m', '''
                Date a = new Date()

                @groovy.transform.CompileStatic
                long m(Date a) {
                    return a.fastTime // this is a private member
                }
            ''')
        }
    }

    /*
     * this test should fail, but passes because it generates ScriptBytecodeAdapter.getGroovyObjectField
     * instead of direct field access
    void testShouldFailToReturnPrivateField() {
        shouldFail {
            compile(method: 'm', '''
            package test

            class A {
                int x = 10
            }

            A a = new A()
            @groovy.transform.CompileStatic
            int m(A a) {
                return a.@x // x is a private member, direct access should not be allowed
            }
            assert m(a) == 10
        ''')
            println sequence
        }
    }*/

    public static class StaticCompileFieldAccessSupport1 {
        protected int x = 10
    }

}
