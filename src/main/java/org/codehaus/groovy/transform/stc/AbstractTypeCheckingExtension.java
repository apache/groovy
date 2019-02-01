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
package org.codehaus.groovy.transform.stc;

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * <p>Custom type checking extensions may extend this method in order to benefit from a lot
 * of support methods.</p>
 *
 * <p>The methods found in this class are made directly available in type checking scripts
 * through the {@link org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport} class.</p>
 *
 * @since 2.3.0
 */
public class AbstractTypeCheckingExtension extends TypeCheckingExtension {
    private static final Logger LOG = Logger.getLogger(GroovyTypeCheckingExtensionSupport.class.getName());
    protected final TypeCheckingContext context;
    private final Set<MethodNode> generatedMethods = new LinkedHashSet<MethodNode>();
    private final LinkedList<TypeCheckingScope> scopeData = new LinkedList<TypeCheckingScope>();
    // this boolean is used through setHandled(boolean)
    protected boolean handled = false;
    protected boolean debug = false;

    public AbstractTypeCheckingExtension(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor);
        this.context = typeCheckingVisitor.typeCheckingContext;
    }

    public void setHandled(final boolean handled) {
        this.handled = handled;
    }

    public TypeCheckingScope newScope() {
        TypeCheckingScope scope = new TypeCheckingScope(scopeData.peek());
        scopeData.addFirst(scope);
        return scope;
    }

    public TypeCheckingScope newScope(Closure code) {
        TypeCheckingScope scope = newScope();
        Closure callback = code.rehydrate(scope, this, this);
        callback.call();
        return scope;
    }

    public TypeCheckingScope scopeExit() {
        return scopeData.removeFirst();
    }

    public TypeCheckingScope getCurrentScope() {
        return scopeData.peek();
    }

    public TypeCheckingScope scopeExit(Closure code) {
        TypeCheckingScope scope = scopeData.peek();
        Closure copy = code.rehydrate(scope, this, this);
        copy.call();
        return scopeExit();
    }

    public boolean isGenerated(MethodNode node) {
        return generatedMethods.contains(node);
    }

    public List<MethodNode> unique(MethodNode node) {
        return Collections.singletonList(node);
    }

    public MethodNode newMethod(final String name, final Class returnType) {
        return newMethod(name, ClassHelper.make(returnType));
    }

    public MethodNode newMethod(final String name, final ClassNode returnType) {
        MethodNode node = new MethodNode(name,
                Opcodes.ACC_PUBLIC,
                returnType,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                EmptyStatement.INSTANCE);
        generatedMethods.add(node);
        return node;
    }

    public MethodNode newMethod(final String name,
                                final Callable<ClassNode> returnType) {
        MethodNode node = new MethodNode(name,
                Opcodes.ACC_PUBLIC,
                ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                EmptyStatement.INSTANCE) {
            @Override
            public ClassNode getReturnType() {
                try {
                    return returnType.call();
                } catch (Exception e) {
                    return super.getReturnType();
                }
            }
        };
        generatedMethods.add(node);
        return node;
    }

    public void delegatesTo(ClassNode type) {
        delegatesTo(type, Closure.OWNER_FIRST);
    }

    public void delegatesTo(ClassNode type, int strategy) {
        delegatesTo(type, strategy, typeCheckingVisitor.typeCheckingContext.delegationMetadata);
    }

    public void delegatesTo(ClassNode type, int strategy, DelegationMetadata parent) {
        typeCheckingVisitor.typeCheckingContext.delegationMetadata = new DelegationMetadata(type, strategy, parent);
    }

    public boolean isAnnotatedBy(ASTNode node, Class annotation) {
        return isAnnotatedBy(node, ClassHelper.make(annotation));
    }

    public boolean isAnnotatedBy(ASTNode node, ClassNode annotation) {
        return node instanceof AnnotatedNode && !((AnnotatedNode)node).getAnnotations(annotation).isEmpty();
    }

    public boolean isDynamic(VariableExpression var) {
        return var.getAccessedVariable() instanceof DynamicVariable;
    }

    public boolean isExtensionMethod(MethodNode node) {
        return node instanceof ExtensionMethodNode;
    }

    public ArgumentListExpression getArguments(MethodCall call) {
        return InvocationWriter.makeArgumentList(call.getArguments());
    }

    protected Object safeCall(Closure closure, Object... args) {
        try {
            return closure.call(args);
        } catch (Exception err) {
            typeCheckingVisitor.getSourceUnit().addException(err);
            return null;
        }
    }

    public boolean isMethodCall(Object o) {
        return o instanceof MethodCallExpression;
    }

    public boolean argTypesMatches(ClassNode[] argTypes, Class... classes) {
        if (classes == null) return argTypes == null || argTypes.length == 0;
        if (argTypes.length != classes.length) return false;
        boolean match = true;
        for (int i = 0; i < argTypes.length && match; i++) {
            match = matchWithOrWithourBoxing(argTypes[i], classes[i]);
        }
        return match;
    }

    private static boolean matchWithOrWithourBoxing(final ClassNode argType, final Class aClass) {
        final boolean match;
        ClassNode type = ClassHelper.make(aClass);
        if (ClassHelper.isPrimitiveType(type) && !ClassHelper.isPrimitiveType(argType)) {
            type = ClassHelper.getWrapper(type);
        } else if (ClassHelper.isPrimitiveType(argType) && !ClassHelper.isPrimitiveType(type)) {
            type = ClassHelper.getUnwrapper(type);
        }
        match = argType.equals(type);
        return match;
    }

    public boolean argTypesMatches(MethodCall call, Class... classes) {
        ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
        ClassNode[] argumentTypes = typeCheckingVisitor.getArgumentTypes(argumentListExpression);
        return argTypesMatches(argumentTypes, classes);
    }

    public boolean firstArgTypesMatches(ClassNode[] argTypes, Class... classes) {
        if (classes == null) return argTypes == null || argTypes.length == 0;
        if (argTypes.length<classes.length) return false;
        boolean match = true;
        for (int i = 0; i < classes.length && match; i++) {
            match = matchWithOrWithourBoxing(argTypes[i], classes[i]);
        }
        return match;
    }

    public boolean firstArgTypesMatches(MethodCall call, Class... classes) {
        ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
        ClassNode[] argumentTypes = typeCheckingVisitor.getArgumentTypes(argumentListExpression);
        return firstArgTypesMatches(argumentTypes, classes);
    }

    public boolean argTypeMatches(ClassNode[] argTypes, int index, Class clazz) {
        if (index >= argTypes.length) return false;
        return matchWithOrWithourBoxing(argTypes[index], clazz);
    }

    public boolean argTypeMatches(MethodCall call, int index, Class clazz) {
        ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
        ClassNode[] argumentTypes = typeCheckingVisitor.getArgumentTypes(argumentListExpression);
        return argTypeMatches(argumentTypes, index, clazz);
    }

    @SuppressWarnings("unchecked")
    public <R> R withTypeChecker(Closure<R> code) {
        Closure<R> clone = (Closure<R>) code.clone();
        clone.setDelegate(typeCheckingVisitor);
        clone.setResolveStrategy(Closure.DELEGATE_FIRST);
        return clone.call();
    }

    /**
     * Used to instruct the type checker that the call is a dynamic method call.
     * Calling this method automatically sets the handled flag to true. The expected
     * return type of the dynamic method call is Object.
     * @param call the method call which is a dynamic method call
     * @return a virtual method node with the same name as the expected call
     */
    public MethodNode makeDynamic(MethodCall call) {
        return makeDynamic(call, ClassHelper.OBJECT_TYPE);
    }

    /**
     * Used to instruct the type checker that the call is a dynamic method call.
     * Calling this method automatically sets the handled flag to true.
     * @param call the method call which is a dynamic method call
     * @param returnType the expected return type of the dynamic call
     * @return a virtual method node with the same name as the expected call
     */
    public MethodNode makeDynamic(MethodCall call, ClassNode returnType) {
        TypeCheckingContext.EnclosingClosure enclosingClosure = context.getEnclosingClosure();
        MethodNode enclosingMethod = context.getEnclosingMethod();
        ((ASTNode)call).putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, returnType);
        if (enclosingClosure!=null) {
            enclosingClosure.getClosureExpression().putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, Boolean.TRUE);
        } else {
            enclosingMethod.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, Boolean.TRUE);
        }
        setHandled(true);
        if (debug) {
            LOG.info("Turning "+call.getText()+" into a dynamic method call returning "+returnType.toString(false));
        }
        return new MethodNode(call.getMethodAsString(), 0, returnType, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
    }

    /**
     * Instructs the type checker that a property access is dynamic, returning an instance of an Object.
     * Calling this method automatically sets the handled flag to true.
     * @param pexp the property or attribute expression
     */
    public void makeDynamic(PropertyExpression pexp) {
        makeDynamic(pexp, ClassHelper.OBJECT_TYPE);
    }

    /**
     * Instructs the type checker that a property access is dynamic.
     * Calling this method automatically sets the handled flag to true.
     * @param pexp the property or attribute expression
     * @param returnType the type of the property
     */
    public void makeDynamic(PropertyExpression pexp, ClassNode returnType) {
        context.getEnclosingMethod().putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, Boolean.TRUE);
        pexp.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, returnType);
        storeType(pexp, returnType);
        setHandled(true);
        if (debug) {
            LOG.info("Turning '"+pexp.getText()+"' into a dynamic property access of type "+returnType.toString(false));
        }
    }

    /**
     * Instructs the type checker that an unresolved variable is a dynamic variable of type Object.
     * Calling this method automatically sets the handled flag to true.
     * @param vexp the dynamic variable
     */
    public void makeDynamic(VariableExpression vexp) {
        makeDynamic(vexp, ClassHelper.OBJECT_TYPE);
    }

    /**
     * Instructs the type checker that an unresolved variable is a dynamic variable.
     * @param returnType the type of the dynamic variable
     * Calling this method automatically sets the handled flag to true.
     * @param vexp the dynamic variable
     */
    public void makeDynamic(VariableExpression vexp, ClassNode returnType) {
        context.getEnclosingMethod().putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, Boolean.TRUE);
        vexp.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, returnType);
        storeType(vexp, returnType);
        setHandled(true);
        if (debug) {
            LOG.info("Turning '"+vexp.getText()+"' into a dynamic variable access of type "+returnType.toString(false));
        }
    }

    public void log(String message) {
        LOG.info(message);
    }

    public BinaryExpression getEnclosingBinaryExpression() {
        return context.getEnclosingBinaryExpression();
    }

    public void pushEnclosingBinaryExpression(final BinaryExpression binaryExpression) {
        context.pushEnclosingBinaryExpression(binaryExpression);
    }

    public void pushEnclosingClosureExpression(final ClosureExpression closureExpression) {
        context.pushEnclosingClosureExpression(closureExpression);
    }

    public Expression getEnclosingMethodCall() {
        return context.getEnclosingMethodCall();
    }

    public Expression popEnclosingMethodCall() {
        return context.popEnclosingMethodCall();
    }

    public MethodNode popEnclosingMethod() {
        return context.popEnclosingMethod();
    }

    public ClassNode getEnclosingClassNode() {
        return context.getEnclosingClassNode();
    }

    public List<MethodNode> getEnclosingMethods() {
        return context.getEnclosingMethods();
    }

    public MethodNode getEnclosingMethod() {
        return context.getEnclosingMethod();
    }

    public void popTemporaryTypeInfo() {
        context.popTemporaryTypeInfo();
    }

    public void pushEnclosingClassNode(final ClassNode classNode) {
        context.pushEnclosingClassNode(classNode);
    }

    public BinaryExpression popEnclosingBinaryExpression() {
        return context.popEnclosingBinaryExpression();
    }

    public List<ClassNode> getEnclosingClassNodes() {
        return context.getEnclosingClassNodes();
    }

    public List<TypeCheckingContext.EnclosingClosure> getEnclosingClosureStack() {
        return context.getEnclosingClosureStack();
    }

    public ClassNode popEnclosingClassNode() {
        return context.popEnclosingClassNode();
    }

    public void pushEnclosingMethod(final MethodNode methodNode) {
        context.pushEnclosingMethod(methodNode);
    }

    public Set<MethodNode> getGeneratedMethods() {
        return generatedMethods;
    }

    public List<BinaryExpression> getEnclosingBinaryExpressionStack() {
        return context.getEnclosingBinaryExpressionStack();
    }

    public TypeCheckingContext.EnclosingClosure getEnclosingClosure() {
        return context.getEnclosingClosure();
    }

    public List<Expression> getEnclosingMethodCalls() {
        return context.getEnclosingMethodCalls();
    }

    public void pushEnclosingMethodCall(final Expression call) {
        context.pushEnclosingMethodCall(call);
    }

    public TypeCheckingContext.EnclosingClosure popEnclosingClosure() {
        return context.popEnclosingClosure();
    }

    public void pushTemporaryTypeInfo() {
        context.pushTemporaryTypeInfo();
    }

    private static class TypeCheckingScope extends LinkedHashMap<String, Object> {
        private static final long serialVersionUID = 7607331333917615144L;
        private final AbstractTypeCheckingExtension.TypeCheckingScope parent;

        private TypeCheckingScope(final AbstractTypeCheckingExtension.TypeCheckingScope parentScope) {
            this.parent = parentScope;
        }

        public AbstractTypeCheckingExtension.TypeCheckingScope getParent() {
            return parent;
        }

    }
}
