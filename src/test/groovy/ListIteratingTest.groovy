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
 * Tests iterating using Groovy
 */
class ListIteratingTest extends GroovyTestCase {

    void testIteratingWithSequences() {
        def s = [1, 2, 3, 4 ]
        assertSequence(s)
    }
    
    void testIteratingWithSequencesAsParameter() {
        assertSequence([1, 2, 3, 4 ])
    }
    
    void testIteratingWithList() {
        def s = new ArrayList()
        s.add(1)
        s.add(2)
        s.add(3)
        s.add(4)
        assertSequence(s)
    }

    protected void assertSequence(s) {
        def result = 0
        for ( i in s ) {
            result = result + i
        }

        assert(result == 10)
        assert(s.size() == 4)
        
        assert(s[2] == 3)
        result = 0
        for ( i in s[1,2] ) {    // or s[1..2]
            result += i
        }
        assert(result == 2+3)
    }
}
