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
package groovy.util

import org.apache.commons.cli.BasicParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Option
import org.codehaus.groovy.cli.GroovyPosixParser

/**
 * Test class for the CliBuilder.
 * <p>
 * Commons CLI has a long history of different parsers with slightly differing behavior and bugs.
 * In nearly all cases, we now recommend using DefaultParser. In case you have very unique circumstances
 * and really need behavior that can only be supplied by one of the legacy parsers, we also include
 * some test case runs against some of the legacy parsers.
 *
 * @author Dierk König
 * @author Russel Winder
 * @author Paul King
 */

class CliBuilderTest extends GroovyTestCase {

    private StringWriter stringWriter
    private PrintWriter printWriter

    void setUp() {
        resetPrintWriter()
    }

    private final expectedParameter = 'ASCII'
    private final usageString = 'groovy [option]* filename'

    private void runSample(parser, optionList) {
        resetPrintWriter()
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: parser)
        cli.h(longOpt: 'help', 'usage information')
        cli.c(argName: 'charset', args: 1, longOpt: 'encoding', 'character encoding')
        cli.i(argName: 'extension', optionalArg: true, 'modify files in place, create backup if extension is given (e.g. \'.bak\')')
        def stringified = cli.options.toString()
        assert stringified =~ /i=\[ option: i  :: modify files in place, create backup if extension is given/
        assert stringified =~ /c=\[ option: c encoding  \[ARG] :: character encoding/
        assert stringified =~ /h=\[ option: h help  :: usage information/
        assert stringified =~ /encoding=\[ option: c encoding  \[ARG] :: character encoding/
        assert stringified =~ /help=\[ option: h help  :: usage information/
        def options = cli.parse(optionList)
        assert options.hasOption('h')
        assert options.hasOption('help')
        assert options.h
        assert options.help
        if (options.h) { cli.usage() }
        def expectedUsage = """usage: $usageString
 -c,--encoding <charset>   character encoding
 -h,--help                 usage information
 -i                        modify files in place, create backup if
                           extension is given (e.g. '.bak')"""
        assertEquals(expectedUsage, stringWriter.toString().tokenize('\r\n').join('\n'))
        resetPrintWriter()
        cli.writer = printWriter
        if (options.help) { cli.usage() }
        assertEquals(expectedUsage, stringWriter.toString().tokenize('\r\n').join('\n'))
        assert options.hasOption('c')
        assert options.c
        assert options.hasOption('encoding')
        assert options.encoding
        assertEquals(expectedParameter, options.getOptionValue('c'))
        assertEquals(expectedParameter, options.c)
        assertEquals(expectedParameter, options.getOptionValue('encoding'))
        assertEquals(expectedParameter, options.encoding)
        assertEquals(false, options.noSuchOptionGiven)
        assertEquals(false, options.hasOption('noSuchOptionGiven'))
        assertEquals(false, options.x)
        assertEquals(false, options.hasOption('x'))
    }

    private void resetPrintWriter() {
        stringWriter = new StringWriter()
        printWriter = new PrintWriter(stringWriter)
    }

    void testSampleShort() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser(), new BasicParser()].each { parser ->
            runSample(parser, ['-h', '-c', expectedParameter])
        }
    }

    void testSampleLong() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser(), new BasicParser()].each { parser ->
            runSample(parser, ['--help', '--encoding', expectedParameter])
        }
    }

