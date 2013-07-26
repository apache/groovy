/*
 *  Copyright 2003-2013 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is
 *  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing permissions and limitations under the
 *  License.
 */

package groovy.util

import org.codehaus.groovy.cli.GroovyPosixParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.PosixParser
import org.apache.commons.cli.BasicParser

/**
 * Test class for the CliBuilder -- but then that is obvious from the name :-)
 * <p>
 * There appear to be issues when using the <code>PosixParser</code> in 1.0 and 1.1 &ndash; when an
 * option with a parameter is passed using a long form and a single letter parameter of some sort has been
 * declared (the problem does not occur if no single letter option has been declared) then the value "--"
 * is returned instead of the option parameter value.  This problem does not happen using the
 * <code>GnuParser</code>.
 * <p>
 * There appears to be an issue with <code>GnuParser</code> in 1.0 and 1.1 &ndash; if only a long option
 * is defined then the usual Groovy syntax for accessing the option fails to work.  It is fine if a short
 * option of some sort is defined.  This must be a <code>CliBuilder</code>/<code>OptionAccessor</code>
 * problem.  This problem does not happen with the <code>PosixParser</code>.
 * <p>
 * Commons CLI 1.0 appears not to be able to access arguments using a long name, if that option has a
 * short name -- in this case access is only using a short name.  This means it is possible to work with
 * long name option if and only if they have no short name.
 * <p>
 * Commons CLI 1.1 has fixed most of the problems in 1.0, but appears to have a broken getOptionValues
 * -- it returns only the first value -- and so is worse than useless.
 * <p>
 * 1.0 PosixBuilder removes unrecognized single letter options silently.  1.1 version may also do this.
 * GnuParser behaves according to the <code>stopAtNonOption</code> parameter -- throw
 * <code>UnrecognizedOptionException</code> when <code>false</code>, terminate parse leaving everything
 * following unprocessed if <code>true</code>.
 * <p>
 * Commons CLI 1.2 is supposed to fix all the bugs!
 *
 * @author Dierk König
 * @author Russel Winder
 * @author Paul King
 */

class CliBuilderTest extends GroovyTestCase {

    private StringWriter stringWriter
    private PrintWriter printWriter

    void setUp() {
        stringWriter = new StringWriter()
        printWriter = new PrintWriter(stringWriter)
    }

    private final expectedParameter = 'ASCII'
    private final usageString = 'groovy [option]* filename'

