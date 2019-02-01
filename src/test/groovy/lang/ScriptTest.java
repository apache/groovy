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
package groovy.lang;

import org.codehaus.groovy.classgen.TestSupport;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.IOException;

/**
 * Tests some particular script features.
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

    /**
     * GROOVY-6582 : Script.invokeMethod bypasses getProperty when looking for closure-valued properties.
     *
     * Make sure that getProperty and invokeMethod are consistent.
     *
     */
    public void testGROOVY_6582() {
        String script = "" +
            "abstract class DeclaredBaseScript extends Script {\n" +
            "   def v = { it * 2 }\n" +
            "   def z = { it * 3 }\n" +
            "   def getProperty(String n) { n == 'c' ? v : super.getProperty(n) }\n" +
            "}\n" +
            "@groovy.transform.BaseScript DeclaredBaseScript baseScript\n" +
            "assert c(2) == 4\n" +
            "assert z(2) == 6";

        GroovyShell shell = new GroovyShell();
        shell.evaluate(script);
    }

    // GROOVY-6344
    public void testScriptNameMangling() {
        String script = "this.getClass().getName()";
        GroovyShell shell = new GroovyShell();
        String name = (String) shell.evaluate(script,"a!b");
        assertEquals("a_b", name);
    }

}
