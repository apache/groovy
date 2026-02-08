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
package org.apache.groovy.contracts.tests.interfaces

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class SimpleInterfaceInheritanceTests extends BaseTestClass {

    def source_stackable = '''
@Contracted
package tests

import groovy.contracts.*

abstract class Stackable {

  @Requires({ item != null })
  abstract void push(def item)
}

'''

    def source_stack = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ list != null && anotherName != null })
class Stack extends Stackable {

  protected def list
  def anotherName = ""
  def protected name = ""

  public Stack()  {
    this.list = []
  }

  public Stack(def list)  {
    this.list = list
  }

  @Requires({ item > 2 })
  @Ensures({ list[-1] == item })
  void push(def item)  {
    list.add item
  }
}
'''

    def source_implicit_interface = '''
@Contracted
package tests

import groovy.contracts.*

interface A {
   @Ensures({ old != null && result != null })
   def some_method()
}

class B implements A {

   def some_method() { return null }

}

class C extends B {
   def some_method() { return null }
}
'''

    def source_implicit_interface2 = '''
@Contracted
package tests

import groovy.contracts.*

class C extends B {
   def some_method() { return null }
}
'''

    @Test
    void creation() {
        add_class_to_classpath(source_stackable)
        create_instance_of(source_stack)
    }

    @Test
    void push_precondition() {
        add_class_to_classpath(source_stackable)

        def stack = create_instance_of(source_stack)

        shouldFail AssertionError, {
            stack.push null
        }

        stack.push 1
        stack.push 2
    }

    @Test
    void postcondition_in_indirect_parent_interface() {
        add_class_to_classpath(source_implicit_interface)
        def c = create_instance_of(source_implicit_interface2)

        shouldFail AssertionError, {
            c.some_method()
        }
    }

}
