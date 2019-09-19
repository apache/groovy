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

class ForLoopBug extends GroovyTestCase {

    void testBug() {
        assertScript """
            def list = []
            def a = 1
            def b = 5
            for (c in a..b) {
                list << c
            }
            assert list == [1, 2, 3, 4, 5]
        """
    }

    void testSeansBug() {
        assertScript """
            for (i in 1..10) {
                println i
            }
        """
    }

    void testNormalMethod() {
        def list = []
        def a = 1
        def b = 5
        for (c in a..b) {
            list << c
        }
        assert list == [1, 2, 3, 4, 5]
    }

    void testBytecodeGenBug() {
        def a = 1
        def b = 5

        def lastIndex
        for (i in a..b) {
            lastIndex = i
        }
        a = lastIndex
        assert a == 5
    }


    void testVisibility() {
        assertScript """
            def array = [ true, true, true ];
            for( boolean i in array ) {
                1.times {
                    assert i == true;
                }
            }
        """
    }

}
