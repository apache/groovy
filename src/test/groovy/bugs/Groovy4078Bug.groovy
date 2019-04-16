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

class Groovy4078Bug extends GroovyTestCase {
    void testInfiniteLoopDetectionInStepUsage() {
        (2..2).step 0, {assert it != null} //IntRange

        ('b'..'b').step 0, {assert it != null} //ObjectRange

        5.step( 5, 1 ) { assert it != null } // DGM.step(), int

        5.0.step (5.0, 1 ) { assert it != null } // DGM.step(), BigDecimal

        def from = BigInteger.valueOf(5)
        def to = BigInteger.valueOf(5)
        from.step (to, 1 ) { assert it != null }  // DGM.step(), BigInteger

        try{
            (1..2).step 0, {assert it != null} //IntRange
            fail('Should have failed as step size 0 causes infinite loop')
        } catch(ex) {
            assert ex.message.contains('Infinite loop detected due to step size of 0')
        }

        try{
            ('a'..'b').step 0, {assert it != null} // ObjectRange
            fail('Should have failed as step size 0 causes infinite loop')
        } catch(ex) {
            assert ex.message.contains('Infinite loop detected due to step size of 0')
        }
    }
}
