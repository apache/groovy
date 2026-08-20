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
package org.codehaus.groovy.reflection

import groovy.lang.GroovySystem

import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * GROOVY-12281 stress probe for {@code -Dgroovy.use.classvalue=soft} under
 * <em>real</em> GC clearing (unlike {@link ClassInfoSoftModeProbe}, which
 * clears deterministically): run with a small heap and
 * {@code -XX:SoftRefLRUPolicyMSPerMB=0} so every collection clears whatever
 * soft references are not strongly protected, while dispatch, MetaClass
 * mutation and class churn race the collector. Invoked from
 * {@link ClassInfoSoftModeStressTest}; prints {@code OK} on success.
 *
 * Invariants asserted:
 * <ul>
 * <li>no exception anywhere (a MissingMethodException on the mutated class
 *     means a dirty-rooted customization was lost to collection);</li>
 * <li>observed EMC generations stay within what was installed and are
 *     non-decreasing per dispatcher thread;</li>
 * <li>the mutated (dirty-rooted) ClassInfo keeps its identity for the whole
 *     run — it must never be collected/recreated;</li>
 * <li>churn-class ClassInfos really do get collected (the stress is live) and
 *     dispatch on them stays correct across recreation;</li>
 * <li>after the final generation is installed, every dispatcher — indy and
 *     classic — observes it while pressure continues (no straggler guard
 *     survives a mutation on a resurrected/recreated ClassInfo).</li>
 * </ul>
 */
final class ClassInfoSoftModeStressProbe {

    private static final int CHURN_CLASSES = 100
    private static final long STRESS_MILLIS = 20_000
    private static final long QUIESCE_TIMEOUT_MILLIS = 10_000

    private static final ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>()
    private static final AtomicBoolean mutating = new AtomicBoolean(true)
    private static final AtomicInteger installedGen = new AtomicInteger()
    private static volatile int finalGen = -1
    private static final AtomicLong dispatches = new AtomicLong()

