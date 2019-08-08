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
package org.codehaus.groovy.tools.shell

import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import jline.TerminalFactory
import jline.UnixTerminal
import jline.UnsupportedTerminal
import jline.WindowsTerminal
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.MessageSource
import org.codehaus.groovy.tools.shell.util.NoExitSecurityManager
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole

import static org.apache.groovy.util.SystemUtil.setSystemPropertyFrom

/**
 * A Main instance has a Groovysh member representing the shell,
 * and a startGroovysh() method to run an interactive shell.
 * Subclasses should preferably extend createIO() or configure the shell
 * via getShell prior to invoking startGroovysh.
 * Clients may use configureAndStartGroovysh to provide the same CLI params
 * but a different Groovysh implementation (implementing getIO() and run()).
 *
 * The class also has static utility methods to manipulate the
 * static ansi state using the jAnsi library.
 *
 * Main CLI entry-point for <tt>groovysh</tt>.
 */
class Main {
    final Groovysh groovysh

    /**
     * @param io: may just be new IO(), which is the default
     */
    Main(IO io) {
        Logger.io = io
        groovysh = new Groovysh(io)
    }

    /**
     * @param io: may just be new IO(), which is the default
     */
    Main(IO io, CompilerConfiguration configuration) {
        Logger.io = io
        groovysh = new Groovysh(io, configuration)
    }

    /**
     * create a Main instance, configures it according to CLI arguments, and starts the shell.
     * @param main must have a Groovysh member that has an IO member.
     */
    static void main(final String[] args) {
        MessageSource messages = new MessageSource(Main)
        CliBuilder cli = new CliBuilder(usage: 'groovysh [options] [...]', stopAtNonOption: false,
                header: messages['cli.option.header'])
        cli.with {
            _(names: ['-cp', '-classpath', '--classpath'], messages['cli.option.classpath.description'])
            h(longOpt: 'help', messages['cli.option.help.description'])
            V(longOpt: 'version', messages['cli.option.version.description'])
            v(longOpt: 'verbose', messages['cli.option.verbose.description'])
            q(longOpt: 'quiet', messages['cli.option.quiet.description'])
            d(longOpt: 'debug', messages['cli.option.debug.description'])
            e(longOpt: 'evaluate', args: 1, argName: 'CODE', optionalArg: false, messages['cli.option.evaluate.description'])
            C(longOpt: 'color', args: 1, argName: 'FLAG', optionalArg: true, messages['cli.option.color.description'])
            D(longOpt: 'define', type: Map, argName: 'name=value', messages['cli.option.define.description'])
            T(longOpt: 'terminal', args: 1, argName: 'TYPE', messages['cli.option.terminal.description'])
            pa(longOpt: 'parameters', messages['cli.option.parameters.description'])
        }
        OptionAccessor options = cli.parse(args)

        if (options == null) {
            // CliBuilder prints error, but does not exit
            System.exit(22) // Invalid Args
        }

        if (options.h) {
            cli.usage()
            System.exit(0)
        }

        if (options.V) {
            System.out.println(messages.format('cli.info.version', GroovySystem.version))
            System.exit(0)
        }

        boolean suppressColor = false
        if (options.hasOption('C')) {
            def value = options.getOptionValue('C')
            if (value != null) {
                suppressColor = !Boolean.valueOf(value).booleanValue() // For JDK 1.4 compat
            }
        }

        String type = TerminalFactory.AUTO
        if (options.hasOption('T')) {
            type = options.getOptionValue('T')
        }
        try {
            setTerminalType(type, suppressColor)
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage())
            cli.usage()
            System.exit(22) // Invalid Args
        }

        // IO must be constructed AFTER calling setTerminalType()/AnsiConsole.systemInstall(),
        // else wrapped System.out does not work on Windows.
        IO io = new IO()

        if (options.hasOption('D')) {
            options.Ds.each { k, v -> System.setProperty(k, v) }
        }

        if (options.v) {
            io.verbosity = IO.Verbosity.VERBOSE
        }

        if (options.d) {
            io.verbosity = IO.Verbosity.DEBUG
        }

        if (options.q) {
            io.verbosity = IO.Verbosity.QUIET
        }

        String evalString = null
        if (options.e) {
            evalString = options.getOptionValue('e')
        }
        def configuration = new CompilerConfiguration(System.getProperties())
        configuration.setParameters((boolean) options.hasOption("pa"))

        List<String> filenames = options.arguments()
        Main main = new Main(io, configuration)
        main.startGroovysh(evalString, filenames)
    }

    /**
     * @param evalString commands that will be executed at startup after loading files given with filenames param
     * @param filenames files that will be loaded at startup
     */
    protected void startGroovysh(String evalString, List<String> filenames) {
        int code
        Groovysh shell = getGroovysh()

        // Add a hook to display some status when shutting down...
        addShutdownHook {
            //
            // FIXME: We need to configure JLine to catch CTRL-C for us... Use gshell-io's InputPipe
            //

            if (code == null) {
                // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                // will set an exit code through the proper channels

                println('WARNING: Abnormal JVM shutdown detected')
            }

            if (shell.history) {
                shell.history.flush()
            }
        }


        SecurityManager psm = System.getSecurityManager()
        System.setSecurityManager(new NoExitSecurityManager())

        try {
            code = shell.run(evalString, filenames)
        }
        finally {
            System.setSecurityManager(psm)
        }

        // Force the JVM to exit at this point, since shell could have created threads or
        // popped up Swing components that will cause the JVM to linger after we have been
        // asked to shutdown

        System.exit(code)
    }

    /**
     * @param type: one of 'auto', 'unix', ('win', 'windows'), ('false', 'off', 'none')
     * @param suppressColor only has effect when ansi is enabled
     */
    static void setTerminalType(String type, boolean suppressColor) {
        assert type != null

        type = type.toLowerCase()
        boolean enableAnsi = true
        switch (type) {
            case TerminalFactory.AUTO:
                type = null
                break
            case TerminalFactory.UNIX:
                type = UnixTerminal.canonicalName
                break
            case TerminalFactory.WIN:
            case TerminalFactory.WINDOWS:
                type = WindowsTerminal.canonicalName
                break
            case TerminalFactory.FALSE:
            case TerminalFactory.OFF:
            case TerminalFactory.NONE:
                type = UnsupportedTerminal.canonicalName
                // Disable ANSI, for some reason UnsupportedTerminal reports ANSI as enabled, when it shouldn't
                enableAnsi = false
                break
            default:
                // Should never happen
                throw new IllegalArgumentException("Invalid Terminal type: $type")
        }
        if (enableAnsi) {
            installAnsi() // must be called before IO(), since it modifies System.in
            Ansi.enabled = !suppressColor
        } else {
            Ansi.enabled = false
        }

        if (type != null) {
            System.setProperty(TerminalFactory.JLINE_TERMINAL, type)
        }
    }

    static void installAnsi() {
        // Install the system adapters, replaces System.out and System.err
        // Must be called before using IO(), because IO stores refs to System.out and System.err
        AnsiConsole.systemInstall()

        // Register jline ansi detector
        Ansi.setDetector(new AnsiDetector())
    }

    @Deprecated
    static void setSystemProperty(final String nameValue) {
        setSystemPropertyFrom(nameValue)
    }
}

