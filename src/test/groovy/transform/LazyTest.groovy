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

import groovy.test.GroovyTestCase

import java.lang.ref.SoftReference
import java.lang.reflect.Modifier

/**
 * Unit tests for the Lazy annotation
 */
class LazyTest extends GroovyTestCase {
    void testLazyPrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass(
          '''class MyClass {
            |    @Lazy int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private non-volatile Integer
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isVolatile(field.modifiers)
        assert field.type == Integer
    }

    void testLazyVolatilePrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass(
          '''class MyClass {
            |    @Lazy volatile int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private volatile Integer
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert Modifier.isVolatile(field.modifiers)
        assert field.type == Integer
    }

    void testLazySoftPrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass(
          '''class MyClass {
            |    @Lazy(soft=true) int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private non-volatile SoftReference
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isVolatile(field.modifiers)
        assert field.type == SoftReference
    }

    void testLazyVolatileSoftPrimitiveWrapping() {
        def tester = new GroovyClassLoader().parseClass(
          '''class MyClass {
            |    @Lazy(soft=true) volatile int index = { ->
            |        1
            |    }
            |}'''.stripMargin() )
        // Should be a private volatile SoftReference
        def field = tester.getDeclaredField( '$index' )
        assert field
        assert Modifier.isPrivate(field.modifiers)
        assert Modifier.isVolatile(field.modifiers)
        assert field.type == SoftReference
    }
}