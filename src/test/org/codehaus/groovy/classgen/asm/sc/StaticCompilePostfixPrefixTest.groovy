package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class StaticCompilePostfixPrefixTest extends AbstractBytecodeTestCase {
    void testPostfixOnInt() {
        def bytecode = compile([method:'m'], '''@groovy.transform.CompileStatic
            void m() {
                int i = 0
                i++
                assert i==1
                assert i++==1
            }
        ''')
        clazz.newInstance().m()

        bytecode = compile([method:'m'], '''@groovy.transform.CompileStatic
            void m() {
                int i = 0
                i--
                assert i == -1
                assert i-- == -1
            }
        ''')

        clazz.newInstance().m()
    }

    void testPostfixOnDate() {
        def bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date tomorrow = d+1
                d++
                assert d == tomorrow
                assert d++ == tomorrow
                assert d == tomorrow +1
            }
        ''')

        clazz.newInstance().m()
        bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date yesterday = d - 1
                d--
                assert d == yesterday
                assert d-- == yesterday
                assert d == yesterday - 1
            }
        ''')

        clazz.newInstance().m()
    }

    void testPrefixOnInt() {
        def bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
         void m() {
            int i = 0
            ++i
            assert i==1
            assert ++i == 2
         }
        ''')
        clazz.newInstance().m()

        bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                int i = 0
                --i
                assert i==-1
                assert --i == -2
            }
        ''')
        clazz.newInstance().m()
    }

    void testPrefixOnDate() {
        def bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date tomorrow = d + 1
                Date aftertomorrow = d + 2
                ++d
                assert d == tomorrow
                assert ++d == aftertomorrow
            }
        ''')
        bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date yesterday = d - 1
                Date beforeyesterday = d - 2
                --d
                assert d == yesterday
                assert --d == beforeyesterday
            }
        ''')
    }

}
