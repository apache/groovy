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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.DefaultGroovyMethodsSTCTest

/**
 * Unit tests for static compilation: DGM method calls.
 */
class StaticCompileDGMTest extends DefaultGroovyMethodsSTCTest implements StaticCompilationTestSupport {

    void testThreadDotStart() {
        assertScript '''
            @ASTTest(phase = INSTRUCTION_SELECTION, value = {
                lookup('start').each {
                    def target = it.expression.target
                    assert target instanceof org.codehaus.groovy.transform.stc.ExtensionMethodNode
                    assert target.name == 'start'
                    assert target.parameters.size() == 1
                    assert target.parameters[0].type.nameWithoutPackage == 'Closure'
                    assert target.returnType.nameWithoutPackage == 'Thread'
                }
            })
            void foo() {
                start:
                Thread.start {
                    println 'ok'
                }
            }
            foo()
        '''
    }
}

