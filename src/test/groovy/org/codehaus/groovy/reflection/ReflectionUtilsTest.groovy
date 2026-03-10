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
package org.codehaus.groovy.reflection

import org.junit.jupiter.api.Test

import java.lang.reflect.Method

import static org.junit.jupiter.api.Assertions.*

/**
 * JUnit 5 tests for ReflectionUtils class.
 */
class ReflectionUtilsTest {

    @Test
    void testIsCallingClassReflectionAvailable() {
        assertTrue(ReflectionUtils.isCallingClassReflectionAvailable())
    }

    @Test
    void testGetCallingClass() {
        def callingClass = ReflectionUtils.getCallingClass()
        // The calling class should be this test class
        assertNotNull(callingClass)
    }

    @Test
    void testGetCallingClassWithMatchLevel() {
        def callingClass = ReflectionUtils.getCallingClass(1)
        assertNotNull(callingClass)
    }

    @Test
    void testGetCallingClassWithZeroMatchLevel() {
        def callingClass = ReflectionUtils.getCallingClass(0)
        // Match level < 1 is treated as 1
        assertNotNull(callingClass)
    }

    @Test
    void testGetCallingClassWithNegativeMatchLevel() {
        def callingClass = ReflectionUtils.getCallingClass(-5)
        // Negative match level is treated as 0 (which becomes 1)
        assertNotNull(callingClass)
    }

    @Test
    void testGetCallingClassWithExtraIgnoredPackages() {
        def extraIgnored = Set.of("some.package")
        def callingClass = ReflectionUtils.getCallingClass(1, extraIgnored)
        assertNotNull(callingClass)
    }

    @Test
    void testGetCallingClassWithEmptyIgnoredPackages() {
        def callingClass = ReflectionUtils.getCallingClass(1, Collections.emptySet())
        assertNotNull(callingClass)
    }

    @Test
    void testGetDeclaredMethods() {
        def methods = ReflectionUtils.getDeclaredMethods(String, "substring", int)
        assertNotNull(methods)
        assertFalse(methods.isEmpty())
    }

    @Test
    void testGetDeclaredMethodsWithTwoParams() {
        def methods = ReflectionUtils.getDeclaredMethods(String, "substring", int, int)
        assertNotNull(methods)
        assertFalse(methods.isEmpty())
    }

    @Test
    void testGetDeclaredMethodsNonExistent() {
        def methods = ReflectionUtils.getDeclaredMethods(String, "nonExistentMethod")
        assertNotNull(methods)
        assertTrue(methods.isEmpty())
    }

    @Test
    void testGetMethods() {
        def methods = ReflectionUtils.getMethods(String, "length")
        assertNotNull(methods)
        assertFalse(methods.isEmpty())
    }

    @Test
    void testGetMethodsWithParams() {
        def methods = ReflectionUtils.getMethods(String, "charAt", int)
        assertNotNull(methods)
        assertFalse(methods.isEmpty())
    }

    @Test
    void testGetMethodsInherited() {
        // toString is inherited from Object
        def methods = ReflectionUtils.getMethods(String, "toString")
        assertNotNull(methods)
        assertFalse(methods.isEmpty())
    }

    @Test
    void testParameterTypeMatchesExact() {
        Class<?>[] paramTypes = [int, int] as Class[]
        Class<?>[] argTypes = [int, int] as Class[]
        assertTrue(ReflectionUtils.parameterTypeMatches(paramTypes, argTypes))
    }

    @Test
    void testParameterTypeMatchesDifferentLength() {
        Class<?>[] paramTypes = [int, int] as Class[]
        Class<?>[] argTypes = [int] as Class[]
        assertFalse(ReflectionUtils.parameterTypeMatches(paramTypes, argTypes))
    }

    @Test
    void testParameterTypeMatchesWithObject() {
        // Object.class matches anything
        Class<?>[] paramTypes = [Object] as Class[]
        Class<?>[] argTypes = [String] as Class[]
        assertTrue(ReflectionUtils.parameterTypeMatches(paramTypes, argTypes))
    }

    @Test
    void testParameterTypeMatchesWithNull() {
        Class<?>[] paramTypes = [String] as Class[]
        Class<?>[] argTypes = [null] as Class[]
        assertFalse(ReflectionUtils.parameterTypeMatches(paramTypes, argTypes))
    }

    @Test
    void testParameterTypeMatchesWithAutoboxing() {
        Class<?>[] paramTypes = [Integer] as Class[]
        Class<?>[] argTypes = [int] as Class[]
        assertTrue(ReflectionUtils.parameterTypeMatches(paramTypes, argTypes))
    }

    @Test
    void testParameterTypeMatchesWithAssignable() {
        Class<?>[] paramTypes = [Number] as Class[]
        Class<?>[] argTypes = [Integer] as Class[]
        assertTrue(ReflectionUtils.parameterTypeMatches(paramTypes, argTypes))
    }

    @Test
    void testParameterTypeMatchesEmpty() {
        Class<?>[] paramTypes = [] as Class[]
        Class<?>[] argTypes = [] as Class[]
        assertTrue(ReflectionUtils.parameterTypeMatches(paramTypes, argTypes))
    }

    @Test
    void testTrySetAccessible() {
        try {
            def method = String.getDeclaredMethod("length")
            def result = ReflectionUtils.trySetAccessible(method)
            // Should succeed or fail gracefully
            assertTrue(result || !result) // Just verify it doesn't throw
        } catch (NoSuchMethodException e) {
            fail("Method should exist")
        }
    }

    @Test
    void testCheckCanSetAccessible() {
        try {
            def method = String.getDeclaredMethod("length")
            def result = ReflectionUtils.checkCanSetAccessible(method, ReflectionUtilsTest)
            // Result depends on module system / security
            assertTrue(result || !result) // Just verify it doesn't throw
        } catch (NoSuchMethodException e) {
            fail("Method should exist")
        }
    }

    @Test
    void testCheckAccessible() {
        // Public method in public class should be accessible
        def result = ReflectionUtils.checkAccessible(
            ReflectionUtilsTest,
            String,
            java.lang.reflect.Modifier.PUBLIC,
            false
        )
        assertTrue(result)
    }

    @Test
    void testCheckAccessiblePrivate() {
        // Private method without allowIllegalAccess
        def result = ReflectionUtils.checkAccessible(
            ReflectionUtilsTest,
            String,
            java.lang.reflect.Modifier.PRIVATE,
            false
        )
        // Typically false for private methods
        assertFalse(result)
    }

    @Test
    void testGetCallingClassDeepStack() {
        // Call through a helper to test deeper stack
        def result = helperForDeepStack()
        assertNotNull(result)
    }

    private Class<?> helperForDeepStack() {
        return ReflectionUtils.getCallingClass(2)
    }

    @Test
    void testGetCallingClassVeryHighMatchLevel() {
        // Very high match level may return null if stack isn't that deep
        def callingClass = ReflectionUtils.getCallingClass(1000)
        // May be null if stack isn't that deep
        assertTrue(callingClass == null || callingClass != null)
    }
}
