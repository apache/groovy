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
 * Captures the joint-compilation surface for {@code @AutoImplement}.
 *
 * <p>The stubber walks the same supertype/interface graph as the full
 * transform, but at CONVERSION. Coverage is wider than initially expected:
 *
 * <ul>
 *   <li><b>Classpath interfaces</b> — fully resolved at CONVERSION;
 *       abstract methods get placeholder implementations in the stub.</li>
 *   <li><b>Same-unit Groovy abstract classes</b> — source-declared
 *       abstracts are visible at CONVERSION; same coverage as classpath.</li>
 *   <li><b>Same-unit Groovy traits with source-declared abstract methods</b>
 *       — also covered. {@code TraitASTTransformation} runs at
 *       CANONICALIZATION, but the trait's source-declared {@code abstract}
 *       methods exist on the trait's {@link ClassNode} from CONVERSION
 *       onward. The stubber's walk reaches the trait via the implementing
 *       class's {@code getInterfaces()} (Groovy puts traits in the
 *       implements list even though they aren't yet rewritten as
 *       interfaces) and stubs the abstract method.</li>
 * </ul>
 *
 * <p>The remaining boundary is narrow: an abstract method that another
 * transform contributes via {@code cNode.addMethod(...)} at SEMANTIC_ANALYSIS
 * or later, on a same-unit Groovy supertype/interface. Such a method isn't
 * present at CONVERSION and won't be stubbed. We don't have a real-world
 * transform that does this today, so the documented gap is theoretical
 * rather than something users will hit.
 */
final class AutoImplementJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            // Classpath interface: Iterator<String>.hasNext / next / remove are
            // all visible at CONVERSION.
            'foo/Empty.groovy': '''
                package foo

                @groovy.transform.AutoImplement(exception = UnsupportedOperationException)
                class Empty implements Iterator<String> {
                    boolean hasNext() { false }
                }
            ''',
            // Same-unit Groovy abstract class with source-declared abstract.
            'foo/Shape.groovy': '''
                package foo
                abstract class Shape {
                    abstract double area()
                }
            ''',
            'foo/Blank.groovy': '''
                package foo

                @groovy.transform.AutoImplement
                class Blank extends Shape {
                }
            ''',
            // Same-unit Groovy trait with a source-declared abstract method.
            // TraitASTTransformation runs at CANONICALIZATION, but the trait's
            // source-declared abstract is on the trait's ClassNode from
            // CONVERSION onward, so the stubber's walk reaches it via
            // Painter.getInterfaces().
            'foo/Renderable.groovy': '''
                package foo
                trait Renderable {
                    abstract String render()
                }
            ''',
            'foo/Painter.groovy': '''
                package foo

                @groovy.transform.AutoImplement
                class Painter implements Renderable {
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    // Classpath-interface abstract — visible on the stub.
                    public static boolean emptyHasNext() {
                        return new Empty().hasNext();
                    }

                    // Same-unit Groovy abstract class abstract — visible on the stub.
                    public static double blankArea() {
                        return new Blank().area();
                    }

                    // Same-unit trait abstract — visible on the stub via
                    // Painter.getInterfaces() reaching the trait's ClassNode.
                    public static String painterRender() {
                        return new Painter().render();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // === Classpath interface ===
        String emptyStub = stubJavaSourceFor('foo.Empty')
        // The user-declared concrete hasNext stays as-is.
        assert emptyStub =~ /public\s+boolean\s+hasNext\(\s*\)/
        // Iterator<String>.next() is abstract → gets stubbed with the
        // erased return type. (Iterator.remove() has a JDK 8+ default
        // implementation, so it's not abstract and not stubbed —
        // matching the full transform's runtime behavior.)
        assert emptyStub =~ /public\s+java\.lang\.String\s+next\(\s*\)/

        Class emptyClass = loader.loadClass('foo.Empty')
        def empty = emptyClass.newInstance()
        assert !empty.hasNext()
        try {
            empty.next()
            assert false, 'expected UnsupportedOperationException from runtime'
        } catch (UnsupportedOperationException expected) {
        }

        // === Same-unit Groovy abstract class ===
        String blankStub = stubJavaSourceFor('foo.Blank')
        assert blankStub =~ /public\s+double\s+area\(\s*\)/

        Class blankClass = loader.loadClass('foo.Blank')
        // Default mode: returns 0.0d for primitive double.
        assert blankClass.newInstance().area() == 0.0d

        // === Same-unit trait abstract ===
        // Empirically: the trait's source-declared abstract IS visible at
        // CONVERSION via Painter.getInterfaces(), so it gets stubbed.
        // (TraitASTTransformation rewrites the trait at CANONICALIZATION,
        // but the abstract method exists on its ClassNode from CONVERSION on.)
        String painterStub = stubJavaSourceFor('foo.Painter')
        assert painterStub =~ /public\s+java\.lang\.String\s+render\(\s*\)/

        // Runtime: full transform installs the real default-value body.
        Class painterClass = loader.loadClass('foo.Painter')
        assert painterClass.newInstance().render() == null
    }
}
