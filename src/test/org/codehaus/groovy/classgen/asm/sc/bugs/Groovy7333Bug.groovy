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

final class Groovy7333Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    // GROOVY-7333
    void testIncorrectInstanceOfInference1() {
        assertScript '''
            int len(byte[] arr) { arr.length }
            def foo(arg) {
               if (arg instanceof byte[]) {
                  len(arg)
               }
            }
            assert foo(new byte[3]) == 3
        '''
    }

    // GROOVY-9769
    void testIncorrectInstanceOfInference2() {
        assertScript '''
            interface A {
                def foo()
            }
            interface B extends A {
                def bar()
            }
            @groovy.transform.CompileStatic
            void test(A a) {
                if (a instanceof B) {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def type = node.rightExpression.getNodeMetaData(INFERRED_RETURN_TYPE)
                        assert type.text == 'B' // not '<UnionType:A+B>'
                    })
                    def x = a
                    a.foo()
                    a.bar()
                }
            }

            def result = ''

            test([
                foo: { -> result += 'foo' },
                bar: { -> result += 'bar' }
            ] as B)

            assert result == 'foobar'
        '''
    }
}
