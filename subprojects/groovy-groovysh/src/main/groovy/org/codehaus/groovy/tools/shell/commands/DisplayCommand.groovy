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

import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * The 'display' command. Displays the current buffer (e.g. while method declaration was not finished).
 */
@Deprecated
class DisplayCommand
    extends CommandSupport
{
    public static final String COMMAND_NAME = ':display'

    DisplayCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':d')
    }

    @Override
    Object execute(final List<String> args) {
        assertNoArguments(args)

        if (buffer.isEmpty()) {
            io.out.println('Buffer is empty') // TODO: i18n
        }
        else {
            shell.displayBuffer(buffer)
        }
    }
}
