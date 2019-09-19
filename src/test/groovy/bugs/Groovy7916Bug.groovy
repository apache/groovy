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

import groovy.test.GroovyTestCase

class Groovy7916Bug extends GroovyTestCase {
    void testShouldBeAbleToOverrideStaticConstantProperties() {
        assertScript '''
            import groovy.transform.PackageScope
            import static groovy.test.GroovyAssert.shouldFail

            class Base {
                static class Inner1 {
                    static final String CONST = 'Hello1'
                }

                protected static class Inner2 {
                    static final String CONST = 'Hello2'
                }

                @PackageScope static class Inner3 {
                    static final String CONST = 'Hello3'
                }

                private static class Inner4 {
                    static final String CONST = 'Hello4'
                }
            }

            class Derived extends Base { }

            assert Base.Inner1.CONST == 'Hello1'
            assert Base.Inner2.CONST == 'Hello2'
            assert Base.Inner3.CONST == 'Hello3'
            assert Base.Inner4.CONST == 'Hello4'

            assert Derived.Inner1.CONST == 'Hello1'
            assert Derived.Inner2.CONST == 'Hello2'
            assert Derived.Inner3.CONST == 'Hello3'
            shouldFail(MissingPropertyException) {
                assert Derived.Inner4.CONST == 'Hello4'
            }
        '''
    }
}
