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
package groovy.typecheckers

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class NullCheckerTest {

    private static GroovyShell shell
    private static GroovyShell strictShell

    private static final String ANNOS = '''
        import java.lang.annotation.*
        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface Nullable {}
        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface NonNull {}
        @Target([ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface MonotonicNonNull {}
    '''

    @BeforeAll
    static void setUp() {
        shell = new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(groovy.transform.TypeChecked)
            customizer.annotationParameters = [extensions: 'groovy.typecheckers.NullChecker']
            addCompilationCustomizers(customizer)
        })
        strictShell = new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(groovy.transform.TypeChecked)
            customizer.annotationParameters = [extensions: 'groovy.typecheckers.NullChecker(strict: true)']
            addCompilationCustomizers(customizer)
        })
    }

    // === Annotation-based: null assignment to @NonNull ===

    @Test
    void testNullAssignedToNonNullField() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @NonNull String name
                void bar() {
                    name = null
                }
            }
        '''
        assert err.message.contains("Cannot assign null to @NonNull variable 'name'")
    }

    @Test
    void testNonNullFieldWithValidValue() {
        assertScript shell, ANNOS + '''
            class Foo {
                @NonNull String name
                void bar() {
                    name = 'hello'
                }
            }
            new Foo().bar()
        '''
    }

    @Test
    void testNullReassignedToNonNullField() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @NonNull String name
                void bar() {
                    name = 'hello'
                    name = null
                }
            }
        '''
        assert err.message.contains("Cannot assign null to @NonNull variable 'name'")
    }

    // === Annotation-based: null passed to @NonNull parameter ===

    @Test
    void testNullPassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void main(String[] args) {
                    bar(null)
                }
            }
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 's'")
    }

    @Test
    void testNullablePassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(@Nullable String x) {
                    bar(x)
                }
            }
        '''
        assert err.message.contains("Cannot pass @Nullable value to @NonNull parameter 's'")
    }

    @Test
    void testValidValuePassedToNonNullParameter() {
        assertScript shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { assert s.length() > 0 }
            }
            Foo.bar('hello')
        '''
    }

    // === Annotation-based: return value checks ===

    @Test
    void testNullReturnFromNonNullMethod() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @NonNull static String bar() {
                    return null
                }
            }
        '''
        assert err.message.contains("Cannot return null from @NonNull method 'bar'")
    }

    @Test
    void testNullableReturnFromNonNullMethod() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @NonNull static String bar(@Nullable String s) {
                    return s
                }
            }
        '''
        assert err.message.contains("Cannot return @Nullable value from @NonNull method 'bar'")
    }

    @Test
    void testValidReturnFromNonNullMethod() {
        assertScript shell, ANNOS + '''
            class Foo {
                @NonNull static String bar() {
                    return 'hello'
                }
            }
            assert Foo.bar() == 'hello'
        '''
    }

    // === Annotation-based: dereference checks ===

    @Test
    void testNullableDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@Nullable String s) {
                    s.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testNullableSafeNavigation() {
        assertScript shell, ANNOS + '''
            class Foo {
                static Integer bar(@Nullable String s) {
                    s?.length()
                }
            }
            assert Foo.bar(null) == null
        '''
    }

    @Test
    void testNullablePropertySafeNavigation() {
        assertScript shell, ANNOS + '''
            class Person {
                String name
            }
            class Foo {
                static String bar(@Nullable Person p) {
                    p?.name
                }
            }
            assert Foo.bar(null) == null
        '''
    }

    @Test
    void testNonNullParameterDereference() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@NonNull String s) {
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    // === Null guards ===

    @Test
    void testNullableWithNotNullGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s != null) {
                        return s.length()
                    }
                    return -1
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testNullableWithReversedNotNullGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (null != s) {
                        return s.length()
                    }
                    return -1
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testNullableWithEarlyReturn() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s == null) return -1
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testNullableWithEarlyThrow() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s == null) throw new IllegalArgumentException()
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testNullableInElseBlock() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s == null) {
                        return -1
                    } else {
                        return s.length()
                    }
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    // === @NullCheck integration ===

    @Test
    void testNullCheckMethodNullArg() {
        def err = shouldFail shell, '''
            import groovy.transform.NullCheck

            class Foo {
                @NullCheck
                static void bar(String s) { }
                static void main(String[] args) {
                    bar(null)
                }
            }
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 's'")
    }

    @Test
    void testNullCheckClassNullArg() {
        def err = shouldFail shell, '''
            import groovy.transform.NullCheck

            @NullCheck
            class Foo {
                static void bar(String s) { }
                static void main(String[] args) {
                    bar(null)
                }
            }
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 's'")
    }

    @Test
    void testNullCheckSkipsPrimitiveParams() {
        assertScript shell, '''
            import groovy.transform.NullCheck

            @NullCheck
            class Foo {
                static int bar(int x) { x + 1 }
            }
            assert Foo.bar(5) == 6
        '''
    }

    @Test
    void testNullCheckRespectsNullableParam() {
        // @NullCheck + @Nullable: checker allows null at compile time (overrides @NullCheck for type-checking)
        // but @NullCheck still generates a runtime null check
        assertScript shell, ANNOS + '''
            import groovy.transform.NullCheck

            @NullCheck
            class Foo {
                static String bar(@Nullable String s) { s?.toUpperCase() }
            }
            assert Foo.bar('hi') == 'HI'
        '''
    }

    @Test
    void testNullCheckWithValidArgs() {
        assertScript shell, '''
            import groovy.transform.NullCheck

            @NullCheck
            class Foo {
                static String bar(String s) { s.toUpperCase() }
            }
            assert Foo.bar('hi') == 'HI'
        '''
    }

    // === @NonNullByDefault ===

    @Test
    void testNonNullByDefaultNullParam() {
        def err = shouldFail shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NonNullByDefault {}

            @NonNullByDefault
            class Foo {
                static void bar(String s) { }
                static void main(String[] args) {
                    bar(null)
                }
            }
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 's'")
    }

    @Test
    void testNonNullByDefaultNullReturn() {
        def err = shouldFail shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NonNullByDefault {}

            @NonNullByDefault
            class Foo {
                static String bar() {
                    return null
                }
            }
        '''
        assert err.message.contains("Cannot return null from @NonNull method 'bar'")
    }

    @Test
    void testNonNullByDefaultNullFieldAssign() {
        def err = shouldFail shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NonNullByDefault {}

            @NonNullByDefault
            class Foo {
                String name
                void clear() {
                    name = null
                }
            }
        '''
        assert err.message.contains("Cannot assign null to @NonNull variable 'name'")
    }

    @Test
    void testNonNullByDefaultNullableOverride() {
        assertScript shell, ANNOS + '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NonNullByDefault {}

            @NonNullByDefault
            class Foo {
                @Nullable static String bar() { return null }
                static void baz(@Nullable String s) { }
            }
            assert Foo.bar() == null
            Foo.baz(null)
        '''
    }

    @Test
    void testNonNullByDefaultValidCode() {
        assertScript shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NonNullByDefault {}

            @NonNullByDefault
            class Foo {
                static String bar(String s) { s.toUpperCase() }
            }
            assert Foo.bar('hi') == 'HI'
        '''
    }

    @Test
    void testNonNullByDefaultVoidMethodOk() {
        assertScript shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NonNullByDefault {}

            @NonNullByDefault
            class Foo {
                static void bar() { }
            }
            Foo.bar()
        '''
    }

    @Test
    void testParametersAreNonnullByDefault() {
        def err = shouldFail shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface ParametersAreNonnullByDefault {}

            @ParametersAreNonnullByDefault
            class Foo {
                static void bar(String s) { }
                static void main(String[] args) {
                    bar(null)
                }
            }
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 's'")
    }

    // === JSpecify @NullMarked / @NullUnmarked ===

    @Test
    void testNullMarkedNullParam() {
        def err = shouldFail shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NullMarked {}

            @NullMarked
            class Foo {
                static void bar(String s) { }
                static void main(String[] args) {
                    bar(null)
                }
            }
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 's'")
    }

    @Test
    void testNullMarkedNullReturn() {
        def err = shouldFail shell, '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NullMarked {}

            @NullMarked
            class Foo {
                static String bar() { return null }
            }
        '''
        assert err.message.contains("Cannot return null from @NonNull method 'bar'")
    }

    @Test
    void testNullUnmarkedOverridesNullMarked() {
        assertScript shell, ANNOS + '''
            import java.lang.annotation.*
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NullMarked {}
            @Target(ElementType.TYPE)
            @Retention(RetentionPolicy.RUNTIME)
            @interface NullUnmarked {}

            @NullMarked
            class Outer {
                @NullUnmarked
                static class Inner {
                    static String bar() { return null }
                }
            }
            assert Outer.Inner.bar() == null
        '''
    }

    // === JSpecify real annotations ===

    @Test
    void testJSpecifyNullableDereference() {
        def err = shouldFail shell, '''
            import org.jspecify.annotations.Nullable

            class Foo {
                static void bar(@Nullable String s) {
                    s.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testJSpecifyNullableSafeNavigation() {
        assertScript shell, '''
            import org.jspecify.annotations.Nullable

            class Foo {
                static Integer bar(@Nullable String s) {
                    s?.length()
                }
            }
            assert Foo.bar(null) == null
        '''
    }

    @Test
    void testJSpecifyNullMarkedNullParam() {
        def err = shouldFail shell, '''
            import org.jspecify.annotations.NullMarked

            @NullMarked
            class Foo {
                static void bar(String s) { }
                static void main(String[] args) {
                    bar(null)
                }
            }
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 's'")
    }

    @Test
    void testJSpecifyNullMarkedNullableOverride() {
        assertScript shell, '''
            import org.jspecify.annotations.NullMarked
            import org.jspecify.annotations.Nullable

            @NullMarked
            class Foo {
                static void bar(@Nullable String s) { }
            }
            Foo.bar(null)
        '''
    }

    @Test
    void testJSpecifyNullUnmarked() {
        assertScript shell, '''
            import org.jspecify.annotations.NullMarked
            import org.jspecify.annotations.NullUnmarked

            @NullMarked
            class Outer {
                @NullUnmarked
                static class Inner {
                    static String bar() { return null }
                }
            }
            assert Outer.Inner.bar() == null
        '''
    }

    // === Unannotated code ===

    @Test
    void testUnannotatedCodeNoErrors() {
        assertScript shell, '''
            def x = 'hello'
            assert x.length() == 5
        '''
    }

    @Test
    void testUnannotatedNullAssignNoErrorWithNullChecker() {
        assertScript shell, '''
            def x = null
            x.toString()
        '''
    }

    // === @Lazy (implicit @MonotonicNonNull) ===

    @Test
    void testLazyFieldAccessThroughGetterNoWarning() {
        assertScript shell, '''
            class Foo {
                @Lazy String value = 'computed'
                String getUpperValue() {
                    value.toUpperCase()
                }
            }
            assert new Foo().upperValue == 'COMPUTED'
        '''
    }

    @Test
    void testLazyFieldAccessWithList() {
        assertScript shell, '''
            class Foo {
                @Lazy ArrayList items = [1, 2, 3]
                int getTotal() { (int) items.sum() }
            }
            assert new Foo().total == 6
        '''
    }

    // === Ternary/elvis null detection ===

    @Test
    void testStrictTernaryWithNullBranch() {
        def err = shouldFail strictShell, '''
            class Foo {
                static int bar(boolean flag) {
                    def x = flag ? 'hello' : null
                    x.length()
                }
            }
        '''
        assert err.message.contains("'x' may be null")
    }

    @Test
    void testStrictTernaryBothNonNull() {
        assertScript strictShell, '''
            class Foo {
                static int bar(boolean flag) {
                    def x = flag ? 'hello' : 'world'
                    x.length()
                }
            }
            assert Foo.bar(true) == 5
        '''
    }

    @Test
    void testStrictTernaryNullReassignment() {
        def err = shouldFail strictShell, '''
            class Foo {
                static int bar(boolean flag) {
                    def x = 'safe'
                    x = flag ? null : 'ok'
                    x.length()
                }
            }
        '''
        assert err.message.contains("'x' may be null")
    }

    @Test
    void testStrictElvisWithNullFallback() {
        def err = shouldFail strictShell, '''
            class Foo {
                static int bar(String s) {
                    def x = s ?: null
                    x.length()
                }
            }
        '''
        assert err.message.contains("'x' may be null")
    }

    // === Elvis assignment operator ?= ===

    @Test
    void testStrictElvisAssignmentClearsNullable() {
        assertScript strictShell, '''
            class Foo {
                static int bar() {
                    String x = null
                    x ?= 'default'
                    x.length()
                }
            }
            assert Foo.bar() == 7
        '''
    }

    // === @MonotonicNonNull ===

    @Test
    void testMonotonicNonNullDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @MonotonicNonNull String name
                void bar() {
                    name.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 'name' is @Nullable")
    }

    @Test
    void testMonotonicNonNullWithGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                @MonotonicNonNull String name
                int bar() {
                    if (name != null) {
                        return name.length()
                    }
                    return -1
                }
            }
            assert new Foo().bar() == -1
        '''
    }

    @Test
    void testMonotonicNonNullReassignNull() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @MonotonicNonNull String name
                void bar() {
                    name = 'hello'
                    name = null
                }
            }
        '''
        assert err.message.contains("Cannot assign null to @MonotonicNonNull variable 'name' after non-null assignment")
    }

    // === NullChecker(strict: true): flow-sensitive checks ===

    @Test
    void testStrictFlowNullReassignInsideGuard() {
        def err = shouldFail strictShell, '''
            class Foo {
                static void bar() {
                    def x = (String) null
                    if (x != null) {
                        x = null
                        x.toString()
                    }
                }
            }
        '''
        assert err.message.contains("'x' may be null")
    }

    @Test
    void testStrictFlowCastedNullDetected() {
        def err = shouldFail strictShell, '''
            def x = (String) null
            x.toString()
        '''
        assert err.message.contains("'x' may be null")
    }

    @Test
    void testStrictFlowNullDereferenceDetected() {
        def err = shouldFail strictShell, '''
            def x = null
            x.toString()
        '''
        assert err.message.contains("'x' may be null")
    }

    @Test
    void testStrictFlowUninitializedVariable() {
        def err = shouldFail strictShell, '''
            class Foo {
                static int bar() {
                    String result
                    return result.length()
                }
            }
        '''
        assert err.message.contains("'result' may be null")
    }

    @Test
    void testStrictFlowUninitializedThenAssigned() {
        assertScript strictShell, '''
            class Foo {
                static int bar() {
                    String result
                    result = 'hello'
                    return result.length()
                }
            }
            assert Foo.bar() == 5
        '''
    }

    @Test
    void testStrictFlowPrimitiveUninitializedOk() {
        assertScript strictShell, '''
            int x
            assert x == 0
        '''
    }

    @Test
    void testStrictFlowNullReassignedNonNull() {
        assertScript strictShell, '''
            def x = null
            x = 'hello'
            assert x.toString() == 'hello'
        '''
    }

    @Test
    void testStrictFlowNullGuardProtects() {
        assertScript strictShell, '''
            def x = (String) null
            if (x != null) {
                x.toString()
            }
        '''
    }

    @Test
    void testStrictFlowEarlyReturnProtects() {
        assertScript strictShell, '''
            class Foo {
                static void bar() {
                    def x = (String) null
                    if (x == null) return
                    x.toString()
                }
            }
            Foo.bar()
        '''
    }

    @Test
    void testStrictFlowNullableMethodReturn() {
        def err = shouldFail strictShell, ANNOS + '''
            class Foo {
                @Nullable static String findName() { return null }
                static void main(String[] args) {
                    def name = findName()
                    name.length()
                }
            }
        '''
        assert err.message.contains("'name' may be null")
    }

    @Test
    void testStrictAlsoChecksAnnotations() {
        def err = shouldFail strictShell, ANNOS + '''
            class Foo {
                static void bar(@Nullable String s) {
                    s.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    // === Chained method call dereference ===

    @Test
    void testNullableMethodReturnDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @Nullable static String findName() { return null }
                static void main(String[] args) {
                    findName().length()
                }
            }
        '''
        assert err.message.contains("'findName()' may return null")
    }

    @Test
    void testNullableMethodReturnSafeNavigation() {
        assertScript shell, ANNOS + '''
            class Foo {
                @Nullable static String findName() { return null }
            }
            assert Foo.findName()?.length() == null
        '''
    }

    // === Variable-to-variable nullability propagation ===

    @Test
    void testStrictFlowVariableToVariablePropagation() {
        def err = shouldFail strictShell, '''
            def x = (String) null
            def y = x
            y.toString()
        '''
        assert err.message.contains("'y' may be null")
    }

    @Test
    void testStrictFlowVariableToVariableReassignment() {
        def err = shouldFail strictShell, '''
            class Foo {
                static void bar() {
                    def x = (String) null
                    def y = 'safe'
                    y = x
                    y.toString()
                }
            }
        '''
        assert err.message.contains("'y' may be null")
    }

    @Test
    void testStrictFlowVariableToVariableNonNull() {
        assertScript strictShell, '''
            def x = 'hello'
            def y = x
            assert y.toString() == 'hello'
        '''
    }
}
