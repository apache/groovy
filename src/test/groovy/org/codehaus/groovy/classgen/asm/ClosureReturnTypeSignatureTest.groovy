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
package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.junit.jupiter.api.Assertions.assertThrows

final class ClosureReturnTypeSignatureTest {

    private static final String PACKED = ClosureWriter.PACKED_METHOD_PREFIX

    private static CompilationUnit compile(File targetDir, List<File> sources, File classpath = null) {
        def loader = new GroovyClassLoader(ClosureReturnTypeSignatureTest.classLoader)
        if (classpath) loader.addClasspath(classpath.absolutePath)
        def unit = new CompilationUnit(new CompilerConfiguration(targetDirectory: targetDir), null, loader)
        unit.addSources(sources as File[])
        unit.compile()
        unit
    }

    private static byte[] bytes(CompilationUnit unit, String className) {
        unit.classes.find { it.name == className }.bytes
    }

    /** Calls back with the name and the "descriptor signature" of every method of the class. */
    private static void eachMethod(byte[] bytes, Closure callback) {
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                callback(name, "$descriptor $signature".toString())
                null
            }
        }, ClassReader.SKIP_CODE)
    }

    private static String classSignature(byte[] bytes) {
        String[] found = new String[1]
        new ClassReader(bytes).accept(new ClassVisitor(Opcodes.ASM9) {
            @Override
            void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                found[0] = signature
            }
        }, ClassReader.SKIP_CODE)
        found[0]
    }

    private static List<String> doCallSignatures(byte[] bytes) {
        List<String> signatures = []
        eachMethod(bytes) { name, signature -> if (name == 'doCall') signatures << signature }
        signatures
    }

    /** The signatures of the hoisted methods of the packed closures of a class, by method name. */
    private static Map<String, String> packedSignatures(byte[] bytes) {
        Map<String, String> signatures = [:]
        eachMethod(bytes) { name, signature -> if (name.startsWith(PACKED)) signatures[name] = signature }
        signatures
    }

    /**
     * Compiles a class with closures whose inferred return types refer to a class of the compilation
     * unit, once together with that class and once against it compiled, and passes both compilation
     * units to the check.
     */
    private static void compileTogetherAndSeparately(String annotations, Closure check) {
        File sourceDir = File.createTempDir()
        File targetDir = File.createTempDir()
        try {
            def up = new File(sourceDir, 'Up.groovy')
            up.write 'interface Up {}'
            def res = new File(sourceDir, 'Res.groovy')
            res.write 'class Res<T> { List<T> items = [] }'
            def svc = new File(sourceDir, 'Svc.groovy')
            svc.write """
                $annotations
                class Svc {
                    static <V> V timeIt(Closure<V> body) { body.call() }
                    def <U extends Up> Res<U> read(Class<U> type) { new Res<U>() }
                    <U extends Up> Res<U> generic(Class<U> type) { timeIt { -> read(type) } }
                    Res<Up> concrete(Class<Up> type) { timeIt { -> read(type) } }
                    Res raw() { timeIt { -> new Res() } }
                    Svc self() { timeIt { -> this } }
                }
            """
            def together = compile(new File(targetDir, 'together'), [up, res, svc])

            File first = new File(targetDir, 'first')
            compile(first, [up, res])
            def separate = compile(new File(targetDir, 'separate'), [svc], first)

            check(together, separate)
        } finally {
            sourceDir.deleteDir()
            targetDir.deleteDir()
        }
    }

    // the doCall method of a statically compiled closure has the same signature whether
    // the class of its inferred return type is in the same compilation unit or not
    @Test
    void testInferredReturnTypeOfClassInSameOrOtherCompilationUnit() {
        compileTogetherAndSeparately('@groovy.transform.CompileStatic') { together, separate ->
            def closures = together.classes*.name.findAll { it.startsWith('Svc$') }.sort()
            assert closures.size() == 4
            for (name in closures) {
                assert doCallSignatures(bytes(together, name)) == doCallSignatures(bytes(separate, name)) : name
            }
            assert doCallSignatures(bytes(together, 'Svc$_generic_closure1')) == ['()LRes; null']
            assert doCallSignatures(bytes(together, 'Svc$_concrete_closure2')) == ['()LRes; ()LRes<LUp;>;']
            assert doCallSignatures(bytes(together, 'Svc$_raw_closure3')) == ['()LRes; null']
            assert doCallSignatures(bytes(together, 'Svc$_self_closure4')) == ['()LSvc; null']
        }
    }

    // the method a packed closure is hoisted to has the return type the doCall method of its
    // closure class would have, including the type arguments
    @Test
    void testInferredReturnTypeOfPackedClosure() {
        compileTogetherAndSeparately('@groovy.transform.CompileStatic @groovy.transform.PackedClosures') { together, separate ->
            assert together.classes*.name.sort() == ['Res', 'Svc', 'Up'] // no closure classes: all packed
            def packed = packedSignatures(bytes(together, 'Svc'))
            def packedSeparately = packedSignatures(bytes(separate, 'Svc'))
            assert packed == packedSeparately
            assert packed == [
                (PACKED + '0'): '(Ljava/lang/Class;)LRes; null', // generic
                (PACKED + '1'): '(Ljava/lang/Class;)LRes; (Ljava/lang/Class;)LRes<LUp;>;', // concrete
                (PACKED + '2'): '()LRes; null', // raw
                (PACKED + '3'): '()LSvc; null', // self
            ]
        }
    }

    // JLS 4.5: Serializable declares no type parameters, so a generic class that
    // implements it does not construct Serializable<T>.
    @Test
    void serializableStaysRawOnAGenericClass() {
        File dir = File.createTempDir()
        try {
            def holder = new File(dir, 'Holder.groovy')
            holder.write '''
                class Holder<T> implements java.io.Serializable {
                    T value
                }
            '''
            def unit = compile(dir, [holder])
            String signature = classSignature(bytes(unit, 'Holder'))
            assert signature != null
            assert signature.contains('Ljava/io/Serializable;')
            assert !signature.contains('Serializable<')
            def face = unit.ast.classes.find { it.name == 'Holder' }.interfaces.find { it.name == 'java.io.Serializable' }
            assert face != null
            assert face.genericsTypes == null || face.genericsTypes.length == 0
        } finally {
            dir.deleteDir()
        }
    }

    // JLS 4.5: a use site may not parameterize a class that declares no type
    // parameters. GenericsVisitor rejects the arguments.
    @Test
    void nonGenericUseSiteIsRejected() {
        File dir = File.createTempDir()
        try {
            def holder = new File(dir, 'Holder.groovy')
            holder.write '''
                class Holder<T> {
                    java.io.Serializable<T> box
                    Raw<String> raw
                }
                class Raw {}
            '''
            def ex = assertThrows(MultipleCompilationErrorsException) {
                compile(dir, [holder])
            }
            assert ex.message.contains('Serializable')
            assert ex.message.contains('Raw')
            assert ex.message.contains('(supplied with 1 type parameter)')
            assert ex.message.contains('which takes no parameters')
        } finally {
            dir.deleteDir()
        }
    }

    // JLS 4.5 / javac: of(1, 2) infers T as Integer. The call type is
    // List<Integer>. An int[] is not a T[] (javac rejects that call).
    @Test
    void primitiveArgumentIsBoxedInTheInferredType() {
        File dir = File.createTempDir()
        try {
            def source = new File(dir, 'Boxes.groovy')
            source.write '''
                @groovy.transform.CompileStatic
                class Boxes {
                    static <T> java.util.List<T> of(T a, T b) { null }
                    static <T> java.util.List<? extends T> ext(T value) { null }
                    static Object list() { of(1, 2) }
                    static Object bound() { ext(1) }
                }
            '''
            def unit = compile(dir, [source])
            def calls = [:]
            unit.ast.classes.find { it.name == 'Boxes' }.methods.findAll { it.name in ['list', 'bound'] }.each { method ->
                method.code.visit(new ClassCodeVisitorSupport() {
                    @Override
                    void visitMethodCallExpression(MethodCallExpression expression) {
                        if (expression.methodAsString in ['of', 'ext']) calls[expression.methodAsString] = expression
                        super.visitMethodCallExpression(expression)
                    }
                    @Override
                    protected SourceUnit getSourceUnit() { null }
                })
            }
            def ofType = calls.of.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE)
            assert ofType.name == 'java.util.List'
            assert ofType.genericsTypes[0].type.name == 'java.lang.Integer'
            assert !ClassHelper.isPrimitiveType(ofType.genericsTypes[0].type)
            def extType = calls.ext.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE)
            def bound = extType.genericsTypes[0]
            assert bound.wildcard
            def upper = bound.upperBounds ? bound.upperBounds[0] : bound.type
            assert upper.name == 'java.lang.Integer'
            assert !ClassHelper.isPrimitiveType(upper)
            eachMethod(bytes(unit, 'Boxes')) { name, methodSignature ->
                assert !methodSignature.contains('<I') : methodSignature
                assert !methodSignature.contains('+I') : methodSignature
            }
        } finally {
            dir.deleteDir()
        }
    }
}
