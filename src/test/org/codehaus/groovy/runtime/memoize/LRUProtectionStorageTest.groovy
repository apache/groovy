package org.codehaus.groovy.runtime.memoize

/**
 * @author Vaclav Pech
 */
public class LRUProtectionStorageTest extends GroovyTestCase {
    public void testLRUStrategyWithOneElement() {
        def storage = new LRUProtectionStorage(1)
        assert storage.size() == 0
        storage['key1'] = 1
        assert storage.size() == 1
        storage['key2'] = 2
        assert storage.size() == 1
        assertEquals 2, storage['key2']
        storage['key1'] = 1
        assert storage.size() == 1
        assertNull storage['key2']
        assertEquals 1, storage['key1']

    }

    public void testLRUStrategy() {
        def storage = new LRUProtectionStorage(3)
        assert storage.size() == 0
        storage['key1'] = 1
        assert storage.size() == 1
        storage['key2'] = 2
        storage['key3'] = 3
        assert storage.size() == 3
        assertEquals 1, storage['key1']
        assertEquals 2, storage['key2']
        assertEquals 3, storage['key3']
        storage['key4'] = 4
        assert storage.size() == 3
        assertNull storage['key1']
        assertEquals 2, storage['key2']
        assertEquals 3, storage['key3']
        assertEquals 4, storage['key4']
        storage['key4']
        storage['key2']
        storage['key5'] = 5
        assert storage.size() == 3
        assertNull storage['key3']
        assertEquals 2, storage['key2']
        assertEquals 4, storage['key4']
        assertEquals 5, storage['key5']
    }

    public void testTouch() {
        def storage = new LRUProtectionStorage(3)
        storage['key1'] = 1
        storage['key2'] = 2
        storage['key3'] = 3
        storage.touch('key1', 11)
        storage['key4'] = 4
        assert storage.size() == 3
        assertEquals 11, storage['key1']
        assertEquals 4, storage['key4']
        assertEquals 3, storage['key3']
        assertNull storage['key2']
    }
}
