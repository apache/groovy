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

    HttpClientHelper(String baseUrl, Map<String, String> defaultHeaders) {
        this.http = HttpBuilder.http(baseUrl)
        this.defaultHeaders = Collections.unmodifiableMap(new LinkedHashMap<>(defaultHeaders))
    }

    /**
     * Execute an HTTP request synchronously.
     *
     * @param method HTTP method (GET, POST, PUT, DELETE, PATCH)
     * @param urlTemplate URL template with {param} placeholders
     * @param returnTypeName the method's return type name for response conversion
     * @param pathParams map of path parameter names to values
     * @param queryParams map of query parameter names to values
     * @param headers map of additional headers for this request
     * @param body request body (serialized as JSON if non-null)
     * @return the converted response
     */
    Object execute(String method, String urlTemplate, String returnTypeName,
                   Map<String, Object> pathParams, Map<String, Object> queryParams,
                   Map<String, String> headers, Object body) {
        String url = resolveUrl(urlTemplate, pathParams)
        HttpResult result = http.request(method, url) {
            defaultHeaders.each { k, v -> header(k, v) }
            headers.each { k, v -> header(k, v) }
            queryParams.each { k, v -> query(k, v) }
            if (body != null) { json(body) }
        }
        return convertResult(result, returnTypeName)
    }

    /**
     * Execute an HTTP request asynchronously.
     *
     * @return a CompletableFuture containing the converted response
     */
    CompletableFuture<Object> executeAsync(String method, String urlTemplate, String returnTypeName,
                                           Map<String, Object> pathParams, Map<String, Object> queryParams,
                                           Map<String, String> headers, Object body) {
        CompletableFuture.supplyAsync {
            execute(method, urlTemplate, returnTypeName, pathParams, queryParams, headers, body)
        }
    }

    private static String resolveUrl(String template, Map<String, Object> params) {
        String url = template
        params.each { String k, Object v ->
            url = url.replace("{${k}}", URLEncoder.encode(String.valueOf(v), 'UTF-8'))
        }
        url
    }

    private static Object convertResult(HttpResult result, String returnTypeName) {
        if (result.status() >= 400) {
            throw new RuntimeException("HTTP ${result.status()}: ${result.body()}")
        }
        if (returnTypeName == HttpResult.name) return result
        if (returnTypeName == String.name || returnTypeName == 'java.lang.String') return result.body()
        if (returnTypeName == 'void') return null
        // Map, List, Object — parse as JSON
        return result.json
    }
}
