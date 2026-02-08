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

import groovy.test.GroovyTestCase

import javax.management.ObjectName

class JmxEmbeddedMetaMapBuilderTest extends GroovyTestCase {
    void testWithNoDescriptor() {
        def object = new BaseEmbeddedClass()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.attributes
        assert map.attributes.Id
        assert map.attributes.Id.type == "int"
        assert map.attributes.Id.readable
        assert map.attributes.Id.getMethod == "getId"
        assert !map.attributes.Id.writable
        assert map.attributes.Id.setMethod == null

        assert map.attributes.Name
        assert map.attributes.Name.type == "java.lang.String"
        assert map.attributes.Name.readable
        assert map.attributes.Name.getMethod == "getName"
        assert !map.attributes.Name.writable
        assert map.attributes.Name.setMethod == null

        assert map.attributes.Location
        assert map.attributes.Location.type == "java.lang.Object"
        assert map.attributes.Location.readable
        assert map.attributes.Location.getMethod == "getLocation"
        assert !map.attributes.Location.writable
        assert map.attributes.Location.setMethod == null

        assert map.attributes.Available
        assert map.attributes.Available.type == "boolean"
        assert map.attributes.Available.readable
        assert map.attributes.Available.getMethod == "isAvailable"
        assert !map.attributes.Available.writable
        assert map.attributes.Available.setMethod == null

        assert map.operations
        assert map.operations.doNothing
        assert map.operations.doNothing.name == "doNothing"
        assert map.operations.doTwoThings
        assert map.operations.doTwoThings.name == "doTwoThings"
        assert map.operations.doThreeThings
        assert map.operations.doThreeThings.name == "doThreeThings"
        assert map.operations.doThreeThings.method
        assert map.operations.doThreeThings.params.size() == 3

        map.operations.doSoemthingElse
        assert map.operations.doSomethingElse.name == "doSomethingElse"

    }

    void testEmbeddedJmxNameOnly() {
        def object = new EmbeddedNameOnly()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.jmxName == new ObjectName("jmx.builder:type=EmbeddedObject")
        assert map.name == object.getClass().name

        assert map.attributes
        assert map.attributes.Id
        assert map.attributes.Id.type == "int"
        assert map.attributes.Id.readable
        assert map.attributes.Id.getMethod == "getId"
        assert !map.attributes.Id.writable
        assert map.attributes.Id.setMethod == null

        assert map.attributes.Name
        assert map.attributes.Name.type == "java.lang.String"
        assert map.attributes.Name.readable
        assert map.attributes.Name.getMethod == "getName"
        assert !map.attributes.Name.writable
        assert map.attributes.Name.setMethod == null

        assert map.attributes.Location
        assert map.attributes.Location.type == "java.lang.Object"
        assert map.attributes.Location.readable
        assert map.attributes.Location.getMethod == "getLocation"
        assert !map.attributes.Location.writable
        assert map.attributes.Location.setMethod == null

        assert map.attributes.Available
        assert map.attributes.Available.type == "boolean"
        assert map.attributes.Available.readable
        assert map.attributes.Available.getMethod == "isAvailable"
        assert !map.attributes.Available.writable
        assert map.attributes.Available.setMethod == null

        assert map.operations
        assert map.operations.doNothing
        assert map.operations.doNothing.name == "doNothing"
        assert map.operations.doTwoThings
        assert map.operations.doTwoThings.name == "doTwoThings"
        assert map.operations.doThreeThings
        assert map.operations.doThreeThings.name == "doThreeThings"
        assert map.operations.doThreeThings.method
        assert map.operations.doThreeThings.params.size() == 3

        map.operations.doSoemthingElse
        assert map.operations.doSomethingElse.name == "doSomethingElse"
    }

    void testEmbeddedAllAttributes() {
        def object = new EmbeddedAllAttribsOnly()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.attributes
        assert !map.constructors
        assert !map.operations
        assert !map.listeners

        assert map.attributes.Id
        assert map.attributes.Id.type == "int"
        assert map.attributes.Id.readable
        assert map.attributes.Id.getMethod == "getId"
        assert !map.attributes.Id.writable
        assert map.attributes.Id.setMethod == null


        assert map.attributes.Name
        assert map.attributes.Name.type == "java.lang.String"
        assert map.attributes.Name.readable
        assert map.attributes.Name.getMethod == "getName"
        assert !map.attributes.Name.writable
        assert map.attributes.Name.setMethod == null

        assert map.attributes.Location
        assert map.attributes.Location.type == "java.lang.Object"
        assert map.attributes.Location.readable
        assert map.attributes.Location.getMethod == "getLocation"
        assert !map.attributes.Location.writable
        assert map.attributes.Location.setMethod == null

        assert map.attributes.Available
        assert map.attributes.Available.type == "boolean"
        assert map.attributes.Available.readable
        assert map.attributes.Available.getMethod == "isAvailable"
        assert !map.attributes.Available.writable
        assert map.attributes.Available.setMethod == null

    }

    void testEmbeddedAttribList() {
        def object = new EmbeddedAttribsListOnly()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.attributes
        assert !map.constructors
        assert !map.operations
        assert !map.listeners

        assert map.attributes.Id
        assert map.attributes.Id.type == "int"
        assert map.attributes.Id.readable
        assert map.attributes.Id.getMethod == "getId"
        assert !map.attributes.Id.writable
        assert map.attributes.Id.setMethod == null


        assert map.attributes.Name
        assert map.attributes.Name.type == "java.lang.String"
        assert map.attributes.Name.readable
        assert map.attributes.Name.getMethod == "getName"
        assert !map.attributes.Name.writable
        assert map.attributes.Name.setMethod == null

        assert !map.attributes.Location
        assert !map.attributes.Available

    }

