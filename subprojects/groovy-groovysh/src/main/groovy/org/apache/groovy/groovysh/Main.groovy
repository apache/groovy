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
import org.apache.groovy.groovysh.ExtraConsoleCommands as DefaultExtraConsoleCommands
import org.apache.groovy.groovysh.jline.GroovyBuiltins
import org.apache.groovy.groovysh.jline.GroovyCommands
import org.apache.groovy.groovysh.jline.GroovyConsoleEngine
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.groovysh.jline.GroovyPrinter
import org.apache.groovy.groovysh.jline.GroovySystemRegistry
import org.apache.groovy.groovysh.util.DocFinder
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.tools.shell.util.MessageSource
import org.jline.builtins.ClasspathResourceUtil
import org.jline.builtins.ConfigurationPath
import org.jline.builtins.SyntaxHighlighter
import org.jline.console.CommandRegistry
import org.jline.console.impl.CommandRegistryAdapter
import org.jline.console.ConsoleEngine
import org.jline.console.Printer
import org.jline.console.impl.SystemHighlighter
import org.jline.keymap.KeyMap
import org.jline.reader.Binding
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReader.Option
import org.jline.reader.LineReaderBuilder
import org.jline.reader.Reference
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.DefaultParser
import org.jline.reader.impl.DefaultParser.Bracket
import org.jline.terminal.Size
import org.jline.terminal.Terminal
import org.jline.terminal.Terminal.Signal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.OSUtils
import org.jline.widget.AutosuggestionWidgets
import org.jline.widget.TailTipWidgets
import org.jline.widget.TailTipWidgets.TipType
import org.jline.widget.Widgets

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.function.Consumer
import java.util.function.Supplier

import static org.jline.jansi.AnsiRenderer.render

/**
 * Boots and runs the interactive {@code groovysh} console.
 * <p>
 * Applications that embed groovysh should call {@link #start(GroovyshOptions, String[])}
 * (or the binding-only {@link #start(Map, String[])} overload) rather than
 * {@link #main(String[])}, which calls {@link System#exit(int)}.
 */
class Main {
    private static final MessageSource messages = new MessageSource(Main)
    /** Preference key that controls whether groovysh starts in interpreter mode. */
    public static final String INTERPRETER_MODE_PREFERENCE_KEY = 'interpreterMode'
    private static final List<String> GROOVY_POSIX_CMDS = DefaultExtraConsoleCommands.POSIX_COMMANDS

    /**
     * @deprecated since 7.0.0, use {@link DefaultExtraConsoleCommands}
     */
    @Deprecated(since = '7.0.0')
    protected static class ExtraConsoleCommands extends DefaultExtraConsoleCommands {
        ExtraConsoleCommands(Path workDir, GroovyEngine scriptEngine, LineReader reader) {
            super(workDir, scriptEngine, reader)
        }
    }

    /**
     * Returns the user state directory used by groovysh for persisted files.
     *
     * @return the user-specific state directory
     */
    static Path getUserStateDirectory() {
        Path.of(System.getProperty('user.home'), '.groovy').tap { groovyHome ->
            if (!Files.exists(groovyHome)) {
                createDirectoryOwnerOnly(groovyHome)
            }
        }
    }

    private static final Set<PosixFilePermission> FILE_OWNER_ONLY = PosixFilePermissions.fromString('rw-------')
    private static final Set<PosixFilePermission> DIRECTORY_OWNER_ONLY = PosixFilePermissions.fromString('rwx------')

    /**
     * Whether the default file system records POSIX permissions. Windows and other non-POSIX file
     * systems carry their own access control, which this code does not attempt to second-guess.
     */
    private static boolean posixFileSystem() {
        FileSystems.default.supportedFileAttributeViews().contains('posix')
    }

    private static void createDirectoryOwnerOnly(Path dir) {
        if (posixFileSystem()) {
            Files.createDirectories(dir, PosixFilePermissions.asFileAttribute(DIRECTORY_OWNER_ONLY))
        } else {
            Files.createDirectories(dir)
        }
    }

