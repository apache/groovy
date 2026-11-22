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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-8346: a definitely assigned {@code final} local such as
 * {@code final begin = new Date()} must not be rejected as possibly
 * uninitialized by final-variable analysis.
 */
final class Groovy8346 {

    @Test
    void testFinalLocalInitializedInOneStatement() {
        assertScript '''
            final begin = new Date()
            assert begin != null
            final foo = 'bar'
            assert foo == 'bar'
        '''
    }

    @Test
    void testFinalLocalInitializedInsideMethod() {
        assertScript '''
            class WorkflowMetadataTest {
                def test() {
                    final begin = new Date()
                    return begin
                }
            }
            assert new WorkflowMetadataTest().test() instanceof Date
        '''
    }
}
