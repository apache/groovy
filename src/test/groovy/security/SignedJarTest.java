/**
 * Copyright 2003-2015 the original author or authors.
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
package groovy.security;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Read a .groovy file from a signed jar and verify that a policy file grant with a signedBy field
 * works.  The following steps were used to create and manage the keys used to sign and read the jar:
 * <ol>
 * <li>keytool -genkey -alias groovy -keypass keypass -keystore groovystore -storepass storepass -validity 7000
 * <li>keytool -export -keystore groovystore -alias groovy -file GroovyDev.cer
 * <li>keytool -import -alias groovy -file GroovyDev.cer -keystore groovykeys
 * </ol>
 * Once the keys are constructed, creat the jar and sign:
 * <ol>
 * <li>jar -cvf Groovy.jar groovy
 * <li>jarsigner -keystore groovystore -signedjar GroovyJarTest.jar Groovy.jar groovy
 * </ol>
 * Add the keystore to the policy file and write the grant:
 * <ol>
 * <li>keystore "file:${user.dir}/src/test/groovy/security/groovykeys";
 * </ol>
 */
public class SignedJarTest extends SecurityTestSupport {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(SignedJarTest.class);
    }

    public void testReadSignedJar() throws Exception {
        if (!isSecurityAvailable() || (notYetImplemented())) return;

        //spg 2006-02-09 The GroovyClassLoader code that checked jar files
        //for source files was removed last July.  This test will not function
        //without that capability.
        Class c = loader.loadClass("groovy.security.JarTest");  // ClassNotFoundException !
        executeTest(c, null);

    }
}
