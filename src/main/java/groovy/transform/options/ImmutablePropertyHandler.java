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
package groovy.transform.options;

import groovy.lang.ReadOnlyPropertyException;
import groovy.transform.stc.POJO;
import org.apache.groovy.ast.tools.ImmutablePropertyUtils;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.ImmutableASTTransformation;
import org.codehaus.groovy.transform.MapConstructorASTTransformation;
import org.codehaus.groovy.transform.NullCheckASTTransformation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import static org.apache.groovy.ast.tools.ConstructorNodeUtils.checkPropNamesS;
import static org.apache.groovy.ast.tools.ImmutablePropertyUtils.cloneArrayOrCloneableExpr;
import static org.apache.groovy.ast.tools.ImmutablePropertyUtils.cloneDateExpr;
import static org.apache.groovy.ast.tools.ImmutablePropertyUtils.derivesFromDate;
import static org.apache.groovy.ast.tools.ImmutablePropertyUtils.implementsCloneable;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignNullS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classList2args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.findArg;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.ast.tools.GeneralUtils.list2args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.safeExpression;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

public class ImmutablePropertyHandler extends PropertyHandler {
    private static final ClassNode CLONEABLE_TYPE = make(Cloneable.class);
    private static final ClassNode COLLECTION_TYPE = makeWithoutCaching(Collection.class, false);
    private static final ClassNode DGM_TYPE = make(DefaultGroovyMethods.class);
    private static final ClassNode SELF_TYPE = make(ImmutableASTTransformation.class);
    private static final ClassNode MAP_TYPE = makeWithoutCaching(Map.class, false);
    private static final ClassNode SORTEDSET_CLASSNODE = make(SortedSet.class);
    private static final ClassNode SORTEDMAP_CLASSNODE = make(SortedMap.class);
    private static final ClassNode SET_CLASSNODE = make(Set.class);
    private static final ClassNode MAP_CLASSNODE = make(Map.class);
    private static final ClassNode READONLYEXCEPTION_TYPE = make(ReadOnlyPropertyException.class);
    private static final ClassNode POJO_TYPE = make(POJO.class);

    @Override
    public Statement createPropGetter(final PropertyNode pNode) {
        FieldNode fNode = pNode.getField();
        BlockStatement body = new BlockStatement();
        final ClassNode fieldType = fNode.getType();
        final Statement statement;
        if (fieldType.isArray() || implementsCloneable(fieldType)) {
            statement = createGetterBodyArrayOrCloneable(fNode);
        } else if (derivesFromDate(fieldType)) {
            statement = createGetterBodyDate(fNode);
        } else {
            statement = createGetterBodyDefault(fNode);
        }
        body.addStatement(statement);
        return body;
    }

    @Override
    public Statement createPropSetter(final PropertyNode pNode) {
        return null;
    }

    @Override
    public boolean validateAttributes(final AbstractASTTransformation xform, final AnnotationNode anno) {
        return isValidAttribute(xform, anno, "useSuper");
    }

    @Override
    public boolean validateProperties(final AbstractASTTransformation xform, final BlockStatement body, final ClassNode cNode, final List<PropertyNode> props) {
        if (xform instanceof MapConstructorASTTransformation) {
            VariableExpression namedArgs = varX("args");
            body.addStatement(ifS(equalsNullX(namedArgs), assignS(namedArgs, new MapExpression())));
            boolean pojo = !cNode.getAnnotations(POJO_TYPE).isEmpty();
            body.addStatement(checkPropNamesS(namedArgs, pojo, props));
        }
        return super.validateProperties(xform, body, cNode, props);
    }

    @Override
    public Statement createPropInit(final AbstractASTTransformation xform, final AnnotationNode anno, final ClassNode cNode, final PropertyNode pNode, final Parameter namedArgsMap) {
        FieldNode fNode = pNode.getField();
        if (fNode.isFinal() && fNode.isStatic()) return null;
        if (fNode.isFinal() && fNode.getInitialExpression() != null) {
            return checkFinalArgNotOverridden(cNode, fNode);
        }
        return createConstructorStatement(xform, cNode, pNode, namedArgsMap);
    }

    private static Statement createGetterBodyDefault(final FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        return stmt(fieldExpr);
    }

    private Statement createGetterBodyArrayOrCloneable(final FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final Expression expression = cloneArrayOrCloneableExpr(fieldExpr, fNode.getType());
        return safeExpression(fieldExpr, expression);
    }

