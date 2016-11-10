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
package gls.statements

import gls.CompilableTestSupport

class MultipleAssignmentDeclarationTest extends CompilableTestSupport {

  void testDef() {
    assertScript """
      def (a,b) = [1,2]
      assert a==1
      assert b==2
    """
  }
  
  void testDefWithoutLiteral() {
    def list = [1, 2]
    def (c, d) = list
    assert c==1
    assert d==2
  }
  
  void testMixedTypes() {
    assertScript """
      def x = "foo"
      def (int i, String j) = [1,x]
      assert x == "foo"
      assert i == 1
      assert i instanceof Integer
      assert j == "foo"
      assert j instanceof String
    """
  }
  
  void testMixedTypesWithConversion() {
    assertScript '''
      def x = "foo"
      def (int i, String j) = [1,"$x $x"]
      assert x == "foo"
      assert i == 1
      assert i instanceof Integer
      assert j == "foo foo"
      assert j instanceof String
    '''
  }
  
  void testDeclarationOrder() {
    assertScript """
      try {
        def (i,j) = [1,i]
        assert false
      } catch (MissingPropertyException mpe) {
        assert true
      }
    """
  }
  
  void testNestedScope() {
    assertScript """
       def c = {
         def (i,j) = [1,2]
         assert i==1
         assert j==2
       }
       c()
       
       try {
         println i
         assert false
       } catch (MissingPropertyException mpe) {
         assert true
       }
       
       try {
         println j
         assert false
       } catch (MissingPropertyException mpe) {
         assert true
       }
              
       def (i,j) = [2,3]
       assert i==2
       assert j==3
       c()
       
       assert i==2
       assert j==3
    """   
  }

  void testChainedMultiAssignmentDecl() {
    def a, b
    def (c, d) = (a, b) = [1, 2]
    assert [a, b] == [1, 2]
    assert [c, d] == [1, 2]
  }
}
