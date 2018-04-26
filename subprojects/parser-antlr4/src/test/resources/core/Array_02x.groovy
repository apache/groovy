import groovy.transform.CompileStatic

def testArrayLiteral() {
    int[] a = {1, 2, 3}
    assert 3 == a.length
    assert 1 == a[0]
    assert 2 == a[1]
    assert 3 == a[2]

    int[][] a2 = {
        {1, 2, 3},
        {1, 2, 3, 4}
    }
    assert 2 == a2.length
    assert 3 == a2[0].length
    assert 1 == a2[0][0]
    assert 2 == a2[0][1]
    assert 3 == a2[0][2]
    assert 4 == a2[1].length
    assert 1 == a2[1][0]
    assert 2 == a2[1][1]
    assert 3 == a2[1][2]
    assert 4 == a2[1][3]

    int[] a3 = { 1 }
    assert 1 == a3[0]

    int[][] a4 = { {1} }
    assert 1 == a4[0][0]

    int[][] a5 = { {1} }
    assert 1 == a5[0][0]

    int[][] a6 = { {1, 2} }
    assert 1 == a6[0][0]

    int[][] a7 = { new int[] {1} }
    assert 1 == a7[0][0]

    int[][] a8 = { {1}, new int[] {2, 3} }
    assert 2 == a8.length
    assert 1 == a8[0][0]
    assert 2 == a8[1][0]
    assert 3 == a8[1][1]

    int[][] a9 = {{}}
    assert 0 == a9[0].length

    int[][] a10 = {{}, {1}}
    assert 2 == a10.length
    assert 0 == a10[0].length
    assert 1 == a10[1].length
    assert 1 == a10[1][0]

    int[][] a11 = new int[][] { {}, {1}, {2, 3}, new int[] {4, 5, 6}, {7, 8, 9, 10} }
    assert 5 == a11.length
    assert 0 == a11[0].length
    assert 1 == a11[1][0]
    assert 2 == a11[2][0]
    assert 4 == a11[3][0]
    assert 7 == a11[4][0]

    int[][][] a12 = { new int[][] { {} } }
    assert 0 == a12[0][0].length

    int[][][] a13 = { { {} } }
    assert 0 == a13[0][0].length

    int[][][] a14 = { {{}}, new int[][] { {} }, new int[][] { {1} }, { {2} } }
    assert 4 == a14.length
    assert 0 == a14[0][0].length
    assert 0 == a14[1][0].length
    assert 1 == a14[2][0].length
    assert 1 == a14[3][0].length
    assert 1 == a14[2][0][0]
    assert 2 == a14[3][0][0]

    int[][][] a15 = new int[][][] { {{}}, new int[][] { {} }, new int[][] { {1} }, { {2} } }
    assert 4 == a15.length
    assert 0 == a15[0][0].length
    assert 0 == a15[1][0].length
    assert 1 == a15[2][0].length
    assert 1 == a15[3][0].length
    assert 1 == a15[2][0][0]
    assert 2 == a15[3][0][0]

    Closure[] a16 = {{def x = 1; return x}}
    assert 1 == a16[0]()

    Closure[] a17 = {{def x = 1; return x}, {def x = 2; return x}, {}}
    assert 1 == a17[0]()
    assert 2 == a17[1]()
    assert a17[2] instanceof Closure

    Closure[] a18 = {{}}
    assert a18[0] instanceof Closure

    // legacy way to handle Closure array
    Closure[] a19 = {-> 1}
    assert 1 == a19[0]()
    Closure[] a20 = {def y = 1; return y}
    assert 1 == a20[0]()

    Closure[] a21 = {{-> 1}}
    assert 1 == a21[0]()

    Closure[] a22 = {}
    assert 0 == a22.length


    def a23 = { 1 }
    assert 1 == a23()
}
testArrayLiteral()

@CompileStatic
def testArrayLiteralCS() {
    int[] a = {1, 2, 3}
    assert 3 == a.length
    assert 1 == a[0]
    assert 2 == a[1]
    assert 3 == a[2]

    int[][] a2 = {
        {1, 2, 3},
        {1, 2, 3, 4}
    }
    assert 2 == a2.length
    assert 3 == a2[0].length
    assert 1 == a2[0][0]
    assert 2 == a2[0][1]
    assert 3 == a2[0][2]
    assert 4 == a2[1].length
    assert 1 == a2[1][0]
    assert 2 == a2[1][1]
    assert 3 == a2[1][2]
    assert 4 == a2[1][3]

    int[] a3 = { 1 }
    assert 1 == a3[0]

    int[][] a4 = { {1} }
    assert 1 == a4[0][0]

    int[][] a5 = { {1} }
    assert 1 == a5[0][0]

    int[][] a6 = { {1, 2} }
    assert 1 == a6[0][0]

    int[][] a7 = { new int[] {1} }
    assert 1 == a7[0][0]

    int[][] a8 = { {1}, new int[] {2, 3} }
    assert 2 == a8.length
    assert 1 == a8[0][0]
    assert 2 == a8[1][0]
    assert 3 == a8[1][1]

    int[][] a9 = {{}}
    assert 0 == a9[0].length

    int[][] a10 = {{}, {1}}
    assert 2 == a10.length
    assert 0 == a10[0].length
    assert 1 == a10[1].length
    assert 1 == a10[1][0]

    int[][] a11 = new int[][] { {}, {1}, {2, 3}, new int[] {4, 5, 6}, {7, 8, 9, 10} }
    assert 5 == a11.length
    assert 0 == a11[0].length
    assert 1 == a11[1][0]
    assert 2 == a11[2][0]
    assert 4 == a11[3][0]
    assert 7 == a11[4][0]

    int[][][] a12 = { new int[][] { {} } }
    assert 0 == a12[0][0].length

    int[][][] a13 = { { {} } }
    assert 0 == a13[0][0].length

    int[][][] a14 = { {{}}, new int[][] { {} }, new int[][] { {1} }, { {2} } }
    assert 4 == a14.length
    assert 0 == a14[0][0].length
    assert 0 == a14[1][0].length
    assert 1 == a14[2][0].length
    assert 1 == a14[3][0].length
    assert 1 == a14[2][0][0]
    assert 2 == a14[3][0][0]

    int[][][] a15 = new int[][][] { {{}}, new int[][] { {} }, new int[][] { {1} }, { {2} } }
    assert 4 == a15.length
    assert 0 == a15[0][0].length
    assert 0 == a15[1][0].length
    assert 1 == a15[2][0].length
    assert 1 == a15[3][0].length
    assert 1 == a15[2][0][0]
    assert 2 == a15[3][0][0]

    Closure[] a16 = {{def x = 1; return x}}
    assert 1 == a16[0]()

    Closure[] a17 = {{def x = 1; return x}, {def x = 2; return x}, {}}
    assert 1 == a17[0]()
    assert 2 == a17[1]()
    assert a17[2] instanceof Closure

    Closure[] a18 = {{}}
    assert a18[0] instanceof Closure

    // legacy way to handle Closure array
//    Closure[] a19 = {-> 1}
//    assert 1 == a19[0]()
//    Closure[] a20 = {def y = 1; return y}
//    assert 1 == a20[0]()

    Closure[] a21 = {{-> 1}}
    assert 1 == a21[0]()

    Closure[] a22 = {}
    assert 0 == a22.length


    def a23 = { 1 }
    assert 1 == a23()
}
testArrayLiteralCS()
