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
 * Mixes variables, closures and method calls in markup
 *
 */
class MarkupAndMethodBug extends GroovyTestCase {
    
    void testBug() {
        def tree = createTree()
        def name = tree.person[0]['@name']
        assert name == 'James'
    }
    
    protected def createTree() {
        def builder = NodeBuilder.newInstance()
        
        def root = builder.people() {
            person(name:getTestName())
        }
        
        assert root != null
        
        return root
    }
    
    protected def getTestName() {
        "James"
    }
}
