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

import org.jline.builtins.Commands
import org.jline.builtins.ConfigurationPath
import org.jline.builtins.Less
import org.jline.builtins.Nano
import org.jline.builtins.Options
import org.jline.console.CommandInput
import org.jline.console.CommandMethods
import org.jline.console.impl.Builtins
import org.jline.reader.LineReader
import org.jline.reader.Widget

import java.nio.file.Path
import java.util.function.Function
import java.util.function.Supplier

class GroovyBuiltins extends Builtins {
    private final ConfigurationPath configPath
    private final Supplier<Path> workDir
    private final GroovyEngine engine

    GroovyBuiltins(GroovyEngine engine, Supplier<Path> workDir, ConfigurationPath configPath, LineReader reader, Function<String, Widget> widgetCreator) {
        super(workDir, configPath, widgetCreator)
        this.workDir = workDir
        this.configPath = configPath
        this.engine = engine
        // Builtins doesn't have the extension points we want, so duplicate its command registration here
        def commandExecute = commandNames().collectEntries { name ->
            def methods = getCommandMethods(name)
            if (name in ['nano', 'less']) {
                methods = new CommandMethods((Function)this::"$name", methods.compileCompleter())
            }
            [Command."${name.toUpperCase()}", methods]
        }
        def commandName = commandNames().collectEntries{ name ->
            [Command."${name.toUpperCase()}", '/' + name]
        }
        setLineReader(reader)
        registerCommands(commandName, commandExecute)
    }

    private void less(CommandInput input) {
        Options opt = Options.compile(*Less.usage()*.replaceAll('less ', '/less ')).parse(input.args())
        try {
            if (opt.isSet("help")) {
                throw new Options.HelpException(opt.usage());
            }
            boolean usingBuffer = opt.args().size() == 0
            if (usingBuffer) {
                def temp = File.createTempFile('groovysh', '.groovy')
                temp.text = engine.buffer
                input = new CommandInput(input.command(), [*input.args(), temp.absolutePath] as String[], input.terminal(), input.in(), input.out(), input.err())
            }
            Commands.less(input.terminal(), input.in(), input.out(), input.err(), workDir.get(), input.xargs(), configPath)
        } catch (Exception e) {
            saveException(e)
        }
    }

    private void nano(CommandInput input) {
        Options opt = Options.compile(*Nano.usage()*.replaceAll('nano ', '/nano ')).parse(input.args())
        try {
            if (opt.isSet("help")) {
                throw new Options.HelpException(opt.usage());
            }
            boolean usingBuffer = opt.args().size() == 0
            def temp = null
            if (usingBuffer) {
                temp = File.createTempFile('groovysh', '.groovy')
                temp.text = engine.buffer
                input = new CommandInput(input.command(), [*input.args(), temp.absolutePath] as String[], input.terminal(), input.in(), input.out(), input.err())
            }
            Commands.nano(input.terminal(), input.out(), input.err(), workDir.get(), input.args(), configPath)
            if (temp) GroovyCommands.loadFile(engine, temp)
        } catch (Exception e) {
            saveException(e)
        }
    }

    @Override
    String name() {
        'Console Commands'
    }
}
