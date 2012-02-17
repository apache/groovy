package groovy.bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class Groovy5267Bug extends AbstractBytecodeTestCase {
    void testShouldNotThrowVerifyError() {
        shouldFail(MissingMethodException) {
            assertScript '''
            class Bar {
               int defaultValue = 40
               void m() {
                defaultValue()
               }
            }
            new Bar().m()
        '''
        }
    }
    
    void testClosureCallBytecode() {
        def bytecode= compile(method:'m', '''
        class BarScript {
               int defaultValue = 40
               void m() {
                defaultValue()
               }
        }
        ''')
        
        assert bytecode.hasSequence(['INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;'])
    }
}
