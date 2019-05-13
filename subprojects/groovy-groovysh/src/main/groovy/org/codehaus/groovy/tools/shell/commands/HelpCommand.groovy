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
 * The 'help' command.
 */
@Deprecated
class HelpCommand
    extends CommandSupport
{

    public static final String COMMAND_NAME = ':help'

    HelpCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':h')

        alias('?', ':?')
    }

    protected List<Completer> createCompleters() {
        return [
            new CommandNameCompleter(registry, false),
            null
        ]
    }

    @Override
    Object execute(final List<String> args) {
        assert args != null

        if (args.size() > 1) {
            fail(messages.format('error.unexpected_args', args.join(' ')))
        }

        if (args.size() == 1) {
            help(args[0])
        }
        else {
            list()
        }
    }

    private void help(final String name) {
        assert name

        Command command = registry.find(name)
        if (!command) {
            fail("No such command: $name") // TODO: i18n
        }

        io.out.println()
        io.out.println("usage: @|bold ${command.name}|@ $command.usage") // TODO: i18n
        io.out.println()
        io.out.println(command.help)
        io.out.println()
    }

    private void list() {
        // Figure out the max command name and shortcut length dynamically
        int maxName = 0
        int maxShortcut

        for (Command command in registry.commands()) {
            if (command.hidden) {
                continue
            }

            if (command.name.size() > maxName) {
                maxName = command.name.size()
            }

            if (command.shortcut.size() > maxShortcut) {
                maxShortcut = command.shortcut.size()
            }
        }

        io.out.println()
        io.out.println('For information about @|green Groovy|@, visit:') // TODO: i18n
        io.out.println('    @|cyan http://groovy-lang.org|@ ') // FIXME: parsing freaks out if end tok is at the last char...
        io.out.println()

        // List the commands we know about
        io.out.println('Available commands:') // TODO: i18n

        for (Command command in registry.commands()) {
            if (command.hidden) {
                continue
            }

            def n = command.name.padRight(maxName, ' ')
            def s = command.shortcut.padRight(maxShortcut, ' ')

            //
            // TODO: Wrap description if needed
            //

            def d = command.description

            io.out.println("  @|bold ${n}|@  (@|bold ${s}|@) $d")
        }

        io.out.println()
        io.out.println('For help on a specific command type:') // TODO: i18n
        io.out.println('    :help @|bold command|@ ')
        io.out.println()
    }
}

