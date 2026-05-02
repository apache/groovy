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

import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import org.apache.groovy.lang.annotation.Incubating

import java.net.http.HttpHeaders
import java.net.http.HttpResponse
import java.util.Locale

/**
 * Simple response wrapper for the {@link HttpBuilder} DSL.
 *
 * @param status the HTTP status code
 * @param body the buffered response body
 * @param headers the response headers
 * @param raw the underlying JDK response
 * @since 6.0.0
 */
@Incubating
record HttpResult(int status, String body, HttpHeaders headers, HttpResponse<String> raw) {

    /**
     * Creates a result wrapper from a JDK HTTP response.
     *
     * @param response the response to wrap
     *
     * @since 6.0.0
     */
    HttpResult(final HttpResponse<String> response) {
        this(response.statusCode(), response.body(), response.headers(), response)
    }

    /**
     * Parses the buffered body as JSON.
     *
     * @return the parsed JSON value
     *
     * @since 6.0.0
     */
    Object getJson() {
        return new JsonSlurper().parseText(body)
    }

    /**
     * Parses the buffered body as XML.
     *
     * @return the parsed XML value
     *
     * @since 6.0.0
     */
    Object getXml() {
        return new XmlSlurper().parseText(body)
    }

    /**
     * Parses the buffered body as HTML using jsoup when available.
     *
     * @return the parsed HTML document
     *
     * @since 6.0.0
     */
    Object getHtml() {
        try {
            Class<?> jsoup = loadOptionalClass('org.jsoup.Jsoup')
            if (jsoup == null) {
                throw new ClassNotFoundException('org.jsoup.Jsoup')
            }
            return jsoup.getMethod('parse', String).invoke(null, body)
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("HTML parsing requires jsoup on the classpath", e)
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to parse HTML via jsoup", e)
        }
    }

    private static Class<?> loadOptionalClass(final String className) {
        List<ClassLoader> classLoaders = [
                Thread.currentThread().contextClassLoader,
                HttpResult.class.classLoader,
                ClassLoader.systemClassLoader
        ].findAll { it != null }.unique()

        for (ClassLoader classLoader : classLoaders) {
            try {
                return Class.forName(className, false, classLoader)
            } catch (ClassNotFoundException ignore) {
                // try next class loader
            }
        }
        return null
    }

    /**
     * Parses the buffered body according to the {@code Content-Type} header when possible.
     *
     * @return parsed JSON, XML, HTML, or the raw body text
     *
     * @since 6.0.0
     */
    Object getParsed() {
        String contentType = headers.firstValue('Content-Type').orElse('')
        String mediaType = contentType.split(';', 2)[0].trim().toLowerCase(Locale.ROOT)

        if (mediaType == 'application/json' || mediaType.endsWith('+json')) {
            return getJson()
        }
        if (mediaType == 'application/xml' || mediaType == 'text/xml' || mediaType.endsWith('+xml')) {
            return getXml()
        }
        if (mediaType == 'text/html') {
            try {
                return getHtml()
            } catch (IllegalStateException ignored) {
                System.err.println "HttpResult unable to parse HTML: $ignored.message"
            }
        }
        return body
    }
}
