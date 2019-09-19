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
package org.apache.groovy.groovysh.util

import org.apache.groovy.groovysh.Command
import org.apache.groovy.groovysh.Shell
import org.codehaus.groovy.tools.shell.util.Logger

/**
 * Registers {@link Command} classes from an XML file like:
 *
 * <pre>{@literal
 <commands>
   <!-- default commands -->
   <command>org.codehaus.groovy.tools.shell.commands.HelpCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.ExitCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.ImportCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.DisplayCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.ClearCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.ShowCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.InspectCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.PurgeCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.EditCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.LoadCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.SaveCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.RecordCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.HistoryCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.AliasCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.SetCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.ShadowCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.RegisterCommand</command>
   <command>org.codehaus.groovy.tools.shell.commands.DocCommand</command>
   <!-- custom commands -->
 </commands>
 *}
 * <pre>
 */
class XmlCommandRegistrar {
    private final Logger log = Logger.create(this.class)

    private final Shell shell

    private final ClassLoader classLoader

    XmlCommandRegistrar(final Shell shell, final ClassLoader classLoader) {
        assert shell != null
        assert classLoader != null

        this.shell = shell
        this.classLoader = classLoader
    }

    void register(final URL url) {
        assert url

        if (log.debugEnabled) {
            log.debug("Registering commands from: $url")
        }

        url.withReader { Reader reader ->
            groovy.util.Node doc = new groovy.xml.XmlParser().parse(reader)

            doc.children().each { groovy.util.Node element ->
                String classname = element.text()

                Class type = classLoader.loadClass(classname)

                Command command = type.newInstance(shell) as Command

                if (log.debugEnabled) {
                    log.debug("Created command '${command.name}': $command")
                }

                shell << command
            }
        }
    }
}
