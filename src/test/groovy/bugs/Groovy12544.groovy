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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

final class Groovy12544 {

    private static CompilationUnit compile(File targetDir, List<File> sources, File classpath = null) {
        def loader = new GroovyClassLoader(Groovy12544.classLoader)
        if (classpath) loader.addClasspath(classpath.absolutePath)
        def unit = new CompilationUnit(new CompilerConfiguration(targetDirectory: targetDir), null, loader)
        unit.addSources(sources as File[])
        unit.compile()
        unit
    }

    private static byte[] bytes(CompilationUnit unit, String className) {
        unit.classes.find { it.name == className }.bytes
    }

    private static List<String> innerClassEntries(byte[] bytes) {
        List<String> entries = []
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            void visitInnerClass(String name, String outerName, String innerName, int access) {
                entries << "$name of $outerName".toString()
            }
        }, ClassReader.SKIP_CODE)
        entries
    }

    // a class referring to a nested class gets the same InnerClasses entries
    // whether or not the nested class is in the same compilation unit
    @Test
    void testInnerClassesEntriesForPrecompiledNestedClass() {
        File sourceDir = File.createTempDir()
        File targetDir = File.createTempDir()
        try {
            def a = new File(sourceDir, 'A.groovy')
            a.write '''
                class A { static class B { static class C {} } }
            '''
            def d = new File(sourceDir, 'D.groovy')
            d.write '''
                class D { def m() { new A.B.C(); new AbstractMap.SimpleEntry(1, 2) } }
            '''
            def s = new File(sourceDir, 'S.groovy')
            s.write '''
                new A.B()
            '''
            def together = compile(new File(targetDir, 'together'), [a, d, s])

            File first = new File(targetDir, 'first')
            compile(first, [a])
            def separate = compile(new File(targetDir, 'separate'), [d, s], first)

            assert innerClassEntries(bytes(separate, 'D')) == [
                'A$B of A',
                'A$B$C of A$B',
                'java/util/AbstractMap$SimpleEntry of java/util/AbstractMap'
            ]
            assert innerClassEntries(bytes(separate, 'S')) == ['A$B of A']
            for (name in ['D', 'S']) {
                assert bytes(together, name) == bytes(separate, name)
            }
        } finally {
            sourceDir.deleteDir()
            targetDir.deleteDir()
        }
    }
}
