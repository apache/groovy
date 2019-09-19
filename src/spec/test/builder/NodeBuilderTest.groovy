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
package builder

import groovy.test.GroovyTestCase

class NodeBuilderTest extends GroovyTestCase {

    void testNodeBuilder() {
        // tag::node_builder_example[]
        def nodeBuilder = new NodeBuilder()
        def userlist = nodeBuilder.userlist {
            user(id: '1', firstname: 'John', lastname: 'Smith') {
                address(type: 'home', street: '1 Main St.', city: 'Springfield', state: 'MA', zip: '12345')
                address(type: 'work', street: '2 South St.', city: 'Boston', state: 'MA', zip: '98765')
            }
            user(id: '2', firstname: 'Alice', lastname: 'Doe')
        }
        // end::node_builder_example[]
         
        // tag::node_builder_gpath_assert[]
        assert userlist.user.@firstname.join(', ') == 'John, Alice'
        assert userlist.user.find { it.@lastname == 'Smith' }.address.size() == 2
        // end::node_builder_gpath_assert[]
    }

    // GROOVY-7044
    void testNodeCloning() {
        def node = new NodeBuilder().a {
            b()
        }
        def clonedNode = node.clone()
        node.appendNode('c')

        // clonedNode should not contain node c
        assert !clonedNode.children().any { it.name() == 'c' }
    }

    // GROOVY-7044
    void testNodeCloningWithAttributes() {
        def node = new NodeBuilder().a(foo: 'bar') {
            b()
        }
        def clonedNode = node.clone()
        node.appendNode('c')

        // clonedNode should not contain node c
        assert !clonedNode.children().any { it.name() == 'c' }
    }
}
