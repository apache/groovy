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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.GZIPOutputStream

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertThrows

/**
 * Decoding of compressed response bodies, and the opt-in request for them.
 */
final class HttpBuilderCompressionTest {

    private static final String JSON = '{"name":"groovy","license":"Apache License 2.0"}'

    private HttpServer server
    private HttpServer otherServer
    private URI rootUri
    private URI otherUri

    @BeforeEach
    void setup() {
        server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        // sends JSON encoded with the codings named by the query, e.g. ?deflate,gzip
        server.createContext('/data') { HttpExchange exchange ->
            String query = exchange.requestURI.rawQuery
            List<String> codings = query ? query.split(',') as List<String> : []
            byte[] bytes = JSON.getBytes(StandardCharsets.UTF_8)
            for (coding in codings) bytes = encode(bytes, coding)
            exchange.responseHeaders.add('Content-Type', 'application/json')
            if (codings) {
                exchange.responseHeaders.add('Content-Encoding',
                        codings.collect { it == 'raw-deflate' ? 'deflate' : it }.join(', '))
            }
            echoAcceptEncoding(exchange)
            if (exchange.requestMethod == 'HEAD') {
                exchange.sendResponseHeaders(200, -1)
                exchange.close()
            } else {
                exchange.sendResponseHeaders(200, bytes.length)
                exchange.responseBody.withCloseable { it.write(bytes) }
            }
        }
        server.createContext('/unsupported') { HttpExchange exchange ->
            byte[] bytes = 'not decoded'.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Encoding', 'br')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/corrupt') { HttpExchange exchange ->
            byte[] bytes = 'this is not gzip'.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Encoding', 'gzip')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.start()
        rootUri = URI.create("http://127.0.0.1:${server.address.port}/")

        otherServer = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        otherServer.createContext('/moved') { HttpExchange exchange ->
            exchange.responseHeaders.add('Location', "${rootUri}data?gzip")
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }
        otherServer.start()
        otherUri = URI.create("http://127.0.0.1:${otherServer.address.port}/")
    }

    @AfterEach
    void cleanup() {
        server?.stop(0)
        otherServer?.stop(0)
    }

    private static void echoAcceptEncoding(HttpExchange exchange) {
        String acceptEncoding = exchange.requestHeaders.getFirst('Accept-Encoding')
        exchange.responseHeaders.add('X-Accept-Encoding', acceptEncoding ?: 'none')
    }

    private static byte[] encode(byte[] bytes, String coding) {
        def out = new ByteArrayOutputStream()
        switch (coding) {
            case 'gzip':
            case 'x-gzip':
                new GZIPOutputStream(out).withCloseable { it.write(bytes) }
                break
            case 'deflate':
                new DeflaterOutputStream(out).withCloseable { it.write(bytes) }
                break
            case 'raw-deflate':
                def deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true)
                new DeflaterOutputStream(out, deflater).withCloseable { it.write(bytes) }
                deflater.end()
                break
            default:
                throw new IllegalArgumentException(coding)
        }
        out.toByteArray()
    }

    @Test
    void decodesGzipRequestedWithAnExplicitHeader() {
        def http = HttpBuilder.http(rootUri.toString())
        def result = http.get('/data?gzip') { header 'Accept-Encoding', 'gzip' }
        assert result.status() == 200
        assert result.body() == JSON
        assert result.json.name == 'groovy'
        assert result.headers().firstValue('X-Accept-Encoding').get() == 'gzip'
        // the result describes the decoded body; the raw response is as received
        assert !result.headers().firstValue('Content-Encoding').present
        assert !result.headers().firstValue('Content-Length').present
        assert result.headers().firstValue('Content-Type').get() == 'application/json'
        assert result.raw().headers().firstValue('Content-Encoding').get() == 'gzip'
    }

    @Test
    void decodesEachSupportedCoding() {
        def http = HttpBuilder.http(rootUri.toString())
        for (codings in ['gzip', 'x-gzip', 'deflate', 'raw-deflate', 'deflate,gzip', 'gzip,gzip']) {
            def result = http.get("/data?$codings")
            assert result.body() == JSON : codings
            assert result.json.license == 'Apache License 2.0' : codings
        }
    }

    @Test
    void passesOnAnUnencodedBody() {
        def result = HttpBuilder.http(rootUri.toString()).get('/data')
        assert result.body() == JSON
        assert result.headers().firstValue('Content-Length').get() == String.valueOf(JSON.length())
    }

    @Test
    void passesOnABodyWithAnUnsupportedCoding() {
        def result = HttpBuilder.http(rootUri.toString()).get('/unsupported')
        assert result.body() == 'not decoded'
        assert result.headers().firstValue('Content-Encoding').get() == 'br'
    }

    @Test
    void decodesAnEmptyBody() {
        def result = HttpBuilder.http(rootUri.toString()).request('HEAD', '/data?gzip')
        assert result.status() == 200
        assert result.body() == ''
    }

    @Test
    void reportsABodyThatCannotBeDecoded() {
        def http = HttpBuilder.http(rootUri.toString())
        def e = assertThrows(RuntimeException) { http.get('/corrupt') }
        assert e.cause instanceof IOException
        assert e.cause.message.contains('Cannot decode the gzip response body')
    }

    @Test
    void doesNotAskForCompressionByDefault() {
        def result = HttpBuilder.http(rootUri.toString()).get('/data')
        assert result.headers().firstValue('X-Accept-Encoding').get() == 'none'
    }

    @Test
    void asksForCompressionWhenEnabled() {
        def http = HttpBuilder.http {
            baseUri rootUri
            compression true
        }
        def result = http.get('/data?gzip')
        assert result.headers().firstValue('X-Accept-Encoding').get() == 'gzip, deflate'
        assert result.json.name == 'groovy'
    }

    @Test
    void anExplicitAcceptEncodingHeaderWins() {
        def http = HttpBuilder.http {
            baseUri rootUri
            compression true
        }
        def result = http.get('/data') { header 'accept-encoding', 'identity' }
        assert result.headers().firstValue('X-Accept-Encoding').get() == 'identity'
        assert result.body() == JSON
    }

    @Test
    void asksForCompressionOnARedirectHopToAnotherOrigin() {
        def http = HttpBuilder.http {
            baseUri otherUri
            followRedirects true
            compression true
        }
        def result = http.get('/moved')
        assert result.status() == 200
        assert result.headers().firstValue('X-Accept-Encoding').get() == 'gzip, deflate'
        assert result.json.name == 'groovy'
    }

    @Test
    void decodesAsynchronously() {
        def http = HttpBuilder.http {
            baseUri rootUri
            compression true
        }
        def result = http.getAsync('/data?deflate').get()
        assert result.body() == JSON
        assert !result.headers().firstValue('Content-Encoding').present

        def e = assertThrows(ExecutionException) { http.getAsync('/corrupt').get() }
        assert e.cause instanceof IOException
    }

    @Test
    void streamingNeitherAsksForNorDecodesCompression() {
        def http = HttpBuilder.http {
            baseUri rootUri
            compression true
        }
        def result = http.getStreamAsync('/data').get()
        assert result.headers().firstValue('X-Accept-Encoding').get() == 'none'
    }

    @Test
    void declarativeClient() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient(value = '${rootUri}', compression = true)
            interface DataApi {
                @Get('/data?gzip')
                Map data()

                @Get('/data?gzip')
                HttpResult result()
            }

            def api = DataApi.create()
            assert api.data() == [name: 'groovy', license: 'Apache License 2.0']
            assert api.result().headers().firstValue('X-Accept-Encoding').get() == 'gzip, deflate'
        """
    }
}
