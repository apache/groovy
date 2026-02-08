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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.messages.WarningMessage
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests the use of properties in Groovy
 */
final class PropertyTest {

    @Test
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
    @Test
    void testClassWithPrivateFieldAndGetter() {
        if (HeadlessTestSupport.headless) return
        assert java.awt.Font.getName() == 'java.awt.Font'
        assert java.awt.Font.name == 'java.awt.Font'
    }

    @Test
    void testOverloadedGetter() {
        def foo = new Foo()
        assert foo.getCount() == 1
        assert foo.count == 1
        foo.count = 7
        assert foo.count == 7
        assert foo.getCount() == 7
    }

    @Test
    void testNoSetterAvailableOnPrivateProperty() {
        def foo = new Foo()

        // methods should fail on non-existent method calls
        //shouldFail { foo.blah = 4 }
        shouldFail { foo.setBlah(4) }
    }

    @Test
    void testCannotSeePrivateProperties() {
        def foo = new Foo()

        // def access fails on non-existent def
        //shouldFail { def x = foo.invisible } //todo: correct handling of access rules

        // methods should fail on non-existent method calls
        shouldFail { foo.getQ() }
    }

    @Test
    void testConstructorWithNamedProperties() {
        def foo = new Foo(name: 'Gromit', location: 'Moon')

        assert foo.name == 'Gromit'
        assert foo.location == 'Moon'
    }

    @Test
    void testToString() {
        def foo = new Foo(name: 'Gromit', location: 'Moon')
        assert foo.toString().endsWith('name: Gromit location: Moon')
    }

    @Test
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

    @Test
    void testGstringAssignment() {
        def foo = new Foo()
        foo.body = "${foo.name}"
        assert foo.body == "James"
    }

    // GROOVY-11675
    @Test
    void testSplitProperty() {
        assertScript '''import java.lang.reflect.*
            class C {
                @Deprecated private final Integer one
                final Integer one

                protected synchronized Integer two
                synchronized Integer two

                public Integer three
                @Deprecated Integer three
            }

            Member m = C.getDeclaredField('one')
            assert m.isAnnotationPresent(Deprecated)
            assert m.modifiers == Modifier.PRIVATE + Modifier.FINAL

            m = C.getDeclaredMethod('getOne')
            assert !m.isAnnotationPresent(Deprecated)
            assert m.modifiers == Modifier.PUBLIC + Modifier.FINAL

            groovy.test.GroovyAssert.shouldFail(NoSuchMethodException) {
                m = C.getDeclaredMethod('setOne', Integer)
            }

            m = C.getDeclaredField('two')
            assert m.modifiers == Modifier.PROTECTED
            // field cannot carry modifier SYNCHRONIZED

            m = C.getDeclaredMethod('getTwo')
            assert m.modifiers == Modifier.PUBLIC + Modifier.SYNCHRONIZED

            m = C.getDeclaredMethod('setTwo', Integer)
            assert m.modifiers == Modifier.PUBLIC + Modifier.SYNCHRONIZED

            m = C.getDeclaredField('three')
            assert m.modifiers == Modifier.PUBLIC
            assert !m.isAnnotationPresent(Deprecated)

            m = C.getDeclaredMethod('getThree')
            assert m.modifiers == Modifier.PUBLIC
            assert m.isAnnotationPresent(Deprecated)

            m = C.getDeclaredMethod('setThree', Integer)
            assert m.modifiers == Modifier.PUBLIC
            assert m.isAnnotationPresent(Deprecated)
        '''
    }

    @Test
    void testFinalProperty() {
        assertScript '''
            class A {
               final foo = 1
            }
            A.class.declaredMethods.each {
                assert it.name!="setFoo"
            }
            assert new A().foo==1
        '''
        shouldFail '''
            class A {
                final foo = 1
            }
            new A().foo = 2
        '''
    }

    @Test
    void testFinalField() {
        shouldFail '''
            class A {
                public final foo = 1
            }
            new A().foo = 2
        '''
    }

