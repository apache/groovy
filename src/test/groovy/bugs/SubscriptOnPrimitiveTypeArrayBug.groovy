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

class SubscriptOnPrimitiveTypeArrayBug extends TestSupport {
    int[] ia;  // type is not necessary
    int i1;

    void testBug() {
        def array = getIntArray() // this function returns [I, true primitive array
        
        def value = array[2]
        
        assert value == 3
        
        array[2] = 8

        value = array[2]
        assert value == 8
        
        // lets test a range
        def range = array[1..2]
        
        assert range == [2, 8]
    }

    void testGroovyIntArray() {
        int[] ia = [1, 2]
        int[] ia1 = ia; // type is not necessary
        def i1 = ia1[0]
        int i2 = i1
        assert i2 == 1
    }

    void testIntArrayObjectRangeSelection() {
        int[] ia = [1000, 1100, 1200, 1300, 1400]
        def range = new ObjectRange(new Integer(1), new Integer(3))
        def selected = ia[range]
        assert selected == [1100, 1200, 1300]
    }

}
