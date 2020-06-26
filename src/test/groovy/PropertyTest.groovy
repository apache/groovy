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
package groovy

import groovy.test.GroovyTestCase

/**
 * Tests the use of properties in Groovy
 */
class PropertyTest extends GroovyTestCase {

    void testNormalPropertyGettersAndSetters() {
        def foo = new Foo()
        def value = foo.getMetaClass()

        assert foo.name == "James"
        assert foo.getName() == "James"
        assert foo.location == "London"
        assert foo.getLocation() == "London"
        assert foo.blah == 9
        assert foo.getBlah() == 9

        foo.name = "Bob"
        foo.location = "Atlanta"

        assert foo.name == "Bob"
        assert foo.getName() == "Bob"
        assert foo.location == "Atlanta"
        assert foo.getLocation() == "Atlanta"
    }

    // GROOVY-1809
    void testClassWithPrivateFieldAndGetter() {
        if (HeadlessTestSupport.headless) return
        assert java.awt.Font.getName() == 'java.awt.Font'
        assert java.awt.Font.name == 'java.awt.Font'
    }

    void testOverloadedGetter() {
        def foo = new Foo()
        assert foo.getCount() == 1
        assert foo.count == 1
        foo.count = 7
        assert foo.count == 7
        assert foo.getCount() == 7
    }

    void testNoSetterAvailableOnPrivateProperty() {
        def foo = new Foo()

        // methods should fail on non-existent method calls
        //shouldFail { foo.blah = 4 }
        shouldFail { foo.setBlah(4) }
    }

    void testCannotSeePrivateProperties() {
        def foo = new Foo()

        // def access fails on non-existent def
        //shouldFail { def x = foo.invisible } //todo: correct handling of access rules

        // methods should fail on non-existent method calls
        shouldFail { foo.getQ() }
    }

    void testConstructorWithNamedProperties() {
        def foo = new Foo(name: 'Gromit', location: 'Moon')

        assert foo.name == 'Gromit'
        assert foo.location == 'Moon'
    }

    void testToString() {
        def foo = new Foo(name: 'Gromit', location: 'Moon')
        assert foo.toString().endsWith('name: Gromit location: Moon')
    }

    void testArrayLengthProperty() {
        // create two arrays, since all use the same MetaArrayLengthProperty object -
        // make sure it can work for all types and sizes
        def i = new Integer[5]
        def s = new String[10]

        // put something in it to make sure we're returning the *allocated* length, and
        // not the *used* length
        s[0] = "hello"

        assert i.length == 5
        assert s.length == 10

        // this def does not mean there is a getLength() method
        shouldFail { i.getLength() }

        // verify we can't set this def, it's read-only
        shouldFail { i.length = 6 }
    }

    void testGstringAssignment() {
        def foo = new Foo()
        foo.body = "${foo.name}"
        assert foo.body == "James"
    }

    void testFinalProperty() {
        def shell = new GroovyShell();
        assertScript """
        class A {
           final foo = 1
        }
        A.class.declaredMethods.each {
          assert it.name!="setFoo"
          
        }
        assert new A().foo==1
      """
        shouldFail {
            shell.execute """
          class A {
            final foo = 1
          }
          new A().foo = 2
        """
        }
    }

    void testFinalField() {
        def shell = new GroovyShell();
        shouldFail {
            shell.execute """
          class A {
            public final foo = 1
          }
          new A().foo = 2
        """
        }
    }

    void testFinalPropertyWithInheritance() {
        def child = new Child()
        assert child.finalProperty == 1
        child.finalProperty = 22
        assert child.finalProperty == 1
    }

    void testBaseProperties() {
        assert new Child().field == 'foobar'
    }

    // GROOVY-1736
    void testGetSuperProperties() {
        def c = new Child()
        assert c.thing == 'bar thing'
        assert c.superthing() == 'bar1foo thing'
        assert c.x() == 'bar2foo x'
        assert c.xprop == 'bar3foo x prop'
        assert c.xpropViaMethod == 'bar4foo x prop'
    }

    void testSetSuperProperties() {
        def c = new Child()
        assert c.superField == 'bar'
        c.setSuperField('baz1')
        assert c.superField == 'baz1'
        c.superField = 'baz2'
        assert c.superField == 'baz2'

        assert c.superthing() == 'bar1foo thing'
        c.superThing = 'bar thing'
        assert c.superthing() == 'bar1bar thing'
    }

    void testOverwritingNormalProperty() {
        def c = new Child()
        assert c.normalProperty == 2
    }

    //GROOVY-2244
    void testWriteOnlyBeanProperty() {
        def bean = new Child()

        // assert the property exists
        assert bean.metaClass.properties.findAll { it.name == 'superThing' }

        // attempt to write to it
        bean.superThing = 'x'

        // attempt to read it
        shouldFail(MissingPropertyException) {
            fail("We shouldn't be able to read bean.superThing, but we can: '$bean.superThing'")
        }
    }

    void testPrivatePropertyThroughSubclass() {
        assertScript '''
            class A {
                private getFoo(){1}
                def bar(){return foo}
            }
            class B extends A {}

            def b = new B()
            assert b.bar()==1
        '''
    }

    void testPropertyWithMultipleSetters() {
        assertScript '''
            class A {
                private field
                void setX(Integer a) {field=a}
                void setX(String b) {field=b}
                def getX(){field}
            }
            def a = new A()
            a.x = 1
            assert a.x==1
            a.x = "3"
            assert a.x == "3"
        '''
    }

    void testPropertyWithOverrideGetterAndSetter() {
        assertScript '''
            abstract class Base {
                abstract String getName()
                abstract void setName(String name)
            }
            class A extends Base {
                private String name = 'AA'
            
                @Override
                String getName() {
                    this.name
                }
                @Override
                void setName(String name) {
                    this.name = name
                }
            }
            Base a = new A()
            assert 'AA' == a.name
            a.name = 'BB'
            assert 'BB' == a.name
        '''
    }

    void testOverrideMultiSetterThroughMetaClass() {
        assertScript '''
        class A {
            private String field
            void setConstraints(Closure cl) {}
            void setConstraints(String s) {}
            String getField() { field }
        }

        A.metaClass.setConstraints = { delegate.field = it+it }
        def a = new A()
        a.constraints = '100'
        assert a.field == '100100'
        '''
    }

}

class Base {
    protected String field = 'bar'

    protected thing = 'foo thing'

    def getXprop() { 'foo x prop' }

    def x() { 'foo x' }

    void setThing(value) { thing = value }

    //testing final property getter
    final getFinalProperty() { 1 }

    // testing normal property
    def normalProperty = 1
}

class Child extends Base {
    protected String field = 'foo' + super.field

    def getField() { field }

    void setSuperField(value) { super.field = value }

    def getSuperField() { super.field }

    def thing = 'bar thing'

    def superthing() {
        'bar1' + super.thing
    }

    def x() {
        'bar2' + super.x()
    }

    def getXprop() {
        'bar3' + super.xprop
    }

    def getXpropViaMethod() {
        'bar4' + super.getXprop()
    }

    def setSuperThing(value) {
        super.thing = value
    }

    // testing final property getter
    // the following property should not add a new getter
    // method, this would result in a verify error
    def finalProperty = 32

    // testing overwriting normal property
    def normalProperty = 2
}
