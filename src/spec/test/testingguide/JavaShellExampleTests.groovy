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
package testingguide

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class JavaShellExampleTests {

    @Test
    void testJavaFixture() {
        assertScript '''// tag::javashell_test_fixture[]
import org.apache.groovy.util.JavaShell

def js = new JavaShell()
js.compile('fixtures.PojoFixture', """
    package fixtures;
    public class PojoFixture {
        private final String name;
        public PojoFixture(String name) { this.name = name; }
        public String getName() { return name; }
    }
""")                                                                            // <1>

// share JavaShell\'s class loader so Groovy code under test sees the fixture
def shell = new GroovyShell(js.classLoader)                                     // <2>
shell.evaluate """
    def bean = fixtures.PojoFixture.newInstance('Groovy')
    assert bean.name == 'Groovy'
"""                                                                             // <3>
// end::javashell_test_fixture[]
'''
    }

    @Test
    void testBytecodeCapture() {
        assertScript '''// tag::javashell_bytecode_capture[]
import org.apache.groovy.util.JavaShell
import java.nio.file.Files

def out = Files.createTempDirectory('bc-')
try {
    new JavaShell().compileAllTo('bc.Calc', """
        package bc;
        public class Calc { public static int add(int a, int b) { return a + b; } }
    """, out)                                                                   // <1>
    byte[] javaBytes = Files.readAllBytes(out.resolve('bc/Calc.class'))         // <2>
    // Feed javaBytes (and bytes for the equivalent Groovy source) into ASM,
    // javap, or your bytecode-diff tool of choice for an equivalence assertion. // <3>
    assert javaBytes.length > 0
    assert javaBytes[0..3] == [(byte)0xCA, (byte)0xFE, (byte)0xBA, (byte)0xBE]
} finally {
    out.toFile().deleteDir()
}
// end::javashell_bytecode_capture[]
'''
    }
}
