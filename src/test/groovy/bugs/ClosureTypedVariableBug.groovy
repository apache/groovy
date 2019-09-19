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
class ClosureTypedVariableBug extends GroovyTestCase {
    
    void testBug2() {
        def count = makeClosure(0)
        assert count == 1
        
        count = makeClosure2(0)
        assert count == 1
    }


    def makeClosure(Number count) {
        def closure = { count = it }
        closure(1)
        return count
    }

    def makeClosure2(Number c) {
        def count = c
        def closure = { count = it }
        closure(1)
        return count
    }

    void testBug() {
        Integer count = 0
        def closure = { count = it }
        closure(1)
        assert count == 1
    }
    
    void testBug3() {
        def closure = getElementClosure("p")
        def answer = closure("b")
        def value = answer("c")
        println "returned : ${value}"
    }
    
    Closure getElementClosure(tag) {
        return { body ->
            if (true) {
                return {"${body}"}
            }
            else {
                body = null
            }
        }
    }
    
    void testDoubleSlotReference() {
        // there was a bug that the local variable index
        // was wrong set for a closure shared variable. 
        // One slot should have be used and one was used sometimes
        // Thus resulting in sometimes assuming a wrong index 
        double d1 = 1.0d
        double d2 = 10.0d
        1.times { d1=d1*d2 }
        assert d1==10d
        
        long l1 = 1l
        long l2 = 10l
        1.times { l1=l1*l2 }
        assert l1==10l
    }
}