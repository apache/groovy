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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

class NullCheckerTest {

    @Test
    void testIntro() {
        assertScript('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface Nullable {}

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface NonNull {}

        // tag::intro_example[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        int safeLength(@Nullable String text) {
            if (text != null) {
                return text.length()    // ok: null guard
            }
            return -1
        }

        assert safeLength('hello') == 5
        assert safeLength(null) == -1
        // end::intro_example[]
        ''')
    }

    @Test
    void testNullableParameterDereference() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface Nullable {}

        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        static main(args) {
            // tag::nullable_dereference[]
            def greet = { @Nullable String name ->
                name.toUpperCase()                     // potential null dereference
            }
            // end::nullable_dereference[]
        }
        ''')
        def expectedError = '''\
        # tag::nullable_dereference_message[]
        [Static type checking] - Potential null dereference: 'name' is @Nullable
        # end::nullable_dereference_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testNullableParameterSafeNavigation() {
        assertScript('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface Nullable {}

        // tag::safe_navigation[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        String greet(@Nullable String name) {
            name?.toUpperCase()                          // ok: safe navigation
        }

        assert greet('world') == 'WORLD'
        assert greet(null) == null
        // end::safe_navigation[]
        ''')
    }

    @Test
    void testNullGuardNotNull() {
        assertScript('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface Nullable {}

        // tag::null_guard[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        String process(@Nullable String input) {
            if (input != null) {
                return input.toUpperCase()              // ok: null guard
            }
            return 'default'
        }
        // end::null_guard[]
        assert process('hello') == 'HELLO'
        assert process(null) == 'default'
        ''')
    }

    @Test
    void testEarlyReturn() {
        assertScript('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface Nullable {}

        // tag::early_return[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        String process(@Nullable String input) {
            if (input == null) return 'default'        // early exit
            input.toUpperCase()                        // ok: input is non-null here
        }
        // end::early_return[]
        assert process('hello') == 'HELLO'
        assert process(null) == 'default'
        ''')
    }

    @Test
    void testNullPassedToNonNull() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface NonNull {}

        // tag::null_to_nonnull[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class Greeter {
            static String greet(@NonNull String name) {
                name.toUpperCase()
            }
            static void main(String[] args) {
                greet(null)                              // cannot pass null
            }
        }
        // end::null_to_nonnull[]
        ''')
        def expectedError = '''\
        # tag::null_to_nonnull_message[]
        [Static type checking] - Cannot pass null to @NonNull parameter 'name' of 'greet'
        # end::null_to_nonnull_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testNullReturnFromNonNull() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface NonNull {}

        // tag::null_return[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class Greeter {
            @NonNull static String greet() {
                return null                              // cannot return null
            }
        }
        // end::null_return[]
        ''')
        def expectedError = '''\
        # tag::null_return_message[]
        [Static type checking] - Cannot return null from @NonNull method 'greet'
        # end::null_return_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testNonNullFieldAssignNull() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface NonNull {}

        // tag::nonnull_assign[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class Config {
            @NonNull String name
            void clear() {
                name = null                              // cannot assign null
            }
        }
        // end::nonnull_assign[]
        ''')
        def expectedError = '''\
        # tag::nonnull_assign_message[]
        [Static type checking] - Cannot assign null to @NonNull variable 'name'
        # end::nonnull_assign_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testNullableMethodReturnDereference() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface Nullable {}

        // tag::nullable_return_deref[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class UserService {
            @Nullable static String findUser() { null }
            static void main(String[] args) {
                findUser().toUpperCase()                  // may return null
            }
        }
        // end::nullable_return_deref[]
        ''')
        def expectedError = '''\
        # tag::nullable_return_deref_message[]
        [Static type checking] - Potential null dereference: 'findUser()' may return null
        # end::nullable_return_deref_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testMonotonicNonNull() {
        assertScript('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface MonotonicNonNull {}

        // tag::monotonic_nonnull[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class LazyService {
            @MonotonicNonNull String cachedValue

            String getValue() {
                if (cachedValue != null) {
                    return cachedValue.toUpperCase()    // ok: null guard
                }
                cachedValue = 'computed'
                return cachedValue.toUpperCase()        // ok: just assigned non-null
            }
        }
        // end::monotonic_nonnull[]
        assert new LazyService().getValue() == 'COMPUTED'
        ''')
    }

    @Test
    void testMonotonicNonNullReassignNull() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target([ElementType.FIELD])
        @Retention(RetentionPolicy.RUNTIME)
        @interface MonotonicNonNull {}

        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class Service {
            @MonotonicNonNull String value
            // tag::monotonic_reassign[]
            void reset() {
                value = "hello"
                value = null                             // cannot re-null
            }
            // end::monotonic_reassign[]
        }
        ''')
        def expectedError = '''\
        # tag::monotonic_reassign_message[]
        [Static type checking] - Cannot assign null to @MonotonicNonNull variable 'value' after non-null assignment
        # end::monotonic_reassign_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testNonNullByDefault() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import java.lang.annotation.*

        @Target(ElementType.TYPE)
        @Retention(RetentionPolicy.RUNTIME)
        @interface NonNullByDefault {}

        // tag::nonnull_by_default[]
        @NonNullByDefault
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class UserService {
            String name

            static String greet(String name) {
                "Hello, $name!"
            }

            static void main(String[] args) {
                greet(null)                              // compile error: params are @NonNull
            }
        }
        // end::nonnull_by_default[]
        ''')
        def expectedError = '''\
        # tag::nonnull_by_default_message[]
        [Static type checking] - Cannot pass null to @NonNull parameter 'name' of 'greet'
        # end::nonnull_by_default_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testNullCheckIntegration() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import groovy.transform.NullCheck

        // tag::nullcheck_integration[]
        @NullCheck
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class Greeter {
            static String greet(String name) {
                "Hello, $name!"
            }
            static void main(String[] args) {
                greet(null)                              // caught at compile time
            }
        }
        // end::nullcheck_integration[]
        ''')
        def expectedError = '''\
        # tag::nullcheck_integration_message[]
        [Static type checking] - Cannot pass null to @NonNull parameter 'name' of 'greet'
        # end::nullcheck_integration_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testFlowSensitive() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked

        @TypeChecked(extensions='groovy.typecheckers.NullChecker(strict: true)')
        static main(args) {
            // tag::flow_sensitive[]
            def x = null
            x.toString()                             // x may be null
            // end::flow_sensitive[]
        }
        ''')
        def expectedError = '''\
        # tag::flow_sensitive_message[]
        [Static type checking] - Potential null dereference: 'x' may be null
        # end::flow_sensitive_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testFlowSensitiveReassign() {
        assertScript('''
        import groovy.transform.TypeChecked

        // tag::flow_reassign[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker(strict: true)')
        static main(args) {
            def x = null
            x = 'hello'
            assert x.toString() == 'hello'           // ok: reassigned non-null
        }
        // end::flow_reassign[]
        ''')
    }

    @Test
    void testRequiresIntegration() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import groovy.contracts.Requires

        // tag::requires_integration[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class Greeter {
            @Requires({ name != null })
            static String greet(name) { "Hello, $name!" }

            static void main(String[] args) {
                greet(null)                              // caught at compile time
            }
        }
        // end::requires_integration[]
        ''')
        def expectedError = '''\
        # tag::requires_integration_message[]
        [Static type checking] - Cannot pass null to @NonNull parameter 'name' of 'greet'
        # end::requires_integration_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

    @Test
    void testEnsuresIntegration() {
        def err = shouldFail('''
        import groovy.transform.TypeChecked
        import groovy.contracts.Ensures

        // tag::ensures_integration[]
        @TypeChecked(extensions='groovy.typecheckers.NullChecker')
        class Greeter {
            @Ensures({ result != null })
            String greet() {
                return null                              // caught at compile time
            }
        }
        // end::ensures_integration[]
        ''')
        def expectedError = '''\
        # tag::ensures_integration_message[]
        [Static type checking] - Cannot return null from @NonNull method 'greet'
        # end::ensures_integration_message[]
        '''
        assert err.message.contains(expectedError.readLines()[1].trim())
    }

}
