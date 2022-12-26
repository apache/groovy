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
package org.apache.groovy.groovysh

import groovy.transform.AutoFinal
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import jline.Terminal
import jline.WindowsTerminal
import jline.console.history.FileHistory
import org.apache.groovy.groovysh.commands.LoadCommand
import org.apache.groovy.groovysh.commands.RecordCommand
import org.apache.groovy.groovysh.util.CurlyCountingGroovyLexer
import org.apache.groovy.groovysh.util.DefaultCommandsRegistrar
import org.apache.groovy.groovysh.util.PackageHelper
import org.apache.groovy.groovysh.util.PackageHelperImpl
import org.apache.groovy.groovysh.util.ScriptVariableAnalyzer
import org.apache.groovy.groovysh.util.XmlCommandRegistrar
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.Message
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StackTraceUtils
import org.codehaus.groovy.tools.shell.IO
import org.codehaus.groovy.tools.shell.util.MessageSource
import org.codehaus.groovy.tools.shell.util.Preferences
import org.fusesource.jansi.AnsiRenderer

import java.util.regex.Pattern

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT

/**
 * An interactive shell for evaluating Groovy code from the command-line (aka. groovysh).
 *
 * The set of available commands can be modified by placing a file in the classpath named
 * <code>org/codehaus/groovy/tools/shell/commands.xml</code>
 *
 * @see XmlCommandRegistrar
 */
@AutoFinal @CompileStatic
class Groovysh extends Shell {

    private static final Pattern TYPEDEF_PATTERN = ~'^\\s*((?:public|protected|private|static|abstract|final)\\s+)*(?:class|enum|interface).*'
    private static final Pattern METHODDEF_PATTERN = ~'^\\s*((?:public|protected|private|static|abstract|final|synchronized)\\s+)*[a-zA-Z_.]+[a-zA-Z_.<>]+\\s+[a-zA-Z_]+\\(.*'

    public static final String COLLECTED_BOUND_VARS_MAP_VARNAME = 'groovysh_collected_boundvars'

    public static final String INTERPRETER_MODE_PREFERENCE_KEY = 'interpreterMode'
    public static final String AUTOINDENT_PREFERENCE_KEY = 'autoindent'
    public static final String COLORS_PREFERENCE_KEY = 'colors'
    public static final String SANITIZE_PREFERENCE_KEY = 'sanitizeStackTrace'
    public static final String SHOW_LAST_RESULT_PREFERENCE_KEY = 'showLastResult'

    // after how many prefix characters we start displaying all metaclass methods
    public static final String METACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY = 'meta-completion-prefix-length'

    private static final MessageSource messages = new MessageSource(Groovysh)

    //

    final BufferManager buffers = new BufferManager()

    final Parser parser

    final Interpreter interp

    // individual imports are stored without leading 'import ' or trailing ';'
    final List<String> imports = []

    int indentSize = 2

    InteractiveShellRunner runner

    FileHistory history

    boolean historyFull // used as a workaround for GROOVY-2177

    String evictedLine // remembers the command which will get evicted if history is full

    PackageHelper packageHelper

    private CompilerConfiguration configuration

    private static Closure createDefaultRegistrar(ClassLoader classLoader) {
        return { Groovysh groovysh ->
            URL xmlCommandResource = groovysh.getClass().getResource('commands.xml')
            if (xmlCommandResource != null) {
                def registrar = new XmlCommandRegistrar(groovysh, classLoader)
                registrar.register(xmlCommandResource)
            } else {
                def registrar = new DefaultCommandsRegistrar(groovysh)
                registrar.register()
            }
        }
    }

    //--------------------------------------------------------------------------

    Groovysh(ClassLoader classLoader, Binding binding, IO io, @ClosureParams(value=SimpleType, options='org.apache.groovy.groovysh.Groovysh') Closure registrar, CompilerConfiguration configuration, Interpreter interpreter) {
        super(io)
        assert classLoader
        assert binding
        def theRegistrar = registrar ?: createDefaultRegistrar(classLoader)
        interp = interpreter
        parser = new Parser()
        theRegistrar.call(this)
        packageHelper = new PackageHelperImpl(classLoader)
        this.configuration = configuration
    }

