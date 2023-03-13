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

final class Groovy10611 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'C.groovy': '''
                class C {
                    public static final long LONG = 123_456
                    public static final float FLOAT = 78.90
                    public static final String STRING = 'x'+'y'
                }
            ''',
            'Main.java': '''
                public class Main {
                    public static void main(String[] args) {
                        new C();
                    }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('C')
        assert stub.contains('final long LONG = 123456;');
        assert stub.contains('final float FLOAT = 78.90;');
        assert stub.contains('final java.lang.String STRING = "xy";');
    }
}
