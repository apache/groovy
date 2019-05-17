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
package org.apache.groovy.groovysh.commands

import jline.console.completer.Completer
import org.apache.groovy.groovysh.Groovysh
import org.apache.groovy.groovysh.util.PackageHelper
import org.codehaus.groovy.tools.shell.util.Preferences

/**
 * Tests for the {@link SetCommand} class.
 */
class SetCommandTest extends CommandTestSupport {
    void testSet() {
        shell.execute(SetCommand.COMMAND_NAME)
    }

    void testComplete() {

        List<String> candidates = []
        SetCommand command = new SetCommand(shell)
        List<Completer> completers = command.createCompleters()
        assert 2 == completers.size()
        assert 0 == completers[0].complete('', 0, candidates)
        assert Groovysh.AUTOINDENT_PREFERENCE_KEY + ' ' in candidates
        assert PackageHelper.IMPORT_COMPLETION_PREFERENCE_KEY + ' ' in candidates
        assert Preferences.EDITOR_KEY + ' ' in candidates
        assert Preferences.PARSER_FLAVOR_KEY + ' ' in candidates
        assert Preferences.SANITIZE_STACK_TRACE_KEY + ' ' in candidates
        assert Preferences.SHOW_LAST_RESULT_KEY + ' ' in candidates
        assert Preferences.VERBOSITY_KEY + ' ' in candidates
    }
}
