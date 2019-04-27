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

import javax.management.modelmbean.DescriptorSupport
import javax.management.modelmbean.ModelMBeanAttributeInfo

/**
 * This class is responsible for assembling JMX Attribute Info from the meta map.
 * It cycles through the meta map from the Node and generate JMX Info objects used to
 * expose information in the MBeanServer.
 *
 * @see groovy.jmx.builder.JmxMetaMapBuilder
 */
class JmxAttributeInfoManager {
    /**
     * Generates a list of ModelMBeanAttributeInfo from a list meta data Maps.
     * @param metaMap list of meta data maps
     * @return ModelMBeanAttributeInfo
     */
    static List<ModelMBeanAttributeInfo> getAttributeInfosFromMap(Map metaMap) {
        if (!metaMap) return null

        def attribs = []
        metaMap.each { attribName, map ->
            map.name = attribName
            ModelMBeanAttributeInfo info = getAttributeInfoFromMap(map)
            attribs << info
        }

        return attribs
    }

    /**
     * This method builds a single ModelMBeanAttribute info from a given meta data map object.
     * @param map the map object containing the meta data
     * @return the generated ModelMBeanAttribute Info object.
     */
    static ModelMBeanAttributeInfo getAttributeInfoFromMap(Map map) {
        if (!map) return null

        MetaProperty prop = map.remove("property")

        if (!prop) {
            throw new JmxBuilderException("Unable generate ModelMBeanAttributeInfo, missing property object.")
        }

        DescriptorSupport desc = new DescriptorSupport()
        desc.setField(JmxBuilderTools.DESC_KEY_NAME, map.remove(JmxBuilderTools.DESC_KEY_NAME))
        desc.setField(JmxBuilderTools.DESC_KEY_TYPE, JmxBuilderTools.DESC_VAL_TYPE_ATTRIB)
        boolean isReadable = map.remove(JmxBuilderTools.DESC_KEY_READABLE) ?: true
        boolean isWritable = map.remove(JmxBuilderTools.DESC_KEY_WRITABLE) ?: false
        desc.setField JmxBuilderTools.DESC_KEY_READABLE, isReadable
        desc.setField JmxBuilderTools.DESC_KEY_WRITABLE, isWritable

        if (isReadable)
            desc.setField JmxBuilderTools.DESC_KEY_GETMETHOD, map.remove(JmxBuilderTools.DESC_KEY_GETMETHOD)
        if (isWritable)
            desc.setField JmxBuilderTools.DESC_KEY_SETMETHOD, map.remove(JmxBuilderTools.DESC_KEY_SETMETHOD)

        desc.setField("default", map.remove('defaultValue'))
        desc.setField JmxBuilderTools.DESC_KEY_DISPLAY_NAME, map.remove(JmxBuilderTools.DESC_KEY_DISPLAY_NAME)

        ModelMBeanAttributeInfo attrib = new ModelMBeanAttributeInfo(
                (String) desc.getFieldValue(JmxBuilderTools.DESC_KEY_NAME), // name
                prop.type.getName(),
                (String) desc.getFieldValue(JmxBuilderTools.DESC_KEY_DISPLAY_NAME),
                (boolean) desc.getFieldValue(JmxBuilderTools.DESC_KEY_READABLE), // readable
                (boolean) desc.getFieldValue(JmxBuilderTools.DESC_KEY_WRITABLE), // readOnly
                (prop.type instanceof Boolean)  // is boolean
        )
        attrib.setDescriptor desc
        return attrib
    }
}