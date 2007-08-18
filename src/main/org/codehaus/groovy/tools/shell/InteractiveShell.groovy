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
    
    private final GroovyLog log = new GroovyLog(this.class)
    
    private final MessageSource messages = new MessageSource(this.class)
    
    private final GroovyShell shell

    private final IO io
    
    private final ConsoleReader reader
    
    private final CommandRegistry registry = new CommandRegistry()

    private final List buffer = []
    
    boolean verbose

    InteractiveShell(ClassLoader classLoader, Binding binding, IO io) {
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

    InteractiveShell(Binding binding, IO io) {
        this(null, binding, io)
    }

    InteractiveShell(IO io) {
        this(new Binding(), io)
    }
    
    InteractiveShell() {
        this(new IO())
    }
    
    private void registerCommands() {
        io.output.println('For information about Groovy, visit:') // TODO: i18n
        io.output.println('    http://groovy.codehaus.org')
        io.output.println()
        
        registry << new Command('help', '\\h', {
            // Figure out the max command name length dynamically
            int maxlen = 0
            registry.commands.each {
                if (it.name.size() > maxlen) maxlen = it.name.size()
            }
            
            io.output.println('Available commands:') // TODO: i18n
            
            registry.commands.each {
                /*
                FIXME: This is only supported on Java 5

                io.output.println(sprintf("%${maxlen}s (%s) %s", it.name, it.shortcut, it.description))
                */

                def name = it.name.padRight(maxlen, ' ')
                io.output.println("  ${name}  ($it.shortcut) $it.description")
            }
        })
        
        registry << new CommandAlias('?', '\\?', 'help')
        
        registry << new Command('reset', '\\r', {
            buffer.clear()
            
            if (verbose) {
                io.output.println('Buffer cleared')
            }
        })
    }
    
    int run(String[] args) {
        try {
            processCommandLine(args)
            run()
        }
        catch (Throwable t) {
            io.error.println(messages.format('info.fatal', t))
            t.printStackTrace()
            
            return 1
        }

        return 0
    }

    private void exit(int code) {
        log.debug("Exiting w/code: $code")
        io.flush()
        System.exit(code)
    }

    void processCommandLine(String[] args) {
        assert args != null

        log.debug("Processing command-line args: $args")
        
        def cli = new CliBuilder(usage : 'groovysh [options]', writer: io.output)
        
        cli.h(longOpt: 'help', messages['cli.option.help.description'])
        cli.V(longOpt: 'version', messages['cli.option.version.description'])
        cli.v(longOpt: 'verbose', messages['cli.option.verbose.description'])

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
    }

    private void displayBanner() {
        io.output.println(messages.format('startup_banner.0', InvokerHelper.version, System.properties['java.vm.version']))
        io.output.println(messages['startup_banner.1'])
    }

    /*
    FIXME: This is only supported on Java 5
    
    private static final String PROMPT_PATTERN = 'groovy:%03d> '

    private String getPrompt() {
        return sprintf(PROMPT_PATTERN, buffer.size())
    }
    */

    private String getPrompt() {
        // Make a %03d-like string for the line number
        def lineNum = buffer.size().toString()
        lineNum = lineNum.padLeft(3, '0')
        return "groovy:${lineNum}> "
    }

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
            
            // Process builtin commands
            def args = line.trim().tokenize()
            def command = registry.find(args[0])

            if (command) {
                log.debug("Executing command: $command; w/args: $args")
                command.execute(args)
            }
            else {
                // Append the line to the execution buffer
                buffer << line

                //
                // FIXME: Only append to the buffer if the current + line parse completes
                //
                
                def source = buffer.join(NEWLINE)

                // Attempt to parse the buffer
                if (parse(source, 1)) {
                    if (verbose) {
                        buffer.each {
                            io.output.println("> $it")
                        }
                    }

                    // Execute the buffer contents
                    try {
                        log.debug("Evaluating buffer...")

                        def script = shell.parse(source)
                        def result = script.run()

                        //
                        // TODO: Post-exectuion hook?
                        //
                        
                        log.debug("Evaluation result: $result")
                        
                        if (verbose) {
                            io.output.println("===> $result")
                        }
                    }
                    catch (Throwable t) {
                        //
                        // TODO: Failure hook?
                        //
                        
                        // Unroll invoker exceptions
                        if (t instanceof InvokerInvocationException) {
                            t = t.cause
                        }

                        io.error.println(messages.format('info.error', t))
                        t.printStackTrace(io.error)
                    }
                    finally {
                        // Reset the buffer
                        buffer.clear()
                    }
                }
            }
        }

        log.debug('Finished')
    }

    private boolean parse(String source, int tolerance) {
        assert source

        def parser
        def error // FIXME: Need to find a better way to report this

        try {
            parser = SourceUnit.create('groovysh-script', source, tolerance)
            parser.parse()
            
            return true
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

        return false
    }

    //
    // Command-line entry point
    //

    static void main(String[] args) {
        int result = new InteractiveShell().run(args)

        // Force the JVM to exit at this point, since shell could have created threads or
        // popped up Swing components that will cause the JVM to linger after we have been
        // asked to shutdown
        System.exit(result)
    }
}