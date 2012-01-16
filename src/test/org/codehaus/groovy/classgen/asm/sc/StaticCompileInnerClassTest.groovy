package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class StaticCompileInnerClassTest extends AbstractBytecodeTestCase {
    void testStaticCompileCallToOwnerField() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            class Config {
                String path
                class Inner {
                    String m() { path }
                }
                String foo() { new Inner().m() }
            }
            def c = new Config(path:'/tmp')
            assert c.foo() == '/tmp'
        ''')
        clazz.newInstance().main()

    }

    void testStaticCompileCallToOwnerMethod() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            class Config {
                String path
                class Inner {
                    String m() { bar() }
                }
                String bar() { path }
                String foo() { new Inner().m() }
            }
            def c = new Config(path:'/tmp')
            assert c.foo() == '/tmp'
        ''')
        clazz.newInstance().main()

    }

    void testStaticCompileCallToOwnerPrivateMethod() {

        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            class Config {
                String path
                class Inner {
                    String m() { bar() }
                }
                private String foo() { new Inner().m() }
                String bar() { path }
            }
            def c = new Config(path:'/tmp')
            assert c.foo() == '/tmp'
        ''')
        clazz.newInstance().main()

        /*assert bytecode.hasStrictSequence(
                ['public m()V', 'L0', 'RETURN']
        )*/
    }
}
