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

import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.groovysh.jline.GroovyPosixCommands
import org.apache.groovy.groovysh.jline.GroovyPosixContext
import org.apache.groovy.lang.annotation.Incubating
import org.jline.builtins.Completers
import org.jline.builtins.Options
import org.jline.builtins.PosixCommands
import org.jline.builtins.PosixCommandsRegistry
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.CommandRegistry
import org.jline.console.impl.JlineCommandRegistry
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.impl.completer.ArgumentCompleter
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.Terminal
import org.jline.utils.InfoCmp.Capability
import org.jline.utils.OSUtils

import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function

/**
 * Console commands that groovysh registers in addition to the Groovy and
 * JLine builtin registries: {@code /clear}, {@code /pwd}, {@code /cd},
 * {@code /ls}, {@code /cat}, {@code /grep}, {@code /head}, {@code /tail},
 * {@code /wc}, {@code /sort}, {@code /date}, {@code /echo} and {@code /!}.
 * <p>
 * Embedders that want the same command set without copying {@link Main}
 * can construct this class and pass the instance to
 * {@link GroovyshOptions.Builder#extraCommandRegistry(CommandRegistry)}
 * or to {@code SystemRegistry.setCommandRegistries}. {@link Main} still
 * registers it by default.
 *
 * @since 7.0.0
 */
@Incubating
@SuppressWarnings(['resource', 'deprecation'])
class ExtraConsoleCommands extends JlineCommandRegistry implements CommandRegistry {

    /**
     * POSIX-style commands implemented by {@link GroovyPosixCommands}.
     */
    static final List<String> POSIX_COMMANDS = ['/ls', '/wc', '/sort', '/head', '/tail', '/cat', '/grep'].asImmutable()

    private static final String[] CLEAR_USAGE = [
        '/clear -  clear terminal',
        'Usage: /clear',
        '  -? --help                       Displays command help'
    ]

    private static final String[] SHELL_USAGE = [
        '/!<command> -  execute shell command',
        'Usage: /!<command>',
        '  -? --help                       Displays command help'
    ]

    private final LineReader reader
    private final GroovyEngine scriptEngine
    private final Map<String, List<String>> commandInfos = new ConcurrentHashMap<>()
    private PosixCommandsRegistry posix

    /**
     * Creates the auxiliary console command registry used by the shell.
     *
     * @param workDir initial working directory
     * @param scriptEngine script engine backing the shell session
     * @param reader active line reader
     */
    ExtraConsoleCommands(Path workDir, GroovyEngine scriptEngine, LineReader reader) {
        super()
        this.scriptEngine = scriptEngine
        this.reader = reader
        def terminal = reader.terminal
        def context = new GroovyPosixContext(
            terminal.input(),
            new PrintStream(terminal.output()),
            new PrintStream(terminal.output()),
            workDir,
            terminal,
            scriptEngine::get
        )
        posix = new PosixCommandsRegistry(context)
        def cmds = [
            '/clear': new CommandMethods((Function) this::clear, this::defaultCompleter),
            '/pwd'  : new CommandMethods((Function) this::pwd, this::defaultCompleter),
            '/cd'   : new CommandMethods((Function) this::cd, this::optDirCompleter),
            '/date' : new CommandMethods((Function) this::date, this::defaultCompleter),
            '/echo' : new CommandMethods((Function) this::echo, this::defaultCompleter),
            "/!"    : new CommandMethods((Function) this::shell, this::defaultCompleter)
        ]
        POSIX_COMMANDS.each { String cmd ->
            String base = cmd[1..-1]
            posix.register(cmd, PosixCommands::"$base")
            cmds.put(cmd, new CommandMethods((Function) this::posixCommand, this::optFileCompleter))
        }
        posix.register('cd', PosixCommands::cd)
        posix.register('/cd', PosixCommands::cd)
        posix.register('/pwd', PosixCommands::pwd)
        posix.register('/date', PosixCommands::date)
        posix.register('/echo', PosixCommands::echo)
        registerCommands(cmds)
    }

    /**
     * Returns the current working directory tracked by the POSIX command context.
     *
     * @return the active working directory
     */
    Path currentDir() {
        posix.context.currentDir
    }

    private String[] adjustUsage(String from, String to) {
        try {
            posix.execute(from, [from, '--help'] as String[])
        } catch (Options.HelpException e) {
            e.message.readLines()*.replaceAll("$from ", "$to ") as String[]
        }
    }

