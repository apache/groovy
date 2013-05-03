/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell

import groovy.mock.interceptor.MockFor
import groovy.mock.interceptor.StubFor
import jline.ConsoleReader

/**
 * Support for testing {@link Command} instances.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
abstract class ShellRunnerTestSupport
extends GroovyTestCase {

    IO testio
    BufferedOutputStream mockOut
    BufferedOutputStream mockErr

    MockFor shellMocker
    StubFor readerStubber

    void setUp() {
        super.setUp()
        mockOut = new BufferedOutputStream(
                new ByteArrayOutputStream());

        mockErr = new BufferedOutputStream(
                new ByteArrayOutputStream());

        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        testio.verbosity = IO.Verbosity.QUIET
        // setup mock and stub with calls expected from InteractiveShellRunner Constructor

        shellMocker = new MockFor(Groovysh.class)
        shellMocker.demand.createDefaultRegistrar(1) { {Shell shell -> null} }
        shellMocker.demand.getClass(1) { Groovysh }
        shellMocker.demand.getIo(2) { testio }
        shellMocker.demand.getRegistry(1) {[]}
        shellMocker.demand.getHistory(2) {[size: 0, maxsize: 1]}
        shellMocker.demand.setHistoryFull(1) {}
        shellMocker.demand.getHistoryFull(1) {false}
        // adding number of commands from xml file
        for (i in 1..19) {
            shellMocker.demand.getIo(0..1) { testio }
            shellMocker.demand.leftShift(0..1) { testio }
            shellMocker.demand.getIo(0..1) { testio }
        }

//        shellMocker.demand.getClass(1) {Groovysh}
//        // don't care how often this is called
//        shellMocker.demand.getIo(2..10) {testio}

        readerStubber = new StubFor(ConsoleReader)
        // adding 2 completors
        readerStubber.demand.addCompletor() {}
        readerStubber.demand.printNewline() {}
        readerStubber.demand.addCompletor() {}
        shellMocker.demand.getIo(0..1) { testio }
    }

}
