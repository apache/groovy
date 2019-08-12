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
final class Groovy9115 {

    @Test
    void testSetPropertyInIfStmt() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Derived {
                def m() {
                    if (true) {
                        File file = File.createTempFile("hello${System.nanoTime()}", ".tmp")
                        file.text = 'Groovy9115Bug'
                        assert 'Groovy9115Bug' == file.text
                    }

                    return null
                }
            }

            new Derived().m()
        '''
    }

    @Test
    void testSetProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Derived {
                def m() {
                    File file = File.createTempFile("hello${System.nanoTime()}", ".tmp")
                    file.text = 'Groovy9115Bug'
                    assert 'Groovy9115Bug' == file.text

                    return null
                }
            }

            new Derived().m()
        '''
    }
}
