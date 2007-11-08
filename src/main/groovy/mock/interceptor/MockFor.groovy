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
    Map instanceExpectations = [:]
    Class clazz

    MockFor(Class clazz) {
        this.clazz = clazz
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

    void verify(GroovyObject obj) {
        instanceExpectations[obj].verify()
    }

    Object proxyInstance() {
        if (!clazz.isInterface()) return null
        def instance = ProxyGenerator.instantiateAggregateFromInterface(clazz)
        def thisproxy = MockProxyMetaClass.make(clazz)
        def thisexpect = new StrictExpectation(demand)
        thisproxy.interceptor = new MockInterceptor(expectation: thisexpect)
        instance.metaClass = thisproxy
        instanceExpectations[instance] = thisexpect
        return instance
    }

    Object proxyDelegateInstance() {
        if (!clazz.isInterface()) return null
        def instance = ProxyGenerator.instantiateAggregateFromInterface(clazz)
        def thisproxy = MockProxyMetaClass.make(clazz)
        def thisexpect = new StrictExpectation(demand)
        thisproxy.interceptor = new MockInterceptor(expectation: thisexpect)
        instance.metaClass = thisproxy
        def wrapped = ProxyGenerator.instantiateDelegate([clazz], instance)
        instanceExpectations[wrapped] = thisexpect
        return wrapped
    }
}