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

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class GrabResolverTest {

    private static String originalGrapeRoot

    @BeforeClass
    static void setUpTestSuite() {
        originalGrapeRoot = System.getProperty('grape.root')
    }

    @Before
    void setUp() {
        Grape.@instance = null // isolate each test

        // create a new grape root directory so as to insure a clean slate for each test
        def grapeRoot = new File(System.getProperty('java.io.tmpdir'), "GrabResolverTest${System.currentTimeMillis()}")
        System.setProperty('grape.root', grapeRoot.path)
        assert grapeRoot.mkdir()
        grapeRoot.deleteOnExit()
    }

    @AfterClass
    static void tearDownTestSuite() {
        if (originalGrapeRoot == null) {
            System.clearProperty('grape.root')
        } else {
            System.setProperty('grape.root', originalGrapeRoot)
        }

        Grape.@instance = null // isolate these tests from other tests
    }

    @Test @Ignore('manual')
    void testChecksumsCanBeDisabled() {
        // TODO someone has cleaned up the checksum info in the public repos that this test
        // was relying on and so this test no longer fails unless you have the corrupt SHA1
        // value cached in your local grapes repo, change test to not rely on that fact and
        // then reinstate (use a local file repo?)
        shouldFail RuntimeException, '''
            @Grab('org.mvel:mvel2:2.1.3.Final')
            import org.mvel2.MVEL
            assert MVEL.name == 'org.mvel2.MVEL'
        '''
        assertScript '''
            @Grab('org.mvel:mvel2:2.1.3.Final')
            @GrabConfig(disableChecksums=true)
            import org.mvel2.MVEL
            assert MVEL.name == 'org.mvel2.MVEL'
        '''
    }

    @Test // NOTE: 'org.restlet:org.restlet:1.1.6' must not be in local m2 repository for this test to pass
    void testResolverDefinitionIsRequired() {
        shouldFail CompilationFailedException, '''
            @Grab(group='org.restlet', module='org.restlet', version='1.1.6')
            import org.restlet.Application
            class AnnotationHost {}
            println 'hello world'
        '''
    }

    @Test
    void testResolverDefinitionResolvesDependency() {
        assertScript '''
            @GrabResolver(name='restlet.org', root='http://maven.restlet.org')
            @Grab(group='org.restlet', module='org.restlet', version='1.1.6')
            class AnnotationHost {}
            assert org.restlet.Application.class.simpleName == 'Application'
        '''
    }

    @Test
    void testResolverDefinitionResolvesDependencyWithShorthand() {
        assertScript '''
            @GrabResolver('http://maven.restlet.org')
            @Grab('org.restlet:org.restlet:1.1.6')
            class AnnotationHost {}
            assert org.restlet.Application.class.simpleName == 'Application'
        '''
    }
}
