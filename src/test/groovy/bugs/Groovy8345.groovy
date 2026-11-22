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

import groovy.ant.AntBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * GROOVY-8345: the Ant {@code groovy} task should be able to run a script
 * that imports lowercase script/class names from other packages, without
 * a ClassNodeResolver "lookup caused a failed compilation" BUG! error.
 */
final class Groovy8345 {

    @TempDir
    File tempDir

    @Test
    void testAntGroovyTaskWithLowercasePackageScripts() {
        new File(tempDir, 'one').mkdirs()
        new File(tempDir, 'two').mkdirs()
        new File(tempDir, 'test.groovy').text = '''
            import one.foo
            import two.bar
            bar.run()
        '''
        new File(tempDir, 'one/foo.groovy').text = '''
            package one
            class foo {
            }
            class foobar {
                foobar() {
                    new File(System.getProperty('groovy8345.out')).text = 'foobar'
                }
            }
        '''
        new File(tempDir, 'two/bar.groovy').text = '''
            package two
            import one.foobar
            class bar {
                static def run() {
                    new foobar()
                }
            }
        '''
        def out = new File(tempDir, 'out.txt')
        def previous = System.setProperty('groovy8345.out', out.absolutePath)
        try {
            def ant = new AntBuilder()
            ant.project.baseDir = tempDir
            ant.taskdef(name: 'groovy', classname: 'org.codehaus.groovy.ant.Groovy')
            ant.groovy(src: 'test.groovy')
            assert out.file
            assert out.text.trim() == 'foobar'
        } finally {
            if (previous == null) System.clearProperty('groovy8345.out')
            else System.setProperty('groovy8345.out', previous)
        }
    }
}
