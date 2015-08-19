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

import jline.console.ConsoleReader
import jline.console.completer.CandidateListCompletionHandler


class ErrorDisplayTest extends ShellRunnerTestSupport {

    void testInput() {
        readerStubber.demand.readLine { 'foo' }
        readerStubber.demand.getCompletionHandler {new CandidateListCompletionHandler()}
        shellMocker.use {
            readerStubber.use {
                Groovysh shellMock = new Groovysh()
                ConsoleReader readerStub = new ConsoleReader()

                InteractiveShellRunner shellRunner = new InteractiveShellRunner(shellMock, { '>' })
                shellRunner.reader = readerStub
                // assert no exception
                shellRunner.run()
            }
        }
    }

    void testError() {
        readerStubber.demand.readLine { throw new StringIndexOutOfBoundsException() }
        readerStubber.demand.getCompletionHandler {new CandidateListCompletionHandler()}
        shellMocker.use {
            readerStubber.use {
                Groovysh shellMock = new Groovysh()
                ConsoleReader readerStub = new ConsoleReader()

                InteractiveShellRunner shellRunner = new InteractiveShellRunner(shellMock, { '>' })
                shellRunner.reader = readerStub
                // assert no exception
                shellRunner.run()
            }
        }
    }

    void testError2() {
        readerStubber.demand.readLine { throw new Throwable('MockException') }
        readerStubber.demand.getCompletionHandler {new CandidateListCompletionHandler()}
        shellMocker.use { readerStubber.use {
            Groovysh shellMock = new Groovysh()
            ConsoleReader readerStub = new ConsoleReader()

            InteractiveShellRunner shellRunner = new InteractiveShellRunner(shellMock, {'>'})
            shellRunner.reader = readerStub
            // assert no exception
            shellRunner.run()
        }}
    }

}
