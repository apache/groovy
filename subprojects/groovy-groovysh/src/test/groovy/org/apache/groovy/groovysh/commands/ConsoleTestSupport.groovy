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

import groovy.test.GroovyTestCase
import org.apache.groovy.groovysh.Main2
import org.apache.groovy.groovysh.jline.GroovyConsoleEngine
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.jline.builtins.ClasspathResourceUtil
import org.jline.builtins.ConfigurationPath
import org.jline.console.CommandRegistry
import org.jline.console.ConsoleEngine
import org.jline.console.Printer
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.DefaultParser

import java.nio.file.Path

/**
 * Support for testing {@link ConsoleEngine} instances.
 */
abstract class ConsoleTestSupport extends GroovyTestCase {
    protected GroovyEngine scriptEngine = new GroovyEngine()
    private URL rootURL = Main2.getResource('/nanorc')
    private Path root = ClasspathResourceUtil.getResourcePath(rootURL)
    private Path temp = File.createTempDir().toPath()
    private ConfigurationPath configPath = new ConfigurationPath(root, temp)
    protected List<String> output = []
    protected Printer printer = new DummyPrinter(output)
    protected ConsoleEngine console = new GroovyConsoleEngine(scriptEngine, printer, null, configPath)
    protected CommandRegistry.CommandSession session = new CommandRegistry.CommandSession()

    @Override
    void setUp() {
        super.setUp()
        console.lineReader = LineReaderBuilder.builder().parser(new DefaultParser()).build()
    }

    static class DummyPrinter implements Printer {
        DummyPrinter(List<String> output) {
            this.output = output
        }
        private List<String> output

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
