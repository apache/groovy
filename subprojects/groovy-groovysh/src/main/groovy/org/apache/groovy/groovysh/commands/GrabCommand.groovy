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

import groovy.grape.Grape
import jline.console.completer.Completer
import org.codehaus.groovy.tools.GrapeUtil
import org.apache.groovy.groovysh.CommandSupport
import org.apache.groovy.groovysh.Groovysh

/**
 * The 'grab' command.
 */
class GrabCommand extends CommandSupport {

    public static final String COMMAND_NAME = ':grab'

    GrabCommand(Groovysh shell) {
        super(shell, COMMAND_NAME, ':g')
    }

    @Override protected List<Completer> createCompleters() { [ null ] }

    @Override Object execute(List<String> args) {
        validate(args)
        grab(dependency(args))
        shell.packageHelper.reset()
    }

    private void validate(List<String> args) {
        if ( args?.size() != 1 || 
             !( args[0] ==~ /^(\w|\.|-)+:(\w|\.|-)+(\w|\.|-)(:+(\w|\.|-|\*)+){0,2}$/ ) ) {
            fail("usage: @|bold ${COMMAND_NAME}|@ ${usage}")
        }
    }

    private String dependency(List<String> args) {
        validate(args)
        args[0]
    }

    private Map<String, Object> dependencyMap(String dependency) {
        GrapeUtil.getIvyParts(dependency)
    }

    private void grab(String dependency) {
        Grape.grab([classLoader: shell.interp.classLoader.parent,
                    refObject: shell.interp],
                   dependencyMap(dependency))
    }

}
