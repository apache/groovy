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
package groovy.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Decodes a buffered response body sent with a {@code gzip} or {@code deflate}
 * {@code Content-Encoding}. The JDK client neither requests nor decodes
 * compressed content, so a compressed body would otherwise reach the body
 * handler as raw bytes.
 */
final class ContentDecoding {

    /** The {@code Accept-Encoding} value sent when compression is enabled. */
    static final String ACCEPT_ENCODING = "gzip, deflate";

    private ContentDecoding() {
    }

    /**
     * Returns the content codings applied to a body, in the order they were
     * applied, or {@code null} if there are none or one of them is not
     * supported, in which case the body is passed on as received.
     */
    static List<String> codings(final HttpHeaders headers) {
        List<String> codings = new ArrayList<>();
        for (String value : headers.allValues("Content-Encoding")) {
            for (String token : value.split(",")) {
                String coding = token.trim().toLowerCase(Locale.ROOT);
                if (coding.isEmpty() || coding.equals("identity")) continue;
                if (coding.equals("x-gzip")) coding = "gzip"; // RFC 9110, 8.4.1.3
                if (!coding.equals("gzip") && !coding.equals("deflate")) return null;
                codings.add(coding);
            }
        }
        return codings.isEmpty() ? null : codings;
    }

    /**
     * Wraps a body handler so that a body with supported content codings is
     * decoded before it reaches the handler's subscriber.
     */
    static <T> HttpResponse.BodyHandler<T> decoding(final HttpResponse.BodyHandler<T> handler) {
        return responseInfo -> {
            HttpResponse.BodySubscriber<T> target = handler.apply(responseInfo);
            List<String> codings = codings(responseInfo.headers());
            return codings == null ? target : new DecodingSubscriber<>(target, codings);
        };
    }

    /**
     * Returns the headers that describe the body after decoding: without
     * {@code Content-Encoding} and {@code Content-Length}, which describe the
     * encoded body, when the body was decoded.
     */
    static HttpHeaders decodedHeaders(final HttpResponse<?> response) {
        HttpHeaders headers = response.headers();
        if (codings(headers) == null) return headers;
        return HttpHeaders.of(headers.map(), (name, value) ->
                !name.equalsIgnoreCase("Content-Encoding") && !name.equalsIgnoreCase("Content-Length"));
    }

    static byte[] decode(byte[] bytes, final List<String> codings) throws IOException {
        for (int i = codings.size() - 1; i >= 0 && bytes.length > 0; i -= 1) {
            bytes = "gzip".equals(codings.get(i)) ? gunzip(bytes) : inflate(bytes);
        }
        return bytes;
    }

    private static byte[] gunzip(final byte[] bytes) throws IOException {
        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return in.readAllBytes();
        }
    }

    /**
     * "deflate" is meant to be zlib-wrapped (RFC 1950), but some servers send a
     * raw deflate stream (RFC 1951); a zlib header is recognized by its check bits.
     */
    private static byte[] inflate(final byte[] bytes) throws IOException {
        boolean zlib = bytes.length >= 2 && (bytes[0] & 0x0F) == 8
                && (((bytes[0] & 0xFF) << 8) | (bytes[1] & 0xFF)) % 31 == 0;
        Inflater inflater = new Inflater(!zlib);
        try (InputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes), inflater)) {
            return in.readAllBytes();
        } finally {
            inflater.end();
        }
    }

    /**
     * Collects the encoded body, then delivers the decoded body to the target
     * subscriber as a single item.
     */
    private static final class DecodingSubscriber<T> implements HttpResponse.BodySubscriber<T> {
        private final HttpResponse.BodySubscriber<T> target;
        private final List<String> codings;
        private final ByteArrayOutputStream encoded = new ByteArrayOutputStream();

        DecodingSubscriber(final HttpResponse.BodySubscriber<T> target, final List<String> codings) {
            this.target = target;
            this.codings = codings;
        }

        @Override
        public CompletionStage<T> getBody() {
            return target.getBody();
        }

        @Override
        public void onSubscribe(final Flow.Subscription subscription) {
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(final List<ByteBuffer> items) {
            for (ByteBuffer item : items) {
                byte[] chunk = new byte[item.remaining()];
                item.get(chunk);
                encoded.write(chunk, 0, chunk.length);
            }
        }

        @Override
        public void onError(final Throwable throwable) {
            target.onSubscribe(new Delivery(null));
            target.onError(throwable);
        }

        @Override
        public void onComplete() {
            byte[] decoded;
            try {
                decoded = decode(encoded.toByteArray(), codings);
            } catch (IOException e) {
                onError(new IOException("Cannot decode the " + String.join(", ", codings) + " response body: " + e.getMessage(), e));
                return;
            }
            if (decoded.length == 0) {
                target.onSubscribe(new Delivery(null));
                target.onComplete();
            } else {
                target.onSubscribe(new Delivery(ByteBuffer.wrap(decoded)));
            }
        }

        /** Delivers the decoded body, if any, on the first request, then completes. */
        private final class Delivery implements Flow.Subscription {
            private final ByteBuffer body;
            private final AtomicBoolean done = new AtomicBoolean();

            Delivery(final ByteBuffer body) {
                this.body = body;
                if (body == null) done.set(true);
            }

            @Override
            public void request(final long n) {
                if (!done.compareAndSet(false, true)) return;
                if (n <= 0) {
                    target.onError(new IllegalArgumentException("non-positive request: " + n));
                } else {
                    target.onNext(List.of(body));
                    target.onComplete();
                }
            }

            @Override
            public void cancel() {
                done.set(true);
            }
        }
    }
}
