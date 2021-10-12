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
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assert.fail

final class Groovy10302 {

    @Test
    void testSuperStackOverflow() {
        try {
            assertScript '''
                void test() throws ClassNotFoundException {
                    def list = []

                    def loader = new GroovyClassLoader() {
                        @Override
                        protected Class<?> findClass(String name) throws ClassNotFoundException {
                            list.add(name); super.findClass(name)
                        }
                    }

                    Class result = loader.findClass('foo.bar.Baz')
                }

                test()
            '''
            fail('ClassNotFoundException expected')
        } catch (ClassNotFoundException ignore) {
        } catch (StackOverflowError todo) {
            assert isAtLeastJdk('16')
        }
    }
}
