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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

class AsyncAwaitSpecTest {

    // === Getting started ===

    @Test
    void testBasicAsyncAwait() {
        assertScript '''
        // tag::basic_async_await[]
        def task = async { 21 * 2 }
        assert await(task) == 42
        // end::basic_async_await[]
        '''
    }

    @Test
    void testDrawCard() {
        assertScript '''
        // tag::draw_card[]
        def deck = ['2♠', '3♥', 'K♦', 'A♣']
        def card = async { deck.shuffled()[0] }
        println "You drew: ${await card}"
        // end::draw_card[]
        '''
    }

    @Test
    void testExceptionHandling() {
        assertScript '''
        // tag::exception_handling[]
        def drawFromEmpty = async {
            throw new IllegalStateException('deck is empty')
        }
        try {
            await drawFromEmpty
        } catch (IllegalStateException e) {
            // Original exception — no CompletionException wrapper
            assert e.message == 'deck is empty'
        }
        // end::exception_handling[]
        '''
    }

    @Test
    void testCfInterop() {
        assertScript '''
        import java.util.concurrent.CompletableFuture

        // tag::cf_interop[]
        // await works with CompletableFuture from Java libraries
        def future = CompletableFuture.supplyAsync { 'A♠' }
        assert await(future) == 'A♠'
        // end::cf_interop[]
        '''
    }

    // === Parallel tasks and combinators ===

    @Test
    void testDealHands() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::deal_hands[]
        // Deal cards to three players concurrently
        def deck = ('A'..'K').collectMany { r -> ['♠','♥','♦','♣'].collect { "$r$it" } }.shuffled()
        int i = 0
        def draw5 = { -> deck[i..<(i+5)].tap { i += 5 } }

        def alice = async { draw5() }
        def bob   = async { draw5() }
        def carol = async { draw5() }

        def (a, b, c) = await Awaitable.all(alice, bob, carol)
        assert a.size() == 5 && b.size() == 5 && c.size() == 5
        // end::deal_hands[]
        '''
    }

    @Test
    void testMultiArgAwait() {
        assertScript '''
        // tag::multi_arg_await[]
        def a = async { 1 }
        def b = async { 2 }
        def c = async { 3 }

