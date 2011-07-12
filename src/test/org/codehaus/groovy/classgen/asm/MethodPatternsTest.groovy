package org.codehaus.groovy.classgen.asm


/**
 * @author Jochen Theodorou
 */
class MethodPatternsTest extends AbstractBytecodeTestCase {

    // make a test for native compilation of the ackerman function
    // and ensure the nested call is optimized
    void testAckerman() {
        assert compile(method: 'A', '''
                int A(int x, int y) {
                    if (x == 0) return y+1
                    if (y == 0) return A(x-1, 1)
                    return A(x-1, A(x, y-1))
                }
            ''').hasSequence([
                // if (x==0) return y+1
                'ILOAD 1',
                'LDC 0',
                'IF_ICMPNE',
                'ICONST_1',
                'ICONST_0',
                'IFEQ',     // x==0 and branching till here
                'ILOAD 2',
                'LDC 1',
                'IADD',     // y+1
                'IRETURN',  // return
                // if (y==0) return A(x-1,1)
                'ILOAD 2',
                'LDC 0',
                'IF_ICMPNE',
                'ICONST_1',
                'ICONST_0',
                'IFEQ',     // y==0 and branching till here
                'ALOAD 0',
                'ILOAD 1',
                'LDC 1',
                'ISUB',     // x-1 argument
                'LDC 1',
                'INVOKEVIRTUAL script.A (II)I', // A(x-1,1)
                'IRETURN',  //return
                // return A(x-1,A(x,y-1))
                'ALOAD 0',
                'ILOAD 1',
                'LDC 1',
                'ISUB',     // outer A x-1 argument
                'ALOAD 0',
                'ILOAD 1',  // inner A x argument
                'ILOAD 2',
                'LDC 1',
                'ISUB',     //inner A y-1 argument
                'INVOKEVIRTUAL script.A (II)I', // inner A
                'INVOKEVIRTUAL script.A (II)I', // outer A
                'IRETURN' //return
            ])
    }
    
}