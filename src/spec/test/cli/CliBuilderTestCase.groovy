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
package cli

abstract class CliBuilderTestCase extends GroovyTestCase {

    abstract String getImportCliBuilder()

    void testAnnotationsInterface() {
        assertScript """
        import cli.GreeterI
        $importCliBuilder
        // tag::annotationInterface[]
        // import CliBuilder not shown
        def cli = new CliBuilder(usage: 'groovy Greeter')  // <1>
        def argz = '--audience Groovologist'.split()
        def options = cli.parseFromSpec(GreeterI, argz)             // <2>
        assert options.audience() == 'Groovologist'                 // <3>

        argz = '-h Some Other Args'.split()
        options = cli.parseFromSpec(GreeterI, argz)                 // <4>
        assert options.help()
        assert options.remaining() == ['Some', 'Other', 'Args']     // <5>
        // end::annotationInterface[]
        """
    }

    void testAnnotationsClass() {
        assertScript """
        import cli.GreeterC
        $importCliBuilder
        // tag::annotationClass[]
        // import CliBuilder not shown
        def cli = new CliBuilder(usage: 'groovy Greeter [option]') // <1>
        def options = new GreeterC()                               // <2>
        def argz = '--audience Groovologist foo'.split()
        cli.parseFromInstance(options, argz)                       // <3>
        assert options.audience == 'Groovologist'                  // <4>
        assert options.remaining == ['foo']                        // <5>
        // end::annotationClass[]
        """
    }

    // performs toString comparison of usage help - this isn't because we guarantee any specific output
    // (hence expected is provided as a parameter) but we want to know when it changes significantly and
    // in particular we want to ensure that no information that we expect to be there just disappears
    protected void doTestAnnotationsInterfaceToString(String key, String expected) {
        assertScript """
        import cli.GreeterI
        $importCliBuilder
        def cli = new CliBuilder($key: 'groovy Greeter')

        def options = cli.parseFromSpec(GreeterI, ['-h', 'Some', 'Other', 'Args'] as String[])
        assert options.help()
        assert options.remaining() == ['Some', 'Other', 'Args']
        StringWriter sw = new StringWriter()
        cli.writer = new PrintWriter(sw)
        cli.usage()

        assert '''$expected'''.normalize() == sw.toString().normalize()
        """
    }

    void testParseScript() {
        def argz = '--audience Groovologist foo'.split()
        new GroovyShell().run("""
            $importCliBuilder
            // tag::annotationScript[]
            // import CliBuilder not shown
            import groovy.cli.OptionField
            import groovy.cli.UnparsedField

            @OptionField String audience
            @OptionField Boolean help
            @UnparsedField List remaining
            new CliBuilder().parseFromInstance(this, args)
            assert audience == 'Groovologist'
            assert remaining == ['foo']
            // end::annotationScript[]
        """, 'TestScript.groovy', argz)
    }

    void testWithArgument() {
        assertScript """
        $importCliBuilder
        // tag::withArgument[]
        // import CliBuilder not shown
        def cli = new CliBuilder()
        cli.a(args: 0, 'a arg') // <1>
        cli.b(args: 1, 'b arg') // <2>
        cli.c(args: 1, optionalArg: true, 'c arg') // <3>
        def options = cli.parse('-a -b foo -c bar baz'.split()) // <4>

        assert options.a == true
        assert options.b == 'foo'
        assert options.c == 'bar'
        assert options.arguments() == ['baz']

        options = cli.parse('-a -c -b foo bar baz'.split()) // <5>

        assert options.a == true
        assert options.c == true
        assert options.b == 'foo'
        assert options.arguments() == ['bar', 'baz']
        // end::withArgument[]
        """
    }

