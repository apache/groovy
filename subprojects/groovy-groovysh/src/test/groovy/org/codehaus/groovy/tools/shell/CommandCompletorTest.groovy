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

import org.codehaus.groovy.tools.shell.commands.*
import org.codehaus.groovy.tools.shell.util.Preferences

class CommandCompletorTest
extends CompletorTestSupport {

    void testEmpty() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        assert -1 == completor.complete('', 0, [])
        assert -1 == completor.complete('i', 2, [])
        assert -1 == completor.complete('imp', 4, [])
    }

    void testAlias() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        CommandRegistry registry = new CommandRegistry()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            AliasCommand aliasCommand = new AliasCommand(groovyshMock)
            registry.register(new SetCommand(groovyshMock))
            registry.register(new ShowCommand(groovyshMock))
            aliasCommand.registry = registry
            completor.add(aliasCommand)
            completor.refresh()

            assert 0 == completor.complete(':a', ':a'.length(), candidates)
            assert [':a ', AliasCommand.COMMAND_NAME + ' '] == candidates
            candidates = []
            assert 3 == completor.complete(':a ', ':a '.length(), candidates)
            assert [':= ', ':S ', SetCommand.COMMAND_NAME + ' ', ShowCommand.COMMAND_NAME + ' '] == candidates
        }
    }

    void testSet() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.demand.getINTERPRETER_MODE_PREFERENCE_KEY(1){'interpreterMode'}
        groovyshMocker.demand.getAUTOINDENT_PREFERENCE_KEY(1){'autoindent'}
        groovyshMocker.demand.getCOLORS_PREFERENCE_KEY(1){'colors'}
        groovyshMocker.demand.getMETACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY(1){'meta-completion-prefix-length'}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completor.add(new SetCommand(groovyshMock))
            completor.refresh()

            assert 0 == completor.complete(':s', ':s'.length(), candidates)
            String buffer = SetCommand.COMMAND_NAME + ' '
            assert [buffer] == candidates
            candidates = []
            assert 5 == completor.complete(buffer, buffer.length(), candidates)
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
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completor.add(new SaveCommand(groovyshMock))
            completor.refresh()

            assert 0 == completor.complete(':s', ':s'.length(), candidates)
            assert [':s ', SaveCommand.COMMAND_NAME + ' '] == candidates
            candidates = []
            String buffer = SaveCommand.COMMAND_NAME + ' '
            assert 6 == completor.complete(buffer, buffer.length(), candidates)
            assert candidates.size() > 0 // completes filenames from testing dir
        }
    }

    void testClear() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completor.add(new ClearCommand(groovyshMock))
            completor.refresh()

            assert 0 == completor.complete(':c', ':c'.length(), candidates)
            assert [':c', ClearCommand.COMMAND_NAME] == candidates
            candidates = []
            assert -1 == completor.complete(':c ', ':c '.length(), candidates)
            assert [] == candidates

        }

    }

    void testSaveSetShow() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.demand.getINTERPRETER_MODE_PREFERENCE_KEY(1){'interpreterMode'}
        groovyshMocker.demand.getAUTOINDENT_PREFERENCE_KEY(1){'autoindent'}
        groovyshMocker.demand.getCOLORS_PREFERENCE_KEY(1){'colors'}
        groovyshMocker.demand.getMETACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY(1){'meta-completion-prefix-length'}
        groovyshMocker.demand.getIo(1){testio}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completor.add(new SaveCommand(groovyshMock))
            completor.add(new SetCommand(groovyshMock))
            completor.add(new ShowCommand(groovyshMock))
            completor.refresh()

            assert 0 == completor.complete(':s', ':s'.length(), candidates)
            assert [':s ', SaveCommand.COMMAND_NAME + ' ', SetCommand.COMMAND_NAME + ' ', ShowCommand.COMMAND_NAME + ' '] == candidates
            candidates = []
            String buffer = SaveCommand.COMMAND_NAME + ' '
            assert 6 == completor.complete(buffer, buffer.length(), candidates)
            assert ! candidates.contains('all')
            candidates = []
            assert 3 == completor.complete(':s ', ':s '.length(), candidates)
            assert ! candidates.contains('all')

        }

    }
}
