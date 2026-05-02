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

import groovy.lang.DelegatesTo
import groovy.lang.Closure
import groovy.json.JsonOutput
import org.apache.groovy.lang.annotation.Incubating

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * Tiny DSL over JDK {@link HttpClient}.
 *
 * @since 6.0.0
 */
@Incubating
final class HttpBuilder {
    private final HttpClient client
    private final URI baseUri
    private final Map<String, String> defaultHeaders
    private final Duration defaultRequestTimeout

    private HttpBuilder(final Config config) {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
        if (config.connectTimeout != null) {
            clientBuilder.connectTimeout(config.connectTimeout)
        }
        if (config.followRedirects) {
            clientBuilder.followRedirects(HttpClient.Redirect.NORMAL)
        }
        if (config.clientConfigurer != null) {
            Closure<?> code = (Closure<?>) config.clientConfigurer.clone()
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code.delegate = clientBuilder
            code.call(clientBuilder)
        }
        client = clientBuilder.build()
        baseUri = config.baseUri
        defaultHeaders = Collections.unmodifiableMap(new LinkedHashMap<>(config.headers))
        defaultRequestTimeout = config.requestTimeout
    }

    /**
     * Creates an {@code HttpBuilder} from the supplied configuration closure.
     *
     * @param spec configures the builder defaults
     * @return a configured HTTP builder
     */
    static HttpBuilder http(
            @DelegatesTo(value = Config, strategy = Closure.DELEGATE_FIRST)
            final Closure<?> spec
    ) {
        Config config = new Config()
        Closure<?> code = (Closure<?>) spec.clone()
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.delegate = config
        code.call()
        return new HttpBuilder(config)
    }

    /**
     * Creates an {@code HttpBuilder} with only a base URI configured.
     *
     * @param baseUri the absolute base URI used to resolve relative requests
     * @return a configured HTTP builder
     */
    static HttpBuilder http(final String baseUri) {
        Config config = new Config()
        config.baseUri(baseUri)
        return new HttpBuilder(config)
    }

    /**
     * Executes a synchronous {@code GET} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return the buffered response wrapper
     */
    HttpResult get(final Object uri = null,
                   @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                   final Closure<?> spec = null) {
        return request('GET', uri, spec)
    }

    /**
     * Executes a synchronous {@code POST} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return the buffered response wrapper
     */
    HttpResult post(final Object uri = null,
                    @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                    final Closure<?> spec = null) {
        return request('POST', uri, spec)
    }

    /**
     * Executes a synchronous {@code PUT} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return the buffered response wrapper
     */
    HttpResult put(final Object uri = null,
                   @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                   final Closure<?> spec = null) {
        return request('PUT', uri, spec)
    }

    /**
     * Executes a synchronous {@code DELETE} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return the buffered response wrapper
     */
    HttpResult delete(final Object uri = null,
                      @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                      final Closure<?> spec = null) {
        return request('DELETE', uri, spec)
    }

    /**
     * Executes a synchronous {@code PATCH} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return the buffered response wrapper
     */
    HttpResult patch(final Object uri = null,
                     @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                     final Closure<?> spec = null) {
        return request('PATCH', uri, spec)
    }

    /**
     * Executes a synchronous request with the supplied HTTP method.
     *
     * @param method the HTTP method to use
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return the buffered response wrapper
     */
    HttpResult request(final String method,
                       final Object uri,
                       @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                       final Closure<?> spec = null) {
        def (HttpRequest httpRequest, HttpResponse.BodyHandler<String> bodyHandler) = buildRequest(method, uri, spec)
        HttpResponse<String> response
        try {
            response = client.send(httpRequest, bodyHandler)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt()
            throw new RuntimeException("HTTP request " + method + " " + httpRequest.uri() + " was interrupted", e)
        } catch (IOException e) {
            throw new RuntimeException("I/O error during HTTP request " + method + " " + httpRequest.uri(), e)
        }
        return new HttpResult(response)
    }

    /**
     * Executes an asynchronous request with the supplied HTTP method.
     *
     * @param method the HTTP method to use
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the buffered response
     */
    CompletableFuture<HttpResult> requestAsync(final String method,
                                                final Object uri,
                                                @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                                final Closure<?> spec = null) {
        def (HttpRequest httpRequest, HttpResponse.BodyHandler<String> bodyHandler) = buildRequest(method, uri, spec)
        return client.sendAsync(httpRequest, bodyHandler)
                .thenApply { HttpResponse<String> response -> new HttpResult(response) }
    }

