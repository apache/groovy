package org.codehaus.groovy.runtime.iterator

class IteratorsTest extends GroovyTestCase {

    void testIterate() {
        use(Iterators) {
            def iter

            assertEquals 6, (1..6).iterate().collect().size()

            iter = [1, 2, 3, 4, 5].iterate()
            assertTrue iter instanceof Iterator

            iter = [x: 1, y: 2, z: 3].iterate()
            assertTrue iter instanceof Iterator

            3.times {
                assertTrue iter.hasNext()
                iter.next()
            }

            assertFalse iter.hasNext()
            shouldFail(NoSuchElementException) {
                iter.next()
            }
        }
    }

    void testUnique() {
        use(Iterators) {
            assertEquals([1, 2, 3, 4], [1, 1, 2, 3, 4, 2, 2, 4, 1].unique())
        }
    }

    void testFilter() {
        use(Iterators) {
            def filteredOut = []
            assertEquals([1, 2], [1, 2, 3, 5, 6].withFilter {it <= 2}.collect())

            assertEquals([1, 2, 3, 4, 5, 6], (1..6).withFilter {a, b -> a + b}.collect())

            assertEquals([3, 11, 15], (1..10).withFilter {a, b ->
                def res = a + b
                if (res == 7)
                    continueIteration()
                put(res)
                if (res > 15)
                    breakIterationImmidiately()
            }.collect())
        }
    }

    void testTransform() {
        use(Iterators) {
            assertEquals([1, 2, 2, 3, 3, 3], [1, 2, 3].withTransform {item ->
                item.times {
                    put item
                }
            }.collect())

            assertEquals(["i1", "i6"], [1, 12, 13, 15, 6].withTransform {"i${it}"}.withFilter {it.size() == 2}.collect {it})

            assertEquals(["j1"], [1, 12, 13, 15, 6].withFilter {it < 10}.withTransform {"J$it".toLowerCase()}.withFilter {it.indexOf("1") != -1}.collect())

            assertEquals([6, 7, 9], [1, 5, 3, 4, 3, 6].withTransform {a, b -> a + b}.collect())
        }
    }

    void testCompose() {
        use(Iterators) {
            def iter = [1, 2, 3].composeWith([4, 5]).composeInReverseOrger([6, 7])
            assertEquals(6, iter.next())
            assertEquals(7, iter.next())
            (1..5).each {
                assertEquals(it, iter.next())
            }
        }
    }

    void testMerge() {
        use(Iterators) {
            def iter

            iter = [1, 3, 5].mergeWith([2, 4, 6, 8, 10, 12])
            (1..6).each {
                assertEquals(it, iter.next())
            }
            assertEquals(8, iter.next())
            assertEquals(10, iter.next())
            assertEquals(12, iter.next())

            iter = [2, 4, 6, 8, 10, 12].mergeInReverseOrger([1, 3, 5])
            (1..6).each {
                assertEquals(it, iter.next())
            }
            assertEquals(8, iter.next())
            assertEquals(10, iter.next())
            assertEquals(12, iter.next())
        }
    }

    void testMaps() {
        use(Iterators) {
            assertEquals(["x", "y", "z"], [x: 1, y: 2, z: 3].withKeys().collect {it})

            assertEquals([1, 2, 3], [x: 1, y: 2, z: 3].withValues().collect {it})

            assertEquals([["x", 1], ["y", 2], ["z", 3], [5, 6]], [x: 1, y: 2, z: 3].composeWith([5, 6]).withTransform {key, value ->
                put key
                put value
            }.groupBy(2).collect {it})

            assertEquals([["x", 1], ["y", 2], ["z", 3]], [x: 1, y: 2, z: 3].withTransform {key, value ->
                        [key, value]
            }.collect {it})
        }
    }

    void testGroupBy() {
        use(Iterators) {
            assertEquals([[1, 2], [3, 4], [5]], [1, 2, 3, 4, 5].groupBy(2).collect())
        }
    }

    void testBreak() {
        use(Iterators) {
            assertEquals([1, 3, 4], [2, 4, 6, 8, 9, 12].withTransform {
                if (it == 4)
                    continueIteration()
                if (it == 9)
                    breakIteration()
                it / 2
            }.collect())

            assertEquals([1, 2, 3, 4], [2, 4, 6, 8, 9, 12].withTransform {
                put it / 2
                if (it == 8)
                    breakIteration()
            }.collect())

            assertEquals([1, 2, 3], [2, 4, 6, 8, 9, 12].withTransform {
                put it / 2
                if (it == 8)
                    breakIterationImmidiately()
            }.collect())
        }
    }

    void testMath() {
        use(IteratorsOperations, Iterators) {
            assertEquals([2, 4, 6, 8], (2 * [1, 2, 3, 4].iterator()).collect())
            assertEquals([3, 4, 5, 6], ([1, 2, 3, 4].iterator() + 2).collect())
            assertEquals([1, 2, 3, 4], ((2 * [1, 2, 3, 4].iterator()) / 2).collect())
            assertEquals([12, 6, 4, 3], (12 / [1, 2, 3, 4].iterator()).collect())
        }
    }

    void testSpreadOp() {
        use(IteratorsOperations, Iterators) {
            assertEquals([2, 4, 6, 8], [1, 2, 3, 4].iterator()*.multiply(2).collect())
        }
    }

}
