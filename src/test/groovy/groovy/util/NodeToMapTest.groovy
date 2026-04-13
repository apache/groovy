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
package groovy.util

import org.junit.jupiter.api.Test

class NodeToMapTest {

    @Test
    void testFlatElements() {
        def root = new Node(null, 'server', [
            new Node(null, 'host', 'localhost'),
            new Node(null, 'port', '8080')
        ])
        assert root.toMap() == [host: 'localhost', port: '8080']
    }

    @Test
    void testAttributesOnly() {
        def root = new Node(null, 'server', [host: 'localhost', port: '8080'])
        assert root.toMap() == [host: 'localhost', port: '8080']
    }

    @Test
    void testAttributesAndChildElements() {
        def root = new Node(null, 'server', [env: 'prod'])
        new Node(root, 'host', 'localhost')
        new Node(root, 'port', '8080')
        assert root.toMap() == [env: 'prod', host: 'localhost', port: '8080']
    }

    @Test
    void testChildElementWinsOnCollision() {
        def root = new Node(null, 'server', [host: 'from-attr'])
        new Node(root, 'host', 'from-element')
        def map = root.toMap()
        assert map.host == 'from-element'
    }

    @Test
    void testNestedElements() {
        def root = new Node(null, 'server')
        def db = new Node(root, 'database')
        new Node(db, 'host', 'dbhost')
        new Node(db, 'port', '5432')
        assert root.toMap() == [database: [host: 'dbhost', port: '5432']]
    }

    @Test
    void testRepeatedElements() {
        def root = new Node(null, 'server')
        new Node(root, 'alias', 'a1')
        new Node(root, 'alias', 'a2')
        new Node(root, 'alias', 'a3')
        assert root.toMap() == [alias: ['a1', 'a2', 'a3']]
    }

    @Test
    void testRepeatedNestedElements() {
        def root = new Node(null, 'config')
        def s1 = new Node(root, 'server', [name: 'web'])
        new Node(s1, 'port', '80')
        def s2 = new Node(root, 'server', [name: 'api'])
        new Node(s2, 'port', '8080')
        assert root.toMap() == [
            server: [
                [name: 'web', port: '80'],
                [name: 'api', port: '8080']
            ]
        ]
    }

    @Test
    void testAttributesAndTextContent() {
        def root = new Node(null, 'price', [currency: 'USD'], '9.99')
        assert root.toMap() == [currency: 'USD', (Node.TEXT_KEY): '9.99']
    }

    @Test
    void testLeafWithAttributesAsChildValue() {
        def root = new Node(null, 'order')
        def price = new Node(root, 'price', [currency: 'USD'], '9.99')
        def map = root.toMap()
        assert map.price == [currency: 'USD', (Node.TEXT_KEY): '9.99']
    }

    @Test
    void testEmptyNode() {
        def root = new Node(null, 'empty')
        assert root.toMap() == [:]
    }

    @Test
    void testEmptyNodeWithAttributes() {
        def root = new Node(null, 'empty', [id: '1'])
        assert root.toMap() == [id: '1']
    }

    @Test
    void testTextKeyConstant() {
        assert Node.TEXT_KEY == '_text'
    }
}
