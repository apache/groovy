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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class Groovy8289Bug extends AbstractBytecodeTestCase {
    void testNoArgCtorLines() {
        def bytecode = compile '''\
            @groovy.transform.CompileStatic
            class C {
                String string
                C(String s = null) { string = s }
                static void main(args) {
                    def c = new C('') // put breakpoint on this line, run as Java app, and step
                    println c
                }
            }
        ''', method:'<init>'

        assert bytecode.hasStrictSequence([
                'L0',
                'LINENUMBER 4 L0',
                'ALOAD 0',
                'INVOKESPECIAL java/lang/Object.<init> ()V'
        ])
    }

    // GROOVY-9199
    void testTryFinallyLines() {
        def bytecode = compile '''\
            def m(p) {
            }
            void test() {
                try {
                    m(true)
                } finally {
                    m(false)
                }
            }
        ''', method:'test'

        assert bytecode.hasStrictSequence([
                'L0',
                'LINENUMBER 5 L0',
                'ALOAD 0',
                'ICONST_1'
        ])
    }
}
