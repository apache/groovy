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

final class Groovy9777 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'A9777.groovy': '''
                abstract class A9777 {
                    A9777(Object o) {
                        this(o.class.name)
                    }
                    A9777(Class  c) {
                        this(c.name)
                    }
                    A9777(String s) {
                    }
                }
            ''',
            'C9777.groovy': '''
                class C9777 extends A9777 {
                    C9777() {
                        super(C9777.class.name) // generates ambiguous "super (null);" in stub
                    }
                }
            ''',
            'Main.java': '''
                public class Main {
                    public static void main(String[] args) {
                        new C9777();
                    }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        def specialCtorCall = (stubJavaSourceFor('C9777') =~ /super\s*\((.+?)\);/)
        assert specialCtorCall.find() && specialCtorCall.group(1) == '(java.lang.Object)null'
    }
}
