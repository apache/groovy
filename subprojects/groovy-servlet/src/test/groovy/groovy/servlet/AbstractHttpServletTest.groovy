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

import groovy.test.GroovyTestCase

import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest

/**
 * This test case tests the AbstractHttpServlet class. It uses a test
 * specific subclass called ConcreteHttpServlet to test the abstract
 * class in isolation from any implementations.
 */
class AbstractHttpServletTest extends GroovyTestCase {

    def servlet

    void setUp() {
        super.setUp()
        servlet = new ConcreteHttpServlet()
    }

    /**
     * getScriptUri() concatenates the servlet path and path info
     * attributes if attributes exist on the http request.
     */
    void testGetScriptUri_AllAttributesExist() {
        // just return whatever attributes were requested
        def request = { attribute -> attribute }

        assert servlet.getScriptUri(request as HttpServletRequest) ==
                AbstractHttpServlet.INC_SERVLET_PATH + AbstractHttpServlet.INC_PATH_INFO
    }

    /**
     * getScriptUri() returns the servlet path if the http request
     * contains path but no path info attribute.
     */
    void testGetScriptUri_NoPathInfoAttribute() {
        // just return whatever attributes were requested, except for path info attribute
        def request = { attribute ->
            attribute == AbstractHttpServlet.INC_PATH_INFO ? null : attribute
        }

        assert servlet.getScriptUri(request as HttpServletRequest) ==
                AbstractHttpServlet.INC_SERVLET_PATH
    }

    /**
     * Tests getScriptUri when no attributes exist, but servletPath and
     * pathInfo methods return data.
     */
    void testGetScriptUri_NoAttributesPathInfoExists() {
        def request = [
                getAttribute: { null },
                getServletPath: { 'servletPath' },
                getPathInfo: { 'pathInfo' }
        ] as HttpServletRequest

        def servlet = new ConcreteHttpServlet()

        assert servlet.getScriptUri(request) == 'servletPathpathInfo'
    }

    /**
     * Tests getScriptUri when no attributes exist, no path info exists,
     * but servletPath returns data.
     */
    void testGetScriptUri_NoAttributesPathInfoMissing() {
        def request = [
                getAttribute: { null },
                getServletPath: { 'servletPath' },
                getPathInfo: { null }
        ] as HttpServletRequest

        def servlet = new ConcreteHttpServlet()

        assert servlet.getScriptUri(request) == 'servletPath'
    }

    /**
     * Tests getting URIs as files.
     */
    void testGetScriptURIasFile() {
        def request = [
                getAttribute: { null },
                getServletPath: { 'servletPath' },
                getPathInfo: { 'pathInfo' }
        ] as HttpServletRequest

        def servletContext = [
                getRealPath: { arg -> 'realPath' + arg }
        ] as ServletContext

        def servletConfig = [
                getServletContext: { servletContext },
                getInitParameter: { null }
        ] as ServletConfig

        servlet.init(servletConfig)
        def file = servlet.getScriptUriAsFile(request)

        assert file.getName() == 'realPathservletPathpathInfo'
    }

    /**
     * Tests getting URIs as files where filename not available.
     */
    void testGetScriptURIasFileNoMapping() {
        def request = [
                getAttribute: { null },
                getServletPath: { 'servletPath' },
                getPathInfo: { 'pathInfo' }
        ] as HttpServletRequest

        def servletContext = [
                getRealPath: { arg -> null }
        ] as ServletContext

        def servletConfig = [
                getServletContext: { servletContext },
                getInitParameter: { null }
        ] as ServletConfig

        servlet.init(servletConfig)
        def file = servlet.getScriptUriAsFile(request)

        assert file == null
    }

    /**
     * Tests that exception is thrown when resource is not found.
     */
    void testGetResourceConnection_MissingResource() {
        def servletContext = [
                getRealPath: { arg -> 'realPath' + arg },
                getResource: { arg -> null }
        ] as ServletContext

        def servletConfig = [
                getServletContext: { servletContext },
                getInitParameter: { null }
        ] as ServletConfig

        // servlet config is used to find resources
        servlet.init(servletConfig)

        shouldFail(groovy.util.ResourceException) {
            servlet.getResourceConnection('someresource')
        }
    }