    Groovysh(ClassLoader classLoader, Binding binding, IO io, @ClosureParams(value=SimpleType, options='org.apache.groovy.groovysh.Groovysh') Closure registrar = null, CompilerConfiguration configuration = DEFAULT) {
        this(classLoader, binding, io, registrar, configuration, new Interpreter(classLoader, binding, configuration))
    }

    // ClassLoader,Binding,IO variants (drop left-to-right)

    Groovysh(Binding binding, IO io) {
        this(Thread.currentThread().getContextClassLoader(), binding, io)
    }

    Groovysh(IO io, CompilerConfiguration cc) {
        this(Thread.currentThread().getContextClassLoader(), new Binding(), io, null, cc)
    }

    Groovysh(IO io) {
        this(new Binding(), io)
    }

    Groovysh() {
        this(new IO())
    }

    //
    // Execution
    //

    /**
     * Execute a single line, where the line may be a command or Groovy code (complete or incomplete).
     */
    @Override
    Object execute(String line) {
        assert line != null

        // Ignore empty lines
        if (line.trim().isEmpty()) {
            return null
        }

        maybeRecordInput(line)

        Object result

        // First try normal command execution
        if (isExecutable(line)) {
            result = executeCommand(line)

            // For commands, only set the last result when its non-null
            if (result != null) {
                setLastResult(result)
            }

            return result
        }

        // Otherwise treat the line as Groovy
        List<String> current = new ArrayList<>(buffers.current())

        // Append the line to the current buffer
        current << line

        String importsSpec = this.getImportStatements()

        // Attempt to parse the current buffer
        def status = parser.parse([importsSpec] + current)

        switch (status.code) {
            case ParseCode.COMPLETE:
                log.debug('Evaluating buffer...')

                if (io.verbose) {
                    displayBuffer(current)
                }

                if (!Boolean.valueOf(getPreference(INTERPRETER_MODE_PREFERENCE_KEY, 'false')) || isTypeOrMethodDeclaration(current)) {
                    // Evaluate the current buffer w/imports and dummy statement
                    List buff = [importsSpec] + [ 'true' ] + current
                    try {
                        setLastResult(result = interp.evaluate(buff))
                    } catch(MultipleCompilationErrorsException t) {
                        if (isIncompleteCaseOfAntlr4(t)) {
                            // treat like INCOMPLETE case
                            buffers.updateSelected(current)
                            break
                        }
                        throw t
                    }
                } else {
                    // Evaluate Buffer wrapped with code storing bounded vars
                    try {
                        result = evaluateWithStoredBoundVars(importsSpec, current)
                    } catch(MultipleCompilationErrorsException t) {
                        if (isIncompleteCaseOfAntlr4(t)) {
                            // treat like INCOMPLETE case
                            buffers.updateSelected(current)
                            break
                        }
                        throw t
                    }
                }

                buffers.clearSelected()
                break

            case ParseCode.INCOMPLETE:
                // Save the current buffer so user can build up complex multi-line code blocks
                buffers.updateSelected(current)
                break

            case ParseCode.ERROR:
                throw status.cause

            default:
                // Should never happen
                throw new Error("Invalid parse status: $status.code")
        }

        return result
    }

    private boolean isIncompleteCaseOfAntlr4(MultipleCompilationErrorsException t) {
        // TODO antlr4 parser errors pop out here - can we rework to be like antlr2?
        (
                (t.message.contains('Unexpected input: ') || t.message.contains('Unexpected character: ')) && !(
                            t.message.contains("Unexpected input: '}'")
                        || t.message.contains("Unexpected input: ')'")
                        || t.message.contains("Unexpected input: ']'")
                )
        )
    }

