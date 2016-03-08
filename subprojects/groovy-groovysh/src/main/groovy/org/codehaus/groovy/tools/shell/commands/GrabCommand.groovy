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

import java.util.prefs.PreferenceChangeEvent
import groovy.grape.Grape
import org.codehaus.groovy.tools.GrapeUtil
import org.codehaus.groovy.tools.shell.*
import org.codehaus.groovy.tools.shell.util.PackageHelper
import jline.console.completer.*

/**
 * The 'grab' command.
 *
 * @author <a href="mailto:jake.gage@gmail.com">Jake Gage</a>
 */
class GrabCommand
    extends CommandSupport
{
    public static final String COMMAND_NAME = ':grab'

    private static final java.util.prefs.Preferences PREFERENCES =
        java.util.prefs.Preferences.userRoot().node('/org/codehaus/groovy/tools/shell')

    public GrabCommand(org.codehaus.groovy.tools.shell.Groovysh shell) {
        super(shell, COMMAND_NAME, ':g')
    }

    @Override protected List<Completer> createCompleters() {
        [ new FileNameCompleter(), null ]
    }

    @Override Object execute(List<String> args) {
        validate(args)
        grab(getDependency(args))
        if ( importCompletionEnabled() ) { reloadImportCompletion() }
    }

    private void validate(List<String> args) {
        if ( args?.size() != 1 && args?.size() != 3 ) {
            fail("usage: @|bold ${COMMAND_NAME}|@ ${usage}")
        }
    }

    private String getDependency(List<String> args) {
        if ( args.size() == 3 ) {
            def (group, name, version) = args
            "${group}:${name}:${version}"
        } else {
            args[0]
        }
    }

    private void grab(String dependency) {
        Map<String, Object> dependencyMap = GrapeUtil.getIvyParts(dependency)
        Grape.grab([classLoader: shell.interp.classLoader.parent,
                    refObject: shell.interp],
                   dependencyMap)
    }

    private Boolean importCompletionEnabled() {
        ! Boolean.valueOf(org.codehaus.groovy.tools.shell.util.Preferences.get(PackageHelper.IMPORT_COMPLETION_PREFERENCE_KEY))
    }

    private void reloadImportCompletion() {
        shell.packageHelper.preferenceChange(ignorePackageCompletion(true))
        shell.packageHelper.preferenceChange(ignorePackageCompletion(false))
    }

    private PreferenceChangeEvent ignorePackageCompletion(Boolean complete) {
        new PreferenceChangeEvent(PREFERENCES,
                                  PackageHelper.IMPORT_COMPLETION_PREFERENCE_KEY,
                                  complete as String)
    }

}
