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
import org.jline.console.impl.SystemRegistryImpl
import org.jline.reader.Parser
import org.jline.terminal.Terminal

import java.nio.file.Path
import java.util.function.Supplier

class GroovySystemRegistry extends SystemRegistryImpl {
    GroovySystemRegistry(Parser parser, Terminal terminal, Supplier<Path> workDir, ConfigurationPath configPath) {
        super(parser, terminal, workDir, configPath)
        rename(Pipe.AND, '/&&')
        rename(Pipe.OR, '/||')
    }

    // workaround for: https://github.com/jline/jline3/issues/1361
    @Override
    Object execute(String line) throws Exception {
        def m = line =~ /([a-zA-Z][a-zA-Z0-9_]*)=(\/\S.*)/
        def target = null
        if (m.matches()) {
            (target, line) = m[0][1,2]
        }
        def result = super.execute(line)
        if (target) {
            consoleEngine().with {
                putVariable(target, getVariable('_'))
            }
        }
        result
    }

    @Override
    boolean isCommandOrScript(String command) {
        return command.startsWith("/!") || super.isCommandOrScript(command)
    }
}
