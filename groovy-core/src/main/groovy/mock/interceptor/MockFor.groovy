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
    Facade over the Mocking details.
    A Mock's expectation is always sequence dependent and it's use always ends with a verify().
    See also StubFor.
*/

class MockFor {

    MockProxyMetaClass proxy
    Demand demand
    def expect

    MockFor(Class clazz) {
        proxy = MockProxyMetaClass.make(clazz)
        demand = new Demand()
        expect = new StrictExpectation(demand)
        proxy.interceptor = new MockInterceptor(expectation: expect)
    }

    void use(Closure closure) {
        proxy.use closure
        expect.verify()
    }

    void use(GroovyObject obj, Closure closure) {
        proxy.use obj, closure
        expect.verify()
    }
}