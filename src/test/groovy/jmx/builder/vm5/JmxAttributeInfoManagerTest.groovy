package groovy.jmx.builder.vm5

import javax.management.modelmbean.DescriptorSupport
import javax.management.modelmbean.ModelMBeanAttributeInfo
import groovy.jmx.builder.*

class JmxAttributeInfoManagerTest extends GroovyTestCase {
  def setup() {
  }

  void testGetAttributeInfoFromAttributeMap() {
    def object = new MockManagedObject()
    def attribs = JmxMetaMapBuilder.buildAttributeMapFrom(object)
    assert attribs

    ModelMBeanAttributeInfo info = JmxAttributeInfoManager.getAttributeInfoFromMap(attribs.Something)
    assert info
    DescriptorSupport desc = info.descriptor
    assert desc
    assert desc.getFieldValue("name") == "Something"
    assert desc.getFieldValue("readable") == true
    assert desc.getFieldValue("getMethod") == "getSomething"
    assert desc.getFieldValue("writable") == false
    assert info.getName() == "Something"
    assert info.getType() == "java.lang.String"

    info = JmxAttributeInfoManager.getAttributeInfoFromMap(attribs.SomethingElse)
    assert info
    desc = info.descriptor
    assert desc
    assert desc.getFieldValue("name") == "SomethingElse"
    assert desc.getFieldValue("readable") == true
    assert desc.getFieldValue("getMethod") == "getSomethingElse"
    assert desc.getFieldValue("writable") == false
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