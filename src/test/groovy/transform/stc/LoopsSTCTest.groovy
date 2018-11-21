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
package groovy.transform.stc

import groovy.transform.CompileStatic

/**
 * Unit tests for static type checking : loops.
 *
 * @author Cedric Champeau
 */
class LoopsSTCTest extends StaticTypeCheckingTestCase {

    void testMethodCallInLoop() {
        assertScript '''
            int foo(int x) { x+1 }
            int x = 0
            for (int i=0;i<10;i++) {
                x = foo(x)
            }
        '''
    }

    void testMethodCallInLoopAndDef() {
        assertScript '''
            int foo(int x) { x+1 }
            def x = 0
            for (int i=0;i<10;i++) {
                x = foo(x)
            }
        '''
    }

    // GROOVY-8882
    void testStringCollectionLoop() {
        for (char c in 'abc') assert c instanceof Character
        for (Character c in 'abc') assert c instanceof Character
        for (String s in 'abc') assert s instanceof String
        for (s in 'abc') assert s instanceof String
    }

    // GROOVY-8882
    @CompileStatic
    void testStringCollectionLoopCS() {
        for (char c in 'abc') assert c instanceof Character
        for (Character c in 'abc') assert c instanceof Character
        for (String s in 'abc') assert s instanceof String
        for (s in 'abc') assert s instanceof String
    }

    void testMethodCallWithEachAndDefAndTwoFooMethods() {
        shouldFailWithMessages '''
            Date foo(Integer x) { new Date() }
            Integer foo(Date x) { 1 }
            def x = 0
            10.times {
                 // there are two possible target methods. This is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethods() {
        shouldFailWithMessages '''
            Date foo(Integer x) { new Date() }
            Integer foo(Date x) { 1 }
            def x = 0
            for (int i=0;i<10;i++) {
                 // there are two possible target methods. This is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadType() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            for (int i=0;i<10;i++) {
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = foo(x)
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            for (int i=0;i<10;i++) {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = y
            }
        ''', 'Cannot find matching method'
    }

    void testMethodCallWithEachAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date foo(Double x) { new Date((long)x) }
            def x = 0
            10.times {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo(). There's no #foo(Date)
                x = y
            }
        ''', 'Cannot find matching method'
    }

    // GROOVY-5587
    void testMapEntryInForInLoop() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                lookup('forLoop').each {
                    assert it instanceof org.codehaus.groovy.ast.stmt.ForStatement
                    def collection = it.collectionExpression // MethodCallExpression
                    def inft = collection.getNodeMetaData(INFERRED_TYPE)
                    assert inft == make(Set)
                    def entryInft = inft.genericsTypes[0].type
                    assert entryInft == make(Map.Entry)
                    assert entryInft.genericsTypes[0].type == STRING_TYPE
                    assert entryInft.genericsTypes[1].type == Integer_TYPE
                }
            })
            void test() {
                def result = ""
                def sum = 0
                forLoop:
                for ( Map.Entry<String, Integer> it in [a:1, b:3].entrySet() ) {
                   result += it.getKey()
                   sum += it.getValue()
                }
                assert result == "ab"
                assert sum == 4
            }
            test()
        '''
    }

    void testShouldNotInferSoftReferenceAsComponentType() {
        assertScript '''import java.lang.reflect.Field
            import org.codehaus.groovy.ast.stmt.ForStatement

            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                def FIELD_ARRAY = make(Field).makeArray()
                def forStmt = lookup('myLoop')[0]
                assert forStmt instanceof ForStatement
                def collectionType = forStmt.collectionExpression.getNodeMetaData(INFERRED_TYPE)
                assert collectionType == FIELD_ARRAY
            })
            void forInTest() {
                int i = 0;
                myLoop:
                for (def field : String.class.declaredFields) {
                    i++;
                }
                assert i > 0
            }
        '''
    }

    // GROOVY-5640
    void testShouldInferComponentTypeAsIterableOfNodes() {
        assertScript '''import org.codehaus.groovy.ast.stmt.ForStatement
        class Node {}

        interface Traverser {
            Iterable<Node> nodes()
        }

        class MyTraverser implements Traverser {

            Iterable<Node> nodes() {
                []
            }
        }

        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            def forStmt = lookup('loop')[0]
            assert forStmt instanceof ForStatement
            def collectionType = forStmt.collectionExpression.getNodeMetaData(INFERRED_TYPE)
            assert collectionType == make(Iterable)
            assert collectionType.isUsingGenerics()
            assert collectionType.genericsTypes.length == 1
            assert collectionType.genericsTypes[0].type.name == 'Node'
        })
        void test() {
            loop:
            for (def node : new MyTraverser().nodes()) {
                println node.class.name
            }
        }

        '''
    }

    // GROOVY-5641
    void testShouldInferLoopElementTypeWithUndeclaredType() {
        assertScript '''import org.codehaus.groovy.ast.stmt.ForStatement
        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            def forStmt = lookup('loop')[0]
            assert forStmt instanceof ForStatement
            def collectionType = forStmt.collectionExpression.getNodeMetaData(INFERRED_TYPE)
            assert collectionType == make(IntRange)
        })
        void foo() {
            int[] perm = new int[10]
            loop:
            for (i in 0..<10) {
              assert perm[i-0] == 0
            }
        }
        '''
    }
}