    private Statement createGetterBodyDate(final FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final Expression expression = cloneDateExpr(fieldExpr);
        return safeExpression(fieldExpr, expression);
    }

    protected Expression cloneCollectionExpr(final Expression fieldExpr, final ClassNode type) {
        return castX(type, createIfInstanceOfAsImmutableS(fieldExpr, SORTEDSET_CLASSNODE,
                createIfInstanceOfAsImmutableS(fieldExpr, SORTEDMAP_CLASSNODE,
                        createIfInstanceOfAsImmutableS(fieldExpr, SET_CLASSNODE,
                                createIfInstanceOfAsImmutableS(fieldExpr, MAP_CLASSNODE,
                                        createIfInstanceOfAsImmutableS(fieldExpr, ClassHelper.LIST_TYPE,
                                                createAsImmutableX(fieldExpr, COLLECTION_TYPE))
                                )
                        )
                )
        ));
    }

    private Expression createIfInstanceOfAsImmutableS(final Expression expr, final ClassNode type, final Expression elseStatement) {
        return ternaryX(isInstanceOfX(expr, type), createAsImmutableX(expr, type), elseStatement);
    }

    protected Expression createAsImmutableX(final Expression expr, final ClassNode type) {
        return callX(DGM_TYPE, "asImmutable", castX(type, expr));
    }

    protected Statement createConstructorStatement(final AbstractASTTransformation xform, final ClassNode cNode, final PropertyNode pNode, final Parameter namedArgsMap) {
        final List<String> knownImmutableClasses = ImmutablePropertyUtils.getKnownImmutableClasses(xform, cNode);
        final List<String> knownImmutables = ImmutablePropertyUtils.getKnownImmutables(xform, cNode);
        FieldNode fNode = pNode.getField();
        final ClassNode fType = fNode.getType();
        Statement statement;
        boolean shouldNullCheck = NullCheckASTTransformation.hasIncludeGenerated(cNode);
        if (ImmutablePropertyUtils.isKnownImmutableType(fType, knownImmutableClasses) || isKnownImmutable(pNode.getName(), knownImmutables)) {
            statement = createConstructorStatementDefault(fNode, namedArgsMap, shouldNullCheck);
        } else if (fType.isArray() || implementsCloneable(fType)) {
            statement = createConstructorStatementArrayOrCloneable(fNode, namedArgsMap, shouldNullCheck);
        } else if (derivesFromDate(fType)) {
            statement = createConstructorStatementDate(fNode, namedArgsMap, shouldNullCheck);
        } else if (isOrImplements(fType, COLLECTION_TYPE) || fType.isDerivedFrom(COLLECTION_TYPE) || isOrImplements(fType, MAP_TYPE) || fType.isDerivedFrom(MAP_TYPE)) {
            statement = createConstructorStatementCollection(fNode, namedArgsMap, shouldNullCheck);
        } else if (fType.isResolved()) {
            xform.addError(ImmutablePropertyUtils.createErrorMessage(cNode.getName(), fNode.getName(), fType.getName(), "compiling"), fNode);
            statement = EmptyStatement.INSTANCE;
        } else {
            statement = createConstructorStatementGuarded(fNode, namedArgsMap, knownImmutables, knownImmutableClasses, shouldNullCheck);
        }
        return statement;
    }

