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
package testingguide

import groovy.test.GroovyTestCase

class GDKMethodTests extends GroovyTestCase {

    // tag::combinations[]
    void testCombinations() {
        def combinations = [[2, 3],[4, 5, 6]].combinations()
        assert combinations == [[2, 4], [3, 4], [2, 5], [3, 5], [2, 6], [3, 6]]
    }
    // end::combinations[]

    // tag::each_combination[]
    void testEachCombination() {
        [[2, 3],[4, 5, 6]].eachCombination { println it[0] + it[1] }
    }
    // end::each_combination[]
}
