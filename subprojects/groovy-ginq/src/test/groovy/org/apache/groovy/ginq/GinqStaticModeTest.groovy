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
import groovy.transform.CompileStatic
import groovy.transform.Internal
import org.apache.groovy.ginq.provider.collection.runtime.LazyQueryable
import org.apache.groovy.ginq.provider.collection.runtime.Queryable
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.ArrayExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import java.util.stream.Stream

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX

/**
 * Behaviour called out on the static GINQ review: checking mode, query scope,
 * lexical capture, return-type widening, per-expression dynamic calls,
 * extension registration, and re-enumeration.
 */
final class GinqStaticModeTest {

    @Test
    void compileDynamicInsideTypeCheckedStillChecksTheQuery() {
        assertScript '''
            import groovy.transform.TypeChecked
            import groovy.transform.CompileDynamic
            @TypeChecked
            class Example {
                @CompileDynamic
                def query(List<Map> rows) {
                    GQ {
                        from r in rows
                        where r.value > 10
                        select r.value
                    }
                }
            }
            def rows = [[value: 5], [value: 12], [value: 20]]
            assert new Example().query(rows).toList() == [12, 20]
        '''
    }

    @Test
    void compileStaticSkipInsideTypeCheckedStillChecksTheQuery() {
        assertScript '''
            import groovy.transform.TypeChecked
            import groovy.transform.CompileStatic
            import groovy.transform.TypeCheckingMode
            @TypeChecked
            class Example {
                @CompileStatic(TypeCheckingMode.SKIP)
                def query(List<Map> rows) {
                    GQ {
                        from r in rows
                        select r.value
                    }
                }
            }
            assert new Example().query([[value: 4], [value: 1]]).toList() == [4, 1]
        '''
    }

    @Test
    void typeCheckedClassStillChecksAMethodThatIsNotSkipped() {
        def err = shouldFail '''
            import groovy.transform.TypeChecked
            import groovy.transform.CompileDynamic
            @TypeChecked
            class Example {
                @CompileDynamic
                def query(List rows) {
                    GQ { from r in rows select r.value }
                }
                int bad() { 'no' }
            }
        '''
        assert err.message.contains('bad')
    }

    @Test
    void compileDynamicInsideCompileStaticIsNotTypeChecked() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.CompileDynamic
            @CompileStatic
            class Example {
                @CompileDynamic
                def query(List rows) {
                    GQ { from r in rows select r.notARealProperty }
                }
            }
            assert new Example().query([[notARealProperty: 7]]).toList() == [7]
        '''
    }

    @Test
    void rowChecksDoNotLeakToOtherMethods() {
        def property = shouldFail '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                Object value
                def query(List<Map> rows) {
                    GQ { from r in rows select r.value }
                }
                String unrelated() { value.mispelledProperty }
            }
        '''
        assert property.message.contains('mispelledProperty')

