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
package groovy.annotations

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.*

class ParameterAnnotationTest extends GroovyTestCase {
    void testParameterAnnotation() {

        def gcl = new GroovyClassLoader()

        gcl.parseClass """
            import java.lang.annotation.*

            @Target(ElementType.METHOD)
            @Retention(RetentionPolicy.RUNTIME)
            @interface MethodAnnotation {}

            @Target(ElementType.PARAMETER)
            @Retention(RetentionPolicy.RUNTIME)
            @interface ParameterAnnotation {}

            interface MyInterface {
                @MethodAnnotation
                def method(@ParameterAnnotation def param)
            }
        """

        GroovyCodeSource codeSource = new GroovyCodeSource("""
            class MyInterfaceImpl implements MyInterface {
                def method(def param) {}
            }
        """, "script" + System.currentTimeMillis() + ".groovy", "/groovy/script")

        def cu = new CompilationUnit(CompilerConfiguration.DEFAULT, codeSource.codeSource, gcl)
        cu.addSource(codeSource.getName(), codeSource.scriptText);
        cu.compile(CompilePhase.FINALIZATION.phaseNumber)

        def classNode = cu.getClassNode("MyInterfaceImpl")
        def interfaceClassNode = classNode.getInterfaces().find { it.nameWithoutPackage == 'MyInterface' }

        def methodNode = interfaceClassNode.getDeclaredMethods("method")[0]

        // check if the AnnotationNode for 'MethodAnnotation' has been created
        assert methodNode.getAnnotations().any { an -> an.classNode.nameWithoutPackage == 'MethodAnnotation' }

        // check if the AnnotationNode for 'ParameterAnnotation' has been created
        assert methodNode.getParameters()[0].getAnnotations().any { an -> an.classNode.nameWithoutPackage == 'ParameterAnnotation' }
    }

    @Override
    protected void tearDown() {
        super.tearDown()
        new File("MyInterfaceImpl.class").deleteOnExit()
    }

}