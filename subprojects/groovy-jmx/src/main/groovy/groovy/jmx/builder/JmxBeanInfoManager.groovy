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
import javax.management.modelmbean.DescriptorSupport
import javax.management.modelmbean.ModelMBeanAttributeInfo
import javax.management.modelmbean.ModelMBeanConstructorInfo
import javax.management.modelmbean.ModelMBeanInfo
import javax.management.modelmbean.ModelMBeanInfoSupport
import javax.management.modelmbean.ModelMBeanNotificationInfo
import javax.management.modelmbean.ModelMBeanOperationInfo

/**
 * The JmxBeanInfoManager creates fully-described model mbean info object using the underlying meta map.
 * The MBeanInfo object is used to provide description about the actual exported MBean instance.
 */
class JmxBeanInfoManager {
    /**
     * Builds a default JMX ObjectName instance using meta data from object.
     * @param object used for name
     * @return an instance of ObjectName
     */
    static ObjectName buildDefaultObjectName(String defaultDomain, String defaultType, def object) {
        def name = "${defaultDomain}:type=${defaultType},name=${object.class.canonicalName}@${object.hashCode()}"
        return new ObjectName(name)
    }

    /**
     * Returns a fully-realized ModelMBeanInfo object from info gathered from the associated meta map.
     * @param map map of object
     * @return ModelMBeanInfo built from map
     */
    static ModelMBeanInfo getModelMBeanInfoFromMap(Map map) {
        if (!map) {
            throw new JmxBuilderException("Unable to create default ModelMBeanInfo, missing meta map.")
        }
        def object = map.target
        if (!object) {
            throw new JmxBuilderException("Unable to create default ModelMBeanInfo, missing target object.")
        }

        def attributes = JmxAttributeInfoManager.getAttributeInfosFromMap(map.attributes)
        def operations = JmxOperationInfoManager.getOperationInfosFromMap(map.operations) ?: []

        //generate setters/getters operations for found attribs
        attributes.each { info ->
            MetaProperty prop = object.metaClass.getMetaProperty(JmxBuilderTools.uncapitalize(info.name))
            if (prop && info.isReadable()) {
                operations << JmxOperationInfoManager.createGetterOperationInfoFromProperty(prop)
            }
            if (prop && info.isWritable()) {
                operations << JmxOperationInfoManager.createSetterOperationInfoFromProperty(prop)
            }
        }

        ModelMBeanAttributeInfo[] attribs = attributes
        ModelMBeanConstructorInfo[] ctors = JmxOperationInfoManager.getConstructorInfosFromMap(map.constructors)
        ModelMBeanOperationInfo[] ops = operations

        //todo add notification
        //ModelMBeanNotificationInfo[] notes = (ModelMBeanNotificationInfo[]) ctx.notifications.toArray()
        ModelMBeanNotificationInfo[] notes = null

        DescriptorSupport desc = new DescriptorSupport()
        desc.setField(JmxBuilderTools.DESC_KEY_TYPE, JmxBuilderTools.DESC_VAL_TYPE_MBEAN)
        desc.setField(JmxBuilderTools.DESC_KEY_DISPLAY_NAME, map.displayName)
        desc.setField JmxBuilderTools.DESC_KEY_NAME, map.name

        new ModelMBeanInfoSupport(
                (String) object.getClass().name,
                (String) desc.getFieldValue(JmxBuilderTools.DESC_KEY_DISPLAY_NAME),
                attribs,
                ctors,
                ops,
                notes,
                desc
        )
    }
}