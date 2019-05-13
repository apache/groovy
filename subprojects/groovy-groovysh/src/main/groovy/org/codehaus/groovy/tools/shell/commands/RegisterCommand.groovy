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

import org.codehaus.groovy.tools.shell.Command
import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * The 'register' command.
 * Registers a class as a new groovysh command.
 * Requires the command to have matching constructors (shell) or (shell, name, alias).
 */
@Deprecated
class RegisterCommand
    extends CommandSupport
{
    static final String COMMAND_NAME = ':register'

    RegisterCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':rc')
    }

    @Override
    Object execute(final List<String> args) {
        assert args != null

        if (args.size() < 1) {
            fail("Command '$COMMAND_NAME' requires at least 1 arguments") // TODO: i18n
        }

        String classname = args.get(0)

        Class type = classLoader.loadClass(classname)

        Command command = null

        if (args.size() == 1) {                   // use default name
            command = type.newInstance(shell) as Command
        }
        else if (args.size() == 2) {              // pass name to completor
            command = type.newInstance(shell, args.get(1), null) as Command
        }
        else if (args.size() == 3) {              // pass name, alias to completor
            command = type.newInstance(shell, args.get(1), args.get(2)) as Command
        }

        def oldcommand = registry[command.name]   // let's prevent collisions

        if (oldcommand) {
            fail("Can not rebind command: ${command.name}") // TODO: i18n
        }

        if (log.debugEnabled) {
            log.debug("Created command '${command.name}': $command")
        }

        command = shell << command

        if (shell.runner) {
            shell.runner.completer.add(command)
        }
    }
}
