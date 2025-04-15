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
 * Unit tests for static type checking : unary operators.
 */
class UnaryOperatorSTCTest extends StaticTypeCheckingTestCase {

    void testUnaryPlus_int() {
        assertScript '''
            int x = 1
            assert +x == 1
        '''
    }

    void testUnaryMinus_int() {
        assertScript '''
            int x = 1
            assert -x == -1
        '''
    }

    void testBitwiseNegate_int() {
        assertScript '''
            int x = 1
            assert ~x == -2
        '''
    }

    //

    void testUnaryPlus_long() {
        assertScript '''
            long x = 1L
            assert +x == 1L
        '''
    }

    void testUnaryMinus_long() {
        assertScript '''
            long x = 1L
            assert -x == -1L
        '''
    }

    // GROOVY-9704
    void testBitwiseNegate_long() {
        assertScript '''
            long x = 1L
            assert ~x == -2L
        '''
    }

    //

    void testUnaryPlus_short() {
        assertScript '''
            short x = 1
            assert +x == 1
        '''
    }

    void testUnaryMinus_short() {
        assertScript '''
            short x = 1
            assert -x == -1
        '''
    }

    void testBitwiseNegate_short() {
        assertScript '''
            short x = 1
            assert ~x == -2
        '''
    }

    //

    void testUnaryPlus_byte() {
        assertScript '''
            byte x = 1
            assert +x == 1
        '''
    }

    void testUnaryMinus_byte() {
        assertScript '''
            byte x = 1
            assert -x == -1
        '''
    }

    void testBitwiseNegate_byte() {
        assertScript '''
            byte x = 1
            assert ~x == -2
        '''
    }

    //

    void testUnaryPlus_char() {
        shouldFail MissingMethodException, '''
            char x = 1
            +x
        '''
    }

    void testUnaryMinus_char() {
        shouldFail MissingMethodException, '''
            char x = 1
            -x
        '''
    }

    void testBitwiseNegate_char() {
        shouldFail MissingMethodException, '''
            char x = 1
            ~x
        '''
    }

    //

    void testUnaryPlus_float() {
        assertScript '''
            float x = 1f
            assert +x == 1f
        '''
    }

    void testUnaryMinus_float() {
        assertScript '''
            float x = 1f
            assert -x == -1f
        '''
    }

    void testBitwiseNegate_float() {
        shouldFail UnsupportedOperationException, '''
            float x = 1f
            ~x
        '''
    }

    //

    void testUnaryPlus_double() {
        assertScript '''
            double x = 1d
            assert +x == 1d
        '''
    }

    void testUnaryMinus_double() {
        assertScript '''
            double x = 1d
            assert -x == -1d
        '''
    }

    void testBitwiseNegate_double() {
        shouldFail UnsupportedOperationException, '''
            double x = 1d
            ~x
        '''
    }

    //

    void testUnaryPlus_Integer() {
        assertScript '''
            Integer x = new Integer(1)
            assert +x == 1
        '''
    }

    void testUnaryMinus_Integer() {
        assertScript '''
            Integer x = new Integer(1)
            assert -x == -1
        '''
    }

    void testBitwiseNegate_Integer() {
        assertScript '''
            Integer x = new Integer(1)
            assert ~x == -2
        '''
    }

    //

    void testUnaryPlus_Long() {
        assertScript '''
            Long x = new Long(1L)
            assert +x == 1L
        '''
    }

    void testUnaryMinus_Long() {
        assertScript '''
            Long x = new Long(1L)
            assert -x == -1L
        '''
    }

    void testBitwiseNegate_Long() {
        assertScript '''
            Long x = new Long(1L)
            assert ~x == -2L
        '''
    }

    //

    void testUnaryPlus_Short() {
        assertScript '''
            Short x = new Short((short)1)
            assert +x == 1
        '''
    }

    void testUnaryMinus_Short() {
        assertScript '''
            Short x = new Short((short)1)
            assert -x == -1
        '''
    }

