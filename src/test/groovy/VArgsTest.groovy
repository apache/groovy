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

class VArgsTest extends GroovyTestCase {

  def primitiveMethod(){0}
  def primitiveMethod(int i) {1}  
  def primitiveMethod(int i, int j) {2}
  def primitiveMethod(int[] is) {10+is.length}
  
  void testPrimitiveMethod() {
    assert primitiveMethod()==0
    assert primitiveMethod(1)==1
    assert primitiveMethod(1,1)==2
    assert primitiveMethod(1,1,1)==13 
    assert primitiveMethod([1,2,2,2] as int[])==14
  }  

  def doubleMethod(double[] id) {20+id.length}
    
  void testDoubleMethod() {
    // with BigDecimal
    assert doubleMethod()==20
    assert doubleMethod(1.0G)==21
    assert doubleMethod(1.0G,1.0G)==22
    assert doubleMethod(1.0G,1.0G,1.0G)==23 
    assert doubleMethod([1,2,2,2] as BigDecimal[])==24
    
    // with double
    assert doubleMethod()==20
    assert doubleMethod(1.0d)==21
    assert doubleMethod(1.0d,1.0d)==22
    assert doubleMethod(1.0d,1.0d,1.0d)==23 
    assert doubleMethod([1,2,2,2] as double[])==24
  }
  
  // test vargs with one fixed argument for primitives
  def doubleMethod2(double a, double[] id) {31+id.length}

  void testDoubleMethodWithOneFixedPrimitive() {
    // with BigDecimal
    assert doubleMethod2(1.0G)==31
    assert doubleMethod2(1.0G,1.0G)==32
    assert doubleMethod2(1.0G,1.0G,1.0G)==33 
    assert doubleMethod2(1.0G, [1,2,2,2] as BigDecimal[])==35
        
    // with double
    assert doubleMethod2(1.0d)==31
    assert doubleMethod2(1.0d,1.0d)==32
    assert doubleMethod2(1.0d,1.0d,1.0d)==33 
    assert doubleMethod2(1.0d,[1,2,2,2] as double[])==35
  }
  
  
  def objectMethod(){0}
  def objectMethod(Object i) {1}  
  def objectMethod(Object i, Object j) {2}
  def objectMethod(Object[] is) {10+is.length}
  
  void testObjectMethod() {
    assert objectMethod()==0
    assert objectMethod(1)==1
    assert objectMethod(1,1)==2
    assert objectMethod(1,1,1)==13
    assert objectMethod([1,2,2,2] as Object[])==14
  }
  
  def gstringMethod(GString[] gstrings){gstrings.length}
  
  void testGStringVargsMethod() {
    def content = 1
    def gstring ="$content"
    assert gstringMethod() == 0
    assert gstringMethod(gstring) == 1
    assert gstringMethod(gstring,gstring,gstring) == 3
    assert gstringMethod([gstring] as GString[]) == 1
  }
  
  def stringMethod(String[] strings) {strings.length}
  
  void testStringMethod() {
    def content = 1
    def gstring ="$content"
    assert stringMethod() == 0
    assert stringMethod(gstring) == 1
    assert stringMethod(gstring,gstring,gstring) == 3
    assert stringMethod([gstring] as GString[]) == 1
    assert stringMethod() == 0
    assert stringMethod("a") == 1
    assert stringMethod("a","a","a") == 3
    assert stringMethod(["a"] as String[]) == 1
  }
  
  //tests related to GROOVY-1807
  
  def overloadedMethod1(String s){1}
  def overloadedMethod1(Object[] args){2}
  
  void testOverloadedMethod1() {
    assert overloadedMethod1() == 2
  }
  
  def overloadedMethod2(x,y){1}
  def overloadedMethod2(x,Object... y){2}
  
  void testOverloadedMethod2() {
    assert overloadedMethod2(null) == 2
    assert overloadedMethod2("foo") == 2
  }
  
