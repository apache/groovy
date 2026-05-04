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

final class ExtensionModuleHelperForTests {

    private ExtensionModuleHelperForTests() {}

    static void doInFork(String baseTestClass = 'java.lang.Object', String code) {
        doInFork(baseTestClass, code, Collections.<String>emptyList())
    }

    static void doInFork(String baseTestClass, String code, List<String> extraJvmArgs) {
        File baseDir = File.createTempDir()
        File sourceFile = new File(baseDir, 'Temp.groovy')
        sourceFile << """import org.codehaus.groovy.runtime.m12n.*
            import static groovy.test.GroovyAssert.assertScript
            class TempTest extends $baseTestClass {
                @org.junit.jupiter.api.Test
                void testCode() {
                    $code
                }
            }

            import org.junit.platform.launcher.core.*
            import org.junit.platform.launcher.listeners.SummaryGeneratingListener
            import static org.junit.platform.engine.discovery.DiscoverySelectors.*

            def launcher = LauncherFactory.create()
            def listener = new SummaryGeneratingListener()
            launcher.registerTestExecutionListeners(listener)
            def testPlan = launcher.discover(
                LauncherDiscoveryRequestBuilder.request().selectors(
                    selectClass("TempTest")
                ).build()
            )
            launcher.execute(testPlan)
            // JUnit Platform's launcher is silent on failure; surface failures via
            // stderr so the parent process (doInFork) detects them as stray lines.
            def summary = listener.summary
            if (summary.totalFailureCount) {
                summary.failures.each { f ->
                    System.err.println('TEST FAILED: ' + f.testIdentifier.displayName + ' :: ' + f.exception)
                    f.exception.printStackTrace(System.err)
                }
            }
        """

        Set<String> cp = System.getProperty('java.class.path').split(File.pathSeparator) as Set
        cp << baseDir.absolutePath

        def ant = new AntBuilder()
        def allowed = [
            ~/Picked up JAVA_TOOL_OPTIONS: .*/,
            ~/Picked up _JAVA_OPTIONS: .*/
        ]
        def jvmArgs = []
        jvmArgs.addAll(extraJvmArgs)
        if (Runtime.version().feature() == 25) {
            // JEP 471/498: silence terminal-deprecation warnings for sun.misc.Unsafe
            // memory-access methods called from agents on the inherited classpath
            // (e.g. testlens's shaded protobuf UnsafeUtil::arrayBaseOffset).
            // remove when we can - this could mask errors we want to pick up
            jvmArgs << '--sun-misc-unsafe-memory-access=allow'
        }
        try {
            ant.with {
                // Compile via FileSystemCompilerFacade in a forked JVM (same as forked groovyc),
                // but using ant.java so we can attach arbitrary JVM args (e.g. system properties).
                java(classname: 'org.codehaus.groovy.ant.FileSystemCompilerFacade', fork: 'true', failonerror: 'true') {
                    jvmArgs.each { jvmarg(value: it) }
                    classpath {
                        cp.each { pathelement location: it }
                    }
                    arg(value: '--classpath')
                    arg(value: cp.join(File.pathSeparator))
                    arg(value: '-d')
                    arg(value: baseDir.absolutePath)
                    arg(value: sourceFile.absolutePath)
                }
                java(classname: 'Temp', fork: 'true', outputproperty: 'out', errorproperty: 'err') {
                    jvmArgs.each { jvmarg(value: it) }
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
            def stray = err?.readLines()?.findAll { line ->
                line.trim() && !allowed.any { line ==~ it }
            } ?: []
            if (stray) {
                throw new RuntimeException("${stray.join('\n')}\nClasspath: ${cp.join('\n')}")
            }
            if (out && (out.contains('FAILURES') || !out.contains('OK'))) {
                throw new RuntimeException("$out\nClasspath: ${cp.join('\n')}")
            }
        }
    }
}
