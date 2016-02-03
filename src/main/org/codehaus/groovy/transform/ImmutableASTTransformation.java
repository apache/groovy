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
import groovy.transform.Immutable;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.SortedMap;
import java.util.Collections;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;
import static org.codehaus.groovy.transform.EqualsAndHashCodeASTTransformation.createEquals;
import static org.codehaus.groovy.transform.EqualsAndHashCodeASTTransformation.createHashCode;
import static org.codehaus.groovy.transform.ToStringASTTransformation.createToString;

/**
 * Handles generation of code for the @Immutable annotation.
 *
 * @author Paul King
 * @author Andre Steingress
 * @author Tim Yates
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
    private static List<String> immutableList = Arrays.asList(
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
            "java.util.UUID"
    );
    private static final Class MY_CLASS = groovy.transform.Immutable.class;
    public static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    static final String MEMBER_KNOWN_IMMUTABLE_CLASSES = "knownImmutableClasses";
    static final String MEMBER_KNOWN_IMMUTABLES = "knownImmutables";
    static final String MEMBER_ADD_COPY_WITH = "copyWith";
    static final String COPY_WITH_METHOD = "copyWith";

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
        // temporarily have weaker check which allows for old Deprecated Annotation
//        if (!MY_TYPE.equals(node.getClassNode())) return;
        if (!node.getClassNode().getName().endsWith(".Immutable")) return;
        List<PropertyNode> newProperties = new ArrayList<PropertyNode>();

        if (parent instanceof ClassNode) {
            final List<String> knownImmutableClasses = getKnownImmutableClasses(node);
            final List<String> knownImmutables = getKnownImmutables(node);

            ClassNode cNode = (ClassNode) parent;
            String cName = cNode.getName();
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            makeClassFinal(cNode);

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
                ensureNotPublic(cName, fNode);
            }
            createConstructors(cNode, knownImmutableClasses, knownImmutables);
            if (!hasAnnotation(cNode, EqualsAndHashCodeASTTransformation.MY_TYPE)) {
                createHashCode(cNode, true, false, false, null, null);
                createEquals(cNode, false, false, false, null, null);
            }
            if (!hasAnnotation(cNode, ToStringASTTransformation.MY_TYPE)) {
                createToString(cNode, false, false, null, null, false, true);
            }
            if( memberHasValue(node, MEMBER_ADD_COPY_WITH, true) &&
                    !pList.isEmpty() &&
                !hasDeclaredMethod(cNode, COPY_WITH_METHOD, 1) ) {
                createCopyWith( cNode, pList ) ;
            }
        }
    }

    private void doAddConstructor(final ClassNode cNode, final ConstructorNode constructorNode) {
        cNode.addConstructor(constructorNode);
        // GROOVY-5814: Immutable is not compatible with @CompileStatic
        Parameter argsParam = null;
        for (Parameter p : constructorNode.getParameters()) {
            if ("args".equals(p.getName())) {
                argsParam = p;
                break;
            }
        }
        if (argsParam!=null) {
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

    private List<String> getKnownImmutableClasses(AnnotationNode node) {
        final ArrayList<String> immutableClasses = new ArrayList<String>();

        final Expression expression = node.getMember(MEMBER_KNOWN_IMMUTABLE_CLASSES);
        if (expression == null) return immutableClasses;

        if (!(expression instanceof ListExpression)) {
            addError("Use the Groovy list notation [el1, el2] to specify known immutable classes via \"" + MEMBER_KNOWN_IMMUTABLE_CLASSES + "\"", node);
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

    private List<String> getKnownImmutables(AnnotationNode node) {
        final ArrayList<String> immutables = new ArrayList<String>();

        final Expression expression = node.getMember(MEMBER_KNOWN_IMMUTABLES);
        if (expression == null) return immutables;

        if (!(expression instanceof ListExpression)) {
            addError("Use the Groovy list notation [el1, el2] to specify known immutable property names via \"" + MEMBER_KNOWN_IMMUTABLES + "\"", node);
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

    private void makeClassFinal(ClassNode cNode) {
        if ((cNode.getModifiers() & ACC_FINAL) == 0) {
            cNode.setModifiers(cNode.getModifiers() | ACC_FINAL);
        }
    }

    private void createConstructors(ClassNode cNode, List<String> knownImmutableClasses, List<String> knownImmutables) {
        if (!validateConstructors(cNode)) return;

        List<PropertyNode> list = getInstanceProperties(cNode);
        boolean specialHashMapCase = list.size() == 1 && list.get(0).getField().getType().equals(HASHMAP_TYPE);
        if (specialHashMapCase) {
            createConstructorMapSpecial(cNode, list);
        } else {
            createConstructorMap(cNode, list, knownImmutableClasses, knownImmutables);
            createConstructorOrdered(cNode, list);
        }
    }

    private void createConstructorOrdered(ClassNode cNode, List<PropertyNode> list) {
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

    private Statement createGetterBodyDefault(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        return stmt(fieldExpr);
    }

    private Expression cloneCollectionExpr(Expression fieldExpr, ClassNode type) {
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

    private Expression createAsImmutableX(final Expression expr, final ClassNode type) {
        return callX(DGM_TYPE, "asImmutable", castX(type, expr));
    }

    private Expression cloneArrayOrCloneableExpr(Expression fieldExpr, ClassNode type) {
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

    private void createConstructorMapSpecial(ClassNode cNode, List<PropertyNode> list) {
        final BlockStatement body = new BlockStatement();
        body.addStatement(createConstructorStatementMapSpecial(list.get(0).getField()));
        createConstructorMapCommon(cNode, body);
    }

    private void createConstructorMap(ClassNode cNode, List<PropertyNode> list, List<String> knownImmutableClasses, List<String> knownImmutables) {
        final BlockStatement body = new BlockStatement();
        body.addStatement(ifS(equalsNullX(varX("args")), assignS(varX("args"), new MapExpression())));
        for (PropertyNode pNode : list) {
            body.addStatement(createConstructorStatement(cNode, pNode, knownImmutableClasses, knownImmutables));
        }
        // check for missing properties
        body.addStatement(stmt(callX(SELF_TYPE, "checkPropNames", args("this", "args"))));
        createConstructorMapCommon(cNode, body);
        if (!list.isEmpty()) {
            createNoArgConstructor(cNode);
        }
    }

    private void createNoArgConstructor(ClassNode cNode) {
        Statement body = stmt(ctorX(ClassNode.THIS, args(new MapExpression())));
        doAddConstructor(cNode, new ConstructorNode(ACC_PUBLIC, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, body));
    }

    private void createConstructorMapCommon(ClassNode cNode, BlockStatement body) {
        final List<FieldNode> fList = cNode.getFields();
        for (FieldNode fNode : fList) {
            if (fNode.isPublic()) continue; // public fields will be rejected elsewhere
            if (cNode.getProperty(fNode.getName()) != null) continue; // a property
            if (fNode.isFinal() && fNode.isStatic()) continue;
            if (fNode.getName().contains("$") || fNode.isSynthetic()) continue; // internal field
            if (fNode.isFinal() && fNode.getInitialExpression() != null)
                body.addStatement(checkFinalArgNotOverridden(cNode, fNode));
            body.addStatement(createConstructorStatementDefault(fNode));
        }
        doAddConstructor(cNode, new ConstructorNode(ACC_PUBLIC, params(new Parameter(HASHMAP_TYPE, "args")), ClassNode.EMPTY_ARRAY, body));
    }

    private Statement checkFinalArgNotOverridden(ClassNode cNode, FieldNode fNode) {
        final String name = fNode.getName();
        Expression value = findArg(name);
        return ifS(
                notX(equalsNullX(value)),
                throwS(ctorX(READONLYEXCEPTION_TYPE,
                        args(constX(name), constX(cNode.getName()))
                )));
    }

    private Statement createConstructorStatementMapSpecial(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        final ClassNode fieldType = fieldExpr.getType();
        final Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneCollectionExpr(initExpr, fieldType));
        }
        Expression namedArgs = findArg(fNode.getName());
        Expression baseArgs = varX("args");
        return ifElseS(
                equalsNullX(baseArgs),
                assignInit,
                ifElseS(
                        equalsNullX(namedArgs),
                        ifElseS(
                                isTrueX(callX(baseArgs, "containsKey", constX(fNode.getName()))),
                                assignS(fieldExpr, namedArgs),
                                assignS(fieldExpr, cloneCollectionExpr(baseArgs, fieldType))),
                        ifElseS(
                                isOneX(callX(baseArgs, "size")),
                                assignS(fieldExpr, cloneCollectionExpr(namedArgs, fieldType)),
                                assignS(fieldExpr, cloneCollectionExpr(baseArgs, fieldType)))
                )
        );
    }

    private void ensureNotPublic(String cNode, FieldNode fNode) {
        String fName = fNode.getName();
        // TODO: do we need to lock down things like: $ownClass
        if (fNode.isPublic() && !fName.contains("$") && !(fNode.isStatic() && fNode.isFinal())) {
            addError("Public field '" + fName + "' not allowed for " + MY_TYPE_NAME + " class '" + cNode + "'.", fNode);
        }
    }

    private void addProperty(ClassNode cNode, PropertyNode pNode) {
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
            if (nodeMetaData != null && ((Boolean)nodeMetaData)) {
                continue;
            }
            // TODO: allow constructors which only call provided constructor?
            addError("Explicit constructors not allowed for " + MY_TYPE_NAME + " class: " + cNode.getNameWithoutPackage(), constructorNode);
            return false;
        }
        return true;
    }

    private Statement createConstructorStatement(ClassNode cNode, PropertyNode pNode, List<String> knownImmutableClasses, List<String> knownImmutables) {
        FieldNode fNode = pNode.getField();
        final ClassNode fieldType = fNode.getType();
        Statement statement = null;
        if (fieldType.isArray() || isOrImplements(fieldType, CLONEABLE_TYPE)) {
            statement = createConstructorStatementArrayOrCloneable(fNode);
        } else if (isKnownImmutableClass(fieldType, knownImmutableClasses) || isKnownImmutable(pNode.getName(), knownImmutables)) {
            statement = createConstructorStatementDefault(fNode);
        } else if (fieldType.isDerivedFrom(DATE_TYPE)) {
            statement = createConstructorStatementDate(fNode);
        } else if (isOrImplements(fieldType, COLLECTION_TYPE) || fieldType.isDerivedFrom(COLLECTION_TYPE) || isOrImplements(fieldType, MAP_TYPE) || fieldType.isDerivedFrom(MAP_TYPE)) {
            statement = createConstructorStatementCollection(fNode);
        } else if (fieldType.isResolved()) {
            addError(createErrorMessage(cNode.getName(), fNode.getName(), fieldType.getName(), "compiling"), fNode);
            statement = EmptyStatement.INSTANCE;
        } else {
            statement = createConstructorStatementGuarded(cNode, fNode);
        }
        return statement;
    }

    private Statement createConstructorStatementGuarded(ClassNode cNode, FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, checkUnresolved(fNode, initExpr));
        }
        Expression unknown = findArg(fNode.getName());
        return ifElseS(equalsNullX(unknown), assignInit, assignS(fieldExpr, checkUnresolved(fNode, unknown)));
    }

    private Expression checkUnresolved(FieldNode fNode, Expression value) {
        Expression args = args(callThisX("getClass"), constX(fNode.getName()), value);
        return callX(SELF_TYPE, "checkImmutable", args);
    }

    private Statement createConstructorStatementCollection(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        ClassNode fieldType = fieldExpr.getType();
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneCollectionExpr(initExpr, fieldType));
        }
        Expression collection = findArg(fNode.getName());
        return ifElseS(
                equalsNullX(collection),
                assignInit,
                ifElseS(
                        isInstanceOfX(collection, CLONEABLE_TYPE),
                        assignS(fieldExpr, cloneCollectionExpr(cloneArrayOrCloneableExpr(collection, fieldType), fieldType)),
                        assignS(fieldExpr, cloneCollectionExpr(collection, fieldType)))
        );
    }

    private boolean isKnownImmutableClass(ClassNode fieldType, List<String> knownImmutableClasses) {
        if (inImmutableList(fieldType.getName()) || knownImmutableClasses.contains(fieldType.getName()))
            return true;
        if (!fieldType.isResolved())
            return false;
        return fieldType.isEnum() ||
                ClassHelper.isPrimitiveType(fieldType) ||
                !fieldType.getAnnotations(MY_TYPE).isEmpty();
    }

    private boolean isKnownImmutable(String fieldName, List<String> knownImmutables) {
        return knownImmutables.contains(fieldName);
    }

    private static boolean inImmutableList(String typeName) {
        return immutableList.contains(typeName);
    }

    private Statement createConstructorStatementArrayOrCloneable(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        Expression initExpr = fNode.getInitialValueExpression();
        ClassNode fieldType = fNode.getType();
        final Expression array = findArg(fNode.getName());
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneArrayOrCloneableExpr(initExpr, fieldType));
        }
        return ifElseS(equalsNullX(array), assignInit, assignS(fieldExpr, cloneArrayOrCloneableExpr(array, fieldType)));
    }

    private Statement createConstructorStatementDate(FieldNode fNode) {
        final Expression fieldExpr = varX(fNode);
        Expression initExpr = fNode.getInitialValueExpression();
        final Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
        } else {
            assignInit = assignS(fieldExpr, cloneDateExpr(initExpr));
        }
        final Expression date = findArg(fNode.getName());
        return ifElseS(equalsNullX(date), assignInit, assignS(fieldExpr, cloneDateExpr(date)));
    }

    private Expression cloneDateExpr(Expression origDate) {
        return ctorX(DATE_TYPE, callX(origDate, "getTime"));
    }

    private void adjustPropertyForImmutability(PropertyNode pNode, List<PropertyNode> newNodes) {
        final FieldNode fNode = pNode.getField();
        fNode.setModifiers((pNode.getModifiers() & (~ACC_PUBLIC)) | ACC_FINAL | ACC_PRIVATE);
        adjustPropertyNode(pNode, createGetterBody(fNode));
        newNodes.add(pNode);
    }

    private void adjustPropertyNode(PropertyNode pNode, Statement getterBody) {
        pNode.setSetterBlock(null);
        pNode.setGetterBlock(getterBody);
    }

    private Statement createGetterBody(FieldNode fNode) {
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
        return MY_TYPE_NAME + " processor doesn't know how to handle field '" + fieldName + "' of type '" +
                prettyTypeName(typeName) + "' while " + mode + " class " + className + ".\n" +
                MY_TYPE_NAME + " classes only support properties with effectively immutable types including:\n" +
                "- Strings, primitive types, wrapper types, Class, BigInteger and BigDecimal, enums\n" +
                "- other " + MY_TYPE_NAME + " classes and known immutables (java.awt.Color, java.net.URI)\n" +
                "- Cloneable classes, collections, maps and arrays, and other classes with special handling (java.util.Date)\n" +
                "Other restrictions apply, please see the groovydoc for " + MY_TYPE_NAME + " for further details";
    }

    private static String prettyTypeName(String name) {
        return name.equals("java.lang.Object") ? name + " or def" : name;
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

    private Statement createCheckForProperty( final PropertyNode pNode ) {
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

    private void createCopyWith(final ClassNode cNode, final List<PropertyNode> pList) {
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
        if (field == null || field instanceof Enum || inImmutableList(field.getClass().getName())) return field;
        if (field instanceof Collection) return DefaultGroovyMethods.asImmutable((Collection) field);
        if (field.getClass().getAnnotation(MY_CLASS) != null) return field;
        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(className, fieldName, typeName, "constructing"));
    }

    @SuppressWarnings("Unchecked")
    public static Object checkImmutable(Class<?> clazz, String fieldName, Object field) {
        Immutable immutable = (Immutable) clazz.getAnnotation(MY_CLASS);
        List<Class> knownImmutableClasses = new ArrayList<Class>();
        if (immutable != null && immutable.knownImmutableClasses().length > 0) {
            knownImmutableClasses = Arrays.asList(immutable.knownImmutableClasses());
        }

        if (field == null || field instanceof Enum || inImmutableList(field.getClass().getName()) || knownImmutableClasses.contains(field.getClass()))
            return field;
        if (field.getClass().getAnnotation(MY_CLASS) != null) return field;
        if (field instanceof Collection) {
            Field declaredField;
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                Class<?> fieldType = declaredField.getType();
                if (Collection.class.isAssignableFrom(fieldType)) {
                    return DefaultGroovyMethods.asImmutable((Collection) field);
                }
                // potentially allow Collection coercion for a constructor
                if (fieldType.getAnnotation(MY_CLASS) != null) return field;
                if (inImmutableList(fieldType.getName()) || knownImmutableClasses.contains(fieldType)) {
                    return field;
                }
            } catch (NoSuchFieldException ignore) { }
        }
        final String typeName = field.getClass().getName();
        throw new RuntimeException(createErrorMessage(clazz.getName(), fieldName, typeName, "constructing"));
    }

    public static void checkPropNames(Object instance, Map<String, Object> args) {
        final MetaClass metaClass = InvokerHelper.getMetaClass(instance);
        for (String k : args.keySet()) {
            if (metaClass.hasProperty(instance, k) == null)
                throw new MissingPropertyException(k, instance.getClass());
        }
    }
}
