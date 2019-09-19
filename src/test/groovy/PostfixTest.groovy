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

class PostfixTest extends GroovyTestCase {

    void testIntegerPostfix() {
        def x = 1
        
        def y = x++
        
        assert y == 1
        assert x == 2
        
        assert x++ == 2
        assert x == 3
    }
    
    void testDoublePostfix() {
        def x = 1.2
        def y = x++

        assert y == 1.2
        assert x++ == 2.2
        assert x == 3.2
    }

     void testStringPostfix() {
         def x = "bbc"
         x++

         assert x == "bbd"

         def y = "bbc"++
         assert y == "bbc"
    }
    
    
    void testArrayPostfix() {
        int[] i = [1]
        
        def y = i[0]++
        
        assert y == 1
        assert i[0]++ == 2
        assert i[0] == 3
    }
    
    void testConstantPostFix() {
        assert 1 == 1++
    }

    def valueReturned() { 0 }

    void testFunctionPostfix() {
        def z = (valueReturned())++

        assert z == 0
    }

    void testPrefixAndPostfix() {
        def u = 0
        
        assert -1 == -- u --
        assert 0 == ++ u ++
        assert 0 == u
        assert 0 == (u++)++
        assert 1 == u
    }
}
