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
package org.apache.groovy.groovysh.commands

import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Shell

/**
 * Tests for the {@link RegisterCommand} class.
 */
class RegisterCommandTest extends CommandTestSupport {
    void testRegister() {
        shell.execute(RegisterCommand.COMMAND_NAME + ' org.apache.groovy.groovysh.commands.EchoCommand')
    }

    void testRegisterDupes() {
        shell.execute(RegisterCommand.COMMAND_NAME + ' org.apache.groovy.groovysh.commands.EchoCommand')
        shell.execute(RegisterCommand.COMMAND_NAME + ' org.apache.groovy.groovysh.commands.EchoCommand echo2 \\e2')
    }

    void testRegisterDupesFail() {
        shell.execute(RegisterCommand.COMMAND_NAME + ' org.apache.groovy.groovysh.commands.EchoCommand')
        shell.execute(RegisterCommand.COMMAND_NAME + ' org.apache.groovy.groovysh.commands.EchoCommand')
    }

    void testRegisterFail() {
            shell.execute(RegisterCommand.COMMAND_NAME)
        }
}

class EchoCommand extends CommandSupport {
    EchoCommand(final Shell shell, final String name, final String alias) {
        super(shell, name, alias)
    }

    EchoCommand(final Shell shell) {
        super(shell, ':echo', ':ec')
    }

    Object execute(final List args) {
        io.out.println(args.join(' ')) //  TODO: i18n
    }
}
