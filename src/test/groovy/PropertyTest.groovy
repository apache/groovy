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

    // GROOVY-9618
    void testJavaBeanNamingForPropertyAccess() {
        assertScript '''
        class A {
            private String X = 'fieldX'
            private String Prop = 'fieldProp'
            private String DB = 'fieldDB'
            private String XML = 'fieldXML'
            String getProp() { 'Prop' }
            String getSomeProp() { 'SomeProp' }
            String getX() { 'X' }
            String getDB() { 'DB' }
            String getXML() { 'XML' }
            String getaProp() { 'aProp' }
            String getAProp() { 'AProp' }
            String getpNAME() { 'pNAME' }
        }
        new A().with {
            assert prop == 'Prop'
            assert Prop == 'Prop'
            assert x == 'X'
            assert X == 'X'
            assert someProp == 'SomeProp'
            assert SomeProp == 'SomeProp'
            assert DB == 'DB'
            assert XML == 'XML'
            assert AProp == 'AProp'
            assert aProp == 'aProp'
            assert pNAME == 'pNAME'
        }
        '''
    }

    // GROOVY-9618
    void testJavaBeanNamingForPropertyAccessCS() {
        assertScript '''
        class A {
            String getProp() { 'Prop' }
            String getSomeProp() { 'SomeProp' }
            String getX() { 'X' }
            String getDB() { 'DB' }
            String getXML() { 'XML' }
            String getaProp() { 'aProp' }
            String getAProp() { 'AProp' }
            String getpNAME() { 'pNAME' }
        }
        class B {
            @groovy.transform.CompileStatic
            def method() {
                new A().with {
                    assert prop == 'Prop'
                    assert Prop == 'Prop'
                    assert x == 'X'
                    assert X == 'X'
                    assert someProp == 'SomeProp'
                    assert SomeProp == 'SomeProp'
                    assert DB == 'DB'
                    assert XML == 'XML'
                    assert AProp == 'AProp'
                    assert aProp == 'aProp'
                    assert pNAME == 'pNAME'
                }
            }
        }
        new B().method()
        '''
    }

    // GROOVY-9618
    void testJavaBeanNamingForStaticPropertyAccess() {
        assertScript '''
        class A {
            private static String X = 'fieldX'
            private static String Prop = 'fieldProp'
            private static String DB = 'fieldDB'
            private static String XML = 'fieldXML'
            static String getProp() { 'Prop' }
            static String getSomeProp() { 'SomeProp' }
            static String getX() { 'X' }
            static String getDB() { 'DB' }
            static String getXML() { 'XML' }
            static String getaProp() { 'aProp' }
            static String getAProp() { 'AProp' }
            static String getpNAME() { 'pNAME' }
        }
        A.with {
            assert prop == 'Prop'
            assert Prop == 'Prop'
            assert x == 'X'
            assert X == 'X'
            assert someProp == 'SomeProp'
            assert SomeProp == 'SomeProp'
            assert DB == 'DB'
            assert XML == 'XML'
            assert AProp == 'AProp'
            assert aProp == 'aProp'
            assert pNAME == 'pNAME'
        }
        '''
    }

    // GROOVY-9618
    void testJavaBeanNamingForStaticPropertyAccessCS() {
        assertScript '''
        class A {
            static String getProp() { 'Prop' }
            static String getSomeProp() { 'SomeProp' }
            static String getX() { 'X' }
            static String getDB() { 'DB' }
            static String getXML() { 'XML' }
            static String getaProp() { 'aProp' }
            static String getAProp() { 'AProp' }
            static String getpNAME() { 'pNAME' }
        }
        class B {
            @groovy.transform.CompileStatic
            static method() {
                A.with {
                    assert prop == 'Prop'
                    assert Prop == 'Prop'
                    assert x == 'X' // TODO fix CCE
                    assert X == 'X' // TODO fix CCE
                    assert someProp == 'SomeProp'
                    assert SomeProp == 'SomeProp'
                    assert DB == 'DB'
                    assert XML == 'XML'
                    assert AProp == 'AProp'
                    assert aProp == 'aProp'
                    assert pNAME == 'pNAME'
                }
            }
        }
        B.method()
        '''
    }

    // GROOVY-9618
    void testJavaBeanNamingForPropertyStaticImport() {
        assertScript '''
        class A {
            static String getProp() { 'Prop' }
            static String getSomeProp() { 'SomeProp' }
            static String getX() { 'X' }
            static String getDB() { 'DB' }
            static String getXML() { 'XML' }
            static String getaProp() { 'aProp' }
            static String getAProp() { 'AProp' }
            static String getpNAME() { 'pNAME' }
        }
        
        import static A.*

        assert prop == 'Prop'
        assert Prop == 'Prop'
        assert x == 'X'
        assert X == 'X'
        assert someProp == 'SomeProp'
        assert SomeProp == 'SomeProp'
        assert DB == 'DB'
        assert XML == 'XML'
        assert AProp == 'AProp'
        assert aProp == 'aProp'
        assert pNAME == 'pNAME'
        '''
    }

    // GROOVY-9618
    void testJavaBeanNamingForPropertyStaticImportCS() {
        assertScript '''
        class A {
            static String getProp() { 'Prop' }
            static String getSomeProp() { 'SomeProp' }
            static String getX() { 'X' }
            static String getDB() { 'DB' }
            static String getXML() { 'XML' }
            static String getaProp() { 'aProp' }
            static String getAProp() { 'AProp' }
            static String getpNAME() { 'pNAME' }
        }
        
        import static A.*

        class B {
            @groovy.transform.CompileStatic
            def method() {
                assert prop == 'Prop'
                assert Prop == 'Prop'
                assert x == 'X'
                assert X == 'X'
                assert someProp == 'SomeProp'
                assert SomeProp == 'SomeProp'
                assert DB == 'DB'
                assert XML == 'XML'
                assert AProp == 'AProp'
                assert aProp == 'aProp'
                assert pNAME == 'pNAME'
            }
        }
        new B().method()
        '''
    }

    // GROOVY-9618
    void testJavaBeanNamingForPropertyAccessWithCategory() {
        assertScript '''
        class A {}
        class ACategory {
            static String getProp(A self) { 'Prop' }
            static String getSomeProp(A self) { 'SomeProp' }
            static String getX(A self) { 'X' }
            static String getDB(A self) { 'DB' }
            static String getXML(A self) { 'XML' }
            static String getaProp(A self) { 'aProp' }
            static String getAProp(A self) { 'AProp' }
            static String getpNAME(A self) { 'pNAME' }
        }
        use(ACategory) {
            def a = new A()
            assert a.prop == 'Prop'
            assert a.Prop == 'Prop'
            assert a.x == 'X'
            assert a.X == 'X'
            assert a.someProp == 'SomeProp'
            assert a.SomeProp == 'SomeProp'
            assert a.DB == 'DB'
            assert a.XML == 'XML'
            assert a.AProp == 'AProp'
            assert a.aProp == 'aProp'
            assert a.pNAME == 'pNAME'
        }
        '''
    }

    void testMissingPropertyWithStaticImport() { // GROOVY-5852+GROOVY-9618

        def errMsg = shouldFail '''
            class Constants {
                static final pi = 3.14
            }

            import static Constants.*

            assert PI.toString().startsWith('3')
        '''

        assert errMsg.contains('No such property: PI for class:')
    }

    void testMissingPropertyWithDirectUsage() { // GROOVY-5852+GROOVY-9618
        def errMsg = shouldFail '''
            class Constants {
                static final pi = 3.14
            }

            assert Constants.PI.toString().startsWith('3')
        '''

        assert errMsg.contains('No such property: PI for class: Constants')
    }

    void testPropertyDirectUsageWithAllowableCaseChange() { // GROOVY-5852+GROOVY-9618
        assertScript '''
            class Constants {
                static final pi = 3.14
            }

            assert Constants.Pi.toString().startsWith('3')
        '''
    }

    void testPropertyStaticImportWithAllowableCaseChange() { // GROOVY-5852+GROOVY-9618
        assertScript '''
            class Constants {
                static final pi = 3.14
            }

            import static Constants.*

            assert Pi.toString().startsWith('3')
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
