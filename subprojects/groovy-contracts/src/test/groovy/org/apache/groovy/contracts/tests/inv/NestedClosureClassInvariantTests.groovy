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
package org.apache.groovy.contracts.tests.inv

import org.apache.groovy.contracts.ClassInvariantViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

// GROOVY-12059: class invariants may contain nested closures that use the implicit 'it'
// and reference the enclosing instance's fields.
class NestedClosureClassInvariantTests extends BaseTestClass {

    def source = '''
import groovy.contracts.*

@Invariant({ values.every { it > lowerBound } })
class A {

  List<Integer> values
  int lowerBound

  A(List<Integer> values, int lowerBound) {
    this.values = values
    this.lowerBound = lowerBound
  }
}
'''

    @Test
    void nested_closure_in_invariant_satisfied() {
        create_instance_of(source, [[1, 2, 3], 0])
    }

    @Test
    void nested_closure_in_invariant_violated() {
        shouldFail ClassInvariantViolation, {
            create_instance_of(source, [[1, 0, 3], 0])
        }
    }
}
