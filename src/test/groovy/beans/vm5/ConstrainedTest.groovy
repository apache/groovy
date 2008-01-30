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

package groovy.beans

import org.codehaus.groovy.control.CompilationFailedException

/**
 * @author Danno Ferrin (shemnon)
 */
class ConstrainedTest extends GroovyTestCase {

    public void testSimpleConstrainedProperty() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
            import groovy.beans.Constrained

            class SimpleBean {
                @Constrained String name
            }

            sb = new SimpleBean()
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

    public void testBoundConstrainedProperty() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate("""
            import groovy.beans.Bound
            import groovy.beans.Constrained

            class SimpleBean {
                @Bound @Constrained String name
            }

            sb = new SimpleBean()
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

    public void testExisingSetter() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class SimpleBean {
                    @groovy.beans.Constrained String name
                    void setName() { }
                }
            """)
        }
    }

    public void testOnField() {
        GroovyShell shell = new GroovyShell()
        shouldFail(CompilationFailedException) {
            shell.evaluate("""
                class SimpleBean {
                    public @groovy.beans.Constrained String name
                    void setName() { }
                }
            """)
        }
    }
}