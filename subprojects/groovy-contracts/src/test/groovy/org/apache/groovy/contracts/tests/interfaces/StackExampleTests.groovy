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

import static org.junit.Assert.assertTrue

class StackExampleTests extends BaseTestClass {

    def source_stackable = '''
@Contracted
package tests

import groovy.contracts.*

interface Stackable {

  @Requires({ item != null })
  void push(def item)

  @Ensures({ result != null && old != null })
  def isEmpty()
}
'''

    def source_stack = '''
@Contracted
package tests

import groovy.contracts.*

class Stack implements Stackable  {

  protected def list

  public Stack()  {
    this.list = []
  }

  public Stack(def list)  {
    this.list = list
  }

  @Ensures({ list.last() == item })
  void push(def item)  {
    list.add item
  }

  def isEmpty()  {
    return list.size() == 0
  }
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

        stack.push 1
        shouldFail AssertionError, {
            stack.push null
        }
    }

    @Test
    void old_variable_in_postcondition() {
        add_class_to_classpath(source_stackable)
        def stack = create_instance_of(source_stack)

        assertTrue(stack.isEmpty())
    }

}
