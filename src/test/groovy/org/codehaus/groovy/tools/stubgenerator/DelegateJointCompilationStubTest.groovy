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
 * Captures the joint-compilation surface for {@code @Delegate}.
 *
 * <p>The stubber and full transform share enumeration and filter helpers
 * (see {@link org.codehaus.groovy.transform.DelegateASTTransformation}),
 * so the stub honours {@code interfaces}, {@code deprecated},
 * {@code includes}/{@code excludes}, {@code includeTypes}/{@code excludeTypes}
 * the same way the runtime does. The stub surface is therefore a strict
 * subset of the runtime — never a superset.
 *
 * <p>Scenarios:
 *
 * <ol>
 *   <li><b>Field-target classpath delegate</b> ({@code @Delegate Date when}) —
 *       full coverage; Java callers can invoke any of {@code Date}'s public
 *       methods on the owner.</li>
 *   <li><b>Method-target classpath delegate</b> — same coverage via the
 *       method-return form.</li>
 *   <li><b>{@code excludes} filter</b> — excluded method does not appear on
 *       the stub (locks in the subset invariant).</li>
 *   <li><b>{@code interfaces=false}</b> — owner stub does not inherit the
 *       delegate type's interfaces.</li>
 *   <li><b>Same-unit Groovy delegate with hand-written methods</b> — full
 *       coverage; source-declared methods on a same-unit delegate type are
 *       visible at CONVERSION.</li>
 *   <li><b>{@code @Delegate} + {@code @Lazy}</b> — composition works at the
 *       signature level; runtime body uses the lazy-getter form.</li>
 *   <li><b>Generic owner with generic-typed delegate</b>
 *       ({@code class MyClass<T> { @Delegate List<T> items }}) — type
 *       parameter propagates through delegated method signatures.</li>
 * </ol>
 */
