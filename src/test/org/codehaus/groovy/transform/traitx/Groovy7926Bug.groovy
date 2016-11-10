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
package org.codehaus.groovy.transform.traitx

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class Groovy7926Bug extends AbstractBytecodeTestCase {

    void testThatVoidTypesFromTraitsWithGenericsWork() {
        assertScript '''
            trait MyTrait<D> {
                void delete() {
                    // no-op
                    println "works"
                }
            }
            class MyImpl implements MyTrait<MyImpl> { }
            new MyImpl().delete()
            return true
        '''
    }

    void testThatVoidTypesAreNotUsedForVariableNamesInByteCode() {
        def byteCode = compile([method:"delete", classNamePattern:"MyImpl"],"""\
            trait MyTrait<D> {
                void delete() {
                    // no-op
                    println "works"
                }
            }
            class MyImpl implements MyTrait<MyImpl> { }
        """)

        def instructions = byteCode.instructions
        byteCode.instructions = instructions[
                instructions.indexOf("public delete()V")..-1
        ]
        instructions = byteCode.instructions
        byteCode.instructions = instructions[
                0..instructions.indexOf( instructions.find { it == '--BEGIN----END--' } )
        ]
        assert !byteCode.hasSequence([
                "CHECKCAST void"
        ])
    }
}