        // Parenthesized multi-arg await (sugar for Awaitable.all):
        def results = await(a, b, c)
        assert results == [1, 2, 3]
        // end::multi_arg_await[]
        '''
    }

    @Test
    void testFastestServer() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::fastest_server[]
        // Race two servers — use whichever responds first
        def primary  = async { Thread.sleep(200); 'primary-response' }
        def fallback = async { 'fallback-response' }

        def response = await Awaitable.any(primary, fallback)
        assert response == 'fallback-response'
        // end::fastest_server[]
        '''
    }

    @Test
    void testFirstSuccess() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::first_success[]
        // Try multiple sources — use the first that succeeds
        def failing    = async { throw new RuntimeException('server down') }
        def succeeding = async { 'card-data-from-cache' }

        def result = await Awaitable.first(failing, succeeding)
        assert result == 'card-data-from-cache'
        // end::first_success[]
        '''
    }

    @Test
    void testAllSettled() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::all_settled[]
        def save1 = async { 42 }
        def save2 = async { throw new RuntimeException('db error') }

        def results = await Awaitable.allSettled(save1, save2)
        assert results[0].success && results[0].value == 42
        assert !results[1].success && results[1].error.message == 'db error'
        // end::all_settled[]
        '''
    }

    // === Generators and for await ===

    @Test
    void testDeckGenerator() {
        assertScript '''
        // tag::deck_generator[]
        def dealCards = async {
            def suits = ['♠', '♥', '♦', '♣']
            def ranks = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K']
            for (suit in suits) {
                for (rank in ranks) {
                    yield return "$rank$suit"
                }
            }
        }
        def cards = dealCards.collect()
        assert cards.size() == 52
        assert cards.first() == 'A♠'
        assert cards.last() == 'K♣'
        // end::deck_generator[]
        '''
    }

    @Test
    void testForAwaitGenerator() {
        assertScript '''
        // tag::for_await_generator[]
        def topCards = async {
            for (card in ['A♠', 'K♥', 'Q♦']) {
                yield return card
            }
        }
        def hand = []
        for await (card in topCards) {
            hand << card
        }
        assert hand == ['A♠', 'K♥', 'Q♦']
        // end::for_await_generator[]
        '''
    }

    @Test
    void testForAwaitPlainCollection() {
        assertScript '''
        // tag::for_await_collection[]
        def results = []
        for await (card in ['A♠', 'K♥', 'Q♦']) {
            results << card
        }
        assert results == ['A♠', 'K♥', 'Q♦']
        // end::for_await_collection[]
        '''
    }

    @Test
    void testGeneratorRegularFor() {
        assertScript '''
        // tag::generator_regular_for[]
        def scores = async {
            for (s in [100, 250, 75]) { yield return s }
        }
        // Generators return Iterable — regular for and collect work
        assert scores.collect { it * 2 } == [200, 500, 150]
        // end::generator_regular_for[]
        '''
    }

    // === Channels ===

    @Test
    void testChannel() {
        assertScript '''
        import groovy.concurrent.AsyncChannel

        // tag::channel[]
        def cardStream = AsyncChannel.create(3)

        // Dealer — sends cards concurrently
        async {
            for (card in ['A♠', 'K♥', 'Q♦', 'J♣']) {
                await cardStream.send(card)
            }
            cardStream.close()
        }

        // Player — receives cards as they arrive
        def hand = []
        for await (card in cardStream) {
            hand << card
        }
        assert hand == ['A♠', 'K♥', 'Q♦', 'J♣']
        // end::channel[]
        '''
    }

    // === Defer ===

    @Test
    void testDeferBasic() {
        assertScript '''
        // tag::defer_basic[]
        def log = []
        def task = async {
            log << 'open connection'
            defer { log << 'close connection' }
            log << 'open transaction'
            defer { log << 'close transaction' }
            log << 'save game state'
            'saved'
        }
        assert await(task) == 'saved'
        // Deferred actions run in LIFO order — last registered, first to run
        assert log == ['open connection', 'open transaction', 'save game state',
                       'close transaction', 'close connection']
        // end::defer_basic[]
        '''
    }

    @Test
    void testDeferOnException() {
        assertScript '''
        // tag::defer_exception[]
        def cleaned = false
        def task = async {
            defer { cleaned = true }
            throw new RuntimeException('save failed')
        }
        try {
            await task
        } catch (RuntimeException e) {
            assert e.message == 'save failed'
        }
        // Deferred actions run even when an exception occurs
        assert cleaned
        // end::defer_exception[]
        '''
    }

    // === Structured concurrency ===

    @Test
    void testTournamentScope() {
        assertScript '''
        import groovy.concurrent.AsyncScope

        // tag::structured_concurrency[]
        // Run a tournament round — all tables play concurrently
        def results = AsyncScope.withScope { scope ->
            def table1 = scope.async { [winner: 'Alice',  score: 320] }
            def table2 = scope.async { [winner: 'Bob',    score: 280] }
            def table3 = scope.async { [winner: 'Carol',  score: 410] }
            [await(table1), await(table2), await(table3)]
        }
        // All tables guaranteed complete when withScope returns
        assert results.size() == 3
        assert results.max { it.score }.winner == 'Carol'
        // end::structured_concurrency[]
        '''
    }

    @Test
    void testScopeFailFast() {
        assertScript '''
        import groovy.concurrent.AsyncScope

        // tag::scope_fail_fast[]
        try {
            AsyncScope.withScope { scope ->
                scope.async { Thread.sleep(5000); 'still playing' }
                scope.async { throw new RuntimeException('player disconnected') }
            }
        } catch (RuntimeException e) {
            // First failure cancels all siblings and propagates
            assert e.message == 'player disconnected'
        }
        // end::scope_fail_fast[]
        '''
    }

    @Test
    void testScopeWaitsForAll() {
        assertScript '''
        import groovy.concurrent.AsyncScope
        import java.util.concurrent.atomic.AtomicInteger

        // tag::scope_waits[]
        def completed = new AtomicInteger(0)
        AsyncScope.withScope { scope ->
            3.times { scope.async { Thread.sleep(50); completed.incrementAndGet() } }
        }
        // All children have completed — even without explicit await
        assert completed.get() == 3
        // end::scope_waits[]
        '''
    }

    // === Timeouts and delays ===

    @Test
    void testTimeout() {
        assertScript '''
        import groovy.concurrent.Awaitable
        import java.util.concurrent.TimeoutException

        // tag::timeout[]
        def slowPlayer = async { Thread.sleep(5000); 'finally played' }
        try {
            await Awaitable.orTimeoutMillis(slowPlayer, 100)
        } catch (TimeoutException e) {
            // Player took too long — turn forfeited
            assert true
        }
        // end::timeout[]
        '''
    }

    @Test
    void testTimeoutFallback() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::timeout_fallback[]
        def slowPlayer = async { Thread.sleep(5000); 'deliberate move' }
        def move = await Awaitable.completeOnTimeoutMillis(slowPlayer, 'auto-pass', 100)
        assert move == 'auto-pass'
        // end::timeout_fallback[]
        '''
    }

    @Test
    void testDelay() {
        assertScript '''
        import groovy.concurrent.Awaitable

        // tag::delay[]
        long start = System.currentTimeMillis()
        await Awaitable.delay(100)   // pause without blocking a thread
        assert System.currentTimeMillis() - start >= 90
        // end::delay[]
        '''
    }
}
