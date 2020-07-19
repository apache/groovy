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
import org.junit.Test

class ResultAndOldVariablePostconditionTests extends BaseTestClass {

    def source = '''
@Contracted
package tests

import groovy.contracts.*

class EnsureVariables {

  private String string1

  public EnsureVariables(final String other)  {
    string1 = other
  }

  @Ensures({ result, old -> result == part1 + "," + part2 && old.string1 == string1 })
  def String concatenateColon(final String part1, final String part2)  {
    return part1 + "," + part2
  }

  @Ensures({ old, result -> result == part1 + "," + part2 && old.string1 == string1 })
  def String concatenateColon2(final String part1, final String part2)  {
    return part1 + "," + part2
  }
}

'''

    @Test
    void result_than_old_variable() {
        def var = create_instance_of(source, ['some string'])

        var.concatenateColon("part1", "part2")
    }

    @Test
    void old_than_result_variable() {
        def var = create_instance_of(source, ['some string'])

        var.concatenateColon2("part1", "part2")
    }
}
