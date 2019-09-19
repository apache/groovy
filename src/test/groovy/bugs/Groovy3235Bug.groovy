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

import groovy.test.GroovyTestCase

class Groovy3235Bug extends GroovyTestCase {

void testBug3235 () {
      def d = """This is one line.

      That was an empty line.
      Another empty line follows.

      All these lines should be written.
"""
      def f = File.createTempFile("groovy.bugs.Groovy3235Bug", ".txt")
      f.deleteOnExit()
      f.withWriter { w ->
          d.eachLine { w.println it }
      }

      def t = f.text

      assert d == t.normalize()
      
      assert d.denormalize() == t
   }
}
