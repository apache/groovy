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


package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy7325Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testGenericIdentityWithClosure() {
        assertScript '''
public static <T> T identity(T self) { self }

@ASTTest(phase=INSTRUCTION_SELECTION,value={
    assert node.rightExpression.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
})
Integer i = identity(2)

@ASTTest(phase=INSTRUCTION_SELECTION,value={
    assert node.rightExpression.getNodeMetaData(INFERRED_TYPE) == CLOSURE_TYPE
})
Closure c = identity {'foo'}
'''
    }

    void testShouldNotThrowIllegalAccessToProtectedData() {
        shouldFailWithMessages('''
            class Test {
              final Set<String> HISTORY = [] as HashSet

              Set<String> getHistory() {
                return HISTORY.clone() as HashSet<String>
              }
            }

            Test test = new Test()
            println test.history
        ''', 'Method clone is protected in java.lang.Object')
    }
}
