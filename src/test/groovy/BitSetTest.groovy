
package groovy

class BitSetTest extends GroovyTestCase{

    void testSubscriptOperator() {
        def bitSet = new BitSet()

        bitSet[2] = true
        bitSet[3] = true

        assertFalse 'index 0 should have been false', bitSet[0]
        assertFalse 'index 1 should have been false', bitSet[1]
        assertTrue 'index 2 should have been true', bitSet[2]
        assertTrue 'index 3 should have been true', bitSet[3]
        assertFalse 'index 4 should have been false', bitSet[4]
    }


    void testSubscriptAssignmentWithRange() {
        def bitSet = new BitSet()

        bitSet[2..4] = true

        assertFalse 'index 0 should have been false', bitSet[0]
        assertFalse 'index 1 should have been false', bitSet[1]
        assertTrue 'index 2 should have been true', bitSet[2]
        assertTrue 'index 3 should have been true', bitSet[3]
        assertTrue 'index 4 should have been true', bitSet[4]
        assertFalse 'index 5 should have been false', bitSet[5]
    }

    void testSubscriptAssignmentWithReverseRange() {
        def bitSet = new BitSet()

        bitSet[4..2] = true

        assertFalse 'index 0 should have been false', bitSet[0]
        assertFalse 'index 1 should have been false', bitSet[1]
        assertTrue 'index 2 should have been true', bitSet[2]
        assertTrue 'index 3 should have been true', bitSet[3]
        assertTrue 'index 4 should have been true', bitSet[4]
        assertFalse 'index 5 should have been false', bitSet[5]
    }

    void testSubscriptAccessWithRange() {
        def bitSet = new BitSet()

        bitSet[7] = true
        bitSet[11] = true

        def subSet = bitSet[5..11]

        assertTrue 'subSet should have been a BitSet', subSet instanceof BitSet

        assertNotSame 'subSet should not have been the same object', bitSet, subSet

        // the last true bit should be at index 6
        assertEquals 'result had wrong logical size', 7, subSet.length()

        assertFalse 'index 0 should have been false', subSet[0]
        assertFalse 'index 1 should have been false', subSet[1]
        assertTrue 'index 2 should have been true', subSet[2]
        assertFalse 'index 3 should have been false', subSet[3]
        assertFalse 'index 4 should have been false', subSet[4]
        assertFalse 'index 5 should have been false', subSet[5]
        assertTrue 'index 6 should have been true', subSet[6]
    }

    void testSubscriptAccessWithReverseRange() {
        def bitSet = new BitSet()

        bitSet[3] = true
        bitSet[4] = true

        def subSet = bitSet[8..2]

        assertTrue 'subSet should have been a BitSet', subSet instanceof BitSet

        assertNotSame 'subSet should not have been the same object', bitSet, subSet

        // the last true bit should be at index 6
        assertEquals 'result had wrong logical size', 6, subSet.length()

        assertFalse 'index 0 should have been false', subSet[0]
        assertFalse 'index 1 should have been false', subSet[1]
        assertFalse 'index 2 should have been false', subSet[2]
        assertFalse 'index 3 should have been false', subSet[3]
        assertTrue 'index 4 should have been true', subSet[4]
        assertTrue 'index 5 should have been true', subSet[5]
        assertFalse 'index 6 should have been false', subSet[6]
    }
}