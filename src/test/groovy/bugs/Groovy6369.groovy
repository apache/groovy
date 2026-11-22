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

/**
 * GROOVY-6369: {@code new T()} on a type parameter used to compile.
 * Instantiating a generic type variable must be rejected.
 */
final class Groovy6369 {

    @Test
    void testNewOnTypeParameterIsACompileError() {
        // Exact snippet from the ticket
        def err = compileFails 'public <T> T nouveau() { new T() }'
        assert err.contains('Cannot instantiate the type parameter T')
        assert err.contains('new T()')
    }

    @Test
    void testNewOnTypeParameterInsideClassIsACompileError() {
        def err = compileFails 'class C { public <T> T nouveau() { new T() } }'
        assert err.contains('Cannot instantiate the type parameter T')
        assert err.contains('new T()')
    }

    private static String compileFails(String src) {
        try {
            new GroovyClassLoader().parseClass(src)
            assert false, 'expected a compilation failure'
            return ''
        } catch (MultipleCompilationErrorsException e) {
            return e.message
        }
    }
}
