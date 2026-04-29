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
 * Captures the joint-compilation surface for {@code @InheritConstructors}.
 *
 * <p>Five scenarios exercise the boundaries:
 *
 * <ol>
 *   <li><b>Classpath super</b> ({@code RuntimeException}) — fully covered.
 *       Java callers can use any inherited constructor from the stub.</li>
 *   <li><b>Same-unit Groovy super with hand-written constructors</b> —
 *       fully covered. Source-declared constructors are visible at
 *       CONVERSION regardless of class iteration order.</li>
 *   <li><b>Chained {@code @InheritConstructors}</b> (Sub → Mid → Base) —
 *       fully covered. The stubber mirrors the runtime's recursive
 *       processing pattern, so transitive constructors flatten into the
 *       leaf subclass.</li>
 *   <li><b>Same-unit super with {@code @TupleConstructor}, super-first</b>
 *       — fully covered. Super's tuple stubber runs before sub's inherit
 *       stubber, so the tuple constructor is visible to be copied.</li>
 *   <li><b>Same-unit super with {@code @TupleConstructor}, sub-first</b>
 *       — boundary case. When the sub class node is processed before the
 *       super by the CONVERSION phase dispatcher, sub's stubber sees only
 *       super's source-declared constructors, not the tuple constructor.
 *       The runtime still implements the full surface; the stub is a
 *       strict subset.</li>
 * </ol>
 */
