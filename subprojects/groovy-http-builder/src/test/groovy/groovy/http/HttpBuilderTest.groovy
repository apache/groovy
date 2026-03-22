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

import static org.junit.jupiter.api.Assertions.assertThrows

import java.nio.charset.StandardCharsets
import java.time.Duration

class HttpBuilderTest {

    private HttpServer server
    private URI rootUri

    @BeforeEach
    void setup() {
        server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        server.createContext('/hello') { exchange ->
            String body = "method=${exchange.requestMethod};query=${exchange.requestURI.query};ua=${exchange.requestHeaders.getFirst('User-Agent')}"
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/echo') { exchange ->
            String requestBody = exchange.requestBody.getText(StandardCharsets.UTF_8.name())
            String body = "method=${exchange.requestMethod};header=${exchange.requestHeaders.getFirst('X-Trace')};body=${requestBody}"
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(201, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/json') { exchange ->
            String requestBody = exchange.requestBody.getText(StandardCharsets.UTF_8.name())
            String contentType = exchange.requestHeaders.getFirst('Content-Type')
            String body = /{"ok":true,"contentType":"${contentType}","requestBody":${requestBody}}/
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Type', 'application/json')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/xml') { exchange ->
            String body = '<repo><name>groovy</name><license>Apache License 2.0</license></repo>'
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Type', 'application/xml')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/plain') { exchange ->
            String body = 'just text'
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Type', 'text/plain')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/form') { exchange ->
            String requestBody = exchange.requestBody.getText(StandardCharsets.UTF_8.name())
            String contentType = exchange.requestHeaders.getFirst('Content-Type')
            String body = "method=${exchange.requestMethod};contentType=${contentType};body=${requestBody}"
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/html') { exchange ->
            String body = '<!DOCTYPE html><html><head><link rel="preconnect" crossorigin></head><body><span class="b lic">Apache License 2.0</span></body></html>'
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Type', 'text/html; charset=UTF-8')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/redirect-target') { exchange ->
            String body = 'redirect reached'
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/redirect-me') { exchange ->
            exchange.responseHeaders.add('Location', '/redirect-target')
            exchange.sendResponseHeaders(302, -1)
            exchange.close()
        }
        server.start()
        rootUri = URI.create("http://127.0.0.1:${server.address.port}/")
    }

    @AfterEach
    void cleanup() {
        server?.stop(0)
    }

    @Test
    void getsWithBaseUriDefaultHeadersAndQueryDsl() {
        HttpBuilder http = HttpBuilder.http {
            baseUri rootUri
            connectTimeout Duration.ofSeconds(2)
            requestTimeout Duration.ofSeconds(2)
            header 'User-Agent', 'groovy-http-builder-test'
        }

        HttpResult result = http.get('/hello') {
            query lang: 'groovy', page: 1
        }

        assert result.status == 200
        assert result.body.contains('method=GET')
        assert result.body.contains('lang=groovy')
        assert result.body.contains('page=1')
        assert result.body.contains('ua=groovy-http-builder-test')
    }

    @Test
    void getsUsingStringBaseUriFactoryWithoutClosureConfig() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())

        HttpResult result = http.get('/hello') {
            query page: 1
        }

