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
package org.apache.groovy.groovysh

import groovy.mock.interceptor.MockFor
import groovy.test.GroovyTestCase
import jline.console.ConsoleReader
import jline.console.completer.CandidateListCompletionHandler
import org.codehaus.groovy.tools.shell.IO
import org.codehaus.groovy.tools.shell.util.Preferences

class ShellRunnerTest extends GroovyTestCase {

    private Groovysh groovysh

    @Override
    void setUp() {
        super.setUp()
        ByteArrayOutputStream mockOut = new ByteArrayOutputStream()
        ByteArrayOutputStream mockErr = new ByteArrayOutputStream()

        IO testio = new IO(new ByteArrayInputStream(),
                mockOut,
                mockErr)
        groovysh = new Groovysh(testio)
    }

    void testReadLineIndentPreferenceOff() {
        groovysh.buffers.buffers.add(['Foo {'])
        groovysh.buffers.select(1)

        MockFor readerMocker = primedMockForConsoleReader()
        readerMocker.demand.readLine(1) {'Foo {'}
        MockFor preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'false'}
        preferencesMocker.use {readerMocker.use {
            InteractiveShellRunner runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()
            assertEquals(0, runner.wrappedInputStream.inserted.available())
        }}
    }

    void testReadLineIndentNone() {
        MockFor readerMocker = primedMockForConsoleReader()
        readerMocker.demand.readLine(1) {'Foo {'}
        MockFor preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        preferencesMocker.use {readerMocker.use {
            InteractiveShellRunner runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()
            assertEquals(0, runner.wrappedInputStream.inserted.available())
        }}
    }

    void testReadLineIndentOne() {
        groovysh.buffers.buffers.add(['Foo {'])
        groovysh.buffers.select(1)

        MockFor readerMocker = primedMockForConsoleReader()
        readerMocker.demand.readLine(1) {'Foo {'}
        MockFor preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        preferencesMocker.use {readerMocker.use {
            InteractiveShellRunner runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()
            assertEquals(groovysh.indentSize, runner.wrappedInputStream.inserted.available())
        }}
    }

    void testReadLineIndentTwo() {
        groovysh.buffers.buffers.add(['Foo { {'])
        groovysh.buffers.select(1)
        MockFor readerMocker = primedMockForConsoleReader()
        readerMocker.demand.readLine(1) {'Foo { {'}
        MockFor preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        preferencesMocker.use {readerMocker.use {
            InteractiveShellRunner runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()
            assertEquals(groovysh.indentSize * 2, runner.wrappedInputStream.inserted.available())
        }}
    }

    private MockFor primedMockForConsoleReader() {
        def readerMocker = new MockFor(ConsoleReader)
        CandidateListCompletionHandler clch = new CandidateListCompletionHandler()
        clch.stripAnsi = true
        readerMocker.demand.getCompletionHandler(1) {clch}
        readerMocker.demand.setExpandEvents {}
        readerMocker.demand.addCompleter(2) {}
        readerMocker
    }
}

class ShellRunnerTest2 extends GroovyTestCase {

    void testReadLinePaste() {
        ByteArrayOutputStream mockOut = new ByteArrayOutputStream()
        ByteArrayOutputStream mockErr = new ByteArrayOutputStream()

        IO testio = new IO(new ByteArrayInputStream('Some Clipboard Content'.bytes),
                mockOut,
                mockErr)
        Groovysh groovysh = new Groovysh(testio)
        groovysh.buffers.buffers.add(['Foo { {'])
        groovysh.buffers.select(1)

        MockFor readerMocker = new MockFor(ConsoleReader)
        readerMocker.demand.getCompletionHandler {new CandidateListCompletionHandler()}
        readerMocker.demand.setExpandEvents {}
        readerMocker.demand.addCompleter(2) {}
        readerMocker.demand.readLine(1) {'Foo { {'}
        MockFor preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        preferencesMocker.use {readerMocker.use {
            InteractiveShellRunner runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()
            assertEquals(0, runner.wrappedInputStream.inserted.available())
        }}
    }
}
