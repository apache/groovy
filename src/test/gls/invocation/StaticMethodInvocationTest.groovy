/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gls.invocation

import gls.CompilableTestSupport

public class StaticMethodInvocationTest extends CompilableTestSupport {

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
}
