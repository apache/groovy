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
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
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
import java.util.Iterator;
import java.util.List;

public class InnerClassVisitor extends InnerClassVisitorHelper implements Opcodes {

    private final SourceUnit sourceUnit;
    private ClassNode classNode;
    private static final int PUBLIC_SYNTHETIC = Opcodes.ACC_PUBLIC + Opcodes.ACC_SYNTHETIC;
    private FieldNode thisField = null;
    private MethodNode currentMethod;
    private FieldNode currentField;
    private boolean processingObjInitStatements = false;
    private boolean inClosure = false;

    public InnerClassVisitor(CompilationUnit cu, SourceUnit su) {
        sourceUnit = su;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitClass(ClassNode node) {
        this.classNode = node;
        thisField = null;
        InnerClassNode innerClass = null;
        if (!node.isEnum() && !node.isInterface() && node instanceof InnerClassNode) {
            innerClass = (InnerClassNode) node;
            if (!isStatic(innerClass) && innerClass.getVariableScope() == null) {
                thisField = innerClass.addField("this$0", PUBLIC_SYNTHETIC, node.getOuterClass().getPlainNodeReference(), null);
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
        this.currentMethod = node;
        visitAnnotations(node);
        visitClassCodeContainer(node.getCode());
        // GROOVY-5681: initial expressions should be visited too!
        for (Parameter param : node.getParameters()) {
            if (param.hasInitialExpression()) {
                param.getInitialExpression().accept(this);
            }
            visitAnnotations(param);
        }
        this.currentMethod = null;
    }

    @Override
    public void visitField(FieldNode node) {
        this.currentField = node;
        super.visitField(node);
        this.currentField = null;
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
        if (superClass instanceof InnerClassNode
                && !superClass.isInterface()
                && !(superClass.isStaticClass()||((superClass.getModifiers()&ACC_STATIC)==ACC_STATIC))) {
            insertThis0ToSuperCall(call, innerClass);
        }
        if (!innerClass.getDeclaredConstructors().isEmpty()) return;
        if ((innerClass.getModifiers() & ACC_STATIC) != 0) return;

        VariableScope scope = innerClass.getVariableScope();
        if (scope == null) return;

        // expressions = constructor call arguments
        List<Expression> expressions = ((TupleExpression) call.getArguments()).getExpressions();
        // block = init code for the constructor we produce
        BlockStatement block = new BlockStatement();
        // parameters = parameters of the constructor
        final int additionalParamCount = 1 + scope.getReferencedLocalVariablesCount();
        List<Parameter> parameters = new ArrayList<Parameter>(expressions.size() + additionalParamCount);
        // superCallArguments = arguments for the super call == the constructor call arguments
        List<Expression> superCallArguments = new ArrayList<Expression>(expressions.size());

        // first we add a super() call for all expressions given in the 
        // constructor call expression
        int pCount = additionalParamCount;
        for (Expression expr : expressions) {
            pCount++;
            // add one parameter for each expression in the
            // constructor call
            Parameter param = new Parameter(ClassHelper.OBJECT_TYPE, "p" + pCount);
            parameters.add(param);
            // add to super call
            superCallArguments.add(new VariableExpression(param));
        }

        // add the super call
        ConstructorCallExpression cce = new ConstructorCallExpression(
                ClassNode.SUPER,
                new TupleExpression(superCallArguments)
        );

        block.addStatement(new ExpressionStatement(cce));

        // we need to add "this" to access unknown methods/properties
        // this is saved in a field named this$0
        pCount = 0;
        expressions.add(pCount, VariableExpression.THIS_EXPRESSION);
        boolean isStatic = isStaticThis(innerClass,scope);
        ClassNode outerClassType = getClassNode(outerClass, isStatic);
        if (!isStatic && inClosure) outerClassType = ClassHelper.CLOSURE_TYPE;
        outerClassType = outerClassType.getPlainNodeReference();
        Parameter thisParameter = new Parameter(outerClassType, "p" + pCount);
        parameters.add(pCount, thisParameter);

        thisField = innerClass.addField("this$0", PUBLIC_SYNTHETIC, outerClassType, null);
        addFieldInit(thisParameter, thisField, block);

        // for each shared variable we add a reference and save it as field
        for (Iterator it = scope.getReferencedLocalVariablesIterator(); it.hasNext();) {
            pCount++;
            org.codehaus.groovy.ast.Variable var = (org.codehaus.groovy.ast.Variable) it.next();
            VariableExpression ve = new VariableExpression(var);
            ve.setClosureSharedVariable(true);
            ve.setUseReferenceDirectly(true);
            expressions.add(pCount, ve);

            ClassNode rawReferenceType = ClassHelper.REFERENCE_TYPE.getPlainNodeReference();
            Parameter p = new Parameter(rawReferenceType, "p" + pCount);
            parameters.add(pCount, p);
            p.setOriginType(var.getOriginType());
            final VariableExpression initial = new VariableExpression(p);
            initial.setSynthetic(true);
            initial.setUseReferenceDirectly(true);
            final FieldNode pField = innerClass.addFieldFirst(ve.getName(), PUBLIC_SYNTHETIC,rawReferenceType, initial);
            pField.setHolder(true);
            pField.setOriginType(ClassHelper.getWrapper(var.getOriginType()));
        }

        innerClass.addConstructor(ACC_SYNTHETIC, parameters.toArray(Parameter.EMPTY_ARRAY), ClassNode.EMPTY_ARRAY, block);
    }

    private boolean isStaticThis(InnerClassNode innerClass, VariableScope scope) {
        if (inClosure) return false;
        boolean ret = innerClass.isStaticClass();
        if (    innerClass.getEnclosingMethod()!=null) {
            ret = ret || innerClass.getEnclosingMethod().isStatic();
        } else if (currentField!=null) {
            ret = ret || currentField.isStatic();
        } else if (currentMethod!=null && "<clinit>".equals(currentMethod.getName())) {
            ret = true;
        }
        return ret;
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
