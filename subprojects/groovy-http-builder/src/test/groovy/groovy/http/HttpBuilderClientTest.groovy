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
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

class HttpBuilderClientTest {

    static HttpServer server
    static int port

    @BeforeAll
    static void setUpClass() {
        server = HttpServer.create(new InetSocketAddress('127.0.0.1', 0), 0)
        port = server.address.port

        server.createContext('/users') { HttpExchange exchange ->
            String path = exchange.requestURI.path
            String method = exchange.requestMethod

            if (method == 'GET' && path =~ '/users/\\w+') {
                String username = path.split('/')[-1]
                respond(exchange, 200, [name: username, bio: "Bio of ${username}"])
            } else if (method == 'GET') {
                def query = parseQuery(exchange.requestURI.query)
                def users = [[name: 'Alice'], [name: 'Bob']]
                if (query.name) {
                    users = users.findAll { it.name == query.name }
                }
                respond(exchange, 200, users)
            } else if (method == 'POST') {
                def body = new JsonSlurper().parseText(exchange.requestBody.text)
                body.id = 42
                respond(exchange, 201, body)
            } else {
                respond(exchange, 404, [error: 'not found'])
            }
        }

        server.createContext('/echo-headers') { HttpExchange exchange ->
            def headers = [:]
            exchange.requestHeaders.each { k, v -> headers[k] = v.join(', ') }
            respond(exchange, 200, headers)
        }

        server.createContext('/items') { HttpExchange exchange ->
            String method = exchange.requestMethod
            String path = exchange.requestURI.path
            if (method == 'PUT' && path =~ '/items/\\d+') {
                def body = new JsonSlurper().parseText(exchange.requestBody.text)
                respond(exchange, 200, body)
            } else if (method == 'DELETE' && path =~ '/items/\\d+') {
                respond(exchange, 204, null)
            } else if (method == 'PATCH' && path =~ '/items/\\d+') {
                def body = new JsonSlurper().parseText(exchange.requestBody.text)
                body.patched = true
                respond(exchange, 200, body)
            } else {
                respond(exchange, 404, [error: 'not found'])
            }
        }

        server.createContext('/form-echo') { HttpExchange exchange ->
            if (exchange.requestMethod == 'POST') {
                String body = exchange.requestBody.text
                // Parse form-encoded body
                def params = body.split('&').collectEntries { String pair ->
                    def parts = pair.split('=', 2)
                    [(URLDecoder.decode(parts[0], 'UTF-8')): parts.length > 1 ? URLDecoder.decode(parts[1], 'UTF-8') : '']
                }
                respond(exchange, 200, params)
            } else {
                respond(exchange, 405, [error: 'method not allowed'])
            }
        }

        server.createContext('/text-echo') { HttpExchange exchange ->
            if (exchange.requestMethod == 'POST') {
                String body = exchange.requestBody.text
                respond(exchange, 200, [text: body])
            } else {
                respond(exchange, 405, [error: 'method not allowed'])
            }
        }

        server.createContext('/xml-data') { HttpExchange exchange ->
            exchange.responseHeaders.set('Content-Type', 'application/xml')
            byte[] xml = '<root><name>Groovy</name><version>6</version></root>'.bytes
            exchange.sendResponseHeaders(200, xml.length)
            exchange.responseBody.write(xml)
            exchange.close()
        }

        server.createContext('/not-found') { HttpExchange exchange ->
            respond(exchange, 404, [error: 'resource not found', code: 404])
        }

        server.start()
    }

    @AfterAll
    static void tearDownClass() {
        server?.stop(0)
    }

    private static void respond(HttpExchange exchange, int status, Object body) {
        byte[] bytes = body != null ? JsonOutput.toJson(body).getBytes(StandardCharsets.UTF_8) : new byte[0]
        exchange.responseHeaders.set('Content-Type', 'application/json')
        exchange.sendResponseHeaders(status, bytes.length > 0 ? bytes.length : -1)
        if (bytes.length > 0) {
            exchange.responseBody.write(bytes)
        }
        exchange.close()
    }

    private static Map<String, String> parseQuery(String query) {
        if (!query) return [:]
        query.split('&').collectEntries { String pair ->
            def parts = pair.split('=', 2)
            [(URLDecoder.decode(parts[0], 'UTF-8')): parts.length > 1 ? URLDecoder.decode(parts[1], 'UTF-8') : '']
        }
    }

