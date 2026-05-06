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

import groovy.junit6.plugin.ForkedJvm
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
@ForkedJvm(inheritProperties = ['grape.root', 'user.home', 'gradle.home'])
final class GrabResolverTest {

    @BeforeEach @CompileDynamic
    void setUp() {
        // Lifecycle hooks fire in BOTH the parent test JVM and each forked
        // child; the parent never runs the test bodies, so its Grape state
        // and grape.root must stay untouched. The previous BeforeAll/AfterAll
        // save/restore dance becomes redundant once nothing in the parent
        // mutates grape.root.
        if (!Boolean.parseBoolean(System.getProperty('groovy.junit6.forked'))) return

        Grape.@instance = null // isolate each test

        // create a new grape root directory for each test for isolation
        def grapeRoot = new File(System.getProperty('java.io.tmpdir'), "GrabResolverTest${System.currentTimeMillis()}")
        System.setProperty('grape.root', grapeRoot.path)
        assert grapeRoot.mkdir()
        grapeRoot.deleteOnExit()

        def engine = Grape.instance
        if (engine.getClass().name == 'groovy.grape.ivy.GrapeIvy') {
            engine.settings.getResolver('downloadGrapes').resolvers.removeAll {
                // jcenter is no longer used but it is left in this test just in case
                // someone running this test has an old ~/.groovy/grapeConfig.xml
                it.name == 'localm2' || it.name == 'cachedGrapes' || it.name == 'jcenter'
            }
        } else if (engine.metaClass.hasProperty(engine, 'repos')) {
            // Maven engine: start from no repositories so @GrabResolver is required by the test.
            engine.repos.clear()
        }
    }

    @Test
    void testResolverDefinitionIsRequired() {
        shouldFail CompilationFailedException, '''
            @Grab(group='org.restlet', module='org.restlet', version='1.1.6')
            import org.restlet.Application

            assert false : '"org.restlet:org.restlet:1.1.6" should not resolve without @GrabResolver'
        '''
    }

    @Test
    void testResolverDefinitionResolvesDependency() {
        assertScript '''
            @GrabResolver(name='restlet.org', root='https://maven.restlet.talend.com')
            @Grab(group='org.restlet', module='org.restlet', version='1.1.6')
            import org.restlet.Application

            assert Application.simpleName == 'Application'
        '''
    }

    @Test
    void testResolverDefinitionResolvesDependencyWithShorthand() {
        assertScript '''
            @GrabResolver('https://maven.restlet.talend.com')
            @Grab('org.restlet:org.restlet:1.1.6')
            import org.restlet.Application

            assert Application.simpleName == 'Application'
        '''
    }
}
