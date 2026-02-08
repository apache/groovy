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
package org.apache.groovy.contracts.tests.other

import org.apache.groovy.contracts.PreconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class AbstractClassTests extends BaseTestClass {

    def source1 = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property != null })
abstract class A {

  def property

  def A(def someValue)  {
    property = someValue
  }

  @Requires({ param != null })
  def some_operation(def param)  {
    // noop
  }
}
'''

    def source2 = '''
@Contracted
package tests

import groovy.contracts.*

class B extends A  {

  def B(def someValue)  {
    super(someValue)
  }

  def some_operation(def param)  {
    // noop
  }
}
'''

    @Test
    void inherited_class_invariant() {
        add_class_to_classpath source1

        shouldFail AssertionError, {
            create_instance_of(source2, [null])
        }
    }

    @Test
    void inherited_precondition() {
        add_class_to_classpath source1

        def bInstance = create_instance_of(source2, ["test"])

        shouldFail PreconditionViolation, {
            bInstance.some_operation null
        }
    }

}