    private static void assertScript(String script) {
        new GroovyShell().evaluate(script)
    }

    @Test
    void testDeclarativeClient() {
        assertScript """
            import groovy.http.*
            import java.util.concurrent.CompletableFuture
            import java.util.concurrent.TimeUnit

            @HttpBuilderClient('http://127.0.0.1:${port}')
            @Header(name = 'Accept', value = 'application/json')
            interface TestApi {
                @Get('/users/{username}')
                Map getUser(String username)

                @Get('/users')
                List listUsers()

                @Get('/users')
                List searchUsers(@Query('name') String name)

                @Post('/users')
                Map createUser(@Body Map user)

                @Get('/users/{username}')
                CompletableFuture<Map> getUserAsync(String username)

            }

            def api = TestApi.create()

            // GET with path parameter
            def user = api.getUser('alice')
            assert user.name == 'alice'
            assert user.bio == 'Bio of alice'

            // GET list
            def users = api.listUsers()
            assert users.size() == 2

            // GET with query parameter
            def searched = api.searchUsers('Alice')
            assert searched.size() == 1
            assert searched[0].name == 'Alice'

            // POST with body
            def created = api.createUser([name: 'Charlie', email: 'charlie@example.com'])
            assert created.name == 'Charlie'
            assert created.id == 42

            // Async GET with timeout
            def future = api.getUserAsync('bob')
            assert future instanceof CompletableFuture
            def asyncUser = future.get(5, TimeUnit.SECONDS)
            assert asyncUser.name == 'bob'
        """
    }

    @Test
    void testHeadersPropagated() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            @Header(name = 'X-Custom', value = 'test-value')
            interface HeaderApi {
                @Get('/echo-headers')
                @Header(name = 'X-Method-Header', value = 'method-value')
                Map echoHeaders()
            }

