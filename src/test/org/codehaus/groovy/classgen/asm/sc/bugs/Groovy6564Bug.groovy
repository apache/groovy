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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

final class Groovy6564Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testShouldNotRequireIntermediateVariableToPass() {
        assertScript '''
            class Stream<T> implements Iterable<T> {
                static Stream<String> from(BufferedReader reader) {
                    new Stream<>(data: ['a', 'b', 'c'])
                }

                List<T> data

                Iterator<T> iterator() { data.iterator() }

                def <U> Stream<U> flatMap(Closure<? extends Collection<U>> closure) {
                    new Stream<U>(data: data.collect(closure).flatten() as List)
                }
            }

            Map<String, Integer> frequencies = new HashMap<>()
            frequencies = frequencies.withDefault { 0 }
            Stream.from(null)
                .flatMap { String s -> s.toList() }
                .each { String s -> frequencies[s.toUpperCase()]++ }
            assert frequencies == [A:1, B:1, C:1]
        '''
    }

    void testShouldNotRequireIntermediateVariableToPassWithEachParamInference() {
        assertScript '''
            class Stream<T> implements Iterable<T> {
                static Stream<String> from(BufferedReader reader) {
                    new Stream<>(data: ['a', 'b', 'c'])
                }

                List<T> data

                Iterator<T> iterator() { data.iterator() }

                def <U> Stream<U> flatMap(Closure<? extends Collection<U>> closure) {
                    new Stream<U>(data: data.collect(closure).flatten() as List)
                }
            }

            Map<String, Integer> frequencies = new HashMap<>()
            frequencies = frequencies.withDefault { 0 }
            Stream.from(null)
                .flatMap { String s -> s.toList() }
                .each { frequencies[it.toUpperCase()]++ }
            assert frequencies == [A:1, B:1, C:1]
        '''
    }
}
