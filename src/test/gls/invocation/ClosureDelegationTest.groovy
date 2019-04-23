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
package gls.invocation

import gls.CompilableTestSupport

class ClosureDelegationTest extends CompilableTestSupport {

    void testMissingMethodMissingMethod() {
      assertScript """
class A {
  def methodMissing(String name, args) {
     "A" 
  }
}

def methodMissing(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }

    void testInvokeMethodMissingMethod() {
      assertScript """
class A {
  def invokeMethod(String name, args) {
     "A" 
  }
}

def methodMissing(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }
    
    void testMissingMethodInvokeMethod() {
      assertScript """
class A {
  def methodMissing(String name, args) {
     "A" 
  }
}

def invokeMethod(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }
    
    void testInvokeMethodInvokeMethod() {
      assertScript """
class A {
  def invokeMethod(String name, args) {
     "A" 
  }
}

def invokeMethod(String name, args) {
  visited=true
  throw new MissingMethodException(name,this.class,args)
}

visited=false
def closure = { foo() }
closure.delegate = new A()
assert closure() == "A"
assert visited==true
        """
    }

    void testStaticMethod() {
        assertScript '''
            class Foo {
                static visited 
                static closureInStaticContext = {
                    foo()
                }
                static foo(){ visited = true }
            }
            Foo.closureInStaticContext()
            assert Foo.visited == true
        '''
    }

    void testStaticContextClosureDelegation() {
        assertScript '''
            class Foo {
                static aClosure = { foo() }
                static { aClosure.delegate = new Bar() }
            }
            class Bar {
                static visited
                def foo() { visited = true }
            }
            Foo.aClosure()
            assert Bar.visited == true
        '''
    }
}