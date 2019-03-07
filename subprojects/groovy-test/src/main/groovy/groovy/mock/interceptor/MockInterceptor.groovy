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

    def expectation = null

    def beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        def result = expectation.match(methodName)
        if (result == MockProxyMetaClass.FALL_THROUGH_MARKER) return result
        return result(*arguments)
    }

    def beforeGet(Object object, String property) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "get${property[0].toUpperCase()}${property[1..-1]}"
        def result = expectation.match(name)
        if (result == MockProxyMetaClass.FALL_THROUGH_MARKER) return result
        return result()
    }

    void beforeSet(Object object, String property, Object newValue) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "set${property[0].toUpperCase()}${property[1..-1]}"
        def result = expectation.match(name)
        if (result != MockProxyMetaClass.FALL_THROUGH_MARKER) {
            result(newValue)
            result = null
        }
        // object is never used so cheat and use it for return value
        if (object instanceof Object[]) ((Object[])object)[0] = result
    }

    def afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        return null // never used
    }

    boolean doInvoke() {
        return false // future versions may allow collaborator method calls depending on state
    }
}