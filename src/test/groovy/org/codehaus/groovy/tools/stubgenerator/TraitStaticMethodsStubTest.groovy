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
 * GROOVY-11899: Static methods in traits should not appear in the trait stub,
 * as they are compiled into the $Trait$Helper companion class. Including them
 * as bodyless static methods produces invalid Java (static interface methods
 * require a body), breaking joint compilation.
 */
final class TraitStaticMethodsStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'MyTrait.groovy': '''
                trait MyTrait {
                    static String getMessage() { return "hello" }
                    String getGreeting() { return "hi" }
                }
            ''',
            'MyClass.groovy': '''
                class MyClass implements MyTrait {
                    @groovy.transform.CompileStatic
                    String test() { return getMessage() }
                }
            ''',
            'MyJavaClass.java': '''
                public class MyJavaClass {
                    public static void main(String[] args) {
                        MyClass obj = new MyClass();
                    }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        String traitStub = stubJavaSourceFor('MyTrait')
        assert !traitStub.contains('getMessage') : 'static method should not appear in trait stub'
        assert  traitStub.contains('getGreeting') : 'instance method should still appear in trait stub'

        String classStub = stubJavaSourceFor('MyClass')
        assert  classStub.contains('getMessage') : 'static method should appear in implementing class stub'
        assert  classStub.contains('getGreeting') : 'instance method should appear in implementing class stub'
    }
}
