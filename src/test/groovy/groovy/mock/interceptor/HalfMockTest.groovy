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

import groovy.test.GroovyTestCase

class HalfMockTest extends GroovyTestCase {

    void setUp() {
        Baz.constructorCalls = 0
        Baz.staticExistsCalls = 0
        Baz.existsCalls = 0
    }

    void testCallsConstructorOfMockedObject() {
        def mock = new MockFor(Baz)
        mock.use {
            def baz = new Baz()
        }
        assert Baz.constructorCalls == 1
    }

    void testMocksNonExistingMethods() {
        def mock = new MockFor(Baz)
        mock.demand.doesntExist() { 'testMocksNonExistingMethods' }
        mock.use {
            def baz = new Baz()
            assert baz.doesntExist() == 'testMocksNonExistingMethods'
        }
    }

    void testCallsExistingMethodsIfIgnored() {
        def mock = new MockFor(Baz)
        mock.ignore('exists')
        mock.use {
            def baz = new Baz()
            baz.exists()
        }
        assert Baz.existsCalls == 1
    }

    void testMocksExistingMethods() {
        def mock = new MockFor(Baz)
        mock.demand.exists() { 'testMocksExistingMethods' }
        mock.use {
            def baz = new Baz()
            assert baz.exists() == 'testMocksExistingMethods'
        }
        assert Baz.existsCalls == 0
    }

    void testMocksNonExistingStaticMethods() {
        def mock = new MockFor(Baz)
        mock.demand.staticDoesntExist() { 'testMocksNonExistingStaticMethods' }
        mock.use {
            def baz = new Baz()
            assert Baz.staticDoesntExist() == 'testMocksNonExistingStaticMethods'
        }
    }

    void testCallsExistingStaticMethodsIfIgnored() {
        def mock = new MockFor(Baz)
        mock.ignore('staticExists')
        mock.use {
            def baz = new Baz()
            Baz.staticExists()
        }
        assert Baz.staticExistsCalls == 1
    }

    void testMocksNonExistingProperties() {
        def mock = new MockFor(Baz)
        mock.demand.setNonExistingProperty() {}
        mock.demand.getNonExistingProperty() {2}
        mock.use {
            def baz = new Baz()
            baz.nonExistingProperty = 1
            assert baz.nonExistingProperty == 2
        }
    }

    void testAccessesExistingPropertiesIfIgnored() {
        def mock = new MockFor(Baz)
        mock.ignore(~'[sg]etExistingProperty')
        mock.use {
            Baz baz = new Baz()
            baz.existingProperty = 1
            assert baz.existingProperty == 1
        }
    }

    void testAccessesExistingInheritedPropertiesIfIgnored() {
        def mock = new MockFor(Bar)
        mock.ignore(~'[sg]etExistingProperty')
        mock.use {
            Baz bar = new Bar()
            bar.existingProperty = 1
            assert bar.existingProperty == 1
        }
    }

}

class Baz {

    static existsCalls = 0, staticExistsCalls = 0, constructorCalls = 0
    def existingProperty = 0

    Baz() {
        constructorCalls++
    }

    def exists() {
        existsCalls++
    }

    def callsDoesntExist() {
        doesntExist()
    }

    static void staticExists() {
        staticExistsCalls++
    }
}

class Bar extends Baz {

}
