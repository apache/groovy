/*
 * Copyright 2003-2010 the original author or authors.
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
package gls

import org.codehaus.groovy.control.CompilationFailedException

public class CompilableTestSupport extends GroovyTestCase {
    protected shouldNotCompile(String script) {
        try {
            GroovyClassLoader gcl = new GroovyClassLoader()
            gcl.parseClass(script, getTestClassName())
        } catch (CompilationFailedException cfe) {
            return cfe.message
        }
        fail("the compilation succeeded but should have failed")
    }

    protected void shouldCompile(String script) {
        GroovyClassLoader gcl = new GroovyClassLoader()
        gcl.parseClass(script, getTestClassName())
        assert true
    }

    protected void shouldFail(Class th = null, String script) {
        try {
            def shell = new GroovyShell()
            shell.evaluate(script, getTestClassName())
        } catch (Throwable thrown) {
            if (th == null) return
            if (th != thrown.getClass()) {
                fail "script should have thrown $th, but it did throw ${thrown.getClass()}"
            }
            return
        }
        fail "script should have failed, but succeeded"
    }
}