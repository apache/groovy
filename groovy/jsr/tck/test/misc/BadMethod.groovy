/**
 * Various good and bad methods and constructors
 */
class Foo {

    static blah() {}  //@fail:parse looks like constructor with bad name

    static void blah() {} // OK
    private Foo() {
    }

    Foo(String x) {
    }


    def Foo(int x) {} //@fail:parse cannot use def with constructors
}
