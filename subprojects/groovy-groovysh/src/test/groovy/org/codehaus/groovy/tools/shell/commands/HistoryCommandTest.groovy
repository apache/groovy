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
package org.codehaus.groovy.tools.shell.commands

import jline.console.history.FileHistory
import org.codehaus.groovy.tools.shell.CompletorTestSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * Tests for the {@link HistoryCommand} class.
 */
class HistoryCommandTest extends CommandTestSupport
{
    void testHistory() {
        shell.execute(HistoryCommand.COMMAND_NAME + ' nocommandhere')
    }

    void testHistoryNoArg() {
        shell.execute(HistoryCommand.COMMAND_NAME)
    }
}

class HistoryCommandIntegrationTest extends CompletorTestSupport
{
    private File filemock

    @Override
    void setUp() {
        super.setUp()
        filemock = new File('aaaa') {
            @Override
            boolean delete() {
                return true
            }

            @Override
            boolean isFile() {
                return true
            }
        }
    }

    void testShowEmpty() {
        FileHistory history = new FileHistory(filemock)
        groovyshMocker.demand.getHistory(1) {history}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            HistoryCommand command = new HistoryCommand(groovyshMock)
            command.do_show()
            assert '' == mockOut.toString()
        }
    }

    void testShowLines() {
        FileHistory history = new FileHistory(filemock)
        history.add('test1')
        history.add('test2')
        assert 2 == history.size()
        groovyshMocker.demand.getHistory(1) {history}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            HistoryCommand command = new HistoryCommand(groovyshMock)
            command.do_show()
            assert 'test1' in mockOut.toString().split()
            assert 'test2' in mockOut.toString().split()
        }
    }

    void testClear() {
        FileHistory history = new FileHistory(filemock)
        history.add('test1')
        history.add('test2')
        groovyshMocker.demand.getHistory(1) {history}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            HistoryCommand command = new HistoryCommand(groovyshMock)
            command.do_clear()
            assert 0 == history.size()
        }
    }

    void testRecall() {
        FileHistory history = new FileHistory(filemock)
        history.add('test1')
        history.add('test2')
        groovyshMocker.demand.getHistoryFull(1) {false}
        groovyshMocker.demand.getHistory(1) {history}
        groovyshMocker.demand.execute(1) {String it -> assert(it == 'test1'); 34}
        // second call
        groovyshMocker.demand.getHistoryFull(1) {false}
        groovyshMocker.demand.getHistory(1) {history}
        groovyshMocker.demand.execute(1) {String it -> assert(it == 'test2'); 56}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            HistoryCommand command = new HistoryCommand(groovyshMock)
            def result = command.do_recall(['0'])
            assert 34 == result
            result = command.do_recall(['1'])
            assert 56 == result
        }
    }

    void testRecallHistoryFull() {
        FileHistory history = new FileHistory(filemock)
        history.add('test1')
        history.add('test2')
        groovyshMocker.demand.getHistoryFull(1) {true}
        groovyshMocker.demand.getHistory(1) {history}
        groovyshMocker.demand.getEvictedLine(1) {'test3'}
        groovyshMocker.demand.execute(1) {String it -> assert(it == 'test3'); 45}
        // second call
        groovyshMocker.demand.getHistoryFull(1) {true}
        groovyshMocker.demand.getHistory(1) {history}
        groovyshMocker.demand.execute(1) {String it -> assert(it == 'test1'); 56}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            HistoryCommand command = new HistoryCommand(groovyshMock)
            def result = command.do_recall(['0'])
            assert 45 == result
            result = command.do_recall(['1'])
            assert 56 == result
        }
    }

}
