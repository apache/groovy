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

class Groovy1706_Bug extends GroovyTestCase {
   void testStaticMethodIsCalledFromSubclass() {
      // disclaimer: static methods shouldn't be
      // called on instances
      Groovy1706A a = new Groovy1706A()
      Groovy1706B b = new Groovy1706B()
      assert "A" == a.doit()
      assert "B" == b.doit()
   }

   void testStaticMethodIsCalledInCorrectInstance() {
      // disclaimer: static methods shouldn't be
      // called on instances
      Groovy1706A i = new Groovy1706B()
      assert "B" == i.doit()
      // in Java the answer would be "A"
   }
}

class Groovy1706A { static doit() { "A" } }
class Groovy1706B extends Groovy1706A { static doit() { "B" } }
