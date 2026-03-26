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
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.attrX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperS;
import static org.codehaus.groovy.transform.trait.Traits.isTrait;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_MANDATED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

public class InnerClassVisitor extends InnerClassVisitorHelper {

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

    //--------------------------------------------------------------------------

    @Override
    public void visitClass(ClassNode node) {
        classNode = node;
        InnerClassNode innerClass = null;
        if (!node.isEnum() && !node.isInterface() && node instanceof InnerClassNode) {
            innerClass = (InnerClassNode) node;
            if (innerClass.getVariableScope() == null && (innerClass.getModifiers() & ACC_STATIC) == 0) {
                innerClass.addField("this$0", ACC_FINAL | ACC_SYNTHETIC, node.getOuterClass().getPlainNodeReference(), null);
            }
        }

        super.visitClass(node);

        if (innerClass != null && innerClass.isAnonymous()) {
            var upperClass = node.getUnresolvedSuperClass(false);
            if (upperClass.isInterface() || isTrait(upperClass)) {
                node.addInterface(upperClass);
                node.setUnresolvedSuperClass(ClassHelper.OBJECT_TYPE);
            }
        }
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
    public void visitClosureExpression(ClosureExpression closure) {
        boolean inClosurePrevious = inClosure;
        inClosure = true;
        super.visitClosureExpression(closure);
        inClosure = inClosurePrevious;
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
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        if (!call.isUsingAnonymousInnerClass()) {
            passThisReference(call);
            return;
        }

        ClassNode innerClass = call.getType();
        ClassNode outerClass = innerClass.getOuterClass();
        ClassNode upperClass = innerClass.getUnresolvedSuperClass(false);
        if (upperClass.getOuterClass() != null && !upperClass.isInterface()
                && !(upperClass.isStaticClass() || (upperClass.getModifiers() & ACC_STATIC) != 0)) {
            insertThis0ToSuperCall(call, innerClass);
        }

        if ((innerClass.getModifiers() & ACC_STATIC) != 0) return;
        if (!innerClass.getDeclaredConstructors().isEmpty()) return;

        VariableScope scope = ((InnerClassNode) innerClass).getVariableScope();
        if (scope != null) {
            List<Expression> arguments = ((TupleExpression) call.getArguments()).getExpressions();
            boolean isStatic = !inClosure && isStatic(innerClass, scope, call);
            addConstructor(innerClass, outerClass, scope, arguments, isStatic);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * This is the counterpart of <code>addThisReference</code>.  For non-static
     * inner classes, outer this should be passed as the implicit first argument.
     */
    private void passThisReference(final ConstructorCallExpression call) {
        ClassNode cn = call.getType().redirect();
        if (!shouldHandleImplicitThisForInnerClass(cn)) return;

        boolean isInStaticContext;
        if (currentMethod != null)
            isInStaticContext = currentMethod.isStatic();
        else if (currentField != null)
            isInStaticContext = currentField.isStatic();
        else
            isInStaticContext = !processingObjInitStatements;

        // GROOVY-10289
        ClassNode enclosing = classNode;
        while (!isInStaticContext && !enclosing.equals(cn.getOuterClass())) {
            isInStaticContext = (enclosing.getModifiers() & ACC_STATIC) != 0;
            // TODO: if enclosing is a local type, also test field or method
            enclosing = enclosing.getOuterClass();
            if (enclosing == null) break;
        }

        if (isInStaticContext) {
            // constructor call is in static context and the inner class is non-static - 1st arg is supposed to be
            // passed as enclosing "this" instance
            Expression args = call.getArguments();
            if (args instanceof TupleExpression && ((TupleExpression) args).getExpressions().isEmpty()) {
                addError("No enclosing instance passed in constructor call of a non-static inner class", call);
            }
        } else {
            insertThis0ToSuperCall(call, cn);
        }
    }

    private void insertThis0ToSuperCall(final ConstructorCallExpression call, final ClassNode innerClass) {
        // calculate outer class which we need for this$0
        ClassNode parent = classNode;
        int level = 0;
        for (; parent != null && parent != innerClass.getOuterClass(); parent = parent.getOuterClass()) {
            level++;
        }

        // if constructor call is not in outer class, do not pass 'this' implicitly
        if (parent == null) return;

        Expression args = call.getArguments();
        if (args instanceof TupleExpression tuple) {
            Expression this0 = new VariableExpression("this"); // bypass closure
            for (int i = 0; i != level; ++i) {
                this0 = attrX(this0, constX("this$0"));
                // GROOVY-8104: an anonymous inner class may still have closure nesting
                if (i == 0 && classNode.getDeclaredField("this$0").getType().equals(ClassHelper.CLOSURE_TYPE)) {
                    this0 = callX(this0, "getThisObject");
                }
            }
            tuple.getExpressions().add(0, this0);
        }
    }

    private boolean isStatic(final ClassNode innerClass, final VariableScope scope, final ConstructorCallExpression call) {
        boolean isStatic = innerClass.isStaticClass();
        if (!isStatic) {
            if (currentMethod != null) {
                if (currentMethod instanceof ConstructorNode ctor) {
                    boolean[] precedesSuperOrThisCall = new boolean[1];
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
        // GROOVY-8433: Category transform implies static method
        isStatic = isStatic || innerClass.getOuterClass().getAnnotations().stream()
            .anyMatch(a -> "groovy.lang.Category".equals(a.getClassNode().getName()));
        return isStatic;
    }

    private void addConstructor(final ClassNode innerClass, final ClassNode outerClass, final VariableScope scope, final List<Expression> arguments, final boolean isStatic) {
        // block: init code for the constructor
        BlockStatement block = new BlockStatement();
        // parameters: parameters of the constructor
        int additionalParamCount = (isStatic ? 0 : 1) + scope.getReferencedLocalVariablesCount();
        List<Parameter> parameters = new ArrayList<>(arguments.size() + additionalParamCount);
        // superCallArguments: arguments for the super constructor call
        List<Expression> superCallArguments = new ArrayList<>(arguments.size());

        // first we add a super() call for all expressions given in the constructor call expression
        for (int i = 0, n = arguments.size(); i < n; i += 1) {
            // add one parameter for each expression in the constructor call
            Parameter p = new Parameter(ClassHelper.OBJECT_TYPE, "p" + additionalParamCount + i);
            parameters.add(p);
            // add the corresponding argument to the super constructor call
            superCallArguments.add(new VariableExpression(p));
        }

        // add the super call
        block.addStatement(ctorSuperS(new TupleExpression(superCallArguments)));

        int pCount = 0;
        if (!isStatic) {
            // need to pass "this" to access unknown methods/properties
            ClassNode enclosingType = (inClosure ? ClassHelper.CLOSURE_TYPE : outerClass).getPlainNodeReference();
            arguments.add(pCount, new VariableExpression("this", enclosingType));
            Parameter thisParameter = new Parameter(enclosingType, "p" + pCount);
            thisParameter.setModifiers(ACC_FINAL | ACC_MANDATED);
            parameters.add(pCount++, thisParameter);

            // "this" reference is saved in a field named "this$0"
            FieldNode thisField = innerClass.addField("this$0", ACC_FINAL | ACC_SYNTHETIC, enclosingType, null);
            addFieldInit(thisParameter, thisField, block);
        }

        // for each shared variable, add a Reference field
        for (Iterator<Variable> it = scope.getReferencedLocalVariablesIterator(); it.hasNext(); ) {
            Variable v = it.next();

            VariableExpression ve = new VariableExpression(v);
            ve.setClosureSharedVariable(true);
            ve.setUseReferenceDirectly(true);
            arguments.add(pCount, ve);

            ClassNode referenceType = ClassHelper.REFERENCE_TYPE.getPlainNodeReference();
            Parameter p = new Parameter(referenceType, "p" + pCount);
            p.setOriginType(v.getOriginType());
            parameters.add(pCount++, p);

            VariableExpression initial = new VariableExpression(p);
            initial.setSynthetic(true);
            initial.setUseReferenceDirectly(true);

            FieldNode pField = innerClass.addFieldFirst(ve.getName(), ACC_PUBLIC | ACC_SYNTHETIC, referenceType, initial);
            pField.setHolder(true);
            pField.setOriginType(ClassHelper.getWrapper(v.getOriginType()));
        }

        // TODO: JLS 15.9.5.1 : checked exceptions of super constructor should be declared by constructor
        innerClass.addConstructor(0, parameters.toArray(Parameter[]::new), ClassNode.EMPTY_ARRAY, block);
    }
}
