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

import java.nio.file.Path
import java.util.function.Supplier

class GroovyConsoleEngine extends ConsoleEngineImpl {
    private final Printer printer

    GroovyConsoleEngine(ScriptEngine engine, Printer printer, Supplier<Path> workDir, ConfigurationPath configPath) {
        super(Command.values().toSet() - Command.SLURP, engine, printer, workDir, configPath)
        this.printer = printer
        commandNames().each{ name -> rename(Command."${name.toUpperCase()}", "/$name") }
    }

    void println(Map<String, Object> options, Object object) {
        printer.println(options, object)
    }

    @Override
    String name() {
        'Console Commands'
    }
}