    void testMultipleArgsAndOptionalValueSeparator() {
        assertScript """
        $importCliBuilder
        // tag::multipleArgs[]
        // import CliBuilder not shown
        def cli = new CliBuilder()
        cli.a(args: 2, 'a-arg')
        cli.b(args: '2', valueSeparator: ',', 'b-arg') // <1>
        cli.c(args: '+', valueSeparator: ',', 'c-arg') // <2>

        def options = cli.parse('-a 1 2 3 4'.split()) // <3>
        assert options.a == '1' // <4>
        assert options.as == ['1', '2'] // <5>
        assert options.arguments() == ['3', '4']

        options = cli.parse('-a1 -a2 3'.split()) // <6>
        assert options.as == ['1', '2']
        assert options.arguments() == ['3']

        options = cli.parse(['-b1,2']) // <7>
        assert options.bs == ['1', '2']

        options = cli.parse(['-c', '1'])
        assert options.cs == ['1']

        options = cli.parse(['-c1'])
        assert options.cs == ['1']

        options = cli.parse(['-c1,2,3'])
        assert options.cs == ['1', '2', '3']
        // end::multipleArgs[]
        """
    }

    void testWithArgumentInterface() {
        assertScript """
        import cli.WithArgsI
        $importCliBuilder
        // tag::withArgumentInterface[]
        def cli = new CliBuilder()
        def options = cli.parseFromSpec(WithArgsI, '-a -b foo -c bar baz'.split())
        assert options.a()
        assert options.b() == 'foo'
        assert options.c() == ['bar']
        assert options.remaining() == ['baz']

        options = cli.parseFromSpec(WithArgsI, '-a -c -b foo bar baz'.split())
        assert options.a()
        assert options.c() == []
        assert options.b() == 'foo'
        assert options.remaining() == ['bar', 'baz']
        // end::withArgumentInterface[]
        """
    }

    void testMultipleArgsAndOptionalValueSeparatorInterface() {
        assertScript """
        import cli.ValSepI
        $importCliBuilder
        // tag::multipleArgsInterface[]
        def cli = new CliBuilder()

        def options = cli.parseFromSpec(ValSepI, '-a 1 2 3 4'.split())
        assert options.a() == ['1', '2']
        assert options.remaining() == ['3', '4']

        options = cli.parseFromSpec(ValSepI, '-a1 -a2 3'.split())
        assert options.a() == ['1', '2']
        assert options.remaining() == ['3']

        options = cli.parseFromSpec(ValSepI, ['-b1,2'] as String[])
        assert options.b() == ['1', '2']

        options = cli.parseFromSpec(ValSepI, ['-c', '1'] as String[])
        assert options.c() == ['1']

        options = cli.parseFromSpec(ValSepI, ['-c1'] as String[])
        assert options.c() == ['1']

        options = cli.parseFromSpec(ValSepI, ['-c1,2,3'] as String[])
        assert options.c() == ['1', '2', '3']
        // end::multipleArgsInterface[]
        """
    }

    void testType() {
        assertScript """
        import java.math.RoundingMode
        $importCliBuilder
        // tag::withType[]
        def argz = '''-a John -b -d 21 -e 1980 -f 3.5 -g 3.14159
            -h cv.txt -i DOWN and some more'''.split()
        def cli = new CliBuilder()
        cli.a(type: String, 'a-arg')
        cli.b(type: boolean, 'b-arg')
        cli.c(type: Boolean, 'c-arg')
        cli.d(type: int, 'd-arg')
        cli.e(type: Long, 'e-arg')
        cli.f(type: Float, 'f-arg')
        cli.g(type: BigDecimal, 'g-arg')
        cli.h(type: File, 'h-arg')
        cli.i(type: RoundingMode, 'i-arg')
        def options = cli.parse(argz)
        assert options.a == 'John'
        assert options.b
        assert !options.c
        assert options.d == 21
        assert options.e == 1980L
        assert options.f == 3.5f
        assert options.g == 3.14159
        assert options.h == new File('cv.txt')
        assert options.i == RoundingMode.DOWN
        assert options.arguments() == ['and', 'some', 'more']
        // end::withType[]
        """
    }

    void testTypeMultiple() {
        assertScript """
        $importCliBuilder
        // tag::withTypeMultiple[]
        def argz = '''-j 3 4 5 -k1.5,2.5,3.5 and some more'''.split()
        def cli = new CliBuilder()
        cli.j(args: 3, type: int[], 'j-arg')
        cli.k(args: '+', valueSeparator: ',', type: BigDecimal[], 'k-arg')
        def options = cli.parse(argz)
        assert options.js == [3, 4, 5] // <1>
        assert options.j == [3, 4, 5]  // <1>
        assert options.k == [1.5, 2.5, 3.5]
        assert options.arguments() == ['and', 'some', 'more']
        // end::withTypeMultiple[]
        """
    }

