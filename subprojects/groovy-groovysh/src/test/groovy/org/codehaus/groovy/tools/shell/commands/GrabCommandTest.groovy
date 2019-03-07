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

import groovy.grape.Grape
import groovy.mock.interceptor.StubFor
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.util.PackageHelperImpl

/**
 * Tests for the {@link GrabCommand} class.
 */
class GrabCommandTest extends CommandTestSupport {

    protected GrabCommand command
    def grapeStub = new StubFor(Grape.class)

    void setUp() {
        Groovysh groovysh = new Groovysh()
        PackageHelperImpl packageHelper = new PackageHelperImpl()
        packageHelper.metaClass.reset = { }
        groovysh.metaClass.packageHelper = packageHelper
        command = new GrabCommand(groovysh)
        command.metaClass.fail = { String message -> 
            throw new RuntimeException("fail(${message}) called")
        }
        def stubber = new StubFor(Grape.class)
    }

    void testWrongNumberOfArguments() {
        shouldFail(RuntimeException) { command.execute([]) }
        shouldFail(RuntimeException) { command.execute(['alpha', 'beta']) }
    }

    void testInvalidDependencyFormat() {
        shouldFail(RuntimeException) { command.execute(['net.sf.json-lib']) }
    }

    void testGroup_Module() {
        grapeStub.demand.grab()  { arg1, arg2 -> }
        grapeStub.use {
            command.execute(['net.sf.json-lib:json-lib'])
        }
    }

    void testGroupModuleVersion() {
        grapeStub.demand.grab()  { arg1, arg2 -> }
        grapeStub.use {
            command.execute(['net.sf.json-lib:json-lib:2.2.3'])
        }
    }

    void testGroupModuleVersionWildcard() {
        grapeStub.demand.grab()  { arg1, arg2 -> }
        grapeStub.use {
            command.execute(['net.sf.json-lib:json-lib:*'])
        }
    }

    void testGroupModuleVersionClassifier() {
        grapeStub.demand.grab()  { arg1, arg2 -> }
        grapeStub.use {
            command.execute(['net.sf.json-lib:json-lib:2.2.3:jdk15'])
        }
    }

}
