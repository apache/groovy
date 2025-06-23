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
import org.apache.groovy.groovysh.jline.GroovyCommands
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.groovysh.jline.GroovySystemRegistry
import org.jline.console.CommandRegistry
import org.jline.console.Printer

/**
 * Support for testing commands from {@link GroovyCommands}.
 */
abstract class GroovyCommandTestSupport extends GroovyTestCase {
    protected GroovyEngine engine = new GroovyEngine() {
        def getLoader() {
            classLoader
        }
    }
    protected List<String> output = []
    protected Printer printer = new DummyPrinter(output)
    protected CommandRegistry groovy = new GroovyCommands(engine, null, printer, null)
    protected CommandRegistry.CommandSession session = new CommandRegistry.CommandSession()

    static class DummyPrinter implements Printer {
        DummyPrinter(List<String> output) {
            this.output = output
        }
        private List<String> output

        @Override
        void println(Map<String, Object> options, Object object) {
            // a bit ugly to partially replicate the logic from
            // DefaultPrinter here, but it isn't easy to mock out
            if (object instanceof GroovyEngine.EngineClassLoader) {
                options?.columns?.each { col ->
                    output << "$col=" + object."$col"
                }
                return
            }
            output << object.toString()
        }

        @Override
        boolean refresh() {
            false
        }
    }
}
