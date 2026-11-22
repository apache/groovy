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
 * GROOVY-8826: a foreach / {@code for-in} loop variable captured by a
 * lambda or closure must be a per-iteration binding, matching Java
 * (historically every capture saw the last value).
 */
final class Groovy8826 {

    @Test
    void testLambdaCapturesForeachVariablePerIteration() {
        assertScript '''
            def seen = []
            def closures = []
            for (int i : new int[] {1, 2}) {
                closures.add(() -> seen << i)
            }
            closures.each { it.run() }
            assert seen == [1, 2]
        '''
    }

    @Test
    void testClosureCapturesForInVariablePerIteration() {
        assertScript '''
            def seen = []
            def closures = []
            for (i in [1, 2]) {
                closures.add({ seen << i })
            }
            closures.each { it() }
            assert seen == [1, 2]
        '''
    }
}
