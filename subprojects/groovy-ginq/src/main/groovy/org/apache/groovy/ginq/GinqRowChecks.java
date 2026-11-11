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
package org.apache.groovy.ginq;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.groovy.transform.stc.TypeCheckingContext;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Type-checking extension for a GINQ query under {@code @CompileStatic} and {@code @TypeChecked}.
 * <p>
 * The extension is registered on the checking annotation, so it is present for the whole
 * class. Every hook returns immediately unless the expression is inside a closure marked
 * {@link #QUERY}. Code outside that closure is left to the ordinary checker.
 * <p>
 * The lowered query closure stays on the static writer. A call the checker selects,
 * including a target another extension replaced, is kept. An unresolved call is
 * rewritten to {@link ScriptBytecodeAdapter} so the stand-in is not invoked.
 * {@code from} stays dynamic when the chosen parameter is an array and the
 * argument is not. A target whose declaring class is not public stays dynamic.
 * Outside the query, only a query result used as the receiver of {@code toList},
 * {@code get}, {@code getAt}, or a subscript is treated as a row. A field or an
 * enclosing-method parameter is left to the ordinary checker.
 *
 * @since 7.0.0
 */
public class GinqRowChecks extends AbstractTypeCheckingExtension {

    /**
     * Metadata key on a closure that was lowered from a GINQ query.
     * The value is {@link Boolean#TRUE}.
     */
    static final Object QUERY = GinqRowChecks.class;

    /**
     * Creates the extension for one type-checking visitor.
     *
     * @param typeCheckingVisitor the visitor compiling the class that contains the query
     */
    public GinqRowChecks(StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor);
    }

    @Override
    public boolean beforeMethodCall(MethodCall call) {
        if (!insideQuery() || !(call instanceof Expression expression) || compileStaticScope()) {
            // Static compilation has to visit the arguments so a method another
            // extension selects is still the target that is emitted.
            return false;
        }
        // @TypeChecked does not emit that target. Inferring clause SAM types
        // here writes a generic signature the dynamic closure cannot load.
        makeDynamic(call, ClassHelper.OBJECT_TYPE);
        storeType(expression, ClassHelper.OBJECT_TYPE);
        return true;
    }

    @Override
    public boolean handleUnresolvedProperty(PropertyExpression expression) {
        if (insideQuery() || resultReceiver(expression.getObjectExpression())) {
            makeDynamic(expression);
            return true;
        }
        return false;
    }

    @Override
    public List<MethodNode> handleMissingMethod(ClassNode receiver, String name, ArgumentListExpression argumentList, ClassNode[] argumentTypes, MethodCall call) {
        if (call == null) {
            return Collections.emptyList();
        }
        boolean query = insideQuery();
        boolean result = !query && call instanceof MethodCallExpression methodCall && resultReceiver(methodCall.getObjectExpression());
        if (!query && !result) {
            return Collections.emptyList();
        }
        if (result && !"toList".equals(name) && !"get".equals(name) && !"getAt".equals(name)) {
            return Collections.emptyList();
        }
        ClassNode type = "toList".equals(name) ? ClassHelper.LIST_TYPE : ClassHelper.OBJECT_TYPE;
        MethodNode standIn = makeDynamic(call, type);
        // A null declaring class fails later when the target is stored. Object
        // is only the stand-in's owner: the call is rewritten to a real adapter
        // method before bytecode, so this is not invokevirtual Object.name().
        if (standIn.getDeclaringClass() == null) {
            standIn.setDeclaringClass(ClassHelper.OBJECT_TYPE);
        }
        return Collections.singletonList(standIn);
    }

    @Override
    public boolean handleIncompatibleReturnType(ReturnStatement statement, ClassNode inferredReturnType) {
        // A clause lambda's row value is dynamic. The SAM type is what a
        // typed aggregate asked for; the value is coerced when the lambda runs.
        return insideQuery();
    }

    @Override
    public void afterMethodCall(MethodCall call) {
        if (!(call instanceof MethodCallExpression methodCall)) {
            return;
        }
        if (insideQuery()) {
            if (!compileStaticScope()) {
                if (methodCall.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION) != null) {
                    methodCall.setMethodTarget(null);
                    methodCall.removeNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                }
            } else {
                MethodNode target = methodCall.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                if (standIn(methodCall, target)) {
                    rewriteUnresolved(methodCall);
                } else if (target != null && arrayFromMismatch(methodCall, target)) {
                    methodCall.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, ClassHelper.OBJECT_TYPE);
                } else if (target != null && hiddenType(target.getDeclaringClass())) {
                    methodCall.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, ClassHelper.OBJECT_TYPE);
                }
            }
        }
        if (!"toList".equals(methodCall.getMethodAsString())) {
            return;
        }
        Expression receiver = methodCall.getObjectExpression();
        if (!(receiver instanceof MethodCallExpression macro) || !"macroMethod".equals(macro.getMethodAsString())) {
            return;
        }
        // The macro result is still MacroStub.macroMethod(query) when the checker
        // runs. toList() on that call is a raw List.
        ClassNode list = ClassHelper.LIST_TYPE.getPlainNodeReference();
        list.setGenericsTypes(null);
        list.setUsingGenerics(false);
        storeType(methodCall, list);
    }

    /**
     * A subscript on a query result is coerced to {@code List} so static compilation
     * can emit {@code getAt}. The coercion stays outside the query closure: inside it
     * the dynamic writer handles the subscript, and a method parameter or field is
     * left alone so {@code value[0]} on an unrelated {@code Object} is still an error.
     */
    @Override
    public void afterVisitMethod(MethodNode node) {
        if (node.getCode() == null) {
            return;
        }
        node.getCode().visit(new CodeVisitorSupport() {
            private int queryDepth;

            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                boolean query = Boolean.TRUE.equals(expression.getNodeMetaData(QUERY));
                if (query) {
                    queryDepth++;
                }
                super.visitClosureExpression(expression);
                if (query) {
                    queryDepth--;
                }
            }

            @Override
            public void visitBinaryExpression(BinaryExpression expression) {
                if (queryDepth == 0
                        && expression.getOperation().getType() == Types.LEFT_SQUARE_BRACKET
                        && resultReceiver(expression.getLeftExpression())) {
                    CastExpression cast = castX(ClassHelper.LIST_TYPE, expression.getLeftExpression());
                    cast.setCoerce(true);
                    cast.setSourcePosition(expression.getLeftExpression());
                    expression.setLeftExpression(cast);
                }
                super.visitBinaryExpression(expression);
            }
        });
    }

    /**
     * A closure or lambda parameter that holds a query row. A field or a parameter
     * of the enclosing method is not a row, so a typo on it stays an error.
     */
    private boolean resultReceiver(Expression receiver) {
        ClassNode type = typeOf(receiver);
        if (namedRow(type)) {
            return true;
        }
        if (receiver instanceof BinaryExpression binary
                && binary.getOperation().getType() == Types.LEFT_SQUARE_BRACKET
                && heldLocal(binary.getLeftExpression())) {
            return true;
        }
        if (receiver instanceof MethodCallExpression call) {
            String name = call.getMethodAsString();
            if (("getAt".equals(name) || "get".equals(name)) && heldLocal(call.getObjectExpression())) {
                return true;
            }
        }
        if (!(receiver instanceof VariableExpression variable)) {
            return false;
        }
        Variable accessed = variable.getAccessedVariable();
        if (accessed instanceof FieldNode || accessed instanceof PropertyNode) {
            return false;
        }
        if (accessed instanceof Parameter parameter) {
            MethodNode method = typeCheckingVisitor.getTypeCheckingContext().getEnclosingMethod();
            if (method != null) {
                for (Parameter declared : method.getParameters()) {
                    if (declared == parameter) {
                        return false;
                    }
                }
            }
            return true;
        }
        return "it".equals(variable.getName())
                && !typeCheckingVisitor.getTypeCheckingContext().getEnclosingClosureStack().isEmpty();
    }

    /**
     * A local or nested row holder. A field and an enclosing-method parameter are excluded,
     * so {@code value[0]} on those stays a static error. A local such as the {@code GQ}
     * result is included, so {@code result[0].toList()} still type-checks.
     */
    private boolean heldLocal(Expression receiver) {
        if (resultReceiver(receiver)) {
            return true;
        }
        if (receiver instanceof BinaryExpression binary
                && binary.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
            return heldLocal(binary.getLeftExpression());
        }
        if (!(receiver instanceof VariableExpression variable)) {
            return false;
        }
        Variable accessed = variable.getAccessedVariable();
        if (accessed instanceof FieldNode || accessed instanceof PropertyNode) {
            return false;
        }
        if (accessed instanceof Parameter parameter) {
            MethodNode method = typeCheckingVisitor.getTypeCheckingContext().getEnclosingMethod();
            if (method != null) {
                for (Parameter declared : method.getParameters()) {
                    if (declared == parameter) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@code @CompileStatic} emits the method the checker selected.
     * {@code @TypeChecked} only checks, and the clause stays a dynamic closure.
     */
    private boolean compileStaticScope() {
        TypeCheckingContext context = typeCheckingVisitor.getTypeCheckingContext();
        if (annotated(context.getEnclosingMethod(), "groovy.transform.CompileDynamic")) {
            return false;
        }
        if (annotated(context.getEnclosingMethod(), "groovy.transform.CompileStatic")) {
            return true;
        }
        ClassNode type = context.getEnclosingClassNode();
        while (type != null) {
            if (annotated(type, "groovy.transform.CompileDynamic")) {
                return false;
            }
            if (annotated(type, "groovy.transform.CompileStatic")) {
                return true;
            }
            type = type.getOuterClass();
        }
        return false;
    }

    private static boolean annotated(AnnotatedNode node, String name) {
        if (node == null) {
            return false;
        }
        for (AnnotationNode annotation : node.getAnnotations()) {
            ClassNode type = annotation.getClassNode();
            if (type != null && name.equals(type.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean insideQuery() {
        List<TypeCheckingContext.EnclosingClosure> stack = typeCheckingVisitor.getTypeCheckingContext().getEnclosingClosureStack();
        for (TypeCheckingContext.EnclosingClosure enclosing : stack) {
            if (Boolean.TRUE.equals(enclosing.getClosureExpression().getNodeMetaData(QUERY))) {
                return true;
            }
        }
        return false;
    }

    private static ClassNode typeOf(Expression expression) {
        ClassNode inferred = expression.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        return inferred != null ? inferred : expression.getType();
    }

    private static boolean namedRow(ClassNode type) {
        if (type == null) {
            return false;
        }
        String name = type.getName();
        return "org.apache.groovy.ginq.provider.collection.runtime.NamedRecord".equals(name)
                || "org.apache.groovy.ginq.provider.collection.runtime.NamedTuple".equals(name)
                || "org.apache.groovy.ginq.provider.collection.runtime.SourceRecord".equals(name)
                || "org.apache.groovy.ginq.provider.collection.runtime.GroupResult".equals(name);
    }

    /**
     * A {@code makeDynamic} stand-in has no real parameter list. A selected
     * method, including one another extension stored, is not a stand-in.
     */
    private static boolean standIn(MethodCallExpression call, MethodNode target) {
        if (call.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION) == null) {
            return false;
        }
        String name = call.getMethodAsString();
        if ("invokeMethodN".equals(name) || "invokeMethodNSafe".equals(name)) {
            return false;
        }
        if (target == null || target.getDeclaringClass() == null) {
            return true;
        }
        return target.getParameters().length == 0 && target.getName().equals(name);
    }

    private static boolean arrayFromMismatch(MethodCallExpression call, MethodNode target) {
        if (!"from".equals(target.getName()) || target.getParameters().length != 1) {
            return false;
        }
        if (!target.getParameters()[0].getType().isArray()) {
            return false;
        }
        List<Expression> arguments = argumentList(call.getArguments());
        if (arguments.size() != 1) {
            return false;
        }
        ClassNode argumentType = arguments.get(0).getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (argumentType == null) {
            argumentType = arguments.get(0).getType();
        }
        return argumentType == null || !argumentType.isArray();
    }

    private static boolean hiddenType(ClassNode type) {
        if (type == null || !type.isResolved()) {
            return false;
        }
        return !Modifier.isPublic(type.redirect().getModifiers());
    }

    /**
     * Replaces an unresolved call with {@code ScriptBytecodeAdapter.invokeMethodN}.
     * The adapter method is a real public target. The original receiver and
     * arguments are the values it dispatches.
     */
    private void rewriteUnresolved(MethodCallExpression call) {
        String name = call.getMethodAsString();
        if (name == null) {
            return;
        }
        Expression object = call.getObjectExpression();
        // implicitThis stays true on calls the walker built with an explicit
        // receiver. Only a real this-call uses the enclosing instance.
        boolean implicit = call.isImplicitThis() && thisReceiver(object);
        Expression receiver = implicit ? implicitReceiver() : object;
        List<Expression> original = argumentList(call.getArguments());
        ArrayExpression array = new ArrayExpression(ClassHelper.OBJECT_TYPE, new ArrayList<>(original));
        String adapter = call.isSafe() ? "invokeMethodNSafe" : "invokeMethodN";
        call.setObjectExpression(classX(ClassHelper.make(ScriptBytecodeAdapter.class)));
        call.setMethod(constX(adapter));
        call.setArguments(args(nullX(), receiver, constX(name), array));
        call.setImplicitThis(false);
        call.setSafe(false);
        MethodNode target = adapterMethod(adapter);
        call.setMethodTarget(target);
        call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, target);
        call.removeNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION);
        storeType(call, ClassHelper.OBJECT_TYPE);
    }

    private static boolean thisReceiver(Expression object) {
        return object instanceof VariableExpression variable && "this".equals(variable.getName());
    }

    private Expression implicitReceiver() {
        MethodNode enclosing = typeCheckingVisitor.getTypeCheckingContext().getEnclosingMethod();
        if (enclosing != null && enclosing.isStatic()) {
            return classX(typeCheckingVisitor.getTypeCheckingContext().getEnclosingClassNode());
        }
        return varX("this");
    }

    private static MethodNode adapterMethod(String name) {
        ClassNode type = ClassHelper.make(ScriptBytecodeAdapter.class);
        for (MethodNode method : type.getDeclaredMethods(name)) {
            if (method.getParameters().length == 4) {
                return method;
            }
        }
        throw new IllegalStateException(name);
    }

    private static List<Expression> argumentList(Expression arguments) {
        if (arguments instanceof TupleExpression tuple) {
            return new ArrayList<>(tuple.getExpressions());
        }
        List<Expression> one = new ArrayList<>();
        if (arguments != null) {
            one.add(arguments);
        }
        return one;
    }
}
