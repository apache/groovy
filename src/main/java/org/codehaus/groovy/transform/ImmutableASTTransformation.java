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
package org.codehaus.groovy.transform;

import groovy.lang.MetaClass;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import groovy.transform.ImmutableBase;
import groovy.transform.KnownImmutable;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.ReflectionMethodInvoker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classList2args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.findArg;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.hasDeclaredMethod;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isInstanceOfX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOneX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isOrImplements;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isTrueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.list2args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.neX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.notX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.orX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.safeExpression;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @Immutable annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ImmutableASTTransformation extends AbstractASTTransformation {

    /*
      Currently leaving BigInteger and BigDecimal in list but see:
      http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6348370

      Also, Color is not final so while not normally used with child
      classes, it isn't strictly immutable. Use at your own risk.

      This list can by extended by providing "known immutable" classes
      via Immutable.knownImmutableClasses
     */
    private static Set<String> builtinImmutables = new HashSet(Arrays.asList(
            "java.lang.Class",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.String",
            "java.math.BigInteger",
            "java.math.BigDecimal",
            "java.awt.Color",
            "java.net.URI",
            "java.util.UUID",
            "java.time.DayOfWeek",
            "java.time.Duration",
            "java.time.Instant",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.Month",
            "java.time.MonthDay",
            "java.time.OffsetDateTime",
            "java.time.OffsetTime",
            "java.time.Period",
            "java.time.Year",
            "java.time.YearMonth",
            "java.time.ZonedDateTime",
            "java.time.ZoneOffset",
            "java.time.ZoneRegion",
            "java.time.chrono.ChronoLocalDate",
            "java.time.chrono.ChronoLocalDateTime",
            "java.time.chrono.Chronology",
            "java.time.chrono.ChronoPeriod",
            "java.time.chrono.ChronoZonedDateTime",
            "java.time.chrono.Era",
            "java.time.format.DecimalStyle",
            "java.time.format.FormatStyle",
            "java.time.format.ResolverStyle",
            "java.time.format.SignStyle",
            "java.time.format.TextStyle",
            "java.time.temporal.IsoFields",
            "java.time.temporal.JulianFields",
            "java.time.temporal.ValueRange",
            "java.time.temporal.WeekFields"
    ));
    private static final String KNOWN_IMMUTABLE_NAME = KnownImmutable.class.getName();
    private static final Class<? extends Annotation> MY_CLASS = ImmutableBase.class;
    private static final ClassNode IMMUTABLE_BASE_TYPE = makeWithoutCaching(MY_CLASS, false);
    public static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final String MEMBER_KNOWN_IMMUTABLE_CLASSES = "knownImmutableClasses";
    private static final String MEMBER_KNOWN_IMMUTABLES = "knownImmutables";
    private static final String MEMBER_ADD_COPY_WITH = "copyWith";
    private static final String COPY_WITH_METHOD = "copyWith";

    private static final ClassNode DATE_TYPE = make(Date.class);
    private static final ClassNode CLONEABLE_TYPE = make(Cloneable.class);
    private static final ClassNode COLLECTION_TYPE = makeWithoutCaching(Collection.class, false);
    private static final ClassNode READONLYEXCEPTION_TYPE = make(ReadOnlyPropertyException.class);
    private static final ClassNode DGM_TYPE = make(DefaultGroovyMethods.class);
    private static final ClassNode SELF_TYPE = make(ImmutableASTTransformation.class);
    private static final ClassNode HASHMAP_TYPE = makeWithoutCaching(HashMap.class, false);
    private static final ClassNode MAP_TYPE = makeWithoutCaching(Map.class, false);
    private static final ClassNode REFLECTION_INVOKER_TYPE = make(ReflectionMethodInvoker.class);
    private static final ClassNode SORTEDSET_CLASSNODE = make(SortedSet.class);
    private static final ClassNode SORTEDMAP_CLASSNODE = make(SortedMap.class);
    private static final ClassNode SET_CLASSNODE = make(Set.class);
    private static final ClassNode MAP_CLASSNODE = make(Map.class);
    public static final String IMMUTABLE_SAFE_FLAG = "Immutable.Safe";

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof ClassNode) {
            doMakeImmutable((ClassNode) parent, node);
        }
    }

    private void doMakeImmutable(ClassNode cNode, AnnotationNode node) {
        List<PropertyNode> newProperties = new ArrayList<PropertyNode>();
        final List<String> knownImmutables = getKnownImmutables(this, node);

        String cName = cNode.getName();
        if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
        if (!checkPropertyList(cNode, knownImmutables, "knownImmutables", node, "immutable class", false)) return;
        makeClassFinal(this, cNode);

        final List<PropertyNode> pList = getInstanceProperties(cNode);
        for (PropertyNode pNode : pList) {
            adjustPropertyForImmutability(pNode, newProperties);
        }
        for (PropertyNode pNode : newProperties) {
            cNode.getProperties().remove(pNode);
            addProperty(cNode, pNode);
        }
        final List<FieldNode> fList = cNode.getFields();
        for (FieldNode fNode : fList) {
            ensureNotPublic(this, cName, fNode);
        }
        if (hasAnnotation(cNode, TupleConstructorASTTransformation.MY_TYPE)) {
            AnnotationNode tupleCons = cNode.getAnnotations(TupleConstructorASTTransformation.MY_TYPE).get(0);
            if (unsupportedTupleAttribute(tupleCons, "excludes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includes")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeFields")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeProperties")) return;
            if (unsupportedTupleAttribute(tupleCons, "includeSuperFields")) return;
            if (unsupportedTupleAttribute(tupleCons, "callSuper")) return;
            if (unsupportedTupleAttribute(tupleCons, "useSetters")) return;
            if (unsupportedTupleAttribute(tupleCons, "force")) return;
        }
        if (!validateConstructors(cNode)) return;
        if (memberHasValue(node, MEMBER_ADD_COPY_WITH, true) && !pList.isEmpty() &&
                !hasDeclaredMethod(cNode, COPY_WITH_METHOD, 1)) {
            createCopyWith(cNode, pList);
        }
    }

    private boolean unsupportedTupleAttribute(AnnotationNode anno, String memberName) {
        if (getMemberValue(anno, memberName) != null) {
            String tname = TupleConstructorASTTransformation.MY_TYPE_NAME;
            addError("Error during " + MY_TYPE_NAME + " processing: Annotation attribute '" + memberName +
                    "' not supported for " + tname + " when used with " + MY_TYPE_NAME, anno);
            return true;
        }
        return false;
    }

    private static void doAddConstructor(final ClassNode cNode, final ConstructorNode constructorNode) {
        cNode.addConstructor(constructorNode);
        // GROOVY-5814: Immutable is not compatible with @CompileStatic
        Parameter argsParam = null;
        for (Parameter p : constructorNode.getParameters()) {
            if ("args".equals(p.getName())) {
                argsParam = p;
                break;
            }
        }
        if (argsParam != null) {
            final Parameter arg = argsParam;
            ClassCodeVisitorSupport variableExpressionFix = new ClassCodeVisitorSupport() {
                @Override
                protected SourceUnit getSourceUnit() {
                    return cNode.getModule().getContext();
                }

                @Override
                public void visitVariableExpression(final VariableExpression expression) {
                    super.visitVariableExpression(expression);
                    if ("args".equals(expression.getName())) {
                        expression.setAccessedVariable(arg);
                    }
                }
            };
            variableExpressionFix.visitConstructor(constructorNode);
        }
    }

    private static List<String> getKnownImmutableClasses(AbstractASTTransformation xform, AnnotationNode node) {
        final List<String> immutableClasses = new ArrayList<String>();

        if (node == null) return immutableClasses;
        final Expression expression = node.getMember(MEMBER_KNOWN_IMMUTABLE_CLASSES);
        if (expression == null) return immutableClasses;

        if (!(expression instanceof ListExpression)) {
            xform.addError("Use the Groovy list notation [el1, el2] to specify known immutable classes via \"" + MEMBER_KNOWN_IMMUTABLE_CLASSES + "\"", node);
            return immutableClasses;
        }

        final ListExpression listExpression = (ListExpression) expression;
        for (Expression listItemExpression : listExpression.getExpressions()) {
            if (listItemExpression instanceof ClassExpression) {
                immutableClasses.add(listItemExpression.getType().getName());
            }
        }

        return immutableClasses;
    }

    private static List<String> getKnownImmutables(AbstractASTTransformation xform, AnnotationNode node) {
        final List<String> immutables = new ArrayList<String>();

        if (node == null) return immutables;
        final Expression expression = node.getMember(MEMBER_KNOWN_IMMUTABLES);
        if (expression == null) return immutables;

        if (!(expression instanceof ListExpression)) {
            xform.addError("Use the Groovy list notation [el1, el2] to specify known immutable property names via \"" + MEMBER_KNOWN_IMMUTABLES + "\"", node);
            return immutables;
        }

        final ListExpression listExpression = (ListExpression) expression;
        for (Expression listItemExpression : listExpression.getExpressions()) {
            if (listItemExpression instanceof ConstantExpression) {
                immutables.add((String) ((ConstantExpression) listItemExpression).getValue());
            }
        }

        return immutables;
    }

    private static void makeClassFinal(AbstractASTTransformation xform, ClassNode cNode) {
        int modifiers = cNode.getModifiers();
        if ((modifiers & ACC_FINAL) == 0) {
            if ((modifiers & (ACC_ABSTRACT | ACC_SYNTHETIC)) == (ACC_ABSTRACT | ACC_SYNTHETIC)) {
                xform.addError("Error during " + MY_TYPE_NAME + " processing: annotation found on inappropriate class " + cNode.getName(), cNode);
                return;
            }
            cNode.setModifiers(modifiers | ACC_FINAL);
        }
    }

    static boolean isSpecialHashMapCase(List<PropertyNode> list) {
        return list.size() == 1 && list.get(0).getField().getType().equals(HASHMAP_TYPE);
    }

    @Deprecated
    static List<PropertyNode> getProperties(ClassNode cNode, boolean includeSuperProperties, boolean allProperties) {
        List<PropertyNode> list = getInstanceProperties(cNode);
        if (includeSuperProperties) {
            ClassNode next = cNode.getSuperClass();
            while (next != null) {
                List<PropertyNode> tail = list;
                list = getInstanceProperties(next);
                list.addAll(tail);
                next = next.getSuperClass();
            }
        }
        return list;
    }

    @Deprecated
    static void createConstructorOrdered(ClassNode cNode, List<PropertyNode> list) {
        final MapExpression argMap = new MapExpression();
        final Parameter[] orderedParams = new Parameter[list.size()];
        int index = 0;
        for (PropertyNode pNode : list) {
            Parameter param = new Parameter(pNode.getField().getType(), pNode.getField().getName());
            orderedParams[index++] = param;
            argMap.addMapEntryExpression(constX(pNode.getName()), varX(pNode.getName()));
        }
        final BlockStatement orderedBody = new BlockStatement();
        orderedBody.addStatement(stmt(ctorX(ClassNode.THIS, args(castX(HASHMAP_TYPE, argMap)))));
        doAddConstructor(cNode, new ConstructorNode(ACC_PUBLIC, orderedParams, ClassNode.EMPTY_ARRAY, orderedBody));
    }

    private static Statement createGetterBodyDefault(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        return stmt(fieldExpr);
    }

    private static Expression cloneCollectionExpr(Expression fieldExpr, ClassNode type) {
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

    private static Expression createIfInstanceOfAsImmutableS(Expression expr, ClassNode type, Expression elseStatement) {
        return ternaryX(isInstanceOfX(expr, type), createAsImmutableX(expr, type), elseStatement);
    }

    private static Expression createAsImmutableX(final Expression expr, final ClassNode type) {
        return callX(DGM_TYPE, "asImmutable", castX(type, expr));
    }

    private static Expression cloneArrayOrCloneableExpr(Expression fieldExpr, ClassNode type) {
        Expression smce = callX(
                REFLECTION_INVOKER_TYPE,
                "invoke",
                args(
                        fieldExpr,
                        constX("clone"),
                        new ArrayExpression(ClassHelper.OBJECT_TYPE.makeArray(), Collections.<Expression>emptyList())
                )
        );
        return castX(type, smce);
    }

    static void createConstructorMapCommon(ClassNode cNode, BlockStatement body) {
        final List<FieldNode> fList = cNode.getFields();
        for (FieldNode fNode : fList) {
            if (fNode.isPublic()) continue; // public fields will be rejected elsewhere
            if (cNode.getProperty(fNode.getName()) != null) continue; // a property
            if (fNode.isFinal() && fNode.isStatic()) continue;
            if (fNode.getName().contains("$") || fNode.isSynthetic()) continue; // internal field
            if (fNode.isFinal() && fNode.getInitialExpression() != null)
                body.addStatement(checkFinalArgNotOverridden(cNode, fNode));
            body.addStatement(createConstructorStatementDefault(fNode, true));
        }
        doAddConstructor(cNode, new ConstructorNode(ACC_PUBLIC, params(new Parameter(HASHMAP_TYPE, "args")), ClassNode.EMPTY_ARRAY, body));
    }

    private static Statement checkFinalArgNotOverridden(ClassNode cNode, FieldNode fNode) {
        final String name = fNode.getName();
        Expression value = findArg(name);
        return ifS(
                notX(equalsNullX(value)),
                throwS(ctorX(READONLYEXCEPTION_TYPE,
                        args(constX(name), constX(cNode.getName()))
                )));
    }

    static Statement createConstructorStatementMapSpecial(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final ClassNode fieldType = fieldExpr.getType();
        final Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneCollectionExpr(initExpr, fieldType));
        }
        Expression namedArgs = findArg(fNode.getName());
        Expression baseArgs = varX("args");
        Statement assignStmt = ifElseS(
                equalsNullX(namedArgs),
                ifElseS(
                        isTrueX(callX(baseArgs, "containsKey", constX(fNode.getName()))),
                        assignS(fieldExpr, namedArgs),
                        assignS(fieldExpr, cloneCollectionExpr(baseArgs, fieldType))),
                ifElseS(
                        isOneX(callX(baseArgs, "size")),
                        assignS(fieldExpr, cloneCollectionExpr(namedArgs, fieldType)),
                        assignS(fieldExpr, cloneCollectionExpr(baseArgs, fieldType)))
        );
        return ifElseS(equalsNullX(baseArgs), assignInit, assignStmt);
    }

    private static void ensureNotPublic(AbstractASTTransformation xform, String cNode, FieldNode fNode) {
        String fName = fNode.getName();
        // TODO: do we need to lock down things like: $ownClass
        if (fNode.isPublic() && !fName.contains("$") && !(fNode.isStatic() && fNode.isFinal())) {
            xform.addError("Public field '" + fName + "' not allowed for " + MY_TYPE_NAME + " class '" + cNode + "'.", fNode);
        }
    }

    private static void addProperty(ClassNode cNode, PropertyNode pNode) {
        final FieldNode fn = pNode.getField();
        cNode.getFields().remove(fn);
        cNode.addProperty(pNode.getName(), pNode.getModifiers() | ACC_FINAL, pNode.getType(),
                pNode.getInitialExpression(), pNode.getGetterBlock(), pNode.getSetterBlock());
        final FieldNode newfn = cNode.getField(fn.getName());
        cNode.getFields().remove(newfn);
        cNode.addField(fn);
    }

    private boolean validateConstructors(ClassNode cNode) {
        List<ConstructorNode> declaredConstructors = cNode.getDeclaredConstructors();
        for (ConstructorNode constructorNode : declaredConstructors) {
            // allow constructors added by other transforms if flagged as safe
            Object nodeMetaData = constructorNode.getNodeMetaData(IMMUTABLE_SAFE_FLAG);
            if (nodeMetaData != null && ((Boolean) nodeMetaData)) {
                continue;
            }
            addError("Explicit constructors not allowed for " + MY_TYPE_NAME + " class: " + cNode.getNameWithoutPackage(), constructorNode);
            return false;
        }
        return true;
    }

    static boolean makeImmutable(ClassNode cNode) {
        List<AnnotationNode> annotations = cNode.getAnnotations(IMMUTABLE_BASE_TYPE);
        AnnotationNode annoImmutable = annotations.isEmpty() ? null : annotations.get(0);
        return annoImmutable != null;
    }

    static Statement createConstructorStatement(AbstractASTTransformation xform, ClassNode cNode, PropertyNode pNode, boolean namedArgs) {
        List<AnnotationNode> annotations = cNode.getAnnotations(IMMUTABLE_BASE_TYPE);
        AnnotationNode annoImmutable = annotations.isEmpty() ? null : annotations.get(0);
        final List<String> knownImmutableClasses = getKnownImmutableClasses(xform, annoImmutable);
        final List<String> knownImmutables = getKnownImmutables(xform, annoImmutable);
        FieldNode fNode = pNode.getField();
        final ClassNode fieldType = fNode.getType();
        Statement statement;
        if (isKnownImmutableType(fieldType, knownImmutableClasses) || isKnownImmutable(pNode.getName(), knownImmutables)) {
            statement = createConstructorStatementDefault(fNode, namedArgs);
        } else if (fieldType.isArray() || isOrImplements(fieldType, CLONEABLE_TYPE)) {
            statement = createConstructorStatementArrayOrCloneable(fNode, namedArgs);
        } else if (fieldType.isDerivedFrom(DATE_TYPE)) {
            statement = createConstructorStatementDate(fNode, namedArgs);
        } else if (isOrImplements(fieldType, COLLECTION_TYPE) || fieldType.isDerivedFrom(COLLECTION_TYPE) || isOrImplements(fieldType, MAP_TYPE) || fieldType.isDerivedFrom(MAP_TYPE)) {
            statement = createConstructorStatementCollection(fNode, namedArgs);
        } else if (fieldType.isResolved()) {
            xform.addError(createErrorMessage(cNode.getName(), fNode.getName(), fieldType.getName(), "compiling"), fNode);
            statement = EmptyStatement.INSTANCE;
        } else {
            statement = createConstructorStatementGuarded(cNode, fNode, namedArgs, knownImmutables, knownImmutableClasses);
        }
        return statement;
    }

    private static Statement createConstructorStatementDefault(FieldNode fNode, boolean namedArgs) {
        final ClassNode fType = fNode.getType();
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression initExpr = fNode.getInitialValueExpression();
        Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            if (ClassHelper.isPrimitiveType(fType)) {
                assignInit = EmptyStatement.INSTANCE;
            } else {
                assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
            }
        } else {
            assignInit = assignS(fieldExpr, initExpr);
        }
        fNode.setInitialValueExpression(null);
        Expression param = getParam(fNode, namedArgs);
        Statement assignStmt = assignS(fieldExpr, castX(fType, param));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Statement assignWithDefault(boolean namedArgs, Statement assignInit, Expression param, Statement assignStmt) {
        if (!namedArgs) {
            return assignStmt;
        }
        return ifElseS(equalsNullX(param), assignInit, assignStmt);
    }

    private static Statement createConstructorStatementGuarded(ClassNode cNode, FieldNode fNode, boolean namedArgs, List<String> knownImmutables, List<String> knownImmutableClasses) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, checkUnresolved(fNode, initExpr, knownImmutables, knownImmutableClasses));
        }
        Expression param = getParam(fNode, namedArgs);
        Statement assignStmt = assignS(fieldExpr, checkUnresolved(fNode, param, knownImmutables, knownImmutableClasses));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Expression checkUnresolved(FieldNode fNode, Expression value, List<String> knownImmutables, List<String> knownImmutableClasses) {
        Expression args = args(callThisX("getClass"), constX(fNode.getName()), value, list2args(knownImmutables), classList2args(knownImmutableClasses));
        return callX(SELF_TYPE, "checkImmutable", args);
    }

    private static Statement createConstructorStatementCollection(FieldNode fNode, boolean namedArgs) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        ClassNode fieldType = fieldExpr.getType();
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
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

    private static Statement createConstructorStatementArrayOrCloneable(FieldNode fNode, boolean namedArgs) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        final Expression initExpr = fNode.getInitialValueExpression();
        final ClassNode fieldType = fNode.getType();
        final Expression param = getParam(fNode, namedArgs);
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneArrayOrCloneableExpr(initExpr, fieldType));
        }
        Statement assignStmt = assignS(fieldExpr, cloneArrayOrCloneableExpr(param, fieldType));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Expression getParam(FieldNode fNode, boolean namedArgs) {
        return namedArgs ? findArg(fNode.getName()) : varX(fNode.getName(), fNode.getType());
    }

    private static Statement createConstructorStatementDate(FieldNode fNode, boolean namedArgs) {
        final Expression fieldExpr = propX(varX("this"), fNode.getName());
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression) initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneDateExpr(initExpr));
        }
        final Expression param = getParam(fNode, namedArgs);
        Statement assignStmt = assignS(fieldExpr, cloneDateExpr(param));
        return assignWithDefault(namedArgs, assignInit, param, assignStmt);
    }

    private static Expression cloneDateExpr(Expression origDate) {
        return ctorX(DATE_TYPE, callX(origDate, "getTime"));
    }

    private static void adjustPropertyForImmutability(PropertyNode pNode, List<PropertyNode> newNodes) {
        final FieldNode fNode = pNode.getField();
        fNode.setModifiers((pNode.getModifiers() & (~ACC_PUBLIC)) | ACC_FINAL | ACC_PRIVATE);
        adjustPropertyNode(pNode, createGetterBody(fNode));
        newNodes.add(pNode);
    }

    private static void adjustPropertyNode(PropertyNode pNode, Statement getterBody) {
        pNode.setSetterBlock(null);
        pNode.setGetterBlock(getterBody);
    }

    private static Statement createGetterBody(FieldNode fNode) {
        BlockStatement body = new BlockStatement();
        final ClassNode fieldType = fNode.getType();
        final Statement statement;
        if (fieldType.isArray() || isOrImplements(fieldType, CLONEABLE_TYPE)) {
            statement = createGetterBodyArrayOrCloneable(fNode);
        } else if (fieldType.isDerivedFrom(DATE_TYPE)) {
            statement = createGetterBodyDate(fNode);
        } else {
            statement = createGetterBodyDefault(fNode);
        }
        body.addStatement(statement);
        return body;
    }

    private static String createErrorMessage(String className, String fieldName, String typeName, String mode) {
        return "Unsupported type (" + prettyTypeName(typeName) + ") found for field '" + fieldName + "' while " + mode + " immutable class " + className + ".\n" +
                "Immutable classes only support properties with effectively immutable types including:\n" +
                "- Strings, primitive types, wrapper types, Class, BigInteger and BigDecimal, enums\n" +
                "- classes annotated with @KnownImmutable and known immutables (java.awt.Color, java.net.URI)\n" +
                "- Cloneable classes, collections, maps and arrays, and other classes with special handling\n" +
                "  (java.util.Date and various java.time.* classes and interfaces)\n" +
                "Other restrictions apply, please see the groovydoc for " + MY_TYPE_NAME + " for further details";
    }

    private static String prettyTypeName(String name) {
        return name.equals("java.lang.Object") ? name + " or def" : name;
    }

    private static Statement createGetterBodyArrayOrCloneable(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final Expression expression = cloneArrayOrCloneableExpr(fieldExpr, fNode.getType());
        return safeExpression(fieldExpr, expression);
    }

    private static Statement createGetterBodyDate(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final Expression expression = cloneDateExpr(fieldExpr);
        return safeExpression(fieldExpr, expression);
    }

    private static Statement createCheckForProperty(final PropertyNode pNode) {
        return block(
                new VariableScope(),
                ifElseS(
                        callX(
                                varX("map", HASHMAP_TYPE),
                                "containsKey",
                                args(constX(pNode.getName()))
                        ),
                        block(
                                new VariableScope(),
                                declS(
                                        varX("newValue", ClassHelper.OBJECT_TYPE),
                                        callX(
                                                varX("map", HASHMAP_TYPE),
                                                "get",
                                                args(constX(pNode.getName()))
                                        )
                                ),
                                declS(
                                        varX("oldValue", ClassHelper.OBJECT_TYPE),
                                        callThisX(getGetterName(pNode))
                                ),
                                ifS(
                                        neX(
                                                varX("newValue", ClassHelper.OBJECT_TYPE),
                                                varX("oldValue", ClassHelper.OBJECT_TYPE)
                                        ),
                                        block(
                                                new VariableScope(),
                                                assignS(
                                                        varX("oldValue", ClassHelper.OBJECT_TYPE),
                                                        varX("newValue", ClassHelper.OBJECT_TYPE)),
                                                assignS(
                                                        varX("dirty", ClassHelper.boolean_TYPE),
                                                        ConstantExpression.TRUE)
                                        )
                                ),
                                stmt(callX(
                                        varX("construct", HASHMAP_TYPE),
                                        "put",
                                        args(
                                                constX(pNode.getName()),
                                                varX("oldValue", ClassHelper.OBJECT_TYPE)
                                        )
                                ))
                        ),
                        block(
                                new VariableScope(),
                                stmt(callX(
                                        varX("construct", HASHMAP_TYPE),
                                        "put",
                                        args(
                                                constX(pNode.getName()),
                                                callThisX(getGetterName(pNode))
                                        )
                                ))
                        )
                )
        );
    }

    private static void createCopyWith(final ClassNode cNode, final List<PropertyNode> pList) {
        BlockStatement body = new BlockStatement();
        body.addStatement(ifS(
                orX(
                        equalsNullX(varX("map", ClassHelper.MAP_TYPE)),
                        eqX(callX(varX("map", HASHMAP_TYPE), "size"), constX(0))
                ),
                returnS(varX("this", cNode))
        ));
        body.addStatement(declS(varX("dirty", ClassHelper.boolean_TYPE), ConstantExpression.PRIM_FALSE));
        body.addStatement(declS(varX("construct", HASHMAP_TYPE), ctorX(HASHMAP_TYPE)));

        // Check for each property
        for (final PropertyNode pNode : pList) {
            body.addStatement(createCheckForProperty(pNode));
        }

        body.addStatement(returnS(ternaryX(
                isTrueX(varX("dirty", ClassHelper.boolean_TYPE)),
                ctorX(cNode, args(varX("construct", HASHMAP_TYPE))),
                varX("this", cNode)
        )));

        final ClassNode clonedNode = cNode.getPlainNodeReference();

        cNode.addMethod(COPY_WITH_METHOD,
                ACC_PUBLIC | ACC_FINAL,
                clonedNode,
                params(new Parameter(new ClassNode(Map.class), "map")),
                null,
                body);
    }

    /**
     * This method exists to be binary compatible with 1.7 - 1.8.6 compiled code.
     */
    @SuppressWarnings("Unchecked")
    public static Object checkImmutable(String className, String fieldName, Object field) {
        if (field == null || field instanceof Enum || isBuiltinImmutable(field.getClass().getName())) return field;
        if (field instanceof Collection) return DefaultGroovyMethods.asImmutable((Collection) field);
        if (getAnnotationByName(field, "groovy.transform.Immutable") != null) return field;

        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(className, fieldName, typeName, "constructing"));
    }

    private static Annotation getAnnotationByName(Object field, String name) {
        // find longhand since the annotation from earlier versions is now a meta annotation
        for (Annotation an : field.getClass().getAnnotations()) {
            if (an.getClass().getName().equals(name)) {
                return an;
            }
        }
        return null;
    }

    /**
     * For compatibility with pre 2.5 compiled classes
     */
    @SuppressWarnings("Unchecked")
    public static Object checkImmutable(Class<?> clazz, String fieldName, Object field) {
        if (field == null || field instanceof Enum || builtinOrMarkedImmutableClass(field.getClass())) {
            return field;
        }

        boolean isImmutable = false;
        for (Annotation an : field.getClass().getAnnotations()) {
            if (an.getClass().getName().startsWith("groovy.transform.Immutable")) {
                isImmutable = true;
                break;
            }
        }
        if (isImmutable) return field;

        if (field instanceof Collection) {
            Field declaredField;
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                Class<?> fieldType = declaredField.getType();
                if (Collection.class.isAssignableFrom(fieldType)) {
                    return DefaultGroovyMethods.asImmutable((Collection) field);
                }
                // potentially allow Collection coercion for a constructor
                if (builtinOrMarkedImmutableClass(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException ignore) {
                // ignore
            }
        }
        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(clazz.getName(), fieldName, typeName, "constructing"));
    }

    @SuppressWarnings("Unchecked")
    public static Object checkImmutable(Class<?> clazz, String fieldName, Object field, List<String> knownImmutableFieldNames, List<Class> knownImmutableClasses) {
        if (field == null || field instanceof Enum || builtinOrMarkedImmutableClass(field.getClass()) || knownImmutableFieldNames.contains(fieldName) || knownImmutableClasses.contains(field.getClass())) {
            return field;
        }

        boolean isImmutable = false;
        for (Annotation an : field.getClass().getAnnotations()) {
            if (an.getClass().getName().startsWith("groovy.transform.Immutable")) {
                isImmutable = true;
                break;
            }
        }
        if (isImmutable) return field;

        if (field instanceof Collection) {
            Field declaredField;
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                Class<?> fieldType = declaredField.getType();
                if (Collection.class.isAssignableFrom(fieldType)) {
                    return DefaultGroovyMethods.asImmutable((Collection) field);
                }
                // potentially allow Collection coercion for a constructor
                if (builtinOrMarkedImmutableClass(fieldType) || knownImmutableClasses.contains(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException ignore) {
                // ignore
            }
        }
        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(clazz.getName(), fieldName, typeName, "constructing"));
    }

    private static boolean isKnownImmutableType(ClassNode fieldType, List<String> knownImmutableClasses) {
        if (builtinOrDeemedType(fieldType, knownImmutableClasses))
            return true;
        if (!fieldType.isResolved())
            return false;
        if ("java.util.Optional".equals(fieldType.getName()) && fieldType.getGenericsTypes() != null && fieldType.getGenericsTypes().length == 1) {
            GenericsType optionalType = fieldType.getGenericsTypes()[0];
            if (optionalType.isResolved() && !optionalType.isPlaceholder() && !optionalType.isWildcard()) {
                ClassNode valueType = optionalType.getType();
                if (builtinOrDeemedType(valueType, knownImmutableClasses)) return true;
                if (valueType.isEnum()) return true;
            }
        }
        return fieldType.isEnum() ||
                ClassHelper.isPrimitiveType(fieldType) ||
                hasImmutableAnnotation(fieldType);
    }

    private static boolean builtinOrDeemedType(ClassNode fieldType, List<String> knownImmutableClasses) {
        return isBuiltinImmutable(fieldType.getName()) || knownImmutableClasses.contains(fieldType.getName()) || hasImmutableAnnotation(fieldType);
    }

    private static boolean hasImmutableAnnotation(ClassNode type) {
        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode next : annotations) {
            String name = next.getClassNode().getName();
            if (matchingMarkerName(name)) return true;
        }
        return false;
    }

    private static boolean hasImmutableAnnotation(Class clazz) {
        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation next : annotations) {
            String name = next.annotationType().getName();
            if (matchingMarkerName(name)) return true;
        }
        return false;
    }

    private static boolean matchingMarkerName(String name) {
        return name.equals("groovy.transform.Immutable") || name.equals(KNOWN_IMMUTABLE_NAME);
    }

    private static boolean isKnownImmutable(String fieldName, List<String> knownImmutables) {
        return knownImmutables.contains(fieldName);
    }

    private static boolean isBuiltinImmutable(String typeName) {
        return builtinImmutables.contains(typeName);
    }

    private static boolean builtinOrMarkedImmutableClass(Class<?> clazz) {
        return isBuiltinImmutable(clazz.getName()) || hasImmutableAnnotation(clazz);
    }

    public static void checkPropNames(Object instance, Map<String, Object> args) {
        final MetaClass metaClass = InvokerHelper.getMetaClass(instance);
        for (String k : args.keySet()) {
            if (metaClass.hasProperty(instance, k) == null)
                throw new MissingPropertyException(k, instance.getClass());
        }
    }
}
