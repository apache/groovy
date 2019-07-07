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
package groovy.cli.internal

import groovy.cli.CliBuilderException
import groovy.cli.TypedOption
import org.codehaus.groovy.runtime.InvokerHelper
import picocli.CommandLine

/**
 * Cut-down version of CliBuilder with just enough functionality for Groovy's internal usage.
 * Uses the embedded version of picocli classes.
 * TODO: prune this right back to have only the functionality needed by Groovy commandline tools
 */
class CliBuilderInternal {
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
    int width = CommandLine.Model.UsageMessageSpec.DEFAULT_USAGE_WIDTH

    /**
     * Not normally accessed directly but allows fine-grained control over the
     * parser behaviour via the API of the underlying library if needed.
     * @since 2.5
     */
    // Implementation note: this object is separate from the CommandSpec.
    // The values collected here are copied into the ParserSpec of the command.
    final CommandLine.Model.ParserSpec parser = new CommandLine.Model.ParserSpec()
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
    final CommandLine.Model.UsageMessageSpec usageMessage = new CommandLine.Model.UsageMessageSpec()

    /**
     * Internal data structure mapping option names to their associated {@link groovy.cli.TypedOption} object.
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
    private CommandLine.Model.CommandSpec commandSpec = CommandLine.Model.CommandSpec.create()

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
            if (args.size() == 1 && args[0] instanceof CommandLine.Model.OptionSpec && name == 'leftShift') {
                CommandLine.Model.OptionSpec option = args[0] as CommandLine.Model.OptionSpec
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

    private TypedOption create(CommandLine.Model.OptionSpec o, Class theType, defaultValue, convert) {
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
            commandSpec.addPositional(CommandLine.Model.PositionalParamSpec.builder().type(String[]).arity("*").paramLabel("P").hidden(true).build())
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

    private static class ArgSpecAttributes {
        Class type
        Class[] auxiliaryTypes
        String label
        CommandLine.Model.IGetter getter
        CommandLine.Model.ISetter setter
        Object initialValue
        boolean hasInitialValue
    }

    // implementation details -------------------------------------
    /**
     * Internal method: How to create an OptionSpec from the specification.
     */
    CommandLine.Model.OptionSpec option(shortname, Map details, description) {
        CommandLine.Model.OptionSpec.Builder builder
        if (shortname == '_') {
            builder = CommandLine.Model.OptionSpec.builder("--$details.longOpt").description(description)
            if (acceptLongOptionsWithSingleHyphen) {
                builder.names("-$details.longOpt", "--$details.longOpt")
            }
            details.remove('longOpt')
        } else {
            builder = CommandLine.Model.OptionSpec.builder("-$shortname").description(description)
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
                [[converters: [v] as CommandLine.ITypeConverter[] ]]
            } else {
                [[(k): v]]
            }
        }.sum() as Map
        result
    }
}
