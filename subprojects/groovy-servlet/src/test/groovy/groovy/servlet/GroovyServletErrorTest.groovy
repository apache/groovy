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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class GroovyServletErrorTest {

    private static final String VERBOSE = 'groovy.servlet.verbose.errors'

    @TempDir
    File tempDir

    @AfterEach
    void clearVerbose() {
        System.clearProperty(VERBOSE)
    }

    @Test
    void failingScriptSendsGenericErrorWithoutDetailByDefault() {
        def data = serviceFailingScript()

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data.error)
        assertNull(data.errorMessage, 'exception detail must not be sent to the client by default')
    }

    @Test
    void verboseErrorsRestoreDetailInResponseWhenEnabled() {
        System.setProperty(VERBOSE, 'true')

        def data = serviceFailingScript()

        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data.error)
        assertTrue(data.errorMessage?.contains('boom'),
                "expected the exception detail in the response, but got: ${data.errorMessage}")
    }

    private Map serviceFailingScript() {
        def script = new File(tempDir, 'boom.groovy')
        script.text = "throw new RuntimeException('boom')"

        def servlet = new GroovyServlet()
        servlet.init(mockConfig(script.toURI().toURL()))

        def data = [error: null, errorMessage: null, status: null, writer: new StringWriter()]
        servlet.service(mockRequest(), mockResponse(data))
        data
    }

    private HttpServletRequest mockRequest() {
        [
                getAttribute     : { null },
                getPathInfo      : { '/boom.groovy' },
                getServletPath   : { '/boom.groovy' },
                getSession       : { null },
                getParameterNames: { new Vector().elements() },
                getHeaderNames   : { new Vector().elements() },
        ] as HttpServletRequest
    }

    private HttpServletResponse mockResponse(Map data) {
        [
                getWriter     : { new PrintWriter(data.writer) },
                sendError     : { Object... args -> data.error = args[0]; if (args.length > 1) data.errorMessage = args[1] },
                setContentType: { contentType -> },
                setStatus     : { status -> data.status = status },
                flushBuffer   : { -> },
        ] as HttpServletResponse
    }

    private ServletConfig mockConfig(URL scriptUrl) {
        def servletContext = [
                getRealPath: { arg -> null },
                getResource: { arg -> scriptUrl },
                log        : { Object... args -> },
        ] as ServletContext
        [
                getServletName   : { 'Groovy' },
                getServletContext: { servletContext },
                getInitParameter : { null },
        ] as ServletConfig
    }
}
