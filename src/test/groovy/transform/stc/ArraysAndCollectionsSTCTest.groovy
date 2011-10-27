/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc


/**
 * Unit tests for static type checking : arrays and collections.
 *
 * @author Cedric Champeau
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
        ''', 'Cannot assign value of type java.lang.String to variable of type java.lang.Integer'
    }

    void testWrongComponentTypeInArray() {
        shouldFailWithMessages '''
            int[] intArray = ['a']
        ''', 'Cannot assign value of type java.lang.String into array of type [I'
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
        ''', 'Cannot assign value of type [[Ljava.lang.Object; to variable of type [[I'
    }

    void testBidimensionalArrayWithWrongSubArrayType() {
        shouldFailWithMessages '''
            int[][] arr2 = new int[1][]
            arr2[0] = ['1']
        ''', 'Cannot assign value of type java.lang.String into array of type [I'
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
        ''', 'Cannot loop with element of type int with collection of type [Ljava.lang.String;'
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
        ''', 'Cannot loop with element of type int with collection of type [Ljava.lang.String;'
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
    }

    void testListStarProperty() {
        assertScript '''
            List list = ['a','b','c']
            List classes = list*.class
        '''
    }

    void testListStarMethod() {
        assertScript '''
            List list = ['a','b','c']
            List classes = list*.toUpperCase()
        '''
    }
}

