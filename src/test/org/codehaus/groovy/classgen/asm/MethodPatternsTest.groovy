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
    
    void testForLoopSettingArray() {
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
            'LDC',
            'IADD',
            'ISTORE',
            'ALOAD',
            'ILOAD',
            'ILOAD',
            'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet ([III)V',
            'ILOAD',
            'DUP',
            'ISTORE',
            'ICONST_1',
            'IADD',
            'DUP',
            'ISTORE 11',
            'POP',
            'GOTO'
        ])
    }
    
    void testForLoopSettingArrayWithOperatorUsedInAssignmentAndArrayRHS() {
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
            'GOTO L14'
        ])
    }
    
    void testRoghtShiftUnsignedWithLongArgument() {
        assert compile(method:"hashCode", '''
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
            'LDC 32',
            'LUSHR',
            'LXOR',
            'L2I',
            'IADD',
        ])
    }
}