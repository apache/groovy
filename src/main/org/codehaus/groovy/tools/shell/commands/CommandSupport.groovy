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

import org.codehaus.groovy.tools.shell.MessageSource
import org.codehaus.groovy.tools.shell.Command;
import org.codehaus.groovy.tools.shell.InteractiveShell
import org.codehaus.groovy.tools.shell.CommandRegistry
import org.codehaus.groovy.tools.shell.IO
import org.codehaus.groovy.tools.shell.ShellLog

import org.codehaus.groovy.tools.shell.completor.SimpleCompletor

import jline.Completor
import jline.NullCompletor
import jline.ArgumentCompletor

/**
 * Support for {@link Command} instances.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
abstract class CommandSupport
    implements Command
{
    /** Instance logger for the command, initialized late to include the command name. */
    protected final ShellLog log

    /** i18n message source for the command. */
    protected final MessageSource messages = new MessageSource(this.class)

    /** The name of the command. */
    final String name

    /** The shortcut switch */
    final String shortcut

    /** The owning shell. */
    protected final InteractiveShell shell

    /** Provides the command instance with the registry, for aliasing support. */
    protected final CommandRegistry registry

    /** The I/O container for the command to spit stuff out. */
    protected final IO io

    protected CommandSupport(final InteractiveShell shell, final String name, final String shortcut) {
        assert shell
        assert name
        assert shortcut
        
        this.log = new ShellLog(this.class, name)
        this.shell = shell
        this.registry = shell.registry
        this.io = shell.io
        this.name = name
        this.shortcut = shortcut
    }

    String getDescription() {
        return messages['command.description']
    }

    String getUsage() {
        return messages['command.usage']
    }

    String getHelp() {
        return messages['command.help']
    }

    /**
     * Override to provide custom completion semantics for the command.
     */
    protected List createCompletors() {
        return null
    }

    /**
     * Setup the completor for the command.
     */
    Completor getCompletor() {
        def list = []
        list << new SimpleCompletor(name, shortcut)

        def completors = createCompletors()
        if (completors) {
            completors.each {
                if (it) {
                    list << it
                }
                else {
                    list << new NullCompletor()
                }
            }
        }
        else {
            list << new NullCompletor()
        }

        return new ArgumentCompletor(list)
    }
}