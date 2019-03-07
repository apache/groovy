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
 * Checks that {@code @Immutable} classes work correctly with stubs.
 */
class ImmutableWithJointCompilationStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
                'Dummy.java': '''
                    class Dummy {}
                ''',

                'Foo4825.groovy': '''
                    @groovy.transform.Immutable class Foo4825 { }
                '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('Foo4825')
        // check that default constructor doesn't appear
        assert !stubSource.matches(/(?ms).*public\s+Foo4825\s*\(\s*\)\s*\{\s*\}.*/)
    }
}
