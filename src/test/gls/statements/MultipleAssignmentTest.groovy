package gls.statements

import gls.CompilableTestSupport

class MultipleAssignmentTest extends CompilableTestSupport {

    void testList() {
        def list = [1, 2]
        def a, b

        (a, b) = list
        assert a == 1
        assert b == 2

        (a, b) = [3, 4]
        assert a == 3
        assert b == 4
    }

    void testArray() {
        def array = [1, 2] as int[]
        def a, b

        (a, b) = array
        assert a == 1
        assert b == 2
    }

    def foo() {[1, 2]}

    void testMethod() {
        def a, b

        (a, b) = foo()
        assert a == 1
        assert b == 2
    }

    void testMethodOverflow() {
        def a, b = 3

        (a) = foo()
        assert a == 1
        assert b == 3
    }

    void testMethodUnderflow() {
        def a, b, c = 4

        (a, b, c) = foo()
        assert a == 1
        assert b == 2
        assert c == null
    }
}