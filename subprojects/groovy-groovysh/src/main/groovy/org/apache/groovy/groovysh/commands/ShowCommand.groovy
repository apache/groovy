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

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.MethodClosure
import org.codehaus.groovy.tools.shell.ComplexCommandSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.util.Preferences

/**
 * The 'show' command.
 */
class ShowCommand
    extends ComplexCommandSupport
{
    public static final String COMMAND_NAME = ':show'

    ShowCommand(final Groovysh shell) {
        super(shell, COMMAND_NAME, ':S', [ 'variables', 'classes', 'imports', 'preferences', 'all' ])
    }

    def do_variables = {
        if (variables.isEmpty()) {
            io.out.println('No variables defined') // TODO: i18n
        }
        else {
            io.out.println('Variables:') // TODO: i18n

            variables.each { key, value ->
                // Special handling for defined methods, just show the sig
                if (value instanceof MethodClosure) {
                    //
                    // TODO: Would be nice to show the argument types it will accept...
                    //
                    value = "method ${value.method}()"
                }

                io.out.println("  $key = ${InvokerHelper.toString(value)}")
            }
        }
    }

    def do_classes = {
        Class[] classes = classLoader.loadedClasses

        if (classes.size() == 0) {
            io.out.println('No classes have been loaded') // TODO: i18n
        }
        else {
            io.out.println('Classes:') // TODO: i18n

            classes.each { Class classIt ->
                io.out.println("  $classIt")
            }
        }
    }

    def do_imports = {
        if (imports.isEmpty()) {
            io.out.println('No custom imports have been defined') // TODO: i18n
        }
        else {
            io.out.println('Custom imports:') // TODO: i18n

            imports.each {String importIt ->
                io.out.println("  $importIt")
            }
        }
    }

    def do_preferences = {
        String[] keys = Preferences.keys()

        if (keys.size() == 0) {
            io.out.println('No preferences are set')
            return
        }

        io.out.println('Preferences:')
        keys.each { String key ->
            def value = Preferences.get(key, null)
            io.out.println("    $key=$value")
        }
        return
    }
}

