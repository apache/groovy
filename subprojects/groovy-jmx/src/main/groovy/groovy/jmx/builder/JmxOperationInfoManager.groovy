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
import javax.management.modelmbean.DescriptorSupport
import javax.management.modelmbean.ModelMBeanConstructorInfo
import javax.management.modelmbean.ModelMBeanOperationInfo

/**
 * This class is responsible for assembling JMX Operation Info from the meta map.
 * It cycles through the provided meta map from the bean() node and generate JMX Info objects used to
 * expose information in the MBeanServer.
 *
 * @see groovy.jmx.builder.JmxMetaMapBuilder
 */
class JmxOperationInfoManager {
    /** *
     * This method extracts an array of MBeanConstructorInfo from a list of meta maps.
     * @param list of meta maps
     * @return array of ModelMBeanConstructorInfo
     */
    static List<ModelMBeanConstructorInfo> getConstructorInfosFromMap(Map metaMap) {
        if (!metaMap) return null
        def ctors = []
        metaMap.each {ctorName, map ->
            ModelMBeanConstructorInfo info = getConstructorInfoFromMap(map)
            ctors << info
        }
        return ctors
    }

    /** *
     * This method extracts ModelMBeanConstructorInfo from provided meta map.
     * It also iterates over any parameters and builds the necessary MBeanParameterInfo array.
     * @param meta map containing descriptor information
     * @return the fully realized ModelMBeanConstructorInfo for the provided constructor reference.
     */
    static ModelMBeanConstructorInfo getConstructorInfoFromMap(Map map) {
        if (!map) return null

        def ctor = map.remove("constructor")

        if (!ctor) {
            throw new JmxBuilderException("Unable generate ModelMBeanConstructorInfo, missing constructor reference.")
        }

        MBeanParameterInfo[] params = (MBeanParameterInfo[]) buildParamInfosFromMaps(map.remove("params"))

        Descriptor desc = new DescriptorSupport()
        desc.setField(JmxBuilderTools.DESC_KEY_NAME, map.remove(JmxBuilderTools.DESC_KEY_NAME))
        desc.setField(JmxBuilderTools.DESC_KEY_TYPE, JmxBuilderTools.DESC_VAL_TYPE_OP)
        desc.setField(JmxBuilderTools.DESC_KEY_ROLE, map.remove(JmxBuilderTools.DESC_KEY_ROLE))
        desc.setField JmxBuilderTools.DESC_KEY_DISPLAY_NAME, map.remove(JmxBuilderTools.DESC_KEY_DISPLAY_NAME)

        map.each {key, value ->
            desc.setField(key, value)
        }

        ModelMBeanConstructorInfo info = new ModelMBeanConstructorInfo(
                ctor.name,
                (String) desc.getFieldValue(JmxBuilderTools.DESC_KEY_DISPLAY_NAME),
                params,
                desc
        )

        return info
    }

    /** *
     * This method extracts an array of MBeanOperationInfo from a list of meta maps.
     * @param list of meta maps
     * @return array of ModelMBeanOperationInfo
     */
    static List<ModelMBeanOperationInfo> getOperationInfosFromMap(Map metaMap) {
        if (!metaMap) return null
        def ops = []
        metaMap.each {opNames, map ->
            ModelMBeanOperationInfo info = getOperationInfoFromMap(map)
            ops << info
        }
        return ops
    }

    /** *
     * Generates a ModelMBeanOperationInfo object from a meta map provided.
     * @param the meta map for the method
     * @return the generated ModelMBeanOperationInfo
     */
    static ModelMBeanOperationInfo getOperationInfoFromMap(Map map) {
        if (!map) return null

        MetaMethod method = map.remove("method")

        if (!method) {
            throw new JmxBuilderException("Unable to generate ModelMBeanOperationInfo, missing method reference.")
        }

        MBeanParameterInfo[] params = (MBeanParameterInfo[]) buildParamInfosFromMaps(map.remove("params"))

        Descriptor desc = new DescriptorSupport()
        desc.setField(JmxBuilderTools.DESC_KEY_NAME, map.remove(JmxBuilderTools.DESC_KEY_NAME))
        desc.setField(JmxBuilderTools.DESC_KEY_TYPE, JmxBuilderTools.DESC_VAL_TYPE_OP)
        desc.setField(JmxBuilderTools.DESC_KEY_ROLE, map.remove(JmxBuilderTools.DESC_KEY_ROLE))
        desc.setField JmxBuilderTools.DESC_KEY_DISPLAY_NAME, map.remove(JmxBuilderTools.DESC_KEY_DISPLAY_NAME)

        ModelMBeanOperationInfo info = new ModelMBeanOperationInfo(
                method.name,
                (String) desc.getFieldValue(JmxBuilderTools.DESC_KEY_DISPLAY_NAME),
                params,
                method.returnType.name,
                ModelMBeanOperationInfo.ACTION,
                desc
        )

        return info
    }

