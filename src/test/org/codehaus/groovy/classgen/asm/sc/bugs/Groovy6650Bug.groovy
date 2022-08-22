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

final class Groovy6650Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testShouldChooseVargsMethod() {
        assertScript '''
            import org.codehaus.groovy.transform.stc.ExtensionMethodNode

            def x = 'a,b,c'.split(',')
            def y = 'd,e,f'.split(',')

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def mn = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert mn instanceof ExtensionMethodNode
                def td = mn.extensionMethodNode.typeDescriptor
                assert td == 'java.lang.Object[] plus(java.lang.Object[], java.lang.Object[])'
            })
            String[] result = x + y
            assert result as List == ['a', 'b', 'c', 'd', 'e', 'f']
        '''
    }

    void testShouldNotThrowAmbiguousSelectionError() {
        assertScript '''
            import org.codehaus.groovy.ast.ClassNode
            import org.codehaus.groovy.ast.MethodNode

            ClassNode  annotation = null
            MethodNode methodNode = null
            List annos = methodNode?.getAnnotations(annotation)
            return (annos != null && annos.size() > 0)
        '''
    }
}
