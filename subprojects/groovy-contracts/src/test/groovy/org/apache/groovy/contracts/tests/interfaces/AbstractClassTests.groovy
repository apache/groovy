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

import org.apache.groovy.contracts.PreconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class AbstractClassTests extends BaseTestClass {

    def source_stackable = '''
@Contracted
package tests

import groovy.contracts.*

abstract class Stackable {

  @Requires({ item != null })
  abstract void push(def item)

  @Requires({ item1 != null && item2 != null })
  abstract void multi_push(def item1, def item2)
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

  @Ensures({ list[-1] == item })
  void push(def item)  {
    list.add item
  }

  void multi_push(def item1, def item2)  {
    push item1
    push item2
  }

//  @Requires({ list.size() > 0 })
//  @Ensures({ result != null })
//  def Object pop()  {
//    list[-1]
//  }

  @Ensures({ result -> result == list.size() })
  def int size()  {
    return list.size()
  }

  @Ensures({ result -> comp1 != null && comp2 != null && result > 0 })
  def int size(def comp1, comp2)  {
      return comp1 + comp2
  }

  @Ensures({ result -> result == 'tostring'})
  @Override
  def String toString()  {
    return 'tostring'
  }

  void modifyClassInvariant()  {
    anotherName = null
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

        shouldFail PreconditionViolation.class, {
            stack.push null
        }
    }
}
