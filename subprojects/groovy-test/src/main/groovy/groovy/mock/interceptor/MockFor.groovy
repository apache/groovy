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

import java.lang.reflect.Modifier
import java.util.regex.Pattern

/**
 * MockFor supports (typically unit) testing of classes in isolation by allowing
 * a strictly ordered expectation of the behavior of collaborators to be defined.
 *
 * A typical test scenario involves a class under test (CUT) and one or more
 * collaborators. In such a scenario it is often desirable to just test the
 * business logic of the CUT. One strategy for doing that is to replace
 * the collaborator instances with simplified mock objects to help isolate out
 * the logic in the CUT. MockFor allows such mocks to be created using
 * meta-programming. The desired behavior of collaborators is defined as a
 * behavior specification. The behavior is enforced and checked automatically.
 * With MockFor, a mock's expectation is always sequence dependent and its use
 * automatically ends with a verify().
 *
 * Typical usage is as follows:
 * <pre class="groovyTestCase">
 * import groovy.mock.interceptor.MockFor
 *
 * class Person {
 *   String first, last
 * }
 *
 * class Family {
 *   Person father, mother
 *   def nameOfMother() { "$mother.first $mother.last" }
 * }
 *
 * def mock = new MockFor(Person)
 * mock.demand.getFirst{ 'dummy' }
 * mock.demand.getLast{ 'name' }
 * mock.use {
 *   def mary = new Person(first:'Mary', last:'Smith')
 *   def f = new Family(mother:mary)
 *   assert f.nameOfMother() == 'dummy name'
 * }
 * </pre>
 * Here, <code>Family</code> is our class under test and <code>Person</code> is the collaborator.
 * We are using normal Groovy property semantics here; hence the statement
 * <code>mother.last</code> causes a call to <code>mother.getLast()</code> to occur.
 *
 * The following features are supported:
 * <ul>
 * <li>typical mock style of failing early
 * <li>mocks instance and class/static methods
 * <li>mocks property access using normal getters and setters
 * <li>mocks final methods and final Collaborators
 * <li>mocks Groovy and Java Collaborators (Caller must normally be Groovy but see <code>proxyDelegateInstance()</code>)
 * <li>can mock all objects of a given class (or a single Groovy object)
 * <li>mocks even if Collaborator cannot be injected into the Caller
 * <li>mocks even if Collaborator is not accessible on the Caller (no getter)
 * <li>demanded calls specified via recording calls on the Demand object (EasyMock style).
 * <li>cardinality specified as Integers or Ranges, default is 1..1; 'optional' can be achieved with 0..1
 * <li>behavior specified via Closures, allowing static or calculated return values, throwing exceptions, asserting argument values, etc. (even tricky sequence constraints by sharing state in the testMethod scope between the behavior Closures)
 * <li>matching parameter list specified via Closure's parameter list, supporting typed or untyped params, default params, and varargs.
 * <li>not dependent on any external mock library
 * <li>can mock constructors using an overloaded constructor for MockFor
 * <li>can support an instance-style mode rather than the default behaviour-style mode using <code>proxyInstance()</code> and <code>proxyDelegateInstance()</code>
 * <li>can support half-mocks using an <code>ignore</code> specification on the mock instead of a demand specification
 * <li>can mock non-existing methods if desired
 * </ul>
 *
 * See also: {@link StubFor}.
 */
class MockFor {

    MockProxyMetaClass proxy
    Demand demand
    Ignore ignore
    def expect
    Map instanceExpectations = [:]
    Class clazz

    /**
     * The optional <code>interceptConstruction</code> flag allows mocking of
     * constructor calls. These are represented in the demand specification
     * using the class name as this example shows:
     * <pre class="groovyTestCase">
     * import groovy.mock.interceptor.MockFor
     * class Person {
     *   String first, last
     * }
     * def interceptConstructorCalls = true
     * def mock = new MockFor(Person, interceptConstructorCalls)
     * def dummy = new Person(first:'Tom', last:'Jones')
     * mock.demand.with {
     *   Person() { dummy } // expect constructor call, return dummy
     *   getFirst() {'John'}
     *   getLast() {'Doe'}
     * }
     * mock.use {
     *   def p = new Person(first:'Mary', last:'Smith')
     *   assert p.first == 'John'
     *   assert p.last == 'Doe'
     * }
     * </pre>
     */
    MockFor(Class clazz, boolean interceptConstruction = false) {
        if (interceptConstruction && !GroovyObject.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("MockFor with constructor interception enabled is only allowed for Groovy objects but found: " + clazz.name)
        }
        this.clazz = clazz
        proxy = MockProxyMetaClass.make(clazz, interceptConstruction)
        demand = new Demand()
        ignore = new Ignore(parent:this)
        expect = new StrictExpectation(demand)
        proxy.interceptor = new MockInterceptor(expectation: expect)
    }

    /**
     * Identifies the Closure where the mocked collaborator behavior
     * will be applied and verified.
     */
    void use(Closure closure) {
        proxy.use closure
        expect.verify()
    }

    void use(GroovyObject obj, Closure closure) {
        proxy.use obj, closure
        expect.verify()
    }

    /**
     * If manual verification is required
     */
    void verify(GroovyObject obj) {
        instanceExpectations[obj].verify()
    }

