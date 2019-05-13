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
import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Groovysh
import org.apache.groovy.groovysh.util.PackageHelper
import org.apache.groovy.groovysh.util.SimpleCompleter
import org.codehaus.groovy.tools.shell.util.Preferences

/**
 * The 'set' command, used to set preferences.
 */
class SetCommand extends CommandSupport {
    public static final String COMMAND_NAME = ':set'

    SetCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':=')
    }

    @Override
    protected List<Completer> createCompleters() {
        def loader = {
            Set<String> set = [] as Set<String>

            String[] keys = Preferences.keys()

            keys.each { String key -> set.add(key) }

            set << Preferences.VERBOSITY_KEY
            set << Preferences.EDITOR_KEY
            set << Preferences.PARSER_FLAVOR_KEY
            set << Preferences.SANITIZE_STACK_TRACE_KEY
            set << Preferences.SHOW_LAST_RESULT_KEY
            set << Groovysh.INTERPRETER_MODE_PREFERENCE_KEY
            set << Groovysh.AUTOINDENT_PREFERENCE_KEY
            set << Groovysh.COLORS_PREFERENCE_KEY
            set << Groovysh.METACLASS_COMPLETION_PREFIX_LENGTH_PREFERENCE_KEY
            set << PackageHelper.IMPORT_COMPLETION_PREFERENCE_KEY

            return set.toList()
        }

        return [
                new SimpleCompleter(loader),
                null
        ]
    }

    @Override
    Object execute(final List<String> args) {
        assert args != null

        if (args.size() == 0) {
            def keys = Preferences.keys()

            if (keys.size() == 0) {
                io.out.println('No preferences are set')
                return
            }

            io.out.println('Preferences:')
            keys.each { String key ->
                def keyvalue = Preferences.get(key, null)
                io.out.println("    $key=$keyvalue")
            }
            return
        }

        if (args.size() > 2) {
            fail("Command '$name' requires arguments: <name> [<value>]")
        }

        String name = args[0]
        def value

        if (args.size() == 1) {
            value = true
        } else {
            value = args[1]
        }

        log.debug("Setting preference: $name=$value")

        Preferences.put(name, String.valueOf(value))
    }
}
