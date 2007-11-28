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

package org.codehaus.groovy.tools.shell.commands

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Command
import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.CommandRegistry
import org.codehaus.groovy.tools.shell.util.SimpleCompletor

/**
 * The 'help' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class HelpCommand
    extends CommandSupport
{
    HelpCommand(final Shell shell) {
        super(shell, 'help', '\\h')

        alias('?', '\\?')
    }

    protected List createCompletors() {
        return [
            new HelpCommandCompletor(registry),
            null
        ]
    }

    Object execute(final List args) {
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
        
        Command command = registry[name]
        if (!command) {
            fail("No such command: $name") // TODO: i18n
        }
        
        io.out.println()
        io.out.println("usage: @|bold ${command.name}| $command.usage") // TODO: i18n
        io.out.println()
        io.out.println(command.help)
        io.out.println()
    }

    private void list() {
        // Figure out the max command name and shortcut length dynamically
        int maxName = 0
        int maxShortcut
        
        for (command in registry) {
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
        io.out.println('For information about @|green Groovy|, visit:') // TODO: i18n
        io.out.println('    @|cyan http://groovy.codehaus.org| ') // FIXME: parsing freaks out if end tok is at the last char...
        io.out.println()

        // List the commands we know about
        io.out.println('Available commands:') // TODO: i18n

        for (command in registry) {
            if (command.hidden) {
                continue
            }
            
            def n = command.name.padRight(maxName, ' ')
            def s = command.shortcut.padRight(maxShortcut, ' ')
            
            //
            // TODO: Wrap description if needed
            //
            
            def d = command.description
            
            io.out.println("  @|bold ${n}|  (@|bold ${s}|) $d")
        }
        
        io.out.println()
        io.out.println('For help on a specific command type:') // TODO: i18n
        io.out.println('    help @|bold command| ')
        io.out.println()
    }
}

/**
 * Completor for the 'help' command.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class HelpCommandCompletor
    extends SimpleCompletor
{
    private final CommandRegistry registry

    HelpCommandCompletor(final CommandRegistry registry) {
        assert registry

        this.registry = registry
    }

    SortedSet getCandidates() {
        def set = new TreeSet()

        for (command in registry) {
            if (command.hidden) {
                continue
            }
            
            set << command.name
            set << command.shortcut
        }

        return set
    }
}