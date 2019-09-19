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
 * This test uses the verbose syntax to test the building of trees
 * using GroovyMarkup
 */
class VerboseTreeTest extends GroovyTestCase {
    
    def b

    void testSmallTree() {
        b = NodeBuilder.newInstance()
        
        def root = b.root1(['a':5, 'b':7], {
            elem1('hello1')
            elem2('hello2')
            elem3(['x':7])
        })
        
        assert root != null
        
        print(root)
    }
    
    void testTree() {
        b = NodeBuilder.newInstance()
        
        def root = b.root2(['a':5, 'b':7], {
            elem1('hello1')
            elem2('hello2')
            nestedElem(['x':'abc', 'y':'def'], {
                child(['z':'def'])
                child2()  
            })
            
            nestedElem2(['z':'zzz'], {
                child(['z':'def'])
                child2("hello")  
            })
        })
        
        assert root != null
        
        print(root)

        def e1 = root.elem1.get(0)
        assert e1.value() == 'hello1'
        
        def e2 = root.elem2.get(0)
        assert e2.value() == 'hello2'

        assert root.elem1.get(0).value() == 'hello1'
        assert root.elem2.get(0).value() == 'hello2'

        assert root.nestedElem.get(0).attributes() == ['x':'abc', 'y':'def']        
        assert root.nestedElem.child.get(0).attributes() == ['z':'def']
        
        assert root.nestedElem.child2.get(0).value() == []
        assert root.nestedElem.child2.get(0).text() == ''

        assert root.nestedElem2.get(0).attributes() == ['z':'zzz']      
        assert root.nestedElem2.child.get(0).attributes() == ['z':'def']
        assert root.nestedElem2.child2.get(0).value() == 'hello'
        assert root.nestedElem2.child2.get(0).text() == 'hello'
        
        def list = root.value()
        assert list.size() == 4

        assert root.attributes().a == 5
        assert root.attributes().b == 7
        
        assert root.nestedElem.get(0).attributes().x == 'abc'
        assert root.nestedElem.get(0).attributes().y == 'def'
        assert root.nestedElem2.get(0).attributes().z == 'zzz'
        assert root.nestedElem2.child.get(0).attributes().z == 'def'
        
        /** @todo parser add .@ as an operation
                assert root.@a == 5
                assert root.@b == 7
        */        
    }
}
