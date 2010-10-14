package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */
public class NullValueTest extends GroovyTestCase {
    public void testEquals() throws Exception {
        assert new Memoize.MemoizeNullValue() == new Memoize.MemoizeNullValue()
    }

    public void testHashCode() throws Exception {
        assert new Memoize.MemoizeNullValue().hashCode() == new Memoize.MemoizeNullValue().hashCode()
    }
}
