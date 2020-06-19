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

import groovy.test.GroovyTestCase
import jline.console.completer.Completer
import jline.console.history.FileHistory
import org.codehaus.groovy.tools.shell.IO
import org.apache.groovy.groovysh.commands.DocCommand
import org.apache.groovy.groovysh.commands.EditCommand
import org.apache.groovy.groovysh.commands.ExitCommand
import org.apache.groovy.groovysh.commands.HelpCommand
import org.apache.groovy.groovysh.commands.InspectCommand
import org.apache.groovy.groovysh.commands.PurgeCommand
import org.apache.groovy.groovysh.commands.SetCommand
import org.apache.groovy.groovysh.commands.ShowCommand

/**
 * Test the combination of multiple completers via JLine ConsoleReader
 */
class AllCompletersTest extends GroovyTestCase {

    private IO testio
    private BufferedOutputStream mockOut
    private BufferedOutputStream mockErr
    private List<Completer> completers

    /**
     * code copied from Jline console Handler,
     * need this logic to ensure completers are combined in the right way
     * The Jline contract is that completers are tried in sequence, and as
     * soon as one returns something other than -1, the candidates are used and the following
     * completers ignored.
     *
     */
    private List complete(String buffer, cursor) throws IOException {
        // debug ("tab for (" + buf + ")")
        if (completers.size() == 0) {
            return []
        }
        List candidates = new LinkedList()
        String bufstr = buffer
        int position = -1
        for (Completer comp : completers) {
            if ((position = comp.complete(bufstr, cursor, candidates)) != -1) {
                break
            }
        }
        // no candidates? Fail.
        if (candidates.size() == 0) {
            return []
        }
        return [candidates, position]
    }

    @Override
    void setUp() {
        super.setUp()
        mockOut = new BufferedOutputStream(
                new ByteArrayOutputStream())

        mockErr = new BufferedOutputStream(
                new ByteArrayOutputStream())

        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)


        Groovysh groovysh = new Groovysh(testio)

        def filemock = new File('aaaa') {
            @Override
            boolean delete() {
                return true
            }

            @Override
            boolean isFile() {
                return true
            }
        }
        groovysh.history = new FileHistory(filemock)
        InteractiveShellRunner shellRun = new InteractiveShellRunner(groovysh, { '>'})
        // setup completers in run()
        shellRun.run()
        completers = shellRun.reader.completers
    }

    void testEmpty() {
        def result = complete('', 0)
        assert HelpCommand.COMMAND_NAME + ' ' in result[0]
        assert ExitCommand.COMMAND_NAME in result[0]
        assert 'import ' in result[0]
        assert ShowCommand.COMMAND_NAME + ' ' in result[0]
        assert SetCommand.COMMAND_NAME + ' ' in result[0]
        assert InspectCommand.COMMAND_NAME + ' ' in result[0]
        assert DocCommand.COMMAND_NAME + ' ' in result[0]
        assert 0 == result[1]
    }

    void testExitEdit() {
        assert [[ExitCommand.COMMAND_NAME, ':e', EditCommand.COMMAND_NAME], 0] == complete(':e', 0)
    }

    void testShow() {
        String prompt = ':show '
        assert [['all', 'classes', 'imports', 'preferences', 'variables'], prompt.length()] == complete(prompt, prompt.length())
    }

    void testShowV() {
        String prompt = ShowCommand.COMMAND_NAME + ' v'
        assert [['variables'], prompt.length() - 1] == complete(prompt, prompt.length())
    }

    void testShowVariables() {
        String prompt = ShowCommand.COMMAND_NAME + ' variables '
        assert [] == complete(prompt, prompt.length())
    }

    void testImportJava() {
        // tests interaction with ReflectionCompleter
        String prompt = 'import j'
        def result = complete(prompt, prompt.length())
        assert result
        assert prompt.length() - 1 == result[1]
        assert 'java.' in result[0]
    }

    void testShowVariablesJava() {
        // tests against interaction with ReflectionCompleter
        String prompt = ShowCommand.COMMAND_NAME + ' variables java'
        assert [] == complete(prompt, prompt.length())
    }

    void testKeyword() {
        // tests against interaction with ReflectionCompleter
        String prompt = 'pub'
        assert [['public '], 0] == complete(prompt, prompt.length())
    }

    void testCommandAndKeyword() {
        // tests against interaction with ReflectionCompleter
        String prompt = ':pu' // purge, public
        assert [["${PurgeCommand.COMMAND_NAME} "], 0] == complete(prompt, prompt.length())
    }

    void testDoc() {
        String prompt = DocCommand.COMMAND_NAME + ' j'
        def result = complete(prompt, prompt.length())
        assert result
        assert prompt.length() - 1 == result[1]
        assert 'java.' in result[0]
    }

}
