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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy9120 {

    @Test
    void testLocalVariableReferencesFromAIC() {
        assertScript '''
            import java.util.concurrent.Callable

            interface Face9120 {
                Runnable runnable()
                Callable<Long> callable()
            }

            static Face9120 make() {
                final long number = 42
                return new Face9120() {
                    @Override
                    Runnable runnable() {
                        return { ->
                            assert "number is ${number}" == 'number is 42'
                        }
                    }
                    @Override
                    Callable<Long> callable() {
                        return { -> number }
                    }
                }
            }

            def face = make()
            face.runnable().run()
            assert "number is ${face.callable().call()}" == 'number is 42'
        '''
    }
}
