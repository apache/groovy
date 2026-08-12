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

    // === Broader null guards (GROOVY-12208) ===

    @Test
    void testGroovyTruthGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s) {
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
    void testGroovyTruthGuardElseBranchStillChecked() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s) {
                        return s.length()
                    } else {
                        return s.length()
                    }
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testNegatedGroovyTruthEarlyExit() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (!s) return -1
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testConjunctionGuardsRightOperandAndThenBlock() {
        assertScript shell, ANNOS + '''
            class Foo {
                static String bar(@Nullable String s) {
                    if (s != null && s.length() > 0) {
                        return s.toUpperCase()
                    }
                    return 'empty'
                }
            }
            assert Foo.bar('hi') == 'HI'
            assert Foo.bar(null) == 'empty'
        '''
    }

    @Test
    void testConjunctionElseBranchStillChecked() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s != null && s.length() > 0) {
                        return s.length()
                    } else {
                        return s.length()
                    }
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testConjunctionInBooleanExpression() {
        assertScript shell, ANNOS + '''
            class Foo {
                static boolean bar(@Nullable String s) {
                    boolean ok = s != null && s.length() > 0
                    return ok
                }
            }
            assert Foo.bar('hi')
            assert !Foo.bar(null)
        '''
    }

    @Test
    void testDisjunctionGuardsRightOperandAndEarlyExit() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (s == null || s.isEmpty()) return -1
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar('') == -1
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testDisjunctionThenBlockStillChecked() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s, @Nullable String t) {
                    if (s != null || t != null) {
                        return s.length()
                    }
                    return -1
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testNegatedDisjunctionGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (!(s == null || s.isEmpty())) {
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
    void testInstanceofGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable Object o) {
                    if (o instanceof String) {
                        return o.length()
                    }
                    return -1
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testInstanceofElseBranchStillChecked() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable Object o) {
                    if (o instanceof String) {
                        return o.length()
                    } else {
                        return o.hashCode()
                    }
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 'o' is @Nullable")
    }

    @Test
    void testNegatedInstanceofEarlyExit() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable Object o) {
                    if (o !instanceof String) return -1
                    o.hashCode()
                }
            }
            assert Foo.bar('hi') == 'hi'.hashCode()
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testObjectsNonNullGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (Objects.nonNull(s)) {
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
    void testObjectsIsNullEarlyExit() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (Objects.isNull(s)) return -1
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testObjectsIsNullElseGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    if (Objects.isNull(s)) {
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

    @Test
    void testAssertNotNullGuards() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    assert s != null
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testAssertGroovyTruthGuards() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    assert s
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testAssertWithMessageGuards() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    assert s != null : 'must not be null'
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testAssertNullDoesNotGuard() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    assert s == null
                    s.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testWhileGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    int total = 0
                    while (s != null) {
                        total += s.length()
                        s = null
                    }
                    total
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == 0
        '''
    }

    @Test
    void testWhileGuardDoesNotExtendPastLoop() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    while (s != null) {
                        s = null
                    }
                    s.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testTernaryGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    return s != null ? s.length() : -1
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testTernaryReversedGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    return s == null ? -1 : s.length()
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testTernaryGroovyTruthGuard() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    return s ? s.length() : -1
                }
            }
            assert Foo.bar('hi') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testTernaryUnguardedBranchStillChecked() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    return s == null ? s.length() : -1
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    // === Validator and assertion narrowing (requireNonNull, assertNotNull, assertThat) ===

    @Test
    void testRequireNonNullNarrowsNullableParameter() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    Objects.requireNonNull(s)
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testRequireNonNullWithMessageNarrows() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    Objects.requireNonNull(s, 's must not be null')
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testStaticallyImportedRequireNonNullNarrows() {
        assertScript shell, 'import static java.util.Objects.requireNonNull\n' + ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    requireNonNull(s)
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testRequireNonNullResultDereference() {
        assertScript shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    Objects.requireNonNull(s).length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testRequireNonNullNarrowsFlowNullable() {
        assertScript strictShell, '''
            class Foo {
                static int bar(boolean b) {
                    def s = b ? 'x' : null
                    Objects.requireNonNull(s)
                    s.length()
                }
            }
            assert Foo.bar(true) == 1
        '''
    }

    @Test
    void testCheckNotNullNarrows() {
        assertScript shell, ANNOS + '''
            class Foo {
                static <T> T checkNotNull(T ref) {
                    if (ref == null) throw new NullPointerException()
                    ref
                }
                static int bar(@Nullable String s) {
                    checkNotNull(s)
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testUnrecognizedValidatorDoesNotNarrow() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void verifyNotNull(Object o) { }
                static int bar(@Nullable String s) {
                    verifyNotNull(s)
                    s.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    @Test
    void testAssertNotNullNarrowsActualNotMessage() {
        // JUnit 4 parameter order: (message, actual)
        assertScript shell, ANNOS + '''
            class Foo {
                static void assertNotNull(String message, Object actual) {
                    assert actual != null : message
                }
                static int bar(@Nullable String s) {
                    assertNotNull('s must be set', s)
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testJUnit5AssertNotNullNarrows() {
        assertScript shell, 'import static org.junit.jupiter.api.Assertions.assertNotNull\n' + ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    assertNotNull(s)
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testAssertThatIsNotNullNarrows() {
        assertScript shell, ANNOS + '''
            class Check {
                Check isNotNull() { this }
                Check describedAs(String description) { this }
            }
            class Foo {
                static Check assertThat(Object actual) { new Check() }
                static int bar(@Nullable String s) {
                    assertThat(s).isNotNull()
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testAssertThatChainedIsNotNullNarrows() {
        assertScript shell, ANNOS + '''
            class Check {
                Check isNotNull() { this }
                Check describedAs(String description) { this }
            }
            class Foo {
                static Check assertThat(Object actual) { new Check() }
                static int bar(@Nullable String s) {
                    assertThat(s).describedAs('the name').isNotNull()
                    s.length()
                }
            }
            assert Foo.bar('hi') == 2
        '''
    }

    @Test
    void testAssertThatWithoutIsNotNullDoesNotNarrow() {
        def err = shouldFail shell, ANNOS + '''
            class Check {
                Check isNotNull() { this }
                Check describedAs(String description) { this }
            }
            class Foo {
                static Check assertThat(Object actual) { new Check() }
                static int bar(@Nullable String s) {
                    assertThat(s).describedAs('the name')
                    s.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
    }

    // === Nullable expression results (GROOVY-12209) ===

    @Test
    void testSafeNavResultDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    s?.trim().length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's?.trim()' may be null")
    }

    @Test
    void testSafeNavResultSafeDereference() {
        assertScript shell, ANNOS + '''
            class Foo {
                static Integer bar(@Nullable String s) {
                    s?.trim()?.length()
                }
            }
            assert Foo.bar(' hi ') == 2
            assert Foo.bar(null) == null
        '''
    }

    @Test
    void testSafeNavPropertyResultDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Address {
                String city
            }
            class Person {
                Address address
            }
            class Foo {
                static String bar(@Nullable Person p) {
                    p?.address.city
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 'p?.address' may be null")
    }

    @Test
    void testSafeNavPropertyResultSafeDereference() {
        assertScript shell, ANNOS + '''
            class Address {
                String city
            }
            class Person {
                Address address
            }
            class Foo {
                static String bar(@Nullable Person p) {
                    p?.address?.city
                }
            }
            assert Foo.bar(null) == null
            assert Foo.bar(new Person(address: new Address(city: 'Perth'))) == 'Perth'
        '''
    }

    @Test
    void testNullableCallResultPassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @Nullable static String find() { null }
                static void bar(@NonNull String s) { }
                static void baz() {
                    bar(find())
                }
            }
        '''
        assert err.message.contains("Cannot pass @Nullable value to @NonNull parameter 's' of 'bar'")
    }

    @Test
    void testSafeNavResultPassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(@Nullable String x) {
                    bar(x?.trim())
                }
            }
        '''
        assert err.message.contains("Cannot pass @Nullable value to @NonNull parameter 's' of 'bar'")
    }

    @Test
    void testGuardedNullablePassedToNonNullParameter() {
        assertScript shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(@Nullable String x) {
                    if (x != null) {
                        bar(x)
                    }
                }
            }
            Foo.baz('hi')
            Foo.baz(null)
        '''
    }

    @Test
    void testTernaryWithNullBranchPassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(boolean flag) {
                    bar(flag ? 'a' : null)
                }
            }
        '''
        assert err.message.contains("Cannot pass @Nullable value to @NonNull parameter 's' of 'bar'")
    }

    @Test
    void testTernaryWithNullableVarBranchPassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(boolean flag, @Nullable String x) {
                    bar(flag ? x : 'a')
                }
            }
        '''
        assert err.message.contains("Cannot pass @Nullable value to @NonNull parameter 's' of 'bar'")
    }

    @Test
    void testGuardedTernaryPassedToNonNullParameter() {
        assertScript shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(@Nullable String x) {
                    bar(x != null ? x : 'default')
                }
            }
            Foo.baz('hi')
            Foo.baz(null)
        '''
    }

    @Test
    void testElvisWithSafeFallbackPassedToNonNullParameter() {
        assertScript shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(@Nullable String x) {
                    bar(x ?: 'default')
                }
            }
            Foo.baz('hi')
            Foo.baz(null)
        '''
    }

    @Test
    void testElvisWithNullFallbackPassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(@Nullable String x) {
                    bar(x ?: null)
                }
            }
        '''
        assert err.message.contains("Cannot pass @Nullable value to @NonNull parameter 's' of 'bar'")
    }

    @Test
    void testCastNullablePassedToNonNullParameter() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static void bar(@NonNull String s) { }
                static void baz(@Nullable String x) {
                    bar((String) x)
                }
            }
        '''
        assert err.message.contains("Cannot pass @Nullable value to @NonNull parameter 's' of 'bar'")
    }

    @Test
    void testTernaryWithNullableBranchReturnedFromNonNullMethod() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @NonNull static String bar(boolean flag, @Nullable String s) {
                    return flag ? s : 'default'
                }
            }
        '''
        assert err.message.contains("Cannot return @Nullable value from @NonNull method 'bar'")
    }

    @Test
    void testSafeNavResultReturnedFromNonNullMethod() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                @NonNull static String bar(@Nullable String s) {
                    return s?.trim()
                }
            }
        '''
        assert err.message.contains("Cannot return @Nullable value from @NonNull method 'bar'")
    }

    @Test
    void testGuardedNullableReturnedFromNonNullMethod() {
        assertScript shell, ANNOS + '''
            class Foo {
                @NonNull static String bar(@Nullable String s) {
                    if (s != null) return s
                    return 'default'
                }
            }
            assert Foo.bar('hi') == 'hi'
            assert Foo.bar(null) == 'default'
        '''
    }

    @Test
    void testElvisWithSafeFallbackReturnedFromNonNullMethod() {
        assertScript shell, ANNOS + '''
            class Foo {
                @NonNull static String bar(@Nullable String s) {
                    return s ?: 'default'
                }
            }
            assert Foo.bar('hi') == 'hi'
            assert Foo.bar(null) == 'default'
        '''
    }

    @Test
    void testNullablePropertyDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Person {
                @Nullable String nickname
            }
            class Foo {
                static int bar(Person p) {
                    p.nickname.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 'nickname' may be null")
    }

    @Test
    void testNullableGetterPropertyDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Person {
                @Nullable String getNickname() { null }
            }
            class Foo {
                static int bar(Person p) {
                    p.nickname.length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 'nickname' may be null")
    }

    @Test
    void testNullablePropertySafeDereference() {
        assertScript shell, ANNOS + '''
            class Person {
                @Nullable String nickname
            }
            class Foo {
                static Integer bar(Person p) {
                    p.nickname?.length()
                }
            }
            assert Foo.bar(new Person()) == null
            assert Foo.bar(new Person(nickname: 'Ace')) == 3
        '''
    }

    @Test
    void testNonNullablePropertyDereference() {
        assertScript shell, ANNOS + '''
            class Person {
                String name = 'Anon'
            }
            class Foo {
                static int bar(Person p) {
                    p.name.length()
                }
            }
            assert Foo.bar(new Person()) == 4
        '''
    }

    @Test
    void testCastNullableDereference() {
        def err = shouldFail shell, ANNOS + '''
            class Foo {
                static int bar(@Nullable String s) {
                    ((String) s).length()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 's' is @Nullable")
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

    // === JSpecify annotations from precompiled Java (GROOVY-12206) ===

    @Test
    void testJSpecifyNullableReturnFromPrecompiledClass() {
        def err = shouldFail shell, '''
            import groovy.typecheckers.support.JSpecifyExample

            class Foo {
                static void bar() {
                    JSpecifyExample.findValue('missing').toUpperCase()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 'findValue()' may return null")
    }

    @Test
    void testJSpecifyNonNullParameterOfPrecompiledClass() {
        def err = shouldFail shell, '''
            import groovy.typecheckers.support.JSpecifyExample

            class Foo {
                static void bar() {
                    JSpecifyExample.requireValue(null)
                }
            }
        '''
        assert err.message.contains('Cannot pass null to @NonNull parameter')
    }

    @Test
    void testJSpecifyNullableResultPassedToNonNullParameterOfPrecompiledClass() {
        def err = shouldFail strictShell, '''
            import groovy.typecheckers.support.JSpecifyExample

            class Foo {
                static void bar() {
                    def v = JSpecifyExample.findValue('missing')
                    JSpecifyExample.requireValue(v)
                }
            }
        '''
        assert err.message.contains('Cannot pass @Nullable value to @NonNull parameter')
    }

    // === Package-level @NullMarked from precompiled package-info.class (GROOVY-12207) ===

    @Test
    void testPackageNullMarkedNullParameterOfPrecompiledClass() {
        def err = shouldFail shell, '''
            import groovy.typecheckers.support.nullmarked.PackageMarkedExample

            class Foo {
                static void bar() {
                    PackageMarkedExample.greet(null)
                }
            }
        '''
        assert err.message.contains('Cannot pass null to @NonNull parameter')
    }

    @Test
    void testPackageNullMarkedNonNullParameterOfPrecompiledClassOk() {
        assertScript shell, '''
            import groovy.typecheckers.support.nullmarked.PackageMarkedExample

            class Foo {
                static String bar() {
                    PackageMarkedExample.greet('world')
                }
            }
            assert new Foo().bar() == 'hi world'
        '''
    }

    @Test
    void testPackageNullMarkedNullableReturnStillNullable() {
        def err = shouldFail shell, '''
            import groovy.typecheckers.support.nullmarked.PackageMarkedExample

            class Foo {
                static void bar() {
                    PackageMarkedExample.find('missing').toUpperCase()
                }
            }
        '''
        assert err.message.contains("Potential null dereference: 'find()' may return null")
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
    void testStrictFlowTruthGuardProtects() {
        assertScript strictShell, '''
            class Foo {
                static void bar() {
                    def x = (String) null
                    if (x) {
                        x.toString()
                    }
                }
            }
            Foo.bar()
        '''
    }

    @Test
    void testStrictFlowAssertProtects() {
        assertScript strictShell, '''
            class Foo {
                static int bar(boolean flag) {
                    def x = flag ? 'hello' : null
                    assert x != null
                    x.length()
                }
            }
            assert Foo.bar(true) == 5
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
    void testStrictFlowSafeNavResultTracked() {
        def err = shouldFail strictShell, '''
            class Foo {
                static int bar(String s) {
                    def x = s?.trim()
                    x.length()
                }
            }
        '''
        assert err.message.contains("'x' may be null")
    }

    @Test
    void testStrictFlowSafeNavResultGuarded() {
        assertScript strictShell, '''
            class Foo {
                static int bar(String s) {
                    def x = s?.trim()
                    if (x == null) return -1
                    x.length()
                }
            }
            assert Foo.bar(' hi ') == 2
            assert Foo.bar(null) == -1
        '''
    }

    @Test
    void testStrictFlowTernaryWithNullableVarBranchTracked() {
        def err = shouldFail strictShell, ANNOS + '''
            class Foo {
                static int bar(boolean flag, @Nullable String s) {
                    def x = flag ? s : 'default'
                    x.length()
                }
            }
        '''
        assert err.message.contains("'x' may be null")
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

    // === NullChecker(strict: true): @NonNull field initialization ===

    @Test
    void testStrictNonNullFieldNotInitialized() {
        def err = shouldFail strictShell, ANNOS + '''
            class Foo {
                @NonNull String name
            }
        '''
        assert err.message.contains("@NonNull field 'name' is not initialized")
    }

    @Test
    void testStrictNonNullFieldWithInitializer() {
        assertScript strictShell, ANNOS + '''
            class Foo {
                @NonNull String name = 'unknown'
            }
            assert new Foo().name == 'unknown'
        '''
    }

    @Test
    void testStrictNonNullFieldAssignedInConstructor() {
        assertScript strictShell, ANNOS + '''
            class Foo {
                @NonNull String name
                Foo(String n) { name = n }
            }
            assert new Foo('groovy').name == 'groovy'
        '''
    }

    @Test
    void testStrictNonNullFieldAssignedViaThisInConstructor() {
        assertScript strictShell, ANNOS + '''
            class Foo {
                @NonNull String name
                Foo(String name) { this.name = name }
            }
            assert new Foo('groovy').name == 'groovy'
        '''
    }

    @Test
    void testStrictNonNullFieldMissedByOneConstructor() {
        def err = shouldFail strictShell, ANNOS + '''
            class Foo {
                @NonNull String name
                Foo(String n) { name = n }
                Foo() { }
            }
        '''
        assert err.message.contains("@NonNull field 'name' is not initialized by all constructors")
    }

    @Test
    void testStrictDelegatingConstructorReliesOnDelegate() {
        assertScript strictShell, ANNOS + '''
            class Foo {
                @NonNull String name
                Foo(String n) { name = n }
                Foo() { this('unknown') }
            }
            assert new Foo().name == 'unknown'
        '''
    }

    @Test
    void testStrictInitializerBlockInitializes() {
        assertScript strictShell, ANNOS + '''
            class Foo {
                @NonNull String name
                {
                    name = 'unknown'
                }
                Foo() { }
            }
            assert new Foo().name == 'unknown'
        '''
    }

    @Test
    void testLenientModeDoesNotCheckFieldInitialization() {
        assertScript shell, ANNOS + '''
            class Foo {
                @NonNull String name
            }
            new Foo()
        '''
    }

    @Test
    void testStrictMonotonicFieldNotChecked() {
        assertScript strictShell, ANNOS + '''
            class Foo {
                @MonotonicNonNull String cached
            }
            new Foo()
        '''
    }

    @Test
    void testStrictDefaultNonNullFieldsNotChecked() {
        // only explicitly-annotated @NonNull fields are checked, so idiomatic Groovy
        // property classes constructed via named arguments stay noise-free
        assertScript strictShell, '''
            import java.lang.annotation.*
            @Target([ElementType.TYPE])
            @Retention(RetentionPolicy.RUNTIME)
            @interface NonNullByDefault {}

            @NonNullByDefault
            class Book {
                String title
            }
            assert new Book(title: 'Groovy in Action').title
        '''
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

    // === groovy-contracts: @Requires/@Ensures inferring @NonNull ===

    @Test
    void testRequiresNotEqualNullInfersNonNullParam() {
        def err = shouldFail shell, '''
            import groovy.contracts.Requires
            class Foo {
                @Requires({ x != null })
                String bar(x) { x.toString() }
            }
            new Foo().bar(null)
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 'x'")
    }

    @Test
    void testRequiresReverseNullComparisonInfersNonNullParam() {
        def err = shouldFail shell, '''
            import groovy.contracts.Requires
            class Foo {
                @Requires({ null != x })
                String bar(x) { x.toString() }
            }
            new Foo().bar(null)
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 'x'")
    }

    @Test
    void testRequiresConjunctionInfersNonNullForEachParam() {
        def err = shouldFail shell, '''
            import groovy.contracts.Requires
            class Foo {
                @Requires({ left != null && right != null && right.size() > 0 })
                String concat(left, middle, right) { left + middle + right }
            }
            new Foo().concat('a', 'b', null)
        '''
        assert err.message.contains("Cannot pass null to @NonNull parameter 'right'")
    }

    @Test
    void testRequiresNonNullParamAcceptsNonNullArg() {
        assertScript shell, '''
            import groovy.contracts.Requires
            class Foo {
                @Requires({ x != null })
                String bar(x) { x.toString() }
            }
            assert new Foo().bar('hi') == 'hi'
        '''
    }

    @Test
    void testEnsuresResultNonNullRejectsNullReturn() {
        def err = shouldFail shell, '''
            import groovy.contracts.Ensures
            class Foo {
                @Ensures({ result != null })
                String bar() {
                    return null
                }
            }
        '''
        assert err.message.contains("Cannot return null from @NonNull method 'bar'")
    }

    @Test
    void testEnsuresResultNonNullAcceptsNonNullReturn() {
        assertScript shell, '''
            import groovy.contracts.Ensures
            class Foo {
                @Ensures({ result != null })
                String bar() { return 'hi' }
            }
            assert new Foo().bar() == 'hi'
        '''
    }

    @Test
    void testExplicitNonNullWithEnsuresRejectsNullReturn() {
        // Contracts rewrites the return statement (because of @Ensures), which
        // would otherwise hide the literal null from NullChecker. The stashed
        // violation list carries the fact through.
        def err = shouldFail shell, ANNOS + '''
            import groovy.contracts.Ensures
            class Foo {
                @NonNull
                @Ensures({ result.size() >= 0 })
                String bar() {
                    return null
                }
            }
        '''
        assert err.message.contains("Cannot return null from @NonNull method 'bar'")
    }

    @Test
    void testExplicitNonNullWithClassInvariantRejectsNullReturn() {
        def err = shouldFail shell, ANNOS + '''
            import groovy.contracts.Invariant
            @Invariant({ true })
            class Foo {
                @NonNull
                String bar() {
                    return null
                }
            }
        '''
        assert err.message.contains("Cannot return null from @NonNull method 'bar'")
    }

    @Test
    void testRequiresDisjunctionDoesNotInferNonNull() {
        // x != null || y != null does not guarantee either is non-null
        assertScript shell, '''
            import groovy.contracts.Requires
            class Foo {
                @Requires({ x != null || y != null })
                String bar(x, y) { 'ok' }
            }
            assert new Foo().bar(null, 'present') == 'ok'
        '''
    }
}
