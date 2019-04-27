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

class ReturnTest extends CompilableTestSupport {

  void testObjectInitializer() {
      shouldNotCompile """
         class A {
            {return}
         }      
      """
  }
  
  void testStaticInitializer() {
      assertScript """
         class A {
             static foo=2
             static { return; foo=1 }
         }
         assert A.foo==2
      """      
  }

  void testReturnAdditionInFinally() {
      //GROOVY-7065
      assertScript """
        class CountDown { int counter = 10 }

        CountDown finalCountDown() {
            def countDown = new CountDown()
            try {
                countDown.counter = --countDown.counter
            } catch (ignored) {
                countDown.counter = Integer.MIN_VALUE
            } finally {
                return countDown
            }
        }

        assert finalCountDown().counter == 9
      """
  }
}