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
package org.codehaus.groovy.runtime

import org.junit.jupiter.api.Test


/**
 * Tests for DefaultGroovyStaticMethods
 */
class DefaultGroovyStaticMethodsTest {
    @Test
    void testCurrentTimeSeconds() {
	    long timeMillis = System.currentTimeMillis()
        long timeSeconds = System.currentTimeSeconds()
        long timeMillis2 = System.currentTimeMillis()
        assert timeMillis/1000 as int <= timeSeconds
        assert timeMillis2/1000 as int >= timeSeconds
    }

    @Test
    void testDumpAll() {
        assert Thread.dumpAll().contains("dumpAll")
    }

    @Test
    void testAllThreads() {
        assert Thread.allThreads().stream().anyMatch(t -> 'Finalizer' == t.name)
    }

    @Test
    void testTimedNanos() {
        boolean ran = false
        long elapsed = System.timedNanos { ran = true; (1..1000).sum() }
        assert ran
        assert elapsed >= 0
    }

    @Test
    void testTimedMillis() {
        boolean ran = false
        long elapsed = System.timedMillis { ran = true; (1..1000).sum() }
        assert ran
        assert elapsed >= 0
    }

    @Test
    void testTimed() {
        def t = System.timed { (1..1000).sum() }
        assert t.result == 500500
        assert t.nanos >= 0
        assert t.millis == t.nanos.intdiv(1_000_000)
        assert t.duration == java.time.Duration.ofNanos(t.nanos)
    }

    @Test
    void testTimedPropagatesException() {
        def ex = new IllegalStateException('boom')
        def caught = null
        try {
            System.timed { throw ex }
        } catch (IllegalStateException e) {
            caught = e
        }
        assert caught.is(ex)
    }
}
