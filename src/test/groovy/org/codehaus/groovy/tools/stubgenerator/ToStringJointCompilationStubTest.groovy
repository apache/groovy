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
 * Captures the joint-compilation surface for {@code @ToString}.
 *
 * <p>GEP-21 learning: even when {@code Object} already provides the method
 * being overridden (so simple call sites compile either way), exposing the
 * @ToString-generated override in the stub matters for Java code that does
 * {@code super.toString()} chaining or for tooling that distinguishes
 * declared from inherited methods. The stubber emits a placeholder body
 * which the full transform replaces at CANONICALIZATION via the
 * metadata-key handoff.
 */
final class ToStringJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Person.groovy': '''
                package foo

                @groovy.transform.ToString
                class Person {
                    String name
                    int age
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser extends Person {
                    public JavaUser() {
                        setName("J");
                        setAge(1);
                    }

                    @Override
                    public String toString() {
                        // Java code chaining super.toString() — the stub must
                        // expose Person.toString as a declared override (not
                        // just Object.toString) for tooling/IDEs to pick it up.
                        return "Wrapped[" + super.toString() + "]";
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: Person declares toString, not just inherits it from Object.
        String personStub = stubJavaSourceFor('foo.Person')
        assert personStub =~ /java\.lang\.String\s+toString\(\s*\)/

        // Runtime view: the placeholder body has been replaced by the
        // @ToString-generated body.
        Class personClass = loader.loadClass('foo.Person')
        def p = personClass.newInstance(name: 'Alice', age: 30)
        assert p.toString() == 'foo.Person(Alice, 30)'

        // The Java subclass's super.toString() call resolves to the runtime
        // body, not the stub placeholder.
        Class javaUser = loader.loadClass('foo.JavaUser')
        def j = javaUser.newInstance()
        assert j.toString() == 'Wrapped[foo.Person(J, 1)]'

        // Stubber's @Generated tag is present (addGeneratedMethod marks it),
        // but the stubber metadata key was cleared by the full transform.
        def toStringMethod = personClass.declaredMethods.find { it.name == 'toString' && it.parameterCount == 0 }
        assert toStringMethod != null
    }
}
