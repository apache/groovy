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

/**
 * Abstract base class providing helper methods for inner class visitors.
 * This class contains utility methods for generating dispatcher code that
 * allows inner classes to access members of their enclosing classes.
 */
public abstract class InnerClassVisitorHelper extends ClassCodeVisitorSupport {

    private static final ClassNode OBJECT_ARRAY = ClassHelper.OBJECT_TYPE.makeArray();

    /**
     * Adds a statement to initialize a field from a constructor parameter.
     *
     * @param p the parameter to read from
     * @param fn the field to initialize
     * @param block the block statement to add the initialization to
     */
    protected static void addFieldInit(final Parameter p, final FieldNode fn, final BlockStatement block) {
        block.addStatement(assignS(fieldX(fn), varX(p)));
    }

    /**
     * Generates property getter dispatcher code for dynamic property access.
     *
     * @param block the block to add the dispatcher code to
     * @param target the target object to get the property from
     * @param parameters the dispatcher method parameters (property name)
     */
    protected static void setPropertyGetterDispatcher(final BlockStatement block, final Expression target, final Parameter[] parameters) {
        block.addStatement(returnS(propX(target, varX(parameters[0]))));
    }

    /**
     * Generates property setter dispatcher code for dynamic property access.
     *
     * @param block the block to add the dispatcher code to
     * @param target the target object to set the property on
     * @param parameters the dispatcher method parameters (property name, value)
     */
    protected static void setPropertySetterDispatcher(final BlockStatement block, final Expression target, final Parameter[] parameters) {
        block.addStatement(stmt(assignX(propX(target, varX(parameters[0])), varX(parameters[1]))));
    }

    /**
     * Generates method dispatcher code for dynamic method invocation.
     * Handles both single arguments and spread arguments.
     *
     * @param block the block to add the dispatcher code to
     * @param target the target object to invoke methods on
     * @param parameters the dispatcher method parameters (method name, arguments)
     */
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

    /**
     * Returns the class node to expose when wiring dispatch methods for an inner class.
     *
     * @param cn the enclosing class node
     * @param isStatic whether the generated access is static
     * @return the effective dispatch receiver type
     */
    protected static ClassNode getClassNode(final ClassNode cn, final boolean isStatic) {
        return isStatic ? ClassHelper.CLASS_Type : cn; // TODO: Set class type parameter?
    }

    /**
     * Calculates the inheritance distance from the supplied type to {@code Object}.
     *
     * @param cn the class node to measure
     * @return the number of superclass hops to {@code Object}
     */
    protected static int getObjectDistance(ClassNode cn) {
        int count = 0;
        while (cn != null && !ClassHelper.isObjectType(cn)) {
            cn = cn.getSuperClass();
            count += 1;
        }
        return count;
    }

    /**
     * Determines whether the supplied inner class behaves as a static nested class.
     *
     * @param cn the inner class node to test
     * @return {@code true} if no outer-instance field is required
     */
    protected static boolean isStatic(final InnerClassNode cn) {
        return cn.getDeclaredField("this$0") == null;
    }

    /**
     * Determines whether synthetic outer-instance handling should be applied to the inner class.
     *
     * @param cn the class node to test
     * @return {@code true} if implicit {@code this$0} handling is required
     */
    protected static boolean shouldHandleImplicitThisForInnerClass(final ClassNode cn) {
        final int explicitOrImplicitStatic = Opcodes.ACC_ENUM | Opcodes.ACC_INTERFACE | Opcodes.ACC_RECORD | Opcodes.ACC_STATIC;
        return (cn.getModifiers() & explicitOrImplicitStatic) == 0 && (cn instanceof InnerClassNode inner && !inner.isAnonymous())
                && cn.getAnnotations().stream().noneMatch(aNode -> "groovy.transform.RecordType".equals(aNode.getClassNode().getName())); // GROOVY-11600
    }
}
