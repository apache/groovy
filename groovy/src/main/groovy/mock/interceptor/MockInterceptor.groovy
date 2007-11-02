/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.mock.interceptor

/**
    Intercepting calls to the collaborating object and notify the expectation object.
    @author Dierk Koenig
*/

class MockInterceptor implements PropertyAccessInterceptor {

    def expectation = null

    Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        return expectation.match(methodName).call(arguments)
    }

    Object beforeGet(Object object, String property) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "get${property[0].toUpperCase()}${property[1..-1]}"
        return expectation.match(name).call()                    
    }

    void beforeSet(Object object, String property, Object newValue) {
        if (!expectation) throw new IllegalStateException("Property 'expectation' must be set before use.")
        String name = "set${property[0].toUpperCase()}${property[1..-1]}"
        expectation.match(name).call(newValue)
    }

    Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        return null // never used
    }

    boolean doInvoke() {
        return false // future versions may allow collaborator method calls depending on state
    }
}