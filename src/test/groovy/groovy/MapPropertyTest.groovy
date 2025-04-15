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
package groovy

import groovy.test.GroovyTestCase

class MapPropertyTest extends GroovyTestCase {

    void testGetAndSetProperties() {
        def m = ['name': 'James', 'location': 'London', 'id': 1]

        assert m.name == 'James'
        assert m.location == 'London'
        assert m.id == 1

        m.name = 'Bob'
        m.location = 'Atlanta'
        m.id = 2

        assert m.name == 'Bob'
        assert m.location == 'Atlanta'
        assert m.id == 2
    }

    void testSetupAndEmptyMap() {
        def m = [:]

        m.name = 'Bob'
        m.location = 'Atlanta'
        m.id = 2

        assert m.name == 'Bob'
        assert m.location == 'Atlanta'
        assert m.id == 2
    }

    void testMapSubclassing() {
        def c = new MyClass()

        c.id = "hello"
        c.class = 1
        c.myMethod()
        assert c.id == "hello"
        assert c.class == 1
        assert c.getClass() != 1
    }

    // GROOVY-5985
    void testMapPutAtWithKeyMatchingReadOnlyProperty() {
        def map = [serialVersionUID: 123]
        assert map["serialVersionUID"] == 123
        assert map.serialVersionUID == 123

        map.put("serialVersionUID", 789)
        assert map["serialVersionUID"] == 789
        assert map.serialVersionUID == 789

        map.putAt("serialVersionUID", 333)
        assert map.serialVersionUID == 333

        map = new MyMapClassWithReadOnlyProperties()

        assert map['classVar'] == null
        assert map.@classVar == 'class var'

        map['classVar'] = 'map var'
        assert map['classVar'] == 'map var'

        assert map['instanceVar'] == null
        assert map.@instanceVar == 77

        map['instanceVar'] = 42
        assert map['instanceVar'] == 42
    }
}

class MyClass extends HashMap {
    def myMethod() {
        assert id == "hello"
        assert this.class == 1
    }
}

class MyMapClassWithReadOnlyProperties extends HashMap {
    private static final String classVar = 'class var'
    private final int instanceVar = 77
}
