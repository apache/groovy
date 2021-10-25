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

final class Groovy7510 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'p/A.groovy': '''
                package p
                class A {
                    static String aString
                }
            ''',

            'p/B.groovy': '''
                package p
                import static p.A.aString
                class B {
                    String returnAString() {
                        return aString
                    }
                }
            ''',

            'p/C.java': '''
                package p;
                public class C {
                    public static void main(String[] args) {
                        new B().returnAString();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('p.B')
        assert !stub.contains('import static p.A.aString')
    }
}
