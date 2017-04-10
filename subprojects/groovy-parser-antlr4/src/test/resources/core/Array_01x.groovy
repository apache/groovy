import groovy.transform.CompileStatic

def testArrayInitializer() {
    def x = new double[] {}
    assert x.length == 0

    def y = new double[][] {}
    assert y.length == 0

    def a = new int[] {1, 2}
    assert a[0] == 1
    assert a[1] == 2
    assert a as List == [1, 2]

    def b = new int[][] {
        new int[] {1, 1.plus(1)},
        new int[] {2.plus(1), 4}
    }
    assert b[0][0] == 1
    assert b[0][1] == 2
    assert b[1][0] == 3
    assert b[1][1] == 4

    def c = new String[] {
        'a'
        ,
        'b'
        ,
        'c'
        ,
    }
    assert c[0] == 'a'
    assert c[1] == 'b'
    assert c[2] == 'c'
}
testArrayInitializer();

@CompileStatic
def testArrayInitializerCS() {
    def x = new double[] {}
    assert x.length == 0

    def y = new double[][] {}
    assert y.length == 0

    def a = new int[] {1, 2}
    assert a[0] == 1
    assert a[1] == 2
    assert a as List == [1, 2]

    def b = new int[][] {
        new int[] {1, 1.plus(1)},
        new int[] {2.plus(1), 4}
    }
    assert b[0][0] == 1
    assert b[0][1] == 2
    assert b[1][0] == 3
    assert b[1][1] == 4

    def c = new String[] {
        'a'
        ,
        'b'
        ,
        'c'
        ,
    }
    assert c[0] == 'a'
    assert c[1] == 'b'
    assert c[2] == 'c'
}
testArrayInitializerCS();