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
package groovy.transform

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for the {@link Lazy} AST transform.
 */
final class LazyTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            staticStar 'java.lang.reflect.Modifier'
        }
    }

    @Test
    void testLazyPrimitiveWrapping() {
        assertScript shell, '''
            class C {
                @Lazy int index = { -> 1 }
            }

            def field = C.getDeclaredField('$index')
            assert field
            assert field.type == Integer
            assert isPrivate(field.modifiers)
            assert !isVolatile(field.modifiers)
        '''
    }

    @Test
    void testLazyVolatilePrimitiveWrapping() {
        assertScript shell, '''
            class C {
                @Lazy volatile int index = { -> 1 }
            }

            def field = C.getDeclaredField('$index')
            assert field
            assert field.type == Integer
            assert isPrivate(field.modifiers)
            assert isVolatile(field.modifiers)
        '''
    }

    @Test
    void testLazySoftPrimitiveWrapping() {
        assertScript shell, '''
            import java.lang.ref.SoftReference as SoftRef

            class C {
                @Lazy(soft=true) int index = { -> 1 }
            }

            def field = C.getDeclaredField('$index')
            assert field
            assert field.type == SoftRef
            assert isPrivate(field.modifiers)
            assert !isVolatile(field.modifiers)
        '''
    }

    @Test
    void testLazyVolatileSoftPrimitiveWrapping() {
        assertScript shell, '''
            import java.lang.ref.SoftReference as SoftRef

            class C {
                @Lazy(soft=true) volatile int index = { -> 1 }
            }

            def field = C.getDeclaredField('$index')
            assert field
            assert field.type == SoftRef
            assert isPrivate(field.modifiers)
            assert isVolatile(field.modifiers)
        '''
    }
}
