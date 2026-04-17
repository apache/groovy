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
package groovy.http

import com.sun.net.httpserver.HttpServer
import groovy.concurrent.Agent
import groovy.concurrent.AsyncChannel
import groovy.concurrent.BroadcastChannel
import groovy.concurrent.ChannelClosedException
import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

import static org.apache.groovy.runtime.async.AsyncSupport.await

/**
 * End-to-end integration of {@link HttpStreamResult#bodyAsLinePublisher()},
 * {@link BroadcastChannel#asPublisher()} and {@link Agent#changes()}.
 * <p>
 * Three mock exchanges stream NDJSON ticks over chunked HTTP. The pipeline
 * fans them into a single {@link AsyncChannel}, updates a per-symbol stats
 * {@link Agent}, and routes threshold-crossing alerts through a broadcast
 * topic. Two independent consumers — one on the agent's change stream, one
 * on the alert broadcast — verify the wiring end-to-end.
 *
 * <pre>
 *      binance (NDJSON)     coinbase (NDJSON)     kraken (NDJSON)
 *            │                    │                     │
 *            ▼                    ▼                     ▼
 *      getStreamAsync        getStreamAsync       getStreamAsync
 *            │                    │                     │
 *            ╰── bodyAsLinePublisher() (Flow.Publisher&lt;String&gt;) ──╮
 *                                                                  │
 *                              for await (line in publisher)       │ ← FlowPublisherAdapter
 *                                          │                       │
 *                                          ▼                       │
 *                              AsyncChannel&lt;Map&gt; (unified) ◀───────╯
 *                                          │
 *                                          ▼
 *                              aggregator: stats.send { … }
 *                                          │
 *                                          ▼
 *                       Agent&lt;Map&lt;String, Map&gt;&gt; (per-symbol stats)
 *                          │                            │
 *                          │                 if price &gt;= threshold
 *                          ▼                            ▼
 *                    agent.changes()        BroadcastChannel&lt;String&gt;
 *                    Flow.Publisher                    │
 *                          │              ╭────────────┴─────────────╮
 *                          │              │                          │
 *                          ▼              ▼                          ▼
 *                  Flow.Subscriber   asPublisher()              subscribe()
 *                  stateSnapshots    Flow.Subscriber             for (a in alertChannel)
 *                                    alertsViaPublisher          alertsViaIter
 * </pre>
 */
class MultiExchangeTickerEndToEndTest {

    private final List<HttpServer> servers = []

    @BeforeEach
    void setup() {
        // 3 exchanges: each emits a fixed sequence of ticks over chunked HTTP.
        servers << buildExchange('binance',  [50000, 50100, 50250, 50180])
        servers << buildExchange('coinbase', [50050, 50120, 50300, 50220])
        servers << buildExchange('kraken',   [50010, 50090, 50260, 50190])
    }

    @AfterEach
    void teardown() {
        servers.each { it.stop(0) }
    }

