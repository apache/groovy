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
package groovy.security;

import groovy.lang.GroovyCodeSource;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Security;
import java.util.PropertyPermission;

/**
 * Test the effects of enabling security in Groovy.  Some tests below check for proper framework
 * behavior (e.g. ensuring that GroovyCodeSources may only be created for which proper permissions exist).
 * Other tests run .groovy scripts under a secure environment and ensure that the proper permissions
 * are required for success.
 */
public class SecurityTest extends SecurityTestSupport {

    public void testForbiddenProperty() {
        String script = "System.getProperty(\"user.home\")";
        assertExecute(script, null, new PropertyPermission("user.home", "read"));
    }

    public void testForbiddenPackage() {
        String script = "import sun.net.*; s = new NetworkClient()";
        assertExecute(script, "/groovy/security/testForbiddenPackage", new RuntimePermission("accessClassInPackage.sun.*"));
    }

    public void testForbiddenCodebase() {
        assertExecute(new File("src/test/groovy/security/forbiddenCodeBase.gvy"), new GroovyCodeSourcePermission("/groovy/security/forbiddenCodeBase"));
    }

    public void testForbiddenCodebaseWithActions() {
        assertExecute(new File("src/test/groovy/security/forbiddenCodeBase.gvy"), new GroovyCodeSourcePermission("/groovy/security/forbiddenCodeBase", "unused actions string"));
    }

    //Check that the Security package.access control works.
    public void testPackageAccess() {
        String script = "new javax.print.PrintException();";
        // Use our privileged access in order to prevent checks lower in the call stack.  Otherwise we would have
        // to grant access to IDE unit test runners and unit test libs.  We only care about testing the call stack
        // higher upstream from this point of execution.
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                Security.setProperty("package.access", "javax.print");
                return null;
            }
        });
        //This should throw an ACE because its codeBase does not allow access to javax.print
        assertExecute(script, "/groovy/security/javax/print/deny", new RuntimePermission("accessClassInPackage.javax.print"));
        //This should not throw an ACE because groovy.policy grants the codeBase access to javax.print
        assertExecute(script, "/groovy/security/javax/print/allow", null);
    }

    public void testBadScriptNameBug() {
        assertExecute(new File("src/test/groovy/bugs/BadScriptNameBug.groovy"), null);
    }

    public void testClosureMethodTest() {
        assertExecute(new File("src/test/groovy/ClosureMethodTest.groovy"), null);
    }

    public void testClosureWithDefaultParamTest() {
        assertExecute(new File("src/test/groovy/ClosureWithDefaultParamTest.groovy"), null);
    }

    public void testScriptTest() {
        assertExecute(new File("src/test/groovy/script/ScriptTest.groovy"), null);
    }

    public void testConstructorBug() {
        assertExecute(new File("src/test/groovy/bugs/ConstructorBug.groovy"), null);
    }

    //Mailing list post by Richard Hensley reporting a CodeSource bug.  A GroovyCodeSource created
    //with a URL was causing an NPE.
    public void testCodeSource() throws IOException, CompilationFailedException {
        URL script = loader.getResource("groovy/ArrayTest.groovy");
        try {
            new GroovyCodeSource(script);
        } catch (RuntimeException re) {
            assertEquals("Could not construct a GroovyCodeSource from a null URL", re.getMessage());
        }
    }

}
