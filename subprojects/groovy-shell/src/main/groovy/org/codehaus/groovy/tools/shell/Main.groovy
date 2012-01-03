/*
 * Copyright 2003-2011 the original author or authors.
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

import org.codehaus.groovy.tools.shell.util.HelpFormatter
import org.codehaus.groovy.tools.shell.util.Logger
import org.codehaus.groovy.tools.shell.util.MessageSource
import org.codehaus.groovy.tools.shell.util.NoExitSecurityManager
import java.util.concurrent.Callable
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import jline.Terminal

/**
 * Main CLI entry-point for <tt>groovysh</tt>.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class Main
{
    static {
        // Install the system adapters
        AnsiConsole.systemInstall()

        // Register jline ansi detector
        Ansi.setDetector(new AnsiDetector())
    }

    private static final MessageSource messages = new MessageSource(Main.class)

    static void main(final String[] args) {
        IO io = new IO()
        Logger.io = io

        def cli = new CliBuilder(usage : 'groovysh [options] [...]', formatter: new HelpFormatter(), writer: io.out)

        cli.classpath(messages['cli.option.classpath.description'])
        cli.cp(longOpt: 'classpath', messages['cli.option.cp.description'])
        cli.h(longOpt: 'help', messages['cli.option.help.description'])
        cli.V(longOpt: 'version', messages['cli.option.version.description'])
        cli.v(longOpt: 'verbose', messages['cli.option.verbose.description'])
        cli.q(longOpt: 'quiet', messages['cli.option.quiet.description'])
        cli.d(longOpt: 'debug', messages['cli.option.debug.description'])
        cli.C(longOpt: 'color', args: 1, argName: 'FLAG', optionalArg: true, messages['cli.option.color.description'])
        cli.D(longOpt: 'define', args: 1, argName: 'NAME=VALUE', messages['cli.option.define.description'])
        cli.T(longOpt: 'terminal', args: 1, argName: 'TYPE', messages['cli.option.terminal.description'])

        def options = cli.parse(args)

        if (options.h) {
            cli.usage()
            System.exit(0)
        }

        if (options.V) {
            io.out.println(messages.format('cli.info.version', GroovySystem.version))
            System.exit(0)
        }

        if (options.hasOption('T')) {
            def type = options.getOptionValue('T')
            setTerminalType(type)
        }

        if (options.hasOption('D')) {
            def values = options.getOptionValues('D')

            values.each {
                setSystemProperty(it as String)
            }
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

        if (options.hasOption('C')) {
            def value = options.getOptionValue('C')
            setColor(value)
        }

        def code

        // Add a hook to display some status when shutting down...
        addShutdownHook {
            //
            // FIXME: We need to configure JLine to catch CTRL-C for us... Use gshell-io's InputPipe
            //

            if (code == null) {
                // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                // will set an exit code through the proper channels

                io.err.println()
                io.err.println('@|red WARNING:|@ Abnormal JVM shutdown detected')
            }

            io.flush()
        }

        // Boot up the shell... :-)
        Groovysh shell = new Groovysh(io)

        SecurityManager psm = System.getSecurityManager()
        System.setSecurityManager(new NoExitSecurityManager())

        try {
            code = shell.run(options.arguments() as String[])
        }
        finally {
            System.setSecurityManager(psm)
        }

        // Force the JVM to exit at this point, since shell could have created threads or
        // popped up Swing components that will cause the JVM to linger after we have been
        // asked to shutdown

        System.exit(code)
    }

    static void setTerminalType(String type) {
        assert type != null
        
        type = type.toLowerCase();

        switch (type) {
            case 'auto':
                type = null;
                break;

            case 'unix':
                type = 'jline.UnixTerminal'
                break

            case 'win':
            case 'windows':
                type = 'jline.WindowsTerminal'
                break

            case 'false':
            case 'off':
            case 'none':
                type = 'jline.UnsupportedTerminal'
                // Disable ANSI, for some reason UnsupportedTerminal reports ANSI as enabled, when it shouldn't
                Ansi.enabled = false
                break;
        }

        if (type != null) {
            System.setProperty('jline.terminal', type)
        }
    }

    static void setColor(value) {
        if (value == null) {
            value = true // --color is the same as --color=true
        }
        else {
            value = Boolean.valueOf(value).booleanValue(); // For JDK 1.4 compat
        }

        Ansi.enabled = value
    }

    static void setSystemProperty(final String nameValue) {
        String name
        String value

        if (nameValue.indexOf('=') > 0) {
            def tmp = nameValue.split('=', 2)
            name = tmp[0]
            value = tmp[1]
        }
        else {
            name = nameValue
            value = Boolean.TRUE.toString()
        }

        System.setProperty(name, value)
    }
}

class AnsiDetector
    implements Callable<Boolean>
{
    public Boolean call() throws Exception {
        return Terminal.getTerminal().isANSISupported()
    }
}
