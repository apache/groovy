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

class RangesSTCTest extends StaticTypeCheckingTestCase{
    // GROOVY-5699
    void testIntRangeInference() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == make(IntRange)
            })
            def range = 1..10

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
            })
            def from = range.fromInt
        '''
    }

    // GROOVY-6124
    void testInferrenceOfIntRange() {
        assertScript '''
            String[] args = ['a','b','c','d']

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                assert node.getNodeMetaData(INFERRED_TYPE) == make(IntRange)
            })
            def range = 1..-1

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def irt = node.getNodeMetaData(INFERRED_TYPE)
                assert irt == LIST_TYPE
                assert irt.isUsingGenerics()
                assert irt.genericsTypes[0].type == STRING_TYPE
            })
            def arr = args[range]
            assert arr == ['b','c','d']
        '''
    }

    // GROOVY-
    void testInferenceOfBigIntRange() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def irt = node.getNodeMetaData(INFERRED_TYPE)
                assert irt==make(Range)
                assert irt.isUsingGenerics()
                assert irt.genericsTypes[0].type == make(BigInteger)
            })
            def range = (1G..1G)
            range.each { BigInteger bi -> println bi}
        '''
    }

}
