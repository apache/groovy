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
class SuperMethod2Bug extends GroovyTestCase {
     
    void testBug() {
        def base = new SuperBase()
        def value = base.doSomething()
        assert value == "TestBase"
        
        
        base = new SuperDerived()
        value = base.doSomething()
        assert value == "TestDerivedTestBase"
    }

    void testBug2() {
        def base = new SuperBase()
        def value = base.foo(2)
        assert value == "TestBase2"
        
        
        base = new SuperDerived()
        value = base.foo(3)
        assert value == "TestDerived3TestBase3"
    }

    void testBug3() {
        def base = new SuperBase()
        def value = base.foo(2,3)
        assert value == "foo(x,y)Base2,3"
        
        
        base = new SuperDerived()
        value = base.foo(3,4)
        assert value == "foo(x,y)Derived3,4foo(x,y)Base3,4"
    }

    void testBug4() {
        def base = new SuperBase("Cheese")
        def value = base.name
        assert value == "Cheese"
        
        
        base = new SuperDerived("Cheese")
        value = base.name
        assert value == "CheeseDerived"
    }
    
    void testCallsToSuperMethodsReturningPrimitives(){
       def base = new SuperBase("super cheese")
       assert base.longMethod() == 1
       assert base.intMethod() == 1
       assert base.boolMethod() == true
       
       base = new SuperDerived("derived super cheese")
       assert base.longMethod() == 1
       assert base.intMethod() == 1
       assert base.boolMethod() == true       
    }
}

class SuperBase {
    String name

    SuperBase() {
    }
    
    SuperBase(String name) {
        this.name = name
    }
    
    def doSomething() {
        "TestBase"
    }

    def foo(param) {
        "TestBase" + param
    }
    
    def foo(x, y) {
        "foo(x,y)Base" + x + "," + y
    }
    
    boolean boolMethod(){true}
    long longMethod(){1l}
    int intMethod(){1i}
}

class SuperDerived extends SuperBase {
    
    def calls = 0
    
    SuperDerived() {
    }
    
    SuperDerived(String name) {
        super(name + "Derived")
    }
    
    def doSomething() {
        /** @todo ++calls causes bug */
        //calls++
        /*
        calls = calls + 1
        assert calls < 3
        */
        
        "TestDerived" + super.doSomething()
    }
    
    def foo(param) {
        "TestDerived" + param + super.foo(param)
    }
    
    def foo(x, y) {
        "foo(x,y)Derived" + x + "," + y + super.foo(x, y)
    }
    
    // we want to ensure that a call with super, which is directly added into 
    // bytecode without calling MetaClass does correct boxing
    boolean booMethod(){super.boolMethod()}
    int intMethod(){super.intMethod()}
    long longMethod(){super.longMethod()}
}

