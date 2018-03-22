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
package org.codehaus.groovy.runtime.vm8

class InterfaceStaticMethodCallTest extends GroovyTestCase {
    void testStreamOf() {
        // "of" is a static method declared on the interface, we only want to be sure we can call the method
        assertScript '''
            import java.util.stream.Stream
            assert Stream.of("1") instanceof Stream
        '''
    }

    // GROOVY-8494
    void testComparatorNaturalOrder() {
        assertScript '''
            def no = Comparator.naturalOrder()
            assert -1 == no.compare(42, 52)
            assert  0 == no.compare(42, 42)
            assert  1 == no.compare(42, 32)
        '''
    }

    // GROOVY-8494
    void testFunctionIdentity() {
        assertScript '''
            import java.util.function.Function
            Function<Integer, Integer> function = Function.identity()
            assert 42 == function.apply(42)
        '''
    }
}