    private static Statement createConstructorStatementDefault(final FieldNode fNode, final Parameter namedArgsMap, final boolean shouldNullCheck) {
        final ClassNode fType = fNode.getType();
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression param = getParam(fNode, namedArgsMap != null);
        Statement assignStmt = assignS(fieldExpr, castX(fType, param));
        if (shouldNullCheck) {
            assignStmt = ifElseS(equalsNullX(param), NullCheckASTTransformation.makeThrowStmt(fNode.getName()), assignStmt);
        }
        Expression initExpr = fNode.getInitialValueExpression();
        Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            if (ClassHelper.isPrimitiveType(fType)) {
                assignInit = EmptyStatement.INSTANCE;
            } else {
                assignInit = shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr);
            }
        } else {
            assignInit = assignS(fieldExpr, initExpr);
        }
        return assignFieldWithDefault(namedArgsMap, fNode, assignStmt, assignInit);
    }

    private static Statement assignFieldWithDefault(final Parameter map, final FieldNode fNode, final Statement assignStmt, final Statement assignInit) {
        if (map == null) {
            return assignStmt;
        }
        ArgumentListExpression nameArg = args(constX(fNode.getName()));
        MethodCallExpression var = callX(varX(map), "get", nameArg);
        var.setImplicitThis(false);
        MethodCallExpression containsKey = callX(varX(map), "containsKey", nameArg);
        containsKey.setImplicitThis(false);
        fNode.getDeclaringClass().getField(fNode.getName()).setInitialValueExpression(null); // to avoid default initialization
        return ifElseS(containsKey, assignStmt, assignInit);
    }

    private static Statement createConstructorStatementGuarded(final FieldNode fNode, final Parameter namedArgsMap, final List<String> knownImmutables, final List<String> knownImmutableClasses, final boolean shouldNullCheck) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression param = getParam(fNode, namedArgsMap != null);
        Statement assignStmt = assignS(fieldExpr, createCheckImmutable(fNode, param, knownImmutables, knownImmutableClasses));
        assignStmt = ifElseS(
                equalsNullX(param),
                shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr),
                assignStmt);
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, createCheckImmutable(fNode, initExpr, knownImmutables, knownImmutableClasses));
        }
        return assignFieldWithDefault(namedArgsMap, fNode, assignStmt, assignInit);
    }

    // check at runtime since classes might not be resolved
    private static Expression createCheckImmutable(final FieldNode fNode, final Expression value, final List<String> knownImmutables, final List<String> knownImmutableClasses) {
        Expression args = args(callThisX("getClass"), constX(fNode.getName()), value, list2args(knownImmutables), classList2args(knownImmutableClasses));
        return callX(SELF_TYPE, "checkImmutable", args);
    }

    private Statement createConstructorStatementCollection(final FieldNode fNode, final Parameter namedArgsMap, final boolean shouldNullCheck) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        ClassNode fieldType = fieldExpr.getType();
        Expression param = getParam(fNode, namedArgsMap != null);
        Statement assignStmt = ifElseS(
                isInstanceOfX(param, CLONEABLE_TYPE),
                assignS(fieldExpr, cloneCollectionExpr(cloneArrayOrCloneableExpr(param, fieldType), fieldType)),
                assignS(fieldExpr, cloneCollectionExpr(param, fieldType)));
        assignStmt = ifElseS(
                equalsNullX(param),
                shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr),
                assignStmt);
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, cloneCollectionExpr(initExpr, fieldType));
        }
        return assignFieldWithDefault(namedArgsMap, fNode, assignStmt, assignInit);
    }

    private static Statement createConstructorStatementArrayOrCloneable(final FieldNode fNode, final Parameter namedArgsMap, final boolean shouldNullCheck) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        final ClassNode fieldType = fNode.getType();
        final Expression param = getParam(fNode, namedArgsMap != null);
        Statement assignStmt = assignS(fieldExpr, cloneArrayOrCloneableExpr(param, fieldType));
        assignStmt = ifElseS(
                equalsNullX(param),
                shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr),
                assignStmt);
        final Statement assignInit;
        final Expression initExpr = fNode.getInitialValueExpression();
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, cloneArrayOrCloneableExpr(initExpr, fieldType));
        }
        return assignFieldWithDefault(namedArgsMap, fNode, assignStmt, assignInit);
    }

    private static Expression getParam(final FieldNode fNode, final boolean namedArgs) {
        return namedArgs ? findArg(fNode.getName()) : varX(fNode.getName(), fNode.getType());
    }

    private static Statement createConstructorStatementDate(final FieldNode fNode, final Parameter namedArgsMap, final boolean shouldNullCheck) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        final Expression param = getParam(fNode, namedArgsMap != null);
        Statement assignStmt = assignS(fieldExpr, cloneDateExpr(param));
        assignStmt = ifElseS(
                equalsNullX(param),
                shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr),
                assignStmt);
        final Statement assignInit;
        Expression initExpr = fNode.getInitialValueExpression();
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = shouldNullCheck ? NullCheckASTTransformation.makeThrowStmt(fNode.getName()) : assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, cloneDateExpr(initExpr));
        }
        return assignFieldWithDefault(namedArgsMap, fNode, assignStmt, assignInit);
    }

    private static boolean isKnownImmutable(final String fieldName, final List<String> knownImmutables) {
        return knownImmutables.contains(fieldName);
    }

    protected Statement checkFinalArgNotOverridden(final ClassNode cNode, final FieldNode fNode) {
        String name = fNode.getName();
        Expression value = findArg(name);
        return ifS(
                notX(equalsNullX(value)),
                throwS(
                        ctorX(READONLYEXCEPTION_TYPE, args(constX(name), constX(cNode.getName())))
                )
        );
    }
}
