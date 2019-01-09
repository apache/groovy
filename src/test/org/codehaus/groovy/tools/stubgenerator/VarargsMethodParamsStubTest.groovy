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

/**
 * Test that Groovy treatment of array types as varargs is reflected in stub parameter types.
 */
class VarargsMethodParamsStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'Main4112.java': '''
                    public class Main4112 {
                        public static void main(String[] args) {
                            GroovyClass4112.foo("one", "two", "three");
                        }
                    }
                ''',
                'GroovyClass4112.groovy': '''
                    class GroovyClass4112 {
                        static foo(String[] args) {}
                    }
                '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('GroovyClass4112')
        assert stubSource.contains('foo(java.lang.String... args)')
    }
}
