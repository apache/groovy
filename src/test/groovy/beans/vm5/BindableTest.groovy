/*
 * Copyright 2008 the original author or authors.
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

package groovy.beans.vm5

import org.codehaus.groovy.control.CompilationFailedException

/**
 * @author Danno Ferrin (shemnon)
 */
class BindableTest extends GroovyTestCase {

    public void testSimpleBindableProperty() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
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
        """)
    }

    public void testMultipleBindableProperty() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
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
        """)
    }

    public void testExisingSetter() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class BindableTestBean3 {
                    @groovy.beans.Bindable String name
                    void setName() { }
                }
            """)
        }
    }

    public void testOnField() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class BindableTestBean4 {
                    public @groovy.beans.Bindable String name
                }
            """)
        }
    }

    public void testOnStaticField() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class BindableTestBean5 {
                     @groovy.beans.Bindable static String name
                }
            """)
        }
    }

    public void testClassMarkers() {
        for (int i = 0; i < 7; i++) {
            boolean bindField  = i & 1
            boolean bindClass  = i & 2
            boolean staticField  = i & 4
            int bindCount = bindClass?(staticField?2:3):(bindField?1:0);

            String script = """
                    import groovy.beans.Bindable

                    ${bindClass?'@Bindable ':''}class ClassMarkerBindable$i {
                        String neither

                        ${bindField?'@Bindable ':''}String bind

                        ${staticField?'static ':''}String staticString
                    }

                    cb = new ClassMarkerBindable$i(neither:'a', bind:'b', staticString:'c')
                    bindCount = 0
                    ${bindClass|bindField?'cb.propertyChange = { bindCount++ }':''}
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
}