    /**
     * Tests finding resource.
     */
    void testGetResourceConnection_FoundInCurrentDir() {
        def urlStub = new java.net.URL('file:realPath/someresource')
        def servletContext = [
                getRealPath: { arg -> 'realPath' + arg },
                getResource: { arg -> arg == '/someresource' ? urlStub : null }
        ] as ServletContext

        def servletConfig = [
                getServletContext: { servletContext },
                getInitParameter: { null }
        ] as ServletConfig

        // servlet config is used to find resources
        servlet.init(servletConfig)

        def connection = servlet.getResourceConnection('someresource')

        assert connection.getURL() == urlStub
    }

    /**
     * Tests finding resource in web-inf directory.
     */
    void testGetResourceConnection_FoundInWebInf() {
        def urlStub = new java.net.URL('file:realPath/WEB-INF/groovy/someresource')
        def servletContext = [
                getRealPath: { arg -> 'realPath' + arg },
                getResource: { arg -> arg == '/WEB-INF/groovy/someresource' ? urlStub : null }
        ] as ServletContext

        def servletConfig = [
                getServletContext: { servletContext },
                getInitParameter: { null }
        ] as ServletConfig

        // servlet config is used to find resources
        servlet.init(servletConfig)

        def connection = servlet.getResourceConnection('someresource')

        assert connection.getURL() == urlStub
    }

    /**
     * Tests regex style resource replacement for first occurrence.
     */
    void testGetResourceConnection_Replace1stFooWithBar() {
        def servletContext = [
                getRealPath: { arg -> 'realPath' + arg },
                getResource: { arg ->
                    if (arg.startsWith('//')) arg = arg.substring(2)
                    new URL('http://' + (arg == '/' ? '' : arg))
                }
        ] as ServletContext

        def servletConfig = [
                getServletContext: { servletContext },
                getInitParameter: { arg ->
                    // replace first occurrence of foo resources with bar resources
                    if (arg == 'resource.name.regex') return 'foo'
                    else if (arg == 'resource.name.replacement') return 'bar'
                    else if (arg == 'resource.name.replace.all') return 'false'
                    return null
                }
        ] as ServletConfig

        def request = [
                getAttribute: { null },
                getServletPath: { '/somefoo/foo' },
                getPathInfo: { null }
        ] as HttpServletRequest

        // servlet config is used to find resources
        servlet.init(servletConfig)

        // replace first foo with bar in resources
        def connection = servlet.getResourceConnection(servlet.getScriptUri(request))

        assert connection.getURL().toExternalForm() == new URL('http:/somebar/foo').toExternalForm()
    }

    /**
     * Tests regex style resource replacement for all occurrences.
     */
    void testGetResourceConnection_ReplaceAllFooWithBar() {
        def servletContext = [
                getRealPath: { arg -> 'realPath' + arg },
                getResource: { arg ->
                    if (arg.startsWith('//')) arg = arg.substring(2)
                    new URL('http://' + (arg == '/' ? '' : arg))
                }
        ] as ServletContext

        def servletConfig = [
                getServletContext: { servletContext },
                getInitParameter: { arg ->
                    // replace all occurrences of foo resources with bar resources
                    if (arg == 'resource.name.regex') return 'foo'
                    else if (arg == 'resource.name.replacement') return 'bar'
                    else if (arg == 'resource.name.replace.all') return 'true'
                    return null
                }
        ] as ServletConfig

        def request = [
                getAttribute: { null },
                getServletPath: { '/somefoo/foo' },
                getPathInfo: { null }
        ] as HttpServletRequest

        // servlet config is used to find resources
        servlet.init(servletConfig)

        // replace all foo(s) with bar in resources
        def connection = servlet.getResourceConnection(servlet.getScriptUri(request))

        assert connection.getURL().toExternalForm() == new URL('http:/somebar/bar').toExternalForm()
    }
}

// test specific subclass
class ConcreteHttpServlet extends AbstractHttpServlet {}
