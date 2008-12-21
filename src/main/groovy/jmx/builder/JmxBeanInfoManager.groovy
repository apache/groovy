package groovy.jmx.builder

import javax.management.ObjectName
import javax.management.modelmbean.*

class JmxBeanInfoManager {
    /**
     * Builds a default JMX ObjectName instance using meta data from object.
     * @param object used for name
     * @return an instance of ObjectName
     */
    public static ObjectName buildDefaultObjectName(String defaultDomain, String defaultType, def object) {
        def name = "${defaultDomain}:type=${defaultType},name=${object.class.canonicalName}@${object.hashCode()}"
        return new ObjectName(name)
    }

    /**
     * Returns a ModelMBeanInfo object from the full meta map.
     * @param meta map of object
     * @return ModelMBeanInfo built from map
     */
    public static ModelMBeanInfo getModelMBeanInfoFromMap(Map map) {
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
        attributes.each {info ->
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