/*
 * Copyright 2003-2011 the original author or authors.
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
package groovy.lang;

import org.codehaus.groovy.classgen.TestSupport;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;

/**
 * Tests some particular script features.
 *
 * @author Guillaume Laforge
 */
public class ScriptTest extends TestSupport {
    /**
     * When a method is not found in the current script, checks that it's possible to call a method closure from the binding.
     *
     * @throws IOException
     * @throws CompilationFailedException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public void testInvokeMethodFallsThroughToMethodClosureInBinding() throws IOException, CompilationFailedException, IllegalAccessException, InstantiationException {
        String text = "if (method() == 3) { println 'succeeded' }";

        GroovyCodeSource codeSource = new GroovyCodeSource(text, "groovy.script", "groovy.script");
        GroovyClassLoader loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader());
        Class clazz = loader.parseClass(codeSource);
        Script script = ((Script) clazz.newInstance());

        Binding binding = new Binding();
        binding.setVariable("method", new MethodClosure(new Dummy(), "method"));
        script.setBinding(binding);

        script.run();
    }

    public static class Dummy {
        public Integer method() {
            return new Integer(3);
        }
    }
}
