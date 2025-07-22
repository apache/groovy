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
package org.apache.groovy.groovysh.jline

import groovy.console.ui.Console
import groovy.console.ui.ObjectBrowser
import groovy.toml.TomlSlurper
import groovy.xml.XmlParser
import groovy.yaml.YamlSlurper
import org.apache.groovy.groovysh.Main
import org.jline.builtins.Completers
import org.jline.builtins.Completers.OptDesc
import org.jline.builtins.Completers.OptionCompleter
import org.jline.builtins.SyntaxHighlighter
import org.jline.console.CmdDesc
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.CommandRegistry
import org.jline.console.Printer
import org.jline.console.ScriptEngine
import org.jline.console.impl.JlineCommandRegistry
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.reader.impl.completer.AggregateCompleter
import org.jline.reader.impl.completer.ArgumentCompleter
import org.jline.reader.impl.completer.NullCompleter
import org.jline.reader.impl.completer.StringsCompleter
import org.jline.utils.AttributedString

import java.awt.event.ActionListener
import java.lang.reflect.Method
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream

class GroovyCommands extends JlineCommandRegistry implements CommandRegistry {
    private static final String DEFAULT_NANORC_VALUE = 'gron'
    private final GroovyEngine engine
    private final Printer printer
    private final SyntaxHighlighter highlighter
    private final Map<String, Tuple4<Function, Function, Function, List<String>>> commands = [
        '/inspect'     : new Tuple4<>(this::inspect, this::inspectCompleter, this::inspectCmdDesc, ['display/browse object info on terminal/object browser']),
        '/console'     : new Tuple4<>(this::console, this::defCompleter, this::defCmdDesc, ['launch Groovy console']),
        '/grab'        : new Tuple4<>(this::grab, this::grabCompleter, this::grabCmdDesc, ['add maven repository dependencies to classpath']),
        '/classloader' : new Tuple4<>(this::classLoader, this::classloaderCompleter, this::classLoaderCmdDesc, ['display/manage Groovy classLoader data']),
        '/imports'     : new Tuple4<>(this::importsCommand, this::importsCompleter, this::nameDeleteCmdDesc, ['show/delete import statements']),
        '/vars'        : new Tuple4<>(this::varsCommand, this::varsCompleter, this::nameDeleteCmdDesc, ['show/delete variable declarations']),
        '/reset'       : new Tuple4<>(this::reset, this::defCompleter, this::defCmdDesc, ['clear the buffer']),
        '/load'        : new Tuple4<>(this::load, this::loadCompleter, this::loadCmdDesc, ['load state/a file into the buffer']),
        '/save'        : new Tuple4<>(this::save, this::saveCompleter, this::saveCmdDesc, ['save state/the buffer to a file']),
        '/slurp'       : new Tuple4<>(this::slurpcmd, this::slurpCompleter, this::slurpCmdDesc, ['slurp file or string variable context to object']),
        '/types'       : new Tuple4<>(this::typesCommand, this::typesCompleter, this::nameDeleteCmdDesc, ['show/delete types']),
        '/methods'     : new Tuple4<>(this::methodsCommand, this::methodsCompleter, this::nameDeleteCmdDesc, ['show/delete methods'])
    ]
    private boolean consoleUi
    private boolean ivy
    private Supplier<Path> workDir

    GroovyCommands(GroovyEngine engine, Supplier<Path> workDir, Printer printer, SyntaxHighlighter highlighter) {
        this.engine = engine
        this.printer = printer
        this.workDir = workDir
        this.highlighter = highlighter
        try {
            Class.forName('groovy.console.ui.ObjectBrowser')
            consoleUi = true
        } catch (Exception e) {
            // ignore
        }
        try {
            Class.forName('org.apache.ivy.util.Message')
            System.setProperty('groovy.grape.report.downloads', 'false')
            ivy = true
        } catch (Exception e) {
            // ignore
        }

        if (!consoleUi) {
            commands.remove('/console')
        }
        if (!ivy) {
            commands.remove('/grab')
        }
        def available = commands.collectEntries { name, tuple ->
            [name, new CommandMethods((Function)tuple.v1, tuple.v2)]
        }
        registerCommands(available)
    }

    @Override
    List<String> commandInfo(String command) {
        commands[command].v4
    }

    @Override
    CmdDesc commandDescription(List<String> args) {
        String command = args?[0] ?: ''
        commands[command].v3(command)
    }

    String name() {
        'Groovy Commands'
    }

