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

import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

/**
 * JDK {@link java.net.http.HttpClient} retries GET/HEAD when a keep-alive
 * connection is already closed, but not PUT/POST/PATCH. HttpBuilder retries
 * that stale-connection failure once so a follow-up hop (or the next call on
 * the same builder) is not failed by a peer that already dropped the socket.
 */
class HttpBuilderStaleConnectionTest {

    @Test
    void retriesPutOnceWhenThePeerDropsTheConnectionBeforeResponseHeaders() {
        withDropFirstConnectionServer { int port, AtomicInteger connections ->
            HttpBuilder http = HttpBuilder.http {
                baseUri "http://127.0.0.1:${port}/"
                connectTimeout Duration.ofSeconds(2)
                requestTimeout Duration.ofSeconds(2)
            }

            HttpResult result = http.put('/') { json([a: 1]) }

            assert result.status == 200
            assert result.body == 'recovered'
            assert connections.get() >= 2
        }
    }

    @Test
    void retriesPutAsyncOnceWhenThePeerDropsTheConnectionBeforeResponseHeaders() {
        withDropFirstConnectionServer { int port, AtomicInteger connections ->
            HttpBuilder http = HttpBuilder.http {
                baseUri "http://127.0.0.1:${port}/"
                connectTimeout Duration.ofSeconds(2)
                requestTimeout Duration.ofSeconds(2)
            }

            HttpResult result = http.putAsync('/') { json([a: 1]) }.get()

            assert result.status == 200
            assert result.body == 'recovered'
            assert connections.get() >= 2
        }
    }

    private static void withDropFirstConnectionServer(Closure<?> body) {
        ServerSocket listener = new ServerSocket()
        listener.bind(new InetSocketAddress('127.0.0.1', 0))
        AtomicInteger connections = new AtomicInteger()
        Thread acceptor = Thread.startDaemon {
            try {
                while (!listener.closed) {
                    Socket socket = listener.accept()
                    int n = connections.incrementAndGet()
                    Thread.startDaemon {
                        socket.withCloseable { Socket s ->
                            consumeHttpRequest(s.inputStream)
                            if (n == 1) {
                                return
                            }
                            byte[] payload = 'recovered'.getBytes(StandardCharsets.UTF_8)
                            OutputStream out = s.outputStream
                            out.write("HTTP/1.1 200 OK\r\nContent-Length: ${payload.length}\r\nConnection: close\r\n\r\n".bytes)
                            out.write(payload)
                            out.flush()
                        }
                    }
                }
            } catch (IOException ignored) {
                // listener closed
            }
        }
        try {
            body.call(listener.localPort, connections)
        } finally {
            listener.close()
            acceptor.join(1000)
        }
    }

    private static void consumeHttpRequest(InputStream input) {
        StringBuilder headers = new StringBuilder()
        int seen = 0
        int ch
        while ((ch = input.read()) != -1) {
            headers.append((char) ch)
            if (ch == (int) '\r' && (seen == 0 || seen == 2)) {
                seen++
            } else if (ch == (int) '\n' && (seen == 1 || seen == 3)) {
                seen++
                if (seen == 4) {
                    break
                }
            } else if (ch != (int) '\r') {
                seen = 0
            }
        }
        def matcher = headers.toString() =~ /(?i)Content-Length:\s*(\d+)/
        int length = matcher.find() ? matcher.group(1).toInteger() : 0
        if (length > 0) {
            input.skipNBytes(length)
        }
    }
}
