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

    @Override
    public Statement createPropGetter(PropertyNode pNode) {
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
    public Statement createPropSetter(PropertyNode pNode) {
        return null;
    }

    @Override
    public boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno) {
        boolean success = isValidAttribute(xform, anno, "useSuper");
        return success;
    }

    @Override
    public boolean validateProperties(AbstractASTTransformation xform, BlockStatement body, ClassNode cNode, List<PropertyNode> props) {
        if (xform instanceof MapConstructorASTTransformation) {
            body.addStatement(ifS(equalsNullX(varX("args")), assignS(varX("args"), new MapExpression())));
            body.addStatement(stmt(callX(SELF_TYPE, "checkPropNames", args("this", "args"))));
        }
        return super.validateProperties(xform, body, cNode, props);
    }

    @Override
    public Statement createPropInit(AbstractASTTransformation xform, AnnotationNode anno, ClassNode cNode, PropertyNode pNode, Parameter namedArgsMap) {
        FieldNode fNode = pNode.getField();
        if (fNode.isFinal() && fNode.isStatic()) return null;
        if (fNode.isFinal() && fNode.getInitialExpression() != null) {
            return checkFinalArgNotOverridden(cNode, fNode);
        }
        return createConstructorStatement(xform, cNode, pNode, namedArgsMap);
    }

    private static Statement createGetterBodyDefault(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        return stmt(fieldExpr);
    }

    private Statement createGetterBodyArrayOrCloneable(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final Expression expression = cloneArrayOrCloneableExpr(fieldExpr, fNode.getType());
        return safeExpression(fieldExpr, expression);
    }

    private Statement createGetterBodyDate(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final Expression expression = cloneDateExpr(fieldExpr);
        return safeExpression(fieldExpr, expression);
    }

    protected Expression cloneCollectionExpr(Expression fieldExpr, ClassNode type) {
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

    private Expression createIfInstanceOfAsImmutableS(Expression expr, ClassNode type, Expression elseStatement) {
        return ternaryX(isInstanceOfX(expr, type), createAsImmutableX(expr, type), elseStatement);
    }

    protected Expression createAsImmutableX(final Expression expr, final ClassNode type) {
        return callX(DGM_TYPE, "asImmutable", castX(type, expr));
    }

    @Deprecated
    protected Statement createConstructorStatement(AbstractASTTransformation xform, ClassNode cNode, PropertyNode pNode, boolean namedArgs) {
        final List<String> knownImmutableClasses = ImmutablePropertyUtils.getKnownImmutableClasses(xform, cNode);
        final List<String> knownImmutables = ImmutablePropertyUtils.getKnownImmutables(xform, cNode);
        FieldNode fNode = pNode.getField();
        final ClassNode fType = fNode.getType();
        Statement statement;
        if (ImmutablePropertyUtils.isKnownImmutableType(fType, knownImmutableClasses) || isKnownImmutable(pNode.getName(), knownImmutables)) {
            statement = createConstructorStatementDefault(fNode, namedArgs);
        } else if (fType.isArray() || implementsCloneable(fType)) {
            statement = createConstructorStatementArrayOrCloneable(fNode, namedArgs);
        } else if (derivesFromDate(fType)) {
            statement = createConstructorStatementDate(fNode, namedArgs);
        } else if (isOrImplements(fType, COLLECTION_TYPE) || fType.isDerivedFrom(COLLECTION_TYPE) || isOrImplements(fType, MAP_TYPE) || fType.isDerivedFrom(MAP_TYPE)) {
            statement = createConstructorStatementCollection(fNode, namedArgs);
        } else if (fType.isResolved()) {
            xform.addError(ImmutablePropertyUtils.createErrorMessage(cNode.getName(), fNode.getName(), fType.getName(), "compiling"), fNode);
            statement = EmptyStatement.INSTANCE;
        } else {
            statement = createConstructorStatementGuarded(cNode, fNode, namedArgs, knownImmutables, knownImmutableClasses);
        }
        return statement;
    }

    protected Statement createConstructorStatement(AbstractASTTransformation xform, ClassNode cNode, PropertyNode pNode, Parameter namedArgsMap) {
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

    // only used by deprecated method
    private static Statement createConstructorStatementDefault(FieldNode fNode, boolean namedArgs) {
        final ClassNode fType = fNode.getType();
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression initExpr = fNode.getInitialValueExpression();
        Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            if (ClassHelper.isPrimitiveType(fType)) {
                assignInit = EmptyStatement.INSTANCE;
            } else {
                assignInit = assignNullS(fieldExpr);
            }
        } else {
            assignInit = assignS(fieldExpr, initExpr);
        }
        fNode.setInitialValueExpression(null);
        Expression param = getParam(fNode, namedArgs);
        Statement assignStmt = assignS(fieldExpr, castX(fType, param));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Statement createConstructorStatementDefault(FieldNode fNode, Parameter namedArgsMap, boolean shouldNullCheck) {
        final ClassNode fType = fNode.getType();
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression param = getParam(fNode, namedArgsMap != null);
        Statement assignStmt = assignS(fieldExpr, castX(fType, param));
        if (shouldNullCheck) {
            assignStmt = ifElseS(equalsNullX(param), NullCheckASTTransformation.makeThrowStmt(fNode.getName()), assignStmt);
        }
        Expression initExpr = fNode.getInitialValueExpression();
        Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
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

    private static Statement assignFieldWithDefault(Parameter map, FieldNode fNode, Statement assignStmt, Statement assignInit) {
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

    private static Statement assignWithDefault(boolean namedArgs, Statement assignInit, Expression param, Statement assignStmt) {
        if (!namedArgs) {
            return assignStmt;
        }
        return ifElseS(equalsNullX(param), assignInit, assignStmt);
    }

    // only used by deprecated method
    private static Statement createConstructorStatementGuarded(ClassNode cNode, FieldNode fNode, boolean namedArgs, List<String> knownImmutables, List<String> knownImmutableClasses) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, createCheckImmutable(fNode, initExpr, knownImmutables, knownImmutableClasses));
        }
        Expression param = getParam(fNode, namedArgs);
        Statement assignStmt = assignS(fieldExpr, createCheckImmutable(fNode, param, knownImmutables, knownImmutableClasses));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Statement createConstructorStatementGuarded(FieldNode fNode, Parameter namedArgsMap, List<String> knownImmutables, List<String> knownImmutableClasses, boolean shouldNullCheck) {
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
    private static Expression createCheckImmutable(FieldNode fNode, Expression value, List<String> knownImmutables, List<String> knownImmutableClasses) {
        Expression args = args(callThisX("getClass"), constX(fNode.getName()), value, list2args(knownImmutables), classList2args(knownImmutableClasses));
        return callX(SELF_TYPE, "checkImmutable", args);
    }

    // only used by deprecated method
    private Statement createConstructorStatementCollection(FieldNode fNode, boolean namedArgs) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        ClassNode fieldType = fieldExpr.getType();
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, cloneCollectionExpr(initExpr, fieldType));
        }
        Expression param = getParam(fNode, namedArgs);
        Statement assignStmt = ifElseS(
                isInstanceOfX(param, CLONEABLE_TYPE),
                assignS(fieldExpr, cloneCollectionExpr(cloneArrayOrCloneableExpr(param, fieldType), fieldType)),
                assignS(fieldExpr, cloneCollectionExpr(param, fieldType)));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private Statement createConstructorStatementCollection(FieldNode fNode, Parameter namedArgsMap, boolean shouldNullCheck) {
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

    // only used by deprecated method
    private static Statement createConstructorStatementArrayOrCloneable(FieldNode fNode, boolean namedArgs) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        final Expression initExpr = fNode.getInitialValueExpression();
        final ClassNode fieldType = fNode.getType();
        final Expression param = getParam(fNode, namedArgs);
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, cloneArrayOrCloneableExpr(initExpr, fieldType));
        }
        Statement assignStmt = assignS(fieldExpr, cloneArrayOrCloneableExpr(param, fieldType));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Statement createConstructorStatementArrayOrCloneable(FieldNode fNode, Parameter namedArgsMap, boolean shouldNullCheck) {
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

    private static Expression getParam(FieldNode fNode, boolean namedArgs) {
        return namedArgs ? findArg(fNode.getName()) : varX(fNode.getName(), fNode.getType());
    }

    // only used by deprecated method
    private static Statement createConstructorStatementDate(FieldNode fNode, boolean namedArgs) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignNullS(fieldExpr);
        } else {
            assignInit = assignS(fieldExpr, cloneDateExpr(initExpr));
        }
        final Expression param = getParam(fNode, namedArgs);
        Statement assignStmt = assignS(fieldExpr, cloneDateExpr(param));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Statement createConstructorStatementDate(FieldNode fNode, Parameter namedArgsMap, boolean shouldNullCheck) {
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

    private static boolean isKnownImmutable(String fieldName, List<String> knownImmutables) {
        return knownImmutables.contains(fieldName);
    }

    protected Statement checkFinalArgNotOverridden(ClassNode cNode, FieldNode fNode) {
        final String name = fNode.getName();
        Expression value = findArg(name);
        return ifS(
                notX(equalsNullX(value)),
                throwS(ctorX(READONLYEXCEPTION_TYPE,
                        args(constX(name), constX(cNode.getName()))
                )));
    }
}
