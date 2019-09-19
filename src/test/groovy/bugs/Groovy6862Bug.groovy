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

class Groovy6862Bug extends GroovyTestCase {
    void testDollarAllowedInTraitMethodNames() {
        assertScript '''
            trait Test {
                def foo_ok() { 'trait non-$ instance OK' }
                def foo$oops() { 'trait $ instance OK' }
                static sfoo_ok() { 'trait non-$ static OK' }
                static sfoo$oops() { 'trait $ static OK' }
            }
            class Foo implements Test {
                def foo$ok() { 'class $ instance OK' }
                static sfoo$ok() { 'class $ static OK' }
            }
            def foo = new Foo()
            assert foo.foo$ok() == 'class $ instance OK'
            assert foo.foo_ok() == 'trait non-$ instance OK'
            assert foo.foo$oops() == 'trait $ instance OK'
            assert Foo.sfoo$ok() == 'class $ static OK'
            assert Foo.sfoo_ok() == 'trait non-$ static OK'
            assert Foo.sfoo$oops() == 'trait $ static OK'
        '''
    }
}
