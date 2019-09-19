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

class MultiplyDivideEqualsTest extends GroovyTestCase {

    void testIntegerMultiplyEquals() {
        def x = 2
        def y = 3
        x *= y
        
        assert x == 6
        
        y *= 4
        
        assert y == 12
    }

    void testCharacterMultiplyEquals() {
        Character x = 2
        Character y = 3
        x *= y
        
        assert x == 6
        
        y *= 4
        
        assert y == 12
    }
    
    void testNumberMultiplyEquals() {
        def x = 1.2
        def y = 2
        x *= y
        
        assert x == 2.4
    }
    
    void testStringMultiplyEquals() {
        def x = "bbc"
        def y = 2
        x *= y
        
        assert x == "bbcbbc"

        x = "Guillaume"
        y = 0
        x *= y
        assert x == ""
    }
    
    
    void testIntegerDivideEquals() {
        def x = 18
        def y = 6
        x /= y
        
        assert x == 3.0
        
        y /= 3
        
        assert y == 2.0
    }
    
    void testCharacterDivideEquals() {
        Character x = 18
        Character y = 6
        x /= y
        
        assert x == 3
        
        y /= 3
        
        assert y == 2
    }
    
    void testNumberDivideEquals() {
        def x = 10.4
        def y = 2
        x /= y
        
        assert x == 5.2
    }
}