    @Test
    void testFinalPropertyWithInheritance() {
        def child = new Child()
        assert child.finalProperty == 1
        child.finalProperty = 22
        assert child.finalProperty == 1
    }

    @Test
    void testBaseProperties() {
        assert new Child().field == 'foobar'
    }

    // GROOVY-1736
    @Test
    void testGetSuperProperties() {
        def c = new Child()
        assert c.thing == 'bar thing'
        assert c.superthing() == 'bar1foo thing'
        assert c.x() == 'bar2foo x'
        assert c.xprop == 'bar3foo x prop'
        assert c.xpropViaMethod == 'bar4foo x prop'
    }

    // GROOVY-1736, GROOVY-9609
    @Test
    void testGetSuperProperties2() {
        for (vis in ['', 'public', 'protected', '@groovy.transform.PackageScope']) {
            assertScript """
                abstract class A {
                    $vis def getX() { 'A' }
                }
                class C extends A {
                    def getX() { super.x + 'C' } // no stack overflow
                    def m() {
                        '' + x + this.x + super.x // TODO: test safe and spread
                    }
                }
                String result = new C().m()
                assert result == 'ACACA'
            """
        }
    }

    // GROOVY-6097
    @Test
    void testGetSuperProperties3() {
        for (vis in ['', 'public', 'protected', '@groovy.transform.PackageScope']) {
            assertScript """
                abstract class A {
                    $vis boolean isX() { true }
                }
                class C extends A {
                    def m() {
                        '' + x + this.x + super.x
                    }
                }
                String result = new C().m()
                assert result == 'truetruetrue'
            """
        }
    }

    @Test
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

    @Test
    void testOverwritingNormalProperty() {
        def c = new Child()
        assert c.normalProperty == 2
    }

    // GROOVY-2244
    @Test
    void testWriteOnlyBeanProperty() {
        def bean = new Child()

        // assert the property exists
        assert bean.metaClass.properties.findAll { it.name == 'superThing' }

        // attempt to write to it
        bean.superThing = 'x'

        // attempt to read it
        shouldFail(MissingPropertyException) {
            bean.superThing
        }
    }

    // GROOVY-10456
    @Test
    void testEmptyPropertyAccessForObject() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            shouldFail(MissingPropertyException) {
                o = new Object()
                o[""]
            }
        '''
    }

    @Test
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

    @Test
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

    // GROOVY-11753
    @Test
    void testPropertyOverridesGetterAndSetter() {
        assertScript '''
            interface A {
                String getFoo()
            }
            interface B {
                void setFoo(String s)
            }
            class C implements A,B {
                class NestMate { }
                String foo
            }
            class D extends C {
            }

            def pogo = new D()
            pogo.setFoo("")
            assert pogo.getFoo().isEmpty()
        '''
    }

    @Test
    void testPropertyOverridesGetterAndSetter2() {
        assertScript '''
            abstract class A {
                private final String getFoo() { 'A' }
            }
            class C extends A {
                final String foo = 'C'
            }

            assert new C().getFoo() == 'C'
            assert C.getMethod('getFoo').declaringClass.name == 'C'
        '''

        assertScript '''
            abstract class A {
                private final void setFoo(String foo) { }
            }
            class C extends A {
                String foo
            }

