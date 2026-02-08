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

import javax.management.modelmbean.DescriptorSupport
import javax.management.modelmbean.ModelMBeanAttributeInfo

class JmxAttributeInfoManagerTest extends GroovyTestCase {

    void testGetAttributeInfoFromAttributeMap() {
        def object = new MockManagedObject()
        def attribs = JmxMetaMapBuilder.buildAttributeMapFrom(object)
        assert attribs

        ModelMBeanAttributeInfo info = JmxAttributeInfoManager.getAttributeInfoFromMap(attribs.Something)
        assert info
        DescriptorSupport desc = info.descriptor
        assert desc
        assert desc.getFieldValue("name") == "Something"
        assert desc.getFieldValue("readable")
        assert desc.getFieldValue("getMethod") == "getSomething"
        assert !desc.getFieldValue("writable")
        assert info.getName() == "Something"
        assert info.getType() == "java.lang.String"

        info = JmxAttributeInfoManager.getAttributeInfoFromMap(attribs.SomethingElse)
        assert info
        desc = info.descriptor
        assert desc
        assert desc.getFieldValue("name") == "SomethingElse"
        assert desc.getFieldValue("readable")
        assert desc.getFieldValue("getMethod") == "getSomethingElse"
        assert !desc.getFieldValue("writable")
        assert info.getName() == "SomethingElse"
        assert info.getType() == "int"
    }

    void testGetAttributeInfosFromAttributeMap() {
        def object = new MockManagedGroovyObject()
        def attribs = JmxMetaMapBuilder.buildAttributeMapFrom(object)
        ModelMBeanAttributeInfo[] infos = JmxAttributeInfoManager.getAttributeInfosFromMap(attribs)

        assert infos
        infos.each {info ->
            assert object.metaClass.getMetaProperty(JmxBuilderTools.uncapitalize(info.getName()))
        }
    }
}