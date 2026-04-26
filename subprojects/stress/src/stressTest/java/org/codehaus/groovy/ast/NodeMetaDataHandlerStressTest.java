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
package org.codehaus.groovy.ast;

import org.apache.groovy.stress.util.ThreadUtils;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Exercises concurrent access to {@link NodeMetaDataHandler}'s default methods on a
 * shared AST node. The underlying {@code ListHashMap} is not thread-safe, so without
 * external protection concurrent compiles sharing a {@link ClassNode} (e.g. built-in
 * annotation nodes cached by {@link ClassHelper}) trip {@code ArrayIndexOutOfBoundsException}
 * during the array-to-{@code HashMap} transition in {@code ListHashMap.put}. The default
 * {@code newMetaDataMap()} now wraps the {@code ListHashMap} in
 * {@link java.util.Collections#synchronizedMap}, making individual {@code get}/{@code put}/
 * {@code remove}/{@code computeIfAbsent} operations thread-safe; this test verifies that
 * guarantee under contention.
 */
public class NodeMetaDataHandlerStressTest {

    private static final int THREADS = 16;
    private static final int ITERATIONS = 5_000;
    private static final int KEY_SPACE = 4;

    @Test
    public void testConcurrentAccessOnSharedNode() throws Exception {
        // ClassHelper.makeCached returns a process-wide shared ClassNode; this is
        // the path that built-in annotation ClassNodes reach in real compilations.
        // Use a unique target class so other tests in the same JVM don't interfere.
        ClassNode shared = ClassHelper.makeCached(SharedTarget.class);

        CyclicBarrier start = new CyclicBarrier(THREADS);
        List<Throwable> errors = new CopyOnWriteArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            for (int t = 0; t < THREADS; t++) {
                final int threadId = t;
                pool.submit(() -> {
                    try {
                        ThreadUtils.await(start);
                        for (int i = 0; i < ITERATIONS; i++) {
                            final int iter = i;
                            String key = "k" + (i % KEY_SPACE);
                            String otherKey = "k" + ((i + 1) % KEY_SPACE);

                            // factory variant — the path used by AnnotationNode.isTargetAllowed
                            shared.getNodeMetaData(key, k -> "v-" + threadId + "-" + iter);
                            // plain read
                            shared.getNodeMetaData(otherKey);
                            // explicit put / remove to force the array<->HashMap transition
                            if (i % 7 == 0) shared.putNodeMetaData(key, "p-" + threadId + "-" + i);
                            if (i % 11 == 0) shared.removeNodeMetaData(key);
                        }
                    } catch (Throwable th) {
                        errors.add(th);
                    }
                });
            }
            pool.shutdown();
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                throw new AssertionError("stress test did not complete within 60s");
            }
        } finally {
            pool.shutdownNow();
        }

        if (!errors.isEmpty()) {
            AssertionError ae = new AssertionError(
                "concurrent NodeMetaDataHandler access produced " + errors.size() + " errors; first: " + errors.get(0));
            ae.initCause(errors.get(0));
            throw ae;
        }
        assertEquals(0, errors.size());
    }

    /** Dedicated target class so we don't poison metadata on a shared standard ClassNode. */
    private static final class SharedTarget { }
}
