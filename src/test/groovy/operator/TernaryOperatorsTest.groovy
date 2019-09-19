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
package groovy.operator

import groovy.test.GroovyTestCase

class TernaryOperatorsTest extends GroovyTestCase {

    void testSimpleUse() {
        def y = 5

        def x = (y > 1) ? "worked" : "failed"
        assert x == "worked"


        x = (y < 4) ? "failed" : "worked"
        assert x == "worked"
    }

    void testUseInParameterCalling() {
        def z = 123
        assertCalledWithFoo(z > 100 ? "foo" : "bar")
        assertCalledWithFoo(z < 100 ? "bar" : "foo")
       }

    def assertCalledWithFoo(param) {
        println "called with param ${param}"
        assert param == "foo"
    }
    
    void testWithBoolean(){
        def a = 1
        def x = a!=null ? a!=2 : a!=1
        assert x == true
        def y = a!=1 ? a!=2 : a!=1
        assert y == false
    }

    void testElvisOperator() {
        def a = 1
        def x = a?:2
        assert x==a
        x = a
          ?: 2
        assert x==a

        a = null
        x = a?:2
        assert x==2
        
        def list = ['a','b','c']
        def index = 0
        def ret = list[index++]?:"something else"
        assert index==1
        assert ret=='a'
        def ret2 = list[index]
          ?: "something else entirely"
        assert ret2 == 'b'
    }
    
    void testForType() {
        boolean b = false
        int anInt = b ? 100 : 100 / 3
        assert anInt.class == Integer
    }
    
    void testBytecodeRegisters() {
        // this code will blow up if the true and false parts
        // are not handled correctly in regards to the registers.
        def i = 1
        def c= { false? { i } : it == i }
        assert true
    }

    void testLineBreaks() {
        def bar = 0 ? "moo" : "cow"
        assert bar == 'cow'

        bar = 0 ?
            "moo" : "cow"
        assert bar == 'cow'

        bar = 0 ? "moo" :
            "cow"
        assert bar == 'cow'

        bar = 0 ?
            "moo" :
            "cow"
        assert bar == 'cow'

        bar = 0
            ? "moo"
            : "cow"
        assert bar == 'cow'

        bar = 0 ? "moo"         \
              : "cow"
        assert bar == 'cow'

        // This used to fail
        bar = 0 ? "moo"
                : "cow"
        assert bar == 'cow'
    }
}   