    void testBitwiseNegate_Short() {
        assertScript '''
            Short x = new Short((short)1)
            assert ~x == -2
        '''
    }

    //

    void testUnaryPlus_Byte() {
        assertScript '''
            Byte x = new Byte((byte)1)
            assert +x == 1
        '''
    }

    void testUnaryMinus_Byte() {
        assertScript '''
            Byte x = new Byte((byte)1)
            assert -x == -1
        '''
    }

    void testBitwiseNegate_Byte() {
        assertScript '''
            Byte x = new Byte((byte)1)
            assert ~x == -2
        '''
    }

    //

    void testUnaryPlus_Character() {
        shouldFail MissingMethodException, '''
            Character x = new Character((char)1)
            +x
        '''
    }

    void testUnaryMinus_Character() {
        shouldFail MissingMethodException, '''
            Character x = new Character((char)1)
            -x
        '''
    }

    void testBitwiseNegate_Character() {
        shouldFail MissingMethodException, '''
            Character x = new Character((char)1)
            ~x
        '''
    }

    //

    void testUnaryPlus_Float() {
        assertScript '''
            Float x = new Float(1f)
            assert +x == 1f
        '''
    }

    void testUnaryMinus_Float() {
        assertScript '''
            Float x = new Float(1f)
            assert -x == -1f
        '''
    }

    void testBitwiseNegate_Float() {
        shouldFail UnsupportedOperationException, '''
            Float x = new Float(1f)
            ~x
        '''
    }

    //

    void testUnaryPlus_Double() {
        assertScript '''
            Double x = new Double(1d)
            assert +x == 1d
        '''
    }

    void testUnaryMinus_Double() {
        assertScript '''
            Double x = new Double(1d)
            assert -x == -1d
        '''
    }

    void testBitwiseNegate_Double() {
        shouldFail UnsupportedOperationException, '''
            Double x = new Double(1d)
            ~x
        '''
    }

    //

    void testIntXIntInferredType() {
        assertScript '''
            int x = 1
            int y = 2
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def zp = x+y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def zm = x*y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def zmi = x-y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == BigDecimal_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == BigDecimal_TYPE
            })
            def zd = x/y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def zmod = x%y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def zpeq = (x+=y)
        '''
    }

    void testDoubleXDoubleInferredType() {
        assertScript '''
            double x = 1
            double y = 2
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def zp = x+y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def zm = x*y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def zmi = x-y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def zd = x/y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def zmod = x%y

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
                def right = node.rightExpression
                assert right.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def zpeq = (x+=y)
        '''
    }

    void testIntUnaryMinusInferredType() {
        assertScript '''
            int x = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def y = -x
        '''
    }

    void testShortUnaryMinusInferredType() {
        assertScript '''
            short x = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == short_TYPE
            })
            def y = -x
        '''
    }

    void testByteUnaryMinusInferredType() {
        assertScript '''
            byte x = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == byte_TYPE
            })
            def y = -x
        '''
    }

    void testLongUnaryMinusInferredType() {
        assertScript '''
            long x = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == long_TYPE
            })
            def y = -x
        '''
    }

    void testFloatUnaryMinusInferredType() {
        assertScript '''
            float x = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == float_TYPE
            })
            def y = -x
        '''
    }

    void testDoubleUnaryMinusInferredType() {
        assertScript '''
            double x = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.getNodeMetaData(INFERRED_TYPE) == double_TYPE
            })
            def y = -x
        '''
    }

    // GROOVY-5834
    void testCreatePatternInField() {
        assertScript '''
            class Sample {
                def pattern = ~'foo|bar'
                void test() {
                    assert pattern instanceof java.util.regex.Pattern
                }
            }
            new Sample().test()
        '''
    }

    // GROOVY-6223
    void testShouldNotRequireExplicitTypeDefinition() {
        assertScript '''
        def i = 0
        def j = 0

        int x = i++
        int y = ++j
        assert x == 0
        assert y == 1
        '''
    }
}
