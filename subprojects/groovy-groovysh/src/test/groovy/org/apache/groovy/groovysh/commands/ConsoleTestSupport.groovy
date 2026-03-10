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
package org.apache.groovy.groovysh.commands

import org.apache.groovy.groovysh.Main
import org.apache.groovy.groovysh.jline.GroovyCommands
import org.apache.groovy.groovysh.jline.GroovyConsoleEngine
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.jline.builtins.ClasspathResourceUtil
import org.jline.builtins.ConfigurationPath
import org.jline.builtins.SyntaxHighlighter
import org.jline.console.CommandRegistry
import org.jline.console.ConsoleEngine
import org.jline.console.impl.DefaultPrinter
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.DefaultParser
import org.junit.jupiter.api.BeforeEach

import java.nio.file.Path

/**
 * Support for testing {@link ConsoleEngine} instances.
 */
abstract class ConsoleTestSupport {
    protected GroovyEngine engine = new GroovyEngine()
    private URL rootURL = Main.getResource('/nanorc')
    private Path root = ClasspathResourceUtil.getResourcePath(rootURL)
    private Path temp = File.createTempDir().toPath()
    protected ConfigurationPath configPath = new ConfigurationPath(root, temp)
    protected DummyPrinter printer = new DummyPrinter(configPath)
    private highlighter = SyntaxHighlighter.build(root, "DUMMY")
    protected CommandRegistry groovy = new GroovyCommands(engine, null, printer, highlighter)
    protected ConsoleEngine console
    protected CommandRegistry.CommandSession session = new CommandRegistry.CommandSession()
    protected LineReader reader

    @BeforeEach
    void setUp() {
        reader = LineReaderBuilder.builder().parser(new DefaultParser(regexCommand: /\/?[a-zA-Z!]+\S*/)).build()
        console = new GroovyConsoleEngine(engine, printer, null, configPath, reader)
    }

    static class DummyPrinter extends DefaultPrinter {
        DummyPrinter(ConfigurationPath configPath) {
            super(configPath)
        }
        List<String> output = []

        @Override
        void println(Object object) {
            println(defaultPrntOptions(false), object)
        }

        @Override
        void println(Map<String, Object> options, Object object) {
            output << object.toString()
        }

        @Override
        boolean refresh() {
            false
        }
    }
}
