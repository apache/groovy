/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.InvokerInvocationException

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationFailedException

import org.codehaus.groovy.tools.ErrorReporter

import groovy.inspect.swingui.ObjectBrowser

import jline.ConsoleReader

/**
 * An interactive shell for evaluating Groovy code from the command-line (aka. groovysh).
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class InteractiveShell
    implements Runnable
{
    private static final String NEWLINE = System.properties['line.separator']
    
    private final ShellLog log = new ShellLog(this.class)
    
    private final MessageSource messages = new MessageSource(this.class)
    
    private final GroovyShell shell

    private final IO io
    
    private final ConsoleReader reader
    
    private final CommandRegistry registry = new CommandRegistry()

    private final List buffer = []

    private Object lastResult
    
    boolean verbose
    
    InteractiveShell(final ClassLoader classLoader, final Binding binding, final IO io) {
        assert binding
        assert io

        this.io = io
        
        registerCommands()
        
        // Initialize the JLine console input reader
        reader = new ConsoleReader(io.inputStream, io.output)
        
        // Add some completors to fancy things up
        reader.addCompletor(new CommandNameCompletor(registry))
        
        if (classLoader != null) {
            shell = new GroovyShell(classLoader, binding)
        }
        else {
            shell = new GroovyShell(binding)
        }

        log.debug('Initialized')
    }

    InteractiveShell(final Binding binding, final IO io) {
        this(null, binding, io)
    }

    InteractiveShell(final IO io) {
        this(new Binding(), io)
    }
    
    InteractiveShell() {
        this(new IO())
    }
    
    private void registerCommands() {
        registry << new Command('help', '\\h', this.&doHelpCommand)
        
        registry << new CommandAlias('?', '\\?', 'help')

        registry << new Command('exit', '\\e', this.&doExitCommand)

        registry << new CommandAlias('quit', '\\q', 'exit')

        registry << new Command('display', '\\d', this.&doDisplayCommand)

        registry << new Command('variables', '\\v', this.&doVariablesCommand)

        registry << new Command('clear', '\\c', this.&doClearCommand)

        registry << new Command('inspect', '\\i', this.&doInspectCommand)

        registry << new Command('purge', '\\p', this.&doPurgeCommand)

        //
        // TODO: Add 'edit' command, which will pop up some Swing bits to allow the full buffer to be edited
        //

        //
        // TODO: Add 'source' (or 'read') command to fill the buffer from a file/url
        //

        //
        // TODO: Add 'buffer' command to switch buffers, create new ones, list, etc (aka. console tabs)
        //
    }
    
    int run(final String[] args) {
        try {
            processCommandLine(args)
            run()
        }
        catch (ExitNotification n) {
            log.debug("Exiting w/code: ${n.code}")

            return n.code
        }
        catch (Throwable t) {
            io.error.println(messages.format('info.fatal', t))
            t.printStackTrace(io.error)
            
            return 1
        }
        finally {
            io.flush()
        }

        return 0
    }

    /**
     * Signal for the shell to exit.
     */
    private void exit(final int code) {
        throw new ExitNotification(code)
    }

    /**
     * Process command-line arguments.
     */
    private void processCommandLine(final String[] args) {
        assert args != null

        log.debug("Processing command-line args: $args")
        
        def cli = new CliBuilder(usage : 'groovysh [options]', writer: io.output)
        
        cli.h(longOpt: 'help', messages['cli.option.help.description'])
        cli.V(longOpt: 'version', messages['cli.option.version.description'])
        cli.v(longOpt: 'verbose', messages['cli.option.verbose.description'])
        cli.d(longOpt: 'debug', messages['cli.option.debug.description'])
        
        def options = cli.parse(args)
        assert options

        // Currently no arguments are allowed, so complain if there are any
        def _args = options.arguments()
        if (_args.size() != 0) {
            cli.usage()
            io.error.println(messages.format('cli.info.unexpected_args', _args.join(' ')))
            exit(1)
        }

        if (options.h) {
            cli.usage()
            exit(0)
        }

        if (options.V) {
            io.output.println(messages.format('cli.info.version', InvokerHelper.version))
            exit(0)
        }

        if (options.v) {
            verbose = true
        }

        if (options.d) {
            ShellLog.debug = true
        }
    }

    /**
     * Display the weclome banner.
     */
    private void displayBanner() {
        io.output.println(messages.format('startup_banner.0', InvokerHelper.version, System.properties['java.vm.version']))
        io.output.println(messages['startup_banner.1'])
    }

    /**
     * Get the current prompt.
     */
    private String getPrompt() {
        def lineNum = formatLineNumber(buffer.size())
        return "groovy:${lineNum}> "
    }

    /**
     * Run the main interactive shell loop.
     */
    void run() {
        log.debug('Running')

        displayBanner()

        while (true) {
            def line = reader.readLine(prompt)
            
            log.debug("Read line: $line")

            // Stop on null
            if (line == null) {
                break
            }

            // Ingore empty lines
            if (line.trim().size() == 0) {
                continue
            }
            
            execute(line)
        }

        log.debug('Finished')
    }

    /**
     * Execute a single line, where the line may be a command or Groovy code (complete or incomplete).
     */
    void execute(final String line) {
        assert line

        if (!executeCommand(line)) {
            def current = []
            current += buffer

            // Append the line to the current buffer
            current << line

            // Attempt to parse the current buffer
            def status = parse(current, 1)

            switch (status.code) {
                case ParseStatus.COMPLETE:
                    // Evaluate the current buffer
                    evaluate(current)
                    buffer.clear()
                    break

                case ParseStatus.INCOMPLETE:
                    // Save the current buffer so user can build up complex muli-line code blocks
                    buffer = current
                    break

                case ParseStatus.ERROR:
                    // Show a simple compilation error, otherwise dump the full details
                    if (status.cause instanceof CompilationFailedException) {
                        io.error.println(messages.format('info.error', status.cause.message))
                    }
                    else {
                        io.error.println(messages.format('info.error', status.cause))
                        status.cause.printStackTrace(io.error)
                    }
                    break

                default:
                    // Should never happen
                    throw new Error("Invalid parse status: $status.code")
            }
        }
    }

    /**
     * @see #execute(String)
     */
    def leftShift(final String line) {
        execute(line)
    }

    /**
     * Process built-in command execution.
     *
     * @return True if the line was a command and it was executed.
     */
    private boolean executeCommand(final String line) {
        def args = line.trim().tokenize()
        def command = registry.find(args[0])

        if (command) {
            if (args.size() == 1) {
                args = []
            }
            else {
                args = args[1..-1]
            }

            log.debug("Executing command: $command; w/args: $args")

            command.execute(args)

            return true
        }

        return false
    }

    /**
     * Attempt to parse the given buffer.
     */
    private ParseStatus parse(final List buffer, final int tolerance) {
        assert buffer

        def source = buffer.join(NEWLINE)

        log.debug("Parsing: $source")

        def parser
        Throwable error

        try {
            parser = SourceUnit.create('groovysh-script', source, tolerance)
            parser.parse()

            log.debug('Parse complete')

            return new ParseStatus(ParseStatus.COMPLETE)
        }
        catch (CompilationFailedException e) {
            // Report errors other than unexpected EOF
            if (parser.errorCollector.errorCount > 1 || !parser.failedWithUnexpectedEOF()) {
                error = e
            }
        }
        catch (Exception e) {
            error = e
        }

        if (error) {
            log.debug("Parse error: $error")

            return new ParseStatus(error)
        }
        else {
            log.debug('Parse incomplete')

            return new ParseStatus(ParseStatus.INCOMPLETE)
        }
    }

    /**
     * Evaluate the given buffer.  The buffer is assumed to be complete.
     */
    private void evaluate(final List buffer) {
        assert buffer

        log.debug("Evaluating buffer...")

        if (verbose) {
            displayBuffer(buffer, true)
        }

        def source = buffer.join(NEWLINE)

        try {
            def script = shell.parse(source)

            log.debug("Compiled script: $script")
            
            //
            // TODO: Need smarter bits here to allow a simple class def w/o main() or run() muck...
            //

            def result = script.run()

            log.debug("Evaluation result: $result")

            if (verbose) {
                io.output.println("===> $result")
            }

            lastResult = result
        }
        catch (Throwable t) {
            log.debug("Evaluation failed: $t", t)

            // Unroll invoker exceptions
            if (t instanceof InvokerInvocationException) {
                t = t.cause
            }

            io.error.println(messages.format('info.error', t))
            t.printStackTrace(io.error)
        }
    }

    /**
     * Format the given number suitable for rendering as a line number column.
     */
    private String formatLineNumber(final int num) {
        assert num >= 0
        
        // Make a %03d-like string for the line number
        def lineNum = num.toString()
        return lineNum.padLeft(3, '0')
    }

    /**
     * Display the given buffer.
     */
    private void displayBuffer(final List buffer, final boolean lineNumbers) {
        assert buffer

        if (lineNumbers) {
            buffer.eachWithIndex { line, index ->
                def lineNum = formatLineNumber(index + 1)
                io.output.println("${lineNum}> $line")
            }
        }
        else {
            buffer.each { line ->
                io.output.println("> $line")
            }
        }
    }

    //
    // Commands
    //

    private void doHelpCommand(args) {
        // Figure out the max command name length dynamically
        int maxlen = 0
        registry.commands.each {
            if (it.name.size() > maxlen) maxlen = it.name.size()
        }

        io.output.println('For information about Groovy, visit:') // TODO: i18n
        io.output.println('    http://groovy.codehaus.org')
        io.output.println()

        io.output.println('Available commands:') // TODO: i18n

        registry.commands.each {
            def name = it.name.padRight(maxlen, ' ')
            io.output.println("  ${name}  ($it.shortcut) $it.description")
        }
    }

    private void doExitCommand(args) {
        if (verbose) {
            io.output.println('Bye') // TODO: i18n
        }
        
        exit(0)
    }

    private void doDisplayCommand(args) {
        if (buffer.isEmpty()) {
            io.output.println('Buffer is empty') // TODO: i18n
            return
        }

        displayBuffer(buffer, true)
    }

    private void doVariablesCommand(args) {
        def vars = shell.context.variables

        if (vars.isEmpty()) {
            io.output.println('No variables defined') // TODO: i18n
            return
        }
        
        io.output.println('Variables:') // TODO: i18n
        vars.each { key, value ->
            io.output.println("  $key = $value")
        }
    }

    private void doClearCommand(args) {
        buffer.clear()

        if (verbose) {
            io.output.println('Buffer cleared') //  TODO: i18n
        }
    }

    private void doInspectCommand(args) {
        if (lastResult == null) {
            io.output.println('Last result is null; nothing to inspect') // TODO: i18n
            return
        }

        if (verbose) {
            io.output.println("Launching object browser to inspect: $lastResult") // TODO: i18n
        }
        
        ObjectBrowser.inspect(lastResult);
    }

    private void doPurgeCommand(args) {
        shell.resetLoadedClasses()

        if (verbose) {
            io.output.println('Purged loaded classes') // TODO: i18n
        }
    }

    //
    // Command-line entry point
    //

    static void main(String[] args) {
        int code = new InteractiveShell().run(args)

        // Force the JVM to exit at this point, since shell could have created threads or
        // popped up Swing components that will cause the JVM to linger after we have been
        // asked to shutdown
        System.exit(code)
    }
}