final class DelegateJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            // Scenario 1: field-target classpath delegate.
            'foo/Event.groovy': '''
                package foo
                import java.util.Date

                class Event {
                    @Delegate Date when
                    String title
                }
            ''',

            // Scenario 2: method-target classpath delegate.
            'foo/EventByMethod.groovy': '''
                package foo
                import java.util.Date

                class EventByMethod {
                    private Date when = new Date(0L)
                    @Delegate Date getWhen() { when }
                }
            ''',

            // Scenario 3: excludes filter — exclude one method.
            'foo/EventTrimmed.groovy': '''
                package foo
                import java.util.Date

                class EventTrimmed {
                    @Delegate(excludes = 'before') Date when
                }
            ''',

            // Scenario 4: interfaces=false — owner does not inherit delegate's interfaces.
            'foo/EventNoIface.groovy': '''
                package foo
                import java.util.Date

                class EventNoIface {
                    @Delegate(interfaces = false) Date when
                }
            ''',

            // Scenario 5: same-unit Groovy delegate with hand-written methods.
            // Delegate type declared first so its source-declared methods are
            // visible at CONVERSION when the owner's stubber walks.
            'foo/Inner.groovy': '''
                package foo
                class Inner {
                    String describe(int x) { "Inner: $x" }
                    int calc(int a, int b) { a + b }
                }
            ''',
            'foo/Outer.groovy': '''
                package foo
                class Outer {
                    @Delegate Inner inner = new Inner()
                }
            ''',

            // Scenario 6: @Delegate + @Lazy combo.
            'foo/Holder.groovy': '''
                package foo

                class Holder {
                    @Delegate @Lazy java.util.ArrayList<String> $items = { new ArrayList<String>() }()
                }
            ''',

            // Scenario 7: generic owner with generic-typed delegate.
            'foo/Bag.groovy': '''
                package foo

                class Bag<T> {
                    @Delegate List<T> items = new ArrayList<T>()
                }
            ''',

            'foo/JavaUser.java': '''
                package foo;

                import java.util.Date;
                import java.util.Arrays;
                import java.util.List;

                public class JavaUser {
                    // Scenario 1: field-target classpath delegate.
                    public static boolean eventBefore(Event a, Event b) {
                        return a.before(b.getWhen());
                    }
                    public static long eventTime(Event a) {
                        return a.getTime();
                    }

                    // Scenario 2: method-target classpath delegate.
                    public static long methodEventTime(EventByMethod e) {
                        return e.getTime();
                    }

                    // Scenario 4: interfaces=false. Owner is NOT Cloneable.
                    public static EventNoIface buildNoIface() {
                        return new EventNoIface();
                    }

                    // Scenario 5: same-unit delegate.
                    public static String outerDescribe(Outer o, int x) {
                        return o.describe(x);
                    }
                    public static int outerCalc(Outer o, int a, int b) {
                        return o.calc(a, b);
                    }

                    // Scenario 6: @Lazy combo. Java caller treats Holder as a
                    // List<String> via stub-exposed methods.
                    public static int holderSize(Holder h) {
                        return h.size();
                    }
                    public static boolean holderAdd(Holder h, String s) {
                        return h.add(s);
                    }

                    // Scenario 7: generic propagation.
                    public static int bagSize(Bag<String> b) {
                        return b.size();
                    }
                    public static boolean bagAdd(Bag<String> b, String value) {
                        return b.add(value);
                    }
                    public static String bagFirst(Bag<String> b) {
                        // get(int) returns T which is String here.
                        return b.get(0);
                    }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // === Scenario 1: field-target classpath delegate ===
        String eventStub = stubJavaSourceFor('foo.Event')
        // Sample of Date methods that should be delegated.
        assert eventStub =~ /public\s+boolean\s+before\s*\(\s*java\.util\.Date\s+\w+\s*\)/
        assert eventStub =~ /public\s+long\s+getTime\s*\(\s*\)/
        // interfaces=true (default): Date implements Cloneable, Comparable<Date>, Serializable.
        // At minimum, expect Cloneable to appear on Event's interfaces clause.
        assert eventStub =~ /implements[^{]*\bCloneable\b/

        // Runtime: full delegation in place.
        Class eventClass = loader.loadClass('foo.Event')
        long t0 = 1_000_000L
        long t1 = 2_000_000L
        def e0 = eventClass.newInstance()
        e0.when = new Date(t0)
        def e1 = eventClass.newInstance()
        e1.when = new Date(t1)
        assert e0.before(e1.when)
        assert e0.getTime() == t0

        // === Scenario 2: method-target classpath delegate ===
        String byMethodStub = stubJavaSourceFor('foo.EventByMethod')
        assert byMethodStub =~ /public\s+long\s+getTime\s*\(\s*\)/

        Class byMethodClass = loader.loadClass('foo.EventByMethod')
        assert byMethodClass.newInstance().getTime() == 0L

        // === Scenario 3: excludes filter (subset invariant) ===
        String trimmedStub = stubJavaSourceFor('foo.EventTrimmed')
        // before(Date) MUST NOT appear on the stub (excludes mirrored).
        assert !(trimmedStub =~ /\bbefore\s*\(\s*java\.util\.Date/),
                "@Delegate(excludes='before') leaked through to stub. Subset invariant broken. Stub:\n${trimmedStub}"
        // Other Date methods still delegated.
        assert trimmedStub =~ /public\s+long\s+getTime\s*\(\s*\)/

        // === Scenario 4: interfaces=false ===
        String noIfaceStub = stubJavaSourceFor('foo.EventNoIface')
        // Cloneable should not appear in EventNoIface's implements clause
        // (only GroovyObject from the standard surface).
        assert !(noIfaceStub =~ /implements[^{]*\bCloneable\b/),
                "@Delegate(interfaces=false) added Cloneable to stub. Stub:\n${noIfaceStub}"

        Class noIfaceClass = loader.loadClass('foo.EventNoIface')
        assert !Cloneable.isAssignableFrom(noIfaceClass)

        // === Scenario 5: same-unit Groovy delegate ===
        String outerStub = stubJavaSourceFor('foo.Outer')
        assert outerStub =~ /public\s+java\.lang\.String\s+describe\s*\(\s*int\s+\w+\s*\)/
        assert outerStub =~ /public\s+int\s+calc\s*\(\s*int\s+\w+\s*,\s*int\s+\w+\s*\)/

        Class outerClass = loader.loadClass('foo.Outer')
        def outer = outerClass.newInstance()
        assert outer.describe(7) == 'Inner: 7'
        assert outer.calc(2, 3) == 5

        // === Scenario 6: @Delegate + @Lazy ===
        String holderStub = stubJavaSourceFor('foo.Holder')
        // ArrayList methods delegated through the lazy property.
        assert holderStub =~ /public\s+int\s+size\s*\(\s*\)/
        assert holderStub =~ /public\s+boolean\s+add\s*\(\s*java\.lang\.String\s+\w+\s*\)/

        Class holderClass = loader.loadClass('foo.Holder')
        def h = holderClass.newInstance()
        assert h.size() == 0
        assert h.add('one')
        assert h.size() == 1

        // === Scenario 7: generic owner with generic-typed delegate ===
        String bagStub = stubJavaSourceFor('foo.Bag')
        // The stub should declare Bag<T> with T propagating into delegated
        // methods. add(T), get(int) returning T, size().
        assert bagStub =~ /class\s+Bag\b[^{]*<\s*T\b/
        assert bagStub =~ /public\s+boolean\s+add\s*\(\s*T\s+\w+\s*\)/,
                "Expected add(T) on Bag stub, got:\n${bagStub}"
        assert bagStub =~ /public\s+T\s+get\s*\(\s*int\s+\w+\s*\)/,
                "Expected T get(int) on Bag stub, got:\n${bagStub}"
        assert bagStub =~ /public\s+int\s+size\s*\(\s*\)/

        Class bagClass = loader.loadClass('foo.Bag')
        def bag = bagClass.newInstance()
        assert bag.size() == 0
        assert bag.add('hello')
        assert bag.get(0) == 'hello'
    }
}
