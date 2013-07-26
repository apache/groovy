/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.transform.stc

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.objectweb.asm.Opcodes

// A simple extension that says that the "plus" method returns a String
methodNotFound { receiver, name, argumentList, argTypes, call ->
    if (name=='plus') {
        return new MethodNode("plus", Opcodes.ACC_PUBLIC, STRING_TYPE, [new Parameter(argTypes[0], "op")] as Parameter[], ClassNode.EMPTY_ARRAY, null)
    } else if (name=="leftShift") {
        return new MethodNode("leftShift", Opcodes.ACC_PUBLIC, STRING_TYPE, [new Parameter(argTypes[0], "op")] as Parameter[], ClassNode.EMPTY_ARRAY, null)
    }
}
