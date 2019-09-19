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

/**
 */
class CompareTypesTest extends GroovyTestCase {
    void testCompareByteToInt() { 
        Byte a = 12
        Integer b = 10
        
        assert a instanceof Byte
        assert b instanceof Integer
        
        assert a > b
    } 
    
    void testCompareByteToDouble() { 
        Byte a = 12
        Double b = 10
        
        assert a instanceof Byte
        assert b instanceof Double
        
        assert a > b
    } 
     
    void testCompareLongToDouble() { 
        Long a = 12
        Double b = 10
        
        assert a instanceof Long
        assert b instanceof Double
        
        assert a > b
    } 
     
    void testCompareLongToByte() { 
        Long a = 12
        Byte b = 10
        
        assert a instanceof Long
        assert b instanceof Byte
        
        assert a > b
    } 
     
    void testCompareIntegerToByte() { 
        Integer a = 12
        Byte b = 10
        
        assert a instanceof Integer
        assert b instanceof Byte
        
        assert a > b
    }
    
    void testCompareCharToLong() {
        def a = Integer.MAX_VALUE
        def b = ((long) a)+1
        a=(char) a
        
        assert a instanceof Character
        assert b instanceof Long
        
        assert a < b
    }
    
    void testCompareCharToInteger() {
        Character a = Integer.MAX_VALUE
        Integer b = a-1
        
        assert a instanceof Character
        assert b instanceof Integer
        
        assert a > b
    } 
} 


