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

import java.util.regex.Pattern

/**
 * StubFor supports (typically unit) testing of classes in isolation by allowing
 * a loosely-ordered expectation of the behavior of collaborators to be defined.
 *
 * A typical test scenario involves a class under test (CUT) and one or more
 * collaborators. In such a scenario it is often desirable to just test the
 * business logic of the CUT. One strategy for doing that is to replace
 * the collaborator instances with simplified stub objects to help isolate out
 * the logic in the CUT. StubFor allows such stubs to be created using
 * meta-programming. The desired behavior of collaborators is defined as a
 * behavior specification. The behavior can be checked by the user using verify().
 * With StubFor, a stub's expectation is sequence independent and use of verify()
 * is left to the user.
 *
 * Typical usage is as follows:
 * <<pre class="groovyTestCase">
 * import groovy.mock.interceptor.StubFor
 *
 * class Person {
 *   String first, last
 * }
 *
 * class Family {
 *   Person mother, father
 *   def nameOfFather() { "$father.first $father.last" }
 * }
 *
 * def stub = new StubFor(Person)
 * stub.demand.with {
 *   getLast{ 'name' }
 *   getFirst{ 'dummy' }
 * }
 * stub.use {
 *   def john = new Person(first:'John', last:'Smith')
 *   def f = new Family(father:john)
 *   assert f.nameOfFather() == 'dummy name'
 * }
 * stub.expect.verify()
 * </pre>
 * Here, <code>Family</code> is our class under test and <code>Person</code> is the collaborator.
 * We are using normal Groovy property semantics here; hence the statement
 * <code>father.first</code> causes a call to <code>father.getFirst()</code> to occur.
 *
 * For a complete list of features, see: {@link MockFor}.
 */
class StubFor {

    MockProxyMetaClass proxy
    Demand demand
    Ignore ignore
    def expect
    Map instanceExpectations = [:]
    Class clazz

    StubFor(Class clazz, boolean interceptConstruction = false) {
        if (interceptConstruction && !GroovyObject.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("StubFor with constructor interception enabled is only allowed for Groovy objects but found: " + clazz.name)
        }
        this.clazz = clazz
        proxy = MockProxyMetaClass.make(clazz, interceptConstruction)
        demand = new Demand()
        ignore = new Ignore(parent:this)
        expect = new LooseExpectation(demand)
        proxy.interceptor = new MockInterceptor(expectation: expect)
    }

    /**
     * @See MockFor#use(Closure)
     */
    void use(Closure closure) {
        proxy.use closure
    }

    void use(GroovyObject obj, Closure closure) {
        proxy.use obj, closure
    }

    /**
     * For manual verification
     */
    void verify(GroovyObject obj) {
        instanceExpectations[obj].verify()
    }

    /**
     * Convenience method
     */
    void verify() {
        expect.verify()
    }

    /**
     * Allows particular method calls to be ignored and not treated as part of
     * the required behavior specification. If you don't specify a return closure
     * the method call will fall through to the underlying instance, i.e. half-mock style.
     * The <code>filter</code> object is invoked using the normal Groovy <code>isCase()</code> semantics.
     *
     * @See MockFor#ignore(Object, Closure)
     */
    def ignore(Object filter, Closure filterBehavior = null) {
        // if Stubbing Strings, attempt not to also match Strings with filter
        if (clazz.name == 'java.lang.String' && filter instanceof String) {
            filter = Pattern.compile(filter)
        }
        demand.ignore.put(filter, filterBehavior ?: MockProxyMetaClass.FALL_THROUGH_MARKER)
    }

    /**
     * Allows a more traditional instance-style stubbing paradigm. This is the
     * recommended method to call to use the instance-style with Groovy classes.
     *
     * @See MockFor#proxyInstance(Object)
     */
    GroovyObject proxyInstance(args=null) {
        makeProxyInstance(args, false)
    }

    /**
     * Allows a more traditional instance-style stubbing paradigm. This is the
     * recommended method to call to use the instance-style with Java classes.
     *
     * @See MockFor#proxyDelegateInstance(Object)
     */
    GroovyObject proxyDelegateInstance(args=null) {
        makeProxyInstance(args, true)
    }

    GroovyObject makeProxyInstance(args, boolean isDelegate) {
        def instance = MockFor.getInstance(clazz, args)
        def thisproxy = MockProxyMetaClass.make(isDelegate ? instance.getClass() : clazz)
        def thisdemand = new Demand(recorded: new ArrayList(demand.recorded), ignore: new HashMap(demand.ignore))
        def thisexpect = new LooseExpectation(thisdemand)
        thisproxy.interceptor = new MockInterceptor(expectation: thisexpect)
        instance.metaClass = thisproxy
        def wrapped = instance
        if (isDelegate && clazz.isInterface()) {
            wrapped = ProxyGenerator.INSTANCE.instantiateDelegate([clazz], instance)
        }
        instanceExpectations[wrapped] = thisexpect
        return wrapped
    }

}