    /**
     * return true if the buffer can be recognized as a type declaration statement
     * @param strings
     * @return
     */
    static boolean isTypeOrMethodDeclaration(List<String> buffer) {
        String joined = buffer.join('')
        return joined.matches(TYPEDEF_PATTERN) || joined.matches(METHODDEF_PATTERN)
    }
/*
     * to simulate an interpreter mode, this method wraps the statements into a try/finally block that
     * stores bound variables like unbound variables
     */
    private Object evaluateWithStoredBoundVars(String importsSpec, List<String> current) {
        Object result
        String variableBlocks = null
        // To make groovysh behave more like an interpreter, we need to retrieve all bound
        // vars at the end of script execution, and then update them into the groovysh Binding context.
        Set<String> boundVars = ScriptVariableAnalyzer.getBoundVars(importsSpec + Parser.NEWLINE + current.join(Parser.NEWLINE), interp.classLoader)
        if (boundVars) {
            variableBlocks = "$COLLECTED_BOUND_VARS_MAP_VARNAME = new HashMap();"
            boundVars.each({ String varname ->
                // bound vars can be in global or some local scope.
                // We discard locally scoped vars by ignoring MissingPropertyException
                variableBlocks += """
try {$COLLECTED_BOUND_VARS_MAP_VARNAME[\"$varname\"] = $varname;
} catch (MissingPropertyException e){}"""
            })
        }
        // Evaluate the current buffer w/imports and dummy statement
        List<String> buff
        if (variableBlocks) {
            buff = [importsSpec] + ['try {', 'true'] + current + ['} finally {' + variableBlocks + '}']
        } else {
            buff = [importsSpec] + ['true'] + current
        }
        setLastResult(result = interp.evaluate(buff))

        if (variableBlocks) {
            def boundVarValues = (Map<String, Object>) interp.context.getVariable(COLLECTED_BOUND_VARS_MAP_VARNAME)
            boundVarValues.each({ String name, Object value -> interp.context.setVariable(name, value) })
        }

        return result
    }

    protected Object executeCommand(String line) {
        return super.execute(line)
    }

    /**
     * Display the given buffer.
     */
    void displayBuffer(List buffer) {
        assert buffer

        buffer.eachWithIndex { line, index ->
            def lineNum = formatLineNumber(index)

            io.out.println(" ${lineNum}@|bold >|@ $line")
        }
    }

    String getImportStatements() {
        return this.imports.collect({String it -> "import $it;"}).join('')
    }

    //
    // Prompt
    //

    /**
     * Builds the command prompt name in 1 of 3 ways:
     * <ol>
     * <li>Checks the groovysh.prompt property passed into groovysh script: {@code -Dgroovysh.prompt="hello"}
     * <li>Checks an environment variable called GROOVYSH_PROMPT: {@code export GROOVYSH_PROMPT}
     * <li>If no value is defined returns the default groovy shell prompt.
     * </ol>
     * The code will always assume you want the line number in the prompt. To
     * implement differently overhead the render prompt variable.
     */
    private String buildPrompt() {
        def lineNum = formatLineNumber(buffers.current().size())

        def groovyshellProperty = System.getProperty('groovysh.prompt')
        if (groovyshellProperty) {
            return "@|bold ${groovyshellProperty}:|@${lineNum}@|bold >|@ "
        }
        def groovyshellEnv = System.getenv('GROOVYSH_PROMPT')
        if (groovyshellEnv) {
            return  "@|bold ${groovyshellEnv}:|@${lineNum}@|bold >|@ "
        }
        return "@|bold groovy:|@${lineNum}@|bold >|@ "

    }

    /**
     * Calculate probably desired indentation based on parenthesis balance and last char,
     * as well as what the user used last as indentation.
     * @return a string to indent the next line in the buffer
     */
    String getIndentPrefix() {
        List<String> buffer = this.buffers.current()
        if (buffer.size() < 1) {
            return ''
        }
        StringBuilder src = new StringBuilder()
        for (String line: buffer) {
            src.append(line).append('\n')
        }

        int curlyLevel
        if (configuration.getPluginFactory() instanceof org.apache.groovy.parser.antlr4.Antlr4PluginFactory) {
            def lexer = org.apache.groovy.groovysh.util.antlr4.CurlyCountingGroovyLexer.createGroovyLexer(src.toString())
            curlyLevel = lexer.countCurlyLevel()
        } else {
            // not sure whether the same Lexer instance could be reused.
            def lexer = CurlyCountingGroovyLexer.createGroovyLexer(src.toString())
            // read all tokens
            try {
                while (lexer.nextToken().getType() != CurlyCountingGroovyLexer.EOF) {
                }
            } catch (Exception e) { // TokenStreamException for antlr2
                // ignore
            }
            curlyLevel = lexer.getParenLevel()
        }
        int curlyIndent = curlyLevel * indentSize

        // dedent after closing brackets
        return ' ' * Math.max(curlyIndent, 0)
    }

    String renderPrompt() {
        return AnsiRenderer.render(buildPrompt())
    }

