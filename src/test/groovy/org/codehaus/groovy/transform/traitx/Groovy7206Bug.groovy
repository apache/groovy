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


package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyTestCase

class Groovy7206Bug extends GroovyTestCase {
    void testShouldNotThrowNPEDuringCompilation() {
        assertScript '''
            trait SetUnknown {
                def setup() {
                    this.unknownProperty = 1
                }
            }
            class A implements SetUnknown {
                def unknownProperty
            }
            def a = new A()
            a.setup()
            assert a.unknownProperty == 1
        '''
    }

    void testShouldNotThrowNPEDuringCompilation2() {
        assertScript '''
            trait SetUnknown {
                int pInt = 2
                def setup() {
                    this.unknownProperty = 1
                }
            }
            class A implements SetUnknown {
                def unknownProperty
            }
            def a = new A()
            a.setup()
            assert a.unknownProperty == 1
            assert a.pInt == 2
        '''
    }

    void testShouldNotThrowNPEDuringCompilation3() {
        assertScript '''
            trait SetUnknown {
                int pInt = 2
                def setup() {
                    this.unknownProperty = unknownProperty + pInt
                }
            }
            class A implements SetUnknown {
                def unknownProperty = 1
            }
            def a = new A()
            a.setup()
            assert a.unknownProperty == 3
            assert a.pInt == 2
        '''
    }

    void testShouldRecognizeStaticProperty() {
        assertScript '''
trait Validateable {
    private static Map constraintsMapInternal
    static Map getConstraintsMap() {
        if(this.constraintsMapInternal == null) {
            this.constraintsMapInternal = [:]
        }
    }
}
class ValidateTest implements Validateable {}
def v = new ValidateTest()
assert v.constraintsMap == [:]
        '''
    }
}
