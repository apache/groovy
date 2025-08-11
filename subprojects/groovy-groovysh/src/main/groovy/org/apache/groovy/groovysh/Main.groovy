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

import groovy.cli.internal.CliBuilderInternal
import groovy.cli.internal.OptionAccessor
import org.apache.groovy.groovysh.jline.GroovyBuiltins
import org.apache.groovy.groovysh.jline.GroovyCommands
import org.apache.groovy.groovysh.jline.GroovyConsoleEngine
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.groovysh.jline.GroovySystemRegistry
import org.apache.groovy.groovysh.util.DocFinder
import org.codehaus.groovy.tools.shell.util.MessageSource
import org.jline.builtins.ClasspathResourceUtil
import org.jline.builtins.Completers
import org.jline.builtins.ConfigurationPath
import org.jline.builtins.Options
import org.jline.builtins.PosixCommands
import org.jline.builtins.PosixCommandsRegistry
import org.jline.builtins.SyntaxHighlighter
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.CommandRegistry
import org.jline.console.ConsoleEngine
import org.jline.console.Printer
import org.jline.console.impl.DefaultPrinter
import org.jline.console.impl.JlineCommandRegistry
import org.jline.console.impl.SystemHighlighter
import org.jline.keymap.KeyMap
import org.jline.reader.Binding
import org.jline.reader.Completer
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReader.Option
import org.jline.reader.LineReaderBuilder
import org.jline.reader.Reference
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.DefaultParser.Bracket
import org.jline.reader.impl.completer.ArgumentCompleter
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.Size
import org.jline.terminal.Terminal
import org.jline.terminal.Terminal.Signal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.InfoCmp.Capability
import org.jline.utils.OSUtils
import org.jline.widget.AutosuggestionWidgets
import org.jline.widget.TailTipWidgets
import org.jline.widget.TailTipWidgets.TipType
import org.jline.widget.Widgets

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function
import java.util.function.Supplier

import static org.jline.jansi.AnsiRenderer.render

/**
 * Groovy Repo modelled on JLine3 Groovy Repl demo
 */
class Main {
    private static final MessageSource messages = new MessageSource(Main)
    public static final String INTERPRETER_MODE_PREFERENCE_KEY = 'interpreterMode'
    private static POSIX_FILE_CMDS = ['/tail', '/ls', '/head', '/grep', '/wc', '/sort', '/cat']

    @SuppressWarnings("resource")
    protected static class ExtraConsoleCommands extends JlineCommandRegistry implements CommandRegistry {
        private final LineReader reader
        private final Supplier<Path> workDir
        private final Map<String, Object> variables
        private final PosixCommandsRegistry posix
        private final Map<String, String[]> usage = [:]

        ExtraConsoleCommands(Supplier<Path> workDir, Map<String, Object> variables, LineReader reader) {
            super()
            this.workDir = workDir
            this.variables = variables
            this.reader = reader
            def terminal = reader.terminal
            posix = new PosixCommandsRegistry(
                terminal.input(),
                new PrintStream(terminal.output()),
                new PrintStream(terminal.output()),
                workDir.get(),
                terminal,
                variables::get)
            def cmds = [
                '/clear': new CommandMethods((Function) this::clear, this::defaultCompleter),
                '/echo' : new CommandMethods((Function) this::echo, this::defaultCompleter),
                "/!"    : new CommandMethods((Function) this::shell, this::defaultCompleter)
            ]
            POSIX_FILE_CMDS.each { String cmd ->
                String orig = cmd[1..-1]
                usage[cmd] = adjustUsage(orig, cmd)
                posix.register(cmd, PosixCommands::"$orig")
                cmds.put(cmd, new CommandMethods((Function) this::posix, this::optFileCompleter))
            }
            registerCommands(cmds)
        }

        private String[] adjustUsage(String from, String to) {
            try {
                posix.execute(from, [from, '--help'] as String[])
            } catch (Options.HelpException e) {
                e.message.readLines()*.replaceAll("$from ", "$to ") as String[]
            }
        }

        @Override
        String name() {
            'Console Commands'
        }

        private Terminal terminal() {
            return reader?.terminal
        }

        private List<Completer> optFileCompleter(String command) {
            [new ArgumentCompleter(NullCompleter.INSTANCE, new Completers.OptionCompleter(new Completers.FilesCompleter(workDir), this::commandOptions, 1))]
        }

        private void posix(CommandInput input) {
            try {
                parseOptions(usage[input.command()], input.args())
                posix.execute(input.command(), [input.command(), *input.args()] as String[])
            } catch (Exception e) {
                saveException(e)
            }
        }

        private void clear(CommandInput input) {
            final String[] usage = [
                "/clear -  clear terminal",
                "Usage: /clear",
                "  -? --help                       Displays command help"
            ]
            try {
                parseOptions(usage, input.args())
                terminal().puts(Capability.clear_screen)
                terminal().flush()
            } catch (Exception e) {
                saveException(e)
            }
        }

