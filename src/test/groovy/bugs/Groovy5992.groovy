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
 * GROOVY-5992: compiling a class that implements an interface and
 * overrides {@code protected void finalize()} used to fail with
 * "The method finalize should be public as it implements the
 * corresponding method from interface java.lang.Object" because
 * the verifier treated {@code java.lang.Object} as an interface.
 * Jochen's reproduction from the ticket is the script below.
 */
final class Groovy5992 {

    @Test
    void testProtectedFinalizeOnClassImplementingInterface() {
        assertScript '''
            interface X {}
            class Foo implements X {
                @Override
                protected void finalize() throws Throwable {
                    super.finalize()
                }
            }
            new Foo()
        '''
    }
}
