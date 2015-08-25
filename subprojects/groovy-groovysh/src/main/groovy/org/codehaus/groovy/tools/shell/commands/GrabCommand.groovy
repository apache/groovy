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

import jline.console.completer.Completer
import org.codehaus.groovy.tools.shell.CommandSupport
import org.codehaus.groovy.tools.shell.Groovysh

/**
 * The 'grab' command.
 *
 * @author Thibault Kruse
 */
class GrabCommand
    extends CommandSupport
{
    public static final String COMMAND_NAME = ':grab'

    GrabCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':g')
    }

    @Override
    protected List<Completer> createCompleters() {
        return []
    }

    @Override
    Object execute(final List<String> args) {
        assert args != null

        if (args.size() == 0) {
            fail("Command '$COMMAND_NAME' requires at least one argument") // TODO: i18n
        }
        String specString = buildSpec(args)

        log.debug("Attempting to grab: \"$specString\"")
        shell.execute("groovy.grape.Grape.grab($specString)")
        shell.packageHelper.reset()
    }

    protected String buildSpec(List<String> args) {
        Map<String, String> spec = [:]
        List<String> splitArgs
        if (args.size() == 1 && args[0].contains(':')) {
            splitArgs = args[0].split(':')
        } else {
            splitArgs = args
        }
        if (splitArgs.size() == 1) {
            spec['group'] = ''
            spec['module'] = splitArgs[0]
            spec['version'] = ''
        } else if (splitArgs.size() == 2) {
            spec['group'] = splitArgs[0]
            spec['module'] = splitArgs[1]
            spec['version'] = ''
        } else if (splitArgs.size() == 3) {
            spec['group'] = splitArgs[0]
            spec['module'] = splitArgs[1]
            spec['version'] = splitArgs[2]
        } else {
            fail("Command '$COMMAND_NAME' takes one to three arguments") // TODO: i18n
        }
        return spec.collect({String k, String v -> "$k:'$v'"}).join(', ')
    }
}
