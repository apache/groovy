package groovy.jmx.builder.vm5

import javax.management.ObjectName
import groovy.jmx.builder.JmxMetaMapBuilder

class JmxMetaMapBuilderTest extends GroovyTestCase {

  void testBuildAttributeMapFromObject() {
    def object = new MockManagedGroovyObject()
    def attribs = JmxMetaMapBuilder.buildAttributeMapFrom(object)

    assert attribs

    assert attribs.Id.type == "int"
    assert attribs.Id.readable == true
    assert attribs.Id.writable == false
    assert attribs.Id.getMethod == "getId"

    Map m = attribs.Name
    assert m
    assert m.type == "java.lang.String"
    assert m.readable == true
    assert m.writable == false
    assert m.getMethod == "getName"

    m = attribs.Location
    assert m
    assert m.type == "java.lang.Object"
    assert m.readable == true
    assert m.writable == false
    assert m.getMethod == "getLocation"

  }

  void testBuildAttributeMapFromDescriptorMap() {
    def object = new MockManagedObject()
    def attribs = JmxMetaMapBuilder.buildAttributeMapFrom(object, [
            something: "*",
            somethingElse: [desc: "somethingElse", readable: false, writable: true]
    ])

    assert attribs

    assert attribs.Something
    assert attribs.Something.type == "java.lang.String"
    assert attribs.Something.readable
    assert attribs.Something.getMethod == "getSomething"
    assert !attribs.Something.writable
    assert attribs.Something.setMethod == null

    assert attribs.SomethingElse
    assert attribs.SomethingElse.type == "int"
    assert !attribs.SomethingElse.readable
    assert attribs.SomethingElse.getMethod == null
    assert attribs.SomethingElse.writable
    assert attribs.SomethingElse.setMethod == "setSomethingElse"

  }


  void testBuildAttributeMapFromDescriptorList() {
    def object = new MockManagedObject()
    def attribs = JmxMetaMapBuilder.buildAttributeMapFrom(object, ["something", "somethingElse"])

    assert attribs

    assert attribs.Something
    assert attribs.Something.type == "java.lang.String"
    assert attribs.Something.readable
    assert attribs.Something.getMethod == "getSomething"
    assert !attribs.Something.writable
    assert attribs.Something.setMethod == null

    assert attribs.SomethingElse
    assert attribs.SomethingElse.type == "int"
    assert attribs.SomethingElse.readable
    assert attribs.SomethingElse.getMethod == "getSomethingElse"
    assert !attribs.SomethingElse.writable
    assert attribs.SomethingElse.setMethod == null
  }

  void TODO_testBuildConstructorMapFromObject() {
    def object = new MockManagedObject()
    def ctors = JmxMetaMapBuilder.buildConstructorMapFrom(object)

    assert ctors

    assert ctors."MockManagedObject@0".name == "groovy.jmx.builder.test.MockManagedObject"
    assert ctors."MockManagedObject@0".role == "constructor"

    assert ctors."MockManagedObject@1".name == "groovy.jmx.builder.test.MockManagedObject"
    assert ctors."MockManagedObject@1".role == "constructor"

    assert ctors."MockManagedObject@2".name == "groovy.jmx.builder.test.MockManagedObject"
    assert ctors."MockManagedObject@2".role == "constructor"

    Map m = getCtorMapByParamSize(ctors, 2)
    assert m
    assert m.params.size() == 2
    assert m.params."java.lang.String"
    assert m.params."java.lang.String".name == "java.lang.String"
    assert m.params."java.lang.String".type == String.class
    assert m.params."int".name == "int"
    assert m.params."int".type == Integer.TYPE
  }

