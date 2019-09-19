import groovy.test.GroovyTestCase

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
class DeclarativeConcurrencyASTTransformsTest extends GroovyTestCase {
    void testSynchronizedASTTransform() {
        assertScript '''
// tag::example_synchronized[]
import groovy.transform.Synchronized

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Counter {
    int cpt
    @Synchronized
    int incrementAndGet() {
        cpt++
    }
    int get() {
        cpt
    }
}
// end::example_synchronized[]
/*
// tag::example_synchronized_equiv[]
class Counter {
    int cpt
    private final Object $lock = new Object()

    int incrementAndGet() {
        synchronized($lock) {
            cpt++
        }
    }
    int get() {
        cpt
    }

}
// end::example_synchronized_equiv[]
*/
def c = new Counter()
def pool = Executors.newFixedThreadPool(4)
1000.times {
    pool.submit { c.incrementAndGet() }
}
pool.shutdown()
pool.awaitTermination(5, TimeUnit.SECONDS)
assert c.get() == 1000
'''
        assertScript '''
// tag::example_synchronized_customlock[]
import groovy.transform.Synchronized

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Counter {
    int cpt
    private final Object myLock = new Object()
    
    @Synchronized('myLock')
    int incrementAndGet() {
        cpt++
    }
    int get() {
        cpt
    }
}
// end::example_synchronized_customlock[]
def c = new Counter()
def pool = Executors.newFixedThreadPool(4)
1000.times {
    pool.submit { c.incrementAndGet() }
}
pool.shutdown()
pool.awaitTermination(5, TimeUnit.SECONDS)
assert c.get() == 1000
'''
    }

    void testWithReadLockASTTransform() {
        assertScript '''
// tag::example_rwlock[]
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock

class Counters {
    public final Map<String,Integer> map = [:].withDefault { 0 }

    @WithReadLock
    int get(String id) {
        map.get(id)
    }

    @WithWriteLock
    void add(String id, int num) {
        Thread.sleep(200) // emulate long computation
        map.put(id, map.get(id)+num)
    }
}
// end::example_rwlock[]

/*
// tag::example_rwlock_equiv[]
import groovy.transform.WithReadLock as WithReadLock
import groovy.transform.WithWriteLock as WithWriteLock

public class Counters {

    private final Map<String, Integer> map
    private final java.util.concurrent.locks.ReentrantReadWriteLock $reentrantlock

    public int get(java.lang.String id) {
        $reentrantlock.readLock().lock()
        try {
            map.get(id)
        }
        finally {
            $reentrantlock.readLock().unlock()
        }
    }

    public void add(java.lang.String id, int num) {
        $reentrantlock.writeLock().lock()
        try {
            java.lang.Thread.sleep(200)
            map.put(id, map.get(id) + num )
        }
        finally {
            $reentrantlock.writeLock().unlock()
        }
    }
}
// end::example_rwlock_equiv[]
*/

def counters = new Counters()
assert counters.get('a') == 0
assert counters.get('b') == 0

10.times { cpt ->
    Thread.start { counters.add('a', 1) }
    def t = Thread.start {
        Thread.sleep(20)
        assert counters.get('a') == cpt+1
    }
    t.join(250)
}
'''

        assertScript '''
// tag::example_rwlock_alter[]
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock

import java.util.concurrent.locks.ReentrantReadWriteLock

class Counters {
    public final Map<String,Integer> map = [:].withDefault { 0 }
    private final ReentrantReadWriteLock customLock = new ReentrantReadWriteLock()

    @WithReadLock('customLock')
    int get(String id) {
        map.get(id)
    }

    @WithWriteLock('customLock')
    void add(String id, int num) {
        Thread.sleep(200) // emulate long computation
        map.put(id, map.get(id)+num)
    }
}
// end::example_rwlock_alter[]

def counters = new Counters()
assert counters.get('a') == 0
assert counters.get('b') == 0

10.times { cpt ->
    Thread.start { counters.add('a', 1) }
    def t = Thread.start {
        Thread.sleep(20)
        assert counters.get('a') == cpt+1
    }
    t.join(250)
}
'''
    }
}
