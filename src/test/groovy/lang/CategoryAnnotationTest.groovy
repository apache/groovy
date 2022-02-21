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
package groovy.lang

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class CategoryAnnotationTest {

    @Test // GROOVY-3367
    void testTransformationOfPropertyInvokedOnThis() {
        assertScript '''
            class Distance {
                def number
            }
            @Category(Distance)
            class DistanceCategory {
                Distance plus(Distance increment) {
                    new Distance(number: this.number + increment.number)
                }
            }

            use(DistanceCategory) {
                def d1 = new Distance(number: 5)
                def d2 = new Distance(number: 10)
                def d3 = d1 + d2
                assert d3.number == 15
            }
        '''
    }

    @Test // GROOVY-3543
    void testCategoryMethodsHavingDeclarationStatements() {
        // Declaration statements in category class' methods were not being visited by
        // CategoryASTTransformation's expressionTransformer resulting in declaration
        // variables not being defined on varStack resulting in compilation errors later
        assertScript '''
            interface Test {
                String getName()
            }
            class MyTest implements Test {
                String getName() {
                    return "Pre-"
                }
            }
            @Category(Test)
            class TestCategory {
                String getSuperName1() {
                    String myname = ""
                    return myname + "hi from category"
                }
                // 2nd test case of JIRA
                String getSuperName2() {
                    String myname = this.getName()
                    for(int i = 0; i < 5; i++) myname += i
                    return myname + "-Post"
                }
                // 3rd test case of JIRA
                String getSuperName3() {
                    String myname = this.getName()
                    for(i in 0..4) myname += i
                    return myname + "-Post"
                }
            }

            def test = new MyTest()
            use(TestCategory) {
                assert test.getSuperName1() == "hi from category"
                assert test.getSuperName2() == "Pre-01234-Post"
                assert test.getSuperName3() == "Pre-01234-Post"
            }
        '''
    }

    @Test // GROOVY-3543
    void testPropertyNameExpandingToGetterInsideCategoryMethod() {
        // Inside the category method, this.getType().name was failing but this.getType().getName() was not.
        assertScript '''
            class Type {
                String name
            }
            interface Guy {
                Type getType()
            }
            class McGuyver implements Guy {
                Type type
            }
            @Category(Guy)
            class Naming {
                String getTypeName() {
                    if(this.getType() != null)
                        this.getType().name
                    else
                        ""
                }
            }

            def atype = new Type(name: 'String')
            def onetest = new McGuyver(type:atype)

            use(Naming) {
                assert onetest.getTypeName() == onetest.getType().getName()
            }
        '''
    }

    @Test
    void testClosureUsingThis() {
        assertScript '''
            interface Guy {
                String getName()
                List getMessages()
            }
            class McGuyver implements Guy {
                List messages
                String name
            }
            @Category(Guy)
            class Filtering {
                List process() {
                    this.messages.findAll{ it.name != this.getName() }
                }
            }

            def onetest = new McGuyver(name: 'coucou',
                messages: [['name':'coucou'], ['name':'test'], ['name':'salut']]
            )

            Guy.mixin(Filtering)

            assert onetest.process() == onetest.messages.findAll{ it.name != onetest.getName() }
        '''
    }

    @Test // GROOVY-6510
    void testClosureUsingImplicitThis() {
        assertScript '''
            @Category(Number)
            class NumberCategory {
                def foo() {
                    def bar = { ->
                        baz() // do not want "$this.baz()"
                    }
                    bar.resolveStrategy = Closure.DELEGATE_FIRST
                    bar.delegate = new NumberDelegate(this)
                    bar.call()
                }
            }

            class NumberDelegate {
                private final Number n
                NumberDelegate(Number n) { this.n = n }
                String baz() { 'number ' + n.intValue() }
            }

            use(NumberCategory) {
                String result = 1.foo()
                assert result == 'number 1'
            }
        '''

        assertScript '''
            @Category(Number)
            class NumberCategory {
                def foo() {
                    def bar = { ->
                        baz // do not want "$this.baz"
                    }
                    bar.resolveStrategy = Closure.DELEGATE_FIRST
                    bar.delegate = new NumberDelegate(this)
                    bar.call()
                }
            }

            class NumberDelegate {
                private final Number n
                NumberDelegate(Number n) { this.n = n }
                String getBaz() { 'number ' + n.intValue() }
            }

            use(NumberCategory) {
                String result = 1.foo()
                assert result == 'number 1'
            }
        '''
    }

    @Test // GROOVY-4546
    void testClosureWithinDeclarationExpressionAndMultipleAssignment() {
        assertScript '''
            @Category(Integer)
            class MyOps {
                def multiplesUpTo4() { [this * 2, this * 3, this * 4] }
                def multiplesUpTo(num) {
                    (2..num).collect{ j -> this * j }
                }
                def alsoMultiplesUpTo(num) {
                    def ans = (2..num).collect{ i -> this * i }
                    ans
                }
                def twice() {
                    def (twice, thrice, quad) = multiplesUpTo4()
                    twice
                }
            }

            use(MyOps) {
                assert 5.multiplesUpTo4() == [10, 15, 20]
                assert 5.multiplesUpTo(6) == [10, 15, 20, 25, 30]
                assert 5.alsoMultiplesUpTo(6) == [10, 15, 20, 25, 30]
                assert 5.twice() == 10
            }
        '''
    }

    @Test // GROOVY-6120
    void testFieldShouldNotBeAllowedInCategory() {
        def err = shouldFail RuntimeException, '''
            @Category(Bar)
            class Foo {
                public x = 5
                def foo() {
                    x
                }
            }
            @Mixin(Foo)
            class Bar {
            }

            assert new Bar().foo() == 5
        '''
        assert err =~ /The @Category transformation does not support instance fields/
    }

    @Test // GROOVY-6120
    void testPropertyShouldNotBeAllowedInCategory() {
        def err = shouldFail RuntimeException, '''
            @Category(Bar)
            class Foo {
                int x = 5
                def foo() {
                    x
                }
            }
            @Mixin(Foo)
            class Bar {
            }

            assert new Bar().foo() == 5
        '''
        assert err =~ /The @Category transformation does not support instance properties/
    }

    @Test // GROOVY-6120
    void testShouldNotThrowVerifyError() {
        assertScript '''
            @Category(Bar)
            class Foo {
                def foo() {
                    x
                }
            }
            @Mixin(Foo)
            class Bar {
                int x = 5
            }

            assert new Bar().foo() == 5
        '''
    }

    @Test // GROOVY-6120
    void testCategoryShouldBeCompatibleWithCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            @Category(Bar)
            class Foo {
                def foo() {
                    x
                }
            }
            @Mixin(Foo)
            class Bar {
                int x = 5
            }

            assert new Bar().foo() == 5
        '''
    }

    @Test // GROOVY-6917
    void testCategoryShouldBeCompatibleWithCompileStatic2() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            @Category(Integer)
            class IntegerCategory {
                Integer twice() {
                    this * 2
                }
                List<Integer> multiplesUpTo(Integer num) {
                    (2..num).collect{ n -> this * n }
                }
                List<Integer> multiplyAll(List<Integer> nums) {
                    nums.collect{ it * this }
                }
            }

            use(IntegerCategory) {
                assert 7.twice() == 14
                assert 7.multiplesUpTo(4) == [14, 21, 28]
                assert 7.multiplyAll([1, 3, 5]) == [7, 21, 35]
            }
        '''
    }
}