  void TODO_testBuildConstructorMapFromDescriptor() {
    def object = new MockManagedObject()

    def map = JmxMetaMapBuilder.buildConstructorMapFrom(object, "*")
    assert map
    assert map.size() == 3
    assert map."MockManagedObject@0".name == "groovy.jmx.builder.test.MockManagedObject"
    assert map."MockManagedObject@0".role == "constructor"

    map = JmxMetaMapBuilder.buildConstructorMapFrom(object, ["ctor1": []])
    assert map
    assert map.size() == 1
    assert map."ctor1".params.size() == 0

    map = JmxMetaMapBuilder.buildConstructorMapFrom(object, [
            "ctor1": [],
            "ctor2": [desc: "Ctor2 description", params: ["java.lang.String", "int"]],
            "ctor3": [desc: "Ctor3 description",
                    params: ["java.lang.String": [name: "quantity", "desc": "Initial Value"]]
            ]
    ])
    assert map
    assert map."ctor2".params.size() == 2
    assert map."ctor2".params."int".type == int.class
    assert map."ctor2".params."java.lang.String".type == String.class
    assert map."ctor3".params."java.lang.String".name == "quantity"

    map = JmxMetaMapBuilder.buildConstructorMapFrom(object, [
            "ctor2": [desc: "Ctor2 description", params: ["java.lang.String": "*", "int": "*"]],
            "ctor3": [desc: "Ctor3 description",
                    params: ["java.lang.String": [name: "quantity", "desc": "Initial Value"]]
            ]
    ])

    assert map."ctor2".params.size() == 2
    assert map."ctor2".params."int".type == int.class

  }



  void testBuildOperationMapFromObject() {
    def object = new MockManagedObject()
    def map = JmxMetaMapBuilder.buildOperationMapFrom(object)


    assert map
    assert map."doSomethingElse".name == "doSomethingElse"
    assert map."doSomethingElse".displayName
    assert map."doSomethingElse".method
    assert map."doSomethingElse".params.size() == 2
    assert map."doSomethingElse".params."int".name == "int"
    assert map."doSomethingElse".params."int".displayName
    assert map."doSomethingElse".params."java.lang.String".name == "java.lang.String"
    assert map."doSomethingElse".params."java.lang.String".displayName
  }

  void testBuildOperationFromDescriptorMap() {
    def object = new MockManagedObject()
    def map = JmxMetaMapBuilder.buildOperationMapFrom(object, "*")
    assert map
    assert map."doSomething"
    assert map."doSomethingElse"
    assert map."dontDoThis"

    map = JmxMetaMapBuilder.buildOperationMapFrom(object, ["doSomething", "dontDoThis"])
    assert map
    assert map.size() == 2
    assert map."doSomething"
    assert !map."doSomethingElse"
    assert map."dontDoThis"

    map = JmxMetaMapBuilder.buildOperationMapFrom(object, ["doSomethingElse": "*"])
    assert map
    assert map.size() == 1
    assert map."doSomethingElse"
    assert map."doSomethingElse".params

    map = JmxMetaMapBuilder.buildOperationMapFrom(object, [
            "doSomething": "*",
            "dontDoThis": ["java.lang.Object"],
            "doSomethingElse": [
                    desc: "This is doSomethingElse",
                    params: [
                            "int": [desc: "Quantity"],
                            "String": "*"
                    ]
            ]
    ])

    assert map
    assert map."doSomethingElse"
    assert map."doSomethingElse".displayName
    assert map."doSomethingElse".params
    assert map.doSomethingElse.params.size() == 2
    assert map.doSomethingElse.params."java.lang.String"

  }




  void testBuildParameterMapFromConstructor() {
    def object = new MockManagedObject()
    def ctor
    object.class.getDeclaredConstructors().each {c ->
      if (c.getParameterTypes().size() == 2) {
        ctor = c
      }
    }

    assert ctor

    def map = JmxMetaMapBuilder.buildParameterMapFrom(ctor)
    assert map
    assert map.keySet().size() == 2
    ctor.getParameterTypes().each {c ->
      assert map."${c.name}"
      assert map."${c.name}".name == c.name
      assert map."${c.name}".displayName
    }

    map = JmxMetaMapBuilder.buildParameterMapFrom(ctor, [
            "java.lang.String": [name: "place", desc: "the location to execute"],
            "int": "*"
    ])

    assert map
    assert map."java.lang.String"
    assert map."java.lang.String".type.name == "java.lang.String"
    assert map."java.lang.String".name == "place"
    assert map."int"
    assert map."int".name == "int"
    assert map."int".type.name == "int"

    map = JmxMetaMapBuilder.buildParameterMapFrom(ctor, ["java.lang.String", "int"])
    assert map

    assert map
    assert map."java.lang.String"
    assert map."java.lang.String".type.name == "java.lang.String"
    assert map."java.lang.String".name == "java.lang.String"
    assert map."int"
    assert map."int".name == "int"
    assert map."int".type.name == "int"

  }

