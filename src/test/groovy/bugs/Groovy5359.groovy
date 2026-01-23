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

import org.codehaus.groovy.runtime.metaclass.MissingPropertyExceptionNoStack
import org.junit.jupiter.api.Test

final class Groovy5359 {

    static class A {
        static method() { 'A' }
    }

    static class B extends A {
    }

    static class C extends A {
    }

    @groovy.transform.CompileStatic
    private long measure(Number count, Closure block) {
        long t0 = System.currentTimeMillis()
        count.times(block)
        System.currentTimeMillis() - t0
    }

    @Test
    void testStaticMethodOfSuperClass() {
        ExpandoMetaClass.enableGlobally()
        try {
            int count = 0
            C.metaClass.static.propertyMissing = { name ->
                count += 1
                throw new MissingPropertyExceptionNoStack('static property missing', getDelegate())
            }

            assert B.method() == 'A'
            assert C.method() == 'A'
            assert count == 0

            assert measure(1e6) { B.method() } < 1500 // was ~26000 with property check
            assert measure(1e6) { C.method() } < 1500 // was ~26000 with property check
            assert count == 0
        } finally {
            ExpandoMetaClass.disableGlobally()
        }
    }
}
