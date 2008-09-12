/*
 * Copyright 2003-2008 the original author or authors.
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
package org.codehaus.groovy.transform.vm5

/**
 * @author Danno.Ferrin
 * @author Alex Tkachman
 */
class GlobalTransformTest extends GroovyTestCase {

    GroovyShell shell;

    URL transformRoot = new File(getClass().classLoader.
            getResource("org/codehaus/groovy/transform/vm5/META-INF/services/org.codehaus.groovy.transform.ASTTransformation").
            toURI()).parentFile.parentFile.parentFile.toURL()

    protected void setUp() {
        super.setUp();
        shell = new GroovyShell();
    }

    protected void tearDown() {
        shell = null;
        super.tearDown();
    }

    void testGlobalTransform() {
        shell.classLoader.addURL(transformRoot)
        shell.evaluate("""
            import org.codehaus.groovy.control.CompilePhase

            if (org.codehaus.groovy.transform.vm5.TestTransform.phases == [CompilePhase.CONVERSION, CompilePhase.CLASS_GENERATION]) {
               println "Phase sync bug fixed"
            } else if (org.codehaus.groovy.transform.vm5.TestTransform.phases == [CompilePhase.CONVERSION, CompilePhase.INSTRUCTION_SELECTION]) {
               println "Phase sync bug still present"
            } else {
               assert false, "FAIL"
            }
        """)
    }
}