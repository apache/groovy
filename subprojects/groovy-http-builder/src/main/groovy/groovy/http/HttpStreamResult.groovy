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

import org.apache.groovy.lang.annotation.Incubating

import java.net.http.HttpHeaders
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Flow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Streaming response wrapper for {@link HttpBuilder}'s {@code streamAsync} family.
 * <p>
 * Unlike {@link HttpResult}, this does not buffer the response body. The body
 * is exposed as a {@link Flow.Publisher} of byte chunks that can be consumed
 * incrementally — typically via {@code for await (chunk in result.bodyAsPublisher())}
 * once the {@code groovy-concurrent-java} {@code FlowPublisherAdapter} is on
 * the classpath.
 */
@Incubating
record HttpStreamResult(
        int status,
        HttpHeaders headers,
        HttpResponse<Flow.Publisher<List<ByteBuffer>>> raw) {

    HttpStreamResult(HttpResponse<Flow.Publisher<List<ByteBuffer>>> response) {
        this(response.statusCode(), response.headers(), response)
    }

    /**
     * Returns the response body as a publisher of {@code List<ByteBuffer>}
     * chunks, exactly as JDK {@code HttpResponse.BodyHandlers.ofPublisher()}
     * provides it.
     */
    Flow.Publisher<List<ByteBuffer>> bodyAsPublisher() {
        return raw.body()
    }

    /**
     * Returns the response body as a publisher of decoded strings, one per
     * arriving chunk. Each emitted string corresponds to one upstream HTTP
     * frame (often a complete SSE event or a JSON line, but boundaries are
     * not guaranteed). Use {@link #bodyAsLinePublisher} for line-aligned
     * delivery.
     *
     * @param charset the charset to decode bytes with (defaults to UTF-8)
     */
    Flow.Publisher<String> bodyAsTextPublisher(Charset charset = StandardCharsets.UTF_8) {
        Closure mapper = { chunk ->
            StringBuilder sb = new StringBuilder()
            for (ByteBuffer buf : (List<ByteBuffer>) chunk) {
                sb.append(charset.decode(buf))
            }
            sb.toString()
        }
        return (Flow.Publisher<String>) new MappedPublisher(raw.body(), mapper)
    }

    /**
     * Returns the response body as a publisher of newline-terminated strings.
     * Useful for line-oriented streaming protocols (NDJSON, SSE without event
     * framing, log tails). Trailing partial lines are emitted on completion.
     *
     * @param charset the charset to decode bytes with (defaults to UTF-8)
     */
    Flow.Publisher<String> bodyAsLinePublisher(Charset charset = StandardCharsets.UTF_8) {
        return new LinePublisher(raw.body(), charset)
    }

    /**
     * One-shot mapping {@link Flow.Publisher} that delegates subscription to a
     * source publisher and applies a transformer per signalled item.
     */
    // package-private for same-package unit testing
    static final class MappedPublisher implements Flow.Publisher<Object> {
        private final Flow.Publisher source
        private final Closure mapper

        MappedPublisher(Flow.Publisher source, Closure mapper) {
            this.source = source
            this.mapper = mapper
        }

        @Override
        void subscribe(Flow.Subscriber<? super Object> downstream) {
            source.subscribe(new MappingSubscriber(downstream, mapper))
        }
    }

    // package-private for same-package unit testing
    static final class MappingSubscriber implements Flow.Subscriber<Object> {
        private final Flow.Subscriber<? super Object> downstream
        private final Closure mapper
        private volatile Flow.Subscription subscription
        private volatile boolean done

        MappingSubscriber(Flow.Subscriber<? super Object> downstream, Closure mapper) {
            this.downstream = downstream
            this.mapper = mapper
        }

        @Override
        void onSubscribe(Flow.Subscription s) {
            this.subscription = s
            downstream.onSubscribe(s)
        }

        @Override
        void onNext(Object item) {
            if (done) return
            Object mapped
            try {
                mapped = mapper.call(item)
            } catch (Throwable t) {
                done = true
                Flow.Subscription s = subscription
                if (s != null) s.cancel()
                downstream.onError(t)
                return
            }
            downstream.onNext(mapped)
        }

        @Override
        void onError(Throwable t) {
            if (done) return
            done = true
            downstream.onError(t)
        }

        @Override
        void onComplete() {
            if (done) return
            done = true
            downstream.onComplete()
        }
    }

    /**
     * Adapts a chunked byte publisher into a publisher of complete lines.
     * Buffers across chunk boundaries so multi-chunk lines emit correctly,
     * and honours downstream demand — a single upstream chunk containing
     * many newlines is drip-fed to the downstream one line per {@code request}.
     */
    // package-private for same-package unit testing
    static final class LinePublisher implements Flow.Publisher<String> {
        private final Flow.Publisher<List<ByteBuffer>> source
        private final Charset charset

        LinePublisher(Flow.Publisher<List<ByteBuffer>> source, Charset charset) {
            this.source = source
            this.charset = charset
        }

        @Override
        void subscribe(Flow.Subscriber<? super String> downstream) {
            source.subscribe(new LineSubscription(downstream, charset))
        }
    }

    /**
     * Flow.Subscription that mediates demand between a chunk publisher and a
     * line subscriber. Parses decoded bytes on '\n', honours '\r\n', and
     * flushes any trailing pending text on upstream completion.
     */
    private static final class LineSubscription implements Flow.Subscription, Flow.Subscriber<List<ByteBuffer>> {

        // Upstream window: chunks kept in flight, replenished when half consumed.
        // Tuned for HTTP line-streaming; not exposed — revisit if pathological.
        private static final int UPSTREAM_BATCH = 8
        private static final int UPSTREAM_REPLENISH_AT = UPSTREAM_BATCH.intdiv(2)

        private final Flow.Subscriber<? super String> downstream
        private final Charset charset

        private final AtomicReference<Flow.Subscription> upstreamSubRef = new AtomicReference<>()

        // Parsed lines (cross-thread: upstream onNext writes, drain reads).
        private final ConcurrentLinkedQueue<String> buffer = new ConcurrentLinkedQueue<>()
        // Partial trailing line (upstream-thread only per §1.3 serialisation).
        private final StringBuilder pending = new StringBuilder()

        private final AtomicLong requested = new AtomicLong()
        private final AtomicBoolean cancelled = new AtomicBoolean()
        private final AtomicInteger wip = new AtomicInteger()
        private final AtomicInteger chunksOutstanding = new AtomicInteger()

        private volatile boolean done
        private volatile Throwable error
        // Drain-thread only (wip-serialised).
        private boolean terminated

        LineSubscription(Flow.Subscriber<? super String> downstream, Charset charset) {
            this.downstream = downstream
            this.charset = charset
        }

        // --- Flow.Subscription (downstream-facing) ---

        @Override
        void request(long n) {
            if (n <= 0) {
                if (cancelled.compareAndSet(false, true)) {
                    Flow.Subscription s = upstreamSubRef.getAndSet(null)
                    if (s != null) s.cancel()
                    downstream.onError(new IllegalArgumentException(
                            "Flow.Subscription.request must be positive (Reactive Streams §3.9): " + n))
                }
                return
            }
            if (cancelled.get()) return
            long r, nr
            do {
                r = requested.get()
                if (r == Long.MAX_VALUE) { drain(); return }
                nr = r + n
                if (nr < 0) nr = Long.MAX_VALUE
            } while (!requested.compareAndSet(r, nr))
            drain()
        }

        @Override
        void cancel() {
            if (cancelled.compareAndSet(false, true)) {
                Flow.Subscription s = upstreamSubRef.getAndSet(null)
                if (s != null) s.cancel()
                drain()
            }
        }

        // --- Flow.Subscriber (upstream-facing) ---

        @Override
        void onSubscribe(Flow.Subscription s) {
            if (!upstreamSubRef.compareAndSet(null, s)) {
                s.cancel()
                return
            }
            downstream.onSubscribe(this)
            // Cancel-before-subscribe race: cancel() may have already flipped
            // the flag but found a null subRef. Clean up the late subscription.
            if (cancelled.get()) {
                Flow.Subscription sub = upstreamSubRef.getAndSet(null)
                if (sub != null) sub.cancel()
            }
        }

        @Override
        void onNext(List<ByteBuffer> chunk) {
            if (cancelled.get() || done) return
            for (ByteBuffer buf : chunk) {
                pending.append(charset.decode(buf))
            }
            int nl
            while ((nl = pending.indexOf('\n')) != -1) {
                String line = pending.substring(0, nl)
                if (line.endsWith('\r')) line = line.substring(0, line.length() - 1)
                pending.delete(0, nl + 1)
                buffer.offer(line)
            }
            chunksOutstanding.decrementAndGet()
            drain()
        }

        @Override
        void onError(Throwable t) {
            if (done) return
            error = t
            done = true
            drain()
        }

        @Override
        void onComplete() {
            if (done) return
            if (pending.length() > 0) {
                buffer.offer(pending.toString())
                pending.setLength(0)
            }
            done = true
            drain()
        }

        // --- drain: emit buffered lines up to downstream demand, serialised by wip ---

        private void drain() {
            if (wip.getAndIncrement() != 0) return
            int missed = 1
            for (;;) {
                if (cancelled.get()) { buffer.clear(); return }
                if (terminated) return

                long r = requested.get()
                long e = 0
                while (e < r) {
                    if (cancelled.get()) { buffer.clear(); return }
                    String line = buffer.poll()
                    if (line == null) break
                    downstream.onNext(line)
                    e++
                }
                if (e > 0 && r != Long.MAX_VALUE) requested.addAndGet(-e)

                if (done && buffer.isEmpty()) {
                    terminated = true
                    Throwable t = error
                    if (t != null) downstream.onError(t)
                    else downstream.onComplete()
                    return
                }

                // Keep the upstream window topped up while downstream has demand.
                if (!done && requested.get() > 0) {
                    int outstanding = chunksOutstanding.get()
                    if (outstanding <= UPSTREAM_REPLENISH_AT) {
                        int deficit = UPSTREAM_BATCH - outstanding
                        chunksOutstanding.addAndGet(deficit)
                        Flow.Subscription s = upstreamSubRef.get()
                        if (s != null) s.request(deficit)
                    }
                }

                missed = wip.addAndGet(-missed)
                if (missed == 0) return
            }
        }
    }
}
