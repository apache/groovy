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
import groovy.cli.picocli.CliBuilder
import groovy.cli.TypedOption
import groovy.transform.TypeChecked

// tag::mapOptionImports[]
import java.util.concurrent.TimeUnit
import static java.util.concurrent.TimeUnit.DAYS
import static java.util.concurrent.TimeUnit.HOURS
// end::mapOptionImports[]

// Core functionality we expect to remain the same for all implementations is tested in the base test case
// here we also add any functionality specific to this implementation that we value highly
class CliBuilderTest extends CliBuilderTestCase {

    String getImportCliBuilder() { 'import groovy.cli.picocli.CliBuilder\n' }

    void testAnnotationsInterfaceToStringWithUsage() {
        doTestAnnotationsInterfaceToString('usage', '''\
Usage: groovy Greeter
      [<remaining>...]   positional parameters
  -a, --audience=<audience>
                         greeting audience
  -h, --help             display usage
''')
    }

    void testAnnotationsInterfaceToStringWithName() {
        doTestAnnotationsInterfaceToString('name', '''\
Usage: groovy Greeter [-h] [-a=<audience>] [<remaining>...]
      [<remaining>...]   positional parameters
  -a, --audience=<audience>
                         greeting audience
  -h, --help             display usage
''')
    }

    @TypeChecked
    void testTypeChecked_addingSingleHyphenForLongOptSupport() {
        def cli = new CliBuilder(acceptLongOptionsWithSingleHyphen: true)
        TypedOption<String> name = cli.option(String, opt: 'n', longOpt: 'name', 'name option')
        TypedOption<Integer> age = cli.option(Integer, longOpt: 'age', 'age option')
        def argz = "--name John -age 21 and some more".split()
        def options = cli.parse(argz)
        String n = options[name]
        int a = options[age]
        assert n == 'John' && a == 21
        assert options.arguments() == ['and', 'some', 'more']
    }

    @TypeChecked
    void testTypeChecked_defaultOnlyDoubleHyphen() {
        def cli = new CliBuilder()
        TypedOption<String> name = cli.option(String, opt: 'n', longOpt: 'name', 'name option')
        TypedOption<Integer> age = cli.option(Integer, longOpt: 'age', 'age option')
        def argz = "--name John -age 21 and some more".split()
        def options = cli.parse(argz)
        assert options[name] == 'John'
        assert options[age] == null
        assert options.arguments() == ['-age', '21', 'and', 'some', 'more']
    }

    void testUsageMessageSpec() {
        // suppress ANSI escape codes to make this test pass on all environments
        System.setProperty("picocli.ansi", "false")
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos, true))

        // tag::withUsageMessageSpec[]
        def cli = new CliBuilder()
        cli.name = "myapp"
        cli.usageMessage.with {
            headerHeading("@|bold,underline Header heading:|@%n")
            header("Header 1", "Header 2")                     // before the synopsis
            synopsisHeading("%n@|bold,underline Usage:|@ ")
            descriptionHeading("%n@|bold,underline Description heading:|@%n")
            description("Description 1", "Description 2")      // after the synopsis
            optionListHeading("%n@|bold,underline Options heading:|@%n")
            footerHeading("%n@|bold,underline Footer heading:|@%n")
            footer("Footer 1", "Footer 2")
        }
        cli.a('option a description')
        cli.b('option b description')
        cli.c(args: '*', 'option c description')
        cli.usage()
        // end::withUsageMessageSpec[]

        String expected = '''\
Header heading:
Header 1
Header 2

Usage: myapp [-ab] [-c[=PARAM...]]...

Description heading:
Description 1
Description 2

Options heading:
  -a               option a description
  -b               option b description
  -c=[PARAM...]    option c description

Footer heading:
Footer 1
Footer 2
'''
        assertEquals(expected.normalize(), baos.toString().normalize())
    }

    void testMapOption() {
        // tag::mapOption[]
        def cli = new CliBuilder()
        cli.D(args: 2,   valueSeparator: '=', 'the old way')                          // <1>
        cli.X(type: Map, 'the new way')                                               // <2>
        cli.Z(type: Map, auxiliaryTypes: [TimeUnit, Integer].toArray(), 'typed map')  // <3>

        def options = cli.parse('-Da=b -Dc=d -Xx=y -Xi=j -ZDAYS=2 -ZHOURS=23'.split())// <4>
        assert options.Ds == ['a', 'b', 'c', 'd']                                     // <5>
        assert options.Xs == [ 'x':'y', 'i':'j' ]                                     // <6>
        assert options.Zs == [ (DAYS as TimeUnit):2, (HOURS as TimeUnit):23 ]         // <7>
        // end::mapOption[]
    }

    void testGroovyDocAntExample() {
        def cli = new CliBuilder(usage:'ant [options] [targets]',
                header:'Options:')
        cli.help('print this message')
        cli.logfile(type:File, argName:'file', 'use given file for log')
        cli.D(type:Map, argName:'property=value', 'use value for given property')
        cli.lib(argName:'path', valueSeparator:',', args: '3',
                'comma-separated list of 3 paths to search for jars and classes')

        // suppress ANSI escape codes to make this test pass on all environments
        System.setProperty("picocli.ansi", "false")
        StringWriter sw = new StringWriter()
        cli.writer = new PrintWriter(sw)

        cli.usage()

        String expected = '''\
Usage: ant [options] [targets]
Options:
  -D=<property=value>    use value for given property
      -help              print this message
      -lib=<path>,<path>,<path>
                         comma-separated list of 3 paths to search for jars and
                           classes
      -logfile=<file>    use given file for log
'''
        assertEquals(expected.normalize(), sw.toString().normalize())
    }

    void testGroovyDocCurlExample() {
        // suppress ANSI escape codes to make this test pass on all environments
        System.setProperty("picocli.ansi", "false")
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        System.setOut(new PrintStream(baos, true))

        def cli = new CliBuilder(name:'curl')
        cli._(longOpt:'basic', 'Use HTTP Basic Authentication')
        cli.d(longOpt:'data', args:1, argName:'data', 'HTTP POST data')
        cli.G(longOpt:'get', 'Send the -d data with a HTTP GET')
        cli.q('If used as the first parameter disables .curlrc')
        cli._(longOpt:'url', type:URL, argName:'URL', 'Set URL to work with')

        cli.usageMessage.sortOptions(false)
        cli.usage()

        String expected = '''\
Usage: curl [-Gq] [--basic] [--url=<URL>] [-d=<data>]
      --basic         Use HTTP Basic Authentication
  -d, --data=<data>   HTTP POST data
  -G, --get           Send the -d data with a HTTP GET
  -q                  If used as the first parameter disables .curlrc
      --url=<URL>     Set URL to work with
'''
        assertEquals(expected.normalize(), baos.toString().normalize())
    }
}
