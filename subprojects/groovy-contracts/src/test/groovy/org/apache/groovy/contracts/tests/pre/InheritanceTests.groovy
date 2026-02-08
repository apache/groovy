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
package org.apache.groovy.contracts.tests.pre

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test
import org.apache.groovy.contracts.PostconditionViolation

class InheritanceTests extends BaseTestClass {

    def source_parent = '''
@Contracted
package tests

import groovy.contracts.*

class Parent {

  @Requires({ true })
  def Parent()  {

  }

  @Requires({ param1 > 0 && param2 > 0  })
  def some_operation1(Integer param1, Integer param2)  {

  }

  boolean boolean_operation() {
    println "blue"
    return true
  }

  @Requires({ param1 > 0 && param2 > 1 })
  def some_operation3(Integer param1, Integer param2)  {

  }

  @Requires({ param1 > 0 && param2 > 0 })
  def some_operation4(Integer param1, Integer param2)  {
    println param1
    println param2
  }
}
'''

    def source_descendant = '''
@Contracted
package tests

import groovy.contracts.*

class Descendant extends Parent {

  @Requires({ true })
  @Ensures({ true })
  def Descendant()  {
    super()
  }

  @Override
  @Requires({ param1 > 1 && param2 > 1  })
  def some_operation1(Integer param1, Integer param2)  {

  }

  @Requires({ boolean_operation() })
  def some_operation2()  {

  }

  @Requires({ x > 0 && y > 0 })
  def some_operation3(Integer x, Integer y)  {

  }

}
'''

    @Test
    void redefined_precondition() throws Exception {
        // create_instance_of(source_parent)
        add_class_to_classpath(source_parent)
        def child = create_instance_of(source_descendant)

        child.some_operation1(1, 1)
    }

    @Test
    void redefined_precondition2() throws Exception {
        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation1(0, 0)
        }
    }

    @Test
    void method_call_of_super_class_in_precondition() throws Exception {
        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        println child.boolean_operation()

        child.some_operation2()
    }

    @Test
    void refined_precondition_with_other_param_names() throws Exception {
        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation3(0, 0)
        }
    }

    @Test
    void refined_precondition_with_other_param_names1() throws Exception {
        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        shouldFail AssertionError, {
            child.some_operation3(0, 1)
        }
    }

    @Test
    void refined_precondition_with_other_param_names2() throws Exception {
        create_instance_of(source_parent)
        def child = create_instance_of(source_descendant)

        child.some_operation3(1, 2)
    }


    @Test
    void abstract_class_and_concrete_class_in_single_script() {

        add_class_to_classpath """
            import groovy.contracts.*

            abstract class Base {
                @Ensures({ result })
                abstract List<String> sources()
            }
        """

        def concreteClass = add_class_to_classpath """
            import groovy.contracts.*

            class ConcreteClass extends Base {
                public List<String> sources
                public List<String> sources () { sources }
            }
        """
        def c = concreteClass.newInstance()

        shouldFail PostconditionViolation, {
            c.sources()
        }
    }


}
