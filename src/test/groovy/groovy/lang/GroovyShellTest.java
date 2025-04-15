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

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class GroovyShellTest {

    @Test
    public void testExecuteScript() {
        Object result = new GroovyShell().evaluate("test = 1", "Test.groovy");
        assertEquals(1, result);
    }

    @Test
    public void testExecuteScriptWithContext() {
        class PropertyHolder {
            private Map<String, Object> map = new HashMap<>();

            @SuppressWarnings("unused")
            public void set(String key, Object value) {
                map.put(key, value);
            }

            @SuppressWarnings("unused")
            public Object get(String key) {
                return map.get(key);
            }
        }

        Binding context = new Binding();
        context.setVariable("test", new PropertyHolder());
        String script = "test.prop = 2\nreturn test.prop";
        Object result = new GroovyShell(context).evaluate(script);
        assertEquals(2, result);
    }

    // GROOVY-228
    @Test
    public void testScriptWithDerivedBaseClass() {
        Binding context = new Binding();
        String script = "x = 'abc'; doSomething(cheese)";
        Object result = new GroovyShell(context, baseScript(BaseScript228.class)).evaluate(script);
        assertEquals("I like Cheddar", result);
        assertEquals("abc", context.getVariable("x"));
    }

    // GROOVY-6615
    @Test
    public void testScriptWithCustomBodyMethod() {
        String script = "'I like ' + cheese";
        Object result = new GroovyShell(baseScript(BaseScript6615.class)).evaluate(script);
        assertEquals("I like Cheddar", result);
    }

    // GROOVY-8096
    @Test
    public void testScriptWithBindingInitField() {
        String arg = "Hello Groovy";
        String script =
                "@groovy.transform.Field def script_args = getProperty('args')\n" +
                "assert script_args[0] == '" + arg + "'\n" +
                "script_args[0]\n";
        Object result = new GroovyShell(baseScript(BaseScript8096.class)).run(script, "Script8096.groovy", new String[]{arg});
        assertEquals(arg, result);
    }

    @Test
    public void testClassLoader() {
        String script =
                "evaluate '''\n"+
                "class XXXX{}\n"+
                "assert evaluate('XXXX') == XXXX\n"+
                "'''";
        new GroovyShell().evaluate(script);
    }

    // GROOVY-3934
    @Test
    public void testWithGCSWithURL() throws Exception {
        String scriptFileName = "src/test/groovy/bugs/scriptForGroovy3934.groovy";
        File helperScript = new File(scriptFileName);
        if (!helperScript.exists()) {
            fail("File " + scriptFileName + " does not exist");
        } else {
            URL url = helperScript.toURI().toURL();
            GroovyCodeSource gcs = new GroovyCodeSource(url);
            Object result = new GroovyShell().evaluate(gcs);
            assertEquals("GROOVY3934Helper script called", result);
        }
    }

    @Test
    public void testLaunchesJUnitTestSuite() throws Exception {
        // create a valid (empty) test suite on disk
        String testName = "GroovyShellTestJUnit3Test"+System.currentTimeMillis();
        File testSuite = new File(System.getProperty("java.io.tmpdir"), testName);
        ResourceGroovyMethods.write(testSuite, "import junit.framework.*; \r\n" +
                "public class " + testName + " extends TestSuite { \r\n" +
                "    public static Test suite() { \r\n" +
                "        return new TestSuite(); \r\n" +
                "    } \r\n" +
                "} \r\n");
        testSuite.deleteOnExit();

        PrintStream out = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            // makes this more of an integration test than a unit test...
            GroovyShell.main(new String[]{testSuite.getCanonicalPath()});
        } finally {
            System.setOut(out);
        }
    }

    //--------------------------------------------------------------------------

    private static CompilerConfiguration baseScript(Class<? extends Script> c) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(c.getName());
        return config;
    }

    public static abstract class BaseScript228  extends Script {
        public String doSomething(String food) {
            return "I like " + food;
        }
        public String getCheese() {
            return "Cheddar";
        }
    }

    public static abstract class BaseScript6615 extends Script {
        abstract protected Object runScriptBody();
        public String cheese = "Swiss";
        @Override
        public Object run() {
            cheese = "Cheddar";
            return runScriptBody();
        }
    }

    public static abstract class BaseScript8096 extends Script {
        protected BaseScript8096(Binding binding) {
            super(binding);
        }
        protected BaseScript8096() {
            super();
        }
    }
}
