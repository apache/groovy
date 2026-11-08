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
package org.apache.groovy.ginq

import groovy.ginq.Ginq
import groovy.lang.Tuple
import org.apache.groovy.ginq.provider.GinqAggregate
import org.apache.groovy.ginq.provider.GinqAggregates
import org.apache.groovy.ginq.provider.collection.runtime.LazyQueryable
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.apache.groovy.ginq.provider.collection.runtime.WindowDefinition
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.nio.file.Files
import java.util.function.Function

import static groovy.ginq.Ginq.from

/**
 * Product paths that the main GINQ suite does not happen to call:
 * fluent defaults, aggregate construction failures, windows, and queries
 * compiled in fields, constructors, and checked methods.
 * <p>
 * Some changed branches stay uncovered because nothing the compiler
 * produces can enter them:
 * <ul>
 * <li>{@code GinqQuery.from(Iterable)} treating its argument as a
 * {@code GinqQuery}, and the same test in {@code queryableOf}.
 * {@code GinqQuery} implements {@code Query}, not {@code Iterable}.</li>
 * <li>{@code queryableOf(Query)} for a query that is not a {@code GinqQuery}.
 * That is the only {@code Query} implementation.</li>
 * <li>Boxing a prefix {@code ++} or an assignment inside {@code GQ}.
 * The clause parser rejects both ({@code select ++cnt}, {@code select cnt += n}).</li>
 * <li>The number-wrapper arm of {@code arrayComponent}. Boxing runs at
 * conversion, before the declared type is resolved, so only a primitive
 * {@code int} matches {@code isNumberType}.</li>
 * <li>{@code schedule} with a null compilation unit, a null bridge host,
 * and {@code findHost} missing the call. The macro only calls
 * {@code embed} with the live compilation unit and the call it is visiting.</li>
 * </ul>
 */
class GinqEdgePathTest {

    @Test
    void fluentDefaultsAndEmptySources() {
        assert Ginq.of().toList().isEmpty()
        assert Ginq.of(null).toList().isEmpty()
        assert from([1, 2, 3]).any()
        assert !from([]).any()
        assert from([1, 2, 3]).except([2]).toList() == [1, 3]
        assert from([1, 2, 3]).except(from([2])).toList() == [1, 3]
        assert from([1, 2]).concat([3]).toList() == [1, 2, 3]

        assert from([1, 2]).selectMany { n -> null }.toList().isEmpty()
        assert from([1]).selectMany({ n -> null }, { a, b -> a }).toList().isEmpty()

        def right = from([1, 2, 3]).rightJoin([2, 3], { a, b -> a == b }, { a, b -> a }).toList()
        assert right.contains(2)
        def full = from([1, 2]).fullJoin([2, 4], { a, b -> a == b }, { a, b -> a == null ? b : a }).toList()
        assert full.contains(1)
        assert full.contains(4)

        Queryable<Integer> alsoQueryable = Queryable.from([2, 3])
        Iterable<Integer> both = (Iterable<Integer>) Proxy.newProxyInstance(
                Queryable.classLoader,
                new Class[]{Iterable, Queryable},
                (InvocationHandler) (proxy, method, args) -> {
                    if (method.name == 'iterator' && method.parameterCount == 0) {
                        return alsoQueryable.toList().iterator()
                    }
                    return method.invoke(alsoQueryable, args)
                })
        assert from([1, 2, 3]).rightJoin(both, { a, b -> a == b }, { a, b -> a }).toList().contains(2)

        assert from([1, 2]).union(from([2, 4])).toList() == [1, 2, 4]
        assert from([1]).union([2]).toList() == [1, 2]
        assert from([1, 2, 2]).intersect(from([2, 9])).toList() == [2]
        assert from([1, 2, 2]).intersect([2, 9]).toList() == [2]
        assert from([3, 1, 2]).orderBy { it }.thenByDescending { it }.toList() == [1, 2, 3]
        assert from([1, 2, 3]).stream().count() == 3
        assert from([1, 2]).iterator().toList() == [1, 2]
        assert from([1, 2, 3]).all { it > 0 }
        assert !from([1, -1]).all { it > 0 }
    }

