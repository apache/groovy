import groovy.transform.CompileStatic

@CompileStatic
def a() {
    int i = 0;
    int result = 0;
   do {
        result += 2
    } while(i++ < 2)
        result += 3

    return result;
}
assert 9 == a()