    /**
     * Executes an asynchronous {@code GET} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the buffered response
     */
    CompletableFuture<HttpResult> getAsync(final Object uri = null,
                                            @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                            final Closure<?> spec = null) {
        return requestAsync('GET', uri, spec)
    }

    /**
     * Executes an asynchronous {@code POST} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the buffered response
     */
    CompletableFuture<HttpResult> postAsync(final Object uri = null,
                                             @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                             final Closure<?> spec = null) {
        return requestAsync('POST', uri, spec)
    }

    /**
     * Executes an asynchronous {@code PUT} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the buffered response
     */
    CompletableFuture<HttpResult> putAsync(final Object uri = null,
                                            @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                            final Closure<?> spec = null) {
        return requestAsync('PUT', uri, spec)
    }

    /**
     * Executes an asynchronous {@code DELETE} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the buffered response
     */
    CompletableFuture<HttpResult> deleteAsync(final Object uri = null,
                                               @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                               final Closure<?> spec = null) {
        return requestAsync('DELETE', uri, spec)
    }

    /**
     * Executes an asynchronous {@code PATCH} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the buffered response
     */
    CompletableFuture<HttpResult> patchAsync(final Object uri = null,
                                              @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                              final Closure<?> spec = null) {
        return requestAsync('PATCH', uri, spec)
    }

