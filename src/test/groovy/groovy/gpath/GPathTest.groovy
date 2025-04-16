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
package groovy.gpath

import groovy.test.GroovyTestCase

/**
 * Some GPath tests using maps and lists
 */
class GPathTest extends GroovyTestCase {

    void testSimpleGPathExpressions() {
        def tree = createTree()
        assert tree.people.find { it.name == 'James' }.location == 'London'
        assert tree.people.name == ['James', 'Bob']
        def expected = ['James works on 2 project(s)', 'Bob works on 2 project(s)']
        assert tree.people.findAll { it.projects.size() > 1 }.collect { it.name + ' works on ' + it.projects.size() + " project(s)" } == expected
    }

    protected def createTree() {
        return [
                'people': [
                        ['name': 'James', 'location': 'London', 'projects': ['geronimo', 'groovy']],
                        ['name': 'Bob', 'location': 'Atlanta', 'projects': ['drools', 'groovy']]
                ]
        ]
    }
}