    def grab(CommandInput input) {
        if (!input.xargs()) {
            return null
        }
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/grab')) return
        try {
            String arg = input.args()[0]
            if (arg == '-l' || arg == '--list') {
                Object resp = engine.execute('groovy.grape.Grape.instance.enumerateGrapes()')
                printer.println([
                    (Printer.VALUE_STYLE)         : engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE),
                    (Printer.INDENTION)           : 4,
                    (Printer.MAX_DEPTH)           : 1,
                    (Printer.SKIP_DEFAULT_OPTIONS): true
                ], resp)
            } else {
                int artifactId = 0
                if (input.args().length == 2) {
                    if (input.args()[0] == '-v' || input.args()[0] == '--verbose') {
                        System.setProperty('groovy.grape.report.downloads', 'true')
                        artifactId = 1
                    } else if (input.args()[1] == '-v' || input.args()[1] == '--verbose') {
                        System.setProperty('groovy.grape.report.downloads', 'true')
                    } else {
                        throw new IllegalArgumentException('Unknown command parameters!')
                    }
                }
                Map<String, String> artifact = [:]
                Object xarg = input.xargs()[artifactId]
                if (xarg instanceof String) {
                    String[] vals = input.args()[artifactId].split(':')
                    if (vals.length != 3) {
                        throw new IllegalArgumentException('Invalid command parameter: ' + input.args()[artifactId])
                    }
                    artifact.put('group', vals[0])
                    artifact.put('module', vals[1])
                    artifact.put('version', vals[2])
                } else if (xarg instanceof Map) {
                    artifact = (Map<String, String>) xarg
                } else {
                    throw new IllegalArgumentException('Unknown command parameter: ' + xarg)
                }
                engine.put('_artifact', artifact)
                engine.execute('groovy.grape.Grape.grab(_artifact)')
            }
        } catch (Exception e) {
            saveException(e)
        } finally {
            System.setProperty('groovy.grape.report.downloads', 'false')
        }
        return null
    }

    void reset(CommandInput input) {
        checkArgCount(input, [0, 1])
        if (maybePrintHelp(input, '/reset')) return
        engine.reset()
    }

    void save(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/save')) return
        if (input.args().length == 0) {
            def out = new File(Main.userStateDirectory, 'groovysh.ser')
            out.text = engine.toJson(engine.sharedData)
            return
        }
        boolean overwrite = false
        String arg = input.args()[0]
        if (arg == '-o' || arg == '--overwrite') {
            overwrite = true
            arg = input.args()[1]
        }
        saveFile(engine, new File(arg), overwrite)
    }

    static void saveFile(GroovyEngine engine, File file, boolean overwrite = false) {
        if (!file && overwrite) {
            throw new IllegalArgumentException('File to overwrite not found: ' + file.path)
        }
        if (file && !overwrite) {
            throw new IllegalArgumentException("Can't overwrite existing file: " + file.path)
        }
        file.text = engine.buffer
        println "Saved: " + file.path
    }

    void load(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/load')) return
        if (input.args().length == 0) {
            def ser = new File(Main.userStateDirectory, 'groovysh.ser')
            def map = engine.sharedData.variables
            map.clear()
            map.putAll(engine.deserialize(ser.text).variables)
            return
        }
        boolean merge = false
        String arg = input.args()[0]
        if (arg == '-m' || arg == '--merge') {
            merge = true
            arg = input.args()[1]
        }
        loadFile(engine, new File(arg), merge)
    }

    void slurpcmd(CommandInput input) {
        checkArgCount(input, [0, 1, 2, 3, 4])
        if (maybePrintHelp(input, '/slurp')) return
        Charset encoding = StandardCharsets.UTF_8
        String format = null
        def out = null
        def args = input.args()
        int index = 0
        int optionIndex = optionIdx(args, index)
        while (optionIndex > -1) {
            index++
            def option = args[optionIndex]
            def arg = null
            if (option.contains('=')) {
                arg = option.substring(option.indexOf('=') + 1)
                option = option.substring(0, option.indexOf('='))
            } else if (optionIndex + 1 < args.length) {
                arg = args[optionIndex + 1]
                index++
            }
            if (option in ['-e', '--encoding'] && arg) {
                encoding = Charset.forName(arg)
            }
            if (option in ['-f', '--format'] && arg) {
                format = arg
            }
            optionIndex = optionIdx(args, index)
        }
        Object arg = args[index]
        if (!(arg instanceof String)) {
            throw new IllegalArgumentException("Invalid parameter type: " + arg.getClass().simpleName)
        }
        try {
            Path path = Paths.get(arg)
            if (Files.exists(path)) {
                if (!format) {
                    def ext = path.extension
                    if (ext.equalsIgnoreCase('json')) {
                        format = 'JSON'
                    } else if (ext.equalsIgnoreCase('yaml') || ext.equalsIgnoreCase('yml')) {
                        format = 'YAML'
                    } else if (ext.equalsIgnoreCase('xml') || ext.equalsIgnoreCase('rdf')) {
                        format = 'XML'
                    } else if (ext.equalsIgnoreCase('groovy')) {
                        format = 'GROOVY'
                    } else if (ext.equalsIgnoreCase('toml')) {
                        format = 'TOML'
                    } else if (ext.equalsIgnoreCase('txt') || ext.equalsIgnoreCase('text')) {
                        format = 'TEXT'
                    }
                }
                if (format == 'TEXT') {
                    out = Files.readAllLines(path, encoding)
                } else if (format in engine.deserializationFormats) {
                    byte[] encoded = Files.readAllBytes(path)
                    out = engine.deserialize(new String(encoded, encoding), format)
                } else if (format == 'XML') {
                    out = new XmlParser().parse(path.toFile())
                } else if (format == 'TOML') {
                    out = new TomlSlurper().parse(path)
                } else if (format == 'YAML') {
                    out = new YamlSlurper().parse(path)
                } else {
                    out = engine.deserialize(Files.readString(path, encoding), 'NONE')
                }
            } else {
                if (format == 'TEXT') {
                    out = arg.readLines()
                } else if (format in engine.deserializationFormats) {
                    out = engine.deserialize(arg, format)
                } else if (format == 'XML') {
                    out = new XmlParser().parseText(arg)
                } else if (format == 'TOML') {
                    out = new TomlSlurper().parseText(arg)
                } else if (format == 'YAML') {
                    out = new YamlSlurper().parseText(arg)
                } else {
                    out = engine.deserialize(arg, 'NONE')
                }
            }
        } catch (Exception ignore) {
            println ignore.message
            ignore.printStackTrace()
            out = engine.deserialize(arg, format)
        }
        engine.put("_", out)
        printer.println(out)
    }

    static void loadFile(GroovyEngine engine, File file, boolean merge = false) {
        if (!file) {
            throw new IllegalArgumentException('File not found: ' + file.path)
        }
        if (!merge) {
            engine.reset()
        }
        def unprocessed = []
        file.readLines().each { line ->
            try {
                unprocessed << line
                engine.execute(unprocessed.join('\n'))
                unprocessed.clear()
            } catch (Exception ignore) {
                // ignore
            }
        }
        println "${merge ? 'Merged' : 'Loaded'}: " + file.path
    }

    private highlight(Collection<String> lines) {
        highlighter.highlight(lines.collect{ ' \b' + it }.join('\n\n')).toAnsi()
    }

    private boolean maybePrintHelp(CommandInput input, String name) {
        if (!input.args()) return false
        String arg = input.args()[0]
        if (arg == '-?' || arg == '--help') {
            printer.println(helpDesc(name))
            return true
        }
        return false
    }

    private static boolean maybeRemoveItem(CommandInput input, String name, Map<String, String> aggregate, Closure remove) {
        if (input.args().length == 2) {
            if (input.args()[0] == '-d' || input.args()[0] == '--delete') {
                def toRemove = []
                if (name == '*') {
                    toRemove.addAll(aggregate.keySet())
                } else if (name.endsWith('*')) {
                    String prefix = name[0..-2]
                    toRemove.addAll(aggregate.keySet().findAll { key -> key.startsWith(prefix) })
                } else if (name.startsWith('*')) {
                    String suffix = name[1..-1]
                    toRemove.addAll(aggregate.keySet().findAll { key -> key.endsWith(suffix) })
                } else {
                    toRemove.add(name)
                }
                toRemove.each{ remove(it) }
                return true
            }
        }
        return false
    }

    private static void checkArgCount(CommandInput input, Collection<Integer> nums) {
        if (input.args().length !in nums) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
    }

    void importsCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/imports')) return
        if (!input.args()) printer.println(highlight(engine.imports.values()))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.imports, engine::removeImport)) return
            String source = engine.imports.get(name)
            if (source) printer.println(highlight([source]))
        }
    }

    void typesCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/types')) return
        if (!input.args()) printer.println(highlight(engine.types.values()))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.types, engine::removeType)) return
            String source = engine.types.get(name)
            if (source) printer.println(highlight([source]))
        }
    }

    void varsCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/vars')) return
        if (!input.args()) printer.println(highlight(engine.variables.values()))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.variables, engine::removeVariable)) return
            String source = engine.variables.get(name)
            if (source) printer.println(highlight([source]))
        }
    }

    void methodsCommand(CommandInput input) {
        checkArgCount(input, [0, 1, 2])
        if (maybePrintHelp(input, '/methods')) return
        if (!input.args()) printer.println(highlight(engine.methods.values()))
        else {
            String name = input.args()[-1]
            if (maybeRemoveItem(input, name, engine.methods, engine::removeMethod)) return
            List<String> sources = engine.methods.entrySet().findAll{ e -> e.key.startsWith("$name(") }*.value
            if (sources) printer.println(highlight(sources))
        }
    }

    void console(CommandInput input) {
        checkArgCount(input, [0, 1])
        if (maybePrintHelp(input, '/console')) return
        Console c = new Console(engine.sharedData)
        def poller = new javax.swing.Timer(100, null)
        poller.addActionListener({ e ->
            if (c.inputArea != null) {
                poller.stop()
                c.inputArea.text = engine.buffer
            }
        } as ActionListener)
        poller.start()
        c.run()
    }

    def inspect(CommandInput input) {
        checkArgCount(input, [1, 2])
        if (maybePrintHelp(input, '/inspect')) return
        int idx = optionIdx(input.args())
        String option = idx < 0 ? '--info' : input.args()[idx]
        int id = 0
        if (idx >= 0) {
            id = idx == 0 ? 1 : 0
        }
        if (input.args().length < id + 1) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
        try {
            Object obj = input.xargs()[id]
            ObjectInspector inspector = new ObjectInspector(obj)
            Object out = null
            Map<String, Object> options = [:]
            if (option == '-m' || option == '--methods') {
                out = inspector.methods()
                options[Printer.COLUMNS] = ObjectInspector.METHOD_COLUMNS
            } else if (option == '-n' || option == '--metaMethods') {
                out = inspector.metaMethods()
                options[Printer.COLUMNS] = ObjectInspector.METHOD_COLUMNS
            } else if (option == '-i' || option == '--info') {
                out = inspector.properties()
                options[Printer.VALUE_STYLE] = engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE)
            } else if (consoleUi && (option == '-g' || option == '--gui')) {
                ObjectBrowser.inspect(obj)
            } else {
                throw new IllegalArgumentException('Unknown option: ' + option)
            }
            options[Printer.SKIP_DEFAULT_OPTIONS] = true
            options[Printer.MAX_DEPTH] = 1
            options[Printer.INDENTION] = 4
            printer.println(options, out)
        } catch (Exception e) {
            saveException(e)
        }
        return null
    }

    private classLoader(CommandInput input) {
        checkArgCount(input, [0, 1])
        String option = '--view'
        String arg = null
        if (input.args().length) {
            String[] args = input.args()
            int idx = optionIdx(args)
            option = idx > -1 ? args[idx] : '--view'
            if (option.contains('=')) {
                arg = option.substring(option.indexOf('=') + 1)
                option = option.substring(0, option.indexOf('='))
            } else if (input.args().length == 2 && idx > -1) {
                arg = idx == 0 ? args[1] : args[0]
            }
        }
        try {
            switch (option) {
                case '-?':
                case '--help':
                    printer.println(helpDesc('/classloader'))
                    break
                case '-v':
                case '--view':
                    printer.println([
                        (Printer.SKIP_DEFAULT_OPTIONS): true,
                        (Printer.VALUE_STYLE)         : engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE),
                        (Printer.INDENTION)           : 4,
                        (Printer.MAX_DEPTH)           : 1,
                        (Printer.COLUMNS)             : ['loadedClasses', 'definedPackages', 'classPath']
                    ], engine.classLoader)
                    break
                case '-d':
                case '--delete':
                    engine.purgeClassCache(arg != null ? arg.replace('*', '.*') : null)
                    break
                case '-a':
                case '--add':
                    File file = arg != null ? new File(arg) : null
                    if (!file) {
                        throw new IllegalArgumentException('Bad or missing argument!')
                    }
                    if (file.isDirectory()) {
                        String separator = FileSystems.default.separator
                        if (separator == '\\' && !arg.contains('\\') && arg.contains('/')) {
                            arg = arg.replace('/', '\\')
                        }
                        if (arg.endsWith(separator)) {
                            separator = ''
                        }
                        PathMatcher matcher = FileSystems.default
                            .getPathMatcher('regex:' + arg.replace('\\', '\\\\').replace('.', '\\.') + separator.replace('\\', '\\\\') + '.*\\.jar')
                        try (Stream<Path> pathStream = Files.walk(Paths.get(arg))) {
                            pathStream
                                .filter(matcher::matches)
                                .map(Path::toString)
                                .forEach(engine.classLoader::addClasspath)
                        }
                    } else {
                        engine.classLoader.addClasspath(arg)
                    }
                    break
            }
        } catch (Exception exp) {
            saveException(exp)
        }
        return null
    }

    private CmdDesc helpDesc(String command) {
        def tuple = commands[command]
        doHelpDesc(command, tuple.v4, tuple.v3(command))
    }

    private CmdDesc grabCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] <group>:<artifact>:<version>"),
            new AttributedString("$name --list")
        ], [], [
            '-? --help'     : doDescription('Displays command help'),
            '-l --list'     : doDescription('List the modules in the cache'),
            '-v --verbose'  : doDescription('Report downloads')
        ])
    }

    private CmdDesc slurpCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] file|variable")
        ], [], [
            '-? --help'               : doDescription('Displays command help'),
            '-e --encoding=ENCODING'  : doDescription('Encoding (default UTF-8)'),
            '-f --format=FORMAT'      : doDescription('Serialization format')
        ])
    }

    private CmdDesc defCmdDesc(String name) {
        new CmdDesc([
            new AttributedString(name),
        ], [], [
            '-? --help'     : doDescription('Displays command help')
        ])
    }

    private CmdDesc nameDeleteCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] [name]"),
        ], [], [
            '-? --help'      : doDescription('Displays command help'),
            '-d --delete'    : doDescription('Delete the named item'),
        ])
    }

    private CmdDesc loadCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] [filename]")
        ], [], [
            '-? --help'     : doDescription('Displays command help'),
            '-m --merge'    : doDescription('Merge into existing buffer')
        ])
    }

    private CmdDesc saveCmdDesc(String name) {
        new CmdDesc([
            new AttributedString("$name [OPTIONS] [filename]")
        ], [], [
            '-? --help'       : doDescription('Displays command help'),
            '-o --overwrite'  : doDescription('Overwrite existing file')
        ])
    }

    private CmdDesc inspectCmdDesc(String name) {
        def optDescs = [
            '-? --help'        : doDescription('Displays command help'),
            '-i --info'        : doDescription('Object class info'),
            '-m --methods'     : doDescription('List object methods'),
            '-n --metaMethods' : doDescription('List object metaMethods')
        ]
        if (consoleUi) {
            optDescs['-g --gui'] = doDescription('Launch object browser')
        }
        new CmdDesc([
            new AttributedString("$name [OPTIONS] OBJECT"),
        ], [], optDescs)
    }

    private CmdDesc classLoaderCmdDesc(String name) {
        new CmdDesc([
            new AttributedString(name),
        ], [], [
            '-? --help'           : doDescription('Displays command help'),
            '-v --view'           : doDescription('View class loader info'),
            '-d --delete [REGEX]' : doDescription('Delete loaded classes'),
            '-a --add PATH'       : doDescription('Add classpath PATH - a jar file or a directory')
        ])
    }

    private List<AttributedString> doDescription(String description) {
        [new AttributedString(description)]
    }

    private static int optionIdx(String[] args, idx = 0) {
        args.findIndexOf(idx) { it.startsWith('-') }
    }

    private List<String> variables() {
        engine.find(null).keySet().collect{'$' + it }
    }

    private List<OptDesc> compileOptDescs(String command) {
        def tuple = commands[command]
        List<OptDesc> out = []
        for (Map.Entry<String, List<AttributedString>> entry : tuple.v3(command).optsDesc.entrySet() ) {
            String[] option = entry.key.split(/\s+/)
            String desc = entry.value[0].toString()
            if (option.length == 2) {
                out.add(new OptDesc(option[0], option[1], desc))
            } else if (option[0].charAt(1) == '-') {
                out.add(new OptDesc(null, option[0], desc))
            } else {
                out.add(new OptDesc(option[0], null, desc))
            }
        }
        return out
    }

    private List<Completer> classloaderCompleter(String command) {
        List<Completer> argsCompleters = Collections.singletonList(NullCompleter.INSTANCE)
        List<OptDesc> options = [
            new OptDesc('-?', '--help', NullCompleter.INSTANCE),
            new OptDesc('-a', '--add', new Completers.FilesCompleter(new File('.'), '*.jar')),
            new OptDesc('-d', '--delete', NullCompleter.INSTANCE),
            new OptDesc('-v', '--view', NullCompleter.INSTANCE)]
        [new ArgumentCompleter(NullCompleter.INSTANCE, new OptionCompleter(argsCompleters, options, 1))]
    }

    private List<Completer> loadCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE, new OptionCompleter(new Completers.FilesCompleter(workDir), this::compileOptDescs, 1))]
    }

    private List<Completer> saveCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE, new OptionCompleter(new Completers.FilesCompleter(workDir), this::compileOptDescs, 1))]
    }

    private List<Completer> slurpCompleter(String command) {
        for (OptDesc o in compileOptDescs(command)) {
            if (o.shortOption()?.equals('-f')) {
                o.valueCompleter = new StringsCompleter('JSON', 'GROOVY', 'NONE', 'TEXT', 'YAML', 'TOML', 'XML')
                break
            }
        }
        [new ArgumentCompleter(
            NullCompleter.INSTANCE,
            new OptionCompleter(Arrays.asList(new AggregateCompleter(new Completers.FilesCompleter(workDir),
                new VariableReferenceCompleter(engine)), NullCompleter.INSTANCE), this::compileOptDescs, 1))]
    }

    private List<Completer> importsCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.imports.keySet()), NullCompleter.INSTANCE], this::compileOptDescs, 1))]
    }

    private List<Completer> inspectCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter([new StringsCompleter((Supplier) this::variables), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> grabCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter([new MavenCoordinateCompleter(), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> typesCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.types.keySet()), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> varsCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.variables.keySet()), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> methodsCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
            new OptionCompleter([new StringsCompleter((Supplier)() -> engine.methodNames), NullCompleter.INSTANCE],
                        this::compileOptDescs, 1))]
    }

    private List<Completer> defCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter(NullCompleter.INSTANCE,
                        this::compileOptDescs, 1)).tap{strict = false }]
    }

    // Temporarily mimicking code from ConsoleEngineImpl (remove if a viable extension point to access it emerges)
    private static class VariableReferenceCompleter implements Completer {
        private final ScriptEngine engine

        VariableReferenceCompleter(ScriptEngine engine) {
            this.engine = engine
        }

        @Override
        @SuppressWarnings("unchecked")
        void complete(LineReader reader, ParsedLine commandLine, List<Candidate> candidates) {
            assert commandLine != null
            assert candidates != null
            String word = commandLine.word()
            try {
                if (!word.contains('.') && !word.contains('}')) {
                    for (String v : engine.find().keySet()) {
                        String c = '${' + v + '}'
                        candidates.add(new Candidate(AttributedString.stripAnsi(c), c, null, null, null, null, false))
                    }
                } else if (word.startsWith('${') && word.contains('}') && word.contains('.')) {
                    String var = word.substring(2, word.indexOf('}'))
                    if (engine.hasVariable(var)) {
                        String curBuf = word.substring(0, word.lastIndexOf('.'))
                        String objStatement = curBuf.replace('${', ' ').replace('}', '')
                        Object obj = curBuf.contains('.') ? engine.execute(objStatement) : engine.get(var)
                        Map<?, ?> map = obj instanceof Map ? (Map<?, ?>) obj : null
                        Set<String> identifiers = new HashSet<>()
                        if (map != null
                            && !map.isEmpty()
                            && map.keySet().iterator().next() instanceof String) {
                            identifiers = (Set<String>) map.keySet()
                        } else if (map == null && obj != null) {
                            identifiers = getClassMethodIdentifiers(obj.getClass())
                        }
                        for (String key : identifiers) {
                            candidates.add(new Candidate(AttributedString.stripAnsi(curBuf + "." + key), key, null, null, null, null, false))
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }

        private static Set<String> getClassMethodIdentifiers(Class<?> clazz) {
            Set<String> out = new HashSet<>()
            do {
                for (Method m : clazz.getMethods()) {
                    if (!m.isSynthetic() && m.getParameterCount() == 0) {
                        String name = m.getName()
                        if (name.matches("get[A-Z].*")) {
                            out.add(convertGetMethod2identifier(name))
                        }
                    }
                }
                clazz = clazz.getSuperclass()
            } while (clazz != null)
            return out
        }

        private static String convertGetMethod2identifier(String name) {
            char[] c = name.substring(3).toCharArray()
            c[0] = Character.toLowerCase(c[0])
            return new String(c)
        }
    }
}
