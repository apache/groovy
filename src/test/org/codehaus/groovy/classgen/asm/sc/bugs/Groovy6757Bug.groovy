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

class Groovy6757Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testExplicitTypeHint() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def ift = node.getNodeMetaData(INFERRED_TYPE)
                assert ift == make(List)
                def gt = ift.genericsTypes[0]
                assert gt.type == STRING_TYPE
            })
            def list = Collections.<String>emptyList()
            if (list) {
              list.get(0).toUpperCase()
            }
        '''
    }

    void testExplicitTypeHintWithBoundedGenerics() {
        // example from GROOVY-7307
        assertScript '''
            class A {
                static <T extends Number> T id(T value) {
                    value
                }

                // Narrower generic type: doesn't compile
                static <U extends Integer> U id2(U value) {
                    A.<U>id(value)
                }
            }
            A
        '''
    }

}
