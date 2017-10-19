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
package org.codehaus.groovy.tools.stubgenerator

class Groovy8343StubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
                'groovy/Groovy8343.java': '''
                    package groovy;
                    /**
                     * Precompiled Java class in same package as Groovy8343Test
                     */
                    public interface Groovy8343 {
                        Groovy8343 createRelative(String relativePath);
                    }
                ''',
                'groovy/Groovy8343Test.groovy': '''
                    package groovy
                    @groovy.transform.CompileStatic
                    class Groovy8343Impl implements Groovy8343 {
                        Groovy8343 createRelative(String relativePath) {
                            throw new UnsupportedOperationException("Method createRelative not supported")
                        }
                    }
                    assert new Groovy8343Impl()
                '''
        ]
    }

    @Override
    void verifyStubs() {
        // We are just testing that the above compiles ok and using stub test to create the correct conditions
    }
}
