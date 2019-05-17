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

import org.apache.groovy.groovysh.commands.AliasCommand
import org.apache.groovy.groovysh.commands.ClearCommand
import org.apache.groovy.groovysh.commands.SaveCommand
import org.apache.groovy.groovysh.commands.SetCommand
import org.apache.groovy.groovysh.commands.ShowCommand
import org.codehaus.groovy.tools.shell.util.Preferences

class CommandCompleterTest extends CompleterTestSupport {

    void testEmpty() {
        CommandsMultiCompleter completer = new CommandsMultiCompleter()
        assert -1 == completer.complete('', 0, [])
        assert -1 == completer.complete('i', 2, [])
        assert -1 == completer.complete('imp', 4, [])
    }

    void testAlias() {
        CommandsMultiCompleter completer = new CommandsMultiCompleter()
        def candidates = []
        CommandRegistry registry = new CommandRegistry()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            AliasCommand aliasCommand = new AliasCommand(groovyshMock)
            registry.register(new SetCommand(groovyshMock))
            registry.register(new ShowCommand(groovyshMock))
            aliasCommand.registry = registry
            completer.add(aliasCommand)
            completer.refresh()

            assert 0 == completer.complete(':a', ':a'.length(), candidates)
            assert [':a ', AliasCommand.COMMAND_NAME + ' '] == candidates
            candidates = []
            assert 3 == completer.complete(':a ', ':a '.length(), candidates)
            assert [':= ', ':S ', SetCommand.COMMAND_NAME + ' ', ShowCommand.COMMAND_NAME + ' '] == candidates
        }
    }

    void testSet() {
        CommandsMultiCompleter completer = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.demand.getINTERPRETER_MODE_PREFERENCE_KEY(1){'interpreterMode'}
        groovyshMocker.demand.getAUTOINDENT_PREFERENCE_KEY(1){'autoindent'}
        groovyshMocker.demand.getCOLORS_PREFERENCE_KEY(1){'colors'}
        groovyshMocker.demand.getMETACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY(1){'meta-completion-prefix-length'}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completer.add(new SetCommand(groovyshMock))
            completer.refresh()

            assert 0 == completer.complete(':s', ':s'.length(), candidates)
            String buffer = SetCommand.COMMAND_NAME + ' '
            assert [buffer] == candidates
            candidates = []
            assert 5 == completer.complete(buffer, buffer.length(), candidates)
        }
        assert Groovysh.AUTOINDENT_PREFERENCE_KEY + ' ' in candidates
        assert Groovysh.COLORS_PREFERENCE_KEY + ' ' in candidates
        assert Groovysh.METACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY + ' ' in candidates
        assert Preferences.EDITOR_KEY + ' ' in candidates
        assert Preferences.PARSER_FLAVOR_KEY + ' ' in candidates
        assert Preferences.VERBOSITY_KEY + ' ' in candidates
        assert Preferences.SANITIZE_STACK_TRACE_KEY + ' ' in candidates
        assert Preferences.SHOW_LAST_RESULT_KEY + ' ' in candidates
        assert Preferences.VERBOSITY_KEY + ' ' in candidates
    }

    void testSave() {
        CommandsMultiCompleter completer = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completer.add(new SaveCommand(groovyshMock))
            completer.refresh()

            assert 0 == completer.complete(':s', ':s'.length(), candidates)
            assert [':s ', SaveCommand.COMMAND_NAME + ' '] == candidates
            candidates = []
            String buffer = SaveCommand.COMMAND_NAME + ' '
            assert 6 == completer.complete(buffer, buffer.length(), candidates)
            assert candidates.size() > 0 // completes filenames from testing dir
        }
    }

    void testClear() {
        CommandsMultiCompleter completer = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completer.add(new ClearCommand(groovyshMock))
            completer.refresh()

            assert 0 == completer.complete(':c', ':c'.length(), candidates)
            assert [':c', ClearCommand.COMMAND_NAME] == candidates
            candidates = []
            assert -1 == completer.complete(':c ', ':c '.length(), candidates)
            assert [] == candidates

        }

    }

    void testSaveSetShow() {
        CommandsMultiCompleter completer = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.demand.getINTERPRETER_MODE_PREFERENCE_KEY(1){'interpreterMode'}
        groovyshMocker.demand.getAUTOINDENT_PREFERENCE_KEY(1){'autoindent'}
        groovyshMocker.demand.getCOLORS_PREFERENCE_KEY(1){'colors'}
        groovyshMocker.demand.getMETACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY(1){'meta-completion-prefix-length'}
        groovyshMocker.demand.getIo(1){testio}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completer.add(new SaveCommand(groovyshMock))
            completer.add(new SetCommand(groovyshMock))
            completer.add(new ShowCommand(groovyshMock))
            completer.refresh()

            assert 0 == completer.complete(':s', ':s'.length(), candidates)
            assert [':s ', SaveCommand.COMMAND_NAME + ' ', SetCommand.COMMAND_NAME + ' ', ShowCommand.COMMAND_NAME + ' '] == candidates
            candidates = []
            String buffer = SaveCommand.COMMAND_NAME + ' '
            assert 6 == completer.complete(buffer, buffer.length(), candidates)
            assert ! candidates.contains('all')
            candidates = []
            assert 3 == completer.complete(':s ', ':s '.length(), candidates)
            assert ! candidates.contains('all')

        }

    }
}
