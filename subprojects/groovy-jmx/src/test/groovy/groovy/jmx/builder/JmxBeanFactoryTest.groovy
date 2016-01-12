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

import javax.management.MBeanServerConnection
import javax.management.ObjectName

class JmxBeanFactoryTest extends GroovyTestCase {
    def builder
    MBeanServerConnection server

    void setUp() {
        super.setUp()
        builder = new JmxBuilder()
        server = builder.getMBeanServer()
        builder.registerFactory("bean", new JmxBeanFactory())
    }

    void testMetaMapValidity() {
        def object = new MockManagedObject()
        def metaMap = builder.bean(object)
        assert metaMap
        assert metaMap.name == object.class.canonicalName
        assert metaMap.target == object
        assert metaMap.jmxName.toString() == "jmx.builder:type=ExportedObject,name=${object.class.canonicalName}@${object.hashCode()}"
    }

    void testImplicitMetaMap() {
        def object = new MockManagedObject()
        def objName = "jmx.builder:type=ExportedObject,name=${object.class.canonicalName}@${object.hashCode()}"

        def map = builder.bean(object)

        assert map
        assert map.jmxName.toString() == objName
    }

    void testEmbeddedBeanGeneration() {
        def object = new MockManagedGroovyObject()
        def map = builder.bean(object)

        assert map

        assert map.target == object
        assert map.name == object.class.canonicalName

        assert map.jmxName == new ObjectName("jmx.builder:type=EmbeddedObject")
        assert map.attributes.Id
        assert map.attributes.Id.type == "int"

        assert map.attributes.Location
        assert map.attributes.Location.type == "java.lang.Object"
    }

    void testAttributeMethodListeners() {
        def object = new MockManagedGroovyObject()
        def map = builder.bean(target: object, name: "jmx.builder:type=ExplicitObject",
                attributes: ["Id": [onChange: {-> Hello}]]
        )

        assert map
        assert map.attributes.Id
        assert map.attributes.Id.methodListener
    }

    void testMBeanClass() {
        def object = new MockSimpleObject()
        def map = builder.bean(object)
        assert map
        assert map.isMBean
        assert map.target
        assert map.jmxName
        assert map.attributes
        assert map.constructors
        assert !map.operations
    }
}