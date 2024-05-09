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

import static groovy.test.GroovyAssert.assertScript

final class Groovy9115 {

    @Test
    void testSetProperty1() {
        assertScript '''
            @groovy.transform.CompileStatic
            void test() {
                File file = File.createTempFile('test', null)
                try {
                    file.text = 'GROOVY-9115' // via ResourceGroovyMethods#setText
                    assert file.text == 'GROOVY-9115'
                } finally {
                    file.delete()
                }
            }

            test()
        '''
    }

    @Test
    void testSetProperty2() {
        assertScript '''
            @groovy.transform.CompileStatic
            void test() {
                if (true) {
                    File file = File.createTempFile('test', null)
                    try {
                        file.text = 'GROOVY-9115' // via ResourceGroovyMethods#setText
                        assert file.text == 'GROOVY-9115'
                    } finally {
                        file.delete()
                    }
                }
            }

            test()
        '''
    }
}
