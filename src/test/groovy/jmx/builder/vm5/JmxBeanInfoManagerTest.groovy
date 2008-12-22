package groovy.jmx.builder.vm5

import javax.management.ObjectName
import javax.management.modelmbean.ModelMBeanInfo
import groovy.jmx.builder.*

class JmxBeanInfoManagerTest extends GroovyTestCase {
  def defaultDomain
  def defaultType
  def defaultObjectName
  def object

  void setUp() {
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
    assert m.attributes.size() == 2
  }

  void TODO_testGetModelMBeanInfoFromMap() {
    def object = new MockManagedObject()
    Map m = JmxMetaMapBuilder.buildObjectMapFrom(object)
    assert m

    ModelMBeanInfo info = JmxBeanInfoManager.getModelMBeanInfoFromMap(m)
    assert info

    assert info.getAttributes().size() == 2
    assert info.getAttribute("Something").getName() == "Something"
    assert info.getAttribute("SomethingElse").getName() == "SomethingElse"

    assert info.getConstructors().size() == 3
    assert info.getConstructors()[0].getName() == "groovy.jmx.builder.test.vm5.MockManagedObject"

    assert info.getOperation("doSomething").getName() == "doSomething"
    assert info.getOperation("doSomething").getSignature().size() == 0
    assert info.getOperation("doSomethingElse").getName() == "doSomethingElse"
    assert info.getOperation("doSomethingElse").getSignature().size() == 2

  }
}