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
package builder

import cli.CliBuilderTestCase
import groovy.cli.TypedOption
import groovy.cli.commons.CliBuilder
import groovy.transform.TypeChecked

class CliBuilderTest extends CliBuilderTestCase {

    String getImportCliBuilder() { 'import groovy.cli.commons.CliBuilder\n' }

    // don't expect toString values to be the same, so test per CliBuilder implementation
    void testAnnotationsInterfaceToString() {
        doTestAnnotationsInterfaceToString('usage', '''\
usage: groovy Greeter
 -a,--audience <arg>   greeting audience
 -h,--help             display usage
''')
    }

    @TypeChecked
    void testTypeChecked_showingSingleHyphenForLongOptSupport() {
        def cli = new CliBuilder()
        TypedOption<String> name = cli.option(String, opt: 'n', longOpt: 'name', 'name option')
        TypedOption<Integer> age = cli.option(Integer, longOpt: 'age', 'age option')
        def argz = "--name John -age 21 and some more".split()
        def options = cli.parse(argz)
        String n = options[name]
        int a = options[age]
        assert n == 'John' && a == 21
        assert options.arguments() == ['and', 'some', 'more']
    }

}
