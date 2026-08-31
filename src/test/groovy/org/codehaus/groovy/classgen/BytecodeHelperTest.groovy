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
        assert typeSig == 'Lcom/example/Outer<Ljava/lang/String;>.Inner<Ljava/lang/Integer;>;'

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
        assert BytecodeHelper.getTypeGenericsSignature(mismatchedInner) == 'Lcom/example/Outer<Ljava/lang/String;>.Item<Ljava/lang/Integer;>;'

        // Parser records Outer.Inner; after resolve the name is Outer$Inner.
        ClassNode dottedInner = ClassHelper.makeWithoutCaching('com.example.Outer.Inner')
        dottedInner.outerClassType = outer
        dottedInner.genericsTypes = [new GenericsType(ClassHelper.Integer_TYPE)] as GenericsType[]
        assert BytecodeHelper.getTypeGenericsSignature(dottedInner) == 'Lcom/example/Outer<Ljava/lang/String;>.Inner<Ljava/lang/Integer;>;'

        ClassNode dollarInner = ClassHelper.makeWithoutCaching('com.example.Outer$Inner')
        dollarInner.outerClassType = outer
        dollarInner.genericsTypes = [new GenericsType(ClassHelper.Integer_TYPE)] as GenericsType[]
        assert BytecodeHelper.getTypeGenericsSignature(dollarInner) == 'Lcom/example/Outer<Ljava/lang/String;>.Inner<Ljava/lang/Integer;>;'

        // Anonymous: owner Foo, inner Foo$1 — still a prefix, not the fallback.
        ClassNode anonOwner = ClassHelper.makeWithoutCaching('com.example.Foo')
        anonOwner.genericsTypes = [new GenericsType(ClassHelper.STRING_TYPE)] as GenericsType[]
        ClassNode anon = ClassHelper.makeWithoutCaching('com.example.Foo$1')
        anon.outerClassType = anonOwner
        assert BytecodeHelper.getTypeGenericsSignature(anon) == 'Lcom/example/Foo<Ljava/lang/String;>.1;'

        // Unrelated inner name Bar.X is not extra qualification of owner Foo.
        ClassNode unrelated = ClassHelper.makeWithoutCaching('Bar.X')
        unrelated.outerClassType = anonOwner
        assert BytecodeHelper.getTypeGenericsSignature(unrelated) == 'Lcom/example/Foo<Ljava/lang/String;>.X;'

        // Simple identifier with no '.' / '$' (fallback sep < 0).
        ClassNode simple = ClassHelper.makeWithoutCaching('Item')
        simple.outerClassType = anonOwner
        assert BytecodeHelper.getTypeGenericsSignature(simple) == 'Lcom/example/Foo<Ljava/lang/String;>.Item;'

        // Equal names: not nested (length <= owner).
        ClassNode sameName = ClassHelper.makeWithoutCaching('com.example.Foo')
        sameName.outerClassType = anonOwner
        sameName.genericsTypes = [new GenericsType(ClassHelper.Integer_TYPE)] as GenericsType[]
        assert BytecodeHelper.getTypeGenericsSignature(sameName) == 'Lcom/example/Foo<Ljava/lang/String;>.Foo<Ljava/lang/Integer;>;'

        // Prefix that is not a nesting separator (Foo vs FooBar).
        ClassNode notNested = ClassHelper.makeWithoutCaching('com.example.FooBar')
        notNested.outerClassType = anonOwner
        assert BytecodeHelper.getTypeGenericsSignature(notNested) == 'Lcom/example/Foo<Ljava/lang/String;>.FooBar;'

        // Remainder after owner may contain further $ nesting.
        ClassNode deepRemainder = ClassHelper.makeWithoutCaching('com.example.Outer$Middle$Inner')
        deepRemainder.outerClassType = outer
        deepRemainder.genericsTypes = [new GenericsType(ClassHelper.Integer_TYPE)] as GenericsType[]
        assert BytecodeHelper.getTypeGenericsSignature(deepRemainder) == 'Lcom/example/Outer<Ljava/lang/String;>.Middle.Inner<Ljava/lang/Integer;>;'

        // Recursive enclosing types: Outer<T>.Middle<U>.Inner<V>.
        ClassNode middle = ClassHelper.makeWithoutCaching('com.example.Outer$Middle')
        middle.outerClassType = outer
        middle.genericsTypes = [new GenericsType(ClassHelper.Integer_TYPE)] as GenericsType[]
        ClassNode inner2 = ClassHelper.makeWithoutCaching('com.example.Outer$Middle$Inner')
        inner2.outerClassType = middle
        inner2.genericsTypes = [new GenericsType(ClassHelper.Long_TYPE)] as GenericsType[]
        assert BytecodeHelper.getTypeGenericsSignature(inner2) == 'Lcom/example/Outer<Ljava/lang/String;>.Middle<Ljava/lang/Integer;>.Inner<Ljava/lang/Long;>;'

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
