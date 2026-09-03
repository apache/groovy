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
package groovy.servlet

import jakarta.servlet.ServletConfig
import jakarta.servlet.ServletContext
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class TemplateServletTest {

    TemplateServlet servlet

    @TempDir
    public File temporaryFolder

    @BeforeEach
    void setUp() {
        servlet = new TemplateServlet()
    }

    @Test
    void test_service_for_existing_resource() {
        def templateFile = new File(temporaryFolder, 'template.gsp').tap { createNewFile() }
        def url = templateFile.toURI().toURL()
        def servletConfig = mockServletConfigForUrlResource(url)
        HttpServletRequest request = mockRequest()
        def (HttpServletResponse response, responseData) = mockResponse()
        servlet.init(servletConfig)

        servlet.service(request, response)

        assert responseData.error == null
        assert responseData.writer.toString() != ''
        assert responseData.status == HttpServletResponse.SC_OK
    }

    @Test
    void test_service_for_missing_resource() {
        def url = null
        def servletConfig = mockServletConfigForUrlResource(url)
        HttpServletRequest request = mockRequest()
        def (HttpServletResponse response, responseData) = mockResponse()
        servlet.init(servletConfig)

        servlet.service(request, response)

        assert responseData.error == HttpServletResponse.SC_NOT_FOUND
        assert responseData.writer.toString() == ''
        assert responseData.status == null
    }

    @Test
    void test_compiled_template_is_cached_across_garbage_collection() {
        def templateFile = new File(temporaryFolder, 'cached.gsp').tap { write 'hello' }
        def url = templateFile.toURI().toURL()
        servlet.init(mockServletConfigForUrlResource(url))

        def first = servlet.getTemplate(url)
        forceGarbageCollection()
        def second = servlet.getTemplate(url)

        assert second.is(first)
    }

    /**
     * The cache key is a fresh string on every lookup and nothing outside the cache
     * refers to it, so a cache holding its keys weakly discards every entry as soon
     * as the collector runs. Force a collection to tell such a cache apart from one
     * that retains what it stored.
     */
    private static void forceGarbageCollection() {
        def canary = new java.lang.ref.WeakReference<Object>(new Object())
        for (int i = 0; canary.get() != null && i < 100; i++) {
            System.gc()
            Thread.sleep(10)
        }
        assert canary.get() == null : 'could not force a garbage collection'
    }

    private mockRequest() {
        return [
                getAttribute     : { null },
                getPathInfo      : { 'pathInfo' },
                getScriptUri     : 'scriptUri',
                getServletPath   : { 'servletPath' },
                getSession       : { null },
                getParameterNames: { new Vector().elements() },
                getHeaderNames   : { new Vector().elements() },
        ] as HttpServletRequest
    }

    private mockResponse() {
        def data = [
                writer: new StringWriter(),
                status: null,
                error : null,
        ]
        def mock = [
                getWriter     : { new PrintWriter(data.writer) },
                sendError     : { error -> data.error = error },
                setContentType: { contentType -> },
                setStatus     : { status -> data.status = status },
                flushBuffer   : { -> },
        ] as HttpServletResponse
        return [mock, data]
    }

    private mockServletConfigForUrlResource(URL mockedResourceUrl) {
        def servletContext = [
                getRealPath: { arg -> null },
                getResource: { arg -> mockedResourceUrl },
                log        : { msg -> },
        ] as ServletContext
        [
                getServletName   : { 'name' },
                getServletContext: { servletContext },
                getInitParameter : { null },
        ] as ServletConfig
    }

}
