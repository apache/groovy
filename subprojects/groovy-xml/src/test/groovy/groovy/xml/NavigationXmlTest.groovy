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
package groovy.xml

import groovy.test.GroovyTestCase

/**
 * Simple test of tree walking for XML
 *
 * @see groovy.tree.NavigationNodeTest
 */
class NavigationXmlTest extends GroovyTestCase {
    void testPrePostOrder() {
        def root = createTreeFromXmlParser()
        def combos = [[false, true], ['depthFirst', 'breadthFirst']].combinations()
        def actual = combos.collect{ preorder, type ->
            root."$type"(preorder)*.name()
        }*.toString()
        def expected = [
            '[child1a, child1b, parent1, child2a, grandchild2, child2b, child2c, parent2, root]', // df post
            '[root, parent1, child1a, child1b, parent2, child2a, child2b, grandchild2, child2c]', // df pre
            '[grandchild2, child1a, child1b, child2a, child2b, child2c, parent1, parent2, root]', // bf post
            '[root, parent1, parent2, child1a, child1b, child2a, child2b, child2c, grandchild2]'  // bf pre
        ]
        (0..3).each{ assert actual[it] == expected[it] }
    }

    void testPrePostOrderWithClosure() {
        def root = createTreeFromXmlParser()
        def combos = [[false, true], ['depthFirst', 'breadthFirst']].combinations()
        def actual = combos.collect{ preorder, type ->
            def names = []
            root."$type"(preorder: preorder) { names << it.name() }
            names
        }*.toString()
        def expected = [
            '[child1a, child1b, parent1, child2a, grandchild2, child2b, child2c, parent2, root]', // df post
            '[root, parent1, child1a, child1b, parent2, child2a, child2b, grandchild2, child2c]', // df pre
            '[grandchild2, child1a, child1b, child2a, child2b, child2c, parent1, parent2, root]', // bf post
            '[root, parent1, parent2, child1a, child1b, child2a, child2b, child2c, grandchild2]'  // bf pre
        ]
        (0..3).each{ assert actual[it] == expected[it] }
    }

    void testLevelWithClosure() {
        def root = createTreeFromXmlParser()
        def result = [:].withDefault { [] }
        root.depthFirst { node, index ->
            result[index] << node.name()
        }
        assert result == [1: ['root'], 2: ['parent1', 'parent2'], 4: ['grandchild2'],
                          3: ['child1a', 'child1b', 'child2a', 'child2b', 'child2c']]
    }

    private static createTreeFromXmlParser() {
        def xml = '''
        <root>
          <parent1>
            <child1a/>
            <child1b/>
          </parent1>
          <parent2>
            <child2a/>
            <child2b><grandchild2/></child2b>
            <child2c/>
          </parent2>
        </root>
        '''
        new XmlParser().parseText(xml)
    }

}
