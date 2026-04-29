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
 * Captures the joint-compilation surface for {@code @Builder} with
 * {@link groovy.transform.builder.DefaultStrategy} (the default) and
 * {@link groovy.transform.builder.SimpleStrategy} with a non-default
 * prefix.
 *
 * <p>For DefaultStrategy: the stubber emits the inner {@code FooBuilder}
 * class with placeholder fluent setters and a {@code build()} method,
 * plus a static {@code Foo.builder()} factory on the buildee.
 *
 * <p>For SimpleStrategy with a non-default {@code prefix}: the stubber
 * emits chained {@code Foo prefixName(T value)} setters on the buildee
 * itself. The default {@code prefix = "set"} is intentionally not stubbed
 * because the chained {@code Foo setX(value)} would collide with the void
 * {@code setX(value)} auto-generated for the property.
 *
 * <p>Other strategies (Initializer/External), method-level use,
 * {@code forClass}, and {@code includeSuperProperties} are deliberately
 * out of scope for this spike pass.
 */
final class BuilderJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Person.groovy': '''
                package foo

                @groovy.transform.builder.Builder
                class Person {
                    String name
                    int age
                }
            ''',
            'foo/Box.groovy': '''
                package foo

                import groovy.transform.builder.Builder
                import groovy.transform.builder.SimpleStrategy

                @Builder(builderStrategy = SimpleStrategy, prefix = "with")
                class Box {
                    String label
                    int count
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static Person make() {
                        // DefaultStrategy chained against the inner builder.
                        return Person.builder().name("Alice").age(30).build();
                    }
                    public static Box configure() {
                        // SimpleStrategy chained on Box itself with "with" prefix.
                        return new Box().withLabel("alpha").withCount(7);
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // === DefaultStrategy ===
        // The stub generator emits inner classes inside the outer class's
        // stub file, so all assertions hit the Person stub.
        String personStub = stubJavaSourceFor('foo.Person')

        // Static factory on Person.
        assert personStub =~ /public\s+static\s+foo\.Person\.PersonBuilder\s+builder\(\s*\)/

        // Inner builder class declared on Person, with fluent setters and build().
        assert personStub.contains('PersonBuilder')
        assert personStub =~ /foo\.Person\.PersonBuilder\s+name\(\s*java\.lang\.String\s+\w+\s*\)/
        assert personStub =~ /foo\.Person\.PersonBuilder\s+age\(\s*int\s+\w+\s*\)/
        assert personStub =~ /foo\.Person\s+build\(\s*\)/

        // Runtime view: real bodies installed; chained construction works.
        Class personClass = loader.loadClass('foo.Person')
        def builder = personClass.getMethod('builder').invoke(null)
        builder.name('Alice').age(30)
        def person = builder.build()
        assert person.name == 'Alice'
        assert person.age == 30

        // === SimpleStrategy with prefix = "with" ===
        String boxStub = stubJavaSourceFor('foo.Box')

        // Chained setters on Box itself, returning Box.
        assert boxStub =~ /foo\.Box\s+withLabel\(\s*java\.lang\.String\s+\w+\s*\)/
        assert boxStub =~ /foo\.Box\s+withCount\(\s*int\s+\w+\s*\)/

        // Runtime view: chained setters work and return the same instance.
        Class boxClass = loader.loadClass('foo.Box')
        def box = boxClass.newInstance()
        def chained = box.withLabel('alpha').withCount(7)
        assert chained.is(box)
        assert box.label == 'alpha'
        assert box.count == 7
    }
}
