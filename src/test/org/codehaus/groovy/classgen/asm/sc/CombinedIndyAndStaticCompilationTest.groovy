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

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

/**
 * Tests for combined static compilation and indy code
 */
class CombinedIndyAndStaticCompilationTest extends AbstractBytecodeTestCase {
    void testArrayAccess() {
        if (!config.indyEnabled) return;
        ["byte", "short", "int", "long", "float", "double", "boolean", "char"].each { type->
            //array get
            compile ("""
                @groovy.transform.CompileStatic
                def foo() {
                    ${type}[] array = new ${type}[10]
                    $type x = array[0]
                }
            """).hasSequence(["INVOKEDYNAMIC"])
            //array set
            compile ("""
                @groovy.transform.CompileStatic
                def foo() {
                    ${type}[] array = new ${type}[10]
                    array[0] = 1
                }
            """).hasSequence(["INVOKEDYNAMIC"])
        }
    }

    void testNegativeAccess() {
        ["byte", "short", "int", "long", "float", "double", "char"].each { type ->
            assertScript """
                @groovy.transform.CompileStatic
                def foo() {
                    ${type}[] array = [0,1,2]
                    assert array[0] == 0
                    assert array[1] == 1
                    assert array[2] == 2
                    assert array[-1] == 2
                    assert array[-2] == 1
                    array[0] = 9
                    assert array[0] == 9
                    array[-1] = 8
                    assert array[2] == 8
                }
                foo()
            """
        }
        assertScript """
                @groovy.transform.CompileStatic
                def foo() {
                    boolean[] array = [false, false, true]
                    assert array[0] == false
                    assert array[1] == false
                    assert array[2] == true
                    assert array[-1] == true
                    assert array[-2] == false
                    array[0] = true
                    assert array[0] == true
                    array[-1] = false
                    assert array[2] == false
                }
                foo()
            """
    }
}
