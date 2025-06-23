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
import org.jline.builtins.Completers
import org.jline.builtins.Completers.OptDesc
import org.jline.builtins.Completers.OptionCompleter
import org.jline.console.CmdDesc
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.CommandRegistry
import org.jline.console.Printer
import org.jline.console.impl.JlineCommandRegistry
import org.jline.reader.Completer
import org.jline.reader.impl.completer.ArgumentCompleter
import org.jline.reader.impl.completer.NullCompleter
import org.jline.reader.impl.completer.StringsCompleter
import org.jline.utils.AttributedString

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream

import static org.jline.builtins.Completers.TreeCompleter.node

class GroovyCommands extends JlineCommandRegistry implements CommandRegistry {
    private static final String DEFAULT_NANORC_VALUE = 'gron'
    private final GroovyEngine engine
    private final Printer printer
    private final Map<String, Tuple4<Function, Function, Function, List<String>>> commands = [
        '/inspect'     : new Tuple4<>(this::inspect, this::inspectCompleter, this::inspectCmdDesc, ['Display/browse object info on terminal/object browser']),
        '/console'     : new Tuple4<>(this::console, this::defCompleter, this::consoleCmdDesc, ['Launch Groovy console']),
        '/grab'        : new Tuple4<>(this::grab, this::grabCompleter, this::grabCmdDesc, ['Add maven repository dependencies to classpath']),
        '/classloader' : new Tuple4<>(this::classLoader, this::classloaderCompleter, this::classLoaderCmdDesc, ['Display and manage Groovy classLoader data'])
    ]
    private boolean consoleUi
    private boolean ivy

    GroovyCommands(GroovyEngine engine, Printer printer) {
        this.engine = engine
        this.printer = printer
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
        if (input.args().length > 2) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
        try {
            String arg = input.args()[0]
            if (arg == '-?' || arg == '--help') {
                printer.println(helpDesc('/grab'))
            } else if (arg == '-l' || arg == '--list') {
                Object resp = engine.execute('groovy.grape.Grape.getInstance().enumerateGrapes()')
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

    void console(CommandInput input) {
        if (input.args().length > 1) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
        if (input.args().length == 1) {
            String arg = input.args()[0]
            if (arg == '-?' || arg == '--help') {
                printer.println(helpDesc('/console'))
                return
            } else {
                throw new IllegalArgumentException('Unknown command parameter: ' + input.args()[0])
            }
        }
        Console c = new Console(engine.sharedData)
        c.run()
    }

    def inspect(CommandInput input) {
        if (!input.xargs()) {
            return null
        }
        if (input.args().length > 2) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
        int idx = optionIdx(input.args())
        String option = idx < 0 ? '--info' : input.args()[idx]
        if (option == '-?' || option == '--help') {
            printer.println(helpDesc('/inspect'))
            return null
        }
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
                options.put(Printer.COLUMNS, ObjectInspector.METHOD_COLUMNS)
            } else if (option == '-n' || option == '--metaMethods') {
                out = inspector.metaMethods()
                options.put(Printer.COLUMNS, ObjectInspector.METHOD_COLUMNS)
            } else if (option == '-i' || option == '--info') {
                out = inspector.properties()
                options.put(Printer.VALUE_STYLE, engine.groovyOption(GroovyEngine.NANORC_VALUE, DEFAULT_NANORC_VALUE))
            } else if (consoleUi && (option == '-g' || option == '--gui')) {
                ObjectBrowser.inspect(obj)
            } else {
                throw new IllegalArgumentException('Unknown option: ' + option)
            }
            options.put(Printer.SKIP_DEFAULT_OPTIONS, true)
            options.put(Printer.MAX_DEPTH, 1)
            options.put(Printer.INDENTION, 4)
            printer.println(options, out)
        } catch (Exception e) {
            saveException(e)
        }
        return null
    }

    private classLoader(CommandInput input) {
        if (input.args().length > 2) {
            throw new IllegalArgumentException('Wrong number of command parameters: ' + input.args().length)
        }
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
        new CmdDesc([], [
            '-? --help'     : doDescription('Displays command help'),
            '-l --list'     : doDescription('List the modules in the cache'),
            '-v --verbose'  : doDescription('Report downloads')
        ]).tap {
            mainDesc = [
                new AttributedString("$name [OPTIONS] <group>:<artifact>:<version>"),
                new AttributedString("$name --list")
            ]
            highlighted = false
        }
    }

    private CmdDesc consoleCmdDesc(String name) {
        new CmdDesc([], [
            '-? --help'     : doDescription('Displays command help'),
            '-l --list'     : doDescription('List the modules in the cache'),
            '-v --verbose'  : doDescription('Report downloads')
        ]).tap {
            mainDesc = [
                new AttributedString(name),
            ]
            highlighted = false
        }
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
        new CmdDesc([], optDescs).tap {
            mainDesc = [
                new AttributedString("$name [OPTION] OBJECT"),
            ]
            highlighted = false
        }
    }

    private CmdDesc classLoaderCmdDesc(String name) {
        new CmdDesc([], [
            '-? --help'           : doDescription('Displays command help'),
            '-v --view'           : doDescription('View class loader info'),
            '-d --delete [REGEX]' : doDescription('Delete loaded classes'),
            '-a --add PATH'       : doDescription('Add classpath PATH - a jar file or a directory')
        ]).tap {
            mainDesc = [
                new AttributedString(name),
            ]
            highlighted = false
        }
    }

    private List<AttributedString> doDescription(String description) {
        [new AttributedString(description)]
    }

    private int optionIdx(String[] args) {
        int out = 0
        for (String a : args) {
            if (a.startsWith('-')) {
                return out
            }
            out++
        }
        return -1
    }

    private List<String> variables() {
        engine.find(null).keySet().collect{'$' + it }
    }

    private List<OptDesc> compileOptDescs(String command) {
        def tuple = commands[command]
        List<OptDesc> out = []

        for (Map.Entry<String, List<AttributedString>> entry : tuple.v3(command).optsDesc.entrySet() ) {
            String[] option = entry.getKey().split(/\s+/)
            String desc = entry.value.get(0).toString()
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

    private List<Completer> defCompleter(String command) {
        [new ArgumentCompleter(NullCompleter.INSTANCE,
                new OptionCompleter(NullCompleter.INSTANCE,
                        this::compileOptDescs, 1)).tap{strict = false }]
    }
}
