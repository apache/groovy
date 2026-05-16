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
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
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
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.Opcodes;

import java.io.Serial;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

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

    /** Shared type-checking context exposed to extension helpers. */
    protected final TypeCheckingContext context;
    /** @see {@link #log(String)} */ protected boolean debug;
    /** @see {@link #setHandled(boolean)} */ protected boolean handled;
    private final Set<MethodNode> generatedMethods = new LinkedHashSet<>();
    private final LinkedList<TypeCheckingScope> scopeData = new LinkedList<>();

    /**
     * Creates an extension helper bound to the supplied visitor.
     */
    public AbstractTypeCheckingExtension(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor);
        this.context = typeCheckingVisitor.typeCheckingContext;
    }

    //--------------------------------------------------------------------------

    /**
     * Marks whether the current event was handled by the extension.
     */
    public void setHandled(final boolean handled) {
        this.handled = handled;
    }

    /**
     * Pushes a new extension scope onto the scope stack.
     */
    public TypeCheckingScope newScope() {
        TypeCheckingScope scope = new TypeCheckingScope(scopeData.peek());
        scopeData.addFirst(scope);
        return scope;
    }

    /**
     * Pushes a new scope and executes the supplied callback against it.
     */
    public TypeCheckingScope newScope(Closure code) {
        TypeCheckingScope scope = newScope();
        Closure callback = code.rehydrate(scope, this, this);
        callback.call();
        return scope;
    }

    /**
     * Pops and returns the current scope.
     */
    public TypeCheckingScope scopeExit() {
        return scopeData.removeFirst();
    }

    /**
     * Returns the current scope, or {@code null} if none is active.
     */
    public TypeCheckingScope getCurrentScope() {
        return scopeData.peek();
    }

    /**
     * Executes the supplied callback against the current scope and then pops it.
     */
    public TypeCheckingScope scopeExit(Closure code) {
        TypeCheckingScope scope = scopeData.peek();
        Closure copy = code.rehydrate(scope, this, this);
        copy.call();
        return scopeExit();
    }

    /**
     * Indicates whether the supplied method node was created by this extension.
     */
    public boolean isGenerated(MethodNode node) {
        return generatedMethods.contains(node);
    }

    /**
     * Wraps the supplied method node in a singleton list.
     */
    public List<MethodNode> unique(MethodNode node) {
        return Collections.singletonList(node);
    }

    /**
     * Creates a synthetic public method with the supplied return type.
     */
    public MethodNode newMethod(final String name, final Class returnType) {
        return newMethod(name, ClassHelper.make(returnType));
    }

    /**
     * Creates a synthetic public method with the supplied return type.
     */
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

    /**
     * Creates a synthetic public method whose return type is resolved lazily.
     */
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

    /**
     * Sets closure delegation metadata with {@link Closure#OWNER_FIRST}.
     */
    public void delegatesTo(ClassNode type) {
        delegatesTo(type, Closure.OWNER_FIRST);
    }

    /**
     * Sets closure delegation metadata for the supplied type and strategy.
     */
    public void delegatesTo(ClassNode type, int strategy) {
        delegatesTo(type, strategy, typeCheckingVisitor.typeCheckingContext.delegationMetadata);
    }

    /**
     * Sets closure delegation metadata for the supplied type, strategy, and parent metadata.
     */
    public void delegatesTo(ClassNode type, int strategy, DelegationMetadata parent) {
        typeCheckingVisitor.typeCheckingContext.delegationMetadata = new DelegationMetadata(type, strategy, parent);
    }

    /**
     * Checks whether the node is annotated with the supplied annotation type.
     */
    public boolean isAnnotatedBy(ASTNode node, Class annotation) {
        return isAnnotatedBy(node, ClassHelper.make(annotation));
    }

    /**
     * Checks whether the node is annotated with the supplied annotation node.
     */
    public boolean isAnnotatedBy(ASTNode node, ClassNode annotation) {
        return node instanceof AnnotatedNode && !((AnnotatedNode)node).getAnnotations(annotation).isEmpty();
    }

    /**
     * Indicates whether the variable resolves dynamically.
     */
    public boolean isDynamic(VariableExpression var) {
        return var.getAccessedVariable() instanceof DynamicVariable;
    }

    /**
     * Indicates whether the supplied method node models an extension method.
     */
    public boolean isExtensionMethod(MethodNode node) {
        return node instanceof ExtensionMethodNode;
    }

    /**
     * Returns the normalized argument list for the supplied method call.
     */
    public ArgumentListExpression getArguments(MethodCall call) {
        return InvocationWriter.makeArgumentList(call.getArguments());
    }

    /**
     * Invokes the supplied closure and reports any failure to the source unit.
     */
    protected Object safeCall(Closure closure, Object... args) {
        try {
            return closure.call(args);
        } catch (Exception err) {
            typeCheckingVisitor.getSourceUnit().addException(err);
            return null;
        }
    }

    /**
     * Indicates whether the supplied object is a method call expression.
     */
    public boolean isMethodCall(Object o) {
        return o instanceof MethodCallExpression;
    }

    /**
     * Checks whether the supplied argument types exactly match the given classes.
     */
    public boolean argTypesMatches(ClassNode[] argTypes, Class... classes) {
        if (classes == null) return argTypes == null || argTypes.length == 0;
        if (argTypes.length != classes.length) return false;
        boolean match = true;
        for (int i = 0; i < argTypes.length && match; i++) {
            match = matchWithOrWithoutBoxing(argTypes[i], classes[i]);
        }
        return match;
    }

    private static boolean matchWithOrWithoutBoxing(final ClassNode argType, final Class aClass) {
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

    /**
     * Checks whether a method call argument list exactly matches the given classes.
     */
    public boolean argTypesMatches(MethodCall call, Class... classes) {
        ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
        ClassNode[] argumentTypes = typeCheckingVisitor.getArgumentTypes(argumentListExpression);
        return argTypesMatches(argumentTypes, classes);
    }

    /**
     * Checks whether the leading argument types match the given classes.
     */
    public boolean firstArgTypesMatches(ClassNode[] argTypes, Class... classes) {
        if (classes == null) return argTypes == null || argTypes.length == 0;
        if (argTypes.length<classes.length) return false;
        boolean match = true;
        for (int i = 0; i < classes.length && match; i++) {
            match = matchWithOrWithoutBoxing(argTypes[i], classes[i]);
        }
        return match;
    }

    /**
     * Checks whether a method call starts with arguments matching the given classes.
     */
    public boolean firstArgTypesMatches(MethodCall call, Class... classes) {
        ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
        ClassNode[] argumentTypes = typeCheckingVisitor.getArgumentTypes(argumentListExpression);
        return firstArgTypesMatches(argumentTypes, classes);
    }

    /**
     * Checks whether the argument at the supplied index matches the given class.
     */
    public boolean argTypeMatches(ClassNode[] argTypes, int index, Class clazz) {
        if (index >= argTypes.length) return false;
        return matchWithOrWithoutBoxing(argTypes[index], clazz);
    }

    /**
     * Checks whether a method call argument at the supplied index matches the given class.
     */
    public boolean argTypeMatches(MethodCall call, int index, Class clazz) {
        ArgumentListExpression argumentListExpression = InvocationWriter.makeArgumentList(call.getArguments());
        ClassNode[] argumentTypes = typeCheckingVisitor.getArgumentTypes(argumentListExpression);
        return argTypeMatches(argumentTypes, index, clazz);
    }

    /**
     * Executes the supplied closure with the type checker as delegate.
     */
    public <R> R withTypeChecker(@DelegatesTo(value = StaticTypeCheckingVisitor.class, strategy = Closure.DELEGATE_FIRST)
            @ClosureParams(value = SimpleType.class, options = "org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor") final Closure<R> code) {
        return DefaultGroovyMethods.with(typeCheckingVisitor, code);
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
        ((ASTNode) call).putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, returnType);
        setHandled(true);
        if (debug) {
            log("Turning " + call.getText() + " into a dynamic method call returning " + StaticTypeCheckingSupport.prettyPrintType(returnType));
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
        pexp.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, returnType);
        storeType(pexp, returnType);
        setHandled(true);
        if (debug) {
            log("Turning '" + pexp.getText() + "' into a dynamic property access of type " + StaticTypeCheckingSupport.prettyPrintType(returnType));
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
        vexp.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, returnType);
        storeType(vexp, returnType);
        setHandled(true);
        if (debug) {
            log("Turning '" + vexp.getText() + "' into a dynamic variable access of type " + StaticTypeCheckingSupport.prettyPrintType(returnType));
        }
    }

    /**
     * Logs a type-checking extension message.
     */
    public void log(final String message) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GroovyTypeCheckingExtensionSupport.class.getName());
        logger.info(message);
    }

    /**
     * Returns the current enclosing binary expression.
     */
    public BinaryExpression getEnclosingBinaryExpression() {
        return context.getEnclosingBinaryExpression();
    }

    /**
     * Pushes an enclosing binary expression onto the context stack.
     */
    public void pushEnclosingBinaryExpression(final BinaryExpression binaryExpression) {
        context.pushEnclosingBinaryExpression(binaryExpression);
    }

    /**
     * Pushes an enclosing closure expression onto the context stack.
     */
    public void pushEnclosingClosureExpression(final ClosureExpression closureExpression) {
        context.pushEnclosingClosureExpression(closureExpression);
    }

    /**
     * Returns the current enclosing method call.
     */
    public Expression getEnclosingMethodCall() {
        return context.getEnclosingMethodCall();
    }

    /**
     * Pops the current enclosing method call.
     */
    public Expression popEnclosingMethodCall() {
        return context.popEnclosingMethodCall();
    }

    /**
     * Pops the current enclosing method.
     */
    public MethodNode popEnclosingMethod() {
        return context.popEnclosingMethod();
    }

    /**
     * Returns the current enclosing class node.
     */
    public ClassNode getEnclosingClassNode() {
        return context.getEnclosingClassNode();
    }

    /**
     * Returns the enclosing method stack.
     */
    public List<MethodNode> getEnclosingMethods() {
        return context.getEnclosingMethods();
    }

    /**
     * Returns the current enclosing method.
     */
    public MethodNode getEnclosingMethod() {
        return context.getEnclosingMethod();
    }

    /**
     * Pops the temporary type-information stack.
     */
    public void popTemporaryTypeInfo() {
        context.popTemporaryTypeInfo();
    }

    /**
     * Pushes an enclosing class node onto the context stack.
     */
    public void pushEnclosingClassNode(final ClassNode classNode) {
        context.pushEnclosingClassNode(classNode);
    }

    /**
     * Pops the current enclosing binary expression.
     */
    public BinaryExpression popEnclosingBinaryExpression() {
        return context.popEnclosingBinaryExpression();
    }

    /**
     * Returns the enclosing class stack.
     */
    public List<ClassNode> getEnclosingClassNodes() {
        return context.getEnclosingClassNodes();
    }

    /**
     * Returns the enclosing closure stack.
     */
    public List<TypeCheckingContext.EnclosingClosure> getEnclosingClosureStack() {
        return context.getEnclosingClosureStack();
    }

    /**
     * Pops the current enclosing class node.
     */
    public ClassNode popEnclosingClassNode() {
        return context.popEnclosingClassNode();
    }

    /**
     * Pushes an enclosing method onto the context stack.
     */
    public void pushEnclosingMethod(final MethodNode methodNode) {
        context.pushEnclosingMethod(methodNode);
    }

    /**
     * Returns the generated methods created by this extension.
     */
    public Set<MethodNode> getGeneratedMethods() {
        return generatedMethods;
    }

    /**
     * Returns the enclosing binary-expression stack.
     */
    public List<BinaryExpression> getEnclosingBinaryExpressionStack() {
        return context.getEnclosingBinaryExpressionStack();
    }

    /**
     * Returns the current enclosing closure metadata.
     */
    public TypeCheckingContext.EnclosingClosure getEnclosingClosure() {
        return context.getEnclosingClosure();
    }

    /**
     * Returns the enclosing method-call stack.
     */
    public List<Expression> getEnclosingMethodCalls() {
        return context.getEnclosingMethodCalls();
    }

    /**
     * Pushes an enclosing method call onto the context stack.
     */
    public void pushEnclosingMethodCall(final Expression call) {
        context.pushEnclosingMethodCall(call);
    }

    /**
     * Pops the current enclosing closure metadata.
     */
    public TypeCheckingContext.EnclosingClosure popEnclosingClosure() {
        return context.popEnclosingClosure();
    }

    /**
     * Pushes a new temporary type-information frame.
     */
    public void pushTemporaryTypeInfo() {
        context.pushTemporaryTypeInfo();
    }

    //--------------------------------------------------------------------------

    /**
     * Map-backed scope used to share data across extension callbacks.
     */
    public static class TypeCheckingScope extends LinkedHashMap<String, Object> {
        @Serial
        private static final long serialVersionUID = 7607331333917615144L;
        private final AbstractTypeCheckingExtension.TypeCheckingScope parent;

        private TypeCheckingScope(final AbstractTypeCheckingExtension.TypeCheckingScope parentScope) {
            this.parent = parentScope;
        }

        /**
         * Returns the parent scope, or {@code null} for the root scope.
         */
        public AbstractTypeCheckingExtension.TypeCheckingScope getParent() {
            return parent;
        }
    }
}
