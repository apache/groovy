package groovy.transform.stc


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 04/10/11
 * Time: 14:36
 */

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
}

