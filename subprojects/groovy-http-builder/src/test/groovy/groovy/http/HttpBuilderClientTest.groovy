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
            } else {
                respond(exchange, 404, [error: 'not found'])
            }
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
}
