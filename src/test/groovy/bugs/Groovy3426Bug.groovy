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

class Groovy3426Bug extends GroovyTestCase {

    MetaClassRegistry registry
    MetaClass originalMetaClass

    void setUp() {
        registry = GroovySystem.metaClassRegistry
        originalMetaClass = registry.getMetaClass(Groovy3426Foo)
    }

    void tearDown() {
        registry.setMetaClass(Groovy3426Foo, originalMetaClass)
    }

    void testCallSiteShouldBeUpdatedAfterOriginalMetaClassIsRestored() {
        def getFoo = { -> Groovy3426Foo.get() }

        MetaClass mc = new Groovy3426ClassProxyMetaClass(registry, Groovy3426Foo, originalMetaClass)
        registry.setMetaClass(Groovy3426Foo, mc)

        assert 'static' == Groovy3426Foo.get()
        assert 'static' == getFoo()

        registry.setMetaClass(Groovy3426Foo, originalMetaClass)

        assert 'foo' == Groovy3426Foo.get()
        assert 'foo' == getFoo()
    }

}

class Groovy3426Foo {
    static get() { 'foo' }
}

class Groovy3426ClassProxyMetaClass extends ProxyMetaClass {
    Groovy3426ClassProxyMetaClass(MetaClassRegistry metaClassRegistry, Class aClass, MetaClass adaptee) {
        super(metaClassRegistry, aClass, adaptee)
    }
    public Object invokeStaticMethod(final Object aClass, final String method, final Object[] arguments) {
        'static'
    }
}