    @Test
    void aggregateLookupAndConstruction() {
        assert GinqAggregates.find(null, null) == null
        assert GinqAggregates.find('', GinqAggregates.class.classLoader) == null
        assert GinqAggregates.find('missing', null) == null
        assert GinqAggregates.find('total', null) == TotalAggregate

        Queryable<Integer> group = Queryable.from([1, 2, 3])
        assert GinqAggregates.apply(TotalAggregate, group, null) == 6
        assert GinqAggregates.apply(TotalAggregate, group, { n -> n }) == 6
        Function<Object, Object> selector = (Object n) -> n
        assert GinqAggregates.apply(TotalAggregate, group, selector) == 6

        IllegalArgumentException badSelector = null
        try {
            GinqAggregates.apply(TotalAggregate, group, 'nope')
        } catch (IllegalArgumentException caught) {
            badSelector = caught
        }
        assert badSelector != null
        assert badSelector.message.contains('closure or a function')

        IllegalStateException hidden = null
        try {
            GinqAggregates.apply(HiddenAggregate, group, null)
        } catch (IllegalStateException caught) {
            hidden = caught
        }
        assert hidden != null
        assert hidden.message.contains(HiddenAggregate.name)

        IllegalStateException boom = null
        try {
            GinqAggregates.apply(BoomAggregate, group, null)
        } catch (IllegalStateException caught) {
            boom = caught
        }
        assert boom != null

        File root = Files.createTempDirectory('ginq-agg').toFile()
        try {
            File services = new File(root, 'META-INF/services')
            assert services.mkdirs()
            new File(services, 'org.apache.groovy.ginq.provider.GinqAggregate').text = 'missing.Aggregate\n'
            URLClassLoader loader = new URLClassLoader([root.toURI().toURL()] as URL[], GinqEdgePathTest.class.classLoader)
            assert GinqAggregates.find('total', loader) == TotalAggregate
        } finally {
            root.deleteDir()
        }
    }

    @Test
    void deferredWindowDelegates() {
        Queryable<Integer> query = LazyQueryable.defer(new Object[0], (Object[] args) -> Queryable.from([1, 2, 3]))
        def window = WindowDefinition.of().setId(Tuple.tuple('p', 'o', 'w'))
        assert query.over(Tuple.tuple(1, 0L), window) != null
    }

    @Test
    void bridgeGuardsAndCheckedHosts() {
        ConstantExpression query = new ConstantExpression(1)
        assert GinqStaticBridge.embed(null, null, null, query).is(query)

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            class Probe {
                @CompileStatic
                static List run() {
                    return GQ { from n in [1, 2] select n }.toList()
                }
            }
            assert Probe.run() == [1, 2]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Holder {
                List values = GQ { from n in [1, 2] select n }.toList()
            }
            assert new Holder().values == [1, 2]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Built {
                List values
                Built() {
                    values = GQ { from n in [3] select n }.toList()
                }
            }
            assert new Built().values == [3]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Caught {
                static List run() {
                    try {
                        throw new IllegalStateException('x')
                    } catch (IllegalStateException e) {
                        return GQ { from n in [e.message] select n }.toList()
                    }
                }
            }
            assert Caught.run() == ['x']
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Multi {
                static List run() {
                    def (a, b) = [1, 2]
                    return GQ { from n in [a, b] select n }.toList()
                }
            }
            assert Multi.run() == [1, 2]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Box {
                static List run() {
                    Integer cnt = 0
                    return GQ { from n in [1, 2] select cnt++ }.toList()
                }
            }
            assert Box.run() == [0, 1]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic(extensions = 'org.codehaus.groovy.transform.stc.TraitTypeCheckingExtension')
            class Extra {
                static List run() {
                    return GQ { from n in [1] select n }.toList()
                }
            }
            assert Extra.run() == [1]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic(extensions = ['org.codehaus.groovy.transform.stc.TraitTypeCheckingExtension'])
            class Listed {
                static List run() {
                    return GQ { from n in [1] select n }.toList()
                }
            }
            assert Listed.run() == [1]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic(extensions = ['org.codehaus.groovy.transform.stc.TraitTypeCheckingExtension', 'org.apache.groovy.ginq.GinqRowChecks'])
            class Already {
                static List run() {
                    return GQ { from n in [1] select n }.toList()
                }
            }
            assert Already.run() == [1]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Outer {
                static class Inner {
                    static List run() {
                        return GQ { from n in [7] select n }.toList()
                    }
                }
            }
            assert Outer.Inner.run() == [7]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.DYNAMIC, '''
            class Base {
                String ginq(list, extra) { 'no' }
                Object ginq(list) { null }
            }
            class Child extends Base {
                @groovy.ginq.transform.GQ
                def ginq(list) {
                    from n in list
                    select n
                }
                @groovy.ginq.transform.GQ
                @groovy.transform.CompileDynamic
                def tagged(list) {
                    from n in list
                    select n
                }
                @groovy.ginq.transform.GQ
                @groovy.transform.CompileStatic(groovy.transform.TypeCheckingMode.SKIP)
                def skipped(list) {
                    from n in list
                    select n
                }
            }
            def child = new Child()
            assert child.ginq([1, 2]).toList() == [1, 2]
            assert child.tagged([4]).toList() == [4]
            assert child.skipped([5]).toList() == [5]
        ''')

