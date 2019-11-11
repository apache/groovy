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
package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import static groovy.grape.Grape.resolve

final class Groovy9197 {

    @Test
    void testJointCompilationClasspathPropagation() {
        def uris = resolve(autoDownload:true, classLoader:new GroovyClassLoader(null),
            [groupId:'org.apache.commons', artifactId:'commons-lang3', version:'3.9'])

        def config = new CompilerConfiguration(
            classpath: new File(uris[0]).path,
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )

        def parentDir = File.createTempDir()
        try {
            def pojo = new File(parentDir, 'Pojo.java')
            pojo.write '''
                import static org.apache.commons.lang3.StringUtils.isEmpty;
                public class Pojo {
                    public static void main(String[] args) {
                        assert !isEmpty(" ");
                        assert isEmpty("");
                    }
                }
            '''

            def unit = new JavaAwareCompilationUnit(config)
            unit.addSources(pojo)
            unit.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
