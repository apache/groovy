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
package groovy.cli.picocli

import groovy.cli.CliBuilderException
import groovy.cli.Option
import groovy.cli.TypedOption
import groovy.cli.Unparsed
import groovy.transform.Undefined
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MetaClassHelper
import picocli.CommandLine
import picocli.CommandLine.ITypeConverter
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Model.IGetter
import picocli.CommandLine.Model.ISetter
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Model.ParserSpec
import picocli.CommandLine.Model.PositionalParamSpec
import picocli.CommandLine.Model.UsageMessageSpec

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Provides a builder to assist the processing of command line arguments.
 * Two styles are supported: dynamic api style (declarative method calls provide a mini DSL for describing options)
 * and annotation style (annotations on an interface or class describe options).
 * <p>
 * <b>Dynamic api style</b>
 * <p>
 * Typical usage (emulate partial arg processing of unix command: ls -alt *.groovy):
 * <pre>
 * def cli = new CliBuilder(name:'ls')
 * cli.a('display all files')
 * cli.l('use a long listing format')
 * cli.t('sort by modification time')
 * def options = cli.parse(args)
 * assert options // would be null (false) on failure
 * assert options.arguments() == ['*.groovy']
 * assert options.a && options.l && options.t
 * </pre>
 * The usage message for this example (obtained using <code>cli.usage()</code>) is shown below:
 * <pre>
 * Usage: ls [-alt]
 *   -a     display all files
 *   -l     use a long listing format
 *   -t     sort by modification time
 * </pre>
 * An underlying parser that supports what is called argument 'bursting' is used
 * by default. Bursting would convert '-alt' into '-a -l -t' provided no long
 * option exists with value 'alt' and provided that none of 'a', 'l' or 't'
 * takes an argument (in fact the last one is allowed to take an argument).
 * The bursting behavior can be turned off by configuring the underlying parser.
 * The simplest way to achieve this is by setting the posix property on the CliBuilder
 * to false, i.e. include {@code posix: false} in the constructor call.
 * <p>
 * Another example (partial emulation of arg processing for 'ant' command line):
 * <pre>
 * def cli = new CliBuilder(usage:'ant [options] [targets]',
 *                          header:'Options:')
 * cli.help('print this message')
 * cli.logfile(type:File, argName:'file', 'use given file for log')
 * cli.D(type:Map, argName:'property=value', 'use value for given property')
 * cli.lib(argName:'path', valueSeparator:',', args: '3',
 *      'comma-separated list of 3 paths to search for jars and classes')
 * def options = cli.parse(args)
 * ...
 * </pre>
 * Usage message would be:
 * <pre>
 * Usage: ant [options] [targets]
 * Options:
 *   -D= &lt;property=value>   use value for given property
 *       -help              print this message
 *       -lib=&lt;path>,&lt;path>,&lt;path>
 *                          comma-separated list of 3 paths to search for jars and
 *                            classes
 *       -logfile=&lt;file>    use given file for log
 * </pre>
 * And if called with the following arguments '-logfile foo -Dbar=baz -lib=/tmp,/usr/lib,~/libs target'
 * then the following assertions would be true:
 * <pre>
 * assert options // would be null (false) on failure
 * assert options.arguments() == ['target']
 * assert options.D == ['bar': 'baz']
 * assert options.libs == ['/tmp', '/usr/lib', '~/libs']
 * assert options.lib == '/tmp'
 * assert options.logfile == new File('foo')
 * </pre>
 * Note the use of some special notation. By adding 's' onto an option
 * that may appear multiple times and has an argument or as in this case
 * uses a valueSeparator to separate multiple argument values
 * causes the list of associated argument values to be returned.
 * <p>
 * Another example showing long options (partial emulation of arg processing for 'curl' command line):
 * <pre>
 * def cli = new CliBuilder(name:'curl')
 * cli._(longOpt:'basic', 'Use HTTP Basic Authentication')
 * cli.d(longOpt:'data', args:1, argName:'data', 'HTTP POST data')
 * cli.G(longOpt:'get', 'Send the -d data with a HTTP GET')
 * cli.q('If used as the first parameter disables .curlrc')
 * cli._(longOpt:'url', type:URL, argName:'URL', 'Set URL to work with')
 * </pre>
 * Which has the following usage message:
 * <pre>
 * Usage: curl [-Gq] [--basic] [--url=&lt;URL>] [-d=&lt;data>]
 *       --basic         Use HTTP Basic Authentication
 *   -d, --data=&lt;data>   HTTP POST data
 *   -G, --get           Send the -d data with a HTTP GET
 *   -q                  If used as the first parameter disables .curlrc
 *       --url=&lt;URL>     Set URL to work with
 * </pre>
 * This example shows a common convention. When mixing short and long names, the
 * short names are often one character in size. One character options with
 * arguments don't require a space between the option and the argument, e.g.
 * <code>-Ddebug=true</code>. The example also shows
 * the use of '_' when no short option is applicable.
 * <p>
 * Also note that '_' was used multiple times. This is supported but if
 * any other shortOpt or any longOpt is repeated, then the underlying library throws an exception.
 * <p>
 * Short option names may not contain a hyphen. If a long option name contains a hyphen, e.g. '--max-wait' then you can either
 * use the long hand method call <code>options.hasOption('max-wait')</code> or surround
 * the option name in quotes, e.g. <code>options.'max-wait'</code>.
 * <p>
 * Although CliBuilder on the whole hides away the underlying library used
 * for processing the arguments, it does provide some hooks which let you
 * make use of the underlying library directly should the need arise. For
 * example, the last two lines of the 'curl' example above could be replaced
 * with the following:
 * <pre>
 * import picocli.CommandLine.Model.*
 * ... as before ...
 * cli << OptionSpec.builder('-q').
 *                      description('If used as the first parameter disables .curlrc').build()
 * cli << OptionSpec.builder('--url').type(URL.class).paramLabel('&lt;URL>').
 *                      description('Set URL to work with').build()
 * ...
 * </pre>
 * As another example, the <code>usageMessage</code> property gives
 * fine-grained control over the usage help message (see the
 * <a href="http://picocli.info/#_usage_help_with_styles_and_colors">picocli user manual</a>
 * for details):
 *
 * <pre>
 * def cli = new CliBuilder()
 * cli.name = "myapp"
 * cli.usageMessage.with {
 *     headerHeading("@|bold,underline Header heading:|@%n")
 *     header("Header 1", "Header 2")                     // before the synopsis
 *     synopsisHeading("%n@|bold,underline Usage:|@ ")
 *     descriptionHeading("%n@|bold,underline Description heading:|@%n")
 *     description("Description 1", "Description 2")      // after the synopsis
 *     optionListHeading("%n@|bold,underline Options heading:|@%n")
 *     footerHeading("%n@|bold,underline Footer heading:|@%n")
 *     footer("Footer 1", "Footer 2")
 * }</pre>
 *
 * <p>
 * <b>Supported Option Properties</b>:
 * <table border="1" cellspacing="0">
 *   <tr>
 *     <th>Property</th>
 *     <th>Type</th>
 *     <th>Picocli equivalent</th>
 *     <th>Description</th>
 *   </tr>
 *   <tr>
 *     <th><code>argName</code></th>
 *     <td>String</td>
 *     <td><code>names</code></td>
 *     <td>Short name for the option, will be prefixed with a single hyphen.</td>
 *   </tr>
 *   <tr>
 *     <th><code>longOpt</code></th>
 *     <td>String</td>
 *     <td><code>names</code></td>
 *     <td>Long name for the option, will be prefixed with two hyphens
 *       unless {@link CliBuilder#acceptLongOptionsWithSingleHyphen acceptLongOptionsWithSingleHyphen}
 *       is <code>true</code>.
 *       An option must have either a long name or a short name (or both).</td>
 *   </tr>
 *   <tr>
 *     <th><code>args</code></th>
 *     <td>int&nbsp;or&nbsp;String</td>
 *     <td><code>arity</code></td>
 *     <td><code>args</code> indicates the number of parameters for this option.
 *       A String value of '+' indicates at least one up to any number of parameters.
 *       The minimum number of parameters depends on the type (booleans require no parameters)
 *       and the <code>optionalArg</code> setting.
 *       <code>args</code> can often be omitted if a <code>type</code> is specified.
 *       </td>
 *   </tr>
 *   <tr>
 *     <th><code>optionalArg</code></th>
 *     <td>boolean</td>
 *     <td><code>arity</code></td>
 *     <td>If <code>optionalArg=true</code>, then <code>args=3</code>
 *       is the equivalent of <code>arity="0..3"</code> in picocli.
 *       When <code>optionalArg=true</code>, <code>args='+'</code>
 *       is equivalent to <code>arity="0..*"</code>.
 *       </td>
 *   </tr>
 *   <tr>
 *     <th><code>required</code></th>
 *     <td>boolean</td>
 *     <td><code>required</code></td>
 *     <td>If <code>true</code>, this option must be specified on the command line, or an exception is thrown.
 *       </td>
 *   </tr>
 *   <tr>
 *     <th><code>type</code></th>
 *     <td>Class</td>
 *     <td><code>type</code></td>
 *     <td>Option parameters are converted to this type. The underlying library has built-in converters for
 *     <a href="http://picocli.info/#_built_in_types">many types</a>.
 *       A custom converter can be specified with the <code>convert</code> property.
 *       </td>
 *   </tr>
 *   <tr>
 *     <th><code>convert</code></th>
 *     <td>Closure</td>
 *     <td><code>converter</code></td>
 *     <td>A closure that takes a single String parameter and returns an object converted to the <code>type</code> of this option.
 *       The picocli equivalent is the <code><a href="http://picocli.info/#_custom_type_converters">ITypeConverter</a></code> interface.
 *       </td>
 *   </tr>
 *   <tr>
 *     <th><code>valueSeparator</code></th>
 *     <td>char</td>
 *     <td><code>splitRegex</code></td>
 *     <td>The character used to split a single command line argument into parts.
 *       </td>
 *   </tr>
 *   <tr>
 *     <th><code>defaultValue</code></th>
 *     <td>String</td>
 *     <td><code>defaultValue</code></td>
 *     <td>The value the option should have if it did not appear on the command line.
 *       The specified String value will be split into parts with the <code>valueSeparator</code> and
 *       converted to the option <code>type</code> before it is set.
 *       </td>
 *   </tr>
 * </table>
 * See {@link groovy.cli.picocli.CliBuilderTest} for further examples.
 * <p>
 * <b>@-files</b>
 * <p>
 * CliBuilder also supports Argument File processing. If an argument starts with
 * an '@' character followed by a filename, then the contents of the file with name
 * filename are placed into the command line. The feature can be turned off by
 * setting expandArgumentFiles to false. If turned on, you can still pass a real
 * parameter with an initial '@' character by escaping it with an additional '@'
 * symbol, e.g. '@@foo' will become '@foo' and not be subject to expansion. As an
 * example, if the file temp.args contains the content:
 * <pre>
 * -arg1
 * paramA
 * paramB paramC
 * </pre>
 * Then calling the command line with:
 * <pre>
 * someCommand @temp.args -arg2 paramD
 * </pre>
 * Is the same as calling this:
 * <pre>
 * someCommand -arg1 paramA paramB paramC -arg2 paramD
 * </pre>
 * This feature is particularly useful on operating systems which place limitations
 * on the size of the command line (e.g. Windows). The feature is similar to
 * the 'Command Line Argument File' processing supported by javadoc and javac.
 * Consult the corresponding documentation for those tools if you wish to see further examples.
 * <p>
 * <b>Annotation style with an interface</b>
 * <p>
 * With this style an interface is defined containing an annotated method for each option.
 * It might look like this (following roughly the earlier 'ls' example):
 * <pre>
 * import groovy.cli.Option
 * import groovy.cli.Unparsed
 *
 * interface OptionInterface {
 *     @{@link groovy.cli.Option}(shortName='a', description='display all files') boolean all()
 *     @{@link groovy.cli.Option}(shortName='l', description='use a long listing format') boolean longFormat()
 *     @{@link groovy.cli.Option}(shortName='t', description='sort by modification time') boolean time()
 *     @{@link groovy.cli.Unparsed} List remaining()
 * }
 * </pre>
 * Then this description is supplied to CliBuilder during parsing, e.g.:
 * <pre>
 * def args = '-alt *.groovy'.split() // normally from commandline itself
 * def cli = new CliBuilder(usage:'ls')
 * def options = cli.parseFromSpec(OptionInterface, args)
 * assert options.remaining() == ['*.groovy']
 * assert options.all() && options.longFormat() && options.time()
 * </pre>
 * <p>
 * <b>Annotation style with a class</b>
 * <p>
 * With this style a user-supplied instance is used. Annotations on that instance's class
 * members (properties and setter methods) indicate how to set options and provide the option details
 * using annotation attributes.
 * It might look like this (again using the earlier 'ls' example):
 * <pre>
 * import groovy.cli.Option
 * import groovy.cli.Unparsed
 *
 * class OptionClass {
 *     @{@link groovy.cli.Option}(shortName='a', description='display all files') boolean all
 *     @{@link groovy.cli.Option}(shortName='l', description='use a long listing format') boolean longFormat
 *     @{@link groovy.cli.Option}(shortName='t', description='sort by modification time') boolean time
 *     @{@link groovy.cli.Unparsed} List remaining
 * }
 * </pre>
 * Then this description is supplied to CliBuilder during parsing, e.g.:
 * <pre>
 * def args = '-alt *.groovy'.split() // normally from commandline itself
 * def cli = new CliBuilder(usage:'ls')
 * def options = new OptionClass()
 * cli.parseFromInstance(options, args)
 * assert options.remaining == ['*.groovy']
 * assert options.all && options.longFormat && options.time
 * </pre>
 */
class CliBuilder {
    /**
     * The command synopsis displayed as the first line in the usage help message, e.g., when <code>cli.usage()</code> is called.
     * When not set, a default synopsis is generated that shows the supported options and parameters.
     * @see #name
     */
    String usage = 'groovy'

    /**
     * This property allows customizing the program name displayed in the synopsis when <code>cli.usage()</code> is called.
     * Ignored if the {@link #usage} property is set.
     * @since 2.5
     */
    String name = 'groovy'

    /**
     * To disallow clustered POSIX short options, set this to false.
     */
    Boolean posix = true

    /**
     * Whether arguments of the form '{@code @}<i>filename</i>' will be expanded into the arguments contained within the file named <i>filename</i> (default true).
     */
    boolean expandArgumentFiles = true

    /**
     * Configures what the parser should do when arguments not recognized
     * as options are encountered: when <code>true</code> (the default), the
     * remaining arguments are all treated as positional parameters.
     * When <code>false</code>, the parser will continue to look for options, and
     * only the unrecognized arguments are treated as positional parameters.
     */
    boolean stopAtNonOption = true

    /**
     * For backwards compatibility with Apache Commons CLI, set this property to
     * <code>true</code> if the parser should recognize long options with both
     * a single hyphen and a double hyphen prefix. The default is <code>false</code>,
     * so only long options with a double hypen prefix (<code>--option</code>) are recognized.
     * @since 2.5
     */
    boolean acceptLongOptionsWithSingleHyphen = false

    /**
     * The PrintWriter to write the {@link #usage} help message to
     * when <code>cli.usage()</code> is called.
     * Defaults to stdout but you can provide your own PrintWriter if desired.
     */
    PrintWriter writer = new PrintWriter(System.out)

    /**
     * The PrintWriter to write to when invalid user input was provided to
     * the {@link #parse(java.lang.String[])} method.
     * Defaults to stderr but you can provide your own PrintWriter if desired.
     * @since 2.5
     */
    PrintWriter errorWriter = new PrintWriter(System.err)

    /**
     * Optional additional message for usage; displayed after the usage summary
     * but before the options are displayed.
     */
    String header = null

    /**
     * Optional additional message for usage; displayed after the options.
     */
    String footer = null

    /**
     * Allows customisation of the usage message width.
     */
    int width = UsageMessageSpec.DEFAULT_USAGE_WIDTH

    /**
     * Not normally accessed directly but allows fine-grained control over the
     * parser behaviour via the API of the underlying library if needed.
     * @since 2.5
     */
    // Implementation note: this object is separate from the CommandSpec.
    // The values collected here are copied into the ParserSpec of the command.
    final ParserSpec parser = new ParserSpec()
            .stopAtPositional(true)
            .unmatchedOptionsArePositionalParams(true)
            .aritySatisfiedByAttachedOptionParam(true)
            .limitSplit(true)
            .overwrittenOptionsAllowed(true)
            .toggleBooleanFlags(false)

    /**
     * Not normally accessed directly but allows fine-grained control over the
     * usage help message via the API of the underlying library if needed.
     * @since 2.5
     */
    // Implementation note: this object is separate from the CommandSpec.
    // The values collected here are copied into the UsageMessageSpec of the command.
    final UsageMessageSpec usageMessage = new UsageMessageSpec()

    /**
     * Internal data structure mapping option names to their associated {@link TypedOption} object.
     */
    Map<String, TypedOption> savedTypeOptions = new HashMap<String, TypedOption>()

    // CommandSpec is the entry point into the picocli object model for a command.
    // It gives access to a ParserSpec to customize the parser behaviour and
    // a UsageMessageSpec to customize the usage help message.
    // Add OptionSpec and PositionalParamSpec objects to this object to define
    // the options and positional parameters this command recognizes.
    //
    // This field is private for now.
    // It is initialized to an empty spec so options and positional parameter specs
    // can be added dynamically via the programmatic API.
    // When a command spec is defined via annotations, the existing instance is
    // replaced with a new one. This allows the outer CliBuilder instance can be reused.
    private CommandSpec commandSpec = CommandSpec.create()

    /**
     * Sets the {@link #usage usage} property on this <code>CliBuilder</code> and the
     * <code>customSynopsis</code> on the {@link #usageMessage} used by the underlying library.
     * @param usage the custom synopsis of the usage help message
     */
    void setUsage(String usage) {
        this.usage = usage
        usageMessage.customSynopsis(usage)
    }

    /**
     * Sets the {@link #footer} property on this <code>CliBuilder</code>
     * and on the {@link #usageMessage} used by the underlying library.
     * @param footer the footer of the usage help message
     */
    void setFooter(String footer) {
        this.footer = footer
        usageMessage.footer(footer)
    }

    /**
     * Sets the {@link #header} property on this <code>CliBuilder</code> and the
     * <code>description</code> on the {@link #usageMessage} used by the underlying library.
     * @param header the description text of the usage help message
     */
    void setHeader(String header) {
        this.header = header
        // "header" is displayed after the synopsis in previous CliBuilder versions.
        // The picocli equivalent is the "description".
        usageMessage.description(header)
    }

    /**
     * Sets the {@link #width} property on this <code>CliBuilder</code>
     * and on the {@link #usageMessage} used by the underlying library.
     * @param width the width of the usage help message
     */
    void setWidth(int width) {
        this.width = width
        usageMessage.width(width)
    }

    /**
     * Sets the {@link #expandArgumentFiles} property on this <code>CliBuilder</code>
     * and on the {@link #parser} used by the underlying library.
     * @param expand whether to expand argument @-files
     */
    void setExpandArgumentFiles(boolean expand) {
        this.expandArgumentFiles = expand
        parser.expandAtFiles(expand)
    }

    /**
     * Sets the {@link #posix} property on this <code>CliBuilder</code> and the
     * <code>posixClusteredShortOptionsAllowed</code> property on the {@link #parser}
     * used by the underlying library.
     * @param posix whether to allow clustered short options
     */
    void setPosix(Boolean posix) {
        this.posix = posix
        parser.posixClusteredShortOptionsAllowed(posix ?: false)
    }

    /**
     * Sets the {@link #stopAtNonOption} property on this <code>CliBuilder</code> and the
     * <code>stopAtPositional</code> property on the {@link #parser}
     * used by the underlying library.
     * @param stopAtNonOption when <code>true</code> (the default), the
     *          remaining arguments are all treated as positional parameters.
     *          When <code>false</code>, the parser will continue to look for options, and
     *          only the unrecognized arguments are treated as positional parameters.
     */
    void setStopAtNonOption(boolean stopAtNonOption) {
        this.stopAtNonOption = stopAtNonOption
        parser.stopAtPositional(stopAtNonOption)
        parser.unmatchedOptionsArePositionalParams(stopAtNonOption)
    }

    /**
     * For backwards compatibility reasons, if a custom {@code writer} is set, this sets
     * both the {@link #writer} and the {@link #errorWriter} to the specified writer.
     * @param writer the writer to initialize both the {@code writer} and the {@code errorWriter} to
     */
    void setWriter(PrintWriter writer) {
        this.writer = writer
        this.errorWriter = writer
    }

    public <T> TypedOption<T> option(Map args, Class<T> type, String description) {
        def name = args.opt ?: '_'
        args.type = type
        args.remove('opt')
        "$name"(args, description)
    }

    /**
     * Internal method: Detect option specification method calls.
     */
    def invokeMethod(String name, Object args) {
        if (args instanceof Object[]) {
            if (args.size() == 1 && (args[0] instanceof String || args[0] instanceof GString)) {
                def option = option(name, [:], args[0]) // args[0] is description
                commandSpec.addOption(option)
                return create(option, null, null, null)
            }
            if (args.size() == 1 && args[0] instanceof OptionSpec && name == 'leftShift') {
                OptionSpec option = args[0] as OptionSpec
                commandSpec.addOption(option)
                return create(option, null, null, null)
            }
            if (args.size() == 2 && args[0] instanceof Map) {
                Map m = args[0] as Map
                if (m.type && !(m.type instanceof Class)) {
                    throw new CliBuilderException("'type' must be a Class")
                }
                def option = option(name, m, args[1])
                commandSpec.addOption(option)
                return create(option, m.type, option.defaultValue(), option.converters())
            }
        }
        return InvokerHelper.getMetaClass(this).invokeMethod(this, name, args)
    }

    private TypedOption create(OptionSpec o, Class theType, defaultValue, convert) {
        String opt = o.names().sort { a, b -> a.length() - b.length() }.first()
        opt = opt?.length() == 2 ? opt.substring(1) : null

        String longOpt = o.names().sort { a, b -> b.length() - a.length() }.first()
        longOpt = longOpt?.startsWith("--") ? longOpt.substring(2) : null

        Map<String, Object> result = new TypedOption<Object>()
        if (opt != null) result.put("opt", opt)
        result.put("longOpt", longOpt)
        result.put("cliOption", o)
        if (defaultValue) {
            result.put("defaultValue", defaultValue)
        }
        if (convert) {
            if (theType) {
                throw new CliBuilderException("You can't specify 'type' when using 'convert'")
            }
            result.put("convert", convert)
            result.put("type", convert instanceof Class ? convert : convert.getClass())
        } else {
            result.put("type", theType)
        }
        savedTypeOptions[longOpt ?: opt] = result
        result
    }

    /**
     * Make options accessible from command line args with parser.
     * Returns null on bad command lines after displaying usage message.
     */
    OptionAccessor parse(args) {
        CommandLine commandLine = createCommandLine()
        try {
            def accessor = new OptionAccessor(commandLine.parseArgs(args as String[]))
            accessor.savedTypeOptions = savedTypeOptions
            return accessor
        } catch (CommandLine.ParameterException pe) {
            errorWriter.println("error: " + pe.message)
            printUsage(pe.commandLine, errorWriter)
            return null
        }
    }

    private CommandLine createCommandLine() {
        commandSpec.parser(parser)
        commandSpec.name(name).usageMessage(usageMessage)
        if (commandSpec.positionalParameters().empty) {
            commandSpec.addPositional(PositionalParamSpec.builder().type(String[]).arity("*").paramLabel("P").hidden(true).build())
        }
        return new CommandLine(commandSpec)
    }

    /**
     * Prints the usage message with the specified {@link #header header}, {@link #footer footer} and {@link #width width}
     * to the specified {@link #writer writer} (default: System.out).
     */
    void usage() {
        printUsage(commandSpec.commandLine() ?: createCommandLine(), writer)
    }

    private void printUsage(CommandLine commandLine, PrintWriter pw) {
        commandLine.usage(pw)
        pw.flush()
    }

    /**
     * Given an interface containing members with annotations, derive
     * the options specification.
     *
     * @param optionsClass
     * @param args
     * @return an instance containing the processed options
     */
    public <T> T parseFromSpec(Class<T> optionsClass, String[] args) {
        def cliOptions = [:]
        commandSpec = CommandSpec.create()
        addOptionsFromAnnotations(optionsClass, cliOptions, true)
        addPositionalsFromAnnotations(optionsClass, cliOptions, true)
        parse(args)
        DefaultGroovyMethods.asType(cliOptions, optionsClass)
    }

    /**
     * Given an instance containing members with annotations, derive
     * the options specification.
     *
     * @param optionInstance
     * @param args
     * @return the options instance populated with the processed options
     */
    public <T> T parseFromInstance(T optionInstance, args) {
        commandSpec = CommandSpec.create()
        addOptionsFromAnnotations(optionInstance.getClass(), optionInstance, false)
        addPositionalsFromAnnotations(optionInstance.getClass(), optionInstance, false)
        def optionAccessor = parse(args)

        // initialize the boolean properties that were not matched
        if (optionAccessor) {
            optionAccessor.parseResult.commandSpec().options().each { option ->
                if (!optionAccessor.parseResult.hasMatchedOption(option)) {
                    boolean isFlag = option.arity().max == 0 && option.type().simpleName.toLowerCase() == 'boolean'
                    if (isFlag) { option.value = false } // else default has already been applied
                }
            }
        }
        optionInstance
    }

    private void addOptionsFromAnnotations(Class optionClass, Object target, boolean isCoercedMap) {
        optionClass.methods.findAll{ it.getAnnotation(Option) }.each { Method m ->
            Option annotation = m.getAnnotation(Option)
            ArgSpecAttributes attributes = extractAttributesFromMethod(m, isCoercedMap, target)
            commandSpec.addOption(createOptionSpec(annotation, attributes, target))
        }
        def optionFields = optionClass.declaredFields.findAll { it.getAnnotation(Option) }
        if (optionClass.isInterface() && !optionFields.isEmpty()) {
            throw new CliBuilderException("@Option only allowed on methods in interface " + optionClass.simpleName)
        }
        optionFields.each { Field f ->
            Option annotation = f.getAnnotation(Option)
            ArgSpecAttributes attributes = extractAttributesFromField(f, target)
            commandSpec.addOption(createOptionSpec(annotation, attributes, target))
        }
    }

    private void addPositionalsFromAnnotations(Class optionClass, Object target, boolean isCoercedMap) {
        optionClass.methods.findAll{ it.getAnnotation(Unparsed) }.each { Method m ->
            Unparsed annotation = m.getAnnotation(Unparsed)
            ArgSpecAttributes attributes = extractAttributesFromMethod(m, isCoercedMap, target)
            commandSpec.addPositional(createPositionalParamSpec(annotation, attributes, target))
        }
        def optionFields = optionClass.declaredFields.findAll { it.getAnnotation(Unparsed) }
        if (optionClass.isInterface() && !optionFields.isEmpty()) {
            throw new CliBuilderException("@Unparsed only allowed on methods in interface " + optionClass.simpleName)
        }
        optionFields.each { Field f ->
            Unparsed annotation = f.getAnnotation(Unparsed)
            ArgSpecAttributes attributes = extractAttributesFromField(f, target)
            commandSpec.addPositional(createPositionalParamSpec(annotation, attributes, target))
        }
    }

    private static class ArgSpecAttributes {
        Class type
        Class[] auxiliaryTypes
        String label
        IGetter getter
        ISetter setter
        Object initialValue
        boolean hasInitialValue
    }

    private ArgSpecAttributes extractAttributesFromMethod(Method m, boolean isCoercedMap, target) {
        Class type = isCoercedMap ? m.returnType : (m.parameterTypes.size() > 0 ? m.parameterTypes[0] : m.returnType)
        type = type && type == Void.TYPE ? null : type

        Class[] auxTypes = null // TODO extract generic types like List<Integer> or Map<Integer,Double>

        // If the method is a real setter, we can't invoke it to get its value,
        // so instead we need to keep track of its current value ourselves.
        // Additionally, implementation classes may annotate _getter_ methods with @Option;
        // if the getter returns a Collection or Map, picocli will add parsed values to it.
        def currentValue = initialValue(type, m, target, isCoercedMap)
        def getter = {
            currentValue
        }
        def setter = {
            def old = currentValue
            currentValue = it
            if (!isCoercedMap && m.parameterTypes.size() > 0) {
                m.invoke(target, [currentValue].toArray())
            }
            return old
        }
        if (isCoercedMap) {
            target[m.name] = getter
        }
        def label = m.name.startsWith("set") || m.name.startsWith("get") ? MetaClassHelper.convertPropertyName(m.name.substring(3)) : m.name
        new ArgSpecAttributes(type: type, auxiliaryTypes: auxTypes, label: label, getter: getter, setter: setter, initialValue: currentValue, hasInitialValue: isCoercedMap)
    }

    private Object initialValue(Class<?> cls, Method m, Object target, boolean isCoercedMap) {
        if (m.parameterTypes.size() == 0 && m.returnType != Void.TYPE) { // annotated getter
            if (!isCoercedMap) {
                return m.invoke(target)
            }
            if (cls.primitive) {
                if (cls.simpleName.toLowerCase() == 'boolean') {
                    return false
                }
                return 0
            }
            return target[m.name]
        }
        // annotated setter
        if (List.class.isAssignableFrom(cls)) { // TODO support other Collections in future
            return new ArrayList()
        }
        if (Map.class.isAssignableFrom(cls)) {
            return new LinkedHashMap()
        }
        null
    }

    private ArgSpecAttributes extractAttributesFromField(Field f, target) {
        def getter = {
            f.accessible = true
            f.get(target)
        }
        def setter = { newValue ->
            f.accessible = true
            def oldValue = f.get(target)
            f.set(target, newValue)
            oldValue
        }
        Class[] auxTypes = null // TODO extract generic types like List<Integer> or Map<Integer,Double>
        new ArgSpecAttributes(type: f.type, auxiliaryTypes: auxTypes, label: f.name, getter: getter, setter: setter, initialValue: getter.call(), hasInitialValue: true)
    }

    private PositionalParamSpec createPositionalParamSpec(Unparsed unparsed, ArgSpecAttributes attr, Object target) {
        PositionalParamSpec.Builder builder = PositionalParamSpec.builder()

        CommandLine.Range arity = CommandLine.Range.valueOf("0..*")
        if (attr.type == Object) { attr.type = String[] }
        if (attr.type)           { builder.type(attr.type) } // cannot set type to null
        if (attr.auxiliaryTypes) { builder.auxiliaryTypes(attr.auxiliaryTypes) } // cannot set aux types to null
        builder.arity(arity)
        builder.description(unparsed.description())
        builder.paramLabel("<$attr.label>")
        builder.getter(attr.getter)
        builder.setter(attr.setter)
        builder.hasInitialValue(attr.hasInitialValue)
        if (arity.max == 0 && attr.type.simpleName.toLowerCase() == 'boolean' && !attr.initialValue) {
            attr.initialValue = false
        }
        try {
            builder.initialValue(attr.initialValue)
        } catch (Exception ex) {
            throw new CliBuilderException("Could not get initial value of positional parameters: " + ex, ex)
        }
        builder.build()
    }

    private OptionSpec createOptionSpec(Option annotation, ArgSpecAttributes attr, Object target) {
        Map names = calculateNames(annotation.longName(), annotation.shortName(), attr.label)
        String arityString = extractArity(attr.type, annotation.optionalArg(), annotation.numberOfArguments(), annotation.numberOfArgumentsString(), names)
        CommandLine.Range arity = CommandLine.Range.valueOf(arityString)
        if (attr.type == Object && arity.max == 0) { attr.type = boolean }
        OptionSpec.Builder builder = OptionSpec.builder(hyphenate(names))
        if (attr.type)           { builder.type(attr.type) } // cannot set type to null
        if (attr.auxiliaryTypes) { builder.auxiliaryTypes(attr.auxiliaryTypes) } // cannot set aux types to null
        builder.arity(arity)
        builder.description(annotation.description())
        builder.splitRegex(annotation.valueSeparator())
        if (annotation.defaultValue()) { builder.defaultValue(annotation.defaultValue()) } // don't default picocli model to empty string
        builder.paramLabel("<$attr.label>")
        if (annotation.convert() != Undefined.CLASS) {
            if (annotation.convert() instanceof Class) {
                builder.converters(annotation.convert().newInstance(target, target) as ITypeConverter)
            }
        }
        builder.getter(attr.getter)
        builder.setter(attr.setter)
        builder.hasInitialValue(attr.hasInitialValue)
        if (arity.max == 0 && attr.type.simpleName.toLowerCase() == 'boolean' && !attr.initialValue) {
            attr.initialValue = false
        }
        try {
            builder.initialValue(attr.initialValue)
        } catch (Exception ex) {
            throw new CliBuilderException("Could not get initial value of option " + names + ": " + ex, ex)
        }
        builder.build()
    }

    private String[] hyphenate(Map<String, String> names) {
        def both = acceptLongOptionsWithSingleHyphen
        names.values().findAll { it && it != "_" }.collect { it.length() == 1 ? "-$it" : (both ? ["-$it", "--$it"] : ["--$it"]) }.flatten().toArray()
    }

    private String extractArity(Class<?> type, boolean optionalArg, int numberOfArguments, String numberOfArgumentsString, Map names) {
        if (optionalArg && (!type || !isMultiValue(type))) {
            throw new CliBuilderException("Attempted to set optional argument for single-value type on flag '${names.long ?: names.short}'")
        }
        if (numberOfArguments != 1 && numberOfArgumentsString) {
            throw new CliBuilderException("You can't specify both 'numberOfArguments' and 'numberOfArgumentsString' on flag '${names.long ?: names.short}'")
        }
        def isFlag = type.simpleName.toLowerCase() == 'boolean' ||
                     (type.simpleName.toLowerCase() == 'object' && (numberOfArguments == 0 || numberOfArgumentsString == "0"))
        String arity = "0"
        if (numberOfArgumentsString) {
            String max = numberOfArgumentsString.replace('+', '*')
            arity = optionalArg ? "0..$max" : "1..$max"
        } else {
            if (!isFlag) {
                arity = optionalArg ? "0..$numberOfArguments" : "1..$numberOfArguments"
            }
        }
        if (arity == "0" && !(isFlag || type.name == 'java.lang.Object')) {
            throw new CliBuilderException("Flag '${names.long ?: names.short}' must be Boolean or Object")
        }
        arity
    }
    private static boolean isMultiValue(Class<?> cls) {
        cls.isArray() || Collection.class.isAssignableFrom(cls) || Map.class.isAssignableFrom(cls)
    }

    private Map calculateNames(String longName, String shortName, String label) {
        boolean useShort = longName == '_'
        if (longName == '_') longName = ""
        def result = longName ?: label
        [long: useShort ? "" : result, short: (useShort && !shortName) ? result : shortName]
    }

    // implementation details -------------------------------------
    /**
     * Internal method: How to create an OptionSpec from the specification.
     */
    OptionSpec option(shortname, Map details, description) {
        OptionSpec.Builder builder
        if (shortname == '_') {
            builder = OptionSpec.builder("--$details.longOpt").description(description)
            if (acceptLongOptionsWithSingleHyphen) {
                builder.names("-$details.longOpt", "--$details.longOpt")
            }
            details.remove('longOpt')
        } else {
            builder = OptionSpec.builder("-$shortname").description(description)
        }
        commons2picocli(shortname, details).each { key, value ->
            if (builder.hasProperty(key)) {
                builder[key] = value
            } else if (key != 'opt') {    // GROOVY-8607 ignore opt since we already have that
                builder.invokeMethod(key, value)
            }
        }
        if (!builder.type() && !builder.arity() && builder.converters()?.length > 0) {
            builder.arity("1").type(details.convert ? Object : String[])
        }
        return builder.build()
    }

    /** Commons-cli constant that specifies the number of argument values is infinite */
    private static final int COMMONS_CLI_UNLIMITED_VALUES = -2

    // - argName:        String
    // - longOpt:        String
    // - args:           int or String
    // - optionalArg:    boolean
    // - required:       boolean
    // - type:           Class
    // - valueSeparator: char
    // - convert:        Closure
    // - defaultValue:   String
    private Map commons2picocli(shortname, Map m) {
        if (m.args && m.optionalArg) {
            m.arity = "0..${m.args}"
            m.remove('args')
            m.remove('optionalArg')
        }
        if (!m.defaultValue) {
            m.remove('defaultValue') // don't default the picocli model to empty string
        }
        def result = m.collectMany { k, v ->
            if (k == 'args' && v == '+') {
                [[arity: '1..*']]
            } else if (k == 'args' && v == 0) {
                [[arity: '0']]
            } else if (k == 'args') {
                v == COMMONS_CLI_UNLIMITED_VALUES ? [[arity: "*"]] : [[arity: "$v"]]
            } else if (k == 'optionalArg') {
                v ? [[arity: '0..1']] : [[arity: '1']]
            } else if (k == 'argName') {
                [[paramLabel: "<$v>"]]
            } else if (k == 'longOpt') {
                acceptLongOptionsWithSingleHyphen ?
                        [[names: ["-$shortname", "-$v", "--$v"] as String[] ]] :
                        [[names: ["-$shortname",        "--$v"] as String[] ]]
            } else if (k == 'valueSeparator') {
                [[splitRegex: "$v"]]
            } else if (k == 'convert') {
                [[converters: [v] as ITypeConverter[] ]]
            } else {
                [[(k): v]]
            }
        }.sum() as Map
        result
    }
}
