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
package groovy.beans

import groovy.test.GroovyShellTestCase
import org.codehaus.groovy.control.CompilationFailedException

import java.beans.PropertyChangeListener

import static java.lang.reflect.Modifier.isPublic

class BindableTransformTest extends GroovyShellTestCase {

    void testSimpleBindableProperty() {
        assertScript """
            import groovy.beans.Bindable

            class BindableTestBean1 {
                @Bindable String name
            }

            sb = new BindableTestBean1()
            sb.name = "bar"
            changed = false
            sb.propertyChange = {changed = true}
            sb.name = "foo"
            assert changed
        """
    }

    void testMultipleBindableProperty() {
        assertScript """
            import groovy.beans.Bindable

            class BindableTestBean2 {
                @Bindable String name
                @Bindable String value
            }

            sb = new BindableTestBean2(name:"foo", value:"bar")
            changed = 0
            sb.propertyChange = {changed++}
            sb.name = "baz"
            sb.value = "biff"
            assert changed == 2
        """
    }

    void testMutatingSetter() {
        assertScript """
            class BindableTestBean3 {
                @groovy.beans.Bindable String name
                void setName(String newName) {
                     this.@name = "x\$newName"
                }
            }
            sb = new BindableTestBean3(name:"foo")
            changed = 0
            sb.propertyChange = {evt ->
                assert evt.newValue =~ '^x'
                changed++
            }
            sb.name = "baz"
            assert changed == 1
        """
    }

    void testWithSettersAndGetters() {
        for (int i = 0; i < 16; i++) {
            boolean bindClass = i & 1
            boolean field = i & 2
            boolean setter = i & 4
            boolean getter = i & 8
            int expectedCount = (bindClass && !field) ? 2 : 1
            String script = """
                    import groovy.beans.Bindable

                    ${bindClass ? '@Bindable ' : ''}class BindableTestSettersAndGetters$i {

                        @Bindable String alwaysBound
                        ${field ? 'protected ' : ''} String name

                        ${setter ? '' : '//'}void setName(String newName) { this.@name = "x\$newName" }
                        ${getter ? '' : '//'}String getName() { return this.@name }
                    }
                    sb = new BindableTestSettersAndGetters$i(name:"foo", alwaysBound:"bar")
                    changed = 0
                    sb.propertyChange = {evt ->
                        changed++
                    }
                    sb.alwaysBound = "baz"
                    sb.name = "bif"
                    sb.name = "bif"
                    assert changed == $expectedCount
                """
            try {
                GroovyShell shell = new GroovyShell()
                shell.evaluate(script);
            } catch (Throwable t) {
                System.out.println("Failed Script: $script")
                throw t
            }
        }
    }

