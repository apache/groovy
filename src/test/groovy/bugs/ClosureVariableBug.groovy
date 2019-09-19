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
 */
class ClosureVariableBug extends GroovyTestCase {
    
    void testClosurePassingBug() {
        def count = 0
        def closure = { assert count == it }
        closure(0)
        
        count = 1
        closure(1)
    }
    
    void testPassingClosureAsNamedParameter() {
        def x = 123
        
        def foo = new Expando(a:{x}, b:456)
    
        assert foo.a != null
        
        def value = foo.a()
        assert value == 123
    }
    
    void testBug() {
        def value = callClosure([1, 2])
        assert value == 2
    }
    
    protected Integer callClosure(collection) {
        Integer x
        /** @todo
        Integer x = 0
        */
        collection.each { x = it }
        return x
    }

    void testLocalVariableWithPrimitiveType() {
        assertScript """
            int x
            1.times { x=2 }
            assert x==2
        """
        assertScript """
            long x
            1.times { x=2 }
            assert x==2
        """
        assertScript """
            double x
            1.times { x=2 }
            assert x==2
        """
    }
}
