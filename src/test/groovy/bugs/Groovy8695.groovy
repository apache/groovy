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
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * GROOVY-8695: {@code @Delegate} must emit methods in a deterministic order
 * across repeated compilations of the same source.
 */
final class Groovy8695 {

    private static final String SRC = '''
        class DelegateTarget {
            void alpha() {}
            void beta(String s) {}
            int gamma(int a, int b) { a + b }
            def delta() { 'd' }
            def epsilon(Object o) { o }
            void zeta() {}
            void eta() {}
            void theta(List x) {}
            String iota() { 'i' }
            void kappa(String a, int b) {}
        }
        class DelegateHost {
            @Delegate DelegateTarget t = new DelegateTarget()
            def own() { 1 }
        }
    '''

    @Test
    void testDelegateMethodOrderIsDeterministic() {
        def orders = (1..8).collect { compileHostMethodDescriptors() }
        assert orders.toSet().size() == 1
        def expected = ['alpha', 'beta', 'delta', 'epsilon', 'eta', 'gamma', 'iota', 'kappa', 'theta', 'zeta']
        def got = orders[0].findAll { it.replaceFirst(/\(.*/, '') in expected }
        assert got.collect { it.replaceFirst(/\(.*/, '') } == expected
    }

    private List<String> compileHostMethodDescriptors() {
        def outDir = File.createTempDir('Groovy8695-')
        try {
            def conf = new CompilerConfiguration()
            conf.targetDirectory = outDir
            def cu = new CompilationUnit(conf, null, new GroovyClassLoader(this.class.classLoader))
            cu.addSource('DelegateHost.groovy', SRC)
            cu.compile()
            def bytes = new File(outDir, 'DelegateHost.class').bytes
            def names = []
            new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                    if ((access & Opcodes.ACC_SYNTHETIC) == 0 && name != '<clinit>' && !name.startsWith('$')) {
                        names << (name + descriptorToSimple(descriptor))
                    }
                    return null
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES)
            return names
        } finally {
            outDir.deleteDir()
        }
    }

    private static String descriptorToSimple(String descriptor) {
        // (Ljava/lang/String;I)V -> (java.lang.String, int) style is unnecessary;
        // keep JVM descriptor for a stable exact comparison.
        return descriptor
    }
}
