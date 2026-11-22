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
package bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * GROOVY-8814: joint-compilation stubs must not leave an
 * {@code @AnnotationCollector} such as {@code @CompileDynamic} in place
 * for javac to reject; the collected annotations should appear instead.
 */
final class Groovy8814 {

    @TempDir
    File tempDir

    @Test
    void testAnnotationCollectorExpandedInStub() {
        def srcDir = new File(tempDir, 'src')
        def stubDir = new File(tempDir, 'stubs')
        def target = new File(tempDir, 'classes')
        srcDir.mkdirs(); stubDir.mkdirs(); target.mkdirs()

        new File(srcDir, 'Service.groovy').text = '''
            import groovy.transform.CompileStatic
            import groovy.transform.CompileDynamic
            @CompileStatic
            class Service {
                @CompileDynamic
                List<String> dynamic() {
                    []
                }
            }
        '''
        new File(srcDir, 'UseService.java').text = '''
            public class UseService {
                public static void main(String[] args) {
                    Service s = new Service();
                    System.out.println(s.dynamic());
                }
            }
        '''

        def config = new CompilerConfiguration(
            targetDirectory: target,
            jointCompilationOptions: [stubDir: stubDir, keepStubs: true]
        )
        def cu = new JavaAwareCompilationUnit(config, new GroovyClassLoader(this.class.classLoader))
        cu.addSources(new File(srcDir, 'Service.groovy'), new File(srcDir, 'UseService.java'))
        cu.compile()

        def stub = new File(stubDir, 'Service.java')
        assert stub.file
        def text = stub.text
        assert !text.contains('CompileDynamic')
        assert text.contains('CompileStatic(value=groovy.transform.TypeCheckingMode.SKIP)')
        assert new File(target, 'Service.class').file
        assert new File(target, 'UseService.class').file
    }
}
