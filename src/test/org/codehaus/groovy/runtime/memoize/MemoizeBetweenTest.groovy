package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */

public class MemoizeBetweenTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoizeBetween(50, 100)
    }

    public void testParameters() {
        Closure cl = {}
        shouldFail(IllegalArgumentException) {
            cl.memoizeBetween(1, 0)
        }
        shouldFail(IllegalArgumentException) {
            cl.memoizeBetween(-2, -1)
        }
        shouldFail(IllegalArgumentException) {
            cl.memoizeBetween(-1, -1)
        }
    }

    public void testZeroCache() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoizeBetween(0, 0)
        [1, 2, 3, 4, 5, 6].each {mem(it)}
        assert flag
        flag = false
        assert 12 == mem(6)
        assert flag
    }

    public void testLRUCache() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoizeBetween(3, 3)
        [1, 2, 3, 4, 5, 6].each {mem(it)}
        assert flag
        flag = false
        assert 8 == mem(4)
        assert 10 == mem(5)
        assert 12 == mem(6)
        assert !flag
        assert 6 == mem(3)
        assert flag
        flag = false
        assert 10 == mem(5)
        assert 12 == mem(6)
        assert 6 == mem(3)
        assert !flag
        assert 8 == mem(4)
        assert flag

        flag = false
        assert 10 == mem(5)
        assert flag
    }
}
