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

final class Groovy7306 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'A7306.groovy': '''
                abstract class A7306<T extends Number> {
                    final T value
                    A7306(T value) {
                        this.value = value
                    }
                }
            ''',
            'C7306.groovy': '''
                class C7306 extends A7306<Integer> {
                    C7306(Integer value) {
                        super(value)
                    }
                }
            ''',
            'Main.java': '''
                public class Main {
                    public static void main(String[] args) {
                        new C7306(1234);
                    }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        compile([new File(stubDir,'A7306.java'), new File(stubDir,'C7306.java')])

        def specialCtorCall = (stubJavaSourceFor('C7306') =~ /super\s*\((.+?)\);/)
        assert specialCtorCall.find() && specialCtorCall.group(1) == '(java.lang.Integer)null'
    }
}
