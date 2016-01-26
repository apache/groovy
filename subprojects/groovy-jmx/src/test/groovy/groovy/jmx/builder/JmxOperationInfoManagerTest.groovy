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

import javax.management.Descriptor
import javax.management.MBeanParameterInfo
import javax.management.modelmbean.ModelMBeanConstructorInfo
import javax.management.modelmbean.ModelMBeanOperationInfo

class JmxOperationInfoManagerTest extends GroovyTestCase {

    void testGetConstructorInfoFromMap() {
        def object = new MockManagedObject()
        def maps = JmxMetaMapBuilder.buildConstructorMapFrom(object);
        assert maps

        Map m = getCtorMapByParamSize(maps, 0)
        ModelMBeanConstructorInfo info0 = JmxOperationInfoManager.getConstructorInfoFromMap(m)
        assert info0
        assert info0.name == "groovy.jmx.builder.MockManagedObject"
        assert info0.signature.size() == 0
        Descriptor desc = info0.descriptor
        assert desc
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME) == "groovy.jmx.builder.MockManagedObject"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_ROLE) == "constructor"

        m = getCtorMapByParamSize(maps, 1)
        ModelMBeanConstructorInfo info1 = JmxOperationInfoManager.getConstructorInfoFromMap(m)
        assert info1
        assert info1.name == "groovy.jmx.builder.MockManagedObject"
        assert info1.signature.size() == 1
        MBeanParameterInfo param0 = info1.signature[0]
        assert param0.name == "java.lang.String"
        assert param0.getType() == "java.lang.String"
        desc = info1.descriptor
        assert desc
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME) == "groovy.jmx.builder.MockManagedObject"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_ROLE) == "constructor"

        m = getCtorMapByParamSize(maps, 2)
        ModelMBeanConstructorInfo info2 = JmxOperationInfoManager.getConstructorInfoFromMap(m)
        assert info2
        assert info2.name == "groovy.jmx.builder.MockManagedObject"
        assert info2.signature.size() == 2
        MBeanParameterInfo param20 = info2.signature[0]
        assert param20.name == "java.lang.String"
        assert param20.getType() == "java.lang.String"
        MBeanParameterInfo param21 = info2.signature[1]
        assert param21.name == "int"
        assert param21.getType() == "int"

        desc = info2.descriptor
        assert desc
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME) == "groovy.jmx.builder.MockManagedObject"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_ROLE) == "constructor"
    }

    void testGetConstructorInfosMap() {
        def object = new MockManagedObject()
        def maps = JmxMetaMapBuilder.buildConstructorMapFrom(object)
        assert maps

        ModelMBeanConstructorInfo[] infos = JmxOperationInfoManager.getConstructorInfosFromMap(maps)
        assert infos
        assert infos.size() == 3
    }

    void testGetOperationInfoFromMap() {
        def object = new MockManagedObject()
        def maps = JmxMetaMapBuilder.buildOperationMapFrom(object);
        assert maps

        Map m0 = maps.doSomething
        assert m0
        ModelMBeanOperationInfo info0 = JmxOperationInfoManager.getOperationInfoFromMap(m0)
        assert info0.name == "doSomething"
        assert info0.signature.size() == 0
        Descriptor desc = info0.descriptor
        assert desc
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME) == "doSomething"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"

        Map m1 = maps.doSomethingElse
        assert m1
        ModelMBeanOperationInfo info1 = JmxOperationInfoManager.getOperationInfoFromMap(m1)
        assert info1.name == "doSomethingElse"
        assert info1.signature.size() == 2
        assert info1.signature[0].type == "int"
        assert info1.signature[1].type == "java.lang.String"
        desc = info1.descriptor
        assert desc
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME) == "doSomethingElse"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"

        Map m2 = maps.dontDoThis
        assert m2
        ModelMBeanOperationInfo info2 = JmxOperationInfoManager.getOperationInfoFromMap(m2)
        assert info2.name == "dontDoThis"
        assert info2.signature.size() == 1
        assert info2.signature[0].type == "java.lang.Object"
        desc = info2.descriptor
        assert desc
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME) == "dontDoThis"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
    }

    void testGetOperationInfosFromMap() {
        def object = new MockManagedObject()
        def maps = JmxMetaMapBuilder.buildOperationMapFrom(object)
        assert maps

        ModelMBeanOperationInfo[] infos = JmxOperationInfoManager.getOperationInfosFromMap(maps)
        assert infos
        assert infos.size() > 0
    }

    void testGetDynamicOperationInfo() {
        def object = new MockManagedObject()
        // note: closure:{->} returns 0 param, but closure:{} returns 1 param.
        MockManagedObject.metaClass."dynamicMethod" = {->
            println "This is a dynamic method"
        }
        def maps = JmxMetaMapBuilder.buildOperationMapFrom(object);

        assert maps
        Map m0 = maps."dynamicMethod"
        assert m0
        ModelMBeanOperationInfo info0 = JmxOperationInfoManager.getOperationInfoFromMap(m0)
        assert info0.name == "dynamicMethod"
        assert info0.signature.size() == 0
        Descriptor desc = info0.descriptor
        assert desc
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME) == "dynamicMethod"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
        assert desc.getFieldValue(JmxBuilderTools.DESC_KEY_TYPE) == "operation"
    }

    void testCreateGetterOperationInfoFromProperty() {
        def object = new MockManagedObject()
        MetaProperty prop = object.metaClass.getMetaProperty("something")
        ModelMBeanOperationInfo op = JmxOperationInfoManager.createGetterOperationInfoFromProperty(prop)
        assert op
        assert op.name == "getSomething"
        assert op.returnType == "java.lang.String"
        assert op.signature.size() == 0
    }

    void testCreateSetterOperationInfoFromProperty() {
        def object = new MockManagedObject()
        MetaProperty prop = object.metaClass.getMetaProperty("something")
        ModelMBeanOperationInfo op = JmxOperationInfoManager.createSetterOperationInfoFromProperty(prop)
        assert op
        assert op.name == "setSomething"
        assert op.returnType == "void"
        assert op.signature.size() == 1
    }

    private Map getCtorMapByParamSize(Map maps, int size) {
        for (m in maps) {
            if (!m.value.params && size == 0) {
                return m.value
            } else if (m.value.params && m.value.params.size() == size) {
                return m.value
            }
        }
    }
}