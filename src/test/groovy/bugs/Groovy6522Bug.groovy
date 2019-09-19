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

class Groovy6522Bug extends GroovyTestCase {
    // this is a non-regression test that makes sure
    // that the fix for 6522 doesn't introduce breaking changes
    void testDelegateShouldOverrideField() {
        assertScript '''
class Delegate {
  def getProperty(String name) {1}
}
class Tester {
  def someProp = 2
  def foo() {
    def cl = {return someProp}
    cl.delegate = new Delegate()
    cl.resolveStrategy = Closure.DELEGATE_ONLY
    return cl()
  }
}

def t = new Tester()
assert t.someProp == 2
assert t.foo() == 1'''
    }
}
