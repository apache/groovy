/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy

import groovy.test.GroovyTestCase

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.LinkedBlockingQueue
import java.lang.ref.WeakReference

class ActorTest extends GroovyTestCase {
    void testSync () {
        new FibCalculator().calcFibSync(15)
    }

    void testAsync () {
        ReentrantLock.metaClass {
            withLock { Closure c ->
                lock()
                try {
                    c.call ()
                }
                finally {
                    unlock()
                }
            }
        }

        WorkerThread.metaClass.mixin ReentrantLock
        Thread.metaClass.mixin WorkerThread

        WorkerThread.startPool(15)

        Thread thread = Thread.currentThread()
        thread.name = "Main App Thread"
        thread.registerWorker ()
        new FibCalculator().calcFib(18)

        Object.metaClass = null
        ReentrantLock.metaClass = null
        WorkerThread.metaClass = null
    }
}

class WorkerThread {
    static {
        Object.metaClass {
            longOperation{ Closure c ->
                new LongOperation (c)
            }

            send{ Closure c ->
                new LongOperation (c).send ()
            }

            post{ Closure c ->
                new LongOperation (c).post ()
            }
        }
    }

    def list = new LinkedList ()

    private static final ReentrantLock globalLock = new ReentrantLock()
    private static final LinkedBlockingQueue globalQueue = new LinkedBlockingQueue ()
    private static final ArrayList allWorkers = new ArrayList ()
    private static final Random r = new Random();

    WorkerThread () {
    }

    void registerWorker() {
        globalLock.withLock {
            WeakReference ref = new WeakReference(delegate)
            for (int i = 0; i != allWorkers.size(); ++i) {
                WeakReference or = allWorkers[i]
                if (or == null || or.get() == null) {
                    allWorkers[i] = ref
                    return ref
                }
            }

            allWorkers << ref
        }
    }

    void schedule (Closure action) {
        withLock {
            list.addFirst action
        }
    }

    void post (Closure action) {
        globalQueue.offer action
    }

    Object[] send (List actions) {
        int len = actions.size()
        AtomicInteger counter = new AtomicInteger()
        def results = new Object[len]
        actions.eachWithIndex {Closure action, int index ->
            schedule {
                Object res = action.call()
                results[index] = res
                counter.incrementAndGet()
            }
        }

        while (counter.get() != len) {
            execute()
        }
        return results
    }

    void execute () {
        Closure action = nextTask ()

        if (action)
          action ()
    }

    Closure nextTask() {
        Closure action = withLock {
            return list.isEmpty() ? null : (Closure)list.removeFirst()
        }

        if (action != null)
          return action

        action = globalQueue.poll()
        if (action != null)
          return action

        globalLock.withLock {
           int len = allWorkers.size()
           if (len == 1)
             return null

           int from = r.nextInt(len)
           for (int i = from+1; i != from; ++i) {
               if (i == len) {
                 i = 0
                 if (from == 0) {
                     return null
                 }
               }

               def worker = allWorkers[i]
               if (worker) {
                   worker = worker.get ()
                   if (!worker) {
                     allWorkers [i] = null
                     continue
                   }

                   action = worker.withLock {
                       if (!worker.list.isEmpty()) {
                            worker.list.removeLast ()
                       }
                   }

                   if (action)
                     return action
               }
           }

           return null
        }
    }

    static void startPool(int count) {
        for (int i = 0; i != count; ++i) {
            def n = i
            Thread.start {
                Thread thread = Thread.currentThread()
                thread.registerWorker ()
                thread.name = "WorkerPool-" + n
                try {
                    while(true) {
                        thread.execute ()
                    }
                }
                finally {
                    return 
                }
            }
        }
    }
}

class FibCalculator {
    def calcFib (value) {
        def a = calcFibImpl (value)
        String calc = Thread.currentThread().name
        post {
            println "fib(${value})=$a $calc ${Thread.currentThread().name}"
        }
        Thread.sleep(1)
        a
    }

    def calcFibImpl (value) {
        if (value <= 0)
           0
        else {
           if (value <= 2) {
               1
           }
           else
               longOperation {
                   calcFib (value-1)
               }
               .and {
                   calcFib (value-2)
               }
               .send { Object [] it ->
                   it [0] + it [1]
               }
        }
    }

    def calcFibSync (value) {
        def a = calcFibSyncImpl (value)
        String calc = Thread.currentThread().name
        println "fib(${value})=$a $calc ${Thread.currentThread().name}"
        Thread.sleep(1)
        a
    }


    def calcFibSyncImpl (value) {
        if (value <= 0)
           0
        else {
           if (value <= 2) {
               1
           }
           else
             calcFibSync(value-1) + calcFibSync(value-2)
        }
    }
}

class LongOperation {
    def actions = []

    LongOperation (Closure action) {
        actions << action
    }

    LongOperation and (Closure c) {
        actions << c
        this
    }

    def send (Closure op) {
        op.call (send())
    }

    Object[] send () {
        Thread.currentThread().send (actions)
    }

    void post () {
        actions.each { Closure action ->
           Thread.currentThread().post(action)
        }
    }
}