    void testSimpleArg() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser(), new BasicParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            cli.a([:], '')
            def options = cli.parse(['-a', '1', '2'])
            assertEquals(['1', '2'], options.arguments())
        }
    }

    void testMultipleArgs() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser(), new BasicParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            cli.a(longOpt: 'arg', args: 2, valueSeparator: ',' as char, 'arguments')
            def options = cli.parse(['-a', '1,2'])
            assertEquals('1', options.a)
            assertEquals(['1', '2'], options.as)
            assertEquals('1', options.arg)
            assertEquals(['1', '2'], options.args)
        }
    }

    void testFailedParsePrintsUsage() {
        def cli = new CliBuilder(writer: printWriter)
        cli.x(required: true, 'message')
        cli.parse([])
        // NB: This test is very fragile and is bound to fail on different locales and versions of commons-cli... :-(
        assert stringWriter.toString().normalize() == '''error: Missing required option: x
usage: groovy
 -x   message
'''
    }

    void testLongOptsOnly_nonOptionShouldStopArgProcessing() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            def anOption = Option.builder().longOpt('anOption').hasArg().desc('An option.').build()
            cli.options.addOption(anOption)
            def options = cli.parse(['-v', '--anOption', 'something'])
            // no options should be found
            assert options.getOptionValue('anOption') == null
            assert !options.anOption
            assert !options.v
            // arguments should be still sitting there
            assert options.arguments() == ['-v', '--anOption', 'something']
        }
    }

    void testLongAndShortOpts_allOptionsValid() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser(), new BasicParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            def anOption = Option.builder().longOpt('anOption').hasArg().desc('An option.').build()
            cli.options.addOption(anOption)
            cli.v(longOpt: 'verbose', 'verbose mode')
            def options = cli.parse(['-v', '--anOption', 'something'])
            assert options.v
            assert options.getOptionValue('anOption') == 'something'
            assert options.anOption == 'something'
            assert !options.arguments()
        }
    }

    void testUnrecognizedOptions() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser(), new BasicParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            cli.v(longOpt: 'verbose', 'verbose mode')
            def options = cli.parse(['-x', '-yyy', '--zzz', 'something'])
            assertEquals(['-x', '-yyy', '--zzz', 'something'], options.arguments())
        }
    }

    void testMultipleOccurrencesSeparateSeparate() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser(), new BasicParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            cli.a(longOpt: 'arg', args: Option.UNLIMITED_VALUES, 'arguments')
            def options = cli.parse(['-a', '1', '-a', '2', '-a', '3'])
            assertEquals('1', options.a)
            assertEquals(['1', '2', '3'], options.as)
            assertEquals('1', options.arg)
            assertEquals(['1', '2', '3'], options.args)
            assertEquals([], options.arguments())
        }
    }

    void testMultipleOccurrencesSeparateJuxtaposed() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            //cli.a ( longOpt : 'arg' , args : Option.UNLIMITED_VALUES , 'arguments' )
            cli.a(longOpt: 'arg', args: 1, 'arguments')
            def options = cli.parse(['-a1', '-a2', '-a3'])
            assertEquals('1', options.a)
            assertEquals(['1', '2', '3'], options.as)
            assertEquals('1', options.arg)
            assertEquals(['1', '2', '3'], options.args)
            assertEquals([], options.arguments())
        }
    }

    void testMultipleOccurrencesTogetherSeparate() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser()].each { parser ->
            def cli = new CliBuilder(parser: parser)
            cli.a(longOpt: 'arg', args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, 'arguments')
            def options = cli.parse(['-a 1,2,3'])
            assertEquals(' 1', options.a)
            assertEquals([' 1', '2', '3'], options.as)
            assertEquals(' 1', options.arg)
            assertEquals([' 1', '2', '3'], options.args)
            assertEquals([], options.arguments())
        }
    }

    void testMultipleOccurrencesTogetherJuxtaposed() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser()].each { parser ->
            def cli1 = new CliBuilder(parser: parser)
            cli1.a(longOpt: 'arg', args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, 'arguments')
            def options = cli1.parse(['-a1,2,3'])
            assertEquals('1', options.a)
            assertEquals(['1', '2', '3'], options.as)
            assertEquals('1', options.arg)
            assertEquals(['1', '2', '3'], options.args)
            assertEquals([], options.arguments()) }
        }

    /*
    *  Behaviour with unrecognized options.
    *
    *  TODO: Should add the BasicParser here as well?
    */

    void testUnrecognizedOptionSilentlyIgnored_GnuParser() {
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: new GnuParser())
        def options = cli.parse(['-v'])
        assertEquals('''''', stringWriter.toString().tokenize('\r\n').join('\n'))
        assert !options.v
    }

    private void checkNoOutput() {
        assert stringWriter.toString().tokenize('\r\n').join('\n') == ''''''
    }

    void testUnrecognizedOptionSilentlyIgnored_DefaultParser() {
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: new DefaultParser())
        def options = cli.parse(['-v'])
        checkNoOutput()
        assert !options.v
    }

    void testUnrecognizedOptionTerminatesParse_GnuParser() {
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: new GnuParser())
        cli.h(longOpt: 'help', 'usage information')
        def options = cli.parse(['-v', '-h'])
        checkNoOutput()
        assert !options.v
        assert !options.h
        assertEquals(['-v', '-h'], options.arguments())
    }

    void testUnrecognizedOptionTerminatesParse_DefaultParser() {
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: new DefaultParser())
        cli.h(longOpt: 'help', 'usage information')
        def options = cli.parse(['-v', '-h'])
        checkNoOutput()
        assert !options.v
        assert !options.h
        assertEquals(['-v', '-h'], options.arguments())
    }

    void testMultiCharShortOpt() {
        [new DefaultParser(), new GroovyPosixParser(), new GnuParser()].each { parser ->
            def cli = new CliBuilder(writer: printWriter, parser: parser)
            cli.abc('abc option')
            cli.def(longOpt: 'defdef', 'def option')
            def options = cli.parse(['-abc', '--defdef', 'ghi'])
            assert options
            assert options.arguments() == ['ghi']
            assert options.abc && options.def && options.defdef
            checkNoOutput()
        }
    }

    void testArgumentBursting_DefaultParserOnly() {
        def cli = new CliBuilder(writer: printWriter)
        // must not have longOpt 'abc' and also no args for a or b
        cli.a('a')
        cli.b('b')
        cli.c('c')
        def options = cli.parse(['-abc', '-d'])
        assert options
        assert options.arguments() == ['-d']
        assert options.a && options.b && options.c && !options.d
        checkNoOutput()
    }

    void testLongOptEndingWithS() {
        def cli = new CliBuilder()
        cli.s(longOpt: 'number_of_seconds', 'a long arg that ends with an "s"')

        def options = cli.parse(['-s'])

        assert options.hasOption('s')
        assert options.hasOption('number_of_seconds')
        assert options.s
        assert options.number_of_seconds
    }

    void testArgumentFileExpansion() {
        def cli = new CliBuilder(usage: 'test usage')
        cli.h(longOpt: 'help', 'usage information')
        cli.d(longOpt: 'debug', 'turn on debug info')
        def args = ['-h', '@temp.args', 'foo', '@@baz']
        def temp = new File('temp.args')
        temp.deleteOnExit()
        temp.text = '-d bar'
        def options = cli.parse(args)
        assert options.h
        assert options.d
        assert options.arguments() == ['bar', 'foo', '@baz']
    }

    void testArgumentFileExpansionArgOrdering() {
        def cli = new CliBuilder(usage: 'test usage')
        def args = ['one', '@temp1.args', 'potato', '@temp2.args', 'four']
        def temp1 = new File('temp1.args')
        temp1.deleteOnExit()
        temp1.text = 'potato two'
        def temp2 = new File('temp2.args')
        temp2.deleteOnExit()
        temp2.text = 'three potato'
        def options = cli.parse(args)
        assert options.arguments() == 'one potato two potato three potato four'.split()
    }

    void testArgumentFileExpansionTurnedOff() {
        def cli = new CliBuilder(usage: 'test usage', expandArgumentFiles:false)
        cli.h(longOpt: 'help', 'usage information')
        cli.d(longOpt: 'debug', 'turn on debug info')
        def args = ['-h', '@temp.args', 'foo', '@@baz']
        def temp = new File('temp.args')
        temp.deleteOnExit()
        temp.text = '-d bar'
        def options = cli.parse(args)
        assert options.h
        assert !options.d
        assert options.arguments() == ['@temp.args', 'foo', '@@baz']
    }

    void testGStringSpecification_Groovy4621() {
        def user = 'scott'
        def pass = 'tiger'
        def ignore = false
        def longOptName = 'user'
        def cli = new CliBuilder(usage: 'blah')
        cli.dbusername(longOpt:"$longOptName", args: 1, "Database username [default $user]")
        cli.dbpassword(args: 1, "Database password [default $pass]")
        cli.i("ignore case [default $ignore]")
        def args = ['-dbpassword', 'foo', '--user', 'bar', '-i']
        def options = cli.parse(args)
        assert options.user == 'bar'
        assert options.dbusername == 'bar'
        assert options.dbpassword == 'foo'
        assert options.i
    }

    void testNoExpandArgsWithEmptyArg() {
        def cli = new CliBuilder(expandArgumentFiles: false)
        cli.parse(['something', ''])
    }

    void testExpandArgsWithEmptyArg() {
        def cli = new CliBuilder(expandArgumentFiles: true)
        cli.parse(['something', ''])
    }

    void testDoubleHyphenShortOptions() {
        def cli = new CliBuilder()
        cli.a([:], '')
        cli.b([:], '')
        def options = cli.parse(['-a', '--', '-b', 'foo'])
        assert options.arguments() == ['-b', 'foo']
    }

    void testDoubleHyphenLongOptions() {
        def cli = new CliBuilder()
        cli._([longOpt:'alpha'], '')
        cli._([longOpt:'beta'], '')
        def options = cli.parse(['--alpha', '--', '--beta', 'foo'])
        assert options.alpha
        assert options.arguments() == ['--beta', 'foo']
    }

    void testMixedShortAndLongOptions() {
        def cli = new CliBuilder()
        cli.a([longOpt:'alpha', args:1], '')
        cli.b([:], '')
        def options = cli.parse(['-b', '--alpha', 'param', 'foo'])
        assert options.a == 'param'
        assert options.arguments() == ['foo']
    }

    void testMixedBurstingAndLongOptions() {
        def cli = new CliBuilder()
        cli.a([:], '')
        cli.b([:], '')
        cli.c([:], '')
        cli.d([longOpt:'abacus'], '')
        def options = cli.parse(['-abc', 'foo'])
        assert options.a
        assert options.b
        assert options.c
        assert options.arguments() == ['foo']
        options = cli.parse(['-abacus', 'foo'])
        assert !options.a
        assert !options.b
        assert !options.c
        assert options.d
        assert options.arguments() == ['foo']
    }
}
