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

import org.jline.builtins.ConfigurationPath
import org.jline.console.Printer
import org.jline.console.ScriptEngine
import org.jline.console.impl.ConsoleEngineImpl
import org.jline.reader.LineReader

import java.nio.file.Path
import java.util.function.Supplier

/**
 * Adapts the generic JLine console engine to groovysh command naming and printing behavior.
 */
class GroovyConsoleEngine extends ConsoleEngineImpl {
    private final Printer printer

    /**
     * Creates the console-engine adapter used by groovysh.
     *
     * @param engine script engine that evaluates Groovy input
     * @param printer printer used for formatted output
     * @param workDir supplier for the current working directory
     * @param configPath configuration lookup path
     * @param reader line reader bound to the interactive shell
     */
    GroovyConsoleEngine(ScriptEngine engine, Printer printer, Supplier<Path> workDir, ConfigurationPath configPath, LineReader reader) {
        super(Command.values().toSet() - Command.SLURP - Command.DOC, engine, printer, workDir, configPath)
        this.printer = printer
        setLineReader(reader)
        commandNames().each{ name -> rename(Command."${name.toUpperCase(Locale.ROOT)}", "/$name") }
    }

    /**
     * Prints a value using the configured shell printer and option set.
     *
     * @param options print options to apply
     * @param object value to render
     */
    void println(Map<String, Object> options, Object object) {
        printer.println(options, object)
    }

    /**
     * Returns the help-group name used for these commands.
     *
     * @return the console command group name
     */
    @Override
    String name() {
        'Console Commands'
    }
}
