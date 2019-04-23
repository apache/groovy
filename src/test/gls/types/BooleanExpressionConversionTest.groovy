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
package gls.types

import gls.CompilableTestSupport

class BooleanExpressionConversionTest extends CompilableTestSupport {
    void testInt() {
        assertScript """
            boolean foo(int i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testLong() {
        assertScript """
            boolean foo(long i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testFloat() {
        assertScript """
            boolean foo(float i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testDouble() {
        assertScript """
            boolean foo(double i){
                if (i) return true
                return false
            }
            assert !foo(0)
            assert foo(1)
            assert foo(256)
        """
    }
    
    void testChar() {
        assertScript """
            boolean foo(char i){
                if (i) return true
                return false
            }
            assert !foo((char)0)
            assert foo((char)1)
            assert foo((char)256)
        """
    }
    
    void testByte() {
        assertScript """
            boolean foo(byte i){
                if (i) return true
                return false
            }
            assert !foo((byte)0)
            assert foo((byte)1)
            assert !foo((byte)256)
        """
    }
    
    void testShort() {
        assertScript """
            boolean foo(short i){
                if (i) return true
                return false
            }
            assert !foo((short)0)
            assert foo((short)1)
            assert foo((short)256)
        """
    }
    
}