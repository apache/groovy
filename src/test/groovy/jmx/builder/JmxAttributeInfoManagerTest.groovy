package groovy.jmx.builder

import groovy.jmx.builder.JmxAttributeInfoManager
import groovy.jmx.builder.JmxBuilderTools
import groovy.jmx.builder.JmxMetaMapBuilder
import groovy.jmx.builder.MockManagedGroovyObject
import groovy.jmx.builder.test.MockManagedObject
import javax.management.modelmbean.DescriptorSupport
import javax.management.modelmbean.ModelMBeanAttributeInfo

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