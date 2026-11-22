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

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.FormatHelper
import org.junit.jupiter.api.Test

import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Formatting a value must not initialize {@code ResolveVisitor}, and the
 * default-import scan must wait until a type is resolved.
 * <p>
 * Each case runs {@link Probe} in a fresh JVM, because a class is initialized
 * once per JVM and the test JVM has already compiled code. The probe reports
 * which classes its JVM has loaded. {@code ClassFinder} stands for the scan:
 * its only caller is the scan in {@code Java9.getDefaultImportClasses}.
 */
final class Groovy9646 {

    private static final String RESOLVE_VISITOR = 'org.codehaus.groovy.control.ResolveVisitor'
    private static final String SCAN = 'org.codehaus.groovy.vmplugin.v9.ClassFinder'

    // GROOVY-9646
    @Test
    void testFormattingDoesNotInitializeResolveVisitor() {
        Map<String, String> probe = runProbe('format')

        assertEquals('[1, a, [k:2.5]]', probe.toString, probe.output)
        assertEquals("[1, 'a', ['k':2.5]]", probe.inspect, probe.output)
        assertEquals('false', probe[RESOLVE_VISITOR], "formatting a value loaded ResolveVisitor\n$probe.output")
        assertEquals('false', probe[SCAN], "formatting a value ran the default-import scan\n$probe.output")
    }

    // GROOVY-9646
    @Test
    void testMissingMethodMessageDoesNotScanDefaultImports() {
        Map<String, String> probe = runProbe('missingMethod')

        // the short type names show that the default imports were consulted
        assertTrue(probe.message.contains('argument types: (Integer, String, java.util.concurrent.TimeUnit)'), probe.output)
        assertEquals('false', probe[SCAN], "building the message ran the default-import scan\n$probe.output")
    }

    // GROOVY-9646
    @Test
    void testCompilationStillResolvesDefaultImports() {
        Map<String, String> probe = runProbe('compile')

        assertEquals('ArrayList|PT24H|Tuple2|Expando', probe.result, probe.output)
        // also shows that the scan signal used above does appear when the scan runs
        assertEquals('true', probe[SCAN], "compiling did not run the default-import scan\n$probe.output")
    }

    /**
     * Runs {@link Probe} in a fresh JVM and returns the {@code key=value} lines
     * it printed, plus its whole output under {@code output}.
     */
    private static Map<String, String> runProbe(String scenario) {
        def javaBin = new File(System.getProperty('java.home'), 'bin/java').absolutePath
        def proc = new ProcessBuilder(javaBin,
                '--add-opens=java.base/java.lang=ALL-UNNAMED', // for ClassLoader#findLoadedClass
                '-cp', System.getProperty('java.class.path'),
                Probe.name, scenario).start()
        def out = new StringBuffer(), err = new StringBuffer()
        Thread outReader = proc.consumeProcessOutputStream(out)
        Thread errReader = proc.consumeProcessErrorStream(err)
        if (!proc.waitFor(2, TimeUnit.MINUTES)) {
            proc.destroyForcibly()
            throw new AssertionError("probe '$scenario' timed out\nout: $out\nerr: $err")
        }
        outReader.join()
        errReader.join()
        String output = "out: $out\nerr: $err"
        assertEquals(0, proc.exitValue(), "probe '$scenario' failed\n$output")

        Map<String, String> result = [output: output]
        out.toString().eachLine { String line ->
            int eq = line.indexOf('=')
            if (eq > 0) result[line.substring(0, eq)] = line.substring(eq + 1)
        }
        return result
    }

    /**
     * Runs one scenario and prints what it produced, then which of the classes
     * of interest the JVM has loaded. It is statically compiled and prints
     * through {@code PrintStream}, so that nothing but the scenario formats a
     * value or resolves a type.
     */
    @CompileStatic
    static class Probe {
        static void main(String[] args) {
            switch (args[0]) {
                case 'format':
                    List<Object> value = [1, 'a', [k: 2.5]]
                    System.out.println('toString=' + FormatHelper.toString(value))
                    System.out.println('inspect=' + FormatHelper.inspect(value))
                    break
                case 'missingMethod':
                    Object[] arguments = [1, 'a', TimeUnit.DAYS]
                    String message = new MissingMethodException('foo', String, arguments).message
                    System.out.println('message=' + message.readLines().first())
                    break
                case 'compile':
                    Object result = new GroovyShell().evaluate('''
                        [new ArrayList(), Duration.ofDays(1), new Tuple2(1, 2), new Expando()].collect {
                            it instanceof Duration ? it.toString() : it.class.simpleName
                        }.join('|')
                    ''')
                    System.out.println('result=' + result)
                    break
                default:
                    throw new IllegalArgumentException(args[0])
            }

            Method findLoadedClass = ClassLoader.getDeclaredMethod('findLoadedClass', String)
            findLoadedClass.accessible = true
            for (String name : [RESOLVE_VISITOR, SCAN]) {
                System.out.println(name + '=' + (findLoadedClass.invoke(Probe.classLoader, name) != null))
            }
        }
    }
}
