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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.trait.Traits;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.nonGeneric;

public class InnerClassVisitor extends InnerClassVisitorHelper implements Opcodes {

    private ClassNode classNode;
    private FieldNode currentField;
    private MethodNode currentMethod;
    private final SourceUnit sourceUnit;
    private boolean inClosure, processingObjInitStatements;

    public InnerClassVisitor(CompilationUnit cu, SourceUnit su) {
        sourceUnit = su;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitClass(ClassNode node) {
        classNode = node;
        InnerClassNode innerClass = null;
        if (!node.isEnum() && !node.isInterface() && node instanceof InnerClassNode) {
            innerClass = (InnerClassNode) node;
            if (!isStatic(innerClass) && innerClass.getVariableScope() == null) {
                innerClass.addField("this$0", ACC_FINAL | ACC_SYNTHETIC, node.getOuterClass().getPlainNodeReference(), null);
            }
        }

        super.visitClass(node);

        if (node.isEnum() || node.isInterface()) return;
        if (innerClass == null) return;

        if (node.getSuperClass().isInterface() || Traits.isAnnotatedWithTrait(node.getSuperClass())) {
            node.addInterface(node.getUnresolvedSuperClass());
            node.setUnresolvedSuperClass(ClassHelper.OBJECT_TYPE);
        }
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        boolean inClosureOld = inClosure;
        inClosure = true;
        super.visitClosureExpression(expression);
        inClosure = inClosureOld;
    }

    @Override
    protected void visitObjectInitializerStatements(ClassNode node) {
        processingObjInitStatements = true;
        super.visitObjectInitializerStatements(node);
        processingObjInitStatements = false;
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        currentMethod = node;
        visitAnnotations(node);
        visitClassCodeContainer(node.getCode());
        // GROOVY-5681: initial expressions should be visited too!
        for (Parameter param : node.getParameters()) {
            if (param.hasInitialExpression()) {
                param.getInitialExpression().visit(this);
            }
            visitAnnotations(param);
        }
        currentMethod = null;
    }

    @Override
    public void visitField(FieldNode node) {
        currentField = node;
        super.visitField(node);
        currentField = null;
    }

    @Override
    public void visitProperty(PropertyNode node) {
        final FieldNode field = node.getField();
        final Expression init = field.getInitialExpression();
        field.setInitialValueExpression(null);
        super.visitProperty(node);
        field.setInitialValueExpression(init);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        if (!call.isUsingAnonymousInnerClass()) {
            passThisReference(call);
            return;
        }

        InnerClassNode innerClass = (InnerClassNode) call.getType();
        ClassNode outerClass = innerClass.getOuterClass();
        ClassNode superClass = innerClass.getSuperClass();
        if (!superClass.isInterface() && superClass.getOuterClass() != null
                && !(superClass.isStaticClass() || (superClass.getModifiers() & ACC_STATIC) != 0)) {
            insertThis0ToSuperCall(call, innerClass);
        }
        if (!innerClass.getDeclaredConstructors().isEmpty()) return;
        if ((innerClass.getModifiers() & ACC_STATIC) != 0) return;

        VariableScope scope = innerClass.getVariableScope();
        if (scope == null) return;
        boolean isStatic = !inClosure && isStatic(innerClass, scope, call);

        // expressions = constructor call arguments
        List<Expression> expressions = ((TupleExpression) call.getArguments()).getExpressions();
        // block = init code for the constructor we produce
        BlockStatement block = new BlockStatement();
        // parameters = parameters of the constructor
        int additionalParamCount = (isStatic ? 0 : 1) + scope.getReferencedLocalVariablesCount();
        List<Parameter> parameters = new ArrayList<>(expressions.size() + additionalParamCount);
        // superCallArguments = arguments for the super call == the constructor call arguments
        List<Expression> superCallArguments = new ArrayList<>(expressions.size());

        // first we add a super() call for all expressions given in the constructor call expression
        for (int i = 0, n = expressions.size(); i < n; i += 1) {
            // add one parameter for each expression in the constructor call
            Parameter param = new Parameter(ClassHelper.OBJECT_TYPE, "p" + additionalParamCount + i);
            parameters.add(param);
            // add the corresponsing argument to the super constructor call
            superCallArguments.add(new VariableExpression(param));
        }

        // add the super call
        ConstructorCallExpression cce = new ConstructorCallExpression(
                ClassNode.SUPER,
                new TupleExpression(superCallArguments)
        );

        block.addStatement(new ExpressionStatement(cce));

        int pCount = 0;
        if (!isStatic) {
            // need to pass "this" to access unknown methods/properties
            expressions.add(pCount, VariableExpression.THIS_EXPRESSION);

            ClassNode enclosingType = (inClosure ? ClassHelper.CLOSURE_TYPE : outerClass).getPlainNodeReference();
            Parameter thisParameter = new Parameter(enclosingType, "p" + pCount);
            parameters.add(pCount++, thisParameter);

            // "this" reference is saved in a field named "this$0"
            FieldNode thisField = innerClass.addField("this$0", ACC_FINAL | ACC_SYNTHETIC, enclosingType, null);
            addFieldInit(thisParameter, thisField, block);
        }/* else {
            innerClass.addField("this$0", ACC_FINAL | ACC_SYNTHETIC, nonGeneric(ClassHelper.CLASS_Type), classX(outerClass));
        }*/

        // for each shared variable, add a Reference field
        for (Iterator<Variable> it = scope.getReferencedLocalVariablesIterator(); it.hasNext();) {
            Variable var = it.next();

            VariableExpression ve = new VariableExpression(var);
            ve.setClosureSharedVariable(true);
            ve.setUseReferenceDirectly(true);
            expressions.add(pCount, ve);

            ClassNode referenceType = ClassHelper.REFERENCE_TYPE.getPlainNodeReference();
            Parameter p = new Parameter(referenceType, "p" + pCount);
            p.setOriginType(var.getOriginType());
            parameters.add(pCount++, p);

            VariableExpression initial = new VariableExpression(p);
            initial.setSynthetic(true);
            initial.setUseReferenceDirectly(true);
            FieldNode pField = innerClass.addFieldFirst(ve.getName(), ACC_PUBLIC | ACC_SYNTHETIC, referenceType, initial);
            pField.setHolder(true);
            pField.setOriginType(ClassHelper.getWrapper(var.getOriginType()));
        }

        innerClass.addConstructor(ACC_SYNTHETIC, parameters.toArray(Parameter.EMPTY_ARRAY), ClassNode.EMPTY_ARRAY, block);
    }

    private boolean isStatic(InnerClassNode innerClass, VariableScope scope, ConstructorCallExpression call) {
        boolean isStatic = innerClass.isStaticClass();
        if (!isStatic) {
            if (currentMethod != null) {
                if (currentMethod instanceof ConstructorNode) {
                    boolean[] precedesSuperOrThisCall = new boolean[1];
                    ConstructorNode ctor = (ConstructorNode) currentMethod;
                    GroovyCodeVisitor visitor = new CodeVisitorSupport() {
                        @Override
                        public void visitConstructorCallExpression(ConstructorCallExpression cce) {
                            if (cce == call) {
                                precedesSuperOrThisCall[0] = true;
                            } else {
                                super.visitConstructorCallExpression(cce);
                            }
                        }
                    };
                    if (ctor.firstStatementIsSpecialConstructorCall()) currentMethod.getFirstStatement().visit(visitor);
                    Arrays.stream(ctor.getParameters()).filter(Parameter::hasInitialExpression).forEach(p -> p.getInitialExpression().visit(visitor));

                    isStatic = precedesSuperOrThisCall[0];
                } else {
                    isStatic = currentMethod.isStatic();
                }
            } else if (currentField != null) {
                isStatic = currentField.isStatic();
            }
        }
        return isStatic;
    }

    // this is the counterpart of addThisReference(). To non-static inner classes, outer this should be
    // passed as the first argument implicitly.
    private void passThisReference(ConstructorCallExpression call) {
        ClassNode cn = call.getType().redirect();
        if (!shouldHandleImplicitThisForInnerClass(cn)) return;

        boolean isInStaticContext = true;
        if (currentMethod != null)
            isInStaticContext = currentMethod.getVariableScope().isInStaticContext();
        else if (currentField != null)
            isInStaticContext = currentField.isStatic();
        else if (processingObjInitStatements)
            isInStaticContext = false;

        // if constructor call is not in static context, return
        if (isInStaticContext) {
            // constructor call is in static context and the inner class is non-static - 1st arg is supposed to be
            // passed as enclosing "this" instance
            //
            Expression args = call.getArguments();
            if (args instanceof TupleExpression && ((TupleExpression) args).getExpressions().isEmpty()) {
                addError("No enclosing instance passed in constructor call of a non-static inner class", call);
            }
            return;
        }
        insertThis0ToSuperCall(call, cn);
    }

    private void insertThis0ToSuperCall(final ConstructorCallExpression call, final ClassNode cn) {
        // calculate outer class which we need for this$0
        ClassNode parent = classNode;
        int level = 0;
        for (; parent != null && parent != cn.getOuterClass(); parent = parent.getOuterClass()) {
            level++;
        }

        // if constructor call is not in outer class, don't pass 'this' implicitly. Return.
        if (parent == null) return;

        //add this parameter to node
        Expression argsExp = call.getArguments();
        if (argsExp instanceof TupleExpression) {
            TupleExpression argsListExp = (TupleExpression) argsExp;
            Expression this0 = VariableExpression.THIS_EXPRESSION;
            for (int i = 0; i != level; ++i)
                this0 = new PropertyExpression(this0, "this$0");
            argsListExp.getExpressions().add(0, this0);
        }
    }
}
