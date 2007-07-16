/*
 *  Copyright 2003-2007 the original author or authors.
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

import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.Option
import org.apache.commons.cli.OptionBuilder
import org.apache.commons.cli.PosixParser
import org.apache.commons.cli.BasicParser

/**
 *  Test class for the CliBuilder -- but then that is obvious from the name :-)
 *
 *  <p>There appear to be issues when using the <code>PosixParser</code> &ndash; when an option with a
 *  parameter is passed using a long form and a single letter parameter of some sort has been declared (the
 *  problem does not occur if no single letter option has been declared) then the value "--" is returned
 *  instead of the option parameter value.  This problem does not happen using the
 *  <code>GnuParser</code>.</p>
 *
 *  <p>There appears to be an issue with <code>GnuParser</code> &ndash; if only a long option is defined
 *  then the usual Groovy syntax for accessing the option fails to work.  It is fine if a short option of
 *  some sort is defined.  This must be a <code>CliBuilder</code>/<code>OptionAccessor</code> problem.  This
 *  problem does not happen with the <code>PosixParser</code>.</p>
 *
 *  @author Dierk König
 *  @author Russel Winder
 */

//// Tests marked with 4 slashes work in Commons CLI 1.1 but not in 1.0.  There are other differences, read
//// the comments.

class CliBuilderTest extends GroovyTestCase {

  private StringWriter stringWriter
  private PrintWriter printWriter

  void setUp ( ) {
    stringWriter = new StringWriter ( )
    printWriter = new PrintWriter ( stringWriter )
  }

  private final expectedParameter = 'ASCII'
  private final usageString =  'groovy [option]* filename'

