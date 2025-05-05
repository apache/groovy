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
package bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

final class Groovy5193 {

    @Test
    void testMixingMethodsWithPrivatePublicAccessInSameClass1() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class A5193 {
                static main(args) {
                }
                public find(String id) {
                }
                private <T> T find(Class<T> type, String id, boolean suppressExceptions) {
                }
            }
        '''
        assert err.message.contains('Mixing private and public/protected methods of the same name causes multimethods to be disabled')
    }

    @Test
    void testMixingMethodsWithPrivatePublicAccessInSameClass2() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class B5193 {
                static main(args) {
                }
                public find(String id) {
                }
                private <T> T find(Class<T> type, String id, boolean suppressExceptions = true) {
                }
            }
        '''
        assert err.message.contains('Mixing private and public/protected methods of the same name causes multimethods to be disabled')
    }
}
