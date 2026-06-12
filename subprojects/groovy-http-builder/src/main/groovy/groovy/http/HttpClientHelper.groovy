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

import org.apache.groovy.lang.annotation.Incubating

import java.time.Duration
import java.util.concurrent.CompletableFuture

/**
 * Runtime helper used by the generated declarative HTTP client implementations.
 * Not intended for direct use.
 *
 * @since 6.0.0
 */
@Incubating
final class HttpClientHelper {
    private final HttpBuilder http
    private final Map<String, String> defaultHeaders

    /**
     * Creates a helper with default timeout and redirect settings.
     *
     * @param baseUrl the base URL used by generated clients
     * @param defaultHeaders the headers applied to every request
     */
    HttpClientHelper(String baseUrl, Map<String, String> defaultHeaders) {
        this(baseUrl, defaultHeaders, 0, 0, false)
    }

    /**
     * Creates a helper backed by a new {@link HttpBuilder}.
     *
     * @param baseUrl the base URL used by generated clients
     * @param defaultHeaders the headers applied to every request
     * @param connectTimeoutSeconds the connect timeout in seconds, or {@code 0} for none
     * @param requestTimeoutSeconds the request timeout in seconds, or {@code 0} for none
     * @param followRedirects whether redirects should be followed automatically
     */
    HttpClientHelper(String baseUrl, Map<String, String> defaultHeaders,
                     int connectTimeoutSeconds, int requestTimeoutSeconds,
                     boolean followRedirects) {
        this.http = HttpBuilder.http {
            baseUri(baseUrl)
            if (connectTimeoutSeconds > 0) connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
            if (requestTimeoutSeconds > 0) requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
            if (followRedirects) delegate.followRedirects(true)
        }
        this.defaultHeaders = Collections.unmodifiableMap(new LinkedHashMap<>(defaultHeaders))
    }

    /**
     * Constructor accepting a pre-configured HttpBuilder instance.
     * Used by the create(Closure) factory for advanced configuration.
     *
     * @param http the pre-configured HTTP builder to reuse
     * @param defaultHeaders the headers applied to every request
     */
    HttpClientHelper(HttpBuilder http, Map<String, String> defaultHeaders) {
        this.http = http
        this.defaultHeaders = Collections.unmodifiableMap(new LinkedHashMap<>(defaultHeaders))
    }

    /**
     * Execute an HTTP request synchronously.
     *
     * @param method HTTP method (GET, POST, PUT, DELETE, PATCH)
     * @param urlTemplate URL template with {param} placeholders
     * @param returnTypeName the method's return type name for response conversion
     * @param pathParams map of path parameter names to values
     * @param queryOrFormParams map of query parameter names or form fields to values
     * @param headers map of additional headers for this request
     * @param body request body (serialized as JSON if non-null)
     * @param timeoutSeconds per-request timeout in seconds, or {@code 0} for none
     * @param bodyMode serialization mode for the request body
     * @param errorTypeName fully qualified exception type used for HTTP error responses
     * @return the converted response
     */
    Object execute(String method, String urlTemplate, String returnTypeName,
                   Map<String, Object> pathParams, Map<String, Object> queryOrFormParams,
                   Map<String, String> headers, Object body, int timeoutSeconds = 0,
                   String bodyMode = 'json', String errorTypeName = '') {
        String url = resolveUrl(urlTemplate, pathParams)
        boolean isForm = bodyMode == 'form'
        HttpResult result = http.request(method, url) {
            defaultHeaders.each { k, v -> header(k, v) }
            headers.each { k, v -> header(k, v) }
            if (isForm) {
                form(queryOrFormParams)
            } else {
                queryOrFormParams.each { k, v -> query(k, v) }
            }
            if (timeoutSeconds > 0) timeout(Duration.ofSeconds(timeoutSeconds))
            if (body != null) {
                switch (bodyMode) {
                    case 'text': text(body); break
                    case 'form': break // already handled above
                    default: json(body)
                }
            }
        }
        return convertResult(result, returnTypeName, errorTypeName)
    }

