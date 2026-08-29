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
package org.codehaus.groovy.classgen

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.codehaus.groovy.classgen.asm.BytecodeHelper
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.util.Printer

import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC
import static org.objectweb.asm.Opcodes.RETURN
import static org.objectweb.asm.Opcodes.V17
import static org.junit.jupiter.api.Assertions.assertEquals

class BytecodeHelperTest extends AbstractBytecodeTestCase {

    @Test
    void testTypeName() {
        assertEquals("[C", BytecodeHelper.getTypeDescription(ClassHelper.char_TYPE.makeArray()))
    }

    @Test
    void testMethodDescriptor() {
        String answer = BytecodeHelper.getMethodDescriptor(char[].class, new Class[0])
        assertEquals("()[C", answer)

        answer = BytecodeHelper.getMethodDescriptor(int.class, [long.class] as Class[])
        assertEquals("(J)I", answer)

        answer = BytecodeHelper.getMethodDescriptor(String[].class, [String.class, int.class] as Class[])
        assertEquals("(Ljava/lang/String;I)[Ljava/lang/String;", answer)
    }

    @Test
    void testMethodDescriptorMethodNode() {
        assertEquals("()V",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))

        assertEquals("()Ljava/lang/String;",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))

        assertEquals("()B",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.byte_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
        assertEquals("()C",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.char_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
        assertEquals("()D",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.double_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
        assertEquals("()F",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.float_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
        assertEquals("()I",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
        assertEquals("()J",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.long_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
        assertEquals("()S",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.short_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
        assertEquals("()Z",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.boolean_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], EmptyStatement.INSTANCE)))
    }

    @Test // GROOVY-12319
    void testRareTypeAndArrayGenericsSignatures() {
        ClassNode outer = ClassHelper.makeWithoutCaching('com.example.Outer')
        outer.genericsTypes = [new GenericsType(ClassHelper.STRING_TYPE)] as GenericsType[]
        ClassNode inner = ClassHelper.makeWithoutCaching('com.example.Outer$Inner')
        inner.outerClassType = outer
        inner.genericsTypes = [new GenericsType(ClassHelper.Integer_TYPE)] as GenericsType[]

        String typeSig = BytecodeHelper.getTypeGenericsSignature(inner)
        assert typeSig.contains('Outer')
        assert typeSig.contains('Inner')
        assert typeSig.contains('String')

        ClassNode list = ClassHelper.LIST_TYPE.getPlainNodeReference()
        list.genericsTypes = [new GenericsType(ClassHelper.STRING_TYPE)] as GenericsType[]
        String arrayBounds = BytecodeHelper.getGenericsBounds(list.makeArray())
        assert arrayBounds.startsWith('[')
        assert arrayBounds.contains('String')

        assert BytecodeHelper.getGenericsBounds(ClassHelper.STRING_TYPE.makeArray()) == null

        ClassNode diamond = ClassHelper.LIST_TYPE.getPlainNodeReference()
        diamond.genericsTypes = GenericsType.EMPTY_ARRAY
        assert BytecodeHelper.getTypeGenericsSignature(diamond) == null

        ClassNode placeholder = ClassHelper.makeWithoutCaching('T')
        placeholder.genericsPlaceHolder = true
        placeholder.genericsTypes = [new GenericsType(placeholder)] as GenericsType[]
        placeholder.redirect = ClassHelper.OBJECT_TYPE
        assert BytecodeHelper.getGenericsBounds(placeholder) != null

        ClassNode mismatchedInner = ClassHelper.makeWithoutCaching('totally.Different$Item')
        mismatchedInner.outerClassType = outer
        mismatchedInner.genericsTypes = [new GenericsType(ClassHelper.Integer_TYPE)] as GenericsType[]
        String fallback = BytecodeHelper.getTypeGenericsSignature(mismatchedInner)
        assert fallback.contains('Item')

        ClassNode superDiamond = ClassHelper.LIST_TYPE.getPlainNodeReference()
        superDiamond.genericsTypes = GenericsType.EMPTY_ARRAY
        ClassNode t = ClassHelper.makeWithoutCaching('T')
        t.genericsPlaceHolder = true
        ClassNode genericClass = new ClassNode('C', ACC_PUBLIC, superDiamond)
        genericClass.genericsTypes = [new GenericsType(t)] as GenericsType[]
        assert BytecodeHelper.getGenericsSignature(genericClass) != null

        MethodNode mn = new MethodNode('echo', 0, inner, [new Parameter(inner, 'p')] as Parameter[], [] as ClassNode[], EmptyStatement.INSTANCE)
        assert BytecodeHelper.getGenericsMethodSignature(mn).contains('Outer')
    }

    @Test
    void testPushConstantUsesCompactIntegerInstructions() {
        def bytecode = emitBytecode {
            BytecodeHelper.pushConstant(it, -1)
            BytecodeHelper.pushConstant(it, 6)
            BytecodeHelper.pushConstant(it, 200)
            BytecodeHelper.pushConstant(it, 70_000)
            it.visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_M1',
                'BIPUSH 6',
                'SIPUSH 200',
                'LDC 70000',
                'RETURN',
        ]
    }

    private emitBytecode(@DelegatesTo(MethodVisitor) Closure emitter) {
        def writer = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        writer.visit(V17, ACC_PUBLIC, 'BytecodeHelperTestSupport', null, 'java/lang/Object', null)

        MethodVisitor mv = writer.visitMethod(ACC_PUBLIC | ACC_STATIC, 'sample', '()V', null, null)
        mv.visitCode()
        emitter.delegate = mv
        emitter.resolveStrategy = Closure.DELEGATE_FIRST
        if (emitter.maximumNumberOfParameters == 0) {
            emitter.call()
        } else {
            emitter.call(mv)
        }
        mv.visitMaxs(0, 0)
        mv.visitEnd()

        writer.visitEnd()
        extractSequence(writer.toByteArray(), [method: 'sample'])
    }

    private static List<String> opcodeLines(bytecode) {
        bytecode.instructions.findAll { line ->
            Printer.OPCODES.any { opcode -> opcode != null && line.startsWith(opcode) }
        }
    }
}
