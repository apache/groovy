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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.GenericsSTCTest

/**
 * Unit tests for static compilation : generics.
 */
class GenericsStaticCompileTest extends GenericsSTCTest implements StaticCompilationTestSupport {

    // GROOVY-10053
    void testReturnTypeInferenceWithMethodGenericsSC() {
        assertScript """
            Set<Number> f() {
                Collections.<Number>singleton(42)
            }
            def <N extends Number> Set<N> g(Class<N> t) {
                Set<N> result = new HashSet<>()
                f().stream().filter(n -> t.isInstance(n))
                    .<N>map(t::cast).forEach(n -> result.add(n))
                return result
            }

            def result = g(Integer)
            assert result == [42] as Set
        """
    }
}