    @Test
    void aggregatesTicksAcrossExchangesAndPublishesAlerts() {
        var stats = Agent.create([:].withDefault { [count: 0, last: 0d, max: 0d] })
        var alertTopic = BroadcastChannel.create()
        var unified = AsyncChannel.<Map>create(64)

        // Subscribers register FIRST so no early signals are dropped:
        // SubmissionPublisher drops items with no subscribers, and
        // BroadcastChannel.subscribe() misses pre-subscription broadcasts.

        var stateSnapshots = [].asSynchronized()
        var sawSomeUpdates = new CountDownLatch(3)
        agentChangesSubscriber(stats, stateSnapshots, sawSomeUpdates)

        var alertPublisher = alertTopic.asPublisher()
        var alertsViaPublisher = [].asSynchronized()
        var alertsDone = new CountDownLatch(1)
        alertPublisher.subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(String alert) { alertsViaPublisher << alert }
            @Override void onError(Throwable t) { alertsDone.countDown() }
            @Override void onComplete() { alertsDone.countDown() }
        })

        // Also iterate the broadcast directly via for await (Iterable view).
        var alertsViaIter = [].asSynchronized()
        var alertChannel = alertTopic.subscribe()
        Thread.startDaemon('alert-iter') {
            for (a in alertChannel) alertsViaIter << a
        }

        // Producers and aggregator.

        servers.each { server ->
            var base = URI.create("http://127.0.0.1:${server.address.port}/")
            var http = HttpBuilder.http(base.toString())
            Thread.startDaemon("drain-${base}") {
                var res = http.getStreamAsync('/ticker').join()
                drainPublisherIntoChannel(res.bodyAsLinePublisher(), unified)
            }
        }

        // When all 3 exchanges have published their 4 ticks each, close the
        // unified channel so the aggregator loop exits.
        Thread.startDaemon('unified-closer') {
            var totalExpected = 3 * 4
            var seen = 0
            while (seen < totalExpected) {
                Thread.sleep(20)
                seen = ((Map) await(stats.getAsync())).values().sum { (int) it.count } ?: 0
            }
            unified.close()
            alertTopic.close()
        }

        Thread.startDaemon('aggregator') {
            for (Map tick in unified) {
                // The closure passed to stats.send runs on the agent's executor
                // and would otherwise capture the mutable for-loop variable
                // `tick` by reference. Bind locals so the queued update sees
                // the right values.
                String symbol = tick.symbol
                double price = (double) tick.price
                stats.send { Map<String, Map> current ->
                    var symStats = current[symbol] ?: [count: 0, last: 0d, max: 0d]
                    var updated = [
                            count: ((int) symStats.count) + 1,
                            last : price,
                            max  : Math.max((double) symStats.max, price),
                    ]
                    var next = new HashMap<String, Map>(current)
                    next.put(symbol, updated)
                    next
                }
                if (price >= 50250) {
                    try { alertTopic.send("HIGH ${symbol}@${price}".toString()) }
                    catch (ChannelClosedException ignored) {}
                }
            }
        }

        assert sawSomeUpdates.await(5, TimeUnit.SECONDS), 'agent.changes() did not deliver updates'
        assert alertsDone.await(5, TimeUnit.SECONDS), 'alert publisher did not complete'
        Thread.sleep(100)  // settle
        stats.shutdown()

        Map<String, Map> finalStats = stateSnapshots[-1]
        assert finalStats.size() == 3
        assert finalStats['binance'].count == 4
        assert finalStats['coinbase'].count == 4
        assert finalStats['kraken'].count == 4
        assert finalStats['binance'].max == 50250d
        assert finalStats['coinbase'].max == 50300d
        assert finalStats['kraken'].max == 50260d

        // Threshold ≥ 50250: binance (50250), coinbase (50300), kraken (50260) → 3 alerts.
        assert alertsViaPublisher.size() == 3
        assert alertsViaIter.size() == 3
        assert alertsViaPublisher.toSorted() == alertsViaIter.toSorted()
        assert alertsViaPublisher.any { it.startsWith('HIGH binance@50250') }
        assert alertsViaPublisher.any { it.startsWith('HIGH coinbase@50300') }
        assert alertsViaPublisher.any { it.startsWith('HIGH kraken@50260') }
    }

    /** Subscribes to {@code agent.changes()} and records every snapshot. */
    private void agentChangesSubscriber(Agent<Map<String, Map>> agent, List sink, CountDownLatch firstFew) {
        agent.changes().subscribe(new Flow.Subscriber<Map<String, Map>>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(Map<String, Map> snap) {
                sink << new HashMap<>(snap)
                firstFew.countDown()
            }
            @Override void onError(Throwable t) {}
            @Override void onComplete() {}
        })
    }

    /** Bridges a publisher of NDJSON lines into the unified channel. */
    private void drainPublisherIntoChannel(Flow.Publisher<String> publisher, AsyncChannel<Map> channel) {
        var slurper = new JsonSlurper()
        publisher.subscribe(new Flow.Subscriber<String>() {
            Flow.Subscription sub
            @Override void onSubscribe(Flow.Subscription s) { sub = s; s.request(Long.MAX_VALUE) }
            @Override void onNext(String line) {
                if (line.trim().isEmpty()) return
                try {
                    var parsed = (Map) slurper.parseText(line)
                    // JsonSlurper returns a LazyMap whose values may share
                    // backing storage with the parser's buffer. Eagerly
                    // materialise plain types before handing off to another
                    // thread.
                    var tick = [
                            symbol: parsed.symbol.toString(),
                            price : (parsed.price as Number).doubleValue(),
                    ]
                    await channel.send(tick)
                } catch (ChannelClosedException ignored) {
                    sub?.cancel()
                } catch (Exception ignored) {
                    // ignore malformed line
                }
            }
            @Override void onError(Throwable t) {}
            @Override void onComplete() {}
        })
    }

    /** Builds an HTTP server that streams NDJSON ticks for a fake exchange. */
    private HttpServer buildExchange(String name, List<Number> prices) {
        var server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        server.createContext('/ticker') { exchange ->
            exchange.responseHeaders.add('Content-Type', 'application/x-ndjson')
            exchange.sendResponseHeaders(200, 0)  // chunked
            exchange.responseBody.withCloseable { out ->
                prices.each { Number p ->
                    var line = /{"symbol":"${name}","price":${p}}/ + '\n'
                    out.write(line.getBytes(StandardCharsets.UTF_8))
                    out.flush()
                    Thread.sleep(15)
                }
            }
        }
        server.start()
        server
    }
}
