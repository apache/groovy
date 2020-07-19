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
package org.apache.groovy.contracts.tests.post

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class InheritanceTests extends BaseTestClass {

    def source_parent = '''
@Contracted
package tests

import groovy.contracts.*

class Parent {

  def Integer field1

  @Ensures({ field1 == param1 })
  void some_operation(Integer param1)  {
    field1 = param1
  }

  @Ensures({ false })
  void some_operation2(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 != field1 })
  void some_operation3(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 != field1 })
  void some_operation4(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 == field1 })
  void some_operation5(Integer param1) {
    field1 = param1
  }

  @Ensures({ false })
  def Integer some_operation6() {
    return 0
  }

  @Ensures({ result -> result >= 0 })
  def Integer some_operation7() {
    return 0
  }

  @Ensures({ result, old -> result >= 0 && old.field1 == field1 })
  def Integer some_operation8() {
    return 0
  }

  @Ensures({ false })
  def Integer some_operation9() {
    return 0
  }
}
'''

    def source_descendant = '''
@Contracted
package tests

import groovy.contracts.*

class Descendant extends Parent {

  @Override
  @Ensures({ false })
  void some_operation(Integer param1) {
    field1 = param1  
  }

  @Override
  @Ensures({ field1 == param1 })
  void some_operation2(Integer param1) {
    field1 = param1
  }

  @Ensures({ true })                                             
  void some_operation3(Integer param1) {
    field1 = param1
  }

  @Ensures({ false })
  void some_operation4(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 != field1 })
  void some_operation5(Integer param1) {
    field1 = param1
  }

  @Ensures({ result -> result >= 0 })
  def Integer some_operation6() {
    return 0
  }
  
  @Ensures({ true })
  def Integer some_operation7() {
    return 0
  }

  @Ensures({ true })
  def Integer some_operation8() {
    return 0
  }

  def Integer some_operation9() {
    return 0
  }
}
'''

    def source2 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ speed != null && speed >= 0 && speed <= 100 })
class Rocket  {
    int speed
    boolean started

    @Requires({ !started })
    def start() { started = true }

    @Requires({ started })
    @Ensures({ old -> (speed - old.speed) > 0 })
    def accelerate()  { speed = 12 }
}
'''

    def source3 = '''
@Contracted
package tests

import groovy.contracts.*

class BetterRocket extends Rocket {
    @Override
    def accelerate() {
      speed += 20
    }
}
'''

    @Test
    void simple() {
        def rocket = create_instance_of(source2)

        rocket.start()
        rocket.accelerate()

    }

    @Test
    void inherited_postcondition_with_param() {
        add_class_to_classpath(source2)
        def betterRocket = create_instance_of(source3)

        betterRocket.start()
        betterRocket.accelerate()

    }

    @Test
    void inherited_postcondition() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation 0
        }

    }

    @Test
    void inherited_postcondition_with_fail_in_parent() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation2 0
        }

    }

    @Test
    void inherited_postcondition_with_old_variable() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        child.some_operation3 0
    }

    @Test
    void inherited_postcondition_fail_with_old_variable() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation4 0
        }
    }

    @Test
    void inherited_postcondition_fail() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation5 0
        }
    }

    @Test
    void inherited_postcondition_fail_with_result_variable() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation6()
        }
    }

    @Test
    void inherited_postcondition_with_result_variable() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        child.some_operation7()
    }

    @Test
    void inherited_postcondition_with_result_and_old_variables() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        child.some_operation8()
    }

    @Test
    void inherited_postcondition_fail_with_result_variable2() {

        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation9()
        }
    }

}
