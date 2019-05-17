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
import groovy.xml.MarkupBuilder

import javax.servlet.ServletContext
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * This test case tests the ServletBinding class.
 */
class ServletBindingTest extends GroovyTestCase {

    def session = {} as HttpSession
    def response = {} as HttpServletResponse
    def context = {} as ServletContext

    def makeDefaultBinding = { request ->
        new ServletBinding(
                request as HttpServletRequest,
                response as HttpServletResponse,
                context as ServletContext
        )
    }

    def makeDefaultRequest = {
        [
            getSession: { session },
            getParameterNames: { new Vector().elements() },
            getHeaderNames: { new Vector().elements() }
        ] as HttpServletRequest
    }

    /**
     * Tests that the constructor binds the correct default variables.
     */
    void testConstructor_VariableBindings() {
        def request = makeDefaultRequest()
        def binding = makeDefaultBinding(request)

        assert request == binding.getVariable('request')
        assert response == binding.getVariable('response')
        assert context == binding.getVariable('context')
        assert context == binding.getVariable('application')
        assert session == binding.getVariable('session')
        assert binding.getVariable('params').isEmpty()
        assert binding.getVariable('headers').isEmpty()
    }

    /**
     * Tests that the constructor binds request parameter names correctly.
     */
    void testConstructor_ParameterNameBindings() {
        def parmNames = new Vector()
        parmNames.add('name1')
        parmNames.add('name2')

        def request = [
                getSession: { session },
                getHeaderNames: { new Vector().elements() },
                getParameterNames: { parmNames.elements() },
                getParameterValues: {
                    // prepend string parm to known value to simulate attribute map
                    ['value_for_' + it] as String[]
                }
        ] as HttpServletRequest

        def binding = makeDefaultBinding(request)

        def variables = binding.getVariable('params')
        assert 2 == variables.size()
        assert 'value_for_name1' == variables.get('name1')
        assert 'value_for_name2' == variables.get('name2')
    }

    /**
     * Tests that the constructor binds request header values correctly.
     */
    void testConstructor_HeaderBindings() {
        def headerNames = new Vector()
        headerNames.add('name1')
        headerNames.add('name2')

        def request = [
                getSession: {session},
                getParameterNames: {new Vector().elements()},
                getHeaderNames: {headerNames.elements()},
                getHeader: {
                    // prepend string parm to known value to simulate header map
                    'value_for_' + it
                }
        ] as HttpServletRequest

        def binding = makeDefaultBinding(request)

        def variables = binding.getVariable('headers')
        assert 2 == variables.size()
        assert 'value_for_name1' == variables.get('name1')
        assert 'value_for_name2' == variables.get('name2')
    }

    /**
     * Tests the argument contract on getVariable.
     */
    void testGetVariable_Contract() {
        def request = makeDefaultRequest()
        def binding = makeDefaultBinding(request)

        shouldFail(IllegalArgumentException) { binding.getVariable(null) }
        shouldFail(IllegalArgumentException) { binding.getVariable('') }
    }

    /**
     * Tests that getVariables truely returns all variables
     */
    void testGetVariables_Contract() {
        def expectedVariables = ['request', 'response', 'context', 'application',
                'session', 'params', 'headers', 'out', 'sout', 'html', 'json']
        def request = makeDefaultRequest()
        def binding = makeDefaultBinding(request)
        def keys = binding.variables.keySet()

        expectedVariables.each {
            assert keys.contains(it)
        }
    }

    /**
     * Tests that getVariable works for the special key names.
     */
    void testGetVariable_ImplicitKeyNames() {
        def writer = new PrintWriter(new StringWriter())
        def outputStream = new OutputStreamStub()

        def response = [
                getWriter: { writer },
                getOutputStream: { outputStream }
        ] as HttpServletResponse

        def request = makeDefaultRequest()

        def binding = new ServletBinding(
                request as HttpServletRequest,
                response as HttpServletResponse,
                context as ServletContext
        )

        assert binding.getVariable('out') instanceof PrintWriter
        assert binding.getVariable('html') instanceof MarkupBuilder
        assert binding.getVariable('sout') instanceof ServletOutputStream
        assert binding.getVariable('json') instanceof groovy.json.StreamingJsonBuilder
    }

    void testOutSoutWriteException() {
        def writer = new PrintWriter(new StringWriter())
        def outputStream = new OutputStreamStub()

        def response = [
                getWriter: { writer },
                getOutputStream: { outputStream }
        ] as HttpServletResponse

        def request = makeDefaultRequest()

        def binding = new ServletBinding(
                request as HttpServletRequest,
                response as HttpServletResponse,
                context as ServletContext
        )

        binding.out.print('foo')
        binding.html.foo()
        binding.out.print(binding.json.foo())

        shouldFail(IllegalStateException) {
            binding.sout.print('foo')
        }
    }

    void testSoutOutWriteException() {
        def writer = new PrintWriter(new StringWriter())
        def outputStream = new OutputStreamStub()

        def response = [
                getWriter: { writer },
                getOutputStream: { outputStream }
        ] as HttpServletResponse

        def request = makeDefaultRequest()

        def binding = new ServletBinding(
                request as HttpServletRequest,
                response as HttpServletResponse,
                context as ServletContext
        )

        binding.sout.print('foo')
        shouldFail(IllegalStateException) {
            binding.out.print('foo')
        }
        shouldFail(IllegalStateException) {
            binding.html.foo()
        }
    }

    /**
     * Tests the contract on setVarible().
     */
    void testSetVariable_Contract() {
        def request = makeDefaultRequest()
        def binding = makeDefaultBinding(request)

        shouldFail(IllegalArgumentException) { binding.setVariable(null, null) }
        shouldFail(IllegalArgumentException) { binding.setVariable('', null) }
        shouldFail(IllegalArgumentException) { binding.setVariable('out', null) }
        shouldFail(IllegalArgumentException) { binding.setVariable('sout', null) }
        shouldFail(IllegalArgumentException) { binding.setVariable('html', null) }
        shouldFail(IllegalArgumentException) { binding.setVariable('json', null) }
    }

    /**
     * Tests setVariable.
     */
    void testSetVariable() {
        def request = makeDefaultRequest()
        def binding = makeDefaultBinding(request)

        binding.setVariable('var_name', 'var_value')
        def variables = binding.getVariables()

        assert 'var_value' == variables.get('var_name')
    }
}

/**
 * Test specific sub class to stub out the ServletOutputStream class.
 */
class OutputStreamStub extends ServletOutputStream {
    void write(int x) { }
}