    void testOnField() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class BindableTestBean4 {
                    public @groovy.beans.Bindable String name
                }
            """)
        }
    }

    void testOnStaticField() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class BindableTestBean5 {
                     @groovy.beans.Bindable static String name
                }
            """)
        }
    }

    void testClassMarkers() {
        for (int i = 0; i < 7; i++) {
            boolean bindField = i & 1
            boolean bindClass = i & 2
            boolean staticField = i & 4
            int bindCount = bindClass ? (staticField ? 2 : 3) : (bindField ? 1 : 0);

            String script = """
                    import groovy.beans.Bindable

                    ${bindClass ? '@Bindable ' : ''}class ClassMarkerBindable$i {
                        String neither

                        ${bindField ? '@Bindable ' : ''}String bind

                        ${staticField ? 'static ' : ''}String staticString
                    }

                    cb = new ClassMarkerBindable$i(neither:'a', bind:'b', staticString:'c')
                    bindCount = 0
                    ${bindClass | bindField ? 'cb.propertyChange = { bindCount++ }' : ''}
                    cb.neither = 'd'
                    cb.bind = 'e'
                    cb.staticString = 'f'
                    assert bindCount == $bindCount
                """
            try {
                GroovyShell shell = new GroovyShell()
                shell.evaluate(script);
            } catch (Throwable t) {
                System.out.println("Failed Script: $script")
                throw t
            }
        }
    }

    void testPrimitiveTypes() {
        assertScript """
            import groovy.beans.Bindable

            class BindableTestBean7 {
                @Bindable String testField
                @Bindable boolean testBoolean
                @Bindable byte testByte
                @Bindable short testShort
                @Bindable int testInt
                @Bindable long testLong
                @Bindable float testFloat
                @Bindable double testDouble
            }

            sb = new BindableTestBean7()
            sb.testField = "bar"
            int changed = 0
            sb.propertyChange = {changed++}
            sb.testField = "foo"
            sb.testBoolean = true
            sb.testByte = 1
            sb.testShort = 1
            sb.testInt = 1
            sb.testLong = 1
            sb.testFloat = 1
            sb.testDouble = 1
            assert changed == 8
        """
    }

    void testBadInheritance() {
        shouldFail(CompilationFailedException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Bindable

                class BindableTestBean8  {
                    @Bindable String testField
                    void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}
                }
                new BindableTestBean8()
            """)
        }
        shouldFail(CompilationFailedException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Bindable

                class BindableTestBean9  {
                    void addPropertyChangeListener(java.beans.PropertyChangeListener l) {}
                }

                class BindableTestBean10 extends BindableTestBean9 {
                    @Bindable String testField
                }

                new BindableTestBean10()
            """)
        }
    }

    void testBindableParent() {
        assertScript """
            import groovy.beans.Bindable
            import java.beans.PropertyChangeEvent
            import java.beans.PropertyChangeListener

            @Bindable
            class BindableTestBeanChild extends BindableTestBeanParent {
                String prop2
                BindableTestBeanChild() {
                    super()
                }
            }

            @Bindable
            class BindableTestBeanParent implements PropertyChangeListener {
                String prop1
                BindableTestBeanParent() {
                    addPropertyChangeListener(this)
                }

                void propertyChange(PropertyChangeEvent event) {}
            }

            new BindableTestBeanChild()
        """
    }

    void testFinalProperty() {
        shouldFail(CompilationFailedException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Bindable

                class BindableTestBean11  {
                  @Bindable final String testField
                }
                1+1
            """)
        }
    }

    void testOnClassFinalProperty() {
        shouldFail(ReadOnlyPropertyException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Bindable

                @Bindable class BindableTestBean12  {
                  String testField
                  final String anotherTestField = 'Fixed'
                }

                sb = new BindableTestBean12()
                int changed = 0
                sb.propertyChange = {changed++}
                sb.testField = 'newValue'
                assert changed == 1

                sb.anotherTestField = 'Changed'
            """)
        }
    }

    void testFinalClass() {
        shouldFail(ReadOnlyPropertyException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Bindable

                @Bindable final class BindableTestBean12  {
                  String testField
                  final String anotherTestField = 'Fixed'
                }

                sb = new BindableTestBean12()
                int changed = 0
                sb.propertyChange = {changed++}
                sb.testField = 'newValue'
                assert changed == 1

                sb.anotherTestField = 'Changed'
            """)
        }
    }

    void testGetPropertyChangeListeners() {
        assertScript """
            import groovy.beans.Bindable
            import java.beans.PropertyChangeListener
            import java.beans.PropertyChangeEvent

            class BindableTestBean14 {
                @Bindable String foo
                @Bindable String bar
            }

            class FooListener implements PropertyChangeListener {
               void propertyChange( PropertyChangeEvent e ) { }
            }

            sb = new BindableTestBean14()
            assert !sb.propertyChangeListeners
            listener = new FooListener()
            sb.addPropertyChangeListener("foo",listener)
            assert !sb.getPropertyChangeListeners("bar")
            assert sb.getPropertyChangeListeners("foo") == [listener]
            assert sb.propertyChangeListeners.size() == 1
        """
    }

    void testPropertyChangeMethodsNotSynthetic() {
        def clazz = new GroovyClassLoader().parseClass('class MyBean { @groovy.beans.Bindable String dummy }', 'dummyName')
        def method = clazz.getMethod('addPropertyChangeListener', PropertyChangeListener)
        assert isPublic(method.modifiers)
        assert !method.isSynthetic()
    }

    void testPropertyChangeMethodWithCompileStatic() {
        assertScript """
            import groovy.beans.Bindable
            import groovy.transform.CompileStatic

            @CompileStatic
            class MyBean {
              @Bindable String test = "a test"
            }
            assert new MyBean()
        """
    }

    void testBindableGeneratedMethodsAreAnnotatedWithGenerated_GROOVY9053() {
        def person = evaluate('''
            class Person {
                @groovy.beans.Bindable
                String firstName

                void setFirstName(String fn) {
                    this.firstName = fn.toUpperCase()
                }

                @groovy.beans.Bindable
                def zipCode
             }
             new Person()
        ''')

        person.class.declaredMethods.each { m ->
            if (m.name.contains('PropertyChange') || m.name in ['setZipCode']) {
                assert m.annotations*.annotationType().name.contains('groovy.transform.Generated')
            }
            if (m.name in ['setFirstName']) {
                // wrapped methods should not be marked since they contain non-generated logic
                assert !m.annotations*.annotationType().name.contains('groovy.transform.Generated')
            }
        }
    }
}
