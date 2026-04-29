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
 * Captures the joint-compilation surface for {@code @Canonical}, which is
 * defined as
 * {@code @AnnotationCollector(value=[ToString, TupleConstructor, EqualsAndHashCode])}.
 *
 * <p>No new stubber is needed: {@code @AnnotationCollector} aliases are
 * expanded in place by {@code ASTTransformationCollectorCodeVisitor} at
 * CONVERSION (before our CONVERSION-phase invoker fires), so the class
 * node ends up carrying the three constituent annotations and each
 * stubber fires on its own. This test is the proof that the transitive
 * coverage actually works in joint compilation — every constituent's
 * stub contribution is visible to Java consumers.
 */
final class CanonicalJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Person.groovy': '''
                package foo

                @groovy.transform.Canonical
                class Person {
                    String name
                    int age
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    // Each of these call sites depends on a different
                    // @Canonical-composed transform's stubber output.

                    public static Person construct() {
                        // @TupleConstructor stubber: prefix-overload chain.
                        return new Person("Alice", 30);
                    }

                    public static Person constructEmpty() {
                        // Verifier-generated overload from defaults=true.
                        return new Person();
                    }

                    public static String renderInherited(Person p) {
                        // @ToString stubber: declared toString() override.
                        return p.toString();
                    }

                    public static int hashOf(Person p) {
                        // @EqualsAndHashCode stubber: declared hashCode().
                        return p.hashCode();
                    }

                    public static boolean same(Person a, Person b) {
                        // @EqualsAndHashCode stubber: declared equals(Object).
                        return a.equals(b);
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: every constituent's contribution is present.
        String personStub = stubJavaSourceFor('foo.Person')
        // @TupleConstructor: prefix chain Person(), Person(String), Person(String,int).
        assert personStub =~ /public\s+Person\s*\(\s*\)/
        assert personStub =~ /public\s+Person\s*\(\s*java\.lang\.String\s+\w+\s*\)/
        assert personStub =~ /public\s+Person\s*\(\s*java\.lang\.String\s+\w+\s*,\s*int\s+\w+\s*\)/
        // @ToString:
        assert personStub =~ /java\.lang\.String\s+toString\(\s*\)/
        // @EqualsAndHashCode:
        assert personStub =~ /int\s+hashCode\(\s*\)/
        assert personStub =~ /boolean\s+equals\(\s*java\.lang\.Object\s+\w+\s*\)/

        // Runtime view: full transforms have replaced placeholder bodies
        // with the real implementations.
        Class personClass = loader.loadClass('foo.Person')

        def alice1 = personClass.newInstance('Alice', 30)
        def alice2 = personClass.newInstance('Alice', 30)
        def bob = personClass.newInstance('Bob', 25)

        // @ToString
        assert alice1.toString() == 'foo.Person(Alice, 30)'
        // @EqualsAndHashCode
        assert alice1.equals(alice2)
        assert !alice1.equals(bob)
        assert alice1.hashCode() == alice2.hashCode()
        // @TupleConstructor's overload chain
        def empty = personClass.newInstance()
        assert empty.name == null
        assert empty.age == 0
    }
}
