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
package groovy.transform.stc

/**
 * Unit tests for static type checking : ternary operator.
 */
class TernaryOperatorSTCTest extends StaticTypeCheckingTestCase {

    void testByteByte() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == byte_TYPE
            })
            def y = true?(byte)1:(byte)0
        '''
    }

    void testShortShort() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == short_TYPE
            })
            def y = true?(short)1:(short)0
        '''
    }

    void testIntInt() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def y = true?1:0
        '''
    }

    void testLongLong() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == long_TYPE
            })
            def y = true?1L:0L
        '''
    }

    void testFloatFloat() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == float_TYPE
            })
            def y = true?1f:0f
        '''
    }

    void testDoubleDouble() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def y = true?1d:0d
        '''
    }

    void testBoolBool() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == boolean_TYPE
            })
            def y = true?true:false
        '''
    }

    void testDoubleFloat() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def y = true?1d:1f
        '''
    }

    void testDoubleDoubleWithBoxedTypes() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?new Double(1d):new Double(1f)
        '''
    }

    void testDoubleFloatWithBoxedTypes() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?new Double(1d):new Float(1f)
        '''
    }

    void testDoubleFloatWithOneBoxedType1() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?1d:new Float(1f)
        '''
    }

    void testDoubleFloatWithOneBoxedType2() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?new Double(1d):1f
        '''
    }

    // GROOVY-5523
    void testNull1() {
        assertScript '''
            def findFile() {
                String str = ""
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
                })
                File f = str ? new File(str) : null
            }
        '''
        assertScript '''
            def findFile() {
                String str = ""
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
                })
                File f = str ? null : new File(str)
            }
        '''
        assertScript '''
            def findFile() {
                String str = ""
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
                })
                File f = str ? null : null
            }
        '''
    }

    void testNull2() {
        assertScript '''
            def test(String str) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
                })
                String s = str ?: null
            }

            assert test('x') == 'x'
            assert test('') == null
        '''
    }

    // GROOVY-5734
    void testNull3() {
        assertScript '''
            Integer test() { false ? null : 42 }

            assert test() == 42
        '''
    }
}
