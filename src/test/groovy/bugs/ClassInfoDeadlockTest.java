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
package groovy.bugs;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.reflection.ClassInfo;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * When running Groovy without -Dgroovy.use.classvalue the ClassInfo
 * instances are cached in a ManagedConcurrentMap.  New values are
 * computed on demand.  The problem is that both the ManagedConcurrentMap
 * and the GlobalClassSet share the same ReferenceQueue.
 * <p>
 * Assume there is an enqueued ClassInfo value (already GC'd) that is in
 * Segment2 of the ManagedConcurrentMap.  Now assume that Thread1 and Thread2
 * both request ClassInfo.getClassInfo(..) for two different classes that do
 * not currently exist in the cache.  Assume that based on hashing Thread1
 * gets a lock on Segment1 and Thread2 gets a lock on Segment2.  Assume that
 * Thread1 is the first to call computeValue which in turn calls
 * GlobalClassSet.add(..).  This call adds a new value to a ManagedLinkedList,
 * and since it's managed the add operation will process the ReferenceQueue.
 * So Thread1 will attempt to dequeue the ClassInfo attempt to remove it from
 * Segment2. Thread2 holds the lock for Segment2 and can't progress because
 * Thread1 holds the lock for the GlobalClassSet, so deadlock occurs.
 * <p>
 * NOTE: This test is just for demonstration purposes and should not be
 * included.
 * <p>
 * NOTE: Since this issue depends on a timing issue several iterations are
 * run in order to attempt to trigger the deadlock.
 * <p>
 * NOTE: See end of this file for a thread dump captured from this test.
 *
 */
public class ClassInfoDeadlockTest { // IMPORTANT: DO NOT COMMIT THIS TEST, ONLY FOR DEMO PURPOSES

    private static final int DEADLOCK_TRIES = 50;
    private static final int THREAD_COUNT = 10;

    private final CountDownLatch startLatch = new CountDownLatch(1);
    private final CountDownLatch completeLatch = new CountDownLatch(THREAD_COUNT);
    private final GroovyClassLoader gcl = new GroovyClassLoader();
    private final AtomicInteger counter = new AtomicInteger();

    @Test
    public void testDeadlock() throws Exception {
        for (int i = 1; i <= DEADLOCK_TRIES; i++) {
            System.out.println("Test Number: " + i);
            generateGarbage();
            collectGarbage();
            attemptDeadlock();
        }
    }

    private void attemptDeadlock() throws Exception {
        for (int i = 0; i < THREAD_COUNT; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Class<?> newClass = createRandomClass();
                    // attempt to get all threads to request (create a new cache entry)
                    // at the same time
                    try {
                        startLatch.await();
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                    ClassInfo ci = ClassInfo.getClassInfo(newClass);
                    assertEquals(newClass, ci.getTheClass());
                    completeLatch.countDown();
                }
            };
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            t.start();
        }
        startLatch.countDown();
        completeLatch.await(10L, TimeUnit.SECONDS);
        if (completeLatch.getCount() != 0) {
            System.out.println("Possible deadlock, grab a thread dump now");
            completeLatch.await(1L, TimeUnit.MINUTES);
            if (completeLatch.getCount() == 0) {
                System.out.println("No deadlock, but took longer than expected");
            } else {
                fail("Deadlock occurred");
            }
        } else {
            System.out.println("No deadlock detected");
        }
    }

    // This may deadlock so run in a separate thread
    private void generateGarbage() throws Exception {
        System.out.println("Generating garbarge");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5000; i++) {
                    Class<?> c = createRandomClass();
                    ClassInfo ci = ClassInfo.getClassInfo(c);
                    assert ci.getTheClass() == c;
                }
            }
        };
        Thread t = new Thread(runnable, "GenerateGarbageThread");
        t.setDaemon(true);
        t.start();
        t.join(TimeUnit.SECONDS.toMillis(120L));
        if (t.isAlive()) {
            fail("Deadlock detected while generating garbage");
        }
    }

    private void collectGarbage() {
        System.out.println("Collecting garbarge");
        for (int i = 0; i < 10; i++) {
            System.gc();
        }
    }

    private Class<?> createRandomClass() {
        return gcl.parseClass("println foo-" + counter.incrementAndGet(), "Script1.groovy");
    }

}

