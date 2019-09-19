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

class Groovy8260Bug extends GroovyTestCase {

    void testNoCastForInstanceofInsideLoop() {
        assertScript '''
            import groovy.transform.CompileStatic

            interface FooI {
                def intfMethod()
            }

            class Foo implements FooI {
                def intfMethod() { 'Foo Interface method' }
                def implMethod() { 'Foo Implementation method' }
            }

            @CompileStatic
            def method(FooI propIn, List result) {
                if (propIn instanceof Foo) {
                    result << propIn.implMethod()
                } else {
                    result << propIn?.intfMethod()
                }
                for (FooI propLoop : [null, new Foo()]) {
                    result << propLoop?.intfMethod()
                    if (propLoop instanceof Foo) {
                        result << propLoop.implMethod()
                    }
                }
            }

            def result = []
            method(null, result)
            assert result == [null, null, 'Foo Interface method', 'Foo Implementation method']
        '''
    }

}
