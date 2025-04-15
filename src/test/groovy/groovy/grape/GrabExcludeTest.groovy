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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.BeforeClass
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

class GrabExcludeTest {

    @BeforeClass
    static void populateCache() {
        // ensure files are installed locally
        Grape.resolve([autoDownload:true, classLoader:new GroovyClassLoader()],
        [groupId:'org.mortbay.jetty', artifactId:'jetty', version:'6.0.0'])
    }

    @Test
    void testExcludeByGroupAndModule() {
        shouldFailCompilationWithMessage('''
                @Grab('org.mortbay.jetty:jetty:6.0.0')
                @GrabExclude(group='org.mortbay.jetty', module='jetty-util')
                static testMethod() {
                    // access class from excluded module to provoke an error
                    org.mortbay.util.IO.name
                }
                ''',
                "Apparent variable 'org' was found")
    }

    @Test
    void testExcludeByValue() {
       shouldFailCompilationWithMessage('''
                @Grab('org.mortbay.jetty:jetty:6.0.0')
                @GrabExclude('org.mortbay.jetty:jetty-util')
                static testMethod() {
                    // access class from excluded module to provoke an error
                    org.mortbay.util.IO.name
                }
                ''',
                "Apparent variable 'org' was found")
    }

    @Test
    void testExcludeByGroupAndModule_requiresGroup() {
       shouldFailCompilationWithMessage('''
                @Grab('org.mortbay.jetty:jetty:6.0.0')
                @GrabExclude(module='jetty-util')
                class AnnotatedClass { }
                ''',
                'The missing attribute "group" is required in @GrabExclude annotations')
    }

    @Test
    void testExcludeByGroupAndModule_requiresModule() {
        shouldFailCompilationWithMessage('''
                @Grab('org.mortbay.jetty:jetty:6.0.0')
                @GrabExclude(group='org.mortbay.jetty')
                class AnnotatedClass { }
                ''',
                'The missing attribute "module" is required in @GrabExclude annotations')
    }

    private static shouldFailCompilationWithMessage(String code, String expectedString) {
        GroovyClassLoader loader = new GroovyClassLoader()
        String exceptionMessage = shouldFail(MultipleCompilationErrorsException) {
            loader.parseClass(code)
        }
        assert exceptionMessage.contains(expectedString)
    }
}
