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
 * Captures the joint-compilation surface for {@code @AutoClone}.
 *
 * <p>Without the stubber, Java code calling {@code foo.clone()} fails to
 * compile because {@code Object.clone()} is {@code protected}. The
 * stubber adds the {@link Cloneable} interface to the class header and
 * emits a public covariant {@code clone()} placeholder so Java consumers
 * can call it. The full transform replaces the body at CANONICALIZATION
 * with whichever implementation the chosen {@code style} produces.
 *
 * <p>The Java source in this test demands the public covariant
 * {@code Box clone()} surface (without an explicit cast from
 * {@code Object}), which only compiles when the stub exposes the
 * override.
 */
final class AutoCloneJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Box.groovy': '''
                package foo

                @groovy.transform.AutoClone
                class Box {
                    String label
                    int count
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    // Calls the covariant Box clone() — only compiles if the
                    // stub declares the public override (Object.clone() is
                    // protected and would not be reachable from this caller).
                    public static Box duplicate(Box b) throws CloneNotSupportedException {
                        return b.clone();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: Cloneable on the implements clause; public covariant
        // clone() with throws CloneNotSupportedException.
        String stub = stubJavaSourceFor('foo.Box')
        assert stub.contains('java.lang.Cloneable')
        assert stub =~ /public\s+foo\.Box\s+clone\(\s*\)\s*throws\s+java\.lang\.CloneNotSupportedException/

        // Runtime view: clone returns a deep-equivalent Box with same fields.
        Class boxClass = loader.loadClass('foo.Box')
        assert Cloneable.isAssignableFrom(boxClass)

        def original = boxClass.newInstance(label: 'first', count: 7)
        def copy = original.clone()
        assert !copy.is(original)
        assert copy.label == 'first'
        assert copy.count == 7
    }
}
