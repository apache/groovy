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

import groovy.transform.stc.STCnAryExpressionTest

/**
 * Unit tests for static type checking : n-ary operators
 */
class NaryExpressionTestStaticCompileTest extends STCnAryExpressionTest implements StaticCompilationTestSupport {

    // GROOVY-10395
    void testUfoOperatorShouldRedirectForPrimitives() {
        assertScript '''
            int test(boolean a, boolean b) {
                a <=> b
            }
            assert test(false,false) == 0
            assert test(false,true) < 0
            assert test(true,false) > 0
            assert test(true,true) == 0
        '''
        String out = astTrees.values()[0][1]
        assert out.contains('INVOKESTATIC java/lang/Boolean.compare')
    }

    // GROOVY-10909
    void testMultipleNotExpressionOptimization() {
        assertScript '''
            def item = "", list = []
            def x = !!(item in list)
            return 0
        '''
        String out = astTrees.values()[0][1]
        out = out.substring(out.indexOf('run()Ljava/lang/Object;'))
        assert !out.contains('IXOR')
        assert !out.contains('INVOKESTATIC java/lang/Boolean.valueOf')
    }
}
