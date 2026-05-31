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
package groovy.mock.interceptor

/**
 * Intercepting calls to the collaborating object and notify the expectation object.
 */

class MockInterceptor implements PropertyAccessInterceptor {

    /**
     * Expectation object that resolves and validates intercepted interactions.
     */
    def expectation = null

    /**
     * Resolves an intercepted method call against the configured expectation.
     *
     * @param object the invocation receiver
     * @param methodName the method name
     * @param arguments the invocation arguments
     * @return the expectation result or {@link MockProxyMetaClass#FALL_THROUGH_MARKER}
     */
    def beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        def result = expectation.match(methodName)
        if (result == MockProxyMetaClass.FALL_THROUGH_MARKER) return result
        return result(*arguments)
    }

    /**
     * Resolves an intercepted property read against the configured expectation.
     *
     * @param object the property owner
     * @param property the property name
     * @return the expectation result or {@link MockProxyMetaClass#FALL_THROUGH_MARKER}
     */
    def beforeGet(Object object, String property) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "get${property[0].toUpperCase(Locale.ROOT)}${property[1..-1]}"
        def result = expectation.match(name)
        if (result == MockProxyMetaClass.FALL_THROUGH_MARKER) return result
        return result()
    }

    /**
     * Resolves an intercepted property write against the configured expectation.
     *
     * @param object the property owner or result holder used by the proxy
     * @param property the property name
     * @param newValue the value being assigned
     */
    void beforeSet(Object object, String property, Object newValue) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "set${property[0].toUpperCase(Locale.ROOT)}${property[1..-1]}"
        def result = expectation.match(name)
        if (result != MockProxyMetaClass.FALL_THROUGH_MARKER) {
            result(newValue)
            result = null
        }
        // object is never used so cheat and use it for return value
        if (object instanceof Object[]) ((Object[])object)[0] = result
    }

    /**
     * Post-invocation hook required by the interceptor contract.
     *
     * @param object the invocation receiver
     * @param methodName the method name
     * @param arguments the invocation arguments
     * @param result the invocation result
     * @return always {@code null}
     */
    def afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        return null // never used
    }

    /**
     * Indicates whether the proxy should invoke the original implementation after interception.
     *
     * @return {@code false} because this interceptor decides invocation explicitly
     */
    boolean doInvoke() {
        return false // future versions may allow collaborator method calls depending on state
    }
}
