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
import gls.CompilableTestSupport

class StyleGuideTest extends CompilableTestSupport {

    void testDataStructures() {
        // tag::data_structures[]
        def list = [1, 4, 6, 9]

        // by default, keys are Strings, no need to quote them
        // you can wrap keys with () like [(variableStateAcronym): stateName] to insert a variable or object as a key.
        def map = [CA: 'California', MI: 'Michigan']

        // ranges can be inclusive and exclusive
        def range = 10..20 // inclusive
        assert range.size() == 11
        // use brackets if you need to call a method on a range definition
        assert (10..<20).size() == 10 // exclusive

        def pattern = ~/fo*/

        // equivalent to add()
        list << 5

        // call contains()
        assert 4 in list
        assert 5 in list
        assert 15 in range

        // subscript notation
        assert list[1] == 4

        // add a new key value pair
        map << [WA: 'Washington']
        // subscript notation
        assert map['CA'] == 'California'
        // property notation
        assert map.WA == 'Washington'

        // matches() strings against patterns
        assert 'foo' ==~ pattern
        // end::data_structures[]
    }
}
