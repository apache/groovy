package org.codehaus.groovy.runtime.memoize

import java.lang.ref.ReferenceQueue
import java.lang.ref.SoftReference

/**
 * @author Rafael Luque
 */
public class CacheCleanupCollectedSoftReferencesTest extends GroovyTestCase {   


    // TODO re-enable this test once CI server can safely handle it
    public void manual_testCollectedCacheValuesAreEnqueued() {

        Closure cl = { 
            new Integer(it + 1) 
        }

        UnlimitedConcurrentCache cache = new UnlimitedConcurrentCache()
        Closure memoizedClosure = Memoize.buildSoftReferenceMemoizeFunction(0, cache, cl)

        assert cache.cache.size() == 0
        memoizedClosure.call(1)
        assert cache.cache.size() == 1

        forceSoftReferencesRecollection()

        def softReference = cache.get([1])
        checkSoftReferenceAreSoftlyReachable(softReference)
        checkCollectedSoftReferenceAreEnqueued(softReference)

    }


    // TODO re-enable this test once CI server can safely handle it
    public void manual_testCollectedCacheValuesAreRemovedFromCache() {

        Closure cl = { 
            new Integer(it + 1) 
        }

        UnlimitedConcurrentCache cache = new UnlimitedConcurrentCache()
        Closure memoizedClosure = Memoize.buildSoftReferenceMemoizeFunction(0, cache, cl)

        assert cache.cache.size() == 0
        memoizedClosure.call(1)
        assert cache.cache.size() == 1

        forceSoftReferencesRecollection()

        assert cache.cache.size() == 1

        // As there is not a cleanup thread polling the ReferenceQueue, 
        // a call() invocation is needed to fire the cleaning up of null references.
        memoizedClosure.call(2)

        assert cache.cache.size() == 1 : 'collected SoftReferences should be removed from cache'
    }


    private void checkSoftReferenceAreSoftlyReachable(softReference) {

        assert softReference.get() == null : 
            'cache values should be softly reachable and collected before an OOME' 

    }

    private void checkCollectedSoftReferenceAreEnqueued(softReference) {

        assert softReference.enqueued : 'collected cache values should be enqueued'

    }

    private void forceSoftReferencesRecollection() {

        try {
            int maxMemory = Runtime.getRuntime().maxMemory()
            Object[] fillingMemory = new Object[maxMemory]
        } catch (Throwable e) {
            // Ignore OOME
        }

    }


}
