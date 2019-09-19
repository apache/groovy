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

class StaticPropertyBug extends GroovyTestCase {

    MetaClassRegistry registry
    MetaClass originalMetaClass

    void setUp() {
        registry = GroovySystem.metaClassRegistry
        originalMetaClass = registry.getMetaClass(StaticPropertyFoo)
    }

    void tearDown() {
        registry.setMetaClass(StaticPropertyFoo, originalMetaClass)
    }

    void testCallSiteShouldBeUpdatedAfterProxyMetaClassIsSet() {
        def getFoo = {-> StaticPropertyFoo.bar }

        assert 'foo' == StaticPropertyFoo.bar
        assert 'foo' == getFoo()

        MetaClass mc = new StaticPropertyClassProxyMetaClass(registry, StaticPropertyFoo, originalMetaClass)
        registry.setMetaClass(StaticPropertyFoo, mc)

        assert 'static' == StaticPropertyFoo.bar
        assert 'static' == getFoo()
    }

    void testCallSiteShouldBeUpdatedAfterOriginalMetaClassIsRestored() {
        def getFoo = {-> StaticPropertyFoo.bar }

        MetaClass mc = new StaticPropertyClassProxyMetaClass(registry, StaticPropertyFoo, originalMetaClass)
        registry.setMetaClass(StaticPropertyFoo, mc)

        assert 'static' == StaticPropertyFoo.bar
        assert 'static' == getFoo()

        registry.setMetaClass(StaticPropertyFoo, originalMetaClass)

        assert 'foo' == StaticPropertyFoo.bar
        assert 'foo' == getFoo()
    }

}

class StaticPropertyFoo {
    static bar = 'foo'
}

class StaticPropertyClassProxyMetaClass extends ProxyMetaClass {
    StaticPropertyClassProxyMetaClass(MetaClassRegistry metaClassRegistry, Class aClass, MetaClass adaptee) {
        super(metaClassRegistry, aClass, adaptee)
    }

    public Object getProperty(Class aClass, Object object, String property, boolean b, boolean b1) {
        'static'
    }
}
