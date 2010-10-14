package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */

public class MemoizeTest extends AbstractMemoizeTestCase {
    Closure buildMemoizeClosure(Closure cl) {
        cl.memoize()
    }
}
