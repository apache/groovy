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
package groovy.bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.junit.Test

final class Groovy9126 extends AbstractBytecodeTestCase {

    @Test
    void testUnreachableBytecode() {
        assert compile(method:'nonVoidMethod', '''@groovy.transform.CompileStatic
            int nonVoidMethod() {
                1 * 1
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ICONST_1',
                'ICONST_1',
                'IMUL',
                'IRETURN'
            ])
    }

    @Test
    void testUnreachableBytecode2() {
        assert compile(method:'nonVoidMethod', '''@groovy.transform.CompileStatic
            def nonVoidMethod() {
                println 123
                567
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ALOAD 0',
                'BIPUSH 123',
                'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
                'INVOKEVIRTUAL script.println (Ljava/lang/Object;)V',
                'L1',
                'LINENUMBER 4',
                'SIPUSH 567',
                'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
                'ARETURN'
            ])
    }

    @Test
    void testUnreachableBytecode3() {
        assert compile(method:'nonVoidMethod', '''@groovy.transform.CompileStatic
            def nonVoidMethod() {
                println 123
                throw new RuntimeException()
            }
        ''').hasStrictSequence([
                'L0',
                'LINENUMBER 3',
                'ALOAD 0',
                'BIPUSH 123',
                'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
                'INVOKEVIRTUAL script.println (Ljava/lang/Object;)V',
                'L1',
                'LINENUMBER 4',
                'NEW java/lang/RuntimeException',
                'DUP',
                'INVOKESPECIAL java/lang/RuntimeException.<init> ()V',
                'CHECKCAST java/lang/Throwable',
                'ATHROW'
            ])
    }
}
