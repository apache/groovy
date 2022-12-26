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

final class ShellRunnerTest extends GroovyTestCase {

    private Groovysh groovysh
    private MockFor  readerMocker

    @Override
    protected void setUp() {
        super.setUp()
        ByteArrayOutputStream mockOut = new ByteArrayOutputStream()
        ByteArrayOutputStream mockErr = new ByteArrayOutputStream()
        IO testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)

        groovysh = new Groovysh(testio)

        readerMocker = new MockFor(ConsoleReader)
        readerMocker.demand.getCompletionHandler(1) {new CandidateListCompletionHandler(stripAnsi: true)}
        readerMocker.demand.setExpandEvents {}
        readerMocker.demand.addCompleter(2) {}
    }

    //--------------------------------------------------------------------------

    void testReadLineIndentPreferenceOff() {
        groovysh.buffers.buffers.add(['Foo {'])
        groovysh.buffers.select(1)

        def preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'false'}
        readerMocker.demand.readLine(1) {'Foo {'}
        preferencesMocker.use { readerMocker.use {
            def runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()

            assertEquals(0, runner.wrappedInputStream.inserted.available())
        }}
    }

    void testReadLineIndentNone() {
        def preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        readerMocker.demand.readLine(1) {'Foo {'}
        preferencesMocker.use { readerMocker.use {
            def runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()

            assertEquals(0, runner.wrappedInputStream.inserted.available())
        }}
    }

    void testReadLineIndentOne() {
        groovysh.buffers.buffers.add(['Foo {'])
        groovysh.buffers.select(1)

        def preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        readerMocker.demand.readLine(1) {'Foo {'}
        preferencesMocker.use { readerMocker.use {
            def runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()

            assertEquals(groovysh.indentSize, runner.wrappedInputStream.inserted.available())
        }}
    }

    void testReadLineIndentTwo() {
        groovysh.buffers.buffers.add(['Foo { {'])
        groovysh.buffers.select(1)

        def preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        readerMocker.demand.readLine(1) {'Foo { {'}
        preferencesMocker.use { readerMocker.use {
            def runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()

            assertEquals(groovysh.indentSize * 2, runner.wrappedInputStream.inserted.available())
        }}
    }
}

class ShellRunnerTest2 extends GroovyTestCase {

    void testReadLinePaste() {
        Groovysh groovysh = new Groovysh(new IO(new ByteArrayInputStream('Some Clipboard Content'.bytes),
                                                new ByteArrayOutputStream(), new ByteArrayOutputStream()))
        groovysh.buffers.buffers.add(['Foo { {'])
        groovysh.buffers.select(1)

        MockFor preferencesMocker = new MockFor(Preferences)
        preferencesMocker.demand.get(1) {'true'}
        MockFor readerMocker = new MockFor(ConsoleReader)
        readerMocker.demand.getCompletionHandler {new CandidateListCompletionHandler()}
        readerMocker.demand.setExpandEvents {}
        readerMocker.demand.addCompleter(2) {}
        readerMocker.demand.readLine(1) {'Foo { {'}

        preferencesMocker.use { readerMocker.use {
            def runner = new InteractiveShellRunner(groovysh, {'>'})
            runner.readLine()

            assertEquals(0, runner.wrappedInputStream.inserted.available())
        }}
    }
}