    /**
     * Ensures a file under the user state directory exists and is readable only by its owner.
     * <p>
     * These files record what was typed at the shell and the values it held, so on a shared host
     * they should not be left at whatever the ambient umask happens to permit. A file that already
     * exists is tightened only when it is currently more permissive than owner-only, so a
     * deliberately-chosen mode is left alone. Failing to adjust permissions is not fatal: the file
     * is still usable, and the shell should not refuse to start over it.
     *
     * @param file the state file
     * @return the same path, for chaining
     */
    static Path createOwnerOnlyStateFile(Path file) {
        try {
            Path parent = file.parent
            if (parent != null && !Files.exists(parent)) {
                createDirectoryOwnerOnly(parent)
            }
            if (!Files.exists(file)) {
                if (posixFileSystem()) {
                    Files.createFile(file, PosixFilePermissions.asFileAttribute(FILE_OWNER_ONLY))
                } else {
                    Files.createFile(file)
                }
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            // best effort: a state file we cannot create is reported by whoever needs to write it
        }
        tightenStateFile(file)
    }

    /**
     * The state files JLine writes into the same directory, which it creates at the ambient umask.
     */
    private static final List<String> JLINE_STATE_FILES = ['aliases.json', 'pipeline-names.json']

    /**
     * Restricts JLine's own state files to their owner once they exist.
     * <p>
     * JLine creates them through {@code ConfigurationPath.getUserConfig}, which calls
     * {@code Files.createFile} with no permission attributes; as of 4.4.1 that is still the case,
     * even though {@code DefaultHistory} was hardened for exactly this. They are not created here,
     * because handing JLine an empty file to parse as JSON would be worse than leaving the mode
     * alone. A file JLine creates during this session is therefore tightened on the next startup
     * rather than this one.
     * <p>
     * The lasting fix belongs upstream, in {@code getUserConfig} itself.
     */
    private static void tightenJLineStateFiles() {
        Path stateDirectory = userStateDirectory
        JLINE_STATE_FILES.each { name -> tightenStateFile(stateDirectory.resolve(name)) }
    }

    /**
     * Restricts an existing state file to its owner when it is currently more permissive, and does
     * nothing at all when the file is absent.
     * <p>
     * JLine warns about an over-permissive history file rather than changing it, so as not to
     * surprise the user. This is the one place groovysh goes further: the file records what was
     * typed at a shell prompt, the warning is easy to miss in a log, and the directory it lives in
     * belongs to groovysh. A mode stricter than owner-only is still left alone.
     *
     * @param file the state file
     * @return the same path, for chaining
     */
    static Path tightenStateFile(Path file) {
        try {
            if (posixFileSystem() && Files.exists(file)
                    && !FILE_OWNER_ONLY.containsAll(Files.getPosixFilePermissions(file))) {
                Files.setPosixFilePermissions(file, FILE_OWNER_ONLY)
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            // best effort: a state file we cannot tighten is still a usable state file
        }
        file
    }

    /**
     * Programmatic entry point for embedding groovysh.
     *
     * @param args CLI-like arguments (same as {@link #main(String[])}).
     * @param initialBindings binding variables for the GroovyEngine
     * @return process exit code (0 for success)
     *
     * @since 6.0.0
     */
    static int start(Map<String, ?> initialBindings = Collections.emptyMap(), String[] args = new String[0]) {
        def builder = GroovyshOptions.builder()
        if (initialBindings) {
            builder.bindings(initialBindings)
        }
        start(builder.build(), args)
    }

    /**
     * Programmatic entry point for embedding groovysh with compiler
     * configuration, extra command registries, prompt, result/error
     * handlers, and related hooks. Binding variables belong in
     * {@link GroovyshOptions#getBindings()}, not as ad-hoc map keys.
     *
     * @param groovyshOptions embedding options, must not be {@code null}
     * @param args CLI-like arguments (same as {@link #main(String[])})
     * @return process exit code (0 for success)
     *
     * @since 7.0.0
     */
    @Incubating
    static int start(GroovyshOptions groovyshOptions, String[] args = new String[0]) {
        Objects.requireNonNull(groovyshOptions, 'groovyshOptions')
        def cli = new CliBuilderInternal(usage: 'groovysh [options] [...]', stopAtNonOption: false,
            header: messages['cli.option.header'])
        cli.with {
            _(names: ['-cp', '-classpath', '--classpath'], messages['cli.option.classpath.description'])
            h(longOpt: 'help', messages['cli.option.help.description'])
            V(longOpt: 'version', messages['cli.option.version.description'])
            v(longOpt: 'verbose', messages['cli.option.verbose.description'])
            q(longOpt: 'quiet', messages['cli.option.quiet.description'])
            c(longOpt: 'encoding', args: 1, argName: 'CHARSET', optionalArg: false, messages['cli.option.encoding.description'])
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
            // CliBuilder prints error
            return 22 // Invalid Args
        }

        if (options.h) {
            cli.usage()
            return 0
        }

        if (options.V) {
            println render(messages.format('cli.info.version', GroovySystem.version))
            return 0
        }
        String evaluate = options.e ?: null

        try {
            DefaultParser parser = new DefaultParser(
                regexCommand: /\/?[a-zA-Z!]\S*/,
                eofOnUnclosedQuote: true,
                eofOnEscapedNewLine: true
            )
            parser.blockCommentDelims(new DefaultParser.BlockCommentDelims('/*', '*/'))
                .lineCommentDelims(new String[]{'//'})
                .setEofOnUnclosedBracket(Bracket.CURLY, Bracket.ROUND, Bracket.SQUARE)
            Terminal terminal = groovyshOptions.terminal
            if (terminal == null) {
                terminal = TerminalBuilder.builder().tap{
                    if (options.T) {
                        type(options.T)
                    }
                    if (options.c) {
                        encoding(options.c)
                    }
                    if (options.C) {
                        color(options.C as boolean)
                    }
                    name('groovysh')
                }.build()
            }
            if (terminal.columns == 0 || terminal.rows == 0) {
                terminal.size = new Size(120, 40) // hard-coded terminal size when redirecting
            }
            Thread executeThread = Thread.currentThread()
            terminal.handle(Signal.INT, signal -> executeThread.interrupt())

            def rootURL = Main.getResource('/nanorc')
            Path root = ClasspathResourceUtil.getResourcePath(rootURL)
            ConfigurationPath configPath = new ConfigurationPath(root, userStateDirectory)

            // ScriptEngine and command registries
            GroovyEngine scriptEngine = groovyshOptions.engine ?: new GroovyEngine(groovyshOptions.compilerConfiguration)

            groovyshOptions.bindings.each { k, v ->
                if (k != null) {
                    scriptEngine.put(k, v)
                }
            }

            scriptEngine.put('ROOT', rootURL.toString())
            if (!scriptEngine.hasVariable('CONSOLE_OPTIONS')) {
                scriptEngine.put('CONSOLE_OPTIONS', [:])
            }
            def interpreterMode = Boolean.parseBoolean(System.getProperty("groovysh.interpreterMode", "true"))
            scriptEngine.put('GROOVYSH_OPTIONS', [interpreterMode: interpreterMode])
            Printer printer = new GroovyPrinter(scriptEngine, configPath)

            scriptEngine.put(GroovyEngine.NANORC_VALUE, rootURL.toString())
            Path jnanorc = root.resolve('jnanorc')
            def commandHighlighter = SyntaxHighlighter.build(jnanorc, "COMMAND")
            def argsHighlighter = SyntaxHighlighter.build(jnanorc, "ARGS")
            def groovyHighlighter = SyntaxHighlighter.build(jnanorc, "Groovy")

            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(parser)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                .variable(LineReader.INDENTATION, 2)
                .variable(LineReader.LIST_MAX, 100)
                // Left for JLine to create: DefaultHistory creates the history file owner-only,
                // handles the concurrent-creation race, and falls back on non-POSIX file systems.
                // Creating it here first is what used to defeat that and leave it at the umask
                // default. Only an already-open file is adjusted.
                .variable(LineReader.HISTORY_FILE, groovyshOptions.historyFile != null
                    ? groovyshOptions.historyFile
                    : tightenStateFile(userStateDirectory.resolve('groovysh_history')))
                .option(Option.INSERT_BRACKET, true)
                .option(Option.EMPTY_WORD_OPTIONS, false)
                .option(Option.USE_FORWARD_SLASH, true)
                .option(Option.DISABLE_EVENT_EXPANSION, true)
                .build()
            if (OSUtils.IS_WINDOWS) {
                reader.setVariable(
                    LineReader.BLINK_MATCHING_PAREN, 0) // if enabled cursor remains in begin parenthesis (gitbash)
            }

            def extra = new DefaultExtraConsoleCommands(Paths.get(System.getProperty('user.dir')), scriptEngine, reader)
            Supplier<Path> workDir = extra::currentDir
            scriptEngine.put('PWD', workDir.get())

            CommandRegistry groovy = new GroovyCommands(scriptEngine, workDir, printer, groovyHighlighter)

            ConsoleEngine consoleEngine = new GroovyConsoleEngine(scriptEngine, printer, workDir, configPath, reader)
            consoleEngine.setConsoleOption('docs', new DocFinder())
            tightenJLineStateFiles()

            CommandRegistry builtins = new GroovyBuiltins(scriptEngine, workDir, configPath, reader, (String fun) ->
                new ConsoleEngine.WidgetCreator(consoleEngine, fun)
            )

            GroovySystemRegistry systemRegistry = new GroovySystemRegistry(parser, terminal, workDir, configPath).tap {
                groupCommandsInHelp(false)
                def registries = groovyshOptions.groups.collect { new CommandRegistryAdapter(it) as CommandRegistry }
                registries.addAll([extra, consoleEngine, builtins, groovy])
                setCommandRegistries(registries.toArray(new CommandRegistry[0]))
                addCompleter(scriptEngine.scriptCompleter)
                setScriptDescription(scriptEngine::scriptDescription)
                renameLocal 'exit', '/exit'
                renameLocal 'help', '/help'
                invoke '/alias', '/x', '/exit'
                invoke '/alias', '/q', '/exit'
                invoke '/alias', '/h', '/help'
                setConsoleOption "ignoreUnknownPipes", true
            }

            def highlighter = new SystemHighlighter(commandHighlighter, argsHighlighter, groovyHighlighter).tap {
                if (!OSUtils.IS_WINDOWS) {
                    setSpecificHighlighter("/!", SyntaxHighlighter.build(jnanorc, "SH-REPL"))
                }
                addFileHighlight('/nano', '/less', '/slurp', '/load', '/save', '/img', *GROOVY_POSIX_CMDS, '/cd')
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
            // last, so an embedder sees the reader fully wired — completer included
            groovyshOptions.onReaderReady?.accept(reader, systemRegistry)

            def init = configPath.getUserConfig('groovysh_init.groovy')
            if (init) {
                systemRegistry.initialize(init.toFile())
            }

            Supplier<String> prompt = groovyshOptions.prompt ?: ({ 'groovy> ' } as Supplier<String>)
            Supplier<String> rightPrompt = groovyshOptions.rightPrompt

            if (groovyshOptions.showBanner) {
                if (options.q) {
                    println render(messages.format('cli.info.version', GroovySystem.version))
                } else {
                    println render(messages.format('startup_banner.0', GroovySystem.version, System.properties['java.version'], terminal.type))
                    println render(messages['startup_banner.1'])
                    println render(messages['startup_banner.2'])
                }
                println '-' * (terminal.columns - 1)
            }
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
                    String line
                    if (evaluate) {
                        line = evaluate
                        evaluate = null
                    } else {
                        // for debugging
//                        line = lines[index++]
                        line = reader.readLine(prompt.get(), rightPrompt?.get(), (Character) null, null)
                    }
                    line = line.readLines().collect{ s ->
                        // remove Groovy continuation character for repl not Groovy's sake
                        s.endsWith(' \\') ? s[0..-3] : s
                    }.join('\n').replaceAll($/(/[a-z]+\s+)(https?://\S+)/$, '$1"$2"') // quote URLs after commands
                    if (line.startsWith(':')) {
                        // some simple legacy support for ':' commands
                        def maybeCmd = line.split()[0].replaceFirst(':', '/')
                        if (systemRegistry.hasCommand(maybeCmd) || systemRegistry.isCommandAlias(maybeCmd)) {
                            line = line.replaceFirst(':', '/')
                        }
                    }
                    Object result = systemRegistry.execute(line)
                    emitResult(printer, result, groovyshOptions.resultHandler)
//                    consoleEngine.println([(Printer.OBJECT_TO_STRING): [(Object) : {  o -> o.toString() }] ], result)
                } catch (UserInterruptException e) {
                    // Ignore
                } catch (EndOfFileException e) {
                    String pl = e.getPartialLine()
                    if (pl != null) { // execute last line from redirected file (required for Windows)
                        try {
                            emitResult(printer, systemRegistry.execute(pl), groovyshOptions.resultHandler)
                        } catch (Exception e2) {
                            emitError(systemRegistry, e2, groovyshOptions.errorHandler)
                        }
                    }
                    break
                } catch (Exception | Error e) {
                    emitError(systemRegistry, e, groovyshOptions.errorHandler)
                }
            }
            systemRegistry.close() // persist pipeline completer names etc

            boolean groovyRunning = Thread.getAllStackTraces().keySet().any { it.name.startsWith("AWT-Shut") }
            if (groovyRunning) {
                consoleEngine.println("Please, close Groovy Consoles/Object Browsers!")
            }
        } catch (Throwable t) {
            t.printStackTrace()
            return 1
        }
        return 0
    }

    /**
     * Programmatic entry point for embedding groovysh.
     *
     * @param args CLI-like arguments (same as {@link #main(String[])}).
     * @return process exit code (0 for success)
     *
     * @since 6.0.0
     */
    static int start(String[] args) {
        start(GroovyshOptions.builder().build(), args)
    }

    private static void emitResult(Printer printer, Object result, GroovyshOptions.ResultHandler handler) throws Exception {
        if (handler != null) {
            handler.handle(printer, result)
        } else {
            printer.println(result?.toString())
        }
    }

    private static void emitError(GroovySystemRegistry registry, Throwable error, GroovyshOptions.ErrorHandler handler) {
        Consumer<Throwable> defaultTrace = registry::trace
        if (handler != null) {
            handler.handle(error, defaultTrace)
        } else {
            defaultTrace.accept(error)
        }
    }

    /**
     * Launches groovysh as a standalone JVM process.
     *
     * @param args command-line arguments
     */
    static void main(String[] args) {
        System.exit(start(args))
    }
}