        assert result.status == 200
        assert result.body.contains('method=GET')
        assert result.body.contains('page=1')
    }

    @Test
    void relativeUriWithoutBaseUriConfiguredThrows() {
        HttpBuilder http = HttpBuilder.http {
            header 'User-Agent', 'groovy-http-builder-test'
        }

        IllegalArgumentException error = assertThrows(IllegalArgumentException) {
            http.get('/hello')
        }

        assert error.message == 'Request URI must be absolute when no baseUri is configured'
    }

    @Test
    void omittedUriWithoutBaseUriConfiguredThrows() {
        HttpBuilder http = HttpBuilder.http {
            header 'User-Agent', 'groovy-http-builder-test'
        }

        IllegalArgumentException error = assertThrows(IllegalArgumentException) {
            http.get()
        }

        assert error.message == 'URI must be provided when no baseUri is configured'
    }

    @Test
    void relativeBaseUriConfiguredInClosureThrows() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException) {
            HttpBuilder.http {
                baseUri '/api'
            }
        }

        assert error.message == 'baseUri must be an absolute URI with scheme and host'
    }

    @Test
    void relativeBaseUriConfiguredViaStringFactoryThrows() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException) {
            HttpBuilder.http('/api')
        }

        assert error.message == 'baseUri must be an absolute URI with scheme and host'
    }

    @Test
    void queryDslUsesRfc3986StyleEncoding() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())

        HttpResult result = http.get('/hello') {
            query 'sp ace', 'a b'
            query 'plus', 'c+d'
            query 'marks', '~*'
            query 'empty', null
        }

        assert result.status == 200
        assert result.body.contains('query=sp%20ace=a%20b&plus=c%2Bd&marks=~%2A&empty=')
    }

    @Test
    void postsWithBodyAndPerRequestHeader() {
        HttpBuilder http = HttpBuilder.http {
            baseUri rootUri
        }

        HttpResult result = http.post('/echo') {
            header 'X-Trace', 'trace-42'
            text 'hello from DSL'
        }

        assert result.status == 201
        assert result.body == 'method=POST;header=trace-42;body=hello from DSL'
    }

    @Test
    void formHookEncodesBodyAndSetsDefaultContentType() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())

        HttpResult result = http.post('/form') {
            form([username: 'admin', password: 'p@ss word'])
        }

        assert result.status == 200
        assert result.body == 'method=POST;contentType=application/x-www-form-urlencoded;body=username=admin&password=p%40ss+word'
    }

    @Test
    void perRequestHeaderOverridesDefaultHeader() {
        HttpBuilder http = HttpBuilder.http {
            baseUri rootUri
            connectTimeout Duration.ofSeconds(2)
            requestTimeout Duration.ofSeconds(2)
            header 'User-Agent', 'default-ua'
        }
        HttpResult result = http.get('/hello') {
            header 'User-Agent', 'overridden-ua'
        }
        assert result.status == 200
        assert result.body.contains('ua=overridden-ua')
        assert !result.body.contains('ua=default-ua')
    }

    @Test
    void jsonHookSerializesRequestAndParsesResponse() {
        HttpBuilder http = HttpBuilder.http {
            baseUri rootUri
        }

        HttpResult result = http.post('/json') {
            json([name: 'Groovy', version: 6])
        }

        assert result.status == 200
        Map payload = (Map) result.getJson()
        assert payload.ok == true
        assert payload.contentType == 'application/json'
        assert payload.requestBody.name == 'Groovy'
        assert payload.requestBody.version == 6

        Map parsed = (Map) result.parsed
        assert parsed.ok == true
    }

    @Test
    void xmlHookParsesResponseBody() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())

        HttpResult result = http.get('/xml')

        assert result.status == 200
        def xml = result.xml
        assert xml.name.text() == 'groovy'
        assert xml.license.text() == 'Apache License 2.0'

        def parsed = result.parsed
        assert parsed.name.text() == 'groovy'
    }

    @Test
    void parsedFallsBackToRawBodyForUnsupportedContentType() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())

        HttpResult result = http.get('/plain')

        assert result.status == 200
        assert result.parsed == 'just text'
    }

    @Test
    void htmlHookParsesMalformedHtmlViaJsoup() {
        HttpBuilder http = HttpBuilder.http(rootUri.toString())

        HttpResult result = http.get('/html')

        assert result.status == 200
        assert result.html.select('span.b.lic').text() == 'Apache License 2.0'
        assert result.parsed.select('span.b.lic').text() == 'Apache License 2.0'
    }

    @Test
    void followsRedirectsWhenFlagEnabled() {
        HttpBuilder noRedirectClient = HttpBuilder.http {
            baseUri rootUri
        }
        HttpResult noRedirect = noRedirectClient.get('/redirect-me')
        assert noRedirect.status == 302

        HttpBuilder redirectClient = HttpBuilder.http {
            baseUri rootUri
            followRedirects true
        }
        HttpResult redirected = redirectClient.get('/redirect-me')
        assert redirected.status == 200
        assert redirected.body == 'redirect reached'
    }
}
