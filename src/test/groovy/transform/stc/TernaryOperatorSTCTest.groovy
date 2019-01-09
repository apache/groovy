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
 * Unit tests for static type checking : ternary operator tests.
 */
class TernaryOperatorSTCTest extends StaticTypeCheckingTestCase {

    void testByteByteTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == byte_TYPE
            })
            def y = true?(byte)1:(byte)0
        '''
    }
    void testShortShortTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == short_TYPE
            })
            def y = true?(short)1:(short)0
        '''
    }

    void testIntIntTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def y = true?1:0
        '''
    }

    void testLongLongTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == long_TYPE
            })
            def y = true?1L:0L
        '''
    }

    void testFloatFloatTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == float_TYPE
            })
            def y = true?1f:0f
        '''
    }

    void testDoubleDoubleTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def y = true?1d:0d
        '''
    }

    void testBoolBoolTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == boolean_TYPE
            })
            def y = true?true:false
        '''
    }

    void testDoubleFloatTernary() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def y = true?1d:1f
        '''
    }

    void testDoubleDoubleTernaryWithBoxedTypes() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?new Double(1d):new Double(1f)
        '''
    }

    void testDoubleFloatTernaryWithBoxedTypes() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?new Double(1d):new Float(1f)
        '''
    }

    void testDoubleFloatTernaryWithOneBoxedType() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?1d:new Float(1f)
        '''
    }

    void testDoubleFloatTernaryWithOneBoxedType2() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == Double_TYPE
            })
            def y = true?new Double(1d):1f
        '''
    }

    // GROOVY-5523
    void testTernaryOperatorWithNull() {
        assertScript '''File findFile() {
            String str = ""
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
            })
            File f = str ? new File(str) : null
        }
        '''
        assertScript '''File findFile() {
            String str = ""
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
            })
            File f = str ? null : new File(str)
        }
        '''
        assertScript '''File findFile() {
            String str = ""
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == make(File)
            })
            File f = str ? null : null
        }
        '''
    }
    void testElvisOperatorWithNull() {
        assertScript '''String findString() {
            String str = ""
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
            })
            String f = str ?: null
        }
        '''
    }

    // GROOVY-5734
    void testNullInTernary() {
        assertScript '''
            Integer someMethod() { (false) ? null : 8 }
            assert someMethod() == 8
        '''
    }
}

