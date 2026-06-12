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
package org.codehaus.groovy.tools.stubgenerator

/**
 * Captures the joint-compilation surface for {@code @Singleton}.
 *
 * <p>The stubber emits a placeholder {@code getInstance()} (or
 * {@code getXxx()} for a custom property name); the full transform
 * replaces the body via the metadata-key handoff. Java consumers can call
 * {@code MyClass.getInstance()} from the stub as expected.
 *
 * <p>The corresponding singleton field is intentionally not in the stub —
 * see {@code SingletonASTStubber} for rationale.
 */
final class SingletonJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Eager.groovy': '''
                package foo

                @groovy.lang.Singleton
                class Eager {
                    String greet() { 'hi' }
                }
            ''',
            'foo/Lazy.groovy': '''
                package foo

                @groovy.lang.Singleton(lazy = true, strict = false)
                class Lazy {
                    String greet() { 'lazy hi' }
                }
            ''',
            'foo/CustomName.groovy': '''
                package foo

                @groovy.lang.Singleton(property = 'self', strict = false)
                class CustomName {
                    String greet() { 'self hi' }
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static String greetEager()  { return Eager.getInstance().greet(); }
                    public static String greetLazy()   { return Lazy.getInstance().greet(); }
                    public static String greetCustom() { return CustomName.getSelf().greet(); }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: getInstance / getSelf appear as static accessors.
        String eagerStub = stubJavaSourceFor('foo.Eager')
        assert eagerStub =~ /public\s+static\s+foo\.Eager\s+getInstance\(\s*\)/

        String lazyStub = stubJavaSourceFor('foo.Lazy')
        assert lazyStub =~ /public\s+static\s+foo\.Lazy\s+getInstance\(\s*\)/

        String customStub = stubJavaSourceFor('foo.CustomName')
        assert customStub =~ /public\s+static\s+foo\.CustomName\s+getSelf\(\s*\)/

        // Runtime view: stubber-tagged method has had its body replaced by
        // the full transform's real implementation.
        Class eager = loader.loadClass('foo.Eager')
        def eagerInstance = eager.getMethod('getInstance').invoke(null)
        assert eagerInstance.greet() == 'hi'
        // Singleton instance is stable across calls.
        assert eager.getMethod('getInstance').invoke(null).is(eagerInstance)

        Class lazy = loader.loadClass('foo.Lazy')
        def lazyInstance = lazy.getMethod('getInstance').invoke(null)
        assert lazyInstance.greet() == 'lazy hi'
        assert lazy.getMethod('getInstance').invoke(null).is(lazyInstance)

        Class custom = loader.loadClass('foo.CustomName')
        def customInstance = custom.getMethod('getSelf').invoke(null)
        assert customInstance.greet() == 'self hi'
    }
}
