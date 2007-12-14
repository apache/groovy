package org.codehaus.groovy.runtime.iterator

class ConcurrencyTest extends GroovyTestCase {

    void testMax() {
        use(Iterators, Concurrency) {
            TransformIterator iterator = [1, 6, 2, 5, 4, 3].withTransform {
                defineLocal("max", Integer.MIN_VALUE)
                if (max < it) {
                    max = it
                }
                it
            }
            iterator.collect()
            assertEquals(6, iterator.max)

            Object[] list = (0..100).toList() as Object[]
            def iter = withProducerThread(repeatable: true, poolSize: 10) {
                defineLocal("index", threadIndexInPool)
                defineLocal("max", Integer.MIN_VALUE)

                Object cur = list[index]
                put cur

                if (cur > max)
                    max = cur

                index = index + 1
                if (index >= list.length)
                    index = 0
                if (index == threadIndexInPool)
                    breakIteration()

                sleep((int) (10 * Math.random()))
            }
            println iter.threadVars
        }
    }

    void testConcurrentTransform() {
        use(Iterators, Concurrency) {

            assertEquals((1..100).toList(), (1..100).withConcurrentTransform(poolSize: 10) {
                assertEquals 10, poolSize
                println "$it, ${Thread.currentThread()}"
                sleep((long) (100 * Math.random()))
                it
            }.withTransform {
                defineLocal "prev", null

                if (prev != null) {
                    if (prev + 1 != it)
                        println "$prev + 1 != $it"
                }
                prev = it
            }.collect().sort())

            println "DONE!"

        }
    }

    void testConcurrently() {
        use(Iterators, Concurrency) {
            assertEquals((1..100).withFilter {it % 10 != 0}.withFilter {it % 20 != 19}.collect(),
                    (1..100).withConcurrentTransform(poolSize: 5) {a, b ->
                        println "$a, $b, ${Thread.currentThread()}"
                        if (a % 20 == 19) {
                            println "$a, $b, skipped"
                            continueIteration()
                        }

                        put a
                        if (b % 10 != 0)
                            put b
                        else
                            println "$b skipped"
                        sleep((int) (100 * Math.random()))
                    }.withTransform(prev: null) {
                        println "-> $it"
                        if (prev != null) {
                            if (prev + 1 != it)
                                println "$prev + 1 != $it"
                        }
                        prev = it
                    }.collect().sort())
        }
    }

    void testProducerThread() {
        use(Iterators, Concurrency) {
            assertEquals((1..100).withFilter {it % 2 == 1}.toList(), withProducerThread(repeatable: true, index: 0) {
                defineLocal("index", 0)

                println index
                index = index + 1
                if (index == 100)
                    breakIteration()
                if (index % 2 == 0)
                    continueIteration();
                put index
                sleep((int) (10 * Math.random()))
            }.collect())

            println "Done!"

            assertEquals((0..100).toList(), withProducerThread(repeatable: true, poolSize: 5) {
                defineLocal("index", threadIndexInPool)

                println index
                if (index > 100)
                    breakIteration()
                put index
                index = index + poolSize
                sleep((int) (10 * Math.random()))
            }.collect().sort())
        }
    }

    void testTests() {
        use(Iterators, Concurrency) {
            withProducerThread {
                def ant = new AntBuilder()
                def scanner = ant.fileScanner {
                    fileset(dir: "src/test", includes: "**/*Test.groovy")
                    fileset(dir: "src/test", includes: "**/*Bug.groovy")
                }
                for (f in scanner) {
                    put f.getAbsolutePath()
                }
            }.withConcurrentTransform(poolSize: 4, maxCapacity: 6) {file ->
                defineLocal("loader", null)

                if (!loader)
                    loader = new GroovyClassLoader()

                println "Compiling $file on thread $threadIndexInPool ${Thread.currentThread()}"
                loader.parseClass(new File(file))
            }.withFilter {type ->
                junit.framework.Test.isAssignableFrom(type) && !type.name.endsWith("ConcurrencyTest")
            }.withConcurrentTransform(poolSize: 4, maxCapacity: 6) {type ->
                println "Running ${type.name} on thread: $threadIndexInPool  ${Thread.currentThread()}"
                junit.textui.TestRunner.run(type)
            }.each {}
        }
    }
}