  private void runSample ( cli , optionList ) {
    cli.h ( longOpt : 'help', 'usage information' )
    cli.c ( argName : 'charset' , args :1 , longOpt : 'encoding' , 'character encoding' )
    cli.i ( argName : 'extension' , optionalArg : true, 'modify files in place, create backup if extension is given (e.g. \'.bak\')' )
    def stringified = cli.options.toString ( )
    assert stringified =~ /i=. option: i  :: modify files in place, create backup if extension is given/
    assert stringified =~ /c=. option: c encoding  :: character encoding/
    assert stringified =~ /h=. option: h help  :: usage information/
    assert stringified =~ /encoding=. option: c encoding  :: character encoding/
    assert stringified =~ /help=. option: h help  :: usage information/
    def options = cli.parse ( optionList )
    assert options.hasOption ( 'h' )
    ////assert options.hasOption ( 'help' )
    assert options.h
    ////assert options.help
    if ( options.h ) { cli.usage ( ) }
    def expectedUsage = """usage: $usageString
 -c,--encoding <charset>   character encoding
 -h,--help                 usage information
 -i                        modify files in place, create backup if
                           extension is given (e.g. '.bak')"""
    assertEquals ( expectedUsage , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    stringWriter = new StringWriter ( )
    printWriter = new PrintWriter ( stringWriter )
    cli.writer = printWriter
    ////if ( options.help ) { cli.usage ( ) }
    ////assertEquals ( expectedUsage , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    assert options.hasOption ( 'c' )
    ////assert options.hasOption ( 'encoding' )
    ////assert options.encoding
    assertEquals ( expectedParameter, options.getOptionValue ( 'c' ) )
    ////assertEquals ( expectedParameter, options.getOptionValue ( 'encoding' ) )
    assertEquals ( expectedParameter, options.c )
    ////assertEquals ( expectedParameter, options.encoding )
    assertEquals ( false, options.noSuchOptionGiven )
    assertEquals ( false, options.x )
  }
  void testSampleShort_GnuParser ( ) {
    runSample ( new CliBuilder ( usage : usageString , writer : printWriter , parser : new GnuParser ( ) ) , [ '-h' , '-c' , expectedParameter ] )
  }
  void testSampleShort_PosixParser ( ) {
    runSample ( new CliBuilder ( usage : usageString , writer : printWriter , parser : new PosixParser ( ) ) , [ '-h' , '-c' , expectedParameter ] )
  }
  void testSampleLong_GnuParser ( ) {
    runSample ( new CliBuilder ( usage : usageString , writer : printWriter , parser : new GnuParser ( ) ) , [ '--help' , '--encoding' , expectedParameter ] )
  }
  /*
   *  Cannot run this test because of the "--" instead of "ASCII" problem.  See
   *  testLongAndShortOpts_PosixParser below.  This is a 1.0 and a 1.1 problem.
   */
  void XXX_testSampleLong_PosixParser ( ) {
    runSample ( new CliBuilder ( usage : usageString , writer : printWriter , parser : new PosixParser ( ) ) , [ '--help' , '--encoding' , expectedParameter ] )
  }

  void testMultipleArgs ( ) {
    def cli = new CliBuilder ( )
    cli.a ( longOpt : 'arg' , args : 2 , valueSeparator : ',' as char , 'arguments' )
    def options = cli.parse ( [ '-a' , '1,2' ] )
    assertEquals ( '1' , options.a )
    assertEquals ( [ '1' , '2' ] , options.as )
    ////assertEquals ( '1' , options.arg )
    ////assertEquals ( [ '1' , '2' ] , options.args )
  }

  void testArgs ( ) {
    def cli = new CliBuilder ( )
    cli.a ( [:] , '' )
    def options = cli.parse ( [ '-a' , '1' , '2' ] )
    assertEquals ( [ '1' , '2' ] , options.arguments ( ) )
  }

  void testFailedParsePrintsUsage ( ) {
    def cli = new CliBuilder ( writer : printWriter )
    cli.x ( required : true , 'message' )
    def options = cli.parse ( [ ] )
    /*
     *  Error messages are slightly different between 1.0 and 1.1.
     *
    assertEquals ( '''error: Missing required option: x
usage: groovy
 -x   message''',  stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    */
    assertEquals ( '''error: -x
usage: groovy
 -x   message''',  stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    }

  void testLongOptsOnly_GnuParser ( ) {
    def cli = new CliBuilder ( writer : printWriter , parser : new GnuParser ( ) )
    def anOption = OptionBuilder.withLongOpt ( 'anOption' ).hasArg ( ).withDescription ( 'An option.' ).create ( )
    cli.options.addOption ( anOption )
    def options = cli.parse ( [ '-v' , '--anOption' , 'something' ] )
    cli.usage ( )
    assert ! options.v
    /*
     *  In 1.1, when run individually using testOne groovy.util.CliBuilderTest, but not when run using testAll or
     *  testOne UberTestCaseGroovySourceSubPackages, the <arg> is missing and this test fails.  What is it
     *  about this way of running things that means the above Commons CLI calls fail to work as they should?
     *  This is WORRYING.
     *
     *  TODO: fixme
     *
    assertEquals ( '''usage: groovy
    --anOption <arg>   An option.''' , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    *
    *  1.0 gets it wrong always :-(
    */ 
    assertEquals ( '''usage: groovy
    --anOption    An option.''' , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    /*
     *  For some reason, no matter how this test is run, anOption is not a recognized option.  This is the
     *  extant behaviour and not what should happen, i.e. this is a bug to be investigated and fixed.  This
     *  is a bug for 1.1, 1.0 just always gets it wrong anyway.
     *
     *  TODO:  Fixme
     *
    assertEquals ( 'something' , options.getOptionValue ( 'anOption' ) )
    assertEquals ( 'something' , options.anOption )
    */
    assertEquals ( null , options.getOptionValue ( 'anOption' ) )
    assertEquals ( false , options.anOption )
  }
  void testLongOptsOnly_PosixParser ( ) {
    def cli = new CliBuilder ( writer : printWriter , parser : new PosixParser ( ) )
    def anOption = OptionBuilder.withLongOpt ( 'anOption' ).hasArg ( ).withDescription ( 'An option.' ).create ( )
    cli.options.addOption ( anOption )
    def options = cli.parse ( [ '-v' , '--anOption' , 'something' ] )
    cli.usage ( )
    /*
     *  1.0 gets this totally wrong.
     *
    assertEquals ( '''usage: groovy
    --anOption <arg>   An option.''' , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    */
    assertEquals ( '''usage: groovy
    --anOption    An option.''' , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    assertEquals ( 'something' , options.getOptionValue ( 'anOption' ) )
    assertEquals ( 'something' , options.anOption )
    assert ! options.v
  }

  private createOptionsWithLongAndShortOpts ( parser ) {
    def cli = new CliBuilder ( writer : printWriter , parser : parser )
    def anOption = OptionBuilder.withLongOpt ( 'anOption' ).hasArg ( ).withDescription ( 'An option.' ).create ( )
    cli.options.addOption ( anOption )
    cli.v ( longOpt : 'verbose' , 'verbose mode' )
    def options = cli.parse ( [ '-v' , '--anOption' , 'something' ] )
    cli.usage ( )
    /*
     *  1.0 gets this totally wrong.
     *
    assertEquals ( '''usage: groovy
    --anOption <arg>   An option.
 -v,--verbose          verbose mode''' , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
    */
    assertEquals ( '''usage: groovy
    --anOption    An option.
 -v,--verbose     verbose mode''' , stringWriter.toString ( ).tokenize ( '\r\n' ).join ( '\n' ) )
     assert options.v
    return options
  }

  void testLongAndShortOpts_BasicParser ( ) {
    def options = createOptionsWithLongAndShortOpts ( new BasicParser ( ) )
    assertEquals ( 'something' , options.getOptionValue ( 'anOption' ) )
    assertEquals ( 'something' , options.anOption )
  }

  void testLongAndShortOpts_PosixParser ( ) {
    def options = createOptionsWithLongAndShortOpts ( new PosixParser ( ) )
    //
    //  This represents what currently happens, not what should happen.  The problem needs investigating so
    //  that the expected results here can be set to 'something' as it should.
    //
    assertEquals ( '--' , options.getOptionValue ( 'anOption' ) )
    assertEquals ( '--' , options.anOption )
  }

  void testLongAndShortOpts_GnuParser ( ) {
    def options = createOptionsWithLongAndShortOpts ( new GnuParser ( ) )
    assertEquals ( 'something' , options.getOptionValue ( 'anOption' ) )
    assertEquals ( 'something' , options.anOption )
  }

  void unknownOptions ( parser ) {
    def cli = new CliBuilder ( parser : parser )
    cli.v ( longOpt : 'verbose' , 'verbose mode' )
    def options = cli.parse ( [ '-x' , '-yyy' , '--zzz' , 'something' ] )
    assertEquals ( [ '-x' , '-yyy' , '--zzz' , 'something' ] , options.arguments ( ) )
  }
  void testUnrecognizedOptions_BasicParser ( ) { unknownOptions ( new BasicParser ( ) ) }
  void testUnrecognizedOptions_GnuParser ( ) { unknownOptions ( new GnuParser ( ) ) }
  //
  //  The Posix Parser absorbs unrecognized options rather than passing them through.  It is not clear if
  //  this is the correct or incorrect behaviour.
  //
  void testUnrecognizedOptions_PosixParser ( ) {
    def cli = new CliBuilder ( parser : new PosixParser ( ) )
    cli.v ( longOpt : 'verbose' , 'verbose mode' )
    def options = cli.parse ( [ '-x' , '-yyy' , '--zzz' , 'something' ] )
    assertEquals ( [ '-yyy' , '--zzz' , 'something' ] , options.arguments ( ) )
 }

  void bizarreProcessing ( parser ) {
    def cli = new CliBuilder ( parser : parser )
    def options = cli.parse ( [ '-xxxx' ] )
    assertEquals ( [ '-xxxx' ] , options.arguments ( ) )
  }
  void testBizarreProcessing_BasicParser ( ) { bizarreProcessing ( new BasicParser ( ) ) }
  void testBizarreProcessing_GnuParser ( ) { bizarreProcessing ( new GnuParser ( ) ) }
  //
  //  This behaviour by the PosixParser is truly bizarre and so militates in favour of the switch from
  //  PosixParser to GnuParser as the CliBuilder default.
  //
  void testPosixBizarreness ( ) {
    def cli = new CliBuilder ( parser : new PosixParser ( ) )
    def options = cli.parse ( [ '-xxx' ] )
    assertEquals ( [ 'xxx' , 'xx' , 'x' ] , options.arguments ( ) )
  }

  private void multipleOccurrencesSeparateSeparate ( parser ) {
    def cli = new CliBuilder ( parser : parser )
    cli.a ( longOpt : 'arg' , args : Option.UNLIMITED_VALUES , 'arguments' )
    def options = cli.parse ( [ '-a' , '1' , '-a' , '2' , '-a' , '3' ] )
    assertEquals ( '1' , options.a )
    assertEquals ( [ '1' , '2' , '3' ] , options.as )
    ////assertEquals ( '1' , options.arg )
    ////assertEquals ( [ '1' , '2' , '3' ] , options.args )
    assertEquals ( [ ] , options.arguments ( ) )
  }
  void testMultipleOccurrencesSeparateSeparate_BasicParser ( ) { multipleOccurrencesSeparateSeparate ( new BasicParser ( ) ) }
  void testMultipleOccurrencesSeparateSeparate_GnuParser ( ) { multipleOccurrencesSeparateSeparate ( new GnuParser ( ) ) }
  void testMultipleOccurrencesSeparateSeparate_PosixParser ( ) { multipleOccurrencesSeparateSeparate ( new PosixParser ( ) ) }

  private void multipleOccurrencesSeparateJuxtaposed ( parser ) {
    def cli = new CliBuilder ( parser : parser )
    cli.a ( longOpt : 'arg' , args : Option.UNLIMITED_VALUES , 'arguments' )
    def options = cli.parse ( [ '-a1' , '-a2' , '-a3' ] )
    assertEquals ( '1' , options.a )
    assertEquals ( [ '1' , '2' , '3' ] , options.as )
    ////assertEquals ( '1' , options.arg )
    ////assertEquals ( [ '1' , '2' , '3' ] , options.args )
    assertEquals ( [ ] , options.arguments ( ) )
  }
  //
  //  BasicParser cannot handle this one.
  //
  //void testMultipleOccurrencesSeparateJuxtaposed_BasicParser ( ) { multipleOccurrencesSeparateJuxtaposed ( new BasicParser ( ) ) }
  void testMultipleOccurrencesSeparateJuxtaposed_GnuParser ( ) { multipleOccurrencesSeparateJuxtaposed ( new GnuParser ( ) ) }
  void testMultipleOccurrencesSeparateJuxtaposed_PosixParser ( ) { multipleOccurrencesSeparateJuxtaposed ( new PosixParser ( ) ) }

  private void multipleOccurrencesTogetherSeparate ( parser ) {
    def cli = new CliBuilder ( parser : parser )
    cli.a ( longOpt : 'arg' , args : Option.UNLIMITED_VALUES , valueSeparator : ',' as char , 'arguments' )
    def options = cli.parse ( [ '-a 1,2,3' ] )
    assertEquals ( ' 1' , options.a )
    assertEquals ( [ ' 1' , '2' , '3' ] , options.as )
    ////assertEquals ( ' 1' , options.arg )
    ////assertEquals ( [ ' 1' , '2' , '3' ] , options.args )
    assertEquals ( [ ] , options.arguments ( ) )
  }
  //
  //  BasicParser cannot handle this one.
  //
  //void testMultipleOccurrencesTogetherSeparate_BasicParser ( ) { multipleOccurrencesTogetherSeparate ( new BasicParser ( ) ) }
  void testMultipleOccurrencesTogetherSeparate_GnuParser ( ) { multipleOccurrencesTogetherSeparate ( new GnuParser ( ) ) }
  void testMultipleOccurrencesTogetherSeparate_PosixParser ( ) { multipleOccurrencesTogetherSeparate ( new PosixParser ( ) ) }

  private void multipleOccurrencesTogetherJuxtaposed ( parser ) {
    def cli = new CliBuilder ( parser : parser )
    cli.a ( longOpt : 'arg' , args : Option.UNLIMITED_VALUES , valueSeparator : ',' as char , 'arguments' )
    def options = cli.parse ( [ '-a1,2,3' ] )
    assertEquals ( '1' , options.a )
    assertEquals ( [ '1' , '2' , '3' ] , options.as )
    ////assertEquals ( '1' , options.arg )
    ////assertEquals ( [ '1' , '2' , '3' ] , options.args )
    assertEquals ( [ ] , options.arguments ( ) )
  }
  //
  //  BasicParser cannot handle this one.
  //
  //void testMultipleOccurrencesTogetherJuxtaposed_BasicParser ( ) { multipleOccurrencesTogetherJuxtaposed ( new BasicParser ( ) ) }
  void testMultipleOccurrencesTogetherJuxtaposed_GnuParser ( ) { multipleOccurrencesTogetherJuxtaposed ( new GnuParser ( ) ) }
  void testMultipleOccurrencesTogetherJuxtaposed_PosixParser ( ) { multipleOccurrencesTogetherJuxtaposed ( new PosixParser ( ) ) }

}
