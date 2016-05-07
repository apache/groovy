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
package groovy.util

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @author Andre Steingress
 */
@RunWith(JUnit4)
class GroovyScriptEngineTest extends GroovyTestCase {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    void createASTDumpWhenScriptIsLoadedByName() {

        def scriptFile = temporaryFolder.newFile('Script1.groovy')

        scriptFile << "assert 1 + 1 == 2" // the script just has to have _some_ content

        try {
            System.setProperty('groovy.ast', 'xml')

            def clazz = new GroovyScriptEngine([temporaryFolder.root.toURL()] as URL[]).loadScriptByName('Script1.groovy')

            assert new File(temporaryFolder.root, scriptFile.name + '.xml').exists()
            assert clazz != null

        } finally {
            System.clearProperty('groovy.ast')
        }
    }

    @Test
    void whenSystemPropertyIsMissingDontCreateASTDump() {

        def scriptFile = temporaryFolder.newFile('Script1.groovy')

        scriptFile << "assert 1 + 1 == 2" // the script just has to have _some_ content

        System.clearProperty('groovy.ast')

        def clazz = new GroovyScriptEngine([temporaryFolder.root.toURL()] as URL[]).loadScriptByName('Script1.groovy')
        assert clazz != null

        assert !new File(temporaryFolder.root, scriptFile.name + '.xml').exists()
    }
}