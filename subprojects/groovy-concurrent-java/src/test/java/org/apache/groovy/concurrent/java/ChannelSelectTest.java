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

import groovy.concurrent.AsyncChannel;
import groovy.concurrent.Awaitable;
import groovy.concurrent.ChannelSelect;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static groovy.concurrent.ChannelSelect.after;
import static groovy.concurrent.ChannelSelect.offers;
import static groovy.concurrent.ChannelSelect.receive;
import static groovy.concurrent.ChannelSelect.send;
import static org.apache.groovy.runtime.async.AsyncSupport.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ChannelSelect} driven from plain Java: choice policies, mixed
 * send/receive offers, guards, preconditions and timer branches.
 */
class ChannelSelectTest {

    @Test
    void selectTakesTheFirstChannelToDeliver() {
        AsyncChannel<String> ch1 = AsyncChannel.create(10);
        AsyncChannel<String> ch2 = AsyncChannel.create(10);
        ChannelSelect sel = ChannelSelect.from(ch1, ch2);

        Awaitable.go(() -> {
            await(Awaitable.delay(50));
            return await(ch1.send("from-ch1"));
        });

        ChannelSelect.Result result = await(sel.select());
        assertEquals(0, result.getIndex());
        assertSame(ch1, result.getChannel());
        String value = result.getValue();
        assertEquals("from-ch1", value);
        assertFalse(result.isSend());
        assertFalse(result.isTimeout());
    }

    @Test
    void losingBranchesAreNotConsumed() {
        AsyncChannel<String> a = AsyncChannel.create(4);
        AsyncChannel<String> b = AsyncChannel.create(4);
        await(a.send("a1"));
        await(b.send("b1"));

        ChannelSelect.Result result = await(ChannelSelect.from(a, b).select());
        assertEquals(0, result.getIndex());
        assertEquals("a1", result.<String>getValue());

        assertEquals(1, b.getBufferedSize());
        assertEquals("b1", await(b.receive()));
    }

    @Test
    void fairChoiceRotatesAmongReadyChannels() {
        AsyncChannel<String> a = AsyncChannel.create(4);
        AsyncChannel<String> b = AsyncChannel.create(4);
        AsyncChannel<String> c = AsyncChannel.create(4);
        for (int i = 0; i < 2; i++) {
            await(a.send("a" + i));
            await(b.send("b" + i));
            await(c.send("c" + i));
        }

        ChannelSelect fair = ChannelSelect.from(a, b, c).fair();
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            order.add(await(fair.select()).getIndex());
        }
        assertEquals(List.of(0, 1, 2, 0, 1, 2), order);