        AssertionError incompatible = null
        try {
            GinqScriptModes.runOne(GinqScriptModes.Kind.DYNAMIC, '''
                class Base {
                    String ginq(list) { 'no' }
                }
                class Child extends Base {
                    @groovy.ginq.transform.GQ
                    def ginq(list) {
                        from n in list
                        select n
                    }
                }
            ''')
        } catch (AssertionError caught) {
            incompatible = caught
        }
        assert incompatible != null
        assert incompatible.message.contains('incompatible with java.lang.String')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Indexed {
                static List run() {
                    def found = []
                    for (i, n in [9]) {
                        found << GQ { from x in [n, i] select x }.toList()[0]
                    }
                    return found
                }
            }
            assert Indexed.run() == [9]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Named {
                static void ginqQuery$0() { }
                static List run() {
                    return GQ { from n in [1] select n }.toList()
                }
            }
            assert Named.run() == [1]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Init {
                List values
                {
                    values = GQ { from n in [8] select n }.toList()
                }
            }
            assert new Init().values == [8]
        ''')

        GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            abstract class Checked {
                abstract void unused()
                static List run() {
                    return GQ { from n in [1] select n }.toList()
                }
            }
            assert Checked.run() == [1]
        ''')

        AssertionError rejected = null
        try {
            GinqScriptModes.runOne(GinqScriptModes.Kind.COMPILE_STATIC, '''
                import groovy.transform.CompileStatic
                @CompileStatic
                class Checked {
                    static List run() {
                        return GQ { from n in [1] select n }.toList()
                    }
                    static Object badProperty() {
                        return 'ab'.notAProperty
                    }
                    static Object badCall() {
                        Object value = new Object()
                        return value.missingMethod()
                    }
                }
                Checked.badProperty()
                Checked.badCall()
            ''')
        } catch (AssertionError caught) {
            rejected = caught
        }
        assert rejected != null
        assert rejected.message.contains('notAProperty')
        assert rejected.message.contains('missingMethod')
    }

    static class HiddenAggregate implements GinqAggregate {
        private HiddenAggregate() {
        }

        @Override
        String name() {
            return 'hidden'
        }

        @Override
        Object apply(Queryable<?> group, Function<Object, Object> selector) {
            return null
        }
    }

    static class BoomAggregate implements GinqAggregate {
        BoomAggregate() {
            throw new IllegalStateException('boom')
        }

        @Override
        String name() {
            return 'boom'
        }

        @Override
        Object apply(Queryable<?> group, Function<Object, Object> selector) {
            return null
        }
    }
}
