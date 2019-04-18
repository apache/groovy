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
package groovy.jmx.builder

class JmxBeansFactoryTest extends GroovyTestCase {
    def builder

    void setUp() {
        super.setUp()
        builder = new JmxBuilder()
        builder.registerFactory("beans", new JmxBeansFactory())
    }

    void testFactoryNodeInstance() {
        def obj1 = new MockManagedObject()
        def obj2 = new BaseEmbeddedClass()
        def maps = builder.beans(obj1, obj2)

        assert maps
        assert maps.size() == 2

        // test MockManagedObject map
        def map = maps[0]
        assert map

        def attribs = map.attributes
        assert attribs.Something
        assert attribs.Something.type == "java.lang.String"

        assert attribs.SomethingElse
        assert attribs.SomethingElse.type == "int"

        assert attribs.Available
        assert attribs.Available.type == "boolean"

        // test MockEmbeddedObject map
        map = maps[1]
        assert map.attributes
        assert map.attributes.Id
        assert map.attributes.Id.type == "int"

        assert map.attributes.Name
        assert map.attributes.Name.type == "java.lang.String"

        assert map.attributes.Location
        assert map.attributes.Location.type == "java.lang.Object"

        assert map.attributes.Available
        assert map.attributes.Available.type == "boolean"
    }

    void testMBeanClass() {
        def object = new MockSimpleObject()
        def maps = builder.beans([object])
        assert maps
        def map = maps[0]
        assert map.isMBean
        assert map.target
        assert map.jmxName
        assert map.attributes
        assert map.constructors
        assert !map.operations
    }
}

