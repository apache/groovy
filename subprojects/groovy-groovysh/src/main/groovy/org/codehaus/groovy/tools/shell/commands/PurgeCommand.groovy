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

import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.util.Preferences

/**
 * The 'purge' command.
 */
@Deprecated
class PurgeCommand
    extends ComplexCommandSupport
{
    public static final String COMMAND_NAME = ':purge'

    PurgeCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':p', [ 'variables', 'classes', 'imports', 'preferences', 'all' ])
    }

    def do_variables = {
        if (variables.isEmpty()) {
            io.out.println('No variables defined') // TODO: i18n
        }
        else {
            variables.clear()

            if (io.verbose) {
                io.out.println('Custom variables purged') // TODO: i18n
            }
        }
    }

    def do_classes = {
        if (classLoader.loadedClasses.size() == 0) {
            io.out.println('No classes have been loaded') // TODO: i18n
        }
        else {
            classLoader.clearCache()

            if (io.verbose) {
                io.out.println('Loaded classes purged') // TODO: i18n
            }
        }
    }

    def do_imports = {
        if (imports.isEmpty()) {
            io.out.println('No custom imports have been defined') // TODO: i18n
        }
        else {
            imports.clear()

            if (io.verbose) {
                io.out.println('Custom imports purged') // TODO: i18n
            }
        }
    }

    def do_preferences = {
        Preferences.clear()

        if (io.verbose) {
            io.out.println('Preferences purged') // TODO: i18n
        }
    }
}

