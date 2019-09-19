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

class ArrayTest extends GroovyTestCase {

    void testFixedSize() {
        def array = new String[10]
        assert array.size() == 10
        array[0] = "Hello"
        assert array[0] == "Hello"
    }

    void testArrayWithInitializer() {
        String[] array = ["nice", "cheese", "gromit"]
        assert array.size() == 3
        assert array[0] == "nice", array.inspect()
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
        char[] array = ['a', 'b', "$bar"]
        assert array.size() == 3
        assert array[0] == 'a', array.inspect()
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
        assert array[0] == 0, array.inspect()
        assert array[1] == 1
        assert array[2] == 2
        assert array[3] == 3
    }

    void testByteArrayWithInitializerAndAssignmentOfNumber() {
        byte[] array = [2, 4]
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
        def array = new double[3]
        assert array.size() == 3
    }

    void testDoubleArrayWithInitializer() {
        double[] array = [1.3, 3.14, 2.7]
        assert array.size() == 3
        assert array[0] == 1.3, array.inspect()
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
        assert array[0] == 42, array.inspect()
        assert array[1] == -5
        assert array[2] == 360
    }

    void testArrayDeclaration() {
        String[] array = ["a", "b", "c"]
        assert array.class == String[].class
        assert array.size() == 3
        assert array[0] == "a"
        assert array[1] == "b"
        assert array[2] == "c"
    }

    void testArrayAssignmentShouldHonorInheritance() {
        String[] array = ["a", "b", "c"]
        Object[] other = array
        assert other.class == String[].class
        assert other.hashCode() == array.hashCode()
    }

