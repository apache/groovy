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
package org.codehaus.groovy.transform


import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Unit tests for {@link AsyncASTTransformation}.
 */
class AsyncASTTransformationTest {

    @Test
    void testAsyncAnnotation() {
        ['@groovy.transform.CompileStatic', '@groovy.transform.CompileDynamic', ''].each { compileAnnotation ->
            assertScript """
                $compileAnnotation
                class A {
                    @groovy.transform.Async
                    String fetchName() {
                        return 'Daniel'
                    }
                }
                def result = new A().fetchName()
                assert result instanceof groovy.util.concurrent.async.Promise
                assert result.await() == 'Daniel'
                assert result.done
            """
        }
    }

    @Test
    void testAsyncAnnotationOnVoidMethod() {
        ['@groovy.transform.CompileStatic', '@groovy.transform.CompileDynamic', ''].each { compileAnnotation ->
            assertScript """
                $compileAnnotation
                class A {
                    @groovy.transform.Async
                    String fetchName() {
                        return 'Daniel'
                    }

                    @groovy.transform.Async
                    void processName() {
                        groovy.util.concurrent.async.Promise<String> p = fetchName()
                        String name = await p
                        assert name == 'Daniel'
                        return
                    }
                }
                def result = new A().processName()
                assert result instanceof groovy.util.concurrent.async.Promise
                await result
                assert result.done
            """
        }

    }
}
