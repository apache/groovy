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

import jline.Terminal
import jline.History

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.InvokerInvocationException
import org.codehaus.groovy.runtime.MethodClosure

import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationFailedException

import org.codehaus.groovy.tools.shell.util.ANSI
import org.codehaus.groovy.tools.shell.util.HelpFormatter
import org.codehaus.groovy.tools.shell.util.Logger
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
    
    private static final MessageSource messages = new MessageSource(Groovysh.class)
    
    private final GroovyShell interp
    
    private final BufferManager buffers = new BufferManager()

    private final List imports = []
    
    private InteractiveShellRunner runner
    
    private History history
    
    Groovysh(final ClassLoader classLoader, final Binding binding, final IO io) {
        super(io)
        
        assert classLoader
        assert binding
        
        interp = new GroovyShell(classLoader, binding)
        
        def registrar = new CommandRegistrar(this, classLoader)
        registrar.register(getClass().getResource('commands.xml'))
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
            io.out.println("@|bold ===>| $obj")
        }

        interp.context['_'] = obj
    }
    
    File getUserStateDirectory() {
        def userHome = new File(System.properties['user.home'])
        def dir = new File(userHome, '.groovy')
        return dir.canonicalFile
    }
    
    private Object getLastResult() {
        return interp.context['_']
    }
    
    private ANSI.Renderer prompt = new ANSI.Renderer()
    
    private String renderPrompt() {
        def lineNum = formatLineNumber(buffers.current().size())
        
        return prompt.render("@|bold groovy:|(${buffers.selected})@|bold :|${lineNum}@|bold >| ")
    }
    
    protected Object executeCommand(final String line) {
        return super.execute(line)
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
        
        def result
        
        // First try normal command execution
        if (isExecutable(line)) {
            result = executeCommand(line)
            
            // For commands, only set the last result when its non-null/true
            if (result) {
                lastResult = result
            }
            
            return result
        }
        
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
                    displayError(status.cause)
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
            parser = SourceUnit.create('groovysh_parse', source, tolerance)
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

    private static final String EVAL_SCRIPT_FILENAME = 'groovysh_evaluate'

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
            Script script = interp.parse(source, EVAL_SCRIPT_FILENAME)
            type = script.getClass()

            log.debug("Compiled script: $script")

            if (type.declaredMethods.any { it.name == 'main' }) {
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
        /*
        catch (Throwable t) {
            // Unroll invoker exceptions
            if (t instanceof InvokerInvocationException) {
                t = t.cause
            }

            throw t
        }
        */
        finally {
            def cache = interp.classLoader.classCache
            
            // Remove the script class generated
            cache.remove(type?.name)

            // Remove the inline closures from the cache as well
            cache.remove('$_run_closure')
        }
        
        return result
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
            
            io.out.println(" ${lineNum}@|bold >| $line")
        }
    }
    
    private void loadUserScript(final String filename) {
        assert filename
        
        def file = new File(userStateDirectory, filename)
        
        if (file.exists()) {
            log.debug("Loading user-script: $file")
            
            execute("load ${file.toURL()}")
        }
    }
    
    private void displayError(final Throwable cause) {
        assert cause != null
        
        io.err.println("@|bold,red ERROR| ${cause.class.name}: @|bold,red ${cause.message}|")
        
        if (log.debug) {
            // If we have debug enabled then skip the fancy bits below
            log.debug(cause)
        }
        else if (io.verbose) {
            def trace = cause.stackTrace
            def buff = new StringBuffer()
            
            for (e in trace) {
                buff << "        @|bold at| ${e.className}.${e.methodName} (@|bold "
                
                buff << (e.nativeMethod ? 'Native Method' : 
                            (e.fileName != null && e.lineNumber != -1 ? "${e.fileName}:${e.lineNumber}" :
                                (e.fileName != null ? e.fileName : 'Unknown Source')))
                
                buff << '|)'
                
                io.err.println(buff)
                
                buff.setLength(0) // Reset the buffer
                
                //
                // FIXME: Need to make sure this doesn't eat up other muck...
                //
                
                // Stop the trace once we find the root of the evaluated script
                if (e.className == EVAL_SCRIPT_FILENAME && e.methodName == 'run') {
                    io.err.println('        @|bold ...|')
                    break
                }
            }
        }
    }
    
    int run(final String[] args) {
        def term = Terminal.terminal
        log.debug("Using terminal: $term")
        
        def code
        
        try {
            loadUserScript('groovysh.profile')
            
            if (args != null && args.length > 0) {
                // Run the given commands
                execute(args.join(' '))
            }
            else {
                loadUserScript('groovysh.rc')
                
                // Setup the interactive runner
                runner = new InteractiveShellRunner(this, this.&renderPrompt as Closure)
                
                // Setup the history
                runner.history = history = new History()
                runner.historyFile = new File(userStateDirectory, 'groovysh.history')
                
                // Setup the error handler
                runner.errorHandler = this.&displayError
                
                //
                // TODO: See if we want to add any more language specific completions, like for println for example?
                //
                
                // Display the welcome banner
                if (!io.quiet) {
                    def width = term.terminalWidth
                    
                    // If we can't tell, or have something bogus then use a reasonable default
                    if (width < 1) {
                        width = 80
                    }
                    
                    io.out.println(messages.format('startup_banner.0', InvokerHelper.version, System.properties['java.vm.version']))
                    io.out.println(messages['startup_banner.1'])
                    io.out.println('-' * (width - 1))
                }
                
                // And let 'er rip... :-)
                runner.run()
            }
            
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
        
        assert code != null // This should never happen
        
        return code
    }

    static void main(final String[] args) {
        def io = new IO()
        Logger.io = io
        
        def cli = new CliBuilder(usage : 'groovysh [options] [...]', formatter: new HelpFormatter(), writer: io.out)

        cli.h(longOpt: 'help', messages['cli.option.help.description'])
        cli.V(longOpt: 'version', messages['cli.option.version.description'])
        cli.v(longOpt: 'verbose', messages['cli.option.verbose.description'])
        cli.q(longOpt: 'quiet', messages['cli.option.quiet.description'])
        cli.d(longOpt: 'debug', messages['cli.option.debug.description'])
        cli.C(longOpt: 'color', args: 1, argName: 'FLAG', optionalArg: true, messages['cli.option.color.description'])
        
        def options = cli.parse(args)

        if (options.h) {
            cli.usage()
            System.exit(0)
        }

        if (options.V) {
            io.out.println(messages.format('cli.info.version', InvokerHelper.version))
            System.exit(0)
        }
        
        if (options.v) {
            io.verbose = true
        }

        if (options.d) {
            Logger.debug = true
            io.verbose = true // --debug implies --verbose
        }
        
        if (options.q) {
            io.quiet = true
            io.verbose = false // --quiet implies !--verbose
            Logger.debug = false // --quiet implies !--debug
        }
        
        if (options.hasOption('C')) {
            def value = options.getOptionValue('C')
            
            if (value == null) {
                value = true // --color is the same as --color=true
            }
            else {
                value = Boolean.valueOf(value).booleanValue(); // For JDK 1.4 compat
            }
            
            ANSI.enabled = value;
        }
        
        def code
        
        // Add a hook to display some status when shutting down...
        addShutdownHook {
            //
            // FIXME: We need to configure JLine to catch CTRL-C for us... if that is possible
            //
            
            if (code == null) {
                // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                // will set an exit code through the proper channels
                
                io.err.println()
                io.err.println('@|red WARNING:| Abnormal JVM shutdown detected')
            }
            
            io.flush()
        }
        
        // Boot up the shell... :-)
        
        code = new Groovysh(io).run(options.arguments() as String[])

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
