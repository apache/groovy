package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */
public class MemoizeAtLeastTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoizeAtLeast(100)
    }

    public void testZeroCache() {
        def flag = false
        Closure cl = {
            flag = true
            it * 2
        }
        Closure mem = cl.memoizeAtLeast(0)
        [1, 2, 3, 4, 5, 6].each {mem(it)}
        assert flag
    }
}
