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
import org.codehaus.groovy.tools.shell.commands.AliasCommand
import org.codehaus.groovy.tools.shell.commands.ClearCommand
import org.codehaus.groovy.tools.shell.commands.DisplayCommand
import org.codehaus.groovy.tools.shell.commands.DocCommand
import org.codehaus.groovy.tools.shell.commands.EditCommand
import org.codehaus.groovy.tools.shell.commands.ExitCommand
import org.codehaus.groovy.tools.shell.commands.GrabCommand
import org.codehaus.groovy.tools.shell.commands.HelpCommand
import org.codehaus.groovy.tools.shell.commands.HistoryCommand
import org.codehaus.groovy.tools.shell.commands.ImportCommand
import org.codehaus.groovy.tools.shell.commands.InspectCommand
import org.codehaus.groovy.tools.shell.commands.LoadCommand
import org.codehaus.groovy.tools.shell.commands.PurgeCommand
import org.codehaus.groovy.tools.shell.commands.RecordCommand
import org.codehaus.groovy.tools.shell.commands.RegisterCommand
import org.codehaus.groovy.tools.shell.commands.SaveCommand
import org.codehaus.groovy.tools.shell.commands.SetCommand
import org.codehaus.groovy.tools.shell.commands.ShowCommand

/**
 * Registers {@link Command} classes from an XML file like:
 * <commands>
 *  <command>org.codehaus.groovy.tools.shell.commands.HelpCommand</command>
 * ...
 * </commands>
 */
@Deprecated
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
                // does not do anything
                //new ShadowCommand(shell),
                new RegisterCommand(shell),
                new DocCommand(shell)
        ]) {
            shell.register(classname)
        }

    }
}
