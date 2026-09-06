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
package org.apache.groovy.concurrent.java;

import groovy.concurrent.Actor;
import groovy.concurrent.Awaitable;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.apache.groovy.runtime.async.AsyncSupport.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ActorTest {

    @Test
    void reactorProcessesMessages() {
        Actor<Integer> doubler = Actor.<Integer, Integer>reactor(n -> n * 2);
        try {
            int result = await(doubler.<Integer>sendAndGet(21));
            assertEquals(42, result);
        } finally {
            doubler.stop();
        }
    }

    @Test
    void statefulActorMaintainsState() {
        Actor<String> counter = Actor.<String, Integer>stateful(0, (state, msg) -> {
            if ("increment".equals(msg)) return state + 1;
            return state;
        });
        try {
            counter.send("increment");
            counter.send("increment");
            int result = await(counter.<Integer>sendAndGet("increment"));
            assertEquals(3, result);
        } finally {
            counter.stop();
        }
    }

    @Test
    void errorHandlerSeesTheFailure() {
        AtomicReference<Throwable> captured = new AtomicReference<>();
        Actor<String> actor = Actor.<String, String>reactor(msg -> {
            if ("boom".equals(msg)) throw new IllegalStateException("boom");
            return msg;
        }).onError((Throwable t, String msg) -> captured.set(t));
        try {
            assertEquals("ok", await(actor.<String>sendAndGet("ok")));
            assertThrows(IllegalStateException.class, () -> await(actor.<String>sendAndGet("boom")));
            for (int i = 0; i < 100 && captured.get() == null; i++) {
                await(Awaitable.delay(10));
            }
            assertEquals("boom", captured.get().getMessage());
        } finally {
            actor.stop();
        }
    }

    @Test
    void contextAwareErrorHandlerCanStopTheActor() {
        Actor<String> actor = Actor.<String, String>reactor(msg -> {
            throw new IllegalStateException("always");
        }).onError((ctx, t, msg) -> ctx.self().stop());

        actor.send("trigger");
        for (int i = 0; i < 100 && actor.isActive(); i++) {
            await(Awaitable.delay(10));
        }
        assertFalse(actor.isActive());
    }
}