    /**
     * Format the given number suitable for rendering as a line number column.
     */
    protected String formatLineNumber(int num) {
        assert num >= 0

        // Make a %03d-like string for the line number
        return num.toString().padLeft(3, '0')
    }

    //
    // User Profile Scripts
    //

    File getUserStateDirectory() {
        def userHome = new File(System.getProperty('user.home'))
        def dir = new File(userHome, '.groovy')
        return dir.canonicalFile
    }

    /**
     * Loads file from within user groovy state directory.
     */
    @CompileDynamic
    protected void loadUserScript(String filename) {
        assert filename

        def file = new File(getUserStateDirectory(), filename)
        if (file.exists()) {
            def command = registry[LoadCommand.COMMAND_NAME] as Command
            if (command) {
                log.debug("Loading user-script: $file")

                // Disable the result hook for profile scripts
                def previousHook = resultHook
                resultHook = { result -> /* nothing */}
                try {
                    command.load(file.toURI().toURL())
                }
                finally {
                    resultHook = previousHook
                }
            } else {
                log.error("Unable to load user-script, missing '$LoadCommand.COMMAND_NAME' command")
            }
        }
    }

    //
    // Recording
    //

    protected void maybeRecordInput(String line) {
        def record = (RecordCommand) registry[RecordCommand.COMMAND_NAME]
        if (record != null) {
            record.recordInput(line)
        }
    }

    protected void maybeRecordResult(Object result) {
        def record = (RecordCommand) registry[RecordCommand.COMMAND_NAME]
        if (record != null) {
            record.recordResult(result)
        }
    }

    protected void maybeRecordError(Throwable cause) {
        def record = (RecordCommand) registry[RecordCommand.COMMAND_NAME]
        if (record != null) {
            Throwable error
            if (getPreference(SANITIZE_PREFERENCE_KEY, 'false')) {
                error = StackTraceUtils.deepSanitize(cause)
            } else {
                error = cause
            }
            record.recordError(error)
        }
    }

    //
    // Hooks
    //

    final Closure defaultResultHook = { Object result ->
        boolean showLastResult = !io.quiet && (io.verbose || getPreference(SHOW_LAST_RESULT_PREFERENCE_KEY, 'false'))
        if (showLastResult) {
            // avoid String.valueOf here because it bypasses pretty-printing of Collections,
            // e.g. String.valueOf( ['a': 42] ) != ['a': 42].toString()
            io.out.println("@|bold ===>|@ ${InvokerHelper.toString(result)}")
        }
    }

    Closure resultHook = defaultResultHook

    private void setLastResult(Object result) {
        if (resultHook == null) {
            throw new IllegalStateException('Result hook is not set')
        }

        resultHook.call(result)

        interp.context['_'] = result

        maybeRecordResult(result)
    }

    final Closure defaultErrorHook = { Throwable cause ->
        assert cause != null

        if (log.debug || !(cause instanceof CompilationFailedException)) {
            // For CompilationErrors, the Exception Class is usually not useful to the user
            io.err.println("@|bold,red ERROR|@ ${cause.getClass().name}:")
        }

        if (cause instanceof MultipleCompilationErrorsException) {
            Writer data = new org.apache.groovy.io.StringBuilderWriter()
            PrintWriter writer = new PrintWriter(data)
            ErrorCollector collector = ((MultipleCompilationErrorsException) cause).getErrorCollector()
            Iterator<? extends Message> msgIterator = collector.getErrors().iterator()
            while (msgIterator.hasNext()) {
                Message errorMsg = msgIterator.next()
                errorMsg.write(writer)
                if (msgIterator.hasNext()) {
                    writer.println()
                }
            }
            io.err.println("@|bold,red ${data.toString()}|@")
        } else {
            io.err.println("@|bold,red ${cause.message}|@")

            maybeRecordError(cause)

            if (log.debug) {
                // If we have debug enabled then skip the fancy bits below
                log.debug(cause)
            }
            else {
                boolean sanitize = getPreference(SANITIZE_PREFERENCE_KEY, 'false')
                // Sanitize the stack trace unless we are in verbose mode, or the user has request otherwise
                def trace = (sanitize && !io.verbose ? StackTraceUtils.deepSanitize(cause) : cause).stackTrace

                def buff = new StringBuilder()

                boolean doBreak = false

                for (e in trace) {
                    // Stop the trace once we find the root of the evaluated script
                    if (e.className == Interpreter.SCRIPT_FILENAME && e.methodName == 'run') {
                        if (io.verbosity != IO.Verbosity.DEBUG && io.verbosity != IO.Verbosity.VERBOSE) {
                            break
                        }
                        doBreak = true
                    }

                    buff << "        @|bold at|@ ${e.className}.${e.methodName} (@|bold "

                    buff << (e.nativeMethod ? 'Native Method' :
                            (e.fileName != null && e.lineNumber != -1 ? "${e.fileName}:${e.lineNumber}" :
                                    (e.fileName != null ? e.fileName : 'Unknown Source')))

                    buff << '|@)'

                    io.err.println(buff)

                    buff.setLength(0) // Reset the buffer
                    if (doBreak) {
                        io.err.println('        @|bold ...|@')
                        break
                    }
                }
            }
        }
    }

