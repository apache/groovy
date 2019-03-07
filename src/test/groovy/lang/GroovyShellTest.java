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

import groovy.util.GroovyTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GroovyShellTest extends GroovyTestCase {

    private String script1 = "test = 1";

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(GroovyShellTest.class);
    }

    public void testExecuteScript() {
        GroovyShell shell = new GroovyShell();
        try {
            Object result = shell.evaluate(script1, "Test.groovy");
            assertEquals(new Integer(1), result);
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    private static class PropertyHolder {
        private Map map = new HashMap();

        public void set(String key, Object value) {
            map.put(key, value);
        }

        public Object get(String key) {
            return map.get(key);
        }
    }

    private String script2 = "test.prop = 2\nreturn test.prop";

    public void testExecuteScriptWithContext() {
        Binding context = new Binding();
        context.setVariable("test", new PropertyHolder());
        GroovyShell shell = new GroovyShell(context);
        try {
            Object result = shell.evaluate(script2, "Test.groovy");
            assertEquals(new Integer(2), result);
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    public void testScriptWithDerivedBaseClass() throws Exception {
        Binding context = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(DerivedScript.class.getName());
        GroovyShell shell = new GroovyShell(context, config);
        Object result = shell.evaluate("x = 'abc'; doSomething(cheese)");
        assertEquals("I like Cheddar", result);
        assertEquals("abc", context.getVariable("x"));
    }

    /**
     * Test for GROOVY-6615
     * @throws Exception
     */
    public void testScriptWithCustomBodyMethod() throws Exception {
        Binding context = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(BaseScriptCustomBodyMethod.class.getName());
        GroovyShell shell = new GroovyShell(context, config);
        Object result = shell.evaluate("'I like ' + cheese");
        assertEquals("I like Cheddar", result);
    }

    public void testClassLoader() {
        Binding context = new Binding();
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(DerivedScript.class.getName());
        GroovyShell shell = new GroovyShell(context, config);
        String script = "evaluate '''\n"+
                        "class XXXX{}\n"+
                        "assert evaluate('XXXX') == XXXX\n"+
                        "'''";
        shell.evaluate(script);
     
    }

    public void testWithGCSWithURL() throws Exception {
        String scriptFileName = "src/test/groovy/bugs/GROOVY3934Helper.groovy";
        File helperScript = new File(scriptFileName);
        if(!helperScript.exists()) {
            fail("File " + scriptFileName + " does not exist");
        } else {
            URL url = helperScript.toURI().toURL();
            GroovyCodeSource gcs = new GroovyCodeSource(url);
            GroovyShell shell = new GroovyShell();
            Object result = shell.evaluate(gcs);
            assertEquals("GROOVY3934Helper script called", result);
        }
    }
    
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
        System.setOut( new PrintStream(new ByteArrayOutputStream()) );
        try {
            // makes this more of an integration test than a unit test...
            GroovyShell.main( new String[] { testSuite.getCanonicalPath() });
        } finally {
            System.setOut( out );
        }
    } 
}
