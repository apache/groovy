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
package groovy.grape

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilationFailedException

class GrabResolverTest extends GroovyTestCase {
    def originalGrapeRoot
    def grapeRoot

    void setUp() {
        Grape.@instance = null // isolate our test from other tests

        // create a new grape root directory so as to insure a clean slate for this test
        originalGrapeRoot = System.getProperty("grape.root")
        grapeRoot = new File(System.getProperty("java.io.tmpdir"), "GrabResolverTest${System.currentTimeMillis()}")
        assert grapeRoot.mkdir()
        grapeRoot.deleteOnExit()
        System.setProperty("grape.root", grapeRoot.path)
    }

    void tearDown() {
        if (originalGrapeRoot == null) {
            // SDN bug: 4463345
            System.getProperties().remove("grape.root")
        } else {
            System.setProperty("grape.root", originalGrapeRoot)
        }

        Grape.@instance = null // isolate our test from other tests
    }

    void manualTestChecksumsCanBeDisabled() {
        // TODO someone has cleaned up the checksum info in the public repos that this test
        // was relying on and so this test no longer fails unless you have the corrupt SHA1
        // value cached in your local grapes repo, change test to not rely on that fact and
        // then reinstate (use a local file repo?)
        GroovyShell shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(RuntimeException) {
            shell.evaluate """
            @Grab('org.mvel:mvel2:2.1.3.Final')
            import org.mvel2.MVEL
            assert MVEL.name == 'org.mvel2.MVEL'
            """
        }
        shell.evaluate """
        @Grab('org.mvel:mvel2:2.1.3.Final')
        @GrabConfig(disableChecksums=true)
        import org.mvel2.MVEL
        assert MVEL.name == 'org.mvel2.MVEL'
        """
    }

    void testResolverDefinitionIsRequired() {
        GroovyShell shell = new GroovyShell(new GroovyClassLoader())
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                @Grab(group='org.restlet', module='org.restlet', version='1.1.6')
                class AnnotationHost {}
                import org.restlet.Application
            """)
        }
    }

    void testResolverDefinitionResolvesDependency() {
        GroovyShell shell = new GroovyShell(new GroovyClassLoader())
        shell.evaluate("""
            @GrabResolver(name='restlet.org', root='http://maven.restlet.org')
            @Grab(group='org.restlet', module='org.restlet', version='1.1.6')
            class AnnotationHost {}
            assert org.restlet.Application.class.simpleName == 'Application'""")
    }

    void testResolverDefinitionResolvesDependencyWithShorthand() {
        GroovyShell shell = new GroovyShell(new GroovyClassLoader())
        shell.evaluate("""
            @GrabResolver('http://maven.restlet.org')
            @Grab('org.restlet:org.restlet:1.1.6')
            class AnnotationHost {}
            assert org.restlet.Application.class.simpleName == 'Application'""")
    }
}
