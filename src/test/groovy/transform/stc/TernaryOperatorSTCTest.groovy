/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

/**
 * Unit tests for static type checking : ternary operator tests.
 *
 * @author Cedric Champeau
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
}

