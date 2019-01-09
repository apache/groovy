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
 * Test stub generation for constructors having optional args 
 */
class StubGenerationForConstructorWithOptionalArgsStubsTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'Base4508.java': '''
                class Base4508 {
                  Base4508(String str) {}
                }
            ''',
            'Derived4508.groovy': '''
                class Derived4508 extends Base4508 {
                  Derived4508(String foo, String bar = "bar") {
                    super(foo)
                  }
                }
            '''
        ]
    }

    void verifyStubs() {
        String source = stubJavaSourceFor('Derived4508')
        assert source.contains("super(") // for constructor call in constructor 1
        assert source.contains("this(") // for constructor call in constructor 2, added due to optional arg
    }
}
