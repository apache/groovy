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
 * Unit tests for static type checking : arrays and collections.
 */
class ArraysAndCollectionsSTCTest extends StaticTypeCheckingTestCase {

    void testArrayAccess() {
        assertScript '''
            String[] strings = ['a','b','c']
            String str = strings[0]
        '''
    }

    void testArrayElementReturnType() {
        shouldFailWithMessages '''
            String[] strings = ['a','b','c']
            int str = strings[0]
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testWrongComponentTypeInArray() {
        shouldFailWithMessages '''
            int[] intArray = ['a']
        ''', 'Cannot assign value of type java.lang.String into array of type int[]'
    }

    void testAssignValueInArrayWithCorrectType() {
        assertScript '''
            int[] arr2 = [1, 2, 3]
            arr2[1] = 4
        '''
    }

    void testAssignValueInArrayWithWrongType() {
        shouldFailWithMessages '''
            int[] arr2 = [1, 2, 3]
            arr2[1] = "One"
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testBidimensionalArray() {
        assertScript '''
            int[][] arr2 = new int[1][]
            arr2[0] = [1,2]
        '''
    }

    void testBidimensionalArrayWithInitializer() {
        shouldFailWithMessages '''
            int[][] arr2 = new Object[1][]
        ''', 'Cannot assign value of type java.lang.Object[][] to variable of type int[][]'
    }

    void testBidimensionalArrayWithWrongSubArrayType() {
        shouldFailWithMessages '''
            int[][] arr2 = new int[1][]
            arr2[0] = ['1']
        ''', 'Cannot assign value of type java.lang.String into array of type int[]'
    }

    void testForLoopWithArrayAndUntypedVariable() {
        assertScript '''
            String[] arr = ['1','2','3']
            for (i in arr) { }
        '''
    }

    void testForLoopWithArrayAndWrongVariableType() {
        shouldFailWithMessages '''
            String[] arr = ['1','2','3']
            for (int i in arr) { }
        ''', 'Cannot loop with element of type int with collection of type java.lang.String[]'
    }
    void testJava5StyleForLoopWithArray() {
        assertScript '''
            String[] arr = ['1','2','3']
            for (String i : arr) { }
        '''
    }

    void testJava5StyleForLoopWithArrayAndIncompatibleType() {
        shouldFailWithMessages '''
            String[] arr = ['1','2','3']
            for (int i : arr) { }
        ''', 'Cannot loop with element of type int with collection of type java.lang.String[]'
    }

    void testForEachLoopOnString() {
        assertScript '''
            String name = 'Guillaume'
            for (String s in name) {
                println s
            }
        '''
    }

    void testSliceInference() {
        assertScript '''
        List<String> foos = ['aa','bb','cc']
        foos[0].substring(1)
        def bars = foos[0..1]
        println bars[0].substring(1)
        '''

        assertScript '''
        def foos = ['aa','bb','cc']
        foos[0].substring(1)
        def bars = foos[0..1]
        println bars[0].substring(1)
        '''

        // GROOVY-5608
        assertScript '''
            List<Integer> a = [1, 3, 5]

            @ASTTest(phase = INSTRUCTION_SELECTION, value = {
                def type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(List)
                assert type.genericsTypes.length == 1
                assert type.genericsTypes[0].type == Integer_TYPE
            })
            List<Integer> b = a[1..2]

            List<Integer> c = (List<Integer>)a[1..2]
         '''

        // check that it also works for custom getAt methods
        assertScript '''
            class SpecialCollection {
                List<Date> getAt(IntRange irange) {
                    return [new Date(), new Date()+1]
                }
            }

            def sc = new SpecialCollection()

            @ASTTest(phase = INSTRUCTION_SELECTION, value = {
                def type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(List)
                assert type.genericsTypes.length == 1
                assert type.genericsTypes[0].type == make(Date)
            })
            List<Date> dates = sc[1..3]
        '''
    }

    void testListStarProperty() {
        assertScript '''
            List list = ['a','b','c']
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def iType = node.getNodeMetaData(INFERRED_TYPE)
                assert iType == make(List)
                assert iType.isUsingGenerics()
                assert iType.genericsTypes[0].type == CLASS_Type
            })
            List classes = list*.class
            assert classes == [String,String,String]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == CLASS_Type
            })
            def listClass = list.class
            assert listClass == ArrayList
        '''
    }

    void testListStarMethod() {
        assertScript '''
            List list = ['a','b','c']
            List classes = list*.toUpperCase()
            assert classes == ['A','B','C']
        '''
    }

    void testInferredMapDotProperty() {
        assertScript '''
            def map = [:]
            map['a'] = 1
            map.b = 2
        '''
    }

    void testInlineMap() {

        assertScript '''
            Map map = [a:1, b:2]
        '''

        assertScript '''
            def map = [a:1, b:2]
            map = [b:2, c:3]
        '''

        assertScript '''
            Map map = ['a':1, 'b':2]
        '''
    }

    void testAmbiguousCallWithVargs() {
        assertScript '''
            int sum(int x) { 1 }
            int sum(int... args) {
                0
            }
            assert sum(1) == 1
        '''
    }

    void testAmbiguousCallWithVargs2() {
        assertScript '''
            int sum(int x) { 1 }
            int sum(int y, int... args) {
                0
            }
            assert sum(1) == 1
        '''
    }

    void testCollectMethodCallOnList() {
        assertScript '''
            [1,2,3].collect { it.toString() }
        '''
    }

    void testForInLoop() {
        assertScript '''
            class A {
                String name = 'foo'
            }
            List<A> myList = [new A(name:'Cedric'), new A(name:'Yakari')] as LinkedList<A>
            for (element in myList) {
                element.name.toUpperCase()
            }
        '''
    }

    void testForInLoopWithDefaultListType() {
        assertScript '''
            class A {
                String name = 'foo'
            }
            List<A> myList = [new A(name:'Cedric'), new A(name:'Yakari')]
            for (element in myList) {
                element.name.toUpperCase()
            }
        '''
    }

    void testForInLoopWithRange() {
        assertScript '''
            for (int i in 1..10) { i*2 }
        '''
    }

    void testForInLoopWithRangeUsingVariable() {
        assertScript '''
            int n = 10
            for (int i in 1..n) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                    assert node.getNodeMetaData(DECLARATION_INFERRED_TYPE) == int_TYPE
                })
                def k = i
            }
        '''
    }

    void testForInLoopWithRangeUsingComputedBound() {
        assertScript '''
            int n = 10
            for (int i in 1..(n-1)) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                    assert node.getNodeMetaData(DECLARATION_INFERRED_TYPE) == int_TYPE
                })
                def k = i
            }
        '''
    }

    void testForInLoopWithRangeUsingListOfInts() {
        assertScript '''
            int n = 10
            for (int i in [-1,1]) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                    assert node.getNodeMetaData(DECLARATION_INFERRED_TYPE) == int_TYPE
                })
                def k = i
            }
        '''
    }

    // GROOVY-5177
    void testShouldNotAllowArrayAssignment() {
        shouldFailWithMessages '''
            class Foo {
                def say() {
                    FooAnother foo1 = new Foo[13] // but FooAnother foo1 = new Foo() reports a STC                        Error
                }
            }
            class FooAnother {

            }
        ''', 'Cannot assign value of type Foo[] to variable of type FooAnother'
    }

    void testListPlusEquals() {
        assertScript '''
            List<String> list = ['a','b']
            list += ['c']
            assert list == ['a','b','c']
        '''

        assertScript '''
            Collection<String> list = ['a','b']
            list += 'c'
            assert list == ['a','b','c']
        '''
    }
    
    void testObjectArrayGet() {
        assertScript '''
            Object[] arr = [new Object()]
            arr.getAt(0)
        '''
    }

    void testStringArrayGet() {
        assertScript '''
            String[] arr = ['abc']
            arr.getAt(0)
        '''
    }

    void testObjectArrayPut() {
        assertScript '''
            Object[] arr = [new Object()]
            arr.putAt(0, new Object())
        '''
    }

    void testObjectArrayPutWithNull() {
        assertScript '''
            Object[] arr = [new Object()]
            arr.putAt(0, null)
        '''
    }

    void testStringArrayPut() {
        assertScript '''
            String[] arr = ['abc']
            arr.putAt(0, 'def')
        '''
    }

    void testStringArrayPutWithNull() {
        assertScript '''
            String[] arr = ['abc']
            arr.putAt(0, null)
        '''
    }

    void testStringArrayPutWithWrongType() {
        shouldFailWithMessages '''
            String[] arr = ['abc']
            arr.putAt(0, new Object())
        ''', 'Cannot call <T,U extends T> java.lang.String[]#putAt(int, U) with arguments [int, java.lang.Object]'
    }

    void testStringArrayPutWithSubType() {
        assertScript '''
            Serializable[] arr = ['abc']
            arr.putAt(0, new Integer(1))
        '''
    }

    void testStringArrayPutWithSubTypeAsPrimitive() {
        assertScript '''
            Serializable[] arr = ['abc']
            arr.putAt(0, 1)
        '''
    }

    void testStringArrayPutWithIncorrectSubType() {
        shouldFailWithMessages '''
            Serializable[] arr = ['abc']
            arr.putAt(0, new XmlSlurper())
        ''', 'Cannot call <T,U extends T> java.io.Serializable[]#putAt(int, U) with arguments [int, groovy.util.XmlSlurper]'
    }

    void testArrayGetOnPrimitiveArray() {
        assertScript '''
            int m() {
                int[] arr = [1,2,3]
                arr.getAt(1)
            }
            assert m()==2
        '''
    }

    void testReturnTypeOfArrayGet() {
        assertScript '''
            Serializable m() {
                String[] arr = ['1','2','3']
                arr.getAt(1)
            }
            assert m()=='2'
        '''
    }

    void testInferredTypeWithListAndFind() {
        assertScript '''List<Integer> list = [ 1, 2, 3, 4 ]

        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
        })
        Integer j = org.codehaus.groovy.runtime.DefaultGroovyMethods.find(list) { int it -> it%2 == 0 }

        @ASTTest(phase=INSTRUCTION_SELECTION, value= {
            assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
        })
        Integer i = list.find { int it -> it % 2 == 0 }
        '''
    }

    // GROOVY-5573
    void testArrayNewInstance() {
        assertScript '''import java.lang.reflect.Array
            @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                assert node.rightExpression.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
            })
            def object = Array.newInstance(Integer.class, 10)
            Object[] joinedArray = (Object[]) object
            assert joinedArray.length == 10
        '''
    }

    // GROOVY-5683
    void testArrayLengthOnMultidimensionalArray() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE.makeArray().makeArray()
            })
            int[][] array = [[1]] as int[][]
            array[0].length
        '''
    }

    // GROOVY-5797
    void testShouldAllowExpressionAsMapPropertyKey() {
        assertScript '''
        def m( Map param ) {
          def map = [ tim:4 ]
          map[ param.key ]
        }

        assert m( [ key: 'tim' ] ) == 4
        '''
    }

    // GROOVY-5793
    void testShouldNotForceAsTypeWhenListOfNullAssignedToArray() {
        assertScript '''
        Integer[] m() {
          Integer[] arr = [ null, null ]
        }
        assert m().length == 2
        '''
    }
    void testShouldNotForceAsTypeWhenListOfNullAssignedToArrayUnlessPrimitive() {
        shouldFailWithMessages '''
        int[] m() {
          int[] arr = [ null, null ]
        }
        ''', 'into array of type'
    }

    // GROOVY-6131
    void testArraySetShouldGenerateBytecode() {
        shouldFailWithMessages '''
            void addToCollection(Collection coll, int index, val) {
                coll[index] = val
            }
            def list = ['a']
            addToCollection(list, 0, 'b')
            assert list == ['b']
        ''', 'Cannot find matching method java.util.Collection#putAt(int, java.lang.Object)'
    }

    // GROOVY-6266
    void testMapKeyGenerics() {
        assertScript """
            HashMap<String,List<List>> AR=new HashMap<String,List<List>>()
            AR.get('key',[['val1'],['val2']])
            assert AR.'key'[0] == ['val1']
        """
    }
    
    // GROOVY-6311
    void testSetSpread() {
        assertScript """
            class Inner {Set<String> strings}
            class Outer {Set<Inner> inners}
            Outer outer = new Outer(inners: [ new Inner(strings: ['abc', 'def'] as Set), new Inner(strings: ['ghi'] as Set) ] as Set)
            def res = outer.inners*.strings
            assert res[1].contains('ghi')
            assert res[0].contains('abc')
            assert res[0].contains('def')
        """
    }
    
    // GROOVY-6241
    void testAsImmutable() {
        assertScript """
            List<Integer> list = [1, 2, 3]
            List<Integer> immutableList = [1, 2, 3].asImmutable()
            Map<String, Integer> map = [foo: 123, bar: 456]
            Map<String, Integer> immutableMap = [foo: 123, bar: 456].asImmutable()
        """
    }
    
    // GROOVY-6350
    void testListPlusList() {
        assertScript """
            def foo = [] + []
            assert foo==[]
        """
    }

    // GROOVY-7122
    void testIterableLoop() {
        assertScript '''
            int countIt(Iterable<Integer> list) {
                int count = 0
                for (Integer obj : list) {count ++}
                return count
            }
            countIt([1,2,3])==3
        '''
    }

    // GROOVY-8033
    void testSetSpreadPropertyInStaticContext() {
        assertScript '''
            class Foo {
                String name
            }
            static List<String> meth() {
                Set<Foo> foos = [new Foo(name: 'pls'), new Foo(name: 'bar')].toSet()
                foos*.name
            }
            assert meth().toSet() == ['pls', 'bar'].toSet()            
        '''
    }
}

