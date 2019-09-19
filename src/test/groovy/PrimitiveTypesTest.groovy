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

class PrimitiveTypesTest extends GroovyTestCase {

    int getInt() {
        return 1
    }
    
    short getShort() {
        return 1
    }
    
    boolean getBoolean() {
        return true
    }
    
    double getDouble() {
        return 1.0
    }
    
    float getFloat() {
        return 1.0
    }
    
    byte getByte() {
        return 1
    }
    
    long getLong() {
        return 1
    }

    char getChar() {
        return 'a'
    }
    
    int getNextInt(int i) {
        return i + 1
    }
    
    short getNextShort(short i) {
        return i + 1
    }
    
    void testPrimitiveTypes() {
        assert 1 == getInt()
        assert 1 == getShort()
        assert 1 == getByte()
        assert 1 == getLong()
        assert getBoolean()
        assert getDouble() > 0.99
        assert getFloat() > 0.99
        assert 'a' == getChar()
    }

    void testPrimitiveParameters() {        
        assert getNextInt(1) == 2
        assert 3 == getNextInt(2)
        
        assert getNextShort((Short) 1) == 2
        assert 3 == getNextShort((Short) 2)
    }

    void testLong2BigDecimal() {
        long l = Long.MAX_VALUE
        assert l == testMethod(l).longValueExact()
    }

    void testBigInteger2BigDecimal() {
        BigInteger big = BigInteger.valueOf(Long.MAX_VALUE)
        assert big.longValue() == testMethod(big).longValueExact()
    }

    private testMethod(BigDecimal bd) {
        return bd
    }

    static void main(args) {
        new PrimitiveTypesTest().testPrimitiveTypes()
    }
}
