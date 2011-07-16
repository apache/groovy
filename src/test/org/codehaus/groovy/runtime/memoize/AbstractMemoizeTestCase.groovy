package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */
public abstract class AbstractMemoizeTestCase extends GroovyTestCase {

    volatile int counter = 0

    AbstractMemoizeTestCase() {
        super()
    }

    public void testCorrectness() {
        Closure cl = { it * 2 }
        Closure mem = buildMemoizeClosure(cl)
        assert 10 == mem(5)
        assert 4 == mem(2)
    }

    abstract Closure buildMemoizeClosure(Closure cl)

    public void testNullParams() {
        Closure cl = { 2 }
        Closure mem = cl.memoize()
        assert 2 == mem(5)
        assert 2 == mem(2)
        assert 2 == mem(null)
    }

    public void testNullResult() {
        Closure cl = {counter++; if (it == 5) return null else return 2}
        Closure mem = cl.memoize()
        assert counter == 0
        assert null == mem(5)
        assert counter == 1
        assert 2 == mem(2)
        assert counter == 2
        assert null == mem(5)
        assert 2 == mem(2)
        assert counter == 2
    }

    public void testNoParams() {
        Closure cl = {-> 2 }
        Closure mem = cl.memoize()
        assert 2 == mem()
        assert 2 == mem()
    }

    public void testCaching() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoize()
        assert 10 == mem(5)
        assert flag
        flag = false
        assert 4 == mem(2)
        assert flag
        flag = false

        assert 4 == mem(2)
        assert 4 == mem(2)
        assert 10 == mem(5)
        assert !flag

        assert 6 == mem(3)
        assert flag
        flag = false
        assert 6 == mem(3)
        assert !flag
    }

    public void testComplexParameter() {
        def callFlag = []

        Closure cl = { a, b, c ->
            callFlag << true
            c
        }
        Closure mem = cl.memoize()
        checkParams(mem, callFlag, [1, 2, 3], 3)
        checkParams(mem, callFlag, [1, 2, 4], 4)
        checkParams(mem, callFlag, [1, [2], 4], 4)
        checkParams(mem, callFlag, [[1: '1'], [2], 4], 4)
        checkParams(mem, callFlag, [[1, 2], 2, 4], 4)
        checkParams(mem, callFlag, [[1, 2], null, 4], 4)
        checkParams(mem, callFlag, [null, null, 4], 4)
        checkParams(mem, callFlag, [null, null, null], null)
        checkParams(mem, callFlag, [null, [null], null], null)
    }

    def checkParams(Closure mem, callFlag, args, desiredResult) {
        assertEquals desiredResult, mem(* args)
        assert !callFlag.empty
        callFlag.clear()
        assert desiredResult == mem(* args)
        assert callFlag.empty
    }
}
