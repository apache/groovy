package groovy

class ArrayTest extends GroovyTestCase {

    void testFixedSize() {
        def array = new String[10]
        assert array.size() == 10
        array[0] = "Hello"
        assert array[0] == "Hello"
    }
    
    void testArrayWithInitializer() {
        String[] array = [ "nice", "cheese", "gromit" ]
        assert array.size() == 3
        assert array[0] == "nice" , array.inspect()
        assert array[1] == "cheese"
        assert array[2] == "gromit"
    }

    void testCharArrayCreate() {
           def array = new char[3]
           assert array.size() == 3
    }

    void testCharArrayAssignToElement() {
        char[] ca = new char[1]

        // Assignments statements.
        ca[0] = 'b' as char
        assert ca[0] == 'b'

        ca[0] = '\u00A1' as char
        assert ca[0] == '\u00A1'

        ca[0] = 'a'
        assert ca[0] == 'a'

        ca[0] = '\u00A0'
        assert ca[0] == '\u00A0'

        def foo = 'z'
        
        ca[0] = "$foo"
        assert ca[0] == 'z'
        
        // Assignment in value context.
        assert (ca[0] = 'b' as char) == 'b'
        assert ca[0] == 'b'

        assert (ca[0] = '\u00A1' as char) == '\u00A1'
        assert ca[0] == '\u00A1'

        assert (ca[0] = 'a') == 'a'
        assert ca[0] == 'a'

        assert (ca[0] = '\u00A0') == '\u00A0'
        assert ca[0] == '\u00A0'

        assert (ca[0] = "$foo") == 'z'
        assert ca[0] == 'z'
    }
    
    void testCharArrayWithInitializer() {
        def bar = 'c'
        char[] array = [ 'a', 'b', "$bar" ]
        assert array.size() == 3
        assert array[0] == 'a' , array.inspect()
        assert array[1] == 'b'
        assert array[2] == 'c'
    }
    
    void testByteArrayCreate() {
        def array = new byte[100]
        assert array.size() == 100;
    }

    void testByteArrayWithInitializer() {
        byte[] array = [0, 1, 2, 3]
        assert array.size() == 4
        assert array[0] == 0 , array.inspect()
        assert array[1] == 1
        assert array[2] == 2
        assert array[3] == 3
    }

    void testByteArrayWithInitializerAndAssignmentOfNumber() {
        byte[] array = [ 2, 4]
        assert array.size() == 2
        assert array[0] == 2
        assert array[1] == 4

        array[0] = 76
        array[1] = 32
        assert array[0] == 76
        assert array[1] == 32

        array.putAt(0, 45)
        array.putAt(1, 67)
        assert array[0] == 45
        assert array[1] == 67
    }

    void testEachByteForByteArray() {
        byte[] array1 = [2, 4]
        def sum = 0
        array1.eachByte { sum += it }
        assert sum == 6
        Byte[] array2 = [2, 4]
        def result = 1
        array2.eachByte { result *= it }
        assert result == 8
    }

    void testDoubleArrayCreate() {
         def array  = new double[3]
         assert array.size() == 3
    }

    void testDoubleArrayWithInitializer() {
        double[] array = [ 1.3, 3.14, 2.7]
        assert array.size() == 3
        assert array[0] == 1.3 , array.inspect()
        assert array[1] == 3.14
        assert array[2] == 2.7
    }

    void testIntArrayCreate() {
        def array = new int[5]
        assert array.size() == 5
    }

    void testIntArrayWithInitializer() {
        int[] array = [42, -5, 360]
        assert array.size() == 3
        assert array[0] == 42 , array.inspect()
        assert array[1] == -5
        assert array[2] == 360
    }

    void testArrayDeclaration() {
        String[] array = [ "a", "b", "c" ]
        assert array.class == String[].class
        assert array.size() == 3
        assert array[0] == "a"
        assert array[1] == "b"
        assert array[2] == "c"
    }

    void testArrayAssignmentShouldHonorInheritance() {
        String[] array = [ "a", "b", "c" ]
        Object[] other = array
        assert other.class == String[].class
        assert other.hashCode() == array.hashCode()
    }

    void testSimpleArrayEquals() {
        Integer[] arr1 = [1,2,3,4]
        Integer[] arr2 = [1,2,3,4]
        assert arr1 == arr2
        int[] primarr1 = [1,2,3,4]
        int[] primarr2 = [1,2,3,4]
        assert primarr1 == primarr2
        assert primarr1 == arr2
        double[] primarr3 = [1,2,3,4]
        long[] primarr4 = [1,2,3,4]
        assert primarr3 == primarr4
        assert primarr3 == primarr1
        assert primarr2 == primarr4
        def list1 = [1,2,3,4]
        assert list1 == arr1
        assert arr1 == list1
        assert list1 == primarr1
        assert primarr1 == list1
        boolean[] bools1 = [true, true, false]
        boolean[] bools2 = [true, true, false]
        assert bools1 == bools2
        assert bools1 == [true, true, false] as boolean[]
    }

    void testComplexArrayEquals() {
        def a = [1,2] as Integer[]
        def b = [1,2]
        assert a == b
        assert [[1,2],[3,4]] == [[1,2],[3,4]]
        def x = [[1,2] as Integer[]]
        Object[] y = [[1,2]]
        assert y == x
        assert [[1,2],[3,4]] as int[][] == [[1,2],[3,4]] as int[][]
        assert [[[5,6],[7,8]]] as int[][][] == [[[5,6],[7,8]]] as Long[][][]
        assert [[1,2],[3,4]] as long[][] == [[1,2],[3,4]] as long[][]
        assert [[1,2],[3,4]] as long[][] == [[1,2],[3,4]] as Long[][]
        assert [[1,2],[3,4]] as long[][] == [[1,2],[3,4]]
        assert [[1,2],[3,4]] as long[][] == [[1,2] as short[], [3,4] as short[]]
        int[][] intsA = [[1,2],[3,4]]
        assert intsA == [[1,2],[3,4]] as int[][]
        int[][] intsB = [[1,2],[3,4]]
        assert intsA == intsB
        boolean[][] boolsA = [[true, true], [false, true], [false]]
        boolean[][] boolsB = [[true, true], [false, true], [false]]
        assert boolsA == boolsB
    }

    void testNumberWrapperArrayAssignToElement() {
        Byte[] bytes = [1, 2]
        bytes[0] = (byte) 20
        bytes[1] = 50
        assertEquals 20, bytes[0]
        assertEquals 50, bytes[1]

        Short[] shorts = [1, 2]
        shorts[1] = 50
        assertEquals 50, shorts[1]

        Float[] floats = [1.0f, 2.0f]
        floats[1] = 50.0d
        assertEquals 50.0d, floats[1]
        
        Double[] doubles = [1.0d, 2.0d]
        doubles[0] = new BigDecimal(100)
        doubles[1] = 50
        
        assertEquals 100, doubles[0]
        assertEquals 50, doubles[1]
    }
    
    void testCharacterArrayElementAssignments() {
        Character[] ca = new Character[1]

        // Assignments statements.
        ca[0] = 'b' as char
        assert ca[0] == 'b'

        ca[0] = '\u00A1' as char
        assert ca[0] == '\u00A1'

        ca[0] = 'a'
        assert ca[0] == 'a'

        ca[0] = '\u00A0'
        assert ca[0] == '\u00A0'

        def foo = 'z'

        ca[0] = "$foo"
        assert ca[0] == 'z'
    }
    
}