    void testEmbeddedAttribDescriptor() {
        def object = new EmbeddedAttribsDescriptorOnly()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.attributes
        assert !map.constructors
        assert !map.operations
        assert !map.listeners

        assert map.attributes.Id
        assert map.attributes.Id.type == "int"
        assert map.attributes.Id.readable
        assert map.attributes.Id.getMethod == "getId"
        assert !map.attributes.Id.writable
        assert map.attributes.Id.setMethod == null


        assert map.attributes.Name
        assert map.attributes.Name.type == "java.lang.String"
        assert map.attributes.Name.displayName == "Name Description"
        assert map.attributes.Name.readable
        assert map.attributes.Name.getMethod == "getName"
        assert map.attributes.Name.writable
        assert map.attributes.Name.setMethod == "setName"

        assert !map.attributes.Location
        assert !map.attributes.Available

    }

    void testEmbeddedConstrucors() {
        def object = new EmbeddedConstructors()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map

        assert !map.attributes
        assert map.constructors
        assert !map.operations
        assert !map.listeners

        assert map.constructors.ctor1
        assert map.constructors.ctor1.displayName == "ctor1"
        assert map.constructors.ctor2
        assert map.constructors.ctor2.displayName == "ctor2"
        assert map.constructors.ctor2.params
        assert map.constructors.ctor2.params."int".name == "Id"
        assert map.constructors.ctor2.params."int".displayName == "Identification"
    }

    void testEmbeddAllOps() {
        def object = new EmbeddedAllOps()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.operations

        assert map.operations.doNothing
        assert map.operations.doNothing.name == "doNothing"

        assert map.operations.doTwoThings
        assert map.operations.doTwoThings.name == "doTwoThings"

        assert map.operations.doThreeThings
        assert map.operations.doThreeThings.name == "doThreeThings"
        assert map.operations.doThreeThings.method
        assert map.operations.doThreeThings.params.size() == 3

        map.operations.doSoemthingElse
        assert map.operations.doSomethingElse.name == "doSomethingElse"
    }

    void testEmbeddedOpsList() {
        def object = new EmbeddedOpsList()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.operations

        assert map.operations.doNothing
        assert map.operations.doThreeThings
        assert map.operations.doThreeThings.params.size() == 3

        assert !map.operations.doTwoThings
        assert !map.operations.doSomethingElse

    }

    void testEmbeddedOpsDescriptor() {
        def object = new EmbeddedOpsDescriptor()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map
        assert map.operations

        assert map.operations.doNothing
        assert map.operations.doNothing.method

        assert map.operations.doTwoThings
        assert map.operations.doTwoThings.params.size() == 2
        assert map.operations.doTwoThings.params."java.lang.Object"
        assert map.operations.doTwoThings.params."java.lang.String"

        assert map.operations.doThreeThings
        assert map.operations.doThreeThings.displayName == "Do Three Things"
        assert map.operations.doThreeThings.params.size() == 3
        assert map.operations.doThreeThings.params."java.lang.Object"
        assert map.operations.doThreeThings.params."java.lang.Object".displayName == "thing1"
        assert map.operations.doThreeThings.params."boolean"
        assert map.operations.doThreeThings.params."int"
    }

    void testEmbeddedAttribChangeListener() {
        def object = new EmbeddedAttribEventListener()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map.attributes
        assert map.attributes.Name.methodListener
        assert map.attributes.Name.methodListener.target == "setName"
        assert map.attributes.Name.methodListener.type == "attributeChangeListener"
        assert map.attributes.Name.methodListener.callback instanceof Closure

        assert map.attributes.Id.methodListener
        assert map.attributes.Id.methodListener.target == "setId"
        assert map.attributes.Id.methodListener.callback instanceof Closure
    }

    void testEmbeddedOpCallListener() {
        def object = new EmbeddedOpEventListener()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map.operations
        assert map.operations.doNothing.methodListener
        assert map.operations.doNothing.methodListener.target == "doNothing"
        assert map.operations.doNothing.methodListener.type == "operationCallListener"
        assert map.operations.doNothing.methodListener.callback instanceof Closure

        assert map.operations.doTwoThings.methodListener
        assert map.operations.doTwoThings.methodListener.target == "doTwoThings"
        assert map.operations.doTwoThings.methodListener.callback instanceof Closure
    }

    void testEmbeddedEventListener() {
        def object = new EmbeddedEventListener()
        def map = JmxMetaMapBuilder.buildObjectMapFrom(object)

        assert map.listeners
        assert map.listeners.heartbeat
        assert map.listeners.heartbeat.type == "eventListener"
        assert map.listeners.heartbeat.event == "event.heartbeat"
        assert map.listeners.heartbeat.from instanceof ObjectName
        assert map.listeners.heartbeat.from == new ObjectName("some:type=object1")
        assert map.listeners.heartbeat.callback instanceof Closure

        assert map.listeners.timer
        assert map.listeners.timer.type == "eventListener"
        assert map.listeners.timer.event == "event.timer"
        assert map.listeners.timer.from instanceof ObjectName
        assert map.listeners.timer.callback instanceof Closure
    }
}

