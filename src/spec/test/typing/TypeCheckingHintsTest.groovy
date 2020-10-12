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
package typing

import groovy.transform.stc.StaticTypeCheckingTestCase

/**
 * This unit test contains both assertScript and new GroovyShell().evaluate
 * calls. It is important *not* to replace the evaluate calls with assertScript, or the semantics
 * of the tests would be very different!
 */
class TypeCheckingHintsTest extends StaticTypeCheckingTestCase {

    void testFirstParamHint() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_firstparam[]
            import groovy.transform.stc.FirstParam
            void doSomething(String str, @ClosureParams(FirstParam) Closure c) {
                c(str)
            }
            doSomething('foo') { println it.toUpperCase() }
            // end::typehint_firstparam[]
        '''
    }

    void testSecondParamHint() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_secondparam[]
            import groovy.transform.stc.SecondParam
            void withHash(String str, int seed, @ClosureParams(SecondParam) Closure c) {
                c(31*str.hashCode()+seed)
            }
            withHash('foo', (int)System.currentTimeMillis()) {
                int mod = it%2
            }
            // end::typehint_secondparam[]
        '''
    }

    void testThirdParamHint() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_thirdparam[]
            import groovy.transform.stc.ThirdParam
            String format(String prefix, String postfix, String o, @ClosureParams(ThirdParam) Closure c) {
                "$prefix${c(o)}$postfix"
            }
            assert format('foo', 'bar', 'baz') {
                it.toUpperCase()
            } == 'fooBAZbar'
            // end::typehint_thirdparam[]
        '''
    }

    void testFirstGenericTypeParamHint() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_firstgt[]
            import groovy.transform.stc.FirstParam
            public <T> void doSomething(List<T> strings, @ClosureParams(FirstParam.FirstGenericType) Closure c) {
                strings.each {
                    c(it)
                }
            }
            doSomething(['foo','bar']) { println it.toUpperCase() }
            doSomething([1,2,3]) { println(2*it) }
            // end::typehint_firstgt[]
        '''
    }

    void testSimpleTypeTypeParamHint() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_simpletype[]
            import groovy.transform.stc.SimpleType
            public void doSomething(@ClosureParams(value=SimpleType,options=['java.lang.String','int']) Closure c) {
                c('foo',3)
            }
            doSomething { str, len ->
                assert str.length() == len
            }
            // end::typehint_simpletype[]
        '''
    }

    void testSimpleTypeTypeMapEntry() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_mapentry[]
            import groovy.transform.stc.MapEntryOrKeyValue
            public <K,V> void doSomething(Map<K,V> map, @ClosureParams(MapEntryOrKeyValue) Closure c) {
                // ...
            }
            doSomething([a: 'A']) { k,v ->
                assert k.toUpperCase() == v.toUpperCase()
            }
            doSomething([abc: 3]) { e ->
                assert e.key.length() == e.value
            }
            // end::typehint_mapentry[]
        '''
    }

    void testFromAbstractTypeHint() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_from_abstract_type[]
            import groovy.transform.stc.FromAbstractTypeMethods
            abstract class Foo {
                abstract void firstSignature(int x, int y)
                abstract void secondSignature(String str)
            }
            void doSomething(@ClosureParams(value=FromAbstractTypeMethods, options=["Foo"]) Closure cl) {
                // ...
            }
            doSomething { a, b -> a+b }
            doSomething { s -> s.toUpperCase() }
            // end::typehint_from_abstract_type[]
        '''
    }

    void testFromStringTypeHint() {
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_from_string_1[]
            import groovy.transform.stc.FromString
            void doSomething(@ClosureParams(value=FromString, options=["String","String,Integer"]) Closure cl) {
                // ...
            }
            doSomething { s -> s.toUpperCase() }
            doSomething { s,i -> s.toUpperCase()*i }
            // end::typehint_from_string_1[]
        '''
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_from_string_2[]
            import groovy.transform.stc.FromString
            void doSomething(@ClosureParams(value=FromString, options=["String","String,Integer"]) Closure cl) {
                // ...
            }
            doSomething { s -> s.toUpperCase() }
            doSomething { s,i -> s.toUpperCase()*i }
            // end::typehint_from_string_2[]
        '''
        assertScript '''import groovy.transform.stc.ClosureParams
            // tag::typehint_from_string_3[]
            import groovy.transform.stc.FromString
            public <T> void doSomething(T e, @ClosureParams(value=FromString, options=["T","T,T"]) Closure cl) {
                // ...
            }
            doSomething('foo') { s -> s.toUpperCase() }
            doSomething('foo') { s1,s2 -> assert s1.toUpperCase() == s2.toUpperCase() }
            // end::typehint_from_string_3[]
        '''

    }

}

