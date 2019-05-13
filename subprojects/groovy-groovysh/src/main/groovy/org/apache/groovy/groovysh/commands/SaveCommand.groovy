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

import jline.console.completer.Completer
import jline.console.completer.FileNameCompleter
import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Groovysh

/**
 * The 'save' command.
 */
class SaveCommand extends CommandSupport {
    public static final String COMMAND_NAME = ':save'

    SaveCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':s')
    }

    @Override
    protected List<Completer> createCompleters() {
        return [
            new FileNameCompleter(),
            null
        ]
    }

    @Override
    Object execute(final List<String> args) {
        assert args != null

        if (args.size() != 1) {
            fail("Command '$COMMAND_NAME' requires a single file argument") // TODO: i18n
        }

        if (buffer.isEmpty()) {
            io.out.println('Buffer is empty') // TODO: i18n
            return
        }

        //
        // TODO: Support special '-' file to simply dump text to io.out
        //

        def file = new File("${args[0]}")

        if (io.verbose) {
            io.out.println("Saving current buffer to file: \"$file\"") // TODO: i18n
        }

        def dir = file.parentFile
        if (dir && !dir.exists()) {
            log.debug("Creating parent directory path: \"$dir\"")

            dir.mkdirs()
        }

        file.write(buffer.join(NEWLINE))
    }
}
