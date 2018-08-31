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
package org.codehaus.groovy.transform

class GlobalTransformTest extends GroovyTestCase {
    def path = "org/codehaus/groovy/transform/META-INF/groovy/org.codehaus.groovy.transform.ASTTransformation"
    URL transformRoot = new File(getClass().classLoader.getResource(path).toURI()).parentFile.parentFile.parentFile.toURI().toURL()

    void testGlobalTransform() {
        def shell = new GroovyShell()
        shell.classLoader.addURL(transformRoot)
        shell.evaluate("""
            import static org.codehaus.groovy.control.CompilePhase.*

            def ph = org.codehaus.groovy.transform.TestTransform.phases
            assert ph.TestTransformSemanticAnalysis == [SEMANTIC_ANALYSIS]
            assert ph.TestTransformClassGeneration == [CLASS_GENERATION]
        """)
    }
}
