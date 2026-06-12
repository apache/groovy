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
 * Captures the joint-compilation surface for {@code @MapConstructor}.
 *
 * <p>GEP-21 learning: {@code @MapConstructor}'s signature is invariant —
 * always {@code Foo(Map)} regardless of property visibility, super-class
 * inclusion flags, or include/exclude lists. The stubber's view at
 * CONVERSION is therefore identical to the runtime, so there is no
 * stub/runtime divergence to manage. Adding a stubber piece is purely
 * about exposing the constructor to Java callers in joint compilation;
 * the stub-fidelity story is the simplest of all the spike's targets.
 *
 * <p>The {@code noArg = true} variant is also covered: when set, the
 * full transform emits a no-arg constructor in addition to the
 * {@code Foo(Map)} form, and the stubber mirrors that.
 */
final class MapConstructorJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Person.groovy': '''
                package foo

                @groovy.transform.MapConstructor
                class Person {
                    String name
                    int age
                }
            ''',
            'foo/Pair.groovy': '''
                package foo

                // noArg=true asks the full transform to ALSO emit a no-arg
                // constructor; the stubber mirrors that.
                @groovy.transform.MapConstructor(noArg = true)
                class Pair {
                    String left
                    String right
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.util.Map;

                public class JavaUser {
                    public static Person personFromMap(Map<String, Object> args) {
                        return new Person(args);
                    }
                    public static Pair pairFromMap(Map<String, Object> args) {
                        return new Pair(args);
                    }
                    public static Pair emptyPair() {
                        // noArg=true makes Pair() callable from Java as well.
                        return new Pair();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: Foo(Map) declared on Person.
        String personStub = stubJavaSourceFor('foo.Person')
        assert personStub =~ /public\s+Person\s*\(\s*java\.util\.Map\s+\w+\s*\)/

        // Stub view: Pair has both Foo(Map) and Foo() because noArg=true.
        String pairStub = stubJavaSourceFor('foo.Pair')
        assert pairStub =~ /public\s+Pair\s*\(\s*java\.util\.Map\s+\w+\s*\)/
        assert pairStub =~ /public\s+Pair\s*\(\s*\)/

        // Runtime view: stubber-tagged constructors were discarded; runtime
        // exposes only what the full transform produces.
        Class personClass = loader.loadClass('foo.Person')
        def personSigs = personClass.declaredConstructors.collect {
            it.parameterTypes*.simpleName
        }.toSet()
        assert personSigs == [['Map']].toSet()

        Class pairClass = loader.loadClass('foo.Pair')
        def pairSigs = pairClass.declaredConstructors.collect {
            it.parameterTypes*.simpleName
        }.toSet()
        assert pairSigs == [['Map'], []].toSet()

        // Runtime semantics: map-style instantiation populates fields.
        def person = personClass.newInstance(name: 'Alice', age: 30)
        assert person.name == 'Alice'
        assert person.age == 30

        // No-arg works at runtime for Pair.
        def empty = pairClass.newInstance()
        assert empty.left == null
        assert empty.right == null
    }
}
