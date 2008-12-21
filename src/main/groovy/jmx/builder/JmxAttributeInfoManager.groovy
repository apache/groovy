package groovy.jmx.builder

import javax.management.modelmbean.DescriptorSupport
import javax.management.modelmbean.ModelMBeanAttributeInfo

class JmxAttributeInfoManager {
    /** *
     * Generates a list of ModelMBeanAttributeInfo from a list meta data Maps.
     * @param list of meta data maps
     * @return ModelMBeanAttributeInfo
     */
    public static List<ModelMBeanAttributeInfo> getAttributeInfosFromMap(Map metaMap) {
        if (!metaMap) return null

        def attribs = []
        metaMap.each {attribName, map ->
            map.name = attribName
            ModelMBeanAttributeInfo info = getAttributeInfoFromMap(map)
            attribs << info
        }

        return attribs
    }

    /** *
     * This method builds a single ModelMBeanAttribute info from a given meta data map object.
     * @param the map object containing the meta data
     * @return the generated ModelMBeanAttribute Info object.
     */
    public static ModelMBeanAttributeInfo getAttributeInfoFromMap(Map map) {
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
                (prop.type instanceof Boolean)  // is bolean
        )
        attrib.setDescriptor desc
        return attrib
    }
}