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
package org.apache.groovy.groovysh

import groovy.mock.interceptor.MockFor
import groovy.test.GroovyTestCase
import org.codehaus.groovy.tools.shell.IO
import org.apache.groovy.groovysh.completion.antlr4.IdentifierCompleter
import org.apache.groovy.groovysh.completion.antlr4.ReflectionCompleter
import org.apache.groovy.groovysh.util.PackageHelper
import org.apache.groovy.groovysh.util.PackageHelperImpl

abstract class CompleterTestSupport extends GroovyTestCase {

    BufferManager bufferManager = new BufferManager()
    IO testio
    ByteArrayOutputStream mockOut
    ByteArrayOutputStream mockErr
    MockFor groovyshMocker
    MockFor packageHelperMocker
    PackageHelper mockPackageHelper
    MockFor reflectionCompleterMocker
    MockFor idCompleterMocker

    @Override
    void setUp() {
        super.setUp()
        mockOut = new ByteArrayOutputStream()
        mockErr = new ByteArrayOutputStream()
        testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)
        reflectionCompleterMocker = new MockFor(ReflectionCompleter)
        idCompleterMocker = new MockFor(IdentifierCompleter)

        groovyshMocker = new MockFor(Groovysh)
        groovyshMocker.demand.getClass(0..1) { Groovysh }
        groovyshMocker.demand.createDefaultRegistrar { { shell -> null } }
        groovyshMocker.demand.getIo(0..2) { testio }
        packageHelperMocker = new MockFor(PackageHelperImpl)
        def registry = new CommandRegistry()
        groovyshMocker.demand.getRegistry(0..1) { registry }
        packageHelperMocker.demand.getContents(6) { ['java', 'test'] }
        groovyshMocker.demand.getIo(0..2) { testio }
        for (i in 1..19) {
            groovyshMocker.demand.getIo(0..1) { testio }
            groovyshMocker.demand.add(0..1) {}
            groovyshMocker.demand.getIo(0..1) { testio }
        }
        groovyshMocker.demand.getRegistry(0..1) { registry }
        groovyshMocker.demand.getBuffers(0..2) { bufferManager }
    }
}