  void testBuildParameterMapFromMethod() {
    def object = new MockManagedObject()
    def method
    object.metaClass.getMethods().each {m ->
      if (m.getParameterTypes().size() == 2) {
        method = m
      }
    }
    assert method
    def map = JmxMetaMapBuilder.buildParameterMapFrom(method)
    assert map

    assert map."java.lang.String"
    assert map.keySet().size() == 2
    method.getParameterTypes().each {c ->
      assert map."${c.name}"
      assert map."${c.name}".name == c.name
      assert map."${c.name}".displayName
    }

    map = JmxMetaMapBuilder.buildParameterMapFrom(method, [
            "int": "*",
            "java.lang.String": [name: "What to do", desc: "the location to execute"],
    ])

    assert map
    assert map."java.lang.String"
    assert map."java.lang.String".type.name == "java.lang.String"
    assert map."java.lang.String".name == "What to do"
    assert map."int"
    assert map."int".name == "int"
    assert map."int".type.name == "int"


    map = JmxMetaMapBuilder.buildParameterMapFrom(method, ["int", "java.lang.String"])

    assert map
    assert map."java.lang.String"
    assert map."java.lang.String".type.name == "java.lang.String"
    assert map."java.lang.String".name == "java.lang.String"
    assert map."int"
    assert map."int".name == "int"
    assert map."int".type.name == "int"
  }

  void testBuildAttributeNotificationFromDescriptor() {
    def object = new MockManagedObject()
    def map

    map = JmxMetaMapBuilder.buildAttributeMapFrom(object, [
            "something": "*",
            somethingElse: [
                    desc: "somethingElse", readable: true, writable: true,
                    onChange: {-> "event block"}
            ]
    ])

    assert map.Something
    assert !map.Something.methodListener

    assert map.SomethingElse
    assert map.SomethingElse.methodListener
    assert map.SomethingElse.methodListener.target == "setSomethingElse"
    assert map.SomethingElse.methodListener.callback instanceof Closure
  }


  void testBuildOperationNotificationFromDescriptor() {
    def object = new MockManagedObject()
    def map

    map = JmxMetaMapBuilder.buildOperationMapFrom(object, [
            "doSomething": [
                    params:[],
                    onCall: {-> "event block"}
            ]
    ])

    assert map
    assert map.doSomething.methodListener
    assert map.doSomething.methodListener.target == "doSomething"
    assert map.doSomething.methodListener.callback instanceof Closure
  }

  void testBuildListenerMap() {
    def object = new MockManagedObject()
    def map

    map = JmxMetaMapBuilder.buildListenerMapFrom(
            [
                    heartbeat: [event: "event.heartbeat", from: "some:type=object", call: {-> "event block"}],
                    timer: [event: "event.timer", from: "some:type=object", call: {-> "event block"}]
            ]
    )
    assert map
    assert map.heartbeat.event == "event.heartbeat"
    assert map.timer.event == "event.timer"
    assert map.timer.from == new ObjectName("some:type=object")
    assert map.timer.callback instanceof Closure

    shouldFail {
      assert map.heartbeat.from instanceof String
    }

  }

  private Map getCtorMapByParamSize(Map maps, int size) {
    for (m in maps) {
      if (!m.value.params && size == 0) {
        return m.value
      } else if (m.value.params && m.value.params.size() == size) {
        return m.value
      }
    }
    return null
  }


}