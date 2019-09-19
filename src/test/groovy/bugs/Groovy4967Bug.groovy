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


class Groovy4967Bug extends GroovyTestCase {
    void testListToLinkedHashSetConversion() {

        /*
         * a), b), c) all worked but d) failed with "GroovyCastException: Cannot cast object '[x]'
         * with class 'java.util.ArrayList' to class 'java.util.LinkedHashSet'"
         */

        // a)
        List x1 = []
        HashSet<String> lhs1 = x1

        // b)
        List x2 = ['x']
        HashSet<String> lhs2 = x2

        // c)
        List x3 = []
        LinkedHashSet<String> lhs3 = x3

        // d)
        List x4 = ['x']
        LinkedHashSet<String> lhs4 = x4
        assert lhs4.size() == 1
        assert lhs4.contains('x')
    }
}