    static void main(String[] args) {
        def gcv = ClassInfo.getDeclaredField('globalClassValue').tap { accessible = true }.get(null)
        assert gcv instanceof GroovyClassValueSoft :
                "probe requires -Dgroovy.use.classvalue=soft, found ${gcv.getClass().simpleName}"

        // POGO group: Groovy-defined classes. Their ClassInfos are self-pinned
        // while the class lives (POGO instances hold their MetaClass and the
        // generated $staticMetaClass field holds it from the Class), so they
        // stress link/relink correctness, not collection.
        def gcl = new GroovyClassLoader(ClassInfoSoftModeStressProbe.classLoader)
        def churnReceivers = new Object[CHURN_CLASSES]
        for (int i = 0; i < CHURN_CLASSES; i++) {
            def cls = gcl.parseClass("class Churn$i { int id() { $i } }", "Churn${i}.groovy")
            churnReceivers[i] = cls.getDeclaredConstructor().newInstance()
        }

        // collection group: JAVA receivers — no MetaClass instance field, no
        // $staticMetaClass, and POJO indy guards capture only Class objects —
        // so their pristine ClassInfos are exactly the population soft mode
        // is allowed to reap and recreate under pressure (the ticket's
        // platform-receiver scenario).
        Object[] javaReceivers = [
                new ArrayList(), new LinkedList(), new HashMap(), new TreeMap(),
                new HashSet(), new TreeSet(), new ArrayDeque(), new PriorityQueue(),
                new Stack(), new Vector(), new Hashtable(), new StringBuilder('x'),
                new StringBuffer('y'), new Random(42), new StringJoiner(','),
                new IdentityHashMap(), new WeakHashMap(), new LinkedHashMap(),
                new LinkedHashSet(), new java.util.concurrent.ConcurrentHashMap(),
                new java.util.concurrent.ConcurrentLinkedDeque(), new java.util.concurrent.CopyOnWriteArrayList(),
                new java.util.concurrent.atomic.AtomicInteger(7), new java.util.concurrent.atomic.AtomicLong(9),
                new java.util.zip.CRC32(), new java.util.zip.Adler32(),
                new java.text.StringCharacterIterator('z'), new java.awt.Point(1, 2),
                new java.awt.Dimension(3, 4), new java.awt.Rectangle(5, 6),
                new java.io.ByteArrayOutputStream(), new java.io.StringWriter(),
                new java.net.InetSocketAddress(80), new java.util.Formatter(),
                new java.util.EventObject('e'), new java.util.SimpleTimeZone(0, 'UTC'),
        ] as Object[]
        long[] javaExpected = new long[javaReceivers.length]
        def javaInfos = new WeakReference[javaReceivers.length]
        for (int i = 0; i < javaReceivers.length; i++) {
            javaExpected[i] = javaReceivers[i].hashCode()   // receivers are never mutated
            javaInfos[i] = new WeakReference<ClassInfo>(ClassInfo.getClassInfo(javaReceivers[i].getClass()))
        }

        // mutated group: an EMC on a platform class — the ticket's exact
        // shape — whose ClassInfo the dirty root must keep alive throughout
        installGen(1)
        ClassInfo mutatedInfoAtStart = ClassInfo.getClassInfo(BitSet)
        def mutatedInfoRef = new WeakReference<ClassInfo>(mutatedInfoAtStart)

        List<Thread> workers = []
        def startGate = new CountDownLatch(1)
        def done = new CountDownLatch(6)

        // 2 indy dispatchers over the POGO classes (one megamorphic site)
        2.times { t ->
            workers << worker(startGate, done, "pogo-$t") {
                for (int i = 0; ; i++) {
                    int k = (i * 31 + t) % CHURN_CLASSES
                    def r = churnReceivers[k]
                    int got = r.id()
                    if (got != k) {
                        throw new IllegalStateException("POGO dispatch returned $got for Churn$k")
                    }
                    dispatches.incrementAndGet()
                    if (shouldStop()) return
                }
            }
        }

        // 1 indy dispatcher over the Java receivers, racing collection and
        // recreation of their ClassInfos
        workers << worker(startGate, done, 'java-churn') {
            for (int i = 0; ; i++) {
                int k = (i * 17 + 3) % javaReceivers.length
                long got = javaReceivers[k].hashCode()
                if (got != javaExpected[k]) {
                    throw new IllegalStateException("Java dispatch returned $got for ${javaReceivers[k].getClass().name}, expected ${javaExpected[k]}")
                }
                dispatches.incrementAndGet()
                if (shouldStop()) return
            }
        }

        // 2 indy dispatchers on the mutated platform class
        2.times { t ->
            workers << worker(startGate, done, "mutated-$t") {
                def receiver = new BitSet()
                int lastSeen = 0
                while (true) {
                    int gen = receiver.probe()
                    checkGen(gen, lastSeen, "mutated-$t")
                    lastSeen = Math.max(lastSeen, gen)
                    dispatches.incrementAndGet()
                    if (shouldStop() && lastSeen == finalGen) return
                    if (shouldStop() && quiesceExpired()) {
                        throw new IllegalStateException("mutated-$t stuck at gen $lastSeen, final is $finalGen")
                    }
                }
            }
        }

        // 1 classic dispatcher on the same class, via CallSiteArray as a
        // legacy-compiled jar would dispatch (groovy-callsite is runtime-only)
        workers << worker(startGate, done, 'classic') {
            def csaClass = Class.forName('org.codehaus.groovy.runtime.callsite.CallSiteArray')
            def csa = csaClass.getConstructor(Class, String[]).newInstance(ClassInfoSoftModeStressProbe, ['probe'] as String[])
            def noparam = csaClass.NOPARAM
            def receiver = new BitSet()
            int lastSeen = 0
            while (true) {
                int gen = (int) csa.array[0].call(receiver, noparam)
                checkGen(gen, lastSeen, 'classic')
                lastSeen = Math.max(lastSeen, gen)
                dispatches.incrementAndGet()
                if (shouldStop() && lastSeen == finalGen) return
                if (shouldStop() && quiesceExpired()) {
                    throw new IllegalStateException("classic stuck at gen $lastSeen, final is $finalGen")
                }
            }
        }

        // mutator: replace the EMC method with an increasing generation
        def mutator = new Thread({
            startGate.await()
            long end = System.currentTimeMillis() + STRESS_MILLIS
            while (System.currentTimeMillis() < end) {
                installGen(installedGen.get() + 1)
                Thread.sleep(150)
            }
            int last = installedGen.get() + 1
            installGen(last)
            finalGen = last          // publish, then let dispatchers converge
            quiesceStart = System.currentTimeMillis()
            mutating.set(false)
        }, 'mutator')
        mutator.daemon = true

        // pressure: keep the collector busy; with SoftRefLRUPolicyMSPerMB=0
        // every GC clears all unprotected soft references
        def pressure = new Thread({
            startGate.await()
            def hog = new byte[24][]
            int i = 0, gcTick = 0
            while (!done.await(0, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                try {
                    hog[i++ % hog.length] = new byte[256 << 10]
                } catch (OutOfMemoryError e) {
                    hog = new byte[24][]  // release and back off
                }
                if (++gcTick % 64 == 0) {
                    System.gc()
                    Thread.sleep(20)
                }
            }
        }, 'pressure')
        pressure.daemon = true

        workers*.start(); mutator.start(); pressure.start()
        startGate.countDown()

        long deadline = System.currentTimeMillis() + STRESS_MILLIS + QUIESCE_TIMEOUT_MILLIS + 10_000
        for (t in workers) {
            long left = deadline - System.currentTimeMillis()
            t.join(Math.max(1, left))
            if (t.alive) errors.add("worker ${t.name} did not finish")
        }

        // dirty root must have preserved the mutated class's identity
        ClassInfo mutatedInfoAtEnd = ClassInfo.getClassInfo(BitSet)
        if (!mutatedInfoAtEnd.is(mutatedInfoAtStart)) {
            errors.add('dirty-rooted ClassInfo(BitSet) was replaced during the run')
        }
        if (mutatedInfoRef.get() == null) {
            errors.add('dirty-rooted ClassInfo(BitSet) was collected during the run')
        }

        // the stress must have been live: Java-receiver ClassInfos really collected
        int collected = javaInfos.count { it.get() == null }
        // and dispatch still works on every class afterwards
        for (int k = 0; k < CHURN_CLASSES; k++) {
            if (churnReceivers[k].id() != k) {
                errors.add("post-run dispatch broken for Churn$k")
            }
        }
        for (int k = 0; k < javaReceivers.length; k++) {
            if (javaReceivers[k].hashCode() != javaExpected[k]) {
                errors.add("post-run dispatch broken for ${javaReceivers[k].getClass().name}")
            }
        }

        GroovySystem.metaClassRegistry.removeMetaClass(BitSet)

        println "dispatches=${dispatches.get()} generations=${installedGen.get()} javaInfosCollected=$collected/${javaInfos.length}"
        if (collected == 0) {
            errors.add('no Java-receiver ClassInfo was ever collected — the stress did not exercise real GC clearing')
        }
        if (errors.isEmpty()) {
            println 'OK'
        } else {
            errors.each { println "ERROR: $it" }
            System.exit(1)
        }
    }

    private static volatile long quiesceStart = Long.MAX_VALUE

    private static boolean shouldStop() {
        !mutating.get()
    }

    private static boolean quiesceExpired() {
        System.currentTimeMillis() - quiesceStart > QUIESCE_TIMEOUT_MILLIS
    }

    private static void checkGen(int gen, int lastSeen, String who) {
        if (gen < 1 || gen > installedGen.get()) {
            throw new IllegalStateException("$who observed gen $gen outside installed range 1..${installedGen.get()}")
        }
        if (gen < lastSeen) {
            throw new IllegalStateException("$who observed gen $gen after already seeing $lastSeen")
        }
    }

    private static void installGen(int gen) {
        installedGen.set(gen)
        BitSet.metaClass.probe = { -> gen }
    }

    private static Thread worker(CountDownLatch startGate, CountDownLatch done, String name, Closure body) {
        def t = new Thread({
            startGate.await()
            try {
                body()
            } catch (Throwable e) {
                errors.add("$name: ${e.getClass().simpleName}: ${e.message}")
            } finally {
                done.countDown()
            }
        }, name)
        t.daemon = true
        t
    }
}
