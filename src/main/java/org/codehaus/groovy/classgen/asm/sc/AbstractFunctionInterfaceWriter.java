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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_FUNCTION_INTERFACE_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PARAMETER_TYPE;

/**
 * @since 3.0.0
 */
public interface AbstractFunctionInterfaceWriter {
    default ClassNode getFunctionInterfaceType(Expression expression) {
        ClassNode type = expression.getNodeMetaData(INFERRED_TYPE);

        if (null == type) {
            type = expression.getNodeMetaData(PARAMETER_TYPE);
        }

        if (null == type) {
            type = expression.getNodeMetaData(INFERRED_FUNCTION_INTERFACE_TYPE);
        }
        return type;
    }

    default String createMethodDescriptor(MethodNode abstractMethodNode) {
        return BytecodeHelper.getMethodDescriptor(
                abstractMethodNode.getReturnType().getTypeClass(),
                Arrays.stream(abstractMethodNode.getParameters())
                        .map(e -> e.getType().getTypeClass())
                        .toArray(Class[]::new)
        );
    }

    default Handle createBootstrapMethod(boolean isInterface) {
        return new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory",
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                isInterface
        );
    }
}
