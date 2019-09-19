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

import groovy.test.GroovyTestCase

class CategoryAnnotationTest extends GroovyTestCase {
    void testTransformationOfPropertyInvokedOnThis() {
        //Test the fix for GROOVY-3367
        assertScript """
            @Category(Distance3367)
            class DistanceCategory3367 {
                Distance3367 plus(Distance3367 increment) {
                    new Distance3367(number: this.number + increment.number)
                }
            }
    
            class Distance3367 {
                def number
            }
    
            use(DistanceCategory3367) {
                def d1 = new Distance3367(number: 5)
                def d2 = new Distance3367(number: 10)
                def d3 = d1 + d2
                assert d3.number == 15
            }
        """
    }

    void testTransformationWithCatchClause() {
        //Test the fix for GROOVY-4801
        assertScript """
            class ExceptionHandler {
                static def handled(Object self, Closure block) {
                    try { block.call() }
                    catch (Throwable t) { t.message }
                }
            }

            @Mixin(ExceptionHandler)
            class Caller {
                def thrower() { handled { 1/0 } }
            }

            assert new Caller().thrower() == 'Division by zero'
        """
    }

    void testCategoryMethodsHavingDeclarationStatements() {
        // GROOVY-3543: Declaration statements in category class' methods were not being visited by 
        // CategoryASTTransformation's expressionTransformer resulting in declaration variables not being 
        // defined on varStack resulting in compilation errors later
        assertScript """
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
    
            interface Test { 
                String getName() 
            }
    
            class MyTest implements Test {
                String getName() {
                    return "Pre-"
                }
            }
    
            def onetest = new MyTest()
            use(TestCategory) { 
                assert onetest.getSuperName1() == "hi from category"
                assert onetest.getSuperName2() == "Pre-01234-Post"
                assert onetest.getSuperName3() == "Pre-01234-Post"
            }
        """
    }

    void testPropertyNameExpandingToGetterInsideCategoryMethod() {
        //GROOVY-3543: Inside the category method, this.getType().name was failing but this.getType().getName() was not.
        assertScript """
            @Category(Guy)
            class Naming {
                String getTypeName() {
                    if(this.getType() != null)
                        this.getType().name
                    else
                        ""
                }
            }
            
            interface Guy {
                Type getType()
            }
            
            class Type {
                String name
            }
            
            class MyGuyver implements Guy {
                Type type
            }
            
            def atype = new Type(name: 'String')
            def onetest = new MyGuyver(type:atype)
            
            use(Naming) {
                assert onetest.getTypeName() == onetest.getType().getName()
            }
        """
    }

    void testClosureUsingThis() {
        assertScript """
            @Category(Guy)
            class Filtering {
                List process() {
                    this.messages.findAll{it.name != this.getName()}
                }
            }
            
            interface Guy {
                String getName()
                List getMessages()
            }
            
            class MyGuyver implements Guy {
                List messages
                String name
            }
            
            def onetest = new MyGuyver(
                    name: 'coucou',
                    messages : [['name':'coucou'], ['name':'test'], ['name':'salut']])
            
            Guy.mixin   Filtering
            
            assert onetest.process() == onetest.messages.findAll{it.name != onetest.getName()}        
        """
    }

    void testClosureWithinDeclarationExpressionAndMultipleAssignment() {
        // GROOVY-4546
        assertScript """
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
        """
    }

    // GROOVY-6120
    void testFieldShouldNotBeAllowedInCategory() {
        def message = shouldFail(RuntimeException) {
            assertScript '''
            @Mixin(Foo)
            class Bar {  }

            @Category(Bar)
            class Foo {
                public x = 5
                def foo() {
                    x
                }
            }

            assert new Bar().foo() == 5
            '''
        }
        assert message.contains('The @Category transformation does not support instance fields')
    }

    // GROOVY-6120
    void testPropertyShouldNotBeAllowedInCategory() {
        def message = shouldFail(RuntimeException) {
            assertScript '''
            @Mixin(Foo)
            class Bar {  }

            @Category(Bar)
            class Foo {
                int x = 5
                def foo() {
                    x
                }
            }

            assert new Bar().foo() == 5
            '''
        }
        assert message.contains('The @Category transformation does not support instance properties')
    }

    // GROOVY-6120
    void testShouldNotThrowVerifyError() {
        assertScript '''
            @Mixin(Foo)
            class Bar { int x = 5 }

            @Category(Bar)
            class Foo {
                def foo() {
                    x
                }
            }

            assert new Bar().foo() == 5
        '''
    }

    // GROOVY-6120
    void testCategoryShouldBeCompatibleWithCompileStatic() {
        assertScript '''
            @Mixin(Foo)
            class Bar { int x = 5 }

            @Category(Bar)
            @groovy.transform.CompileStatic
            class Foo {
                def foo() {
                    x
                }
            }

            assert new Bar().foo() == 5
        '''
    }

    void testCategoryShouldBeCompatibleWithCompileStatic_GROOVY6917() {
        assertScript '''
            @groovy.transform.CompileStatic
            @Category(Integer)
            class IntegerCategory {
                Integer twice() { this * 2 }
                List<Integer> multiplesUpTo(Integer num) {
                    (2..num).collect{ j -> this * j }
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

