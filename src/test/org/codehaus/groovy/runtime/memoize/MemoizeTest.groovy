package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */

public class MemoizeTest extends AbstractMemoizeTestCase {

    Closure buildMemoizeClosure(Closure cl) {
        cl.memoize()
    }

    void testMemoizeWithInject() {
        int maxExecutionCount = 0
        Closure max = { int a, int b ->
            maxExecutionCount++
            Math.max(a, b)
        }.memoize()
        int minExecutionCount = 0
        Closure min = { int a, int b ->
            minExecutionCount++
            Math.min(a, b)
        }.memoize()
        100.times {
            max.call(max.call(1, 2), 3)
        }
        100.times {
            [1, 2, 3].inject(min)
        }
        assert maxExecutionCount == 2
        assert minExecutionCount == 2
    }
}
