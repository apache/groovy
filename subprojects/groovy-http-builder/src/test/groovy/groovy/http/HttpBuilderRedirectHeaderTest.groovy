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

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer

import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.BiPredicate
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * GROOVY-12274: headers the caller configured for one origin must not be carried to another by
 * following a redirect. The JDK client cannot be relied on for this. It protects only the header
 * names it knows about, so a credential in any other header is forwarded regardless; and whether
 * it protects even those depends on the update level of the JDK in use.
 */
class HttpBuilderRedirectHeaderTest {

    static HttpServer origin
    static HttpServer elsewhere
    static int originPort
    static int elsewherePort
    /** Headers seen by whichever endpoint served the final hop; written on server threads. */
    static final Map<String, String> received = new ConcurrentHashMap<>()
    /** Counts final-hop arrivals, so a negative assertion cannot pass by the hop never happening. */
    static final AtomicInteger arrivals = new AtomicInteger()
    /** Body seen by whichever endpoint served the final hop; written on server threads. */
    static volatile String receivedBody

    @BeforeAll
    static void setUpClass() {
        origin = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        originPort = origin.address.port
        elsewhere = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        elsewherePort = elsewhere.address.port

        // The redirect target is passed as the query so one handler serves every case.
        origin.createContext('/redirect') { HttpExchange exchange ->
            exchange.responseHeaders.add('Location', exchange.requestURI.query)
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }
        // 307 variant: the method and body are preserved across the hop.
        origin.createContext('/redirect307') { HttpExchange exchange ->
            exchange.responseHeaders.add('Location', exchange.requestURI.query)
            exchange.sendResponseHeaders(307, -1)
            exchange.close()
        }
        [origin, elsewhere].each { server ->
            server.createContext('/target') { HttpExchange exchange ->
                arrivals.incrementAndGet()
                record(exchange)
                receivedBody = exchange.requestBody.text
                byte[] body = 'ok'.bytes
                exchange.sendResponseHeaders(200, body.length)
                exchange.responseBody.withStream { it.write(body) }
            }
        }
        // Second hop for the return-to-origin case.
        elsewhere.createContext('/bounce') { HttpExchange exchange ->
            exchange.responseHeaders.add('Location', URLDecoder.decode(exchange.requestURI.query, 'UTF-8'))
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }
        origin.start()
        elsewhere.start()
    }

    @AfterAll
    static void tearDownClass() {
        origin?.stop(0)
        elsewhere?.stop(0)
    }

    @BeforeEach
    void setUp() {
        received.clear()
        arrivals.set(0)
        receivedBody = null
    }

    private static void record(HttpExchange exchange) {
        ['Authorization', 'Cookie', 'X-Api-Key', 'Accept', 'Content-Type'].each { name ->
            def values = exchange.requestHeaders.get(name)
            if (values) received.put(name, values.first())
        }
    }

    private static HttpBuilder builderWithHeaders() {
        HttpBuilder.http {
            baseUri "http://127.0.0.1:${originPort}"
            followRedirects true
            headers([
                    'Authorization': 'Bearer SECRET-TOKEN',
                    'Cookie'       : 'session=SECRET-COOKIE',
                    'X-Api-Key'    : 'SECRET-KEY',
                    'Accept'       : 'text/plain',
            ])
        }
    }

    @Test
    void testHeadersAreNotCarriedToAnotherOrigin() {
        builderWithHeaders().get("/redirect?http://127.0.0.1:${elsewherePort}/target")

        assert arrivals.get() == 1, 'the redirect was not followed, so the test proves nothing'
        assert received.isEmpty(),
                "a redirect to another origin received ${received.keySet()}"
    }

    @Test
    void testCustomHeaderIsNotCarriedEither() {
        // The header the JDK never protects, on any update level: the whole reason this is not
        // left to the platform.
        builderWithHeaders().get("/redirect?http://127.0.0.1:${elsewherePort}/target")

        assert arrivals.get() == 1, 'the redirect was not followed, so the test proves nothing'
        assert received['X-Api-Key'] == null
    }

    @Test
    void testHeadersSurviveARedirectWithinTheSameOrigin() {
        builderWithHeaders().get("/redirect?http://127.0.0.1:${originPort}/target")

        assert received['Authorization'] == 'Bearer SECRET-TOKEN'
        assert received['X-Api-Key'] == 'SECRET-KEY'
        assert received['Accept'] == 'text/plain'
    }

