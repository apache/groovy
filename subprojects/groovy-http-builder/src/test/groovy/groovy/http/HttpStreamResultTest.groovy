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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Flow
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class HttpStreamResultTest {

    private static final long AWAIT_TIMEOUT_SECONDS = 10

    private HttpServer server
    private URI rootUri

    @BeforeEach
    void setup() {
        server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        server.createContext('/lines') { exchange ->
            // chunked transfer (size 0)
            exchange.responseHeaders.add('Content-Type', 'text/plain')
            exchange.sendResponseHeaders(200, 0)
            exchange.responseBody.withCloseable { out ->
                (1..5).each { i ->
                    out.write("line-${i}\n".getBytes(StandardCharsets.UTF_8))
                    out.flush()
                    Thread.sleep(10)
                }
            }
        }
        server.createContext('/bytes') { exchange ->
            byte[] body = ('A'..'Z').collect { it.toString() }.join().getBytes(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(200, body.length)
            exchange.responseBody.withCloseable { it.write(body) }
        }
        server.start()
        rootUri = URI.create("http://127.0.0.1:${server.address.port}/")
    }

    @AfterEach
    void cleanup() {
        server?.stop(0)
    }

    @Test
    void streamsBytesAsPublisher() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())
        CompletableFuture<HttpStreamResult> future = http.getStreamAsync('/bytes')
        HttpStreamResult result = future.join()
        assert result.status() == 200

        // Drain the publisher manually to a collected string
        StringBuilder collected = new StringBuilder()
        def latch = new CountDownLatch(1)
        result.bodyAsPublisher().subscribe(new Flow.Subscriber<List>() {
            Flow.Subscription sub
            @Override void onSubscribe(Flow.Subscription s) { sub = s; s.request(Long.MAX_VALUE) }
            @Override void onNext(List buffers) {
                buffers.each { collected.append(StandardCharsets.UTF_8.decode(it)) }
            }
            @Override void onError(Throwable t) { latch.countDown() }
            @Override void onComplete() { latch.countDown() }
        })
        assert latch.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS), 'publisher did not complete in time'
        assert collected.toString() == ('A'..'Z').collect { it.toString() }.join()
    }

    @Test
    void streamsLinesAsLinePublisher() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())
        HttpStreamResult result = http.getStreamAsync('/lines').join()
        assert result.status() == 200

        List<String> lines = []
        def latch = new CountDownLatch(1)
        result.bodyAsLinePublisher().subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(String line) { lines << line }
            @Override void onError(Throwable t) { latch.countDown() }
            @Override void onComplete() { latch.countDown() }
        })
        assert latch.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS), 'publisher did not complete in time'
        assert lines == ['line-1', 'line-2', 'line-3', 'line-4', 'line-5']
    }

    @Test
    void mappingSubscriberCancelsUpstreamWhenMapperThrows() {
        AtomicBoolean upstreamCancelled = new AtomicBoolean()
        AtomicReference<Throwable> received = new AtomicReference<>()
        AtomicBoolean completed = new AtomicBoolean()
        AtomicReference<Flow.Subscription> upstreamSub = new AtomicReference<>()

        Flow.Publisher upstream = { Flow.Subscriber sub ->
            upstreamSub.set(sub as Flow.Subscriber)
            sub.onSubscribe(new Flow.Subscription() {
                @Override void request(long n) {}
                @Override void cancel() { upstreamCancelled.set(true) }
            })
        } as Flow.Publisher

        Flow.Subscriber downstream = new Flow.Subscriber<Object>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(Object o) {}
            @Override void onError(Throwable t) { received.set(t) }
            @Override void onComplete() { completed.set(true) }
        }

        def mapping = new HttpStreamResult.MappingSubscriber(downstream, { throw new RuntimeException('boom') } as Closure)
        upstream.subscribe(mapping)

        mapping.onNext('item-1')
        assert upstreamCancelled.get(), 'upstream must be cancelled when mapper throws'
        assert received.get()?.message == 'boom'

        // Subsequent signals must be dropped (§1.7).
        mapping.onNext('item-2')
        mapping.onError(new RuntimeException('second'))
        mapping.onComplete()

        assert received.get().message == 'boom', 'onError must not be signalled twice'
        assert !completed.get(), 'onComplete must not fire after onError'
    }

    @Test
    void mappingSubscriberIgnoresSignalsAfterComplete() {
        AtomicReference<Throwable> received = new AtomicReference<>()
        int completions = 0
        int items = 0

        Flow.Subscriber downstream = new Flow.Subscriber<Object>() {
            @Override void onSubscribe(Flow.Subscription s) {}
            @Override void onNext(Object o) { items++ }
            @Override void onError(Throwable t) { received.set(t) }
            @Override void onComplete() { completions++ }
        }

        def mapping = new HttpStreamResult.MappingSubscriber(downstream, { it } as Closure)
        mapping.onSubscribe(new Flow.Subscription() {
            @Override void request(long n) {}
            @Override void cancel() {}
        })
        mapping.onNext('a')
        mapping.onComplete()
        // Misbehaving upstream keeps signalling past terminal — we must ignore.
        mapping.onNext('b')
        mapping.onComplete()
        mapping.onError(new RuntimeException('late'))

        assert items == 1
        assert completions == 1
        assert received.get() == null
    }

    @Test
    void linePublisherHonoursBoundedDownstreamDemand() {
        // One chunk with three lines; downstream requests only one — exactly
        // one onNext must fire, even though upstream delivered enough for three.
        def source = new SubmissionPublisher<List<ByteBuffer>>()
        def lp = new HttpStreamResult.LinePublisher(source, StandardCharsets.UTF_8)

        List<String> received = Collections.synchronizedList([])
        AtomicReference<Flow.Subscription> sub = new AtomicReference<>()
        CountDownLatch subscribed = new CountDownLatch(1)
        lp.subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { sub.set(s); subscribed.countDown() }
            @Override void onNext(String line) { received << line }
            @Override void onError(Throwable t) {}
            @Override void onComplete() {}
        })
        assert subscribed.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        source.submit([ByteBuffer.wrap("one\ntwo\nthree\n".getBytes(StandardCharsets.UTF_8))])
        sub.get().request(1)

        waitUntil { received.size() >= 1 }
        assert received == ['one']

        sub.get().request(1)
        waitUntil { received.size() >= 2 }
        assert received == ['one', 'two']

        // No further signals without more demand.
        Thread.sleep(50)
        assert received == ['one', 'two']

        source.close()
    }

    private static void waitUntil(Closure<Boolean> condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(AWAIT_TIMEOUT_SECONDS)
        while (!condition.call() && System.nanoTime() < deadline) {
            Thread.sleep(5)
        }
    }

    @Test
    void linePublisherJoinsLineAcrossChunks() {
        def source = new SubmissionPublisher<List<ByteBuffer>>()
        def lp = new HttpStreamResult.LinePublisher(source, StandardCharsets.UTF_8)

        List<String> received = []
        CountDownLatch completed = new CountDownLatch(1)
        lp.subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(String line) { received << line }
            @Override void onError(Throwable t) { completed.countDown() }
            @Override void onComplete() { completed.countDown() }
        })

        source.submit([ByteBuffer.wrap('hel'.getBytes(StandardCharsets.UTF_8))])
        source.submit([ByteBuffer.wrap('lo\nwo'.getBytes(StandardCharsets.UTF_8))])
        source.submit([ByteBuffer.wrap('rld\n'.getBytes(StandardCharsets.UTF_8))])
        source.close()

        assert completed.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        assert received == ['hello', 'world']
    }

    @Test
    void linePublisherFlushesTrailingPartialLineOnComplete() {
        def source = new SubmissionPublisher<List<ByteBuffer>>()
        def lp = new HttpStreamResult.LinePublisher(source, StandardCharsets.UTF_8)

        List<String> received = []
        CountDownLatch completed = new CountDownLatch(1)
        lp.subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(Long.MAX_VALUE) }
            @Override void onNext(String line) { received << line }
            @Override void onError(Throwable t) {}
            @Override void onComplete() { completed.countDown() }
        })

        source.submit([ByteBuffer.wrap('a\nb'.getBytes(StandardCharsets.UTF_8))])
        source.close()

        assert completed.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        assert received == ['a', 'b']
    }

    @Test
    void linePublisherRejectsNonPositiveRequest() {
        def source = new SubmissionPublisher<List<ByteBuffer>>()
        def lp = new HttpStreamResult.LinePublisher(source, StandardCharsets.UTF_8)

        AtomicReference<Throwable> error = new AtomicReference<>()
        CountDownLatch signalled = new CountDownLatch(1)
        lp.subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { s.request(0L) }
            @Override void onNext(String line) {}
            @Override void onError(Throwable t) { error.set(t); signalled.countDown() }
            @Override void onComplete() { signalled.countDown() }
        })

        assert signalled.await(AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        assert error.get() instanceof IllegalArgumentException
        assert error.get().message.contains('§3.9')
        source.close()
    }

    @Test
    void linePublisherCancelStopsUpstreamAndDownstream() {
        AtomicBoolean upstreamCancelled = new AtomicBoolean()
        AtomicLong chunkRequests = new AtomicLong()
        AtomicReference<Flow.Subscriber<? super List<ByteBuffer>>> upstreamSub = new AtomicReference<>()
        Flow.Publisher<List<ByteBuffer>> source = { sub ->
            upstreamSub.set(sub as Flow.Subscriber)
            sub.onSubscribe(new Flow.Subscription() {
                @Override void request(long n) { chunkRequests.addAndGet(n) }
                @Override void cancel() { upstreamCancelled.set(true) }
            })
        } as Flow.Publisher<List<ByteBuffer>>

        def lp = new HttpStreamResult.LinePublisher(source, StandardCharsets.UTF_8)

        int received = 0
        AtomicReference<Flow.Subscription> sub = new AtomicReference<>()
        lp.subscribe(new Flow.Subscriber<String>() {
            @Override void onSubscribe(Flow.Subscription s) { sub.set(s) }
            @Override void onNext(String line) { received++ }
            @Override void onError(Throwable t) {}
            @Override void onComplete() {}
        })

        sub.get().request(10)
        assert chunkRequests.get() > 0, 'upstream should have received a request once downstream demanded lines'

        // Deliver one line, then cancel; subsequent onNext signals must be ignored.
        upstreamSub.get().onNext([ByteBuffer.wrap('only-line\n'.getBytes(StandardCharsets.UTF_8))])
        Thread.sleep(20)
        sub.get().cancel()
        assert upstreamCancelled.get()

        upstreamSub.get().onNext([ByteBuffer.wrap('ignored\n'.getBytes(StandardCharsets.UTF_8))])
        Thread.sleep(20)
        assert received == 1
    }

    @Test
    void forAwaitOverLinePublisherViaAdapter() {
        // Drives the FlowPublisherAdapter end-to-end against a real HTTP stream.
        def shell = new GroovyShell(new Binding(rootUri: rootUri))
        shell.evaluate '''
            import groovy.http.HttpBuilder

            def http = HttpBuilder.http(rootUri.toString())
            def result = http.getStreamAsync('/lines').join()

            def collected = []
            for await (line in result.bodyAsLinePublisher()) {
                collected << line
            }
            assert collected == ['line-1', 'line-2', 'line-3', 'line-4', 'line-5']
        '''
    }
}