    // protected for mocking in tests
    protected String getPreference(String key, String theDefault) {
        return Preferences.get(key, theDefault)
    }

    Closure errorHook = defaultErrorHook

    private void displayError(Throwable cause) {
        if (errorHook == null) {
            throw new IllegalStateException('Error hook is not set')
        }
        if (cause instanceof MissingPropertyException) {
            if (cause.type && cause.type.canonicalName == Interpreter.SCRIPT_FILENAME) {
                io.err.println("@|bold,red Unknown property|@: " + cause.property)
                return
            }
        }

        errorHook.call(cause)
    }

    /**
     * Run the Interactive Shell with optional initial script and files to load.
     */
    int run(String evalString, List<String> filenames) {
        List<String> startCommands = []
        if (evalString?.trim()) {
            startCommands.add(evalString)
        }
        if (filenames) {
            filenames.each {
                startCommands.add("${LoadCommand.COMMAND_NAME} $it".toString())
            }
        }
        return run(startCommands.join('\n'))
    }

    /**
     * Run the Interactive Shell with initial command.
     */
    int run(String commandLine) {
        def code
        try {
            loadUserScript('groovysh.profile')
            loadUserScript('groovysh.rc')

            // Setup the interactive runner
            runner = new InteractiveShellRunner(this, this.&renderPrompt)

            // if args were passed in, just execute as a command
            // (but cygwin gives an empty string, so ignore that)
            if (commandLine?.trim()) {
                runner.wrappedInputStream.insert(commandLine + '\n')
            }

            // Setup the history
            File histFile = new File(userStateDirectory, 'groovysh.history')
            history = new FileHistory(histFile)
            runner.setHistory(history)

            // Setup the error handler
            runner.errorHandler = this.&displayError

            displayWelcomeBanner(runner)

            // And let 'er rip... :-)
            runner.run()

            code = 0
        } catch (ExitNotification n) {
            code = n.code
            log.debug("Exiting w/code: $code")
        } catch (Throwable t) {
            code = 1
            io.err.println(messages.format('info.fatal', t))
            t.printStackTrace(io.err)
        }

        assert code != null // This should never happen

        return code
    }

    /**
     * maybe displays log information and a welcome message
     * @param term
     */
    void displayWelcomeBanner(InteractiveShellRunner runner) {
        if (!log.debug && io.quiet) {
            // nothing to do here
            return
        }
        Terminal term = runner.reader.terminal
        if (log.debug) {
            log.debug("Terminal ($term)")
            log.debug("    Supported:  $term.supported")
            log.debug("    ECHO:       (enabled: $term.echoEnabled)")
            log.debug("    H x W:      ${term.getHeight()} x ${term.getWidth()}")
            log.debug("    ANSI:       ${term.isAnsiSupported()}")

            if (term instanceof WindowsTerminal) {
                WindowsTerminal winterm = (WindowsTerminal) term
                log.debug("    Direct:     ${winterm.directConsole}")
            }
        }

        // Display the welcome banner
        if (!io.quiet) {
            int width = term.getWidth()

            // If we can't tell, or have something bogus then use a reasonable default
            if (width < 1) {
                width = 80
            }

            io.out.println(messages.format('startup_banner.0', GroovySystem.version, System.properties['java.version']))
            io.out.println(messages['startup_banner.1'])
            io.out.println('-' * (width - 1))
        }
    }
}
