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
package org.codehaus.groovy.runtime.m12n

import groovy.ant.AntBuilder

class ExtensionModuleHelperForTests {

    static void doInFork(String baseTestClass = 'groovy.test.GroovyTestCase', String code) {
        File baseDir = File.createTempDir()
        File sourceFile = new File(baseDir, 'Temp.groovy')
        sourceFile << """import org.codehaus.groovy.runtime.m12n.*
            class TempTest extends $baseTestClass {
                void testCode() {
                    $code
                }
            }
            org.junit.runner.JUnitCore.main('TempTest')
        """

        Set<String> cp = System.getProperty('java.class.path').split(File.pathSeparator) as Set
        cp << baseDir.absolutePath

        def ant = new AntBuilder()
        def allowed = [
            'Picked up JAVA_TOOL_OPTIONS: .*',
            'Picked up _JAVA_OPTIONS: .*'
        ]
        try {
            ant.with {
                taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc')
                groovyc(srcdir: baseDir.absolutePath, destdir: baseDir.absolutePath, includes: 'Temp.groovy', fork: true)
                java(classname: 'Temp', fork: 'true', outputproperty: 'out', errorproperty: 'err') {
                    classpath {
                        cp.each {
                            pathelement location: it
                        }
                    }
                }
            }
        } finally {
            baseDir.deleteDir()
            String out = ant.project.properties.out
            String err = ant.project.properties.err
            if (err && !allowed.any{ err.trim().matches(it) }) {
                throw new RuntimeException("$err\nClasspath: ${cp.join('\n')}")
            }
            if (out && (out.contains('FAILURES') || !out.contains('OK'))) {
                throw new RuntimeException("$out\nClasspath: ${cp.join('\n')}")
            }
        }
    }
}
