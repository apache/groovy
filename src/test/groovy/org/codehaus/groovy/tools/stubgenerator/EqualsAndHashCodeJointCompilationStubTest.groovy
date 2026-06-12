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
 * Captures the joint-compilation surface for {@code @EqualsAndHashCode}.
 *
 * <p>Same shape as the {@code @ToString} spike: simple Java call sites
 * compile either way (Object-inherited dispatch covers {@code equals} and
 * {@code hashCode}), but exposing the {@code @EqualsAndHashCode}-generated
 * overrides as DECLARED on the stub matters for Java code that does
 * {@code super.equals(...)} chaining or for tooling that distinguishes
 * declared from inherited methods. The stubber emits placeholders; the
 * full transform replaces their bodies via the metadata-key handoff.
 */
final class EqualsAndHashCodeJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Point.groovy': '''
                package foo

                @groovy.transform.EqualsAndHashCode
                class Point {
                    int x
                    int y
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser extends Point {
                    public JavaUser(int x, int y) {
                        setX(x);
                        setY(y);
                    }

                    @Override
                    public boolean equals(Object other) {
                        // Java code chaining super.equals() — the stub must
                        // expose Point.equals as a declared override (not
                        // just Object.equals) for tooling/IDEs to pick it up.
                        return super.equals(other) && getClass() == other.getClass();
                    }

                    @Override
                    public int hashCode() {
                        // Same with super.hashCode().
                        return super.hashCode() * 31 + 1;
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: Point declares equals(Object) and hashCode(), not just
        // inherits from Object.
        String pointStub = stubJavaSourceFor('foo.Point')
        assert pointStub =~ /int\s+hashCode\(\s*\)/
        assert pointStub =~ /boolean\s+equals\(\s*java\.lang\.Object\s+\w+\s*\)/

        // Runtime view: placeholder bodies have been replaced by the
        // @EqualsAndHashCode-generated bodies.
        Class pointClass = loader.loadClass('foo.Point')
        def p1 = pointClass.newInstance(x: 1, y: 2)
        def p2 = pointClass.newInstance(x: 1, y: 2)
        def p3 = pointClass.newInstance(x: 1, y: 3)
        assert p1.equals(p2)
        assert !p1.equals(p3)
        assert p1.hashCode() == p2.hashCode()

        // The Java subclass's super.equals/super.hashCode chain through to
        // the runtime bodies (not the stub placeholders).
        Class javaUser = loader.loadClass('foo.JavaUser')
        def u1 = javaUser.newInstance(1, 2)
        def u2 = javaUser.newInstance(1, 2)
        def u3 = javaUser.newInstance(1, 3)
        assert u1.equals(u2)
        assert !u1.equals(u3)
        assert u1.hashCode() == u2.hashCode()
    }
}