final class InheritConstructorsJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            // === Scenario 1: classpath super (java.lang.RuntimeException) ===
            'foo/MyEx.groovy': '''
                package foo

                @groovy.transform.InheritConstructors
                class MyEx extends RuntimeException {
                }
            ''',

            // === Scenario 2: same-unit Groovy super with hand-written ctors ===
            'foo/Person.groovy': '''
                package foo

                class Person {
                    String first, last
                    Person(String first, String last) {
                        this.first = first
                        this.last = last?.toUpperCase()
                    }
                }
            ''',
            'foo/PersonAge.groovy': '''
                package foo

                @groovy.transform.InheritConstructors
                class PersonAge extends Person {
                    int age
                }
            ''',

            // === Scenario 3: chained @InheritConstructors (Leaf → Mid → Base) ===
            'foo/Base.groovy': '''
                package foo

                class Base {
                    final String tag
                    Base(String tag) { this.tag = tag }
                }
            ''',
            'foo/Mid.groovy': '''
                package foo

                @groovy.transform.InheritConstructors
                class Mid extends Base {
                }
            ''',
            'foo/Leaf.groovy': '''
                package foo

                @groovy.transform.InheritConstructors
                class Leaf extends Mid {
                }
            ''',

            // === Scenario 4: same-unit super with @TupleConstructor, super first ===
            // Box and TaggedBox in one source file; Box declared first so its
            // tuple stubber runs before TaggedBox's inherit stubber.
            'foo/Boxes.groovy': '''
                package foo

                @groovy.transform.TupleConstructor
                class Box {
                    String label
                    int count
                }

                @groovy.transform.InheritConstructors
                class TaggedBox extends Box {
                }
            ''',

            // === Scenario 5: same-unit super with @TupleConstructor, sub first ===
            // Reverse declaration order in a single source file.
            'foo/ReversedBoxes.groovy': '''
                package foo

                @groovy.transform.InheritConstructors
                class TaggedReversed extends BoxReversed {
                }

                @groovy.transform.TupleConstructor
                class BoxReversed {
                    String label
                    int count
                }
            ''',

            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    // Scenario 1: classpath super.
                    public static MyEx makeNoArg()       { return new MyEx(); }
                    public static MyEx makeMessage()     { return new MyEx("boom"); }
                    public static MyEx makeWithCause()   { return new MyEx(new RuntimeException("inner")); }
                    public static MyEx makeBoth()        { return new MyEx("boom", new RuntimeException("inner")); }

                    // Scenario 2: same-unit hand-written super.
                    public static PersonAge makePerson() { return new PersonAge("Liam", "Smith"); }

                    // Scenario 3: chained @InheritConstructors.
                    public static Leaf makeLeaf()        { return new Leaf("alpha"); }
                    public static Mid  makeMid()         { return new Mid("beta"); }

                    // Scenario 4: super-first with @TupleConstructor.
                    public static TaggedBox makeTagged() { return new TaggedBox("hello", 7); }
                    public static TaggedBox makeEmpty()  { return new TaggedBox(); }

                    // Scenario 5 NOTE: we deliberately do NOT call
                    // new TaggedReversed("hello", 7) from Java — the boundary
                    // is that this signature may not appear in the stub. We
                    // exercise the runtime-only behavior in verifyStubs().
                    public static TaggedReversed buildReversed() {
                        return new TaggedReversed();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // === Scenario 1: classpath super ===
        String myExStub = stubJavaSourceFor('foo.MyEx')
        // RuntimeException's four public ctors should each appear.
        assert myExStub =~ /public\s+MyEx\s*\(\s*\)/
        assert myExStub =~ /public\s+MyEx\s*\(\s*java\.lang\.String\s+\w+\s*\)/
        assert myExStub =~ /public\s+MyEx\s*\(\s*java\.lang\.String\s+\w+\s*,\s*java\.lang\.Throwable\s+\w+\s*\)/
        assert myExStub =~ /public\s+MyEx\s*\(\s*java\.lang\.Throwable\s+\w+\s*\)/

        Class myExClass = loader.loadClass('foo.MyEx')
        assert myExClass.newInstance().message == null
        assert myExClass.getConstructor(String).newInstance('boom').message == 'boom'

        // === Scenario 2: same-unit hand-written super ===
        String personAgeStub = stubJavaSourceFor('foo.PersonAge')
        assert personAgeStub =~ /public\s+PersonAge\s*\(\s*java\.lang\.String\s+\w+\s*,\s*java\.lang\.String\s+\w+\s*\)/

        Class personAgeClass = loader.loadClass('foo.PersonAge')
        def p = personAgeClass.newInstance('Liam', 'Smith')
        assert p.first == 'Liam'
        assert p.last == 'SMITH'

        // === Scenario 3: chained @InheritConstructors ===
        String leafStub = stubJavaSourceFor('foo.Leaf')
        // Leaf must expose Base's String constructor, transitively through Mid.
        assert leafStub =~ /public\s+Leaf\s*\(\s*java\.lang\.String\s+\w+\s*\)/

        String midStub = stubJavaSourceFor('foo.Mid')
        assert midStub =~ /public\s+Mid\s*\(\s*java\.lang\.String\s+\w+\s*\)/

        Class leafClass = loader.loadClass('foo.Leaf')
        assert leafClass.newInstance('alpha').tag == 'alpha'

        // === Scenario 4: same-unit super with @TupleConstructor, super-first ===
        String taggedStub = stubJavaSourceFor('foo.TaggedBox')
        // TaggedBox should expose Box's tuple-constructor surface.
        // (The stubbed prefix-overload chain comes from TupleConstructor's
        // stubber; @InheritConstructors copies what's there.)
        assert taggedStub =~ /public\s+TaggedBox\s*\(\s*java\.lang\.String\s+\w+\s*,\s*int\s+\w+\s*\)/

        Class taggedClass = loader.loadClass('foo.TaggedBox')
        def tagged = taggedClass.newInstance('hello', 7)
        assert tagged.label == 'hello'
        assert tagged.count == 7

        // === Scenario 5: same-unit super with @TupleConstructor, sub-first ===
        // The runtime still produces the full surface. Verify that:
        Class reversedClass = loader.loadClass('foo.TaggedReversed')
        def reversed = reversedClass.newInstance('alpha', 42)
        assert reversed.label == 'alpha'
        assert reversed.count == 42

        // The stub view depends on within-phase ordering. Whether the
        // tuple-derived signature appears here is implementation-detail;
        // what matters for the GEP-21 invariant is that the stub remains
        // a SUBSET of the runtime — never a divergent surface. Anything
        // the stub claims, the runtime supports.
        // Empirically: when sub is declared before super in the same source
        // file, sub's @InheritConstructors stubber runs at CONVERSION before
        // super's @TupleConstructor stubber, so the tuple-derived signature
        // is invisible at copy time. Lock this in — if it changes, we want
        // to see it (could be a framework reordering or a topological-sort
        // upgrade).
        String reversedStub = stubJavaSourceFor('foo.TaggedReversed')
        assert !(reversedStub =~ /public\s+TaggedReversed\s*\(\s*java\.lang\.String\s+\w+\s*,\s*int\s+\w+\s*\)/),
                "boundary moved: sub-first ordering now covers super's tuple ctor in the stub. Update GEP-21 if intentional. Stub:\n${reversedStub}"
        // Subset-of-runtime invariant: anything the stub claims, the runtime
        // supports. Verify the stub-claimed no-arg-ish signature works.
        // (No tuple ctor on stub means Java code can only reach the implicit
        // default constructor, which the test exercises via JavaUser.buildReversed.)
        assert reversedClass.newInstance() != null
    }
}
