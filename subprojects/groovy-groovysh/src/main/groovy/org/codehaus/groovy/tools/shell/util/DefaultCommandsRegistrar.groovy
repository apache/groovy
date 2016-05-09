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
package org.codehaus.groovy.tools.shell.util

import org.codehaus.groovy.tools.shell.Command
import org.codehaus.groovy.tools.shell.Shell
import org.codehaus.groovy.tools.shell.commands.*

/**
 * Registers {@link Command} classes from an XML file like:
 * <commands>
 *  <command>org.codehaus.groovy.tools.shell.commands.HelpCommand</command>
 * ...
 * </commands>
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class DefaultCommandsRegistrar
{

    private final Shell shell

    DefaultCommandsRegistrar(final Shell shell) {
        assert shell != null

        this.shell = shell
    }

    void register() {

        for (Command classname in [
                new HelpCommand(shell),
                new ExitCommand(shell),
                new ImportCommand(shell),
                new DisplayCommand(shell),
                new ClearCommand(shell),
                new ShowCommand(shell),
                new InspectCommand(shell),
                new PurgeCommand(shell),
                new EditCommand(shell),
                new LoadCommand(shell),
                new SaveCommand(shell),
                new RecordCommand(shell),
                new HistoryCommand(shell),
                new AliasCommand(shell),
                new SetCommand(shell),
                new GrabCommand(shell),
                new RescanCommand(shell),
                // does not do anything
                //new ShadowCommand(shell),
                new RegisterCommand(shell),
                new DocCommand(shell)
        ]) {
            shell.register(classname)
        }

    }
}
