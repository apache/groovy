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

import java.lang.reflect.Method

import groovy.text.MessageSource

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.codehaus.groovy.runtime.MethodClosure

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationFailedException

import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.AnsiUtils as ANSI
import org.codehaus.groovy.tools.shell.util.XmlCommandRegistrar as CommandRegistrar

/**
 * An interactive shell for evaluating Groovy code from the command-line (aka. groovysh).
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Groovysh
    extends Shell
{
    private static final String NEWLINE = System.properties['line.separator']
    
    private final MessageSource messages = new MessageSource(this.class)
    
    private final InteractiveShellRunner runner
    
    private final GroovyShell interp
    
    private final BufferManager buffers = new BufferManager()

    private final List imports = []
    
    Groovysh(final ClassLoader classLoader, final Binding binding, final IO io) {
        super(io)
        
        Logger.io = io
        
        //
        // FIXME: This stuff gets run before the logging config from the command-line can get used, so we loose them...
        //
        //        See HACK in main() below...
        //
        
        assert classLoader
        assert binding
        
        interp = new GroovyShell(classLoader, binding)
        
        //
        // NOTE: Command registration must be done after the shell is initialized and before the runner is created
        //
        
        def registrar = new CommandRegistrar(this, classLoader)
        registrar.register(getClass().getResource('commands.xml'))
        
        Closure prompt = this.&renderPrompt
        runner = new InteractiveShellRunner(this, prompt)
        
        log.debug('Initialized')
    }

    Groovysh(final Binding binding, final IO io) {
        this(Thread.currentThread().contextClassLoader, binding, io)
    }

    Groovysh(final IO io) {
        this(new Binding(), io)
    }
    
    Groovysh() {
        this(new IO())
    }
    
    private void setLastResult(final Object obj) {
        if (io.verbose) {
            io.out.println(ANSI.render("@|bold ===>| $obj"))
        }

        interp.context['_'] = obj
    }
    
    private Object getLastResult() {
        return interp.context['_']
    }
    
    private String renderPrompt() {
        def lineNum = formatLineNumber(buffers.current().size())
        
        return ANSI.render("@|bold groovy:|(${buffers.selected})@|bold :|${lineNum}@|bold >| ")
    }
    
    /**
     * Execute a single line, where the line may be a command or Groovy code (complete or incomplete).
     */
    Object execute(final String line) {
        assert line != null
        
        // Ignore empty lines
        if (line.trim().size() == 0) {
            return null
        }
        
        // First try normal command execution
        if (isExecutable(line)) {
            def result = super.execute(line)
            
            // For commands, only set the last result when its non-null/true
            if (result) {
                lastResult = result
            }
            
            return result
        }
        
        def result
        
        // Otherwise treat the line as Groovy
        def current = []
        current += buffers.current()

        // Append the line to the current buffer
        current << line

        // Attempt to parse the current buffer
        def status = parse(current, 1)

        switch (status.code) {
            case ParseStatus.COMPLETE:
                // Evaluate the current buffer
                result = evaluate(current)
                buffers.clearSelected()
                break

            case ParseStatus.INCOMPLETE:
                // Save the current buffer so user can build up complex muli-line code blocks
                buffers.updateSelected(current)
                break

            case ParseStatus.ERROR:
                // Show a simple compilation error, otherwise dump the full details
                if (status.cause instanceof CompilationFailedException) {
                    io.err.println(messages.format('info.error', status.cause.message))
                }
                else {
                    io.err.println(messages.format('info.error', status.cause))
                    status.cause.printStackTrace(io.err)
                }
                break

            default:
                // Should never happen
                throw new Error("Invalid parse status: $status.code")
        }
        
        return (lastResult = result)
    }
    
    /**
     * Attempt to parse the given buffer.
     */
    private ParseStatus parse(final List buffer, final int tolerance) {
        assert buffer

        String source = (imports + buffer).join(NEWLINE)

        log.debug("Parsing: $source")

        SourceUnit parser
        Throwable error

        try {
            parser = SourceUnit.create('groovysh-script', source, tolerance)
            parser.parse()

            log.debug('Parse complete')

            return new ParseStatus(ParseStatus.COMPLETE)
        }
        catch (CompilationFailedException e) {
            //
            // FIXME: Seems like failedWithUnexpectedEOF() is not always set as expected, as in:
            //
            // class a {               <--- is true here
            //    def b() {            <--- is false here :-(
            //
            
            if (parser.errorCollector.errorCount > 1 || !parser.failedWithUnexpectedEOF()) {
                //
                // HACK: Super insane hack... if we detect a syntax error, but the last line of the
                //       buffer ends with a '{', then ignore... and pretend its okay, cause it might be...
                //
                //       This seems to get around the problem with things like:
                //
                //       class a { def b() {
                //
                
                if (buffer[-1].trim().endsWith('{')) {
                    // ignore, this blows
                }
                else {
                    error = e
                }
            }
        }
        catch (Throwable e) {
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
    private Object evaluate(final List buffer) {
        assert buffer
        
        log.debug("Evaluating buffer...")

        if (io.verbose) {
            displayBuffer(buffer)
        }

        def source = (imports + buffer).join(NEWLINE)
        def result
        
        Class type
        try {
            Script script = interp.parse(source)
            type = script.getClass()

            log.debug("Compiled script: $script")

            if (isRunnableScript(type)) {
                result = script.run()
            }

            log.debug("Evaluation result: $result")

            // Keep only the methods that have been defined in the script
            type.declaredMethods.each { Method m ->
                if (!(m.name in [ 'main', 'run' ] || m.name.startsWith('super$') || m.name.startsWith('class$'))) {
                    log.debug("Saving method definition: $m")
                    interp.context["${m.name}"] = new MethodClosure(type.newInstance(), m.name)
                }
            }
        }
        catch (Throwable t) {
            log.debug("Evaluation failed: $t", t)

            // Unroll invoker exceptions
            if (t instanceof InvokerInvocationException) {
                t = t.cause
            }

            io.err.println(messages.format('info.error', t))
            t.printStackTrace(io.err)
        }
        finally {
            def cache = interp.classLoader.classCache
            
            // Remove the script class generated
            cache.remove(type?.name)

            // Remove the inline closures from the cache as well
            cache.remove('$_run_closure')
        }
        
        return result
    }

    private boolean isRunnableScript(final Class type) {
        assert type

        for (m in type.declaredMethods) {
            if (m.name == 'main') {
                return true
            }
        }

        return false
    }

    /**
     * Format the given number suitable for rendering as a line number column.
     */
    private String formatLineNumber(final int num) {
        assert num >= 0
        
        // Make a %03d-like string for the line number
        return num.toString().padLeft(3, '0')
    }
    
    /**
     * Display the given buffer.
     */
    private void displayBuffer(final List buffer) {
        assert buffer

        buffer.eachWithIndex { line, index ->
            def lineNum = formatLineNumber(index + 1)
            
            io.out.println(ANSI.render(" ${lineNum}@|bold >| $line"))
        }
    }

    //
    // Command-line Support
    //

    int run(final String[] args) {
        def code
        
        try {
            // Configure from command-line
            processCommandLine(args)
            
            // Add a hook to display some status when shutting down...
            addShutdownHook({
                if (code == null) {
                    // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                    // will set an exit code through the proper channels
                    
                    io.err.println()
                    io.err.println(ANSI.render('@|red WARNING:| Abnormal JVM shutdown detected'))
                    
                    io.flush()
                }
            })
            
            // Display the welcome banner
            io.out.println(ANSI.render(messages.format('startup_banner.0', InvokerHelper.version, System.properties['java.vm.version'])))
            io.out.println(ANSI.render(messages['startup_banner.1']))
            io.out.println('-' * (runner.reader.terminal.terminalWidth - 1)) // TODO: Check what the value is when its an unsupported terminal
            
            // Optionally load a user-specific rc file
            def userHome = new File(System.properties['user.home'])
            def file = new File(userHome, '.groovy/groovyshrc')
            if (file.exists()) {
                execute("load ${file.toURL()}")
            }
            
            // Start the interactive shell runner
            runner.run()
            
            code = 0
        }
        catch (ExitNotification n) {
            log.debug("Exiting w/code: ${n.code}")
            
            code = n.code
        }
        catch (Throwable t) {
            io.err.println(messages.format('info.fatal', t))
            t.printStackTrace(io.err)
            
            code = 1
        }
        finally {
            io.flush()
        }
        
        assert code != null
        
        return code
    }

    /**
     * Process command-line arguments.
     */
    private void processCommandLine(final String[] args) {
        assert args != null

        log.debug("Processing command-line args: $args")

        def cli = new CliBuilder(usage : 'groovysh [options]', writer: io.out)

        cli.h(longOpt: 'help', messages['cli.option.help.description'])
        cli.V(longOpt: 'version', messages['cli.option.version.description'])
        cli.v(longOpt: 'verbose', messages['cli.option.verbose.description'])
        cli.d(longOpt: 'debug', messages['cli.option.debug.description'])
        cli.C(longOpt: 'nocolor', messages['cli.option.nocolor.description'])
        
        //
        // TODO: Add --quiet
        //
        
        def options = cli.parse(args)
        assert options

        // Currently no arguments are allowed, so complain if there are any
        def additional = options.arguments()
        if (additional.size() != 0) {
            cli.usage()
            
            io.err.println(messages.format('cli.info.unexpected_args', additional.join(' ')))
            
            throw new ExitNotification(1)
        }

        if (options.h) {
            cli.usage()
            
            throw new ExitNotification(0)
        }

        if (options.V) {
            io.out.println(messages.format('cli.info.version', InvokerHelper.version))
            
            throw new ExitNotification(0)
        }

        if (options.v) {
            io.verbose = true
        }

        if (options.d) {
            Logger.debug = true
            
            // debug implies verbose
            io.verbose = true
        }
        
        if (options.C) {
            ANSI.ENABLED = false
        }
    }

    static void main(String[] args) {
        //
        // HACK: Quickly check args for -d or --debug, need a better way, but for now this will have to do
        //
        
        for (arg in args) {
            if (arg in [ '-d', '--debug' ]) {
                Logger.debug = true
                break
            }
        }
        
        //
        // HACK: Quickly check args for --C or --nocolor early too :-(
        //
        
        for (arg in args) {
            if (arg in [ '-C', '--nocolor' ]) {
                ANSI.ENABLED = false
                break
            }
        }
        
        int code = new Groovysh().run(args)

        // Force the JVM to exit at this point, since shell could have created threads or
        // popped up Swing components that will cause the JVM to linger after we have been
        // asked to shutdown
        
        System.exit(code)
    }
}

/**
 * Container for parse status details.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class ParseStatus
{
    static final int COMPLETE = 0

    static final int INCOMPLETE = 1

    static final int ERROR = 2

    final int code

    final Throwable cause

    ParseStatus(final int code, final Throwable cause) {
        assert code in [ COMPLETE, INCOMPLETE, ERROR ]

        this.code = code
        this.cause = cause
    }

    ParseStatus(final int code) {
        this(code, null)
    }

    ParseStatus(final Throwable cause) {
        this(ERROR, cause)
    }
}
