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

import jline.console.completer.Completer

/**
 * Provides simple command aliasing.
 */
class CommandAlias
    extends CommandSupport
{
    final String targetName

    CommandAlias(final Groovysh shell, final String name, final String shortcut, final String target) {
        super(shell, name, shortcut)

        assert target

        this.targetName = target
    }

    Command getTarget() {
        Command command = registry.find(targetName)

        assert command != null

        return command
    }

    @Override
    protected List<Completer> createCompleters() {
        try {
            // TODO: Use interface with createCompleters()
            if (target instanceof CommandSupport) {
                CommandSupport support = (CommandSupport) target
                return support.createCompleters()
            }

        } catch (MissingMethodException) {
            log.warn('Aliased Command without createCompleters Method')
        }
    }

    @Override
    String getDescription() {
        return messages.format('info.alias_to', targetName)
    }

    @Override
    String getUsage() {
        return target.usage
    }

    @Override
    String getHelp() {
        return target.help
    }

    @Override
    boolean getHidden() {
        return target.hidden
    }

    @Override
    Object execute(final List<String> args) {
        target.execute(args)
    }
}
