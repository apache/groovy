/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.classgen.asm.BytecodeHelper

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class BytecodeHelperTest extends GroovyTestCase {

    void testTypeName() {
        assertEquals("[C", BytecodeHelper.getTypeDescription(ClassHelper.char_TYPE.makeArray()))
    }

    void testMethodDescriptor() {
        String answer = BytecodeHelper.getMethodDescriptor(char[].class, new Class[0])
        assertEquals("()[C", answer)

        answer = BytecodeHelper.getMethodDescriptor(int.class, [long.class] as Class[])
        assertEquals("(J)I", answer)

        answer = BytecodeHelper.getMethodDescriptor(String[].class, [String.class, int.class] as Class[])
        assertEquals("(Ljava/lang/String;I)[Ljava/lang/String;", answer)
    }

    void testMethodDescriptorMethodNode() {
        assertEquals("()V",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))

        assertEquals("()Ljava/lang/String;",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.STRING_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))

        assertEquals("()B",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.byte_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
        assertEquals("()C",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.char_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
        assertEquals("()D",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.double_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
        assertEquals("()F",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.float_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
        assertEquals("()I",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
        assertEquals("()J",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.long_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
        assertEquals("()S",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.short_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
        assertEquals("()Z",
                BytecodeHelper.getMethodDescriptor(new MethodNode('test', 0, ClassHelper.boolean_TYPE, Parameter.EMPTY_ARRAY, [] as ClassNode[], new EmptyStatement())))
    }
}