    void testSimpleArrayEquals() {
        Integer[] arr1 = [1, 2, 3, 4]
        Integer[] arr2 = [1, 2, 3, 4]
        assert arr1 == arr2
        int[] primarr1 = [1, 2, 3, 4]
        int[] primarr2 = [1, 2, 3, 4]
        assert primarr1 == primarr2
        assert primarr1 == arr2
        double[] primarr3 = [1, 2, 3, 4]
        long[] primarr4 = [1, 2, 3, 4]
        assert primarr3 == primarr4
        assert primarr3 == primarr1
        assert primarr2 == primarr4
        def list1 = [1, 2, 3, 4]
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
        def a = [1, 2] as Integer[]
        def b = [1, 2]
        assert a == b
        assert [[1, 2], [3, 4]] == [[1, 2], [3, 4]]
        def x = [[1, 2] as Integer[]]
        Object[] y = [[1, 2]]
        assert y == x
        assert [[1, 2], [3, 4]] as int[][] == [[1, 2], [3, 4]] as int[][]
        assert [[[5, 6], [7, 8]]] as int[][][] == [[[5, 6], [7, 8]]] as Long[][][]
        assert [[1, 2], [3, 4]] as long[][] == [[1, 2], [3, 4]] as long[][]
        assert [[1, 2], [3, 4]] as long[][] == [[1, 2], [3, 4]] as Long[][]
        assert [[1, 2], [3, 4]] as long[][] == [[1, 2], [3, 4]]
        assert [[1, 2], [3, 4]] as long[][] == [[1, 2] as short[], [3, 4] as short[]]
        int[][] intsA = [[1, 2], [3, 4]]
        assert intsA == [[1, 2], [3, 4]] as int[][]
        int[][] intsB = [[1, 2], [3, 4]]
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

    void testAssignmentOfSingleCharStringToNumberArrays() {
        def x = 'x'
        def gx = "$x"

        Short[] sa = new Short[1]
        sa[0] = 'c' as char
        assert sa[0] == 99
        sa[0] = 'd'
        assert sa[0] == 100
        sa[0] = gx
        assert sa[0] == 120
        shouldFail {
            sa[0] = 'zz'
        }

        Integer[] ca = new Integer[1]
        ca[0] = 'c' as char
        assert ca[0] == 99
        ca[0] = 'd'
        assert ca[0] == 100
        ca[0] = gx
        assert ca[0] == 120
        shouldFail {
            ca[0] = 'zz'
        }

        Long[] la = new Long[1]
        la[0] = 'c' as char
        assert la[0] == 99
        la[0] = 'd'
        assert la[0] == 100
        la[0] = gx
        assert la[0] == 120
        shouldFail {
            la[0] = 'zz'
        }

        Float[] fa = new Float[1]
        fa[0] = 'c' as char
        assert fa[0] == 99.0f
        fa[0] = 'd'
        assert fa[0] == 100.0f
        fa[0] = gx
        assert fa[0] == 120.0f
        shouldFail {
            fa[0] = 'zz'
        }

        Double[] da = new Double[1]
        da[0] = 'c' as char
        assert da[0] == 99.0d
        da[0] = 'd'
        assert da[0] == 100.0d
        da[0] = gx
        assert da[0] == 120.0d
        shouldFail {
            da[0] = 'zz'
        }

        BigInteger[] bia = new BigInteger[1]
        bia[0] = 'c' as char
        assert bia[0] == new BigInteger("99")
        bia[0] = 'd'
        assert bia[0] == new BigInteger("100")
        bia[0] = gx
        assert bia[0] == new BigInteger("120")
        shouldFail {
            bia[0] = 'zz'
        }

        BigDecimal[] bda = new BigDecimal[1]
        bda[0] = 'c' as char
        assert bda[0] == new BigDecimal("99")
        bda[0] = 'd'
        assert bda[0] == new BigDecimal("100")
        bda[0] = gx
        assert bda[0] == new BigDecimal("120")
        shouldFail {
            bda[0] = 'zz'
        }

        short[] sap = new short[1]
        sap[0] = 'c' as char
        assert sap[0] == 99
        sap[0] = 'd'
        assert sap[0] == 100
        sap[0] = gx
        assert sap[0] == 120
        shouldFail {
            sap[0] = 'zz'
        }

        int[] iap = new int[1]
        iap[0] = 'c' as char
        assert iap[0] == 99
        iap[0] = 'd'
        assert iap[0] == 100
        iap[0] = gx
        assert iap[0] == 120
        shouldFail {
            iap[0] = 'zz'
        }

        long[] lap = new long[1]
        lap[0] = 'c' as char
        assert lap[0] == 99
        lap[0] = 'd'
        assert lap[0] == 100
        lap[0] = gx
        assert lap[0] == 120
        shouldFail {
            lap[0] = 'zz'
        }

        float[] fap = new float[1]
        fap[0] = 'c' as char
        assert fap[0] == 99.0f
        fap[0] = 'd'
        assert fap[0] == 100.0f
        fap[0] = gx
        assert fap[0] == 120.0f
        shouldFail {
            fap[0] = 'zz'
        }

        double[] dap = new double[1]
        dap[0] = 'c' as char
        assert dap[0] == 99.0d
        dap[0] = 'd'
        assert dap[0] == 100.0d
        dap[0] = gx
        assert dap[0] == 120.0d
        shouldFail {
            dap[0] = 'zz'
        }
    }

    void testFlattenArray() {
        def orig = "onetwo".toList().toArray()
        def flat = orig.flatten()
        assert flat == ["o", "n", "e", "t", "w", "o"]
    }

    void testFlattenArrayOfLists() {
        def orig = ["one".toList(), "two".toList()] as Object[]
        def flat = orig.flatten()
        assert flat == ["o", "n", "e", "t", "w", "o"]
    }

    void testFlattenArrayOfArrays() {
        def orig = ["one".toList().toArray(), "two".toList().toArray()] as Object[]
        def flat = orig.flatten()
        assert flat == ["o", "n", "e", "t", "w", "o"]
    }

    void testFlattenPrimitiveArray() {
        def orig = [1, 2, 3] as int[]
        def flat = orig.flatten()
        assert flat == [1, 2, 3]
    }

    void testFlattenArrayOfPrimitiveArrays() {
        def orig = [[1, 2, 3] as int[], [4, 5, 6] as int[]] as int[][]
        def flat = orig.flatten()
        assert flat == [1, 2, 3, 4, 5, 6]
    }

    void testGroovy5402ArrayPlus() {
        Integer[] a = [ 1, 2, 3 ]
        Integer[] b = [ 3, 4, 5 ]
        def result = a + b
        assert result == [ 1, 2, 3, 3, 4, 5 ]
        assert result.class.isArray()
        // check the originals are untouched
        assert a == [ 1, 2, 3 ]
        assert b == [ 3, 4, 5 ]

        result = a + 4
        assert result == [ 1, 2, 3, 4 ]
        assert result.class.isArray()

        result = a + [ 4, 5 ]
        assert result == [ 1, 2, 3, 4, 5 ]
        assert result.class.isArray()
    }

    void doSomething(long[] values){
         values[1] += 5 
    }

    void testLongArrayIncrement() {
        long[] l = [1l,0l]
        doSomething(l)
        assert l[0]==1
        assert l[1]==5
    }

    void testJoin() {
        def a1 = [false, true] as boolean[]
        def a2 = [1 as byte, 2 as byte] as byte[]
        def a3 = ["a".charAt(0), "b".charAt(0)] as char[]
        def a4 = [1 as double, 2 as double] as double[]
        def a5 = [1 as float, 2 as float] as float[]
        def a6 = [1 as int, 2 as int] as int[]
        def a7 = [1 as long, 2 as long] as long[]
        def a8 = [1 as short, 2 as short] as short[]

        assert "false, true" == a1.join(", ")
        assert "1, 2" == a2.join(", ")
        assert "a, b" == a3.join(", ")
        assert "1.0, 2.0" == a4.join(", ")
        assert "1.0, 2.0" == a5.join(", ")
        assert "1, 2" == a6.join(", ")
        assert "1, 2" == a7.join(", ")
        assert "1, 2" == a8.join(", ")
    }

    void testSum() {
        def a1 = [1, 2, 3] as byte[]
        def a2 = [1, 2, 3] as short[]
        def a3 = [1, 2, 3] as int[]
        def a4 = [1, 2, 3] as long[]
        def a5 = [1, 2, 3] as char[]
        def a6 = [1, 2, 3] as float[]
        def a7 = [1, 2, 3] as double[]

        assert ((byte) 6) == a1.sum()
        assert ((short) 6) == a2.sum()
        assert ((int) 6) == a3.sum()
        assert ((long) 6) == a4.sum()
        assert ((char) 6) == a5.sum()
        assert ((float) 6) == a6.sum()
        assert ((double) 6) == a7.sum()

        assert ((byte) 10) == a1.sum((byte) 4)
        assert ((short) 10) == a2.sum((short) 4)
        assert ((int) 10) == a3.sum(4)
        assert ((long) 10) == a4.sum(4)
        assert ((char) 10) == a5.sum((char) 4)
        assert ((float) 10) == a6.sum(4)
        assert ((double) 10) == a7.sum(4)
    }
}