        // the default policy is priority by list order
        AsyncChannel<String> p = AsyncChannel.create(4);
        AsyncChannel<String> q = AsyncChannel.create(4);
        await(p.send("p0"));
        await(p.send("p1"));
        await(q.send("q0"));
        ChannelSelect priority = ChannelSelect.from(p, q);
        assertEquals(0, await(priority.select()).getIndex());
        assertEquals(0, await(priority.select()).getIndex());
    }

    @Test
    void randomChoiceSpreadsAmongReadyChannelsAndKeepsEachInOrder() {
        int rounds = 200;
        AsyncChannel<Integer> a = AsyncChannel.create(rounds);
        AsyncChannel<Integer> b = AsyncChannel.create(rounds);
        for (int i = 0; i < rounds; i++) {
            await(a.send(i));
            await(b.send(i));
        }

        ChannelSelect random = ChannelSelect.from(a, b).random();
        List<List<Integer>> taken = List.of(new ArrayList<>(), new ArrayList<>());
        for (int i = 0; i < rounds; i++) {
            ChannelSelect.Result result = await(random.select());
            taken.get(result.getIndex()).add(result.getValue());
        }
        // the chance of one channel never being chosen in 200 draws is 2^-199
        assertFalse(taken.get(0).isEmpty());
        assertFalse(taken.get(1).isEmpty());
        for (List<Integer> values : taken) {
            for (int i = 0; i < values.size(); i++) {
                assertEquals(i, values.get(i));
            }
        }
    }

    @Test
    void sendOfferCommitsWhenAReceiverIsWaiting() {
        AsyncChannel<String> out = AsyncChannel.create(); // rendezvous
        AsyncChannel<String> other = AsyncChannel.create(4);

        Awaitable<String> taken = out.receive(); // a receiver is already waiting
        ChannelSelect.Result result = await(offers(send(out, "opener"), receive(other)).select());

        assertEquals(0, result.getIndex());
        assertTrue(result.isSend());
        assertEquals("opener", result.<String>getValue());
        assertEquals("opener", await(taken));
    }

    @Test
    void sendOfferCommitsIntoFreeBufferSpace() {
        AsyncChannel<String> out = AsyncChannel.create(1);
        AsyncChannel<String> other = AsyncChannel.create(4);

        ChannelSelect.Result result = await(offers(send(out, "buffered"), receive(other)).select());
        assertEquals(0, result.getIndex());
        assertTrue(result.isSend());
        assertEquals(1, out.getBufferedSize());
        assertEquals("buffered", await(out.receive()));
    }

    @Test
    void guardMasksAnOfferWithoutRenumberingTheOthers() {
        AsyncChannel<String> first = AsyncChannel.create(4);
        AsyncChannel<String> second = AsyncChannel.create(4);
        await(first.send("f1"));
        await(second.send("s1"));

        AtomicBoolean gate = new AtomicBoolean(false);
        ChannelSelect sel = offers(receive(first).when(gate::get), receive(second));

        // both are ready, but the first branch is guarded off: the second
        // wins and still calls itself index 1
        ChannelSelect.Result result = await(sel.select());
        assertEquals(1, result.getIndex());
        assertEquals("s1", result.<String>getValue());
        assertEquals(1, first.getBufferedSize());

        // the guard is consulted afresh on every select
        gate.set(true);
        result = await(sel.select());
        assertEquals(0, result.getIndex());
        assertEquals("f1", result.<String>getValue());
    }

    @Test
    void positionalPreconditionsMaskOffers() {
        AsyncChannel<String> a = AsyncChannel.create(4);
        AsyncChannel<String> b = AsyncChannel.create(4);
        await(a.send("a1"));
        await(b.send("b1"));
        ChannelSelect sel = ChannelSelect.from(a, b);

        ChannelSelect.Result result = await(sel.select(false, true));
        assertEquals(1, result.getIndex());
        assertEquals("b1", result.<String>getValue());
        assertEquals(1, a.getBufferedSize());

        assertThrows(IllegalArgumentException.class, () -> sel.select(true));
        assertThrows(IllegalStateException.class, () -> await(sel.select(false, false)));
        assertEquals(1, a.getBufferedSize());
    }

    @Test
    void timerOfferWinsAQuietSelect() {
        AsyncChannel<String> work = AsyncChannel.create(4);
        Instant before = Instant.now();

        ChannelSelect.Result result = await(offers(receive(work), after(50)).select());
        assertEquals(1, result.getIndex());
        assertTrue(result.isTimeout());
        assertFalse(result.isSend());
        assertNull(result.getChannel());
        Instant firedAt = result.getValue();
        assertFalse(firedAt.isBefore(before));
    }

    @Test
    void dataBeatsTheTimer() {
        AsyncChannel<String> work = AsyncChannel.create(4);
        await(work.send("job"));

        ChannelSelect.Result result = await(offers(receive(work), after(Duration.ofSeconds(5))).select());
        assertEquals(0, result.getIndex());
        assertFalse(result.isTimeout());
        assertEquals("job", result.<String>getValue());
    }

    @Test
    void timerChannelIsAFixedDeadlineAcrossSelects() {
        AsyncChannel<Integer> work = AsyncChannel.create(16);
        AsyncChannel<Instant> deadline = AsyncChannel.after(200);
        ChannelSelect sel = ChannelSelect.from(work, deadline);

        Awaitable.go(() -> {
            for (int i = 0; i < 3; i++) {
                await(work.send(i));
                await(Awaitable.delay(10));
            }
            return null;
        });

        List<Integer> received = new ArrayList<>();
        while (true) {
            ChannelSelect.Result result = await(sel.select());
            if (result.getIndex() == 1) break;
            received.add(result.getValue());
        }
        assertEquals(List.of(0, 1, 2), received);
        assertTrue(deadline.isClosed());
    }
}
