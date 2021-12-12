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

final class Groovy10407 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'Pogo.groovy': '''
                class Foo {
                    Foo(Map<String, String> map) {
                    }
                }
                class Bar extends Foo {
                    Bar(Map<String, String> map) {
                        super(map)
                    }
                }
            ''',
            'Main.java': '''
                public class Main { Foo foo; }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('Bar')
        assert stub.contains('super ((java.util.Map<java.lang.String, java.lang.String>)null);')
    }
}
