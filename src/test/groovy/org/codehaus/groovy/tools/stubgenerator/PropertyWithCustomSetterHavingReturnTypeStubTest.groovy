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
 * Test that a custom setter for a property with non-void return type takes
 * precedence over default void fallback.
 */
class PropertyWithCustomSetterHavingReturnTypeStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'Dummy.java': '''
                    public class Dummy {}
                ''',
                'foo/SetterWithReturn4646.groovy': '''
                    package foo;

                    class SetterWithReturn4646 {
                        String foo
                        SetterWithReturn4646 setFoo(String foo) { this.foo = foo; return this; }
                    }
            '''
        ]
    }

    void verifyStubs() {
        assert !stubJavaSourceFor('foo.SetterWithReturn4646').contains('void setFoo(java.lang.String ')
    }
}
