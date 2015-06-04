package org.codehaus.groovy.util

import org.junit.Before
import org.junit.Test

class AbstractConcurrentMapSegmentTest {
    private static final Integer INITIAL_SEGMENT_SIZE = 100
    private static final Integer SEGMENT_THRESHOLD = 0.75f * INITIAL_SEGMENT_SIZE
    TestSegment segment
    List<TestEntry> entries = []
    int rehashCount = 0

    @Before
    public void setUp() throws Exception {
        segment = new TestSegment(INITIAL_SEGMENT_SIZE)
    }

    @Test
    public void testSegmentWillNotRehash() {
        whenIAddElements(50)
        thenRehashHappenedTimes(0)
        thenSegmentExpands(false)
    }

    @Test
    public void testSegmentWillNotRehashEdgeCase() {
        whenIAddElements(SEGMENT_THRESHOLD + 1)
        thenRehashHappenedTimes(0)
        thenSegmentExpands(false)
    }

    @Test
    public void testSegmentWillRehashAndExpand() {
        whenIAddElements(SEGMENT_THRESHOLD + 2)
        thenRehashHappenedTimes(1)
        thenSegmentExpands(true)
    }

    @Test
    public void testSegmentWillRehashAndExpandManyTimes() {
        int elementCount = (SEGMENT_THRESHOLD + 1 ) * 6
        whenIAddElements(elementCount)
        //456 elements fit into segment of size 800, which is 100 * 2 * 2 * 2
        thenSegmentSizeIs(INITIAL_SEGMENT_SIZE * 2 * 2 * 2)
        thenRehashHappenedTimes(3)
    }

    @Test
    public void testSegmentWillRehashWithNoExpansion() {
        whenIAddElements(SEGMENT_THRESHOLD)
        whenISetElementsAsInvalid(50)
        whenIAddElements(50)
        thenRehashHappenedTimes(1)
        thenSegmentExpands(false)
    }

    @Test
    public void testSegmentWillRehashAndEventuallyExpand() {
        whenIAddElements(SEGMENT_THRESHOLD)

        // 1-st rehash
        whenISetElementsAsInvalid(50)
        whenIAddElements(50)
        thenSegmentExpands(false)

        // 2-nd rehash
        whenISetElementsAsInvalid(30)
        whenIAddElements(30)
        thenSegmentExpands(false)

        // 3-nd rehash
        whenISetElementsAsInvalid(20)
        whenIAddElements(20)
        thenSegmentExpands(false)

        // 4-th rehash with none invalid => expansion: segment * 2
        whenIAddElements(SEGMENT_THRESHOLD)

        thenRehashHappenedTimes(4)
        thenSegmentSizeIs(INITIAL_SEGMENT_SIZE * 2)
    }

    private void whenIAddElements(int count) {
        count.times {
            String key = "k:${System.currentTimeMillis()}-${it}"
            segment.put(key, key.hashCode(), "v${it}")
        }
    }

    private void whenISetElementsAsInvalid(int count) {
        List<TestEntry> validEntires = entries.findAll { it.isValid() }
        count.times {
            validEntires.get(it).setValid(false)
        }
    }

    private void thenRehashHappenedTimes(int expectedRehashCount) {
        assert rehashCount == expectedRehashCount
    }

    private void thenSegmentSizeIs(int expectedSize) {
        assert segment.table.length == expectedSize
    }

    private void thenSegmentExpands(boolean truth) {
        assert segment.table.length > INITIAL_SEGMENT_SIZE == truth
    }

    class TestSegment extends AbstractConcurrentMap.Segment {

        protected TestSegment(int initialCapacity) {
            super(initialCapacity)
        }

        @Override
        protected AbstractConcurrentMap.Entry createEntry(Object key, int hash, Object value) {
            TestEntry entry = new TestEntry(key, hash, value)
            entries.add(entry)
            return entry
        }

        @Override
        def void rehash() {
            rehashCount++
            super.rehash()
        }
    }
}

class TestEntry implements AbstractConcurrentMap.Entry {
    Object key
    Object value
    int hash
    boolean valid = true;

    public TestEntry(Object key, int hash, Object value) {
        this.key = key
        this.hash = hash
        this.value = value
    }

    @Override
    boolean isEqual(Object key, int hash) {
        return hash == this.hash && key.equals(this.key)
    }

    @Override
    Object getValue() {
        return value
    }

    @Override
    void setValue(Object value) {
        this.value = value
    }

    @Override
    int getHash() {
        return hash
    }

    @Override
    boolean isValid() {
        return valid
    }

    public void setValid(boolean valid) {
        this.valid = valid
    }
}