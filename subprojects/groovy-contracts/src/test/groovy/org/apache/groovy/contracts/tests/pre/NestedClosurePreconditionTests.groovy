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

import org.apache.groovy.contracts.PreconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

// GROOVY-12059: preconditions may contain nested closures that declare their own parameters,
// use the implicit 'it', and reference the enclosing method's parameters.
class NestedClosurePreconditionTests extends BaseTestClass {

    def source = '''
import groovy.contracts.*

class A {

  @Requires({ ns.every { n -> n > 0 } })
  int totalExplicit(int[] ns) { ns.sum() }

  @Requires({ ns.every { it > 0 } })
  int totalImplicit(int[] ns) { ns.sum() }

  @Requires({ ns.indices.every { ns[it] > 0 } })
  int totalIndexed(int[] ns) { ns.sum() }
}
'''

    @Test
    void nested_closure_with_explicit_parameter() {
        def a = create_instance_of(source)

        assert a.totalExplicit([1, 2, 3] as int[]) == 6

        shouldFail PreconditionViolation, {
            a.totalExplicit([1, -2, 3] as int[])
        }
    }

    @Test
    void nested_closure_with_implicit_it() {
        def a = create_instance_of(source)

        assert a.totalImplicit([1, 2, 3] as int[]) == 6

        shouldFail PreconditionViolation, {
            a.totalImplicit([1, -2, 3] as int[])
        }
    }

    @Test
    void nested_closure_referencing_method_parameter() {
        def a = create_instance_of(source)

        assert a.totalIndexed([1, 2, 3] as int[]) == 6

        shouldFail PreconditionViolation, {
            a.totalIndexed([1, -2, 3] as int[])
        }
    }
}
