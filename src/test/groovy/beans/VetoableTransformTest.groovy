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

class VetoableTransformTest extends GroovyShellTestCase {

    void testSimpleConstrainedProperty() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
            import groovy.beans.Vetoable

            class VetoableTestBean1 {
                @Vetoable String name
            }

            sb = new VetoableTestBean1()
            sb.name = "foo"
            changed = false
            sb.vetoableChange = { pce ->
                if (changed) {
                    throw new java.beans.PropertyVetoException("Twice, even!", pce)
                } else {
                    changed = true
                }
            }
            sb.name = "foo"
            sb.name = "bar"
            try {
                sb.name = "baz"
                changed = false
            } catch (java.beans.PropertyVetoException pve) {
                // yep, we were vetoed
            }
        """)
        assert shell.changed
    }

    void testBindableVetoableProperty() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
            import groovy.beans.Bindable
            import groovy.beans.Vetoable

            class VetoableTestBean2 {
                @Bindable @Vetoable String name
            }

            sb = new VetoableTestBean2()
            sb.name = "foo"
            vetoCheck = false
            changed = false
            sb.vetoableChange = { vetoCheck = true }
            sb.propertyChange = { changed = true }
            sb.name = "foo"
            assert !vetoCheck
            assert !changed
            sb.name = "bar"
        """)
        assert shell.changed
        assert shell.vetoCheck
    }

    void testMultipleProperties() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
            import groovy.beans.Bindable
            import groovy.beans.Vetoable

            class VetoableTestBean3 {
                String u1
                @Bindable String b1
                @Vetoable String c1
                @Bindable @Vetoable String bc1
                String u2
                @Bindable String b2
                @Vetoable String c2
                @Bindable @Vetoable String bc2
            }

            sb = new VetoableTestBean3(u1:'a', b1:'b', c1:'c', bc1:'d', u2:'e', b2:'f', c2:'g', bc2:'h')
            changed = 0
            sb.vetoableChange = { changed++ }
            sb.propertyChange = { changed++ }
            sb.u1  = 'i'
            sb.b1  = 'j'
            sb.c1  = 'k'
            sb.bc1 = 'l'
            sb.u2  = 'm'
            sb.b2  = 'n'
            sb.c2  = 'o'
            sb.bc2 = 'p'
        """)
        assert shell.changed == 8
    }

    void testExisingSetter() {
        assertScript """
            class VetoableTestBean4 {
                @groovy.beans.Vetoable String name
                void setName() { }
            }
            new VetoableTestBean4()
        """
    }

    void testWithSettersAndGetters() {
        for (int i = 0; i < 16; i++) {
            boolean vetoClass = i & 1
            boolean field = i & 2
            boolean setter = i & 4
            boolean getter = i & 8
            int expectedCount = (vetoClass && !field)?2:1
            String script = """
                    import groovy.beans.Vetoable

                    ${vetoClass?'@Vetoable ':''}class VetoableTestSettersAndGetters$i {

                        @Vetoable String alwaysVetoable
                        ${field?'protected ':''} String name

                        ${setter?'':'//'}void setName(String newName) { this.@name = "x\$newName" }
                        ${getter?'':'//'}String getName() { return this.@name }
                    }
                    sb = new VetoableTestSettersAndGetters$i(name:"foo", alwaysVetoable:"bar")
                    changed = 0
                    sb.vetoableChange = {evt ->
                        changed++
                    }
                    sb.alwaysVetoable = "baz"
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
                class VetoableTestBean5 {
                    public @groovy.beans.Vetoable String name
                }
            """)
        }
    }

    void testOnStaticField() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class VetoableTestBean6 {
                    @groovy.beans.Vetoable static String name
                }
            """)
        }
    }

    void testInheritance() {
        for (int i = 0; i < 15; i++) {
            boolean bindParent = i & 1
            boolean bindChild  = i & 2
            boolean vetoParent = i & 4
            boolean vetoChild  = i & 8
            int count = (bindParent?1:0) + (bindChild?1:0) + (vetoParent?1:0) + (vetoChild?1:0)
            String script = """
                    import groovy.beans.Bindable
                    import groovy.beans.Vetoable

                    class InheritanceParentBean$i {
                        ${bindParent?'@Bindable':''} String bp
                        ${vetoParent?'@Vetoable':''} String vp
                    }

                    class InheritanceChildBean$i extends InheritanceParentBean$i {
                        ${bindChild?'@Bindable':''} String bc
                        ${vetoChild?'@Vetoable':''} String vc
                    }

                    cb = new InheritanceChildBean$i(bp:'a', vp:'b', bc:'c', vc:'d')
                    changed = 0
                    ${bindParent|bindChild?'cb.propertyChange = { changed++ }':''}
                    ${vetoParent|vetoChild?'cb.vetoableChange = { changed++ }':''}
                    cb.bp = 'e'
                    cb.vp = 'f'
                    cb.bc = 'g'
                    cb.vc = 'h'
                    assert changed == $count
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
                import groovy.beans.Vetoable

                class VetoableTestBean8 {
                    @Vetoable String testField
                    @Vetoable boolean testBoolean
                    @Vetoable byte testByte
                    @Vetoable short testShort
                    @Vetoable int testInt
                    @Vetoable long testLong
                    @Vetoable float testFloat
                    @Vetoable double testDouble
                }

                sb = new VetoableTestBean8()
                sb.testField = "bar"
                int changed = 0
                sb.vetoableChange = {changed++}
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
                    import groovy.beans.Vetoable

                    class VetoableTestBean9  {
                        @Vetoable String testField
                        void addVetoableChangeListener(java.beans.VetoableChangeListener l) {}
                    }
                    new VetoableTestBean9()
                """)
        }
        shouldFail(CompilationFailedException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                    import groovy.beans.Vetoable

                    class VetoableTestBean10  {
                        void addPropertyChangeListener(java.beans.VetoableChangeListener l) {}
                    }

                    class VetoableTestBean11 extends VetoableTestBean9 {
                        @Vetoable String testField
                    }

                    new VetoableTestBean10()
                """)
        }
    }

    void testVetoableParent() {
        assertScript """
            import groovy.beans.Vetoable
            import java.beans.PropertyChangeEvent
            import java.beans.VetoableChangeListener

            @Vetoable
            class VetoableTestBeanChild extends VetoableTestBeanParent {
                String prop2
                VetoableTestBeanChild() {
                    super()
                }
            }

            @Vetoable
            class VetoableTestBeanParent implements VetoableChangeListener {
                String prop1
                VetoableTestBeanParent() {
                    addVetoableChangeListener(this)
                }

                void vetoableChange(PropertyChangeEvent event) {}
            }

            new VetoableTestBeanChild()
        """
    }

    void testFinalProperty() {
        shouldFail(CompilationFailedException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Vetoable

                class VetoableTestBean11  {
                  @Vetoable final String testField
                }
                1+1
            """)
        }
    }

    void testOnClassFinalProperty() {
        shouldFail(ReadOnlyPropertyException) {
            GroovyShell shell = new GroovyShell()
            shell.evaluate("""
                import groovy.beans.Vetoable

                @Vetoable class VetoableTestBean12  {
                  String testField
                  final String anotherTestField = 'Fixed'
                }

                sb = new VetoableTestBean12()
                int changed = 0
                sb.vetoableChange = {changed++}
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
                import groovy.beans.Vetoable

                @Vetoable final class VetoableTestBean12  {
                  String testField
                  final String anotherTestField = 'Fixed'
                }

                sb = new VetoableTestBean12()
                int changed = 0
                sb.vetoableChange = {changed++}
                sb.testField = 'newValue'
                assert changed == 1

                sb.anotherTestField = 'Changed'
            """)
        }
    }

    void testClassMarkers() {
        for (int i = 0; i < 31; i++) {
            boolean bindField  = i & 1
            boolean bindClass  = i & 2
            boolean vetoField  = i & 4
            boolean vetoClass  = i & 8
            boolean staticField = i & 16
            int vetoCount = vetoClass?(staticField?4:5):(vetoField?2:0);
            int bindCount = bindClass?(staticField?4:5):(bindField?2:0);

            String script = """
                    import groovy.beans.Bindable
                    import groovy.beans.Vetoable

                    ${vetoClass?'@Vetoable ':''}${bindClass?'@Bindable ':''}class ClassMarkerBean$i {
                        String neither

                        ${vetoField?'@Vetoable ':''}String veto

                        ${bindField?'@Bindable ':''}String bind

                        ${vetoField?'@Vetoable ':''}${bindField?'@Bindable ':''}String both

                        ${staticField?'static ':''}String staticField
                    }

                    cb = new ClassMarkerBean$i(neither:'a', veto:'b', bind:'c', both:'d', staticField:'e')
                    vetoCount = 0
                    bindCount = 0
                    ${bindClass|bindField?'cb.propertyChange = { bindCount++ }':''}
                    ${vetoClass|vetoField?'cb.vetoableChange = { vetoCount++ }':''}
                    cb.neither = 'f'
                    cb.bind = 'g'
                    cb.veto = 'h'
                    cb.both = 'i'
                    cb.staticField = 'j'
                    assert vetoCount == $vetoCount
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

    void testGetVetoableChangeListeners() {
        assertScript """
            import groovy.beans.Vetoable
            import java.beans.VetoableChangeListener
            import java.beans.PropertyChangeEvent

            class VetoableTestBean14 {
                @Vetoable String foo
                @Vetoable String bar
            }

            class FooVetoListener implements VetoableChangeListener {
               void vetoableChange( PropertyChangeEvent e ) { }
            }

            sb = new VetoableTestBean14()
            assert !sb.vetoableChangeListeners
            listener = new FooVetoListener()
            sb.addVetoableChangeListener("foo",listener)
            assert !sb.getVetoableChangeListeners("bar")
            assert sb.getVetoableChangeListeners("foo") == [listener]
            assert sb.vetoableChangeListeners.size() == 1
        """
    }

    void testPropertyChangeMethodWithCompileStatic() {
        assertScript """
            import groovy.beans.Vetoable
            import groovy.transform.CompileStatic

            @CompileStatic
            class MyBean {
                @Vetoable String test = "a test"
            }
            assert new MyBean()
        """
    }

    void testVetoableGeneratedMethodsAreAnnotatedWithGenerated_GROOVY9052() {
        def person = evaluate('''
            @groovy.beans.Vetoable
            class Person {
                String firstName

                void setFirstName(String fn) {
                    this.firstName = fn.toUpperCase()
                }

                def zipCode
             }
             new Person()
        ''')

        person.class.declaredMethods.each { m ->
            if (m.name.contains('VetoableChange') || m.name in ['setZipCode']) {
                assert m.annotations*.annotationType().name.contains('groovy.transform.Generated')
            } else if (m.name in ['setFirstName']) {
                // wrapped methods should not be marked since they contain non-generated logic
                assert !m.annotations*.annotationType().name.contains('groovy.transform.Generated')
            }
        }
    }
}
