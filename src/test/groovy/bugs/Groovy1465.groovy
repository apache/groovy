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

import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

final class Groovy1465 {

    private static void compileAndVerifyCyclicInheritenceCompilationError(String sourceCode) {
        def err = shouldFail(sourceCode)
        assert err =~ /Cycle detected/
    }

    @Test
    void testInterfaceCyclicInheritenceTC1() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            interface G1465Tt extends G1465Tt { }
            def tt = {} as G1465Tt
        '''
    }

    @Test
    void testInterfaceCyclicInheritenceTC2() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            interface G1465Rr extends G1465Ss { }
            interface G1465Ss extends G1465Rr { }
            def ss = {} as G1465Ss
        '''
    }

    @Test
    void testInterfaceCyclicInheritenceTC3() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            interface G1465A extends G1465B { }
            interface G1465B extends G1465C { }
            interface G1465C extends G1465B { }
        '''
    }
}
