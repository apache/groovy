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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy3424Bug extends GroovyTestCase {

    MetaClassRegistry registry
    MetaClass originalMetaClass

    void setUp() {
        registry = GroovySystem.metaClassRegistry
        originalMetaClass = registry.getMetaClass(Groovy3424Foo)
    }

    void tearDown() {
        registry.setMetaClass(Groovy3424Foo, originalMetaClass)
    }

    void testCallSiteShouldBeUpdatedAfterProxyMetaClassIsSet() {
        def newFoo = { -> new Groovy3424Foo() }

        assert new Groovy3424Foo() instanceof Groovy3424Foo
        assert newFoo() instanceof Groovy3424Foo

        MetaClass mc = new Groovy3424ClassProxyMetaClass(registry, Groovy3424Foo, originalMetaClass)
        registry.setMetaClass(Groovy3424Foo, mc)

        assert 'constructor' == new Groovy3424Foo()
        assert 'constructor' == newFoo()
    }

    void testExpandoCallSiteShouldBeUpdatedAfterProxyMetaClassIsSet() {
        Groovy3424Foo.metaClass.constructor << { String test -> 'foo' }

        def newFoo = { -> new Groovy3424Foo('test') }

        assert 'foo' == new Groovy3424Foo('test')
        assert 'foo' == newFoo()

        MetaClass mc = new Groovy3424ClassProxyMetaClass(registry, Groovy3424Foo, originalMetaClass)
        registry.setMetaClass(Groovy3424Foo, mc)

        assert 'constructor' == new Groovy3424Foo('test')
        assert 'constructor' == newFoo()
    }

    void testCallSiteShouldBeUpdatedAfterOriginalMetaClassIsRestored() {
        def newFoo = { -> new Groovy3424Foo() }

        MetaClass mc = new Groovy3424ClassProxyMetaClass(registry, Groovy3424Foo, originalMetaClass)
        registry.setMetaClass(Groovy3424Foo, mc)

        assert 'constructor' == new Groovy3424Foo()
        assert 'constructor' == newFoo()

        registry.setMetaClass(Groovy3424Foo, originalMetaClass)

        assert new Groovy3424Foo() instanceof Groovy3424Foo
        assert newFoo() instanceof Groovy3424Foo
    }

}

class Groovy3424Foo {}

class Groovy3424ClassProxyMetaClass extends ProxyMetaClass {
    Groovy3424ClassProxyMetaClass(MetaClassRegistry metaClassRegistry, Class aClass, MetaClass adaptee) {
        super(metaClassRegistry, aClass, adaptee)
    }
    public Object invokeConstructor(final Object[] arguments) {
        'constructor'
    }
}
