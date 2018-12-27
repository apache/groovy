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
package org.codehaus.groovy.tools.shell

import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import jline.console.ConsoleReader

/**
 * Support for testing {@link Command} instances.
 */
abstract class ShellRunnerTestSupport extends GroovyTestCase {

    protected IO testio
    protected BufferedOutputStream mockOut
    protected BufferedOutputStream mockErr

    protected MockFor shellMocker
    protected StubFor readerStubber

    @Override
    void setUp() {
        super.setUp()
        mockOut = new BufferedOutputStream(new ByteArrayOutputStream())
        mockErr = new BufferedOutputStream(new ByteArrayOutputStream())
        testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)
        testio.verbosity = IO.Verbosity.QUIET
        // setup mock and stub with calls expected from InteractiveShellRunner Constructor

        shellMocker = new MockFor(Groovysh)
        shellMocker.demand.createDefaultRegistrar(1) { {Shell shell -> null} }
        // when run with compileStatic
        shellMocker.demand.getClass(0..1) {Groovysh}
        shellMocker.demand.getIo(2) { testio }
        shellMocker.demand.getRegistry(1) {new Object() {def commands() {[]} }}
        shellMocker.demand.getHistory(1) {new Serializable(){def size() {0}; def getMaxSize() {1}}}
        shellMocker.demand.setHistoryFull(1) {}
        shellMocker.demand.getHistoryFull(1) {false}
        // adding number of commands from xml file
        for (i in 1..19) {
            shellMocker.demand.getIo(0..1) { testio }
            shellMocker.demand.add(0..1) { testio }
            shellMocker.demand.getIo(0..1) { testio }
        }

        readerStubber = new StubFor(ConsoleReader)
        readerStubber.demand.setExpandEvents {}
        readerStubber.demand.setCompletionHandler {}
        // adding 2 completers
        readerStubber.demand.addCompleter {}
        readerStubber.demand.printNewline {}
        readerStubber.demand.addCompleter {}
        shellMocker.demand.getIo(0..1) { testio }
    }

}
