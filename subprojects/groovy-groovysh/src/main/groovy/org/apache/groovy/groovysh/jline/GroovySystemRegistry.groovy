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

/**
 * Specializes the JLine system registry for groovysh pipeline syntax and assignments.
 */
class GroovySystemRegistry extends SystemRegistryImpl {
    /**
     * Creates the system registry used by groovysh.
     *
     * @param parser command parser
     * @param terminal active terminal
     * @param workDir supplier for the current working directory
     * @param configPath configuration lookup path
     */
    GroovySystemRegistry(Parser parser, Terminal terminal, Supplier<Path> workDir, ConfigurationPath configPath) {
        super(parser, terminal, workDir, configPath)
        rename(Pipe.AND, '|&&')
        rename(Pipe.OR, '|||')
        rename(Pipe.REDIRECT, '|>')
        rename(Pipe.APPEND, '|>>')
    }

    /**
     * Executes a shell line after normalizing groovysh-specific syntax.
     *
     * @param line command line to execute
     * @return the command result
     * @throws Exception if command execution fails
     */
    @Override
    Object execute(String line) throws Exception {
        line = line.startsWith("/!") ? line.replaceFirst("/!", "/! ") : line
        // SystemRegistryImpl assumes we have no spaces around the '=' in variable assignments
        // for commands, so we adjust here to support Groovy idiomatic syntax.
        def m = line =~ /([a-zA-Z][a-zA-Z0-9_]*)\s*=\s*(\/?)(\S.*)/
        if (m.matches()) {
            def (_, variable, slash, rhs) = m[0]
            if (slash) {
                line = variable + '=' + slash + rhs
            }
        }
        super.execute(line)
    }

    /**
     * Determines whether the token should be handled as a command or script name.
     *
     * @param command token to inspect
     * @return {@code true} if the token maps to a command or script
     */
    @Override
    boolean isCommandOrScript(String command) {
        return command.startsWith("/!") || super.isCommandOrScript(command)
    }
}
