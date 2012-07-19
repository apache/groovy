/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.codehaus.groovy.runtime.m12n

import org.codehaus.groovy.tools.FileSystemCompiler;

public class ExtensionModuleHelperForTests {
    static void doInFork(String code) {
        doInFork("GroovyTestCase", code)
    }

    static void doInFork(String baseTestClass, String code) {
        File baseDir = FileSystemCompiler.createTempDir()
        File source = new File(baseDir, 'Temp.groovy')
        source << """import org.codehaus.groovy.runtime.m12n.*
    class TempTest extends $baseTestClass {
        void testCode() {
            $code
        }
    }
    org.junit.runner.JUnitCore.main('TempTest')
"""
        def cl = ExtensionModuleHelperForTests.classLoader
        while (!(cl instanceof URLClassLoader)) {
            cl = cl.parent
            if (cl ==null) {
                throw new RuntimeException("Unable to find class loader")
            }
        }
        def cp = ((URLClassLoader)cl).URLs.collect{ new File(it.toURI()).absolutePath}
        cp << baseDir.absolutePath
        def ant = new AntBuilder()
        try {
            ant.with {
                taskdef(name:'groovyc', classname:"org.codehaus.groovy.ant.Groovyc")
                groovyc(srcdir: baseDir.absolutePath, destdir:baseDir.absolutePath, includes:'Temp.groovy', fork:true)
                java(   classname:'Temp',
                        fork:'true',
                        outputproperty: 'out',
                        errorproperty: 'err',
                        {
                            classpath {
                                cp.each {
                                    pathelement location: it
                                }
                            }
                        }
                )
            }
        } finally {
            String err = ant.project.properties.err
            baseDir.deleteDir()
            if (err) {
                throw new RuntimeException(err)
            }
        }
    }
}
