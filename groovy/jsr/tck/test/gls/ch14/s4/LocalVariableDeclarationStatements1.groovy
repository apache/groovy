/**
 * A local variable declaration statement declares one or more local variable names.
 */
class Foo {
    def run() {
        int a
        int b = 1
        int c,d,e
        int f,g = 2
        def h = "h"
        //def def p//@fail:parse
    }
}
