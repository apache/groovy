/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package groovy.jmx.builder
import groovy.jmx.builder.*
/**
 *
 * @author vladimir
 */
class JmxBeansFactoryTest extends GroovyTestCase {
    def builder
  void setUp() {
    builder = new JmxBuilder()
    builder.registerFactory("beans", new JmxBeansFactory())
  }

    void testFactoryNodeInstance() {
        def obj1 = new MockManagedObject()
        def obj2 = new BaseEmbeddedClass()
        def maps = builder.beans(obj1,obj2)

        assert maps
        assert maps.size == 2

        // test MockManagedObject map
        def map = maps[0]
        assert map

        def attribs = map.attributes
        assert attribs.Something
        assert attribs.Something.type == "java.lang.String"

        assert attribs.SomethingElse
        assert attribs.SomethingElse.type == "int"

        assert attribs.Available
        assert attribs.Available.type == "boolean"

        // test MockEmbeddedObject map
        map = maps[1]
        assert map.attributes
        assert map.attributes.Id
        assert map.attributes.Id.type == "int"

        assert map.attributes.Name
        assert map.attributes.Name.type == "java.lang.String"

        assert map.attributes.Location
        assert map.attributes.Location.type == "java.lang.Object"

        assert map.attributes.Available
        assert map.attributes.Available.type == "boolean"
    }

  void testMBeanClass() {
    def object = new MockSimpleObject()
    def maps = builder.beans([object])
    assert maps
    def map = maps[0]
    assert map.isMBean
    assert map.target
    assert map.jmxName
    assert map.attributes
    assert map.constructors
    assert !map.operations
  }
}

