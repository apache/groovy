/**
 * A local variable declaration can also appear in the header of a for statement.
 * In this case it is executed in the same manner as if it were part of a local variable declaration statement.
 */
class Foo {
    def run() {
        //for (int i = 0;i < 10;i++) {} //@pass
        //for (i = 0;i < 10; i++) {} //@fail
    }
}
