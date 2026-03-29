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
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

class HttpBuilderSpecTest {

    private HttpServer server
    private URI rootUri

    @BeforeEach
    void setup() {
        server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        server.createContext('/api/items') { exchange ->
            String query = exchange.requestURI.query ?: ''
            String method = exchange.requestMethod
            String contentType = exchange.requestHeaders.getFirst('Content-Type') ?: ''
            String requestBody = exchange.requestBody.getText(StandardCharsets.UTF_8.name())
            String ua = exchange.requestHeaders.getFirst('User-Agent') ?: ''
            String body
            if (method == 'GET') {
                body = """{"items":[{"name":"book","qty":2}],"query":"${query}","ua":"${ua}"}"""
            } else if (method == 'POST' && contentType.contains('application/json')) {
                body = """{"ok":true,"received":${requestBody}}"""
            } else {
                body = """{"ok":true}"""
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Type', 'application/json')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/api/repo.xml') { exchange ->
            String body = '<repo><name>groovy</name><license>Apache License 2.0</license></repo>'
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Type', 'application/xml')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.createContext('/login') { exchange ->
            String method = exchange.requestMethod
            if (method == 'GET') {
                String body = '<!DOCTYPE html><html><body><h1>Please Login</h1></body></html>'
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
                exchange.responseHeaders.add('Content-Type', 'text/html; charset=UTF-8')
                exchange.sendResponseHeaders(200, bytes.length)
                exchange.responseBody.withCloseable { it.write(bytes) }
            } else {
                String requestBody = exchange.requestBody.getText(StandardCharsets.UTF_8.name())
                String body
                if (requestBody.contains('username=admin')) {
                    body = '<!DOCTYPE html><html><body><h1>Admin Section</h1></body></html>'
                } else {
                    body = '<!DOCTYPE html><html><body><h1>Login Failed</h1></body></html>'
                }
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
                exchange.responseHeaders.add('Content-Type', 'text/html; charset=UTF-8')
                exchange.sendResponseHeaders(200, bytes.length)
                exchange.responseBody.withCloseable { it.write(bytes) }
            }
        }
        server.createContext('/artifact/org.apache.groovy/groovy-all') { exchange ->
            String body = '''<!DOCTYPE html>
<html>
<head><title>Maven Repository: org.apache.groovy : groovy-all</title></head>
<body>
<h2 class="im-title"><a href="/artifact/org.apache.groovy/groovy-all">groovy-all</a></h2>
<span class="badge badge-license">Apache 2.0</span>
</body>
</html>'''
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8)
            exchange.responseHeaders.add('Content-Type', 'text/html; charset=UTF-8')
            exchange.sendResponseHeaders(200, bytes.length)
            exchange.responseBody.withCloseable { it.write(bytes) }
        }
        server.start()
        rootUri = URI.create("http://127.0.0.1:${server.address.port}")
    }

    @AfterEach
    void cleanup() {
        server?.stop(0)
    }

    @Test
    void testBasicGetWithQuery() {
        assertScript """
        // tag::basic_get_with_query[]
        import groovy.http.HttpBuilder

        def http = HttpBuilder.http {
            baseUri '${rootUri}/'
            header 'User-Agent', 'my-app/1.0'
        }

        def res = http.get('/api/items') {
            query page: 1, size: 10
        }

        assert res.status == 200
        // end::basic_get_with_query[]
        assert res.json.ua == 'my-app/1.0'
        """
    }

    @Test
    void testJsonGet() {
        assertScript """
        // tag::json_get[]
        import static groovy.http.HttpBuilder.http

        def client = http '${rootUri}'
        def res = client.get('/api/items')

        assert res.status == 200
        assert res.json.items[0].name == 'book'
        assert res.parsed.items[0].name == 'book' // auto-parsed from Content-Type
        // end::json_get[]
        """
    }

    @Test
    void testJsonPost() {
        assertScript """
        import static groovy.http.HttpBuilder.http

        def http = http '${rootUri}'
        // tag::json_post[]
        def result = http.post('/api/items') {
            json([name: 'book', qty: 2])
        }

        assert result.status == 200
        assert result.json.ok
        // end::json_post[]
        """
    }

    @Test
    void testXmlGet() {
        assertScript """
        import static groovy.http.HttpBuilder.http

        def http = http '${rootUri}'
        // tag::xml_get[]
        def result = http.get('/api/repo.xml')

        assert result.status == 200
        assert result.xml.license.text() == 'Apache License 2.0'
        assert result.parsed.license.text() == 'Apache License 2.0' // auto-parsed from Content-Type
        // end::xml_get[]
        """
    }

    @Test
    void testHtmlLogin() {
        assertScript """
        import static groovy.http.HttpBuilder.http

        // tag::html_login[]
        def app = http {
            baseUri '${rootUri}'
            followRedirects true
            header 'User-Agent', 'Mozilla/5.0 (Macintosh)'
        }

        def loginPage = app.get('/login')
        assert loginPage.status == 200
        assert loginPage.html.select('h1').text() == 'Please Login'

        def afterLogin = app.post('/login') {
            form(username: 'admin', password: 'p@ssw0rd')
        }

        assert afterLogin.status == 200
        assert afterLogin.html.select('h1').text() == 'Admin Section'
        // end::html_login[]
        """
    }

    @Test
    void testHtmlJsoup() {
        def mvnrepositoryUri = rootUri
        assertScript """
        import static groovy.http.HttpBuilder.http

        // tag::html_jsoup[]
        // @Grab('org.jsoup:jsoup:1.22.1') // needed if running as standalone script
        def client = http('${mvnrepositoryUri}')
        def res = client.get('/artifact/org.apache.groovy/groovy-all') {
            header 'User-Agent', 'Mozilla/5.0 (Macintosh)'
        }

        assert res.status == 200

        def license = res.parsed.select('span.badge.badge-license')*.text().join(', ')
        assert license == 'Apache 2.0'
        // end::html_jsoup[]
        """
    }

    @Test
    void testFormPost() {
        assertScript """
        import static groovy.http.HttpBuilder.http

        def http = http '${rootUri}'
        // tag::form_post[]
        def result = http.post('/login') {
            form(username: 'admin', password: 'p@ssw0rd')
        }

        assert result.status == 200
        // end::form_post[]
        """
    }

    private static void assertScript(String script) {
        new GroovyShell(HttpBuilderSpecTest.classLoader).evaluate(script)
    }
}







