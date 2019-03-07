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
package org.codehaus.groovy.classgen.asm

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

class BinaryOperationsTest extends AbstractBytecodeTestCase {
    
    void testIntPlus() {
        if (config.indyEnabled) return;
        assert compile("""\
            int i = 1
            int j = 2
            int k = i + j
        """).hasSequence([
                "ILOAD",
                "ILOAD",
                "IADD"
        ])
    }
    
    void testIntCompareLessThan() {
        if (config.indyEnabled) return;
        assert compile("""\
            int i = 0
            if (i < 100) println "true"
        """).hasSequence([
                "ILOAD",
                "BIPUSH 100",
                "IF_ICMPGE"
        ])
    }
    
    void testCompareLessThanInClosure() {
        if (config.indyEnabled) return;
        // GROOVY-4741
        assert """
            int a = 0
            [].each {
                if (a < 0) {}
            }
            true
        """
    }
    
    void testLongLeftShift() {
        if (config.indyEnabled) return;
        assert compile("""\
            long a = 1
            long b = a << 32
        """).hasStrictSequence([
                "BIPUSH 32",
                "LSHL"
        ])
    }

    void testIntConstants() {
        if (config.indyEnabled) return;
        (0..5).each {
            assert compile("""\
                int a = $it
            """).hasStrictSequence([
                    "ICONST_$it",
            ])
        }
        [-1, 6,Byte.MIN_VALUE,Byte.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "BIPUSH",
            ])
        }
        [Byte.MIN_VALUE-1,Byte.MAX_VALUE+1,Short.MIN_VALUE,Short.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "SIPUSH",
            ])
        }
        [Short.MAX_VALUE+1,Integer.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "LDC",
            ])
        }
    }

    void testCharXor() {
        if (config.indyEnabled) return;
        assert compile("""
            int i = ('a' as char) ^ ('b' as char) 
        """).hasStrictSequence ([
            "IXOR"
        ])
    }

    void testPrimitiveOrAssign() {
        ['byte','int','short','long'].each { type ->
            assertScript """
            $type[] b = new $type[1]
            b[0] = 16
            b[0] |= 2
            assert b[0] == 18:"Failure for type $type"
            """
            }
    }

    void testPrimitiveAndAssign() {
        ['byte','int','short','long'].each { type ->
            assertScript """
            $type[] b = new $type[1]
            b[0] = 18
            b[0] &= 2
            assert b[0] == 2:"Failure for type $type"
            """
            }
    }
}
