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

import org.apache.groovy.contracts.PostconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

// GROOVY-12059: postconditions may contain nested closures that declare their own parameters,
// use the implicit 'it', and reference the enclosing method's parameters / result.
class NestedClosurePostconditionTests extends BaseTestClass {

    def source = '''
import groovy.contracts.*

class A {

  @Ensures({ result -> result.every { n -> n > 0 } })
  List<Integer> incExplicit(List<Integer> ns) { ns.collect { it + 1 } }

  @Ensures({ result -> result.every { it > 0 } })
  List<Integer> incImplicit(List<Integer> ns) { ns.collect { it + 1 } }

  @Ensures({ result -> result.indices.every { result[it] > ns[it] } })
  List<Integer> incIndexed(List<Integer> ns) { ns.collect { it + 1 } }
}
'''

    @Test
    void nested_closure_with_explicit_parameter() {
        def a = create_instance_of(source)

        assert a.incExplicit([1, 2, 3]) == [2, 3, 4]

        shouldFail PostconditionViolation, {
            a.incExplicit([-1, -2, -3])
        }
    }

    @Test
    void nested_closure_with_implicit_it() {
        def a = create_instance_of(source)

        assert a.incImplicit([1, 2, 3]) == [2, 3, 4]

        shouldFail PostconditionViolation, {
            a.incImplicit([-1, -2, -3])
        }
    }

    @Test
    void nested_closure_referencing_method_parameter_and_result() {
        def a = create_instance_of(source)

        assert a.incIndexed([1, 2, 3]) == [2, 3, 4]
    }
}
