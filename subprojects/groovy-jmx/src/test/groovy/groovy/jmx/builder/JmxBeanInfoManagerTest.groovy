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

import javax.management.ObjectName
import javax.management.modelmbean.ModelMBeanInfo

class JmxBeanInfoManagerTest extends GroovyTestCase {
    def defaultDomain
    def defaultType
    def defaultObjectName
    def object

    void setUp() {
        super.setUp()
        object = new MockManagedObject()
        defaultDomain = "jmx.builder"
        defaultType = "ExportedObject"
        defaultObjectName = "${defaultDomain}:type=${defaultType},name=${object.class.canonicalName}@${object.hashCode()}"
    }

    void testGetDefaultJmxObjectName() {
        def name = JmxBeanInfoManager.buildDefaultObjectName(defaultDomain, defaultType, object)
        assert name instanceof ObjectName
        def expectedName = defaultObjectName
        assert name.toString() == expectedName
    }

    void testGetDefaultMBeanMap() {
        Map m = JmxMetaMapBuilder.buildObjectMapFrom(object)
        assert m
        assert m.target == object
        assert m.name == object.getClass().name
        assert m.displayName
        assert m.constructors.size() == 3
        assert m.attributes.size() == 3
    }

    void testGetModelMBeanInfoFromMap() {
        def object = new MockManagedObject()
        Map m = JmxMetaMapBuilder.buildObjectMapFrom(object)
        assert m

        ModelMBeanInfo info = JmxBeanInfoManager.getModelMBeanInfoFromMap(m)
        assert info

        assert info.getAttributes().size() == 3
        assert info.getAttribute("Something").getName() == "Something"
        assert info.getAttribute("SomethingElse").getName() == "SomethingElse"

        assert info.getConstructors().size() == 3
        assert info.getConstructors()[0].getName() == "groovy.jmx.builder.MockManagedObject"

        assert info.getOperation("doSomething").getName() == "doSomething"
        assert info.getOperation("doSomething").getSignature().size() == 0
        assert info.getOperation("doSomethingElse").getName() == "doSomethingElse"
        assert info.getOperation("doSomethingElse").getSignature().size() == 2
    }
}