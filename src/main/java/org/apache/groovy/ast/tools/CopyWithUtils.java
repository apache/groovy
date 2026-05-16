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
package org.apache.groovy.ast.tools;

import org.apache.groovy.transform.copywith.NestedCopyWithSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Shared AST-generation glue for nested-path {@code copyWith}, used by both
 * the {@code @Immutable} and {@code @RecordType} transforms so the generated
 * code (and its evolution) stays identical across the two.
 *
 * @since 6.0.0
 */
public final class CopyWithUtils {

    private static final ClassNode NESTED_COPYWITH_TYPE = ClassHelper.make(NestedCopyWithSupport.class);

    private CopyWithUtils() {
    }

    /**
     * The leading statement of a nested-aware {@code copyWith(Map)}:
     * {@code <mapParamName> = NestedCopyWithSupport.flatten(this, <mapParamName>)}.
     * Plain keys pass through unchanged, so flat usage is unaffected.
     */
    public static Statement nestedFlattenStmt(final String mapParamName) {
        return assignS(varX(mapParamName),
                callX(NESTED_COPYWITH_TYPE, "flatten", args(varX("this"), varX(mapParamName))));
    }

    /**
     * Backward-compatibility shim: a no-arg {@code copyWith()} with its
     * historical semantics (no changes &rarr; the same instance). Needed once
     * a {@code copyWith(Closure)} overload exists, otherwise the historically
     * supported zero-arg call becomes ambiguous.
     */
    public static void addCopyWithIdentityMethod(final ClassNode cNode) {
        if (hasDeclaredMethod(cNode, "copyWith", 0)) return;
        addGeneratedMethod(cNode, "copyWith", ACC_PUBLIC | ACC_FINAL,
                cNode.getPlainNodeReference(), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                returnS(varX("this")));
    }

    /**
     * The transactional block overload:
     * {@code copyWith { name = 'x'; address.city = 'y' }}.
     */
    public static void addCopyWithBlockMethod(final ClassNode cNode) {
        Parameter block = param(ClassHelper.CLOSURE_TYPE.getPlainNodeReference(), "block");
        addGeneratedMethod(cNode, "copyWith", ACC_PUBLIC | ACC_FINAL,
                cNode.getPlainNodeReference(), params(block), ClassNode.EMPTY_ARRAY,
                returnS(castX(cNode.getPlainNodeReference(),
                        callX(NESTED_COPYWITH_TYPE, "applyBlock",
                                args(varX("this"), varX(block))))));
    }
}
