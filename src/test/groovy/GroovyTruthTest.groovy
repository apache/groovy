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

class GroovyTruthTest extends GroovyTestCase {

    void testTruth() {
        testFalse null

        assertTrue Boolean.TRUE
        testTrue true
        testFalse Boolean.FALSE
        testFalse false

        testFalse ""
        testTrue "bla"
        testTrue "true"
        testTrue "TRUE"
        testTrue "false"
        testFalse ''
        testTrue 'bla'
        testTrue new StringBuffer('bla')
        testFalse new StringBuffer()

        testFalse Collections.EMPTY_LIST
        testFalse([])
        testTrue([1])
        testFalse([].toArray())

        testFalse Collections.EMPTY_MAP
        testFalse([:])
        testTrue([bla: 'some value'])

        testTrue 1234
        testFalse 0
        testTrue 0.3f
        testTrue new Double(3.0f)
        testFalse 0.0f
        testTrue new Character((char) 1)
        testFalse new Character((char) 0)
    }

    void testPrimitiveArrayTruth() {
        testTrue([1] as byte[])
        testTrue([1] as short[])
        testTrue([1] as int[])
        testTrue([1] as long[])
        testTrue([1] as float[])
        testTrue([1] as double[])
        testTrue([true] as boolean[])
        testTrue([1] as char[])

        testFalse([] as byte[])
        testFalse([] as short[])
        testFalse([] as int[])
        testFalse([] as long[])
        testFalse([] as float[])
        testFalse([] as double[])
        testFalse([] as boolean[])
        testFalse([] as char[])
    }

    void testIteratorTruth() {
        testFalse([].iterator())
        testTrue([1].iterator())
    }

    void testEnumerationTruth() {
        def v = new Vector()
        testFalse(v.elements())
        v.add(new Object())
        testTrue(v.elements())
    }

    protected testTrue(someObj) {
        assertTrue someObj ? true : false
    }

    protected testFalse(someObj) {
        assertFalse someObj ? true : false
    }
    
    void testLogicORandAND() {
        def x = null
        assert ((false || x) == false)
        assert ((x || false) == false)
        
        assert ((x && true) == false)
        assert ((true && x) == false)
    }

}