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
 * GROOVY-11110: {@code Stream.<T>collect(...)} used to infer a result of
 * type {@code String} when {@code test} had a bounded type parameter
 * also named {@code T}.
 */
final class Groovy11110 {

    @Test
    void testStreamCollectDoesNotConfuseBoundedTypeParameter() {
        assertScript '''
            import groovy.transform.TypeChecked

            import java.util.stream.Stream

            @TypeChecked
            class Holder {
                static <T extends Number> void test() {
                    java.util.function.BiConsumer<T, T> func = null
                    Stream<String> stream = null
                    T res = stream.<T>collect(() -> (T) null, (T finisher, String hijacking) -> { }, func)
                }
            }

            true
        '''
        assertScript '''
            import groovy.transform.CompileStatic

            import java.util.stream.Stream

            @CompileStatic
            class Holder {
                static <T extends Number> void test() {
                    java.util.function.BiConsumer<T, T> func = null
                    Stream<String> stream = null
                    T res = stream.<T>collect(() -> (T) null, (T finisher, String hijacking) -> { }, func)
                }
            }

            true
        '''
    }
}
