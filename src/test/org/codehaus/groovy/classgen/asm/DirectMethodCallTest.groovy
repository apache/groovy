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
package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.Parameter

class DirectMethodCallTest extends AbstractBytecodeTestCase {
    
  void testVirtual() {
      def target = ClassHelper.Integer_TYPE.getMethod("toString", new Parameter[0])
      def makeDirectCall = {su ->
          su. getAST().classes[0].
              getMethod("run", new Parameter[0]).code.
              statements.last().expression.methodTarget = target;
      }
      
      assert compile (method:"run", conversionAction: makeDirectCall, """
          def a = 1; 
          a.toString()
      """).hasSequence([
              "INVOKEVIRTUAL java/lang/Integer.toString ()Ljava/lang/String;"
      ])
  }
  
  //GROOVY-6384
  void testClassForNameAutomaticDirectCall() {
      ['"Foo"',1,null,"println(x)"].each { arg ->
          assert compile (method:"run", """
              Class.forName($arg)
          """).hasSequence([
              "INVOKESTATIC java/lang/Class.forName (Ljava/lang/String;)Ljava/lang/Class;"
          ])
      }
  }
}