        def subscript = shouldFail '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                def query(List<Map> rows) {
                    GQ { from r in rows select r.value }
                }
                Object getValue(Object value) { value[0] }
            }
        '''
        assert subscript.message.contains('[') || subscript.message.contains('getAt') || subscript.message.contains('value')
    }

    @Test
    void subscriptInsideTheQueryStaysDynamic() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                static List run() {
                    GQ { from r in [[10, 20], [30, 40]] select r[0] }.toList()
                }
            }
            assert Example.run() == [10, 30]
        '''
    }

    @Test
    void shadowingAndAliasesUseRealScope() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.concurrent.TimeUnit as TU
            @CompileStatic
            class Example {
                static List capturedParameter(List<Map> rows) {
                    int threshold = 10
                    def filtered = { int bound ->
                        GQ {
                            from row in rows
                            where row.value > bound
                            select row.value
                        }
                    }
                    threshold = 100
                    // bound is the closure parameter, not the method local.
                    return filtered(10).toList()
                }
                static List alias() {
                    def classes = GQ {
                        from row in [1]
                        select TU
                    }.toList()
                    return classes
                }
            }
            assert Example.capturedParameter([[value: 12], [value: 4]]) == [12]
            assert Example.alias()[0] == java.util.concurrent.TimeUnit
        '''
    }

    @Test
    void mutatedLocalIsSharedAndReadOnlyLocalIsCopied() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                static List<Integer> writes() {
                    Integer cnt = 0
                    List<Integer> values = GQ { from n in [1, 2] select cnt++ }.toList()
                    return values + [cnt]
                }
                static List reads() {
                    int extra = 1
                    def query = GQ { from n in [1, 2, 3, 4] where n > extra select n }
                    extra = 3
                    return query.toList()
                }
            }
            assert Example.writes() == [0, 1, 2]
            assert Example.reads() == [2, 3, 4]
        '''
    }

    @Test
    void returnTypeWidensOnlyForARealOverride() {
        assertScript '''
            class Base {
                String query(String name) { name }
            }
            class Child extends Base {
                @groovy.ginq.transform.GQ
                def query(List rows) {
                    from n in rows
                    select n
                }
            }
            assert Child.getDeclaredMethod('query', List).returnType.name.contains('Queryable')
            assert new Child().query([1, 2]).toList() == [1, 2]
        '''

        assertScript '''
            import org.apache.groovy.ginq.provider.collection.runtime.Queryable
            class Hidden {
                private String query(List rows) { 'hidden' }
            }
            class Child extends Hidden {
                @groovy.ginq.transform.GQ
                def query(List rows) {
                    from n in rows
                    select n
                }
            }
            class Statics {
                static String load(String name) { name }
                Queryable rows(List rows) { Queryable.from(rows) }
            }
            class Other extends Statics {
                @groovy.ginq.transform.GQ
                def load(List rows) {
                    from n in rows
                    select n
                }
                @groovy.ginq.transform.GQ
                def rows(List rows) {
                    from n in rows
                    select n
                }
            }
            assert Child.getDeclaredMethod('query', List).returnType.name.contains('Queryable')
            assert Other.getDeclaredMethod('load', List).returnType.name.contains('Queryable')
            assert Other.getDeclaredMethod('rows', List).returnType.name.contains('Queryable')
            assert new Child().query([4]).toList() == [4]
        '''

        def err = shouldFail '''
            interface Api {
                String load(List rows)
            }
            class Impl implements Api {
                @groovy.ginq.transform.GQ
                def load(List rows) {
                    from n in rows
                    select n
                }
            }
        '''
        assert err.message.contains('incompatible with java.lang.String')
        assert !err.message.contains('Queryable')
    }

    @Test
    void otherExtensionsStillSeeTheQuery() {
        RecordingChecks.NAMES.clear()
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic(extensions = 'org.apache.groovy.ginq.RecordingChecks')
            class Example {
                static List run() {
                    return GQ { from n in [1, 2] where n > 0 select n * 2 }.toList()
                }
            }
            assert Example.run() == [2, 4]
        '''
        // The extension on the enclosing method sees the wrapper the macro leaves
        // behind, and the clause calls the static writer type-checks.
        assert RecordingChecks.NAMES.contains('toList')
        assert RecordingChecks.NAMES.contains('defer') || RecordingChecks.NAMES.contains('call')
    }

    @Test
    void lazyQueryableIsInternalAndDeferIsAbi() {
        assert LazyQueryable.getAnnotation(Internal) != null
        // GroovyABI is CLASS retention, so it is in the class file and not on the reflective method.
        def bytes = LazyQueryable.getResourceAsStream('LazyQueryable.class').bytes
        String utf = new String(bytes, 'ISO-8859-1')
        assert utf.contains('GroovyABI')
        assert utf.contains('7.0.0')
        assert utf.contains('defer')
    }

    @Test
    void extensionMemberThatCannotBeMergedIsReported() {
        SourceUnit source = SourceUnit.create('ginq.groovy', '')
        AnnotationNode annotation = new AnnotationNode(ClassHelper.make(CompileStatic))
        MethodCallExpression call = callX(varX('builder'), 'extensions', args(constX('other.Extension')))
        call.setImplicitThis(false)
        annotation.setMember('extensions', call)

        GinqStaticBridge.addExtension(source, annotation)

        assert source.errorCollector.hasErrors()
        assert annotation.getMember('extensions').is(call)

        source = SourceUnit.create('ginq.groovy', '')
        annotation = new AnnotationNode(ClassHelper.make(CompileStatic))
        annotation.setMember('extensions', new ConstantExpression('other.Extension'))
        GinqStaticBridge.addExtension(source, annotation)
        assert !source.errorCollector.hasErrors()
        assert annotation.getMember('extensions') instanceof ListExpression

        source = SourceUnit.create('ginq.groovy', '')
        annotation = new AnnotationNode(ClassHelper.make(CompileStatic))
        annotation.setMember('extensions', new ClassExpression(ClassHelper.make(RecordingChecks)))
        GinqStaticBridge.addExtension(source, annotation)
        assert !source.errorCollector.hasErrors()
        def member = annotation.getMember('extensions')
        assert member instanceof ListExpression
        assert member.expressions*.text.contains(RecordingChecks.name)

        source = SourceUnit.create('ginq.groovy', '')
        annotation = new AnnotationNode(ClassHelper.make(CompileStatic))
        annotation.setMember('extensions', new ArrayExpression(ClassHelper.STRING_TYPE, [constX('other.Extension')]))
        GinqStaticBridge.addExtension(source, annotation)
        assert !source.errorCollector.hasErrors()
        assert annotation.getMember('extensions') instanceof ListExpression

        source = SourceUnit.create('ginq.groovy', '')
        annotation = new AnnotationNode(ClassHelper.make(CompileStatic))
        annotation.setMember('extensions', new ListExpression([callX(varX('builder'), 'extensions', args(constX('other.Extension')))]))
        GinqStaticBridge.addExtension(source, annotation)
        assert source.errorCollector.hasErrors()
    }

    @Test
    void fluentIterableIsRereadAndStreamIsNeverConsumed() {
        List<Integer> numbers = [1, 2, 3]
        def query = Ginq.from(numbers).where { Integer n -> n > 1 }
        List<Integer> first = query.toList()
        numbers.add(4)
        List<Integer> second = query.toList()
        assert first == [2, 3]
        assert second == [2, 3, 4]

        def streamed = Ginq.from([1, 2, 3].stream()).where { Integer n -> n > 1 }
        assert streamed.toList() == [2, 3]
        assert streamed.toList() == [2, 3]

        Queryable gq = GQ { from n in [1, 2, 3] where n > 1 select n }
        Stream s1 = gq.stream()
        Stream s2 = gq.stream()
        assert !s1.is(s2)
        assert s1.count() == 2
        assert s2.count() == 2
        assert gq.toList() == [2, 3]
        Stream s3 = gq.stream()
        assert !s3.is(s1)
        assert s3.count() == 2

        def listed = Queryable.from([1, 2, 3])
        assert listed.stream().count() == 3
        assert listed.stream().count() == 3

        int seen = 0
        Queryable sized = GQ { from n in [1, 2, 3] select (seen++) }
        assert sized.stream().spliterator().trySplit() == null
        assert sized.stream().count() == 3
        assert seen == 0
        def cursor = sized.stream().spliterator()
        assert cursor.tryAdvance { }
        assert cursor.estimateSize() == 2L
        assert sized.toList().size() == 3

        def right = Queryable.from([1, 2, 3].stream())
        right.stream()
        def joined = Queryable.from([1, 2]).innerJoin(right) { a, b -> a == b }
        assert joined.toList().size() == 2
    }

    @Test
    void skippedTypeCheckedClassDoesNotCheckTheQuery() {
        assertScript '''
            import groovy.transform.TypeChecked
            import groovy.transform.TypeCheckingMode
            @TypeChecked(TypeCheckingMode.SKIP)
            class Example {
                def query(List rows) {
                    GQ { from r in rows select r.notARealProperty }
                }
            }
            assert new Example().query([[notARealProperty: 3]]).toList() == [3]
        '''
    }

    @Test
    void queryInALocalClassInsideASkippedMethodIsNotChecked() {
        assertScript '''
            import groovy.transform.CompileStatic
            import groovy.transform.TypeChecked
            import groovy.transform.TypeCheckingMode
            @CompileStatic
            class Example {
                @TypeChecked(TypeCheckingMode.SKIP)
                def outer(List rows) {
                    def inner = new Object() {
                        def run() {
                            GQ { from r in rows select r.notARealProperty }
                        }
                    }
                    inner.run()
                }
            }
            assert new Example().outer([[notARealProperty: 4]]).toList() == [4]
        '''
    }

    @Test
    void subscriptOnAQueryResultIsCheckedAndAFieldIsNot() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                Object first() {
                    def result = GQ { from n in [[9], [8]] select n }
                    result.getAt(0)
                }
                Object nested() {
                    def result = GQ { from n in [[9], [8]] select n }
                    result[0][0]
                }
            }
            assert new Example().first() == [9]
            assert new Example().nested() == 9
        '''

        def field = shouldFail '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                Object value
                def query(List rows) {
                    GQ { from r in rows select r.value }
                }
                List bad() { value[0].toList() }
            }
        '''
        assert field.message.contains('toList') || field.message.contains('value')

        def parameter = shouldFail '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                def query(List rows) {
                    GQ { from r in rows select r.value }
                }
                List getValue(Object value) { value[0].toList() }
            }
        '''
        assert parameter.message.contains('toList') || parameter.message.contains('value')
    }

    // GROOVY-12724
    @Test
    void fieldAndPropertyAreNotShadowedByASameNamedLocal() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                int threshold = 10
                List mixed() {
                    def query = GQ {
                        from n in [1, 2, 6, 12]
                        where n > threshold
                        select ({ int threshold -> n + threshold }(5))
                    }
                    this.threshold = 4
                    return query.toList()
                }
            }
            assert new Example().mixed() == [11, 17]
        '''

        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                private int stored = 1
                int getThreshold() { stored }
                void setThreshold(int value) { stored = value }
                List later() {
                    def query = GQ {
                        from n in [1, 2, 3]
                        where n > threshold
                        select n
                    }
                    this.threshold = 2
                    return query.toList()
                }
            }
            assert new Example().later() == [3]
        '''

        assertScript '''
            import groovy.transform.TypeChecked
            @TypeChecked
            class Example {
                int threshold = 1
                List later() {
                    def query = GQ {
                        from n in [1, 2, 3]
                        where n > threshold
                        select n
                    }
                    this.threshold = 2
                    return query.toList()
                }
            }
            assert new Example().later() == [3]
        '''

        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                int threshold = 1
                List immediate() {
                    def listed = GQL {
                        from n in [1, 2, 3]
                        where n > threshold
                        select n
                    }
                    this.threshold = 2
                    return listed
                }
            }
            assert new Example().immediate() == [2, 3]
        '''
    }

    // GROOVY-12724
    @Test
    void closureParameterShadowsTheFieldAndAnAliasIsNotALocal() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                int threshold = 10
                List parameterWins(int ignored) {
                    def filtered = { int threshold ->
                        def query = GQ {
                            from n in [1, 2, 6]
                            where n > threshold
                            select n
                        }
                        this.threshold = 100
                        return query.toList()
                    }
                    return filtered(4)
                }
            }
            assert new Example().parameterWins(0) == [6]
        '''

        assertScript '''
            import java.util.concurrent.TimeUnit as threshold
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                static List run() {
                    return GQ {
                        from n in [1]
                        select threshold
                    }.toList()
                }
            }
            assert Example.run()[0] == java.util.concurrent.TimeUnit
        '''

        def err = shouldFail '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                List<Integer> run() {
                    int threshold = 10
                    return GQ {
                        from n in [1, 2, 3]
                        where ({ -> int threshold = 5; n > threshold }())
                        select n
                    }.toList()
                }
            }
        '''
        assert err.message.contains('threshold')
    }

    // GROOVY-12724
    @Test
    void unknownRowPropertyStaysDynamic() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Example {
                static List run() {
                    def rows = [[a: 1], [a: 2]]
                    return GQ {
                        from r in rows
                        select r.vlue
                    }.toList()
                }
            }
            assert Example.run() == [null, null]
        '''
    }

    @Test
    void clauseCallKeepsTheMethodAnotherExtensionSelected() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic(extensions = 'org.apache.groovy.ginq.ChooseChecks')
            class Example {
                static String choose(CharSequence value) { 'seq' }
                static String choose(String value) { 'str' }
                static List run() {
                    String outside = choose('x')
                    List inside = GQ {
                        from n in [1]
                        select choose('x')
                    }.toList()
                    return [outside, inside[0]]
                }
            }
            assert Example.run() == ['seq', 'seq']
        '''
    }

    @Test
    void staticClauseKeepsAWellTypedStream() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.stream.Collectors
            @CompileStatic
            class Example {
                static List sums() {
                    GQ {
                        from n in [1, 1, 3, 3, 6, 6, 6]
                        groupby n
                        select n, agg(_g.stream().map(r -> BigDecimal.valueOf(r.n)).reduce(BigDecimal.ZERO, BigDecimal::add))
                    }.toList()
                }
                static List joined() {
                    GQ {
                        from s in ['a', 'bc', 'def']
                        groupby s.size()
                        select s.size(), agg(_g.stream().map(r -> r.s).collect(Collectors.joining()))
                    }.toList()
                }
            }
            assert Example.sums() == [[1, 2], [3, 6], [6, 18]]
            assert Example.joined() == [[1, 'a'], [2, 'bc'], [3, 'def']]
        '''
    }
}
