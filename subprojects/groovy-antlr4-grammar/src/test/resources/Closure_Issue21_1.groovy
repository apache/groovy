
class A {
    private void emptyClosure() {
        def cl = {-> }
    }

    private void simpleClosure() {
        def cl = {->
            return it
        }
    }

    private void singleArgumentClosure() {
        def cl1 = { a->
            return a
        }

        def cl2 = { def a->
            return a
        }

        def cl3 = { int a->
            return a
        }
    }

    private void multyArgumentClosure() {
        def cl = { a, def b, int c->
            return a + b
        }
    }

    private Closure implicitClosure() {
        call({
            1
        })
        return {
            5
        }
    }
}
