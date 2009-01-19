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

import java.lang.reflect.Modifier

/**
    Facade over the Stubbing details.
    A Stub's expectation is sequence independent and use of verify() is left to the user.
    @see MockFor.
    @author Dierk Koenig
    @author Paul King
*/

class StubFor {

    MockProxyMetaClass proxy
    Demand demand
    def expect
    Map instanceExpectations = [:]
    Class clazz

    StubFor(Class clazz) {
        this.clazz = clazz
        proxy = MockProxyMetaClass.make(clazz)
        demand = new Demand()
        expect = new LooseExpectation(demand)
        proxy.interceptor = new MockInterceptor(expectation: expect)
    }

    void use(Closure closure) {
        proxy.use closure
    }

    void use(GroovyObject obj, Closure closure) {
        proxy.use obj, closure
    }

    // TODO: remove dup with MockFor
    void verify(GroovyObject obj) {
        instanceExpectations[obj].verify()
    }

    Object proxyInstance() {
        proxyInstance(null)
    }

    Object proxyInstance(args) {
        def instance = getInstance(clazz, args)
        def thisproxy = MockProxyMetaClass.make(clazz)
        def thisdemand = new Demand(recorded: new ArrayList(demand.recorded))
        def thisexpect = new LooseExpectation(thisdemand)
        thisproxy.interceptor = new MockInterceptor(expectation: thisexpect)
        instance.metaClass = thisproxy
        instanceExpectations[instance] = thisexpect
        return instance
    }

    Object proxyDelegateInstance() {
        proxyDelegateInstance(null)
    }

    Object proxyDelegateInstance(args) {
        def instance = getInstance(clazz, args)
        def thisproxy = MockProxyMetaClass.make(clazz)
        def thisdemand = new Demand(recorded: new ArrayList(demand.recorded))
        def thisexpect = new LooseExpectation(thisdemand)
        thisproxy.interceptor = new MockInterceptor(expectation: thisexpect)
        instance.metaClass = thisproxy
        def wrapped = null
        if (clazz.isInterface()) {
            wrapped = ProxyGenerator.instantiateDelegate([clazz], instance)
        } else {
            wrapped = ProxyGenerator.instantiateDelegate(instance)
        }
        instanceExpectations[wrapped] = thisexpect
        return wrapped
    }

    private getInstance(Class clazz, args) {
        def instance = null
        if (clazz.isInterface()) {
            instance = ProxyGenerator.instantiateAggregateFromInterface(clazz)
        } else if (Modifier.isAbstract(clazz.modifiers)) {
            instance = ProxyGenerator.instantiateAggregateFromBaseClass(clazz, args)
        } else if (args != null) {
            if (clazz instanceof GroovyObject) {
                instance = clazz.newInstance(args)
            } else {
                instance = ProxyGenerator.instantiateDelegate(clazz.newInstance(args))
            }
        } else {
            if (clazz instanceof GroovyObject) {
                instance = clazz.newInstance()
            } else {
                instance = ProxyGenerator.instantiateDelegate(clazz.newInstance())
            }
        }
        return instance
    }
}