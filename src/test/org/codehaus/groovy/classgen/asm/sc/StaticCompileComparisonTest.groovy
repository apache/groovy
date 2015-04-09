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

class StaticCompileComparisonTest extends AbstractBytecodeTestCase {
    void testCompareInts() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m() {
                return 1 < 2
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ICONST_1','ICONST_2', 'IF_ICMPGE']
        )
    }

    void testCompareDoubles() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m() {
                return 1d < 2d
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['DCONST_1','LDC 2.0', 'DCMPG']
        )
    }

    void testCompareDoubleWithInt() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m() {
                return 1d < 2i
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['DCONST_1','ICONST_2', 'I2D','DCMPG']
        )
    }

    void testCompareArrayLen() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m(Object[] arr) {
                return arr.length >0
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ARRAYLENGTH', 'ICONST_0','IF_ICMPLE']
        )
    }

    void testCompareArrayLenUsingIf() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m(Object[] arr) {
                if (arr.length >0) {
                    return true
                } else {
                    return false
                }
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ARRAYLENGTH', 'ICONST_0','IF_ICMPLE']
        )
    }

    void testIdentityCompare() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m(Object o) {
                return o.is(o)
            }
            assert m(new Object())
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ALOAD', 'IF_ACMPNE']
        )
        clazz.newInstance().main()
    }


}