    private void runSample(parser, optionList) {
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: parser)
        cli.h(longOpt: 'help', 'usage information')
        cli.c(argName: 'charset', args: 1, longOpt: 'encoding', 'character encoding')
        cli.i(argName: 'extension', optionalArg: true, 'modify files in place, create backup if extension is given (e.g. \'.bak\')')
        def stringified = cli.options.toString()
        assert stringified =~ /i=\[ option: i  :: modify files in place, create backup if extension is given/
        assert stringified =~ /c=\[ option: c encoding  \[ARG] :: character encoding/ // 1.2 behaves differently to 1.0 and 1.1 here.
        assert stringified =~ /h=\[ option: h help  :: usage information/
        assert stringified =~ /encoding=\[ option: c encoding  \[ARG] :: character encoding/ // 1.2 behaves differently to 1.0 and 1.1 here.
        assert stringified =~ /help=\[ option: h help  :: usage information/
        def options = cli.parse(optionList)
        assert options.hasOption('h')
        assert options.hasOption('help')  //// Fails in 1.0, works for 1.x where x > 1.
        assert options.h
        assert options.help  //// Fails in 1.0, works for 1.x where x > 1.
        if (options.h) { cli.usage() }
        def expectedUsage = """usage: $usageString
 -c,--encoding <charset>   character encoding
 -h,--help                 usage information
 -i                        modify files in place, create backup if
                           extension is given (e.g. '.bak')"""
        assertEquals(expectedUsage, stringWriter.toString().tokenize('\r\n').join('\n'))
        stringWriter = new StringWriter()
        printWriter = new PrintWriter(stringWriter)
        cli.writer = printWriter
        if (options.help) { cli.usage() }  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(expectedUsage, stringWriter.toString().tokenize('\r\n').join('\n'))  //// Fails in 1.0, works for 1.x where x > 1.
        assert options.hasOption('c')
        assert options.c
        assert options.hasOption('encoding')  //// Fails in 1.0, works for 1.x where x > 1.
        assert options.encoding  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(expectedParameter, options.getOptionValue('c'))
        assertEquals(expectedParameter, options.c)
        assertEquals(expectedParameter, options.getOptionValue('encoding'))  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(expectedParameter, options.encoding)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(false, options.noSuchOptionGiven)
        assertEquals(false, options.hasOption('noSuchOptionGiven'))
        assertEquals(false, options.x)
        assertEquals(false, options.hasOption('x'))
    }

    void testSampleShort_BasicParser() {
        runSample(new BasicParser(), ['-h', '-c', expectedParameter])
    }

    void testSampleShort_GnuParser() {
        runSample(new GnuParser(), ['-h', '-c', expectedParameter])
    }

    void testSampleShort_PosixParser() {
        runSample(new PosixParser(), ['-h', '-c', expectedParameter])
    }

    void testSampleShort_DefaultParser() {
        runSample(new GroovyPosixParser(), ['-h', '-c', expectedParameter])
    }

    void testSampleLong_BasicParser() {
        runSample(new BasicParser(), ['--help', '--encoding', expectedParameter])
    }

    void testSampleLong_GnuParser() {
        runSample(new GnuParser(), ['--help', '--encoding', expectedParameter])
    }

    void testSampleLong_PosixParser() {
        runSample(new PosixParser(), ['--help', '--encoding', expectedParameter])
    }

    void testSampleLong_DefaultParser() {
        runSample(new GroovyPosixParser(), ['--help', '--encoding', expectedParameter])
    }

    private void multipleArgs(parser) {
        def cli = new CliBuilder(parser: parser)
        cli.a(longOpt: 'arg', args: 2, valueSeparator: ',' as char, 'arguments')
        def options = cli.parse(['-a', '1,2'])
        assertEquals('1', options.a)
        assertEquals(['1', '2'], options.as)
        assertEquals('1', options.arg)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(['1', '2'], options.args)  //// Fails in 1.0, works for 1.x where x > 1.
    }

    void testMultipleArgs_BasicParser() { multipleArgs(new BasicParser()) }

    void testMultipleArgs_GnuParser() { multipleArgs(new GnuParser()) }

    void testMultipleArgs_PosixParser() { multipleArgs(new PosixParser()) }

    private void doArgs(parser) {
        def cli = new CliBuilder(parser: parser)
        cli.a([:], '')
        def options = cli.parse(['-a', '1', '2'])
        assertEquals(['1', '2'], options.arguments())
    }

    void testArgs_BasicParser() { doArgs(new BasicParser()) }

    void testArgs_GnuParser() { doArgs(new GnuParser()) }

    void testArgs_PosixParser() { doArgs(new PosixParser()) }

    void testFailedParsePrintsUsage() {
        def cli = new CliBuilder(writer: printWriter)
        cli.x(required: true, 'message')
        cli.parse([])
        //
        // NB: This test is very fragile and is bound to fail on different locales and versions of commons-cli... :-(
        //
        assert stringWriter.toString().normalize() == '''error: Missing required option: x
usage: groovy
 -x   message
'''
    }

    private void checkLongOptsOnly_nonOptionShouldStopArgProcessing(CliBuilder cli) {
        def anOption = OptionBuilder.withLongOpt('anOption').hasArg().withDescription('An option.').create()
        cli.options.addOption(anOption)
        def options = cli.parse(['-v', '--anOption', 'something'])
        // no options should be found
        assert options.getOptionValue('anOption') == null
        assert !options.anOption
        assert !options.v
        // arguments should be still sitting there
        assert options.arguments() == ['-v', '--anOption', 'something']
    }

    void testLongOptsOnly_GnuParser() {
        def cli = new CliBuilder(parser: new GnuParser())
        checkLongOptsOnly_nonOptionShouldStopArgProcessing(cli)
    }

    void testLongOptsOnly_PosixParser() {
        def cli = new CliBuilder(parser: new PosixParser())
        checkLongOptsOnly_nonOptionShouldStopArgProcessing(cli)
    }

    void testLongOptsOnly_GnuParser_settingPosixBooleanFalse() {
        def cli = new CliBuilder(posix: false)
        checkLongOptsOnly_nonOptionShouldStopArgProcessing(cli)
    }

    private void checkLongAndShortOpts_allOptionsValid(parser) {
        def cli = new CliBuilder(parser: parser)
        def anOption = OptionBuilder.withLongOpt('anOption').hasArg().withDescription('An option.').create()
        cli.options.addOption(anOption)
        cli.v(longOpt: 'verbose', 'verbose mode')
        def options = cli.parse(['-v', '--anOption', 'something'])
        assert options.v
        assert options.getOptionValue('anOption') == 'something'
        assert options.anOption == 'something'
        assert !options.arguments()
    }

    void testLongAndShortOpts_BasicParser() {
        checkLongAndShortOpts_allOptionsValid(new BasicParser())
    }

    void testLongAndShortOpts_PosixParser() {
        checkLongAndShortOpts_allOptionsValid(new PosixParser())
    }

    void testLongAndShortOpts_GnuParser() {
        checkLongAndShortOpts_allOptionsValid(new GnuParser())
    }

    void unknownOptions(parser) {
        def cli = new CliBuilder(parser: parser)
        cli.v(longOpt: 'verbose', 'verbose mode')
        def options = cli.parse(['-x', '-yyy', '--zzz', 'something'])
        assertEquals(['-x', '-yyy', '--zzz', 'something'], options.arguments())
    }

    void testUnrecognizedOptions_BasicParser() { unknownOptions(new BasicParser()) }

    void testUnrecognizedOptions_GnuParser() { unknownOptions(new GnuParser()) }

    void testUnrecognizedOptions_PosixParser() { unknownOptions(new PosixParser()) }

    void bizarreProcessing(parser) {
        def cli = new CliBuilder(parser: parser)
        def options = cli.parse(['-xxxx'])
        assertEquals(['-xxxx'], options.arguments())
    }

    void testBizarreProcessing_BasicParser() { bizarreProcessing(new BasicParser()) }

    void testBizarreProcessing_GnuParser() { bizarreProcessing(new GnuParser()) }

    void testPosixBizarreness() {
        def cli = new CliBuilder(parser: new PosixParser())
        def options = cli.parse(['-xxxx'])
        assertEquals(['xxxx'], options.arguments())
    }

    private void multipleOccurrencesSeparateSeparate(parser) {
        def cli = new CliBuilder(parser: parser)
        cli.a(longOpt: 'arg', args: Option.UNLIMITED_VALUES, 'arguments')
        def options = cli.parse(['-a', '1', '-a', '2', '-a', '3'])
        assertEquals('1', options.a)
        assertEquals(['1', '2', '3'], options.as)
        assertEquals('1', options.arg)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(['1', '2', '3'], options.args)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals([], options.arguments())
    }

    void testMultipleOccurrencesSeparateSeparate_BasicParser() { multipleOccurrencesSeparateSeparate(new BasicParser()) }

    void testMultipleOccurrencesSeparateSeparate_GnuParser() { multipleOccurrencesSeparateSeparate(new GnuParser()) }

    void testMultipleOccurrencesSeparateSeparate_PosixParser() { multipleOccurrencesSeparateSeparate(new PosixParser()) }

    private void multipleOccurrencesSeparateJuxtaposed(parser) {
        def cli = new CliBuilder(parser: parser)
        //cli.a ( longOpt : 'arg' , args : Option.UNLIMITED_VALUES , 'arguments' )
        cli.a(longOpt: 'arg', args: 1, 'arguments')
        def options = cli.parse(['-a1', '-a2', '-a3'])
        assertEquals('1', options.a)
        assertEquals(['1', '2', '3'], options.as)
        assertEquals('1', options.arg)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(['1', '2', '3'], options.args)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals([], options.arguments())
    }
    //
    //  BasicParser cannot handle this one.
    //
    //void testMultipleOccurrencesSeparateJuxtaposed_BasicParser ( ) { multipleOccurrencesSeparateJuxtaposed ( new BasicParser ( ) ) }

    void testMultipleOccurrencesSeparateJuxtaposed_GnuParser() { multipleOccurrencesSeparateJuxtaposed(new GnuParser()) }

    void testMultipleOccurrencesSeparateJuxtaposed_PosixParser() { multipleOccurrencesSeparateJuxtaposed(new PosixParser()) }

    private void multipleOccurrencesTogetherSeparate(parser) {
        def cli = new CliBuilder(parser: parser)
        cli.a(longOpt: 'arg', args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, 'arguments')
        def options = cli.parse(['-a 1,2,3'])
        assertEquals(' 1', options.a)
        assertEquals([' 1', '2', '3'], options.as)
        assertEquals(' 1', options.arg)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals([' 1', '2', '3'], options.args)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals([], options.arguments())
    }
    //
    //  BasicParser cannot handle this one.
    //
    //void testMultipleOccurrencesTogetherSeparate_BasicParser ( ) { multipleOccurrencesTogetherSeparate ( new BasicParser ( ) ) }

    void testMultipleOccurrencesTogetherSeparate_GnuParser() { multipleOccurrencesTogetherSeparate(new GnuParser()) }

    void testMultipleOccurrencesTogetherSeparate_PosixParser() { multipleOccurrencesTogetherSeparate(new PosixParser()) }

    private void multipleOccurrencesTogetherJuxtaposed(parser) {
        def cli = new CliBuilder(parser: parser)
        cli.a(longOpt: 'arg', args: Option.UNLIMITED_VALUES, valueSeparator: ',' as char, 'arguments')
        def options = cli.parse(['-a1,2,3'])
        assertEquals('1', options.a)
        assertEquals(['1', '2', '3'], options.as)
        assertEquals('1', options.arg)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals(['1', '2', '3'], options.args)  //// Fails in 1.0, works for 1.x where x > 1.
        assertEquals([], options.arguments())
    }
    //
    //  BasicParser cannot handle this one.
    //
    //void testMultipleOccurrencesTogetherJuxtaposed_BasicParser ( ) { multipleOccurrencesTogetherJuxtaposed ( new BasicParser ( ) ) }

    void testMultipleOccurrencesTogetherJuxtaposed_GnuParser() { multipleOccurrencesTogetherJuxtaposed(new GnuParser()) }

    void testMultipleOccurrencesTogetherJuxtaposed_PosixParser() { multipleOccurrencesTogetherJuxtaposed(new PosixParser()) }

    /*
    *  Behaviour with unrecognized options.
    *
    *  TODO: Should add the BasicParser here as well.
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

    void testUnrecognizedOptionSilentlyIgnored_PosixParser() {
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: new PosixParser())
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

    void testUnrecognizedOptionTerminatesParse_PosixParser() {
        def cli = new CliBuilder(usage: usageString, writer: printWriter, parser: new PosixParser())
        cli.h(longOpt: 'help', 'usage information')
        def options = cli.parse(['-v', '-h'])
        checkNoOutput()
        assert !options.v
        assert !options.h
        assertEquals(['-v', '-h'], options.arguments())
    }

    private checkMultiCharShortOpt(posix) {
        def cli = new CliBuilder(writer: printWriter, posix:posix)
        cli.abc('abc option')
        cli.def(longOpt: 'defdef', 'def option')
        def options = cli.parse(['-abc', '--defdef', 'ghi'])
        assert options
        assert options.arguments() == ['ghi']
        assert options.abc && options.def && options.defdef
        checkNoOutput()
    }

    void testMultiCharShortOpt_PosixParser() {
        checkMultiCharShortOpt(true)
    }

    void testMultiCharShortOpt_GnuParser() {
        checkMultiCharShortOpt(false)
    }

    void testArgumentBursting_PosixParserOnly() {
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
}