        private void echo(CommandInput input) {
            final String[] usage = [
                "/echo - echos a value",
                "Usage:  /echo [-h] <args>",
                "  -? --help                        Displays command help",
            ]
            try {
                Options opt = parseOptions(usage, input.args())
                if (!opt.args().isEmpty()) {
                    terminal().writer().println(String.join(" ", opt.args()))
                }
            } catch (Exception e) {
                saveException(e)
            }
        }

        private static void executeCommand(List<String> args) throws Exception {
            def sout = new StringBuilder(), serr = new StringBuilder()
            def command = OSUtils.IS_WINDOWS ? ['cmd.exe', '/c'] : ['sh', '-c']
            def proc = new ProcessBuilder().command(command + args.join(' ')).start()
            proc.consumeProcessOutput(sout, serr)
            int exitCode = proc.waitFor()
            if (sout.size()) print sout
            if (exitCode != 0) {
                if (serr.size()) print serr
                throw new Exception("Error occurred in shell!")
            }
        }

        private void shell(CommandInput input) {
            final String[] usage = [
                "/!<command> -  execute shell command",
                "Usage: /!<command>",
                "  -? --help                       Displays command help"
            ]
            if (input.args().length == 1 && (input.args()[0].equals("-?") || input.args()[0].equals("--help"))) {
                try {
                    parseOptions(usage, input.args())
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

    }

    static Path getUserStateDirectory() {
        Path.of(System.getProperty('user.home'), '.groovy').tap { groovyHome ->
            if (!groovyHome) {
                Files.createDirectories(groovyHome)
            }
        }
    }

    static void main(String[] args) {
        def cli = new CliBuilderInternal(usage: 'groovysh [options] [...]', stopAtNonOption: false,
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
            pr(longOpt: 'enable-preview', messages['cli.option.enable.preview.description'])
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
            println render(messages.format('cli.info.version', GroovySystem.version))
            System.exit(0)
        }

        try {
            Supplier<Path> workDir = () -> Paths.get(System.getProperty('user.dir'))
            DefaultParser parser = new DefaultParser(
                regexCommand: /\/?[a-zA-Z!]\S*/,
                eofOnUnclosedQuote: true,
                eofOnEscapedNewLine: true
            )
            parser.blockCommentDelims(new DefaultParser.BlockCommentDelims('/*', '*/'))
                .lineCommentDelims(new String[]{'//'})
                .setEofOnUnclosedBracket(Bracket.CURLY, Bracket.ROUND, Bracket.SQUARE)
            Terminal terminal = TerminalBuilder.builder().build()
            if (terminal.width == 0 || terminal.height == 0) {
                terminal.size = new Size(120, 40) // hard-coded terminal size when redirecting
            }
            Thread executeThread = Thread.currentThread()
            terminal.handle(Signal.INT, signal -> executeThread.interrupt())

            def rootURL = Main.getResource('/nanorc')
            Path root = ClasspathResourceUtil.getResourcePath(rootURL)
            ConfigurationPath configPath = new ConfigurationPath(root, userStateDirectory)

            // ScriptEngine and command registries
            GroovyEngine scriptEngine = new GroovyEngine()
            scriptEngine.put('ROOT', rootURL.toString())
            scriptEngine.put('CONSOLE_OPTIONS', [:])
            def interpreterMode = Boolean.parseBoolean(System.getProperty("groovysh.interpreterMode", "true"))
            scriptEngine.put('GROOVYSH_OPTIONS', [interpreterMode: interpreterMode])
            Printer printer = new DefaultPrinter(scriptEngine, configPath)

            scriptEngine.put(GroovyEngine.NANORC_VALUE, rootURL.toString())
            Path jnanorc = root.resolve('jnanorc')
            def commandHighlighter = SyntaxHighlighter.build(jnanorc, "COMMAND")
            def argsHighlighter = SyntaxHighlighter.build(jnanorc, "ARGS")
            def groovyHighlighter = SyntaxHighlighter.build(jnanorc, "Groovy")

            CommandRegistry groovy = new GroovyCommands(scriptEngine, workDir, printer, groovyHighlighter)

            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(parser)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                .variable(LineReader.INDENTATION, 2)
                .variable(LineReader.LIST_MAX, 100)
                .variable(LineReader.HISTORY_FILE, configPath.getUserConfig('groovysh_history', true))
                .option(Option.INSERT_BRACKET, true)
                .option(Option.EMPTY_WORD_OPTIONS, false)
                .option(Option.USE_FORWARD_SLASH, true)
                .option(Option.DISABLE_EVENT_EXPANSION, true)
                .build()
            if (OSUtils.IS_WINDOWS) {
                reader.setVariable(
                    LineReader.BLINK_MATCHING_PAREN, 0) // if enabled cursor remains in begin parenthesis (gitbash)
            }

            def extra = new ExtraConsoleCommands(workDir, scriptEngine.variables, reader)

            ConsoleEngine consoleEngine = new GroovyConsoleEngine(scriptEngine, printer, workDir, configPath, reader)
            consoleEngine.setConsoleOption('docs', new DocFinder())

            CommandRegistry builtins = new GroovyBuiltins(scriptEngine, workDir, configPath, reader, (String fun) ->
                new ConsoleEngine.WidgetCreator(consoleEngine, fun)
            )

            GroovySystemRegistry systemRegistry = new GroovySystemRegistry(parser, terminal, workDir, configPath).tap {
                groupCommandsInHelp(false)
                setCommandRegistries(extra, consoleEngine, builtins, groovy)
                addCompleter(scriptEngine.scriptCompleter)
                setScriptDescription(scriptEngine::scriptDescription)
                renameLocal 'exit', '/exit'
                renameLocal 'help', '/help'
                invoke '/alias', '/x', '/exit'
                invoke '/alias', '/q', '/exit'
                invoke '/alias', '/h', '/help'
            }

            def highlighter = new SystemHighlighter(commandHighlighter, argsHighlighter, groovyHighlighter).tap {
                if (!OSUtils.IS_WINDOWS) {
                    setSpecificHighlighter("/!", SyntaxHighlighter.build(jnanorc, "SH-REPL"))
                }
                addFileHighlight('/nano', '/less', '/slurp', '/load', '/save', *POSIX_FILE_CMDS)
                addFileHighlight('/classloader', null, ['-a', '--add'])
                addExternalHighlighterRefresh(printer::refresh)
                addExternalHighlighterRefresh(scriptEngine::refresh)
            }

            reader.highlighter = highlighter
            reader.completer = systemRegistry.completer()

            // widgets and console initialization
            new TailTipWidgets(reader, systemRegistry::commandDescription, 5, TipType.COMPLETER)
            new AutosuggestionWidgets(reader)
            KeyMap<Binding> keyMap = reader.keyMaps.get("main")
            keyMap.bind(new Reference(Widgets.TAILTIP_TOGGLE), KeyMap.alt("s"))
            keyMap.bind(new Reference(Widgets.AUTOSUGGEST_TOGGLE), KeyMap.alt("v"))
            def init = configPath.getUserConfig('groovysh_init')
            if (init) {
                systemRegistry.setConsoleOption() // initialize(configPath.getUserConfig('groovysh_init').toFile())
            }

            if (options.q) {
                println render(messages.format('cli.info.version', GroovySystem.version))
            } else {
                println render(messages.format('startup_banner.0', GroovySystem.version, System.properties['java.version'], terminal.type))
                println render(messages['startup_banner.1'])
                println render(messages['startup_banner.2'])
            }
            println '-' * (terminal.width - 1)
// for debugging
//            def index = 0
//            def lines = ['/slurp /Users/paulk/Projects/groovy/subprojects/groovy-json/src/test/resources/groovy9802.json',
//                         'println _',
//                         'x = /slurp /Users/paulk/Projects/groovy/subprojects/groovy-json/src/test/resources/groovy9802.json',
//                        'println x',
//                         '/q']
            // REPL-loop
            while (true) {
                try {
                    systemRegistry.cleanUp() // delete temporary variables and reset output streams
// for debugging
//                    String line = lines[index++]
                    String line = reader.readLine("groovy> ")
                    line = line.readLines().collect{ s ->
                        // remove Groovy continuation character for repl not Groovy's sake
                        s.endsWith(' \\') ? s[0..-3] : s
                    }.join('\n')
                    if (line.startsWith(':')) {
                        def maybeCmd = line.split()[0].replaceFirst(':', '/')
                        if (systemRegistry.hasCommand(maybeCmd) || systemRegistry.isCommandAlias(maybeCmd)) {
                            line = line.replaceFirst(':', '/')
                        }
                    }
                    Object result = systemRegistry.execute(line)
                    consoleEngine.println(result?.toString())
//                    consoleEngine.println([(Printer.OBJECT_TO_STRING): [(Object) : {  o -> o.toString() }] ], result)
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    String pl = e.getPartialLine()
                    if (pl != null) { // execute last line from redirected file (required for Windows)
                        try {
                            consoleEngine.println(systemRegistry.execute(pl))
                        } catch (Exception e2) {
                            systemRegistry.trace(e2)
                        }
                    }
                    break
                } catch (Exception | Error e) {
                    systemRegistry.trace(e) // print exception and save it to console variable
                }
            }
            systemRegistry.close() // persist pipeline completer names etc

            boolean groovyRunning = Thread.getAllStackTraces().keySet().any { it.name.startsWith("AWT-Shut") }
            if (groovyRunning) {
                consoleEngine.println("Please, close Groovy Consoles/Object Browsers!")
            }
        } catch (Throwable t) {
            t.printStackTrace()
        }
    }
}
