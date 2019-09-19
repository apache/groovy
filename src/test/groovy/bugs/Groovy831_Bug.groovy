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

/**
 * Test for fixing the Jira issue GROOVY-831
 */
class Groovy831_Bug extends GroovyTestCase {
    
    String[] cities = ['Seoul', 'London', 'Wasington']
    int[] intArrayData = [1, 3, 5]

    public String[] countries = [ 'Republic of Korea', 'United Kingdom', 'United State of America']
    public  int[] intArray  = [ 2, 4, 6 ]

    void testSetFieldProperty() {
        assert cities.size() == 3
        assert cities[0] == 'Seoul'
        assert cities[1] == 'London'
        assert cities[2] == 'Wasington'
        assert intArrayData.size() == 3
        assert intArrayData[0] == 1
        assert intArrayData[1] == 3
        assert intArrayData[2] == 5
    }

    void testSetFieldVariable() {
        assert countries.size() == 3
        assert countries[0] == 'Republic of Korea'
        assert countries[1] == 'United Kingdom'
        assert countries[2] == 'United State of America'
        assert intArray[0] == 2
        assert intArray[1] == 4
        assert intArray[2] == 6
    }
}

