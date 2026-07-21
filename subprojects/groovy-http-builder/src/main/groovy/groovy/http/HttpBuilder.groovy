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
    private final boolean confineToBaseUri
    private final boolean followRedirectsManually

    /** Maximum number of redirects followed when confinement handles them itself. */
    private static final int MAX_REDIRECTS = 5

    private HttpBuilder(final Config config) {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
        if (config.connectTimeout != null) {
            clientBuilder.connectTimeout(config.connectTimeout)
        }
        // When confinement is active we must see every 3xx ourselves so the base-URI
        // gate can be applied to each hop; the JDK client would otherwise follow
        // redirects internally, escaping confinement. Only the unconfined case
        // delegates redirect-following to the JDK.
        followRedirectsManually = config.confineToBaseUri && config.followRedirects
        if (config.followRedirects && !followRedirectsManually) {
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
        confineToBaseUri = config.confineToBaseUri
        if (confineToBaseUri && baseUri == null) {
            throw new IllegalArgumentException('confineToBaseUri requires a baseUri to be configured')
        }
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
        RequestSpec requestSpec = evalSpec(spec)
        URI resolvedUri = resolveUri(uri, requestSpec.queryParameters)
        Map<String, String> headers = combinedHeaders(requestSpec)
        HttpRequest httpRequest = buildHopRequest(method, resolvedUri, headers, requestSpec.body, requestSpec.timeout)
        HttpResponse<String> response = send(method, httpRequest, requestSpec.bodyHandler)
        if (followRedirectsManually) {
            response = followRedirects(method, httpRequest.uri(), response, headers,
                    requestSpec.body, requestSpec.timeout, requestSpec.bodyHandler)
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
        RequestSpec requestSpec = evalSpec(spec)
        URI resolvedUri = resolveUri(uri, requestSpec.queryParameters)
        Map<String, String> headers = combinedHeaders(requestSpec)
        HttpRequest httpRequest = buildHopRequest(method, resolvedUri, headers, requestSpec.body, requestSpec.timeout)
        CompletableFuture<HttpResponse<String>> future = client.sendAsync(httpRequest, requestSpec.bodyHandler)
        if (followRedirectsManually) {
            future = future.thenCompose { HttpResponse<String> response ->
                followRedirectsAsync(method, httpRequest.uri(), response, headers,
                        requestSpec.body, requestSpec.timeout, requestSpec.bodyHandler, 0)
            }
        }
        return future.thenApply { HttpResponse<String> response -> new HttpResult(response) }
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
        // Note: streaming does not auto-follow redirects under confinement. Because
        // the body is an unbuffered publisher, a 3xx is returned to the caller as-is
        // rather than followed. This is safe (no bypass) but not transparent; callers
        // who need confined streaming redirects should resolve the Location themselves.
        RequestSpec requestSpec = evalSpec(spec)
        URI resolvedUri = resolveUri(uri, requestSpec.queryParameters)
        return buildHopRequest(method, resolvedUri, combinedHeaders(requestSpec), requestSpec.body, requestSpec.timeout)
    }

    private RequestSpec evalSpec(final Closure<?> spec) {
        RequestSpec requestSpec = new RequestSpec()
        if (spec != null) {
            Closure<?> code = (Closure<?>) spec.clone()
            code.resolveStrategy = Closure.DELEGATE_FIRST
            code.delegate = requestSpec
            code.call()
        }
        return requestSpec
    }

    private Map<String, String> combinedHeaders(final RequestSpec requestSpec) {
        Map<String, String> headers = new LinkedHashMap<>(defaultHeaders)
        headers.putAll(requestSpec.headers)
        return headers
    }

    private HttpRequest buildHopRequest(final String method, final URI uri,
                                        final Map<String, String> headers,
                                        final Object body, final Duration requestTimeout) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
        Duration timeout = requestTimeout ?: defaultRequestTimeout
        if (timeout != null) {
            requestBuilder.timeout(timeout)
        }
        headers.each { String name, String value ->
            requestBuilder.setHeader(name, value)
        }
        requestBuilder.method(method, bodyPublisher(method, body))
        return requestBuilder.build()
    }

    private <T> HttpResponse<T> send(final String method, final HttpRequest httpRequest,
                                     final HttpResponse.BodyHandler<T> bodyHandler) {
        try {
            return client.send(httpRequest, bodyHandler)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt()
            throw new RuntimeException("HTTP request " + method + " " + httpRequest.uri() + " was interrupted", e)
        } catch (IOException e) {
            throw new RuntimeException("I/O error during HTTP request " + method + " " + httpRequest.uri(), e)
        }
    }

    /**
     * Synchronously follows redirects while confinement is active, applying
     * {@link #enforceConfinement} to every hop. Because a confined hop must share
     * the base URI's origin, a redirect to another host is rejected outright — so
     * sensitive headers can never leak across origins here.
     */
    private HttpResponse<String> followRedirects(String method, URI currentUri, HttpResponse<String> response,
                                                 Map<String, String> headers, Object body,
                                                 Duration timeout, HttpResponse.BodyHandler<String> bodyHandler) {
        int redirects = 0
        while (true) {
            URI target = redirectTarget(currentUri, response)
            if (target == null) {
                return response
            }
            if (++redirects > MAX_REDIRECTS) {
                throw new RuntimeException("Too many redirects (> " + MAX_REDIRECTS + ") for request confined to " + baseUri)
            }
            String nextMethod = redirectMethod(method, response.statusCode())
            boolean sameMethod = nextMethod == method
            Object nextBody = sameMethod ? body : null
            Map<String, String> nextHeaders = sameMethod ? headers : withoutBodyHeaders(headers)
            HttpRequest httpRequest = buildHopRequest(nextMethod, target, nextHeaders, nextBody, timeout)
            response = send(nextMethod, httpRequest, bodyHandler)
            currentUri = target
            method = nextMethod
            body = nextBody
            headers = nextHeaders
        }
    }

    /**
     * Asynchronous counterpart of {@link #followRedirects}. A confinement
     * violation on a hop surfaces as a failed future carrying the
     * {@link SecurityException} thrown by {@link #enforceConfinement}.
     */
    private CompletableFuture<HttpResponse<String>> followRedirectsAsync(
            String method, URI currentUri, HttpResponse<String> response,
            Map<String, String> headers, Object body, Duration timeout,
            HttpResponse.BodyHandler<String> bodyHandler, int redirects) {
        URI target = redirectTarget(currentUri, response)
        if (target == null) {
            return CompletableFuture.completedFuture(response)
        }
        if (redirects + 1 > MAX_REDIRECTS) {
            CompletableFuture<HttpResponse<String>> failed = new CompletableFuture<>()
            failed.completeExceptionally(
                    new RuntimeException("Too many redirects (> " + MAX_REDIRECTS + ") for request confined to " + baseUri))
            return failed
        }
        String nextMethod = redirectMethod(method, response.statusCode())
        boolean sameMethod = nextMethod == method
        Object nextBody = sameMethod ? body : null
        Map<String, String> nextHeaders = sameMethod ? headers : withoutBodyHeaders(headers)
        HttpRequest httpRequest = buildHopRequest(nextMethod, target, nextHeaders, nextBody, timeout)
        return client.sendAsync(httpRequest, bodyHandler).thenCompose { HttpResponse<String> next ->
            followRedirectsAsync(nextMethod, target, next, nextHeaders, nextBody, timeout, bodyHandler, redirects + 1)
        }
    }

    /**
     * Returns the confinement-checked redirect target for a response, or
     * {@code null} if the response is not a followable redirect (non-3xx, or a
     * 3xx with no {@code Location}). Throws {@link SecurityException} via
     * {@link #enforceConfinement} if the target escapes the base URI.
     */
    private URI redirectTarget(final URI currentUri, final HttpResponse<?> response) {
        int status = response.statusCode()
        if (status != 301 && status != 302 && status != 303 && status != 307 && status != 308) {
            return null
        }
        String location = response.headers().firstValue('Location').orElse(null)
        if (location == null || location.isEmpty()) {
            return null
        }
        URI target = currentUri.resolve(location).normalize()
        enforceConfinement(target)
        return target
    }

    /**
     * Mirrors the JDK {@link HttpClient.Redirect#NORMAL} method-rewriting rules
     * so that enabling confinement does not change redirect semantics: 301/302
     * downgrade only {@code POST} to {@code GET} (other methods are preserved),
     * 303 downgrades everything except {@code HEAD} to {@code GET}, and 307/308
     * preserve the original method and body.
     */
    private static String redirectMethod(final String method, final int statusCode) {
        switch (statusCode) {
            case 301:
            case 302:
                return 'POST'.equalsIgnoreCase(method) ? 'GET' : method
            case 303:
                return 'HEAD'.equalsIgnoreCase(method) ? method : 'GET'
            default:
                return method // 307/308 preserve method and body
        }
    }

    private static Map<String, String> withoutBodyHeaders(final Map<String, String> headers) {
        Map<String, String> copy = new LinkedHashMap<>()
        headers.each { String name, String value ->
            if (!'Content-Type'.equalsIgnoreCase(name) && !'Content-Length'.equalsIgnoreCase(name)) {
                copy.put(name, value)
            }
        }
        return copy
    }

    private URI resolveUri(final Object uri, final Map<String, Object> query) {
        URI target = toUri(uri)
        if (!target.isAbsolute()) {
            if (baseUri == null) {
                throw new IllegalArgumentException('Request URI must be absolute when no baseUri is configured')
            }
            target = baseUri.resolve(target.toString())
        }
        enforceConfinement(target)
        return appendQuery(target, query)
    }

    /**
     * When {@code confineToBaseUri} is enabled, rejects any request whose
     * resolved URI leaves the {@link Config#baseUri} namespace: a different
     * origin (scheme, host, or effective port), or a path that escapes the base
     * path prefix (e.g. an absolute {@code /other} path or {@code ..} traversal).
     * <p>
     * The permitted set is exactly the URIs reachable by resolving a
     * non-escaping relative reference against {@code baseUri}. Comparison uses
     * raw (percent-encoded) path segments; a server that decodes {@code %2e%2e}
     * itself is outside the scope of this check.
     *
     * @param target the fully resolved request URI
     * @throws SecurityException if the target escapes the configured base URI
     */
    private void enforceConfinement(final URI target) {
        if (!confineToBaseUri || baseUri == null) {
            return
        }
        URI normalized = target.normalize()
        if (!sameOrigin(baseUri, normalized) || !isPathWithin(baseUri, normalized)) {
            throw new SecurityException(
                    "Request URI '" + target + "' is not confined to baseUri '" + baseUri + "'")
        }
    }

    /**
     * Compares two URIs by origin — scheme, host, and effective port — rather
     * than by raw {@code authority} string. This treats an explicit default
     * port as equivalent to an absent one (e.g. {@code http://host} and
     * {@code http://host:80}) and ignores user-info, so a redirect that differs
     * only in those respects is not spuriously rejected as leaving the origin.
     */
    private static boolean sameOrigin(final URI base, final URI target) {
        if (!base.scheme?.equalsIgnoreCase(target.scheme)) {
            return false
        }
        if (base.host == null || !base.host.equalsIgnoreCase(target.host)) {
            return false
        }
        return effectivePort(base) == effectivePort(target)
    }

    /**
     * The port a URI actually connects to: its explicit port when present,
     * otherwise the scheme's default ({@code 80} for http, {@code 443} for
     * https). Returns {@code -1} for a schemeless URI with no explicit port.
     */
    private static int effectivePort(final URI uri) {
        int port = uri.port
        if (port != -1) {
            return port
        }
        if ('https'.equalsIgnoreCase(uri.scheme)) {
            return 443
        }
        if ('http'.equalsIgnoreCase(uri.scheme)) {
            return 80
        }
        return -1
    }

    private static boolean isPathWithin(final URI base, final URI target) {
        String basePrefix = directoryPath(base.rawPath)
        String targetPath = (target.rawPath == null || target.rawPath.isEmpty()) ? '/' : target.rawPath
        // append '/' so a prefix match always lands on a path-segment boundary
        return (targetPath + '/').startsWith(basePrefix)
    }

    /**
     * Returns the "directory" portion of a path with a trailing slash — the
     * same prefix {@link URI#resolve(String)} keeps when resolving a relative
     * reference — so {@code /v2/x} and {@code /v2/} confine to {@code /v2/},
     * while a slash-less {@code /v2} confines to its parent {@code /}.
     */
    private static String directoryPath(final String path) {
        if (path == null || path.isEmpty()) {
            return '/'
        }
        if (path.endsWith('/')) {
            return path
        }
        int slash = path.lastIndexOf('/')
        return slash >= 0 ? path.substring(0, slash + 1) : '/'
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
         * Whether request URIs are confined to {@link #baseUri}. When enabled,
         * requests that resolve to a different origin (scheme, host, or effective
         * port) or escape the base path prefix are rejected with a
         * {@link SecurityException}. Requires {@code baseUri} to be configured.
         */
        boolean confineToBaseUri
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
         * Confines every request to the configured {@link #baseUri}. Absolute
         * request URIs pointing at another origin, and relative URIs that walk
         * above the base path (e.g. via {@code ..} or a leading {@code /}), are
         * rejected with a {@link SecurityException} instead of being executed.
         *
         * @param value {@code true} to reject requests that escape the base URI
         */
        void confineToBaseUri(final boolean value) {
            confineToBaseUri = value
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
