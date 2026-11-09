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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.groovy.transform.stc.TypeCheckingContext;

import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;

/**
 * Type-checking extension for GINQ under {@code @CompileStatic} and {@code @TypeChecked}.
 * <p>
 * The lowered query is a closure marked {@link StaticTypesMarker#DYNAMIC_RESOLUTION}.
 * Calls directly in that closure are left dynamic, and the static closure writer
 * emits the closure with the dynamic writer, so the clause bodies keep Groovy's
 * object protocol. The enclosing method is still type-checked and, under
 * {@code @CompileStatic}, statically compiled.
 * <p>
 * A projection may also be a scalar, a named record, or a group. Callers still
 * write {@code row.powerOfN}, {@code row[0]} and {@code it.toList()} on the
 * result. A resolved property or method is still checked. Classes that do not
 * contain a query are not given this extension.
 *
 * @since 7.0.0
 */
public class GinqRowChecks extends AbstractTypeCheckingExtension {

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
        if (!insideDynamicQuery() || !(call instanceof Expression expression)) {
            return false;
        }
        // The clause bodies are compiled by the dynamic writer. Skipping them
        // here is what keeps an unresolved row property from failing the method.
        makeDynamic(call, ClassHelper.OBJECT_TYPE);
        storeType(expression, ClassHelper.OBJECT_TYPE);
        return true;
    }

    @Override
    public boolean handleUnresolvedProperty(PropertyExpression expression) {
        if (insideDynamicQuery()) {
            makeDynamic(expression);
            return true;
        }
        if (!rowOrUntyped(typeOf(expression.getObjectExpression()))) {
            return false;
        }
        makeDynamic(expression);
        return true;
    }

    @Override
    public List<MethodNode> handleMissingMethod(ClassNode receiver, String name, ArgumentListExpression argumentList, ClassNode[] argumentTypes, MethodCall call) {
        if (insideDynamicQuery() && call != null) {
            return Collections.singletonList(makeDynamic(call, ClassHelper.OBJECT_TYPE));
        }
        if (!rowOrUntyped(receiver)) {
            return Collections.emptyList();
        }
        if (!"toList".equals(name) && !"get".equals(name) && !"getAt".equals(name)) {
            return Collections.emptyList();
        }
        ClassNode result = "toList".equals(name) ? ClassHelper.LIST_TYPE : ClassHelper.OBJECT_TYPE;
        return Collections.singletonList(makeDynamic(call, result));
    }

    /**
     * Static compilation cannot emit {@code Object#getAt}. A GINQ row is a list,
     * so an untyped subscript is coerced to {@code List} and uses the real method.
     */
    @Override
    public void afterMethodCall(MethodCall call) {
        if (!(call instanceof MethodCallExpression methodCall)) {
            return;
        }
        if (!"toList".equals(methodCall.getMethodAsString())) {
            return;
        }
        Expression receiver = methodCall.getObjectExpression();
        if (!(receiver instanceof MethodCallExpression macro) || !"macroMethod".equals(macro.getMethodAsString())) {
            return;
        }
        // The macro result is still MacroStub.macroMethod(query) when the checker
        // runs. toList() on that call is a raw List, so List<String> x = GQ { ... }.toList()
        // is the same unchecked conversion as a dynamic query.
        ClassNode list = ClassHelper.LIST_TYPE.getPlainNodeReference();
        list.setGenericsTypes(null);
        list.setUsingGenerics(false);
        storeType(methodCall, list);
    }

    @Override
    public void afterVisitMethod(MethodNode node) {
        if (node.getCode() == null) {
            return;
        }
        node.getCode().visit(new CodeVisitorSupport() {
            private int dynamicDepth;

            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                boolean dynamic = Boolean.TRUE.equals(expression.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION));
                if (dynamic) {
                    dynamicDepth++;
                }
                super.visitClosureExpression(expression);
                if (dynamic) {
                    dynamicDepth--;
                }
            }

            @Override
            public void visitBinaryExpression(BinaryExpression expression) {
                // A subscript inside the lowered query is already dynamic. Coercing
                // it to List changes group-key subscripts.
                if (dynamicDepth == 0
                        && expression.getOperation().getType() == Types.LEFT_SQUARE_BRACKET
                        && rowOrUntyped(typeOf(expression.getLeftExpression()))) {
                    CastExpression cast = castX(ClassHelper.LIST_TYPE, expression.getLeftExpression());
                    cast.setCoerce(true);
                    cast.setSourcePosition(expression.getLeftExpression());
                    expression.setLeftExpression(cast);
                }
                super.visitBinaryExpression(expression);
            }
        });
    }

    private boolean insideDynamicQuery() {
        List<TypeCheckingContext.EnclosingClosure> stack = typeCheckingVisitor.getTypeCheckingContext().getEnclosingClosureStack();
        for (TypeCheckingContext.EnclosingClosure enclosing : stack) {
            if (Boolean.TRUE.equals(enclosing.getClosureExpression().getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION))) {
                return true;
            }
        }
        return false;
    }

    private static ClassNode typeOf(Expression expression) {
        ClassNode inferred = expression.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        return inferred != null ? inferred : expression.getType();
    }

    private static boolean rowOrUntyped(ClassNode type) {
        if (type == null || ClassHelper.isDynamicTyped(type)) {
            return true;
        }
        String name = type.getName();
        return "java.lang.Object".equals(name)
                || "org.apache.groovy.ginq.provider.collection.runtime.NamedRecord".equals(name)
                || "org.apache.groovy.ginq.provider.collection.runtime.NamedTuple".equals(name)
                || "org.apache.groovy.ginq.provider.collection.runtime.SourceRecord".equals(name)
                || "org.apache.groovy.ginq.provider.collection.runtime.GroupResult".equals(name);
    }
}
