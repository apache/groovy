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
import org.jline.builtins.PosixCommands
import org.jline.terminal.Terminal

import java.nio.file.Path
import java.util.function.Function

/**
 * Extends the JLine POSIX context with groovysh-specific working-directory tracking.
 */
class GroovyPosixContext extends PosixCommands.Context {
    /** Current working directory exposed to groovysh POSIX commands. */
    Path currentDir
    /**
     * Configuration path used to locate {@code jnanorc} and theme files for
     * commands that integrate with the JLine syntax-highlighter (e.g. {@code /less}).
     * Optional; null disables highlighting.
     */
    ConfigurationPath configPath

    /**
     * Creates a POSIX command context backed by groovysh state.
     *
     * @param inputStream command input stream
     * @param out command standard output stream
     * @param err command error output stream
     * @param currentDir current working directory
     * @param terminal active terminal
     * @param variables variable lookup function
     * @param configPath configuration path used by syntax-highlighting commands; null disables highlighting
     */
    GroovyPosixContext(InputStream inputStream, PrintStream out, PrintStream err, Path currentDir,
                       Terminal terminal, Function<String, Object> variables,
                       ConfigurationPath configPath = null) {
        super(inputStream, out, err, null, terminal, variables)
        this.currentDir = currentDir
        this.configPath = configPath
    }

    /**
     * Returns the current working directory used by POSIX commands.
     *
     * @return the active working directory
     */
    Path currentDir() {
        currentDir
    }
}