            assert new C(foo: 'C').getFoo() == 'C'
            assert C.getMethod('setFoo', String).declaringClass.name == 'C'
        '''
    }

    // GROOVY-8659
    @Test
    void testPropertyCannotOverrideFinalGetter() {
        File parentDir = File.createTempDir()
        def config = new CompilerConfiguration()
        config.targetDirectory = File.createTempDir()
        config.warningLevel = WarningMessage.POSSIBLE_ERRORS
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                abstract class A {
                    final String getFoo() { 'A' }
                }
            '''
            def c = new File(parentDir, 'C.groovy')
            c.write '''
                class C extends A {
                    final String foo = 'C'
                }
            '''
            def m = new File(parentDir, 'Main.groovy')
            m.write '''
                def pogo = new C()
                assert pogo.foo == 'A'
                assert pogo.getFoo() == 'A'
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, c, m)
            cu.compile()

            assert cu.errorCollector.warnings*.message == [
                'Property foo cannot override final method getFoo() of class A'
            ]

            loader.addClasspath(config.targetDirectory.absolutePath)
            loader.loadClass('Main').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    // GROOVY-8659
    @Test
    void testPropertyCannotOverrideFinalSetter() {
        File parentDir = File.createTempDir()
        def config = new CompilerConfiguration()
        config.targetDirectory = File.createTempDir()
        config.warningLevel = WarningMessage.POSSIBLE_ERRORS
        try {
            def a = new File(parentDir, 'A.groovy')
            a.write '''
                abstract class A {
                    final void setFoo(String foo) { }
                }
            '''
            def c = new File(parentDir, 'C.groovy')
            c.write '''
                class C extends A {
                    String foo
                }
            '''
            def m = new File(parentDir, 'Main.groovy')
            m.write '''
                def pogo = new C(foo: 'C')
                assert pogo.foo == null
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, c, m)
            cu.compile()

            assert cu.errorCollector.warnings*.message == [
                'Property foo cannot override final method setFoo(java.lang.String) of class A'
            ]

            loader.addClasspath(config.targetDirectory.absolutePath)
            loader.loadClass('Main').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    @Test
    void testPropertyWithOverrideGetterAndSetter() {
        assertScript '''
            abstract class A {
                abstract String getName()
                abstract void setName(String name)
            }
            class C extends A {
                private String name = 'C'

                @Override
                String getName() {
                    this.name
                }
                @Override
                void setName(String name) {
                    this.name = name
                }
            }

            A a = new C()
            assert a.name == 'C'
            a.name = 'X'
            assert a.name == 'X'
        '''
    }

    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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
    @Test
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

    // GROOVY-5852, GROOVY-9618
    @Test
    void testMissingPropertyWithStaticImport() {
        def err = shouldFail '''
            class Constants {
                static final pi = 3.14
            }

            import static Constants.*

            assert PI.toString().startsWith('3')
        '''
        assert err.message.contains('No such property: PI for class:')
    }

    // GROOVY-5852, GROOVY-9618
    @Test
    void testMissingPropertyWithDirectUsage() {
        def err = shouldFail '''
            class Constants {
                static final pi = 3.14
            }

            assert Constants.PI.toString().startsWith('3')
        '''
        assert err.message.contains('No such property: PI for class: Constants')
    }

    // GROOVY-5852, GROOVY-9618
    @Test
    void testPropertyDirectUsageWithAllowableCaseChange() {
        assertScript '''
            class Constants {
                static final pi = 3.14
            }

            assert Constants.Pi.toString().startsWith('3')
        '''
    }

    // GROOVY-5852, GROOVY-9618
    @Test
    void testPropertyStaticImportWithAllowableCaseChange() {
        assertScript '''
            class Constants {
                static final pi = 3.14
            }

            import static Constants.*

            assert Pi.toString().startsWith('3')
        '''
    }

    //--------------------------------------------------------------------------

    static class Base {
        protected String field = 'bar'

        protected thing = 'foo thing'

        def getXprop() { 'foo x prop' }

        def x() { 'foo x' }

        void setThing(value) { thing = value }

        // testing final property getter
        final getFinalProperty() { 1 }

        // testing normal property
        def normalProperty = 1
    }

    static class Child extends Base {
        protected String field = 'foo' + super.field

        def getField() { field }

        void setSuperField(value) { super.field = value }

        def getSuperField() { super.field }

        def thing = 'bar thing'

        def superthing() {
            'bar1' + super.thing
        }

        @Override
        def x() {
            'bar2' + super.x()
        }

        @Override
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
}
