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

class ImplicitVariableNamesPostconditionTests extends BaseTestClass {

    def source = '''
@Contracted
package tests

import groovy.contracts.*

class EnsureVariables {

  @Ensures({ result -> result == part1 + "," + part2 })
  def String concatenateColon(final String part1, final String part2)  {
    def result = part1 + "," + part2
    return result
  }
}

'''

    void testing_result_with_result_variable_in_method_block() {
        //def var = create_instance_of(source, [])

        //var.concatenateColon("part1", "part2")
    }
}