    /**
     * Build an array of MBeanParameterInfo from the operation's meta map.
     * @param the meta map containing the param info
     * @return an array of JMX MBeanParameterInfo
     */
    static List<MBeanParameterInfo> buildParamInfosFromMaps(Map metaMap) {
        if (!metaMap || metaMap.size() == 0) return null
        List<MBeanParameterInfo> result = new ArrayList<MBeanParameterInfo>(metaMap.size())

        metaMap.each {paramType, param ->
            String type = (param.type instanceof String) ? JmxBuilderTools.getNormalizedType(param.type) : JmxBuilderTools.getNormalizedType(param.type.name)
            MBeanParameterInfo info = new MBeanParameterInfo(param.name, type, param.displayName)
            result << info
        }

        return result
    }

    /**
     * Returns a MBean operation info for getter operation specified by the MetaProperty.  Since no meta
     * map is provided, this method will generate all default descriptor values to describe the operation.
     * @param prop - the property object on the POGO/POJO that represents a getter
     * @return a ModelMBeanOperation info built 
     */
    static ModelMBeanOperationInfo createGetterOperationInfoFromProperty(MetaProperty prop) {
        if (prop == null) return null

        Descriptor desc = new DescriptorSupport()
        String opType = (prop.type.getName().contains("Boolean")) ? "is" : "get"
        String name = opType + JmxBuilderTools.capitalize(prop.name)

        desc.setField JmxBuilderTools.DESC_KEY_NAME, name
        desc.setField JmxBuilderTools.DESC_KEY_TYPE, JmxBuilderTools.DESC_VAL_TYPE_OP
        desc.setField JmxBuilderTools.DESC_KEY_ROLE, JmxBuilderTools.DESC_VAL_TYPE_GETTER
        desc.setField JmxBuilderTools.DESC_KEY_DISPLAY_NAME, "Getter for attribute ${prop.name}".toString()

        ModelMBeanOperationInfo op = new ModelMBeanOperationInfo(
                name, // name
                (String) desc.getFieldValue(JmxBuilderTools.DESC_KEY_DISPLAY_NAME),
                null,
                prop.type.getName(),
                ModelMBeanOperationInfo.INFO,
                desc
        )

        return op
    }

    /**
     * Returns a MBean operation info for setter operation specified by the MetaProperty.  Since
     * no meta data is provided, this method will generate all default descriptor values to describe operation.
     * @param prop - the property object on the POGO/POJO that represents a setter
     * @return a ModelMBeanOperation info built
     */
    static ModelMBeanOperationInfo createSetterOperationInfoFromProperty(MetaProperty prop) {

        Descriptor desc = new DescriptorSupport()
        String name = "set" + JmxBuilderTools.capitalize(prop.name)
        desc.setField JmxBuilderTools.DESC_KEY_NAME, name
        desc.setField JmxBuilderTools.DESC_KEY_TYPE, JmxBuilderTools.DESC_VAL_TYPE_OP
        desc.setField JmxBuilderTools.DESC_KEY_ROLE, JmxBuilderTools.DESC_VAL_TYPE_SETTER
        desc.setField JmxBuilderTools.DESC_KEY_DISPLAY_NAME, "Getter for attribute ${prop.name}".toString()

        MBeanParameterInfo[] params = (MBeanParameterInfo[]) [
                new MBeanParameterInfo(
                        "${prop.getName()}".toString(),
                        prop.type.name,
                        "Parameter for setter"
                )
        ]

        ModelMBeanOperationInfo op = new ModelMBeanOperationInfo(
                name,
                (String) desc.getFieldValue(JmxBuilderTools.DESC_KEY_DISPLAY_NAME),
                params,
                Void.TYPE.name,
                ModelMBeanOperationInfo.INFO,
                desc
        )

        return op
    }

}