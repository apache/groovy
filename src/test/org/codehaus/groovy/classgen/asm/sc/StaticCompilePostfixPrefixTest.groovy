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

class StaticCompilePostfixPrefixTest extends AbstractBytecodeTestCase {
    void testPostfixOnInt() {
        def bytecode = compile([method:'m'], '''@groovy.transform.CompileStatic
            void m() {
                int i = 0
                i++
                assert i==1
                assert i++==1
            }
        ''')
        clazz.newInstance().m()

        bytecode = compile([method:'m'], '''@groovy.transform.CompileStatic
            void m() {
                int i = 0
                i--
                assert i == -1
                assert i-- == -1
            }
        ''')

        clazz.newInstance().m()
    }

    void testPostfixOnDate() {
        def bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date tomorrow = d+1
                d++
                assert d == tomorrow
                assert d++ == tomorrow
                assert d == tomorrow +1
            }
        ''')

        clazz.newInstance().m()
        bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date yesterday = d - 1
                d--
                assert d == yesterday
                assert d-- == yesterday
                assert d == yesterday - 1
            }
        ''')

        clazz.newInstance().m()
    }

    void testPrefixOnInt() {
        def bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
         void m() {
            int i = 0
            ++i
            assert i==1
            assert ++i == 2
         }
        ''')
        clazz.newInstance().m()

        bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                int i = 0
                --i
                assert i==-1
                assert --i == -2
            }
        ''')
        clazz.newInstance().m()
    }

    void testPrefixOnDate() {
        def bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date tomorrow = d + 1
                Date aftertomorrow = d + 2
                ++d
                assert d == tomorrow
                assert ++d == aftertomorrow
            }
        ''')
        bytecode = compile([method:'m'],'''@groovy.transform.CompileStatic
            void m() {
                Date d = new Date()
                Date yesterday = d - 1
                Date beforeyesterday = d - 2
                --d
                assert d == yesterday
                assert --d == beforeyesterday
            }
        ''')
    }

}
