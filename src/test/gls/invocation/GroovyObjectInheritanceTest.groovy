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
package gls.invocation

import gls.CompilableTestSupport

class GroovyObjectInheritanceTest extends CompilableTestSupport {
  void testInheritanceWithGetProperty() {
    assertScript """
        class Foo {
          def getProperty(String name) {1}
        }
        class Bar extends Foo{}
        def bar = new Bar()
        assert bar.foo==1
    """
    assertScript """
        class Foo {}
        class Bar extends Foo{
          def getProperty(String name) {1}
        }
        def bar = new Bar()
        assert bar.foo==1
    """
  }
  
  void testInheritanceWithSetProperty() {
    assertScript """
        class Foo {
          def foo
          void setProperty(String name, x) {this.foo=1}
        }
        class Bar extends Foo{}
        def bar = new Bar()
        bar.foo = 2
        assert bar.foo == 1
    """
    assertScript """
        class Foo {}
        class Bar extends Foo{
          def foo
          void setProperty(String name, x) {this.foo=1}
        }
        def bar = new Bar()
        bar.foo = 2
        assert bar.foo == 1
    """
  }
  
  void testInheritanceWithInvokeMethod() {
    assertScript """
        class Foo {
          def invokeMethod(String name, args) {1}
        }
        class Bar extends Foo{}
        def bar = new Bar()
        assert bar.foo() == 1
    """
    assertScript """
        class Foo {}
        class Bar extends Foo{
          def invokeMethod(String name, args) {1}
        }
        def bar = new Bar()
        assert bar.foo() == 1
    """
  }
  
  void testMetaClassFieldInheritance() {
    assertScript """
        class Foo {}
        class Bar extends Foo{}
        assert Foo.class.declaredFields.find{it.name=="metaClass"}!=null
        assert Bar.class.declaredFields.find{it.name=="metaClass"}==null
    """
  }
  
  void testStandardInheritance() {
    assertScript """
        class Foo{}
        class Bar extends Foo{}
        
        assert Foo.class.declaredFields.find{it.name=="metaClass"}!=null
        assert Bar.class.declaredFields.find{it.name=="metaClass"}==null
        
        assert Foo.class.declaredMethods.find{it.name=="getMetaClass"}!=null
        assert Bar.class.declaredMethods.find{it.name=="getMetaClass"}==null
        
        assert Foo.class.declaredMethods.find{it.name=="setMetaClass"}!=null
        assert Bar.class.declaredMethods.find{it.name=="setMetaClass"}==null
        
        assert Foo.class.declaredMethods.find{it.name=="getProperty"}==null
        assert Bar.class.declaredMethods.find{it.name=="getProperty"}==null
        
        assert Foo.class.declaredMethods.find{it.name=="setProperty"}==null
        assert Bar.class.declaredMethods.find{it.name=="setProperty"}==null
        
        assert Foo.class.declaredMethods.find{it.name=="invokeMethod"}==null
        assert Bar.class.declaredMethods.find{it.name=="invokeMethod"}==null
    """
  }
}