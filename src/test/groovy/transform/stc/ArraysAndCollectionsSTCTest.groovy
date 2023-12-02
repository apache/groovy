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

import groovy.test.NotYetImplemented

/**
 * Unit tests for static type checking : arrays and collections.
 */
class ArraysAndCollectionsSTCTest extends StaticTypeCheckingTestCase {

    void testArrayAccess() {
        assertScript '''
            String[] strings = ['a','b','c']
            String str = strings[0]
            assert str == 'a'
        '''
    }

    void testArrayElementTypeInference() {
        shouldFailWithMessages '''
            String[] strings = ['a','b','c']
            int i = strings[0]
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testWrongComponentTypeInArray() {
        shouldFailWithMessages '''
            int[] intArray = ['a']
        ''',
        'Cannot assign value of type java.lang.String into array of type int[]'
    }

    // GROOVY-9985, GROOVY-9994
    void testWrongComponentTypeInArrayInitializer() {
        shouldFailWithMessages '''
            new int['a']
        ''',
        'Cannot convert from java.lang.String to int'

        shouldFailWithMessages '''
            new int[]{'a'}
        ''',
        'Cannot convert from java.lang.String to int'

        shouldFailWithMessages '''
            new Integer[]{new Object(),1}
        ''',
        'Cannot convert from java.lang.Object to java.lang.Integer'
    }

    // GROOVY-10111
    void testBoundedComponentTypeInArrayInitializer() {
        assertScript '''
            class C<X, Y> {
            }
            def <X extends C<Number, String>> X[] m() {
                new X[]{new C<Number, String>()}
            }
        '''
    }

    void testConvertibleTypesInArrayInitializer() {
        assertScript '''
            def strings = new String[]{1,(long)2,(short)3}
            assert strings.every { it.class == String }
            assert strings.toString() == '[1, 2, 3]'
        '''
    }

    void testAssignValueInArrayWithCorrectType() {
        assertScript '''
            int[] array = [1, 2, 3]
            array[1] = 4
        '''
    }

    void testAssignValueInArrayWithWrongType() {
        shouldFailWithMessages '''
            int[] array = [1, 2, 3]
            array[1] = "One"
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testBidimensionalArray() {
        assertScript '''
            int[][] array = new int[1][]
            array[0] = [1,2]
        '''
    }

    void testBidimensionalArrayWithInitializer() {
        shouldFailWithMessages '''
            int[][] array = new Object[1][]
        ''',
        'Cannot assign value of type java.lang.Object[][] to variable of type int[][]'
    }

    void testBidimensionalArrayWithWrongSubArrayType() {
        shouldFailWithMessages '''
            int[][] array = new int[1][]
            array[0] = ['1']
        ''',
        'Cannot assign value of type java.lang.String into array of type int[]'
    }

    void testForLoopWithArrayAndUntypedVariable() {
        assertScript '''
            String[] array = ['1','2','3']
            for (i in array) { }
        '''
    }

    void testForLoopWithArrayAndWrongVariableType() {
        shouldFailWithMessages '''
            String[] array = ['1','2','3']
            for (int i in array) { }
        ''',
        'Cannot loop with element of type int with collection of type java.lang.String[]'
    }

    void testJava5StyleForLoopWithArray() {
        assertScript '''
            String[] array = ['1','2','3']
            for (String i : array) { }
        '''
    }

    void testJava5StyleForLoopWithArrayAndIncompatibleType() {
        shouldFailWithMessages '''
            String[] array = ['1','2','3']
            for (int i : array) { }
        ''',
        'Cannot loop with element of type int with collection of type java.lang.String[]'
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

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
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

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
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
            for (int i in 1..10) { i * 2 }
        '''
    }

    void testForInLoopWithRangeUsingVariable() {
        assertScript '''
            int n = 10
            for (int i in 1..n) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
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
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
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
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(DECLARATION_INFERRED_TYPE) == int_TYPE
                })
                def k = i
            }
        '''
    }

    void testIsCaseArray() {
        assertScript '''
            def accept = new Integer[]{1}
            def result = ['x','yy','zzz'].findAll { it.size() in accept }
            assert result.size() == 1
        '''
    }

    // GROOVY-10239
    void testIsNotCaseArray() {
        assertScript '''
            def reject = new Integer[]{1}
            def result = ['x','yy','zzz'].findAll { it.size() !in reject }
            assert result.size() == 2
        '''
    }

    void testIsCaseCollection() {
        assertScript '''
            def accept = [1]
            def result = ['x','yy','zzz'].findAll { it.size() in accept }
            assert result.size() == 1
        '''
    }

    // GROOVY-10239
    void testIsNotCaseCollection() {
        assertScript '''
            def reject = [1]
            def result = ['x','yy','zzz'].findAll { it.size() !in reject }
            assert result.size() == 2
        '''
    }

    // GROOVY-10318
    void testIsNotCaseCollection2() {
        assertScript '''
            class Pogo {
                String foo, bar
            }
            @groovy.transform.CompileStatic
            void test() {
                def pogo = new Pogo(foo: 'foo', bar: 'bar')
                Map<String, ?> filteredProperties = pogo.getProperties().findAll {
                    String key = it.key
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                      def type = node.getNodeMetaData(INFERRED_TYPE)
                      assert type == boolean_TYPE
                    })
                    def result = key !in ['class']
                    return result
                }
                assert filteredProperties.size() == 2
            }
            test()
        '''
    }

    // GROOVY-5177
    void testShouldNotAllowArrayAssignment1() {
        shouldFailWithMessages '''
            class Foo {
                def say() {
                    FooAnother foo1 = new Foo[13] // but FooAnother foo1 = new Foo() reports a STC error
                }
            }
            class FooAnother {
            }
        ''',
        'Cannot assign value of type Foo[] to variable of type FooAnother'
    }

    // GROOVY-8984
    void testShouldNotAllowArrayAssignment2() {
        shouldFailWithMessages '''
            List<String> m() { }
            Number[] array = m()
        ''',
        'Cannot assign value of type java.util.List <String> to variable of type java.lang.Number[]'

        shouldFailWithMessages '''
            void test(Set<String> set) {
                Number[] array = set
            }
        ''',
        'Cannot assign value of type java.util.Set <String> to variable of type java.lang.Number[]'

        shouldFailWithMessages '''
            List<? super CharSequence> m() { }
            CharSequence[] array = m()
        ''',
        'Cannot assign value of type java.util.List <? super java.lang.CharSequence> to variable of type java.lang.CharSequence[]'

        shouldFailWithMessages '''
            void test(Set<? super CharSequence> set) {
                CharSequence[] array = set
            }
        ''',
        'Cannot assign value of type java.util.Set <? super java.lang.CharSequence> to variable of type java.lang.CharSequence[]'

        shouldFailWithMessages '''
            List<? super Runnable> m() { }
            Runnable[] array = m()
        ''',
        'Cannot assign value of type java.util.List <? super java.lang.Runnable> to variable of type java.lang.Runnable[]'

        shouldFailWithMessages '''
            void test(List<? super Runnable> list) {
                Runnable[] array = list
            }
        ''',
        'Cannot assign value of type java.util.List <? super java.lang.Runnable> to variable of type java.lang.Runnable[]'
    }

    // GROOVY-8983
    @NotYetImplemented
    void testShouldAllowArrayAssignment1() {
        assertScript '''
            List<String> m() { ['foo'] }
            void test(Set<String> set) {
                String[] one = m()
                String[] two = set
                assert one + two == ['foo','bar']
            }
            test(['bar'].toSet())
        '''

        assertScript '''
            List<String> m() { ['foo'] }
            void test(Set<String> set) {
                CharSequence[] one = m()
                CharSequence[] two = set
                assert one + two == ['foo','bar']
            }
            test(['bar'].toSet())
        '''

        assertScript '''
            List<String> m() { ['foo'] }
            void test(Set<String> set) {
                Object[] one = m()
                Object[] two = set
                assert one + two == ['foo','bar']
            }
            test(['bar'].toSet())
        '''

        assertScript '''
            List<? extends CharSequence> m() { ['foo'] }
            void test(Set<? extends CharSequence> set) {
                CharSequence[] one = m()
                CharSequence[] two = set
                assert one + two == ['foo','bar']
            }
            test(['bar'].toSet())
        '''

        assertScript '''
            List<? super CharSequence> m() { [null] }
            void test(Set<? super CharSequence> set) {
                Object[] one = m()
                Object[] two = set
                assert one + two == [null,null]
            }
            test([null].toSet())
        '''
    }

    // GROOVY-8983
    @NotYetImplemented
    void testShouldAllowArrayAssignment2() {
        assertScript '''
            List<String> m() { ['foo'] }
            void test(Set<String> set) {
                String[] one, two
                one = m()
                two = set
                assert one + two == ['foo','bar']
            }
            test(['bar'].toSet())
        '''
    }

    // GROOVY-9517
    void testShouldAllowArrayAssignment3() {
        assertScript '''
            void test(File directory) {
                File[] files = directory.listFiles()
                files = files?.sort { it.name }
                for (file in files) {
                    // ...
                }
            }
            assert 'no error'
        '''
    }

    // GROOVY-8983
    @NotYetImplemented
    void testShouldAllowArrayAssignment4() {
        assertScript '''
            class C {
                List<String> list = []
                void setX(String[] array) {
                    Collections.addAll(list, array)
                }
            }
            List<String> m() { ['foo'] }
            void test(Set<String> set) {
                def c = new C()
                c.x = m()
                c.x = set
                assert c.list == ['foo','bar']
            }
            test(['bar'].toSet())
        '''
    }

    void testShouldAllowArrayAssignment5() {
        assertScript '''
            Object[] array = new Double[]{1d}
            assert array[0] instanceof Double
        '''

        assertScript '''
            double[] array = new Double[]{1d}
            assert array.length == 1
            assert array[0] == 1.0
        '''

        assertScript '''
            int[] array = new Integer[]{1}
            assert array.length == 1
            assert array[0] == 1
        '''

        assertScript '''
            int[] array = new long[1]
            assert array.length == 1
            assert array[0] == 0
        '''
    }

    // GROOVY-7506
    void testShouldAllowArrayAssignment6() {
        String pogo = '''
            class C {
                public String[] strings
                void setP(String[] strings) {
                    this.strings = strings
                }
            }
        '''

        assertScript pogo + '''
            def c = new C()
            c.p = ['foo','bar']
            assert c.strings == ['foo','bar']
        '''

        assertScript pogo + '''
            def c = new C()
            c.p = ['foo', 123 ]
            assert c.strings == ['foo','123']
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
        ''',
        'Cannot call <T,U extends T> java.lang.String[]#putAt(int, U) with arguments [int, java.lang.Object]'
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
            arr.putAt(0, new groovy.xml.XmlSlurper())
        ''',
        'Cannot call <T,U extends T> java.io.Serializable[]#putAt(int, U) with arguments [int, groovy.xml.XmlSlurper]'
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
            assert m() == '2'
        '''
    }

    void testInferredTypeWithListAndFind() {
        assertScript '''
            List<Integer> list = [ 1, 2, 3, 4 ]

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            Integer i = list.find { int it -> it % 2 == 0 }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            Integer j = org.codehaus.groovy.runtime.DefaultGroovyMethods.find(list) { int it -> it % 2 == 0 }
        '''
    }

    // GROOVY-5573
    void testArrayNewInstance() {
        assertScript '''import java.lang.reflect.Array
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
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
            assert array[0].length == 1
        '''
    }

    // GROOVY-10319
    void testArrayClone() {
        assertScript '''
            assert (new int[9]).clone().length == 9
        '''
        assertScript '''
            package p // must be in package for protected method check

            @groovy.transform.ToString(includeFields=true)
            class C implements Cloneable {
                private int[] array = [1]
                @Override
                C clone() {
                    C c = (C) super.clone()
                    c.array = array.clone()
                    return c
                }
            }

            assert new C().clone().toString() == 'p.C([1])'
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
                Integer[] array = [ null, null ]
                return array
            }
            assert m().length == 2
        '''
    }

    void testShouldNotForceAsTypeWhenListOfNullAssignedToArrayUnlessPrimitive() {
        shouldFailWithMessages '''
            int[] m() {
                int[] array = [ null, null ]
                return array
            }
        ''',
        'Cannot assign value of type java.lang.Object into array of type int[]'
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
        ''',
        'Cannot find matching method java.util.Collection#putAt(int, java.lang.Object)'
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
        assertScript '''
            class Inner {Set<String> strings}
            class Outer {Set<Inner> inners}
            Outer outer = new Outer(inners: [ new Inner(strings: ['abc', 'def'] as Set), new Inner(strings: ['ghi'] as Set) ] as Set)
            def res = outer.inners*.strings
            assert res[1].contains('ghi')
            assert res[0].contains('abc')
            assert res[0].contains('def')
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

    // GROOVY-6241
    void testAsImmutable() {
        assertScript '''
            List<Integer> list = [1,2,3]
            List<Integer> immutableList = [1,2,3].asImmutable()
            assert list !== immutableList && list.equals(immutableList)

            Map<String,Integer> map = [a:1]
            Map<String,Integer> immutableMap = [a:1].asImmutable()
            assert map !== immutableMap && map.equals(immutableMap)
        '''
    }

    void testAsUnmodifiable() {
        assertScript '''
            List<Integer> list = [1,2,3]
            List<Integer> immutableList = [1,2,3].asUnmodifiable()
            assert list !== immutableList && list.equals(immutableList)

            Map<String,Integer> map = [a:1]
            Map<String,Integer> immutableMap = [a:1].asUnmodifiable()
            assert map !== immutableMap && map.equals(immutableMap)
        '''
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

    // GROOVY-6350
    void testListPlusList() {
        assertScript '''
            def list = [] + []
            assert list.isEmpty()
        '''
    }

    // GROOVY-7122
    void testIterableLoop() {
        assertScript '''
            int countIt(Iterable<Integer> list) {
                int count = 0
                for (Integer obj : list) {
                    count++
                }
                return count
            }
            assert countIt([1,2,3]) == 3
        '''
    }

    void testAbstractTypeInitializedByListLiteral() {
        shouldFailWithMessages '''
            abstract class A {
                A(int n) {}
            }
            A a = [1]
        ''',
        'Cannot assign value of type java.util.List <java.lang.Integer> to variable of type A'
    }

    // GROOVY-6912
    void testArrayListTypeInitializedByListLiteral() {
        assertScript '''
            ArrayList list = [1,2,3]
            assert list.size() == 3
            assert list.last() == 3
        '''

        assertScript '''
            ArrayList list = [[1,2,3]]
            assert list.size() == 1
        '''

        assertScript '''
            ArrayList<Integer> list = [1,2,3]
            assert list.size() == 3
            assert list.last() == 3
        '''

        shouldFailWithMessages '''
            ArrayList<String> strings = [1,2,3]
        ''',
        'Incompatible generic argument types. Cannot assign java.util.ArrayList <java.lang.Integer> to: java.util.ArrayList <String>'
    }

    // GROOVY-6912
    void testSetDerivativesInitializedByListLiteral() {
        assertScript '''
            LinkedHashSet set = [1,2,3]
            assert set.size() == 3
            assert set.contains(3)
        '''

        assertScript '''
            HashSet set = [1,2,3]
            assert set.size() == 3
            assert set.contains(3)
        '''

        assertScript '''
            LinkedHashSet set = [[1,2,3]]
            assert set.size() == 1
        '''

        assertScript '''
            LinkedHashSet<Integer> set = [1,2,3]
            assert set.size() == 3
            assert set.contains(3)
        '''

        shouldFailWithMessages '''
            LinkedHashSet<String> strings = [1,2,3]
        ''',
        'Incompatible generic argument types. Cannot assign java.util.LinkedHashSet <java.lang.Integer> to: java.util.LinkedHashSet <String>'
    }

    void testCollectionTypesInitializedByListLiteral1() {
        assertScript '''
            Set<String> set = []
            set << 'foo'
            set << 'bar'
            set << 'foo'
            assert set.size() == 2
        '''

        assertScript '''
            AbstractSet<String> set = []
            set << 'foo'
            set << 'bar'
            set << 'foo'
            assert set.size() == 2
        '''
    }

    // GROOVY-10002
    void testCollectionTypesInitializedByListLiteral2() {
        assertScript '''
            Set<String> set = ['foo', 'bar', 'foo']
            assert set.size() == 2
        '''

        assertScript '''
            AbstractList<String> list = ['foo', 'bar', 'foo']
            assert list.size() == 3
        '''

        assertScript '''
            ArrayDeque<String> deque = [123] // ArrayDeque(int numElements)
        '''
    }

    // GROOVY-10002
    void testCollectionTypesInitializedByListLiteral3() {
        shouldFailWithMessages '''
            Set<String> set = [1,2,3]
        ''',
        'Cannot assign java.util.LinkedHashSet <java.lang.Integer> to: java.util.Set <String>'

        shouldFailWithMessages '''
            List<String> list = ['a','b',3]
        ''',
        'Cannot assign java.util.ArrayList <java.io.Serializable> to: java.util.List <String>'

        shouldFailWithMessages '''
            Iterable<String> iter = [1,2,3]
        ''',
        'Cannot assign java.util.ArrayList <java.lang.Integer> to: java.lang.Iterable <String>'

        shouldFailWithMessages '''
            Collection<String> coll = [1,2,3]
        ''',
        'Cannot assign java.util.ArrayList <java.lang.Integer> to: java.util.Collection <String>'

        shouldFailWithMessages '''
            Deque<String> deque = [""]
        ''',
        'Cannot assign value of type java.util.List', ' to variable of type java.util.Deque <String>'

        shouldFailWithMessages '''
            Deque<String> deque = []
        ''',
        'Cannot assign value of type java.util.List', ' to variable of type java.util.Deque <String>'

        shouldFailWithMessages '''
            Queue<String> queue = []
        ''',
        'Cannot assign value of type java.util.List', ' to variable of type java.util.Queue <String>'
    }

    // GROOVY-7128
    void testCollectionTypesInitializedByListLiteral4() {
        assertScript '''
            Collection<Number> collection = [1,2,3]
            assert collection.size() == 3
            assert collection.last() == 3
        '''

        assertScript '''
            List<Number> list = [1,2,3]
            assert list.size() == 3
            assert list.last() == 3
        '''

        assertScript '''
            Set<Number> set = [1,2,3,3]
            assert set.size() == 3
            assert set.last() == 3
        '''
    }

    // GROOVY-8001, GROOVY-11028, GROOVY-11080
    void testMapWithTypeArgumentsInitializedByMapLiteral() {
        for (spec in ['CharSequence,Integer', 'String,Number', 'CharSequence,Number']) {
            assertScript """
                Map<$spec> map = [a:1,b:2,c:3]
                assert map.size() == 3
                assert map['c'] == 3
                assert 'x' !in map
            """
        }

        assertScript '''
            Map<String,Map<String,Object>> map = [:]
            map.put('foo', [bar:null])
            map.put('baz', [:])
            assert 'foo' in map
        '''

        assertScript '''
            class C {
                Map<String,Object> map
            }
            int value = 42
            def c = new C()
            c.map = [key:"$value"]
            assert c.map['key'] == '42'
            c = new C(map: [key:value])
            assert c.map.key.toString() == '42'
        '''

        assertScript '''
            Map<String,Integer> map = [:].withDefault { 0 }
            assert map.size() == 0
            assert map.foo == 0
        '''

        shouldFailWithMessages '''
            Map<String,Integer> map = [1:2]
        ''',
        'Cannot assign java.util.LinkedHashMap <java.lang.Integer, java.lang.Integer> to: java.util.Map <String, Integer>'
    }

    // GROOVY-8136
    void testInterfaceThatExtendsMapInitializedByMapLiteral() {
        shouldFailWithMessages '''
            interface MVM<K, V> extends Map<K, List<V>> { }
            MVM map = [:] // no STC error; fails at runtime
        ''',
        'No matching constructor found'
    }

    // GROOVY-8136
    void testAbstractClassThatImplementsMapInitializedByMapLiteral() {
        shouldFailWithMessages '''
            abstract class MVM<K, V> implements Map<K, List<V>> { }
            MVM map = [:] // no STC error; fails at runtime
        ''',
        'No matching constructor found'
    }
}
