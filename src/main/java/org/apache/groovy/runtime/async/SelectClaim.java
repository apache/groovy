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
package org.apache.groovy.runtime.async;

import groovy.transform.Internal;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The single-winner state of one selection: the arbiter through which a group
 * of competing offers (the branches of a {@link groovy.concurrent.ChannelSelect},
 * or a lone channel operation) commits exactly one outcome between them.
 * <p>
 * A claim is a three-state machine: OPEN, PENDING, or COMMITTED. PENDING is a
 * revertible reservation held while a two-party pairing (a send offer meeting
 * a receive offer, each belonging to a different selection) verifies that both
 * sides still stand; the reservation always resolves promptly — its holder does
 * only lock-free work, never completes a future and never takes a lock while
 * holding it — so competitors may spin on it. When two claims must be reserved
 * together they are taken in ascending {@link #id} order, which keeps every
 * waits-for chain strictly increasing and therefore cycle-free: the
 * shared-memory form of the commit protocol for generalized (input and output)
 * guards of Buckley &amp; Silberschatz (TOPLAS 1983).
 * <p>
 * A committed claim records the winning party — the completion of the branch
 * that won, or the cancelled marker — so cancellation can distinguish "this
 * branch won, refuse the cancel: its delivery is imminent or done" from "a
 * sibling won, this loser may unpark freely".
 *
 * @since 6.0.0
 */
@Internal
public final class SelectClaim {

    private static final AtomicLong IDS = new AtomicLong();

    /** the terminal state of a claim committed by cancellation rather than a transfer */
    private static final Object CANCELLED = new Object();

    /** a revertible reservation; only its holder may {@link #commit} or {@link #revert} it */
    private static final class Pend {}

    private final long id = IDS.incrementAndGet();

    /** {@code null} = open; a {@link Pend} = pending; anything else = committed to that winner */
    private final AtomicReference<Object> state = new AtomicReference<>();

    /**
     * Reserves this claim, waiting out any other party's reservation.
     *
     * @return the token to {@link #commit} or {@link #revert} with, or
     *         {@code null} if the claim is already committed
     */
    public Object pend() {
        Pend token = new Pend();
        for (;;) {
            Object s = state.get();
            if (s == null) {
                if (state.compareAndSet(null, token)) return token;
            } else if (s instanceof Pend) {
                Thread.onSpinWait();
            } else {
                return null;
            }
        }
    }

    /**
     * Commits a reservation made with {@link #pend}; cannot fail while the
     * token is held.
     *
     * @param token  the reservation token
     * @param winner the completion of the branch that won
     */
    public void commit(Object token, Object winner) {
        if (!state.compareAndSet(token, winner)) {
            throw new IllegalStateException("commit without holding the reservation");
        }
    }

    /**
     * Reverts a reservation made with {@link #pend}, reopening the claim.
     *
     * @param token the reservation token
     */
    public void revert(Object token) {
        if (!state.compareAndSet(token, null)) {
            throw new IllegalStateException("revert without holding the reservation");
        }
    }

    /**
     * Commits in one step when no partner needs verifying: a value provably
     * present under the channel lock, or a send into free buffer space.
     *
     * @param winner the completion of the branch that won
     * @return {@code false} if the claim was already committed elsewhere
     */
    public boolean tryCommit(Object winner) {
        for (;;) {
            Object s = state.get();
            if (s == null) {
                if (state.compareAndSet(null, winner)) return true;
            } else if (s instanceof Pend) {
                Thread.onSpinWait();
            } else {
                return false;
            }
        }
    }

    /**
     * Commits to cancellation: once this succeeds, no branch can deliver.
     *
     * @return {@code false} if a branch had already won
     */
    public boolean tryCommitCancel() {
        return tryCommit(CANCELLED);
    }

    /**
     * Whether the claim has reached a terminal state (a reservation in flight
     * is not terminal).
     */
    public boolean isCommitted() {
        Object s = state.get();
        return s != null && !(s instanceof Pend);
    }

    /**
     * Gate for cancelling the given parked completion. A still-open claim is
     * committed to the cancel and the future may die; a claim committed to a
     * sibling lets this loser unpark freely; but the committed winner itself
     * is refused — its delivery is imminent or done and must reach the caller.
     *
     * @param future the completion being cancelled
     * @return whether the future may be cancelled
     */
    public boolean allowCancel(Object future) {
        for (;;) {
            Object s = state.get();
            if (s == null) {
                if (state.compareAndSet(null, CANCELLED)) return true;
            } else if (s instanceof Pend) {
                Thread.onSpinWait();
            } else {
                return s != future;
            }
        }
    }

    /**
     * Reserves two parties' claims, in ascending id order regardless of
     * argument order, reverting the first if the second turns out committed.
     *
     * @param a      the first party's claim; its token lands in {@code tokens[0]}
     * @param b      the second party's claim; its token lands in {@code tokens[1]}
     * @param tokens receives the two reservation tokens on success
     * @return 0 with both reservations held, 1 if {@code a} is already
     *         committed, 2 if {@code b} is
     */
    public static int pendBoth(SelectClaim a, SelectClaim b, Object[] tokens) {
        SelectClaim first = a.id < b.id ? a : b;
        SelectClaim second = first == a ? b : a;
        Object firstToken = first.pend();
        if (firstToken == null) return first == a ? 1 : 2;
        Object secondToken = second.pend();
        if (secondToken == null) {
            first.revert(firstToken);
            return second == a ? 1 : 2;
        }
        tokens[first == a ? 0 : 1] = firstToken;
        tokens[first == a ? 1 : 0] = secondToken;
        return 0;
    }
}
