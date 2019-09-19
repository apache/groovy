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

import groovy.test.GroovyTestCase

class Groovy7937Bug extends GroovyTestCase {
    void testMethodsWithinAnInstanceShouldBeChosenAheadOfDGMMethodsOnAnInterface() {
        assertScript """
            class MyList extends LinkedList<String> {
                MyList(Collection c) { super(c) }
                String pop() {
                    'foo'
                }
            }
            MyList stack1 = new MyList(1..3)
            assert stack1.pop() == 'foo'
            def stack2 = 1..3 as LinkedList
            assert stack2.pop() == 1
        """
    }
}