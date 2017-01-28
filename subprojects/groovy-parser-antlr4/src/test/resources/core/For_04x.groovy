import groovy.transform.CompileStatic

@CompileStatic
def testCS() {
    int result = 0
    for (def (int i, int j) = [0, 0]; i < 5 && j < 5; i = i + 2, j++) {
        result += i;
        result += j;
    }
    assert 9 == result

    result = 0
    for (int i = 0, j = 0; i < 5 && j < 5; i = i + 2, j++) {
        result += i;
        result += j;
    }
    assert 9 == result
}
testCS()

def test() {
    int result = 0
    for ((i, j) = [0, 0]; i < 5 && j < 5; i = i + 2, j++) {
        result += i;
        result += j;
    }
    assert 9 == result

    result = 0
    for (int i = 0, j = 0; i < 5 && j < 5; i = i + 2, j++) {
        result += i;
        result += j;
    }
    assert 9 == result
}
test()
