import groovy.transform.CompileStatic

@CompileStatic
def testCS() {
    int result = 0
    for (int i = 0, n = 5; i < n; i++) {
        result += i
    }

    assert 10 == result
}
testCS();

def test() {
    int result = 0
    for (int i = 0, n = 5; i < n; i++) {
        result += i
    }

    assert 10 == result
}
test();
