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

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

class NotContractedTests extends BaseTestClass {

    def source = '''
package tests

import groovy.contracts.*

@Invariant({ property != null })
class A {

  def property
}
'''

    def source2 = '''
import groovy.contracts.*

@Invariant({ property != null })
class A {

  def property
}
'''

    def source3 = '''
package test
import groovy.contracts.*

@Invariant({ property != null })
class A {

  def property
}
'''
    def source4 = '''
package test
import groovy.contracts.*

@Invariant({ property != null })
class B {

  def property
}
'''


    @Test
    void AssertionsEnabled_on_package_level() {
        add_class_to_classpath source

        shouldFail AssertionError, {
            create_instance_of(source)
        }
    }

    @Test
    void AssertionsEnabled_on_class_level() {
        add_class_to_classpath source2

        shouldFail AssertionError, {
            create_instance_of(source2)
        }
    }
}
