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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.objectweb.asm.Opcodes;

import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.indexX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public abstract class InnerClassVisitorHelper extends ClassCodeVisitorSupport {

    private static final ClassNode OBJECT_ARRAY = ClassHelper.OBJECT_TYPE.makeArray();

    protected static void addFieldInit(final Parameter p, final FieldNode fn, final BlockStatement block) {
        block.addStatement(assignS(fieldX(fn), varX(p)));
    }

    protected static void setPropertyGetterDispatcher(final BlockStatement block, final Expression target, final Parameter[] parameters) {
        block.addStatement(returnS(propX(target, varX(parameters[0]))));
    }

    protected static void setPropertySetterDispatcher(final BlockStatement block, final Expression target, final Parameter[] parameters) {
        block.addStatement(stmt(assignX(propX(target, varX(parameters[0])), varX(parameters[1]))));
    }

    protected static void setMethodDispatcherCode    (final BlockStatement block, final Expression target, final Parameter[] parameters) {
        // if (!(args instanceof Object[])) return target.(name)(args)
        block.addStatement(ifS(
            notX(isInstanceOfX(varX(parameters[1]), OBJECT_ARRAY)),
            returnS(callX(target, varX(parameters[0]), varX(parameters[1])))));

        // if (((Object[])args).length == 1) return target.(name)(args[0])
        block.addStatement(ifS(
            eqX(propX(castX(OBJECT_ARRAY, varX(parameters[1])), "length"), constX(1, true)),
            returnS(callX(target, varX(parameters[0]), indexX(castX(OBJECT_ARRAY, varX(parameters[1])), constX(0, true))))));

        // return target.(name)(*args)
        block.addStatement(returnS(callX(target, varX(parameters[0]), new SpreadExpression(varX(parameters[1])))));
    }

    //--------------------------------------------------------------------------

    protected static ClassNode getClassNode(final ClassNode cn, final boolean isStatic) {
        return isStatic ? ClassHelper.CLASS_Type : cn; // TODO: Set class type parameter?
    }

    protected static int getObjectDistance(ClassNode cn) {
        int count = 0;
        while (cn != null && !ClassHelper.isObjectType(cn)) {
            cn = cn.getSuperClass();
            count += 1;
        }
        return count;
    }

    protected static boolean isStatic(final InnerClassNode cn) {
        return cn.getDeclaredField("this$0") == null;
    }

    protected static boolean shouldHandleImplicitThisForInnerClass(final ClassNode cn) {
        final int explicitOrImplicitStatic = Opcodes.ACC_ENUM | Opcodes.ACC_INTERFACE | Opcodes.ACC_RECORD | Opcodes.ACC_STATIC;
        return (cn.getModifiers() & explicitOrImplicitStatic) == 0 && (cn instanceof InnerClassNode && !((InnerClassNode) cn).isAnonymous())
                && cn.getAnnotations().stream().noneMatch(aNode -> "groovy.transform.RecordType".equals(aNode.getClassNode().getName())); // GROOVY-11600
    }
}