/*
"Thread-10" #20 prio=5 os_prio=0 tid=0x00007f0a9849a000 nid=0x681b waiting for monitor entry [0x00007f0a75230000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at org.codehaus.groovy.reflection.ClassInfo$GlobalClassSet.add(ClassInfo.java:477)
	- waiting to lock <0x0000000084d48b90> (a org.codehaus.groovy.util.ManagedLinkedList)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:83)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:79)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$EntryWithValue.<init>(GroovyClassValuePreJava7.java:37)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:64)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:55)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:157)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-9" #19 prio=5 os_prio=0 tid=0x00007f0a98499800 nid=0x681a waiting on condition [0x00007f0a75331000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x0000000084d9bd28> (a org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at org.codehaus.groovy.util.LockableObject.lock(LockableObject.java:37)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:104)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-8" #18 prio=5 os_prio=0 tid=0x00007f0a984e7000 nid=0x6819 waiting for monitor entry [0x00007f0a75432000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at org.codehaus.groovy.reflection.ClassInfo$GlobalClassSet.add(ClassInfo.java:477)
	- waiting to lock <0x0000000084d48b90> (a org.codehaus.groovy.util.ManagedLinkedList)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:83)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:79)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$EntryWithValue.<init>(GroovyClassValuePreJava7.java:37)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:64)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:55)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:120)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-7" #17 prio=5 os_prio=0 tid=0x00007f0a9851b000 nid=0x6818 waiting on condition [0x00007f0a75533000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x0000000084d9bd28> (a org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at org.codehaus.groovy.util.LockableObject.lock(LockableObject.java:37)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:104)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-6" #16 prio=5 os_prio=0 tid=0x00007f0a984b3800 nid=0x6817 waiting for monitor entry [0x00007f0a75634000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at org.codehaus.groovy.reflection.ClassInfo$GlobalClassSet.add(ClassInfo.java:477)
	- waiting to lock <0x0000000084d48b90> (a org.codehaus.groovy.util.ManagedLinkedList)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:83)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:79)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$EntryWithValue.<init>(GroovyClassValuePreJava7.java:37)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:64)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:55)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:157)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-5" #15 prio=5 os_prio=0 tid=0x00007f0a9811f000 nid=0x6816 waiting for monitor entry [0x00007f0a75735000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at org.codehaus.groovy.reflection.ClassInfo$GlobalClassSet.add(ClassInfo.java:477)
	- waiting to lock <0x0000000084d48b90> (a org.codehaus.groovy.util.ManagedLinkedList)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:83)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:79)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$EntryWithValue.<init>(GroovyClassValuePreJava7.java:37)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:64)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:55)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:157)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-4" #14 prio=5 os_prio=0 tid=0x00007f0a98511000 nid=0x6815 waiting on condition [0x00007f0a75b33000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x0000000084d4c070> (a org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at org.codehaus.groovy.util.LockableObject.lock(LockableObject.java:37)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:104)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-3" #13 prio=5 os_prio=0 tid=0x00007f0a984f5000 nid=0x6814 waiting for monitor entry [0x00007f0a75c34000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at org.codehaus.groovy.reflection.ClassInfo$GlobalClassSet.add(ClassInfo.java:477)
	- waiting to lock <0x0000000084d48b90> (a org.codehaus.groovy.util.ManagedLinkedList)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:83)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:79)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$EntryWithValue.<init>(GroovyClassValuePreJava7.java:37)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:64)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:55)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:157)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-2" #12 prio=5 os_prio=0 tid=0x00007f0a984c9800 nid=0x6813 waiting on condition [0x00007f0a75d35000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x0000000084d9bdd0> (a org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at org.codehaus.groovy.util.LockableObject.lock(LockableObject.java:37)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:104)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)

"Thread-1" #11 prio=5 os_prio=0 tid=0x00007f0a98580800 nid=0x6812 waiting on condition [0x00007f0a76334000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x0000000084d9bd28> (a org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.parkAndCheckInterrupt(AbstractQueuedSynchronizer.java:836)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquireQueued(AbstractQueuedSynchronizer.java:870)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(AbstractQueuedSynchronizer.java:1199)
	at org.codehaus.groovy.util.LockableObject.lock(LockableObject.java:37)
	at org.codehaus.groovy.util.AbstractConcurrentMapBase$Segment.removeEntry(AbstractConcurrentMapBase.java:173)
	at org.codehaus.groovy.util.ManagedConcurrentMap$Entry.finalizeReference(ManagedConcurrentMap.java:81)
	at org.codehaus.groovy.util.ManagedConcurrentMap$EntryWithValue.finalizeReference(ManagedConcurrentMap.java:115)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$EntryWithValue.finalizeReference(GroovyClassValuePreJava7.java:51)
	at org.codehaus.groovy.util.ReferenceManager$CallBackedManager.removeStallEntries0(ReferenceManager.java:108)
	at org.codehaus.groovy.util.ReferenceManager$CallBackedManager.removeStallEntries(ReferenceManager.java:93)
	at org.codehaus.groovy.util.ReferenceManager$CallBackedManager.afterReferenceCreation(ReferenceManager.java:117)
	at org.codehaus.groovy.util.ReferenceManager$1.afterReferenceCreation(ReferenceManager.java:135)
	at org.codehaus.groovy.util.ManagedReference.<init>(ManagedReference.java:36)
	at org.codehaus.groovy.util.ManagedReference.<init>(ManagedReference.java:40)
	at org.codehaus.groovy.util.ManagedLinkedList$Element.<init>(ManagedLinkedList.java:40)
	at org.codehaus.groovy.util.ManagedLinkedList.add(ManagedLinkedList.java:102)
	at org.codehaus.groovy.reflection.ClassInfo$GlobalClassSet.add(ClassInfo.java:478)
	- locked <0x0000000084d48b90> (a org.codehaus.groovy.util.ManagedLinkedList)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:83)
	at org.codehaus.groovy.reflection.ClassInfo$1.computeValue(ClassInfo.java:79)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$EntryWithValue.<init>(GroovyClassValuePreJava7.java:37)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:64)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7$GroovyClassValuePreJava7Segment.createEntry(GroovyClassValuePreJava7.java:55)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.put(AbstractConcurrentMap.java:157)
	at org.codehaus.groovy.util.AbstractConcurrentMap$Segment.getOrPut(AbstractConcurrentMap.java:100)
	at org.codehaus.groovy.util.AbstractConcurrentMap.getOrPut(AbstractConcurrentMap.java:38)
	at org.codehaus.groovy.reflection.GroovyClassValuePreJava7.get(GroovyClassValuePreJava7.java:94)
	at org.codehaus.groovy.reflection.ClassInfo.getClassInfo(ClassInfo.java:144)
	at org.codehaus.groovy.util.ClassInfoDeadlockTest$1.run(ClassInfoDeadlockTest.java:70)
	at java.lang.Thread.run(Thread.java:745)
 */