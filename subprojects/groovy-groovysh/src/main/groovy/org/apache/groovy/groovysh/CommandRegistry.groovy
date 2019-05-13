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

import org.codehaus.groovy.tools.shell.util.Logger

/**
 * A registry of shell {@link Command} instances which may be executed.
 */
class CommandRegistry
{
    protected final Logger log = Logger.create(CommandRegistry)

    //
    // TODO: Hook up support so one can for (command in registry) { }
    //

    /** A list of all of the registered commands. */
    final List<Command> commandList = []

    /** A set of all of the command names and shortcuts to ensure they are unique. */
    private final Set<String> names = new TreeSet<String>()

    Command register(final Command command) {
        assert command

        // Make sure that the command name and shortcut are unique
        assert !names.contains(command.name) : "Duplicate command name: $command.name"
        names << command.name

        assert !names.contains(command.shortcut) : "Duplicate command shortcut: $command.shortcut"
        names << command.shortcut

        // Hold on to the command in order
        commandList << command

        // Hookup context for alias commands
        if (command instanceof CommandSupport) {
            ((CommandSupport) command).registry = this
        }

        // Add any standard aliases for the command if any
        command.aliases?.each {Command it -> this.register(it) }

        if (log.debugEnabled) {
            log.debug("Registered command: $command.name")
        }

        return command
    }

    Command find(final String name) {
        assert name

        for (c in commandList) {
            if (name in [ c.name, c.shortcut ]) {
                return c
            }
            // also allow :import
            if (!c.name.startsWith(':') && name.equals(':' + c.name)) {
                return c
            }
        }

        return null
    }

    void remove(final Command command) {
        assert command

        commandList.remove(command)

        names.remove(command.name)
        names.remove(command.shortcut)

        if (log.debugEnabled) {
            log.debug("Removed command: $command.name")
        }
    }

    List<Command> commands() {
        return commandList
    }

    Command getProperty(final String name) {
        return find(name)
    }

    Iterator iterator() {
        return commands().iterator()
    }
}
