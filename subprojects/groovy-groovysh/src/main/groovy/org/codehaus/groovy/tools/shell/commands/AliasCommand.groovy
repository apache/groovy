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

import jline.console.completer.Completer
import org.codehaus.groovy.tools.shell.Command
import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.completion.CommandNameCompleter

/**
 * The 'alias' command.
 */
class AliasCommand
    extends CommandSupport
{
    public static final String COMMAND_NAME = ':alias'

    AliasCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':a', )
    }

    @Override
    protected List<Completer> createCompleters() {
        return [
                new CommandNameCompleter(registry, true),
                null
        ]
    }

    @Override
    Object execute(final List<String> args) {
        assert args != null

        if (args.size() < 2) {
            fail("Command 'alias' requires at least 2 arguments") // TODO: i18n
        }

        String name = args[0]
        List target = args[1..-1]

        Command command = registry.find(name)

        if (command == null) {
            command = registry.find(name)
        }
        if (command != null) {
            if (command instanceof AliasTargetProxyCommand) {
                log.debug("Rebinding alias: $name")

                registry.remove(command)
            }
            else {
                fail("Can not rebind non-user aliased command: ${command.name}") // TODO: i18n
            }
        }

        log.debug("Creating alias '$name' to: $target")

        // Register the command
        command = shell << new AliasTargetProxyCommand(shell, name, target)

        //
        // TODO: Should this be here... or should this be in the Shell's impl?
        //

        // Try to install the completor
        if (shell.runner) {
            shell.runner.completer.add(command)
        }
    }
}

class AliasTargetProxyCommand
    extends CommandSupport implements Command
{
    private static int counter = 0

    final List<String> args

    AliasTargetProxyCommand(final Groovysh shell, final String name, final List args) {
        super(shell, name, ':a' + counter++)

        assert args

        this.args = args
    }

    @Override
    String getDescription() {
        return "User defined alias to: @|bold ${args.join(' ')}|@"
    }

    @Override
    String getUsage() {
        return ''
    }

    @Override
    String getHelp() {
        return description
    }

    @Override
    Object execute(final List<String> args) {
        List<String> allArgs = this.args + args

        log.debug("Executing with args: $allArgs")

        //
        // FIXME: Should go back through shell.execute() to allow aliases to groovy snips too
        //

        return shell.executeCommand(allArgs.join(' '))
    }
}