  def normalVargsMethod(Object[] a){a.length}
  
  void testArrayCoercion() {
    assert normalVargsMethod([1,2,3] as int[]) == 3
  }
  
  
  // GROOVY-2204
  def m2204a(Map kwargs=[:], arg1, arg2, Object[] args) {
    "arg1: $arg1, arg2: $arg2, args: $args, kwargs: $kwargs"
  }

  def m2204b(Map kwargs=[:], arg1, arg2="1", Object[] args) {
    "arg1: $arg1, arg2: $arg2, args: $args, kwargs: $kwargs"
  }

  void test2204a() {
     assert m2204a('hello', 'world') == 'arg1: hello, arg2: world, args: [], kwargs: [:]'
     assert m2204a('hello', 'world', 'from', 'list') == 'arg1: hello, arg2: world, args: [from, list], kwargs: [:]'
     assert m2204a('hello', 'world', 'from', 'list', from: 'kwargs') == 'arg1: hello, arg2: world, args: [from, list], kwargs: [from:kwargs]'
     assert m2204a('hello', 'world', from: 'kwargs') == 'arg1: hello, arg2: world, args: [], kwargs: [from:kwargs]'
     assert m2204a([:], 'hello', 'world', [] as Object[]) == 'arg1: hello, arg2: world, args: [], kwargs: [:]'
     
     assert m2204b('hello', 'world') == 'arg1: hello, arg2: 1, args: [world], kwargs: [:]'
     assert m2204b('hello', 'world', 'from', 'list') == 'arg1: hello, arg2: 1, args: [world, from, list], kwargs: [:]'
     assert m2204b('hello', 'world', 'from', 'list', from: 'kwargs') == 'arg1: hello, arg2: world, args: [from, list], kwargs: [from:kwargs]'
     assert m2204b('hello', 'world', from: 'kwargs') == 'arg1: hello, arg2: world, args: [], kwargs: [from:kwargs]'
  }

 
  // GROOVY-2351
  
  def m2351(Object... args)  {1}
  def m2351(Integer... args) {2}
 
  void test2351() {
    assert m2351(1, 2, 3, 4, 5) == 2
  }
  
  // see MetaClassHelper#calculateParameterDistance

  def fooAB(Object[] a) {1}     //-> case B
  def fooAB(a,b,Object[] c) {2} //-> case A
  void testAB() {
    assert fooAB(new Object(),new Object()) == 2
  }

  def fooAC(Object[] a) {1}     //-> case B
  def fooAC(a,b)        {2}     //-> case C
  void testAC(){
    assert fooAC(new Object(),new Object()) == 2
  }

  def fooAD(Object[] a) {1}     //-> case D
  def fooAD(a,Object[] b) {2}   //-> case A
  void testAD(){
    assert fooAD(new Object()) == 2
  }

  def fooBC(Object[] a) {1}     //-> case B
  def fooBC(a,b) {2}            //-> case C
  void testBC() {  
    assert fooBC(new Object(),new Object()) == 2
  }

  def fooBD(Object[] a)   {1}   //-> case B
  def fooBD(a,Object[] b) {2}   //-> case D
  void testBD(){
    assert fooBD(new Object(),new Object()) == 2
  }

  // GROOVY-3019
  def foo3019(Object a, int b) {1}
  def foo3019(Integer a, int b, Object[] arr) {2}
  void test3019() {
    assert foo3019(new Integer(1),1)==1
  }

  // GROOVY-3547
  void testCallObjectVarArgWithInt() {
    assert foo3547(1).getClass() == Object[]
  }

  void testCallObjectVarArgWithStrings() {
    assert foo3547("one", "two").getClass() == Object[]
  }


  void testCallSerializableVarArgWithString() {
    assert bar3547("").getClass() == Serializable[]
  }

  def foo3547(Object... args) {
    args
  }

  def bar3547(Serializable... args) {
    args
  }


}  
