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
 * Joint-compilation surface for {@code @RecordType} in emulated mode
 * (used when targeting {@code < JDK16} or with explicit
 * {@code @RecordOptions(mode = RecordTypeMode.EMULATE)}).
 *
 * <p>Native records are covered by the stub-side back-channel
 * (GROOVY-11974) — this test exercises the Shape C
 * {@code RecordBaseASTStubber}, which:
 *
 * <ul>
 *   <li>flips property modifiers and getter names so the stub generator's
 *       {@code Verifier} sub-pass emits {@code componentName()} accessors
 *       instead of {@code getComponentName()} and skips setter
 *       synthesis;</li>
 *   <li>emits stub placeholders for the Groovy-specific record convenience
 *       methods — {@code getAt(int)}, {@code toList()}, {@code toMap()},
 *       {@code size()} (default-on) and {@code copyWith(Map)},
 *       {@code components()} (opt-in) — driven by the same
 *       {@code shouldAdd*} predicates the runtime transform uses, so the
 *       stub is a strict subset of the runtime surface.</li>
 * </ul>
 *
 * <p>The canonical constructor comes from the existing
 * {@code @TupleConstructor} stubber that {@code @RecordType}'s
 * {@code @AnnotationCollector} pulls in.
 */
final class RecordTypeEmulatedJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            // Point: defaults — getAt/toList/toMap/size on; copyWith/components off.
            'foo/Point.groovy': '''
                package foo

                @groovy.transform.RecordType
                @groovy.transform.RecordOptions(mode = groovy.transform.RecordTypeMode.EMULATE)
                class Point {
                    int x
                    int y
                }
            ''',
            // Range: opt-in copyWith + components, plus size=false to lock in
            // that the stubber respects per-attribute disables.
            'foo/Range.groovy': '''
                package foo

                @groovy.transform.RecordType
                @groovy.transform.RecordOptions(
                        mode = groovy.transform.RecordTypeMode.EMULATE,
                        copyWith = true,
                        components = true,
                        size = false)
                class Range {
                    int lo
                    int hi
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.util.List;
                import java.util.Map;
                import java.util.HashMap;
                import groovy.lang.Tuple2;

                public class JavaUser {
                    // === Point: defaults ===
                    public static int sumComponents(Point p) {
                        return p.x() + p.y();
                    }
                    public static Point build(int x, int y) {
                        return new Point(x, y);
                    }
                    public static Object pointGetAt(Point p, int i) {
                        return p.getAt(i);
                    }
                    public static List pointToList(Point p) {
                        return p.toList();
                    }
                    public static Map pointToMap(Point p) {
                        return p.toMap();
                    }
                    public static int pointSize(Point p) {
                        return p.size();
                    }

                    // === Range: opt-in copyWith / components, size disabled ===
                    public static Range rangeCopyHi(Range r, int newHi) {
                        Map<String, Object> args = new HashMap<>();
                        args.put("hi", newHi);
                        return r.copyWith(args);
                    }
                    public static Tuple2<Integer, Integer> rangeComponents(Range r) {
                        return r.components();
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // === Point: header + accessors + defaults ===
        String pointStub = stubJavaSourceFor('foo.Point')

        // Header: emulated → plain final class, never `record`.
        assert !(pointStub =~ /\brecord\s+Point\b/),
                "emulated Point should not use record syntax in stub:\n${pointStub}"
        assert pointStub =~ /\bclass\s+Point\b/

        // Component accessors x()/y(); no bean-style getters/setters.
        assert pointStub =~ /public\s+int\s+x\s*\(\s*\)/
        assert pointStub =~ /public\s+int\s+y\s*\(\s*\)/
        assert !(pointStub =~ /\bgetX\s*\(/), "stub leaked getX():\n${pointStub}"
        assert !(pointStub =~ /\bgetY\s*\(/), "stub leaked getY():\n${pointStub}"
        assert !(pointStub =~ /\bsetX\s*\(/), "stub leaked setX():\n${pointStub}"
        assert !(pointStub =~ /\bsetY\s*\(/), "stub leaked setY():\n${pointStub}"

        // Canonical constructor (from @TupleConstructor stubber transitively).
        assert pointStub =~ /public\s+Point\s*\(\s*int\s+\w+\s*,\s*int\s+\w+\s*\)/

        // Default-on convenience methods.
        assert pointStub =~ /public\s+(?:final\s+)?java\.lang\.Object\s+getAt\s*\(\s*int\s+\w+\s*\)/
        assert pointStub =~ /public\s+(?:final\s+)?java\.util\.List\s+toList\s*\(\s*\)/
        assert pointStub =~ /public\s+(?:final\s+)?java\.util\.Map\s+toMap\s*\(\s*\)/
        assert pointStub =~ /public\s+(?:final\s+)?int\s+size\s*\(\s*\)/

        // Opt-in convenience methods are NOT on Point (Point doesn't enable them).
        assert !(pointStub =~ /\bcopyWith\s*\(/),
                "Point did not enable copyWith but stub exposes it:\n${pointStub}"
        assert !(pointStub =~ /\bcomponents\s*\(/),
                "Point did not enable components() but stub exposes it:\n${pointStub}"

        // Runtime: defaults compose correctly.
        Class pointClass = loader.loadClass('foo.Point')
        def p = pointClass.getConstructor(int, int).newInstance(3, 4)
        assert p.x() == 3
        assert p.y() == 4
        assert p.getAt(0) == 3
        assert p.getAt(1) == 4
        assert p.toList() == [3, 4]
        assert p.toMap() == [x: 3, y: 4]
        assert p.size() == 2

        // === Range: opt-in copyWith + components, size disabled ===
        String rangeStub = stubJavaSourceFor('foo.Range')

        // copyWith and components are explicitly enabled → present.
        assert rangeStub =~ /public\s+(?:final\s+)?foo\.Range\s+copyWith\s*\(\s*java\.util\.Map\s+\w+\s*\)/,
                "expected copyWith(Map) on Range stub:\n${rangeStub}"
        // components() return type is groovy.lang.Tuple2 for two-component records.
        assert rangeStub =~ /public\s+(?:final\s+)?groovy\.lang\.Tuple2\s+components\s*\(\s*\)/,
                "expected components() on Range stub:\n${rangeStub}"

        // size explicitly disabled → must not appear.
        assert !(rangeStub =~ /\bsize\s*\(/),
                "Range disabled size=false but stub exposes it (subset-invariant break):\n${rangeStub}"

        // Default-on getAt/toList/toMap remain visible on Range.
        assert rangeStub =~ /public\s+(?:final\s+)?java\.lang\.Object\s+getAt\s*\(\s*int\s+\w+\s*\)/

        // Runtime: opt-ins work end-to-end.
        Class rangeClass = loader.loadClass('foo.Range')
        def r = rangeClass.getConstructor(int, int).newInstance(1, 10)
        def r2 = r.copyWith(hi: 99)
        assert r2.lo == 1
        assert r2.hi == 99
        def comps = r.components()
        assert comps.v1 == 1
        assert comps.v2 == 10
    }
}
