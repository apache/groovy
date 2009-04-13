/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.jmx.builder

import javax.management.ObjectName
import groovy.jmx.builder.JmxBuilderTools
import groovy.jmx.builder.JmxBuilderModelMBean
import groovy.jmx.builder.MockManagedObject
import groovy.jmx.builder.MockManagedGroovyObject
import groovy.jmx.builder.MockJmxListener

class JmxBuilderToolsTest extends GroovyTestCase {
  void testCapitlize() {
    assert JmxBuilderTools.capitalize("test") == "Test"
  }

  void testUncapitlize() {
    assert JmxBuilderTools.uncapitalize("Test") == "test"
  }

  void testGetMBeanServer() {
    def server = JmxBuilderTools.getMBeanServer()
    assert server
  }

  void testGetNormalizedType() {
    assert JmxBuilderTools.getNormalizedType("byte") == "byte"
    assert JmxBuilderTools.getNormalizedType("Byte") == "java.lang.Byte"
    assert JmxBuilderTools.getNormalizedType("java.lang.Byte") == "java.lang.Byte"

    assert JmxBuilderTools.getNormalizedType("short") == "short"
    assert JmxBuilderTools.getNormalizedType("Short") == "java.lang.Short"
    assert JmxBuilderTools.getNormalizedType("java.lang.Short") == "java.lang.Short"

    assert JmxBuilderTools.getNormalizedType("char") == "int"
    assert JmxBuilderTools.getNormalizedType("character") == "java.lang.Character"
    assert JmxBuilderTools.getNormalizedType("Character") == "java.lang.Character"
    assert JmxBuilderTools.getNormalizedType("java.lang.Character") == "java.lang.Character"

    assert JmxBuilderTools.getNormalizedType("int") == "int"
    assert JmxBuilderTools.getNormalizedType("integer") == "java.lang.Integer"
    assert JmxBuilderTools.getNormalizedType("Integer") == "java.lang.Integer"
    assert JmxBuilderTools.getNormalizedType("java.lang.Integer") == "java.lang.Integer"

    assert JmxBuilderTools.getNormalizedType("long") == "long"
    assert JmxBuilderTools.getNormalizedType("Long") == "java.lang.Long"
    assert JmxBuilderTools.getNormalizedType("java.lang.Long") == "java.lang.Long"

    assert JmxBuilderTools.getNormalizedType("float") == "float"
    assert JmxBuilderTools.getNormalizedType("Float") == "java.lang.Float"
    assert JmxBuilderTools.getNormalizedType("java.lang.Float") == "java.lang.Float"

    assert JmxBuilderTools.getNormalizedType("double") == "double"
    assert JmxBuilderTools.getNormalizedType("Double") == "java.lang.Double"
    assert JmxBuilderTools.getNormalizedType("java.lang.Double") == "java.lang.Double"

    assert JmxBuilderTools.getNormalizedType("boolean") == "boolean"
    assert JmxBuilderTools.getNormalizedType("Boolean") == "java.lang.Boolean"
    assert JmxBuilderTools.getNormalizedType("java.lang.Boolean") == "java.lang.Boolean"

  }

  void testGetDefaultObjectName() {
    def object = new MockManagedObject()
    def name = new ObjectName(JmxBuilderTools.DEFAULT_DOMAIN +
            "name=${object.getClass().getName()},hashCode=${object.hashCode()}")

    def result = JmxBuilderTools.getDefaultObjectName(object)

    assert result.equals(name)
  }

  void testIsClassMBean() {
    def object = new MockManagedObject()
    assert !JmxBuilderTools.isClassMBean(object.getClass())
    assert !JmxBuilderTools.isClassMBean(MockManagedGroovyObject.class)
    assert JmxBuilderTools.isClassMBean(MockJmxListener.class)
    assert JmxBuilderTools.isClassMBean(JmxBuilderModelMBean.class)

    shouldFail {
      assert JmxBuilderTools.isClassMBean(Class.class)
    }
  }
}