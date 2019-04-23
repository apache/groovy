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

class StaticMethodInvocationTest extends CompilableTestSupport {

    void testDifferentCalls() {
        // GROOVY-2409
        assertScript """
class Test { 
  // all errors go away if method is declared non-private 
  private static foo() {} 
  
  static callFooFromStaticMethod() { 
    Test.foo()         
    foo()              
    this.foo()         
    new Test().foo()   
  } 
  
  def callFooFromInstanceMethod() { 
    Test.foo()        
    foo()             
    this.foo()        
    new Test().foo()  
  } 
} 

Test.callFooFromStaticMethod() 
new Test().callFooFromInstanceMethod()          
        """
    }
    
    //GROOVY-6662
    void testStaticMethodNotWronglyCached() {
        assertScript '''
            class A { static bar() {1} }
            class B { static bar() {2} }
            static foo(Class c) { c.bar() }
            
            assert foo(A) == 1
            assert foo(B) == 2
        '''
    }

    //GROOVY-6883
    void testStaticMethodCallFromOpenBlock() {
        assertScript '''
            class SuperClass {
                    protected static f(String x) { x + " is super " }
            }

            class ChildClass extends SuperClass {
                    public def doit() {
                            works()+fails()
                    }

                    private static def works() { f("Groovy") }
                    private static def fails() { return {f("Groovy")}() }
            }

            assert new ChildClass().doit() == "Groovy is super Groovy is super "
        '''
    }
}
