
class A {
    def field
    static staticField
    def a() {
        field + 5
        staticField - 10
    }

    def b() {
        def local = 5
        def uninitialized
        local + 3
    }
}
