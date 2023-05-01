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

final class Groovy3904 {

    private static void compileAndVerifyCyclicInheritenceCompilationError(String sourceCode) {
        def err = shouldFail(sourceCode)
        assert err =~ /Cycle detected/
    }

    @Test
    void testCyclicInheritence1() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            class G3904R1A extends G3904R1A {}
        '''
    }

    @Test
    void testCyclicInheritence2() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            class G3904R2A extends G3904R2A {
                static main(args) {
                    print 'hello'
                }
            }
        '''
    }

    @Test
    void testCyclicInheritence3() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            class G3904R3A extends G3904R3B {}
            class G3904R3B extends G3904R3A {}
        '''
    }

    @Test
    void testCyclicInheritence4() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            class G3904R4B extends G3904R4A {}
            class G3904R4A extends G3904R4B {}
        '''
    }

    @Test
    void testCyclicInheritence5() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            class G3904R5A extends G3904R5B {}
            class G3904R5B extends G3904R5C {}
            class G3904R5C extends G3904R5B {}
        '''
    }

    @Test
    void testCyclicInheritence6() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            class G3904R6A extends G3904R6B {}
            class G3904R6B extends G3904R6C {}
            class G3904R6C extends G3904R6D {}
            class G3904R6D extends G3904R6B {}
        '''
    }

    @Test // GROOVY-11036
    void testCyclicInheritence7() {
        compileAndVerifyCyclicInheritenceCompilationError '''
            interface I11036 {}
            interface J11036 extends J11036, I11036 {}
        '''
    }
}
