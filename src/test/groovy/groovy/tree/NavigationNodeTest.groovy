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
package groovy.tree

import groovy.test.GroovyTestCase

/**
 * Simple test of tree walking for Nodes
 */
class NavigationNodeTest extends GroovyTestCase {

    void testDepthFirst() {
        def tree = createTreeFromNodeBuilder()
        def names = tree.depthFirst().collect { it.name() }
        assert names == ['a', 'b1', 'b2', 'c1', 'c2', 'b3', 'b4', 'c3', 'c4', 'b5']
    }

    void testBreadthFirst() {
        def tree = createTreeFromNodeBuilder()
        def names = tree.breadthFirst().collect { it.name() }
        assert names == ['a', 'b1', 'b2', 'b3', 'b4', 'b5', 'c1', 'c2', 'c3', 'c4']
    }

    private static createTreeFromNodeBuilder() {
        def b = NodeBuilder.newInstance()
        def root = b.a(a: 5, b: 7) {
            b1()
            b2 {
                c1()
                c2()
            }
            b3()
            b4 {
                c3()
                c4()
            }
            b5()
        }
        assert root != null
        return root
    }
}
