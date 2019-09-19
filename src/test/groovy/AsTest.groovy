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
 * Test case for using the "as" keyword to convert between strings
 * and numbers in both directions.
 */
class AsTest extends GroovyTestCase {

    def subject
    /**
     * Test that "as String" works for various types.
     */
    void testAsString() {
        assert (48256846 as String) == "48256846"
        assert (0.345358 as String) == "0.345358"
        assert (12.5432D as String) == "12.5432"
        assert (3568874G as String) == "3568874"
    }

    void testStringAsBigInteger() {
        subject = "34587203957357" as BigInteger
        assert subject.class == BigInteger
        assert subject == 34587203957357
    }

    void testStringAsLong() {
        subject = "32498687" as Long
        assert subject.class == Long
        assert subject == 32498687L
    }

    void testStringAsInt() {
        subject = "32498687" as int
        assert subject.class == Integer
        assert subject == 32498687
    }

    void testStringAsShort() {
        subject = "13279" as Short
        assert subject.class == Short
        assert subject == 13279
    }

    void testStringAsByte() {
        subject = "12" as Byte
        assert subject.class == Byte
        assert subject == 12
    }

    void testStringAsBigDecimal() {
        subject = "12.54356" as BigDecimal
        assert subject.class == BigDecimal
        assert subject == 12.54356
    }

    void testStringAsDouble() {
        subject = "1.345" as double
        assert subject.class == Double
        assert subject == 1.345
    }

    void testStringAsFloat() {
        subject = "1.345" as float
        assert subject.class == Float
        assert subject == 1.345F
    }
    
    void testFloatAsBigDecimal() {
        subject = 0.1f as BigDecimal
        assert subject.class == BigDecimal
        assert subject == 0.1
    }
    
    void testDoubleAsBigDecimal() {
        subject = 0.1d as BigDecimal
        assert subject.class == BigDecimal
        assert subject == 0.1
    }
    
    void testFloatAsDouble() {
        subject = 0.1f as Double
        assert subject.class == Double
        assert subject == 0.1
    }

    void testGStringAsByte() {
        subject = "${42}" as byte
        assert subject.class == Byte // "as" wraps types
        assert subject == 42
    }
    
    void testGStringAsShort() {
        subject = "${42}" as short
        assert subject.class == Short // "as" wraps types
        assert subject == 42
    }

    void testGStringAsInt() {
        subject = "${42}" as int
        assert subject.class == Integer // "as" wraps types
        assert subject == 42
    }

    void testGStringAsLong() {
        subject = "${42}" as long
        assert subject.class == Long // "as" wraps types
        assert subject == 42
    }

    void testGStringAsFloat() {
        subject = "${42.666}" as float
        assert subject.class == Float // "as" wraps types
        assert subject == 42.666f
    }

    void testGStringAsDouble() {
        subject = "${42.666}" as double
        assert subject.class == Double // "as" wraps types
        assert subject == 42.666d
    }

}
