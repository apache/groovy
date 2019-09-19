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
package groovy.bugs

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

class Groovy6086Bug extends GroovyTestCase {

    // Note that this unit test reproduces the code that we can
    // see on the Grails build. However, it never managed to reproduce
    // the issue so the latter has been fixed independently.
    void testGroovy6086() {

        def config = new CompilerConfiguration()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [stubDir: File.createTempDir()]
        }

        try {
            def unit = new JavaAwareCompilationUnit(config, null, new GroovyClassLoader(getClass().classLoader))

            unit.addSource('Boo.java', 'interface Boo {}')
            unit.addSource('Wrapper.groovy', '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Wrapper {
                private Map cache
                Boo[] boos() {
                    Boo[] locations = (Boo[]) cache.a
                    if (locations == null) {
                        if (true) {
                            locations = [].collect { it }
                        }
                        else {
                            locations = [] as Boo[]
                        }
                    }
                    return locations
                }
            }
        ''')
            unit.compile(CompilePhase.INSTRUCTION_SELECTION.phaseNumber)
        } finally {
            config.targetDirectory.deleteDir()
            config.jointCompilationOptions.stubDir.deleteDir()
        }
    }
}
