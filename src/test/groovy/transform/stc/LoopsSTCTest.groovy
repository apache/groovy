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

/**
 * Unit tests for static type checking : loops.
 */
class LoopsSTCTest extends StaticTypeCheckingTestCase {

    void testMethodCallInLoop() {
        for (type in ['int', 'def', 'var']) {
            assertScript """
                int foo(int x) { x+1 }
                $type x = 0
                for (int i=0;i<10;i++) {
                    x = foo(x)
                }
            """
        }
    }

    // GROOVY-5258
    void testMethodCallInLoopAndDefAndTwoFooMethods() {
        shouldFailWithMessages '''
            Date    foo(Integer x) { new Date() }
            Integer foo(Date    x) { 1 }
            def x = 0
            2.times {
                // there are two possible target methods; this is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''',
        'Cannot find matching method'
    }

    // GROOVY-5258
    void testMethodCallInLoopAndDefAndTwoFooMethods2() {
        shouldFailWithMessages '''
            Date    foo(Integer x) { new Date() }
            Integer foo(Date    x) { 1 }
            def x = 0
            for (int i=0;i<10;i++) {
                // there are two possible target methods; this is not a problem for STC, but it is for static compilation
                x = foo(x)
            }
        ''',
        'Cannot find matching method'
    }

    // GROOVY-5258
    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadType() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date   foo(Double  x) { new Date(x.longValue()) }
            def x = 0
            for (int i=0;i<10;i++) {
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo() -- there's no foo(Date)
                x = foo(x)
            }
        ''',
        'Cannot find matching method'
    }

    // GROOVY-5258
    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date   foo(Double  x) { new Date(x.longValue()) }
            def x = 0
            for (int i=0;i<10;i++) {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo() -- there's no foo(Date)
                x = y
            }
        ''',
        'Cannot find matching method'
    }

    // GROOVY-5258
    void testMethodCallInLoopAndDefAndTwoFooMethodsAndOneWithBadTypeAndIndirection2() {
        shouldFailWithMessages '''
            Double foo(Integer x) { x+1 }
            Date   foo(Double  x) { new Date(x.longValue()) }
            def x = 0
            2.times {
                def y = foo(x)
                // there are two possible target methods and one returns a type which is assigned to 'x'
                // then called in turn as a parameter of foo() -- there's no foo(Date)
                x = y
            }
        ''',
        'Cannot find matching method'
    }

    // GROOVY-5587
    void testForInLoopOnMap1() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
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

    // GROOVY-6240
    void testForInLoopOnMap2() {
        assertScript '''
            Map<String, Integer> map = [foo: 123, bar: 456]
            for (entry in map) {
                assert entry.key.reverse() in ['oof','rab']
                assert entry.value * 2 in [246, 912]
            }
        '''
    }

    void testForInLoopOnMap3() {
        assertScript '''
            class MyMap extends LinkedHashMap<String,Integer> {
            }
            def map = new MyMap([foo: 123, bar: 456])
            for (entry in map) {
                assert entry.key.reverse() in ['oof','rab']
                assert entry.value * 2 in [246, 912]
            }
        '''
    }

    // GROOVY-10179
    void testForInLoopOnMap4() {
        assertScript '''
            void test(args) {
                if (args instanceof Map) {
                    for (e in args) {
                        print "$e.key $e.value"
                    }
                }
            }
            test(a:1,b:2,c:3.14)
        '''
    }

    // GROOVY-8643
    void testForInLoopOnList() {
        assertScript '''
            List<String> list = null
            for (item in list) {
                item.toUpperCase()
            }
        '''
    }

    // GROOVY-8643
    void testForInLoopOnArray() {
        assertScript '''
            String[] strings = null
            for (string in strings) {
                string.toUpperCase()
            }
        '''
    }

    void testForInLoopOnArray2() {
        assertScript '''
            String[] strings = ['a','b','c']
            for (string in strings) {
                string.toUpperCase()
            }
        '''
    }

    // GROOVY-10579
    void testForInLoopOnArray3() {
        assertScript '''
            int[] numbers = [1,2,3,4,5]
            int sum = 0
            for (i in numbers) {
                sum += i
            }
            assert sum == 15
        '''
    }

    // GROOVY-10476
    void testForInLoopOnStream() {
        assertScript '''
            def list = []
            for (item in ['a','b','c'].stream()) {
                list.add(item.toUpperCase())
            }
            assert list == ['A','B','C']
        '''
    }

    // GROOVY-8882
    void testForInLoopOnString() {
        assertScript '''
            for (s in 'abc') assert s instanceof String
            for (String s in 'abc') assert s instanceof String
        '''
        assertScript '''
            for (char c in 'abc') assert c instanceof Character
            for (Character c in 'abc') assert c instanceof Character
        '''
    }

    // GROOVY-8487, GROOVY-10712
    void testForInLoopOnIterator() {
        assertScript '''
            def list = []
            for (item in ['a','b','c'].iterator()) {
                list.add(item.toUpperCase())
            }
            assert list == ['A','B','C']
        '''
    }

    // GROOVY-11335
    void testForInLoopOnCollection() {
        assertScript '''
            def whatever(Collection<String> coll) {
                if (coll instanceof Serializable) {
                    for (item in coll) {
                        return item.toLowerCase()
                    }
                }
            }
            assert whatever(['Works']) == 'works'
        '''
    }

    // GROOVY-6123
    void testForInLoopOnEnumeration() {
        assertScript '''
            Vector<String> v = new Vector<>()
            v.add('ooo')
            Enumeration<String> en = v.elements()
            for (e in en) {
                assert e.toUpperCase() == 'OOO'
            }
            v.add('groovy')
            en = v.elements()
            for (e in en) {
                assert e.toUpperCase() == 'OOO'
                break
            }

            en = v.elements()
            for (e in en) {
                assert e.toUpperCase() in ['OOO','GROOVY']
                if (e=='ooo') continue
            }

            en = null
            for (e in en) {
                e.toUpperCase()
            }
        '''
    }

    // GROOVY-10651
    void testForInLoopOnRawTypeIterable() {
        assertScript '''
            void test(groovy.transform.stc.TreeNode node) {
                for (child in node) {
                    test(child) // Cannot find matching method #test(java.lang.Object)
                }
            }
        '''
    }

    // GROOVY-10651
    void testForInLoopOnUnboundedIterable() {
        assertScript '''
            void test(groovy.transform.stc.TreeNode<?> node) {
                for (child in node) {
                    test(child) // Cannot find matching method #test(java.lang.Object)
                }
            }
        '''
    }

    void testShouldNotInferSoftReferenceAsElementType() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def loop = lookup('loop')[0]
                assert loop instanceof org.codehaus.groovy.ast.stmt.ForStatement
                def collectionType = loop.collectionExpression.getNodeMetaData(INFERRED_TYPE)
                assert collectionType == make(java.lang.reflect.Field).makeArray()
            })
            void test() {
                int i = 0
                loop:
                for (def field : String.class.declaredFields) {
                    i++
                }
                assert i > 0
            }
        '''
    }

    // GROOVY-5640
    void testShouldInferNodeElementTypeForIterableOfNodes() {
        assertScript '''
            class Node {
            }
            interface Traverser {
                Iterable<Node> nodes()
            }
            class MyTraverser implements Traverser {
                Iterable<Node> nodes() {
                    []
                }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def loop = lookup('loop')[0]
                assert loop instanceof org.codehaus.groovy.ast.stmt.ForStatement
                def collectionType = loop.collectionExpression.getNodeMetaData(INFERRED_TYPE)
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
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def loop = lookup('loop')[0]
                assert loop instanceof org.codehaus.groovy.ast.stmt.ForStatement
                def collectionType = loop.collectionExpression.getNodeMetaData(INFERRED_TYPE)
                assert collectionType == make(IntRange)
            })
            void test() {
                int[] ints = new int[10]
                loop:
                for (i in 0..<10) {
                  assert ints[i-0] == 0
                }
            }
        '''
    }
}
