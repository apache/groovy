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

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TemplateServletTest {

    TemplateServlet servlet

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Before
    void setUp() {
        servlet = new TemplateServlet()
    }

    @Test
    void test_service_for_existing_resource() {
        def templateFile = temporaryFolder.newFile('template.gsp')
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
