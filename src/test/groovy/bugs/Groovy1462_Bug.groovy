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

/**
 *  Verifies that the Groovy parser can accept quoted methods.
 */

class Groovy1462_Bug extends GroovyTestCase {
 
    void testShort() {
        def smn = new StringMethodName()
        assert smn.foo0() == 'foo0'
        assert smn.'foo0'() == 'foo0'
        assert smn.foo1() == 'foo1'
        assert smn.'foo1'() == 'foo1'
        assert smn.foo2() == 2
        assert smn.foo3() == 3
        assert smn.foo4(3) == 12
        assert smn.foo5 == 'foo5'
        assert !smn.fooFalse()
        assert smn.fooDef() == null
    }
    
}

class StringMethodName {
    def foo0() {'foo0'} // control
    def 'foo1'() {'foo1'}
    public Integer 'foo2'() {2}
    public int 'foo3'() {3}
    Integer 'foo4'(x) { x * 4}
    public def 'getFoo5'() {'foo5'}
    private boolean 'fooFalse'() {false}
    public def 'fooDef'() {}
}
