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

import java.lang.reflect.Modifier

/**
 * Captures the joint-compilation surface for {@code @Immutable}, which
 * composes (via {@code @AnnotationCollector}):
 * {@code @ImmutableBase}, {@code @Final}, {@code @ImmutableOptions},
 * {@code @PropertyOptions(propertyHandler = ImmutablePropertyHandler)},
 * {@code @ToString(cache = true, includeSuperProperties = true)},
 * {@code @EqualsAndHashCode(cache = true)},
 * {@code @TupleConstructor(defaults = false)},
 * {@code @MapConstructor(noArg = true, includeSuperProperties = true,
 *   includeFields = true)}, and {@code @KnownImmutable}.
 *
 * <p>This is the most demanding compositional test in the spike: every
 * stubber's CONVERSION-phase contribution interacts with the others, and
 * the {@code @Immutable} spike-results entry depends on all of:
 * <ul>
 *   <li>{@code @TupleConstructor(defaults=false)} — only the maximal-arg
 *       constructor (DefaultsMode.OFF path);</li>
 *   <li>{@code @MapConstructor(noArg=true)} — Map constructor plus no-arg
 *       (the no-arg is gated by "has at least one directly-declared
 *       property", which our test class satisfies);</li>
 *   <li>{@code @ToString} placeholder + body replacement;</li>
 *   <li>{@code @EqualsAndHashCode} placeholder + body replacement;</li>
 *   <li>{@code @Final} — class declared final in the stub.</li>
 * </ul>
 */
final class ImmutableJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Money.groovy': '''
                package foo

                @groovy.transform.Immutable
                class Money {
                    String currency
                    int amount
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                import java.util.Map;

                public class JavaUser {
                    public static Money tuple()           { return new Money("USD", 100); }
                    public static Money fromMap(Map m)    { return new Money(m); }
                    public static Money empty()           { return new Money(); }
                    public static String render(Money m)  { return m.toString(); }
                    public static int hashOf(Money m)     { return m.hashCode(); }
                    public static boolean same(Money a, Money b) { return a.equals(b); }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        String moneyStub = stubJavaSourceFor('foo.Money')

        // @Final → class is declared final in the stub.
        assert moneyStub =~ /\bfinal\s+class\s+Money\b/

        // @TupleConstructor(defaults=false) → only the maximal-arg form.
        assert moneyStub =~ /public\s+Money\s*\(\s*java\.lang\.String\s+\w+\s*,\s*int\s+\w+\s*\)/

        // @MapConstructor(noArg=true) → Foo(Map) plus Foo() (since Money has properties).
        assert moneyStub =~ /public\s+Money\s*\(\s*java\.util\.Map\s+\w+\s*\)/
        assert moneyStub =~ /public\s+Money\s*\(\s*\)/

        // @ToString and @EqualsAndHashCode declared overrides.
        assert moneyStub =~ /java\.lang\.String\s+toString\(\s*\)/
        assert moneyStub =~ /int\s+hashCode\(\s*\)/
        assert moneyStub =~ /boolean\s+equals\(\s*java\.lang\.Object\s+\w+\s*\)/

        // Runtime view: every body has been replaced with the real
        // implementation; the class behaves as a fully formed
        // @Immutable.
        Class moneyClass = loader.loadClass('foo.Money')

        // @Final at runtime
        assert Modifier.isFinal(moneyClass.modifiers)

        // Tuple constructor (the maximal form is the runtime constructor)
        def usd100 = moneyClass.getConstructor(String, int).newInstance('USD', 100)
        def usd100b = moneyClass.getConstructor(String, int).newInstance('USD', 100)
        def eur100 = moneyClass.getConstructor(String, int).newInstance('EUR', 100)

        // Map constructor
        def usd100c = moneyClass.newInstance(currency: 'USD', amount: 100)

        // @ToString
        assert usd100.toString() == 'foo.Money(USD, 100)'
        // @EqualsAndHashCode
        assert usd100.equals(usd100b)
        assert usd100.equals(usd100c)
        assert !usd100.equals(eur100)
        assert usd100.hashCode() == usd100b.hashCode()

        // No-arg constructor produced by @MapConstructor(noArg = true).
        def empty = moneyClass.newInstance()
        assert empty.currency == null
        assert empty.amount == 0
    }
}
