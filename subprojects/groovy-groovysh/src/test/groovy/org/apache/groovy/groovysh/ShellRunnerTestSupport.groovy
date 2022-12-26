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
import groovy.mock.interceptor.StubFor
import groovy.test.GroovyTestCase
import jline.console.ConsoleReader
import org.codehaus.groovy.tools.shell.IO

/**
 * Support for testing {@link ShellRunner} instances.
 */
abstract class ShellRunnerTestSupport extends GroovyTestCase {

    protected BufferedOutputStream mockOut
    protected BufferedOutputStream mockErr
    protected IO testio

    protected MockFor shellMocker
    protected StubFor readerStubber

    @Override
    protected void setUp() {
        super.setUp()

        mockOut = new BufferedOutputStream(new ByteArrayOutputStream())
        mockErr = new BufferedOutputStream(new ByteArrayOutputStream())
        testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)
        testio.verbosity = IO.Verbosity.QUIET

        shellMocker = new MockFor(Groovysh)
        // Groovysh constructor
        shellMocker.demand.getClass( 1) { Groovysh }
        shellMocker.demand.getIo(0..21) { testio }
        shellMocker.demand.register(18) { it }
        // InteractiveShellRunner constructor
        shellMocker.demand.getIo(2) { testio }
        // InteractiveShellRunner run
        shellMocker.demand.getRegistry(0..1) { new CommandRegistry() }
        // InteractiveShellRunner adjustHistory
        shellMocker.demand.getHistory(0..1) { null }
        shellMocker.demand.setHistoryFull(0..1) { }
        shellMocker.demand.isHistoryFull(0..1) { }

        readerStubber = new StubFor(ConsoleReader)
        readerStubber.demand.setExpandEvents {}
        readerStubber.demand.setCompletionHandler {}
        // adding 2 completers
        readerStubber.demand.addCompleter {}
        readerStubber.demand.printNewline {}
        readerStubber.demand.addCompleter {}
    }
}
