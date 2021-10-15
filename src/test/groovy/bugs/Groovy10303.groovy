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

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

final class Groovy10303 {
    @Test
    void testCompileStackClearNPE() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        config.optimizationOptions.put('int', true)
        new GroovyShell(config).evaluate '''
            abstract class A {
                def foo(int a, int b) {
                    if (a == 1 && b == 2) // has to have && or ||, cannot be a single truth check
                        def c = add(a,b)
                    def d = 3 // this has to be here after the if or any command before end block
                }
                Integer add(a,b) {
                    return a+b
                }
            }

            null
        '''
    }
}
