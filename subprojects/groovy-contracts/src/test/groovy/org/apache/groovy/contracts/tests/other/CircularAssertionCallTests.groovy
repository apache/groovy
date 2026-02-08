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

class CircularAssertionCallTests extends BaseTestClass {

    @Test
    void detectCircularAssertionCalls() {

        def source = '''
@Contracted
package tests

import groovy.contracts.*

class A {

  @Requires({ isConditionB() })
  def isConditionA() { return false }

  @Requires({ isConditionA() })
  def isConditionB() { return true }
}
'''

        def a = create_instance_of(source)

        shouldFail PreconditionViolation, {
            a.isConditionB()
        }
    }

    @Test
    void detect_diamon_assertion_calls() {

        def source = '''
@Contracted
package tests

import groovy.contracts.*

class A {

  @Requires({ isConditionC() })
  def the_method_to_call() {}

  @Requires({ isConditionA() && isConditionB() })
  def isConditionC() {}

  @Requires({ isConditionC() })
  def isConditionA() {}

  @Requires({ isConditionC() })
  def isConditionB() {}
}
'''

        def a = create_instance_of(source)
        shouldFail PreconditionViolation, {
            a.isConditionB()
        }
    }
}