    /**
     * Returns the help-group name used for the extra console commands.
     *
     * @return the console command group name
     */
    @Override
    String name() {
        'Console Commands'
    }

    private Terminal terminal() {
        return reader?.terminal
    }

    private List<Completer> optFileCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE, new Completers.OptionCompleter(new Completers.FilesCompleter(this::currentDir), this::commandOptions, 1))]
    }

    private List<Completer> optDirCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE, new Completers.OptionCompleter(new Completers.DirectoriesCompleter(this::currentDir), this::commandOptions, 1))]
    }

    private void pwd(CommandInput input) {
        posix(adjustUsage('pwd', '/pwd'), input)
    }

    private void cd(CommandInput input) {
        try {
            parseOptions(adjustUsage('cd', '/cd'), input.args())
            PosixCommands.cd(context(input), ['/cd', *input.args()] as String[], { Path newPath ->
                posix.context.currentDir = newPath
                scriptEngine.put('PWD', newPath)
            })
        } catch (Exception e) {
            saveException(e)
        }
    }

    private void posixCommand(CommandInput input) {
        try {
            String cmd = input.command()
            String name = cmd[1..-1]
            GroovyPosixCommands."$name"(context(input), [cmd, *input.xargs()] as Object[])
        } catch (Exception e) {
            saveException(e)
        }
    }

    private GroovyPosixContext context(CommandInput input) {
        GroovyPosixContext ctx = new GroovyPosixContext(input.in(), input.out(), input.err(),
            posix.context.currentDir(), input.terminal(), scriptEngine::get)
        ctx
    }

    private void date(CommandInput input) {
        posix(adjustUsage('date', '/date'), input)
    }

    private void posix(String[] usage, CommandInput input) {
        try {
            parseOptions(usage, input.args())
            PosixCommands."${input.command()[1..-1]}"(context(input), [input.command(), *input.args()] as String[])
        } catch (Exception e) {
            saveException(e)
        }
    }

    private void clear(CommandInput input) {
        try {
            parseOptions(CLEAR_USAGE, input.args())
            terminal().puts(Capability.clear_screen)
            terminal().flush()
        } catch (Exception e) {
            saveException(e)
        }
    }

    private void echo(CommandInput input) {
        posix(adjustUsage('echo', '/echo'), input)
    }

    private static void executeCommand(List<String> args) throws Exception {
        def sout = new StringBuilder(), serr = new StringBuilder()
        def command = OSUtils.IS_WINDOWS ? ['cmd.exe', '/c'] : ['sh', '-c']
        def proc = new ProcessBuilder().command(command + args.join(' ')).start()
        // waitForProcessOutput joins the two reader threads before returning;
        // consumeProcessOutput only starts them, and waitFor waits for the
        // process rather than for them, so the output could still be unread
        proc.waitForProcessOutput(sout, serr)
        int exitCode = proc.exitValue()
        if (sout.size()) print sout
        if (exitCode != 0) {
            if (serr.size()) print serr
            throw new Exception("Error occurred in shell!")
        }
    }

    private void shell(CommandInput input) {
        if (input.args().length == 1 && (input.args()[0].equals("-?") || input.args()[0].equals("--help"))) {
            try {
                parseOptions(SHELL_USAGE, input.args())
            } catch (Exception e) {
                saveException(e)
            }
        } else {
            List<String> argv = input.args().toList()
            if (!argv.isEmpty()) {
                try {
                    executeCommand(argv)
                } catch (Exception e) {
                    saveException(e)
                }
            }
        }
    }

    /**
     * Returns summary text for the specified command. The first entry is the
     * one-line description JLine renders beside the command in {@code /help}
     * and in completion candidates.
     *
     * @param command command name to describe
     * @return help lines for the command, empty when it is not one of ours
     */
    @Override
    List<String> commandInfo(String command) {
        commandInfos.computeIfAbsent(command) { String cmd ->
            String[] usage = usageFor(cmd)
            usage == null || usage.length == 0 ? [] : [summarise(usage[0])].asImmutable()
        }
    }

    private String[] usageFor(String command) {
        switch (command) {
            case '/clear' -> CLEAR_USAGE
            case '/!' -> SHELL_USAGE
            default -> hasCommand(command) ? adjustUsage(command[1..-1], command) : null
        }
    }

    /**
     * Reduces a usage headline such as {@code "/ls -  list files"} to the
     * description alone.
     */
    private static String summarise(String headline) {
        int dash = headline.indexOf(' -')
        (dash < 0 ? headline : headline.substring(dash + 2)).trim()
    }
}
