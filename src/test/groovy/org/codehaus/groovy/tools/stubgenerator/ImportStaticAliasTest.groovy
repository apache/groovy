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

class ImportStaticAliasTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'Constants.java': '''
                    package test;
                    public class Constants {
                        public static final String C1 = "c1";
                    }
                ''',

                'MyAnnotation.java': '''
                    package test;
                    public @interface MyAnnotation {
                        String value();
                    }
                ''',

                'Test.groovy': '''
                    package test
                    import static test.Constants.C1 as C2
                    @MyAnnotation(C2)
                    class Test {
                        def test
                        Test(test) {
                            this.test = test
                        }
                    }
                ''',

                'SomeJavaClass.java': '''
                    package test;
                    public class SomeJavaClass {
                        Test test;
                    }
                '''
        ]
    }

    void verifyStubs() {
        // Stubs should not include import statements for aliased static imports
        assert !stubJavaSourceFor('test.Test').contains('import static test.Constants.C1')
    }
}