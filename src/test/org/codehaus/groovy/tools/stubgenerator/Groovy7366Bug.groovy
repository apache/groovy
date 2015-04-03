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

class Groovy7366Bug extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'Constants.java': '''
                    package test;
                    public interface Constants {
                        String C1 = "c1";
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
                    import static test.Constants.C1
                    @MyAnnotation(C1)
                    class Test {
                        def test
                        Test(test) {
                            this.test = test
                        }
                    }
                ''',
                'Hello.java': '''
                package test;
                public class Hello {
                    public static void main(String[] args) {
                        System.out.println(new Test("hello").getTest());
                    }
                }
                '''
        ]
    }

    void verifyStubs() {

    }
}