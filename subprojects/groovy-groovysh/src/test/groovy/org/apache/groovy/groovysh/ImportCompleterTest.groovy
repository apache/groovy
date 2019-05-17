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

import jline.console.completer.Completer
import org.apache.groovy.groovysh.commands.ImportCommand

class ImportCompleterTest extends CompleterTestSupport {

    void testEmpty() {
        mockPackageHelper = new MockPackageHelper(['java', 'test'])
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.completer
            def candidates = []
            assert 0 == completer.complete('', 0, candidates)
            // order changed by sort
            assert [':i ', ':i ', 'import ', 'import '] == candidates.sort()
        }
    }

    void testUnknownVar() {
        mockPackageHelper = new MockPackageHelper(['java', 'test'])
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.completer
            def candidates = []
            assert 7 == completer.complete('import ', 'import '.length(), candidates)
            // order changed by sort, needed to make tests run on different JDks
            assert ['java.', 'static ', 'test.'] == candidates.sort()
        }
    }

    void testJ() {
        mockPackageHelper = new MockPackageHelper(['java', 'test'])
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.completer
            def candidates = []
            // argument completer completes after 'import '
            assert 7 == completer.complete('import j', 'import j'.length(), candidates)
            assert ['java.'] == candidates
        }
    }

    void testJavaDot() {
        mockPackageHelper = new MockPackageHelper(['java', 'test'])
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.completer
            def candidates = []
            // argument completer completes after 'import '
            String buffer = 'import java.'
            assert buffer.length() == completer.complete(buffer, buffer.length(), candidates)
            // order changed by sort, needed to run tests on different JDKs
            assert ['* ', 'java.', 'test.'] == candidates.sort()
        }
    }

    void testJavaLangDot() {
        mockPackageHelper = new MockPackageHelper(['java', 'test'])
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.completer
            def candidates = []
            // argument completer completes after 'import '
            String buffer = 'import java.lang.'
            assert buffer.length() == completer.complete(buffer, buffer.length(), candidates)
            // order changed by sort, needed to make tests run on different JDks
            assert ['* ', 'java.', 'test.'] == candidates.sort()
        }
    }

    void testAs() {
        mockPackageHelper = new MockPackageHelper(['java', 'Test'])
        groovyshMocker.demand.getPackageHelper(1) { mockPackageHelper }
        groovyshMocker.demand.getInterp(1) {}
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ImportCommand iCom = new ImportCommand(groovyshMock)
            Completer completer = iCom.completer
            def candidates = []
            // mock package
            String buffer = 'import java.Test '
            assert buffer.length() == completer.complete(buffer, buffer.length(), candidates)
            assert ['as '] == candidates
        }
    }
}
