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

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-11382: {@code new List<Integer>[]{lst}} for a {@code List<String>}
 * used to compile and then fail at runtime with a {@code GroovyCastException}.
 * The program is now rejected at compile time.
 */
final class Groovy11382 {

    @Test
    void testGenericArrayInitializationOfIncompatibleListIsRejected() {
        def err = shouldFail '''
            List<String> lst = ["fda"]
            List<Integer>[] x = new List<Integer>[]{lst}
            Integer k = x[0].get(0)
        '''
        assert err.message.contains('generic array creation')
    }
}
