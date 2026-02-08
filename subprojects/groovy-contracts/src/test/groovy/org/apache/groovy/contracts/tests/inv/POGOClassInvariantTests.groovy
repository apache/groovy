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

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * POGO class invariant tests.
 */
class POGOClassInvariantTests extends BaseTestClass {

    def dynamic_constructor_class_code = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ property != null })
class DynamicConstructor {

  def property
}
'''

    def dynamic_setter_class_code = '''
@Contracted
package tests

import groovy.contracts.*

@Invariant({ string1 != null && string2 != null && string3 != null })
class DynamicSetter {

  String string1 = ''
  def String string2 = ''
  final String string3 = ''
}

'''

    @Test
    void dynamic_constructor_class_invariant() {
        shouldFail AssertionError, {
            create_instance_of dynamic_constructor_class_code;
        }
    }

    @Test
    void dynamic_setter_methods() {
        def instance = create_instance_of(dynamic_setter_class_code)

        shouldFail AssertionError, {
            instance.string1 = null
        }

        shouldFail AssertionError, {
            instance.string2 = null
        }

        shouldFail AssertionError, {
            instance.string3 = null
        }
    }
}