    @Test
    void testHeadersAreNotCarriedToAnotherOriginAsynchronously() {
        builderWithHeaders()
                .requestAsync('GET', "/redirect?http://127.0.0.1:${elsewherePort}/target")
                .join()

        assert arrivals.get() == 1, 'the redirect was not followed, so the test proves nothing'
        assert received.isEmpty(),
                "an asynchronous redirect to another origin received ${received.keySet()}"
    }

    @Test
    void testClientConfigCannotHandRedirectsBackToTheJdkClient() {
        // A redirect policy set on the raw builder would make the JDK client follow hops
        // internally, bypassing the header shedding; it is overridden, so the manual loop
        // still follows the redirect and the other origin still sees no caller headers.
        def builder = HttpBuilder.http {
            baseUri "http://127.0.0.1:${originPort}"
            followRedirects true
            // X-Api-Key is the discriminating probe: a JDK-followed hop forwards it on every
            // JDK version, so this test fails without the override no matter the platform
            headers(['Authorization': 'Bearer SECRET-TOKEN', 'X-Api-Key': 'SECRET-KEY'])
            clientConfig { it.followRedirects(HttpClient.Redirect.NORMAL) }
        }
        builder.get("/redirect?http://127.0.0.1:${elsewherePort}/target")

        assert arrivals.get() == 1, 'the redirect was not followed, so the test proves nothing'
        assert received.isEmpty(),
                "a redirect to another origin received ${received.keySet()}"
    }

    @Test
    void testContentTypeTravelsWithABodyForwardedAcrossOrigins() {
        // A 307 forwards the body, so the header describing it goes too — the one caller
        // header that survives leaving the origin. Credentials are still shed.
        builderWithHeaders().post("/redirect307?http://127.0.0.1:${elsewherePort}/target") {
            json([user: 'alice'])
        }

        assert arrivals.get() == 1, 'the redirect was not followed, so the test proves nothing'
        assert receivedBody == '{"user":"alice"}'
        assert received['Content-Type'] == 'application/json'
        assert received['Authorization'] == null
        assert received['Cookie'] == null
        assert received['X-Api-Key'] == null
    }

    @Test
    void testContentTypeIsShedWhenTheBodyIsDropped() {
        // A 302 downgrades POST to GET and drops the body, so Content-Type no longer
        // describes anything and is shed along with every other caller header.
        builderWithHeaders().post("/redirect?http://127.0.0.1:${elsewherePort}/target") {
            json([user: 'alice'])
        }

        assert arrivals.get() == 1, 'the redirect was not followed, so the test proves nothing'
        assert receivedBody == ''
        assert received.isEmpty(),
                "a body-dropping redirect to another origin received ${received.keySet()}"
    }

    @Test
    void testHttpsToHttpDowngradeHopIsNotFollowed() {
        // Matching HttpClient.Redirect.NORMAL, a hop from an https URL to a plain http one
        // is never followed: the 3xx surfaces to the caller instead. Exercised at the
        // decision level since the test rig has no TLS endpoint.
        def builder = HttpBuilder.http {
            baseUri 'https://secure.example.com'
            followRedirects true
        }
        def start = URI.create('https://secure.example.com/start')

        assert builder.redirectTarget(start, redirectResponse('http://127.0.0.1/target')) == null
        assert builder.redirectTarget(start, redirectResponse('https://other.example.com/target')) ==
                URI.create('https://other.example.com/target')
    }

    private static HttpResponse redirectResponse(String location) {
        def headers = HttpHeaders.of(['Location': [location]], { a, b -> true } as BiPredicate)
        [statusCode: { -> 302 }, headers: { -> headers }] as HttpResponse
    }

    @Test
    void testHeadersAreNotRestoredByReturningToTheOrigin() {
        // Once a chain has left the origin the caller's headers are gone for good; a hop back
        // does not bring them out again.
        String back = URLEncoder.encode("http://127.0.0.1:${originPort}/target", 'UTF-8')
        builderWithHeaders().get("/redirect?http://127.0.0.1:${elsewherePort}/bounce?${back}")

        assert arrivals.get() == 1, 'the chain did not reach the origin again, so the test proves nothing'
        assert received['Authorization'] == null
    }
}
