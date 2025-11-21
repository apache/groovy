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

import org.codehaus.groovy.control.CompilationFailedException

final class Groovy11806 extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'p/A.java': '''
                package p;
                public interface A {
                }
            ''',
            /* not given to compiler
            'p/B.java': '''
                package p;
                public interface B {
                }
            ''',
            */
            'C.groovy': '''
                import p.*
                class C implements A {
                    B m() {
                    }
                }
            ''',
        ]
    }

    @Override
    void verifyStubs() {
        String source= stubJavaSourceFor('C')
        assert source.contains('import p.*;')
    }

    @Override
    void handleCompilationFailure(CompilationFailedException cfe) {
        if (!cfe.message.contains('unable to resolve class B')) {
            super.handleCompilationFailure(cfe)
        }
    }
}
