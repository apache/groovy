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
import org.apache.groovy.groovysh.completion.IdentifierCompleter
import org.apache.groovy.groovysh.completion.ReflectionCompleter
import org.apache.groovy.groovysh.util.PackageHelperImpl
import org.codehaus.groovy.tools.shell.IO

abstract class CompleterTestSupport extends GroovyTestCase {

    ByteArrayOutputStream mockOut
    ByteArrayOutputStream mockErr
    IO testio

    MockFor reflectionCompleterMocker
    MockFor packageHelperMocker
    MockFor idCompleterMocker
    MockFor groovyshMocker

    @Override
    protected void setUp() {
        super.setUp()
        mockOut = new ByteArrayOutputStream()
        mockErr = new ByteArrayOutputStream()
        testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)

        reflectionCompleterMocker = new MockFor(ReflectionCompleter)
        packageHelperMocker = new MockFor(PackageHelperImpl)
        idCompleterMocker = new MockFor(IdentifierCompleter)

        groovyshMocker = new MockFor(Groovysh)
        // no-arg constructor
        groovyshMocker.demand.getClass( 1) { Groovysh }
        groovyshMocker.demand.getIo(0..21) { testio }
        groovyshMocker.demand.register(18) { it }
        // new command
        groovyshMocker.demand.getIo( 0..3) { testio }
    }
}