    /**
     * Streams the response body without buffering. Returns an
     * {@link HttpStreamResult} whose {@code bodyAsPublisher()} delivers
     * {@code List<ByteBuffer>} chunks as they arrive — enabling
     * {@code for await (chunk in result.bodyAsPublisher())} via the
     * {@code FlowPublisherAdapter}.
     * <p>
     * Streaming always uses {@code HttpResponse.BodyHandlers.ofPublisher()};
     * any {@code bodyHandler} configured on the {@code RequestSpec} is ignored.
     *
     * @param method the HTTP method to use
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the streaming response wrapper
     */
    CompletableFuture<HttpStreamResult> streamAsync(final String method,
                                                    final Object uri,
                                                    @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                                    final Closure<?> spec = null) {
        HttpRequest httpRequest = buildStreamRequest(method, uri, spec)
        return client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofPublisher())
                .thenApply { HttpResponse response -> new HttpStreamResult(response) }
    }

    /**
     * Executes an asynchronous streaming {@code GET} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the streaming response wrapper
     */
    CompletableFuture<HttpStreamResult> getStreamAsync(final Object uri = null,
                                                       @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                                       final Closure<?> spec = null) {
        return streamAsync('GET', uri, spec)
    }

    /**
     * Executes an asynchronous streaming {@code POST} request.
     *
     * @param uri the absolute or base-relative request URI
     * @param spec optionally customizes the request
     * @return a future that completes with the streaming response wrapper
     */
    CompletableFuture<HttpStreamResult> postStreamAsync(final Object uri = null,
                                                        @DelegatesTo(value = RequestSpec, strategy = Closure.DELEGATE_FIRST)
                                                        final Closure<?> spec = null) {
        return streamAsync('POST', uri, spec)
    }

    private HttpRequest buildStreamRequest(final String method, final Object uri, final Closure<?> spec) {
        RequestSpec requestSpec = new RequestSpec()
        if (spec != null) {
            Closure<?> code = (Closure<?>) spec.clone()
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code.delegate = requestSpec
            code.call()
        }

        URI resolvedUri = resolveUri(uri, requestSpec.queryParameters)
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(resolvedUri)

        Duration timeout = requestSpec.timeout ?: defaultRequestTimeout
        if (timeout != null) {
            requestBuilder.timeout(timeout)
        }

        defaultHeaders.each { String name, String value ->
            requestBuilder.header(name, value)
        }
        requestSpec.headers.each { String name, String value ->
            requestBuilder.setHeader(name, value)
        }

        requestBuilder.method(method, bodyPublisher(method, requestSpec.body))
        return requestBuilder.build()
    }

    private List buildRequest(final String method, final Object uri, final Closure<?> spec) {
        RequestSpec requestSpec = new RequestSpec()
        if (spec != null) {
            Closure<?> code = (Closure<?>) spec.clone()
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code.delegate = requestSpec
            code.call()
        }

        URI resolvedUri = resolveUri(uri, requestSpec.queryParameters)
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(resolvedUri)

        Duration timeout = requestSpec.timeout ?: defaultRequestTimeout
        if (timeout != null) {
            requestBuilder.timeout(timeout)
        }

        defaultHeaders.each { String name, String value ->
            requestBuilder.header(name, value)
        }
        requestSpec.headers.each { String name, String value ->
            requestBuilder.setHeader(name, value)
        }

        requestBuilder.method(method, bodyPublisher(method, requestSpec.body))

        return [requestBuilder.build(), requestSpec.bodyHandler]
    }

    private URI resolveUri(final Object uri, final Map<String, Object> query) {
        URI target = toUri(uri)
        if (!target.isAbsolute()) {
            if (baseUri == null) {
                throw new IllegalArgumentException('Request URI must be absolute when no baseUri is configured')
            }
            target = baseUri.resolve(target.toString())
        }
        return appendQuery(target, query)
    }

    private static URI requireAbsoluteUriWithHost(final URI uri, final String name) {
        if (uri == null || !uri.isAbsolute() || uri.host == null) {
            throw new IllegalArgumentException(name + ' must be an absolute URI with scheme and host')
        }
        return uri
    }

    private URI toUri(final Object value) {
        if (value == null) {
            if (baseUri == null) {
                throw new IllegalArgumentException('URI must be provided when no baseUri is configured')
            }
            return baseUri
        }
        if (value instanceof URI) {
            return (URI) value
        }
        return URI.create(value.toString())
    }

    private static URI appendQuery(final URI uri, final Map<String, Object> queryValues) {
        if (queryValues.isEmpty()) {
            return uri
        }

        List<String> pairs = new ArrayList<>()
        if (uri.query != null && !uri.query.isEmpty()) {
            pairs.add(uri.query)
        }

        queryValues.each { String key, Object value ->
            String encodedKey = encodeQueryComponent(key)
            String encodedValue = value == null ? '' : encodeQueryComponent(value.toString())
            pairs.add(encodedKey + '=' + encodedValue)
        }

        String query = pairs.join('&')
        return new URI(uri.scheme, uri.authority, uri.path, query, uri.fragment)
    }

    private static String encodeQueryComponent(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace('+', '%20')
                .replace('*', '%2A')
                .replace('%7E', '~')
    }

    private static HttpRequest.BodyPublisher bodyPublisher(final String method, final Object body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody()
        }
        if ('GET'.equalsIgnoreCase(method)) {
            throw new IllegalArgumentException('GET requests do not support a body in this DSL')
        }
        if (body instanceof byte[]) {
            return HttpRequest.BodyPublishers.ofByteArray((byte[]) body)
        }
        return HttpRequest.BodyPublishers.ofString(body.toString())
    }

    /**
     * Configuration DSL used to create an {@link HttpBuilder}.
     *
     * @since 6.0.0
     */
    static final class Config {
        /**
         * Absolute base URI used to resolve relative request URIs.
         */
        URI baseUri
        /**
         * Timeout applied when opening connections.
         */
        Duration connectTimeout
        /**
         * Timeout applied to each request when not overridden.
         */
        Duration requestTimeout
        /**
         * Whether redirects should be followed automatically.
         */
        boolean followRedirects
        /**
         * Headers added to every request created by the builder.
         */
        final Map<String, String> headers = [:]
        /**
         * Optional hook for advanced {@link HttpClient.Builder} customization.
         */
        Closure<?> clientConfigurer

        /**
         * Sets the absolute base URI used for relative requests.
         *
         * @param value the base URI as a {@link URI} or coercible string
         */
        void baseUri(final Object value) {
            URI candidate = value instanceof URI ? (URI) value : URI.create(value.toString())
            baseUri = requireAbsoluteUriWithHost(candidate, 'baseUri')
        }

        /**
         * Sets the client connect timeout.
         *
         * @param value the connection timeout
         */
        void connectTimeout(final Duration value) {
            connectTimeout = value
        }

        /**
         * Sets the default request timeout.
         *
         * @param value the request timeout
         */
        void requestTimeout(final Duration value) {
            requestTimeout = value
        }

        /**
         * Enables or disables automatic redirect following.
         *
         * @param value {@code true} to follow redirects
         */
        void followRedirects(final boolean value) {
            followRedirects = value
        }

        /**
         * Adds a default header to every request.
         *
         * @param name the header name
         * @param value the header value
         */
        void header(final String name, final Object value) {
            headers.put(name, String.valueOf(value))
        }

        /**
         * Adds multiple default headers to every request.
         *
         * @param values the headers to add
         */
        void headers(final Map<String, ?> values) {
            values.each { String name, Object value -> header(name, value) }
        }

        /**
         * Provides direct access to the underlying {@code HttpClient.Builder}
         * for advanced configuration (authenticator, SSL context, proxy, cookie handler, etc.).
         *
         * @param configurer a closure taking an {@code HttpClient.Builder}
         */
        void clientConfig(
                @DelegatesTo(value = HttpClient.Builder, strategy = Closure.DELEGATE_FIRST)
                final Closure<?> configurer) {
            this.clientConfigurer = configurer
        }
    }

    /**
     * Per-request configuration DSL used by the request methods.
     *
     * @since 6.0.0
     */
    static final class RequestSpec {
        /**
         * Request-specific timeout overriding the builder default.
         */
        Duration timeout
        /**
         * Request body value after any DSL serialization step.
         */
        Object body
        /**
         * Body handler used for buffered requests.
         */
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString()
        /**
         * Headers added only to the current request.
         */
        final Map<String, String> headers = new LinkedHashMap<>()
        /**
         * Query parameters appended to the current request URI.
         */
        final Map<String, Object> queryParameters = new LinkedHashMap<>()

        /**
         * Sets the timeout for the current request.
         *
         * @param value the request timeout
         */
        void timeout(final Duration value) {
            timeout = value
        }

        /**
         * Adds a header to the current request.
         *
         * @param name the header name
         * @param value the header value
         */
        void header(final String name, final Object value) {
            headers.put(name, String.valueOf(value))
        }

        /**
         * Adds multiple headers to the current request.
         *
         * @param values the headers to add
         */
        void headers(final Map<String, ?> values) {
            values.each { String name, Object value -> header(name, value) }
        }

        /**
         * Adds a query parameter to the current request.
         *
         * @param name the query parameter name
         * @param value the query parameter value
         */
        void query(final String name, final Object value) {
            queryParameters.put(name, value)
        }

        /**
         * Adds multiple query parameters to the current request.
         *
         * @param values the query parameters to add
         */
        void query(final Map<String, ?> values) {
            values.each { String name, Object value -> query(name, value) }
        }

        /**
         * Sets the request body to the string form of the supplied value.
         *
         * @param value the text body value
         */
        void text(final Object value) {
            body = value == null ? null : value.toString()
        }

        /**
         * Sets the request body to raw bytes.
         *
         * @param value the binary payload
         */
        void bytes(final byte[] value) {
            body = value
        }

        /**
         * Sets the request body without further serialization.
         *
         * @param value the body value
         */
        void body(final Object value) {
            body = value
        }

        /**
         * Encodes map entries as application/x-www-form-urlencoded and sets a default content type.
         *
         * @param values the form fields to encode
         */
        void form(final Map<String, ?> values) {
            if (!headers.find{ it.key.equalsIgnoreCase('Content-Type') }) {
                header('Content-Type', 'application/x-www-form-urlencoded')
            }
            body = values.collect { String name, Object value ->
                String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8)
                String encodedValue = value == null ? '' : URLEncoder.encode(value.toString(), StandardCharsets.UTF_8)
                encodedName + '=' + encodedValue
            }.join('&')
        }

        /**
         * Serializes the given value as JSON and sets a default content type.
         *
         * @param value the value to serialize
         */
        void json(final Object value) {
            if (!headers.find{ it.key.equalsIgnoreCase('Content-Type') }) {
                header('Content-Type', 'application/json')
            }
            body = JsonOutput.toJson(value)
        }

        /**
         * Uses the default string body handler for buffered requests.
         */
        void asString() {
            bodyHandler = HttpResponse.BodyHandlers.ofString()
        }
    }
}
