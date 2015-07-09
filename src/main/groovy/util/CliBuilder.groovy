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

import groovy.cli.EnhancedCommandLine
import groovy.cli.Option
import groovy.cli.TypedOption
import groovy.cli.Unparsed
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.GnuParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option as CliOption
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MetaClassHelper
import org.codehaus.groovy.runtime.StringGroovyMethods

import java.lang.annotation.Annotation
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Provides a builder to assist the processing of command line arguments.
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
 *   args:           int
 *   optionalArg:    boolean
 *   required:       boolean
 *   type:           Object (not currently used)
 *   valueSeparator: char
 * </pre>
 * See {@link org.apache.commons.cli.Option} for the meaning of these properties
 * and {@link CliBuilderTest} for further examples.
 * <p>
 *
 * @author Dierk Koenig
 * @author Paul King
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
    /*
    Object defaultValue = map.get("defaultValue");
    defaultValues.put(makeDefaultValueKey(option), defaultValue);
    Class type = (Class) map.get("type");
    if (type != null) option.setType(type);
     */

    /**
     * Internal method: Detect option specification method calls.
     */
    def invokeMethod(String name, Object args) {
        if (args instanceof Object[]) {
            if (args.size() == 1 && (args[0] instanceof String || args[0] instanceof GString)) {
                options.addOption(option(name, [:], args[0]))
                return null
            }
            if (args.size() == 1 && args[0] instanceof CliOption && name == 'leftShift') {
                options.addOption(args[0])
                return null
            }
            if (args.size() == 2 && args[0] instanceof Map) {
                options.addOption(option(name, args[0], args[1]))
                return null
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
            return new OptionAccessor(new EnhancedCommandLine(delegate: parser.parse(options, args as String[], stopAtNonOption)))
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

    public <T> T parseFromSpec(Class<T> optionsClass, String[] args) {
        addOptionsFromAnnotations(optionsClass, false)
        def cli = parse(args)
        def cliOptions = [:]
        setOptionsFromAnnotations(cli, optionsClass, cliOptions, false)
        cliOptions as T
    }

    public <T> T parseFromInstance(T optionInstance, args) {
        addOptionsFromAnnotations(options.getClass(), true)
        def cli = parse(args)
        setOptionsFromAnnotations(cli, optionInstance.getClass(), optionInstance, true)
        optionInstance
    }

    void addOptionsFromAnnotations(Class optionClass, boolean namesAreSetters) {
        optionClass.methods.findAll{ it.getAnnotation(Option) }.each { Method m ->
            Annotation annotation = m.getAnnotation(Option)
            options.addOption(processAddAnnotation(annotation, m, namesAreSetters))
        }
        optionClass.declaredFields.findAll{ it.getAnnotation(Option) }.each { Field f ->
            Annotation annotation = f.getAnnotation(Option)
            String setterName = "set" + MetaClassHelper.capitalize(f.getName());
            Method m = optionClass.getMethod(setterName, f.getType())
            options.addOption(processAddAnnotation(annotation, m, true))
        }
    }

    private CliOption processAddAnnotation(Option annotation, Method m, boolean namesAreSetters) {
        String shortName = annotation.shortName()
        String description = annotation.description()
        String defaultValue = annotation.defaultValue()
        char valueSeparator = annotation.valueSeparator()
        String longName = adjustLongName(annotation.longName(), m, namesAreSetters)
        def builder = shortName && !shortName.isEmpty() ? CliOption.builder(shortName) : CliOption.builder()
        builder.longOpt(longName)
        if (description && !description.isEmpty()) builder.desc(description)
        if (defaultValue && !defaultValue.isEmpty()) builder.withDefaultValue(defaultValue)
        if (valueSeparator) builder.valueSeparator(valueSeparator)
        Class type = namesAreSetters ? (m.parameterTypes.size() > 0 ? m.parameterTypes[0] : null) : m.returnType
        if (type) {
            println "$longName $shortName $type"
            builder.hasArg(type.simpleName.toLowerCase() != 'boolean')
            builder.type(type)
        }
        def typedOption = builder.build()
        savedTypeOptions[longName] = typedOption
        typedOption
    }

    def setOptionsFromAnnotations(def cli, Class optionClass, Object t, boolean namesAreSetters) {
        optionClass.methods.findAll{ it.getAnnotation(Option) }.each { Method m ->
            Annotation annotation = m.getAnnotation(Option)
            String longName = adjustLongName(annotation.longName(), m, namesAreSetters)
            processSetAnnotation(m, t, longName, cli, namesAreSetters)
        }
        optionClass.declaredFields.findAll{ it.getAnnotation(Option) }.each { Field f ->
            Annotation annotation = f.getAnnotation(Option)
            String setterName = "set" + MetaClassHelper.capitalize(f.getName());
            Method m = optionClass.getMethod(setterName, f.getType())
            String longName = adjustLongName(annotation.longName(), m, true)
            processSetAnnotation(m, t, longName, cli, true)
        }
        def remaining = cli.arguments()
        optionClass.methods.findAll{ it.getAnnotation(Unparsed) }.each { Method m ->
            processSetRemaining(m, remaining, t, namesAreSetters)
        }
        optionClass.declaredFields.findAll{ it.getAnnotation(Unparsed) }.each { Field f ->
            String setterName = "set" + MetaClassHelper.capitalize(f.getName());
            Method m = optionClass.getMethod(setterName, f.getType())
            processSetRemaining(m, remaining, t, namesAreSetters)
        }
    }

    private void processSetRemaining(Method m, remaining, Object t, boolean namesAreSetters) {
        if (namesAreSetters) {
            m.invoke(t, remaining.toList())
        } else {
            String longName = adjustLongName("", m, namesAreSetters)
            t.put(longName, { -> remaining.toList() })
        }
    }

    private void processSetAnnotation(Method m, Object t, String longName, cli, boolean namesAreSetters) {
        if (namesAreSetters) {
            boolean isFlag = m.parameterTypes.size() > 0 && m.parameterTypes[0].simpleName.toLowerCase() == 'boolean'
            if (cli.hasOption(longName) || isFlag) {
                m.invoke(t, [isFlag ? cli.hasOption(longName) : cli[longName] /* cliOptions.getOptionValue(savedTypeOptions[longName]) */] as Object[])
            }
        } else {
            boolean isFlag = m.returnType.simpleName.toLowerCase() == 'boolean'
            t.put(m.getName(), cli.hasOption(longName) ?
                    { -> isFlag ? true : cli[longName] /* cliOptions.getOptionValue(savedTypeOptions[longName]) */} :
                    { -> isFlag ? false : null })
        }
    }

    private String adjustLongName(String longName, Method m, boolean namesAreSetters) {
        if (!longName || longName.isEmpty()) {
            longName = m.getName()
            if (namesAreSetters && longName.startsWith("set")) {
                longName = MetaClassHelper.convertPropertyName(longName.substring(3))
            }
        }
        longName
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
        details.each { key, value -> option[key] = value }
        return option
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

class OptionAccessor {
    /* EnhancedCommandLine */ def inner
    Map<String, TypedOption> savedTypeOptions

    OptionAccessor(inner) {
        this.inner = inner
    }

    boolean hasOption(TypedOption typedOption) {
        return inner.hasOption((String) typedOption.get("longOpt"))
    }

    public <T> T getAt(TypedOption<T> typedOption, T defaultValue) {
        String optionName = (String) typedOption.get("longOpt");
        if (hasOption(optionName)) {
            return getTypedValueFromName(optionName);
        }
        return defaultValue;
    }

    private <T> T getTypedValueFromName(String optionName) {
        CliOption option = savedTypeOptions.get(optionName);
        Object type = option.getType();
        String optionValue = inner.getOptionValue(optionName);
        return (T) getTypedValue(type, optionValue);
    }

    private <T> T getTypedValue(Object type, String optionValue) {
        return StringGroovyMethods.asType(optionValue, (Class<T>) type);
    }

    def invokeMethod(String name, Object args) {
        return InvokerHelper.getMetaClass(inner).invokeMethod(inner, name, args)
    }

    def getProperty(String name) {
        def methodname = 'getParsedOptionValue'
        if (name.size() > 1 && name.endsWith('s')) {
            def singularName = name[0..-2]
            if (hasOption(singularName)) {
                name = singularName
                methodname += 's'
            }
        }
        if (name.size() == 1) name = name as char
        def result = InvokerHelper.getMetaClass(inner).invokeMethod(inner, methodname, name)
        println "result = $result for name $name, methodName $methodname"
        if (null == result) result = inner.hasOption(name)
        if (result instanceof String[]) result = result.toList()
        return result
    }

    List<String> arguments() {
        inner.args.toList()
    }
}
