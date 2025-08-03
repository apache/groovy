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
import java.util.regex.Matcher
import java.util.regex.Pattern

class GroovyConsoleEngine extends ConsoleEngineImpl {
    private final Printer printer
    private final ScriptEngine engine

    GroovyConsoleEngine(ScriptEngine engine, Printer printer, Supplier<Path> workDir, ConfigurationPath configPath) {
        super(Command.values().toSet() - Command.SLURP, engine, printer, workDir, configPath)
        this.printer = printer
        this.engine = engine
        commandNames().each{ name -> rename(Command."${name.toUpperCase()}", "/$name") }
    }

    void println(Map<String, Object> options, Object object) {
        printer.println(options, object)
    }

    // TODO remove if PR JLine#1371 accepted upstream
    @Override
    Object[] expandParameters(String[] args) throws Exception {
        Object[] out = new Object[args.length]
        String regexPath = /(.*)\$\{(.*?)}(\/.*)/
        for (int i = 0; i < args.length; i++) {
            if (args[i].matches(regexPath)) {
                Matcher matcher = Pattern.compile(regexPath).matcher(args[i])
                if (matcher.find()) {
                    out[i] = matcher.group(1) + engine.get(matcher.group(2)) + matcher.group(3)
                } else {
                    throw new IllegalArgumentException()
                }
            } else if (args[i].startsWith('${')) {
                String expanded = expandName(args[i])
                String statement = expanded.startsWith('$') ? args[i][2..-2] : expanded
                out[i] = engine.execute(statement)
            } else if (args[i].startsWith('$')) {
                out[i] = engine.get(expandName(args[i]))
            } else {
                out[i] = engine.deserialize(args[i])
            }
        }
        out
    }

    // can be removed if the following PR is merged and released
    // https://github.com/jline/jline3/pull/1357
    private static String expandName(String name) {
        String regexVar = "[a-zA-Z_][a-zA-Z0-9_-]*"
        String out = name
        if (name.matches('^\\$' + regexVar)) {
            out = name.substring(1)
        } else if (name.matches('^\\$\\{' + regexVar + '}.*')) {
            Matcher matcher = Pattern.compile('^\\$\\{(' + regexVar + ')}(.*)').matcher(name)
            if (matcher.find()) {
                out = matcher.group(1) + matcher.group(2)
            } else {
                throw new IllegalArgumentException()
            }
        }
        return out
    }

    @Override
    String name() {
        'Console Commands'
    }
}
