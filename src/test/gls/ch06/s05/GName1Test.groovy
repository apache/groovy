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
package gls.ch06.s05

import gls.ch06.s05.testClasses.Tt1cgi
import gls.ch06.s05.testClasses.Tt1cgo
import gls.ch06.s05.testClasses.Tt1gi
import gls.ch06.s05.testClasses.Tt1go
import gls.ch06.s05.testClasses.Tt1
import gls.ch06.s05.testClasses.Tt1c

class GName1Test extends GroovyTestCase {
  void testObjectSupportNameHandling() {
    Tt1  obj = new Tt1()  // Test POJO
    def newX = "new x"
    def newX1 = "new x1"
    def newX2 = "new x2"
    
    assert obj.x == "property"
    assert obj.@x == "field"
    assert obj.x() == "method"
    
    obj.x = newX
    obj.@x = newX1
    
    assert obj.x == newX
    assert obj.@x == newX1
    
    obj.setX newX2
    
    assert obj.x == newX2
    assert obj.@x == newX1
  }
  
  void testObjectSupportNameHandling1() {
    Tt1go  obj = new Tt1go()  // Test class subclassing GroovyObjectSupport
    def newX = "new x"
    def newX1 = "new x1"
    def newX2 = "new x2"
    
    assert obj.x == "property"
    assert obj.@x == "field"
    assert obj.x() == "method"
    
    obj.x = newX
    obj.@x = newX1
    
    assert obj.x == newX
    assert obj.@x == newX1
    
    obj.setX newX2
    
    assert obj.x == newX2
    assert obj.@x == newX1
  }
  
  void testObjectSupportNameHandling2() {
    Tt1  obj = new Tt1gi()  // Test POJO implementing GroovyObject
    def newX = "new x"
    def newX1 = "new x1"
    def newX2 = "new x2"
    
    assert obj.x == "dynamic property"
    assert obj.@x == "field"
    assert obj.x() == "dynamic method"
    
    obj.x = newX
    obj.@x = newX1
    
    assert obj.x == "dynamic property"
    assert obj.@x == newX1
    
    obj.setX newX2
    
    assert obj.x == "dynamic property"
    assert obj.@x == newX1
  }
  
  void testObjectSupportNameHandlingWitnClosureValues() {
    Tt1c obj = new Tt1c()  // Test POJO
    def newX = {"new x"}
    def newX1 = {"new x1"}
    def newX2 = {"new x2"}
    
    assert (obj.x)() == "property"
    assert obj.@x() == "field"
    assert obj.x() == "method"
    
      
    obj.x = newX
    obj.@x = newX1
    
    assert (obj.x)() == newX()
    assert obj.@x() == newX1()
    
    obj.setX newX2
    
    assert (obj.x)() == newX2()
    assert obj.@x() == newX1()
  }
  
  void testObjectSupportNameHandlingWitnClosureValues1() {
    Tt1cgo obj = new Tt1cgo()  // class subclassing GroovyObjectSupport
    def newX = {"new x"}
    def newX1 = {"new x1"}
    def newX2 = {"new x2"}
    
    assert (obj.x)() == "property"
    assert obj.@x() == "field"
    assert obj.x() == "method"
    
      
    obj.x = newX
    obj.@x = newX1
    
    assert (obj.x)() == newX()
    assert (obj.@x)() == newX1()
    
    obj.setX newX2
    
    assert (obj.x)() == newX2()
    assert (obj.@x)() == newX1()
  }
  
  void testObjectSupportNameHandlingWitnClosureValues2() {
    Tt1c obj = new Tt1cgi()  // Test POJO implementing GroovyObject
    def newX = {"new x"}
    def newX1 = {"new x1"}
    def newX2 = {"new x2"}
    
    assert (obj.x)() == "property"
    assert (obj.@x)() == "field"  // can't write obj.@x() - syntax error
    assert obj.x() == "method"
    
      
    obj.x = newX
    obj.@x = newX1
    
    assert (obj.x)() == newX()
    assert (obj.@x)() == newX1()
    
    obj.setX newX2
    
    assert (obj.x)() == newX2()
    assert (obj.@x)() == newX1()
  }
}