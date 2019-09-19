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
package groovy

import groovy.test.GroovyTestCase

class DynamicMemberTest extends GroovyTestCase {
  def aTestMethod(o){o}
  def aProperty
  
  public void testGStringMethodCall(){
    def name = "aTestMethod"
    assert this."$name"(1) == 1
    assert this."${name}"(2) == 2
    assert "$name"(3) == 3
    assert "${name}"(4) == 4
    name = "TestMethod"
    assert this.("a"+"TestMethod")(5) == 5
    assert this.("a"+name)(6) == 6
  }
  
  public void testGStringPropertyAccess(){
    def name = "aProperty"
    this.aProperty = "foo"
    assert this."$name" == "foo"
    assert this."${name}" == "foo"
    assert "$name" == "aProperty"
    assert "${name}" == "aProperty"
  }
  
  public void testStringMethodCallAndAttributeAccess() {
    this.aProperty = "String"
    assert this."aProperty" == "String"
    assert this."aTestMethod"("String") == "String"
    assert "aTestMethod"("String") == "String"
  }
  
  public void testDynamicAttributeAccess() {
    this.aProperty = "tada"
    def name = "aProperty"
    assert this.@"$name" == "tada"
    assert this.@"${name}" == "tada"
  }
  
  public void testDynamicMethodClosure() {
    def cl = this.&"aTestMethod"
    assert cl("String") == "String"
    def name ="aTestMethod"
    cl = this.&"$name"
    assert cl("String") == "String"
  }

  void testNewLine() {
      def x = 1
      def y = x
               .&toString
      assert y() == '1'
  }
}