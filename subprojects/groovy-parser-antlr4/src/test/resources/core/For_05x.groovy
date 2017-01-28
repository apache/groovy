import groovy.transform.CompileStatic

@CompileStatic
def testCS() {
    int result = 0
    for (int i = 0, n = 5; i < n; i++) {
        result += i
    }
    assert 10 == result

    result = 0;
    int i;
    int j;
    for (i = 1, j = 5; i < j; i++, j--) {
        result += i;
        result += j;
    }
    assert 12 == result
}
testCS();

def test() {
    int result = 0
    for (int i = 0, n = 5; i < n; i++) {
        result += i
    }
    assert 10 == result

    result = 0;
    int i;
    int j;
    for (i = 1, j = 5; i < j; i++, j--) {
        result += i;
        result += j;
    }
    assert 12 == result
}
test();
