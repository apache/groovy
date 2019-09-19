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

class Groovy5122Bug extends GroovyTestCase {
    void testInterfaceFieldShouldBeInitialized() {
        assertScript '''
            import java.lang.reflect.Field

            interface A {
                public static X x = new X() {
                    public void foo() {}
                }
            }

            interface X {
                void foo()
            }

            class B implements A {
                public B() {
                    for (Field f in getClass().getFields()) {
                        println f
                        println f.get(this)
                    }
                }
            }

            new B()
        '''
    }
}
