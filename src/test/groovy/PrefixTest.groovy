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
package groovy

import groovy.test.GroovyTestCase

class PrefixTest extends GroovyTestCase {

    void testIntegerPrefix() {
        def x = 1
        
        def y = ++x
        
        assert y == 2
        assert x == 2
        
        assert ++x == 3
    }
    
    void testDoublePrefix() {
        def x = 1.2
        def y = ++x
        
        assert y == 2.2
        assert x == 2.2
        assert ++x == 3.2
        assert x == 3.2
    }

    void testStringPrefix() {
        def x = "bbc"
        ++x
        
        assert x == "bbd"
        
        --x
        --x
        
        assert x == "bbb"

        def y = ++"bbc"
        assert y == "bbd"
    }
    
    void testArrayPrefix() {
        int[] i = [1]
        
        ++i[0]
        assert i[0] == 2
        
        --i[0]
        --i[0]
        assert i[0] == 0
    }
    
    void testConstantPostFix() {
        assert 2 == ++1
    }

    def valueReturned() { 0 }

    void testFunctionPostfix() {
        def z = ++(valueReturned())

        assert z == 1
    }

    void testPrefixAndPostfix() {
        def u = 0
        
        assert -1 == -- u --
        assert 0 == ++ u ++
        assert -2 == (--(--u))
        assert -1 == u
    }
}
