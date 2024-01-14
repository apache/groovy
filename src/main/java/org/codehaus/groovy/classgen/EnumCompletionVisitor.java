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
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.TupleConstructorASTTransformation;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.transform.sc.StaticCompilationVisitor.isStaticallyCompiled;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Enums have a parent constructor with two arguments from java.lang.Enum.
 * This visitor adds those two arguments into manually created constructors
 * and performs the necessary super call.
 */
public class EnumCompletionVisitor extends ClassCodeVisitorSupport {

    private final SourceUnit sourceUnit;

    public EnumCompletionVisitor(final CompilationUnit cu, final SourceUnit su) {
        sourceUnit = su;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitClass(final ClassNode node) {
        if (node.isEnum()
                && !EnumVisitor.isAnonymousInnerClass(node)) {
            node.getInnerClasses().forEachRemaining(innerClass -> {
                // EnumConstantClassNode must be completed before its enum class
                if (EnumVisitor.isAnonymousInnerClass(innerClass)) completeEnum(innerClass);
            });
            completeEnum(node);
        }
    }

    private void completeEnum(final ClassNode enumClass) {
        if (nonSyntheticConstructors(enumClass).isEmpty()) {
            addImplicitConstructors(enumClass);
        }

        for (ConstructorNode ctor : nonSyntheticConstructors(enumClass)) {
            transformConstructor(ctor);
        }

        var outerClass = enumClass.getOuterClass(); // GROOVY-7024
        if (outerClass != null && !isStaticallyCompiled(enumClass)) {
            addOuterClassDispatch((InnerClassNode) enumClass, outerClass);
        }
    }

    /**
     * Add map and no-arg constructor or mirror those of the superclass (i.e. base enum).
     */
    private static void addImplicitConstructors(final ClassNode enumClass) {
        if (EnumVisitor.isAnonymousInnerClass(enumClass)) {
            List<ConstructorNode> superCtors = nonSyntheticConstructors(enumClass.getSuperClass());
            if (!superCtors.isEmpty()) {
                for (ConstructorNode ctor : superCtors) {
                    addGeneratedConstructor(enumClass, ACC_PRIVATE, ctor.getParameters(), ClassNode.EMPTY_ARRAY, new BlockStatement());
                }
                return;
            }
        }
        TupleConstructorASTTransformation.addSpecialMapConstructors(ACC_PRIVATE, enumClass, "One of the enum constants for enum " +
                enumClass.getName() + " was initialized with null. Please use a non-null value or define your own constructor.", true);
    }

    /**
     * If constructor does not define a call to super, then transform constructor
     * to get String,int parameters at beginning and add call super(String,int).
     */
    private void transformConstructor(final ConstructorNode ctor) {
        boolean chainedThisConstructorCall = false;
        ConstructorCallExpression cce = null;
        if (ctor.firstStatementIsSpecialConstructorCall()) {
            Statement code = ctor.getFirstStatement();
            cce = (ConstructorCallExpression) ((ExpressionStatement) code).getExpression();
            if (cce.isSuperCall()) return;
            // must be call to this(...)
            chainedThisConstructorCall = true;
        }
        // we need to add parameters
        Parameter[] oldP = ctor.getParameters();
        Parameter[] newP = new Parameter[oldP.length + 2];
        String stringParameterName = getUniqueVariableName("__str", ctor.getCode());
        newP[0] = new Parameter(ClassHelper.STRING_TYPE, stringParameterName);
        String intParameterName = getUniqueVariableName("__int", ctor.getCode());
        newP[1] = new Parameter(ClassHelper.int_TYPE, intParameterName);
        System.arraycopy(oldP, 0, newP, 2, oldP.length);
        ctor.setParameters(newP);
        VariableExpression stringVariable = new VariableExpression(newP[0]);
        VariableExpression intVariable = new VariableExpression(newP[1]);
        if (chainedThisConstructorCall) {
            TupleExpression args = (TupleExpression) cce.getArguments();
            List<Expression> argsExprs = args.getExpressions();
            argsExprs.add(0, stringVariable);
            argsExprs.add(1, intVariable);
        } else {
            // add a super call
            List<Expression> args = new ArrayList<>();
            args.add(stringVariable);
            args.add(intVariable);
            if (EnumVisitor.isAnonymousInnerClass(ctor.getDeclaringClass())) {
                for (Parameter parameter : oldP) {
                    args.add(new VariableExpression(parameter));
                }
                ClassNode enumClass = ctor.getDeclaringClass().getSuperClass();
                makeBridgeConstructor(enumClass, newP); // GROOVY-6747: bridge enum's private constructor
                args.add(new CastExpression(enumClass.getPlainNodeReference(), ConstantExpression.NULL));
            }
            cce = new ConstructorCallExpression(ClassNode.SUPER, new ArgumentListExpression(args));
            BlockStatement code = new BlockStatement();
            code.addStatement(new ExpressionStatement(cce));
            Statement oldCode = ctor.getCode();
            if (oldCode != null) code.addStatement(oldCode);
            ctor.setCode(code);
        }
    }

    private String getUniqueVariableName(final String name, final Statement code) {
        if (code == null) return name;
        final Object[] found = new Object[1];
        CodeVisitorSupport cv = new CodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression expression) {
                if (expression.getName().equals(name)) found[0] = Boolean.TRUE;
            }
        };
        code.visit(cv);
        if (found[0] != null) return getUniqueVariableName("_" + name, code);
        return name;
    }

    /**
     * Ensures the enum type {@code e} has an accessible constructor for its AIC
     * constant class to call.  This constructor delegates to the enum's private
     * constructor.
     */
    private static void makeBridgeConstructor(final ClassNode e, final Parameter[] p) {
        Parameter[] newP = new Parameter[p.length + 1];
        for (int i = 0; i < p.length; i += 1) {
            newP[i] = new Parameter(p[i].getType(), "p" + i);
        }
        newP[p.length] = new Parameter(e.getPlainNodeReference(), "$anonymous");

        if (e.getDeclaredConstructor(newP) == null) {
            ArgumentListExpression args = new ArgumentListExpression();
            for (int i = 0; i < p.length; i += 1) args.addExpression(new VariableExpression(newP[i]));
            Statement thisCtorCall = new ExpressionStatement(new ConstructorCallExpression(ClassNode.THIS, args));
            addGeneratedConstructor(e, ACC_SYNTHETIC, newP, ClassNode.EMPTY_ARRAY, thisCtorCall).setSynthetic(true);
        }
    }

    private static List<ConstructorNode> nonSyntheticConstructors(final ClassNode cn) {
        return cn.getDeclaredConstructors().stream().filter(c -> !c.isSynthetic()).collect(toList());
    }

    private void addOuterClassDispatch(final InnerClassNode innerClass, final ClassNode outerClass) {
        var visitor = new InnerClassCompletionVisitor(null, sourceUnit);

        visitor.addMissingHandler(
                innerClass,
                "methodMissing",
                ACC_PUBLIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "args")),
                (methodBody, parameters) -> {
                    InnerClassVisitorHelper.setMethodDispatcherCode(methodBody, classX(outerClass), parameters);
                }
        );

        visitor.addMissingHandler(
                innerClass,
                "$static_methodMissing",
                ACC_PUBLIC | ACC_STATIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "args")),
                (methodBody, parameters) -> {
                    InnerClassVisitorHelper.setMethodDispatcherCode(methodBody, classX(outerClass), parameters);
                }
        );

        //

        visitor.addMissingHandler(
                innerClass,
                "propertyMissing",
                ACC_PUBLIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name")),
                (methodBody, parameters) -> {
                    InnerClassVisitorHelper.setPropertyGetterDispatcher(methodBody, classX(outerClass), parameters);
                }
        );

        visitor.addMissingHandler(
                innerClass,
                "$static_propertyMissing",
                ACC_PUBLIC | ACC_STATIC,
                OBJECT_TYPE,
                params(param(STRING_TYPE, "name")),
                (methodBody, parameters) -> {
                    InnerClassVisitorHelper.setPropertyGetterDispatcher(methodBody, classX(outerClass), parameters);
                }
        );

        //

        visitor.addMissingHandler(
                innerClass,
                "propertyMissing",
                ACC_PUBLIC,
                VOID_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "value")),
                (methodBody, parameters) -> {
                    InnerClassVisitorHelper.setPropertySetterDispatcher(methodBody, classX(outerClass), parameters);
                }
        );

        visitor.addMissingHandler(
                innerClass,
                "$static_propertyMissing",
                ACC_PUBLIC | ACC_STATIC,
                VOID_TYPE,
                params(param(STRING_TYPE, "name"), param(OBJECT_TYPE, "value")),
                (methodBody, parameters) -> {
                    InnerClassVisitorHelper.setPropertySetterDispatcher(methodBody, classX(outerClass), parameters);
                }
        );
    }
}
