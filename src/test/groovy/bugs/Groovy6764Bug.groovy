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
package bugs

import groovy.test.GroovyTestCase

class Groovy6764Bug extends GroovyTestCase {
    void testStaticImportViaInheritedInterface() {
        assertScript '''
            import static Constants.ANSWER as CA
            import static Helper.ANSWER as HA

            class Foo {
                static method(arg1 = CA, arg2 = HA) {
                    assert CA == 42
                    assert HA == 42
                    "$arg1 $arg2"
                }
                static Closure closure = { arg1 = CA, arg2 = HA ->
                    assert CA == 42
                    assert HA == 42
                    "$arg1 $arg2"
                }
            }

            interface ConstantsBase { int ANSWER = 42 }
            interface Constants extends ConstantsBase {}
            class Helper implements Constants { }

            assert Foo.closure() == '42 42'
            assert Foo.method() == '42 42'
        '''
    }
}