    void testConvert() {
        assertScript """
        $importCliBuilder
        // tag::withConvert[]
        def argz = '''-a John -b Mary -d 2016-01-01 and some more'''.split()
        def cli = new CliBuilder()
        def lower = { it.toLowerCase() }
        cli.a(convert: lower, 'a-arg')
        cli.b(convert: { it.toUpperCase() }, 'b-arg')
        cli.d(convert: { Date.parse('yyyy-MM-dd', it) }, 'd-arg')
        def options = cli.parse(argz)
        assert options.a == 'john'
        assert options.b == 'MARY'
        assert options.d.format('dd-MM-yyyy') == '01-01-2016'
        assert options.arguments() == ['and', 'some', 'more']
        // end::withConvert[]
        """
    }

    void testConvertInterface() {
        assertScript """
        import cli.WithConvertI
        $importCliBuilder
        // tag::withConvertInterface[]
        Date newYears = Date.parse("yyyy-MM-dd", "2016-01-01")
        def argz = '''-a John -b Mary -d 2016-01-01 and some more'''.split()
        def cli = new CliBuilder()
        def options = cli.parseFromSpec(WithConvertI, argz)
        assert options.a() == 'john'
        assert options.b() == 'MARY'
        assert options.d() == newYears
        assert options.remaining() == ['and', 'some', 'more']
        // end::withConvertInterface[]
        """
    }

    void testDefaultValue() {
        assertScript """
        $importCliBuilder
        // tag::withDefaultValue[]
        def cli = new CliBuilder()
        cli.f longOpt: 'from', type: String, args: 1, defaultValue: 'one', 'f option'
        cli.t longOpt: 'to', type: int, defaultValue: '35', 't option'

        def options = cli.parse('-f two'.split())
        assert options.hasOption('f')
        assert options.f == 'two'
        assert !options.hasOption('t')
        assert options.t == 35

        options = cli.parse('-t 45'.split())
        assert !options.hasOption('from')
        assert options.from == 'one'
        assert options.hasOption('to')
        assert options.to == 45
        // end::withDefaultValue[]
        """
    }

    void testDefaultValueInterface() {
        assertScript """
        $importCliBuilder
        import cli.WithDefaultValueI
        // tag::withDefaultValueInterface[]
        def cli = new CliBuilder()

        def options = cli.parseFromSpec(WithDefaultValueI, '-f two'.split())
        assert options.from() == 'two'
        assert options.to() == 35

        options = cli.parseFromSpec(WithDefaultValueI, '-t 45'.split())
        assert options.from() == 'one'
        assert options.to() == 45
        // end::withDefaultValueInterface[]
        """
    }

    void testTypeCheckedInterfaceRunner() {
        assertScript """
        $importCliBuilder
        import cli.TypeCheckedI
        import groovy.transform.TypeChecked
        // tag::withTypeCheckedInterface[]
        @TypeChecked
        void testTypeCheckedInterface() {
            def argz = "--name John --age 21 and some more".split()
            def cli = new CliBuilder()
            def options = cli.parseFromSpec(TypeCheckedI, argz)
            String n = options.name()
            int a = options.age()
            assert n == 'John' && a == 21
            assert options.remaining() == ['and', 'some', 'more']
        }
        // end::withTypeCheckedInterface[]
        testTypeCheckedInterface()
        """
    }

    void testTypeCheckedRunner() {
        assertScript """
        $importCliBuilder
        // tag::withTypeChecked[]
        import groovy.cli.TypedOption
        import groovy.transform.TypeChecked

        @TypeChecked
        void testTypeChecked() {
            def cli = new CliBuilder()
            TypedOption<String> name = cli.option(String, opt: 'n', longOpt: 'name', 'name option')
            TypedOption<Integer> age = cli.option(Integer, longOpt: 'age', 'age option')
            def argz = "--name John --age 21 and some more".split()
            def options = cli.parse(argz)
            String n = options[name]
            int a = options[age]
            assert n == 'John' && a == 21
            assert options.arguments() == ['and', 'some', 'more']
        }
        // end::withTypeChecked[]
        testTypeChecked()
        """
    }
}
