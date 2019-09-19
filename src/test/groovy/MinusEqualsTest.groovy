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

class MinusEqualsTest extends GroovyTestCase {

    void testIntegerMinusEquals() {
        def x = 4
        def y = 2
        x -= y
        
        assert x == 2
        
        y -= 1
        
        assert y == 1
    }

    void testCharacterMinusEquals() {
        Character x = 4
        Character y = 2
        x -= y
        
        assert x == 2
        
        y -= 1
        
        assert y == 1
    }
    
    void testNumberMinusEquals() {
        def x = 4.2
        def y = 2
        x -= y
        
        assert x == 2.2
        
        y -= 0.1
        
        assert y == 1.9
    }
    
    void testStringMinusEquals() {
        def foo = "nice cheese"
        foo -= "cheese"
        
        assert foo == "nice "
    }


    void testSortedSetMinusEquals() {
        def sortedSet = new TreeSet()
        sortedSet.add('one')
        sortedSet.add('two')
        sortedSet.add('three')
        sortedSet.add('four')
        sortedSet -= 'one'
        sortedSet -= ['two', 'three']
        assertTrue 'sortedSet should have been a SortedSet',
                   sortedSet instanceof SortedSet
        assertEquals 'sortedSet had the wrong number of elements', 1, sortedSet.size()
        assertTrue 'sortedSet should have contained the word four', sortedSet.contains('four')
    }
}