    /**
     * Allows particular method calls to be ignored and not treated as part of
     * the required behavior specification. If you don't specify a return closure
     * the method call will fall through to the underlying instance, i.e. half-mock style.
     * The <code>filter</code> object is invoked using the normal Groovy <code>isCase()</code> semantics.
     *
     * Here are some examples:
     * <pre class="groovyTestCase">
     * import groovy.mock.interceptor.MockFor
     * class Person {
     *   String first, last
     *   def name() { "$first $last" }
     *   def ignoreMe() { 'baz' }
     *   def ignoreMeToo() { ignoreMe() }
     *   def ignoreMeThree() { ignoreMe() }
     * }
     * def mock = new MockFor(Person)
     * mock.ignore(~'get.*')
     * mock.ignore('ignoreMeToo') { 'boo' }
     * mock.ignore(~'ignoreMe.*')
     * mock.demand.name{ 'John' }
     * mock.use {
     *   def p = new Person(first:'Mary', last:'Smith')
     *   assert p.first == 'Mary'
     *   assert p.last == 'Smith'
     *   assert p.name() == 'John'
     *   assert p.ignoreMe() == 'baz'
     *   assert p.ignoreMeToo() == 'boo'
     *   assert p.ignoreMeThree() == 'baz'
     * }
     * </pre>
     * There is also a convenience form of ignore that matches the same style as
     * demand. E.g. instead of <code>mock.ignore('hasNext')</code> you can use
     * <code>mock.ignore.hasNext()</code>. A Closure variation is also provided.
     * This convenience shorthand only applies to the <code>String</code> form of ignore
     * and cannot be used with methods from <code>java.lang.Object</code>.
     *
     * Be careful using this feature while mocking some of the fundamental Java
     * classes like <code>String</code> or <code>Pattern</code>. As these are used within the
     * implementation of the ignore capability, strange behavior may be observed.
     */
    def ignore(Object filter, Closure filterBehavior = null) {
        // if Mocking Strings, attempt not to also match Strings with filter
        if (clazz.name == 'java.lang.String' && filter instanceof String) {
            filter = Pattern.compile(filter)
        }
        demand.ignore.put(filter, filterBehavior ?: MockProxyMetaClass.FALL_THROUGH_MARKER)
    }

    /**
     * Allows a more traditional instance-style mocking paradigm. This is the
     * recommended method to call to use the instance-style with Groovy classes.
     *
     * When mocking interfaces or abstract classes, a compatible proxy instance
     * will be returned. When mocking Java classes, a compatible Groovy
     * class will be generated and proxy instance returned. A MockProxyMetaClass
     * will be instantiated for the original class.
     *
     * Typical example:
     * <pre class="groovyTestCase">
     * import groovy.mock.interceptor.MockFor
     *
     * class Person {
     *   String first, last
     * }
     *
     * class Family {
     *   Person mother, father
     *   String nameOfMother() { fullName(mother) }
     *   String nameOfFather() { fullName(father) }
     *   private fullName(p) { "$p.first $p.last" }
     * }
     *
     * def mock = new MockFor(Person)
     * mock.demand.with {
     *   getFirst{ 'dummy' }
     *   getLast{ 'name' }
     * }
     * Person john = mock.proxyInstance()
     * Person mary = mock.proxyInstance()
     * Family f = new Family(father:john, mother:mary)
     * assert f.nameOfFather() == 'dummy name'
     * assert f.nameOfMother() == 'dummy name'
     * [john, mary].each{ mock.verify(it) }
     * </pre>
     * Normally for mocks, <code>verify()</code> is call automatically at the end of the "use" Closure,
     * but with this style, no "use" Closure is present, so <code>verify()</code> must be called manually.
     */
    GroovyObject proxyInstance(args=null) {
        makeProxyInstance(args, false)
    }

    /**
     * Allows a more traditional instance-style mocking paradigm. This is the
     * recommended method to call to use the instance-style with Java classes.
     *
     * When mocking interfaces or abstract classes, a compatible proxy instance
     * will be returned. When mocking Java classes, a compatible Groovy
     * class will be generated and proxy instance returned. A MockProxyMetaClass
     * will be instantiated for the class of the instance (i.e. may be on the
     * generated class not the original class).
     */
    GroovyObject proxyDelegateInstance(args=null) {
        makeProxyInstance(args, true)
    }

    GroovyObject makeProxyInstance(args, boolean isDelegate) {
        def instance = getInstance(clazz, args)
        def thisproxy = MockProxyMetaClass.make(isDelegate ? instance.getClass() : clazz)
        def thisdemand = new Demand(recorded: new ArrayList(demand.recorded), ignore: new HashMap(demand.ignore))
        def thisexpect = new StrictExpectation(thisdemand)
        thisproxy.interceptor = new MockInterceptor(expectation: thisexpect)
        instance.metaClass = thisproxy
        def wrapped = instance
        if (isDelegate && clazz.isInterface()) {
            wrapped = ProxyGenerator.INSTANCE.instantiateDelegate([clazz], instance)
        }
        instanceExpectations[wrapped] = thisexpect
        return wrapped
    }

    static GroovyObject getInstance(Class clazz, args) {
        GroovyObject instance = null
        if (clazz.isInterface()) {
            instance = ProxyGenerator.INSTANCE.instantiateAggregateFromInterface(clazz)
        } else if (Modifier.isAbstract(clazz.modifiers)) {
            instance = ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(clazz, args)
        } else if (args != null) {
            if (GroovyObject.isAssignableFrom(clazz)) {
                instance = clazz.newInstance(args)
            } else {
                instance = ProxyGenerator.INSTANCE.instantiateDelegate(clazz.newInstance(args))
            }
        } else {
            if (GroovyObject.isAssignableFrom(clazz)) {
                instance = clazz.newInstance()
            } else {
                instance = ProxyGenerator.INSTANCE.instantiateDelegate(clazz.newInstance())
            }
        }
        return instance
    }

}