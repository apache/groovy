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

class Groovy7924Bug extends GroovyTestCase {
    void testShouldBeAbleToAssignThisInsideCategoryAnnotatedClass() {
        assertScript '''
            class Base {
                def foo = 'oof'
                def bar() { 'rab' }
            }

            class Child extends Base {
                def superAccess(name) { super."$name" }
                def superInvoke(name) { super."$name"() }
                def thisAccess(name) { this."$name" }
                def thisInvoke(name) { this."$name"() }
            }

            def c = new Child()
            assert c.thisAccess('foo') == 'oof'
            assert c.thisInvoke('bar') == 'rab'
            assert c.superAccess('foo') == 'oof'
            assert c.superInvoke('bar') == 'rab'
        '''
    }
}
