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

import org.apache.groovy.contracts.ClassInvariantViolation
import org.apache.groovy.contracts.PostconditionViolation
import org.apache.groovy.contracts.PreconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class ContractLabelTests extends BaseTestClass {

    @Test
    void class_invariant() {
        def source1 = '''
package tests

import groovy.contracts.*

@Invariant({
    not_null_property: property != null
})
class A {

  def property
}
'''

        shouldFail ClassInvariantViolation, {
            create_instance_of(source1)
        }
    }

    @Test
    void precondition() {
        def source1 = '''
package tests

import groovy.contracts.*

class A {
@Requires({
    not_null_param: param != null
})
def some_operation(def param) {
 param
}
}
'''

        shouldFail PreconditionViolation, {
            def a = create_instance_of(source1)
            a.some_operation null
        }
    }

    @Test
    void postcondition() {
        def source1 = '''
package tests

import groovy.contracts.*

class A {
@Ensures({
    result_is_a_result: result == param
    result_is_no_result: result != param
})
def some_operation(def param) {
 null
}
}
'''

        shouldFail PostconditionViolation, {
            def a = create_instance_of(source1)
            a.some_operation null
        }

        shouldFail PostconditionViolation, {
            def a = create_instance_of(source1)
            a.some_operation "test"
        }
    }

    @Test
    void malformatted_postcondition() {
        def source1 = '''
package tests

import groovy.contracts.*

class A {
@Ensures({
    result_is_a_result:
        result == param
    result_is_no_result:
        result != param
})
def some_operation(def param) {
 null
}
}
'''

        shouldFail PostconditionViolation, {
            def a = create_instance_of(source1)
            a.some_operation null
        }

        shouldFail PostconditionViolation, {
            def a = create_instance_of(source1)
            a.some_operation "test"
        }
    }
}
