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
 * Some GPath tests using trees
 */
class NodeGPathTest extends GroovyTestCase {

    void testFind() {
        def tree = createTree()
        assert tree.person.find { it['@name'] == 'James' }.location[0]['@name'] == 'London'
    }

    void testFindAll() {
        def tree = createTree()
        def peopleWithNameBob = tree.person.findAll { it['@name'] != 'Bob' }
        assert peopleWithNameBob.size() == 1
    }

    void testCollect() {
        def tree = createTree()
        def namesOfAllPeople = tree.person.collect { it['@name'] }
        assert namesOfAllPeople == ['James', 'Bob']
    }

    protected def createTree() {
        def builder = NodeBuilder.newInstance()
        def root = builder.people() {
            person(name:'James') {
                location(name:'London')
                projects {
                    project(name:'geronimo')
                }
            }
            person(name:'Bob') {
                location(name:'Atlanta')
                projects {
                    project(name:'drools')
                }
            }
        }
        assert root != null
        return root
    }
}
