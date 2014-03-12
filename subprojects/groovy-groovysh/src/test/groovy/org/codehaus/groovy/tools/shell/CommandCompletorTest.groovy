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
import org.codehaus.groovy.tools.shell.commands.AliasCommand
import org.codehaus.groovy.tools.shell.commands.ClearCommand
import org.codehaus.groovy.tools.shell.commands.ImportCommand
import org.codehaus.groovy.tools.shell.commands.SaveCommand
import org.codehaus.groovy.tools.shell.commands.SetCommand
import org.codehaus.groovy.tools.shell.commands.ShowCommand
import org.codehaus.groovy.tools.shell.util.Preferences

class CommandCompletorTest
extends CompletorTestSupport {

    void testEmpty() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        assert -1 == completor.complete("", 0, [])
        assert -1 == completor.complete("i", 2, [])
        assert -1 == completor.complete("imp", 4, [])
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

            assert 0 == completor.complete(":a", ":a".length(), candidates)
            assert [':a', ':alias'] == candidates
            candidates = []
            assert 3 == completor.complete(":a ", ":a ".length(), candidates)
            assert [':=', ':S', ':set', ':show'] == candidates
        }
    }

    void testSet() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.demand.getAUTOINDENT_PREFERENCE_KEY(1){"autoindent"}
        groovyshMocker.demand.getMETACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY(1){"meta-completion-prefix-length"}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completor.add(new SetCommand(groovyshMock))
            completor.refresh()

            assert 0 == completor.complete(":s", ":s".length(), candidates)
            assert [':set '] == candidates
            candidates = []
            assert 5 == completor.complete(":set ", ":set ".length(), candidates)
        }
        assert Groovysh.AUTOINDENT_PREFERENCE_KEY in candidates
        assert Groovysh.METACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY in candidates
        assert Preferences.EDITOR_KEY in candidates
        assert Preferences.PARSER_FLAVOR_KEY in candidates
        assert Preferences.VERBOSITY_KEY in candidates
        assert Preferences.SANITIZE_STACK_TRACE_KEY in candidates
        assert Preferences.SHOW_LAST_RESULT_KEY in candidates
        assert Preferences.VERBOSITY_KEY in candidates
    }

    void testSave() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completor.add(new SaveCommand(groovyshMock))
            completor.refresh()

            assert 0 == completor.complete(":s", ":s".length(), candidates)
            assert [':s', ':save'] == candidates
            candidates = []
            assert 6 == completor.complete(":save ", ":save ".length(), candidates)
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

            assert 0 == completor.complete(":c", ":c".length(), candidates)
            assert [':c', ':clear'] == candidates
            candidates = []
            assert -1 == completor.complete(":c ", ":c ".length(), candidates)
            assert [] == candidates

        }

    }

    void testSaveSetShow() {
        CommandsMultiCompleter completor = new CommandsMultiCompleter()
        def candidates = []
        groovyshMocker.demand.getAUTOINDENT_PREFERENCE_KEY(1){"autoindent"}
        groovyshMocker.demand.getMETACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY(1){"meta-completion-prefix-length"}
        groovyshMocker.demand.getIo(1){testio}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            completor.add(new SaveCommand(groovyshMock))
            completor.add(new SetCommand(groovyshMock))
            completor.add(new ShowCommand(groovyshMock))
            completor.refresh()

            assert 0 == completor.complete(":s", ":s".length(), candidates)
            assert [':s', ':save', ':set ', ':show '] == candidates
            candidates = []
            assert 6 == completor.complete(":save ", ":save ".length(), candidates)
            assert ! candidates.contains('all')
            candidates = []
            assert 3 == completor.complete(":s ", ":s ".length(), candidates)
            assert ! candidates.contains('all')

        }

    }
}