    /**
     * Execute an HTTP request asynchronously.
     *
     * @param method HTTP method (GET, POST, PUT, DELETE, PATCH)
     * @param urlTemplate URL template with {param} placeholders
     * @param returnTypeName the method's return type name for response conversion
     * @param pathParams map of path parameter names to values
     * @param queryOrFormParams map of query parameter names or form fields to values
     * @param headers map of additional headers for this request
     * @param body request body before serialization
     * @param timeoutSeconds per-request timeout in seconds, or {@code 0} for none
     * @param bodyMode serialization mode for the request body
     * @param errorTypeName fully qualified exception type used for HTTP error responses
     * @return a CompletableFuture containing the converted response
     */
    CompletableFuture<Object> executeAsync(String method, String urlTemplate, String returnTypeName,
                                           Map<String, Object> pathParams, Map<String, Object> queryOrFormParams,
                                           Map<String, String> headers, Object body, int timeoutSeconds = 0,
                                           String bodyMode = 'json', String errorTypeName = '') {
        String url = resolveUrl(urlTemplate, pathParams)
        boolean isForm = bodyMode == 'form'
        return http.requestAsync(method, url) {
            defaultHeaders.each { k, v -> header(k, v) }
            headers.each { k, v -> header(k, v) }
            if (isForm) {
                form(queryOrFormParams)
            } else {
                queryOrFormParams.each { k, v -> query(k, v) }
            }
            if (timeoutSeconds > 0) timeout(Duration.ofSeconds(timeoutSeconds))
            if (body != null) {
                switch (bodyMode) {
                    case 'text': text(body); break
                    case 'form': break
                    default: json(body)
                }
            }
        }.thenApply { HttpResult result ->
            convertResult(result, returnTypeName, errorTypeName)
        }
    }

    private static String resolveUrl(String template, Map<String, Object> params) {
        String url = template
        params.each { String k, Object v ->
            url = url.replace("{${k}}", URLEncoder.encode(String.valueOf(v), 'UTF-8'))
        }
        url
    }

    private static Object convertResult(HttpResult result, String returnTypeName, String errorTypeName) {
        if (result.status() >= 400) {
            handleError(result, errorTypeName)
        }
        switch (returnTypeName) {
            case 'void': return null
            case String.name:
            case 'java.lang.String': return result.body()
            case HttpResult.name: return result
            case 'groovy.xml.slurpersupport.GPathResult': return result.xml
            case 'org.jsoup.nodes.Document': return result.html
            case Map.name:
            case 'java.util.Map':
            case List.name:
            case 'java.util.List':
            case Object.name: return result.json
            default:
                // Typed response — parse JSON then coerce to target type
                def json = result.json
                try {
                    return json.asType(Class.forName(returnTypeName))
                } catch (Exception e) {
                    return json // fallback: return raw parsed JSON
                }
        }
    }

    private static void handleError(HttpResult result, String errorTypeName) {
        if (errorTypeName) {
            try {
                Class<?> errorType = Class.forName(errorTypeName)
                Exception error = createError(errorType, result)
                if (error != null) throw error
            } catch (ClassNotFoundException ignored) {
                // fall through to default
            }
        }
        throw new RuntimeException("HTTP ${result.status()}: ${result.body()}")
    }

    private static Exception createError(Class<?> errorType, HttpResult result) {
        String message = "HTTP ${result.status()}: ${result.body()}"
        // Try constructor(int status, String body)
        try {
            return (Exception) errorType.getConstructor(int, String).newInstance(result.status(), result.body())
        } catch (ReflectiveOperationException ignored) {}
        // Try constructor(Integer status, String body)
        try {
            return (Exception) errorType.getConstructor(Integer, String).newInstance(result.status(), result.body())
        } catch (ReflectiveOperationException ignored) {}
        // Try constructor(String message)
        try {
            return (Exception) errorType.getConstructor(String).newInstance(message)
        } catch (ReflectiveOperationException ignored) {}
        // Try no-arg constructor
        try {
            return (Exception) errorType.getDeclaredConstructor().newInstance()
        } catch (ReflectiveOperationException ignored) {}
        return null
    }
}