            def api = HeaderApi.create()
            def headers = api.echoHeaders()
            def lc = headers.collectKeys(String::toLowerCase)
            assert lc['x-custom'] == 'test-value'
            assert lc['x-method-header'] == 'method-value'
        """
    }

    @Test
    void testBaseUrlOverride() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://wrong-host:9999')
            interface OverrideApi {
                @Get('/users/{username}')
                Map getUser(String username)
            }

            def api = OverrideApi.create('http://127.0.0.1:${port}')
            def user = api.getUser('alice')
            assert user.name == 'alice'
        """
    }

    @Test
    void testPutAndDelete() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface ItemApi {
                @Put('/items/{id}')
                Map updateItem(String id, @Body Map item)

                @Delete('/items/{id}')
                void deleteItem(String id)
            }

            def api = ItemApi.create()
            def updated = api.updateItem('123', [name: 'Widget'])
            assert updated.name == 'Widget'

            // void delete should not throw
            api.deleteItem('123')
        """
    }

    @Test
    void testImpliedQueryParams() {
        // No @Query annotation needed — non-path, non-body params are implied query params
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface ImpliedApi {
                @Get('/users')
                List searchUsers(String name)
            }

            def api = ImpliedApi.create()
            def users = api.searchUsers('Alice')
            assert users.size() == 1
            assert users[0].name == 'Alice'
        """
    }

    @Test
    void testPatch() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface PatchApi {
                @Patch('/items/{id}')
                Map patchItem(String id, @Body Map updates)
            }

            def api = PatchApi.create()
            def result = api.patchItem('42', [name: 'Updated'])
            assert result.name == 'Updated'
            assert result.patched == true
        """
    }

    @Test
    void testPatchOnBuilder() {
        def http = HttpBuilder.http("http://127.0.0.1:${port}")
        def result = http.patch('/items/42') {
            json([name: 'Patched'])
        }
        assert result.json.patched == true
    }

    @Test
    void testTimeoutAndRedirectConfig() {
        // Verify that the timeout and redirect attributes compile and don't break anything
        assertScript """
            import groovy.http.*

            @HttpBuilderClient(value = 'http://127.0.0.1:${port}',
                               connectTimeout = 5,
                               requestTimeout = 10,
                               followRedirects = true)
            interface ConfiguredApi {
                @Get('/users/{username}')
                Map getUser(String username)
            }

            def api = ConfiguredApi.create()
            def user = api.getUser('alice')
            assert user.name == 'alice'
        """
    }

    @Test
    void testFormPost() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface FormApi {
                @Post('/form-echo')
                @Form
                Map login(String username, String password)
            }

            def api = FormApi.create()
            def result = api.login('alice', 's3cret')
            assert result.username == 'alice'
            assert result.password == 's3cret'
        """
    }

    @Test
    void testBodyText() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface TextApi {
                @Post('/text-echo')
                Map sendText(@BodyText String content)
            }

            def api = TextApi.create()
            def result = api.sendText('Hello, World!')
            assert result.text.contains('Hello')
        """
    }

    @Test
    void testXmlResponse() {
        assertScript """
            import groovy.http.*
            import groovy.xml.slurpersupport.GPathResult

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface XmlApi {
                @Get('/xml-data')
                GPathResult getData()
            }

            def api = XmlApi.create()
            def xml = api.getData()
            assert xml instanceof GPathResult
            assert xml.name.text() == 'Groovy'
            assert xml.version.text() == '6'
        """
    }

    @Test
    void testTypedResponse() {
        assertScript """
            import groovy.http.*

            class UserInfo {
                String name
                String bio
            }

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface TypedApi {
                @Get('/users/{username}')
                UserInfo getUser(String username)
            }

            def api = TypedApi.create()
            def user = api.getUser('alice')
            assert user instanceof UserInfo
            assert user.name == 'alice'
            assert user.bio == 'Bio of alice'
        """
    }

    @Test
    void testErrorMappingViaThrows() {
        // Error mapping uses the throws clause to determine exception type.
        // The exception class must be visible to the helper's classloader,
        // so we test with a RuntimeException subclass (always loadable).
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface ErrorApi {
                @Get('/not-found')
                Map getData() throws IllegalStateException
            }

            def api = ErrorApi.create()
            try {
                api.getData()
                assert false, 'should have thrown'
            } catch (IllegalStateException e) {
                assert e.message.contains('404')
            }
        """
    }

    @Test
    void testErrorDefaultsToRuntimeException() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface DefaultErrorApi {
                @Get('/not-found')
                Map getData()
            }

            def api = DefaultErrorApi.create()
            try {
                api.getData()
                assert false, 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message.contains('404')
            }
        """
    }

    @Test
    void testImperativeAsync() {
        def http = HttpBuilder.http("http://127.0.0.1:${port}")
        def future = http.getAsync('/users/alice')
        assert future instanceof java.util.concurrent.CompletableFuture
        def result = future.get(5, java.util.concurrent.TimeUnit.SECONDS)
        assert result.json.name == 'alice'
    }

    @Test
    void testClientConfig() {
        // Imperative: clientConfig gives access to HttpClient.Builder
        def http = HttpBuilder.http {
            baseUri "http://127.0.0.1:${port}"
            clientConfig { builder ->
                // Can set authenticator, SSL, proxy, etc.
                builder.followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            }
        }
        def result = http.get('/users/alice')
        assert result.json.name == 'alice'
    }

    @Test
    void testDeclarativeCreateWithClosure() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://wrong-host:9999')
            interface ConfigApi {
                @Get('/users/{username}')
                Map getUser(String username)
            }

            // create(Closure) overrides everything — base URL, headers, etc.
            def api = ConfigApi.create {
                baseUri 'http://127.0.0.1:${port}'
                header 'X-Custom', 'from-closure'
            }
            def user = api.getUser('alice')
            assert user.name == 'alice'
        """
    }

    @Test
    void testDeclarativeCreateWithAuth() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient('http://127.0.0.1:${port}')
            interface AuthApi {
                @Get('/echo-headers')
                Map echoHeaders()
            }

            def api = AuthApi.create {
                baseUri 'http://127.0.0.1:${port}'
                header 'Authorization', 'Bearer my-secret-token'
            }
            def headers = api.echoHeaders()
            def lc = headers.collectKeys(String::toLowerCase)
            assert lc['authorization'] == 'Bearer my-secret-token'
        """
    }

    @Test
    void testPerMethodTimeout() {
        assertScript """
            import groovy.http.*

            @HttpBuilderClient(value = 'http://127.0.0.1:${port}',
                               requestTimeout = 5)
            interface TimeoutApi {
                @Get('/users/{username}')
                Map getUser(String username)              // uses default 5s

                @Get('/users/{username}')
                @Timeout(30)
                Map getUserSlow(String username)           // overrides to 30s
            }

            def api = TimeoutApi.create()
            def user = api.getUser('alice')
            assert user.name == 'alice'

            def slow = api.getUserSlow('bob')
            assert slow.name == 'bob'
        """
    }
}
