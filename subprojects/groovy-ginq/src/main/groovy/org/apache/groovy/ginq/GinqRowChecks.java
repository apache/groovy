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
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.apache.groovy.ginq.provider.collection.runtime.Queryable;
import org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper;
import org.apache.groovy.ginq.provider.collection.runtime.SourceRecord;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.SourceUnit;
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
     * Metadata on a {@code navigate} call: the alias names of the row, in
     * from-then-join order. A static property {@code r.alias} uses them to
     * pick the component type of a join tuple.
     */
    public static final String ALIAS_NAMES = "ginqAliasNames";

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
        if (insideQuery() && compileStaticScope() && call instanceof MethodCallExpression methodCall) {
            rememberAliases(methodCall);
        }
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
            ClassNode value = sourceRecordValue(expression);
            makeDynamic(expression, value != null ? value : ClassHelper.OBJECT_TYPE);
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
        if (insideQuery() && compileStaticScope() && "navigate".equals(methodCall.getMethodAsString())) {
            repairNavigateType(methodCall);
        }
        if (insideQuery()) {
            if (!compileStaticScope()) {
                if (methodCall.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION) != null) {
                    methodCall.setMethodTarget(null);
                    methodCall.removeNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                }
            } else {
                MethodNode target = methodCall.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
                if (standIn(methodCall, target) && !"navigate".equals(methodCall.getMethodAsString())) {
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
        if (compileStaticScope()) {
            node.getCode().visit(new UnprovableOperatorRewriter());
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

    /**
     * The static type of {@code row.alias} when {@code row} is a
     * {@code SourceRecord}. A single source is that element type. A join is
     * the matching component of the nested tuple.
     */
    private ClassNode sourceRecordValue(PropertyExpression expression) {
        if (!compileStaticScope()) {
            return null;
        }
        String name = expression.getPropertyAsString();
        if (name == null) {
            return null;
        }
        ClassNode receiver = typeOf(expression.getObjectExpression());
        if (receiver == null || !"org.apache.groovy.ginq.provider.collection.runtime.SourceRecord".equals(receiver.redirect().getName())) {
            return null;
        }
        ClassNode element = ClassHelper.OBJECT_TYPE;
        GenericsType[] arguments = receiver.getGenericsTypes();
        if (arguments != null && arguments.length == 1 && arguments[0].getType() != null) {
            element = arguments[0].getType();
            if (ClassHelper.isPrimitiveType(element) && !ClassHelper.isPrimitiveVoid(element)) {
                element = ClassHelper.getWrapper(element);
            }
        }
        return aliasType(element, aliasNames(), name);
    }

    private void rememberAliases(MethodCallExpression call) {
        List<String> names = aliasNames(call.getObjectExpression());
        if (names == null) {
            names = aliasNames(call);
        }
        if (names == null || !(call.getArguments() instanceof TupleExpression tuple)) {
            return;
        }
        for (Expression argument : tuple.getExpressions()) {
            if (argument instanceof ClosureExpression closure && closure.getNodeMetaData(ALIAS_NAMES) == null) {
                closure.putNodeMetaData(ALIAS_NAMES, names);
            }
        }
    }

    /**
     * {@code navigate} is {@code Queryable<SourceRecord<T>>}. When the argument
     * is still {@code Object}, method selection fails and {@code stream()} binds
     * to the single-element DGM instead of {@link org.apache.groovy.ginq.provider.collection.runtime.Queryable#stream()}.
     * Restore the return type from the argument, or from the clause row parameter.
     */
    private void repairNavigateType(MethodCallExpression call) {
        if (!(call.getObjectExpression() instanceof ClassExpression owner)
                || !"org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper".equals(owner.getType().redirect().getName())) {
            return;
        }
        ClassNode result = typeOf(call);
        if (result != null && !ClassHelper.OBJECT_TYPE.equals(result) && !ClassHelper.isDynamicTyped(result)) {
            return;
        }
        ClassNode element = elementType(call);
        ClassNode record = GenericsUtils.makeClassSafeWithGenerics(SourceRecord.class, element);
        ClassNode queryable = GenericsUtils.makeClassSafeWithGenerics(Queryable.class, record);
        storeType(call, queryable);
        call.removeNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION);
        for (MethodNode method : ClassHelper.make(QueryableHelper.class).getDeclaredMethods("navigate")) {
            if (method.getParameters().length == 2) {
                call.setMethodTarget(method);
                call.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, method);
                break;
            }
        }
    }

    private ClassNode elementType(MethodCallExpression navigate) {
        if (navigate.getArguments() instanceof TupleExpression tuple && !tuple.getExpressions().isEmpty()) {
            ClassNode argument = typeOf(tuple.getExpression(0));
            if (argument != null && "org.apache.groovy.ginq.provider.collection.runtime.Queryable".equals(argument.redirect().getName())) {
                GenericsType[] arguments = argument.getGenericsTypes();
                if (arguments != null && arguments.length == 1 && arguments[0].getType() != null
                        && !ClassHelper.OBJECT_TYPE.equals(arguments[0].getType())
                        && !ClassHelper.isDynamicTyped(arguments[0].getType())) {
                    ClassNode type = arguments[0].getType();
                    return ClassHelper.isPrimitiveType(type) ? ClassHelper.getWrapper(type) : type;
                }
            }
        }
        List<TypeCheckingContext.EnclosingClosure> stack = typeCheckingVisitor.getTypeCheckingContext().getEnclosingClosureStack();
        for (int i = stack.size() - 1; i >= 0; i -= 1) {
            Parameter[] parameters = stack.get(i).getClosureExpression().getParameters();
            if (parameters == null) {
                continue;
            }
            for (Parameter parameter : parameters) {
                ClassNode type = parameter.getType();
                if (type == null || ClassHelper.isDynamicTyped(type) || ClassHelper.OBJECT_TYPE.equals(type)) {
                    continue;
                }
                if ("org.apache.groovy.ginq.provider.collection.runtime.Queryable".equals(type.redirect().getName())) {
                    continue;
                }
                return ClassHelper.isPrimitiveType(type) ? ClassHelper.getWrapper(type) : type;
            }
        }
        return ClassHelper.OBJECT_TYPE;
    }

    private List<String> aliasNames() {
        for (Expression call : typeCheckingVisitor.getTypeCheckingContext().getEnclosingMethodCalls()) {
            List<String> names = aliasNames(call);
            if (names != null) {
                return names;
            }
        }
        List<TypeCheckingContext.EnclosingClosure> stack = typeCheckingVisitor.getTypeCheckingContext().getEnclosingClosureStack();
        for (TypeCheckingContext.EnclosingClosure enclosing : stack) {
            Object metadata = enclosing.getClosureExpression().getNodeMetaData(ALIAS_NAMES);
            if (metadata instanceof List<?> names && !names.isEmpty() && names.get(0) instanceof String) {
                List<String> aliases = new ArrayList<>();
                for (Object name : names) {
                    if (name instanceof String text) {
                        aliases.add(text);
                    }
                }
                return aliases;
            }
        }
        return null;
    }

    private static List<String> aliasNames(Expression expression) {
        Expression current = expression;
        while (current instanceof MethodCallExpression call) {
            if ("navigate".equals(call.getMethodAsString()) && call.getArguments() instanceof TupleExpression tuple
                    && tuple.getExpressions().size() >= 2) {
                List<String> names = constantStrings(tuple.getExpression(1));
                if (names != null) {
                    return names;
                }
            }
            current = call.getObjectExpression();
        }
        return null;
    }

    private static List<String> constantStrings(Expression expression) {
        if (!(expression instanceof org.codehaus.groovy.ast.expr.ListExpression list)) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Expression element : list.getExpressions()) {
            if (!(element instanceof org.codehaus.groovy.ast.expr.ConstantExpression constant) || !(constant.getValue() instanceof String text)) {
                return null;
            }
            names.add(text);
        }
        return names.isEmpty() ? null : names;
    }

    private static ClassNode aliasType(ClassNode element, List<String> aliases, String name) {
        if (element == null) {
            return ClassHelper.OBJECT_TYPE;
        }
        if (aliases == null || aliases.isEmpty() || !tuple(element)) {
            return element;
        }
        ClassNode current = element;
        for (int i = aliases.size() - 1; i >= 0 && current != null; i -= 1) {
            if (name.equals(aliases.get(i))) {
                return tuple(current) ? tupleComponent(current, 1) : current;
            }
            if (!tuple(current)) {
                break;
            }
            current = tupleComponent(current, 0);
        }
        return element;
    }

    private static boolean tuple(ClassNode type) {
        return type != null && "groovy.lang.Tuple2".equals(type.redirect().getName());
    }

    private static ClassNode tupleComponent(ClassNode tuple, int index) {
        GenericsType[] arguments = tuple.getGenericsTypes();
        if (arguments == null || arguments.length <= index || arguments[index].getType() == null) {
            return ClassHelper.OBJECT_TYPE;
        }
        ClassNode type = arguments[index].getType();
        if (ClassHelper.isPrimitiveType(type) && !ClassHelper.isPrimitiveVoid(type)) {
            return ClassHelper.getWrapper(type);
        }
        return type;
    }

    /**
     * Rewrites an arithmetic operator or subscript the static writer cannot
     * prove into {@code ScriptBytecodeAdapter.invokeMethodN}. A number, a
     * string concatenation, a map read, and an array read stay operators.
     */
    private static final class UnprovableOperatorRewriter extends ClassCodeExpressionTransformer {
        private int queryDepth;

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public Expression transform(Expression expression) {
            if (expression instanceof ClosureExpression closure) {
                boolean query = Boolean.TRUE.equals(closure.getNodeMetaData(QUERY));
                if (query) {
                    queryDepth += 1;
                }
                closure.visit(this);
                if (query) {
                    queryDepth -= 1;
                }
                return closure;
            }
            if (expression instanceof BinaryExpression binary && Types.isAssignment(binary.getOperation().getType())) {
                Expression left = binary.getLeftExpression();
                if (left instanceof BinaryExpression index && index.getOperation().getType() == Types.LEFT_SQUARE_BRACKET) {
                    index.setLeftExpression(transform(index.getLeftExpression()));
                    index.setRightExpression(transform(index.getRightExpression()));
                } else {
                    binary.setLeftExpression(transform(left));
                }
                binary.setRightExpression(transform(binary.getRightExpression()));
                return binary;
            }
            Expression transformed = super.transform(expression);
            if (queryDepth > 0 && transformed instanceof BinaryExpression binary) {
                Expression rewritten = unprovable(binary);
                if (rewritten != null) {
                    return rewritten;
                }
            }
            return transformed;
        }

        private static Expression unprovable(BinaryExpression binary) {
            String name = operatorName(binary.getOperation().getType());
            if (name == null || provable(binary)) {
                return null;
            }
            ArrayExpression array = new ArrayExpression(ClassHelper.OBJECT_TYPE, new ArrayList<>(List.of(binary.getRightExpression())));
            MethodCallExpression call = callX(classX(ClassHelper.make(ScriptBytecodeAdapter.class)), "invokeMethodN",
                    args(nullX(), binary.getLeftExpression(), constX(name), array));
            call.setImplicitThis(false);
            call.setSourcePosition(binary);
            return call;
        }

        private static boolean provable(BinaryExpression binary) {
            int operation = binary.getOperation().getType();
            ClassNode left = typeOf(binary.getLeftExpression());
            ClassNode right = typeOf(binary.getRightExpression());
            if (operation == Types.LEFT_SQUARE_BRACKET) {
                if (binary.getLeftExpression() instanceof VariableExpression variable && "ginqCaptured$".equals(variable.getName())) {
                    return true;
                }
                if (left != null && left.isArray() && number(right)) {
                    return true;
                }
                return left != null && (left.implementsInterface(ClassHelper.MAP_TYPE) || ClassHelper.MAP_TYPE.equals(left.redirect()));
            }
            if (operation == Types.PLUS && left != null && ClassHelper.isStringType(left)) {
                return true;
            }
            return number(left) && number(right);
        }

        private static boolean number(ClassNode type) {
            if (type == null || ClassHelper.isDynamicTyped(type) || ClassHelper.OBJECT_TYPE.equals(type)) {
                return false;
            }
            if (ClassHelper.isPrimitiveType(type)) {
                return !ClassHelper.isPrimitiveBoolean(type) && !ClassHelper.isPrimitiveVoid(type);
            }
            return ClassHelper.getWrapper(type).isDerivedFrom(ClassHelper.Number_TYPE);
        }

        private static String operatorName(int operation) {
            return switch (operation) {
                case Types.PLUS -> "plus";
                case Types.MINUS -> "minus";
                case Types.MULTIPLY -> "multiply";
                case Types.DIVIDE -> "div";
                case Types.INTDIV -> "intdiv";
                case Types.MOD -> "mod";
                case Types.REMAINDER -> "remainder";
                case Types.POWER -> "power";
                case Types.LEFT_SQUARE_BRACKET -> "getAt";
                case Types.LEFT_SHIFT -> "leftShift";
                case Types.RIGHT_SHIFT -> "rightShift";
                case Types.RIGHT_SHIFT_UNSIGNED -> "rightShiftUnsigned";
                case Types.BITWISE_AND -> "and";
                case Types.BITWISE_OR -> "or";
                case Types.BITWISE_XOR -> "xor";
                default -> null;
            };
        }
    }
}
