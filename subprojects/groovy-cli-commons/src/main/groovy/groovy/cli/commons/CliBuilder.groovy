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
package groovy.cli.commons

import groovy.cli.CliBuilderException
import groovy.cli.Option
import groovy.cli.TypedOption
import groovy.cli.Unparsed
import groovy.transform.Undefined
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option as CliOption
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MetaClassHelper

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Method

import static org.apache.groovy.util.BeanUtils.capitalize

/**
 * Provides a builder to assist the processing of command line arguments.
 * Two styles are supported: dynamic api style (declarative method calls provide a mini DSL for describing options)
 * and annotation style (annotations on an interface or class describe options).
 * <p>
 * <b>Dynamic api style</b>
 * <p>
 * Typical usage (emulate partial arg processing of unix command: ls -alt *.groovy):
 * <pre>
 * def cli = new CliBuilder(usage:'ls')
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
 * usage: ls
 *  -a   display all files
 *  -l   use a long listing format
 *  -t   sort by modification time
 * </pre>
 * An underlying parser that supports what is called argument 'bursting' is used
 * by default. Bursting would convert '-alt' into '-a -l -t' provided no long
 * option exists with value 'alt' and provided that none of 'a', 'l' or 't'
 * takes an argument (in fact the last one is allowed to take an argument).
 * The bursting behavior can be turned off by using an
 * alternate underlying parser. The simplest way to achieve this is by using
 * the deprecated GnuParser from Commons CLI with the parser property on the CliBuilder,
 * i.e. include <code>parser: new GnuParser()</code> in the constructor call.
 * <p>
 * Another example (partial emulation of arg processing for 'ant' command line):
 * <pre>
 * def cli = new CliBuilder(usage:'ant [options] [targets]',
 *                          header:'Options:')
 * cli.help('print this message')
 * cli.logfile(args:1, argName:'file', 'use given file for log')
 * cli.D(args:2, valueSeparator:'=', argName:'property=value',
 *       'use value for given property')
 * def options = cli.parse(args)
 * ...
 * </pre>
 * Usage message would be:
 * <pre>
 * usage: ant [options] [targets]
 * Options:
 *  -D &lt;property=value>   use value for given property
 *  -help                 print this message
 *  -logfile &lt;file>       use given file for log
 * </pre>
 * And if called with the following arguments '-logfile foo -Dbar=baz target'
 * then the following assertions would be true:
 * <pre>
 * assert options // would be null (false) on failure
 * assert options.arguments() == ['target']
 * assert options.Ds == ['bar', 'baz']
 * assert options.logfile == 'foo'
 * </pre>
 * Note the use of some special notation. By adding 's' onto an option
 * that may appear multiple times and has an argument or as in this case
 * uses a valueSeparator to separate multiple argument values
 * causes the list of associated argument values to be returned.
 * <p>
 * Another example showing long options (partial emulation of arg processing for 'curl' command line):
 * <pre>
 * def cli = new CliBuilder(usage:'curl [options] &lt;url&gt;')
 * cli._(longOpt:'basic', 'Use HTTP Basic Authentication')
 * cli.d(longOpt:'data', args:1, argName:'data', 'HTTP POST data')
 * cli.G(longOpt:'get', 'Send the -d data with a HTTP GET')
 * cli.q('If used as the first parameter disables .curlrc')
 * cli._(longOpt:'url', args:1, argName:'URL', 'Set URL to work with')
 * </pre>
 * Which has the following usage message:
 * <pre>
 * usage: curl [options] &lt;url>
 *     --basic         Use HTTP Basic Authentication
 *  -d,--data &lt;data>   HTTP POST data
 *  -G,--get           Send the -d data with a HTTP GET
 *  -q                 If used as the first parameter disables .curlrc
 *     --url &lt;URL>     Set URL to work with
 * </pre>
 * This example shows a common convention. When mixing short and long names, the
 * short names are often one character in size. One character options with
 * arguments don't require a space between the option and the argument, e.g.
 * <code>-Ddebug=true</code>. The example also shows
 * the use of '_' when no short option is applicable.
 * <p>
 * Also note that '_' was used multiple times. This is supported but if
 * any other shortOpt or any longOpt is repeated, then the behavior is undefined.
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
 * import org.apache.commons.cli.*
 * ... as before ...
 * cli << new Option('q', false, 'If used as the first parameter disables .curlrc')
 * cli << Option.builder().longOpt('url').hasArg().argName('URL').
 *                      desc('Set URL to work with').build()
 * ...
 * </pre>
 *
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
 * <b>Supported Option Properties</b>:
 * <pre>
 *   argName:        String
 *   longOpt:        String
 *   args:           int or String
 *   optionalArg:    boolean
 *   required:       boolean
 *   type:           Class
 *   valueSeparator: char
 *   convert:        Closure
 *   defaultValue:   String
 * </pre>
 * See {@link org.apache.commons.cli.Option} for the meaning of most of these properties
 * and {@link CliBuilderTest} for further examples.
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
     * Usage summary displayed as the first line when <code>cli.usage()</code> is called.
     */
    String usage = 'groovy'

    /**
     * Normally set internally but allows you full customisation of the underlying processing engine.
     */
    CommandLineParser parser = null

    /**
     * To change from the default PosixParser to the GnuParser, set this to false. Ignored if the parser is explicitly set.
     * @deprecated use the parser option instead with an instance of your preferred parser
     */
    @Deprecated
    Boolean posix = null

    /**
     * Whether arguments of the form '{@code @}<i>filename</i>' will be expanded into the arguments contained within the file named <i>filename</i> (default true).
     */
    boolean expandArgumentFiles = true

    /**
     * Normally set internally but can be overridden if you want to customise how the usage message is displayed.
     */
    HelpFormatter formatter = new HelpFormatter()

    /**
     * Defaults to stdout but you can provide your own PrintWriter if desired.
     */
    PrintWriter writer = new PrintWriter(System.out)

    /**
     * Optional additional message for usage; displayed after the usage summary but before the options are displayed.
     */
    String header = ''

    /**
     * Optional additional message for usage; displayed after the options are displayed.
     */
    String footer = ''

    /**
     * Indicates that option processing should continue for all arguments even
     * if arguments not recognized as options are encountered (default true).
     */
    boolean stopAtNonOption = true

    /**
     * Allows customisation of the usage message width.
     */
    int width = HelpFormatter.DEFAULT_WIDTH

    /**
     * Not normally accessed directly but full access to underlying options if needed.
     */
    Options options = new Options()

    Map<String, TypedOption> savedTypeOptions = new HashMap<String, TypedOption>()

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
                def option = option(name, [:], args[0])
                options.addOption(option)

                return create(option, null, null, null)
            }
            if (args.size() == 1 && args[0] instanceof CliOption && name == 'leftShift') {
                CliOption option = args[0]
                options.addOption(option)
                return create(option, null, null, null)
            }
            if (args.size() == 2 && args[0] instanceof Map) {
                def convert = args[0].remove('convert')
                def type = args[0].remove('type')
                def defaultValue = args[0].remove('defaultValue')
                if (type && !(type instanceof Class)) {
                    throw new CliBuilderException("'type' must be a Class")
                }
                if ((convert || type) && !args[0].containsKey('args') &&
                        type?.simpleName?.toLowerCase() != 'boolean') {
                    args[0].args = 1
                }
                def option = option(name, args[0], args[1])
                options.addOption(option)
                return create(option, type, defaultValue, convert)
            }
        }
        return InvokerHelper.getMetaClass(this).invokeMethod(this, name, args)
    }

    /**
     * Make options accessible from command line args with parser.
     * Returns null on bad command lines after displaying usage message.
     */
    OptionAccessor parse(args) {
        if (expandArgumentFiles) args = expandArgumentFiles(args)
        if (!parser) {
            parser = posix != null && posix == false ? new GnuParser() : new DefaultParser()
        }
        try {
            def accessor = new OptionAccessor(
                    parser.parse(options, args as String[], stopAtNonOption))
            accessor.savedTypeOptions = savedTypeOptions
            return accessor
        } catch (ParseException pe) {
            writer.println("error: " + pe.message)
            usage()
            return null
        }
    }

    /**
     * Print the usage message with writer (default: System.out) and formatter (default: HelpFormatter)
     */
    void usage() {
        formatter.printHelp(writer, width, usage, header, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, footer)
        writer.flush()
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
        addOptionsFromAnnotations(optionsClass, false)
        def cli = parse(args)
        def cliOptions = [:]
        setOptionsFromAnnotations(cli, optionsClass, cliOptions, false)
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
        addOptionsFromAnnotations(optionInstance.getClass(), true)
        def cli = parse(args)
        setOptionsFromAnnotations(cli, optionInstance.getClass(), optionInstance, true)
        optionInstance
    }

    void addOptionsFromAnnotations(Class optionClass, boolean namesAreSetters) {
        optionClass.methods.findAll{ it.getAnnotation(Option) }.each { Method m ->
            Annotation annotation = m.getAnnotation(Option)
            def typedOption = processAddAnnotation(annotation, m, namesAreSetters)
            options.addOption(typedOption.cliOption)
        }

        def optionFields = optionClass.declaredFields.findAll { it.getAnnotation(Option) }
        if (optionClass.isInterface() && !optionFields.isEmpty()) {
            throw new CliBuilderException("@Option only allowed on methods in interface " + optionClass.simpleName)
        }
        optionFields.each { Field f ->
            Annotation annotation = f.getAnnotation(Option)
            String setterName = "set" + capitalize(f.getName())
            Method m = optionClass.getMethod(setterName, f.getType())
            def typedOption = processAddAnnotation(annotation, m, true)
            options.addOption(typedOption.cliOption)
        }
    }

    private TypedOption processAddAnnotation(Option annotation, Method m, boolean namesAreSetters) {
        String shortName = annotation.shortName()
        String description = annotation.description()
        String defaultValue = annotation.defaultValue()
        char valueSeparator = 0
        if (annotation.valueSeparator()) valueSeparator = annotation.valueSeparator() as char
        boolean optionalArg = annotation.optionalArg()
        Integer numberOfArguments = annotation.numberOfArguments()
        String numberOfArgumentsString = annotation.numberOfArgumentsString()
        Class convert = annotation.convert()
        if (convert == Undefined.CLASS) {
            convert = null
        }
        Map names = calculateNames(annotation.longName(), shortName, m, namesAreSetters)
        def builder = names.short ? CliOption.builder(names.short) : CliOption.builder()
        if (names.long) {
            builder.longOpt(names.long)
        }
        if (numberOfArguments != 1) {
            if (numberOfArgumentsString) {
                throw new CliBuilderException("You can't specify both 'numberOfArguments' and 'numberOfArgumentsString'")
            }
        }
        def details = [:]
        Class type = namesAreSetters ? (m.parameterTypes.size() > 0 ? m.parameterTypes[0] : null) : m.returnType
        if (optionalArg && (!type || !type.isArray())) {
            throw new CliBuilderException("Attempted to set optional argument for non array type")
        }
        def isFlag = type.simpleName.toLowerCase() == 'boolean'
        if (numberOfArgumentsString) {
            details.args = numberOfArgumentsString
            details = adjustDetails(details)
            if (details.optionalArg) optionalArg = true
        } else {
            details.args = isFlag ? 0 : numberOfArguments
        }
        if (details?.args == 0 && !(isFlag || type.name == 'java.lang.Object')) {
            throw new CliBuilderException("Flag '${names.long ?: names.short}' must be Boolean or Object")
        }
        if (description) builder.desc(description)
        if (valueSeparator) builder.valueSeparator(valueSeparator)
        if (type) {
            if (isFlag && details.args == 1) {
                // special flag: treat like normal not boolean expecting explicit 'true' or 'false' param
                isFlag = false
            }
            if (!isFlag) {
                builder.hasArg(true)
                if (details.containsKey('args')) builder.numberOfArgs(details.args)
            }
            if (type.isArray()) {
                builder.optionalArg(optionalArg)
            }
        }
        def typedOption = create(builder.build(), convert ? null : type, defaultValue, convert)
        typedOption
    }

    private TypedOption create(CliOption o, Class theType, defaultValue, convert) {
        Map<String, Object> result = new TypedOption<Object>()
        o.with {
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
        }
        savedTypeOptions[o.longOpt ?: o.opt] = result
        result
    }

    def setOptionsFromAnnotations(def cli, Class optionClass, Object t, boolean namesAreSetters) {
        optionClass.methods.findAll{ it.getAnnotation(Option) }.each { Method m ->
            Annotation annotation = m.getAnnotation(Option)
            Map names = calculateNames(annotation.longName(), annotation.shortName(), m, namesAreSetters)
            processSetAnnotation(m, t, names.long ?: names.short, cli, namesAreSetters)
        }
        optionClass.declaredFields.findAll { it.getAnnotation(Option) }.each { Field f ->
            Annotation annotation = f.getAnnotation(Option)
            String setterName = "set" + capitalize(f.getName())
            Method m = optionClass.getMethod(setterName, f.getType())
            Map names = calculateNames(annotation.longName(), annotation.shortName(), m, true)
            processSetAnnotation(m, t, names.long ?: names.short, cli, true)
        }
        def remaining = cli.arguments()
        optionClass.methods.findAll{ it.getAnnotation(Unparsed) }.each { Method m ->
            processSetRemaining(m, remaining, t, cli, namesAreSetters)
        }
        optionClass.declaredFields.findAll{ it.getAnnotation(Unparsed) }.each { Field f ->
            String setterName = "set" + capitalize(f.getName())
            Method m = optionClass.getMethod(setterName, f.getType())
            processSetRemaining(m, remaining, t, cli, namesAreSetters)
        }
    }

    private void processSetRemaining(Method m, remaining, Object t, cli, boolean namesAreSetters) {
        def resultType = namesAreSetters ? m.parameterTypes[0] : m.returnType
        def isTyped = resultType?.isArray()
        def result
        def type = null
        if (isTyped) {
            type = resultType.componentType
            result = remaining.collect{ cli.getValue(type, it, null) }.asType(resultType)
        } else {
            result = remaining.toList()
        }
        if (namesAreSetters) {
            m.invoke(t, isTyped ? [result] as Object[] : result)
        } else {
            Map names = calculateNames("", "", m, namesAreSetters)
            t.put(names.long, { -> result })
        }
    }

    private void processSetAnnotation(Method m, Object t, String name, cli, boolean namesAreSetters) {
        def conv = savedTypeOptions[name]?.convert
        if (conv && conv instanceof Class) {
            savedTypeOptions[name].convert = conv.newInstance(t, t)
        }
        boolean hasArg = savedTypeOptions[name]?.cliOption?.numberOfArgs == 1
        boolean noArg = savedTypeOptions[name]?.cliOption?.numberOfArgs == 0
        if (namesAreSetters) {
            def isBoolArg = m.parameterTypes.size() > 0 && m.parameterTypes[0].simpleName.toLowerCase() == 'boolean'
            boolean isFlag = (isBoolArg && !hasArg) || noArg
            if (cli.hasOption(name) || isFlag || cli.defaultValue(name)) {
                m.invoke(t, [isFlag ? cli.hasOption(name) :
                                     cli.hasOption(name) ? optionValue(cli, name) : cli.defaultValue(name)] as Object[])
            }
        } else {
            def isBoolRetType = m.returnType.simpleName.toLowerCase() == 'boolean'
            boolean isFlag = (isBoolRetType && !hasArg) || noArg
            t.put(m.getName(), cli.hasOption(name) ?
                    { -> isFlag ? true : optionValue(cli, name) } :
                    { -> isFlag ? false : cli.defaultValue(name) })
        }
    }

    private optionValue(cli, String name) {
        if (savedTypeOptions.containsKey(name)) {
            return cli.getOptionValue(savedTypeOptions[name])
        }
        cli[name]
    }

    private Map calculateNames(String longName, String shortName, Method m, boolean namesAreSetters) {
        boolean useShort = longName == '_'
        if (longName == '_') longName = ""
        def result = longName
        if (!longName) {
            result = m.getName()
            if (namesAreSetters && result.startsWith("set")) {
                result = MetaClassHelper.convertPropertyName(result.substring(3))
            }
        }
        [long: useShort ? "" : result, short: (useShort && !shortName) ? result : shortName]
    }

    // implementation details -------------------------------------

    /**
     * Internal method: How to create an option from the specification.
     */
    CliOption option(shortname, Map details, info) {
        CliOption option
        if (shortname == '_') {
            option = CliOption.builder().desc(info).longOpt(details.longOpt).build()
            details.remove('longOpt')
        } else {
            option = new CliOption(shortname, info)
        }
        adjustDetails(details).each { key, value ->
            if (key != 'opt') { // GROOVY-8607 ignore opt since we already have that
                option[key] = value
            }
        }
        return option
    }

    static Map adjustDetails(Map m) {
        m.collectMany { k, v ->
            if (k == 'args' && v == '+') {
                [[args: org.apache.commons.cli.Option.UNLIMITED_VALUES]]
            } else if (k == 'args' && v == '*') {
                [[args: org.apache.commons.cli.Option.UNLIMITED_VALUES,
                  optionalArg: true]]
            } else if (k == 'args' && v instanceof String) {
                [[args: Integer.parseInt(v)]]
            } else {
                [[(k): v]]
            }
        }.sum()
    }

    static expandArgumentFiles(args) throws IOException {
        def result = []
        for (arg in args) {
            if (arg && arg != '@' && arg[0] == '@') {
                arg = arg.substring(1)
                if (arg[0] != '@') {
                    expandArgumentFile(arg, result)
                    continue
                }
            }
            result << arg
        }
        return result
    }

    private static expandArgumentFile(name, args) throws IOException {
        def charAsInt = { String s -> s.toCharacter() as int }
        new File(name).withReader { r ->
            new StreamTokenizer(r).with {
                resetSyntax()
                wordChars(charAsInt(' '), 255)
                whitespaceChars(0, charAsInt(' '))
                commentChar(charAsInt('#'))
                quoteChar(charAsInt('"'))
                quoteChar(charAsInt('\''))
                while (nextToken() != StreamTokenizer.TT_EOF) {
                    args << sval
                }
            }
        }
    }

}
