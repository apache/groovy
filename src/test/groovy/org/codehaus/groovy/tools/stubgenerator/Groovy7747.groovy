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
 * Test that enums with an abstract method are compiled successfully.
 */
final class Groovy7747 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'Main.java': '''
                import enums.EnumWithAbstractMethod;
                public class Main {
                    public static void main(String[] args) {
                        System.out.println(EnumWithAbstractMethod.values());
                        System.out.println(EnumWithAbstractMethod.ONE.getInt());
                    }
                }
            ''',
            'enums/EnumWithAbstractMethod.groovy': '''
                package enums
                enum EnumWithAbstractMethod {
                    ONE {
                        @Override
                        int getInt() {
                            return 1
                        }
                    },
                    TWO {
                        @Override
                        int getInt() {
                            return 2
                        }
                    }
                    abstract int getInt()
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String stub = stubJavaSourceFor('enums.EnumWithAbstractMethod')
        assert stub.contains('enum EnumWithAbstractMethod')
        assert !stub.matches('abstract.*enum')
    }
}
