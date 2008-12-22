package groovy.jmx.builder.vm5

import javax.management.MBeanServerConnection
import javax.management.ObjectName
import groovy.jmx.builder.*

class JmxBeanFactoryTest extends GroovyTestCase {
  def builder
  MBeanServerConnection server


  void setUp() {
    builder = new JmxBuilder()
    server = builder.getMBeanServer()
    builder.registerFactory("bean", new JmxBeanFactory())
  }

  void testMetaMapValidity() {
    def object = new MockManagedObject()
    def metaMap = builder.bean(object)
    assert metaMap
    assert metaMap.name == object.class.canonicalName
    assert metaMap.target == object
    assert metaMap.jmxName.toString() == "jmx.builder:type=ExportedObject,name=${object.class.canonicalName}@${object.hashCode()}"
  }

  void testImplicitMetaMap() {
    def object = new MockManagedObject()
    def objName = "jmx.builder:type=ExportedObject,name=${object.class.canonicalName}@${object.hashCode()}"
    def map = builder.bean(object)

    assert map
  }

  void testEmbeddedBeanGeneration() {
    def object = new MockManagedGroovyObject()
    def map = builder.bean(object)

    assert map

    assert map.target == object
    assert map.name == object.class.canonicalName

    assert map.jmxName == new ObjectName("jmx.builder:type=EmbeddedObject")
    assert map.attributes.Id
    assert map.attributes.Id.type == "int"

    assert map.attributes.Location
    assert map.attributes.Location.type == "java.lang.Object"
  }

  void testAttributeMetodListeners() {
    def object = new MockManagedGroovyObject()
    def map = builder.bean(target: object, name: "jmx.builder:type=ExplitObject",
            attributes: ["Id": [onChange: {-> Hello}]]
    )

    assert map
    assert map.attributes.Id
    assert map.attributes.Id.methodListener

  }

  void testMBeanClass() {
    def object = new MockSimpleObject()
    def map = builder.bean(object)
    assert map
    assert map.isMBean
    assert map.target
    assert map.jmxName
    assert !map.attributes
    assert !map.constructors
    assert !map.operations
  }

}