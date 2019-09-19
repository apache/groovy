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
package groovy

import groovy.test.GroovyTestCase
import groovy.transform.NamedParam
import groovy.transform.NamedParams
import groovy.transform.TypeChecked

import static groovy.NamedParameterHelper.myJavaMethod

class NamedParameterTest extends GroovyTestCase {

    void testPassingNamedParametersToMethod() {
        someMethod(name:"gromit", eating:"nice cheese", times:2)
    }
    
    protected void someMethod(args) {
        assert args.name == "gromit"
        assert args.eating == "nice cheese"
        assert args.times == 2
        assert args.size() == 3
    }

    void testNamedParameterSpreadOnSeveralLines() {
        someMethod( name:
                    "gromit",
            eating:
                    "nice cheese",
            times:
                    2)
    }

    void testNamedParameterSpreadOnSeveralLinesWithCommandExpressions() {
        someMethod name:
                    "gromit",
            eating:
                    "nice cheese",
            times:
                    2
    }

    @TypeChecked
    void testNamedParamsAnnotation() {
        assert myJavaMethod(foo: 'FOO', bar: 'BAR') == 'foo = FOO, bar = BAR'
        assert myJavaMethod(bar: 'BAR') == 'foo = null, bar = BAR'
        assert myJavaMethod(foo: 'FOO', bar: 25, 42) == 'foo = FOO, bar = 25, num = 42'
        assert myJavaMethod(foo: 'FOO', 142) == 'foo = FOO, bar = null, num = 142'
        assert myMethod(foo: 'FOO', bar: 'BAR') == 'foo = FOO, bar = BAR'
        assert myMethod(bar: 'BAR') == 'foo = null, bar = BAR'
        assert myMethod(foo: 'FOO', bar: 35,242) == 'foo = FOO, bar = 35, num = 242'
        assert myMethod(foo: 'FOO', 342) == 'foo = FOO, bar = null, num = 342'
        assertScript '''
            import groovy.transform.TypeChecked
            import static groovy.NamedParameterTest.myMethod

            @TypeChecked
            def method() {            
                assert myMethod(foo: 'FOO', bar: 'BAR') == 'foo = FOO, bar = BAR'
                assert myMethod(bar: 'BAR') == 'foo = null, bar = BAR'
                assert myMethod(foo: 'FOO', bar: 45, 442) == 'foo = FOO, bar = 45, num = 442'
                assert myMethod(foo: 'FOO', 542) == 'foo = FOO, bar = null, num = 542'
            }
            method()
        '''
    }

    void testMissingRequiredName() {
        def message = shouldFail '''
            import groovy.transform.TypeChecked
            import static groovy.NamedParameterTest.myMethod

            @TypeChecked
            def method() {            
                myMethod(foo: 'FOO')
            }
            method()
        '''
        assert message.contains("required named arg 'bar' not found")
    }

    void testUnknownName() {
        def message = shouldFail '''
            import groovy.transform.TypeChecked
            import static groovy.NamedParameterTest.myMethod

            @TypeChecked
            def method() {            
                myMethod(bar: 'BAR', baz: 'BAZ')
            }
            method()
        '''
        assert message.contains("unexpected named arg: baz")
    }

    void testInvalidType() {
        def message = shouldFail '''
            import groovy.transform.TypeChecked
            import static groovy.NamedParameterTest.myMethod

            @TypeChecked
            def method() {            
                myMethod(foo: 42, 42)
            }
            method()
        '''
        assert message.contains("parameter for named arg 'foo' has type 'int' but expected 'java.lang.String'")
    }

    static String myMethod(@NamedParams([
            @NamedParam(value = "foo"),
            @NamedParam(value = "bar", type = String, required = true)
    ]) Map params) {
        "foo = $params.foo, bar = $params.bar"
    }

    static String myMethod(@NamedParams([
            @NamedParam(value = "foo", type = String, required = true),
            @NamedParam(value = "bar", type = Integer)
    ]) Map params, int num) {
        "foo = $params.foo, bar = $params.bar, num = $num"
    }
}
